package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonDependencyException;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.ClanAddonRegistrationException;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.external.BountyAddon;
import com.github.sanctum.clans.bridge.external.DynmapAddon;
import com.github.sanctum.clans.construct.api.BanksAPI;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanException;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.bank.BankListener;
import com.github.sanctum.clans.construct.bank.BankPermissions;
import com.github.sanctum.clans.event.TimerEvent;
import com.github.sanctum.clans.event.claim.RaidShieldEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.annotation.Ordinal;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.command.CommandRegistration;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Item;
import com.github.sanctum.labyrinth.library.Metrics;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TimeWatch;
import com.github.sanctum.labyrinth.placeholders.PlaceholderRegistration;
import com.github.sanctum.labyrinth.task.LabyrinthApplicable;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;

// You're probably reading this and thinking "what is this..." its the power of ordinals baby.
public final class StartProcedure {

	final ClansJavaPlugin instance;
	static boolean bail;

	public StartProcedure(ClansJavaPlugin clansJavaPlugin) {
		this.instance = clansJavaPlugin;
	}

	void runMetrics(Consumer<Metrics> metrics) {
		Metrics.register(instance, 10461, metrics);
	}

	void sendBorder() {
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
	}

	List<String> getLogo() {
		return new ArrayList<>(Arrays.asList("   ▄▄▄·▄▄▄        ▄▄ ", "  ▐█ ▄█▀▄ █·▪     ██▌" + "  User ID: ", "   ██▀·▐▀▀▄  ▄█▀▄ ▐█·" + "   " + instance.USER_ID, "  ▐█▪·•▐█•█▌▐█▌.▐▌.▀ " + "  Unique ID: ", "  .▀   .▀  ▀ ▀█▄▀▪ ▀ " + "   " + instance.NONCE));
	}

