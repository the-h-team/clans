package com.github.sanctum.link.cycles;

import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.RankPriority;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.events.clans.ClaimInteractEvent;
import com.github.sanctum.clans.util.events.clans.ClanLeaveEvent;
import com.github.sanctum.clans.util.events.command.CommandInsertEvent;
import com.github.sanctum.kingdoms.Kingdom;
import com.github.sanctum.kingdoms.Progressable;
import com.github.sanctum.kingdoms.RoundTable;
import com.github.sanctum.kingdoms.achievement.KingdomAchievement;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.formatting.PaginatedList;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.link.ClanVentBus;
import com.github.sanctum.link.CycleList;
import com.github.sanctum.link.EventCycle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class KingdomCycle extends EventCycle {


	@Override
	public boolean persist() {
		return false;
	}

	@Override
	public HUID getId() {
		return super.getId();
	}

	@Override
	public String getName() {
		return "Kingdoms";
	}

	@Override
	public String getDescription() {
		return "A kingdoms addon.";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String[] getAuthors() {
		return new String[]{"Hempfest"};
	}

	@Override
	public void onLoad() {

		getLogger().log(Level.INFO, "- Let the progressive hype begin.");

	}

	public static RoundTable getRoundTable() {
		return Progressable.getProgressables().stream().filter(p -> p instanceof RoundTable).map(p -> (RoundTable) p).findFirst().orElse(null);
	}

	@Override
	public void onEnable() {

		new RoundTable(this);

		FileManager kingdoms = getFile("Kingdoms", "Data");

		if (kingdoms.exists()) {

			if (!kingdoms.getConfig().getKeys(false).isEmpty()) {
				for (String name : kingdoms.getConfig().getKeys(false)) {
					new Kingdom(name, this);
				}
			}

		}

		ClanVentBus.subscribe(ClaimInteractEvent.class, Vent.Priority.MEDIUM, (e, subscription) -> {

			EventCycle cycle = CycleList.getAddon(getName());

			if (!cycle.isActive()) {
				subscription.remove();
				return;
			}

			Player p = e.getPlayer();

			Block b = e.getBlock();

			ClansAPI API = ClansAPI.getInstance();

			if (e.getInteraction() == ClaimInteractEvent.InteractionType.BUILD) {

				if (API.isInClan(p.getUniqueId())) {

					Clan c = API.getClan(p.getUniqueId());

					String name = c.getValue(String.class, "kingdom");

					if (name != null) {

						Kingdom k = Kingdom.getKingdom(name);

						KingdomAchievement achievement;

						Stream<Material> stream = Stream.of(Material.STONE_BRICKS, Material.STONE, Material.ANDESITE, Material.COBBLESTONE, Material.DIORITE, Material.ANDESITE);

						if (stream.anyMatch(m -> StringUtils.use(m.name()).containsIgnoreCase(b.getType().name()))) {

							achievement = k.getAchievement("Walls");

							if (achievement == null) return;

							if (achievement.activated(p)) {

								if (!achievement.isComplete()) {

									achievement.progress(1);

									getMessenger().assignPlayer(p).action("&eWalls&r: " + achievement.getPercentage() + "% &bcomplete");


								} else {

									getMessenger().assignPlayer(p).send("&aObjective &eWalls &acompleted.");

									achievement.deactivate(p);

								}
							}

						}

					}

				}

			}

			if (e.getInteraction() == ClaimInteractEvent.InteractionType.BREAK) {

				if (API.isInClan(p.getUniqueId())) {

					Clan c = API.getClan(p.getUniqueId());

					String name = c.getValue(String.class, "kingdom");

					if (name != null) {

						Kingdom k = Kingdom.getKingdom(name);

						KingdomAchievement achievement;

						Stream<Material> stream = Stream.of(Material.STONE_BRICKS, Material.STONE, Material.ANDESITE, Material.COBBLESTONE, Material.DIORITE, Material.ANDESITE);

						if (stream.anyMatch(m -> StringUtils.use(m.name()).containsIgnoreCase(b.getType().name()))) {

							achievement = k.getAchievement("Walls");

							if (achievement == null) return;

							if (achievement.activated(p)) {

								if (!achievement.isComplete()) {

									achievement.unprogress(1);

									getMessenger().assignPlayer(p).action("&eWalls&r: " + achievement.getPercentage() + "% &bcomplete");


								}
							}

						}

					}

				}

			}

		});

		ClanVentBus.subscribe(CommandInsertEvent.class, Vent.Priority.MEDIUM, (e, subscription) -> {

			EventCycle cycle = CycleList.getAddon("Kingdoms");

			if (!cycle.isActive()) {
				subscription.remove();
				return;
			}

			Player p = e.getSender();
			String[] args = e.getArgs();

			if (args.length == 1) {

				if (args[0].equalsIgnoreCase("kingdom")) {


					e.setReturn(true);
				}
			}

			if (args.length == 2) {

				if (args[0].equalsIgnoreCase("kingdom")) {

					if (args[1].equalsIgnoreCase("start")) {

					}

					if (args[1].equalsIgnoreCase("leave")) {

						ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

						if (associate != null) {

							Clan c = associate.getClan();

							String kindom = c.getValue(String.class, "kingdom");

							if (kindom != null) {

								if (associate.getPriority().toInt() < 3) {
									e.getUtil().sendMessage(p, e.getUtil().noClearance());
									e.setReturn(true);
									return;
								}

								Kingdom k = Kingdom.getKingdom(kindom);

								k.getMembers().remove(c);

								c.removeValue("kingdom");

								e.getUtil().sendMessage(p, "&cYour clan is no longer a member of the kingdom.");

								if (k.getMembers().size() == 0) {
									// TODO: Delete the kingdom traces.
								}

							}
						}

					}

					if (args[1].equalsIgnoreCase("jobs")) {

						ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

						if (associate != null) {

							Clan c = associate.getClan();

							String kindom = c.getValue(String.class, "kingdom");

							if (kindom != null) {

								Kingdom k = Kingdom.getKingdom(kindom);

								PaginatedList<KingdomAchievement> help = new PaginatedList<>(new ArrayList<>(k.getAchievements()))
										.limit(6)
										.start((pagination, page, max) -> {
											if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.16")) {
												Message.form(p).send("&7&m------------&7&l[&#ff7700&oKingdom Jobs&7&l]&7&m------------");
											} else {
												Message.form(p).send("&7&m------------&7&l[&6&oKingdom Jobs&7&l]&7&m------------");
											}
										});

								help.finish((pagination, page, max) -> {
									Message.form(p).send("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
									TextLib component = TextLib.getInstance();
									int next = page + 1;
									int last = Math.max(page - 1, 1);
									List<BaseComponent> toSend = new LinkedList<>();
									if (page == 1) {
										if (page == max) {
											toSend.add(component.textHoverable("", "&8« ", "&cYou are on the first page already."));
											toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
											toSend.add(component.textHoverable("", " &8»", "&cYou are already on the last page."));
											p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
											return;
										}
										toSend.add(component.textHoverable("", "&8« ", "&cYou are on the first page already."));
										toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
										toSend.add(component.execute(() -> help.get(next), component.textHoverable("", " &3»", "&aGoto the next page.")));
										p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
										return;
									}
									if (page == max) {
										toSend.add(component.execute(() -> help.get(last), component.textHoverable("", "&3« ", "&aGoto the previous page.")));
										toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
										toSend.add(component.textHoverable("", " &8»", "&cYou are already on the last page."));
										p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
										return;
									}
									if (next <= max) {
										toSend.add(component.execute(() -> help.get(last), component.textHoverable("", "&3« ", "&aGoto the previous page.")));
										toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
										toSend.add(component.execute(() -> help.get(next), component.textHoverable("", " &3»", "&aGoto the next page.")));
										p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
									}
								}).decorate((pagination, achievement, page, max, placement) -> {

									if (achievement.activated(p)) {
										Message.form(p).build(TextLib.getInstance().textRunnable("", " &7# &6&l" + placement + " &c&o" + achievement.getTitle() + " &r(&d" + achievement.getPercentage() + "%&r) " + " &e: &b&l" + achievement.getDescription(), "&6Click to quit job &3&l" + achievement.getTitle(), "c kingdom quit " + achievement.getTitle()));
									} else {
										Message.form(p).build(TextLib.getInstance().textRunnable("", " &7# &6&l" + placement + " &3&o" + achievement.getTitle() + " &r(&d" + achievement.getPercentage() + "%&r) " + " &e: &b&l" + achievement.getDescription(), "&6Click to start job &3&l" + achievement.getTitle(), "c kingdom work " + achievement.getTitle()));
									}

								}).get(1);

							}


						}

					}


					if (args[1].equalsIgnoreCase("roundtable")) {


					}

					e.setReturn(true);
				}
			}

			if (args.length == 3) {

				if (args[0].equalsIgnoreCase("kingdom")) {

					if (args[1].equalsIgnoreCase("join")) {

						Kingdom k = Kingdom.getKingdom(args[2]);

						if (k != null) {

							ClansAPI API = ClansAPI.getInstance();

							ClanAssociate associate = API.getAssociate(p).orElse(null);

							if (associate != null) {

								Clan c = associate.getClan();

								if (c.getValue(String.class, "kingdom") != null) {
									e.getUtil().sendMessage(p, "&cYou are already in a kingdom.");
									e.setReturn(true);
									return;
								}

								if (associate.getPriority().toInt() == 3) {

									k.getMembers().add(c);

								} else {
									e.getUtil().sendMessage(p, e.getUtil().noClearance());
								}

							}

						} else {
							e.getUtil().sendMessage(p, "&cThis kingdom doesn't exist!");
						}

					}


					if (args[1].equalsIgnoreCase("start")) {

						String name = args[2];

						ClansAPI API = ClansAPI.getInstance();

						ClanAssociate associate = API.getAssociate(p).orElse(null);

						if (associate != null) {


							Clan c = associate.getClan();

							Kingdom k = Kingdom.getKingdom(name);

							if (k == null) {

								if (associate.getPriority().toInt() < 3) {
									e.getUtil().sendMessage(p, e.getUtil().noClearance());
									e.setReturn(true);
									return;
								}

								String kingdom = c.getValue(String.class, "kingdom");

								if (kingdom != null) {
									e.getUtil().sendMessage(p, "&cYou are already apart of a kingdom!");
									e.setReturn(true);
									return;
								}

								Kingdom create = new Kingdom(name, this);

								c.setValue("kingdom", name, false);

								create.getMembers().add(c);

								getMessenger().setPrefix(Clan.ACTION.getPrefix()).broadcast(p.getName() + " started a new kingdom called &6" + name);

							} else {

								if (k.getMembers().contains(c)) {
									e.getUtil().sendMessage(p, "&cYou're already a member lol.");
								} else {
									e.getUtil().sendMessage(p, "&cThis kingdom already exists!");
								}

							}


						}

					}

					if (args[1].equalsIgnoreCase("roundtable")) {

						RoundTable table = KingdomCycle.getRoundTable();

						if (args[2].equalsIgnoreCase("jobs")) {

							PaginatedList<KingdomAchievement> help = new PaginatedList<>(new ArrayList<>(table.getAchievements()))
									.limit(6)
									.start((pagination, page, max) -> {
										if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.16")) {
											Message.form(p).send("&7&m------------&7&l[&#ff7700&oRoundtable Jobs&7&l]&7&m------------");
										} else {
											Message.form(p).send("&7&m------------&7&l[&6&oRoundtable Jobs&7&l]&7&m------------");
										}
									});

							help.finish((pagination, page, max) -> {
								Message.form(p).send("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
								TextLib component = TextLib.getInstance();
								int next = page + 1;
								int last = Math.max(page - 1, 1);
								List<BaseComponent> toSend = new LinkedList<>();
								if (page == 1) {
									if (page == max) {
										toSend.add(component.textHoverable("", "&8« ", "&cYou are on the first page already."));
										toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
										toSend.add(component.textHoverable("", " &8»", "&cYou are already on the last page."));
										p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
										return;
									}
									toSend.add(component.textHoverable("", "&8« ", "&cYou are on the first page already."));
									toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
									toSend.add(component.execute(() -> help.get(next), component.textHoverable("", " &3»", "&aGoto the next page.")));
									p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
									return;
								}
								if (page == max) {
									toSend.add(component.execute(() -> help.get(last), component.textHoverable("", "&3« ", "&aGoto the previous page.")));
									toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
									toSend.add(component.textHoverable("", " &8»", "&cYou are already on the last page."));
									p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
									return;
								}
								if (next <= max) {
									toSend.add(component.execute(() -> help.get(last), component.textHoverable("", "&3« ", "&aGoto the previous page.")));
									toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
									toSend.add(component.execute(() -> help.get(next), component.textHoverable("", " &3»", "&aGoto the next page.")));
									p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
								}
							}).decorate((pagination, achievement, page, max, placement) -> {

								if (achievement.activated(p)) {
									Message.form(p).build(TextLib.getInstance().textRunnable("", " &7# &6&l" + placement + " &c&o" + achievement.getTitle() + " &r(&d" + achievement.getPercentage() + "%&r) " + " &e: &b&l" + achievement.getDescription(), "&6Click to quit job &3&l" + achievement.getTitle(), "c kingdom roundtable quit " + achievement.getTitle()));
								} else {
									Message.form(p).build(TextLib.getInstance().textRunnable("", " &7# &6&l" + placement + " &3&o" + achievement.getTitle() + " &r(&d" + achievement.getPercentage() + "%&r) " + " &e: &b&l" + achievement.getDescription(), "&6Click to start job &3&l" + achievement.getTitle(), "c kingdom roundtable work " + achievement.getTitle()));
								}

							}).get(1);

						}

						if (args[2].equalsIgnoreCase("join")) {

							if (table.getUsers().isEmpty()) {

								table.take(p.getUniqueId(), RoundTable.Rank.HIGHEST);
								getMessenger().setPrefix(Clan.ACTION.getPrefix()).broadcast(p.getName() + " is now among the most powerful on the server.");

							} else {

								if (!table.join(p.getUniqueId())) {

									if (table.isMember(p.getUniqueId())) {
										e.getUtil().sendMessage(p, "&cYou are already a member.");
									} else {
										e.getUtil().sendMessage(p, "&cYou are not invited.");
									}

								} else {

									e.getUtil().sendMessage(p, "&a&lWelcome to the round table.");

								}
							}
						}


					}

					if (args[1].equalsIgnoreCase("name")) {

						ClansAPI API = ClansAPI.getInstance();

						ClanAssociate associate = API.getAssociate(p).orElse(null);

						if (associate != null) {

							Clan c = associate.getClan();

							String kingdom = c.getValue(String.class, "kingdom");

							if (kingdom != null) {

								Kingdom k = Kingdom.getKingdom(kingdom);

								k.setName(args[2]);

								e.getUtil().sendMessage(p, "&aKingdom name changed to &b" + args[2]);

							}
						}

					}

					if (args[1].equalsIgnoreCase("work")) {


						ClansAPI API = ClansAPI.getInstance();

						ClanAssociate associate = API.getAssociate(p).orElse(null);

						if (associate != null) {


							Clan c = associate.getClan();

							String kingdom = c.getValue(String.class, "kingdom");

							if (kingdom != null) {

								Kingdom k = Kingdom.getKingdom(kingdom);

								KingdomAchievement achievement = k.getAchievement(args[2]);

								if (achievement != null) {

									if (achievement.activate(p)) {

										p.sendTitle(StringUtils.use(achievement.getTitle()).translate(), StringUtils.use(achievement.getDescription()).translate(), 60, 10, 60);


									} else {

										e.getUtil().sendMessage(p, "&cYou are already working job &e" + achievement.getTitle());

									}

								}
							}
						}

					}

					if (args[1].equalsIgnoreCase("quit")) {

						ClansAPI API = ClansAPI.getInstance();

						ClanAssociate associate = API.getAssociate(p).orElse(null);

						if (associate != null) {


							Clan c = associate.getClan();

							String kingdom = c.getValue(String.class, "kingdom");

							if (kingdom != null) {

								Kingdom k = Kingdom.getKingdom(kingdom);

								KingdomAchievement achievement = k.getAchievement(args[2]);

								if (achievement != null) {

									if (achievement.deactivate(p)) {

										e.getUtil().sendMessage(p, "&cYou are no longer working job &e" + achievement.getTitle());


									} else {

										e.getUtil().sendMessage(p, "&cYou aren't currently working job &e" + achievement.getTitle());

									}

								}
							}
						}

					}

					e.setReturn(true);
				}
			}

			if (args.length == 4) {

				if (args[0].equalsIgnoreCase("kingdom")) {


					if (args[1].equalsIgnoreCase("roundtable")) {

						RoundTable table = KingdomCycle.getRoundTable();

						if (table != null) {

							if (!table.isMember(p.getUniqueId())) {

								e.setReturn(true);
								return;
							}

							if (args[2].equalsIgnoreCase("work")) {


								KingdomAchievement achievement = table.getAchievement(args[3]);

								if (achievement != null) {

									if (achievement.activate(p)) {

										p.sendTitle(StringUtils.use(achievement.getTitle()).translate(), StringUtils.use(achievement.getDescription()).translate(), 60, 10, 60);


									} else {

										e.getUtil().sendMessage(p, "&cYou are already working job &e" + achievement.getTitle());

									}

								}

							}

							if (args[2].equalsIgnoreCase("quit")) {


								KingdomAchievement achievement = table.getAchievement(args[3]);

								if (achievement != null) {

									if (achievement.deactivate(p)) {

										e.getUtil().sendMessage(p, "&cYou are no longer working job &e" + achievement.getTitle());


									} else {

										e.getUtil().sendMessage(p, "&cYou aren't currently working job &e" + achievement.getTitle());

									}

								}

							}

						}

					}

					e.setReturn(true);
				}
			}

		});

		ClanVentBus.subscribe(ClanLeaveEvent.class, Vent.Priority.MEDIUM, (e, subscription) -> {

			if (e.getAssociate().getPriority() == RankPriority.HIGHEST) {

				Clan c = e.getAssociate().getClan();

				String key = c.getValue(String.class, "kingdom");

				if (key != null) {

					Kingdom k = Kingdom.getKingdom(key);

					if (k.getMembers().size() == 1) {

						getMessenger().setPrefix(Clan.ACTION.getPrefix()).broadcast("&rKingdom &6" + k.getName() + " &rhas fallen");

						k.remove(this);

					} else {
						k.getMembers().remove(c);
					}

				}

			}


		});

	}

	@Override
	public void onDisable() {

		for (Progressable progressable : Progressable.getProgressables()) {
			progressable.save(this);
		}

		getLogger().log(Level.INFO, "- Goodbye cruel world D:");

	}
}
