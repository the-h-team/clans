package com.github.sanctum.clans.construct.util;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonDependencyException;
import com.github.sanctum.clans.bridge.ClanAddonQueue;
import com.github.sanctum.clans.bridge.ClanAddonRegistrationException;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.ClanVentCall;
import com.github.sanctum.clans.bridge.external.BountyAddon;
import com.github.sanctum.clans.bridge.external.DynmapAddon;
import com.github.sanctum.clans.bridge.internal.StashesAddon;
import com.github.sanctum.clans.bridge.internal.VaultsAddon;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.AbstractGameRule;
import com.github.sanctum.clans.construct.api.BanksAPI;
import com.github.sanctum.clans.construct.api.Channel;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanException;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.LogoHolder;
import com.github.sanctum.clans.construct.api.RankRegistry;
import com.github.sanctum.clans.construct.api.QnA;
import com.github.sanctum.clans.construct.bank.BankListener;
import com.github.sanctum.clans.construct.bank.BankPermissions;
import com.github.sanctum.clans.construct.impl.DefaultDocketRegistry;
import com.github.sanctum.clans.event.TimerEvent;
import com.github.sanctum.clans.event.claim.RaidShieldEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.data.service.PlayerSearch;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.string.FormattedString;
import com.github.sanctum.labyrinth.gui.unity.simple.Docket;
import com.github.sanctum.labyrinth.gui.unity.simple.MemoryDocket;
import com.github.sanctum.labyrinth.interfacing.UnknownGeneric;
import com.github.sanctum.labyrinth.library.CommandUtils;
import com.github.sanctum.labyrinth.library.Item;
import com.github.sanctum.labyrinth.library.Metrics;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.Workbench;
import com.github.sanctum.labyrinth.library.WorkbenchSlot;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.annotation.AnnotationDiscovery;
import com.github.sanctum.panther.annotation.Ordinal;
import com.github.sanctum.panther.event.VentMap;
import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.placeholder.PlaceholderRegistration;
import com.github.sanctum.panther.util.RandomID;
import com.github.sanctum.skulls.CustomHead;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;

// You're probably reading this and thinking "what is this..." its the power of ordinals baby.
public final class StartProcedure {

	final ClansJavaPlugin instance;
	static boolean softBail;

	public StartProcedure(ClansJavaPlugin clansJavaPlugin) {
		this.instance = clansJavaPlugin;
	}

	void runMetrics(Consumer<Metrics> metrics) {
		Metrics.register(instance, 10461, metrics);
	}

	void sendBorder() {
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
	}

	String replaceDevKey(String key, int id) {
		if (key.startsWith("%%__USER")) {
			if (instance.isTrial()) return "DEMO";
			return "UN-VERIFIED";
		}
		if (key.startsWith("%%__NONCE")) {
			return "" + id;
		}
		return key;
	}

	List<String> getLogo() {
		// FIXME update ascii art
		if (instance.isTrial()) {
			return new ArrayList<>(Arrays.asList("▄▄▄▄▄▄▄▄ .▄▄▄▄▄ ▄ .▄▄▄▄ .▄▄▄  ", "•██  ▀▄.▀·•██  ██▪▐█▀▄.▀·▀▄ █·", " ▐█.▪▐▀▀▪▄ ▐█.▪██▀▐█▐▀▀▪▄▐▀▀▄ ", " ▐█▌·▐█▄▄▌ ▐█▌·██▌▐▀▐█▄▄▌▐█•█▌", " ▀▀▀  ▀▀▀  ▀▀▀ ▀▀▀ · ▀▀▀ .▀  ▀"));
		}
		return new ArrayList<>(Arrays.asList("▄▄▄▄▄▄▄▄ .▄▄▄▄▄ ▄ .▄▄▄▄ .▄▄▄  ", "•██  ▀▄.▀·•██  ██▪▐█▀▄.▀·▀▄ █·" + "  User ID: ", " ▐█.▪▐▀▀▪▄ ▐█.▪██▀▐█▐▀▀▪▄▐▀▀▄ " + "   " + replaceDevKey(instance.USER_ID, 0), " ▐█▌·▐█▄▄▌ ▐█▌·██▌▐▀▐█▄▄▌▐█•█▌" + "  Unique ID: ", " ▀▀▀  ▀▀▀  ▀▀▀ ▀▀▀ · ▀▀▀ .▀  ▀" + "   " + replaceDevKey(instance.NONCE, Integer.parseInt(new RandomID(5, "0123456789").generate()))));
	}

