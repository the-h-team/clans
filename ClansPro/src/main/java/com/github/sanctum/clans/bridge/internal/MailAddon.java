package com.github.sanctum.clans.bridge.internal;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.internal.mail.MailCommand;
import com.github.sanctum.clans.bridge.internal.mail.controller.MailListener;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.event.command.CommandInformationAdaptEvent;
import com.github.sanctum.labyrinth.event.custom.Vent;
import org.jetbrains.annotations.NotNull;

public class MailAddon extends ClanAddon {

	@Override
	public boolean isPersistent() {
		return ClansAPI.getDataInstance().isTrue("Addon." + getName() + ".enabled");
	}

	@Override
	public @NotNull String getName() {
		return "Mail";
	}

	@Override
	public @NotNull String getDescription() {
		return "A new pro addon! Send mail & gifts to other clans!";
	}

	@Override
	public @NotNull String getVersion() {
		return "1.0";
	}

	@Override
	public String[] getAuthors() {
		return new String[]{"Hempfest"};
	}

	@Override
	public void onLoad() {

		getContext().stage(new MailListener());
		getContext().stage(new MailCommand("mail"));

	}


	@Override
	public void onEnable() {

		ClanVentBus.subscribe(CommandInformationAdaptEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			ClanAddon cycle = ClanAddonQuery.getAddon("Mail");

			if (cycle != null && !cycle.getContext().isActive()) {
				subscription.remove();
				return;
			}

			e.insert("&7|&e)&6&o /clan &fmail", "&7|&e)&6&o /clan &fgift");

		});

	}

	@Override
	public void onDisable() {

	}
}
