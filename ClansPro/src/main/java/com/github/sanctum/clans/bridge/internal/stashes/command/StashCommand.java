package com.github.sanctum.clans.bridge.internal.stashes.command;

import com.github.sanctum.clans.bridge.internal.StashesAddon;
import com.github.sanctum.clans.bridge.internal.stashes.events.StashOpenEvent;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.impl.Resident;
import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StashCommand extends ClanSubCommand {
	public StashCommand(String label) {
		super(label);
	}

	@Override
	public boolean player(Player p, String label, String[] args) {

		int length = args.length;
		if (length == 0) {
			if (ClansAPI.getInstance().getAssociate(p.getUniqueId()).isPresent()) {
				Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).get();
				Clan clan = associate.getClan();
				if (ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
					Resident r = Claim.getResident(p);
					if (!Clearance.MANAGE_STASH.test(associate)) {
						Clan.ACTION.sendMessage(p, Clan.ACTION.noClearance());
						return true;
					}
					if (!((Clan) r.getCurrent().getHolder()).getId().toString().equals(clan.getId().toString())) {
						Clan.ACTION.sendMessage(p, "&c&oYou can only view the stash @ the clan base.");
						return true;
					}
					if (clan.getBase() == null) {
						Clan.ACTION.sendMessage(p, "&c&oYou need to set a clan base. You will then gain access to the stash.");
						return true;
					}
					if (!r.getCurrent().getChunk().equals(clan.getBase().getChunk())) {
						Clan.ACTION.sendMessage(p, "&c&oYou can only view the stash @ the clan base.");
						return true;
					}
					String name = clan.getName();
					Menu s = StashesAddon.getStash(name);
					StashOpenEvent event = new StashOpenEvent(clan, p, s, s.getInventory().getElement().getViewers());
					if (!event.isCancelled()) {
						event.open();
					}
				} else {
					Clan.ACTION.sendMessage(p, "&c&oYou can only view the stash within owned land.");
					return true;
				}
			} else {
				Clan.ACTION.sendMessage(p, Clan.ACTION.notInClan());
				return true;
			}
			return true;
		}

		return true;
	}

	@Override
	public boolean console(CommandSender sender, String label, String[] args) {
		return false;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		if (args.length == 1) {
			return getBaseCompletion(args);
		}
		return null;
	}
}