	@Ordinal
	void x() {
		if (System.getProperty("RELOAD") != null && System.getProperty("RELOAD").equals("TRUE")) {
			instance.getLogger().warning("Attempting soft load. (Reload)");
			instance.getLogger().warning("This feature can work but is not recommended. You should always restart to apply major changes.");
			TaskScheduler.of(() -> {
				Bukkit.broadcastMessage(StringUtils.use(instance.getPrefix() + " Alright... so it appears an admin has reloaded the server, someone will need to re-log. Just one person.").translate());
			}).scheduleLater(20L);
			softBail = true;
		} else {
			System.setProperty("RELOAD", "FALSE");
		}
		// Pre-handle game rule injection.
		LabyrinthProvider.getInstance().getLocalPrintManager().register(() -> {
			Map<String, Object> map = new HashMap<>();
			DataManager manager = ClansAPI.getDataInstance();
			manager.getConfig().getRoot().reload();
			manager.getMessages().getRoot().reload();
			map.put(AbstractGameRule.WAR_START_TIME, manager.getConfigInt("Clans.war.start-wait"));
			map.put(AbstractGameRule.BLOCKED_WAR_COMMANDS, manager.getConfig().getRoot().getStringList("Clans.war.blocked-commands"));
			map.put(AbstractGameRule.MAX_CLANS, manager.getConfigInt("Clans.max-clans"));
			map.put(AbstractGameRule.MAX_POWER, manager.getConfig().getRoot().getNode("Clans.max-power").toPrimitive().getDouble());
			map.put(AbstractGameRule.DEFAULT_WAR_MODE, manager.getConfigString("Clans.mode-change.default"));
			map.put(AbstractGameRule.CLAN_INFO_SIMPLE, manager.getMessages().getRoot().getStringList("info-simple"));
			map.put(AbstractGameRule.CLAN_INFO_SIMPLE_OTHER, manager.getMessages().getRoot().getStringList("info-simple-other"));
			map.putAll(manager.getResetTable().values());
			manager.getResetTable().clear();
			return map;
		}, instance.getLocalPrintKey());
	}

	@Ordinal(1)
	void a() {
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		instance.getLogger().info("- Clans. Loading plugin information...");
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		for (String ch : getLogo()) {
			instance.getLogger().info("- " + ch);
		}
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
	}

	@Ordinal(2)
	void b() {
		if (softBail) {
			VentMap.getInstance().unsubscribeAll(instance);
		}
		instance.getLogger().info("- Starting registry procedures.");
		instance.dataManager.copyDefaults();
		new Registry<>(Listener.class).source(instance).filter("com.github.sanctum.clans.listener").operate(listener -> VentMap.getInstance().subscribe(instance, listener));
		new Registry<>(Command.class).source(instance).filter("com.github.sanctum.clans.commands").operate(CommandUtils::register);
		new Registry<>(ClanSubCommand.class).source(instance).filter("com.github.sanctum.clans.commands").operate(cmd -> {
			if (!cmd.isInvisible()) {
				instance.getCommandManager().register(cmd);
			}
		});
		ClanAddonRegistrationException.getLoadingProcedure().run(instance).deploy();
	}

