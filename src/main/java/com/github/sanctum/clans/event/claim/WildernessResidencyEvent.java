package com.github.sanctum.clans.event.claim;

import com.github.sanctum.clans.construct.api.ClaimActionEngine;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.event.player.PlayerEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.event.LabyrinthVentCall;
import com.github.sanctum.panther.annotation.Ordinal;
import java.text.MessageFormat;

import org.jetbrains.annotations.NotNull;

/**
 * Called when a user is within wilderness
 */
public class WildernessResidencyEvent extends PlayerEvent {

	private final Claim previous;
	private final Claim.Resident resident;

	public WildernessResidencyEvent(@NotNull Claim.Resident res) {
		super(res.getPlayer().getUniqueId(), false);
		this.resident = res;
		// receive now leaving message
		if (res.getInfo().getLastKnown() != null) {
			this.previous = res.getInfo().getLastKnown();
		} else {
			this.previous = res.getInfo().getCurrent();
		}
	}

	public Claim getPreviousClaim() {
		return previous;
	}

	@Override
	public Clan getClan() {
		if (getPreviousClaim() == null) return null;
		return ((Clan)getPreviousClaim().getHolder());
	}

	public ClaimActionEngine getClaimEngine() {
		return Claim.ACTION;
	}

	@Ordinal
	void sendNotification() {
		String previousClan = "Disbanded";
		if (getClan() != null) {
			previousClan = ((Clan) resident.getInfo().getLastKnown().getHolder()).getName();
		}
		WildernessNotificationFormatEvent pre = new WildernessNotificationFormatEvent(previous, getPlayer());
		if (getClan() != null) {
			pre.setTitle(MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.land-claiming.wilderness.title"), previousClan));
			pre.setSubTitle(MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.land-claiming.wilderness.sub-title"), previousClan));
		} else {
			pre.setTitle("&4&nWilderness");
			pre.setSubTitle("&7&oOwned by no-one.");
		}
		pre.addMessage(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.land-claiming.wilderness.message"));
		WildernessNotificationFormatEvent event = new LabyrinthVentCall<>(pre).run();
		if (event.isTitlesAllowed()) {
			if (!LabyrinthProvider.getInstance().isLegacy()) {
				getPlayer().sendTitle(getClaimEngine().color(event.getTitle()), getClaimEngine().color(event.getSubTitle()), 10, 25, 10);
			} else {
				getPlayer().sendTitle(getClaimEngine().color(event.getTitle()), getClaimEngine().color(event.getSubTitle()));
			}
		}
		if (event.isMessagesAllowed()) {
			for (String m : event.getMessages()) {
				getClaimEngine().sendMessage(getPlayer(), MessageFormat.format(m, previousClan));
			}
		}
	}

}
