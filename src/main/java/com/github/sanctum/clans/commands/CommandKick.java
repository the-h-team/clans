package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.clans.event.associate.AssociateKickAssociateEvent;
import java.text.MessageFormat;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class CommandKick extends ClanSubCommand {
	public CommandKick() {
		super("kick");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.kick.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("kick")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("kick")));
				return true;
			}
			lib.sendMessage(p, lib.commandKick());
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("kick")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("kick")));
				return true;
			}
			if (associate != null) {
				if (Clearance.KICK_MEMBERS.test(associate)) {
					UUID tid = Clan.ACTION.getId(args[0]).deploy();
					if (tid == null) {
						lib.sendMessage(p, lib.playerUnknown(args[0]));
						return true;
					}
					OfflinePlayer target = Bukkit.getOfflinePlayer(tid);
					Clan clan = associate.getClan();
					Clan.Associate member = clan.getMember(m -> m.getId().equals(target.getUniqueId()));
					if (member == null) {
						lib.sendMessage(p, lib.playerUnknown(args[0]));
						return true;
					}
					if (member.getPriority().toLevel() > associate.getPriority().toLevel()) {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}
					if (tid.equals(p.getUniqueId())) {
						lib.sendMessage(p, lib.nameInvalid(args[0]));
						return true;
					}
					AssociateKickAssociateEvent event = ClanVentBus.call(new AssociateKickAssociateEvent(member, associate));
					if (!event.isCancelled()) {
						member.remove();
						String format = MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("kick-out"), target.getName());
						String format1 = MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("kick-in"), p.getName());
						clan.broadcast(format);
						if (target.isOnline()) {
							lib.sendMessage(target.getPlayer(), format1);
						}
					}
				} else {
					lib.sendMessage(p, lib.noClearance());
					return true;
				}
			} else {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}
			return true;
		}


		return true;
	}
}