	@Ordinal(4)
	void d() {
		sendBorder();
		instance.getLogger().info("- Cleaning misc files.");
		final FileList fileList = instance.getFileList();
		for (String id : Clan.ACTION.getAllClanIDs()) {
			FileManager clan = fileList.get(id, "Clans", instance.TYPE);
			FileManager clanClaims = instance.getClaimManager().getFile();
			clanClaims.getRoot().getNode(id).delete();
			if (clan.read(c -> !c.getNode("name").toPrimitive().isString())) {
				clan.getRoot().delete();
			}
		}
		sendBorder();
		RankRegistry registry = RankRegistry.getInstance();
		instance.getLogger().info("- Loading clans and claims, please be patient...");
		registry.load();
		registry.order();
		instance.getLogger().info("- Loaded (" + instance.getClanManager().refresh() + ") clans ");
		instance.getLogger().info("- Loaded (" + instance.getClaimManager().refresh() + ") claims");
	}

	@Ordinal(6)
	void f() {
		if (!softBail) {
			TaskScheduler.of(() -> {
				instance.getLogger().info("- Checking for placeholders.");
				if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
					new PapiPlaceholders(instance).register();
					new PantherPlaceholders(instance).register();
					instance.getLogger().info("- PlaceholderAPI found! Loading clans placeholders");
				} else {
					PlaceholderRegistration.getInstance().registerTranslation(new PantherPlaceholders(instance));
					instance.getLogger().info("- PlaceholderAPI not found, loading labyrinth provision.");
				}
			}).scheduleLater(38);
		}

