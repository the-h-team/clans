package com.github.sanctum.clans.event.claim;

import com.github.sanctum.clans.ClaimManager;
import com.github.sanctum.clans.ResidentManager;
import com.github.sanctum.clans.model.backend.ClaimFileBackend;
import com.github.sanctum.clans.model.Claim;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.impl.entity.DefaultClaimResident;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.event.LabyrinthVentCall;
import com.github.sanctum.panther.annotation.Ordinal;
import com.github.sanctum.panther.util.HUID;
import java.text.MessageFormat;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a user is within clan owned claims.
 */
public class ClaimResidencyEvent extends ClaimEvent {

	private final Player p;
	private final Claim.Resident r;

	public ClaimResidencyEvent(@NotNull ClaimManager claimManager, @NotNull Player p) {
		super(claimManager.getClaim(p.getLocation()));
		this.p = p;
		ResidentManager residentManager = claimManager.getResidentManager();
		if (residentManager.getResident(p) == null) {
			Claim.Resident res = new DefaultClaimResident(p);
			res.getInfo().setLastKnown(getClaim());
			r = res;
			residentManager.load(r);
		} else {
			r = residentManager.getResident(p);
		}
	}

	@Override
	public Clan getClan() {
		return ((Clan) getClaim().getHolder());
	}

	@Override
	public @Nullable Clan.Associate getAssociate() {
		return ClansAPI.getInstance().getAssociate(getResident().getPlayer()).orElse(super.getAssociate());
	}

	@Override
	public Player getPlayer() {
		return this.p;
	}

	public Claim.Resident getResident() {
		return r;
	}

	public ClaimFileBackend getClaimEngine() {
		return Claim.ACTION;
	}

	@Ordinal
	void sendNotification() {
		String clanName = ClansAPI.getInstance().getClanManager().getClanName(HUID.parseID(getClaim().getOwner().getTag().getId()).toID());
		String color;
		if (ClansAPI.getInstance().getClanManager().getClanID(p.getUniqueId()) != null) {
			color = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).relate(getClan());
		} else {
			color = "&f&o";
		}
		ClaimNotificationFormatEvent pre = new ClaimNotificationFormatEvent(getResident().getInfo().getCurrent(), p);
		pre.setTitle(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.land-claiming.in-land.title"));
		pre.setSubTitle(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.land-claiming.in-land.sub-title"));
		pre.addMessage(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.land-claiming.in-land.message"));
		ClaimNotificationFormatEvent event = new LabyrinthVentCall<>(pre).run();
		if (event.isTitlesAllowed()) {
			if (!LabyrinthProvider.getInstance().isLegacy()) {
				if (event.getTitle() != null) {
					if (event.getSubTitle() != null) {
						p.sendTitle(getClaimEngine().color(MessageFormat.format(event.getTitle(), clanName, color)), getClaimEngine().color(MessageFormat.format(event.getSubTitle(), clanName, color)), 10, 25, 10);
					} else p.sendTitle(getClaimEngine().color(MessageFormat.format(event.getTitle(), clanName, color)), "", 10, 25, 10);
				} else {
					if (event.getSubTitle() != null) {
						p.sendTitle("", getClaimEngine().color(MessageFormat.format(event.getSubTitle(), clanName, color)), 10, 25, 10);
					}
				}
			} else {
				if (event.getTitle() != null) {
					if (event.getSubTitle() != null) {
						p.sendTitle(getClaimEngine().color(MessageFormat.format(event.getTitle(), clanName, color)), getClaimEngine().color(MessageFormat.format(event.getSubTitle(), clanName, color)));
					} else p.sendTitle(getClaimEngine().color(MessageFormat.format(event.getTitle(), clanName, color)), "");
				} else {
					if (event.getSubTitle() != null) {
						p.sendTitle("", getClaimEngine().color(MessageFormat.format(event.getSubTitle(), clanName, color)));
					}
				}
			}
		}
		if (event.isMessagesAllowed()) {
			for (String m : event.getMessages()) {
				getClaimEngine().sendMessage(p, MessageFormat.format(m, clanName, color));
			}
		}
	}

}
