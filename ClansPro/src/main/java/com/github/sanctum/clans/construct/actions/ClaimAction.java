package com.github.sanctum.clans.construct.actions;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.clans.construct.impl.CooldownClaim;
import com.github.sanctum.clans.events.core.LandClaimedEvent;
import com.github.sanctum.clans.events.core.LandPreClaimEvent;
import com.github.sanctum.clans.events.core.LandUnClaimEvent;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.formatting.string.RandomID;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class ClaimAction extends StringLibrary {

	public boolean claim(Player p) {
		LandPreClaimEvent event1 = ClanVentBus.call(new LandPreClaimEvent(p));
		if (event1.isCancelled()) {
			return false;
		}
		Claim claim = Claim.from(p.getLocation());
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (associate == null) {
			return false;
		}

		if (!associate.isValid()) {
			return false;
		}

		boolean claimEffect = ClansAPI.getData().isTrue("Clans.land-claiming.claim-influence.allow");
		if (claim == null) {
			Clan clan = associate.getClan();
			if (!claimEffect) {
				if (clan.getOwnedClaimsList().length == claimHardcap(p)) {
					sendMessage(p, alreadyMaxClaims());
					return false;
				}
			}
			if (claimEffect && clan.getOwnedClaimsList().length >= clan.getMaxClaims()) {
				sendMessage(p, ClansAPI.getData().getMessageResponse("clan-max-claims"));
				return false;
			}
			if (clan.getOwnedClaimsList().length == claimHardcap(p)) {
				sendMessage(p, ClansAPI.getData().getMessageResponse("hard-max-claims"));
				return false;
			}
			int chunkCount = 0;
			for (Chunk chunk : getChunksAroundLocation(p.getLocation(), -1, 0, 1).stream().filter(c -> ClansAPI.getInstance().getClaimManager().isInClaim(c.getX(), c.getZ(), c.getWorld().getName())).collect(Collectors.toList())) {
				if (clan.isOwner(chunk)) {
					chunkCount++;
				}
			}
			if (ClansAPI.getData().isTrue("Clans.land-claiming.claim-connections")) {
				if (clan.getOwnedClaimsList().length >= 1 && chunkCount == 0) {
					sendMessage(p, ClansAPI.getData().getMessageResponse("claim-not-connected"));
					return false;
				}
			}
			String claimID = new RandomID(6, "AKZ0123456789").generate();
			int x = p.getLocation().getChunk().getX();
			int z = p.getLocation().getChunk().getZ();
			String world = p.getWorld().getName();
			FileManager d = ClansAPI.getInstance().getClaimManager().getFile();
			d.write(t -> {
				t.set(clan.getId().toString() + ".Claims." + claimID + ".X", x);
				t.set(clan.getId().toString() + ".Claims." + claimID + ".Z", z);
				t.set(clan.getId().toString() + ".Claims." + claimID + ".World", world);
			});
			Claim cl = new Claim(x, z, clan.getId().toString(), claimID, world, true);
			ClansAPI.getInstance().getClaimManager().load(cl);
			clan.broadcast(claimed(x, z, world));
			chunkBorderHint(p);
			ClanVentBus.call(new LandClaimedEvent(p, cl));
		} else {
			if (claim.isActive()) {
				if (claim.getOwner().equals(associate.getClan().getId().toString())) {
					sendMessage(p, alreadyOwnClaim());
				} else {
					sendMessage(p, notClaimOwner(Clan.ACTION.getRelationColor(associate.getClan().getId().toString(), claim.getOwner()) + ClansAPI.getInstance().getClanName(claim.getOwner())));

				}
				return false;
			}
		}
		return true;
	}

	public boolean claim(Player p, Chunk ch) {
		LandPreClaimEvent event1 = ClanVentBus.call(new LandPreClaimEvent(p));
		if (event1.isCancelled()) {
			return false;
		}
		Claim claim = Claim.from(ch);
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (associate == null) {
			return false;
		}

		if (!associate.isValid()) {
			return false;
		}

		boolean claimEffect = ClansAPI.getData().isTrue("Clans.land-claiming.claim-influence.allow");
		if (claim == null) {
			Clan clan = associate.getClan();
			if (!claimEffect) {
				if (clan.getOwnedClaimsList().length == claimHardcap(p)) {
					sendMessage(p, alreadyMaxClaims());
					return false;
				}
			}
			if (claimEffect && clan.getOwnedClaimsList().length >= clan.getMaxClaims()) {
				sendMessage(p, ClansAPI.getData().getMessageResponse("clan-max-claims"));
				return false;
			}
			if (clan.getOwnedClaimsList().length == claimHardcap(p)) {
				sendMessage(p, ClansAPI.getData().getMessageResponse("hard-max-claims"));
				return false;
			}
			int chunkCount = 0;
			for (Chunk chunk : getChunksAroundChunk(ch, -1, 0, 1).stream().filter(c -> ClansAPI.getInstance().getClaimManager().isInClaim(c.getX(), c.getZ(), c.getWorld().getName())).collect(Collectors.toList())) {
				if (clan.isOwner(chunk)) {
					chunkCount++;
				}
			}
			if (ClansAPI.getData().isTrue("Clans.land-claiming.claim-connections")) {
				if (clan.getOwnedClaimsList().length >= 1 && chunkCount == 0) {
					sendMessage(p, ClansAPI.getData().getMessageResponse("claim-not-connected"));
					return false;
				}
			}
			String claimID = new RandomID(6, "AKZ0123456789").generate();
			int x = ch.getX();
			int z = ch.getZ();
			String world = ch.getWorld().getName();
			FileManager d = ClansAPI.getInstance().getClaimManager().getFile();
			d.write(t -> {
				t.set(clan.getId().toString() + ".Claims." + claimID + ".X", x);
				t.set(clan.getId().toString() + ".Claims." + claimID + ".Z", z);
				t.set(clan.getId().toString() + ".Claims." + claimID + ".World", world);
			});
			clan.broadcast(claimed(x, z, world));
			chunkBorderHint(p);
			Claim cl = new Claim(x, z, clan.getId().toString(), claimID, world, true);
			ClansAPI.getInstance().getClaimManager().load(cl);
			ClanVentBus.call(new LandClaimedEvent(p, cl));
		} else {
			if (claim.isActive()) {
				if (claim.getOwner().equals(associate.getClan().getId().toString())) {
					sendMessage(p, alreadyOwnClaim());
				} else {
					sendMessage(p, notClaimOwner(Clan.ACTION.getRelationColor(associate.getClan().getId().toString(), claim.getOwner()) + ClansAPI.getInstance().getClanName(claim.getOwner())));

				}
				return false;
			}
		}
		return true;
	}

	public boolean unclaim(Player p) {
		Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
		if (ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
			Claim claim = Claim.from(p.getLocation());
			assert claim != null;
			if (!claim.isActive()) {
				return false;
			}
			LandUnClaimEvent event = ClanVentBus.call(new LandUnClaimEvent(p, claim));
			if (claim.getClan().getMembers().stream().anyMatch(a -> p.getName().equals(a.getPlayer().getName()))) {
				if (!event.isCancelled()) {

					if (ClansAPI.getInstance().getAssociate(p).get().getPriority().toInt() < Clan.ACTION.claimingClearance()) {
						sendMessage(p, noClearance());
						return false;
					}
					claim.remove();
					int x = p.getLocation().getChunk().getX();
					int z = p.getLocation().getChunk().getZ();
					String world = p.getWorld().getName();
					clan.broadcast(unclaimed(x, z, world));
					return true;
				}
				return false;
			} else {
				if (ClansAPI.getInstance().getShieldManager().isEnabled()) {
					if (Clan.ACTION.overPowerBypass()) {
						Clan owner = claim.getClan();
						if (clan.getPower() > owner.getPower() && !clan.equals(owner)) {
							if (clan.isPeaceful()) {
								sendMessage(p, peacefulDeny());
								return false;
							}
							if (owner.isPeaceful()) {
								sendMessage(p, peacefulDenyOther(owner.getName()));
								return false;
							}
							if (!event.isCancelled()) {
								if (ClansAPI.getData().getMain().getRoot().getBoolean("Clans.raid-shield.claiming-only-enemy")) {
									if (!clan.getEnemyList().contains(owner.getId().toString())) {
										sendMessage(p, ClansAPI.getData().getMessageResponse("clan-neutral"));
										return false;
									}
								}
								for (Chunk chunk : getChunksAroundLocation(claim.getClan().getBase(), -1, 0, 1).stream().filter(c -> ClansAPI.getInstance().getClaimManager().isInClaim(c.getX(), c.getZ(), c.getWorld().getName())).collect(Collectors.toList())) {
									if (chunk.equals(p.getLocation().getChunk())) {
										sendMessage(p, MessageFormat.format(ClansAPI.getData().getMessageResponse("safe-zone"), claim.getClan().getName()));
										return false;
									}
								}
								if (unClaimCooldown(clan).isComplete()) {
									FileManager cFile = ClansAPI.getData().getClanFile(clan);
									int i = cFile.getRoot().getInt("over-powered");

									if (i <= ClansAPI.getData().getInt("Clans.land-claiming.over-powering.cooldown.after-uses")) {
										i += 1;
										int finalI = i;
										cFile.write(f -> f.set("over-powered", finalI));
									} else {
										cFile.write(f -> f.set("over-powered", 0));
										unClaimCooldown(clan).save();
										unClaimCooldown(clan).setCooldown();
										sendMessage(p, ClansAPI.getData().getMessageResponse("un-claim-cooldown"));
									}
								} else {
									sendMessage(p, unClaimCooldown(clan).fullTimeLeft());
									return false;
								}
								claim.remove();
								int x = p.getLocation().getChunk().getX();
								int z = p.getLocation().getChunk().getZ();
								String world = p.getWorld().getName();
								owner.broadcast(breach(clan.getName()));
								owner.broadcast(overpowered(x, z, world));
								return true;
							}
						} else {
							sendMessage(p, tooWeak());
							return false;
						}
					} else {
						sendMessage(p, shieldDeny());
						return false;
					}
				} else {
					Clan owner = claim.getClan();
					if (clan.getPower() > owner.getPower() && !clan.equals(owner)) {
						if (clan.isPeaceful()) {
							sendMessage(p, peacefulDeny());
							return false;
						}
						if (owner.isPeaceful()) {
							sendMessage(p, peacefulDenyOther(owner.getName()));
							return false;
						}
						if (!event.isCancelled()) {
							if (ClansAPI.getData().getMain().getRoot().getBoolean("Clans.raid-shield.claiming-only-enemy")) {
								if (!clan.getEnemyList().contains(owner.getId().toString())) {
									sendMessage(p, ClansAPI.getData().getMessageResponse("clan-neutral"));
									return false;
								}
							}

							for (Chunk chunk : getChunksAroundLocation(claim.getClan().getBase(), -1, 0, 1).stream().filter(c -> ClansAPI.getInstance().getClaimManager().isInClaim(c.getX(), c.getZ(), c.getWorld().getName())).collect(Collectors.toList())) {
								if (chunk.equals(p.getLocation().getChunk())) {
									sendMessage(p, MessageFormat.format(ClansAPI.getData().getMessageResponse("safe-zone"), claim.getClan().getName()));
									return false;
								}
							}
							if (unClaimCooldown(clan).isComplete()) {
								FileManager cFile = ClansAPI.getData().getClanFile(clan);
								int i = cFile.getRoot().getInt("over-powered");

								if (i <= ClansAPI.getData().getInt("Clans.land-claiming.over-powering.cooldown.after-uses")) {
									i += 1;
									int finalI = i;
									cFile.write(f -> f.set("over-powered", finalI));
								} else {
									cFile.write(f -> f.set("over-powered", 0));
									unClaimCooldown(clan).save();
									unClaimCooldown(clan).setCooldown();
									sendMessage(p, ClansAPI.getData().getMessageResponse("un-claim-cooldown"));
								}
							} else {
								sendMessage(p, unClaimCooldown(clan).fullTimeLeft());
								return false;
							}
							claim.remove();
							int x = p.getLocation().getChunk().getX();
							int z = p.getLocation().getChunk().getZ();
							String world = p.getWorld().getName();
							owner.broadcast(higherpower(ClansAPI.getInstance().getClanName(ClansAPI.getInstance().getClanID(p.getUniqueId()).toString())));
							owner.broadcast(overpowered(x, z, world));
						}
					} else {
						sendMessage(p, tooWeak());
						return false;
					}
				}
			}
		} else {
			sendMessage(p, alreadyWild());
			return false;
		}
		return true;
	}

	public boolean unclaim(Player p, Chunk ch) {
		Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
		if (ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
			Claim claim = Claim.from(ch);
			assert claim != null;
			if (!claim.isActive()) {
				return false;
			}
			LandUnClaimEvent event = ClanVentBus.call(new LandUnClaimEvent(p, claim));
			if (claim.getClan().getMembers().stream().anyMatch(a -> p.getName().equals(a.getPlayer().getName()))) {
				if (!event.isCancelled()) {

					if (ClansAPI.getInstance().getAssociate(p).get().getPriority().toInt() < Clan.ACTION.claimingClearance()) {
						sendMessage(p, noClearance());
						return false;
					}
					claim.remove();
					int x = ch.getX();
					int z = ch.getZ();
					String world = ch.getWorld().getName();
					clan.broadcast(unclaimed(x, z, world));

					return true;
				}
				return false;
			} else {
				if (ClansAPI.getInstance().getShieldManager().isEnabled()) {
					if (Clan.ACTION.overPowerBypass()) {
						Clan owner = claim.getClan();
						if (clan.getPower() > owner.getPower() && !clan.equals(owner)) {
							if (clan.isPeaceful()) {
								sendMessage(p, peacefulDeny());
								return false;
							}
							if (owner.isPeaceful()) {
								sendMessage(p, peacefulDenyOther(owner.getName()));
								return false;
							}
							if (!event.isCancelled()) {
								if (ClansAPI.getData().getMain().getRoot().getBoolean("Clans.raid-shield.claiming-only-enemy")) {
									if (!clan.getEnemyList().contains(owner.getId().toString())) {
										sendMessage(p, ClansAPI.getData().getMessageResponse("clan-neutral"));
										return false;
									}
								}
								for (Chunk chunk : getChunksAroundLocation(claim.getClan().getBase(), -1, 0, 1).stream().filter(c -> ClansAPI.getInstance().getClaimManager().isInClaim(c.getX(), c.getZ(), c.getWorld().getName())).collect(Collectors.toList())) {
									if (chunk.equals(p.getLocation().getChunk())) {
										sendMessage(p, MessageFormat.format(ClansAPI.getData().getMessageResponse("safe-zone"), claim.getClan().getName()));
										return false;
									}
								}
								if (unClaimCooldown(clan).isComplete()) {
									FileManager cFile = ClansAPI.getData().getClanFile(clan);
									int i = cFile.getRoot().getInt("over-powered");

									if (i <= ClansAPI.getData().getInt("Clans.land-claiming.over-powering.cooldown.after-uses")) {
										i += 1;
										int finalI = i;
										cFile.write(f -> f.set("over-powered", finalI));
									} else {
										cFile.write(f -> f.set("over-powered", 0));
										unClaimCooldown(clan).save();
										unClaimCooldown(clan).setCooldown();
										sendMessage(p, ClansAPI.getData().getMessageResponse("un-claim-cooldown"));
									}
								} else {
									sendMessage(p, unClaimCooldown(clan).fullTimeLeft());
									return false;
								}
								claim.remove();
								int x = p.getLocation().getChunk().getX();
								int z = p.getLocation().getChunk().getZ();
								String world = p.getWorld().getName();
								owner.broadcast(breach(clan.getName()));
								owner.broadcast(overpowered(x, z, world));
								return true;
							}
						} else {
							sendMessage(p, tooWeak());
							return false;
						}
					} else {
						sendMessage(p, shieldDeny());
						return false;
					}
				} else {
					Clan owner = claim.getClan();
					if (clan.getPower() > owner.getPower() && !clan.equals(owner)) {
						if (clan.isPeaceful()) {
							sendMessage(p, peacefulDeny());
							return false;
						}
						if (owner.isPeaceful()) {
							sendMessage(p, peacefulDenyOther(owner.getName()));
							return false;
						}
						if (!event.isCancelled()) {
							if (ClansAPI.getData().getMain().getRoot().getBoolean("Clans.raid-shield.claiming-only-enemy")) {
								if (!clan.getEnemyList().contains(owner.getId().toString())) {
									sendMessage(p, ClansAPI.getData().getMessageResponse("clan-neutral"));
									return false;
								}
							}

							for (Chunk chunk : getChunksAroundLocation(claim.getClan().getBase(), -1, 0, 1).stream().filter(c -> ClansAPI.getInstance().getClaimManager().isInClaim(c.getX(), c.getZ(), c.getWorld().getName())).collect(Collectors.toList())) {
								if (chunk.equals(p.getLocation().getChunk())) {
									sendMessage(p, MessageFormat.format(ClansAPI.getData().getMessageResponse("safe-zone"), claim.getClan().getName()));
									return false;
								}
							}
							if (unClaimCooldown(clan).isComplete()) {
								FileManager cFile = ClansAPI.getData().getClanFile(clan);
								int i = cFile.getRoot().getInt("over-powered");

								if (i <= ClansAPI.getData().getInt("Clans.land-claiming.over-powering.cooldown.after-uses")) {
									i += 1;
									int finalI = i;
									cFile.write(f -> f.set("over-powered", finalI));
								} else {
									cFile.write(f -> f.set("over-powered", 0));
									unClaimCooldown(clan).save();
									unClaimCooldown(clan).setCooldown();
									sendMessage(p, ClansAPI.getData().getMessageResponse("un-claim-cooldown"));
								}
							} else {
								sendMessage(p, unClaimCooldown(clan).fullTimeLeft());
								return false;
							}
							claim.remove();
							int x = p.getLocation().getChunk().getX();
							int z = p.getLocation().getChunk().getZ();
							String world = p.getWorld().getName();
							owner.broadcast(higherpower(ClansAPI.getInstance().getClanName(ClansAPI.getInstance().getClanID(p.getUniqueId()).toString())));
							owner.broadcast(overpowered(x, z, world));
						}
					} else {
						sendMessage(p, tooWeak());
						return false;
					}
				}
			}
		} else {
			sendMessage(p, alreadyWild());
			return false;
		}
		return true;
	}

	public boolean unclaimAll(Player p) {
		FileManager d = ClansAPI.getInstance().getClaimManager().getFile();
		Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
		if (!d.getRoot().isNode(clan.getId().toString() + ".Claims")) {
			sendMessage(p, noClaims());
			return false;
		}
		if (!d.getRoot().getNode(clan.getId().toString() + ".Claims").getKeys(false).isEmpty()) {
			for (Claim c : clan.getOwnedClaims()) {
				c.remove();
			}
			clan.broadcast(unclaimedAll(p.getName()));
			return true;
		} else {
			sendMessage(p, noClaims());
		}
		return false;
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
			if (!ClansAPI.getData().COOLDOWNS.contains(target)) {
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
		return main.getRoot().getBoolean("Clans.land-claiming.allow");
	}


}
