package com.github.sanctum.clans.util.listener;

import com.github.sanctum.clans.gui.ClanEditOperation;
import com.github.sanctum.clans.gui.MemberEditOperation;
import com.github.sanctum.clans.gui.UI;
import com.github.sanctum.clans.util.events.command.CommandInsertEvent;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.link.ClanVentBus;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class MenuEventListener implements Listener {

	public MenuEventListener() {
		ClanVentBus.subscribe(CommandInsertEvent.class, Vent.Priority.MEDIUM, (e, subscription) -> {

			String[] args = e.getArgs();
			Player p = e.getSender();
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("respond")) {
					if (UI.getClanEditOption(p.getUniqueId()) != null) {
						ClanEditOperation.Option type = UI.getClanEditOption(p.getUniqueId());
						ClanEditOperation gui = UI.getClanEditOperation(p.getUniqueId());
						switch (type) {
							case CLOSE:
								if (args[1].equalsIgnoreCase("confirm")) {
									gui.execute();
								} else {
									e.getUtil().sendMessage(p, "&c&oFailed to confirm clan deletion.");
								}
								break;
							case PASSWORD:
							case TAG:
							case COLOR:
							case CLAIMS_GIVE:
							case MONEY_GIVE:
							case POWER_GIVE:
							case CLAIMS_TAKE:
							case MONEY_TAKE:
							case POWER_TAKE:
								gui.setContext(args[1]);
								gui.execute();
								break;
						}
					} else {
						if (UI.getMemberEditOption(p.getUniqueId()) == null) {
							e.getUtil().sendMessage(p, "&c&oYou have no known edits being processed.");
						} else {
							MemberEditOperation.Option type = UI.getMemberEditOption(p.getUniqueId());
							MemberEditOperation gui = UI.getMemberEditOperation(p.getUniqueId());
							switch (type) {
								case NICKNAME:
								case SWITCH_CLANS:
									gui.setContext(args[1]);
									gui.execute();
									break;
								case PROMOTE:
								case KICK:
								case DEMOTE:
									gui.execute();
									break;
							}
						}
					}
					UI.clearOperations(p.getUniqueId());
					e.setReturn(true);
					return;
				}
			}
			if (args.length >= 2) {
				if (args[0].equalsIgnoreCase("respond")) {
					if (UI.getClanEditOption(p.getUniqueId()) != null) {
						ClanEditOperation.Option type = UI.getClanEditOption(p.getUniqueId());
						ClanEditOperation gui = UI.getClanEditOperation(p.getUniqueId());
						if (type == ClanEditOperation.Option.DESCRIPTION) {
							StringBuilder msg = new StringBuilder();
							for (int i = 1; i < args.length; i++)
								msg.append(args[i]).append(" ");
							int stop = msg.length() - 1;
							gui.setContext(msg.substring(0, stop));
							gui.execute();
						}
					} else {
						if (UI.getMemberEditOption(p.getUniqueId()) == null) {
							e.getUtil().sendMessage(p, "&c&oYou have no known clan edits being processed.");
						} else {
							MemberEditOperation.Option type = UI.getMemberEditOption(p.getUniqueId());
							MemberEditOperation gui = UI.getMemberEditOperation(p.getUniqueId());
							if (type == MemberEditOperation.Option.BIO) {
								StringBuilder msg = new StringBuilder();
								for (int i = 1; i < args.length; i++)
									msg.append(args[i]).append(" ");
								int stop = msg.length() - 1;
								gui.setContext(msg.substring(0, stop));
								gui.execute();
							}
						}
					}
					UI.clearOperations(p.getUniqueId());
					e.setReturn(true);
				}
			}

		});
	}


}
