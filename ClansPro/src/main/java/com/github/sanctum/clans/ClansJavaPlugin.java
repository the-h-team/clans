package com.github.sanctum.clans;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.ClaimManager;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.ClanManager;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.RankPriority;
import com.github.sanctum.clans.construct.ShieldManager;
import com.github.sanctum.clans.construct.actions.ClansUpdate;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.bank.BankMeta;
import com.github.sanctum.clans.construct.extra.ClanPrefix;
import com.github.sanctum.clans.construct.extra.Metrics;
import com.github.sanctum.clans.construct.extra.StartProcedure;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.data.RegistryData;
import com.github.sanctum.labyrinth.data.container.KeyedServiceManager;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.skulls.CustomHead;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
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
public class ClansJavaPlugin extends JavaPlugin implements ClansAPI {

	private static ClansJavaPlugin PRO;
	private static FileList origin;

	private ClanPrefix prefix;
	private ClaimManager claimManager;
	private ShieldManager shieldManager;
	private ClanManager clanManager;
	public DataManager dataManager;
	private KeyedServiceManager<ClanAddon> serviceManager;

	public String USER_ID = "%%__USER__%%";
	public String NONCE = "%%__NONCE__%%";

	public void onEnable() {
		PRO = this;
		origin = FileList.search(this);
		Bukkit.getServicesManager().register(ClansAPI.class, this, this, ServicePriority.Normal);
		dataManager = new DataManager();
		clanManager = new ClanManager();
		claimManager = new ClaimManager();
		shieldManager = new ShieldManager();
		serviceManager = new KeyedServiceManager<>();
		if (System.getProperty("RELOAD") != null && System.getProperty("RELOAD").equals("TRUE")) {
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
			System.setProperty("RELOAD", "FALSE");
		}

		dataManager.assertDefaults();

		FileManager main = ClansAPI.getData().getMain();
		this.prefix = new ClanPrefix(main.getConfig().getString("Formatting.prefix.prefix"),
				main.getConfig().getString("Formatting.prefix.text"),
				main.getConfig().getString("Formatting.prefix.suffix"));

		StartProcedure primary;
		try {
			primary = new StartProcedure(this, DataManager.hidden());
		} catch (IllegalAccessException e) {
			// switch to re-throw over assertion
			throw new IllegalStateException("Unable to properly initialize the plugin!", e);
		}
		primary.printLogo();
		primary.runRegistry();
		primary.sendBorder();
		primary.runQuickDataCleaning();
		primary.sendBorder();
		primary.runCacheRefresh();
		primary.sendBorder();
		primary.runCoreTask();
		primary.runPlaceholderCheck();
		primary.runUpdateCheck();
		primary.sendBorder();
		primary.runShieldTimer();
		primary.runInternalAddonServices();
		primary.sendBorder();
		primary.runBankSetup();
		primary.sendBorder();
		primary.runMetric(10461, metrics -> {
			metrics.addCustomChart(new Metrics.SimplePie("using_claiming", () -> {
				String result = "No";
				if (Claim.ACTION.isEnabled()) {
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

		FileManager config = DataManager.FileType.MISC_FILE.get("Messages", "Configuration");

		List<String> format = config.getConfig().getStringList("menu-format.clan");

		dataManager.CLAN_GUI_FORMAT.addAll(format);

		FileManager man = getFileList().find("Heads", "Configuration");

		if (!man.exists()) {
			FileManager.copy(getResource("Heads.yml"), man);
			man.reload();
		}

		CustomHead.Manager.newLoader(man.getConfig()).look("My_heads").complete();

	}

	public void onDisable() {
		try {

			for (ClanAddon cycle : ClanAddonQuery.getRegisteredAddons()) {
				cycle.remove();
			}

			BankMeta.clearManagerCache();
			dataManager.ID_MODE.clear();

		} catch (Exception ignored) {
		}
		getClanManager().getClans().list().forEach(Clan::save);
		if (System.getProperty("RELOAD").equals("FALSE")) {
			System.setProperty("RELOAD", "TRUE");
		}
	}

	public void setPrefix(ClanPrefix prefix) {
		this.prefix = prefix;
	}

	@Override
	public KeyedServiceManager<ClanAddon> getServiceManager() {
		return this.serviceManager;
	}

	@Override
	public Clan getClan(UUID target) {
		for (Clan c : getClanManager().getClans().list()) {
			if (c.getMember(m -> m.getPlayer().getUniqueId().equals(target)) != null) {
				return c;
			}
		}
		return null;
	}

	@Override
	public Clan getClan(String clanID) {
		Clan clan = null;
		if (HUID.fromString(clanID) == null) {
			clanID = getClanID(clanID);
		}
		for (Clan c : getClanManager().getClans().list()) {
			if (c.getId().toString().equals(clanID)) {
				clan = c;
			}
		}
		return clan;
	}

	@Override
	public Optional<Clan> getClan(OfflinePlayer player) {
		return getClan(player.getUniqueId()) != null ? Optional.of(getClan(player.getUniqueId())) : Optional.of(getClan(getClanID(player.getUniqueId()).toString()));
	}

	@Override
	public Optional<ClanAssociate> getAssociate(OfflinePlayer player) {
		return getClanManager().getClans().filter(c -> c.getMember(m -> Objects.equals(m.getPlayer().getName(), player.getName())) != null).map(c -> c.getMember(m -> Objects.equals(m.getPlayer().getName(), player.getName()))).findFirst();
	}

	@Override
	public Optional<ClanAssociate> getAssociate(UUID uuid) {
		return getClanManager().getClans().filter(c -> c.getMember(m -> Objects.equals(m.getPlayer().getUniqueId(), uuid)) != null).map(c -> c.getMember(m -> Objects.equals(m.getPlayer().getUniqueId(), uuid))).findFirst();
	}

	@Override
	public Optional<ClanAssociate> getAssociate(String playerName) {
		return getClanManager().getClans().filter(c -> c.getMember(m -> Objects.equals(m.getPlayer().getName(), playerName)) != null).map(c -> c.getMember(m -> Objects.equals(m.getPlayer().getName(), playerName))).findFirst();
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
	public boolean isUpdated() {
		ClansUpdate update = new ClansUpdate(getPlugin());
		try {
			if (update.hasUpdate()) {
				getPlugin().getLogger().warning("- An update is available! " + update.getLatest() + " download: [" + update.getResource() + "]");
				return false;
			} else {
				getPlugin().getLogger().info("- All up to date! Latest:(" + update.getLatest() + ") Current:(" + getDescription().getVersion() + ")");
				return true;
			}
		} catch (Exception e) {
			getPlugin().getLogger().info("- Couldn't connect to servers, unable to check for updates.");
		}
		return false;
	}

	@Override
	public boolean isClanMember(UUID target, String clanID) {
		return Arrays.stream(getClan(clanID).getMemberIds()).anyMatch(i -> i.equals(target.toString()));
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
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		return clan.getConfig().getString("name");
	}

	@Override
	public String getClanID(String clanName) {
		for (Clan c : getClanManager().getClans().list()) {
			if (c.getName().equals(clanName)) {
				return c.getId().toString();
			}
		}
		return null;
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

		if (associate.isValid()) return;

		associate.setPriority(priority);
		Clan clanIndex = associate.getClan();
		String format = MessageFormat.format(ClansAPI.getData().getMessage("promotion"), Bukkit.getOfflinePlayer(associate.getPlayer().getUniqueId()).getName(), associate.getRankTag());
		clanIndex.broadcast(format);

	}

	@Override
	public void searchNewAddons(Plugin plugin, String packageName) {
		RegistryData<ClanAddon> data = new Registry<>(ClanAddon.class)
				.source(plugin)
				.pick(packageName)
				.operate(cycle -> {
					cycle.onLoad();
					ClanAddonQuery.getRegisteredAddons().add(cycle);
					cycle.onEnable();
				});

		getLogger().info("- Found (" + data.getData().size() + ") clan addon(s)");

		for (ClanAddon e : data.getData()) {
			if (e.persist()) {

				getLogger().info(" ");
				getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				getLogger().info("- Addon: " + e.getName());
				getLogger().info("- Version: " + e.getVersion());
				getLogger().info("- Author(s): " + Arrays.toString(e.getAuthors()));
				getLogger().info("- Description: " + e.getDescription());
				getLogger().info("- Persistent: (" + e.persist() + ")");
				getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				getLogger().info(" ");
				getLogger().info("- Listeners: (" + e.getAdditions().size() + ")");
				for (Listener addition : e.getAdditions()) {
					boolean registered = HandlerList.getRegisteredListeners(PRO).stream().anyMatch(r -> r.getListener().equals(addition));
					if (!registered) {
						getLogger().info("- [" + e.getName() + "] (+1) Class " + addition.getClass().getSimpleName() + " loaded");
						Bukkit.getPluginManager().registerEvents(addition, PRO);
					} else {
						getLogger().info("- [" + e.getName() + "] (-1) Class " + addition.getClass().getSimpleName() + " already loaded. Skipping.");
					}
				}
			} else {
				getLogger().info(" ");
				getLogger().info("- Addon: " + e.getName());
				getLogger().info("- Description: " + e.getDescription());
				getLogger().info("- Persistent: (" + e.persist() + ")");
				e.remove();
				getLogger().info(" ");
				getLogger().info("- Listeners: (" + e.getAdditions().size() + ")");
				for (Listener addition : e.getAdditions()) {
					getLogger().info("- [" + e.getName() + "] (+1) Addon failed to load due to no persistence.");
				}
			}
		}

	}

	@Override
	public void importAddon(Class<? extends ClanAddon> cycle) {
		try {
			ClanAddon c = cycle.newInstance();
			c.onLoad();
			ClanAddonQuery.getRegisteredAddons().add(c);
			c.onEnable();
			if (c.persist()) {

				getLogger().info(" ");
				getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				getLogger().info("- Addon: " + c.getName());
				getLogger().info("- Description: " + c.getDescription());
				getLogger().info("- Persistent: (" + c.persist() + ")");
				getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				getLogger().info(" ");
				getLogger().info("- Listeners: (" + c.getAdditions().size() + ")");
				for (Listener addition : c.getAdditions()) {
					boolean registered = HandlerList.getRegisteredListeners(PRO).stream().anyMatch(r -> r.getListener().equals(addition));
					if (!registered) {
						getLogger().info("- [" + c.getName() + "] (+1) Class " + addition.getClass().getSimpleName() + " loaded");
						Bukkit.getPluginManager().registerEvents(addition, PRO);
					} else {
						getLogger().info("- [" + c.getName() + "] (-1) Class " + addition.getClass().getSimpleName() + " already loaded. Skipping.");
					}
				}
			} else {
				getLogger().info(" ");
				getLogger().info("- Addon: " + c.getName());
				getLogger().info("- Description: " + c.getDescription());
				getLogger().info("- Persistent: (" + c.persist() + ")");
				c.remove();
				getLogger().info(" ");
				getLogger().info("- Listeners: (" + c.getAdditions().size() + ")");
				for (Listener addition : c.getAdditions()) {
					getLogger().info("- [" + c.getName() + "] (+1) Addon failed to load due to no persistence.");
				}
			}
		} catch (InstantiationException | IllegalAccessException e) {
			getLogger().severe("- Unable to cast Addon to the class " + cycle.getName() + ". This likely means you are not implementing the Addon interface for your event class properly.");
			e.printStackTrace();
		}
	}

	@Override
	public boolean kickUser(UUID uuid) {
		boolean success = false;
		if (isInClan(uuid) && !getClan(uuid).getOwner().getPlayer().getUniqueId().equals(uuid)) {
			success = true;
			Clan.ACTION.removePlayer(uuid);
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
				Clan.ACTION.joinClan(uuid, clanName, toJoin.getPassword());
			} else {
				Clan.ACTION.joinClan(uuid, clanName, null);
			}
		}
		return success;
	}

	@Override
	public ClanCooldown getCooldownByAction(String action) {
		return ClanCooldown.getById(action);
	}

	@Override
	public ClanAddon getEventCycleByAddon(String name) {
		return ClanAddonQuery.getAddon(name);
	}

	@Override
	public ClanPrefix getPrefix() {
		return this.prefix;
	}

	@Override
	public Plugin getPlugin() {
		return PRO;
	}

}
