package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.extra.ClanWar;
import com.github.sanctum.labyrinth.formatting.UniformedComponents;
import com.github.sanctum.labyrinth.library.HUID;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Clan extends ClanBank, Serializable {

	ClanAction ACTION = new ClanAction();

	/**
	 * Adds the specified target as a clan member and returns an associate object.
	 *
	 * @param target The target to collect.
	 * @return A valid clan associate or null.
	 */
	@Nullable ClanAssociate accept(UUID target);

	/**
	 * Claim the target chunk for this clan if possible.
	 *
	 * @param c The target chunk
	 * @return The newly claimed chunk or null if unable to claim.
	 */
	@Nullable Claim obtain(Chunk c);

	/**
	 * Get a specified cooldown from cache
	 *
	 * @param action The label to search for
	 * @return The clans cooldown information for the given action
	 */
	@Nullable ClanCooldown getCooldown(String action);

	/**
	 * Retrieve a value of specified type from this clans persistent data container.
	 *
	 * @param type The type of object to retrieve.
	 * @param key  The key delimiter for the object.
	 * @param <R>  The desired serializable object.
	 * @return The desired serializable object.
	 */
	<R> R getValue(Class<R> type, String key);

	/**
	 * Store a custom serializable object to this clans data container.
	 *
	 * @param key   The key delimiter for the value.
	 * @param value The desired serializable object to be stored.
	 * @param <R>   The type of the value.
	 * @return The same value passed through the parameters.
	 */
	<R> R setValue(String key, R value, boolean temporary);

	/**
	 * Kick a specified member from the clan.
	 *
	 * @param target The specified target to kick.
	 * @return true if the target is a member and got kicked.
	 */
	boolean kick(UUID target);

	/**
	 * Check the clans pvp-mode
	 *
	 * @return false if war mode
	 */
	boolean isPeaceful();

	/**
	 * Check if the clan allows friend-fire
	 *
	 * @return true if friendly fire
	 */
	boolean isFriendlyFire();

	/**
	 * Check if this clan owns the provided chunk.
	 *
	 * @param chunk The chunk to check
	 * @return true if the provided chunk is owned by this clan.
	 */
	boolean isOwner(@NotNull Chunk chunk);

	/**
	 * Transfer ownership of the clan to a specified clan member.
	 *
	 * @param target The user to transfer ownership to.
	 * @return true if they are a member of the clan and can be promoted.
	 */
	boolean transferOwnership(UUID target);

	/**
	 * Check if the clan is neutral in relation with another clan.
	 *
	 * @param targetClanId Target clan
	 * @return true if the two clans are neutral in relation.
	 */
	boolean isNeutral(String targetClanId);

	/**
	 * Check if the clan has a cooldown
	 *
	 * @param action The label to search for
	 * @return false if cooldown cache doesn't contain reference
	 */
	boolean hasCooldown(String action);

	/**
	 * Remove a persistent value from this clans data container.
	 *
	 * @param key The values key delimiter.
	 * @return true if successfully removed.
	 */
	boolean removeValue(String key);

	/**
	 * Change the clans name
	 *
	 * @param newTag String to change name to.
	 */
	void setName(String newTag);

	/**
	 * Change the clans description
	 */
	void setDescription(String description);

	/**
	 * Change the clan's password
	 *
	 * @param password String to change password to.
	 */
	void setPassword(String password);

	/**
	 * Change the clans color code
	 *
	 * @param newColor Color-code to change the value to.
	 */
	void setColor(String newColor);

	/**
	 * Change the clan's pvp-mode
	 *
	 * @param peaceful The boolean to change the value to
	 */
	void setPeaceful(boolean peaceful);

	/**
	 * Change the friendlyfire status of the clan
	 *
	 * @param friendlyFire The boolean to change the value to
	 */
	void setFriendlyFire(boolean friendlyFire);

	/**
	 * Change the clan's base location
	 *
	 * @param location Update the clans base to a specified location.
	 */
	void setBase(@NotNull Location location);

	/**
	 * Send a message to the clan
	 *
	 * @param message String to broadcast.
	 */
	void broadcast(String message);

	/**
	 * Send a message to specific clan members.
	 *
	 * @param message String to broadcast.
	 */
	void broadcast(Predicate<ClanAssociate> predicate, String message);

	/**
	 * Give the clan some power
	 *
	 * @param amount double amount to give
	 */
	void givePower(double amount);

	/**
	 * Take some power from the clan
	 *
	 * @param amount double amount to take
	 */
	void takePower(double amount);

	/**
	 * Add to the clans max claim's.
	 *
	 * @param amount double amount to give
	 */
	void addMaxClaim(int amount);

	/**
	 * Take from the clans max claim's.
	 *
	 * @param amount double amount to take
	 */
	void takeMaxClaim(int amount);

	/**
	 * Add win's to the clan's war counter
	 */
	void addWin(int amount);

	/**
	 * Add losses to the clan's war counter
	 */
	void addLoss(int amount);

	/**
	 * Send a target clan an ally request.
	 *
	 * @param targetClan The target clan to request positive relation with.
	 */
	void sendAllyRequest(HUID targetClan);

	/**
	 * Send a target clan an ally request.
	 *
	 * @param targetClan The target clan to request positive relation with.
	 * @param message    The custom message to send to the clan.
	 */
	void sendAllyRequest(HUID targetClan, String message);

	/**
	 * Force alliance with a specified clan.
	 *
	 * @param targetClan The target clan to ally by their id.
	 */
	void addAlly(HUID targetClan);

	/**
	 * Force alliance removal from a specified clan for this clan only.
	 * <p>
	 * Its possible you will need to also run this method from the targeted clan.
	 *
	 * @param targetClan The target clan to make neutral by their id.
	 */
	void removeAlly(HUID targetClan);

	/**
	 * Force enemy relation with a specified clan.
	 *
	 * @param targetClan The target clan to make enemies with by their id.
	 */
	void addEnemy(HUID targetClan);

	/**
	 * Force the removal of a specified enemy for this clan only.
	 * <p>
	 * Its possible you will need to also run this method from the targeted clan.
	 *
	 * @param targetClan The target clan to make neutral by their id.
	 */
	void removeEnemy(HUID targetClan);

	/**
	 * Get the id of the clan stored within the object
	 *
	 * @return clanID stored within the clan object as an HUID
	 */
	@NotNull HUID getId();

	/**
	 * Get the name of the clan
	 *
	 * @return Gets the clan objects clan tag
	 */
	@NotNull String getName();

	/**
	 * Get the color theme for the clan
	 *
	 * @return Gets the clan objects clan tag color
	 */
	@NotNull String getColor();

	/**
	 * Get the clans description
	 *
	 * @return The clans description
	 */
	@NotNull String getDescription();

	/**
	 * Get the clan's password
	 *
	 * @return The clan's password otherwise null
	 */
	@Nullable String getPassword();

	/**
	 * Get the user who owns the clan.
	 *
	 * @return Gets the clan owner.
	 */
	@NotNull ClanAssociate getOwner();

	/**
	 * Get a member by specification from the clan.
	 *
	 * @param predicate The operation to use.
	 * @return The clan associate or null.
	 */
	@Nullable ClanAssociate getMember(Predicate<ClanAssociate> predicate);

	/**
	 * Format a given double into different configured language types
	 *
	 * @param amount double to format
	 * @return Gets the formatted result as a local.
	 */
	@NotNull String format(String amount);

	/**
	 * Get a full roster of clan allies by clan id
	 *
	 * @return A string array of clan ids
	 */
	@NotNull List<String> getAllyList();

	/**
	 * Get a full roster of clan enemies by clan id
	 *
	 * @return A string list of clan ids
	 */
	@NotNull List<String> getEnemyList();

	/**
	 * Get all object key's within this clans data container.
	 *
	 * @return The list of key's for this clans data container.
	 */
	@NotNull List<String> getDataKeys();

	/**
	 * Get a full list of all current clan's attempting positive relation with us.
	 *
	 * @return A string list of clan ally requests by clan id.
	 */
	@NotNull List<String> getAllyRequests();

	/**
	 * Get the full roster of clan members.
	 *
	 * @return Gets the member list of the clan object.
	 */
	@NotNull String[] getMemberIds();

	/**
	 * Get an array of information for the clan
	 *
	 * @return String array containing clan stats
	 */
	@NotNull String[] getClanInfo();

	/**
	 * Get the full list of owned claims for this clan by id.
	 *
	 * @return An array of claim id's
	 */
	@NotNull String[] getOwnedClaimsList();

	/**
	 * Get the full list of owned claims for this clan.
	 *
	 * @return An array of clan claims
	 */
	@NotNull Claim[] getOwnedClaims();

	/**
	 * Get the full member roster for the clan.
	 *
	 * @return A set of all clan associates.
	 */
	@NotNull Set<ClanAssociate> getMembers();

	/**
	 * Get a full roster of allied clans for this clan.
	 *
	 * @return A uniformed component listing.
	 */
	@NotNull UniformedComponents<Clan> getAllies();

	/**
	 * Get a full roster of rivaled clans for this clan.
	 *
	 * @return A uniformed component listing.
	 */
	@NotNull UniformedComponents<Clan> getEnemies();

	/**
	 * Get's the location of the clans base
	 *
	 * @return A base location.
	 */
	@Nullable Location getBase();

	/**
	 * Get the amount of power the clan has
	 *
	 * @return double value
	 */
	double getPower();

	/**
	 * {@inheritDoc}
	 */
	int getMaxClaims();

	/**
	 * {@inheritDoc}
	 */
	int getWins();

	/**
	 * {@inheritDoc}
	 */
	int getLosses();

	/**
	 * Get a clans cooldown cache
	 *
	 * @return A collection of cooldown objects for the clan
	 */
	@NotNull List<ClanCooldown> getCooldowns();

	/**
	 * {@inheritDoc}
	 */
	@NotNull List<Clan> getWarInvites();

	default ClanWar getCurrentWar() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	default Implementation getImplementation() {
		return Implementation.UNKNOWN;
	}

	void save();

	enum Implementation {
		DEFAULT, CUSTOM, UNKNOWN
	}


}
