package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.construct.impl.Resident;
import com.github.sanctum.clans.events.core.ClaimResidentEvent;
import com.github.sanctum.clans.events.core.WildernessInhabitantEvent;
import com.github.sanctum.labyrinth.data.Configurable;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.data.Node;
import com.github.sanctum.labyrinth.task.JoinableRepeatingTask;
import com.github.sanctum.labyrinth.task.Schedule;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ClaimManager {

	private final FileManager regions;
	private final JoinableRepeatingTask<Player> task;

	public ClaimManager() {
		switch (((ClansJavaPlugin) ClansAPI.getInstance().getPlugin()).TYPE) {
			case YAML:
				this.regions = DataManager.FileType.MISC_FILE.get("Regions", "Configuration");
				break;
			case JSON:
				this.regions = ClansAPI.getInstance().getFileList().find("regions", "Configuration", FileType.JSON);
				break;
			default:
				this.regions = DataManager.FileType.MISC_FILE.get("Regions", "Configuration");
				break;
		}

		this.task = JoinableRepeatingTask.create(18, ClansAPI.getInstance().getPlugin(), p -> {
			ClansAPI API = ClansAPI.getInstance();

			Clan.Associate associate = API.getAssociate(p).orElse(null);

			if (associate != null) {
				for (ClanCooldown clanCooldown : ClansAPI.getData().COOLDOWNS) {
					if (clanCooldown.getId().equals(p.getUniqueId().toString())) {
						if (clanCooldown.isComplete()) {
							Schedule.sync(() -> ClanCooldown.remove(clanCooldown)).run();
							Clan.ACTION.sendMessage(p, MessageFormat.format(ClansAPI.getData().getMessageResponse("cooldown-expired"), clanCooldown.getAction().replace("Clans:", "")));
						}
					}
				}

			}

			if (Claim.ACTION.isEnabled()) {

				if (!API.getClaimManager().isInClaim(p.getLocation())) {

					ClanVentBus.call(new WildernessInhabitantEvent(p));

				} else {
					ClaimResidentEvent event = ClanVentBus.call(new ClaimResidentEvent(p));
					if (!event.isCancelled()) {
						ClansAPI.getData().INHABITANTS.remove(event.getResident().getPlayer());
						Resident r = event.getResident();
						Claim current = event.getClaim();
						if (current.isActive()) {
							if (ClansAPI.getInstance().getClanName(current.getOwner()) == null) {
								current.remove();
								return;
							}
							Claim lastKnown = r.getLastKnown();
							if (!current.getId().equals(lastKnown.getId())) {
								if (r.hasProperty(Resident.Property.NOTIFIED)) {
									if (!lastKnown.getOwner().equals(r.getCurrent().getOwner())) {
										r.setProperty(Resident.Property.TRAVERSED, true);
										r.setLastKnownClaim(event.getClaim());
										r.setTimeEntered(System.currentTimeMillis());
									}
								}
							}
							if (!r.hasProperty(Resident.Property.NOTIFIED)) {
								event.sendNotification();
								r.setProperty(Resident.Property.NOTIFIED, true);
							} else {
								if (r.hasProperty(Resident.Property.TRAVERSED)) {
									r.setProperty(Resident.Property.TRAVERSED, false);
									r.setTimeEntered(System.currentTimeMillis());
									event.sendNotification();
								}
							}
						}
					}
				}
			}
		});
	}

	public boolean isInClaim(Location location) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans().list()) {
			boolean test = Arrays.stream(clan.getOwnedClaims()).anyMatch(c -> location.getChunk().getX() == c.getPos()[0] && location.getChunk().getZ() == c.getPos()[1] && location.getWorld().getName().equals(c.getKey()[2]));
			if (test) {
				return test;
			}
		}
		return false;
	}

	public boolean isInClaim(int x, int z, String world) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans().list()) {
			boolean test = Arrays.stream(clan.getOwnedClaims()).anyMatch(c -> x == c.getPos()[0] && z == c.getPos()[1] && world.equals(c.getKey()[2]));
			if (test) {
				return test;
			}
		}
		return false;
	}

	public boolean isInClaim(Chunk chunk) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans().list()) {
			boolean test = Arrays.stream(clan.getOwnedClaims()).anyMatch(c -> chunk.getX() == c.getPos()[0] && chunk.getZ() == c.getPos()[1] && chunk.getWorld().getName().equals(c.getKey()[2]));
			if (test) {
				return test;
			}
		}
		return false;
	}

	public String getId(Location location) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans().list()) {
			String id = Arrays.stream(clan.getOwnedClaims()).filter(c -> location.getChunk().getX() == c.getPos()[0] && location.getChunk().getZ() == c.getPos()[1] && location.getWorld().getName().equals(c.getKey()[2])).map(Claim::getId).findFirst().orElse(null);
			if (id != null) {
				return id;
			}
		}
		return null;
	}

	public String getId(int x, int z, String world) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans().list()) {
			String id = Arrays.stream(clan.getOwnedClaims()).filter(c -> x == c.getPos()[0] && z == c.getPos()[1] && world.equals(c.getKey()[2])).map(Claim::getId).findFirst().orElse(null);
			if (id != null) {
				return id;
			}
		}
		return null;
	}

	public Claim getClaim(String claimID) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans().list()) {
			Claim claim = Arrays.stream(clan.getOwnedClaims()).filter(c -> c.getId().equals(claimID)).findFirst().orElse(null);
			if (claim != null) {
				return claim;
			}
		}
		return null;
	}

	public FileManager getFile() {
		return regions;
	}

	public Set<Claim> getClaims() {
		return ClansAPI.getInstance().getClanManager().getClans()
				.map(Clan::getOwnedClaims)
				.map(cl -> (Set<Claim>) new HashSet<>(Arrays.asList(cl)))
				.reduce((claims1, claims2) -> {
					Set<Claim> c = new HashSet<>(claims1);
					c.addAll(claims2);
					return c;
				}).orElse(new HashSet<>());
	}

	public boolean load(Claim claim) {
		if (claim.getClan() instanceof DefaultClan) {
			DefaultClan clan = (DefaultClan) claim.getClan();
			clan.addClaim(claim);
			return true;
		}
		return false;
	}

	public JoinableRepeatingTask<Player> getTask() {
		return this.task;
	}

	/**
	 * Clear and re-load all persistent claims.
	 */
	public int refresh() {

		for (Clan c : ClansAPI.getInstance().getClanManager().getClans().list()) {
			if (c instanceof DefaultClan) {
				DefaultClan clan = (DefaultClan) c;
				clan.resetClaims();
			}
		}
		int loaded = 0;
		Configurable d = getFile().getRoot();
		for (String clan : d.getKeys(false)) {
			Node cl = d.getNode(clan);
			Node claims = cl.getNode("Claims");
			for (String claimID : claims.getKeys(false)) {
				Node claim = claims.getNode(claimID);
				int x = claim.getNode("X").toPrimitive().getInt();
				int z = claim.getNode("Z").toPrimitive().getInt();
				String w = claim.getNode("World").toPrimitive().getString();
				String[] ID = {clan, claimID, w};
				int[] pos = {x, z};
				Claim c = new Claim(ID, pos, true);
				load(c);
				loaded++;
				if (!d.isBoolean(clan + ".Claims." + claimID + ".active")) {
					getFile().write(t -> t.set(clan + ".Claims." + claimID + ".active", true));
				}
			}
		}
		return loaded;
	}


}
