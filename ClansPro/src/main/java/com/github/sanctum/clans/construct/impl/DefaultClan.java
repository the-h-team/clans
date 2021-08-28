package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.RankPriority;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.ClanWar;
import com.github.sanctum.clans.construct.extra.ClanWrapper;
import com.github.sanctum.clans.events.command.OtherInformationAdaptEvent;
import com.github.sanctum.clans.events.core.LandClaimedEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.formatting.UniformedComponents;
import com.github.sanctum.labyrinth.formatting.string.RandomID;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.NamespacedKey;
import com.github.sanctum.labyrinth.task.Schedule;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultClan implements Clan {

	private static final long serialVersionUID = 427254537180595211L;
	private final String clanID;
	private ClanWar clanWar = null;
	private boolean peaceful;
	private boolean friendlyfire;
	private double powerBonus;
	private double claimBonus;
	private String password;
	private String name;
	private String description;
	private Location base;
	private String color;
	transient NamespacedKey key;
	private final Set<ClanAssociate> associates = new HashSet<>();
	private final List<Clan> warInvites = new ArrayList<>();
	private final Set<Claim> claims = new HashSet<>();
	private final List<String> allies = new ArrayList<>();
	private final List<String> enemies = new ArrayList<>();
	protected final List<String> requests = new ArrayList<>();
	private final UniformedComponents<Clan> allyList;
	private final UniformedComponents<Clan> enemyList;

	/**
	 * Create a clan object using a clanID
	 * See ClanAction for getting id.
	 * If using a player object it is recommended to
	 * use the clanManager from main class HempfestClans.java
	 *
	 * @param clanID The id to store in the object.
	 */
	public DefaultClan(String clanID) {
		this.clanID = clanID;
		this.key = new NamespacedKey(ClansAPI.getInstance().getPlugin(), clanID);
		this.allyList = new ClanWrapper(this, ClanWrapper.RelationType.Ally);
		this.enemyList = new ClanWrapper(this, ClanWrapper.RelationType.Enemy);

		FileManager c = DataManager.FileType.CLAN_FILE.get(clanID);

		if (c.exists() && c.getConfig().getString("name") != null) {
			this.name = c.readValue(f -> f.getString("name"));

			if (c.readValue(f -> f.isString("name-color"))) {
				this.color = c.getConfig().getString("name-color");
			}

			if (c.readValue(f -> f.isString("password"))) {
				this.password = c.getConfig().getString("password");
			}

			if (c.readValue(f -> f.isDouble("bonus"))) {
				this.powerBonus = c.getConfig().getDouble("bonus");
			}

			if (c.readValue(f -> f.isDouble("claim-bonus"))) {
				this.claimBonus = c.getConfig().getDouble("claim-bonus");
			}

			if (c.readValue(f -> f.isString("description"))) {
				this.description = c.getConfig().getString("description");
			}

			allies.addAll(c.readValue(f -> f.getStringList("allies")));

			enemies.addAll(c.readValue(f -> f.getStringList("enemies")));

			requests.addAll(c.readValue(f -> f.getStringList("ally-requests")));

			if (c.readValue(f -> f.isConfigurationSection("base"))) {
				if (c.getLegacySafeLocation("base") == null) {
					double x = c.getConfig().getDouble("base.x");
					double y = c.getConfig().getDouble("base.y");
					double z = c.getConfig().getDouble("base.z");
					float yaw = c.getConfig().getFloatList("base.float").get(0);
					float pitch = c.getConfig().getFloatList("base.float").get(1);
					World w = Bukkit.getWorld(Objects.requireNonNull(c.getConfig().getString("base.world")));
					if (w == null) {
						w = Bukkit.getWorld(Objects.requireNonNull(ClansAPI.getData().getMain().getConfig().getString("Clans.raid-shield.main-world")));
					}
					this.base = new Location(w, x, y, z, yaw, pitch);
					c.getConfig().set("base", this.base);
					c.saveConfig();
				} else {
					this.base = c.getLegacySafeLocation("base");
				}

			} else {
				if (c.getLegacySafeLocation("base") != null) {
					this.base = c.getLegacySafeLocation("base");
				}
			}
			if (c.readValue(f -> f.isConfigurationSection("members"))) {
				// Look how simple the new format is...
				for (String rank : c.readValue(f -> f.getConfigurationSection("members").getKeys(false))) {
					RankPriority priority = RankPriority.valueOf(rank);
					for (String mem : c.readValue(f -> f.getStringList("members." + rank))) {
						associates.add(new ClanAssociate(UUID.fromString(mem), priority, getId()));
					}
				}
			} else {
				// Cleaning up old format.
				Map<UUID, RankPriority> map = new HashMap<>();
				for (String m : c.readValue(f -> f.getStringList("members"))) {
					map.put(UUID.fromString(m), RankPriority.NORMAL);
				}
				Schedule.sync(() -> {
					c.getConfig().set("members", null);
					c.saveConfig();
				}).run();
				for (String m : c.readValue(f -> f.getStringList("moderators"))) {
					map.put(UUID.fromString(m), RankPriority.HIGH);
				}
				Schedule.sync(() -> {
					c.getConfig().set("moderators", null);
					c.saveConfig();
				}).run();
				for (String m : c.readValue(f -> f.getStringList("admins"))) {
					map.put(UUID.fromString(m), RankPriority.HIGHER);
				}
				Schedule.sync(() -> {
					c.getConfig().set("admins", null);
					c.saveConfig();
				}).run();
				if (c.readValue(f -> f.isString("owner"))) {
					map.put(UUID.fromString(c.readValue(f -> f.getString("owner"))), RankPriority.HIGHEST);
					Schedule.sync(() -> {
						c.getConfig().set("owner", null);
						c.saveConfig();
					}).run();
				}
				for (Map.Entry<UUID, RankPriority> entry : map.entrySet()) {
					associates.add(new ClanAssociate(entry.getKey(), entry.getValue(), getId()));
				}

			}

			this.powerBonus = c.getConfig().getDouble("bonus");

			this.peaceful = c.getConfig().getBoolean("peaceful");

			this.friendlyfire = c.getConfig().getBoolean("friendly-fire");

		}

	}

	@Override
	public synchronized ClanAssociate accept(UUID target) {
		if (ClansAPI.getInstance().getAssociate(target).orElse(null) != null) {
			return null;
		}
		if (ClansAPI.getInstance().getClanID(target) != null) {
			return null;
		}
		ACTION.joinClan(target, getName(), getPassword());
		return ClansAPI.getInstance().getAssociate(target).get();
	}

	@Override
	public synchronized boolean kick(UUID target) {
		if (getMembers().stream().noneMatch(c -> c.getPlayer().getUniqueId().equals(target))) {
			return false;
		} else
			getMembers().forEach(c -> {
				if (c.getPlayer().getUniqueId().equals(target)) {
					c.kick();
				}
			});
		return true;
	}

	@Override
	public boolean isPeaceful() {
		return this.peaceful;
	}

	@Override
	public boolean isFriendlyFire() {
		return this.friendlyfire;
	}

	@Override
	public boolean isOwner(@NotNull Chunk chunk) {
		for (String claim : getOwnedClaimsList()) {
			Claim c = ClansAPI.getInstance().getClaimManager().getClaim(claim);
			if (c.getChunk().equals(chunk)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean transferOwnership(UUID target) {
		if (Objects.equals(ClansAPI.getInstance().getClan(target), this)) {
			ClanAssociate owner = getOwner();
			owner.setPriority(RankPriority.NORMAL);
			ClanAssociate mem = getMember(m -> m.getPlayer().getUniqueId().equals(target));
			mem.setPriority(RankPriority.HIGHEST);
			save();
			return true;
		}

		return false;
	}

	@Override
	public boolean isNeutral(String targetClanID) {
		return !getAllyList().contains(targetClanID) && !getEnemyList().contains(targetClanID);
	}

	@Override
	public boolean hasCooldown(String action) {
		return getCooldown(action) != null;
	}

	@Override
	public void setName(String newTag) {
		this.name = newTag;
		String format = MessageFormat.format(ClansAPI.getData().getMessage("tag-change"), newTag);
		broadcast(format);
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
		broadcast("&6&oClan description has been updated to: &f" + description);
	}

	@Override
	public void setPassword(String newPassword) {
		if (newPassword.equals("empty")) {
			this.password = null;
			broadcast("&b&o&nThe clan status was set to&r &a&oOPEN.");
			return;
		}
		this.password = newPassword;
		broadcast(MessageFormat.format(ClansAPI.getData().getMessage("password-change"), newPassword));
	}

	@Override
	public void setColor(String newColor) {
		this.color = newColor;
		broadcast(newColor + "The clan name color has been changed.");
	}

	@Override
	public void setPeaceful(boolean peaceful) {
		this.peaceful = peaceful;
	}

	@Override
	public void setFriendlyFire(boolean friendlyFire) {
		this.friendlyfire = friendlyFire;
	}

	public void setCurrentWar(ClanWar war) {
		this.clanWar = war;
	}

	@Override
	public void setBase(@NotNull Location loc) {
		this.base = loc;
		String format = MessageFormat.format(ClansAPI.getData().getMessage("base-changed"), loc.getWorld().getName());
		broadcast(format);
	}

	@Override
	public synchronized @NotNull HUID getId() {
		return HUID.fromString(clanID);
	}

	@Override
	public synchronized @NotNull String getName() {
		return this.name;
	}

	@Override
	public synchronized @NotNull String getDescription() {
		return this.description != null ? this.description : "I have no description.";
	}

	@Override
	public synchronized @NotNull ClanAssociate getOwner() {
		return associates.stream().filter(a -> a.getPriority() == RankPriority.HIGHEST).findFirst().get();
	}

	@Override
	public ClanAssociate getMember(Predicate<ClanAssociate> predicate) {
		return associates.stream().filter(predicate).findFirst().orElse(null);
	}

	@Override
	public synchronized @Nullable String getPassword() {
		return this.password;
	}

	@Override
	public synchronized @NotNull String getColor() {
		return this.color != null ? this.color : "&f";
	}

	@Override
	public synchronized String[] getMemberIds() {
		return getMembers().stream().map(ClanAssociate::getPlayer).map(OfflinePlayer::getUniqueId).map(UUID::toString).toArray(String[]::new);
	}

	@Override
	public @NotNull Set<ClanAssociate> getMembers() {
		return this.associates;
	}

	@Override
	public void save() {

		FileManager file = ClansAPI.getData().getClanFile(this);
		file.getConfig().set("name", getName());
		if (getBase() != null) {
			file.getConfig().set("base", getBase());
		}
		file.getConfig().set("name-color", getColor());
		file.getConfig().set("description", getDescription());
		file.getConfig().set("password", getPassword());
		file.getConfig().set("bonus", this.powerBonus);
		file.getConfig().set("claim-bonus", this.claimBonus);
		file.getConfig().set("peaceful", this.peaceful);
		file.getConfig().set("friendlyfire", this.friendlyfire);
		file.getConfig().set("allies", allies);
		file.getConfig().set("enemies", enemies);
		if (!getAllyRequests().isEmpty()) {
			file.getConfig().set("ally-requests", getAllyRequests());
		}
		Map<RankPriority, List<ClanAssociate>> map = new HashMap<>();

		for (RankPriority v : RankPriority.values()) {
			map.put(v, new ArrayList<>());
		}

		for (ClanAssociate ass : getMembers()) {
			List<ClanAssociate> list = map.get(ass.getPriority());
			list.add(ass);
			map.put(ass.getPriority(), list);
		}

		for (Map.Entry<RankPriority, List<ClanAssociate>> entry : map.entrySet()) {
			file.getConfig().set("members." + entry.getKey().name(), entry.getValue().stream().map(ClanAssociate::getPlayer).map(OfflinePlayer::getUniqueId).map(UUID::toString).collect(Collectors.toList()));
		}

		file.saveConfig();

	}

	@Override
	public synchronized @Nullable Location getBase() {
		if (this.base == null) {
			return null;
		}
		return this.base;
	}

	@Override
	public synchronized double getPower() {
		double result = 0.0;
		double multiplier = 1.4;
		double add = getMemberIds().length + 0.56;
		int claimAmount = getOwnedClaims().length;
		result = result + add + (claimAmount * multiplier);
		double bonus = this.powerBonus;
		if (ClansAPI.getData().getEnabled("Clans.banks.influence")) {
			if (Bukkit.getPluginManager().isPluginEnabled("Vault") || Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
				double bal = getBalance().doubleValue();
				if (bal != 0) {
					bonus += bal / 48.94;
				}
			} else {
				bonus += getWins() * 39.8;
			}
		} else {
			bonus += getWins() * 39.8;
		}
		return result + bonus;
	}

	@Override
	public synchronized String[] getOwnedClaimsList() {
		return claims.stream().map(Claim::getId).toArray(String[]::new);
	}

	@Override
	public synchronized Claim[] getOwnedClaims() {
		return claims.toArray(new Claim[0]);
	}

	public synchronized void resetClaims() {
		claims.clear();
	}

	public synchronized void addClaim(Claim c) {
		Schedule.sync(() -> claims.add(c)).run();
	}

	public synchronized void removeClaim(Claim c) {
		claims.remove(c);
	}

	@Override
	public synchronized int getMaxClaims() {
		if (!ClansAPI.getData().getEnabled("Clans.land-claiming.claim-influence.allow")) {
			return 0;
		}
		if (ClansAPI.getData().getString("Clans.land-claiming.claim-influence.dependence").equalsIgnoreCase("LOW")) {
			this.claimBonus += 13.33;
		}
		if (getBalance() != null) {
			return (int) ((getMemberIds().length + Math.cbrt(getBalance().doubleValue())) + this.claimBonus);
		} else
			return (int) ((getMemberIds().length + Math.cbrt(getPower())) + this.claimBonus);
	}

	@Override
	public synchronized @NotNull List<String> getAllyList() {
		return this.allies;
	}

	@Override
	public @NotNull UniformedComponents<Clan> getAllies() {
		return this.allyList;
	}

	@Override
	public @NotNull UniformedComponents<Clan> getEnemies() {
		return this.enemyList;
	}

	@Override
	public @NotNull List<String> getDataKeys() {
		return LabyrinthProvider.getInstance().getContainer(this.key).persistentKeySet();
	}

	@Override
	public <R> R getValue(Class<R> type, String key) {
		return LabyrinthProvider.getInstance().getContainer(this.key).get(type, key);
	}

	@Override
	public <R> R setValue(String key, R value, boolean temporary) {
		if (temporary) return LabyrinthProvider.getInstance().getContainer(this.key).lend(key, value);
		return LabyrinthProvider.getInstance().getContainer(this.key).attach(key, value);
	}

	@Override
	public boolean removeValue(String key) {
		return LabyrinthProvider.getInstance().getContainer(this.key).delete(key);
	}

	@Override
	public synchronized @NotNull List<String> getEnemyList() {
		return this.enemies;
	}

	@Override
	public synchronized @NotNull List<String> getAllyRequests() {
		return this.requests;
	}

	public @Nullable ClanWar getCurrentWar() {
		return clanWar;
	}

	@Override
	public synchronized int getWins() {
		FileManager c = DataManager.FileType.CLAN_FILE.get(clanID);
		return c.getConfig().getInt("wars-won");
	}

	@Override
	public synchronized int getLosses() {
		FileManager c = DataManager.FileType.CLAN_FILE.get(clanID);
		return c.getConfig().getInt("wars-lost");
	}

	@Override
	public synchronized String[] getClanInfo() {
		List<String> array = new ArrayList<>();
		String password = this.password;
		List<String> members = getMembers().stream().filter(m -> m.getPriority().toInt() == 0).map(ClanAssociate::getPlayer).map(OfflinePlayer::getName).collect(Collectors.toList());
		List<String> mods = getMembers().stream().filter(m -> m.getPriority().toInt() == 1).map(ClanAssociate::getPlayer).map(OfflinePlayer::getName).collect(Collectors.toList());
		List<String> admins = getMembers().stream().filter(m -> m.getPriority().toInt() == 2).map(ClanAssociate::getPlayer).map(OfflinePlayer::getName).collect(Collectors.toList());
		List<String> allies = getAllies().map(Clan::getId).map(HUID::toString).collect(Collectors.toList());
		List<String> enemies = getEnemies().map(Clan::getId).map(HUID::toString).collect(Collectors.toList());
		String status = "LOCKED";
		if (password == null)
			status = "OPEN";
		array.add(" ");
		array.add("&2&lClan&7: " + getColor() + ClansAPI.getInstance().getClanName(clanID));
		array.add("&f&m---------------------------");
		array.add("&2Description: &7" + getDescription());
		array.add("&2" + getOwner().getRankTag() + ": &f" + getOwner().getPlayer().getName());
		array.add("&2Status: &f" + status);
		array.add("&2&lPower [&e" + format(String.valueOf(getPower())) + "&2&l]");
		if (getBase() != null)
			array.add("&2Base: &aSet");
		if (getBase() == null)
			array.add("&2Base: &7Not set");
		if (isPeaceful())
			array.add("&2Mode: &f&lPEACE");
		if (!isPeaceful())
			array.add("&2Mode: &4&lWAR");
		array.add("&2" + ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Styles.Full.Admin") + "s [&b" + admins.size() + "&2]");
		array.add("&2" + ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Styles.Full.Moderator") + "s [&e" + mods.size() + "&2]");
		array.add("&2Claims [&e" + getOwnedClaimsList().length + "&2]");
		array.add("&f&m---------------------------");
		if (allies.isEmpty())
			array.add("&2Allies [&b" + "0" + "&2]");
		if (allies.size() > 0) {
			array.add("&2Allies [&b" + allies.size() + "&2]");
			for (String clanId : allies) {
				array.add("&f- &e&o" + ClansAPI.getInstance().getClanName(clanId));
			}
		}
		if (enemies.isEmpty())
			array.add("&2Enemies [&b" + "0" + "&2]");
		if (enemies.size() > 0) {
			array.add("&2Enemies [&b" + enemies.size() + "&2]");
			for (String clanId : enemies) {
				array.add("&f- &c&o" + ClansAPI.getInstance().getClanName(clanId));
			}
		}
		array.add("&f&m---------------------------");
		array.add("&n" + ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Styles.Full.Member") + "s&r [&7" + members.size() + "&r] - " + members.toString());
		array.add(" ");
		OtherInformationAdaptEvent event = new Vent.Call<>(Vent.Runtime.Synchronous, new OtherInformationAdaptEvent(array, clanID)).run();
		return event.getInsertions().toArray(new String[0]);
	}

	/**
	 * Get the mode-switch cooldown object for the clan
	 *
	 * @return an object containing cooldown information.
	 */
	public ClanCooldown getModeCooldown() {
		ClanCooldown target = null;
		for (ClanCooldown c : getCooldowns()) {
			if (c.getAction().equals("Clans:mode-switch")) {
				target = c;
			}
		}
		if (target == null) {
			CooldownMode mode = new CooldownMode(clanID);
			mode.save();
			target = mode;
		}
		return target;
	}

	/**
	 * Get the ff-switch cooldown object for the clan
	 *
	 * @return an object containing cooldown information.
	 */
	public ClanCooldown getFriendlyCooldown() {
		ClanCooldown target = null;
		for (ClanCooldown c : getCooldowns()) {
			if (c.getAction().equals("Clans:ff-switch")) {
				target = c;
			}
		}
		if (target == null) {
			CooldownFriendlyFire mode = new CooldownFriendlyFire(clanID);
			mode.save();
			target = mode;
		}
		return target;
	}

	@Override
	public ClanCooldown getCooldown(String action) {
		ClanCooldown target = null;
		for (ClanCooldown c : getCooldowns()) {
			if (c.getAction().equals(action)) {
				target = c;
			}
		}
		return target;
	}

	@Override
	public @NotNull List<ClanCooldown> getCooldowns() {
		return ClansAPI.getData().COOLDOWNS.stream().sequential().filter(c -> c.getId().equals(clanID)).collect(Collectors.toList());
	}

	@Override
	public void broadcast(String message) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (ClansAPI.getInstance().isInClan(p.getUniqueId()) && ClansAPI.getInstance().getClanID(p.getUniqueId()).equals(getId())) {
				p.sendMessage(ACTION.color("&7[&6&l" + getName() + "&7] " + message));
			}
		}
	}

	@Override
	public void broadcast(Predicate<ClanAssociate> predicate, String message) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			ClansAPI.getInstance().getAssociate(p).ifPresent(a -> {
				if (equals(a.getClan())) {
					if (predicate.test(a)) {
						p.sendMessage(ACTION.color(message));
					}
				}
			});
		}
	}

	@Override
	public synchronized @NotNull String format(String amount) {
		BigDecimal b1 = new BigDecimal(amount);
		Locale loc = Locale.US;
		switch (ClansAPI.getData().getMain().getConfig().getString("Formatting.locale")) {
			case "fr":
				loc = Locale.FRANCE;
				break;
			case "de":
				loc = Locale.GERMANY;
				break;
			case "nl":
				loc = new Locale("nl", "NL");
				break;
		}
		return NumberFormat.getNumberInstance(loc).format(b1.doubleValue());
	}

	@Override
	public @Nullable Claim obtain(Chunk c) {
		Claim claim = null;
		if (!ClansAPI.getInstance().getClaimManager().isInClaim(c)) {
			int chunkCount = 0;
			for (Chunk chunk : Claim.ACTION.getChunksAroundChunk(c, -1, 0, 1).stream().filter(chunk -> ClansAPI.getInstance().getClaimManager().isInClaim(chunk.getX(), chunk.getZ(), chunk.getWorld().getName())).collect(Collectors.toList())) {
				if (isOwner(chunk)) {
					chunkCount++;
				}
			}
			if (ClansAPI.getData().getEnabled("Clans.land-claiming.claim-connections")) {
				if (getOwnedClaimsList().length >= 1 && chunkCount == 0) {
					return null;
				}
			}
			if (getOwnedClaims().length == getMaxClaims()) return null;
			String claimID = new RandomID(6, "AKZ0123456789").generate();
			int x = c.getX();
			int z = c.getZ();
			String world = c.getWorld().getName();
			FileManager d = ClansAPI.getInstance().getClaimManager().getFile();
			d.getConfig().set(getId().toString() + ".Claims." + claimID + ".X", x);
			d.getConfig().set(getId().toString() + ".Claims." + claimID + ".Z", z);
			d.getConfig().set(getId().toString() + ".Claims." + claimID + ".World", world);
			d.saveConfig();
			claim = new Claim(x, z, clanID, claimID, world, true);
			ClansAPI.getInstance().getClaimManager().load(claim);
			broadcast(Claim.ACTION.claimed(x, z, world));
			new Vent.Call<>(Vent.Runtime.Synchronous, new LandClaimedEvent(null, claim)).run();
		}
		return claim;
	}

	@Override
	public synchronized void givePower(double amount) {
		this.powerBonus += amount;
		broadcast("&a&oNew power was gained. The clan grows stronger..");
		ClansAPI.getInstance().getPlugin().getLogger().info("- Gave " + '"' + amount + '"' + " power to clan " + '"' + clanID + '"');
	}

	@Override
	public synchronized void takePower(double amount) {
		this.powerBonus -= amount;
		broadcast("&c&oPower was stolen from us.. we need to earn it back");
		ClansAPI.getInstance().getPlugin().getLogger().info("- Took " + '"' + amount + '"' + " power from clan " + '"' + clanID + '"');
	}

	@Override
	public synchronized void addMaxClaim(int amount) {
		this.claimBonus += amount;
		broadcast("&a&oNew power was gained. The clan grows stronger..");
		ClansAPI.getInstance().getPlugin().getLogger().info("- Gave " + '"' + amount + '"' + " claim(s) to clan " + '"' + clanID + '"');
	}

	@Override
	public synchronized void takeMaxClaim(int amount) {
		this.claimBonus -= amount;
		broadcast("&c&oPower was stolen from us.. we need to earn it back");
		ClansAPI.getInstance().getPlugin().getLogger().info("- Took " + '"' + amount + '"' + " claim(s) from clan " + '"' + clanID + '"');
	}

	@Override
	public synchronized void addWin(int amount) {
		FileManager c = DataManager.FileType.CLAN_FILE.get(clanID);
		if (!c.getConfig().isInt("wars-won")) {
			c.getConfig().set("wars-won", 0.0);
			c.refreshConfig();
		}
		int current = c.getConfig().getInt("wars-won");
		c.getConfig().set("wars-won", (current + amount));
		c.refreshConfig();
	}

	@Override
	public synchronized void addLoss(int amount) {
		FileManager c = DataManager.FileType.CLAN_FILE.get(clanID);
		if (!c.getConfig().isInt("wars-lost")) {
			c.getConfig().set("wars-lost", 0.0);
			c.saveConfig();
		}
		int current = c.getConfig().getInt("wars-lost");
		c.getConfig().set("wars-lost", (current - amount));
		c.saveConfig();
	}

	@Override
	public synchronized void sendAllyRequest(HUID targetClanID) {
		Clan target = ClansAPI.getInstance().getClan(targetClanID.toString());
		if (getAllyRequests().contains(targetClanID.toString())) {
			addAlly(targetClanID);
			return;
		}
		if (target.getAllyRequests().contains(clanID)) {
			broadcast(Clan.ACTION.waiting(ClansAPI.getInstance().getClanName(targetClanID.toString())));
			return;
		}
		Clan clanIndex = ClansAPI.getInstance().getClan(targetClanID.toString());
		clanIndex.getAllyRequests().add(clanID);
		broadcast(Clan.ACTION.allianceRequested());
		clanIndex.broadcast(Clan.ACTION.allianceRequestedOut(getName(), ClansAPI.getInstance().getClanName(clanID)));
	}

	@Override
	public void sendAllyRequest(HUID targetClanID, String message) {
		Clan target = ClansAPI.getInstance().getClan(targetClanID.toString());
		if (getAllyRequests().contains(targetClanID.toString())) {
			addAlly(targetClanID);
			return;
		}
		if (target.getAllyRequests().contains(clanID)) {
			broadcast(Clan.ACTION.waiting(ClansAPI.getInstance().getClanName(targetClanID.toString())));
			return;
		}
		Clan clanIndex = ClansAPI.getInstance().getClan(targetClanID.toString());
		clanIndex.getAllyRequests().add(clanID);
		broadcast(Clan.ACTION.allianceRequested());
		clanIndex.broadcast(message);
	}

	@Override
	public synchronized void addAlly(HUID targetClanID) {
		if (getAllyRequests().contains(targetClanID.toString())) {
			requests.remove(targetClanID.toString());
		}
		Clan target = ClansAPI.getInstance().getClan(targetClanID.toString());
		allies.add(targetClanID.toString());
		target.getAllyList().add(clanID);
		broadcast(Clan.ACTION.ally(target.getName()));
		target.broadcast(Clan.ACTION.ally(getName()));
	}

	@Override
	public synchronized void removeAlly(HUID targetClanID) {
		Clan target = ClansAPI.getInstance().getClan(targetClanID.toString());
		target.getAllyList().remove(clanID);
		allies.remove(targetClanID.toString());
	}

	@Override
	public synchronized void addEnemy(HUID targetClanID) {
		Clan target = ClansAPI.getInstance().getClan(targetClanID.toString());
		if (getAllyList().contains(targetClanID.toString())) {
			removeAlly(targetClanID);
		}
		enemies.add(targetClanID.toString());
		broadcast(Clan.ACTION.enemy(target.getName()));
		target.broadcast(Clan.ACTION.enemy(getName()));
	}

	@Override
	public synchronized void removeEnemy(HUID targetClanID) {
		Clan target = ClansAPI.getInstance().getClan(targetClanID.toString());
		enemies.remove(targetClanID.toString());
		broadcast(Clan.ACTION.neutral(target.getName()));
		target.broadcast(Clan.ACTION.neutral(getName()));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DefaultClan)) return false;
		DefaultClan clan = (DefaultClan) o;
		return getId().equals(clan.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(clanID);
	}

	private ClanBank getBank() {
		if (!EconomyProvision.getInstance().isValid()) {
			return null;
		}
		return API.defaultImpl.getBank(this);
	}

	@Override
	public boolean deposit(Player player, BigDecimal amount) {
		if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
			return false;
		}
		return Objects.requireNonNull(getBank()).deposit(player, amount);
	}

	@Override
	public boolean withdraw(Player player, BigDecimal amount) {
		if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
			return false;
		}
		return Objects.requireNonNull(getBank()).withdraw(player, amount);
	}

	@Override
	public boolean has(BigDecimal amount) {
		if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
			return false;
		}
		return Objects.requireNonNull(getBank()).has(amount);
	}

	@Override
	public BigDecimal getBalance() {
		if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
			return null;
		}
		return Objects.requireNonNull(getBank()).getBalance();
	}

	@Override
	public boolean setBalance(BigDecimal newBalance) {
		if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
			return false;
		}
		return Objects.requireNonNull(getBank()).setBalance(newBalance);
	}

	public @NotNull List<Clan> getWarInvites() {
		return warInvites;
	}

	@Override
	public Implementation getImplementation() {
		return Implementation.DEFAULT;
	}
}
