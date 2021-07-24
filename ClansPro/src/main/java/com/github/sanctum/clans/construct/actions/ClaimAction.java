package com.github.sanctum.clans.construct.actions;

import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.cooldown.CooldownClaim;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.data.DataManager;
import com.github.sanctum.clans.util.events.clans.LandClaimedEvent;
import com.github.sanctum.clans.util.events.clans.LandPreClaimEvent;
import com.github.sanctum.clans.util.events.clans.LandUnClaimEvent;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.formatting.string.RandomID;
import com.github.sanctum.link.ClanVentBus;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class ClaimAction extends StringLibrary {

	public void claim(Player p) {
		LandPreClaimEvent event1 = ClanVentBus.call(new LandPreClaimEvent(p));
		if (event1.isCancelled()) {
			return;
		}
		Claim claim = Claim.from(p.getLocation());
		ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (associate == null) {
			return;
		}

		if (!associate.isValid()) {
			return;
		}

		boolean claimEffect = ClansAPI.getData().getEnabled("Clans.land-claiming.claim-influence.allow");
		if (claim == null) {
			Clan clan = associate.getClan();
			if (!claimEffect) {
				if (clan.getOwnedClaimsList().length == claimHardcap(p)) {
					sendMessage(p, alreadyMaxClaims());
					return;
				}
			}
			if (claimEffect && clan.getOwnedClaimsList().length >= clan.getMaxClaims()) {
				sendMessage(p, ClansAPI.getData().getMessage("clan-max-claims"));
				return;
			}
			if (clan.getOwnedClaimsList().length == claimHardcap(p)) {
				sendMessage(p, ClansAPI.getData().getMessage("hard-max-claims"));
				return;
			}
			int chunkCount = 0;
			for (Chunk chunk : getChunksAroundLocation(p.getLocation(), -1, 0, 1).stream().filter(c -> ClansAPI.getInstance().getClaimManager().isInClaim(c.getX(), c.getZ(), c.getWorld().getName())).collect(Collectors.toList())) {
				if (clan.isOwner(chunk)) {
					chunkCount++;
				}
			}
			if (ClansAPI.getData().getEnabled("Clans.land-claiming.claim-connections")) {
				if (clan.getOwnedClaimsList().length >= 1 && chunkCount == 0) {
					sendMessage(p, ClansAPI.getData().getMessage("claim-not-connected"));
					return;
				}
			}
			String claimID = new RandomID(6, "AKZ0123456789").generate();
			int x = p.getLocation().getChunk().getX();
			int z = p.getLocation().getChunk().getZ();
			String world = p.getWorld().getName();
			FileManager d = ClansAPI.getInstance().getClaimManager().getFile();
			d.getConfig().set(clan.getId().toString() + ".Claims." + claimID + ".X", x);
			d.getConfig().set(clan.getId().toString() + ".Claims." + claimID + ".Z", z);
			d.getConfig().set(clan.getId().toString() + ".Claims." + claimID + ".World", world);
			d.saveConfig();
			clan.broadcast(claimed(x, z, world));
			chunkBorderHint(p);
			ClanVentBus.call(new LandClaimedEvent(p, ClansAPI.getInstance().getClaimManager().getClaim(claimID)));
		} else {
			if (claim.isActive()) {
				if (claim.getOwner().equals(associate.getClan().getId().toString())) {
					sendMessage(p, alreadyOwnClaim());
				} else {
					sendMessage(p, notClaimOwner(DefaultClan.action.clanRelationColor(associate.getClan().getId().toString(), claim.getOwner()) + DefaultClan.action.getClanTag(claim.getOwner())));

				}
			}
		}
	}

	public void unclaim(Player p) {
		FileManager d = ClansAPI.getInstance().getClaimManager().getFile();
		Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
		if (ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
			Claim claim = Claim.from(p.getLocation());
			assert claim != null;
			if (!claim.isActive()) {
				return;
			}
			LandUnClaimEvent event = ClanVentBus.call(new LandUnClaimEvent(p, claim));
			if (Arrays.stream(Claim.from(p.getLocation()).getClan().getMembersList()).anyMatch(s -> s.equals(p.getUniqueId().toString()))) {
				if (!event.isCancelled()) {
					d.getConfig().set(clan.getId().toString() + ".Claims." + getClaimID(p.getLocation()), null);
					d.saveConfig();
					int x = p.getLocation().getChunk().getX();
					int z = p.getLocation().getChunk().getZ();
					String world = p.getWorld().getName();
					clan.broadcast(unclaimed(x, z, world));
				}
			} else {
				if (ClansAPI.getInstance().getShieldManager().isEnabled()) {
					if (DefaultClan.action.overPowerBypass()) {
						Clan owner = claim.getClan();
						if (clan.getPower() > owner.getPower() && !clan.equals(owner)) {
							if (clan.isPeaceful()) {
								sendMessage(p, peacefulDeny());
								return;
							}
							if (owner.isPeaceful()) {
								sendMessage(p, peacefulDenyOther(owner.getName()));
								return;
							}
							if (!event.isCancelled()) {
								if (ClansAPI.getData().getMain().getConfig().getBoolean("Clans.raid-shield.claiming-only-enemy")) {
									if (!clan.getEnemyList().contains(owner.getId().toString())) {
										sendMessage(p, ClansAPI.getData().getMessage("clan-neutral"));
										return;
									}
								}
								for (Chunk chunk : getChunksAroundLocation(claim.getClan().getBase(), -1, 0, 1).stream().filter(c -> ClansAPI.getInstance().getClaimManager().isInClaim(c.getX(), c.getZ(), c.getWorld().getName())).collect(Collectors.toList())) {
									if (chunk.equals(p.getLocation().getChunk())) {
										sendMessage(p, MessageFormat.format(ClansAPI.getData().getMessage("safe-zone"), claim.getClan().getName()));
										return;
									}
								}
								if (unClaimCooldown(clan).isComplete()) {
									FileManager cFile = ClansAPI.getData().getClanFile(clan);
									int i = cFile.getConfig().getInt("over-powered");

									if (i <= ClansAPI.getData().getInt("Clans.land-claiming.over-powering.cooldown.after-uses")) {
										i += 1;
										cFile.getConfig().set("over-powered", i);
										cFile.saveConfig();
									} else {
										cFile.getConfig().set("over-powered", 0);
										cFile.saveConfig();
										unClaimCooldown(clan).save();
										unClaimCooldown(clan).setCooldown();
										sendMessage(p, ClansAPI.getData().getMessage("un-claim-cooldown"));
									}
								} else {
									sendMessage(p, unClaimCooldown(clan).fullTimeLeft());
									return;
								}
								d.getConfig().set(claim.getOwner() + ".Claims." + claim.getId(), null);
								d.saveConfig();
								int x = p.getLocation().getChunk().getX();
								int z = p.getLocation().getChunk().getZ();
								String world = p.getWorld().getName();
								owner.broadcast(breach(clan.getName()));
								owner.broadcast(overpowered(x, z, world));
							}
						} else {
							sendMessage(p, tooWeak());
						}
					} else {
						sendMessage(p, shieldDeny());
					}
				} else {
					Clan owner = claim.getClan();
					if (clan.getPower() > owner.getPower() && !clan.equals(owner)) {
						if (clan.isPeaceful()) {
							sendMessage(p, peacefulDeny());
							return;
						}
						if (owner.isPeaceful()) {
							sendMessage(p, peacefulDenyOther(owner.getName()));
							return;
						}
						if (!event.isCancelled()) {
							if (ClansAPI.getData().getMain().getConfig().getBoolean("Clans.raid-shield.claiming-only-enemy")) {
								if (!clan.getEnemyList().contains(owner.getId().toString())) {
									sendMessage(p, ClansAPI.getData().getMessage("clan-neutral"));
									return;
								}
							}

							for (Chunk chunk : getChunksAroundLocation(claim.getClan().getBase(), -1, 0, 1).stream().filter(c -> ClansAPI.getInstance().getClaimManager().isInClaim(c.getX(), c.getZ(), c.getWorld().getName())).collect(Collectors.toList())) {
								if (chunk.equals(p.getLocation().getChunk())) {
									sendMessage(p, MessageFormat.format(ClansAPI.getData().getMessage("safe-zone"), claim.getClan().getName()));
									return;
								}
							}
							if (unClaimCooldown(clan).isComplete()) {
								FileManager cFile = ClansAPI.getData().getClanFile(clan);
								int i = cFile.getConfig().getInt("over-powered");

								if (i <= ClansAPI.getData().getInt("Clans.land-claiming.over-powering.cooldown.after-uses")) {
									i += 1;
									cFile.getConfig().set("over-powered", i);
									cFile.saveConfig();
								} else {
									cFile.getConfig().set("over-powered", 0);
									cFile.saveConfig();
									unClaimCooldown(clan).save();
									unClaimCooldown(clan).setCooldown();
									sendMessage(p, ClansAPI.getData().getMessage("un-claim-cooldown"));
								}
							} else {
								sendMessage(p, unClaimCooldown(clan).fullTimeLeft());
								return;
							}
							d.getConfig().set(claim.getOwner() + ".Claims." + getClaimID(p.getLocation()), null);
							d.saveConfig();
							int x = p.getLocation().getChunk().getX();
							int z = p.getLocation().getChunk().getZ();
							String world = p.getWorld().getName();
							owner.broadcast(higherpower(DefaultClan.action.getClanTag(DefaultClan.action.getClanID(p.getUniqueId()))));
							owner.broadcast(overpowered(x, z, world));
						}
					} else {
						sendMessage(p, tooWeak());
					}
				}
			}
		} else {
			sendMessage(p, alreadyWild());
		}
	}

	public void unclaimAll(Player p) {
		FileManager d = ClansAPI.getInstance().getClaimManager().getFile();
		if (!d.getConfig().isConfigurationSection(DefaultClan.action.getClanID(p.getUniqueId()) + ".Claims")) {
			sendMessage(p, noClaims());
			return;
		}
		if (!Objects.requireNonNull(d.getConfig().getConfigurationSection(DefaultClan.action.getClanID(p.getUniqueId()) + ".Claims")).getKeys(false).isEmpty()) {
			d.getConfig().set(DefaultClan.action.getClanID(p.getUniqueId()) + ".Claims", null);
			d.getConfig().createSection(DefaultClan.action.getClanID(p.getUniqueId()) + ".Claims");
			d.saveConfig();
			Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
			clan.broadcast(unclaimedAll(p.getName()));

		} else {
			sendMessage(p, noClaims());
		}
	}

	public synchronized Collection<Chunk> getChunksAroundLocation(Location location, int xoff, int yoff, int zoff) {
		int[] offset = {xoff, yoff, zoff};

		World world = location.getWorld();
		if (world == null) return Collections.emptyList();
		int baseX = location.getChunk().getX();
		int baseZ = location.getChunk().getZ();

		Collection<Chunk> chunksAroundPlayer = new HashSet<>();
		for (int x : offset) {
			for (int z : offset) {
				Chunk chunk = world.getChunkAt(baseX + x, baseZ + z);
				if (!chunk.isLoaded()) {
					chunk.load();
				}
				chunksAroundPlayer.add(chunk);
			}
		}
		return chunksAroundPlayer;
	}

	public Collection<Chunk> getChunksAroundChunk(Chunk chunk, int xoff, int yoff, int zoff) {
		int[] offset = {xoff, yoff, zoff};

		World world = chunk.getWorld();
		int baseX = chunk.getX();
		int baseZ = chunk.getZ();

		Collection<Chunk> chunksAroundPlayer = new HashSet<>();
		for (int x : offset) {
			for (int z : offset) {
				Chunk c = world.getChunkAt(baseX + x, baseZ + z);
				chunksAroundPlayer.add(c);
			}
		}
		return chunksAroundPlayer;
	}

	public ClanCooldown unClaimCooldown(Clan clan) {
		ClanCooldown target = null;
		for (ClanCooldown c : clan.getCooldowns()) {
			if (c.getAction().equals("Clans:unclaim-limit")) {
				target = c;
			}
		}
		if (target == null) {
			target = new CooldownClaim(clan.getId().toString());
			if (!ClansPro.getInstance().dataManager.COOLDOWNS.contains(target)) {
				target.save();
			}
		}
		return target;
	}

	public boolean hasSurface(Location location) {
		Block feet = location.getBlock();
		if (!feet.getType().equals(Material.AIR) && !feet.getLocation().add(0.0D, 1.0D, 0.0D).getBlock().getType().equals(Material.AIR))
			return false;
		Block head = feet.getRelative(BlockFace.UP);
		if (!head.getType().equals(Material.AIR))
			return false;
		Block ground = feet.getRelative(BlockFace.DOWN);
		return ground.getType().isSolid();
	}

	public int claimHardcap(Player player) {
		int result = 0;
		if (player == null)
			return 0;
		for (int i = 1; i < 251; i++) {
			if (player.hasPermission("clanspro." + DataManager.Security.getPermission("claim") + ".infinite")) {
				result = -1;
				break;
			}
			if (player.hasPermission("clanspro." + DataManager.Security.getPermission("claim") + "." + i)) {
				result = i;
				break;
			}
		}
		if (result == -1)
			return 99999;

		return result;
	}

	public String getClaimID(Location loc) {
		if (ClansAPI.getInstance().getClaimManager().isInClaim(loc)) {
			return ClansAPI.getInstance().getClaimManager().getId(loc);
		}
		return null;
	}

	public boolean isEnabled() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getBoolean("Clans.land-claiming.allow");
	}


}
