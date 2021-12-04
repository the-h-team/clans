package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.api.BanksAPI;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.ClearanceLog;
import com.github.sanctum.clans.construct.api.InvasiveEntity;
import com.github.sanctum.clans.construct.api.LogoHolder;
import com.github.sanctum.clans.construct.api.PersistentEntity;
import com.github.sanctum.clans.construct.api.Relation;
import com.github.sanctum.clans.construct.api.Savable;
import com.github.sanctum.clans.construct.api.Teleport;
import com.github.sanctum.clans.construct.extra.BukkitColor;
import com.github.sanctum.clans.construct.extra.ClanRelationElement;
import com.github.sanctum.clans.event.associate.AssociateObtainLandEvent;
import com.github.sanctum.clans.event.command.ClanInformationAdaptEvent;
import com.github.sanctum.clans.event.insignia.InsigniaBuildCarrierEvent;
import com.github.sanctum.clans.event.player.PlayerJoinClanEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.annotation.Ordinal;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.data.DataTable;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.data.MemorySpace;
import com.github.sanctum.labyrinth.data.Node;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.formatting.UniformedComponents;
import com.github.sanctum.labyrinth.formatting.string.RandomHex;
import com.github.sanctum.labyrinth.formatting.string.RandomID;
import com.github.sanctum.labyrinth.interfacing.OrdinalProcedure;
import com.github.sanctum.labyrinth.library.Entities;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.NamespacedKey;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.Schedule;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attributable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SerializableAs("Clan")
public final class DefaultClan implements Clan, PersistentEntity {

	private final String clanID;
	private boolean peaceful;
	private boolean friendlyfire;
	private double powerBonus;
	private double claimBonus;
	private String password;
	private String name;
	private String customName;
	private String description;
	private Location base;
	transient NamespacedKey key;
	private final Set<Associate> associates = Collections.synchronizedSet(new LinkedHashSet<>());
	private final Set<Claim> claims = new HashSet<>();
	private final List<String> allies = new ArrayList<>();
	private final List<String> enemies = new ArrayList<>();
	private final List<String> requests = new ArrayList<>();
	private final UniformedComponents<Clan> allyList;
	private final UniformedComponents<Clan> enemyList;
	private final Color palette;
	private final Tag tag;
	private final Relation dock;

	/**
	 * @deprecated for internal use only!!!
	 */
	@Deprecated
	public DefaultClan() {
		this.clanID = "dummy";
		this.allyList = UniformedComponents.accept(new ArrayList<>());
		this.enemyList = UniformedComponents.accept(new ArrayList<>());
		this.palette = null;
		this.tag = null;
		this.dock = null;
	}

