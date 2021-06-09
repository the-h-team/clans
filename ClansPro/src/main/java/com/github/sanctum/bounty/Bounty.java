package com.github.sanctum.bounty;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.FileManager;
import java.math.BigDecimal;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class Bounty {

	private final Clan charge;
	private final UUID target;
	private final double amount;

	public Bounty(Clan charge, UUID target, double amount) {
		this.charge = charge;
		this.target = target;
		this.amount = amount;
	}

	public Clan getCharge() {
		return charge;
	}

	public BigDecimal getAmount() {
		return BigDecimal.valueOf(amount);
	}

	public OfflinePlayer getTarget() {
		return Bukkit.getOfflinePlayer(target);
	}

	public Clan getAssociatedClan() {
		return ClansAPI.getInstance().getAssociate(target).orElse(null) != null ? ClansAPI.getInstance().getClan(target) : null;
	}

	public void complete() {
		FileManager clanFile = ClansAPI.getData().getClanFile(charge);
		clanFile.getConfig().set("bounties." + target.toString(), null);
		clanFile.refreshConfig();
	}

}
