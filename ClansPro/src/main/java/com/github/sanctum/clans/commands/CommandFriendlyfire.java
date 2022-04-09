package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import java.util.Collections;
import org.bukkit.entity.Player;

public class CommandFriendlyfire extends ClanSubCommand {
	public CommandFriendlyfire() {
		super("friendlyfire");
		setAliases(Collections.singletonList("ff"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("friendlyfire"))) {
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

		if (args.length == 1) {
			if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("create"))) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("create")));
				return true;
			}
			if (!isAlphaNumeric(args[0])) {
				lib.sendMessage(p, lib.nameInvalid(args[0]));
				return true;
			}
			if (Clan.ACTION.getAllClanNames().contains(args[0])) {
				lib.sendMessage(p, lib.alreadyMade(args[0]));
				return true;
			}
			Clan.ACTION.create(p.getUniqueId(), args[0], null);
			return true;
		}


		return true;
	}
}
