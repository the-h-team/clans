package com.github.sanctum.clans.bridge.internal;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.internal.kingdoms.Kingdom;
import com.github.sanctum.clans.bridge.internal.kingdoms.Progressable;
import com.github.sanctum.clans.bridge.internal.kingdoms.Reward;
import com.github.sanctum.clans.bridge.internal.kingdoms.RoundTable;
import com.github.sanctum.clans.bridge.internal.kingdoms.achievement.KingdomAchievement;
import com.github.sanctum.clans.bridge.internal.kingdoms.event.KingdomCreatedEvent;
import com.github.sanctum.clans.bridge.internal.kingdoms.event.KingdomCreationEvent;
import com.github.sanctum.clans.bridge.internal.kingdoms.event.KingdomJobCompleteEvent;
import com.github.sanctum.clans.construct.RankPriority;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.MessagePrefix;
import com.github.sanctum.clans.events.command.CommandInsertEvent;
import com.github.sanctum.clans.events.core.ClaimInteractEvent;
import com.github.sanctum.clans.events.core.ClanLeaveEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.api.TaskService;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.formatting.Bulletin;
import com.github.sanctum.labyrinth.formatting.PaginatedList;
import com.github.sanctum.labyrinth.formatting.string.DefaultColor;
import com.github.sanctum.labyrinth.formatting.string.RandomHex;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TextLib;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class KingdomAddon extends ClanAddon implements Bulletin.Factory {


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

		FileManager kingdoms = getFile(FileType.JSON, "kingdoms", "data");
		FileManager data = getFile(FileType.JSON, "achievements", "data");
		FileManager users = getFile(FileType.JSON, "users", "data");

		if (kingdoms.getRoot().exists()) {

			if (!kingdoms.getRoot().getKeys(false).isEmpty()) {
				for (String name : kingdoms.getRoot().getKeys(false)) {
					new Kingdom(name, this);
				}
			}

		} else {
			try {
				kingdoms.getRoot().create();
				data.getRoot().create();
				users.getRoot().create();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		ClanVentBus.subscribe(KingdomJobCompleteEvent.class, Vent.Priority.MEDIUM, (e, subscription) -> {
			ClanAddon cycle = ClanAddonQuery.getAddon(getName());

			if (!cycle.isActive()) {
				LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.SYNCHRONOUS).run(subscription::remove);
				return;
			}

			Reward<?> reward = e.getAchievement().getReward();
			if (reward != null) {
				reward.give(e.getKingdom());
				e.getKingdom().forEach(c -> c.broadcast("&aYou have been rewarded."));
			}

		});

		ClanVentBus.subscribe(ClaimInteractEvent.class, Vent.Priority.MEDIUM, (e, subscription) -> {

			ClanAddon cycle = ClanAddonQuery.getAddon(getName());

			if (!cycle.isActive()) {
				LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.SYNCHRONOUS).run(subscription::remove);
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

									c.forEach(a -> {
										Player online = a.getPlayer().getPlayer();
										if (online != null) {
											getMessenger().setPlayer(online).action("&eWalls&r: " + achievement.getPercentage() + "% &bcomplete");
										}
									});


								} else {

									getMessenger().setPlayer(p).send("&aObjective &eWalls &acompleted.");

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

									getMessenger().setPlayer(p).action("&eWalls&r: " + achievement.getPercentage() + "% &bcomplete");


								}
							}

						}

					}

				}

			}

		});

		ClanVentBus.subscribe(CommandInsertEvent.class, Vent.Priority.MEDIUM, (e, subscription) -> {

			ClanAddon cycle = ClanAddonQuery.getAddon("Kingdoms");

			if (!cycle.isActive()) {
				LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.SYNCHRONOUS).run(subscription::remove);
				return;
			}

			Player p = e.getSender();
			String[] args = e.getArgs();

			if (args.length == 1) {

				if (args[0].equalsIgnoreCase("kingdom")) {
					// TODO: send help menu
					Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
					message().append(text(" ")).send(p);
					if (associate != null) {
						if (associate.getClan().getValue(String.class, "kingdom") != null) {
							message().append(text(" ")).append(text("[").color(Color.OLIVE)).append(text("Start").color(Color.MAROON).style(ChatColor.STRIKETHROUGH).bind(hover("You are already in a kingdom").style(DefaultColor.VELVET))).append(text("]").color(Color.OLIVE)).send(p);
							message().append(text(" ")).send(p);
							message().append(text(" ")).append(text("[").color(Color.OLIVE)).append(text("Leave").color(Color.FUCHSIA).bind(hover("Click to leave your current kingdom").style(new RandomHex())).bind(command("/c kingdom leave"))).append(text("]").color(Color.OLIVE)).send(p);
							message().append(text(" ")).send(p);
							message().append(text(" ")).append(text("[").color(Color.OLIVE)).append(text("Jobs").color(Color.FUCHSIA).bind(hover("Click to view the jobs your kingdom has available.").style(new RandomHex())).bind(command("/c kingdom jobs"))).append(text("]").color(Color.OLIVE)).send(p);

						} else {
							message().append(text(" ")).append(text("[").color(Color.OLIVE)).append(text("Start").color(Color.FUCHSIA).bind(hover("Click to start a kingdom").style(new RandomHex())).bind(suggest("/c kingdom start "))).append(text("]").color(Color.OLIVE)).send(p);
							message().append(text(" ")).send(p);
							message().append(text(" ")).append(text("[").color(Color.OLIVE)).append(text("Join").style(new RandomHex()).bind(hover("Click to join a kingdom").style(new RandomHex())).bind(suggest("/c kingdom join "))).append(text("]").color(Color.OLIVE)).send(p);
						}
					} else {
						message().append(text("You must own a clan to contribute to kingdom services.").style(DefaultColor.VELVET)).send(p);
					}
					message().append(text(" ")).send(p);
					e.setReturn(true);
				}
			}

			if (args.length == 2) {

				if (args[0].equalsIgnoreCase("kingdom")) {

					if (args[1].equalsIgnoreCase("start")) {
						e.getUtil().sendMessage(p, "&cInvalid usage: &6/clan &7kingdom start <name>");
					}

					if (args[1].equalsIgnoreCase("leave")) {

						Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

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
									// TODO: announce kingdom fallen
									k.remove(this);
								}

							}
						}

					}

					if (args[1].equalsIgnoreCase("jobs")) {

						Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

						if (associate != null) {

							Clan c = associate.getClan();

							String kingdom = c.getValue(String.class, "kingdom");

							if (kingdom != null) {

								Kingdom k = Kingdom.getKingdom(kingdom);

								PaginatedList<KingdomAchievement> help = new PaginatedList<>(new ArrayList<>(k.getAchievements()))
										.limit(6)
										.start((pagination, page, max) -> {
											MessagePrefix prefix = ClansAPI.getInstance().getPrefix();
											message().append(text(prefix.getPrefix())).append(text(prefix.getText()).style(new RandomHex())).append(text(prefix.getSuffix())).append(text(" ")).append(text("|").style(ChatColor.BOLD)).append(text(" ")).append(text("Jobs").style(DefaultColor.MANGO)).send(p);
										});

								help.finish(builder -> {
									builder.setPrefix("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
									builder.setSuffix("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
									builder.setPlayer(p);
								}).decorate((pagination, achievement, page, max, placement) -> {

									if (achievement.activated(p)) {
										message().append(text("[").color(Color.MAROON))
												.append(text("#").color(Color.GRAY))
												.append(text(String.valueOf(placement)).color(Color.ORANGE).style(ChatColor.BOLD))
												.append(text("]").color(Color.MAROON))
												.append(text(" "))
												.append(text(achievement.getTitle()).style(new RandomHex()))
												.append(text(" "))
												.append(text("(").color(Color.MAROON))
												.append(text(String.valueOf(achievement.getPercentage())).color(Color.AQUA).bind(hover("&cClick to quit job &3&l" + achievement.getTitle())).bind(command("/c kingdom quit " + achievement.getTitle())))
												.append(text("%"))
												.append(text(")").color(Color.MAROON))
												.append(text(":"))
												.append(text(" "))
												.append(text(achievement.getDescription()).style(new RandomHex()))
												.send(p);
									} else {
										message().append(text("[").color(Color.MAROON))
												.append(text("#").color(Color.GRAY))
												.append(text(String.valueOf(placement)).color(Color.ORANGE).style(ChatColor.BOLD))
												.append(text("]").color(Color.MAROON))
												.append(text(" "))
												.append(text(achievement.getTitle()).style(new RandomHex()))
												.append(text(" "))
												.append(text("(").color(Color.MAROON))
												.append(text(String.valueOf(achievement.getPercentage())).color(Color.AQUA).bind(hover("&aClick to accept job &3&l" + achievement.getTitle())).bind(command("/c kingdom work " + achievement.getTitle())))
												.append(text("%"))
												.append(text(")").color(Color.MAROON))
												.append(text(":"))
												.append(text(" "))
												.append(text(achievement.getDescription()).style(new RandomHex()))
												.send(p);
									}

								}).get(1);

							}


						}

					}


					if (args[1].equalsIgnoreCase("roundtable")) {
						// TODO: send rountable help menu
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

							Clan.Associate associate = API.getAssociate(p).orElse(null);

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

						Clan.Associate associate = API.getAssociate(p).orElse(null);

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

								KingdomCreationEvent event = ClanVentBus.call(new KingdomCreationEvent(associate, name));
								if (!event.isCancelled()) {
									Kingdom create = new Kingdom(event.getKingdomName(), this);
									ClanVentBus.call(new KingdomCreatedEvent(p, create));
									c.setValue("kingdom", event.getKingdomName(), false);
									create.getMembers().add(c);

									getMessenger().setPrefix(Clan.ACTION.getPrefix()).broadcast(p.getName() + " started a new kingdom called &6" + event.getKingdomName());
								}

							} else {

								if (k.getMembers().contains(c)) {
									e.getUtil().sendMessage(p, "&cYou're already a member.");
								} else {
									e.getUtil().sendMessage(p, "&cThis kingdom already exists!");
								}

							}


						}

					}

					if (args[1].equalsIgnoreCase("roundtable")) {

						RoundTable table = KingdomAddon.getRoundTable();

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

						Clan.Associate associate = API.getAssociate(p).orElse(null);

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

						Clan.Associate associate = API.getAssociate(p).orElse(null);

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

						Clan.Associate associate = API.getAssociate(p).orElse(null);

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

						RoundTable table = KingdomAddon.getRoundTable();

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

			ClanAddon addon = ClanAddonQuery.getAddon(getName());

			if (!addon.isActive()) {
				LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.SYNCHRONOUS).run(subscription::remove);
				return;
			}

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
