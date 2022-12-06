package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQueue;
import com.github.sanctum.clans.construct.ArenaManager;
import com.github.sanctum.clans.construct.ClaimManager;
import com.github.sanctum.clans.construct.ClanManager;
import com.github.sanctum.clans.construct.CommandManager;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.ShieldManager;
import com.github.sanctum.clans.construct.extra.MessagePrefix;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.data.container.KeyedServiceManager;
import com.github.sanctum.labyrinth.library.NamespacedKey;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.annotation.Experimental;
import com.github.sanctum.panther.annotation.Note;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherCollectors;
import com.github.sanctum.panther.paste.PasteManager;
import com.github.sanctum.panther.paste.operative.PasteResponse;
import com.github.sanctum.panther.paste.type.Hastebin;
import com.github.sanctum.panther.util.Check;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**<pre>
 *    ▄████▄   ██▓    ▄▄▄       ███▄    █   ██████   ██████╗ ██████╗  ██████╗ ██╗
 *   ▒██▀ ▀█  ▓██▒   ▒████▄     ██ ▀█   █ ▒██    ▒ ▓ ██╔══██╗██╔══██╗██╔═══██╗██║
 *   ▒▓█    ▄ ▒██░   ▒██  ▀█▄  ▓██  ▀█ ██▒░ ▓██▄   ▓ ██████╔╝██████╔╝██║   ██║██║
 *   ▒▓▓▄ ▄██▒▒██░   ░██▄▄▄▄██ ▓██▒  ▐▌██▒  ▒   ██▒▒ ██╔═══╝ ██╔══██╗██║   ██║╚═╝
 *   ▒ ▓███▀ ░░██████▒▓█   ▓██▒▒██░   ▓██░▒██████▒▒▒ ██║     ██║  ██║╚██████╔╝██╗
 *   ░ ░▒ ▒  ░░ ▒░▓  ░▒▒   ▓▒█░░ ▒░   ▒ ▒ ▒ ▒▓▒ ▒ ░▒ ╚═╝     ╚═╝  ╚═╝ ╚═════╝ ╚═╝
 *       ░  ▒   ░ ░ ▒  ░ ▒   ▒▒ ░░ ░░   ░ ▒░░ ░▒  ░ ░░
 *         ░          ░ ░    ░   ▒      ░   ░ ░ ░  ░  ░  ░
 *           ░ ░          ░  ░     ░  ░         ░       ░
 *             ░
 * <pre>
 * Copyright (c) 2022 Sanctum
 */
public interface ClansAPI {

	static ClansAPI getInstance() {
		return Bukkit.getServicesManager().load(ClansAPI.class);
	}

	static BanksAPI getBankInstance() {
		return BanksAPI.getInstance();
	}

	@Note("This is accessible but you should almost never need to use it directly")
	static DataManager getDataInstance() {
		return JavaPlugin.getPlugin(ClansJavaPlugin.class).dataManager;
	}

	/**
	 * Get this server's unique session id.
	 *
	 * @return the persistently unique id for this session.
	 * @apiNote This id is no longer unique after a single game session.
	 */
	@NotNull UUID getSessionId();

	/**
	 * @return Gets the prefix object for the plugin.
	 */
	@NotNull MessagePrefix getPrefix();

	/**
	 * The plugin instance for the api. Try not to use this!
	 *
	 * @return The primary plugin instance.
	 */
	@NotNull Plugin getPlugin();

	/**
	 * Gets a clan associate by their player object.
	 *
	 * @param player The player to use.
	 * @return A clan associate with properties such as nickname, bio etc.
	 */
	Optional<Clan.Associate> getAssociate(OfflinePlayer player);

	/**
	 * Gets a clan associate by their player idd.
	 *
	 * @param uuid The player to use.
	 * @return A clan associate with properties such as nickname, bio etc.
	 */
	Optional<Clan.Associate> getAssociate(UUID uuid);

	/**
	 * Gets a clan associate by their player name.
	 *
	 * @param playerName The player to use.
	 * @return A clan associate with properties such as nickname, bio etc.
	 */
	Optional<Clan.Associate> getAssociate(String playerName);

	/**
	 * Get the ClansPro file listing.
	 *
	 * @return Get's the file collection for the given plugin.
	 */
	@NotNull FileList getFileList();

