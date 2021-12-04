package com.github.sanctum.clans.event.claim;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.actions.ClaimAction;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.impl.Resident;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.library.HUID;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a user is within clan owned claims.
 */
public class ClaimResidentEvent extends ClaimEvent {

	private final HashMap<String, String> titleContext = new HashMap<>();

	private final Player p;

	private final Resident r;

	private boolean titlesAllowed = DataManager.isTitlesAllowed();

	public ClaimResidentEvent(Player p) {
		super(ClansAPI.getInstance().getClaimManager().getClaim(p.getLocation()));
		this.p = p;
		if (ClansAPI.getDataInstance().getResident(p) == null) {
			Resident res = new Resident(p);
			res.setLastKnownClaim(getClaim());
			r = res;
			ClansAPI.getDataInstance().addClaimResident(r);
		} else {
			r = ClansAPI.getDataInstance().getResident(p);
		}
	}

	{
		if (!titleContext.containsKey("TITLE") || !titleContext.containsKey("SUB-TITLE")) {
			titleContext.put("TITLE", ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.land-claiming.in-land.title"));
			titleContext.put("SUB-TITLE", ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.land-claiming.in-land.sub-title"));
		}
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

	@Override
	public Clan getClan() {
		return ((Clan)getClaim().getHolder());
	}

	@Override
	public @Nullable Clan.Associate getAssociate() {
		return ClansAPI.getInstance().getAssociate(getResident().getPlayer()).orElse(super.getAssociate());
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

	public ClaimAction getClaimUtil() {
		return Claim.ACTION;
	}

	public void sendNotification() {
		String clanName = ClansAPI.getInstance().getClanManager().getClanName(HUID.fromString(getClaim().getOwner().getTag().getId()));
		String color;
		if (ClansAPI.getInstance().getClanManager().getClanID(p.getUniqueId()) != null) {
			color = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).relate(getClan());
		} else {
			color = "&f&o";
		}
		if (titlesAllowed) {
			if (!LabyrinthProvider.getInstance().isLegacy()) {
				p.sendTitle(getClaimUtil().color(MessageFormat.format(titleContext.get("TITLE"), clanName, color)), getClaimUtil().color(MessageFormat.format(titleContext.get("SUB-TITLE"), clanName, color)), 10, 25, 10);
			} else {
				p.sendTitle(getClaimUtil().color(MessageFormat.format(titleContext.get("TITLE"), clanName, color)), getClaimUtil().color(MessageFormat.format(titleContext.get("SUB-TITLE"), clanName, color)));
			}
		}
		if (ClansAPI.getDataInstance().isTrue("Clans.land-claiming.send-messages")) {
			getClaimUtil().sendMessage(p, MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.land-claiming.in-land.message"), clanName, color));
		}
	}

}
