package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.b.BountyAddon;
import com.github.sanctum.clans.bridge.d.DynmapAddon;
import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.bank.BankListener;
import com.github.sanctum.clans.construct.bank.BankPermissions;
import com.github.sanctum.clans.construct.impl.Resident;
import com.github.sanctum.clans.events.core.ClaimResidentEvent;
import com.github.sanctum.clans.events.core.RaidShieldEvent;
import com.github.sanctum.clans.events.core.WildernessInhabitantEvent;
import com.github.sanctum.labyrinth.command.CommandRegistration;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.data.RegistryData;
import com.github.sanctum.labyrinth.data.service.AnnotationDiscovery;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.task.JoinableRepeatingTask;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.labyrinth.task.Synchronous;
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

	private final ClansJavaPlugin instance;

	public static JoinableRepeatingTask<Player> CLAIM_TASK;

	public StartProcedure(ClansJavaPlugin instance, int id) throws IllegalAccessException {
		this.instance = instance;
		CLAIM_TASK = JoinableRepeatingTask.create(18, instance, p -> {
			ClansAPI API = ClansAPI.getInstance();

			ClanAssociate associate = API.getAssociate(p).orElse(null);

			if (associate != null) {
				for (ClanCooldown clanCooldown : ClansAPI.getData().COOLDOWNS) {
					if (clanCooldown.getId().equals(p.getUniqueId().toString())) {
						if (clanCooldown.isComplete()) {
							Schedule.sync(() -> ClanCooldown.remove(clanCooldown)).run();
							Clan.ACTION.sendMessage(p, MessageFormat.format(ClansAPI.getData().getMessage("cooldown-expired"), clanCooldown.getAction().replace("Clans:", "")));
						}
					}
				}

			}

			if (Claim.ACTION.isEnabled()) {

				if (!API.getClaimManager().isInClaim(p.getLocation())) {

					ClanVentBus.call(new WildernessInhabitantEvent(p));

				} else {
					ClaimResidentEvent event = ClanVentBus.call(new ClaimResidentEvent(p));
					if (!event.isCancelled()) {
						ClansAPI.getData().INHABITANTS.remove(event.getResident().getPlayer());
						Resident r = event.getResident();
						Claim current = event.getClaim();
						if (current.isActive()) {
							if (ClansAPI.getInstance().getClanName(current.getOwner()) == null) {
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
		});
		if (id != DataManager.hidden()) {
			JavaPlugin plugin = JavaPlugin.getProvidingPlugin(getClass());
			Bukkit.getPluginManager().disablePlugin(plugin);
			throw new IllegalAccessException("Invalid object access token. Denied.");
		}
		Bukkit.getServicesManager().register(ClansAPI.class, instance, instance, ServicePriority.Normal);

	}

	public void printLogo() {
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		instance.getLogger().info("- Clans [Pro]. Loading plugin information...");
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		for (String ch : getLogo()) {
			instance.getLogger().info("- " + ch);
		}
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
	}

	public void runRegistry() {
		instance.getLogger().info("- Starting registry procedures.");
		instance.dataManager.copyDefaults();
		new Registry<>(Listener.class).source(ClansAPI.getInstance().getPlugin()).pick("com.github.sanctum.clans.listener").operate(listener -> {
			Bukkit.getPluginManager().registerEvents(listener, instance);
			AnnotationDiscovery<Subscribe, Object> discovery = AnnotationDiscovery.of(Subscribe.class, listener);
			if (discovery.filter(m -> m.getParameters().length == 1 && m.getParameters()[0].getType().isAssignableFrom(Vent.class) && m.isAnnotationPresent(Subscribe.class)).count() > 0) {
				Vent.register(instance, listener);
			}
		});
		new Registry<>(Command.class).source(ClansAPI.getInstance().getPlugin()).pick("com.github.sanctum.clans.commands").operate(CommandRegistration::use);
		RegistryData<ClanAddon> data = new Registry.Loader<>(ClanAddon.class)
				.source(instance)
				.from("Addons")
				.confine(e -> {
					try {
						e.onLoad();
						ClanAddonQuery.getRegisteredAddons().add(e);
						e.onEnable();
					} catch (Exception ex) {
						instance.getLogger().severe("- An issue occurred while enabling addon " + e.getName());
						ex.printStackTrace();
					}
				});

		for (ClanAddon e : data.getData()) {
			sendBorder();
			instance.getLogger().info("- Loaded: " + e.getName() + " v" + e.getVersion());
			sendBorder();
		}
		instance.getLogger().info("- (" + data.getData().size() + ") clan addon(s) were injected into cache.");

	}

	public void runCacheRefresh() {
		instance.getLogger().info("- Loading clans and claims, please be patient...");
		instance.getClanManager().refresh();
		instance.getLogger().info("- Loaded (" + instance.getClanManager().getClans().list().size() + ") clans ");
		instance.getClaimManager().refresh();
		instance.getLogger().info("- Loaded (" + instance.getClaimManager().getClaims().size() + ") claims");
	}

	public void runMetric(int ID, Consumer<Metrics> metrics) {
		try {
			metrics.accept(new Metrics(instance, ID));
			instance.getLogger().info("- Converting bStats metrics tables.");
		} catch (Exception ignored) {
		}
	}

	public void runQuickDataCleaning() {
		instance.getLogger().info("- Cleaning misc files.");
		for (String id : Clan.ACTION.getAllClanIDs()) {
			if (ClansAPI.getInstance().getClanName(id) == null) {
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

	public void runCoreTask() {

		Synchronous sync = Schedule.sync(() -> {

			if (Bukkit.getOnlinePlayers().size() == 0) return;

			for (Player p : Bukkit.getOnlinePlayers()) {

				ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

				if (associate == null) continue;

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
									Bukkit.broadcastMessage(Clan.ACTION.color(Clan.ACTION.getPrefix() + " &4&oWar &6between &4" + c.getName() + " &6and &4" + c.getCurrentWar().getTargeted().getName() + " &6concluded with winner " + winner.getName() + " &e(&6&l" + winner.getCurrentWar().getPoints() + "&e)"));
								}
								if (c.getCurrentWar().getTargeted().getCurrentWar().getPoints() > c.getCurrentWar().getPoints()) {
									winner = c.getCurrentWar().getTargeted();
									loser = c;
									Bukkit.broadcastMessage(Clan.ACTION.color(Clan.ACTION.getPrefix() + " &4&oWar &6between &4" + c.getName() + " &6and &4" + c.getCurrentWar().getTargeted().getName() + " &6concluded with winner " + winner.getName() + " &e(&6&l" + winner.getCurrentWar().getPoints() + "&e)"));
								}
								if (c.getCurrentWar().getPoints() == c.getCurrentWar().getTargeted().getCurrentWar().getPoints()) {
									Bukkit.broadcastMessage(Clan.ACTION.color(Clan.ACTION.getPrefix() + " &4&oWar &6between &4" + c.getName() + " &6and &4" + c.getCurrentWar().getTargeted().getName() + " &6finished in a draw."));
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
												Clan.ACTION.sendMessage(par, "&cYou don't have a valid economy system installed. No one received any money.");
											}
										}

										par.sendTitle(Clan.ACTION.color(winnerC + winner.getCurrentWar().getPoints() + " &f/ " + loserC + loser.getCurrentWar().getPoints()), Clan.ACTION.color("&aWe win."), 10, 45, 10);
									}
									loser.takePower((loser.getPower() / 2) + winner.getCurrentWar().getPoints());
									for (Player par : loser.getCurrentWar().getParticipants()) {
										par.giveExp((winner.getCurrentWar().getPoints() * 2));
										final boolean success;
										Optional<Boolean> opt = EconomyProvision.getInstance().deposit(BigDecimal.valueOf(10.14 * loser.getCurrentWar().getPoints()), par);

										success = opt.orElse(false);
										if (!success) {
											if (par.isOp()) {
												Clan.ACTION.sendMessage(par, "&cYou don't have a valid economy system installed. No one received any money.");
											}
										}
										par.sendTitle(Clan.ACTION.color(winnerC + winner.getCurrentWar().getPoints() + " &f/ " + loserC + loser.getCurrentWar().getPoints()), Clan.ACTION.color("&cWe lose."), 10, 45, 10);

									}
								}
								ClanCooldown.remove(c.getCurrentWar().getArenaTimer());
								c.getCurrentWar().conclude();
							}
						} else {
							for (Player par : c.getCurrentWar().getParticipants()) {
								par.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Clan.ACTION.color("&8Time left: &f(&3" + c.getCurrentWar().getArenaTimer().getMinutesLeft() + ":" + c.getCurrentWar().getArenaTimer().getSecondsLeft() + "&f)")));
							}
							for (Player par : c.getCurrentWar().getTargeted().getCurrentWar().getParticipants()) {
								par.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Clan.ACTION.color("&8Time left: &f(&3" + c.getCurrentWar().getArenaTimer().getMinutesLeft() + ":" + c.getCurrentWar().getArenaTimer().getSecondsLeft() + "&f)")));
							}
						}
					}
				}
				for (String ally : c.getAllyList()) {
					if (!Clan.ACTION.getAllClanIDs().contains(ally)) {
						c.removeAlly(HUID.fromString(ally));
						break;
					}
				}
				for (String enemy : c.getEnemyList()) {
					if (!Clan.ACTION.getAllClanIDs().contains(enemy)) {
						c.removeEnemy(HUID.fromString(enemy));
						break;
					}
				}
				for (String allyRe : c.getAllyRequests()) {
					if (!Clan.ACTION.getAllClanIDs().contains(allyRe)) {
						FileManager cl = ClansAPI.getData().getClanFile(c);
						List<String> allies = c.getAllyList();
						allies.remove(allyRe);
						cl.getConfig().set("ally-requests", allies);
						cl.saveConfig();
						break;
					}
				}

			}

		});
		if (ClansAPI.getData().getEnabled("Formatting.console-debug")) {
			sync.debug();
		}
		sync.debug().repeatReal(2, 18);

	}


	public void runPlaceholderCheck() {
		Schedule.sync(() -> {
			instance.getLogger().info("- Checking for placeholders.");
			if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
				new ClanPlaceholders(instance).register();
				instance.getLogger().info("- PlaceholderAPI found! Loading clans placeholders");
			} else {
				instance.getLogger().info("- PlaceholderAPI not found, placeholders will not work!");
			}
		}).wait(5);
	}

	public void runInternalAddonServices() {
		ClanAddonQuery.pickupCycles(instance, "com.github.sanctum.clans.bridge.a");
		Schedule.sync(() -> {
			if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
				instance.importAddon(DynmapAddon.class);
			}
			if (EconomyProvision.getInstance().isValid()) {
				instance.importAddon(BountyAddon.class);
			}
		}).wait(5);
		instance.getLogger().info("- Found (" + ClanAddonQuery.getRegisteredAddons().size() + ") clan addon(s)");
		for (ClanAddon e : ClanAddonQuery.getRegisteredAddons()) {
			if (e.persist()) {
				try {
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
				} catch (NoClassDefFoundError | NoSuchMethodError ex) {
					instance.getLogger().severe("- An issue occurred while enabling addon " + e.getName());
					Schedule.sync(() -> ClanAddonQuery.getRegisteredAddons().removeIf(c -> c.getName().equals(e.getName()))).wait(1);
					ex.printStackTrace();
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

	public void runBankSetup() {
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

	public void runUpdateCheck() {
		if (ClansAPI.getData().getEnabled("Clans.check-version")) {
			ClansAPI.getInstance().isUpdated();
		} else {
			instance.getLogger().info("- Version check skipped.");
		}
	}

	public void sendBorder() {
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
	}

	private List<String> getLogo() {
		return new ArrayList<>(Arrays.asList("   ▄▄▄·▄▄▄        ▄▄ ", "  ▐█ ▄█▀▄ █·▪     ██▌" + "  User ID: ", "   ██▀·▐▀▀▄  ▄█▀▄ ▐█·" + "   " + instance.USER_ID, "  ▐█▪·•▐█•█▌▐█▌.▐▌.▀ " + "  Unique ID: ", "  .▀   .▀  ▀ ▀█▄▀▪ ▀ " + "   " + instance.NONCE));
	}

}