	/**
	 * Gets the service manager for event cycles.
	 *
	 * @return The event cycle services manager.
	 */
	@NotNull KeyedServiceManager<ClanAddon> getServiceManager();

	/**
	 * Get the manager for clan war arenas.
	 *
	 * @return The arena manager.
	 */
	@NotNull ArenaManager getArenaManager();

	/**
	 * Get the manager for clans to load/delete from.
	 *
	 * @return The clan manager.
	 */
	@NotNull ClanManager getClanManager();

	/**
	 * Get the manager for clan claims.
	 *
	 * @return The claim manager.
	 */
	@NotNull ClaimManager getClaimManager();

	/**
	 * Get the manger for the raid-shield
	 *
	 * @return The raid shield manager.
	 */
	@NotNull ShieldManager getShieldManager();

	/**
	 * Get the manager for clan sub commands.
	 *
	 * @return The sub command manager.
	 */
	@NotNull CommandManager getCommandManager();

	/**
	 * Get the logo gallery. A public place for local users to upload their 8-bit art work.
	 *
	 * @return The public logo gallery
	 */
	@NotNull LogoGallery getLogoGallery();

	/**
	 * Get the object dedicated to managing information relays to/from web services.
	 *
	 * @return A paste manager.
	 */
	@NotNull PasteManager getPasteManager();

	/**
	 * Get the locally cached hastebin api instance.
	 *
	 * @return the local hastebin api instance.
	 */
	@NotNull Hastebin getHastebin();

	/**
	 * Check if pro needs to be updated.
	 *
	 * @return true if the plugin has an update.
	 */
	boolean isUpdated();

	/**
	 * Check if this version of the plugin is limited.
	 *
	 * @return true if this is a trial version of pro.
	 */
	boolean isTrial();

	/**
	 * Check if a specified clan name is black-listed
	 *
	 * @param name The clan name in question
	 * @return result = true if the clan name is not allowed.
	 */
	boolean isNameBlackListed(String name);

	/**
	 * Get the local fingerprint key for clans pro data reloading.
	 *
	 * @return the namespace (key) for the clans pro fingerprint.
	 */
	default NamespacedKey getLocalPrintKey() {
		return new NamespacedKey(getPlugin(), "reload_data");
	}