	@Ordinal
	void x() {
		if (System.getProperty("RELOAD") != null && System.getProperty("RELOAD").equals("TRUE")) {
			bail = true;
			FileManager file = instance.getFileList().get("ignore", FileType.JSON);
			String location = new Date().toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE);
			List<String> toAdd = new ArrayList<>(file.getRoot().getStringList(location));
			toAdd.add("RELOAD DETECTED! Shutting down...");
			toAdd.add("      ██╗");
			toAdd.add("  ██╗██╔╝");
			toAdd.add("  ╚═╝██║ ");
			toAdd.add("  ██╗██║ ");
			toAdd.add("  ╚═╝╚██╗");
			toAdd.add("      ╚═╝");
			toAdd.add("(You are not supported in the case of corrupt data)");
			toAdd.add("(Reloading is NEVER safe and you should always restart instead.)");
			for (String t : toAdd) {
				instance.getLogger().severe(t);
			}
			file.write(t -> t.set(location, toAdd));
			Bukkit.getPluginManager().disablePlugin(instance);
		} else {
			System.setProperty("RELOAD", "FALSE");
		}
	}

	@Ordinal(1)
	void a() {
		if (bail) return;
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		instance.getLogger().info("- Clans [Pro]. Loading plugin information...");
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		for (String ch : getLogo()) {
			instance.getLogger().info("- " + ch);
		}
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
	}

	@Ordinal(2)
	void b() {
		if (bail) return;
		instance.getLogger().info("- Starting registry procedures.");
		instance.dataManager.copyDefaults();
		new Registry<>(Listener.class).source(ClansAPI.getInstance().getPlugin()).pick("com.github.sanctum.clans.listener").operate(listener -> LabyrinthProvider.getService(Service.VENT).subscribe(instance, listener));
		new Registry<>(Command.class).source(ClansAPI.getInstance().getPlugin()).pick("com.github.sanctum.clans.commands").operate(CommandRegistration::use);
		ClanAddonRegistrationException.getLoadingProcedure().run(instance).deploy();
	}

	@Ordinal(4)
	void d() {
		if (bail) return;
		sendBorder();
		instance.getLogger().info("- Loading clans and claims, please be patient...");
		instance.getLogger().info("- Loaded (" + instance.getClanManager().refresh() + ") clans ");
		instance.getLogger().info("- Loaded (" + instance.getClaimManager().refresh() + ") claims");
		sendBorder();
		instance.getLogger().info("- Cleaning misc files.");
		for (String id : Clan.ACTION.getAllClanIDs()) {
			if (ClansAPI.getInstance().getClanManager().getClanName(HUID.fromString(id)) == null) {
				Clan o = ClansAPI.getInstance().getClanManager().getClan(HUID.fromString(id));
				FileManager clan = ClansAPI.getDataInstance().getClanFile(o);
				clan.getRoot().delete();
				o.remove();
			}
		}
	}

	@Ordinal(6)
	void f() {
		if (bail) return;
		instance.getLogger().info("- Checking for placeholders.");
		new LabyrinthApplicable("placeholder_registration") {

			private static final long serialVersionUID = 379087412543385L;
			private long time;

			@Override
			public void run() {
				if (time == 0) {
					time = System.currentTimeMillis();
				}
				if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
					new PapiPlaceholders(instance).register();
					new LabyrinthPlaceholders(instance).register().deploy();
					instance.getLogger().info("- PlaceholderAPI found! Loading clans placeholders");
					cancel();
				} else {
					if (TimeWatch.Recording.subtract(time).getMinutes() >= 1) {
						PlaceholderRegistration.getInstance().registerTranslation(new LabyrinthPlaceholders(instance)).deploy();
						instance.getLogger().info("- PlaceholderAPI not found, loading labyrinth provision.");
						cancel();
					}
				}
			}

		}.scheduleTimer(5, 5);

		try {
			new Item(Material.BLAZE_ROD, StringUtils.use("&r[&6Tamer stick&r]").translate()).setKey("tamer_stick")
					.buildStack()
					.attachLore(Arrays.asList("Right click a tamed animal", "to add it as a teammate.", " ", "This will not persist it is", "merely a way to momentarily keep track of", "any tamed animals."))
					.setItem('U', Material.AIR)
					.setItem('I', Material.BLAZE_ROD)
					.setItem('G', Material.LEATHER)
					.shapeRecipe("UGU", "UIU", "UIU")
					.register();
			new Item(Material.STICK, StringUtils.use("&r[&bRemover stick&r]").translate()).setKey("remover_stick")
					.buildStack()
					.attachLore(Arrays.asList("Right click a tamed animal teammate", "to remove it as a teammate."))
					.setItem('U', Material.AIR)
					.setItem('I', Material.STICK)
					.setItem('G', Material.LEATHER)
					.shapeRecipe("UGU", "UIU", "UIU")
					.register();
		} catch (UnsupportedOperationException failed) {
			instance.getLogger().severe("- We were unable to register some crafting recipes due to modded circumstances.");
			instance.getLogger().severe("- Items not registered: [tamer_stick, remover_stick]");
		}
	}

	@Ordinal(7)
	void g() {
		if (bail) return;
		sendBorder();
		if (ClansAPI.getDataInstance().isTrue("Clans.check-version")) {
			ClansAPI.getInstance().isUpdated();
		} else {
			instance.getLogger().info("- Version check skipped.");
		}
		sendBorder();
	}

	@Ordinal(8)
	void h() {
		if (bail) return;
		ClansAPI.getInstance().getShieldManager().setEnabled(true);
		boolean configAllow = instance.dataManager.getConfig().getRoot().getBoolean("Clans.raid-shield.allow");
		if (Claim.ACTION.isEnabled()) {
			if (configAllow) {
				ClanVentBus.subscribe(TimerEvent.class, Vent.Priority.HIGH, (event, subscription) -> {
					if (event.isAsynchronous()) return;
					new Vent.Call<>(Vent.Runtime.Synchronous, new RaidShieldEvent()).run();
				});
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
	}

	@Ordinal(9)
	void i() {
		if (bail) return;
		ClanAddonQuery.load(instance, "com.github.sanctum.clans.bridge.internal");
		TaskScheduler.of(() -> {
			if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
				ClanAddonQuery.register(DynmapAddon.class);
			}
			if (EconomyProvision.getInstance().isValid()) {
				ClanAddonQuery.register(BountyAddon.class);
			}
		}).scheduleLater(5);
		instance.getLogger().info("- Found (" + ClanAddonQuery.getRegisteredAddons().size() + ") clan addon(s)");
		ClanAddonQuery.getRegisteredAddons().forEach(ClanAddonQuery::adjust);
		for (ClanAddon e : ClanAddonQuery.getRegisteredAddons().stream().sorted(Comparator.comparingInt(value -> value.getContext().getLevel())).collect(Collectors.toCollection(LinkedHashSet::new))) {
			if (e.isPersistent()) {
				try {
					for (String precursor : e.getContext().getDependencies()) {
						ClanAddon addon = ClanAddonQuery.getAddon(precursor);
						ClanException.call(ClanAddonDependencyException::new).check(addon).run("Missing dependency " + precursor + " for addon " + e.getName() + ". Please install the missing dependency for this addon.");
					}
					e.onEnable();
					sendBorder();
					instance.getLogger().info("- Addon: " + e.getName());
					instance.getLogger().info("- Description: " + e.getDescription());
					instance.getLogger().info("- Persistent: (" + e.isPersistent() + ")");
					sendBorder();

					instance.getLogger().info("- Listeners: (" + e.getContext().getListeners().length + ")");
					for (Listener listener : e.getContext().getListeners()) {
						boolean registered = HandlerList.getRegisteredListeners(instance).stream().anyMatch(r -> r.getListener().equals(listener));
						if (!registered) {
							instance.getLogger().info("- [" + e.getName() + "] (+1) Listener " + listener.getClass().getSimpleName() + " loaded.");
							LabyrinthProvider.getInstance().getEventMap().subscribe(instance, listener);
						} else {
							instance.getLogger().info("- [" + e.getName() + "] (-1) Listener " + listener.getClass().getSimpleName() + " already loaded. Skipping.");
						}
					}
				} catch (NoClassDefFoundError | NoSuchMethodError ex) {
					instance.getLogger().severe("- An issue occurred while enabling addon " + e.getName());
					ex.printStackTrace();
					ClanAddonQuery.remove(e);
				}
			} else {
				sendBorder();
				instance.getLogger().info("- Addon: " + e.getName());
				instance.getLogger().info("- Description: " + e.getDescription());
				instance.getLogger().info("- Persistent: (" + e.isPersistent() + ")");
				sendBorder();
				instance.getLogger().info("- Listeners: (" + e.getContext().getListeners().length + ")");
				ClanAddonQuery.remove(e);
				for (Listener l : e.getContext().getListeners()) {
					instance.getLogger().info("- [" + l.getClass().getSimpleName() + "] (+1) Listener failed to load due to no persistence.");
				}
			}

		}
		sendBorder();
	}

	@Ordinal(10)
	void j() {
		if (bail) return;
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
		LabyrinthProvider.getInstance().getEventMap().subscribe(instance, new BankListener());
		instance.getLogger().info("- Banking log-level=" + BanksAPI.getInstance().logToConsole());
	}

	@Ordinal(11)
	void k() {
		if (bail) return;
		runMetrics(metrics -> {
			metrics.addCustomChart(new Metrics.SimplePie("using_claiming", () -> {
				String result = "No";
				if (Claim.ACTION.isEnabled()) {
					result = "Yes";
				}
				return result;
			}));
			boolean configAllow = instance.dataManager.getConfig().read(c -> c.getBoolean("Clans.raid-shield.allow"));
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
					if (cycle.isPersistent()) {
						entry.put(Bukkit.getServer().getName(), 1);
						map.put(cycle.getName(), entry);
					}
				}
				return map;
			}));
			metrics.addCustomChart(new Metrics.SingleLineChart("total_logged_players", () -> Clan.ACTION.getAllUsers().size()));
			metrics.addCustomChart(new Metrics.SingleLineChart("total_clans_registered", () -> Clan.ACTION.getAllClanIDs().size()));
		});
	}

	@Ordinal(12)
	void l() {
		if (bail) return;
		if (ClansAPI.getDataInstance().isUpdate()) {
			instance.getLogger().info("- Configuration updated to latest.");
		}
		bail = true;
	}


}