		if (!LabyrinthProvider.getInstance().isModded()) {
			Consumer<Workbench> workbench = w -> {
				w.put('U', WorkbenchSlot.ONE, WorkbenchSlot.THREE, WorkbenchSlot.FOUR, WorkbenchSlot.SIX, WorkbenchSlot.SEVEN, WorkbenchSlot.NINE);
				w.put('G', WorkbenchSlot.TWO);
				w.put('I', WorkbenchSlot.FIVE, WorkbenchSlot.EIGHT);
			};
			new Item().setKey("tamer_stick", instance)
					.edit()
					.setType(Material.BLAZE_ROD)
					.setTitle("&r[&6Tamer stick&r]")
					.setLore(Arrays.asList("Right click a tamed animal to add it as a teammate.", " ", "This effect is temporary."))
					.complete()
					.setItem('U', Material.AIR)
					.setItem('I', Material.BLAZE_ROD)
					.setItem('G', Material.LEATHER)
					.shape(workbench)
					.register();
			new Item().setKey("remover_stick", instance)
					.edit()
					.setType(Material.STICK)
					.setTitle("&r[&bRemover stick&r]")
					.setLore(Collections.singletonList("Right click a tamed animal teammate to remove it as a teammate."))
					.complete()
					.setItem('U', Material.AIR)
					.setItem('I', Material.STICK)
					.setItem('G', Material.LEATHER)
					.shape(workbench)
					.register();
		}
	}

	@Ordinal(7)
	void g() {
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
		ClansAPI.getInstance().getShieldManager().setEnabled(true);
		boolean configAllow = instance.dataManager.getConfig().getRoot().getBoolean("Clans.raid-shield.allow");
		if (Claim.ACTION.isAllowed().deploy()) {
			if (configAllow) {
				ClanVentBus.HIGH_PRIORITY.subscribeTo(TimerEvent.class, (event, subscription) -> {
					if (event.isAsynchronous()) return;
					new ClanVentCall<>(new RaidShieldEvent()).schedule();
				}).queue();
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
		ClanAddonQueue queue = ClanAddonQueue.getInstance();
		queue.load(instance, "com.github.sanctum.clans.bridge.internal");
		TaskScheduler.of(() -> {
			if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
				for (String s : queue.register(DynmapAddon.class).read()) {
					instance.getLogger().info(s);
				}
			}
			if (EconomyProvision.getInstance().isValid()) {
				for (String s : queue.register(BountyAddon.class).read()) {
					instance.getLogger().info(s);
				}
			}
		}).scheduleLater(160);
		instance.getLogger().info("- Found (" + queue.get().size() + ") clan addon(s)");
		queue.get().forEach(queue::bump);
		for (ClanAddon e : queue.get().stream().sorted(Comparator.comparingInt(value -> value.getContext().getLevel())).collect(Collectors.toCollection(LinkedHashSet::new))) {
			if (e.isPersistent()) {
				try {
					for (String precursor : e.getContext().getDependencies()) {
						ClanAddon addon = queue.get(precursor);
						ClanException.call(ClanAddonDependencyException::new).check(addon).run("Missing dependency " + precursor + " for addon " + e.getName() + ". Please install the missing dependency for this addon.");
					}
					e.onEnable();
					e.getContext().setActive(true);
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
					queue.remove(e);
				}
			} else {
				sendBorder();
				instance.getLogger().info("- Addon: " + e.getName());
				instance.getLogger().info("- Description: " + e.getDescription());
				instance.getLogger().info("- Persistent: (" + e.isPersistent() + ")");
				sendBorder();
				instance.getLogger().info("- Listeners: (" + e.getContext().getListeners().length + ")");
				AnnotationDiscovery<Ordinal, ClanAddon> discovery = AnnotationDiscovery.of(Ordinal.class, ClanAddon.class);
				discovery.filter(method -> method.getName().equals("remove"), true);
				discovery.ifPresent((ordinal, method) -> {
					try {
						method.invoke(e);
					} catch (IllegalAccessException | InvocationTargetException ex) {
						ex.printStackTrace();
					}
				});
				for (Listener l : e.getContext().getListeners()) {
					instance.getLogger().info("- [" + l.getClass().getSimpleName() + "] (+1) Listener failed to load due to no persistence.");
				}
			}

		}
		sendBorder();
	}

	@Ordinal(10)
	void j() {
		final Permission balance = new Permission(BankPermissions.BANKS_BALANCE.getNode());
		final Permission deposit = new Permission(BankPermissions.BANKS_DEPOSIT.getNode());
		final Permission withdraw = new Permission(BankPermissions.BANKS_WITHDRAW.getNode());
		final Permission use = new Permission(BankPermissions.BANKS_USE.getNode());
		balance.addParent(use, true);
		final Permission useStar = new Permission(BankPermissions.BANKS_USE_STAR.getNode());
		use.addParent(useStar, true);
		deposit.addParent(useStar, true);
		withdraw.addParent(useStar, true);
		final Permission star = new Permission(BankPermissions.BANKS_STAR.getNode());
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
		if (softBail) return;
		runMetrics(metrics -> {
			metrics.addCustomChart(new Metrics.SimplePie("using_claiming", () -> {
				String result = "No";
				if (Claim.ACTION.isAllowed().deploy()) {
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
				for (ClanAddon cycle : ClanAddonQueue.getInstance().get()) {
					if (cycle.isPersistent()) {
						entry.put(Bukkit.getServer().getName(), 1);
						map.put(cycle.getName(), entry);
					}
				}
				return map;
			}));
			metrics.addCustomChart(new Metrics.SingleLineChart("total_logged_players", () -> PlayerSearch.values().size()));
			metrics.addCustomChart(new Metrics.SingleLineChart("total_clans_registered", () -> Clan.ACTION.getAllClanIDs().size()));
		});
	}

	@Ordinal(12)
	void l() {
		if (ClansAPI.getDataInstance().updateConfigs()) {
			instance.getLogger().info("- Configuration updated to latest.");
		}
	}

	@Ordinal(13)
	void m() {
		// System adapter for locating MOTD carriers.
		LogoHolder.newAdapter(location -> {
			LogoHolder.Carrier def = ReservedLogoCarrier.MOTD;
			for (LogoHolder.Carrier.Line line : def.getLines()) {
				for (int i = 1; i < 23; i++) {
					Block up = location.getBlock().getRelative(BlockFace.UP, i);
					if (line.getStand().getLocation().distance(up.getLocation().add(0.5, 0, 0.5)) <= 1) {
						return def;
					}
				}
			}
			return null;
		}).deploy();
	}

	@Ordinal(14)
	void n() {
		Channel.CLAN.register(context -> {
			String test = context;
			for (String word : instance.dataManager.getConfig().getRoot().getNode("Formatting.Chat.Channel.clan.filters").getKeys(false)) {
				String replacement = instance.dataManager.getConfig().getRoot().getNode("Formatting.Chat.Channel.clan.filters").getNode(word).toPrimitive().getString();
				test = StringUtils.use(test).replaceIgnoreCase(word, replacement);
			}
			return test;
		});

		Channel.ALLY.register(context -> {
			String test = context;
			for (String word : instance.dataManager.getConfig().getRoot().getNode("Formatting.Chat.Channel.ally.filters").getKeys(false)) {
				String replacement = instance.dataManager.getConfig().getRoot().getNode("Formatting.Chat.Channel.ally.filters").getNode(word).toPrimitive().getString();
				test = StringUtils.use(test).replaceIgnoreCase(word, replacement);
			}
			return test;
		});

		QnA.register((player, question) -> {
			StringUtils utils = StringUtils.use(question);
			// keeping 'group' entries for backwards compatibility
			if (utils.containsIgnoreCase(
					"make a clan", "create a clan", "start a clan", "start clan", "make clan", "create clan",
					"make a group", "create a group", "start a group", "start group", "make group", "create group"
			)) {
				player.closeInventory();
				String message = "To make a clan you require the permission clanspro." + DataManager.Security.getPermission("create") + ", if you have permission this message will be white.";
				if (!player.hasPermission("clanspro." + DataManager.Security.getPermission("create"))) {
					Clan.ACTION.sendMessage(player, "&c" + message);
				} else {
					Clan.ACTION.sendMessage(player, message);
					String[] examples = new String[]{"Panthers", "Eggnog", "Dumplin", "Potato"};
					new FancyMessage("Click here to see an example").color(ChatColor.AQUA).style(ChatColor.ITALIC).suggest("/clan create " + examples[new Random().nextInt(examples.length)] + " (password here if you want)").send(player).queue();
				}
				return false;
			}
			if (utils.containsIgnoreCase("whats the raidshield", "what is raidshield", "what is raid shield", "raid shield", "raidshield")) {
				player.closeInventory();
				String message = "The default settings will start the raidshield at dawn and the raidshield will go down at dusk. Once the raidshield goes down this enables players in other clans with more clan power than yours to then overpower the land unclaiming and taking it as their own if they choose. Either way it enables raiding.";
				Clan.ACTION.sendMessage(player, message);
				Clan.ACTION.sendMessage(player, "When the raidshield is down and you indefinitely have more power than a targeted enemy clan, use &c/c unclaim &fto over power their land.");
				return false;
			}
			if (utils.containsIgnoreCase("how do i raid", "how to raid", "raiding", "raid")) {
				if (Claim.ACTION.isAllowed().deploy()) {
					Clan.ACTION.sendMessage(player, "&eRaiding can be achieved by simply overpowering a target clan's land, un-claiming an individual chunk allows you to access the containers within as well as build/break.");
				} else {
					Clan.ACTION.sendMessage(player, "&cClan land claiming is disabled on this server! Therefore raiding becomes inherently impossible.");
				}
				return false;
			}
			if (utils.containsIgnoreCase("gain power", "how do i get more power", "more power", "get power")) {
				Clan.ACTION.sendMessage(player, "&6You can gain power by increasing the overall size of your clan (more members), having more money in the clan bank, owning more clan claims & killing enemy clan associates as well any third party ways to achieve power gains.");
				return false;
			}
			return true;
		});
	}

	@Ordinal(15)
	void o() {
		TaskScheduler.of(() -> {
			for (Clan owner : instance.getClanManager().getClans()) {
				TaskScheduler.of(() -> {
					// Load clan vault and stash into memory.
					VaultsAddon.getVault(owner.getName());
					StashesAddon.getStash(owner.getName());
				}).scheduleLaterAsync(1);
				for (Claim c : owner.getClaims()) {
					// check default flags and register any that arent registered.
					for (Claim.Flag f : instance.getClaimManager().getFlagManager().getFlags()) {
						Claim.Flag temp = c.getFlag(f.getId());
						if (temp == null) {
							c.register(f);
						}
					}
					c.save();
				}
			}
		}).scheduleLater(120);
	}

	@Ordinal(16)
	void p() {
		FileManager th = instance.getFileList().get("heads", "Configuration", Configurable.Type.JSON);
		if (th.getRoot().exists()) {
			th.toMoved("Configuration/Data");
		}
		FileManager man = instance.getFileList().get("heads", "Configuration/Data", Configurable.Type.JSON);
		if (!man.getRoot().exists()) {
			instance.getFileList().copy("config/heads.json", man);
			man.getRoot().reload();
		}
		CustomHead.Manager.newLoader(man.getRoot())
				.look("My_heads")
				.complete();
	}

	@Ordinal(17)
	void q() {
		// load dockets
		ClansAPI api = ClansAPI.getInstance();
		MemoryDocket<Clan> rosterDocket = Docket.newInstance(ClansAPI.getDataInstance().getMessages().getRoot().getNode("menu.roster"));
		rosterDocket.setDataConverter(Clan.memoryDocketReplacer());
		rosterDocket.setNamePlaceholder(":not_supported:");
		rosterDocket.setList(() -> api.getClanManager().getClans().stream().collect(Collectors.toList()));
		rosterDocket.load();
		DefaultDocketRegistry.load(rosterDocket.toMenu().getKey().orElseThrow(RuntimeException::new), rosterDocket);
		MemoryDocket<String> headDocket = new MemoryDocket<>(ClansAPI.getDataInstance().getMessages().getRoot().getNode("menu.head-library"));
		headDocket.setDataConverter((s, h) -> new FormattedString(s).replace("%head_name%", h).get());
		headDocket.setNamePlaceholder(":not_supported:");
		headDocket.setList(() -> CustomHead.Manager.getHeads().stream().map(CustomHead::name).collect(Collectors.toList()));
		headDocket.load();
		DefaultDocketRegistry.load(headDocket.toMenu().getKey().orElseThrow(RuntimeException::new), headDocket);
		MemoryDocket<Clan> rosterTopDocket = Docket.newInstance(ClansAPI.getDataInstance().getMessages().getRoot().getNode("menu.roster-top"));
		rosterTopDocket.setDataConverter(Clan.memoryDocketReplacer());
		rosterTopDocket.setNamePlaceholder(":not_supported:");
		rosterTopDocket.setList(() -> api.getClanManager().getClans().stream().collect(Collectors.toList()));
		rosterTopDocket.setComparator(ClansComparators.comparingByPower());
		rosterTopDocket.load();
		DefaultDocketRegistry.load(rosterTopDocket.toMenu().getKey().orElseThrow(RuntimeException::new), rosterTopDocket);
		MemoryDocket<UnknownGeneric> rosterSelect = Docket.newInstance(ClansAPI.getDataInstance().getMessages().getRoot().getNode("menu.roster-select"));
		rosterSelect.setNamePlaceholder(":not_supported:");
		rosterSelect.load();
		DefaultDocketRegistry.load(rosterSelect.toMenu().getKey().orElseThrow(RuntimeException::new), rosterSelect);
	}


}
