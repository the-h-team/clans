package com.github.sanctum.clans.bridge.a;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.impl.Resident;
import com.github.sanctum.clans.events.command.CommandHelpInsertEvent;
import com.github.sanctum.clans.events.command.CommandInsertEvent;
import com.github.sanctum.clans.events.command.TabInsertEvent;
import com.github.sanctum.clans.internal.stashes.StashContainer;
import com.github.sanctum.clans.internal.stashes.StashListener;
import com.github.sanctum.clans.internal.stashes.events.StashOpenEvent;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.HUID;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class StashesAddon extends ClanAddon {

	@Override
	public boolean persist() {
		return ClansAPI.getData().getEnabled("Addon." + getName() + ".enabled");
	}

	@Override
	public HUID getId() {
		return super.getId();
	}

	@Override
	public String getName() {
		return "Stashes";
	}

	@Override
	public String getDescription() {
		return "An addon that compliments w/ a limited private storage space.";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String[] getAuthors() {
		return new String[]{"Hempfest", "ms5984"};
	}

	@Override
	public void onLoad() {
		register(new StashListener());
	}

	@Override
	public void onEnable() {

		ClanVentBus.subscribe(CommandInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			ClanAddon cycle = ClanAddonQuery.getAddon("Stashes");

			if (cycle != null && !cycle.isActive()) {
				subscription.remove();
				return;
			}

			Player p = e.getSender();
			int length = e.getArgs().length;
			String[] args = e.getArgs();
			if (length == 1) {
				if (args[0].equalsIgnoreCase("stash")) {
					if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
						ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).get();
						Clan clan = associate.getClan();
						if (ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
							Resident r = Claim.getResident(p);
							if (associate.getPriority().toInt() < ClansAPI.getData().getMain().getConfig().getInt("Addon.Stashes.clearance")) {
								e.getUtil().sendMessage(p, Clan.ACTION.noClearance());
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

			ClanAddon cycle = ClanAddonQuery.getAddon("Stashes");

			if (cycle != null && !cycle.isActive()) {
				subscription.remove();
				return;
			}

			if (!e.getArgs(1).contains("stash")) {
				e.add(1, "stash");
			}

		});

		ClanVentBus.subscribe(CommandHelpInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {
			ClanAddon cycle = ClanAddonQuery.getAddon("Stashes");

			if (cycle != null && !cycle.isActive()) {
				subscription.remove();
				return;
			}

			e.insert("&7|&e) &6/clan &fstash")
			;
		});

	}

	@Override
	public void onDisable() {

	}

}
