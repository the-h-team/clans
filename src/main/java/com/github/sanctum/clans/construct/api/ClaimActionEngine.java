package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.util.StringLibrary;
import com.github.sanctum.clans.construct.impl.DefaultUnclaimCooldown;
import com.github.sanctum.clans.construct.impl.DefaultClaim;
import com.github.sanctum.clans.event.associate.AssociateClaimEvent;
import com.github.sanctum.clans.event.associate.AssociateLoseLandEvent;
import com.github.sanctum.clans.event.associate.AssociateObtainLandEvent;
import com.github.sanctum.clans.event.associate.AssociateUnClaimEvent;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.formatting.pagination.AdvancedPagination;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.util.HUID;
import com.github.sanctum.panther.util.RandomID;
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
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ClaimActionEngine extends StringLibrary {

	public Claim.Action<Boolean> claim(Player p) {
		return claim(p, p.getLocation().getChunk());
	}

	public Claim.Action<Boolean> claim(Player p, Chunk ch) {
		return () -> {
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
				AssociateClaimEvent event1 = ClanVentBus.call(new AssociateClaimEvent(p));
				if (event1.isCancelled()) {
					return false;
				}
				Clan clan = associate.getClan();
				if (!claimEffect) {
					if (clan.getClaims().length == getPlayerHardcap(p)) {
						sendMessage(p, alreadyMaxClaims());
						return false;
					}
				}
				if (claimEffect && clan.getClaims().length >= clan.getClaimLimit()) {
					sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("clan-max-claims"));
					return false;
				}
				if (clan.getClaims().length == getPlayerHardcap(p)) {
					sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("hard-max-claims"));
					return false;
				}
				int chunkCount = 0;
				for (Chunk chunk : getSurroundingChunks(ch, -1, 0, 1).deploy().stream().filter(c -> ClansAPI.getInstance().getClaimManager().isInClaim(c.getX(), c.getZ(), c.getWorld().getName())).collect(Collectors.toList())) {
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
		};
	}

	public Claim.Action<Boolean> unclaim(Player p) {
		return unclaim(p, p.getLocation().getChunk());
	}

	public Claim.Action<Boolean> unclaim(Player p, Chunk ch) {
		return () -> {
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
				if (((Clan) claim.getHolder()).getMembers().stream().anyMatch(a -> p.getName().equals(a.getName()))) {
					if (!event.isCancelled()) {
						ClanVentBus.call(new AssociateLoseLandEvent(p, claim.getChunk()));
						if (!Clearance.MANAGE_LAND.test(associate)) {
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
						if (Clan.ACTION.isIgnoringShield()) {
							Clan owner = ((Clan) claim.getHolder());
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
									for (Chunk chunk : getSurroundingChunks(owner.getBase(), -1, 0, 1).deploy().stream().filter(c -> ClansAPI.getInstance().getClaimManager().isInClaim(c.getX(), c.getZ(), c.getWorld().getName())).collect(Collectors.toList())) {
										if (chunk.equals(p.getLocation().getChunk())) {
											sendMessage(p, MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("safe-zone"), ((Clan) claim.getHolder()).getName()));
											return false;
										}
									}
									if (clan.getMemorySpace().isPresent()) {
										Node op = clan.getMemorySpace().get().getNode("over-powered");
										if (getUnclaimCooldown(clan).deploy().isComplete()) {
											int i = op.toPrimitive().getInt();

											if (i <= ClansAPI.getDataInstance().getConfigInt("Clans.land-claiming.over-powering.cooldown.after-uses")) {
												i += 1;
												int finalI = i;
												op.set(finalI);
												op.save();
											} else {
												op.set(0);
												op.save();
												getUnclaimCooldown(clan).deploy().save();
												getUnclaimCooldown(clan).deploy().setCooldown();
												sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("un-claim-cooldown"));
											}
										} else {
											sendMessage(p, getUnclaimCooldown(clan).deploy().fullTimeLeft());
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
						Clan owner = ((Clan) claim.getHolder());
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
								if (ClansAPI.getDataInstance().getConfigString("Clans.raid-shield.mode").equals("TEMPORARY")) {
									sendMessage(p, "&cLand cannot be overtaken only raided.");
									return false;
								}

								for (Chunk chunk : getSurroundingChunks(owner.getBase(), -1, 0, 1).deploy().stream().filter(c -> ClansAPI.getInstance().getClaimManager().isInClaim(c.getX(), c.getZ(), c.getWorld().getName())).collect(Collectors.toList())) {
									if (chunk.equals(p.getLocation().getChunk())) {
										sendMessage(p, MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("safe-zone"), ((Clan) claim.getHolder()).getName()));
										return false;
									}
								}
								if (clan.getMemorySpace().isPresent()) {
									Node op = clan.getMemorySpace().get().getNode("over-powered");
									if (getUnclaimCooldown(clan).deploy().isComplete()) {
										int i = op.toPrimitive().getInt();

										if (i <= ClansAPI.getDataInstance().getConfigInt("Clans.land-claiming.over-powering.cooldown.after-uses")) {
											i += 1;
											int finalI = i;
											op.set(finalI);
											op.save();
										} else {
											op.set(0);
											op.save();
											getUnclaimCooldown(clan).deploy().save();
											getUnclaimCooldown(clan).deploy().setCooldown();
											sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("un-claim-cooldown"));
										}
									} else {
										sendMessage(p, getUnclaimCooldown(clan).deploy().fullTimeLeft());
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
		};
	}

	public Claim.Action<Boolean> unclaimAll(Player p) {
		return () -> {
			FileManager d = ClansAPI.getInstance().getClaimManager().getFile();
			Clan clan = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
			if (clan != null) {
				if (!d.getRoot().isNode(clan.getId().toString())) {
					sendMessage(p, noClaims());
					return false;
				}
				if (!d.getRoot().getNode(clan.getId().toString()).getKeys(false).isEmpty()) {
					for (Claim c : clan.getClaims()) {
						AssociateUnClaimEvent e = ClanVentBus.call(new AssociateUnClaimEvent(p, c));
						if (!e.isCancelled()) {
							ClanVentBus.call(new AssociateLoseLandEvent(p, c.getChunk()));
							c.remove();
						}
					}
					clan.broadcast(unclaimedAll(p.getName()));
					return true;
				} else {
					sendMessage(p, noClaims());
				}
			}
			return false;
		};
	}

	int getFlagCharCorrection(char c) {
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

	int getFlagCharFixedLength(String string) {
		return string.chars().reduce(0, (p, i) -> p + getFlagCharCorrection((char) i) + 1);
	}

	public Claim.Action<AdvancedPagination<Claim.Flag>> getFlags(Player p, Claim claim) {
		return new Claim.Action<AdvancedPagination<Claim.Flag>>() {

			final Set<Claim.Flag> n;
			final AdvancedPagination<Claim.Flag> pag;
			{
				n = ClansAPI.getInstance().getClaimManager().getFlagManager().getFlags().stream().sorted((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getId(), o2.getId())).sorted(Claim.Flag::compareTo).sorted(Comparator.reverseOrder()).collect(Collectors.toCollection(LinkedHashSet::new));
				pag = new AdvancedPagination<>(p, n);
			}

			@Override
			public AdvancedPagination<Claim.Flag> deploy() {
				pag.limit(5);
				pag.setHeader((player, message) -> {
					message.then("&e&lChunk: &aGlobal &r(&2All&r)");
					message.then("\n");
					message.then("--------------------------------------").color(Color.AQUA).style(ChatColor.STRIKETHROUGH);
					message.then("\n");
					message.then("⬛").color(Color.ORANGE).hover("&aClick me to toggle flag control to individual.").action(() -> {
						Set<Claim.Flag> set = Arrays.stream(claim.getFlags().clone()).sorted((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getId(), o2.getId())).sorted(Claim.Flag::compareTo).sorted(Comparator.reverseOrder()).collect(Collectors.toCollection(LinkedHashSet::new));
						getFlags(p, claim, set).deploy().send(1);
					});
					message.then("\n");
					message.then("&m------------------------------------'");
				});
				pag.setFormat((f, placement, message) -> {
					if (f.isValid()) {

						Claim.Flag match = claim.getFlag(f.getId());

						message.then(f.getId() + ";").color(Color.OLIVE).then("Enable").color(match.isEnabled() ? Color.YELLOW : Color.AQUA).hover("Click to enable this flag.").action(() -> {
							Arrays.stream(((Clan) claim.getHolder()).getClaims()).forEach(claim1 -> {
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
							TaskScheduler.of(() -> getFlags(p, claim).deploy().send(placement.getKey())).scheduleLater(1);
						}).then(" ").then("Disable").color(!match.isEnabled() ? Color.YELLOW : Color.AQUA).hover("Click to disallow this flag.").action(() -> {
							Arrays.stream(((Clan) claim.getHolder()).getClaims()).forEach(claim1 -> {
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
							TaskScheduler.of(() -> getFlags(p, claim).deploy().send(placement.getKey())).scheduleLater(1);
						});
						for (Message.Chunk c : message) {
							String text = c.getText();
							if (text.contains(";")) {
								c.replace(";", " &8" + Strings.repeat(".", ((180 - getFlagCharFixedLength(text.replace(";", ""))) / 2)) + " ");
							}
						}
					} else {
						message.then("&c&m" + f.getId()).hover("Click to remove me i no longer work. (#").action(() -> {
							Arrays.stream(((Clan) claim.getHolder()).getClaims()).forEach(claim1 -> claim1.remove(f));
							TaskScheduler.of(() -> getFlags(p, claim).deploy().send(placement.getKey())).scheduleLater(1);
						});
					}
				});

				pag.setFooter((player, message) -> message.then("&b&m--------------------------------------"));
				return pag;
			}
		};
	}

	public Claim.Action<AdvancedPagination<Claim.Flag>> getFlags(Player p, Claim claim, Set<Claim.Flag> set) {
		return new Claim.Action<AdvancedPagination<Claim.Flag>>() {

			final AdvancedPagination<Claim.Flag> pag;
			{
				pag = new AdvancedPagination<>(p, set);
			}

			@Override
			public AdvancedPagination<Claim.Flag> deploy() {
				pag.limit(5);
				pag.setHeader((player, message) -> {
					message.then("&e&lChunk: &bX:&f" + claim.getChunk().getX() + " &bZ:&f" + claim.getChunk().getZ());
					message.then("\n");
					message.then("--------------------------------------").color(org.bukkit.Color.AQUA).style(org.bukkit.ChatColor.STRIKETHROUGH);
					message.then("\n");
					message.then("⬛").color(org.bukkit.Color.AQUA).hover("&aClick me to toggle flag control to global.").action(() -> getFlags(p, claim).deploy().send(1))
							.then("\n")
							.then("&m------------------------------------'");
				});
				pag.setFormat((f, placement, message) -> {
					if (f.isValid()) {
						if (f.isEnabled()) {
							message.then(f.getId() + ";").color(org.bukkit.Color.GREEN).then("Enable").color(org.bukkit.Color.YELLOW).then(" ").then("Disable").color(org.bukkit.Color.GRAY).hover("Click to disallow this flag.").action(() -> {
								f.setEnabled(false);
								TaskScheduler.of(() -> getFlags(p, claim, set).deploy().send(placement.getKey())).scheduleLater(1);
							});
							for (Message.Chunk c : message) {
								String text = c.getText();
								if (text.contains(";")) {
									c.replace(";", " &8" + Strings.repeat(".", ((180 - getFlagCharFixedLength(text.replace(";", ""))) / 2)) + " ");
								}
							}
						} else {
							message.then(f.getId() + ";").color(org.bukkit.Color.GREEN).then("Enable").color(org.bukkit.Color.GRAY).hover("Click to allow this flag.").action(() -> {
								f.setEnabled(true);
								TaskScheduler.of(() -> getFlags(p, claim, set).deploy().send(placement.getKey())).scheduleLater(1);
							}).then(" ").then("Disable").color(org.bukkit.Color.YELLOW);
							for (Message.Chunk c : message) {
								String text = c.getText();
								if (text.contains(";")) {
									c.replace(";", " &8" + Strings.repeat(".", ((180 - getFlagCharFixedLength(text.replace(";", ""))) / 2)) + " ");
								}
							}
						}
					} else {
						message.then("&c&m" + f.getId()).hover("Click to remove me i no longer work.").action(() -> {
							claim.remove(f);
							Set<Claim.Flag> s = Arrays.stream(claim.getFlags()).sorted((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getId(), o2.getId())).sorted(Claim.Flag::compareTo).sorted(Comparator.reverseOrder()).collect(Collectors.toCollection(LinkedHashSet::new));
							TaskScheduler.of(() -> getFlags(p, claim, s).deploy().send(placement.getKey())).scheduleLater(1);
						});
					}
				});

				pag.setFooter((player, message) -> message.then("&b&m--------------------------------------"));
				return pag;
			}
		};
	}

	public synchronized Claim.Action<Collection<Chunk>> getSurroundingChunks(Location location, int xoff, int yoff, int zoff) {
		return () -> {
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
		};
	}

	public synchronized Claim.Action<Collection<Chunk>> getSurroundingChunks(Chunk chunk, int xoff, int yoff, int zoff) {
		return () -> {
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
		};
	}

	public Claim.Action<ClanCooldown> getUnclaimCooldown(Clan clan) {
		return () -> {
			for (ClanCooldown c : clan.getCooldowns()) {
				if (c.getAction().equals("Clans:unclaim-limit")) {
					return c;
				}
			}
			ClanCooldown clanCooldown = new DefaultUnclaimCooldown(clan.getId().toString());
			if (!ClansAPI.getDataInstance().getCooldowns().contains(clanCooldown)) {
				clanCooldown.save();
			}
			return clanCooldown;
		};
	}

	public int getPlayerHardcap(Player player) {
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

	public Claim.Action<Boolean> isAllowed() {
		return new Claim.Action<Boolean>() {
			final FileManager main;

			{
				main = ClansAPI.getDataInstance().getConfig();
			}

			@Override
			public Boolean deploy() {
				return main.getRoot().getBoolean("Clans.land-claiming.allow");
			}
		};
	}


}
