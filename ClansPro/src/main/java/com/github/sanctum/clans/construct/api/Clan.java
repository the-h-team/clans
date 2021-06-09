package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.actions.ClanCooldown;
import com.github.sanctum.clans.construct.extra.misc.ClanWar;
import com.github.sanctum.labyrinth.formatting.UniformedComponents;
import com.github.sanctum.labyrinth.library.HUID;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Clan extends ClanBank, Serializable {

	/**
	 * {@inheritDoc}
	 */
	@Nullable ClanAssociate accept(UUID target);

	/**
	 * {@inheritDoc}
	 */
	boolean kick(UUID target);

	/**
	 * {@inheritDoc}
	 */
	boolean isPeaceful();

	/**
	 * {@inheritDoc}
	 */
	boolean isFriendlyFire();

	/**
	 * {@inheritDoc}
	 */
	boolean isOwner(@NotNull Chunk chunk);

	/**
	 * {@inheritDoc}
	 */
	boolean transferOwnership(UUID target);

	/**
	 * {@inheritDoc}
	 */
	boolean isNeutral(String targetClanId);

	/**
	 * {@inheritDoc}
	 */
	boolean hasCooldown(String action);

	/**
	 * {@inheritDoc}
	 */
	void setName(String newTag);

	/**
	 * {@inheritDoc}
	 */
	void setDescription(String description);

	/**
	 * {@inheritDoc}
	 */
	void setPassword(String password);

	/**
	 * {@inheritDoc}
	 */
	void setColor(String newColor);

	/**
	 * {@inheritDoc}
	 */
	void setPeaceful(boolean peaceful);

	/**
	 * {@inheritDoc}
	 */
	void setFriendlyFire(boolean friendlyFire);

	/**
	 * {@inheritDoc}
	 */
	void setBase(@NotNull Location location);

	/**
	 * {@inheritDoc}
	 */
	void broadcast(String message);

	/**
	 * {@inheritDoc}
	 */
	void givePower(double amount);

	/**
	 * {@inheritDoc}
	 */
	void takePower(double amount);

	/**
	 * {@inheritDoc}
	 */
	void addMaxClaim(int amount);

	/**
	 * {@inheritDoc}
	 */
	void takeMaxClaim(int amount);

	/**
	 * {@inheritDoc}
	 */
	void addWin(int amount);

	/**
	 * {@inheritDoc}
	 */
	void addLoss(int amount);

	/**
	 * {@inheritDoc}
	 */
	void sendAllyRequest(HUID targetClan);

	/**
	 * {@inheritDoc}
	 */
	void addAlly(HUID targetClan);

	/**
	 * {@inheritDoc}
	 */
	void removeAlly(HUID targetClan);

	/**
	 * {@inheritDoc}
	 */
	void addEnemy(HUID targetClan);

	/**
	 * {@inheritDoc}
	 */
	void removeEnemy(HUID targetClan);

	/**
	 * {@inheritDoc}
	 */
	@NotNull HUID getId();

	/**
	 * {@inheritDoc}
	 */
	@NotNull String getName();

	/**
	 * {@inheritDoc}
	 */
	@NotNull String getColor();

	/**
	 * {@inheritDoc}
	 */
	@NotNull String getDescription();

	/**
	 * {@inheritDoc}
	 */
	@Nullable String getPassword();

	/**
	 * {@inheritDoc}
	 */
	@NotNull UUID getOwner();

	/**
	 * {@inheritDoc}
	 */
	@NotNull String format(String amount);

	/**
	 * {@inheritDoc}
	 */
	@NotNull String[] getMembersList();

	/**
	 * {@inheritDoc}
	 */
	@NotNull List<String> getAllyList();

	/**
	 * {@inheritDoc}
	 */
	@NotNull List<String> getEnemyList();

	/**
	 * {@inheritDoc}
	 */
	@NotNull List<String> getAllyRequests();

	/**
	 * {@inheritDoc}
	 */
	@NotNull String[] getClanInfo();

	/**
	 * {@inheritDoc}
	 */
	@NotNull String[] getOwnedClaimsList();

	/**
	 * {@inheritDoc}
	 */
	@NotNull Claim[] getOwnedClaims();

	/**
	 * {@inheritDoc}
	 */
	@NotNull UniformedComponents<OfflinePlayer> getPlayers();

	/**
	 * {@inheritDoc}
	 */
	@NotNull UniformedComponents<ClanAssociate> getMembers();

	/**
	 * {@inheritDoc}
	 */
	@NotNull UniformedComponents<Clan> getAllies();

	/**
	 * {@inheritDoc}
	 */
	@NotNull UniformedComponents<Clan> getEnemies();

	/**
	 * {@inheritDoc}
	 */
	@NotNull List<String> getDataKeys();

	/**
	 * {@inheritDoc}
	 */
	<R> R getValue(Class<R> type, String key);

	/**
	 * {@inheritDoc}
	 */
	<R> R setValue(String key, R value);

	/**
	 * {@inheritDoc}
	 */
	boolean removeValue(String key);

	/**
	 * {@inheritDoc}
	 */
	@Nullable Location getBase();

	/**
	 * {@inheritDoc}
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
	 * {@inheritDoc}
	 */
	@NotNull List<ClanCooldown> getCooldowns();

	/**
	 * {@inheritDoc}
	 */
	@NotNull List<Clan> getWarInvites();

	/**
	 * {@inheritDoc}
	 */
	@Nullable Claim obtain(Chunk c);

	default ClanWar getCurrentWar() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	default Implementation getImplementation() {
		return Implementation.UNKNOWN;
	}

	enum Implementation {
		DEFAULT, CUSTOM, UNKNOWN
	}


}
