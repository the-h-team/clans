package com.github.sanctum.clans.construct;


import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.impl.Resident;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.library.Items;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.skulls.CustomHead;
import com.github.sanctum.skulls.CustomHeadLoader;
import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class DataManager {

	public final List<String> WAR_BLOCKED_CMDS = new ArrayList<>();
	public final HashMap<Player, String> ID_MODE = new HashMap<>();
	public final List<String> CLAN_GUI_FORMAT = new LinkedList<>();
	public final List<Player> CHAT_SPY = new ArrayList<>();
	public final List<Resident> RESIDENTS = new ArrayList<>();
	public final List<Player> INHABITANTS = new ArrayList<>();
	public final LinkedList<ClanCooldown> COOLDOWNS = new LinkedList<>();

	public DataManager() {
		this.WAR_BLOCKED_CMDS.addAll(getMain().getRoot().getStringList("Clans.war.blocked-commands"));
	}

	public @NotNull FileManager getMain() {
		FileManager main = FileType.MISC_FILE.get("Config", "Configuration");
		if (!main.getRoot().exists()) {
			InputStream is = ClansAPI.getInstance().getPlugin().getResource("Config.yml");
			if (is == null) throw new IllegalStateException("Unable to load Config.yml from the jar!");
			FileManager.copy(is, main.getRoot().getParent());
			main.getRoot().reload();
		}
		return main;
	}

	public @NotNull FileManager getMessages() {
		FileManager main = FileType.MISC_FILE.get("Messages", "Configuration");
		if (!main.getRoot().exists()) {
			InputStream is = ClansAPI.getInstance().getPlugin().getResource("Messages.yml");
			if (is == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileManager.copy(is, main.getRoot().getParent());
			main.getRoot().reload();
		}
		return main;
	}

	public @NotNull FileManager getClanFile(Clan c) {
		return FileType.CLAN_FILE.get(c.getId().toString());
	}

	public String getConfigString(String path) {
		return getMain().getRoot().getString(path);
	}

	public boolean isTrue(String path) {
		return getMain().getRoot().getBoolean(path);
	}

	public String getMenuTitle(String menu) {
		FileManager main = FileType.MISC_FILE.get("Messages", "Configuration");
		if (!main.getRoot().exists()) {
			InputStream is = ClansAPI.getInstance().getPlugin().getResource("Messages.yml");
			if (is == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileManager.copy(is, main.getRoot().getParent());
		}
		return main.getRoot().getString("menu-titles." + menu);
	}

	public String getMenuCategory(String menu) {
		FileManager main = FileType.MISC_FILE.get("Messages", "Configuration");
		if (!main.getRoot().exists()) {
			InputStream is = ClansAPI.getInstance().getPlugin().getResource("Messages.yml");
			if (is == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileManager.copy(is, main.getRoot().getParent());
		}
		return main.getRoot().getString("menu-categories." + menu);
	}

	public String getMenuNavigation(String menu) {
		FileManager main = FileType.MISC_FILE.get("Messages", "Configuration");
		if (!main.getRoot().exists()) {
			InputStream is = ClansAPI.getInstance().getPlugin().getResource("Messages.yml");
			if (is == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileManager.copy(is, main.getRoot().getParent());
		}
		return main.getRoot().getString("gui-navigation." + menu);
	}

	private ItemStack improvise(String value) {
		Material mat = Material.getMaterial(value);
		if (mat != null) {
			return new ItemStack(mat);
		} else {
			if (value.length() < 16) {
				return CustomHead.Manager.getHeads().stream().filter(h -> StringUtils.use(h.name()).containsIgnoreCase(value)).map(CustomHead::get).findFirst().orElse(null);
			} else {
				return CustomHeadLoader.provide(value);
			}
		}
	}

	public ItemStack getItem(String object) {
		FileManager main = FileType.MISC_FILE.get("Messages", "Configuration");
		if (!main.getRoot().exists()) {
			InputStream is = ClansAPI.getInstance().getPlugin().getResource("Messages.yml");
			if (is == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileManager.copy(is, main.getRoot().getParent());
		}
		return improvise(Objects.requireNonNull(main.getRoot().getString("menu-items." + object)));
	}

	public Material getMaterial(String object) {
		FileManager main = FileType.MISC_FILE.get("Messages", "Configuration");
		if (!main.getRoot().exists()) {
			InputStream is = ClansAPI.getInstance().getPlugin().getResource("Messages.yml");
			if (is == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileManager.copy(is, main.getRoot().getParent());
		}
		return Items.getMaterial(Objects.requireNonNull(main.getRoot().getString("menu-items." + object)));
	}

	public String getPath(String path) {
		FileManager main = FileType.MISC_FILE.get("Messages", "Configuration");
		if (!main.getRoot().exists()) {
			InputStream is = ClansAPI.getInstance().getPlugin().getResource("Messages.yml");
			if (is == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileManager.copy(is, main.getRoot().getParent());
		}
		return main.getRoot().getString(path);
	}

	public int getInt(String path) {
		return getMain().getRoot().getInt(path);
	}

	public String getMessageResponse(String path) {
		FileManager main = FileType.MISC_FILE.get("Messages", "Configuration");
		if (!main.getRoot().exists()) {
			InputStream is = ClansAPI.getInstance().getPlugin().getResource("Messages.yml");
			if (is == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileManager.copy(is, main.getRoot().getParent());
		}
		return main.getRoot().getString("Response." + path);
	}

	public static boolean titlesAllowed() {
		FileManager main = ClansAPI.getInstance().getFileList().get("Config", "Configuration");
		return main.getRoot().getBoolean("Clans.land-claiming.send-titles");
	}

	public String prefixedTag(String color, String name) {
		return MessageFormat.format(Objects.requireNonNull(getMain().getConfig().getString("Formatting.nametag-prefix.text")), color, name);
	}

	public boolean prefixedTagsAllowed() {
		FileManager main = getMain();
		return main.getRoot().getBoolean("Formatting.nametag-prefix.use");
	}

	public boolean assertDefaults() {
		ClansAPI api = ClansAPI.getInstance();
		FileList list = api.getFileList();
		FileManager main = list.get("Config", "Configuration");
		FileManager messages = list.get("Messages", "Configuration");
		if (!ClansAPI.getInstance().getPlugin().getDescription().getVersion().equals(getMain().getRoot().getString("Version"))) {
			FileManager mainOld = list.get("config_old", "Configuration", com.github.sanctum.labyrinth.data.FileType.JSON);
			FileManager messOld = list.get("messages_old", "Configuration", com.github.sanctum.labyrinth.data.FileType.JSON);
			if (mainOld.getRoot().exists()) {
				mainOld.getRoot().delete();
			}
			if (messOld.getRoot().exists()) {
				messOld.getRoot().delete();
			}
			final String type = main.read(c -> c.getNode("Formatting").getNode("file-type").toPrimitive().getString());
			main.toJSON("config_old", "Configuration");
			messages.toJSON("messages_old", "Configuration");

			Schedule.sync(() -> {
				InputStream mainGrab;
				if (type != null) {
					if (type.equals("JSON")) {
						mainGrab = api.getPlugin().getResource("Config_json.yml");
					} else {
						mainGrab = api.getPlugin().getResource("Config.yml");
					}
				} else {
					mainGrab = api.getPlugin().getResource("Config.yml");
				}
				InputStream msgGrab;
				msgGrab = api.getPlugin().getResource("Messages.yml");
				if (mainGrab == null) throw new IllegalStateException("Unable to load Config.yml from the jar!");
				if (msgGrab == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
				FileList.copy(mainGrab, main.getRoot().getParent());
				FileList.copy(msgGrab, messages.getRoot().getParent());
				messages.getRoot().reload();
				main.getRoot().reload();
			}).wait(1);
			return true;
		}
		return false;
	}

	public void copyDefaults() {
		FileManager main = ClansAPI.getInstance().getFileList().get("Config", "Configuration");
		FileManager msg = ClansAPI.getInstance().getFileList().get("Messages", "Configuration");
		if (!main.getRoot().exists()) {
			InputStream mainGrab = ClansAPI.getInstance().getPlugin().getResource("Config.yml");
			if (mainGrab == null) throw new IllegalStateException("Unable to load Config.yml from the jar!");
			FileList.copy(mainGrab, main.getRoot().getParent());
		}
		if (!msg.getRoot().exists()) {
			InputStream mainGrab = ClansAPI.getInstance().getPlugin().getResource("Messages.yml");
			if (mainGrab == null) throw new IllegalStateException("Unable to load Messages.yml from the jar!");
			FileList.copy(mainGrab, msg.getRoot().getParent());
		}
	}

	public File getClanFolder() {
		final File dir = new File(FileManager.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " "));
		File d = new File(dir.getParentFile().getPath(), ClansAPI.getInstance().getPlugin().getDescription().getName() + "/" + "Clans" + "/");
		if (!d.exists()) {
			d.mkdirs();
		}
		return d;
	}

	public static class Security {
		public static String getPermission(String command) {
			return ClansAPI.getData().getPath("Commands." + command + ".permission");
		}
	}

	public enum FileType {
		CLAN_FILE, MISC_FILE;

		/**
		 * Get the file manager for a User file or for a Clan file.
		 *
		 * @param id user/clan id
		 * @return FileManager for user or clan
		 * @throws IllegalArgumentException if called on MISC_FILE
		 */
		public FileManager get(String id) throws IllegalArgumentException {
			if (this == FileType.CLAN_FILE) {
				return ClansAPI.getInstance().getFileList().get(id, "Clans", ((ClansJavaPlugin) ClansAPI.getInstance().getPlugin()).TYPE);
			}
			throw new IllegalArgumentException("This method should only be invoked on CLAN_FILE");
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
				return ClansAPI.getInstance().getFileList().get(name, directory);
			}
			throw new IllegalArgumentException("This method should only be invoked on MISC_FILE");
		}

	}
}
