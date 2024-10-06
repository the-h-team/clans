package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.Clearance;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.impl.DefaultClan;
import java.util.Collections;
import org.bukkit.entity.Player;

public class CommandFriendlyfire extends ClanSubCommand {
	public CommandFriendlyfire() {
		super("friendlyfire");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.friendlyfire.text"));
		setAliases(Collections.singletonList("ff"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("friendlyfire")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("friendlyfire")));
				return true;
			}

			if (associate == null) {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}
			if (Clearance.MANAGE_FRIENDLY_FIRE.test(associate)) {
				if (!(associate.getClan() instanceof DefaultClan))
					return true;
				DefaultClan c = (DefaultClan) associate.getClan();
				if (ClansAPI.getDataInstance().isTrue("Clans.friendly-fire.timer.use")) {

					if (c.isFriendlyFire()) {
						if (c.getFriendlyCooldown().isComplete()) {
							c.setFriendlyFire(false);
							c.broadcast(lib.friendlyFireOff(p.getName()));
							c.getFriendlyCooldown().setCooldown();
						} else {
							lib.sendMessage(p, c.getFriendlyCooldown().fullTimeLeft());
							return true;
						}
					} else {
						if (c.isPeaceful()) {
							lib.sendMessage(p, lib.peacefulDeny());
							return true;
						}
						if (c.getFriendlyCooldown().isComplete()) {
							c.setFriendlyFire(true);
							c.getFriendlyCooldown().setCooldown();
							c.broadcast(lib.friendlyFireOn(p.getName()));
						} else {
							lib.sendMessage(p, c.getFriendlyCooldown().fullTimeLeft());
							return true;
						}
						return true;
					}
					return true;
				} else {
					if (c.isFriendlyFire()) {
						c.setFriendlyFire(false);
						c.broadcast(lib.friendlyFireOff(p.getName()));
					} else {
						if (c.isPeaceful()) {
							lib.sendMessage(p, lib.peacefulDeny());
							return true;
						}
						c.setFriendlyFire(true);
						c.broadcast(lib.friendlyFireOn(p.getName()));
						return true;
					}
				}
			} else {
				lib.sendMessage(p, lib.noClearance());
				return true;
			}
			return true;
		}

		return true;
	}
}
