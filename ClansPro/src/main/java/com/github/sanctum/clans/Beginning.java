package com.github.sanctum.clans;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonDependencyException;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.external.BountyAddon;
import com.github.sanctum.clans.bridge.external.DynmapAddon;
import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClanException;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.clans.construct.bank.BankListener;
import com.github.sanctum.clans.construct.bank.BankPermissions;
import com.github.sanctum.clans.construct.extra.ClanPlaceholders;
import com.github.sanctum.clans.construct.extra.StartProcedure;
import com.github.sanctum.clans.construct.impl.MapEntry;
import com.github.sanctum.clans.events.core.ClanWarActiveEvent;
import com.github.sanctum.clans.events.core.ClanWarWonEvent;
import com.github.sanctum.clans.events.core.RaidShieldEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.command.CommandRegistration;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.data.RegistryData;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.Metrics;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.labyrinth.task.Synchronous;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;

final class Beginning extends StartProcedure {

	public Beginning(ClansJavaPlugin clansJavaPlugin) {
		super(clansJavaPlugin);
		then((start, instance) -> {
			instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
			instance.getLogger().info("- Clans [Pro]. Loading plugin information...");
			instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
			for (String ch : start.getLogo()) {
				instance.getLogger().info("- " + ch);
			}
			instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		}).then((start, instance) -> {
			instance.getLogger().info("- Starting registry procedures.");
			instance.dataManager.copyDefaults();
			new Registry<>(Listener.class).source(ClansAPI.getInstance().getPlugin()).pick("com.github.sanctum.clans.listener").operate(listener -> LabyrinthProvider.getService(Service.VENT).register(instance, listener));
			new Registry<>(Command.class).source(ClansAPI.getInstance().getPlugin()).pick("com.github.sanctum.clans.commands").operate(CommandRegistration::use);
			RegistryData<ClanAddon> data = new Registry
					.Loader<>(ClanAddon.class)
					.source(instance)
					.from("Addons")
					.confine(ClanAddonQuery::load);
			for (ClanAddon e : data.getData()) {
				sendBorder();
				instance.getLogger().info("- Injected: " + e.getName() + " v" + e.getVersion());
				sendBorder();
			}
			instance.getLogger().info("- (" + data.getData().size() + ") clan addon(s) were injected into cache.");
		}).then((start, instance) -> start.sendBorder()).then((start, instance) -> {
			instance.getLogger().info("- Cleaning misc files.");
			for (String id : Clan.ACTION.getAllClanIDs()) {
				if (ClansAPI.getInstance().getClanName(id) == null) {
					FileManager clan = DataManager.FileType.CLAN_FILE.get(id);
					clan.getRoot().delete();
				}
			}
		}).then((start, instance) -> start.sendBorder()).then((start, instance) -> {
			instance.getLogger().info("- Loading clans and claims, please be patient...");
			instance.getLogger().info("- Loaded (" + instance.getClanManager().refresh() + ") clans ");
			instance.getLogger().info("- Loaded (" + instance.getClaimManager().refresh() + ") claims");
		}).then((start, instance) -> start.sendBorder()).then((start, instance) -> {
			Synchronous sync = Schedule.sync(() -> {

				if (Bukkit.getOnlinePlayers().size() == 0) return;

				War war = ClansAPI.getInstance().getArenaManager().get("PRO");

				if (war != null) {

					if (war.isRunning()) {
						if (war.getTimer().isComplete()) {
							if (war.stop()) {
								War.Team winner = war.getMostPoints().getKey();
								int points = war.getMostPoints().getValue();
								Clan w = war.getClan(winner);
								Map<Clan, Integer> map = new HashMap<>();
								for (Clan c : war.getQueue().teams()) {
									if (!c.getName().equals(w.getName())) {
										War.Team t = war.getTeam(c);
										map.put(c, war.getPoints(t));
									}
								}
								ClanWarWonEvent e = ClanVentBus.call(new ClanWarWonEvent(war, new MapEntry<>(w, points), map));
								if (!e.isCancelled()) {
									Message msg = LabyrinthProvider.getService(Service.MESSENGER).getNewMessage().setPrefix(ClansAPI.getInstance().getPrefix().joined());
									Bukkit.broadcastMessage(" ");
									msg.broadcast("&3A war between clans &b[" + Arrays.stream(war.getQueue().teams()).map(Clan::getName).collect(Collectors.joining(",")) + "]&3 in arena &7#&e" + war.getId() + " &3concluded with winner &6&l" + w.getName() + " &f(&a" + points + "&f)");
									Bukkit.broadcastMessage(" ");
								}
								war.reset();
							}
						} else {
							ClanVentBus.call(new ClanWarActiveEvent(war));
						}
					}

				}

				for (Player p : Bukkit.getOnlinePlayers()) {

					Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

					if (associate == null) continue;

					Clan c = associate.getClan();

					for (ClanCooldown clanCooldown : c.getCooldowns()) {
						if (clanCooldown.isComplete()) {
							ClanCooldown.remove(clanCooldown);
							c.broadcast(MessageFormat.format(ClansAPI.getData().getMessageResponse("cooldown-expired"), clanCooldown.getAction().replace("Clans:", "")));
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
							cl.write(t -> t.set("ally-requests", allies));
							break;
						}
					}

				}

			});
			if (ClansAPI.getData().isTrue("Formatting.console-debug")) {
				sync.debug();
			}
			sync.debug().repeatReal(2, 18);
		}).then((start, instance) -> Schedule.sync(() -> {
			instance.getLogger().info("- Checking for placeholders.");
			if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
				new ClanPlaceholders(instance).register();
				instance.getLogger().info("- PlaceholderAPI found! Loading clans placeholders");
			} else {
				instance.getLogger().info("- PlaceholderAPI not found, placeholders will not work!");
			}
		}).wait(5)).then((start, instance) -> {
			if (ClansAPI.getData().isTrue("Clans.check-version")) {
				ClansAPI.getInstance().isUpdated();
			} else {
				instance.getLogger().info("- Version check skipped.");
			}
		}).then((start, instance) -> start.sendBorder()).then((start, instance) -> {
			ClansAPI.getInstance().getShieldManager().setEnabled(true);
			boolean configAllow = instance.dataManager.getMain().getRoot().getBoolean("Clans.raid-shield.allow");
			if (Claim.ACTION.isEnabled()) {
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
			} else {
				if (configAllow) {
					instance.getLogger().info("- Land claiming is turned off, to use the raid shield make sure you have claiming enabled.");
					instance.getLogger().info("- Denying raid shield timer. (Off)");
				}
			}
		}).then((start, instance) -> {
			ClanAddonQuery.load(instance, "com.github.sanctum.clans.bridge.internal");
			Schedule.sync(() -> {
				if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
					ClanAddonQuery.register(DynmapAddon.class);
				}
				if (EconomyProvision.getInstance().isValid()) {
					ClanAddonQuery.register(BountyAddon.class);
				}
			}).wait(5);
			instance.getLogger().info("- Found (" + ClanAddonQuery.getRegisteredAddons().size() + ") clan addon(s)");
			ClanAddonQuery.getRegisteredAddons().forEach(ClanAddonQuery::adjust);
			for (ClanAddon e : ClanAddonQuery.getRegisteredAddons().stream().sorted(Comparator.comparingInt(ClanAddon::getLevel)).collect(Collectors.toCollection(LinkedHashSet::new))) {
				if (e.persist()) {
					try {
						for (String precursor : e.getDependencies()) {
							ClanAddon addon = ClanAddonQuery.getAddon(precursor);
							ClanException.call(ClanAddonDependencyException::new).check(addon).run("Missing dependency " + precursor + " for addon " + e.getName() + ". Please install the missing dependency for this addon.");
						}
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
								instance.getLogger().info("- [" + e.getName() + "] (+1) Listener " + addition.getClass().getSimpleName() + " loaded.");
								Bukkit.getPluginManager().registerEvents(addition, instance);
							} else {
								instance.getLogger().info("- [" + e.getName() + "] (-1) Listener " + addition.getClass().getSimpleName() + " already loaded. Skipping.");
							}
						}
					} catch (NoClassDefFoundError | NoSuchMethodError ex) {
						instance.getLogger().severe("- An issue occurred while enabling addon " + e.getName());
						ex.printStackTrace();
						e.remove();
					}
				} else {
					sendBorder();
					instance.getLogger().info("- Addon: " + e.getName());
					instance.getLogger().info("- Description: " + e.getDescription());
					instance.getLogger().info("- Persistent: (" + e.persist() + ")");
					sendBorder();
					instance.getLogger().info("- Listeners: (" + e.getAdditions().size() + ")");
					e.remove();
					instance.getLogger().info("- [" + e.getName() + "] (+1) Addon failed to load due to no persistence.");
				}

			}
		}).then((start, instance) -> start.sendBorder()).then((start, instance) -> {
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
		}).then((start, instance) -> start.sendBorder()).then((start, instance) -> {
			start.runMetrics(metrics -> {
				metrics.addCustomChart(new Metrics.SimplePie("using_claiming", () -> {
					String result = "No";
					if (Claim.ACTION.isEnabled()) {
						result = "Yes";
					}
					return result;
				}));
				boolean configAllow = instance.dataManager.getMain().read(c -> c.getBoolean("Clans.raid-shield.allow"));
				metrics.addCustomChart(new Metrics.SimplePie("using_raidshield", () -> {
					String result = "No";
					if (configAllow) {
						result = "Yes";
					}
					return result;
				}));
				metrics.addCustomChart(new Metrics.DrilldownPie("addon_popularity", () -> {
					Map<String, Map<String, Integer>> map = new HashMap<>();
					Map<String, Integer> entry = new HashMap<>();
					for (ClanAddon cycle : ClanAddonQuery.getRegisteredAddons()) {
						if (cycle.persist()) {
							entry.put(Bukkit.getServer().getName(), 1);
							map.put(cycle.getName(), entry);
						}
					}
					return map;
				}));
				metrics.addCustomChart(new Metrics.SingleLineChart("total_logged_players", () -> Clan.ACTION.getAllUsers().size()));
				metrics.addCustomChart(new Metrics.SingleLineChart("total_clans_registered", () -> Clan.ACTION.getAllClanIDs().size()));
			});
		});

	}


}
