package com.github.sanctum.clans.bridge.internal.mail;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.formatting.PaginatedList;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Items;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.TextLib;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MailCommand extends ClanSubCommand {
	public MailCommand(String label) {
		super(label);
		setAliases(Arrays.asList("gift", "gifts", "mail", "retrieve"));
	}

	private List<String> helpMenu() {
		return new ArrayList<>(Arrays.asList("&8|&b)&6&o /clan &7mail view", "&8|&b)&6&o /clan &7mail list", "&8|&b)&6&o /clan &7mail send", "&8|&b)&6&o /clan &7mail read"));
	}
	
	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary u = Clan.ACTION;
		if (args.length == 0) {
			if (label.equalsIgnoreCase("gift")) {
				// need clan name
				u.sendMessage(p, "&a&oSend a clan the item you currently hold.");
				u.sendMessage(p, "&c&oInvalid usage: &6/clan &8gift &7<clanName>");
				return true;
				
			}
			if (label.equalsIgnoreCase("gifts")) {
				if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
					if (GiftBox.getGiftBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())) != null) {
						GiftBox box = GiftBox.getGiftBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()));
						List<String> array = new ArrayList<>();

						for (String s : box.getSenders()) {
							if (!array.contains(s)) {
								array.add(s);
							}
						}

						new PaginatedList<>(array)
								.limit(Clan.ACTION.menuSize())
								.start((pagination, page, max) -> {
									Message.form(p).send("&7&m------------&7&l[&3&oGift Box" + "&7&l]&7&m------------");
								}).finish(builder -> builder.setPlayer(p).setPrefix(Clan.ACTION.menuBorder())).decorate((pagination, string, page, max, placement) -> {

							TextLib.consume(t -> {

								Message.form(p).build(t.textRunnable("&aGift from ", "&b" + string, "&7Click to view gifts from &6" + string, "c gifts " + string));

							});

						}).get(1);
					} else {
						u.sendMessage(p, "&c&oYour clan has no gifts to collect.");
						return true;
						
					}
				} else {
					u.sendMessage(p, u.notInClan());
					return true;
					
				}
				return true;
				
			}
			if (label.equalsIgnoreCase("mail")) {
				if (!ClansAPI.getInstance().isInClan(p.getUniqueId())) {
					u.sendMessage(p, u.notInClan());
					return true;
					
				}
				new PaginatedList<>(helpMenu())
						.limit(Clan.ACTION.menuSize())
						.start((pagination, page, max) -> {
							Message.form(p).send("&7&m------------&7&l[&6&oMail Help&7&l]&7&m------------");
						}).finish(builder -> builder.setPlayer(p).setPrefix(Clan.ACTION.menuBorder())).decorate((pagination, string, page, max, placement) -> {

					TextLib.consume(t -> {

						Message.form(p).send(string);

					});

				}).get(1);
				return true;
				
			}
		}
		if (args.length == 1) {

			if (label.equalsIgnoreCase("gifts")) {
				if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
					if (GiftBox.getGiftBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())) != null) {
						try {
							if (GiftBox.getGiftBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())) != null) {
								GiftBox box = GiftBox.getGiftBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()));
								List<String> array = new ArrayList<>();

								for (String s : box.getSenders()) {
									if (!array.contains(s)) {
										array.add(s);
									}
								}
								new PaginatedList<>(array)
										.limit(Clan.ACTION.menuSize())
										.start((pagination, page, max) -> {
											Message.form(p).send("&7&m------------&7&l[&3&oGift Box" + "&7&l]&7&m------------");
										}).finish(builder -> builder.setPlayer(p).setPrefix(Clan.ACTION.menuBorder())).decorate((pagination, string, page, max, placement) -> {

									TextLib.consume(t -> {

										Message.form(p).build(t.textRunnable("&aGift from ", "&b" + string, "&7Click to view gifts from &6" + string, "c gifts " + string));

									});

								}).get(Integer.parseInt(args[0]));
							} else {
								u.sendMessage(p, "&c&oYour clan has no gifts to collect.");
								return true;
								
							}
						} catch (NumberFormatException ex) {
							if (ClansAPI.getInstance().getClanManager().getClanID(args[0]) == null) {
								u.sendMessage(p, u.clanUnknown(args[0]));
								return true;
								
							}

							Clan target = ClansAPI.getInstance().getClanManager().getClan(ClansAPI.getInstance().getClanManager().getClanID(args[0]));

							GiftBox box = GiftBox.getGiftBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()));
							List<String> array = box.getInboxByClan(target.getName()).stream().map(GiftObject::getItem).map(ItemStack::getType).map(Material::name).collect(Collectors.toList());

							if (array.isEmpty()) {
								u.sendMessage(p, "&c&oYour clan has no gifts from " + target.getName());
								return true;
								
							}

							new PaginatedList<>(array)
									.limit(Clan.ACTION.menuSize())
									.start((pagination, page, max) -> {
										Message.form(p).send("&7&m------------&7&l[&3&oGifts from " + target.getName() + "&7&l]&7&m------------");
									}).finish(builder -> builder.setPlayer(p).setPrefix(Clan.ACTION.menuBorder())).decorate((pagination, string, page, max, placement) -> {

								TextLib.consume(t -> {

									Message.form(p).build(t.textRunnable("&aGift: ", "&b" + string, "&7Click to retrieve gift &6" + string, "c retrieve " + target.getName() + " " + string));

								});

							}).get(1);
						}

					} else {
						u.sendMessage(p, "&c&oYour clan has no gifts to collect.");
						return true;
						
					}
				}
				return true;
				
			}

			if (label.equalsIgnoreCase("gift")) {
				if (ClansAPI.getInstance().getClanManager().getClanID(args[0]) == null) {
					u.sendMessage(p, u.clanUnknown(args[0]));
					return true;
					
				}

				Clan target = ClansAPI.getInstance().getClanManager().getClan(ClansAPI.getInstance().getClanManager().getClanID(args[0]));


				if (target.getId().toString().equals(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).getId().toString())) {
					u.sendMessage(p, "&c&oYou cannot send stuff to your own clan.");
					return true;
					
				}


				if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
					Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

					assert associate != null;

					if (GiftBox.getGiftBox(target) != null) {
						GiftBox box = GiftBox.getGiftBox(target);
						if (box.getInboxByClan(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).getName()).stream().map(GiftObject::getItem).map(ItemStack::getType).collect(Collectors.toList()).contains(p.getInventory().getItemInMainHand().getType())) {
							u.sendMessage(p, "&c&oA gift of this caliber has already been sent to the clan, try something else.");
							return true;
							
						}
						if (p.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
							u.sendMessage(p, "&c&oYou must be holding something to send as a gift.");
							return true;
							
						}
						if (!Clearance.MANAGE_GIFTING.test(associate)) {
							Clan.ACTION.sendMessage(p, u.noClearance());
							
						}
						box.receive(new GiftObject(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).getName(), p.getInventory().getItemInMainHand()));
					} else {
						if (!Clearance.MANAGE_GIFTING.test(associate)) {
							Clan.ACTION.sendMessage(p, u.noClearance());
							
						}
						GiftBox box = new GiftBox(target);
						box.receive(new GiftObject(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).getName(), p.getInventory().getItemInMainHand()));
					}
					p.getInventory().getItemInMainHand().setAmount(0);
					u.sendMessage(p, "&3&oYour gift was sent directly to the clan.");
					target.broadcast("&b&oWe have received a new gift.");
				} else {
					u.sendMessage(p, u.notInClan());
					return true;
					
				}
				return true;
				
			}

			if (label.equalsIgnoreCase("mail")) {
				if (!ClansAPI.getInstance().isInClan(p.getUniqueId())) {
					u.sendMessage(p, u.notInClan());
					return true;
					
				}
				if (args[0].equalsIgnoreCase("read")) {
					u.sendMessage(p, "&c&oInvalid usage: &8&o/clan mail read &7<clanName> <topic>");
					return true;
					
				}

				if (args[0].equalsIgnoreCase("send")) {
					u.sendMessage(p, "&c&oInvalid usage: &8&o/clan mail send &7<clanName> <topic> <context...>");
					return true;
					
				}
				if (args[0].equalsIgnoreCase("view")) {

					if (MailBox.getMailBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())) != null) {
						List<String> array = new ArrayList<>();

						for (String s : MailBox.getMailBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())).getMailList()) {
							if (!array.contains(s)) {
								array.add(s);
							}
						}

						new PaginatedList<>(array)
								.limit(Clan.ACTION.menuSize())
								.start((pagination, page, max) -> {
									Message.form(p).send("&7&m------------&7&l[&6&oInbox&7&l]&7&m------------");
								}).finish(builder -> builder.setPlayer(p).setPrefix(Clan.ACTION.menuBorder())).decorate((pagination, string, page, max, placement) -> {

							TextLib.consume(t -> {

								Message.form(p).build(t.textRunnable("&aMessage(s) from ", "&b" + string, "&7Click to view mail from &6" + string, "c mail list " + string));

							});

						}).get(1);
					} else {
						new MailBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()));
						u.sendMessage(p, "&6&oA new clan mail box was built.");
					}
					return true;
					
				}
				try {
					Integer.parseInt(args[0]);
				} catch (NumberFormatException ex) {
					u.sendMessage(p, "&c&oUnknown page number.");
					return true;
					
				}
				new PaginatedList<>(helpMenu())
						.limit(Clan.ACTION.menuSize())
						.start((pagination, page, max) -> {
							Message.form(p).send("&7&m------------&7&l[&6&oMail Help&7&l]&7&m------------");
						}).finish(builder -> builder.setPlayer(p).setPrefix(Clan.ACTION.menuBorder())).decorate((pagination, string, page, max, placement) -> {

					TextLib.consume(t -> {

						Message.form(p).send(string);

					});

				}).get(1);
				return true;
				
			}
		}

		if (args.length == 2) {

			if (label.equalsIgnoreCase("gifts")) {
				if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
					if (GiftBox.getGiftBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())) != null) {
						try {
							if (ClansAPI.getInstance().getClanManager().getClanID(args[0]) == null) {
								u.sendMessage(p, u.clanUnknown(args[0]));
								return true;
								
							}

							Clan target = ClansAPI.getInstance().getClanManager().getClan(ClansAPI.getInstance().getClanManager().getClanID(args[0]));

							GiftBox box = GiftBox.getGiftBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()));
							List<String> array = new ArrayList<>();

							for (String s : box.getInboxByClan(target.getName()).stream().map(GiftObject::getItem).map(ItemStack::getType).map(Material::name).collect(Collectors.toList())) {
								if (!array.contains(s)) {
									array.add(s);
								}
							}

							if (array.isEmpty()) {
								u.sendMessage(p, "&c&oYour clan has no gifts from " + target.getName());
								return true;
								
							}

							new PaginatedList<>(array)
									.limit(Clan.ACTION.menuSize())
									.start((pagination, page, max) -> {
										Message.form(p).send("&7&m------------&7&l[&3&oGifts from " + target.getName() + "&7&l]&7&m------------");
									}).finish(builder -> builder.setPlayer(p).setPrefix(Clan.ACTION.menuBorder())).decorate((pagination, string, page, max, placement) -> {

								TextLib.consume(t -> {

									Message.form(p).build(t.textRunnable("&aGift: ", "&b" + string, "&7Click to retrieve gift &6" + string, "c retrieve " + target.getName() + " " + string));

								});

							}).get(Integer.parseInt(args[1]));
						} catch (NumberFormatException ex) {
							u.sendMessage(p, "&c&oUnknown page number.");
							return true;
							
						}

					} else {
						u.sendMessage(p, "&c&oYour clan has no gifts to collect.");
						return true;
						
					}
				}
				return true;
				
			}

			if (label.equalsIgnoreCase("retrieve")) {
				if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
					if (GiftBox.getGiftBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())) != null) {
						GiftBox box = GiftBox.getGiftBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()));

						if (ClansAPI.getInstance().getClanManager().getClanID(args[0]) == null) {
							u.sendMessage(p, u.clanUnknown(args[0]));
							return true;
							
						}

						Clan target = ClansAPI.getInstance().getClanManager().getClan(ClansAPI.getInstance().getClanManager().getClanID(args[0]));

						if (box.getInboxByClan(target.getName()).isEmpty()) {
							u.sendMessage(p, "&c&oClan " + args[0] + " hasn't sent you any more gifts.");
							return true;
							
						}

						if (box.getGiftByMaterial(Items.findMaterial(args[1].toUpperCase())) == null) {
							u.sendMessage(p, "&c&oNo gift was found by this type");
							return true;
							
						}

						GiftObject item = box.getGiftByMaterial(Items.findMaterial(args[1]));
						p.getInventory().addItem(item.getItem());
						p.sendMessage(" ");
						Bukkit.getScheduler().scheduleSyncDelayedTask(ClansAPI.getInstance().getPlugin(), () -> u.sendMessage(p, "&3&oYou have collected &b" + item.getItem().getType() + " &3&ofrom clan " + item.getSender()), 2);
						u.sendComponent(p, TextLib.getInstance().textRunnable("&7Reopen the clan gift box ", "&f[&bCLICK&f]", "Click to reopen the clan gift box.", "c gifts " + args[0]));
						box.mark(item);
						p.sendMessage(" ");
					} else {
						u.sendMessage(p, "&c&oYour clan has no gifts to collect.");
						return true;
						
					}
				} else {
					u.sendMessage(p, u.notInClan());
					return true;
					
				}
				return true;
				
			}

			if (label.equalsIgnoreCase("mail")) {
				if (!ClansAPI.getInstance().isInClan(p.getUniqueId())) {
					u.sendMessage(p, u.notInClan());
					
				}
				if (args[0].equalsIgnoreCase("list")) {
					if (MailBox.getMailBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())) == null) {
						
					}
					if (MailBox.getMailBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())).getInboxByClan(args[1]) == null) {
						
					}
					List<String> array = MailBox.getMailBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())).getInboxByClan(args[0]).stream().map(MailObject::getTopic).collect(Collectors.toList());

					if (array.isEmpty()) {
						u.sendMessage(p, "&c&oYou have no unread messages from " + args[1]);
						return true;
						
					}

					new PaginatedList<>(array)
							.limit(Clan.ACTION.menuSize())
							.start((pagination, page, max) -> {
								Message.form(p).send("&7&m------------&7&l[&3&oMail from " + args[1] + "&7&l]&7&m------------");
							}).finish(builder -> builder.setPlayer(p).setPrefix(Clan.ACTION.menuBorder())).decorate((pagination, string, page, max, placement) -> {

						TextLib.consume(t -> {

							Message.form(p).build(t.textRunnable("", "&b" + string, "&7Click to read message &6" + string, "c mail read " + args[1] + " " + string));

						});

					}).get(1);
					return true;
					
				}

				if (args[0].equalsIgnoreCase("view")) {
					try {
						Integer.parseInt(args[1]);
					} catch (NumberFormatException ex) {
						u.sendMessage(p, "&c&oUnknown page number.");
						return true;
						
					}
					if (MailBox.getMailBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())) != null) {

						List<String> array = new ArrayList<>();

						for (String s : MailBox.getMailBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())).getMailList()) {
							if (!array.contains(s)) {
								array.add(s);
							}
						}

						new PaginatedList<>(array)
								.limit(Clan.ACTION.menuSize())
								.start((pagination, page, max) -> {
									Message.form(p).send("&7&m------------&7&l[&6&oInbox&7&l]&7&m------------");
								}).finish(builder -> builder.setPlayer(p).setPrefix(Clan.ACTION.menuBorder())).decorate((pagination, string, page, max, placement) -> {

							TextLib.consume(t -> {

								Message.form(p).build(t.textRunnable("&aMessage(s) from ", "&b" + string, "&7Click to view mail from &6" + string, "c mail list " + string));

							});

						}).get(Integer.parseInt(args[1]));
					} else {
						new MailBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()));
						u.sendMessage(p, "&6&oA new clan mail box was built.");
					}
					return true;
					
				}

				if (args[0].equalsIgnoreCase("read")) {
					u.sendMessage(p, "&c&oInvalid usage: &8&o/clan mail read &7<clanName> <topic>");
					return true;
					
				}

				if (args[0].equalsIgnoreCase("send")) {
					u.sendMessage(p, "&c&oInvalid usage: &8&o/clan mail send &7<clanName> <topic> <context...>");
					return true;
					
				}
				u.sendMessage(p, u.commandUnknown("clan"));
				return true;
				
			}
		}

		if (args.length > 2) {
			if (label.equalsIgnoreCase("mail")) {
				if (!ClansAPI.getInstance().isInClan(p.getUniqueId())) {
					u.sendMessage(p, u.notInClan());
					
				}
				if (args[0].equalsIgnoreCase("list")) {
					try {
						Integer.parseInt(args[2]);
					} catch (NumberFormatException ex) {
						u.sendMessage(p, "&c&oUnknown page number.");
						return true;
						
					}
					if (MailBox.getMailBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())) == null) {
						
					}
					if (MailBox.getMailBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())).getInboxByClan(args[1]) == null) {
						
					}
					List<String> array = MailBox.getMailBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())).getInboxByClan(args[1]).stream().map(MailObject::getTopic).collect(Collectors.toList());
					new PaginatedList<>(array)
							.limit(Clan.ACTION.menuSize())
							.start((pagination, page, max) -> {
								Message.form(p).send("&7&m------------&7&l[&3&oMail from " + args[1] + "&7&l]&7&m------------");
							}).finish(builder -> builder.setPlayer(p).setPrefix(Clan.ACTION.menuBorder())).decorate((pagination, string, page, max, placement) -> {

						TextLib.consume(t -> {

							Message.form(p).build(t.textRunnable("", "&b" + string, "&7Click to read message &6" + string, "c mail read " + args[1] + " " + string));

						});

					}).get(Integer.parseInt(args[2]));
					return true;
					
				}
				if (args[0].equalsIgnoreCase("read")) {
					if (MailBox.getMailBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())) == null) {
						u.sendMessage(p, "&c&oWe don't have a mail box.");
						
					}
					if (MailBox.getMailBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId())).getInboxByClan(args[1]) == null) {
						u.sendMessage(p, "&c&oWe don't have a mail box for the given clan.");
						
					}

					HUID clanID = ClansAPI.getInstance().getClanManager().getClanID(args[1]);

					if (clanID == null) {
						u.sendMessage(p, "&c&oClan " + args[1] + " not found.");
						
					}

					MailBox box = MailBox.getMailBox(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()));
					MailObject mail = box.getMailByTopic(args[2]);
					if (box.getMailByTopic(args[2]) == null) {
						// mail not found.
						u.sendMessage(p, "&c&oMail not found.");
						return true;
						
					}
					p.sendMessage(" ");
					u.sendMessage(p, mail.getTopic() + " :\n" + mail.getContext());
					p.sendMessage(" ");
					u.sendMessage(p, "&e&oMessage marked as read.");
					u.sendComponent(p, TextLib.getInstance().textRunnable("&7Reopen the clan inbox ", "&f[&bCLICK&f]", "Click to reopen the clan inbox.", "c mail list " + args[1]));
					box.markRead(mail);
					return true;
					
				}
				if (args[0].equalsIgnoreCase("send")) {
					String name = args[1];
					HUID clanID = ClansAPI.getInstance().getClanManager().getClanID(name);
					if (clanID == null) {
						u.sendMessage(p, "&c&oClan " + name + " not found.");
						
					}

					if (clanID.equals(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).getId().toString())) {
						u.sendMessage(p, "&c&oYou cannot send stuff to your own clan.");
						return true;
						
					}


					StringBuilder builder = new StringBuilder();
					for (int i = 4; i < args.length; i++) {
						builder.append(args[i]).append(" ");
					}
					String result = builder.substring(0, builder.toString().length() - 1);
					Clan target = ClansAPI.getInstance().getClanManager().getClan(clanID);
					MailBox box = MailBox.getMailBox(target);
					if (box == null) {
						// no target mail box make one a send mail
						box = new MailBox(target);
					}

					if (box.getInboxByClan(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).getName()).stream().map(MailObject::getTopic).collect(Collectors.toList()).contains(args[2])) {
						u.sendMessage(p, "&c&oMail with this topic has already been sent.");
						return true;
						
					}

					box.sendMail(new MailObject(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).getName(), args[2], result));
					u.sendMessage(p, "Mail sent to clan " + target.getName() + " with topic " + args[2] + " and context : " + result);
					target.broadcast("&e&oNew incoming mail from clan &6" + ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).getName());
					return true;
					
				}
				u.sendMessage(p, u.commandUnknown("clan"));
				return true;
			}

		}
		
		return true;
	}

	@Override
	public boolean console(CommandSender sender, String label, String[] args) {
		return false;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return null;
	}
}
