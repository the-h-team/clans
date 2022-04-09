package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.GUI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.clans.event.associate.AssociateDisplayInfoEvent;
import java.util.Collections;
import java.util.UUID;
import org.bukkit.entity.Player;

public class CommandInfo extends ClanSubCommand {
	public CommandInfo() {
		super("info");
		setAliases(Collections.singletonList("i"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (args.length == 0) {
			if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("info"))) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("info")));
				return true;
			}
			if (associate != null) {
				AssociateDisplayInfoEvent ev = ClanVentBus.call(new AssociateDisplayInfoEvent(associate, AssociateDisplayInfoEvent.Type.PERSONAL));
				if (!ev.isCancelled()) {
					Clan.ACTION.getClanboard(p, 1);
				}
			} else {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}
			return true;
		}

		if (args.length == 1) {
			if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("info-other"))) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("info-other")));
				return true;
			}
			UUID target = Clan.ACTION.getUserID(args[0]);
			if (target == null) {
				if (!Clan.ACTION.getAllClanNames().contains(args[0])) {
					lib.sendMessage(p, lib.clanUnknown(args[0]));
					return true;
				}
				if (associate != null && args[0].equals(associate.getClan().getName())) {
					AssociateDisplayInfoEvent ev = ClanVentBus.call(new AssociateDisplayInfoEvent(associate, AssociateDisplayInfoEvent.Type.PERSONAL));
					if (!ev.isCancelled()) {
						Clan.ACTION.getClanboard(p, 1);
					}
					return true;
				}
				Clan clan = ClansAPI.getInstance().getClanManager().getClan(ClansAPI.getInstance().getClanManager().getClanID(args[0]));
				AssociateDisplayInfoEvent ev = ClanVentBus.call(new AssociateDisplayInfoEvent(associate, p, clan, AssociateDisplayInfoEvent.Type.OTHER));
				if (!ev.isCancelled()) {
					for (String info : clan.getClanInfo()) {

						if (associate != null) {
							lib.sendMessage(p, info.replace(associate.getClan().getName(), "&6&lUS"));
						} else {
							lib.sendMessage(p, info);
						}
					}
				}
				if (ClansAPI.getDataInstance().ID_MODE.containsKey(p) && ClansAPI.getDataInstance().ID_MODE.get(p).equals("ENABLED")) {
					lib.sendMessage(p, "&7#&fID &7of clan " + '"' + args[0] + '"' + " is: &e&o" + ClansAPI.getInstance().getClanManager().getClanID(args[0]));
				}
				return true;
			}
			ClansAPI.getInstance().getAssociate(target).ifPresent(a -> {
				GUI.MEMBER_INFO.get(a).open(p);
				if (ClansAPI.getDataInstance().ID_MODE.containsKey(p) && ClansAPI.getDataInstance().ID_MODE.get(p).equals("ENABLED")) {
					lib.sendMessage(p, "&7#&fID &7of player " + '"' + args[0] + '"' + " clan " + '"' + a.getClan().getName() + '"' + " is: &e&o" + a.getClan().getId().toString());
				}
			});
			return true;
		}

		return true;
	}
}
