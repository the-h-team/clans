package com.github.sanctum.link.cycles;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.events.command.CommandHelpInsertEvent;
import com.github.sanctum.clans.util.events.command.CommandInsertEvent;
import com.github.sanctum.clans.util.events.command.TabInsertEvent;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.link.ClanVentBus;
import com.github.sanctum.link.CycleList;
import com.github.sanctum.link.EventCycle;
import com.github.sanctum.map.MapCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;

public class MapCycle extends EventCycle {

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
		return "Map";
	}

	@Override
	public String getDescription() {
		return "Organized area mapping using chat!";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String[] getAuthors() {
		return new String[]{"ms5984"};
	}

	@Override
	public void onLoad() {
		register(new MapCommand());
	}

	@Override
	public void onEnable() {

		getServiceManager().unregisterAll(this);

		getServiceManager().register(ClansAPI.getData().getEnabled("Addon.Map.enhanced"), this, ServicePriority.High);

		ClanVentBus.subscribe(CommandHelpInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			EventCycle cycle = CycleList.getAddon("Map");

			if (cycle != null && !cycle.isActive()) {
				subscription.remove();
				return;
			}
			e.insert("&7|&e) &6/clan &fmap");
		});

		ClanVentBus.subscribe(TabInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			EventCycle cycle = CycleList.getAddon("Map");

			if (cycle != null && !cycle.isActive()) {
				subscription.remove();
				return;
			}

			if (!e.getArgs(1).contains("map")) {
				e.add(1, "map");
			}
			final String[] commandArgs = e.getCommandArgs();
			if (commandArgs.length > 0 && commandArgs[0].equalsIgnoreCase("map")) {
				if (!e.getArgs(2).contains("on")) {
					e.add(2, "on");
				}
				if (!e.getArgs(2).contains("off")) {
					e.add(2, "off");
				}
			}

		});

		ClanVentBus.subscribe(CommandInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			EventCycle cycle = CycleList.getAddon("Map");

			if (cycle != null && !cycle.isActive()) {
				subscription.remove();
				return;
			}

			final Player p = e.getSender();
			final String[] args = e.getArgs();
			final int length = args.length;
			if (length > 0 && args[0].equalsIgnoreCase("map")) {
				if (length == 1) {
					e.setReturn(true);
					MapCommand.sendMapCurrentLoc(p);
				} else {
					if (args[1].equalsIgnoreCase("on")) {
						// on logic
						if (!MapCommand.isToggled(p)) {
							DefaultClan.action.sendMessage(p, "&aMap enabled.");
							MapCommand.sendMapCurrentLoc(p);
							MapCommand.toggle(p);
						}
					} else if (args[1].equalsIgnoreCase("off")) {
						// off logic
						if (MapCommand.isToggled(p)) {
							DefaultClan.action.sendMessage(p, "&cMap disabled.");
							MapCommand.toggle(p);
						}
					} else {
						// receive usage
						return;
					}
					e.setReturn(true);
				}
			}

		});

	}

	@Override
	public void onDisable() {

		getServiceManager().unregisterAll(this);

		getServiceManager().register(false, this, ServicePriority.High);

	}
}
