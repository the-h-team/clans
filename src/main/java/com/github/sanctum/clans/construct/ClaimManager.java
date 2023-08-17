package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.InvasiveEntity;
import com.github.sanctum.clans.construct.api.ResidencyInfo;
import com.github.sanctum.clans.construct.impl.DefaultClaim;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.construct.impl.entity.DefaultAssociate;
import com.github.sanctum.clans.event.TimerEvent;
import com.github.sanctum.clans.event.claim.ClaimResidencyEvent;
import com.github.sanctum.clans.event.claim.ClaimsLoadingProcedureEvent;
import com.github.sanctum.clans.event.claim.WildernessResidencyEvent;
import com.github.sanctum.labyrinth.data.DataTable;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.panther.event.Vent;
import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.util.HUID;
import com.github.sanctum.panther.util.OrdinalProcedure;
import java.io.IOException;
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
	private final ResidentManager residentManager;

	public ClaimManager() {
		this.residentManager = new ResidentManager(this);
		this.flagManager = new FlagManager(this);
		fixRegionsFile();
		this.regions = ClansAPI.getInstance().getFileList().get("regions", "Configuration/Data", JavaPlugin.getPlugin(ClansJavaPlugin.class).TYPE);
		regions.getRoot().reload();
		ClanVentBus.subscribe(TimerEvent.class, Vent.Priority.HIGH, (e, subscription) -> {
			if (e.isAsynchronous()) return;
			Player p = e.getPlayer();
			ClansAPI API = ClansAPI.getInstance();

			if (Claim.ACTION.isAllowed().deploy()) {

				if (!API.getClaimManager().isInClaim(p.getLocation())) {
					Claim.Resident res = API.getClaimManager().getResidentManager().getResident(p);
					if (res != null) {
						if (!res.getInfo().hasProperty(ResidencyInfo.Trigger.LEAVING)) {
							res.getInfo().setProperty(ResidencyInfo.Trigger.LEAVING, true);
							WildernessResidencyEvent wild = ClanVentBus.call(new WildernessResidencyEvent(res));
							if (!wild.isCancelled()) {
								OrdinalProcedure.of(wild).run(0);
								// receive now leaving message
							}
						}
						API.getClaimManager().getResidentManager().remove(res);
					}

				} else {
					ClaimResidencyEvent event = ClanVentBus.call(new ClaimResidencyEvent(API.getClaimManager(), p));
					if (!event.isCancelled()) {
						Claim.Resident r = event.getResident();
						r.getInfo().setProperty(ResidencyInfo.Trigger.LEAVING, false); // ensure we're still in a claim, probably not needed but done anyways.
						Claim current = event.getClaim();
						if (current.isActive()) {
							if (ClansAPI.getInstance().getClanManager().getClanName(HUID.parseID(current.getOwner().getTag().getId()).toID()) == null) {
								current.remove();
								return;
							}
							Claim lastKnown = r.getInfo().getLastKnown();
							if (!current.getId().equals(lastKnown.getId())) {
								if (r.getInfo().hasProperty(ResidencyInfo.Trigger.NOTIFIED)) {
									if (!lastKnown.getOwner().getTag().getId().equals(r.getInfo().getCurrent().getOwner().getTag().getId())) {
										r.getInfo().setProperty(ResidencyInfo.Trigger.TRAVERSED, true);
										r.getInfo().setLastKnown(event.getClaim());
										r.getInfo().setTimeEntered(System.currentTimeMillis());
									}
								}
							}
							if (!r.getInfo().hasProperty(ResidencyInfo.Trigger.NOTIFIED)) {
								OrdinalProcedure.of(event).run(0);
								r.getInfo().setProperty(ResidencyInfo.Trigger.NOTIFIED, true);
							} else {
								if (r.getInfo().hasProperty(ResidencyInfo.Trigger.TRAVERSED)) {
									r.getInfo().setProperty(ResidencyInfo.Trigger.TRAVERSED, false);
									r.getInfo().setTimeEntered(System.currentTimeMillis());
									OrdinalProcedure.of(event).run(0);
								}
							}
						}
					}
				}
			}
		});
	}

	// this will only need to be in for a couple updates. TODO: take it out around 3.0.2
	void fixRegionsFile() {
		FileManager test = ClansAPI.getInstance().getFileList().get("Regions", "Configuration/Data", Configurable.Type.YAML);
		if (test.getRoot().exists()) {
			final DataTable table = test.copy();
			FileManager ne = ClansAPI.getInstance().getFileList().get("regions", "Configuration/Data", Configurable.Type.YAML);
			try {
				ne.getRoot().create();
			} catch (IOException e) {
				e.printStackTrace();
			}
			test.getRoot().delete();
			ne.write(table);
			ne.getRoot().reload();
		}
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
		if (getId(loc) != null) {
			Claim claim = getClaim(getId(loc));
			for (Claim.Flag def : getFlagManager().getFlags()) {
				if (claim.getFlag(def.getId()) == null) {
					claim.register(def);
				}
			}
			return claim;
		}
		return null;
	}

	public Claim getClaim(Chunk chunk) {
		if (getId(chunk.getX(), chunk.getZ(), chunk.getWorld().getName()) != null) {
			return getClaim(getId(chunk.getX(), chunk.getZ(), chunk.getWorld().getName()));
		}
		return null;
	}

	public boolean testBlockBuildPermission(Player player, Block block) {
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

	public ResidentManager getResidentManager() {
		return this.residentManager;
	}

	public Set<Claim> getClaims() {
		return ClansAPI.getInstance().getClanManager().getClans()
				.stream()
				.map(Clan::getClaims)
				.map(cl -> Arrays.stream(cl).sequential().collect(Collectors.toSet()))
				.reduce((claims1, claims2) -> {
					Set<Claim> c = new HashSet<>(claims1);
					c.addAll(claims2);
					return c;
				}).orElse(new HashSet<>());
	}

	public boolean load(Claim claim) {
		for (Claim.Flag def : getFlagManager().getFlags()) {
			if (claim.getFlag(def.getId()) == null) {
				claim.register(def);
			}
		}
		if (claim.getOwner() instanceof Clan) {
			if (claim.getHolder() instanceof DefaultClan) {
				DefaultClan clan = (DefaultClan) claim.getHolder();
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

		for (Clan c : ClansAPI.getInstance().getClanManager().getClans()) {
			if (c instanceof DefaultClan) {
				DefaultClan clan = (DefaultClan) c;
				clan.resetClaims();
			}
		}
		Configurable d = getFile().getRoot();
		d.reload();
		Map<InvasiveEntity.Tag, List<Claim>> map = new HashMap<>();
		for (String clan : d.getKeys(false)) {
			Node cl = d.getNode(clan);
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
						Claim.Flag loading = new Claim.Flag(id, false) {

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
