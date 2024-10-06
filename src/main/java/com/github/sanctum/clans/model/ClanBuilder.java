package com.github.sanctum.clans.model;

import com.github.sanctum.clans.impl.DefaultClan;
import com.github.sanctum.clans.impl.entity.PlayerAssociate;
import com.github.sanctum.clans.impl.entity.EntityAssociate;
import com.github.sanctum.clans.impl.entity.ServerAssociate;
import com.github.sanctum.panther.util.HUID;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;

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
        if (ClansAPI.getInstance().getAssociate(leader).isPresent()) {
            Clan.ACTION.remove(leader, true).deploy();
        }
        HUID newID = HUID.randomID();
        DefaultClan test = new DefaultClan(RankRegistry.getInstance(), newID.toString());
        test.setName(clanName);
        if (!password.equals("none")) {
            test.setPassword(password);
        }
        boolean war = ClansAPI.getDataInstance().getConfig().read(c -> c.getString("Clans.mode-change.default").equalsIgnoreCase("peace"));
        test.setPeaceful(war);
        Clan.Rank highest = RankRegistry.getInstance().getHighest();
        if (leader.equals(ClansAPI.getInstance().getSessionId())) {
            test.add(new ServerAssociate(InvasiveEntity.wrapNonAssociated(Bukkit.getConsoleSender()), highest, test));
        } else if (Bukkit.getEntity(leader) != null) {
			test.add(new EntityAssociate(InvasiveEntity.wrapNonAssociated(Bukkit.getEntity(leader)), highest, test));
        } else {
            test.add(new PlayerAssociate(leader, highest, test));
        }
        for (Map.Entry<UUID, Clan.Rank> entry : memberList.entrySet()) {
            if (ClansAPI.getInstance().getAssociate(entry.getKey()).isPresent()) {
                Clan.ACTION.remove(entry.getKey(), true).deploy();
            }
            if (entry.getKey().equals(ClansAPI.getInstance().getSessionId())) {
                Clan.Associate a = new ServerAssociate(InvasiveEntity.wrapNonAssociated(Bukkit.getConsoleSender()), entry.getValue(), test);
                test.add(a);
            } else {
                Clan.Associate a = new PlayerAssociate(entry.getKey(), entry.getValue(), test);
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
