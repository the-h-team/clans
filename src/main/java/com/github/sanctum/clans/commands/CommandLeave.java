package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Consultant;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandLeave extends ClanSubCommand {
	public CommandLeave() {
		super("leave");
	}

	@Override
	public boolean console(CommandSender sender, String label, String[] args) {
		ClansAPI api = ClansAPI.getInstance();
		Consultant consultant = api.getConsultant();
		if (consultant instanceof Clan.Associate) {
			Clan.Associate associate = (Clan.Associate) consultant;
			TaskScheduler.of(() -> associate.getClan().remove()).schedule();
			sender.sendMessage(StringUtils.use("&cThe default server clan has been removed.").translate());
		} else {
			sender.sendMessage(StringUtils.use("&cThe default server clan doesn't exist.").translate());
		}
		return true;
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("leave")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("leave")));
				return true;
			}
			Clan.ACTION.remove(p.getUniqueId(), false).deploy();
			return true;
		}


		return true;
	}
}
