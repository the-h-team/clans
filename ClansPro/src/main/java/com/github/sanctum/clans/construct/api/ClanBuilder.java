package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.util.RankPriority;
import java.util.Map;
import java.util.UUID;

public class ClanBuilder {

	private final Map<UUID, RankPriority> memberList;

	private final UUID leader;

	private final String clanName;

	private final String password;

	private final ClanBlueprint blueprint;

	protected ClanBuilder(ClanBlueprint blueprint) {
		this.blueprint = blueprint;
		this.memberList = blueprint.getMemberList();
		this.leader = blueprint.getLeader();
		this.clanName = blueprint.getClanName();
		this.password = blueprint.getPassword();
	}

	/**
	 * Create the clan with the retained values from the blueprint object
	 * and apply all member rank priorities. NOTE: If any one of the members including
	 * the clan leader is within a clan during this time period they will be
	 * forcefully removed.
	 */
	public ClanBuilder supply() {
		if (ClansAPI.getInstance().isInClan(leader)) {
			DefaultClan.action.removePlayer(leader);
		}
		if (blueprint.isOffline) {
			DefaultClan.action.createOffline(leader, clanName, !password.equals("none") ? password : null);
		} else {
			DefaultClan.action.create(leader, clanName, !password.equals("none") ? password : null);
		}
		for (Map.Entry<UUID, RankPriority> entry : memberList.entrySet()) {
			if (ClansAPI.getInstance().isInClan(entry.getKey())) {
				DefaultClan.action.removePlayer(entry.getKey());
			}
			DefaultClan.action.joinClan(entry.getKey(), clanName, password);

			ClanAssociate associate = ClansAPI.getInstance().getAssociate(entry.getKey()).orElse(null);
			ClansAPI.getInstance().setRank(associate, entry.getValue());
		}
		return this;
	}

	/**
	 * NOTE: Only use after {@link ClanBuilder#supply()} has been initialized
	 * Get the clan object created from the builder.
	 *
	 * @return The clan object from creation.
	 */
	public Clan getClan() {
		return ClansAPI.getInstance().getClan(leader);
	}

	/**
	 * NOTE: Only use after {@link ClanBuilder#supply()} has been initialized
	 * Get the clan object created from the builder.
	 * <p>
	 * Apply a desired amount of power to the clan by default
	 *
	 * @param amount The amount to give.
	 */
	public ClanBuilder givePower(double amount) {
		getClan().givePower(amount);
		return this;
	}

	/**
	 * NOTE: Only use after {@link ClanBuilder#supply()} has been initialized
	 * Get the clan object created from the builder.
	 * <p>
	 * Apply a desired amount of claims to the clan by default
	 *
	 * @param amount The amount to give.
	 */
	public ClanBuilder giveClaims(int amount) {
		getClan().addMaxClaim(amount);
		return this;
	}

	/**
	 * NOTE: Only use after {@link ClanBuilder#supply()} has been initialized
	 * Get the clan object created from the builder.
	 * <p>
	 * Set back a desired amount of power the clan has by default
	 *
	 * @param amount The amount to take.
	 */
	public ClanBuilder takePower(double amount) {
		getClan().takePower(amount);
		return this;
	}

	/**
	 * NOTE: Only use after {@link ClanBuilder#supply()} has been initialized
	 * Get the clan object created from the builder.
	 * <p>
	 * Set back a desired amount of claims the clan has by default
	 *
	 * @param amount The amount to take.
	 */
	public ClanBuilder takeClaims(int amount) {
		getClan().takeMaxClaim(amount);
		return this;
	}

	/**
	 * NOTE: Only use after {@link ClanBuilder#supply()} has been initialized
	 * Get the clan object created from the builder.
	 * <p>
	 * Set the clans default description
	 *
	 * @param context The information to update the description with
	 */
	public ClanBuilder setDescription(String context) {
		getClan().setDescription(context);
		return this;
	}

	/**
	 * Change the clans friendly status
	 * <p>
	 * NOTE: Only use after {@link ClanBuilder#supply()} has been initialized
	 * Get the clan object created from the builder.
	 * <p>
	 * Setting this to false will make the clan switch
	 * to 'War' mode as-well as turn friendly-fire on.
	 *
	 * @param friendly The peace status of the clan
	 */
	public ClanBuilder setIsFriendly(boolean friendly) {
		getClan().setFriendlyFire(!friendly);
		getClan().setPeaceful(friendly);
		return this;
	}

}
