package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.actions.ClanCooldown;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.cooldown.CooldownFriendlyFire;
import com.github.sanctum.clans.construct.extra.cooldown.CooldownMode;
import com.github.sanctum.clans.construct.extra.misc.ClanWar;
import com.github.sanctum.clans.construct.extra.uniform.ClanWrapper;
import com.github.sanctum.clans.construct.extra.uniform.MemberWrapper;
import com.github.sanctum.clans.util.data.DataManager;
import com.github.sanctum.clans.util.events.clans.LandClaimedEvent;
import com.github.sanctum.clans.util.events.command.OtherInformationAdaptEvent;
import com.github.sanctum.labyrinth.Labyrinth;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.formatting.UniformedComponents;
import com.github.sanctum.labyrinth.formatting.string.RandomID;
import com.github.sanctum.labyrinth.library.HUID;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.sanctum.labyrinth.library.NamespacedKey;
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
	private boolean peaceful;
	private boolean friendlyfire;
	private double powerBonus;
	private double power;
	private String name;
	private String description;
	private Location base;
	private String color;
	transient NamespacedKey key;
	private final List<Clan> warInvites = new ArrayList<>();
	private ClanWar clanWar = null;
	private final MemberWrapper memberList;
	private final UniformedComponents<Clan> allyList;
	private final UniformedComponents<Clan> enemyList;

	public static final ClanAction action = new ClanAction();

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
		this.key = new NamespacedKey(ClansPro.getInstance(), clanID);
		this.memberList = new MemberWrapper(this);
		this.allyList = new ClanWrapper(this, ClanWrapper.RelationType.Ally);
		this.enemyList = new ClanWrapper(this, ClanWrapper.RelationType.Enemy);

		FileManager c = DataManager.FileType.CLAN_FILE.get(clanID);

		this.name = c.getConfig().getString("name");

		if (c.getConfig().isString("description")) {
			this.description = c.getConfig().getString("description");
		}

		if (c.getConfig().isString("name-color")) {
			this.color = c.getConfig().getString("name-color");
		}

		try {
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
		} catch (IndexOutOfBoundsException e) {
			this.base = null;
		}

		this.powerBonus = c.getConfig().getDouble("bonus");

		this.peaceful = c.getConfig().getBoolean("peaceful");

		this.friendlyfire = c.getConfig().getBoolean("friendly-fire");

	}

	/**
	 * Adds the specified target as a clan member and returns an associate object.
	 *
	 * @param target The target to collect.
	 * @return A valid clan associate or null.
	 */
	@Override
	public synchronized ClanAssociate accept(UUID target) {
		if (ClansAPI.getInstance().getAssociate(target).orElse(null) != null) {
			return null;
		}
		if (action.getClanID(target) != null) {
			return null;
		}
		action.joinClan(target, getName(), getPassword());
		return ClansAPI.getInstance().getAssociate(target).get();
	}

	/**
	 * Kick a specified member from the clan.
	 *
	 * @param target The specified target to kick.
	 * @return true if the target is a member and got kicked.
	 */
	@Override
	public synchronized boolean kick(UUID target) {
		if (getMembers().filter(c -> c.getPlayer().getUniqueId().equals(target)).count() == 0) {
			return false;
		} else
			getMembers().list().forEach(c -> {
				if (c.getPlayer().getUniqueId().equals(target)) {
					c.kick();
				}
			});
		return true;
	}

	/**
	 * Check the clans pvp-mode
	 *
	 * @return false if war mode
	 */
	@Override
	public boolean isPeaceful() {
		return this.peaceful;
	}

	/**
	 * Check if the clan allows friend-fire
	 *
	 * @return true if friendly fire
	 */
	@Override
	public boolean isFriendlyFire() {
		return this.friendlyfire;
	}

	/**
	 * Check if this clan owns the provided chunk.
	 *
	 * @param chunk The chunk to check
	 * @return true if the provided chunk is owned by this clan.
	 */
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

	/**
	 * Transfer ownership of the clan to a specified clan member.
	 *
	 * @param target The user to transfer ownership to.
	 * @return true if they are a member of the clan and can be promoted.
	 */
	@Override
	public boolean transferOwnership(UUID target) {

		if (ClansAPI.getInstance().getClan(target) != null) {
			if (ClansAPI.getInstance().getClan(target).getMembers().filter(c -> c.getPlayer().getUniqueId().equals(getOwner())).count() > 0) {
				if (Bukkit.getOfflinePlayer(getOwner()).isOnline()) {
					action.sendMessage(Bukkit.getPlayer(getOwner()), MessageFormat.format(ClansAPI.getData().getMessage("pass-owner"), Bukkit.getOfflinePlayer(target).getName()));
				}
				FileManager clan = ClansAPI.getData().getClanFile(this);
				clan.getConfig().set("owner", target.toString());
				clan.saveConfig();
				if (Bukkit.getOfflinePlayer(target).isOnline()) {
					action.sendMessage(Bukkit.getPlayer(target), MessageFormat.format(ClansAPI.getData().getMessage("now-owner"), Bukkit.getOfflinePlayer(getOwner()).getName()));
				}
				return true;
			}
		}

		return false;
	}

	/**
	 * Check if the clan is neutral in relation with another clan.
	 *
	 * @param targetClanID Target clan
	 * @return true if the two clans are neutral in relation.
	 */
	@Override
	public boolean isNeutral(String targetClanID) {
		return !getAllyList().contains(targetClanID) && !getEnemyList().contains(targetClanID);
	}

	/**
	 * Check if the clan has a cooldown
	 *
	 * @param action The label to search for
	 * @return false if cooldown cache doesn't contain reference
	 */
	@Override
	public boolean hasCooldown(String action) {
		return getCooldown(action) != null;
	}

	/**
	 * Change the clans name
	 *
	 * @param newTag String to change name to.
	 */
	@Override
	public void setName(String newTag) {
		this.name = newTag;
		FileManager c = DataManager.FileType.CLAN_FILE.get(clanID);
		c.getConfig().set("name", newTag);
		c.saveConfig();
		String format = MessageFormat.format(ClansAPI.getData().getMessage("tag-change"), newTag);
		broadcast(format);
	}

	/**
	 * Change the clans description
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		clan.getConfig().set("description", description);
		clan.saveConfig();
		broadcast("&6&oClan description has been updated to: &f" + description);
	}

	/**
	 * Change the clan's password
	 *
	 * @param newPassword String to change password to.
	 */
	@Override
	public void setPassword(String newPassword) {
		FileManager c = DataManager.FileType.CLAN_FILE.get(clanID);
		if (newPassword.equals("empty")) {
			c.getConfig().set("password", null);
			c.saveConfig();
			broadcast("&b&o&nThe clan status was set to&r &a&oOPEN.");
			return;
		}
		c.getConfig().set("password", newPassword);
		c.saveConfig();
		broadcast(MessageFormat.format(ClansAPI.getData().getMessage("password-change"), newPassword));
	}

	/**
	 * Change the clans color theme
	 *
	 * @param newColor Color-code to change the value to.
	 */
	@Override
	public void setColor(String newColor) {
		this.color = newColor;
		FileManager c = DataManager.FileType.CLAN_FILE.get(clanID);
		c.getConfig().set("name-color", newColor);
		c.saveConfig();
		broadcast(newColor + "The clan name color has been changed.");
	}

	/**
	 * Change the clan's pvp-mode
	 *
	 * @param peaceful The boolean to change the value to
	 */
	@Override
	public void setPeaceful(boolean peaceful) {
		this.peaceful = peaceful;
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		clan.getConfig().set("peaceful", peaceful);
		clan.saveConfig();
	}

	/**
	 * Change the friendlyfire status of the clan
	 *
	 * @param friendlyFire The boolean to change the value to
	 */
	@Override
	public void setFriendlyFire(boolean friendlyFire) {
		this.friendlyfire = friendlyFire;
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		clan.getConfig().set("friendly-fire", friendlyFire);
		clan.saveConfig();
	}

	public void setCurrentWar(ClanWar war) {
		this.clanWar = war;
	}

	/**
	 * Change the clan's base location
	 *
	 * @param loc Update the clans base to a specified location.
	 */
	@Override
	public void setBase(@NotNull Location loc) {
		this.base = loc;
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		float yaw = loc.getYaw();
		float pitch = loc.getPitch();
		World w = loc.getWorld();
		clan.getConfig().set("base.x", x);
		clan.getConfig().set("base.y", y);
		clan.getConfig().set("base.z", z);
		List<Float> exact = new ArrayList<>();
		exact.add(yaw);
		exact.add(pitch);
		clan.getConfig().set("base.float", exact);
		clan.getConfig().set("base.world", w.getName());
		clan.saveConfig();
		String format = MessageFormat.format(ClansAPI.getData().getMessage("base-changed"), loc.getWorld().getName());
		broadcast(format);
	}

	/**
	 * @return clanID stored within the clan object.
	 * Get the id of the clan stored within the object
	 * @deprecated Familiarize with new {@link DefaultClan#getId()}
	 */
	@Deprecated
	public synchronized String getClanID() {
		return clanID;
	}

	/**
	 * Get the id of the clan stored within the object
	 *
	 * @return clanID stored within the clan object as an HUID
	 */
	@Override
	public synchronized @NotNull HUID getId() {
		return HUID.fromString(clanID);
	}

	/**
	 * Get the name of the clan
	 *
	 * @return Gets the clan objects clan tag
	 */
	@Override
	public synchronized @NotNull String getName() {
		return this.name;
	}

	/**
	 * Get the clans description
	 *
	 * @return The clans description
	 */
	@Override
	public synchronized @NotNull String getDescription() {
		return this.description != null ? this.description : "I have no description.";
	}

	/**
	 * Get the user who owns the clan.
	 *
	 * @return Gets the clan owner.
	 */
	@Override
	public synchronized @NotNull UUID getOwner() {
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		return UUID.fromString(Objects.requireNonNull(clan.getConfig().getString("owner")));
	}

	/**
	 * Get the clan's password
	 *
	 * @return The clan's password otherwise null
	 */
	@Override
	public synchronized @Nullable String getPassword() {
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		return clan.getConfig().getString("password");
	}

	/**
	 * Get the color theme for the clan
	 *
	 * @return Gets the clan objects clan tag color
	 */
	@Override
	public synchronized @NotNull String getColor() {
		return this.color != null ? this.color : "&f";
	}

	/**
	 * Get the full roster of clan members.
	 *
	 * @return Gets the member list of the clan object.
	 */
	@Override
	public synchronized String[] getMembersList() {
		FileManager c = DataManager.FileType.CLAN_FILE.get(clanID);
		List<String> array = new ArrayList<>(c.getConfig().getStringList("members"));
		return array.toArray(new String[0]);
	}

	@Override
	public @NotNull UniformedComponents<ClanAssociate> getMembers() {
		return this.memberList;
	}

	@Override
	public @NotNull UniformedComponents<OfflinePlayer> getPlayers() {
		return this.memberList.asPlayer();
	}

	/**
	 * Get's the location of the clans base
	 *
	 * @return A base location.
	 */
	@Override
	public synchronized @Nullable Location getBase() {
		if (this.base == null) {
			return null;
		}
		return this.base;
	}

	/**
	 * Get the amount of power the clan has
	 *
	 * @return double value
	 */
	@Override
	public synchronized double getPower() {
		double result = 0.0;
		double multiplier = 1.4;
		double add = getMembersList().length + 0.56;
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

	/**
	 * Get the full list of owned claims for this clan by id.
	 *
	 * @return An array of claim id's
	 */
	@Override
	public synchronized String[] getOwnedClaimsList() {
		List<String> array = new ArrayList<>();
		for (Claim claim : ClansAPI.getInstance().getClaimManager().getClaims()) {
			if (claim.getOwner().equalsIgnoreCase(getId().toString())) {
				array.add(claim.getId());
			}
		}
		return array.toArray(new String[0]);
	}

	/**
	 * Get the full list of owned claims for this clan.
	 *
	 * @return An array of clan claims
	 */
	@Override
	public synchronized Claim[] getOwnedClaims() {
		List<Claim> result = new ArrayList<>();
		for (Claim claim : ClansAPI.getInstance().getClaimManager().getClaims()) {
			if (claim.getOwner().equalsIgnoreCase(getId().toString())) {
				result.add(claim);
			}
		}
		return result.toArray(new Claim[0]);
	}

	@Override
	public synchronized int getMaxClaims() {
		if (!ClansAPI.getData().getEnabled("Clans.land-claiming.claim-influence.allow")) {
			return 0;
		}
		FileManager c = DataManager.FileType.CLAN_FILE.get(clanID);
		double bonus = c.getConfig().getDouble("claim-bonus");
		if (ClansAPI.getData().getString("Clans.land-claiming.claim-influence.dependence").equalsIgnoreCase("LOW")) {
			bonus += 13.33;
		}
		if (getBalance() != null) {
			return (int) ((getMembersList().length + Math.cbrt(getBalance().doubleValue())) + bonus);
		} else
			return (int) ((getMembersList().length + Math.cbrt(getPower())) + bonus);
	}

	/**
	 * Get a full roster of clan allies by clan id
	 *
	 * @return A string array of clan ids
	 */
	@Override
	public synchronized @NotNull List<String> getAllyList() {
		if (!ClansAPI.getData().CLAN_ALLY_MAP.containsKey(clanID)) {
			FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
			return new ArrayList<>(clan.getConfig().getStringList("allies"));
		}
		return ClansAPI.getData().CLAN_ALLY_MAP.get(clanID);
	}

	/**
	 * Get a full roster of allied clans for this clan.
	 *
	 * @return A uniformed component listing.
	 */
	@Override
	public @NotNull UniformedComponents<Clan> getAllies() {
		return this.allyList;
	}

	/**
	 * Get a full roster of rivaled clans for this clan.
	 *
	 * @return A uniformed component listing.
	 */
	@Override
	public @NotNull UniformedComponents<Clan> getEnemies() {
		return this.enemyList;
	}

	/**
	 * Get all object key's within this clans data container.
	 *
	 * @return The list of key's for this clans data container.
	 */
	@Override
	public @NotNull List<String> getDataKeys() {
		return Labyrinth.getContainer(this.key).persistentKeySet();
	}

	/**
	 * Retrieve a value of specified type from this clans persistent data container.
	 *
	 * @param type The type of object to retrieve.
	 * @param key  The key delimiter for the object.
	 * @param <R>  The desired serializable object.
	 * @return The desired serializable object.
	 */
	@Override
	public <R> R getValue(Class<R> type, String key) {
		return Labyrinth.getContainer(this.key).get(type, key);
	}

	/**
	 * Store a custom serializable object to this clans data container.
	 *
	 * @param key   The key delimiter for the value.
	 * @param value The desired serializable object to be stored.
	 * @param <R>   The type of the value.
	 * @return The same value passed through the parameters.
	 */
	@Override
	public <R> R setValue(String key, R value) {
		return Labyrinth.getContainer(this.key).attach(key, value);
	}

	/**
	 * Remove a persistent value from this clans data container.
	 *
	 * @param key The values key delimiter.
	 * @return true if successfully removed.
	 */
	@Override
	public boolean removeValue(String key) {
		return Labyrinth.getContainer(this.key).delete(key);
	}

	/**
	 * Get a full roster of clan enemies by clan id
	 *
	 * @return A string list of clan ids
	 */
	@Override
	public synchronized @NotNull List<String> getEnemyList() {
		if (!ClansAPI.getData().CLAN_ENEMY_MAP.containsKey(clanID)) {
			FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
			return new ArrayList<>(clan.getConfig().getStringList("enemies"));
		}
		return ClansAPI.getData().CLAN_ENEMY_MAP.get(clanID);
	}

	/**
	 * Get a full list of all current clan's attempting positive relation with us.
	 *
	 * @return A string list of clan ally requests by clan id.
	 */
	@Override
	public synchronized @NotNull List<String> getAllyRequests() {
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		return new ArrayList<>(clan.getConfig().getStringList("ally-requests"));
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

	/**
	 * Get an array of information for the clan
	 *
	 * @return String array containing clan stats
	 */
	@Override
	public synchronized String[] getClanInfo() {

		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		List<String> array = new ArrayList<>();
		String password = clan.getConfig().getString("password");
		String owner = clan.getConfig().getString("owner");
		List<String> members = clan.getConfig().getStringList("members");
		List<String> mods = clan.getConfig().getStringList("moderators");
		List<String> admins = clan.getConfig().getStringList("admins");
		List<String> allies = clan.getConfig().getStringList("allies");
		List<String> enemies = clan.getConfig().getStringList("enemies");
		String status = "LOCKED";
		if (password == null)
			status = "OPEN";
		array.add(" ");
		array.add("&2&lClan&7: " + getColor() + action.getClanTag(clanID));
		array.add("&f&m---------------------------");
		array.add("&2Description: &7" + getDescription());
		array.add("&2" + action.getRankTag("Owner") + ": &f" + Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName());
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
		array.add("&2" + action.getRankTag("Admin") + "s [&b" + admins.size() + "&2]");
		array.add("&2" + action.getRankTag("Moderator") + "s [&e" + mods.size() + "&2]");
		array.add("&2Claims [&e" + getOwnedClaimsList().length + "&2]");
		array.add("&f&m---------------------------");
		if (allies.isEmpty())
			array.add("&2Allies [&b" + "0" + "&2]");
		if (allies.size() > 0) {
			array.add("&2Allies [&b" + allies.size() + "&2]");
			for (String clanId : allies) {
				array.add("&f- &e&o" + action.getClanTag(clanId));
			}
		}
		for (String clanId : action.getAllClanIDs()) {
			if (getEnemyList().contains(clanID)) {
				enemies.add(clanId);
			}
		}
		if (enemies.isEmpty())
			array.add("&2Enemies [&b" + "0" + "&2]");
		if (enemies.size() > 0) {
			array.add("&2Enemies [&b" + enemies.size() + "&2]");
			for (String clanId : enemies) {
				array.add("&f- &c&o" + action.getClanTag(clanId));
			}
		}
		array.add("&f&m---------------------------");
		List<String> names = new ArrayList<>();
		for (String u : members) {
			names.add(Bukkit.getOfflinePlayer(UUID.fromString(u)).getName());
		}
		array.add("&n" + action.getRankTag("Member") + "s&r [&7" + members.size() + "&r] - " + names.toString());
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

	/**
	 * Get a specified cooldown from cache
	 *
	 * @param action The label to search for
	 * @return The clans cooldown information for the given action
	 */
	public ClanCooldown getCooldown(String action) {
		ClanCooldown target = null;
		for (ClanCooldown c : getCooldowns()) {
			if (c.getAction().equals(action)) {
				target = c;
			}
		}
		return target;
	}

	/**
	 * Get a clans cooldown cache
	 *
	 * @return A collection of cooldown objects for the clan
	 */
	@Override
	public @NotNull List<ClanCooldown> getCooldowns() {
		return ClansAPI.getData().COOLDOWNS.stream().sequential().filter(c -> c.getId().equals(clanID)).collect(Collectors.toList());
	}

	/**
	 * Send a message to the clan
	 *
	 * @param message String to broadcast.
	 */
	@Override
	public void broadcast(String message) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (ClansAPI.getInstance().isInClan(p.getUniqueId()) && ClansAPI.getInstance().getClanID(p.getUniqueId()).equals(getId())) {
				p.sendMessage(action.color("&7[&6&l" + getName() + "&7] " + message));
			}
		}
	}

	/**
	 * Format a given double into different configured language types
	 *
	 * @param amount double to format
	 * @return Gets the formatted result as a local.
	 */
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

	/**
	 * Claim the target chunk for this clan if possible.
	 *
	 * @param c The target chunk
	 * @return The newly claimed chunk or null if unable to claim.
	 */
	@Override
	public @Nullable Claim obtain(Chunk c) {
		Claim claim = null;
		if (!ClansAPI.getInstance().getClaimManager().isInClaim(c)) {
			int chunkCount = 0;
			for (Chunk chunk : Claim.action.getChunksAroundChunk(c, -1, 0, 1).stream().filter(chunk -> ClansAPI.getInstance().getClaimManager().isInClaim(chunk.getX(), chunk.getZ(), chunk.getWorld().getName())).collect(Collectors.toList())) {
				if (isOwner(chunk)) {
					chunkCount++;
				}
			}
			if (ClansAPI.getData().getEnabled("Clans.land-claiming.claim-connections")) {
				if (getOwnedClaimsList().length >= 1 && chunkCount == 0) {
					return null;
				}
			}
			String claimID = new RandomID(6, "AKZ0123456789").generate();
			int x = c.getX();
			int z = c.getZ();
			String world = c.getWorld().getName();
			FileManager d = ClansAPI.getInstance().getClaimManager().getFile();
			d.getConfig().set(getId().toString() + ".Claims." + claimID + ".X", x);
			d.getConfig().set(getId().toString() + ".Claims." + claimID + ".Z", z);
			d.getConfig().set(getId().toString() + ".Claims." + claimID + ".World", world);
			d.saveConfig();
			ClansAPI.getInstance().getClaimManager().refresh();
			broadcast(Claim.action.claimed(x, z, world));
			claim = ClansAPI.getInstance().getClaimManager().getClaim(claimID);
			new Vent.Call<>(Vent.Runtime.Synchronous, new LandClaimedEvent(null, claim)).run();
		}
		return claim;
	}

	/**
	 * Give the clan some power
	 *
	 * @param amount double amount to give
	 */
	@Override
	public synchronized void givePower(double amount) {
		FileManager c = DataManager.FileType.CLAN_FILE.get(clanID);
		if (!c.getConfig().isDouble("bonus")) {
			c.getConfig().set("bonus", 0.0);
			c.refreshConfig();
		}
		double current = c.getConfig().getDouble("bonus") + amount;
		this.powerBonus = current;
		c.getConfig().set("bonus", current);
		c.refreshConfig();
		broadcast("&a&oNew power was gained. The clan grows stronger..");
		System.out.println(String.format("[%s] - Gave " + '"' + amount + '"' + " power to clan " + '"' + clanID + '"', ClansPro.getInstance().getDescription().getName()));
	}

	/**
	 * Take some power from the clan
	 *
	 * @param amount double amount to take
	 */
	@Override
	public synchronized void takePower(double amount) {
		FileManager c = DataManager.FileType.CLAN_FILE.get(clanID);
		if (!c.getConfig().isDouble("bonus")) {
			c.getConfig().set("bonus", 0.0);
			c.refreshConfig();
		}
		double current = c.getConfig().getDouble("bonus") - amount;
		this.powerBonus = current;
		c.getConfig().set("bonus", current);
		c.refreshConfig();
		broadcast("&c&oPower was stolen from us.. we need to earn it back");
		System.out.println(String.format("[%s] - Took " + '"' + amount + '"' + " power from clan " + '"' + clanID + '"', ClansPro.getInstance().getDescription().getName()));
	}

	/**
	 * Add to the clans max claim's.
	 *
	 * @param amount double amount to give
	 */
	@Override
	public synchronized void addMaxClaim(int amount) {
		FileManager c = DataManager.FileType.CLAN_FILE.get(clanID);
		if (!c.getConfig().isDouble("claim-bonus")) {
			c.getConfig().set("claim-bonus", 0.0);
			c.refreshConfig();
		}
		int current = c.getConfig().getInt("claim-bonus");
		c.getConfig().set("claim-bonus", (current + amount));
		c.refreshConfig();
		broadcast("&a&oNew power was gained. The clan grows stronger..");
		System.out.println(String.format("[%s] - Gave " + '"' + amount + '"' + " claim(s) to clan " + '"' + clanID + '"', ClansPro.getInstance().getDescription().getName()));
	}

	/**
	 * Take from the clans max claim's.
	 *
	 * @param amount double amount to take
	 */
	@Override
	public synchronized void takeMaxClaim(int amount) {
		FileManager c = DataManager.FileType.CLAN_FILE.get(clanID);
		if (!c.getConfig().isDouble("claim-bonus")) {
			c.getConfig().set("claim-bonus", 0.0);
			c.refreshConfig();
		}
		int current = c.getConfig().getInt("claim-bonus");
		c.getConfig().set("claim-bonus", (current - amount));
		c.refreshConfig();
		broadcast("&c&oPower was stolen from us.. we need to earn it back");
		System.out.println(String.format("[%s] - Took " + '"' + amount + '"' + " claim(s) from clan " + '"' + clanID + '"', ClansPro.getInstance().getDescription().getName()));
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

	/**
	 * Send a target clan an ally request.
	 *
	 * @param targetClanID The target clan to request positive relation with.
	 */
	@Override
	public synchronized void sendAllyRequest(HUID targetClanID) {
		FileManager clan = DataManager.FileType.CLAN_FILE.get(targetClanID.toString());
		if (getAllyRequests().contains(targetClanID.toString())) {
			addAlly(targetClanID);
			return;
		}
		if (clan.getConfig().getStringList("ally-requests").contains(clanID)) {
			broadcast(DefaultClan.action.waiting(DefaultClan.action.getClanTag(targetClanID.toString())));
			return;
		}
		List<String> allies = clan.getConfig().getStringList("ally-requests");
		allies.add(clanID);
		clan.getConfig().set("ally-requests", allies);
		clan.saveConfig();
		Clan clanIndex = ClansAPI.getInstance().getClan(targetClanID.toString());
		broadcast(DefaultClan.action.allianceRequested());
		clanIndex.broadcast(DefaultClan.action.allianceRequestedOut(getName(), DefaultClan.action.getClanTag(clanID)));
	}

	/**
	 * Force alliance with a specified clan.
	 *
	 * @param targetClanID The target clan to ally.
	 */
	@Override
	public synchronized void addAlly(HUID targetClanID) {
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		FileManager clan2 = DataManager.FileType.CLAN_FILE.get(targetClanID.toString());
		if (getAllyRequests().contains(targetClanID.toString())) {
			List<String> allyRequests = getAllyRequests();
			allyRequests.remove(targetClanID.toString());
			clan.getConfig().set("ally-requests", allyRequests);
			clan.saveConfig();
		}
		List<String> allies = getAllyList();
		Clan target = ClansAPI.getInstance().getClan(targetClanID.toString());
		List<String> allies2 = target.getAllyList();
		allies.add(targetClanID.toString());
		allies2.add(clanID);
		clan.getConfig().set("allies", allies);
		clan.saveConfig();
		clan2.getConfig().set("allies", allies2);
		clan2.saveConfig();
		broadcast(DefaultClan.action.ally(target.getName()));
		target.broadcast(DefaultClan.action.ally(getName()));
		ClansAPI.getData().CLAN_ALLY_MAP.put(clanID, allies);
	}

	/**
	 * Force alliance removal from a specified clan for this clan only.
	 * <p>
	 * Its possible you will need to also run this method from the targeted clan.
	 *
	 * @param targetClanID The target clan to make neutral.
	 */
	@Override
	public synchronized void removeAlly(HUID targetClanID) {
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		List<String> allies = getAllyList();
		allies.remove(targetClanID.toString());
		clan.getConfig().set("allies", allies);
		clan.saveConfig();
		ClansAPI.getData().CLAN_ALLY_MAP.put(clanID, allies);
	}

	/**
	 * Force enemy relation with a specified clan.
	 *
	 * @param targetClanID The target clan to make enemies with.
	 */
	@Override
	public synchronized void addEnemy(HUID targetClanID) {
		Clan target = ClansAPI.getInstance().getClan(targetClanID.toString());
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		if (getAllyList().contains(targetClanID.toString())) {
			removeAlly(targetClanID);
			target.removeAlly(getId());
		}
		List<String> enemies = getEnemyList();
		enemies.add(targetClanID.toString());
		clan.getConfig().set("enemies", enemies);
		clan.saveConfig();
		broadcast(DefaultClan.action.enemy(target.getName()));
		target.broadcast(DefaultClan.action.enemy(getName()));
		ClansAPI.getData().CLAN_ENEMY_MAP.put(clanID, enemies);
	}

	/**
	 * Force the removal of a specified enemy for this clan only.
	 * <p>
	 * Its possible you will need to also run this method from the targeted clan.
	 *
	 * @param targetClanID The target clan to make neutral.
	 */
	@Override
	public synchronized void removeEnemy(HUID targetClanID) {
		Clan target = ClansAPI.getInstance().getClan(targetClanID.toString());
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		List<String> enemies = getEnemyList();
		enemies.remove(targetClanID.toString());
		clan.getConfig().set("enemies", enemies);
		clan.saveConfig();
		DefaultClan clanIndex = new DefaultClan(clanID);
		DefaultClan clanIndex2 = new DefaultClan(targetClanID.toString());
		clanIndex.broadcast(DefaultClan.action.neutral(target.getName()));
		clanIndex2.broadcast(DefaultClan.action.neutral(getName()));
		ClansAPI.getData().CLAN_ENEMY_MAP.put(clanID, enemies);
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
		return Objects.hash(getClanID());
	}

	private ClanBank getBank() {
		if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
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
