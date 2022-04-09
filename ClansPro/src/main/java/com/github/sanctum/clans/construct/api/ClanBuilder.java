package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.construct.impl.entity.DefaultAssociate;
import com.github.sanctum.clans.construct.impl.entity.ServerAssociate;
import com.github.sanctum.labyrinth.library.HUID;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;

public final class ClanBuilder {

	private final Map<UUID, Clan.Rank> memberList;

	private Clan ticket;

	private final UUID leader;

	private final String clanName;

	private final String password;

	ClanBuilder(ClanBlueprint blueprint) {
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
	public ClanBuilder build() {
		if (ClansAPI.getInstance().isInClan(leader)) {
			Clan.ACTION.removePlayer(leader);
		}
		HUID newID = HUID.randomID();
		DefaultClan test = new DefaultClan(newID.toString());
		test.setName(clanName);
		if (!password.equals("none")) {
			test.setPassword(password);
		}
		boolean war = ClansAPI.getDataInstance().getConfig().read(c -> c.getString("Clans.mode-change.default").equalsIgnoreCase("peace"));
		test.setPeaceful(war);
		if (leader.equals(ClansAPI.getInstance().getSessionId())) {
			test.add(new ServerAssociate(InvasiveEntity.wrapNonAssociated(Bukkit.getConsoleSender()), Clan.Rank.HIGHEST, test));
		} else {
			test.add(new DefaultAssociate(leader, Clan.Rank.HIGHEST, test));
		}
		for (Map.Entry<UUID, Clan.Rank> entry : memberList.entrySet()) {
			if (ClansAPI.getInstance().isInClan(entry.getKey())) {
				Clan.ACTION.removePlayer(entry.getKey());
			}
			if (entry.getKey().equals(ClansAPI.getInstance().getSessionId())) {
				Clan.Associate a = new ServerAssociate(InvasiveEntity.wrapNonAssociated(Bukkit.getConsoleSender()), entry.getValue(), test);
				test.add(a);
			} else {
				Clan.Associate a = new DefaultAssociate(entry.getKey(), entry.getValue(), test);
				test.add(a);
			}
		}
		this.ticket = test;
		return this;
	}

	/**
	 * NOTE: Only use after {@link ClanBuilder#build()} has been initialized
	 * Get the clan object created from the builder.
	 *
	 * @return The clan object from creation.
	 */
	public Clan getClan() {
		return this.ticket;
	}

	/**
	 * NOTE: Only use after {@link ClanBuilder#build()} has been initialized
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
	 * NOTE: Only use after {@link ClanBuilder#build()} has been initialized
	 * Get the clan object created from the builder.
	 * <p>
	 * Apply a desired amount of claims to the clan by default
	 *
	 * @param amount The amount to give.
	 */
	public ClanBuilder giveClaims(int amount) {
		getClan().giveClaims(amount);
		return this;
	}

	/**
	 * NOTE: Only use after {@link ClanBuilder#build()} has been initialized
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
	 * NOTE: Only use after {@link ClanBuilder#build()} has been initialized
	 * Get the clan object created from the builder.
	 * <p>
	 * Set back a desired amount of claims the clan has by default
	 *
	 * @param amount The amount to take.
	 */
	public ClanBuilder takeClaims(int amount) {
		getClan().takeClaims(amount);
		return this;
	}

	/**
	 * NOTE: Only use after {@link ClanBuilder#build()} has been initialized
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
	 * NOTE: Only use after {@link ClanBuilder#build()} has been initialized
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
