package com.github.sanctum.clans;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.ClaimManager;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.ClanManager;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.ShieldManager;
import com.github.sanctum.clans.construct.actions.ClanCooldown;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.bank.BankMeta;
import com.github.sanctum.clans.construct.extra.misc.ClanPrefix;
import com.github.sanctum.clans.util.RankPriority;
import com.github.sanctum.clans.util.data.DataManager;
import com.github.sanctum.clans.util.data.Metrics;
import com.github.sanctum.clans.util.data.StartProcedure;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.data.RegistryData;
import com.github.sanctum.labyrinth.data.container.KeyedServiceManager;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.link.CycleList;
import com.github.sanctum.link.EventCycle;
import com.github.sanctum.skulls.CustomHead;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * MIT License
 * <p>
 * Copyright (c) 2021 Sanctum
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * <p>You will be <strong>required</strong> to publicly display credit to the original authors in any postings regarding both "remastering" or
 * forking of this project. While not enforced what so ever, if you decide on forking + re-selling under
 * modified circumstances that you pay us a royalty fee of $4.50 USD to respect our side of the work involved.</p>
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class ClansPro extends JavaPlugin implements ClansAPI {

	private static ClansPro PRO;

	private static FileList origin;

	private ClanPrefix prefix;

	private ClaimManager claimManager;

	private ShieldManager shieldManager;

	private ClanManager clanManager;

	private KeyedServiceManager<EventCycle> serviceManager;

	public DataManager dataManager = new DataManager();

	public String USER_ID = "%%__USER__%%";

	public String NONCE = "%%__NONCE__%%";

	public void onEnable() {
		setInstance(this);
		origin = FileList.search(this);
		serviceManager = new KeyedServiceManager<>();
		clanManager = new ClanManager();
		claimManager = new ClaimManager();
		shieldManager = new ShieldManager();
		if (System.getProperty("OLD") != null && System.getProperty("OLD").equals("TRUE")) {
			getLogger().severe("- RELOAD DETECTED! Shutting down...");
			getLogger().severe("      ██╗");
			getLogger().severe("  ██╗██╔╝");
			getLogger().severe("  ╚═╝██║ ");
			getLogger().severe("  ██╗██║ ");
			getLogger().severe("  ╚═╝╚██╗");
			getLogger().severe("      ╚═╝");
			getLogger().severe("- (You are not supported in the case of corrupt data)");
			getLogger().severe("- (Reloading is NEVER safe and you should always restart instead.)");
			FileManager file = origin.find("ignore", "");
			String location = new Date().toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE);
			List<String> toAdd = new ArrayList<>(file.getConfig().getStringList(location));
			toAdd.add("RELOAD DETECTED! Shutting down...");
			toAdd.add("      ██╗");
			toAdd.add("  ██╗██╔╝");
			toAdd.add("  ╚═╝██║ ");
			toAdd.add("  ██╗██║ ");
			toAdd.add("  ╚═╝╚██╗");
			toAdd.add("      ╚═╝");
			toAdd.add("(You are not supported in the case of corrupt data)");
			toAdd.add("(Reloading is NEVER safe and you should always restart instead.)");
			file.getConfig().set(location, toAdd);
			file.saveConfig();
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		} else {
			System.setProperty("OLD", "FALSE");
		}


		dataManager.assertDefaults();
		StartProcedure primary;
		try {
			primary = new StartProcedure(this, DataManager.hidden());
		} catch (IllegalAccessException e) {
			// switch to re-throw over assertion
			throw new IllegalStateException("Unable to properly initialize the plugin!", e);
		}
		primary.printLogo();
		primary.registerDefaults();

		FileManager main = ClansAPI.getData().getMain();

		this.prefix = new ClanPrefix(main.getConfig().getString("Formatting.prefix.prefix"), main.getConfig().getString("Formatting.prefix.text"), main.getConfig().getString("Formatting.prefix.suffix"));

		primary.sendBorder();
		primary.runDataCleaner();
		primary.sendBorder();
		primary.runCacheLoader();
		primary.refreshChat();
		primary.sendBorder();
		primary.refillUserData();
		primary.sendBorder();
		primary.checkForPlaceholders();
		primary.sendBorder();
		primary.checkForUpdate();
		primary.sendBorder();
		primary.runShieldTimer();
		primary.sendBorder();
		primary.checkForCycles();
		primary.sendBorder();
		primary.setupBank();
		primary.sendBorder();
		primary.registerMetrics(10461, metrics -> {
			metrics.addCustomChart(new Metrics.SimplePie("using_claiming", () -> {
				String result = "No";
				if (Claim.action.isEnabled()) {
					result = "Yes";
				}
				return result;
			}));
			boolean configAllow = PRO.dataManager.getMain().getConfig().getBoolean("Clans.raid-shield.allow");
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
				for (EventCycle cycle : CycleList.getRegisteredCycles()) {
					if (cycle.persist()) {
						entry.put(cycle.getName(), 1);
						map.put(cycle.getName(), entry);
					}
				}
				return map;
			}));
			metrics.addCustomChart(new Metrics.SingleLineChart("total_logged_players", () -> DefaultClan.action.getAllUsers().size()));
			metrics.addCustomChart(new Metrics.SingleLineChart("total_clans_registered", () -> DefaultClan.action.getAllClanIDs().size()));
		});
		dataManager.runCleaner();

		FileManager config = DataManager.FileType.MISC_FILE.get("Messages", "Configuration");

		List<String> format = config.getConfig().getStringList("menu-format.clan");

		dataManager.CLAN_FORMAT.addAll(format);

		FileManager man = getFileList().find("Heads", "Configuration");

		if (!man.exists()) {
			FileManager.copy(getResource("Heads.yml"), man);
			man.reload();
		}

		CustomHead.Manager.newLoader(man.getConfig()).look("My_heads").complete();

	}

	public void onDisable() {
		try {

			for (EventCycle cycle : CycleList.getCycles()) {
				cycle.remove();
			}

			BankMeta.clearManagerCache();
			dataManager.staffID_MODE.clear();
			dataManager.ASSOCIATES.clear();
			dataManager.CHAT_MODE.clear();
			dataManager.CLAN_ALLY_MAP.clear();
			dataManager.CLAN_ENEMY_MAP.clear();
			dataManager.CLANS.clear();
			for (Player p : Bukkit.getOnlinePlayers()) {
				Clan c = getClan(p.getUniqueId());
				if (c instanceof DefaultClan) {
					try {
						if (((DefaultClan) c).getCurrentWar() != null) {
							getLogger().warning("- Force shut down with active war detected. Stopping gamemode.");
							((DefaultClan) c).getCurrentWar().conclude();
							break;
						}
					} catch (NullPointerException ignored) {
					}
				}
			}
		} catch (Exception ignored) {
		}
		if (System.getProperty("OLD").equals("FALSE")) {
			System.setProperty("OLD", "TRUE");
		}
	}

	public KeyedServiceManager<EventCycle> getServiceManager() {
		return this.serviceManager;
	}

	@Override
	public Clan getClan(UUID target) {
		Clan clan = null;
		for (Clan cache : ClansPro.getInstance().dataManager.CLANS) {
			if (Arrays.asList(cache.getMembersList()).contains(target.toString())) {
				clan = cache;
				break;
			}
		}
		return clan;
	}

	@Override
	public Clan getClan(String clanID) {
		return DefaultClan.action.getClan(clanID);
	}

	@Override
	public Optional<Clan> getClan(OfflinePlayer player) {
		return getClan(player.getUniqueId()) != null ? Optional.of(getClan(player.getUniqueId())) : Optional.of(getClan(getClanID(player.getUniqueId()).toString()));
	}

	@Override
	public Optional<ClanAssociate> getAssociate(OfflinePlayer player) {
		return dataManager.ASSOCIATES.stream().filter(a -> a.getPlayer().getUniqueId().equals(player.getUniqueId())).findFirst();
	}

	@Override
	public Optional<ClanAssociate> getAssociate(UUID uuid) {
		return dataManager.ASSOCIATES.stream().filter(a -> a.getPlayer().getUniqueId().equals(uuid)).findFirst();
	}

	@Override
	public Optional<ClanAssociate> getAssociate(String playerName) {
		return dataManager.ASSOCIATES.stream().filter(a -> a.getPlayer().getName().equals(playerName)).findFirst();
	}

	@Override
	public FileList getFileList() {
		return origin;
	}

	@Override
	public ClanManager getClanManager() {
		return clanManager;
	}

	@Override
	public ClaimManager getClaimManager() {
		return claimManager;
	}

	@Override
	public ShieldManager getShieldManager() {
		return shieldManager;
	}

	@Override
	public boolean isClanMember(UUID target, String clanID) {
		return Arrays.stream(getClan(clanID).getMembersList()).anyMatch(i -> i.equals(target.toString()));
	}

	@Override
	public boolean isInClan(UUID target) {
		return getAssociate(target).isPresent();
	}

	@Override
	public boolean isNameBlackListed(String name) {
		for (String s : ClansAPI.getData().getMain().getConfig().getConfigurationSection("Clans.name-blacklist").getKeys(false)) {
			if (StringUtils.use(name).containsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getClanName(String clanID) {
		return DefaultClan.action.getClanTag(clanID);
	}

	@Override
	public String getClanID(String clanName) {
		return DefaultClan.action.getClanID(clanName);
	}

	@Override
	public HUID getClanID(UUID uuid) {
		ClanAssociate associate = getAssociate(uuid).orElse(null);
		if (associate != null && associate.isValid()) {
			return associate.getClan().getId();
		}
		return null;
	}

	@Override
	public void setRank(ClanAssociate associate, RankPriority priority) {

		if (associate == null) return;

		if (associate.getClanID() == null) return;

		String rank = "";
		switch (priority) {
			case NORMAL:
				rank = "members";
				break;
			case HIGH:
				rank = "moderators";
				break;
			case HIGHER:
				rank = "admins";
				break;
		}
		FileManager clan = DataManager.FileType.CLAN_FILE.get(associate.getClanID().toString());
		if (clan.getConfig().getStringList("members").contains(associate.getPlayer().getUniqueId().toString())) {
			if (priority != RankPriority.HIGHEST) {
				String currentRank = DefaultClan.action.getPriorityKey(DefaultClan.action.getRankPower(associate.getPlayer().getUniqueId()));
				List<String> array = clan.getConfig().getStringList(rank);
				List<String> array2 = clan.getConfig().getStringList(currentRank);
				if (!currentRank.equals("members")) {
					array2.remove(associate.getPlayer().getUniqueId().toString());
				}
				array.add(associate.getPlayer().getUniqueId().toString());
				clan.getConfig().set(DefaultClan.action.getPriorityUpgradeKey(DefaultClan.action.getRankPower(associate.getPlayer().getUniqueId())), array);
				clan.getConfig().set(currentRank, array2);
				clan.saveConfig();
				Clan clanIndex = associate.getClan();
				String format = String.format(ClansAPI.getData().getMessage("promotion"), Bukkit.getOfflinePlayer(associate.getPlayer().getUniqueId()).getName(), DefaultClan.action.getRankTag(DefaultClan.action.getRank(associate.getPlayer().getUniqueId())));
				clanIndex.broadcast(format);
			}
		}
	}

	@Override
	public void searchNewAddons(Plugin plugin, String packageName) {
		RegistryData<EventCycle> data = new Registry<>(EventCycle.class)
				.source(plugin)
				.pick(packageName)
				.operate(cycle -> {
					cycle.onLoad();
					cycle.register();
					cycle.onEnable();
				});

		ClansPro.getInstance().getLogger().info("- Found (" + data.getData().size() + ") event cycle(s)");

		for (EventCycle e : data.getData()) {
			if (e.persist()) {

				ClansPro.getInstance().getLogger().info(" ");
				ClansPro.getInstance().getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				ClansPro.getInstance().getLogger().info("- Addon: " + e.getName());
				ClansPro.getInstance().getLogger().info("- Version: " + e.getVersion());
				ClansPro.getInstance().getLogger().info("- Author(s): " + Arrays.toString(e.getAuthors()));
				ClansPro.getInstance().getLogger().info("- Description: " + e.getDescription());
				ClansPro.getInstance().getLogger().info("- Persistent: (" + e.persist() + ")");
				ClansPro.getInstance().getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				ClansPro.getInstance().getLogger().info(" ");
				ClansPro.getInstance().getLogger().info("- Listeners: (" + e.getAdditions().size() + ")");
				for (Listener addition : e.getAdditions()) {
					boolean registered = HandlerList.getRegisteredListeners(PRO).stream().anyMatch(r -> r.getListener().equals(addition));
					if (!registered) {
						ClansPro.getInstance().getLogger().info("- [" + e.getName() + "] (+1) Class " + addition.getClass().getSimpleName() + " loaded");
						Bukkit.getPluginManager().registerEvents(addition, PRO);
					} else {
						ClansPro.getInstance().getLogger().info("- [" + e.getName() + "] (-1) Class " + addition.getClass().getSimpleName() + " already loaded. Skipping.");
					}
				}
			} else {
				ClansPro.getInstance().getLogger().info(" ");
				ClansPro.getInstance().getLogger().info("- Addon: " + e.getName());
				ClansPro.getInstance().getLogger().info("- Description: " + e.getDescription());
				ClansPro.getInstance().getLogger().info("- Persistent: (" + e.persist() + ")");
				e.remove();
				ClansPro.getInstance().getLogger().info(" ");
				ClansPro.getInstance().getLogger().info("- Listeners: (" + e.getAdditions().size() + ")");
				for (Listener addition : e.getAdditions()) {
					ClansPro.getInstance().getLogger().info("- [" + e.getName() + "] (+1) Cycle failed to load due to no persistence.");
				}
			}
		}

	}

	@Override
	public void importAddon(Class<? extends EventCycle> cycle) {
		try {
			EventCycle c = cycle.newInstance();
			c.onLoad();
			c.register();
			c.onEnable();
			if (c.persist()) {

				ClansPro.getInstance().getLogger().info(" ");
				ClansPro.getInstance().getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				ClansPro.getInstance().getLogger().info("- Addon: " + c.getName());
				ClansPro.getInstance().getLogger().info("- Description: " + c.getDescription());
				ClansPro.getInstance().getLogger().info("- Persistent: (" + c.persist() + ")");
				ClansPro.getInstance().getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				ClansPro.getInstance().getLogger().info(" ");
				ClansPro.getInstance().getLogger().info("- Listeners: (" + c.getAdditions().size() + ")");
				for (Listener addition : c.getAdditions()) {
					boolean registered = HandlerList.getRegisteredListeners(PRO).stream().anyMatch(r -> r.getListener().equals(addition));
					if (!registered) {
						ClansPro.getInstance().getLogger().info("- [" + c.getName() + "] (+1) Class " + addition.getClass().getSimpleName() + " loaded");
						Bukkit.getPluginManager().registerEvents(addition, PRO);
					} else {
						ClansPro.getInstance().getLogger().info("- [" + c.getName() + "] (-1) Class " + addition.getClass().getSimpleName() + " already loaded. Skipping.");
					}
				}
			} else {
				ClansPro.getInstance().getLogger().info(" ");
				ClansPro.getInstance().getLogger().info("- Addon: " + c.getName());
				ClansPro.getInstance().getLogger().info("- Description: " + c.getDescription());
				ClansPro.getInstance().getLogger().info("- Persistent: (" + c.persist() + ")");
				c.remove();
				ClansPro.getInstance().getLogger().info(" ");
				ClansPro.getInstance().getLogger().info("- Listeners: (" + c.getAdditions().size() + ")");
				for (Listener addition : c.getAdditions()) {
					ClansPro.getInstance().getLogger().info("- [" + c.getName() + "] (+1) Cycle failed to load due to no persistence.");
				}
			}
		} catch (InstantiationException | IllegalAccessException e) {
			ClansPro.getInstance().getLogger().severe("- Unable to cast EventCycle to the class " + cycle.getName() + ". This likely means you are not implementing the EventCycle interface for your event class properly.");
			e.printStackTrace();
		}
	}

	@Override
	public boolean kickUser(UUID uuid) {
		boolean success = false;
		if (isInClan(uuid) && !getClan(uuid).getOwner().equals(uuid)) {
			success = true;
			DefaultClan.action.removePlayer(uuid);
		}
		return success;
	}

	@Override
	public boolean obtainUser(UUID uuid, String clanName) {
		boolean success = false;
		if (!isInClan(uuid)) {
			if (getClanID(clanName) == null)
				return false;

			Clan toJoin = getClan(getClanID(clanName));
			success = true;
			if (toJoin.getPassword() != null) {
				DefaultClan.action.joinClan(uuid, clanName, toJoin.getPassword());
			} else {
				DefaultClan.action.joinClan(uuid, clanName, null);
			}
		}
		return success;
	}

	@Override
	public ClanCooldown getCooldownByAction(String action) {
		return ClanCooldown.getById(action);
	}

	@Override
	public EventCycle getEventCycleByAddon(String name) {
		return CycleList.getAddon(name);
	}

	public static ClansPro getInstance() {
		return PRO;
	}

	@Override
	public ClanPrefix getPrefix() {
		return this.prefix;
	}

	private void setInstance(ClansPro instance) {
		ClansPro.PRO = instance;
	}
}
