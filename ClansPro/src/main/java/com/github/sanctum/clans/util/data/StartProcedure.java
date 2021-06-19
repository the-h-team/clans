package com.github.sanctum.clans.util.data;

import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanCooldown;
import com.github.sanctum.clans.construct.actions.ClansUpdate;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.bank.BankListener;
import com.github.sanctum.clans.construct.bank.BankPermissions;
import com.github.sanctum.clans.construct.extra.Resident;
import com.github.sanctum.clans.util.Placeholders;
import com.github.sanctum.clans.util.events.clans.ClaimResidentEvent;
import com.github.sanctum.clans.util.events.clans.RaidShieldEvent;
import com.github.sanctum.clans.util.events.clans.WildernessInhabitantEvent;
import com.github.sanctum.labyrinth.command.CommandRegistration;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.data.RegistryData;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.labyrinth.task.Synchronous;
import com.github.sanctum.link.ClanVentBus;
import com.github.sanctum.link.CycleList;
import com.github.sanctum.link.EventCycle;
import com.github.sanctum.link.dynmap.DynmapCycle;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class StartProcedure {

	private final ClansPro instance;

	public StartProcedure(ClansPro instance, int id) throws IllegalAccessException {
		this.instance = instance;
		if (id != DataManager.hidden()) {
			JavaPlugin plugin = JavaPlugin.getProvidingPlugin(getClass());
			Bukkit.getPluginManager().disablePlugin(plugin);
			throw new IllegalAccessException("Invalid object access token. Denied.");
		}
	}

	public void printLogo() {
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		instance.getLogger().info("- Clans [Pro]. Loading plugin information...");
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		for (String ch : getLogo()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			instance.getLogger().info("- " + ch);
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
	}

	public void registerDefaults() {
		instance.getLogger().info("- Registering defaults.");
		instance.dataManager.copyDefaults();
		Bukkit.getServicesManager().register(ClansAPI.class, instance, instance, ServicePriority.Normal);

		new Registry<>(Listener.class).source(ClansPro.getInstance()).pick("com.github.sanctum.clans.util.listener").operate(listener -> Bukkit.getPluginManager().registerEvents(listener, instance));
		new Registry<>(Command.class).source(ClansPro.getInstance()).pick("com.github.sanctum.clans.commands").operate(CommandRegistration::use);

		RegistryData<EventCycle> data = new Registry.Loader<>(EventCycle.class)
				.source(instance)
				.from("Addons")
				.operate(e -> {
					e.onLoad();
					e.register();
				});

		for (EventCycle e : data.getData()) {
			sendBorder();
			instance.getLogger().info("- Loaded: " + e.getName() + " v" + e.getVersion());
			sendBorder();
		}

		instance.getLogger().info("- (" + data.getData().size() + ") EvenCycle(s) were injected into cache.");

	}

	public void runCacheLoader() {
		instance.getLogger().info("- Loading clans and claims.");
		for (String clanID : DefaultClan.action.getAllClanIDs()) {
			DefaultClan instance = new DefaultClan(clanID);
			ClansAPI.getInstance().getClanManager().load(instance);
		}
		//Claim.action.loadClaims();
		instance.getClaimManager().refresh();
	}

	public void registerMetrics(int ID, Consumer<Metrics> metrics) {
		try {
			metrics.accept(new Metrics(instance, ID));
			instance.getLogger().info("- Converting bStats metrics tables.");
		} catch (Exception ignored) {
		}
	}

	public void runDataCleaner() {
		instance.getLogger().info("- Cleaning misc files.");
		for (String id : DefaultClan.action.getAllClanIDs()) {
			if (DefaultClan.action.getClanTag(id) == null) {
				FileManager clan = DataManager.FileType.CLAN_FILE.get(id);
				clan.delete();
			}
		}
	}

	public void runShieldTimer() {
		ClansAPI.getInstance().getShieldManager().setEnabled(true);
		boolean configAllow = instance.dataManager.getMain().getConfig().getBoolean("Clans.raid-shield.allow");
		if (configAllow) {
			Schedule.sync(() -> {
				if (Bukkit.getOnlinePlayers().size() > 0) {
					new Vent.Call<>(Vent.Runtime.Synchronous, new RaidShieldEvent()).run();
				}
			}).repeatReal(1, 40);
			instance.getLogger().info("- Running raid shield timer.");
		} else {
			instance.getLogger().info("- Denying raid shield timer. (Off)");
		}
	}

	public void refillUserData() {
		instance.getClanManager().refresh();

		Synchronous sync = Schedule.sync(() -> {

			if (Bukkit.getOnlinePlayers().size() == 0) return;

			for (Player p : Bukkit.getOnlinePlayers()) {

				ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

				if (associate == null) return;

				if (!associate.isValid()) {
					Schedule.sync(() -> ClansAPI.getData().ASSOCIATES.removeIf(a -> a.getPlayer().getUniqueId().equals(p.getUniqueId()))).wait(1);
					return;
				}

				Clan c = associate.getClan();

				for (ClanCooldown clanCooldown : c.getCooldowns()) {
					if (clanCooldown.isComplete()) {
						ClanCooldown.remove(clanCooldown);
						c.broadcast(MessageFormat.format(ClansAPI.getData().getMessage("cooldown-expired"), clanCooldown.getAction().replace("Clans:", "")));
					}
				}

				if (c.getCurrentWar() != null) {
					if (ClansAPI.getData().arenaFile().exists()) {
						if (c.getCurrentWar().getArenaTimer().isComplete()) {
							if (c.getCurrentWar().getTargeted() != null) {
								Clan winner = null;
								Clan loser = null;
								if (c.getCurrentWar().getPoints() > c.getCurrentWar().getTargeted().getCurrentWar().getPoints()) {
									winner = c;
									loser = c.getCurrentWar().getTargeted();
									Bukkit.broadcastMessage(DefaultClan.action.color(DefaultClan.action.getPrefix() + " &4&oWar &6between &4" + c.getName() + " &6and &4" + c.getCurrentWar().getTargeted().getName() + " &6concluded with winner " + winner.getName() + " &e(&6&l" + winner.getCurrentWar().getPoints() + "&e)"));
								}
								if (c.getCurrentWar().getTargeted().getCurrentWar().getPoints() > c.getCurrentWar().getPoints()) {
									winner = c.getCurrentWar().getTargeted();
									loser = c;
									Bukkit.broadcastMessage(DefaultClan.action.color(DefaultClan.action.getPrefix() + " &4&oWar &6between &4" + c.getName() + " &6and &4" + c.getCurrentWar().getTargeted().getName() + " &6concluded with winner " + winner.getName() + " &e(&6&l" + winner.getCurrentWar().getPoints() + "&e)"));
								}
								if (c.getCurrentWar().getPoints() == c.getCurrentWar().getTargeted().getCurrentWar().getPoints()) {
									Bukkit.broadcastMessage(DefaultClan.action.color(DefaultClan.action.getPrefix() + " &4&oWar &6between &4" + c.getName() + " &6and &4" + c.getCurrentWar().getTargeted().getName() + " &6finished in a draw."));
								}
								if (winner != null) {
									String loserC;
									String winnerC;
									if (loser.getCurrentWar().isRed()) {
										loserC = "&c";
										winnerC = "&9";
									} else {
										loserC = "&9";
										winnerC = "&c";
									}
									winner.givePower((loser.getPower() / 2) + winner.getCurrentWar().getPoints());
									for (Player par : winner.getCurrentWar().getParticipants()) {
										par.giveExp((winner.getCurrentWar().getPoints() * 2));
										final boolean success;
										Optional<Boolean> opt = EconomyProvision.getInstance().deposit(BigDecimal.valueOf(18.14 * winner.getCurrentWar().getPoints()), par);

										success = opt.orElse(false);

										if (!success) {
											if (par.isOp()) {
												DefaultClan.action.sendMessage(par, "&cYou don't have a valid economy system installed. No one received any money.");
											}
										}

										par.sendTitle(DefaultClan.action.color(winnerC + winner.getCurrentWar().getPoints() + " &f/ " + loserC + loser.getCurrentWar().getPoints()), DefaultClan.action.color("&aWe win."), 10, 45, 10);
									}
									loser.takePower((loser.getPower() / 2) + winner.getCurrentWar().getPoints());
									for (Player par : loser.getCurrentWar().getParticipants()) {
										par.giveExp((winner.getCurrentWar().getPoints() * 2));
										final boolean success;
										Optional<Boolean> opt = EconomyProvision.getInstance().deposit(BigDecimal.valueOf(10.14 * loser.getCurrentWar().getPoints()), par);

										success = opt.orElse(false);
										if (!success) {
											if (par.isOp()) {
												DefaultClan.action.sendMessage(par, "&cYou don't have a valid economy system installed. No one received any money.");
											}
										}
										par.sendTitle(DefaultClan.action.color(winnerC + winner.getCurrentWar().getPoints() + " &f/ " + loserC + loser.getCurrentWar().getPoints()), DefaultClan.action.color("&cWe lose."), 10, 45, 10);

									}
								}
								ClanCooldown.remove(c.getCurrentWar().getArenaTimer());
								c.getCurrentWar().conclude();
							}
						} else {
							for (Player par : c.getCurrentWar().getParticipants()) {
								par.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(DefaultClan.action.color("&8Time left: &f(&3" + c.getCurrentWar().getArenaTimer().getMinutesLeft() + ":" + c.getCurrentWar().getArenaTimer().getSecondsLeft() + "&f)")));
							}
							for (Player par : c.getCurrentWar().getTargeted().getCurrentWar().getParticipants()) {
								par.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(DefaultClan.action.color("&8Time left: &f(&3" + c.getCurrentWar().getArenaTimer().getMinutesLeft() + ":" + c.getCurrentWar().getArenaTimer().getSecondsLeft() + "&f)")));
							}
						}
					}
				}
				for (String ally : c.getAllyList()) {
					if (!DefaultClan.action.getAllClanIDs().contains(ally)) {
						c.removeAlly(HUID.fromString(ally));
						break;
					}
				}
				for (String enemy : c.getEnemyList()) {
					if (!DefaultClan.action.getAllClanIDs().contains(enemy)) {
						c.removeEnemy(HUID.fromString(enemy));
						break;
					}
				}
				for (String allyRe : c.getAllyRequests()) {
					if (!DefaultClan.action.getAllClanIDs().contains(allyRe)) {
						FileManager cl = ClansAPI.getData().getClanFile(c);
						List<String> allies = c.getAllyList();
						allies.remove(allyRe);
						cl.getConfig().set("ally-requests", allies);
						cl.saveConfig();
						break;
					}
				}

			}

		}).applyAfter(() -> {

			if (Bukkit.getOnlinePlayers().size() == 0) return;

			ClansAPI API = ClansAPI.getInstance();

			for (Player p : Bukkit.getOnlinePlayers()) {

				ClanAssociate associate = API.getAssociate(p).orElse(null);

				if (associate != null) {

					for (ClanCooldown clanCooldown : ClansAPI.getData().COOLDOWNS) {
						if (clanCooldown.getId().equals(p.getUniqueId().toString())) {
							if (clanCooldown.isComplete()) {
								Schedule.sync(() -> ClanCooldown.remove(clanCooldown)).run();
								DefaultClan.action.sendMessage(p, MessageFormat.format(ClansAPI.getData().getMessage("cooldown-expired"), clanCooldown.getAction().replace("Clans:", "")));
							}
						}
					}

				}

				if (Claim.action.isEnabled()) {

					if (!API.getClaimManager().isInClaim(p.getLocation())) {

						ClanVentBus.call(new WildernessInhabitantEvent(p));

					} else {
						ClaimResidentEvent event = ClanVentBus.call(new ClaimResidentEvent(p));
						if (!event.isCancelled()) {
							ClansAPI.getData().INHABITANTS.remove(event.getResident().getPlayer());
							Resident r = event.getResident();
							Claim current = event.getClaim();
							if (current.isActive()) {
								if (DefaultClan.action.getClanTag(current.getOwner()) == null) {
									current.remove();
									return;
								}
								Claim lastKnown = r.getLastKnown();
								if (!current.getId().equals(lastKnown.getId())) {
									if (r.hasProperty(Resident.Property.NOTIFIED)) {
										if (!lastKnown.getOwner().equals(r.getCurrent().getOwner())) {
											r.setProperty(Resident.Property.NOTIFIED, false);
											if (API.isInClan(r.getPlayer().getUniqueId())) {
												if (lastKnown.getOwner().equals(API.getClan(r.getPlayer()).get().getId().toString())) {
													r.setProperty(Resident.Property.TRAVERSED, true);
												}
											}
											r.updateLastKnown(event.getClaim());
											r.updateJoinTime(System.currentTimeMillis());
										}
									}
								}
								if (!r.hasProperty(Resident.Property.NOTIFIED)) {
									event.sendNotification();
									r.setProperty(Resident.Property.NOTIFIED, true);
								} else {
									if (r.hasProperty(Resident.Property.TRAVERSED)) {
										if (API.getClan(r.getPlayer()).isPresent()) {
											r.setProperty(Resident.Property.TRAVERSED, false);
											r.setProperty(Resident.Property.NOTIFIED, false);
											r.updateJoinTime(System.currentTimeMillis());
										}
									}
								}
							}
						}
					}
				}

			}

		});
		if (ClansAPI.getData().getEnabled("Formatting.console-debug")) {
			sync.debug();
		}
		sync.repeatReal(2, 18);

	}


	public void checkForPlaceholders() {
		Schedule.sync(() -> {
			instance.getLogger().info("- Checking for placeholders.");
			if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
				new Placeholders(instance).register();
				instance.getLogger().info("- PlaceholderAPI found! Loading clans placeholders");
			} else {
				instance.getLogger().info("- PlaceholderAPI not found, placeholders will not work!");
			}
		}).wait(5);
	}

	public void checkForCycles() {
		CycleList.pickupCycles(instance, "com.github.sanctum.link.cycles");
		Schedule.sync(() -> {
			if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
				instance.importAddon(DynmapCycle.class);
			}
		}).wait(5);
		instance.getLogger().info("- Found (" + CycleList.getRegisteredCycles().size() + ") event cycle(s)");
		for (EventCycle e : CycleList.getRegisteredCycles()) {
			if (e.persist()) {
				e.onEnable();
				sendBorder();
				instance.getLogger().info("- Addon: " + e.getName());
				instance.getLogger().info("- Description: " + e.getDescription());
				instance.getLogger().info("- Persistent: (" + e.persist() + ")");
				sendBorder();

				instance.getLogger().info("- Listeners: (" + e.getAdditions().size() + ")");
				for (Listener addition : e.getAdditions()) {
					boolean registered = HandlerList.getRegisteredListeners(instance).stream().anyMatch(r -> r.getListener().equals(addition));
					if (!registered) {
						instance.getLogger().info("- [" + e.getName() + "] (+1) Class " + addition.getClass().getSimpleName() + " loaded.");
						Bukkit.getPluginManager().registerEvents(addition, instance);
					} else {
						instance.getLogger().info("- [" + e.getName() + "] (-1) Class " + addition.getClass().getSimpleName() + " already loaded. Skipping.");
					}
				}
			} else {
				sendBorder();
				instance.getLogger().info("- Addon: " + e.getName());
				instance.getLogger().info("- Description: " + e.getDescription());
				instance.getLogger().info("- Persistent: (" + e.persist() + ")");
				e.remove();
				sendBorder();
				instance.getLogger().info("- Listeners: (" + e.getAdditions().size() + ")");
				for (Listener addition : e.getAdditions()) {
					instance.getLogger().info("- [" + e.getName() + "] (+1) Cycle failed to load due to no persistence.");
				}
			}

		}
	}

	public void setupBank() {
		// Permissions
		final Permission balance = new Permission(BankPermissions.BANKS_BALANCE.node);
		final Permission deposit = new Permission(BankPermissions.BANKS_DEPOSIT.node);
		final Permission withdraw = new Permission(BankPermissions.BANKS_WITHDRAW.node);
		final Permission use = new Permission(BankPermissions.BANKS_USE.node);
		balance.addParent(use, true);
		final Permission useStar = new Permission(BankPermissions.BANKS_USE_STAR.node);
		use.addParent(useStar, true);
		deposit.addParent(useStar, true);
		withdraw.addParent(useStar, true);
		final Permission star = new Permission(BankPermissions.BANKS_STAR.node);
		useStar.addParent(star, true);
		instance.getServer().getPluginManager().addPermission(star);
		instance.getServer().getPluginManager().addPermission(useStar);
		instance.getServer().getPluginManager().addPermission(use);
		instance.getServer().getPluginManager().addPermission(deposit);
		instance.getServer().getPluginManager().addPermission(withdraw);
		instance.getServer().getPluginManager().addPermission(balance);

		// Events
		instance.getServer().getPluginManager().registerEvents(new BankListener(), instance);
		instance.getLogger().info("Banking log-level=" + ClanBank.API.defaultImpl.logToConsole());
	}

	public void checkForUpdate() {
		if (ClansAPI.getData().getEnabled("Clans.check-version")) {
			ClansUpdate update = new ClansUpdate(instance);
			try {
				if (update.hasUpdate()) {
					instance.getLogger().warning("- An update is available! " + update.getLatest() + " download: " + update.getResource());
				} else {
					instance.getLogger().info("- All up to date!");
				}
			} catch (Exception e) {
				instance.getLogger().info("- There was a problem while looking for an update.");
			}
		} else {
			instance.getLogger().info("- Version check skipped.");
		}
	}

	public void refreshChat() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			instance.dataManager.CHAT_MODE.put(p, "GLOBAL");
		}
	}

	public void refreshChat(String channel) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			instance.dataManager.CHAT_MODE.put(p, channel);
		}
	}

	public void sendBorder() {
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
	}

	private List<String> getLogo() {
		return new ArrayList<>(Arrays.asList("   ▄▄▄·▄▄▄        ▄▄ ", "  ▐█ ▄█▀▄ █·▪     ██▌" + "  User ID: ", "   ██▀·▐▀▀▄  ▄█▀▄ ▐█·" + "   " + ClansPro.getInstance().USER_ID, "  ▐█▪·•▐█•█▌▐█▌.▐▌.▀ " + "  Unique ID: ", "  .▀   .▀  ▀ ▀█▄▀▪ ▀ " + "   " + ClansPro.getInstance().NONCE));
	}

}
