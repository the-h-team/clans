package com.github.sanctum.clans.bridge.internal.kingdoms.command;

import com.github.sanctum.clans.bridge.ClanAddonQueue;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.internal.KingdomAddon;
import com.github.sanctum.clans.bridge.internal.kingdoms.Kingdom;
import com.github.sanctum.clans.bridge.internal.kingdoms.Progressive;
import com.github.sanctum.clans.bridge.internal.kingdoms.Quest;
import com.github.sanctum.clans.bridge.internal.kingdoms.event.KingdomCreatedEvent;
import com.github.sanctum.clans.bridge.internal.kingdoms.event.KingdomCreationEvent;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.api.Teleport;
import com.github.sanctum.clans.construct.util.MessagePrefix;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.formatting.pagination.EasyPagination;
import com.github.sanctum.labyrinth.formatting.string.DefaultColor;
import com.github.sanctum.labyrinth.formatting.string.RandomHex;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.panther.util.ProgressBar;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KingdomCommand extends ClanSubCommand implements Message.Factory {

	private final KingdomAddon addon;

	public KingdomCommand(KingdomAddon addon, String label) {
		super(label);
		this.addon = addon;
	}

	public static String getProgressBar(int currentValue, int maxValue, int maxBars) {
		return new ProgressBar().setProgress(currentValue).setGoal(maxValue).setBars(maxBars).setPercentPosition(ProgressBar.PERCENT_IN_MIDDLE).setPrefix("[").setSuffix("&r]").setSymbol('|').setFullColor("&a").setEmptyColor("&8").toString();
	}

	@Override
	public boolean player(Player p, String label, String[] args) {

		if (args.length == 0) {
			Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
			message()
					.append(text(" "))
					.send(p).deploy();
			if (associate != null) {
				if (associate.getClan().getValue(String.class, "kingdom") != null) {
					message()
							.append(text(" "))
							.append(text("[").color(Color.OLIVE))
							.append(text("Start").color(Color.MAROON).style(ChatColor.STRIKETHROUGH).bind(hover("You are already in a kingdom").style(DefaultColor.VELVET)))
							.append(text("]").color(Color.OLIVE))
							.append(text(" "))
							.append(text("Level: " + Kingdom.getKingdom(associate.getClan()).getLevel()))
							.send(p).deploy();
					message()
							.append(text(" "))
							.send(p).deploy();
					message()
							.append(text(" "))
							.append(text("|").color(Color.OLIVE))
							.append(text(" "))
							.append(text("Name: " + Kingdom.getKingdom(associate.getClan()).getName()))
							.append(text("\n"))
							.append(text(" "))
							.append(text("\n"))
							.append(text(" "))
							.append(text("|").color(Color.OLIVE))
							.append(text(" "))
							.append(text("King: &b" + Kingdom.getKingdom(associate.getClan()).getKing().map(Clan.Associate::getName).orElse("N/A")))
							.send(p).deploy();
					message()
							.append(text(" "))
							.append(text("|").color(Color.OLIVE))
							.append(text(" "))
							.append(text("Members: " + Kingdom.getKingdom(associate.getClan()).getMembers().stream().map(clan -> clan.getPalette().isGradient() ? clan.getPalette().toString(clan.getName()) : clan.getPalette() + clan.getName()).collect(Collectors.joining("&r, "))))
							.send(p).deploy();
					message()
							.append(text(" "))
							.send(p).deploy();
					message()
							.append(text(" "))
							.append(text("[").color(Color.OLIVE))
							.append(text("Leave").color(Color.FUCHSIA).bind(hover("Click to leave your current kingdom").style(new RandomHex())).bind(command("/c kingdom leave")))
							.append(text("]").color(Color.OLIVE))
							.send(p).deploy();
					message()
							.append(text(" "))
							.send(p).deploy();
					message()
							.append(text(" "))
							.append(text("[").color(Color.OLIVE))
							.append(text("Jobs").color(Color.FUCHSIA).bind(hover("Click to view the jobs your kingdom has available.").style(new RandomHex())).bind(command("/c kingdom jobs")))
							.append(text("]").color(Color.OLIVE))
							.send(p).deploy();

				} else {
					message()
							.append(text(" "))
							.append(text("[").color(Color.OLIVE))
							.append(text("Start").color(Color.FUCHSIA).bind(hover("Click to start a kingdom").style(new RandomHex())).bind(suggest("/c kingdom start ")))
							.append(text("]").color(Color.OLIVE))
							.send(p).deploy();
					message()
							.append(text(" "))
							.send(p).deploy();
					message()
							.append(text(" "))
							.append(text("[").color(Color.OLIVE))
							.append(text("Join").style(new RandomHex()).bind(hover("Click to join a kingdom").style(new RandomHex())).bind(suggest("/c kingdom join ")))
							.append(text("]").color(Color.OLIVE))
							.send(p).deploy();
				}
			} else {
				message()
						.append(text("You must own a clan to contribute to kingdom services.").style(DefaultColor.VELVET))
						.send(p).deploy();
			}
			message()
					.append(text(" "))
					.send(p).deploy();
			return true;
		}

		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("start")) {
				Clan.ACTION.sendMessage(p, "&cInvalid usage: &6/clan &7kingdom start <name>");
				return true;
			}

			if (args[0].equalsIgnoreCase("crown")) {
				Clan.ACTION.sendMessage(p, "&cInvalid usage: &6/clan &7kingdom crown <playerName>");
				return true;
			}

			if (args[0].equalsIgnoreCase("castle")) {
				Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
				if (associate != null) {
					Kingdom kingdom = Kingdom.getKingdom(associate.getClan());
					if (kingdom != null) {
						if (kingdom.getCastle() != null) {
							Teleport test = Teleport.get(associate);
							if (test == null) {
								Teleport n = new Teleport.Impl(associate, kingdom.getCastle());
								n.register(parent -> parent.getAsAssociate().getMailer().action("&aWelcome to the castle.").deploy());
								n.teleport();
							} else {
								sendMessage(p, "&cCannot teleport to the castle right now.");
							}
						} else {
							sendMessage(p, "&cOur kingdom doesn't have a kingdom location set.");
						}
					}
				} else {
					sendMessage(p, Clan.ACTION.notInClan());
				}
				return true;
			}

			if (args[0].equalsIgnoreCase("pvp")) {
				Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
				if (associate != null) {
					String k = associate.getClan().getValue("kingdom");
					if (k != null) {
						Kingdom kingdom = Kingdom.getKingdom(k);
						if (kingdom != null) {
							Clan.Associate king = kingdom.getKing().orElse(null);
							if (king != null && king.equals(associate)) {
								boolean result = !kingdom.isPeaceful();
								kingdom.setPeaceful(result);
								sendMessage(p, "&aKingdom pvp mode set to &f" + result);
							} else {
								sendMessage(p, Clan.ACTION.noClearance());
							}
						}
					}
				} else {
					sendMessage(p, Clan.ACTION.notInClan());
				}
			}

			if (args[0].equalsIgnoreCase("leave")) {

				Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

				if (associate != null) {

					Clan c = associate.getClan();

					String kindom = c.getValue(String.class, "kingdom");

					if (kindom != null) {

						if (!Clearance.LEAVE_KINGDOM.test(associate)) {
							Clan.ACTION.sendMessage(p, Clan.ACTION.noClearance());
							return true;
						}

						Kingdom k = Kingdom.getKingdom(kindom);

						k.getMembers().remove(c);

						c.removeValue("kingdom");

						Clan.ACTION.sendMessage(p, "&cYour clan is no longer a member of the kingdom.");

						if (k.getMembers().size() == 0) {
							k.remove(ClanAddonQueue.getInstance().get("Kingdoms"));
							Bukkit.getOnlinePlayers().forEach(pl -> Clan.ACTION.sendMessage(pl, "&2[&b" + k.getName() + "&2]&r &c&ohas fallen.."));
						}

					}
				}
				return true;
			}

			if (args[0].equalsIgnoreCase("jobs")) {

				Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

				if (associate != null) {

					Clan c = associate.getClan();

					String kingdom = c.getValue(String.class, "kingdom");

					if (kingdom != null) {

						Kingdom k = Kingdom.getKingdom(kingdom);

						EasyPagination<Quest> h = new EasyPagination<>(p, k.getQuests(), (o1, o2) -> o2.getTitle().compareTo(o1.getTitle()));
						h.limit(6);
						MessagePrefix prefix = ClansAPI.getInstance().getPrefix();
						message().append(text(prefix.getPrefix())).append(text(prefix.getText()).style(new RandomHex())).append(text(prefix.getSuffix())).append(text(" ")).append(text("|").style(ChatColor.BOLD)).append(text(" ")).append(text("Jobs").style(DefaultColor.MANGO)).send(p).deploy();
						h.setHeader((player, message) -> message.then("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
						h.setFormat((quest, placement, message) -> {
							Supplier<String> supplier = () -> {

								ItemStack item = (ItemStack) quest.getReward().get();

								if (item.getType() == Material.ENCHANTED_BOOK) {
									Map.Entry<Enchantment, Integer> entry = item.getEnchantments().entrySet().stream().findFirst().get();
									return item.getType().name().toLowerCase(Locale.ROOT).replace("_", " ") + " &f(&b" + entry.getKey().getKey().getKey() + " &fLvl." + entry.getValue() + ")";
								}
								return item.getType().name().toLowerCase(Locale.ROOT).replace("_", " ");
							};
							if (quest.activated(p)) {
								message.append(text("[").color(Color.MAROON))
										.append(text("#").color(Color.GRAY))
										.append(text(String.valueOf(placement)).color(Color.ORANGE).style(ChatColor.BOLD))
										.append(text("]").color(Color.MAROON))
										.append(text(" "))
										.append(text(quest.getTitle()).style(new RandomHex()).bind(hover(quest.getDescription()).style(new RandomHex())))
										.append(text(" "))
										.append(text("(").color(Color.ORANGE))
										.append(text(String.valueOf(quest.getProgression())))
										.append(text("/"))
										.append(text(String.valueOf(quest.getRequirement())))
										.append(text(")").color(Color.ORANGE))
										.append(text(" "))
										.append(text("█").color(Color.OLIVE).bind(hover("Reward: &f" + (quest.getReward().get().getClass().isArray() ? "Items" : (quest.getReward().get() instanceof ItemStack ? "Item" : "Money"))).color(Color.RED)).bind(hover((quest.getReward().get().getClass().isArray() ? "&cAmount: &e" + ((ItemStack[]) quest.getReward().get()).length : (quest.getReward().get() instanceof ItemStack ? "&cType: &e" + supplier.get() : quest.getReward().get().toString())))))
								        .append(text("\n"))
										.append(text("(").color(Color.MAROON))
										.append(text(getProgressBar(((Number) quest.getProgression()).intValue(), ((Number) quest.getRequirement()).intValue(), 73)).bind(hover("&cClick to quit job &3&l" + quest.getTitle())).bind(action(() -> {
											if (quest.deactivate(p)) {
												Clan.ACTION.sendMessage(p, "&cYou are no longer working job &e" + quest.getTitle());
											} else {
												Clan.ACTION.sendMessage(p, "&cYou aren't currently working job &e" + quest.getTitle());

											}
										})))
										.append(text(")").color(Color.MAROON))
										.append(text(" "));
							} else {
								message.append(text("[").color(Color.MAROON))
										.append(text("#").color(Color.GRAY))
										.append(text(String.valueOf(placement)).color(Color.ORANGE).style(ChatColor.BOLD))
										.append(text("]").color(Color.MAROON))
										.append(text(" "))
										.append(text(quest.getTitle()).style(new RandomHex()).bind(hover(quest.getDescription()).style(new RandomHex())))
										.append(text(" "))
										.append(text("(").color(Color.ORANGE))
										.append(text(String.valueOf(quest.getProgression())))
										.append(text("/"))
										.append(text(String.valueOf(quest.getRequirement())))
										.append(text(")").color(Color.ORANGE))
										.append(text(" "))
										.append(text("█").color(Color.OLIVE).bind(hover("Reward: &f" + (quest.getReward().get().getClass().isArray() ? "Items" : (quest.getReward().get() instanceof ItemStack ? "Item" : "Money"))).color(Color.RED)).bind(hover((quest.getReward().get().getClass().isArray() ? "&cAmount: &e" + ((ItemStack[]) quest.getReward().get()).length : (quest.getReward().get() instanceof ItemStack ? "&cType: &e" + supplier.get() : quest.getReward().get().toString())))))
								        .append(text("\n"))
										.append(text("(").color(Color.MAROON))
										.append(text(getProgressBar(((Number) quest.getProgression()).intValue(), ((Number) quest.getRequirement()).intValue(), 73)).bind(hover("&aClick to accept job &3&l" + quest.getTitle())).bind(action(() -> {
											if (quest.isComplete()) {
												Clan.ACTION.sendMessage(p, "&cThis quest is already complete!");
												return;
											}

											if (quest.activate(p)) {

												p.sendTitle(StringUtils.use(quest.getTitle()).translate(), StringUtils.use(quest.getDescription()).translate(), 60, 10, 60);


											} else {

												Clan.ACTION.sendMessage(p, "&cYou are already working job &e" + quest.getTitle());

											}
										})))
										.append(text(")").color(Color.MAROON))
										.append(text(" "));
							}
						});
						h.setFooter((player, message) -> message.then("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
						h.send(1);
					}


				}
				return true;
			}

			return true;
		}

		if (args.length == 2) {

			if (args[0].equalsIgnoreCase("castle")) {
				if (args[1].equalsIgnoreCase("set")) {
					Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

					if (associate != null) {
						Kingdom kingdom = Kingdom.getKingdom(associate.getClan());
						if (kingdom != null) {
							Clan.Associate king = kingdom.getKing().orElse(null);
							if (associate.equals(king)) {
								kingdom.setCastle(p.getLocation());
								sendMessage(p, "&aCastle location updated.");
							} else {
								sendMessage(p, Clan.ACTION.noClearance());
							}
						}
					}
					return true;
				}
			}

			if (args[0].equalsIgnoreCase("crown")) {
				Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

				if (associate != null) {

					Clan c = associate.getClan();

					String kingdom = c.getValue(String.class, "kingdom");

					if (kingdom != null) {

						Kingdom k = Kingdom.getKingdom(kingdom);

						if (k.getKing().isPresent()) {
							if (k.getKing().get().equals(associate)) {
								Clan.Associate test = k.getMembers().stream().filter(cl -> cl.getMember(a -> a.getName().equalsIgnoreCase(args[1])) != null).findFirst().map(clan -> clan.getMember(a -> a.getName().equalsIgnoreCase(args[1]))).orElse(null);
								if (test != null) {
									k.setKing(test);
									sendMessage(p, "&aPlayer " + args[1] + " is now the king.");
								} else {
									sendMessage(p, "&cPlayer " + args[1] + " is not apart of our kingdom.");
								}
							} else {
								sendMessage(p, Clan.ACTION.noClearance());
							}
						} else {
							if (Clearance.RESEAT_KINGDOM.test(associate)) {
								Clan.Associate test = k.getMembers().stream().filter(cl -> cl.getMember(a -> a.getName().equalsIgnoreCase(args[1])) != null).findFirst().map(clan -> clan.getMember(a -> a.getName().equalsIgnoreCase(args[1]))).orElse(null);
								if (test != null) {
									k.setKing(test);
									sendMessage(p, "&aPlayer " + args[1] + " is now the king.");
								} else {
									sendMessage(p, "&cPlayer " + args[1] + " is not apart of our kingdom.");
								}
							} else {
								sendMessage(p, Clan.ACTION.noClearance());
							}
						}
					}
				}
			}
			if (args[0].equalsIgnoreCase("join")) {

				Kingdom k = Kingdom.getKingdom(args[1]);

				if (k != null) {

					ClansAPI API = ClansAPI.getInstance();

					Clan.Associate associate = API.getAssociate(p).orElse(null);

					if (associate != null) {

						Clan c = associate.getClan();

						if (c.getValue(String.class, "kingdom") != null) {
							Clan.ACTION.sendMessage(p, "&cYou are already in a kingdom.");
							return true;
						}

						if (Clearance.JOIN_KINGDOM.test(associate)) {

							k.getMembers().add(c);

							c.setValue("kingdom", k.getName(), false);

							k.getMembers().forEach(cl -> cl.broadcast("&2[&b" + k.getName() + "&2]&r " + c.getName() + " vows protection to the kingdom."));

						} else {
							Clan.ACTION.sendMessage(p, Clan.ACTION.noClearance());
						}

					}

				} else {
					Clan.ACTION.sendMessage(p, "&cThis kingdom doesn't exist!");
				}
				return true;
			}


			if (args[0].equalsIgnoreCase("start")) {

				String name = args[1];

				ClansAPI API = ClansAPI.getInstance();

				Clan.Associate associate = API.getAssociate(p).orElse(null);

				if (associate != null) {


					Clan c = associate.getClan();

					Kingdom k = Kingdom.getKingdom(name);

					if (k == null) {

						if (Clearance.CREATE_KINGDOM.test(associate)) {
							Clan.ACTION.sendMessage(p, Clan.ACTION.noClearance());
							return true;
						}

						String kingdom = c.getValue(String.class, "kingdom");

						if (kingdom != null) {
							Clan.ACTION.sendMessage(p, "&cYou are already apart of a kingdom!");
							return true;
						}

						KingdomCreationEvent event = ClanVentBus.call(new KingdomCreationEvent(associate, name));
						if (!event.isCancelled()) {
							Kingdom create = new Kingdom(event.getKingdomName(), addon);
							create.setKing(associate);
							ClanVentBus.call(new KingdomCreatedEvent(p, create));
							c.setValue("kingdom", event.getKingdomName(), false);
							create.getMembers().add(c);
							Progressive.register(create);
							addon.getMailer().prefix().start(Clan.ACTION.getPrefix()).finish().announce(player -> true, p.getName() + " started a new kingdom called &6" + event.getKingdomName()).deploy();
						}

					} else {

						if (k.getMembers().contains(c)) {
							Clan.ACTION.sendMessage(p, "&cYou're already a member.");
						} else {
							Clan.ACTION.sendMessage(p, "&cThis kingdom already exists!");
						}

					}


				}
				return true;
			}

			if (args[0].equalsIgnoreCase("name")) {

				ClansAPI API = ClansAPI.getInstance();

				Clan.Associate associate = API.getAssociate(p).orElse(null);

				if (associate != null) {

					if (!Clearance.RENAME_KINGDOM.test(associate)) {
						Clan.ACTION.sendMessage(p, Clan.ACTION.noClearance());
						return true;
					}

					Clan c = associate.getClan();

					String kingdom = c.getValue(String.class, "kingdom");

					if (kingdom != null) {

						Kingdom k = Kingdom.getKingdom(kingdom);

						k.setName(args[1]);

						Clan.ACTION.sendMessage(p, "&aKingdom name changed to &b" + args[1]);

					}
				}
				return true;
			}

			if (args[0].equalsIgnoreCase("work")) {


				ClansAPI API = ClansAPI.getInstance();

				Clan.Associate associate = API.getAssociate(p).orElse(null);

				if (associate != null) {


					Clan c = associate.getClan();

					String kingdom = c.getValue(String.class, "kingdom");

					if (kingdom != null) {

						Kingdom k = Kingdom.getKingdom(kingdom);

						Quest achievement = k.getQuest(args[1]);

						if (achievement != null) {

							if (achievement.isComplete()) {
								Clan.ACTION.sendMessage(p, "&cThis quest is already complete!");
								return true;
							}

							if (achievement.activate(p)) {

								p.sendTitle(StringUtils.use(achievement.getTitle()).translate(), StringUtils.use(achievement.getDescription()).translate(), 60, 10, 60);


							} else {

								Clan.ACTION.sendMessage(p, "&cYou are already working job &e" + achievement.getTitle());

							}

						}
					}
				}
				return true;
			}

			if (args[0].equalsIgnoreCase("quit")) {

				ClansAPI API = ClansAPI.getInstance();

				Clan.Associate associate = API.getAssociate(p).orElse(null);

				if (associate != null) {


					Clan c = associate.getClan();

					String kingdom = c.getValue(String.class, "kingdom");

					if (kingdom != null) {

						Kingdom k = Kingdom.getKingdom(kingdom);

						Quest achievement = k.getQuest(args[1]);

						if (achievement != null) {

							if (achievement.deactivate(p)) {

								Clan.ACTION.sendMessage(p, "&cYou are no longer working job &e" + achievement.getTitle());


							} else {

								Clan.ACTION.sendMessage(p, "&cYou aren't currently working job &e" + achievement.getTitle());

							}

						}
					}
				}
				return true;
			}
			return true;
		}

		if (args.length == 3) {

			if (args[0].equalsIgnoreCase("work")) {

				ClansAPI API = ClansAPI.getInstance();

				Clan.Associate associate = API.getAssociate(p).orElse(null);

				if (associate != null) {

					Clan c = associate.getClan();

					String kingdom = c.getValue(String.class, "kingdom");

					if (kingdom != null) {

						Kingdom k = Kingdom.getKingdom(kingdom);

						Quest achievement = k.getQuest(args[1] + " " + args[2]);

						if (achievement != null) {

							if (achievement.isComplete()) {
								Clan.ACTION.sendMessage(p, "&cThis quest is already complete!");
								return true;
							}

							if (achievement.activate(p)) {

								p.sendTitle(StringUtils.use(achievement.getTitle()).translate(), StringUtils.use(achievement.getDescription()).translate(), 60, 10, 60);


							} else {

								Clan.ACTION.sendMessage(p, "&cYou are already working job &e" + achievement.getTitle());

							}

						}
					}
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("quit")) {

				ClansAPI API = ClansAPI.getInstance();

				Clan.Associate associate = API.getAssociate(p).orElse(null);

				if (associate != null) {


					Clan c = associate.getClan();

					String kingdom = c.getValue(String.class, "kingdom");

					if (kingdom != null) {

						Kingdom k = Kingdom.getKingdom(kingdom);

						Quest achievement = k.getQuest(args[1] + " " + args[2]);

						if (achievement != null) {

							if (achievement.deactivate(p)) {

								Clan.ACTION.sendMessage(p, "&cYou are no longer working job &e" + achievement.getTitle());


							} else {

								Clan.ACTION.sendMessage(p, "&cYou aren't currently working job &e" + achievement.getTitle());

							}

						}
					}
				}
				return true;
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("work")) {


			ClansAPI API = ClansAPI.getInstance();

			Clan.Associate associate = API.getAssociate(p).orElse(null);

			if (associate != null) {


				Clan c = associate.getClan();

				String kingdom = c.getValue(String.class, "kingdom");

				if (kingdom != null) {

					Kingdom k = Kingdom.getKingdom(kingdom);
					StringBuilder builder = new StringBuilder();
					for (int i = 1; i < args.length; i++) {
						if (i == args.length - 1) {
							builder.append(args[i]);
						} else {
							builder.append(args[i]).append(" ");
						}
					}

					Quest achievement = k.getQuest(builder.toString());

					if (achievement != null) {

						if (achievement.isComplete()) {
							Clan.ACTION.sendMessage(p, "&cThis quest is already complete!");
							return true;
						}

						if (achievement.activate(p)) {

							p.sendTitle(StringUtils.use(achievement.getTitle()).translate(), StringUtils.use(achievement.getDescription()).translate(), 60, 10, 60);


						} else {

							Clan.ACTION.sendMessage(p, "&cYou are already working job &e" + achievement.getTitle());

						}

					}
				}
			}
			return true;
		}

		if (args[0].equalsIgnoreCase("quit")) {

			ClansAPI API = ClansAPI.getInstance();

			Clan.Associate associate = API.getAssociate(p).orElse(null);

			if (associate != null) {


				Clan c = associate.getClan();

				String kingdom = c.getValue(String.class, "kingdom");

				if (kingdom != null) {

					Kingdom k = Kingdom.getKingdom(kingdom);

					StringBuilder builder = new StringBuilder();
					for (int i = 1; i < args.length; i++) {
						if (i == args.length - 1) {
							builder.append(args[i]);
						} else {
							builder.append(args[i]).append(" ");
						}
					}

					Quest achievement = k.getQuest(builder.toString());

					if (achievement != null) {

						if (achievement.deactivate(p)) {

							Clan.ACTION.sendMessage(p, "&cYou are no longer working job &e" + achievement.getTitle());


						} else {

							Clan.ACTION.sendMessage(p, "&cYou aren't currently working job &e" + achievement.getTitle());

						}

					}
				}
			}
			return true;
		}

		return true;
	}

	@Override
	public boolean console(CommandSender sender, String label, String[] args) {
		return false;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, getBaseCompletion(args))
				.then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, () -> Stream.of("start", "join", "castle", "crown", "leave", "work", "quit", "jobs", "pvp", "name").sorted(String::compareToIgnoreCase).collect(Collectors.toList()))
				.then(TabCompletionIndex.THREE, "castle", TabCompletionIndex.TWO, "set")
				.then(TabCompletionIndex.THREE, "work", TabCompletionIndex.TWO, () -> Arrays.stream(Quest.getDefaults()).map(Quest::getTitle).collect(Collectors.toList()))
				.then(TabCompletionIndex.THREE, "quit", TabCompletionIndex.TWO, () -> Arrays.stream(Quest.getDefaults()).map(Quest::getTitle).collect(Collectors.toList()))
				.get();
	}
}
