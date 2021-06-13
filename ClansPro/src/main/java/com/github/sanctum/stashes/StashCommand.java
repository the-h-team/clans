package com.github.sanctum.stashes;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.Resident;
import com.github.sanctum.clans.util.events.command.CommandHelpInsertEvent;
import com.github.sanctum.clans.util.events.command.CommandInsertEvent;
import com.github.sanctum.clans.util.events.command.TabInsertEvent;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.link.ClanVentBus;
import com.github.sanctum.stashes.events.StashOpenEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

public class StashCommand extends StashListener implements Listener {

	public StashCommand() {

		ClanVentBus.subscribe(CommandInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			Player p = e.getSender();
			int length = e.getArgs().length;
			String[] args = e.getArgs();
			if (length == 1) {
				if (args[0].equalsIgnoreCase("stash")) {
					if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
						Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
						if (ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
							Resident r = Claim.getResident(p);
							if (e.getUtil().getRankPower(p.getUniqueId()) < ClansAPI.getData().getMain().getConfig().getInt("Addon.Stashes.clearance")) {
								e.getUtil().sendMessage(p, DefaultClan.action.noClearance());
								e.setReturn(true);
								return;
							}
							if (!r.getCurrent().getClan().getId().toString().equals(clan.getId().toString())) {
								e.getUtil().sendMessage(p, "&c&oYou can only view the stash @ the clan base.");
								e.setReturn(true);
								return;
							}
							if (clan.getBase() == null) {
								e.getUtil().sendMessage(p, "&c&oYou need to set a clan base. You will then gain access to the stash.");
								e.setReturn(true);
								return;
							}
							if (!r.getCurrent().getChunk().equals(clan.getBase().getChunk())) {
								e.getUtil().sendMessage(p, "&c&oYou can only view the stash @ the clan base.");
								e.setReturn(true);
								return;
							}
							String name = clan.getName();
							Inventory s = StashContainer.getStash(name);
							StashOpenEvent event = new StashOpenEvent(clan, p, s, s.getViewers());
							if (!event.isCancelled()) {
								event.open();
							}
						} else {
							e.getUtil().sendMessage(p, "&c&oYou can only view the stash within owned land.");
							e.setReturn(true);
							return;
						}
					} else {
						e.stringLibrary().sendMessage(p, e.stringLibrary().notInClan());
						e.setReturn(true);
						return;
					}
					e.setReturn(true);
				}
			}

		});

		ClanVentBus.subscribe(TabInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			if (!e.getArgs(1).contains("stash")) {
				e.add(1, "stash");
			}

		});

		ClanVentBus.subscribe(CommandHelpInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> e.insert("&7|&e) &6/clan &fstash"));

	}


}
