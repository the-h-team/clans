package com.github.sanctum.clans.events.core;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.actions.ClaimAction;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.impl.Resident;
import com.github.sanctum.clans.events.ClanEventBuilder;
import com.github.sanctum.labyrinth.library.TimeWatch;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class WildernessInhabitantEvent extends ClanEventBuilder {

	private final HashMap<String, String> titleContext = new HashMap<>();

	private final Player p;

	private String lastTime = null;

	private boolean titlesAllowed = DataManager.titlesAllowed();

	public WildernessInhabitantEvent(Player p) {
		this.p = p;
		if (ClansAPI.getData().RESIDENTS.stream().anyMatch(r -> r.getPlayer().getName().equals(p.getName()))) {
			for (Resident res : ClansAPI.getData().RESIDENTS) {
				if (res.getPlayer().getName().equals(p.getName())) {
					// receive now leaving message
					if (!ClansAPI.getData().INHABITANTS.contains(p)) {
						if (titlesAllowed) {
							try {
								titleContext.put("W-TITLE", MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Clans.land-claiming.wilderness.title"), res.getLastKnown().getClan().getName()));
								titleContext.put("W-SUB-TITLE", MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Clans.land-claiming.wilderness.sub-title"), res.getLastKnown().getClan().getName()));
								p.sendTitle(getClaimUtil().color(titleContext.get("W-TITLE")), getClaimUtil().color(titleContext.get("W-SUB-TITLE")), 10, 25, 10);
							} catch (NullPointerException e) {
								titleContext.put("W-TITLE", MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Clans.land-claiming.wilderness.title"), "Un-claimed"));
								titleContext.put("W-SUB-TITLE", MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Clans.land-claiming.wilderness.sub-title"), "Un-claimed"));
								p.sendTitle(getClaimUtil().color(titleContext.get("W-TITLE")), getClaimUtil().color(titleContext.get("W-SUB-TITLE")), 10, 25, 10);
								TimeWatch.Recording recording = TimeWatch.Recording.from(res.timeActiveInMillis());
								lastTime = recording.getDays() + "d" + recording.getHours() + "hr" + recording.getMinutes() + "m" + recording.getSeconds() + "s";
								ClansAPI.getData().RESIDENTS.remove(res);
								break;
							}
						}
						if (ClansAPI.getData().getEnabled("Clans.land-claiming.send-messages")) {
							getClaimUtil().sendMessage(p, MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Clans.land-claiming.wilderness.message"), res.getLastKnown().getClan().getName()));
						}
						ClansAPI.getData().INHABITANTS.add(p);
					}
					TimeWatch.Recording recording = TimeWatch.Recording.from(res.timeActiveInMillis());
					lastTime = recording.getDays() + "d" + recording.getHours() + "hr" + recording.getMinutes() + "m" + recording.getSeconds() + "s";
					ClansAPI.getData().RESIDENTS.remove(res);
					break;
				}
			}
		}
	}

	{
		if (!titleContext.containsKey("W-TITLE") || !titleContext.containsKey("W-SUB-TITLE")) {
			titleContext.put("W-TITLE", "&4&nWilderness");
			titleContext.put("W-SUB-TITLE", "&7&oOwned by no-one.");
		}
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

	public Player getPlayer() {
		return p;
	}

	public Optional<String> getTimeActive() {
		return Optional.ofNullable(this.lastTime);
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
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
