package com.github.sanctum.clans.util.data;

import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClansUpdate;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.bank.BankListener;
import com.github.sanctum.clans.construct.bank.BankPermissions;
import com.github.sanctum.clans.util.Placeholders;
import com.github.sanctum.clans.util.events.clans.RaidShieldEvent;
import com.github.sanctum.labyrinth.command.CommandRegistration;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.data.RegistryData;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.link.CycleList;
import com.github.sanctum.link.EventCycle;
import com.github.sanctum.link.dynmap.DynmapCycle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
		if (id != 420) {
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
		instance.getLogger().info("- Reacquiring user data.");
		for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
			if (DefaultClan.action.getClanID(p.getUniqueId()) != null) {
				if (!instance.getAssociate(p).isPresent()) {
					ClansAPI.getData().ASSOCIATES.add(new ClanAssociate(p));
				}
			}
		}
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
		instance.getLogger().info("- Checking for addon event cycles.");
		CycleList.pickupCycles(instance, "com.github.sanctum.link.cycles");
		Schedule.sync(() -> {
			if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
				instance.importAddon(DynmapCycle.class);
			}
		}).wait(5);
		instance.getLogger().info("- Found (" + CycleList.getRegisteredCycles().size() + ") event cycle(s)");
		for (EventCycle e : CycleList.getRegisteredCycles()) {
			if (e.persist()) {

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
