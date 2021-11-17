package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.InvasiveEntity;
import com.github.sanctum.clans.construct.impl.DefaultAssociate;
import com.github.sanctum.clans.construct.impl.DefaultClaim;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.construct.impl.Resident;
import com.github.sanctum.clans.event.TimerEvent;
import com.github.sanctum.clans.event.claim.ClaimResidentEvent;
import com.github.sanctum.clans.event.claim.ClaimsLoadingProcedureEvent;
import com.github.sanctum.clans.event.claim.WildernessInhabitantEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.Configurable;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.data.Node;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.HUID;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class ClaimManager {

	private final FileManager regions;
	private final FlagManager flagManager;

	public ClaimManager() {
		this.flagManager = new FlagManager(this);
		if (JavaPlugin.getPlugin(ClansJavaPlugin.class).TYPE == FileType.YAML) {
			this.regions = ClansAPI.getInstance().getFileList().get("Regions", "Configuration", FileType.YAML);
		} else {
			this.regions = ClansAPI.getInstance().getFileList().get("regions", "Configuration", FileType.JSON);
		}

		ClanVentBus.subscribe(TimerEvent.class, Vent.Priority.HIGH, (e, subscription) -> {
			if (e.isAsynchronous()) return;
			Player p = e.getPlayer();
			ClansAPI API = ClansAPI.getInstance();

			if (Claim.ACTION.isEnabled()) {

				if (!API.getClaimManager().isInClaim(p.getLocation())) {

					WildernessInhabitantEvent wild = ClanVentBus.call(new WildernessInhabitantEvent(p));
					if (!wild.isCancelled()) {


						if (ClansAPI.getDataInstance().getResident(wild.getPlayer()) != null) {
							Resident res = ClansAPI.getDataInstance().getResident(p);
							// receive now leaving message
							if (!ClansAPI.getDataInstance().isInWild(p)) {
								if (wild.isTitlesAllowed()) {
									if (!LabyrinthProvider.getInstance().isLegacy()) {
										wild.getPlayer().sendTitle(wild.getClaimUtil().color(wild.getWildernessTitle()), wild.getClaimUtil().color(wild.getWildernessSubTitle()), 10, 25, 10);
									} else {
										wild.getPlayer().sendTitle(wild.getClaimUtil().color(wild.getWildernessTitle()), wild.getClaimUtil().color(wild.getWildernessSubTitle()));
									}
								}
								if (ClansAPI.getDataInstance().isTrue("Clans.land-claiming.send-messages")) {
									wild.getClaimUtil().sendMessage(p, MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.land-claiming.wilderness.message"), res.getLastKnown().getClan().getName()));
								}
								ClansAPI.getDataInstance().addWildernessInhabitant(p);
							}
							ClansAPI.getDataInstance().removeClaimResident(res);
						}

					}

				} else {
					ClaimResidentEvent event = ClanVentBus.call(new ClaimResidentEvent(p));
					if (!event.isCancelled()) {
						ClansAPI.getDataInstance().removeWildernessInhabitant(event.getResident().getPlayer());
						Resident r = event.getResident();
						Claim current = event.getClaim();
						if (current.isActive()) {
							if (ClansAPI.getInstance().getClanManager().getClanName(HUID.fromString(current.getOwner().getTag().getId())) == null) {
								current.remove();
								return;
							}
							Claim lastKnown = r.getLastKnown();
							if (!current.getId().equals(lastKnown.getId())) {
								if (r.hasProperty(Resident.Property.NOTIFIED)) {
									if (!lastKnown.getOwner().getTag().getId().equals(r.getCurrent().getOwner().getTag().getId())) {
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
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans()) {
			boolean test = Arrays.stream(clan.getClaims()).anyMatch(c -> location.getChunk().getX() == c.getPos()[0] && location.getChunk().getZ() == c.getPos()[1] && location.getWorld().getName().equals(c.getKey()[2]));
			if (test) {
				return test;
			}
		}
		return false;
	}

	public boolean isInClaim(int x, int z, String world) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans()) {
			boolean test = Arrays.stream(clan.getClaims()).anyMatch(c -> x == c.getPos()[0] && z == c.getPos()[1] && world.equals(c.getKey()[2]));
			if (test) {
				return test;
			}
		}
		return false;
	}

	public boolean isInClaim(Chunk chunk) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans()) {
			boolean test = Arrays.stream(clan.getClaims()).anyMatch(c -> chunk.getX() == c.getPos()[0] && chunk.getZ() == c.getPos()[1] && chunk.getWorld().getName().equals(c.getKey()[2]));
			if (test) {
				return test;
			}
		}
		return false;
	}

	public String getId(Location location) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans()) {
			String id = Arrays.stream(clan.getClaims()).filter(c -> location.getChunk().getX() == c.getPos()[0] && location.getChunk().getZ() == c.getPos()[1] && location.getWorld().getName().equals(c.getKey()[2])).map(Claim::getId).findFirst().orElse(null);
			if (id != null) {
				return id;
			}
		}
		return null;
	}

	public String getId(int x, int z, String world) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans()) {
			String id = Arrays.stream(clan.getClaims()).filter(c -> x == c.getPos()[0] && z == c.getPos()[1] && world.equals(c.getKey()[2])).map(Claim::getId).findFirst().orElse(null);
			if (id != null) {
				return id;
			}
		}
		return null;
	}

	public Claim getClaim(String claimID) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans()) {
			Claim claim = Arrays.stream(clan.getClaims()).filter(c -> c.getId().equals(claimID)).findFirst().orElse(null);
			if (claim != null) {
				return claim;
			}
		}
		return null;
	}

	public Claim getClaim(Location loc) {
		return getId(loc) != null ? getClaim(getId(loc)) : null;
	}

	public Claim getClaim(Chunk chunk) {
		return getId(chunk.getX(), chunk.getZ(), chunk.getWorld().getName()) != null ? getClaim(getId(chunk.getX(), chunk.getZ(), chunk.getWorld().getName())) : null;
	}

	public boolean test(Player player, Block block) {
		BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
		Bukkit.getPluginManager().callEvent(blockBreakEvent);
		return !blockBreakEvent.isCancelled();
	}

	public FileManager getFile() {
		return regions;
	}

	public FlagManager getFlagManager() {
		return flagManager;
	}

	public Set<Claim> getClaims() {
		return ClansAPI.getInstance().getClanManager().getClans()
				.map(Clan::getClaims)
				.map(cl -> Arrays.stream(cl).sequential().collect(Collectors.toSet()))
				.reduce((claims1, claims2) -> {
					Set<Claim> c = new HashSet<>(claims1);
					c.addAll(claims2);
					return c;
				}).orElse(new HashSet<>());
	}

	public boolean load(Claim claim) {
		if (claim.getOwner() instanceof Clan) {
			if (claim.getClan() instanceof DefaultClan) {
				DefaultClan clan = (DefaultClan) claim.getClan();
				clan.addClaim(claim);
				return true;
			}
		} else {
			if (claim.getOwner() instanceof Clan.Associate) {
				if (claim.getOwner() instanceof DefaultAssociate) {
					DefaultAssociate associate = (DefaultAssociate) claim.getOwner();
					// TODO: setup associate claim api
				}
			}
		}
		return false;
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
		Configurable d = getFile().getRoot();
		Map<InvasiveEntity.Tag, List<Claim>> map = new HashMap<>();
		for (String clan : d.getKeys(false)) {
			Node cl = d.getNode(clan);
			if (cl.isNode("Claims")) {
				Node claims = cl.getNode("Claims");
				for (String claimID : claims.getKeys(false)) {
					Node claim = claims.getNode(claimID);
					int x = claim.getNode("X").toPrimitive().getInt();
					int z = claim.getNode("Z").toPrimitive().getInt();
					String w = claim.getNode("World").toPrimitive().getString();
					Claim c = new DefaultClaim(x, z, clan, claimID, w, true);
					if (claim.isNode("flags")) {
						for (String id : claim.getNode("flags").getKeys(false)) {
							boolean allowed = claim.getNode("flags").getNode(id).toPrimitive().getBoolean();
							Claim.Flag loading = new Claim.Flag(id, true) {

								private static final long serialVersionUID = -5092886817202966276L;

								{
									setEnabled(allowed);
								}

								@Override
								public @NotNull String getId() {
									return id;
								}
							};
							c.register(loading);
						}
					}
					if (map.get(c.getOwner().getTag()) != null) {
						map.get(c.getOwner().getTag()).add(c);
					} else {
						List<Claim> list = new ArrayList<>();
						list.add(c);
						map.put(c.getOwner().getTag(), list);
					}
					claim.set(null);
				}
				claims.set(null);
				claims.save();
			} else {
				InvasiveEntity.Tag c = () -> clan;
				for (String claimID : cl.getKeys(false)) {
					Node claim = cl.getNode(claimID);
					int x = claim.getNode("x").toPrimitive().getInt();
					int z = claim.getNode("z").toPrimitive().getInt();
					String world = claim.getNode("world").toPrimitive().getString();
					boolean active = claim.getNode("active").toPrimitive().getBoolean();
					Claim cla = new DefaultClaim(x, z, clan, claimID, world, active);
					if (claim.isNode("flags")) {
						for (String id : claim.getNode("flags").getKeys(false)) {
							boolean allowed = claim.getNode("flags").getNode(id).toPrimitive().getBoolean();
							Claim.Flag loading = new Claim.Flag(id, true) {

								private static final long serialVersionUID = -5092886817202966276L;

								{
									setEnabled(allowed);
								}

								@Override
								public @NotNull String getId() {
									return id;
								}
							};
							cla.register(loading);
						}
					}
					if (map.get(c) != null) {
						map.get(c).add(cla);
					} else {
						List<Claim> list = new ArrayList<>();
						list.add(cla);
						map.put(c, list);
					}
				}
			}
		}
		ClaimsLoadingProcedureEvent loading = ClanVentBus.call(new ClaimsLoadingProcedureEvent(map));
		int size = 0;
		for (Claim claim : loading.getClaims()) {
			if (claim instanceof DefaultClaim) {
				if (!getFile().getRoot().isNode(claim.getOwner().getTag().getId() + "." + claim.getId())) {
					getFile().getRoot().getNode(claim.getOwner().getTag().getId() + "." + claim.getId()).set(claim);
					getFile().getRoot().save();
				}
			}
			load(claim);
			size++;
		}
		return size;
	}

}
