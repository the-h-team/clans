package com.github.sanctum.clans.model;

import com.github.sanctum.panther.util.RandomID;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class ClanBlueprint {

	private final Map<UUID, Clan.Rank> memberList = new HashMap<>();

	private final String clanName;

	private UUID leader;

	private String password;

	private boolean isLocked;

	/**
	 * Instantiate a new clan blueprint
	 * Set the leader, add members & customize other qualities of the clan object.
	 *
	 * @param clanName The name of the clan
	 * @param isLocked Whether or not people can join the clan without a password
	 */
	public ClanBlueprint(String clanName, boolean isLocked) {
		this.clanName = clanName;
		this.isLocked = isLocked;
	}

	/**
	 * Assign a rank position to a player
	 *
	 * @param uuid     The player to gain membership
	 * @param priority The rank priority of the player
	 * @return The same blueprint object with the newly applied values.
	 */
	public ClanBlueprint addMember(UUID uuid, Clan.Rank priority) {
		this.memberList.put(uuid, priority);
		return this;
	}

	/**
	 * Assign multiple rank positions to members from a local map.
	 *
	 * @param members The local map object to apply changes from
	 * @return The same blueprint object with the newly applied values.
	 */
	public ClanBlueprint addMembers(Map<UUID, Clan.Rank> members) {
		this.memberList.putAll(members);
		return this;
	}

	/**
	 * Assign clan leadership to an online player.
	 *
	 * @param player The online player to assign leadership to.
	 * @return The same blueprint object with the newly applied values.
	 */
	public ClanBlueprint setLeader(Player player) {
		this.leader = player.getUniqueId();
		return this;
	}

	/**
	 * Assign clan leadership to a unique id.
	 *
	 * @param id The unique id to use as ownership.
	 * @return The same blueprint object with the newly applied values.
	 */
	public ClanBlueprint setLeader(UUID id) {
		this.leader = id;
		return this;
	}

	/**
	 * Assign a password of specification to the clan only if it is
	 * locked on creation.
	 *
	 * @param password The password to assign to the clan if locked
	 * @return The same blueprint object with the newly applied values.
	 */
	public ClanBlueprint setPassword(String password) {
		if (!isLocked)
			this.isLocked = true;
		this.password = password;
		return this;
	}

	/**
	 * Convert this blueprint object into a builder and get it ready
	 * for finalization..
	 *
	 * @return The finishing builder.
	 */
	public ClanBuilder toBuilder() {
		if (this.password == null && this.isLocked) {
			setPassword(new RandomID(24, "JNASPOIYL125789").generate().toLowerCase(Locale.ROOT));
		}
		return new ClanBuilder(this);
	}

	protected Map<UUID, Clan.Rank> getMemberList() {
		return this.memberList;
	}

	protected UUID getLeader() {
		return this.leader;
	}

	protected String getClanName() {
		return this.clanName;
	}

	protected String getPassword() {
		return this.password != null ? this.password : "none";
	}
}
