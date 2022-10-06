package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.bridge.internal.stashes.events.StashInteractEvent;
import com.github.sanctum.clans.bridge.internal.vaults.events.VaultInteractEvent;
import com.github.sanctum.clans.construct.api.AbstractGameRule;
import com.github.sanctum.clans.construct.api.Channel;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBlueprint;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Consultant;
import com.github.sanctum.clans.construct.api.Ticket;
import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.clans.construct.extra.AnimalConsultantListener;
import com.github.sanctum.clans.construct.extra.InfoSection;
import com.github.sanctum.clans.construct.impl.CooldownCreate;
import com.github.sanctum.clans.construct.impl.SimpleEntry;
import com.github.sanctum.clans.event.associate.AssociateDisplayInfoEvent;
import com.github.sanctum.clans.event.associate.AssociateFromAnimalEvent;
import com.github.sanctum.clans.event.associate.AssociateMessageReceiveEvent;
import com.github.sanctum.clans.event.associate.AssociateObtainLandEvent;
import com.github.sanctum.clans.event.claim.ClaimInteractEvent;
import com.github.sanctum.clans.event.claim.ClaimResidentEvent;
import com.github.sanctum.clans.event.claim.WildernessInhabitantEvent;
import com.github.sanctum.clans.event.clan.ClanFreshlyFormedEvent;
import com.github.sanctum.clans.event.clan.ClansLoadingProcedureEvent;
import com.github.sanctum.clans.event.player.PlayerCreateClanEvent;
import com.github.sanctum.clans.event.war.WarActiveEvent;
import com.github.sanctum.clans.event.war.WarStartEvent;
import com.github.sanctum.clans.event.war.WarWonEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.FancyMessageChain;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.interfacing.Nameable;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.TimeWatch;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.event.Subscribe;
import com.github.sanctum.panther.event.Vent;
import com.github.sanctum.panther.file.Configurable;
import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class ClanEventListener implements Listener {

	private String calc(long i) {
		String val = String.valueOf(i);
		int size = String.valueOf(i).length();
		if (size == 1) {
			val = "0" + i;
		}
		return val;
	}

	@Subscribe
	public void onInteract(ClaimInteractEvent e) {
		if (e.getAssociate() != null) {
			if (e.getAssociate().getClan().equals(e.getClan())) {
				Claim.Flag f = e.getClaim().getFlag("owner-only");
				if (f.isValid()) {
					if (f.isEnabled()) {
						if (e.getAssociate().getPriority().toLevel() != 3) {
							Clan.ACTION.sendMessage(e.getPlayer(), "&cThis is a clan owner only chunk! You can't do this here.");
							e.setCancelled(true);
						}
					}
				}
				Claim.Flag flam = e.getClaim().getFlag("no-flammables");
				if (flam.isValid()) {
					if (flam.isEnabled()) {
						if (e.getInteraction() == ClaimInteractEvent.Type.USE) {
							if (e.getItemInMainHand().getType() == Material.LAVA_BUCKET) {
								Clan.ACTION.sendMessage(e.getPlayer(), "&cFlammables aren't allowed within this chunk.");
								e.setCancelled(true);
							}
							return;
						}
						if (e.getItemInMainHand().getType() == Material.FLINT_AND_STEEL || e.getItemInMainHand().getType() == Material.FIRE_CHARGE) {
							Clan.ACTION.sendMessage(e.getPlayer(), "&cFlammables aren't allowed within this chunk.");
							e.setCancelled(true);
						}
					}
				}
			}
		}
	}

	@Subscribe(priority = Vent.Priority.LOW)
	public void onFix(ClaimResidentEvent e) {
		Claim c = e.getClaim();
		for (Claim.Flag loading : e.getApi().getClaimManager().getFlagManager().getFlags()) {
			if (c.getFlag(loading.getId()) == null) {
				c.register(loading);
			}
		}
	}

	@Subscribe(priority = Vent.Priority.LOW)
	public void onFix(WildernessInhabitantEvent e) {
		if (e.getPreviousClaim() == null) return;
		Claim c = e.getPreviousClaim();
		for (Claim.Flag loading : e.getApi().getClaimManager().getFlagManager().getFlags()) {
			if (c.getFlag(loading.getId()) == null) {
				c.register(loading);
			}
		}
	}

	@Subscribe(priority = Vent.Priority.HIGHEST)
	public void onTitle(ClaimResidentEvent e) {
		Clan owner = ((Clan) e.getClaim().getHolder());
		if (e.getClaim().getFlag("custom-titles").isEnabled()) {
			if (owner.getValue(String.class, "claim_title") != null) {
				if (owner.getValue(String.class, "claim_sub_title") != null) {
					e.setClaimTitle(owner.getValue(String.class, "claim_title").replace("{*}", owner.getName()), owner.getValue(String.class, "claim_sub_title").replace("{*}", owner.getName()));
				} else {
					e.setClaimTitle(owner.getValue(String.class, "claim_title").replace("{*}", owner.getName()), e.getClaimSubTitle());
				}
			}
		}
	}

	@Subscribe(priority = Vent.Priority.HIGHEST)
	public void onTitle(WildernessInhabitantEvent e) {
		if (e.getPreviousClaim() == null) return;
		Clan owner = e.getClan();
		if (e.getPreviousClaim().getFlag("custom-titles").isEnabled()) {
			if (owner.getValue(String.class, "leave_claim_title") != null) {
				if (owner.getValue(String.class, "leave_claim_sub_title") != null) {
					e.setWildernessTitle(owner.getValue(String.class, "leave_claim_title").replace("{*}", owner.getName()), owner.getValue(String.class, "leave_claim_sub_title").replace("{*}", owner.getName()));
				} else {
					e.setWildernessTitle(owner.getValue(String.class, "leave_claim_title").replace("{*}", owner.getName()), e.getWildernessSubTitle());
				}
			}
		}
	}

	@Subscribe
	public void onAnimal(AssociateFromAnimalEvent e) {
		Consultant consultant = e.getAssociate().getConsultant();
		if (!consultant.hasIncomingListener(e.getAssociate().getTag())) {
			AnimalConsultantListener listener = new AnimalConsultantListener(e.getAssociate()); // Register the default animal consultant listeners.
			consultant.registerOutgoingListener(e.getAssociate().getTag(), listener);
			consultant.registerIncomingListener(e.getAssociate().getTag(), listener);
		}
	}

	@Subscribe(priority = Vent.Priority.READ_ONLY)
	public void onClaim(AssociateObtainLandEvent e) {
		for (Claim.Flag f : e.getApi().getClaimManager().getFlagManager().getFlags()) {
			if (e.getClaim().getFlag(f.getId()) == null) {
				e.getClaim().register(f);
			} else {
				if (!e.getClaim().getFlag(f.getId()).isValid()) {
					e.getClaim().remove(e.getClaim().getFlag(f.getId()));
					e.getClaim().register(f);
				}
			}
		}
	}

	@Vent.Disabled
	@Subscribe
	public void onLoad(ClansLoadingProcedureEvent e) {
		if (e.getClans().stream().noneMatch(c -> c.getName().equals("Labyrinth"))) {
			UUID server = e.getApi().getSessionId();
			ClanBlueprint blueprint = new ClanBlueprint("Labyrinth", true).setLeader(server); //TODO: fix blueprint to allow consumers.
			Clan clan = blueprint.toBuilder().build().givePower(4.2).getClan();
			e.insert(clan);
		}
		if (ClansAPI.getDataInstance().isTrue("Formatting.console-debug")) {
			e.getClans().forEach(clan -> e.getApi().debugConsole(clan, true));
		}
	}

	@Subscribe(priority = Vent.Priority.LOW)
	public void onChat(AssociateMessageReceiveEvent e) {
		TaskScheduler.of(() -> {
			Consultant[] server = e.getAssociate().getConsultants();
			if (server != null) { // Our server associate isn't null
				for (Consultant c : server) {
					c.sendMessage(() -> new SimpleEntry<>(e.getMessage(), new SimpleEntry<>(e.getChannel(), e.getSender()))); // Send them the message & ticket a response from the server.
				}
			}
		}).schedule();
	}

	@Subscribe
	public void onSpawn(AssociateFromAnimalEvent e) {
		Consultant associate = (Consultant) e.getAssociate();
		associate.registerIncomingListener(() -> "BOOP", object -> {
			Ticket ticket = new Ticket();
			if (object instanceof SimpleEntry) {
				SimpleEntry<String, SimpleEntry<Channel, Clan.Associate>> entry = (SimpleEntry<String, SimpleEntry<Channel, Clan.Associate>>) object;
				if (entry.getKey().equalsIgnoreCase("hello")) {
					ticket.setType(Ticket.Field.STRING, "I don't know");
					ticket.setType(Ticket.Field.CUSTOM, new SimpleEntry<>(entry.getValue().getKey(), entry.getValue().getValue()));
				}
			}
			return ticket;
		});
	}

	@Subscribe(priority = Vent.Priority.LOW)
	public void onInfo(AssociateDisplayInfoEvent e) {
		Player p = e.getPlayer();
		Clan c = e.getClan();

		if (ClansAPI.getDataInstance().isTrue("Formatting.pretty-info")) {
			e.setCancelled(true);
			//=======================
			//
			//       (ClanName)
			//
			//        [Stats]
			// [Roster]     [Perms]
			//        [Mode]
			// [Bank] [Base] [Vault]
			//       [Stash]
			//
			//=======================
			String color;
			FancyMessageChain chain = null;
			String idMode = ClansAPI.getDataInstance().ID_MODE.containsKey(p) ? " &a(&e*" + c.getId() + "&a)" : "";
			Configurable configurable = ClansAPI.getDataInstance().getMessages().getRoot();
			switch (e.getType()) {
				case OTHER:
					color = "&2";
					InfoSection section_1 = new InfoSection(configurable.getNode("info-other.line-1"));
					FancyMessage section_1_msg = new FancyMessage();
					if (section_1.getPrefix() != null) section_1_msg.then(section_1.getPrefix());
					if (section_1.getText() != null) {
						section_1_msg.then(section_1.getText());
						if (!section_1.getHover().isEmpty()) {
							section_1.getHover().forEach(s -> {
								section_1_msg.hover(s.replace("{0}", c.getName() + idMode)
										.replace("{1}", c.getDescription())
										.replace("{2}", Clan.ACTION.format(c.getPower()))
										.replace("{3}", (c.getPalette().isGradient() ? c.getPalette().toString((c.getPalette().toArray()[0]).replace("&", "").replace("#", "")) + "&f»" + c.getPalette().toString((c.getPalette().toArray()[1]).replace("&", "").replace("#", "")) : color.replace("&", "&f»" + color).replace("#", "&f»" + color)))
										.replace("{4}", String.valueOf(c.getClaims().length))
										.replace("{5}", String.valueOf(c.getClaimLimit()))
										.replace("{9}", c.getPalette().getStart()));
							});
						}
						if (section_1.getCommand() != null) {
							section_1_msg.command(section_1.getCommand());
						}
						if (section_1.getSuggestion() != null) {
							section_1_msg.suggest(section_1.getSuggestion());
						}
						if (section_1.getCopyText() != null) {
							section_1_msg.copy(section_1.getCopyText());
						}
						if (section_1.getUrlCopyText() != null) {
							section_1_msg.url(section_1.getUrlCopyText());
						}
					}
					if (section_1.getSuffix() != null) section_1_msg.then(section_1.getSuffix());
					InfoSection section_2 = new InfoSection(configurable.getNode("info-other.line-2"));
					InfoSection section_3 = new InfoSection(configurable.getNode("info-other.line-3"));
					InfoSection section_4 = new InfoSection(configurable.getNode("info-other.line-4"));
					FancyMessage section_3_msg = new FancyMessage();
					if (section_3.getPrefix() != null) section_3_msg.then(section_3.getPrefix());
					if (section_3.getText() != null) {
						section_3_msg.then(section_3.getText());
						if (!section_3.getHover().isEmpty()) {
							section_3.getHover().forEach(s -> {
								section_3_msg.hover(s.replace("{0}", c.getName())
										.replace("{1}", c.getDescription())
										.replace("{2}", Clan.ACTION.format(c.getPower()))
										.replace("{3}", (c.getPalette().isGradient() ? c.getPalette().toString((c.getPalette().toArray()[0]).replace("&", "").replace("#", "")) + "&f»" + c.getPalette().toString((c.getPalette().toArray()[1]).replace("&", "").replace("#", "")) : color.replace("&", "&f»" + color).replace("#", "&f»" + color)))
										.replace("{4}", String.valueOf(c.getClaims().length))
										.replace("{5}", String.valueOf(c.getClaimLimit()))
										.replace("{9}", c.getPalette().getStart()));
							});
						}
						if (section_3.getCommand() != null) {
							section_3_msg.command(section_3.getCommand().replace("{0}", c.getName()));
						}
						if (section_3.getSuggestion() != null) {
							section_3_msg.suggest(section_3.getSuggestion());
						}
						if (section_3.getCopyText() != null) {
							section_3_msg.copy(section_3.getCopyText());
						}
						if (section_3.getUrlCopyText() != null) {
							section_3_msg.url(section_3.getUrlCopyText());
						}
					}
					if (section_3.getSuffix() != null) section_3_msg.then(section_3.getSuffix());
					chain = new FancyMessageChain()
							.append(space -> space.then(" "))
							.append(top -> {
								top.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ");
								if (section_1.isValid()) {
									top.append(section_1_msg);
								}
								top.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ");
							})
							.append(space -> {
								if (section_2.isValid()) {
									space.append(section_2.toMsg());
								}
							})
							.append(middle -> {
								middle.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ");
								if (section_3.isValid()) {
									middle.append(section_3_msg);
								}
								middle.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ");
							})
							.append(space -> {
								if (section_4.isValid()) {
									space.append(section_4.toMsg());
								}
							});
					break;
				case PERSONAL:
					String allies = c.getRelation().getAlliance().stream().map(Nameable::getName).collect(Collectors.joining(", "));
					String alliesR = c.getRelation().getAlliance().getRequests().stream().map(Nameable::getName).collect(Collectors.joining(", "));
					String enemies = c.getRelation().getRivalry().stream().map(Nameable::getName).collect(Collectors.joining(", "));
					if (allies.isEmpty()) {
						allies = "&cNone";
					}
					if (enemies.isEmpty()) {
						enemies = "&cNone";
					}
					if (alliesR.isEmpty()) {
						alliesR = "&cNone";
					}
					color = c.getPalette().toString();
					String finalAllies = allies;
					String finalEnemies = enemies;
					String finalAlliesR = alliesR;
					InfoSection line_1 = new InfoSection(configurable.getNode("info.line-1"));
					FancyMessage line_1_msg = new FancyMessage();
					if (line_1.getPrefix() != null) line_1_msg.then(line_1.getPrefix());
					if (line_1.getText() != null) {
						line_1_msg.then(line_1.getText());
						if (!line_1.getHover().isEmpty()) {
							line_1.getHover().forEach(s -> {
								line_1_msg.hover(s.replace("{0}", c.getName() + idMode)
										.replace("{1}", c.getDescription())
										.replace("{2}", Clan.ACTION.format(c.getPower()))
										.replace("{3}", (c.getPalette().isGradient() ? c.getPalette().toString((c.getPalette().toArray()[0]).replace("&", "").replace("#", "")) + "&f»" + c.getPalette().toString((c.getPalette().toArray()[1]).replace("&", "").replace("#", "")) : color.replace("&", "&f»" + color).replace("#", "&f»" + color)))
										.replace("{4}", String.valueOf(c.getClaims().length))
										.replace("{5}", String.valueOf(c.getClaimLimit()))
										.replace("{6}", finalAllies)
										.replace("{7}", finalEnemies)
										.replace("{8}", finalAlliesR)
										.replace("{9}", c.getPalette().getStart()));
							});
						}
						if (line_1.getCommand() != null) {
							line_1_msg.command(line_1.getCommand());
						}
						if (line_1.getSuggestion() != null) {
							line_1_msg.suggest(line_1.getSuggestion());
						}
						if (line_1.getCopyText() != null) {
							line_1_msg.copy(line_1.getCopyText());
						}
						if (line_1.getUrlCopyText() != null) {
							line_1_msg.url(line_1.getUrlCopyText());
						}
					}
					if (line_1.getSuffix() != null) line_1_msg.then(line_1.getSuffix());
					InfoSection line_2 = new InfoSection(configurable.getNode("info.line-2"));
					InfoSection line_3 = new InfoSection(configurable.getNode("info.line-3"));
					InfoSection line_4 = new InfoSection(configurable.getNode("info.line-4"));
					InfoSection line_5 = new InfoSection(configurable.getNode("info.line-5"));
					InfoSection line_6 = new InfoSection(configurable.getNode("info.line-6"));
					InfoSection line_7 = new InfoSection(configurable.getNode("info.line-7"));
					InfoSection line_8 = new InfoSection(configurable.getNode("info.line-8"));
					InfoSection line_9 = new InfoSection(configurable.getNode("info.line-9"));
					chain = new FancyMessageChain()
							.append(top -> {
								top.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ");
								if (line_1.isValid()) {
									top.append(line_1_msg);
								}
								top.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ");
							})
							.append(space1 -> {
								if (line_2.isValid()) {
									space1.append(line_2.toMsg());
								}
							})
							.append(top_middle -> {
								top_middle.then(" ");
								if (line_3.isValid()) {
									top_middle.append(line_3.toMsg());
								}
								top_middle
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ");
								if (line_3.getAppendage().isValid()) {
									top_middle.append(line_3.getAppendage().toMsg());
								}
								top_middle
										.then(" ");
							})
							.append(space2 -> {
								if (line_4.isValid()) {
									space2.append(line_4.toMsg());
								}
							})
							.append(middle -> {
								middle.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ");
								if (line_5.isValid()) {
									middle.append(line_5.toMsg());
								}
								middle
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ");
							})
							.append(space3 -> {
								if (line_6.isValid()) {
									space3.append(line_6.toMsg());
								}
							})
							.append(bottom_middle -> {
								bottom_middle.then(" ");
								if (line_7.isValid()) {
									bottom_middle.append(line_7.toMsg());
								}
								bottom_middle
										.then(" ");
								if (line_7.getAppendage().isValid()) {
									bottom_middle.append(line_7.getAppendage().toMsg());
								}
								bottom_middle.then(" ");
								if (line_7.getAppendage().getAppendage().isValid()) {
									bottom_middle.append(line_7.getAppendage().getAppendage().toMsg());
								}
								bottom_middle.then(" ");
							})
							.append(space4 -> {
								if (line_8.isValid()) {
									space4.append(line_8.toMsg());
								}
							})
							.append(bottom -> {
								bottom.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ")
										.then(" ");
								if (line_9.isValid()) {
									bottom.append(line_9.toMsg());
								}
								bottom.then(" ")
										.then(" ")
										.then(" ");
							});
					break;
			}
			chain.send(e.getPlayer()).deploy();
		}
	}

	@Subscribe
	public void onWarStart(WarStartEvent e) {
		War w = e.getWar();
		if (w.getQueue().getAssociates().length == 0) {
			e.setCancelled(true);
			return;
		}
		TimeWatch.Recording r = e.getRecording();
		Cooldown test = LabyrinthProvider.getService(Service.COOLDOWNS).getCooldown("war-" + w.getId() + "-start");
		if (test != null) {
			long msec = TimeUnit.MINUTES.toSeconds(r.getMinutes());
			long sec = r.getSeconds();
			if ((msec + sec) >= LabyrinthProvider.getInstance().getLocalPrintManager().getPrint(e.getApi().getLocalPrintKey()).getNumber("war_start_time").intValue()) {
				e.start();
				LabyrinthProvider.getInstance().remove(test);
				e.setCancelled(true);
			} else {
				Mailer m = LabyrinthProvider.getService(Service.MESSENGER).getEmptyMailer();
				String t = calc(test.getMinutes()) + ":" + calc(test.getSeconds());
				TaskScheduler.of(() -> w.forEach(a -> {
					Player p = a.getTag().getPlayer().getPlayer();
					if (p != null) {
						m.accept(p).action("&2War start&f: &e" + t).deploy();
					}
				})).schedule();
			}
		}
	}

	@Subscribe
	public void onResidency(ClaimResidentEvent e) {
		Clan owner = e.getClan();
		if (owner.getMember(m -> m.getName().equals(e.getResident().getPlayer().getName())) == null) {
			if (!e.getResident().getPlayer().hasPermission("clanspro.claim.bypass")) {
				e.getResident().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 225, -1, false, false));
			}
		} else {
			if (e.getResident().getPlayer().hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
				e.getResident().getPlayer().removePotionEffect(PotionEffectType.SLOW_DIGGING);
			}
		}
		e.getApi().getAssociate(e.getResident().getPlayer()).ifPresent(a -> {
			for (Clan ally : owner.getRelation().getAlliance().get(Clan.class)) {
				if (ally.getName().equals(a.getClan().getName())) {
					if (e.getResident().getPlayer().hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
						e.getResident().getPlayer().removePotionEffect(PotionEffectType.SLOW_DIGGING);
					}
					break;
				}
			}
		});
	}

	@Subscribe
	public void onWilderness(WildernessInhabitantEvent e) {
		if (e.getPlayer() == null) return;
		if (e.getPlayer().hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
			e.getPlayer().removePotionEffect(PotionEffectType.SLOW_DIGGING);
		}
	}

	@Subscribe
	public void onWarWatch(WarActiveEvent e) {
		Cooldown timer = e.getWar().getTimer();
		Mailer msg = LabyrinthProvider.getService(Service.MESSENGER).getEmptyMailer();
		e.getWar().forEach(a -> {
			Player p = a.getTag().getPlayer().getPlayer();
			if (p != null) {
				War.Team t = e.getWar().getTeam(a.getClan());
				int points = e.getWar().getPoints(t);
				String time = calc(timer.getMinutes()) + ":" + calc(timer.getSeconds());
				msg.accept(p).action("&3Points&f:&b " + points + " &6| &3Time left&f:&e " + time).deploy();
			}
		});
	}

	@Subscribe
	public void onWarWin(WarWonEvent e) {
		double reward = new Random().nextInt(e.getWinner().getValue()) + 0.17;
		FileManager config = ClansAPI.getDataInstance().getConfig();
		double additional = config.read(c -> c.getNode("Clans.war.conclusion.winning").toPrimitive().getDouble());
		e.getWinner().getKey().givePower(reward + additional);
		double losing = config.read(c -> c.getNode("Clans.war.conclusion.losing").toPrimitive().getDouble());
		e.getLosers().forEach((clan, integer) -> clan.takePower(reward + losing));
	}

	@Subscribe
	public void onClanCreated(ClanFreshlyFormedEvent e) {
		Clan c = e.getClan();
		if (ClansAPI.getDataInstance().isTrue("Clans.land-claiming.claim-influence.allow")) {
			if (ClansAPI.getDataInstance().getConfigString("Clans.land-claiming.claim-influence.dependence").equalsIgnoreCase("LOW")) {
				c.giveClaims(12);
			}
		}
	}


	@NotNull ClanCooldown creationCooldown(UUID id) {
		ClanCooldown target = null;
		for (ClanCooldown c : ClansAPI.getDataInstance().getCooldowns()) {
			if (c.getAction().equals("Clans:create-limit") && c.getId().equals(id.toString())) {
				target = c;
				break;
			}
		}
		if (target == null) {
			target = new CooldownCreate(id);
			if (!ClansAPI.getDataInstance().getCooldowns().contains(target)) {
				target.save();
			}
		}
		return target;
	}

	@Subscribe
	public void onClanCreate(PlayerCreateClanEvent event) {
		if (event.getPlayer() != null) {
			Player p = event.getPlayer();
			if (Clan.ACTION.getAllClanNames().size() >= LabyrinthProvider.getInstance().getLocalPrintManager().getPrint(event.getApi().getLocalPrintKey()).getNumber(AbstractGameRule.MAX_CLANS).intValue()) {
				event.getUtil().sendMessage(p, "&cYour server has reached it's maxed allowed amount of clans.");
				event.setCancelled(true);
				return;
			}
			if (ClansAPI.getInstance().isNameBlackListed(event.getName())) {
				String command = ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.name-blacklist." + event.getName().toLowerCase() + ".action");
				event.getUtil().sendMessage(p, "&c&oThis name is not allowed!");
				if (command != null && !command.isEmpty()) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Clan.ACTION.format(command, "{PLAYER}", p.getName()));
				}
				event.setCancelled(true);
				return;
			}
			if (ClansAPI.getDataInstance().isTrue("Clans.creation.charge")) {
				double amount = ClansAPI.getDataInstance().getConfig().getRoot().getDouble("Clans.creation.amount");

				boolean success = EconomyProvision.getInstance().has(BigDecimal.valueOf(amount), p, p.getWorld().getName()).orElse(false);
				if (!success) {
					event.setCancelled(true);
					event.getUtil().sendMessage(p, "&c&oYou don't have enough money. Amount needed: &6" + amount);
				} else {
					EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(amount), p, p.getWorld().getName()).orElse(false);
				}
				return;
			}
			if (p != null && ClansAPI.getDataInstance().isTrue("Clans.creation.cooldown.enabled")) {
				if (creationCooldown(p.getUniqueId()).isComplete()) {
					creationCooldown(p.getUniqueId()).setCooldown();
				} else {
					event.setCancelled(true);
					event.getUtil().sendMessage(p, "&c&oYou can't do this right now.");
					event.getUtil().sendMessage(p, creationCooldown(p.getUniqueId()).fullTimeLeft());
				}
			}
		}
	}

	@Subscribe
	public void onVault(VaultInteractEvent e) {
		Bukkit.getOnlinePlayers().forEach(p -> {
			if (ClansAPI.getDataInstance().isSpy(p)) {
				ItemStack item = e.getClickedItem();
				if (item != null) {
					if (e.getAction().name().contains("PICKUP") || e.getAction().name().contains("DROP") || e.getAction().name().contains("MOVE")) {
						if (item.getType() == Material.AIR) return;
						Message m = new FancyMessage().then("&b[&7Vaults&b] &f(" + e.getClan().getName() + ")").then(" ").then((item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name().toLowerCase())).color(ChatColor.AQUA).hover(item).then(" ").then("&fx" + item.getAmount() + " removed by &b" + e.getWhoClicked().getName());
						Mailer.empty(p).chat(m.build()).deploy();
					}
					if (e.getAction().name().contains("PLACE")) {
						Message m = new FancyMessage().then("&b[&7Vaults&b] &f(" + e.getClan().getName() + ")").then(" ").then((item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name().toLowerCase())).color(ChatColor.GREEN).hover(item).then(" ").then("&fx" + item.getAmount() + " added by &a" + e.getWhoClicked().getName());
						Mailer.empty(p).chat(m.build()).deploy();
					}
				}
			}
		});
	}

	@Subscribe
	public void onVault(StashInteractEvent e) {
		Bukkit.getOnlinePlayers().forEach(p -> {
			if (ClansAPI.getDataInstance().isSpy(p)) {
				ItemStack item = e.getClickedItem();
				if (item != null) {
					if (e.getAction().name().contains("PICKUP") || e.getAction().name().contains("DROP") || e.getAction().name().contains("MOVE")) {
						if (item.getType() == Material.AIR) return;
						Message m = new FancyMessage().then("&2[&7Stashes&2] &f(" + e.getClan().getName() + ")").then(" ").then((item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name().toLowerCase())).color(ChatColor.AQUA).hover(item).then(" ").then("&fx" + item.getAmount() + " removed by &b" + e.getWhoClicked().getName());
						Mailer.empty(p).chat(m.build()).deploy();
					}
					if (e.getAction().name().contains("PLACE")) {
						Message m = new FancyMessage().then("&2[&7Stashes&2] &f(" + e.getClan().getName() + ")").then(" ").then((item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name().toLowerCase())).color(ChatColor.GREEN).hover(item).then(" ").then("&fx" + item.getAmount() + " added by &a" + e.getWhoClicked().getName());
						Mailer.empty(p).chat(m.build()).deploy();
					}
				}
			}
		});
	}

}
