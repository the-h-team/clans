package com.github.sanctum.clans.util.data;


import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.actions.ClanCooldown;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.Resident;
import com.github.sanctum.clans.construct.extra.misc.ShieldTamper;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.library.Items;
import com.github.sanctum.labyrinth.task.Schedule;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class DataManager {

	public HashMap<Player, String> staffID_MODE = new HashMap<>();

	public HashMap<Player, String> CHAT_MODE = new HashMap<>();

	public List<String> CLAN_FORMAT = new LinkedList<>();

	public HashMap<String, List<String>> CLAN_ENEMY_MAP = new HashMap<>();

	public HashMap<String, List<String>> CLAN_ALLY_MAP = new HashMap<>();

	public LinkedList<Clan> CLANS = new LinkedList<>();

	public LinkedList<ClanAssociate> ASSOCIATES = new LinkedList<>();

	public List<Player> CLAN_SPY = new ArrayList<>();

	public List<Player> ALLY_SPY = new ArrayList<>();

	public List<Player> CUSTOM_SPY = new ArrayList<>();

	public List<Resident> RESIDENTS = new ArrayList<>();

	public List<Player> INHABITANTS = new ArrayList<>();

	public final ShieldTamper SHIELD_TAMPER = new ShieldTamper();

	public final LinkedList<ClanCooldown> COOLDOWNS = new LinkedList<>();

	public FileManager arenaFile() {
		return FileType.MISC_FILE.get("Settings", "Settings/Arena");
	}

	public FileManager arenaRedTeamFile() {
		return FileType.MISC_FILE.get("Red", "Settings/Teams");
	}

	public FileManager arenaBlueTeamFile() {
		return FileType.MISC_FILE.get("Blue", "Settings/Teams");
	}

	public @NotNull FileManager get(Player p) {
		return get(p.getUniqueId());
	}

	public @NotNull FileManager get(UUID id) {
		FileManager file = FileType.USER_FILE.get(id.toString());
		if (!file.exists()) {
			try {
				file.create();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	public @NotNull FileManager getMain() {
		FileManager main = FileType.MISC_FILE.get("Config", "Configuration");
		if (!main.exists()) {
			InputStream is = ClansPro.getInstance().getResource("Config.yml");
			if (is == null) throw new IllegalStateException("Unable to load Config.yml from the jar!");
			FileManager.copy(is, main.getFile());
			main.reload();
		}
		return main;
	}

	public @NotNull FileManager getClanFile(Clan c) {
		return FileType.CLAN_FILE.get(c.getId().toString());
	}

	public String getString(String path) {
		return getMain().getConfig().getString(path);
	}

	public boolean getEnabled(String path) {
		return getMain().getConfig().getBoolean(path);
	}

	public String getTitle(String object) {
		FileManager main = FileType.MISC_FILE.get("Messages", "Configuration");
		if (!main.exists()) {
			InputStream is = ClansPro.getInstance().getResource("Messages.yml");
			if (is == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileManager.copy(is, main.getFile());
		}
		return main.getConfig().getString("menu-titles." + object);
	}

	public String getCategory(String object) {
		FileManager main = FileType.MISC_FILE.get("Messages", "Configuration");
		if (!main.exists()) {
			InputStream is = ClansPro.getInstance().getResource("Messages.yml");
			if (is == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileManager.copy(is, main.getFile());
		}
		return main.getConfig().getString("menu-categories." + object);
	}

	public String getNavigate(String object) {
		FileManager main = FileType.MISC_FILE.get("Messages", "Configuration");
		if (!main.exists()) {
			InputStream is = ClansPro.getInstance().getResource("Messages.yml");
			if (is == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileManager.copy(is, main.getFile());
		}
		return main.getConfig().getString("gui-navigation." + object);
	}

	public ItemStack getItem(String object) {
		FileManager main = FileType.MISC_FILE.get("Messages", "Configuration");
		if (!main.exists()) {
			InputStream is = ClansPro.getInstance().getResource("Messages.yml");
			if (is == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileManager.copy(is, main.getFile());
		}
		return Items.improvise(Objects.requireNonNull(main.getConfig().getString("menu-items." + object)));
	}

	public Material getMaterial(String object) {
		FileManager main = FileType.MISC_FILE.get("Messages", "Configuration");
		if (!main.exists()) {
			InputStream is = ClansPro.getInstance().getResource("Messages.yml");
			if (is == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileManager.copy(is, main.getFile());
		}
		return Items.getMaterial(Objects.requireNonNull(main.getConfig().getString("menu-items." + object)));
	}

	public String getPath(String path) {
		FileManager main = FileType.MISC_FILE.get("Messages", "Configuration");
		if (!main.exists()) {
			InputStream is = ClansPro.getInstance().getResource("Messages.yml");
			if (is == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileManager.copy(is, main.getFile());
		}
		return main.getConfig().getString(path);
	}

	public int getInt(String path) {
		return getMain().getConfig().getInt(path);
	}

	public String getMessage(String path) {
		FileManager main = FileType.MISC_FILE.get("Messages", "Configuration");
		if (!main.exists()) {
			InputStream is = ClansPro.getInstance().getResource("Messages.yml");
			if (is == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileManager.copy(is, main.getFile());
		}
		return main.getConfig().getString("Response." + path);
	}

	public static boolean titlesAllowed() {
		FileManager main = ClansAPI.getInstance().getFileList().find("Config", "Configuration");
		return main.getConfig().getBoolean("Clans.land-claiming.send-titles");
	}

	public String prefixedTag(String color, String name) {
		return MessageFormat.format(Objects.requireNonNull(getMain().getConfig().getString("Formatting.nametag-prefix.text")), color, name);
	}

	public boolean prefixedTagsAllowed() {
		FileManager main = getMain();
		return main.getConfig().getBoolean("Formatting.nametag-prefix.use");
	}

	public boolean assertDefaults() {
		FileManager messages = ClansAPI.getInstance().getFileList().find("Messages", "Configuration");
		if (!ClansPro.getInstance().getDescription().getVersion().equals(getMain().getConfig().getString("Version"))) {
			FileManager mainOld = ClansAPI.getInstance().getFileList().find("Config_old", "Configuration");
			FileManager messOld = ClansAPI.getInstance().getFileList().find("messages_old", "Configuration");
			if (mainOld.exists()) {
				mainOld.delete();
			}
			if (messOld.exists()) {
				messOld.delete();
			}
			Schedule.sync(() -> {
				mainOld.getConfig().setDefaults(getMain().getConfig());
				mainOld.getConfig().options().copyDefaults(true);
				messOld.getConfig().setDefaults(messages.getConfig());
				messOld.getConfig().options().copyDefaults(true);
				mainOld.saveConfig();
				messOld.saveConfig();
				if (getMain().exists()) {
					getMain().delete();
				}
				if (messages.exists()) {
					messages.delete();
				}
				InputStream mainGrab = ClansPro.getInstance().getResource("Config.yml");
				InputStream msgGrab = ClansPro.getInstance().getResource("Messages.yml");
				if (mainGrab == null) throw new IllegalStateException("Unable to load Config.yml from the jar!");
				if (msgGrab == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
				FileManager.copy(mainGrab, getMain());
				FileManager.copy(msgGrab, messages);
				messages.reload();
				getMain().reload();
			}).wait(1);
			return true;
		}
		return false;
	}

	public void copyDefaults() {
		FileManager main = ClansAPI.getInstance().getFileList().find("Config", "Configuration");
		FileManager msg = ClansAPI.getInstance().getFileList().find("Messages", "Configuration");
		if (!main.exists()) {
			InputStream mainGrab = ClansPro.getInstance().getResource("Config.yml");
			if (mainGrab == null) throw new IllegalStateException("Unable to load Config.yml from the jar!");
			FileManager.copy(mainGrab, main.getFile());
		}
		if (!msg.exists()) {
			InputStream mainGrab = ClansPro.getInstance().getResource("Messages.yml");
			if (mainGrab == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileManager.copy(mainGrab, msg.getFile());
		}
	}

	public void runCleaner() {
		Schedule.async(() -> {
			Schedule.sync(() -> {
				for (final File f : getClanFolder().listFiles()) {
					FileManager c = ClansAPI.getInstance().getFileList().find(f.getName().replace(".yml", ""), "Clans");
					c.reload();
					if (c.getConfig().getString("name") == null) {
						ClansPro.getInstance().getLogger().warning("- Removed empty clan data file " + f.getName().replace(".yml", ""));
						c.delete();
					} else {
						List<String> list = new ArrayList<>();
						for (String id : c.getConfig().getStringList("members")) {
							if (!list.contains(id)) {
								if (get(UUID.fromString(id)).getConfig().getString("Clan.id") != null) {
									list.add(id);
								}
							}
						}
						c.getConfig().set("members", list);
						c.saveConfig();
						c.reload();
					}
				}
			}).debug().wait(1);
		}).repeat(2, getInt("Clans.data-cleaner"));
	}

	public File getClanFolder() {
		final File dir = new File(FileManager.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " "));
		File d = new File(dir.getParentFile().getPath(), ClansPro.getInstance().getDescription().getName() + "/" + "Clans" + "/");
		if (!d.exists()) {
			d.mkdirs();
		}
		return d;
	}

	public File getUserFolder() {
		final File dir = new File(FileManager.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " "));
		File d = new File(dir.getParentFile().getPath(), ClansPro.getInstance().getDescription().getName() + "/" + "Users" + "/");
		if (!d.exists()) {
			d.mkdirs();
		}
		return d;
	}

	public static int hidden() {
		return 420;
	}

	public static class Security {
		public static String getPermission(String command) {
			return ClansAPI.getData().getPath("Commands." + command + ".permission");
		}
	}

	public enum FileType {
		USER_FILE, CLAN_FILE, MISC_FILE;

		/**
		 * Get the file manager for a User file or for a Clan file.
		 *
		 * @param id user/clan id
		 * @return FileManager for user or clan
		 * @throws IllegalArgumentException if called on MISC_FILE
		 */
		public FileManager get(String id) throws IllegalArgumentException {
			switch (this) {
				case USER_FILE:
					return ClansAPI.getInstance().getFileList().find(id, "Users");
				case CLAN_FILE:
					return ClansAPI.getInstance().getFileList().find(id, "Clans");
			}
			throw new IllegalArgumentException("This method should only be invoked on USER_FILE or CLAN_FILE");
		}

		/**
		 * Get the file manager for a miscellaneous file.
		 *
		 * @param name      filename
		 * @param directory directory of file
		 * @return FileManager for miscellaneous file
		 * @throws IllegalArgumentException if not called on MISC_FILE
		 */
		public FileManager get(String name, String directory) throws IllegalArgumentException {
			if (this == MISC_FILE) {
				return ClansAPI.getInstance().getFileList().find(name, directory);
			}
			throw new IllegalArgumentException("This method should only be invoked on MISC_FILE");
		}

	}
}
