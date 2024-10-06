package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.model.ClanVentBus;
import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.Clearance;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.event.associate.AssociateUpdateBaseEvent;
import org.bukkit.entity.Player;

public class CommandSetbase extends ClanSubCommand {
	public CommandSetbase() {
		super("setbase");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.setbase.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("setbase")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("setbase")));
				return true;
			}

			if (associate == null) {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}

			Clan clan = associate.getClan();
			if (Clearance.MANAGE_BASE.test(associate)) {
				if (!ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
					AssociateUpdateBaseEvent event = ClanVentBus.call(new AssociateUpdateBaseEvent(p, p.getLocation()));
					if (!event.isCancelled()) {
						clan.setBase(event.getLocation());
					}
				} else {
					if (ClansAPI.getInstance().getClaimManager().getClaim(p.getLocation()).getOwner().getTag().getId().equals(clan.getId().toString())) {
						AssociateUpdateBaseEvent event = ClanVentBus.call(new AssociateUpdateBaseEvent(p, p.getLocation()));
						if (!event.isCancelled()) {
							clan.setBase(event.getLocation());
						}
					} else {
						lib.sendMessage(p, lib.notClaimOwner(((Clan) ClansAPI.getInstance().getClaimManager().getClaim(p.getLocation()).getHolder()).getName()));
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
