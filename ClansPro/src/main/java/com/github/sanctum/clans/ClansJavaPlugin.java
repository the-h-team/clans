package com.github.sanctum.clans;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.construct.ArenaManager;
import com.github.sanctum.clans.construct.ClaimManager;
import com.github.sanctum.clans.construct.ClanManager;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.RankPriority;
import com.github.sanctum.clans.construct.ShieldManager;
import com.github.sanctum.clans.construct.actions.ClansUpdate;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.bank.BankMeta;
import com.github.sanctum.clans.construct.extra.MessagePrefix;
import com.github.sanctum.clans.construct.extra.StartProcedure;
import com.github.sanctum.clans.construct.impl.DefaultArena;
import com.github.sanctum.labyrinth.data.Configurable;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.data.Node;
import com.github.sanctum.labyrinth.data.container.KeyedServiceManager;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.skulls.CustomHead;
import java.io.InputStream;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
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

	public FileType TYPE;
	private static ClansJavaPlugin PRO;
	private static FileList origin;

	private MessagePrefix prefix;
	private ArenaManager arenaManager;
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
		FileManager main = dataManager.getMain();
		TYPE = FileType.valueOf(main.read(c -> c.getNode("Formatting").getNode("file-type").toPrimitive().getString()));
		clanManager = new ClanManager();
		claimManager = new ClaimManager();
		shieldManager = new ShieldManager();
		serviceManager = new KeyedServiceManager<>();
		arenaManager = new ArenaManager();
		arenaManager.load(new DefaultArena("PRO"));
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
			FileManager file = origin.find("ignore", FileType.JSON);
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
			file.write(t -> t.set(location, toAdd));
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		} else {
			System.setProperty("RELOAD", "FALSE");
		}
		Node formatting = main.read(c -> c.getNode("Formatting"));
		Node prefix = formatting.getNode("prefix");
		this.prefix = new MessagePrefix(prefix.getNode("prefix").toPrimitive().getString(),
				prefix.getNode("text").toPrimitive().getString(),
				prefix.getNode("suffix").toPrimitive().getString());

		StartProcedure start = new Beginning(this).then((s, instance) -> {
			if (instance.dataManager.assertDefaults()) {
				instance.getLogger().info("- The configuration has been updated to the latest version.");
			}
		});
		start.run();

		FileManager config = dataManager.getMessages();
		dataManager.CLAN_GUI_FORMAT.addAll(config.read(c -> c.getStringList("menu-format.clan")));

		FileManager man = getFileList().get("heads", "Configuration", FileType.JSON);
		if (!man.getRoot().exists()) {
			InputStream stream = getResource("heads.data");
			FileList.copy(stream, man.getRoot().getParent());
			man.getRoot().reload();
		}
		Configurable.registerClass(Clan.class);
		ConfigurationSerialization.registerClass(Clan.class);
		CustomHead.Manager.newLoader(man.getRoot()).look("My_heads").complete();

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

	public void setPrefix(MessagePrefix prefix) {
		this.prefix = prefix;
	}

	@Override
	public KeyedServiceManager<ClanAddon> getServiceManager() {
		return this.serviceManager;
	}

	@Override
	public ArenaManager getArenaManager() {
		return this.arenaManager;
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
	public Optional<Clan.Associate> getAssociate(OfflinePlayer player) {
		return getClanManager().getClans().filter(c -> c.getMember(m -> Objects.equals(m.getPlayer().getName(), player.getName())) != null).map(c -> c.getMember(m -> Objects.equals(m.getPlayer().getName(), player.getName()))).findFirst();
	}

	@Override
	public Optional<Clan.Associate> getAssociate(UUID uuid) {
		return getClanManager().getClans().filter(c -> c.getMember(m -> Objects.equals(m.getPlayer().getUniqueId(), uuid)) != null).map(c -> c.getMember(m -> Objects.equals(m.getPlayer().getUniqueId(), uuid))).findFirst();
	}

	@Override
	public Optional<Clan.Associate> getAssociate(String playerName) {
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
			getPlugin().getLogger().info("- Couldn't connect to servers, unable to call for updates.");
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
		for (String s : ClansAPI.getData().getMain().read(c -> c.getNode("Clans.name-blacklist").get(ConfigurationSection.class)).getKeys(false)) {
			if (StringUtils.use(name).containsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getClanName(String clanID) {
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		return clan.read(c -> c.getNode("name").toPrimitive().getString());
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
		Clan.Associate associate = getAssociate(uuid).orElse(null);
		if (associate != null && associate.isValid()) {
			return associate.getClan().getId();
		}
		return null;
	}

	@Override
	public void setRank(Clan.Associate associate, RankPriority priority) {
		if (associate == null) return;

		if (associate.isValid()) return;

		associate.setPriority(priority);
		Clan clanIndex = associate.getClan();
		String format = MessageFormat.format(ClansAPI.getData().getMessageResponse("promotion"), Bukkit.getOfflinePlayer(associate.getPlayer().getUniqueId()).getName(), associate.getRankTag());
		clanIndex.broadcast(format);

	}

	@Override
	public void searchNewAddons(Plugin plugin, String packageName) {
		ClanAddonQuery.register(plugin, packageName);
	}

	@Override
	public void importAddon(Class<? extends ClanAddon> cycle) {
		ClanAddonQuery.register(cycle);
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
	public MessagePrefix getPrefix() {
		return this.prefix;
	}

	@Override
	public Plugin getPlugin() {
		return PRO;
	}

}
