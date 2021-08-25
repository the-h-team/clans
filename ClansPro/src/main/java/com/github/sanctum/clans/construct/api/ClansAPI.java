package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.construct.ClaimManager;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.ClanManager;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.RankPriority;
import com.github.sanctum.clans.construct.ShieldManager;
import com.github.sanctum.clans.construct.extra.ClanPrefix;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.container.KeyedServiceManager;
import com.github.sanctum.labyrinth.library.HUID;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public interface ClansAPI {

	static ClansAPI getInstance() {
		return Bukkit.getServicesManager().load(ClansAPI.class);
	}

	static DataManager getData() {
		return ((ClansJavaPlugin) JavaPlugin.getProvidingPlugin(ClansJavaPlugin.class)).dataManager;
	}

	/**
	 * @return Gets the prefix object for the plugin.
	 */
	ClanPrefix getPrefix();

	/**
	 * The plugin instance for the api. Try not to use this!
	 *
	 * @return The primary plugin instance.
	 */
	Plugin getPlugin();

	/**
	 * @param target The target to look for
	 * @return Gets a clan object for the given target
	 */
	Clan getClan(UUID target);

	/**
	 * Gets a clan object from a clan id
	 *
	 * @param clanID The clan id to convert
	 * @return A clan object containing all values for the clan
	 */
	Clan getClan(String clanID);

	/**
	 * Gets a clan object from an offline-player if they're in one.
	 *
	 * @param player The player to search for.
	 * @return An optional Clan.
	 */
	Optional<Clan> getClan(OfflinePlayer player);

	/**
	 * Gets a clan associate by their player object.
	 *
	 * @param player The player to use.
	 * @return A clan associate with properties such as nickname, bio etc.
	 */
	Optional<ClanAssociate> getAssociate(OfflinePlayer player);

	/**
	 * Gets a clan associate by their player idd.
	 *
	 * @param uuid The player to use.
	 * @return A clan associate with properties such as nickname, bio etc.
	 */
	Optional<ClanAssociate> getAssociate(UUID uuid);

	/**
	 * Gets a clan associate by their player name.
	 *
	 * @param playerName The player to use.
	 * @return A clan associate with properties such as nickname, bio etc.
	 */
	Optional<ClanAssociate> getAssociate(String playerName);

	/**
	 * Get the ClansPro file listing.
	 *
	 * @return Get's the file collection for the given plugin.
	 */
	FileList getFileList();

	/**
	 * Gets the service manager for event cycles.
	 *
	 * @return The event cycle services manager.
	 */
	KeyedServiceManager<ClanAddon> getServiceManager();

	/**
	 * Get the manager for clans to load/delete from.
	 *
	 * @return The clan manager.
	 */
	ClanManager getClanManager();

	/**
	 * Get the manager for clan claims.
	 *
	 * @return The claim manager.
	 */
	ClaimManager getClaimManager();

	/**
	 * Get the manger for the raid-shield
	 *
	 * @return The raid shield manager.
	 */
	ShieldManager getShieldManager();

	/**
	 * Check if pro needs to be updated.
	 *
	 * @return true if the plugin has an update.
	 */
	boolean isUpdated();

	/**
	 * Check if a clan contains the target UUID
	 *
	 * @param target The target "Member"
	 * @param clanID The target clan to search
	 * @return true if the given clan's members contains the given uuid
	 */
	boolean isClanMember(UUID target, String clanID);

	/**
	 * Check if a target player is currently a member of a clan.
	 *
	 * @param target The target uuid to search for.
	 * @return result = true if the target player is in a clan.
	 */
	boolean isInClan(UUID target);

	/**
	 * Check if a specified clan name is black-listed
	 *
	 * @param name The clan name in question
	 * @return result = true if the clan name is not allowed.
	 */
	boolean isNameBlackListed(String name);

	/**
	 * Converts and clan id into a clan name
	 *
	 * @param clanID The clan id to convert
	 * @return A clan name or null
	 */
	String getClanName(String clanID);

	/**
	 * Converts a clan name into a clan id
	 *
	 * @param clanName The clan tag to convert
	 * @return A clan id or null
	 */
	String getClanID(String clanName);

	/**
	 * Get the bare id object for a player's given clan.
	 *
	 * @param uuid The player to search for.
	 * @return A clan id or null
	 */
	HUID getClanID(UUID uuid);

	/**
	 * Set a player's rank priority disregarding all factors. (Besides ownership)
	 *
	 * @param associate The clan associate to promote/demote.
	 * @param priority  The rank to give
	 */
	@Deprecated
	void setRank(ClanAssociate associate, RankPriority priority);

	/**
	 * Search and automatically register all found pro addons in a given package location
	 *
	 * @param packageName The package location to browse for addons.
	 */
	void searchNewAddons(Plugin plugin, String packageName);

	/**
	 * Automatically hook a specific addon via class instantiation.
	 * <p>
	 * Desired class must inherit EventCycle functionality.
	 *
	 * @param cycle The class that extends EventCycle functionality
	 */
	void importAddon(Class<? extends ClanAddon> cycle);

	/**
	 * Kick a specified user from a clan they might be in.
	 *
	 * @param uuid The user to kick from their clan.
	 * @return true if the user was kicked otherwise false if the user isn't in a clan.
	 */
	boolean kickUser(UUID uuid);

	/**
	 * Onboard a specified user to a clan of specification.
	 *
	 * @param uuid The user to invite
	 * @return true if the user isn't in a clan otherwise false if the user is in a clan.
	 */
	boolean obtainUser(UUID uuid, String clanName);

	/**
	 * Gets the first found cooldown object for an action label
	 *
	 * @param action The cooldown label
	 * @return A cooldown object for a given clan.
	 */
	ClanCooldown getCooldownByAction(String action);

	/**
	 * Gets an event cycle for a specified addon
	 *
	 * @param name The addon to manage
	 * @return An event cycle for a given addon's listeners
	 */
	ClanAddon getEventCycleByAddon(String name);


}
