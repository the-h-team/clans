package com.github.sanctum.clans.event.claim;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.actions.ClaimAction;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.impl.Resident;
import com.github.sanctum.clans.event.player.PlayerEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.library.TimeWatch;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

/**
 * Called when a user is within wilderness
 */
public class WildernessInhabitantEvent extends PlayerEvent {

	private final HashMap<String, String> titleContext = new HashMap<>();

	private final long time;

	private boolean titlesAllowed = DataManager.isTitlesAllowed();

	public WildernessInhabitantEvent(Player p) {
		super(p.getUniqueId(), false);
		if (!titleContext.containsKey("W-TITLE") || !titleContext.containsKey("W-SUB-TITLE")) {
			titleContext.put("W-TITLE", "&4&nWilderness");
			titleContext.put("W-SUB-TITLE", "&7&oOwned by no-one.");
		}
		if (ClansAPI.getDataInstance().getResident(p) != null) {
			for (Resident res : ClansAPI.getDataInstance().getResidents()) {
				if (res.getPlayer().getName().equals(p.getName())) {
					// receive now leaving message
					if (!ClansAPI.getDataInstance().isInWild(p)) {
						if (titlesAllowed) {
							try {
								titleContext.put("W-TITLE", MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.land-claiming.wilderness.title"), res.getLastKnown().getClan().getName()));
								titleContext.put("W-SUB-TITLE", MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.land-claiming.wilderness.sub-title"), res.getLastKnown().getClan().getName()));
								if (!LabyrinthProvider.getInstance().isLegacy()) {
									p.sendTitle(getClaimUtil().color(titleContext.get("W-TITLE")), getClaimUtil().color(titleContext.get("W-SUB-TITLE")), 10, 25, 10);
								} else {
									p.sendTitle(getClaimUtil().color(titleContext.get("W-TITLE")), getClaimUtil().color(titleContext.get("W-SUB-TITLE")));
								}
							} catch (NullPointerException e) {
								titleContext.put("W-TITLE", MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.land-claiming.wilderness.title"), "Un-claimed"));
								titleContext.put("W-SUB-TITLE", MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.land-claiming.wilderness.sub-title"), "Un-claimed"));
								if (!LabyrinthProvider.getInstance().isLegacy()) {
									p.sendTitle(getClaimUtil().color(titleContext.get("W-TITLE")), getClaimUtil().color(titleContext.get("W-SUB-TITLE")), 10, 25, 10);
								} else {
									p.sendTitle(getClaimUtil().color(titleContext.get("W-TITLE")), getClaimUtil().color(titleContext.get("W-SUB-TITLE")));
								}
								ClansAPI.getDataInstance().removeClaimResident(res);
								break;
							}
						}
						if (ClansAPI.getDataInstance().isTrue("Clans.land-claiming.send-messages")) {
							getClaimUtil().sendMessage(p, MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.land-claiming.wilderness.message"), res.getLastKnown().getClan().getName()));
						}
						ClansAPI.getDataInstance().addWildernessInhabitant(p);
					}
					ClansAPI.getDataInstance().removeClaimResident(res);
					break;
				}
			}
		}
		time = System.currentTimeMillis();
	}

	public void setTitlesAllowed(boolean b) {
		this.titlesAllowed = b;
	}

	public void setWildernessTitle(String title, String subtitle) {
		titleContext.put("W-TITLE", title);
		titleContext.put("W-SUB-TITLE", subtitle);
	}

	public String getWildernessTitle() {
		return titleContext.get("W-TITLE");
	}

	public String getWildernessSubTitle() {
		return titleContext.get("W-SUB-TITLE");
	}

	public TimeWatch.Recording getTimeActive() {
		return TimeWatch.Recording.subtract(this.time);
	}

	@Override
	public Clan getClan() {
		return null;
	}

	public boolean isTitlesAllowed() {
		return titlesAllowed;
	}

	public List<Chunk> getSurroundingChunks(Chunk centerChunk, int radius) {
		List<Chunk> chunks = new ArrayList<>();
		for (int x = centerChunk.getX() - radius; x < centerChunk.getX() + radius; x++) {
			for (int z = centerChunk.getZ() - radius; z < centerChunk.getZ() + radius; z++) {
				Chunk chunk = centerChunk.getWorld().getChunkAt(x, z);
				chunks.add(chunk);
			}
		}
		return chunks;
	}

	public ClaimAction getClaimUtil() {
		return Claim.ACTION;
	}

}