	public DefaultClan(String clanID) {
		this.clanID = clanID;
		this.dock = new Relation() {

			private final Alliance alliance;
			private final Rivalry rivalry;

			{
				this.alliance = new Alliance() {
					@Override
					public @NotNull List<InvasiveEntity> get() {
						return getAllies();
					}

					@Override
					public @NotNull <T extends InvasiveEntity> List<T> get(Class<T> cl) {
						return getAllies(cl);
					}

					@Override
					public void request(InvasiveEntity target) {
						requestAlliance(target);
					}

					@Override
					public void request(InvasiveEntity target, String message) {
						requestAlliance(target, message);
					}

					@Override
					public @NotNull List<InvasiveEntity> getRequests() {
						return getAllyRequests();
					}

					@Override
					public @NotNull <T extends InvasiveEntity> List<T> getRequests(Class<T> cl) {
						return getAllyRequests(cl);
					}

					@Override
					public Teleport getTeleport(InvasiveEntity entity) {
						return getTeleportation(entity);
					}

					@Override
					public boolean isEmpty() {
						return get().isEmpty();
					}

					@Override
					public InvasiveEntity[] toArray() {
						return get().toArray(new InvasiveEntity[0]);
					}

					@Override
					public <T extends InvasiveEntity> T[] toArray(T[] a) {
						return stream().map(entity -> (T) entity).collect(Collectors.toList()).toArray(a);
					}

					@Override
					public <T extends InvasiveEntity> boolean has(T o) {
						return get().contains(o);
					}

					@Override
					public <T extends InvasiveEntity> boolean hasAll(Collection<T> c) {
						return get().containsAll(c);
					}

					@Override
					public <T extends InvasiveEntity> boolean addAll(Collection<T> c) {
						for (T t : c) {
							if (has(t)) {
								return false;
							} else {
								add(t);
							}
						}
						return true;
					}

					@Override
					public <T extends InvasiveEntity> boolean removeAll(Collection<T> c) {
						for (T t : c) {
							if (!has(t)) {
								return false;
							} else {
								remove(t);
							}
						}
						return true;
					}

					@Override
					public boolean add(InvasiveEntity entity) {
						if (has(entity)) return false;
						addAlly(entity);
						return true;
					}

					@Override
					public boolean remove(InvasiveEntity entity) {
						if (!has(entity)) return false;
						removeAlly(entity);
						return true;
					}

					@Override
					public int size() {
						return get().size();
					}

					@Override
					public void clear() {
						allies.clear();
					}
				};
				this.rivalry = new Rivalry() {
					@Override
					public @NotNull List<InvasiveEntity> get() {
						return getEnemies();
					}

					@Override
					public @NotNull <T extends InvasiveEntity> List<T> get(Class<T> cl) {
						return getEnemies(cl);
					}

					@Override
					public Teleport getTeleport(InvasiveEntity entity) {
						return getTeleportation(entity);
					}

					@Override
					public boolean isEmpty() {
						return get().isEmpty();
					}

					@Override
					public InvasiveEntity[] toArray() {
						return get().toArray(new InvasiveEntity[0]);
					}

					@Override
					public <T extends InvasiveEntity> T[] toArray(T[] a) {
						return stream().map(entity -> (T) entity).collect(Collectors.toList()).toArray(a);
					}

					@Override
					public <T extends InvasiveEntity> boolean has(T o) {
						return get().contains(o);
					}

					@Override
					public <T extends InvasiveEntity> boolean hasAll(Collection<T> c) {
						return get().containsAll(c);
					}

					@Override
					public <T extends InvasiveEntity> boolean addAll(Collection<T> c) {
						for (T t : c) {
							if (has(t)) {
								return false;
							} else {
								add(t);
							}
						}
						return true;
					}

					@Override
					public <T extends InvasiveEntity> boolean removeAll(Collection<T> c) {
						for (T t : c) {
							if (!has(t)) {
								return false;
							} else {
								remove(t);
							}
						}
						return true;
					}

					@Override
					public boolean add(InvasiveEntity entity) {
						if (has(entity)) return false;
						addEnemy(entity);
						return true;
					}

					@Override
					public boolean remove(InvasiveEntity entity) {
						if (!has(entity)) return false;
						removeEnemy(entity);
						return true;
					}

					@Override
					public int size() {
						return get().size();
					}

					@Override
					public void clear() {
						enemies.clear();
					}
				};
			}

			@NotNull List<InvasiveEntity> getAllies() {
				return DefaultClan.this.getAllies().map(clan -> (InvasiveEntity) clan).collect(Collectors.toList());
			}

			@NotNull List<InvasiveEntity> getAllyRequests() {
				return DefaultClan.this.getAllyRequests().stream().map(clan -> (InvasiveEntity) clan).collect(Collectors.toList());
			}

			@NotNull List<InvasiveEntity> getEnemies() {
				return DefaultClan.this.getEnemies().map(clan -> (InvasiveEntity) clan).collect(Collectors.toList());
			}

			<T extends InvasiveEntity> List<T> getAllies(Class<T> cl) {
				return DefaultClan.this.getAllies().filter(clan -> cl.isAssignableFrom(clan.getClass())).map(clan -> (T) clan).collect(Collectors.toList());
			}

			<T extends InvasiveEntity> List<T> getAllyRequests(Class<T> cl) {
				return DefaultClan.this.getAllyRequests().stream().filter(clan -> cl.isAssignableFrom(clan.getClass())).map(clan -> (T) clan).collect(Collectors.toList());
			}

			<T extends InvasiveEntity> List<T> getEnemies(Class<T> cl) {
				return DefaultClan.this.getEnemies().filter(clan -> cl.isAssignableFrom(clan.getClass())).map(clan -> (T) clan).collect(Collectors.toList());
			}

			@Override
			public @NotNull Alliance getAlliance() {
				return this.alliance;
			}

			@Override
			public @NotNull Rivalry getRivalry() {
				return this.rivalry;
			}

			@Override
			public @NotNull InvasiveEntity getEntity() {
				return DefaultClan.this;
			}

			Teleport getTeleportation(InvasiveEntity entity) {
				if (!entity.isAssociate()) return null;
				return Teleport.get(entity.getAsAssociate());
			}

			void requestAlliance(InvasiveEntity target) {
				if (!(target instanceof Clan)) return;
				sendAllyRequest(HUID.fromString(target.getTag().getId()));
			}

			void requestAlliance(InvasiveEntity target, String message) {
				if (!(target instanceof Clan)) return;
				sendAllyRequest(HUID.fromString(target.getTag().getId()), message);
			}

			void addAlly(InvasiveEntity target) {
				if (!(target instanceof Clan)) return;
				DefaultClan.this.addAlly(HUID.fromString(target.getTag().getId()));
			}

			void removeAlly(InvasiveEntity target) {
				if (!(target instanceof Clan)) return;
				DefaultClan.this.removeAlly(HUID.fromString(target.getTag().getId()));
			}

			void addEnemy(InvasiveEntity target) {
				if (!(target instanceof Clan)) return;
				DefaultClan.this.addEnemy(HUID.fromString(target.getTag().getId()));
			}

			void removeEnemy(InvasiveEntity target) {
				if (!(target instanceof Clan)) return;
				DefaultClan.this.removeEnemy(HUID.fromString(target.getTag().getId()));
			}

			@Override
			public boolean isNeutral(InvasiveEntity target) {
				return DefaultClan.this.isNeutral(HUID.fromString(target.getTag().getId()));
			}
		};
		this.tag = () -> DefaultClan.this.getId().toString();
		this.key = new NamespacedKey(ClansAPI.getInstance().getPlugin(), clanID);
		this.allyList = new ClanRelationElement(this, ClanRelationElement.RelationType.Ally);
		this.enemyList = new ClanRelationElement(this, ClanRelationElement.RelationType.Enemy);
		boolean isNew = LabyrinthProvider.getService(Service.LEGACY).isNew();
		this.palette = new Color(this);
		if (isNew) {
			palette.set(new RandomHex());
		} else {
			palette.setStart(BukkitColor.random().toCode());
		}
		FileManager c = ClansAPI.getDataInstance().getClanFile(this);
		if (c.read(f -> f.exists() && f.getString("name") != null)) {
			this.name = c.read(f -> f.getString("name"));
			if (c.read(f -> f.isString("display-name"))) {
				this.customName = c.read(f -> f.getString("display-name"));
			}
			if (c.read(f -> f.isString("name-color"))) {
				getPalette().setStart(c.getRoot().getString("name-color"));
				c.write(t -> t.set("name-color", null)
						.set("color.start", getPalette().toString()));
			}

			if (c.read(f -> f.isString("color.start"))) {
				getPalette().setStart(c.getRoot().getString("color.start"));
				if (c.read(f -> f.isString("color.end"))) {
					getPalette().setEnd(c.getRoot().getString("color.end"));
				}
			}

			if (c.read(f -> f.isString("password"))) {
				this.password = c.getRoot().getString("password");
			}

			if (c.read(f -> f.isDouble("bonus"))) {
				this.powerBonus = c.getRoot().getDouble("bonus");
			}

			if (c.read(f -> f.isDouble("claim-bonus"))) {
				this.claimBonus = c.getRoot().getDouble("claim-bonus");
			}

			if (c.read(f -> f.isString("description"))) {
				this.description = c.getRoot().getString("description");
			}

			allies.addAll(c.read(f -> f.getStringList("allies")));

			enemies.addAll(c.read(f -> f.getStringList("enemies")));

			requests.addAll(c.read(f -> f.getStringList("ally-requests")));

			if (c.read(f -> f.isNode("base"))) {
				if (!c.read(f -> f.isLocation("base"))) {
					Node base = c.getRoot().getNode("base");

					double x = base.getNode("x").toPrimitive().getDouble();
					double y = base.getNode("y").toPrimitive().getDouble();
					double z = base.getNode("z").toPrimitive().getDouble();
					float yaw = base.getNode("float").toPrimitive().getFloatList().get(0);
					float pitch = base.getNode("float").toPrimitive().getFloatList().get(1);
					World w = Bukkit.getWorld(Objects.requireNonNull(c.getRoot().getString("base.world")));
					if (w == null) {
						w = Bukkit.getWorld(Objects.requireNonNull(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.raid-shield.main-world")));
					}
					this.base = new Location(w, x, y, z, yaw, pitch);
					c.write(t -> t.set("base", this.base));
				} else {
					this.base = c.read(f -> f.getLocation("base"));
				}

			} else {
				if (c.read(f -> f.isLocation("base"))) {
					this.base = c.read(f -> f.getLocation("base"));
				}
			}
			if (c.read(f -> f.isNode("members"))) {
				for (String rank : c.read(f -> f.getNode("members").getKeys(false))) {

					Rank priority = Rank.valueOf(rank);
					for (String mem : c.read(f -> f.getStringList("members." + rank))) {
						associates.add(new DefaultAssociate(UUID.fromString(mem), priority, this));
					}
				}
			}

			if (c.read(f -> f.isDouble("bonus"))) {
				this.powerBonus = c.read(f -> f.getDouble("bonus"));
				c.write(t -> t.set("power-bonus", this.powerBonus).set("bonus", null));
			} else {
				this.powerBonus = c.read(f -> f.getDouble("power-bonus"));
			}

			this.peaceful = c.read(f -> f.getBoolean("peaceful"));

			this.friendlyfire = c.read(f -> f.getBoolean("friendlyfire"));

		}

		ClearanceLog clog = getValue(ClearanceLog.class, "clearance");
		if (clog == null) {
			ClearanceLog clearanceLog = new ClearanceLog();
			setValue("clearance", clearanceLog, false);
		}

		if (isNode("hologram")) {
			List<LogoHolder.Carrier> carriersToAdd = new ArrayList<>();
			Node holo = getNode("hologram");
			for (String world : holo.getKeys(false)) {
				Node w = holo.getNode(world);
				for (String ch : w.getKeys(false)) {
					Node chunk = w.getNode(ch);
					for (String carrierID : chunk.getKeys(false)) {
						LogoHolder.Carrier carrier = new Carrier(HUID.fromString(carrierID));
						Node carrierKeys = chunk.getNode(carrierID);
						List<Integer> sortedKeys = carrierKeys.getKeys(false).stream().map(Integer::parseInt).sorted(Integer::compareTo).collect(Collectors.toList());
						int i = 0;
						for (Integer integer : sortedKeys) {
							String l = String.valueOf(integer);
							Location loc = carrierKeys.getNode(l).getNode("location").toBukkit().getLocation();
							if (i == sortedKeys.size() - 1) {
								OrdinalProcedure.select(carrier, 24, loc);
							}
							String name = StringUtils.use(carrierKeys.getNode(l).getNode("content").toPrimitive().getString()).translate();
							InsigniaBuildCarrierEvent event = ClanVentBus.call(new InsigniaBuildCarrierEvent(carrier, i, carrierKeys.getKeys(false).size(), name));
							if (!event.isCancelled()) {
								ArmorStand stand = Entities.ARMOR_STAND.spawn(loc, s -> {
									s.setVisible(false);
									s.setSmall(true);
									s.setMarker(true);
									s.setCustomNameVisible(true);
									s.setCustomName(event.getContent());
								});
								carrier.add(stand);
							}
							i++;
						}
						carriersToAdd.add(carrier);
					}
					carriersToAdd.forEach(station -> {
						if (CACHE.get(station.getChunk().getX() + ";" + station.getChunk().getZ()) == null) {
							List<LogoHolder.Carrier> list = new ArrayList<>();
							list.add(station);
							CACHE.put(station.getChunk().getX() + ";" + station.getChunk().getZ(), list);
						} else {
							CACHE.get(station.getChunk().getX() + ";" + station.getChunk().getZ()).add(station);
						}
					});
				}
			}
		}

	}

	public LogoHolder.Carrier newCarrier(HUID id) {
		return new Carrier(id);
	}

	@Override
	public synchronized @Nullable Associate newAssociate(UUID target) {
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(target).orElse(null);
		if (associate == null) {
			Player test = Bukkit.getPlayer(target);
			if (test != null) {
				PlayerJoinClanEvent event = ClanVentBus.call(new PlayerJoinClanEvent(test, this));
				if (!event.isCancelled()) {
					return new DefaultAssociate(target, Rank.NORMAL, this);
				}
			} else {
				Tag temp = target::toString;
				if (temp.isEntity() && !temp.isPlayer()) {
					return new AnimalAssociate(InvasiveEntity.wrapNonAssociated(temp.getEntity()), Rank.NORMAL, this);
				} else {
					return new DefaultAssociate(target, Rank.NORMAL, this);
				}
			}

		} else {
			ACTION.removePlayer(target);
			return new DefaultAssociate(target, Rank.NORMAL, this);
		}
		return null;
	}

	@Override
	public @Nullable Clan.Associate newAssociate(InvasiveEntity target) {
		if (target.isClan()) return null;
		return newAssociate(UUID.fromString(target.getTag().getId()));
	}

	@Override
	public @Nullable Clan.Associate newAssociate(LabyrinthUser target) {
		return newAssociate(target.getId());
	}

	@Override
	public synchronized boolean kick(Associate target) {
		if (getMembers().stream().noneMatch(c -> c.getId().equals(target.getId()))) {
			return false;
		} else
			getMembers().forEach(c -> {
				if (c.getId().equals(target.getId())) {
					// run the kick event :)
					c.remove();
				}
			});
		return true;
	}

	@Override
	public boolean isValid() {
		return this.clanID != null && this.name != null && ClansAPI.getInstance().getClanManager().getClans().exists(clan -> clan.equals(this));
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
		for (Claim c : getClaims()) {
			if (c.getChunk().equals(chunk)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean transferOwnership(Associate target) {
		if (target == null) return false;
		if (target.getClan().equals(this)) {
			Associate owner = getOwner();
			Associate mem = getMember(m -> m.getId().equals(target.getId()));
			if (mem == null) return false;
			owner.setPriority(Rank.NORMAL);
			mem.setPriority(Rank.HIGHEST);
			save();
			return true;
		}

		return false;
	}

	public boolean isNeutral(HUID targetClanID) {
		return !getAllyList().contains(targetClanID.toString()) && !getEnemyList().contains(targetClanID.toString());
	}

	@Override
	public boolean isCooldown(String action) {
		return getCooldown(action) != null;
	}

	@Override
	public void setName(String newTag) {
		this.name = newTag;
		String format = MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("tag-change"), newTag);
		broadcast(format);
	}

	@Override
	public void setNickname(String newTag) {
		this.customName = newTag;
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
		broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("password-change"), newPassword));
	}

	@Override
	public void setColor(String newColor) {
		getPalette().setStart(newColor);
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

	@Override
	public void setBase(@NotNull Location loc) {
		this.base = loc;
		String format = MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("base-changed"), loc.getWorld().getName());
		broadcast(format);
	}

	@Override
	public synchronized @NotNull HUID getId() {
		return HUID.fromString(clanID);
	}

	@Override
	public synchronized @NotNull("Check that the correct file type is selected in Config.yml/Formatting#file-type") String getName() {
		return this.name;
	}

	@Override
	public @Nullable String getNickname() {
		return this.customName;
	}

	@Override
	public synchronized @NotNull String getDescription() {
		return this.description != null ? this.description : "I have no description.";
	}

	@Override
	public synchronized @NotNull Clan.Associate getOwner() {
		return associates.stream().filter(a -> a.getPriority() == Rank.HIGHEST).findFirst().get();
	}

	@Override
	public Associate getMember(Predicate<Associate> predicate) {
		return associates.stream().filter(predicate).findFirst().orElse(null);
	}

	@Override
	public synchronized @Nullable String getPassword() {
		return this.password;
	}

	@Override
	public @NotNull Clan.Color getPalette() {
		return this.palette;
	}

	public synchronized String[] getMemberIds() {
		return getMembers().stream().map(Associate::getId).map(UUID::toString).toArray(String[]::new);
	}

	@Override
	public @NotNull Set<Associate> getMembers() {
		return Collections.unmodifiableSet(this.associates);
	}

	@Override
	public synchronized void save() {
		if (!getOwner().isServer()) {
			FileManager file = ClansAPI.getDataInstance().getClanFile(this);
			DataTable table = DataTable.newTable();
			table.set("name", getName()).set("base", getBase());
			if (customName != null) {
				table.set("display-name", customName);
			}
			if (getPalette().isGradient()) {
				table.set("color.start", getPalette().toArray()[0]).set("color.end", getPalette().toArray()[1]);
			} else {
				if (file.getRoot().getNode("color.end").toPrimitive().isString()) {
					table.set("color.end", null);
				}
				table.set("color.start", getPalette().toString());
			}
			table.set("description", getDescription())
					.set("password", getPassword())
					.set("power-bonus", this.powerBonus)
					.set("claim-bonus", this.claimBonus)
					.set("peaceful", this.peaceful)
					.set("friendlyfire", this.friendlyfire)
					.set("allies", allies)
					.set("enemies", enemies);
			if (!getAllyRequests().isEmpty()) {
				table.set("ally-requests", requests);
			}
			Map<Rank, List<Associate>> map = new HashMap<>();

			for (Rank v : Rank.values()) {
				map.put(v, new ArrayList<>());
			}

			for (Associate ass : getMembers()) {
				List<Associate> list = map.get(ass.getPriority());
				if (!ass.isTamable()) {
					list.add(ass);
					map.put(ass.getPriority(), list);
				}
			}

			for (Map.Entry<Rank, List<Associate>> entry : map.entrySet()) {
				table.set("members." + entry.getKey().name(), entry.getValue().stream().map(Associate::getId).map(UUID::toString).collect(Collectors.toList()));
			}

			file.write(table);

			Node holo = getNode("hologram");
			holo.set(null);
			getCarriers().forEach(carrier -> {
				Node world = holo.getNode(carrier.getChunk().getWorld().getName());
				Node c = world.getNode(carrier.getChunk().getX() + ";" + carrier.getChunk().getZ());
				Node stat = c.getNode(OrdinalProcedure.select(carrier, 0).cast(() -> HUID.class).toString());
				for (LogoHolder.Carrier.Line l : carrier.getLines()) {
					Node line = stat.getNode(String.valueOf(l.getIndex()));
					line.getNode("location").set(l.getStand().getLocation());
					line.getNode("content").set(l.getStand().getCustomName());
				}
				holo.save();
			});
		}

	}


	@Override
	public void remove(LogoHolder.Carrier carrier) {
		if (carrier.getHolder().equals(this)) {
			carrier.getLines().forEach(LogoHolder.Carrier.Line::destroy);
			CACHE.get(carrier.getChunk().getX() + ";" + carrier.getChunk().getZ()).remove(carrier);
			getNode("hologram").getNode(carrier.getChunk().getWorld().getName()).getNode(carrier.getChunk().getX() + ";" + carrier.getChunk().getZ()).getNode(OrdinalProcedure.select(carrier, 0).cast(() -> HUID.class).toString()).set(null);
			getNode("hologram").getNode(carrier.getChunk().getWorld().getName()).save();
		}
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
		int claimAmount = getClaims().length;
		result = result + add + (claimAmount * multiplier);
		double bonus = this.powerBonus;
		if (ClansAPI.getDataInstance().isTrue("Clans.banks.influence")) {
			if (EconomyProvision.getInstance().isValid()) {
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
	public @NotNull Tag getTag() {
		return tag;
	}

	@Override
	public synchronized Claim[] getClaims() {
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
	public synchronized int getClaimLimit() {
		if (!ClansAPI.getDataInstance().isTrue("Clans.land-claiming.claim-influence.allow")) {
			return 0;
		}
		if (ClansAPI.getDataInstance().getConfigString("Clans.land-claiming.claim-influence.dependence").equalsIgnoreCase("LOW")) {
			this.claimBonus += 13.33;
		}
		if (getBalance() != null) {
			return (int) ((getMemberIds().length + Math.cbrt(getBalance().doubleValue())) + this.claimBonus);
		} else
			return (int) ((getMemberIds().length + Math.cbrt(getPower())) + this.claimBonus);
	}

	public synchronized @NotNull List<String> getAllyList() {
		return this.allies;
	}

	public @NotNull UniformedComponents<Clan> getAllies() {
		return this.allyList;
	}

	public @NotNull UniformedComponents<Clan> getEnemies() {
		return this.enemyList;
	}

	@Override
	public @NotNull List<String> getKeys() {
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
	public void removeValue(String key) {
		LabyrinthProvider.getInstance().getContainer(this.key).delete(key);
	}

	public synchronized @NotNull List<String> getEnemyList() {
		return this.enemies;
	}

	public synchronized @NotNull List<Clan> getAllyRequests() {
		return this.requests.stream().map(HUID::fromString).map(huid -> ClansAPI.getInstance().getClanManager().getClan(huid)).collect(Collectors.toList());
	}

	@Override
	public synchronized int getWins() {
		return getNode("wars-won").toPrimitive().getInt();
	}

	@Override
	public synchronized int getLosses() {
		return getNode("wars-lost").toPrimitive().getInt();
	}

	@Override
	public synchronized String[] getClanInfo() {
		List<String> array = new ArrayList<>();
		String password = this.password;
		List<String> members = getMembers().stream().filter(m -> m.getPriority().toLevel() == 0).map(Associate::getName).collect(Collectors.toList());
		List<String> mods = getMembers().stream().filter(m -> m.getPriority().toLevel() == 1).map(Associate::getName).collect(Collectors.toList());
		List<String> admins = getMembers().stream().filter(m -> m.getPriority().toLevel() == 2).map(Associate::getName).collect(Collectors.toList());
		List<String> allies = getAllies().map(Clan::getId).map(HUID::toString).collect(Collectors.toList());
		List<String> enemies = getEnemies().map(Clan::getId).map(HUID::toString).collect(Collectors.toList());
		String status = "LOCKED";
		if (password == null)
			status = "OPEN";
		array.add(" ");
		array.add("&2&lClan&7: " + getPalette().toString() + ClansAPI.getInstance().getClanManager().getClanName(HUID.fromString(clanID)));
		array.add("&f&m---------------------------");
		array.add("&2Description: &7" + getDescription());
		array.add("&2" + getOwner().getRankFull() + ": &f" + getOwner().getName());
		array.add("&2Status: &f" + status);
		array.add("&2&lPower [&e" + Clan.ACTION.format(getPower()) + "&2&l]");
		if (getBase() != null)
			array.add("&2Base: &aSet");
		if (getBase() == null)
			array.add("&2Base: &7Not set");
		if (isPeaceful())
			array.add("&2Mode: &f&lPEACE");
		if (!isPeaceful())
			array.add("&2Mode: &4&lWAR");
		array.add("&2" + ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Styles.Full.Admin") + "s [&b" + admins.size() + "&2]");
		array.add("&2" + ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Styles.Full.Moderator") + "s [&e" + mods.size() + "&2]");
		array.add("&2Claims [&e" + getClaims().length + "&2]");
		array.add("&f&m---------------------------");
		if (allies.isEmpty())
			array.add("&2Allies [&b" + "0" + "&2]");
		if (allies.size() > 0) {
			array.add("&2Allies [&b" + allies.size() + "&2]");
			for (String clanId : allies) {
				array.add("&f- &e&o" + ClansAPI.getInstance().getClanManager().getClanName(HUID.fromString(clanId)));
			}
		}
		if (enemies.isEmpty())
			array.add("&2Enemies [&b" + "0" + "&2]");
		if (enemies.size() > 0) {
			array.add("&2Enemies [&b" + enemies.size() + "&2]");
			for (String clanId : enemies) {
				array.add("&f- &c&o" + ClansAPI.getInstance().getClanManager().getClanName(HUID.fromString(clanId)));
			}
		}
		array.add("&f&m---------------------------");
		array.add("&n" + ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Styles.Full.Member") + "s&r [&7" + members.size() + "&r] - " + members.toString());
		array.add(" ");
		ClanInformationAdaptEvent event = new Vent.Call<>(Vent.Runtime.Synchronous, new ClanInformationAdaptEvent(array, clanID, ClanInformationAdaptEvent.Type.OTHER)).run();
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
		return ClansAPI.getDataInstance().getCooldowns().stream().sequential().filter(c -> c.getId().equals(clanID)).collect(Collectors.toList());
	}

	@Override
	public void broadcast(String message) {
		getMembers().forEach(a -> {
			if (a.isEntity()) return;
			Optional.ofNullable(a.getUser().toBukkit().getPlayer()).ifPresent(pl -> pl.sendMessage(ACTION.color("&7[&6&l" + getName() + "&7] " + message)));
		});
	}

	@Override
	public void broadcast(BaseComponent... message) {
		getMembers().forEach(a -> {
			if (a.isEntity()) return;
			Optional.ofNullable(a.getUser().toBukkit().getPlayer()).ifPresent(pl -> pl.spigot().sendMessage(message));
		});
	}

	@Override
	public void broadcast(Message... message) {
		getMembers().forEach(a -> {
			if (a.isEntity()) return;
			Optional.ofNullable(a.getUser().toBukkit().getPlayer()).ifPresent(pl -> Arrays.stream(message).forEach(m -> m.send(pl).deploy()));
		});
	}

	@Override
	public void broadcast(Predicate<Associate> predicate, String message) {
		getMembers().forEach(a -> {
			if (a.isEntity()) return;
			if (predicate.test(a)) {
				Optional.ofNullable(a.getUser().toBukkit().getPlayer()).ifPresent(pl -> pl.sendMessage(ACTION.color(message)));
			}
		});
	}

	@Override
	public @Nullable Claim newClaim(Chunk c) {
		Claim claim = null;
		if (!ClansAPI.getInstance().getClaimManager().isInClaim(c)) {
			if (getClaims().length == getClaimLimit()) return null;
			String claimID = new RandomID(6, "AKZ0123456789").generate();
			int x = c.getX();
			int z = c.getZ();
			String world = c.getWorld().getName();
			FileManager d = ClansAPI.getInstance().getClaimManager().getFile();
			DataTable table = DataTable.newTable();
			table.set(getId() + "." + claimID + ".X", x);
			table.set(getId() + "." + claimID + ".Z", z);
			table.set(getId() + "." + claimID + ".World", world);
			d.write(table);
			claim = new DefaultClaim(x, z, clanID, claimID, world, true);
			Claim finalClaim = claim;
			ClansAPI.getInstance().getClaimManager().getFlagManager().getFlags().forEach(finalClaim::register);
			ClansAPI.getInstance().getClaimManager().load(claim);
			broadcast(Claim.ACTION.claimed(x, z, world));
			new Vent.Call<>(Vent.Runtime.Synchronous, new AssociateObtainLandEvent(claim)).run();
		}
		return claim;
	}

	@Override
	public @NotNull Relation getRelation() {
		return this.dock;
	}

	@Override
	public @Nullable Teleport getTeleport() {
		return Teleport.get(this);
	}

	@Override
	public synchronized void givePower(double amount) {
		this.powerBonus += amount;
		broadcast("&fPower: &a+" + amount);
		ClansAPI.getInstance().getPlugin().getLogger().info("- Gave " + '"' + amount + '"' + " power to clan " + '"' + clanID + '"');
	}

	@Override
	public synchronized void takePower(double amount) {
		this.powerBonus -= amount;
		broadcast("&fPower: &c-" + amount);
		ClansAPI.getInstance().getPlugin().getLogger().info("- Took " + '"' + amount + '"' + " power from clan " + '"' + clanID + '"');
	}

	@Override
	public synchronized void giveClaims(int amount) {
		this.claimBonus += amount;
		broadcast("&fClaims: &a+" + amount);
		ClansAPI.getInstance().getPlugin().getLogger().info("- Gave " + '"' + amount + '"' + " claim(s) to clan " + '"' + clanID + '"');
	}

	@Override
	public synchronized void takeClaims(int amount) {
		this.claimBonus -= amount;
		broadcast("&fClaims: &c-" + amount);
		ClansAPI.getInstance().getPlugin().getLogger().info("- Took " + '"' + amount + '"' + " claim(s) from clan " + '"' + clanID + '"');
	}

	@Override
	public synchronized void giveWins(int amount) {
		Node won = getNode("wars-won");
		int current = won.toPrimitive().getInt();
		won.set(current + amount);
		won.save();
	}

	@Override
	public synchronized void takeWins(int amount) {
		Node lost = getNode("wars-lost");
		int current = lost.toPrimitive().getInt();
		lost.set(current - amount);
		lost.save();
	}

	public synchronized void sendAllyRequest(HUID targetClanID) {
		Clan target = ClansAPI.getInstance().getClanManager().getClan(targetClanID);
		if (requests.contains(targetClanID.toString())) {
			addAlly(targetClanID);
			return;
		}
		if (((DefaultClan) target).requests.contains(targetClanID.toString())) {
			broadcast(Clan.ACTION.waiting(ClansAPI.getInstance().getClanManager().getClanName(targetClanID)));
			return;
		}
		((DefaultClan) target).requests.add(clanID);
		broadcast(Clan.ACTION.allianceRequested());
		target.broadcast(Clan.ACTION.allianceRequestedOut(getName(), ClansAPI.getInstance().getClanManager().getClanName(HUID.fromString(clanID))));
	}

	public void sendAllyRequest(HUID targetClanID, String message) {
		Clan target = ClansAPI.getInstance().getClanManager().getClan(targetClanID);
		if (requests.contains(targetClanID.toString())) {
			addAlly(targetClanID);
			return;
		}
		if (((DefaultClan) target).requests.contains(clanID)) {
			broadcast(Clan.ACTION.waiting(ClansAPI.getInstance().getClanManager().getClanName(targetClanID)));
			return;
		}
		Clan clanIndex = ClansAPI.getInstance().getClanManager().getClan(targetClanID);
		((DefaultClan) target).requests.add(clanID);
		broadcast(Clan.ACTION.allianceRequested());
		clanIndex.broadcast(message);
	}

	public synchronized void addAlly(HUID targetClanID) {
		if (requests.contains(targetClanID.toString())) {
			requests.remove(targetClanID.toString());
		}
		Clan target = ClansAPI.getInstance().getClanManager().getClan(targetClanID);
		allies.add(targetClanID.toString());
		((DefaultClan) target).allies.add(clanID);
		broadcast(Clan.ACTION.ally(target.getName()));
		target.broadcast(Clan.ACTION.ally(getName()));
	}

	public synchronized void removeAlly(HUID targetClanID) {
		Clan target = ClansAPI.getInstance().getClanManager().getClan(targetClanID);
		((DefaultClan) target).allies.remove(clanID);
		allies.remove(targetClanID.toString());
	}

	public synchronized void addEnemy(HUID targetClanID) {
		Clan target = ClansAPI.getInstance().getClanManager().getClan(targetClanID);
		if (getAllyList().contains(targetClanID.toString())) {
			removeAlly(targetClanID);
		}
		enemies.add(targetClanID.toString());
		broadcast(Clan.ACTION.enemy(target.getName()));
		target.broadcast(Clan.ACTION.enemy(getName()));
	}

	public synchronized void removeEnemy(HUID targetClanID) {
		Clan target = ClansAPI.getInstance().getClanManager().getClan(targetClanID);
		enemies.remove(targetClanID.toString());
		broadcast(Clan.ACTION.neutral(target.getName()));
		target.broadcast(Clan.ACTION.neutral(getName()));
	}

	@Override
	public String toString() {
		return getTag().getId();
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
		return BanksAPI.getInstance().getBank(this);
	}

	@Override
	public boolean deposit(Player player, BigDecimal amount) {
		if (!EconomyProvision.getInstance().isValid()) {
			return false;
		}
		return Objects.requireNonNull(getBank()).deposit(player, amount);
	}

	@Override
	public boolean withdraw(Player player, BigDecimal amount) {
		if (!EconomyProvision.getInstance().isValid()) {
			return false;
		}
		return Objects.requireNonNull(getBank()).withdraw(player, amount);
	}

	@Override
	public boolean has(BigDecimal amount) {
		if (!EconomyProvision.getInstance().isValid()) {
			return false;
		}
		return Objects.requireNonNull(getBank()).has(amount);
	}

	@Override
	public BigDecimal getBalance() {
		if (!EconomyProvision.getInstance().isValid()) {
			return BigDecimal.ZERO;
		}
		return Objects.requireNonNull(getBank()).getBalance();
	}

	@Override
	public boolean setBalance(BigDecimal newBalance) {
		if (!EconomyProvision.getInstance().isValid()) {
			return false;
		}
		return Objects.requireNonNull(getBank()).setBalance(newBalance);
	}

	@Override
	public Implementation getImplementation() {
		return Implementation.DEFAULT;
	}

	@Override
	public String relate(Clan b) {
		String result = "&f&o";
		if (ACTION.getAllClanIDs().contains(b.getId().toString())) {
			if (isNeutral(b.getId())) {
				result = "&f";
			}
			if (getId().equals(b.getId())) {
				result = "&6";
			}
			if (getAllyList().contains(b.getId().toString())) {
				result = "&a";
			}
			if (getEnemyList().contains(b.getId().toString())) {
				result = "&c";
			}
		}
		return result;
	}

	@Override
	public String getPath() {
		return getId().toString();
	}

	@Override
	public boolean isNode(String key) {
		return ClansAPI.getDataInstance().getClanFile(this).getRoot().isNode(key);
	}

	@Override
	public Node getNode(String key) {
		return ClansAPI.getDataInstance().getClanFile(this).getRoot().getNode(key);
	}

	@Override
	public Set<String> getKeys(boolean deep) {
		return ClansAPI.getDataInstance().getClanFile(this).getRoot().getKeys(deep);
	}

	@Override
	public Map<String, Object> getValues(boolean deep) {
		return ClansAPI.getDataInstance().getClanFile(this).getRoot().getValues(deep);
	}

	public List<String> getLogo() {
		return DefaultClan.this.getValue("logo");
	}

	public List<LogoHolder.Carrier> getCarriers() {
		List<LogoHolder.Carrier> list = new ArrayList<>();
		for (List<LogoHolder.Carrier> s : CACHE.values()) {
			s.forEach(c -> {
				if (c.getHolder().equals(this)) {
					list.add(c);
				}
			});
		}
		return list;
	}

	public List<LogoHolder.Carrier> getCarriers(Chunk chunk) {
		return CACHE.computeIfAbsent(chunk.getX() + ";" + chunk.getZ(), chunk1 -> new ArrayList<>()).stream().filter(c -> c.getHolder().equals(this)).collect(Collectors.toList());
	}

	@Override
	public LogoHolder.Carrier newCarrier(Location location) {
		double y = location.getY() + 0.5;
		List<String> value = getLogo();
		if (value == null) return null;
		List<String> t = new ArrayList<>(value);
		Collections.reverse(t);
		String[] l = t.toArray(new String[0]);
		Carrier carrier = new Carrier();
		for (int i = 0; i < l.length; i++) {
			y += 0.2;
			Location loc = new Location(location.getWorld(), location.getX(), y, location.getZ(), location.getYaw(), location.getPitch()).add(0.5, 0, 0.5);
			if (i == l.length - 1) {
				carrier.top = loc;
			}
			String name = StringUtils.use(l[i]).translate();
			InsigniaBuildCarrierEvent event = ClanVentBus.call(new InsigniaBuildCarrierEvent(carrier, i, l.length, name));
			if (!event.isCancelled()) {
				ArmorStand stand = Entities.ARMOR_STAND.spawn(loc, s -> {
					s.setVisible(false);
					s.setSmall(true);
					s.setMarker(true);
					s.setCustomNameVisible(true);
					s.setCustomName(event.getContent());
				});
				CACHE.computeIfAbsent(loc.getChunk().getX() + ";" + loc.getChunk().getZ(), k -> new ArrayList<>());
				carrier.add(stand);
			}
		}
		CACHE.get(carrier.getChunk().getX() + ";" + carrier.getChunk().getZ()).add(carrier);
		return carrier;
	}

	@Override
	public void remove() {
		getCarriers().forEach(station -> station.getLines().forEach(LogoHolder.Carrier.Line::destroy));
		Schedule.sync(() -> ClansAPI.getInstance().getClanManager().unload(this)).run();
	}

	@NotNull
	@Override
	public Iterator<InvasiveEntity> iterator() {
		return getMembers().stream().map(associate -> (InvasiveEntity) associate).iterator();
	}

	@Override
	public void forEach(Consumer<? super InvasiveEntity> action) {
		getMembers().forEach(action);
	}

	@Override
	public Spliterator<InvasiveEntity> spliterator() {
		return getMembers().stream().map(associate -> (InvasiveEntity) associate).spliterator();
	}

	@Override
	public Teleport getTeleport(InvasiveEntity entity) {
		return getRelation().getAlliance().getTeleport(entity);
	}

	@Override
	public boolean isEmpty() {
		return getMembers().isEmpty();
	}

	@Override
	public boolean add(InvasiveEntity entity) {
		if (entity.isAssociate()) {
			associates.add(entity.getAsAssociate());
			if (ClansAPI.getInstance().getClanManager().getClans().exists(c -> c.equals(this)) && !entity.isTamable()) {
				if (entity.getMemorySpace().isPresent()) {
					Node join_date = entity.getMemorySpace().get().getNode("join-date");
					Date now = new Date();
					join_date.set(now.getTime());
					join_date.save();
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean remove(InvasiveEntity entity) {
		if (entity.isAssociate()) {
			associates.remove(entity.getAsAssociate());
			return true;
		}
		return false;
	}

	@Override
	public <T extends InvasiveEntity> boolean has(T o) {
		if (o.isAssociate()) {
			return getMembers().contains(o.getAsAssociate());
		}
		return false;
	}

	@Override
	public <T extends InvasiveEntity> boolean hasAll(Collection<T> c) {
		return getMembers().stream().map(associate -> (InvasiveEntity) associate).collect(Collectors.toList()).containsAll(c);
	}

	@Override
	public InvasiveEntity[] toArray() {
		return getMembers().stream().map(associate -> (InvasiveEntity) associate).toArray(InvasiveEntity[]::new);
	}

	@Override
	public <T extends InvasiveEntity> T[] toArray(T[] a) {
		return getMembers().stream().map(associate -> (T) associate).collect(Collectors.toList()).toArray(a);
	}

	@Override
	public <T extends InvasiveEntity> boolean addAll(Collection<T> c) {
		for (T t : c) {
			if (t.isAssociate()) {
				if (associates.contains(t.getAsAssociate())) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public <T extends InvasiveEntity> boolean removeAll(Collection<T> c) {
		for (T t : c) {
			if (t.isAssociate()) {
				if (!has(t)) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public void clear() {
		getMembers().forEach(Savable::remove);
	}

	@Override
	public @NotNull Optional<MemorySpace> getMemorySpace() {
		return Optional.of(this);
	}

	@Override
	public Class<Clan> getClassType() {
		return Clan.class;
	}

	final class Carrier implements LogoHolder.Carrier {

		Location top;
		final Set<LogoHolder.Carrier.Line> lines = Collections.synchronizedSet(new LinkedHashSet<>());
		final HUID id;
		private Chunk chunk;

		public Carrier() {
			this.id = HUID.randomID();
		}

		public Carrier(HUID id) {
			this.id = id;
		}

		public void add(Attributable attributable) throws IllegalArgumentException {
			if (attributable instanceof ArmorStand) {
				Line line = new Line((ArmorStand) attributable);
				if (this.chunk == null) {
					this.chunk = line.getStand().getLocation().getChunk();
				}
				lines.add(line);
			} else throw new IllegalArgumentException("Attribute cannot be used for line processing!");
		}

		@Override
		public Location getTop() {
			return top;
		}

		public Chunk getChunk() {
			return chunk;
		}

		@Override
		public LogoHolder getHolder() {
			return DefaultClan.this;
		}

		@Ordinal
		HUID getRealId() {
			return id;
		}

		@Ordinal(2)
		Clan getClanInstance() {
			return DefaultClan.this;
		}

		@Ordinal(24)
		void setTop(Location top) {
			this.top = top;
		}

		public String getId() {
			return id.toString().replace("-", "").substring(8);
		}

		public Set<LogoHolder.Carrier.Line> getLines() {
			return lines.stream().sorted(Comparator.comparingInt(LogoHolder.Carrier.Line::getIndex)).collect(Collectors.toCollection(LinkedHashSet::new));
		}

		public void remove(LogoHolder.Carrier.Line line) {
			lines.remove(line);
		}

		@Override
		public String toString() {
			return "Carrier{" + "world=" + getChunk().getWorld().getName() + ",x=" + getChunk().getX() + ",z=" + getChunk().getZ() + ",size=" + getLines().size() + ",id=" + getRealId().toString() + '}';
		}

		final class Line implements LogoHolder.Carrier.Line {

			private final ArmorStand line;
			private final int index;

			Line(ArmorStand stand) {
				this.line = stand;
				this.index = Carrier.this.lines.size() + 1;
			}

			@Ordinal(420)
			HUID getRealId() {
				return DefaultClan.this.getId();
			}

			public HUID getId() {
				return Carrier.this.id;
			}

			@Override
			public int getIndex() {
				return index;
			}

			public ArmorStand getStand() {
				return line;
			}

			public void destroy() {
				if (!Carrier.this.getChunk().isLoaded()) {
					Carrier.this.getChunk().load(true);
				}
				getStand().remove();
				Schedule.sync(() -> Carrier.this.remove(this)).run();
			}

		}
	}
}
