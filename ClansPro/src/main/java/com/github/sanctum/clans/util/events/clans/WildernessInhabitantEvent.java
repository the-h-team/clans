package com.github.sanctum.clans.util.events.clans;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClaimAction;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.Resident;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.data.DataManager;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WildernessInhabitantEvent extends ClanEventBuilder implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final HashMap<String, String> titleContext = new HashMap<>();

	private final Player p;

	private boolean titlesAllowed = DataManager.titlesAllowed();

	private boolean cancelled;

	public WildernessInhabitantEvent(Player p) {
		this.p = p;
		if (ClansAPI.getData().RESIDENTS.stream().anyMatch(r -> r.getPlayer().getName().equals(p.getName()))) {
			for (Resident res : ClansAPI.getData().RESIDENTS) {
				if (res.getPlayer().getName().equals(p.getName())) {
					// send now leaving message
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
								ClansAPI.getData().RESIDENTS.remove(res);
								break;
							}
						}
						if (ClansAPI.getData().getEnabled("Clans.land-claiming.send-messages")) {
							getClaimUtil().sendMessage(p, MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Clans.land-claiming.wilderness.message"), res.getLastKnown().getClan().getName()));
						}
						ClansAPI.getData().INHABITANTS.add(p);
					}
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

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean b) {
		this.cancelled = b;
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

	// TODO: if this is always null why need method?
	public Claim getClaim() {
		return null;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Player getPlayer() {
		return p;
	}

	public boolean isTitlesAllowed() {
		return titlesAllowed;
	}

	public ClanAction getUtil() {
		return DefaultClan.action;
	}

	@Override
	public StringLibrary stringLibrary() {
		return new StringLibrary();
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
		return Claim.action;
	}

}
