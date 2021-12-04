package com.github.sanctum.clans.construct.actions;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.clans.construct.impl.CooldownClaim;
import com.github.sanctum.clans.construct.impl.DefaultClaim;
import com.github.sanctum.clans.event.associate.AssociateClaimEvent;
import com.github.sanctum.clans.event.associate.AssociateObtainLandEvent;
import com.github.sanctum.clans.event.associate.AssociateUnClaimEvent;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.Node;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.formatting.PaginatedList;
import com.github.sanctum.labyrinth.formatting.string.RandomID;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.task.Schedule;
import com.google.common.base.Strings;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class ClaimAction extends StringLibrary {

	public boolean claim(Player p) {
		return claim(p, p.getLocation().getChunk());
	}

	public boolean claim(Player p, Chunk ch) {
		AssociateClaimEvent event1 = ClanVentBus.call(new AssociateClaimEvent(p));
		if (event1.isCancelled()) {
			return false;
		}
		Claim claim = ClansAPI.getInstance().getClaimManager().getClaim(ch);
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (associate == null) {
			return false;
		}

		if (!associate.isValid()) {
			return false;
		}

		boolean claimEffect = ClansAPI.getDataInstance().isTrue("Clans.land-claiming.claim-influence.allow");
		if (claim == null) {

			Clan clan = associate.getClan();
			if (!claimEffect) {
				if (clan.getClaims().length == claimHardcap(p)) {
					sendMessage(p, alreadyMaxClaims());
					return false;
				}
			}
			if (claimEffect && clan.getClaims().length >= clan.getClaimLimit()) {
				sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("clan-max-claims"));
				return false;
			}
			if (clan.getClaims().length == claimHardcap(p)) {
				sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("hard-max-claims"));
				return false;
			}
			int chunkCount = 0;
			for (Chunk chunk : getChunksAroundChunk(ch, -1, 0, 1).stream().filter(c -> ClansAPI.getInstance().getClaimManager().isInClaim(c.getX(), c.getZ(), c.getWorld().getName())).collect(Collectors.toList())) {
				if (clan.isOwner(chunk)) {
					chunkCount++;
				}
			}
			if (ClansAPI.getDataInstance().isTrue("Clans.land-claiming.claim-connections")) {
				if (clan.getClaims().length >= 1 && chunkCount == 0) {
					sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("claim-not-connected"));
					return false;
				}
			}
			String claimID = new RandomID(6, "AKZ0123456789").generate();
			int x = ch.getX();
			int z = ch.getZ();
			String world = ch.getWorld().getName();
			clan.broadcast(claimed(x, z, world));
			chunkBorderHint(p);
			Claim cl = new DefaultClaim(x, z, clan.getId().toString(), claimID, world, true);
			cl.save();
			ClansAPI.getInstance().getClaimManager().load(cl);
			ClanVentBus.call(new AssociateObtainLandEvent(p, cl));
		} else {
			if (claim.isActive()) {
				if (claim.getOwner().equals(associate.getClan())) {
					sendMessage(p, alreadyOwnClaim());
				} else {
					sendMessage(p, notClaimOwner(Clan.ACTION.getRelationColor(associate.getClan().getId().toString(), claim.getOwner().getTag().getId()) + ClansAPI.getInstance().getClanManager().getClanName(HUID.fromString(claim.getOwner().getTag().getId()))));

				}
				return false;
			}
		}
		return true;
	}

	public boolean unclaim(Player p) {
		return unclaim(p, p.getLocation().getChunk());
	}

	public boolean unclaim(Player p, Chunk ch) {
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (associate == null) return false;
		Clan clan = associate.getClan();
		if (ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
			Claim claim = ClansAPI.getInstance().getClaimManager().getClaim(ch);
			assert claim != null;
			if (!claim.isActive()) {
				return false;
			}
			AssociateUnClaimEvent event = ClanVentBus.call(new AssociateUnClaimEvent(p, claim));
			if (((Clan)claim.getHolder()).getMembers().stream().anyMatch(a -> p.getName().equals(a.getName()))) {
				if (!event.isCancelled()) {

					if (associate.getPriority().toLevel() < Clan.ACTION.claimingClearance()) {
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
						Clan owner = ((Clan)claim.getHolder());
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
								if (ClansAPI.getDataInstance().getConfig().getRoot().getBoolean("Clans.raid-shield.claiming-only-enemy")) {
									if (!clan.getRelation().getRivalry().has(owner)) {
										sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("clan-neutral"));
										return false;
									}
								}
								for (Chunk chunk : getChunksAroundLocation(owner.getBase(), -1, 0, 1).stream().filter(c -> ClansAPI.getInstance().getClaimManager().isInClaim(c.getX(), c.getZ(), c.getWorld().getName())).collect(Collectors.toList())) {
									if (chunk.equals(p.getLocation().getChunk())) {
										sendMessage(p, MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("safe-zone"), ((Clan)claim.getHolder()).getName()));
										return false;
									}
								}
								if (clan.getMemorySpace().isPresent()) {
									Node op = clan.getMemorySpace().get().getNode("over-powered");
									if (unClaimCooldown(clan).isComplete()) {
										int i = op.toPrimitive().getInt();

										if (i <= ClansAPI.getDataInstance().getConfigInt("Clans.land-claiming.over-powering.cooldown.after-uses")) {
											i += 1;
											int finalI = i;
											op.set(finalI);
											op.save();
										} else {
											op.set(0);
											op.save();
											unClaimCooldown(clan).save();
											unClaimCooldown(clan).setCooldown();
											sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("un-claim-cooldown"));
										}
									} else {
										sendMessage(p, unClaimCooldown(clan).fullTimeLeft());
										return false;
									}
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
					Clan owner = ((Clan)claim.getHolder());
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
							if (ClansAPI.getDataInstance().getConfig().getRoot().getBoolean("Clans.raid-shield.claiming-only-enemy")) {
								if (!clan.getRelation().getRivalry().has(owner)) {
									sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("clan-neutral"));
									return false;
								}
							}

							for (Chunk chunk : getChunksAroundLocation(owner.getBase(), -1, 0, 1).stream().filter(c -> ClansAPI.getInstance().getClaimManager().isInClaim(c.getX(), c.getZ(), c.getWorld().getName())).collect(Collectors.toList())) {
								if (chunk.equals(p.getLocation().getChunk())) {
									sendMessage(p, MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("safe-zone"), ((Clan)claim.getHolder()).getName()));
									return false;
								}
							}
							if (clan.getMemorySpace().isPresent()) {
								Node op = clan.getMemorySpace().get().getNode("over-powered");
								if (unClaimCooldown(clan).isComplete()) {
									int i = op.toPrimitive().getInt();

									if (i <= ClansAPI.getDataInstance().getConfigInt("Clans.land-claiming.over-powering.cooldown.after-uses")) {
										i += 1;
										int finalI = i;
										op.set(finalI);
										op.save();
									} else {
										op.set(0);
										op.save();
										unClaimCooldown(clan).save();
										unClaimCooldown(clan).setCooldown();
										sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("un-claim-cooldown"));
									}
								} else {
									sendMessage(p, unClaimCooldown(clan).fullTimeLeft());
									return false;
								}
							}
							claim.remove();
							int x = p.getLocation().getChunk().getX();
							int z = p.getLocation().getChunk().getZ();
							String world = p.getWorld().getName();
							owner.broadcast(higherpower(ClansAPI.getInstance().getClanManager().getClanName(ClansAPI.getInstance().getClanManager().getClanID(p.getUniqueId()))));
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
		Clan clan = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
		if (!d.getRoot().isNode(clan.getId().toString())) {
			sendMessage(p, noClaims());
			return false;
		}
		if (!d.getRoot().getNode(clan.getId().toString()).getKeys(false).isEmpty()) {
			for (Claim c : clan.getClaims()) {
				c.remove();
			}
			clan.broadcast(unclaimedAll(p.getName()));
			return true;
		} else {
			sendMessage(p, noClaims());
		}
		return false;
	}

	static int correctCharLength(char c) {
		switch (c) {
			case ':':
			case 'i':
				return 1;
			case 'l':
				return 2;
			case '*':
			case 't':
				return 3;
			case 'f':
			case 'k':
				return 4;
		}
		return 5;
	}

	static int getFixedLength(String string) {
		return string.chars().reduce(0, (p, i) -> p + correctCharLength((char) i) + 1);
	}

	public PaginatedList<Claim.Flag> getFlags(Player p, Claim claim) {

		Set<Claim.Flag> n = ClansAPI.getInstance().getClaimManager().getFlagManager().getFlags().stream().sorted((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getId(), o2.getId())).sorted(Claim.Flag::compareTo).sorted(Comparator.reverseOrder()).collect(Collectors.toCollection(LinkedHashSet::new));
		return new PaginatedList<>(n)
				.limit(5)
				.start((pagination, page, max) -> {
					sendMessage(p, "&e&lChunk: &aGlobal &r(&2All&r)");
					new FancyMessage("--------------------------------------").color(Color.AQUA).style(ChatColor.STRIKETHROUGH).send(p).deploy();
					new FancyMessage("⬛").color(Color.ORANGE).hover("&aClick me to toggle flag control to individual.").action(() -> {
						Set<Claim.Flag> set = Arrays.stream(claim.getFlags().clone()).sorted((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getId(), o2.getId())).sorted(Claim.Flag::compareTo).sorted(Comparator.reverseOrder()).collect(Collectors.toCollection(LinkedHashSet::new));
						getFlags(p, claim, set).get(1);
					}).then("&m------------------------------------'").send(p).deploy();
				})
				.decorate((pagination, f, page, max, placement) -> {
					Message m;
					if (f.isValid()) {

						Claim.Flag match = claim.getFlag(f.getId());

						m = new FancyMessage().then(f.getId() + ";").color(Color.OLIVE).then("Enable").color(match.isEnabled() ? Color.YELLOW : Color.AQUA).hover("Click to enable this flag.").action(() -> {
							Arrays.stream(((Clan)claim.getHolder()).getClaims()).forEach(claim1 -> {
								if (claim1.getFlag(f.getId()) == null) {
									claim1.register(f);
									claim1.getFlag(f.getId()).setEnabled(true);
								}
								Arrays.stream(claim1.getFlags()).forEach(flag -> {
									if (flag.getId().equals(f.getId())) {
										flag.setEnabled(true);
									}
								});
							});
							Schedule.sync(() -> getFlags(p, claim).get(page)).waitReal(1);
						}).then(" ").then("Disable").color(!match.isEnabled() ? Color.YELLOW : Color.AQUA).hover("Click to disallow this flag.").action(() -> {
							Arrays.stream(((Clan)claim.getHolder()).getClaims()).forEach(claim1 -> {
								if (claim1.getFlag(f.getId()) == null) {
									claim1.register(f);
									claim1.getFlag(f.getId()).setEnabled(false);
								}
								Arrays.stream(claim1.getFlags()).forEach(flag -> {
									if (flag.getId().equals(f.getId())) {
										flag.setEnabled(false);
									}
								});
							});
							Schedule.sync(() -> getFlags(p, claim).get(page)).waitReal(1);
						});
						for (Message.Chunk c : m) {
							String text = c.getText();
							if (text.contains(";")) {
								c.replace(";", " &8" + Strings.repeat(".", ((180 - getFixedLength(text.replace(";", ""))) / 2)) + " ");
							}
						}

						m.send(p).deploy();
					} else {
						new FancyMessage("&c&m" + f.getId()).hover("Click to remove me i no longer work. (#").action(() -> {
							Arrays.stream(((Clan)claim.getHolder()).getClaims()).forEach(claim1 -> claim1.remove(f));
							Schedule.sync(() -> getFlags(p, claim).get(page)).waitReal(1);
						}).send(p).deploy();
					}
				})
				.finish(builder -> builder.setPrefix("&b&m--------------------------------------").setPlayer(p));
	}

	public PaginatedList<Claim.Flag> getFlags(Player p, Claim claim, Set<Claim.Flag> set) {
		return new PaginatedList<>(set)
				.limit(5)
				.start((pagination, page, max) -> {
					sendMessage(p, "&e&lChunk: &bX:&f" + claim.getChunk().getX() + " &bZ:&f" + claim.getChunk().getZ());
					new FancyMessage("--------------------------------------").color(org.bukkit.Color.AQUA).style(org.bukkit.ChatColor.STRIKETHROUGH).send(p).deploy();
					new FancyMessage("⬛").color(org.bukkit.Color.AQUA).hover("&aClick me to toggle flag control to global.").action(() -> getFlags(p, claim).get(1))
							.then("&m------------------------------------'").send(p).deploy();
				})
				.decorate((pagination, f, page, max, placement) -> {
					Message m;
					if (f.isValid()) {
						if (f.isEnabled()) {
							m = new FancyMessage().then(f.getId() + ";").color(org.bukkit.Color.GREEN).then("Enable").color(org.bukkit.Color.YELLOW).then(" ").then("Disable").color(org.bukkit.Color.GRAY).hover("Click to disallow this flag.").action(() -> {
								f.setEnabled(false);
								Schedule.sync(() -> getFlags(p, claim, set).get(page)).waitReal(1);
							});
							for (Message.Chunk c : m) {
								String text = c.getText();
								if (text.contains(";")) {
									c.replace(";", " &8" + Strings.repeat(".", ((180 - getFixedLength(text.replace(";", ""))) / 2)) + " ");
								}
							}
						} else {
							m = new FancyMessage().then(f.getId() + ";").color(org.bukkit.Color.GREEN).then("Enable").color(org.bukkit.Color.GRAY).hover("Click to allow this flag.").action(() -> {
								f.setEnabled(true);
								Schedule.sync(() -> getFlags(p, claim, set).get(page)).waitReal(1);
							}).then(" ").then("Disable").color(org.bukkit.Color.YELLOW);
							for (Message.Chunk c : m) {
								String text = c.getText();
								if (text.contains(";")) {
									c.replace(";", " &8" + Strings.repeat(".", ((180 - getFixedLength(text.replace(";", ""))) / 2)) + " ");
								}
							}
						}
						m.send(p).deploy();
					} else {
						new FancyMessage("&c&m" + f.getId()).hover("Click to remove me i no longer work.").action(() -> {
							claim.remove(f);
							Set<Claim.Flag> s = Arrays.stream(claim.getFlags()).sorted((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getId(), o2.getId())).sorted(Claim.Flag::compareTo).sorted(Comparator.reverseOrder()).collect(Collectors.toCollection(LinkedHashSet::new));
							Schedule.sync(() -> getFlags(p, claim, s).get(page)).waitReal(1);
						}).send(p).deploy();
					}
				})
				.finish(builder -> builder.setPrefix("&b&m--------------------------------------").setPlayer(p));
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
			if (!ClansAPI.getDataInstance().getCooldowns().contains(target)) {
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
		FileManager main = ClansAPI.getDataInstance().getConfig();
		return main.getRoot().getBoolean("Clans.land-claiming.allow");
	}


}