	/**
	 * Debug an invasive entity to check all parameters for stability.
	 * The option of creating a link will generate a <strong>hastebin</strong> link instead
	 * of sending all the information in console.
	 *
	 * @param createLink whether to create a link.
	 * @param entity     The entity to debug.
	 */
	default void debugConsole(InvasiveEntity entity, boolean createLink) {
		if (!createLink) {
			if (entity.isClan()) {
				Clan c = entity.getAsClan();
				getPlugin().getLogger().warning("|==============================================|");
				getPlugin().getLogger().warning("|==============================================|");
				getPlugin().getLogger().warning("|==============================================|");
				getPlugin().getLogger().warning(MessageFormat.format("|           Debug run for clan {0}              ", Check.forNull(c.getName(), "The clan's name is null!")));
				getPlugin().getLogger().warning("|==============================================|");
				getPlugin().getLogger().warning("- Members = [ size: " + c.getMembers().size() + ", visual: " + c.getMembers().stream().map(InvasiveEntity::getName).collect(Collectors.joining(", ")) + " ]");
				getPlugin().getLogger().warning(MessageFormat.format("- Id = [{0}]", c.getId()));
				getPlugin().getLogger().warning(MessageFormat.format("- Type = [{0}]", c.isConsole() ? "SERVER" : "PLAYER"));
				getPlugin().getLogger().warning(MessageFormat.format("- Mode = [{0}]", c.isPeaceful() ? "PEACE" : "WAR"));
				getPlugin().getLogger().warning(MessageFormat.format("- Password = [{0}]", c.getPassword() != null ? c.getPassword() : "N/A"));
				getPlugin().getLogger().warning(MessageFormat.format("- Power = [{0}]", c.getPower()));
				getPlugin().getLogger().warning(MessageFormat.format("- Claims = [{0}/{1}]", c.getClaims().length, c.getClaimLimit()));
				getPlugin().getLogger().warning("|==============================================|");
				getPlugin().getLogger().warning("|==============================================|");
				getPlugin().getLogger().warning("|==============================================|");
				getPlugin().getLogger().warning("|==============================================|");
			}
			if (entity.isAssociate()) {
				Clan.Associate a = entity.getAsAssociate();
				getPlugin().getLogger().warning("|==============================================|");
				getPlugin().getLogger().warning("|==============================================|");
				getPlugin().getLogger().warning("|==============================================|");
				getPlugin().getLogger().warning(MessageFormat.format("|        Debug run for associate {0}              ", a.getName()));
				getPlugin().getLogger().warning("|==============================================|");
				getPlugin().getLogger().warning(MessageFormat.format("- Id = [{0}]", a.getId().toString()));
				getPlugin().getLogger().warning(MessageFormat.format("- Clan = [{0}]", a.getClan().getName()));
				getPlugin().getLogger().warning(MessageFormat.format("- Bio = [{0}]", a.getBiography()));
				getPlugin().getLogger().warning(MessageFormat.format("- Chat = [{0}]", a.getChannel()));
				getPlugin().getLogger().warning(MessageFormat.format("- Joined = [{0}]", a.getJoinDate().toLocaleString()));
				getPlugin().getLogger().warning(MessageFormat.format("- Nickname = [{0}]", a.getNickname()));
				getPlugin().getLogger().warning(MessageFormat.format("- Rank = [{0}]", a.getPriority()));
				getPlugin().getLogger().warning(MessageFormat.format("- KD = [{0}]", a.getKD()));
				getPlugin().getLogger().warning("|==============================================|");
				getPlugin().getLogger().warning("|==============================================|");
				getPlugin().getLogger().warning("|==============================================|");
				getPlugin().getLogger().warning("|==============================================|");
			}
		} else {
			if (entity.isClan()) {
				TaskScheduler.of(() -> {
					Date now = new Date();
					Hastebin bin = getHastebin();
					PasteResponse response;
					boolean isConsole = entity.getAsClan().isConsole();
					Clan.Implementation implementation = entity.getAsClan().getImplementation();
					if (entity.isValid()) {
						boolean doubleCheck = true;
						for (InvasiveEntity e : entity.getAsClan()) {
							if (!e.isValid()) {
								doubleCheck = false;
								break;
							}
						}
						if (doubleCheck) {
							String json = entity.getAsClan().write(entity.getAsClan()).toString();
							response = bin.write("/**",
									" * Debugged: " + now.toLocaleString() + "",
									" * Object: " + json,
									" * Grade: PASS",
									" * Server: " + isConsole,
									" * Implementation: " + implementation,
									" * Comment: This clan object from members to self is fully valid!",
									" */");
						} else {
							response = bin.write("/**",
									" * Debugged: " + now.toLocaleString() + "",
									" * Grade: FAIL",
									" * Server: " + isConsole,
									" * Implementation: " + implementation,
									" * Comment: This clan object can't be used, one or more members are invalid.",
									" */");
						}
					} else {
						response = bin.write("/**",
								" * Debugged: " + now.toLocaleString() + "",
								" * Grade: FAIL",
								" * Server: " + isConsole,
								" * Implementation: " + implementation,
								" * Comment: This clan object can't be used.",
								" */");
					}
					String link = response.get();
					getPlugin().getLogger().warning(entity.getName() + " debug: " + link);
				}).scheduleAsync();
			}
		}
	}

	default Optional<Clan.Associate> getAssociate(LabyrinthUser user) {
		return getAssociate(user.getId());
	}

	default ClanAddonQueue getAddonQueue() {
		return ClanAddonQueue.getInstance();
	}

	/**
	 * Get the server consultant object if one has been provided.
	 *
	 * @return the server consultant object if provided or null.
	 * @apiNote The server consultant is also an {@link com.github.sanctum.clans.construct.api.Clan.Associate}!
	 */
	default @Nullable Consultant getConsultant() {
		return (Consultant) getAssociate(getSessionId()).orElse(null);
	}

	@Experimental(dueTo = "Involving usage of the brand new api! Use at your own risk.")
	default PantherCollection<? extends InvasiveEntity> getEntities() {
		PantherCollection<InvasiveEntity> list = InoperableSpecialMemory.ENTITY_MAP.values().stream().collect(PantherCollectors.toList());
		getClanManager().getClans().forEach(c -> {
			c.getMembers().forEach(list::add);
			list.add(c);
		});
		return list;
	}


}
