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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ClaimResidentEvent extends ClanEventBuilder {

	private final HashMap<String, String> titleContext = new HashMap<>();

	private final Player p;

	private final Resident r;

	private final Claim claim;

	private boolean titlesAllowed = DataManager.titlesAllowed();

	private boolean cancelled;

	public ClaimResidentEvent(Player p) {
		this.p = p;
		this.claim = Claim.from(p.getLocation());
		if (ClansAPI.getData().RESIDENTS.stream().noneMatch(r -> r.getPlayer().getName().equals(p.getName()))) {
			Resident res = new Resident(p);
			res.updateLastKnown(this.claim);
			r = res;
			ClansAPI.getData().RESIDENTS.add(r);
		} else {
			r = ClansAPI.getData().RESIDENTS.stream().filter(r -> r.getPlayer().getName().equals(p.getName())).findFirst().orElse(null);
		}
	}

	{
		if (!titleContext.containsKey("TITLE") || !titleContext.containsKey("SUB-TITLE")) {
			titleContext.put("TITLE", "&3&oClaimed land");
			titleContext.put("SUB-TITLE", "&7Owned by: &b%s");
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

	public void setClaimTitle(String title, String subtitle) {
		titleContext.put("TITLE", title);
		titleContext.put("SUB-TITLE", subtitle);
	}

	public String getClaimTitle() {
		return titleContext.get("TITLE");
	}

	public String getClaimSubTitle() {
		return titleContext.get("SUB-TITLE");
	}

	public Claim getClaim() {
		return claim;
	}

	public Collection<Chunk> getChunksAroundPlayer(int xoff, int yoff, int zoff) {
		int[] offset = {xoff, yoff, zoff};

		World world = p.getLocation().getWorld();
		int baseX = p.getLocation().getChunk().getX();
		int baseZ = p.getLocation().getChunk().getZ();

		Collection<Chunk> chunksAroundPlayer = new HashSet<>();
		for (int x : offset) {
			for (int z : offset) {
				Chunk chunk = world.getChunkAt(baseX + x, baseZ + z);
				chunksAroundPlayer.add(chunk);
			}
		}
		return chunksAroundPlayer;
	}

	public boolean isTitleAllowed() {
		return titlesAllowed;
	}

	public Resident getResident() {
		return r;
	}

	public ClanAction getUtil() {
		return DefaultClan.action;
	}

	@Override
	public StringLibrary stringLibrary() {
		return DefaultClan.action;
	}

	public ClaimAction getClaimUtil() {
		return Claim.action;
	}

	public void playTitle() {
		String clanName = getUtil().getClanTag(getClaim().getOwner());
		String color;
		if (getUtil().getClanID(p.getUniqueId()) != null) {
			color = getUtil().clanRelationColor(getUtil().getClanID(p.getUniqueId()), getClaim().getOwner());
		} else {
			color = "&f&o";
		}
		if (titlesAllowed) {
			titleContext.put("TITLE", MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Clans.land-claiming.in-land.title"), clanName));
			titleContext.put("SUB-TITLE", MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Clans.land-claiming.in-land.sub-title"), clanName));
			p.sendTitle(getClaimUtil().color(titleContext.get("TITLE")), getClaimUtil().color(titleContext.get("SUB-TITLE")), 10, 25, 10);
		}
		if (ClansAPI.getData().getEnabled("Clans.land-claiming.send-messages")) {
			getClaimUtil().sendMessage(p, MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Clans.land-claiming.in-land.message"), clanName));
		}
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

}
