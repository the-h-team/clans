package com.github.sanctum.clans.impl;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.clans.event.associate.AssociateObtainLandEvent;
import com.github.sanctum.clans.event.command.ClanInformationAdaptEvent;
import com.github.sanctum.clans.event.player.PlayerJoinClanEvent;
import com.github.sanctum.clans.impl.entity.EntityAssociate;
import com.github.sanctum.clans.impl.entity.PlayerAssociate;
import com.github.sanctum.clans.model.*;
import com.github.sanctum.clans.util.BukkitColor;
import com.github.sanctum.clans.util.ClanRelationList;
import com.github.sanctum.clans.util.InvalidAssociateTypeException;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.data.*;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.formatting.UniformedComponents;
import com.github.sanctum.labyrinth.formatting.string.RandomHex;
import com.github.sanctum.labyrinth.gui.unity.simple.MemoryDocket;
import com.github.sanctum.labyrinth.library.NamespacedKey;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.Teleport;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.annotation.Removal;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherList;
import com.github.sanctum.panther.container.PantherMap;
import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.file.MemorySpace;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.util.HUID;
import com.github.sanctum.panther.util.RandomID;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SerializableAs("Clan")
public final class DefaultClan implements Clan, PersistentEntity {

    private final Configurable dataFile;
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
    private final RankRegistry positionRegistry;
    private final Set<Associate> associates = Collections.synchronizedSet(new LinkedHashSet<>());
    private final Set<Claim> claims = new HashSet<>();
    private final List<String> allies = new ArrayList<>();
    private final List<String> enemies = new ArrayList<>();
    private final List<String> requests = new ArrayList<>();
    private final List<String> info_board;
    private final UniformedComponents<Clan> allyList;
    private final UniformedComponents<Clan> enemyList;
    private final Color palette;
    private final Tag tag;
    private final Relation relationObject;
    private ClearanceOverride clearanceOverride;

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
        this.relationObject = null;
        this.info_board = null;
        this.dataFile = null;
        this.positionRegistry = null;
    }

    public DefaultClan(RankRegistry registry, String clanID, boolean server) {
        this.clanID = clanID;
        this.positionRegistry = registry;
        this.dataFile = ClansAPI.getInstance().getFileList().get(clanID, "Clans", JavaPlugin.getPlugin(ClansJavaPlugin.class).TYPE).getRoot();
        this.info_board = ClansAPI.getDataInstance().getMessages().getRoot().getStringList("info-simple-other");
        this.relationObject = new Relation() {

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
                        if (target.isClan()) {
                            Clan t = target.getAsClan();
                            if (requests.contains(target.getTag().getId())) {
                                add(target);
                                return;
                            }
                            if (((DefaultClan) t).requests.contains(target.getTag().getId())) {
                                broadcast(Clan.ACTION.waiting(target.getName()));
                                return;
                            }
                            ((DefaultClan) t).requests.add(clanID);
                            broadcast(Clan.ACTION.allianceRequested());
                            t.broadcast(Clan.ACTION.allianceRequestedOut(getName(), target.getName()));
                        }
                    }

                    @Override
                    public void request(InvasiveEntity target, String message) {
                        if (target.isClan()) {
                            Clan t = target.getAsClan();
                            if (requests.contains(target.getTag().getId())) {
                                add(target);
                                return;
                            }
                            if (((DefaultClan) t).requests.contains(clanID)) {
                                broadcast(Clan.ACTION.waiting(target.getName()));
                                return;
                            }
                            ((DefaultClan) t).requests.add(clanID);
                            broadcast(Clan.ACTION.allianceRequested());
                            t.broadcast(message);
                        }
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
                        if (!entity.isAssociate()) return null;
                        return Teleport.get(entity.getAsAssociate());
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
                        if (entity.isClan()) {
                            Clan t = entity.getAsClan();
                            requests.remove(t.getId().toString());
                            allies.add(t.getId().toString());
                            ((DefaultClan) t).allies.add(clanID);
                            broadcast(Clan.ACTION.ally(t.getName()));
                            t.broadcast(Clan.ACTION.ally(getName()));
                            return true;
                        } else return false;
                    }

                    @Override
                    public boolean remove(InvasiveEntity entity) {
                        if (!has(entity)) return false;
                        if (entity.isClan()) {
                            Clan target = entity.getAsClan();
                            ((DefaultClan) target).allies.remove(clanID);
                            allies.remove(target.getId().toString());
                            return true;
                        } else return false;
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
                        if (!entity.isAssociate()) return null;
                        return Teleport.get(entity.getAsAssociate());
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
                    public boolean has(@NotNull UUID uuid) {
                        return false;
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
                        if (entity.isClan()) {
                            Clan target = entity.getAsClan();
                            if (getAllyList().contains(target.getId().toString())) {
                                alliance.remove(entity);
                            }
                            enemies.add(target.getId().toString());
                            broadcast(Clan.ACTION.enemy(target.getName()));
                            target.broadcast(Clan.ACTION.enemy(getName()));
                            return true;
                        } else return false;
                    }

                    @Override
                    public boolean remove(InvasiveEntity entity) {
                        if (!has(entity)) return false;
                        if (entity.isClan()) {
                            Clan target = entity.getAsClan();
                            enemies.remove(target.getId().toString());
                            broadcast(Clan.ACTION.neutral(target.getName()));
                            target.broadcast(Clan.ACTION.neutral(getName()));
                            return true;
                        } else return false;
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

            @Override
            public boolean isNeutral(InvasiveEntity target) {
                return DefaultClan.this.isNeutral(HUID.fromString(target.getTag().getId()));
            }
        };
        this.tag = () -> DefaultClan.this.getId().toString();
        this.key = new NamespacedKey(ClansAPI.getInstance().getPlugin(), clanID);
        this.allyList = new ClanRelationList(this, ClanRelationList.RelationType.Ally);
        this.enemyList = new ClanRelationList(this, ClanRelationList.RelationType.Enemy);
        boolean isNew = LabyrinthProvider.getService(Service.LEGACY).isNew();
        this.palette = new Color(this);
        if (isNew && !ClansAPI.getDataInstance().isTrue("Formatting.symbols")) {
            palette.set(new RandomHex());
        } else {
            palette.setStart(BukkitColor.random().toCode());
        }
        removeValue("clearance");
        try {
            ClearanceOverride.ClearanceData clog = getValue(ClearanceOverride.ClearanceData.class, "permissions");
            if (clog == null) {
                if (!server) {
                    resetPermissions();
                }
            } else {
                this.clearanceOverride = new ClearanceOverride(clog);
            }
        } catch (Exception e) {
            resetPermissions();
        }
        if (dataFile.exists() && dataFile.getString("name") != null) {
            this.name = dataFile.getString("name");
            if (dataFile.isString("description")) this.description = dataFile.getString("description");
            if (dataFile.isString("display-name")) this.customName = dataFile.getString("display-name");
            if (dataFile.isString("color.start")) {
                getPalette().setStart(dataFile.getString("color.start"));
                if (dataFile.isString("color.end")) {
                    getPalette().setEnd(dataFile.getString("color.end"));
                }
            }
            if (dataFile.isString("password")) this.password = dataFile.getString("password");
            if (dataFile.isDouble("bonus")) this.powerBonus = dataFile.getDouble("bonus");
            if (dataFile.isDouble("claim-bonus")) this.claimBonus = dataFile.getDouble("claim-bonus");
            allies.addAll(dataFile.getStringList("allies"));
            enemies.addAll(dataFile.getStringList("enemies"));
            requests.addAll(dataFile.getStringList("ally-requests"));
            if (dataFile.getType() == Configurable.Type.JSON) {
                if (dataFile.getNode("base").isNode("org.bukkit.Location")) {
                    this.base = dataFile.getNode("base").toGeneric(BukkitGeneric.class).getLocation();
                }
            } else {
                if (dataFile.getNode("base").get() != null) {
                    this.base = dataFile.getNode("base").toGeneric(BukkitGeneric.class).getLocation();
                }
            }
            if (dataFile.isNode("members")) {
                fixOldRankNames(dataFile);
                PantherMap<String, List<String>> ranksCorrected = new PantherEntryMap<>();
                Node memberNode = dataFile.getNode("members");
                for (String rank : memberNode.getKeys(false)) {
                    Rank priority = positionRegistry.getRank(rank);
                    List<String> members = new ArrayList<>(dataFile.getStringList("members." + rank));
                    if (priority != null) {
                        ranksCorrected.put(rank, members);
                        for (String mem : members) {
                            try {
                                UUID id = UUID.fromString(mem);
                                try {
                                    Associate associate = new PlayerAssociate(id, priority, this);
                                    associates.add(associate);
                                } catch (InvalidAssociateTypeException e) {
                                    ClansAPI.getInstance().getPlugin().getLogger().warning("A UUID with no playerdata in clan " + name + " was found " + '"' + mem + '"');
                                }
                            } catch (IllegalArgumentException e) {
                                ClansAPI.getInstance().getPlugin().getLogger().warning('"' + mem + '"' + " is not a valid UUID. Please remove it from the " + '"' + name + '"' + " data file");
                            }
                        }
                    } else {
                        ClansAPI.getInstance().getPlugin().getLogger().warning("Non-existing rank " + '"' + rank + '"' + " was found with (" + dataFile.getStringList("members." + rank).size() + ") members in clan file " + '"' + clanID + '"' + " Goal: Attempting to clean-up");
                        // introduce config variable for replacement word, help people fix there data files!
                        // look for old-name variable to find rank.
                        String aName = "INTERNALCLANSPLACEHOLDER123";
                        FileManager ranksFile = ClansAPI.getInstance().getFileList().get("Ranks", "Configuration");
                        for (String tag : ranksFile.getRoot().getKeys(false)) {
                            String currentName = ranksFile.getRoot().getNode(tag + ".name").toPrimitive().getString();
                            String oldName = ranksFile.getRoot().getNode(tag + ".old-name").toPrimitive().getString();
                            if (rank.equals(oldName)) {
                                aName = currentName;
                                break;
                            }
                        }
                        Rank actual = positionRegistry.getRank(aName);
                        if (actual == null) {
                            ClansAPI.getInstance().getPlugin().getLogger().severe("Non-existing rank " + '"' + rank + '"' + " was found with (" + dataFile.getStringList("members." + rank).size() + ") members in clan file " + '"' + clanID + '"' + " Goal: Reset associates in tier.");
                            ClansAPI.getInstance().getPlugin().getLogger().severe("Setting associates to member to reduce risk of corruption.");
                            //remove associates from list and into members remove rank from list
                            actual = positionRegistry.getRank(0);
                        }
                        if (ranksCorrected.containsKey(actual.getName())) {
                            List<String> list = ranksCorrected.get(actual.getName());
                            for (String m : list) {
                                if (!list.contains(m)) {
                                    TaskScheduler.of(() -> list.add(m)).schedule();
                                }
                            }
                        } else {
                            ranksCorrected.put(actual.getName(), members);
                        }
                    }
                }
                if (!ranksCorrected.isEmpty()) {
                    memberNode.set(null);
                    ranksCorrected.forEach(e -> memberNode.getNode(e.getKey()).set(e.getValue()));
                    memberNode.save();
                    for (String rank : memberNode.getKeys(false)) {
                        Rank priority = positionRegistry.getRank(rank);
                        List<String> members = new ArrayList<>(dataFile.getStringList("members." + rank));
                        for (String mem : members) {
                            try {
                                UUID id = UUID.fromString(mem);
                                try {
                                    Associate associate = new PlayerAssociate(id, priority, this);
                                    associates.add(associate);
                                } catch (InvalidAssociateTypeException e) {
                                    ClansAPI.getInstance().getPlugin().getLogger().warning("A UUID with no playerdata in clan " + name + " was found " + '"' + mem + '"');
                                }
                            } catch (IllegalArgumentException e) {
                                ClansAPI.getInstance().getPlugin().getLogger().warning('"' + mem + '"' + " is not a valid UUID. Please remove it from the " + '"' + name + '"' + " data file");
                            }
                        }
                    }
                }
            }
            if (dataFile.isDouble("bonus")) {
                this.powerBonus = dataFile.getDouble("bonus");
                dataFile.set("power-bonus", this.powerBonus);
                dataFile.set("bonus", null);
                dataFile.save();
            } else {
                this.powerBonus = dataFile.getDouble("power-bonus");
            }

            this.peaceful = dataFile.getBoolean("peaceful");

            this.friendlyfire = dataFile.getBoolean("friendlyfire");
        }
        if (ClansAPI.getDataInstance().getMessages().read(n -> n.getNode("menu.enabled").toPrimitive().getBoolean())) {
            // load clan specific member list menu
            TaskScheduler.of(() -> {
                Node members = ClansAPI.getDataInstance().getMessages().getRoot().getNode("menu.members");
                MemoryDocket<Associate> docket = new MemoryDocket<>(members);
                docket.setList(() -> new ArrayList<>(associates));
                docket.setComparator(InvasiveEntity.comparingByEntity());
                docket.setNamePlaceholder(":member_name:");
                docket.setUniqueDataConverter(this, Clan.memoryDocketReplacer());
                docket.setDataConverter(Associate.memoryDocketReplacer());
                docket.load();
                DefaultDocketRegistry.load(Clan.memoryDocketReplacer().apply(members.getNode("id").toPrimitive().getString(), this), docket);
            }).scheduleLater(2);
        }
    }

    public DefaultClan(RankRegistry registry, String clanID) {
        this(registry, clanID, false);
    }

    @Deprecated
    @Removal(because = "temporary way to convert ranking system")
    void fixOldRankNames(Configurable c) {
        if (c.getNode("members").getNode("HIGHEST").toPrimitive().isStringList()) {
            // update to new format then read below.
            RankRegistry registry = positionRegistry;
            Rank normal = registry.getLowest();
            c.set("members." + normal.getName(), c.getNode("members.NORMAL").toPrimitive().getStringList());
            Rank high = normal.getPromotion();
            if (high != null) {
                c.set("members." + high.getName(), c.getNode("members.HIGH").toPrimitive().getStringList());
                Rank higher = high.getPromotion();
                if (higher != null) {
                    c.set("members." + higher.getName(), c.getNode("members.HIGHER").toPrimitive().getStringList());
                }
            }
            Rank highest = registry.getHighest();
            c.set("members." + highest.getName(), c.getNode("members.HIGHEST").toPrimitive().getStringList());
            c.set("members.NORMAL", null);
            c.set("members.HIGH", null);
            c.set("members.HIGHER", null);
            c.set("members.HIGHEST", null);
            c.save();
            c.reload();
        }
    }

    @Override
    public synchronized @Nullable Associate newAssociate(UUID target) {
        Clan.Associate associate = ClansAPI.getInstance().getAssociate(target).orElse(null);
        Rank normal = positionRegistry.getLowest();
        if (associate == null) {
            Player test = Bukkit.getPlayer(target);
            if (test != null) {
                PlayerJoinClanEvent event = ClanVentBus.call(new PlayerJoinClanEvent(test, this));
                if (!event.isCancelled()) {
                    return new PlayerAssociate(target, normal, this);
                }
            } else {
                Tag temp = target::toString;
                if (temp.isEntity() && !temp.isPlayer()) {
                    return new EntityAssociate(InvasiveEntity.wrapNonAssociated(temp.getEntity()), normal, this);
                } else {
                    return new PlayerAssociate(target, normal, this);
                }
            }

        } else {
            ACTION.remove(target, false).deploy();
            return new PlayerAssociate(target, normal, this);
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
        return newAssociate(target.getUniqueId());
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
        return this.clanID != null && this.name != null && ClansAPI.getInstance().getClanManager().getClans().stream().anyMatch(clan -> clan.equals(this));
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
            RankRegistry registry = positionRegistry;
            owner.setRank(registry.getLowest());
            mem.setRank(registry.getHighest());
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
        if (!StringUtils.use(newPassword).containsIgnoreCase("empty", "none")) {
            if (password == null) {
                broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("clan-status"), "&c&oLOCKED"));
            }
            this.password = newPassword;
            broadcast(Clearance.MANAGE_PASSWORD::test, MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("password-change"), newPassword));
        } else {
            if (password != null) {
                this.password = null;
            }
            broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("clan-status"), "&a&oOPEN"));
        }
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
        return HUID.parseID(clanID).toID();
    }

    @Override
    public synchronized @NotNull("Check that the correct file type is selected in Config.yml/Formatting#file-type") String getName() {
        return this.name;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return UUID.nameUUIDFromBytes(tag.getId().getBytes());
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
        return associates.stream().filter(a -> a.getRank().isHighest()).findFirst().get();
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

            for (Rank v : positionRegistry.getRanks()) {
                map.put(v, new ArrayList<>());
            }

            for (Associate ass : getMembers()) {
                List<Associate> list = map.get(ass.getRank());
                if (!ass.isTamable()) {
                    list.add(ass);
                    map.put(ass.getRank(), list);
                }
            }

            for (Map.Entry<Rank, List<Associate>> entry : map.entrySet()) {
                table.set("members." + entry.getKey().getName(), entry.getValue().stream().map(Associate::getId).map(UUID::toString).collect(Collectors.toList()));
            }

            file.write(table);
        }

    }

    @Override
    public synchronized @Nullable Location getBase() {
        return this.base;
    }

    @Override
    public @NotNull ClearanceOverride getPermissiveHandle() {
        return this.clearanceOverride;
    }

    @Override
    public void resetPermissions() {
        ClearanceOverride.ClearanceData override = new ClearanceOverride.ClearanceData();
        setValue("permissions", override, false);
        this.clearanceOverride = new ClearanceOverride(override);
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
                double bal = BanksAPI.getInstance().getBank(this).getBalance().doubleValue();
                if (bal != 0) {
                    bonus += bal / 48.94;
                }
            } else {
                bonus += getWins() * 39.8;
            }
        } else {
            bonus += getWins() * 39.8;
        }
        return Math.min(result + bonus, (double) ClanGameRule.of(LabyrinthProvider.getInstance().getLocalPrintManager().getPrint(ClansAPI.getInstance().getLocalPrintKey())).get(ClanGameRule.MAX_POWER));
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
        TaskScheduler.of(() -> claims.add(c)).schedule();
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
        if (EconomyProvision.getInstance().isValid()) {
            BigDecimal balance = BanksAPI.getInstance().getBank(this).getBalance();
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                return (int) ((getMemberIds().length + Math.cbrt(balance.doubleValue())) + this.claimBonus);
            }
        }
        // common else
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

    public String replacePlaceholders(String s) {
        double totalKD = 0;
        for (InvasiveEntity e : this) {
            if (e.isAssociate()) {
                Associate a = e.getAsAssociate();
                totalKD += a.getKD();
            }
        }
        totalKD = totalKD / getMembers().size();
        String status = "LOCKED";
        if (password == null)
            status = "OPEN";
        String base, mode;
        if (getBase() != null) {
            base = "&aSet";
        } else {
            base = "&7Not set";
        }
        if (isPeaceful()) {
            mode = "&f&lPEACE";
        } else {
            mode = "&4&lWAR";
        }
        String color = getPalette().toString();
        if (getPalette().isGradient()) {
            color = (getPalette().toArray()[0] + getPalette().toArray()[1]).replace("&", "").replace("#", "&f»" + color);
        } else {
            color = color + color.replace("&", "&f»" + color).replace("#", "&f»" + color);
        }
        List<String> mods = getMembers().stream().filter(m -> m.getRank().getLevel() == 1).map(Associate::getName).collect(Collectors.toList());
        List<String> admins = getMembers().stream().filter(m -> m.getRank().getLevel() == 2).map(Associate::getName).collect(Collectors.toList());
        List<String> allies = getAllies().map(Clan::getId).map(HUID::toString).collect(Collectors.toList());
        List<String> enemies = getEnemies().map(Clan::getId).map(HUID::toString).collect(Collectors.toList());
        String allyList = getAllies().collect().stream().map(Clan::getName).collect(Collectors.joining(", "));
        String enemyList = getEnemies().collect().stream().map(Clan::getName).collect(Collectors.joining(", "));
        String requestList = getRelation().getAlliance().getRequests().stream().map(InvasiveEntity::getName).collect(Collectors.joining(", "));
        PantherList<Object> list = new PantherList<>(Arrays.asList(getName(), getDescription(), ACTION.format(getPower()), getPalette().toString(getName()), getClaims().length, getClaimLimit(), allyList, enemyList, requestList
                , getPalette().toString(), ACTION.format(totalKD), getOwner().getName(), status, base, mode, mods.size(), admins.size(), allies.size(), enemies.size(), color, getPassword()));
        positionRegistry.getRanks().forEach(p -> {
            List<String> members = getMembers().stream().filter(m -> m.getRank().getLevel() == p.getLevel()).map(Associate::getName).collect(Collectors.toList());
            String memberList = members.stream().collect(Collectors.joining(", "));
            list.add(memberList);
            list.add(members.size());
        }); // run through and add each configured rank and size
        return MessageFormat.format(s, list.toArray(Object[]::new));
    }

    @Override
    public synchronized String[] getClanInfo() {
        List<String> array = new ArrayList<>();
        info_board.forEach(s -> array.add(replacePlaceholders(s)));
        ClanInformationAdaptEvent event = new ClanVentCall<>(new ClanInformationAdaptEvent(array, clanID, ClanInformationAdaptEvent.Type.OTHER)).run();
        return event.getInsertions().toArray(new String[0]);
    }

    /**
     * Get the mode-switch cooldown object for the clan
     *
     * @return an object containing cooldown information.
     */
    public ClanCooldown getModeCooldown() {
        for (ClanCooldown c : getCooldowns()) {
            if (c.getAction().equals("Clans:mode-switch")) {
                return c;
            }
        }
        DefaultModeSwitchCooldown mode = new DefaultModeSwitchCooldown(clanID);
        mode.save();
        return mode;
    }

    /**
     * Get the ff-switch cooldown object for the clan
     *
     * @return an object containing cooldown information.
     */
    public ClanCooldown getFriendlyCooldown() {
        for (ClanCooldown c : getCooldowns()) {
            if (c.getAction().equals("Clans:ff-switch")) {
                return c;
            }
        }
        DefaultFriendlyFireCooldown mode = new DefaultFriendlyFireCooldown(clanID);
        mode.save();
        return mode;
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
        return ClansAPI.getDataInstance().getCooldowns().stream().sequential().filter(c -> c.getId().equals(clanID)).collect(Collectors.toSet()).stream().collect(Collectors.toList());
    }

    @Override
    public void broadcast(String message) {
        getMembers().forEach(a -> {
            if (a.isEntity()) return;
            Optional.ofNullable(a.getTag().getPlayer().getPlayer()).ifPresent(pl -> pl.sendMessage(ACTION.color("&7[&6&l" + getName() + "&7] " + message)));
        });
    }

    @Override
    public void broadcast(BaseComponent... message) {
        getMembers().forEach(a -> {
            if (a.isEntity()) return;
            Optional.ofNullable(a.getTag().getPlayer().getPlayer()).ifPresent(pl -> pl.spigot().sendMessage(message));
        });
    }

    @Override
    public void broadcast(Message... message) {
        getMembers().forEach(a -> {
            if (a.isEntity()) return;
            Optional.ofNullable(a.getTag().getPlayer().getPlayer()).ifPresent(pl -> Arrays.stream(message).forEach(m -> m.send(pl).deploy()));
        });
    }

    @Override
    public void broadcast(Predicate<Associate> predicate, String message) {
        getMembers().forEach(a -> {
            if (a.isEntity()) return;
            if (predicate.test(a)) {
                Optional.ofNullable(a.getTag().getPlayer().getPlayer()).ifPresent(pl -> pl.sendMessage(ACTION.color("&7[&6&l" + getName() + "&7] " + message)));
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
            claim = new DefaultClaim(x, z, clanID, claimID, world, true);
            Claim finalClaim = claim;
            ClansAPI.getInstance().getClaimManager().getFlagManager().getFlags().forEach(finalClaim::register);
            ClansAPI.getInstance().getClaimManager().load(claim);
            broadcast(Claim.ACTION.claimed(x, z, world));
            new ClanVentCall<>(new AssociateObtainLandEvent(claim)).run();
        }
        return claim;
    }

    @Override
    public @NotNull Relation getRelation() {
        return this.relationObject;
    }

    @Override
    public @Nullable Teleport getTeleport() {
        return Teleport.get(this);
    }

    @Override
    public synchronized void givePower(double amount) {
        amount += ClansAPI.getDataInstance().getConfig().read(c -> c.getDouble("Formatting.level-adjustment.give-power.add"));
        this.powerBonus += amount;
        boolean msg = ClansAPI.getDataInstance().isTrue("Formatting.level-adjustment.give-power.announce");
        if (msg) {
            String text = MessageFormat.format(ClansAPI.getDataInstance().getConfigString("Formatting.level-adjustment.give-power.text"), amount);
            broadcast(text);
        }
        ClansAPI.getInstance().getPlugin().getLogger().info("- Gave " + '"' + amount + '"' + " power to clan " + '"' + clanID + '"');
    }

    @Override
    public synchronized void takePower(double amount) {
        amount += ClansAPI.getDataInstance().getConfig().read(c -> c.getDouble("Formatting.level-adjustment.take-power.add"));
        this.powerBonus -= amount;
        boolean msg = ClansAPI.getDataInstance().isTrue("Formatting.level-adjustment.take-power.announce");
        if (msg) {
            String text = MessageFormat.format(ClansAPI.getDataInstance().getConfigString("Formatting.level-adjustment.take-power.text"), amount);
            broadcast(text);
        }
        ClansAPI.getInstance().getPlugin().getLogger().info("- Took " + '"' + amount + '"' + " power from clan " + '"' + clanID + '"');
    }

    @Override
    public synchronized void giveClaims(int amount) {
        amount += ClansAPI.getDataInstance().getConfig().read(c -> c.getDouble("Formatting.level-adjustment.give-claims.add"));
        this.claimBonus += amount;
        boolean msg = ClansAPI.getDataInstance().isTrue("Formatting.level-adjustment.give-claims.announce");
        if (msg) {
            String text = MessageFormat.format(ClansAPI.getDataInstance().getConfigString("Formatting.level-adjustment.give-claims.text"), amount);
            broadcast(text);
        }
        ClansAPI.getInstance().getPlugin().getLogger().info("- Gave " + '"' + amount + '"' + " claim(s) to clan " + '"' + clanID + '"');
    }

    @Override
    public synchronized void takeClaims(int amount) {
        amount += ClansAPI.getDataInstance().getConfig().read(c -> c.getDouble("Formatting.level-adjustment.take-claims.add"));
        this.claimBonus -= amount;
        boolean msg = ClansAPI.getDataInstance().isTrue("Formatting.level-adjustment.take-claims.announce");
        if (msg) {
            String text = MessageFormat.format(ClansAPI.getDataInstance().getConfigString("Formatting.level-adjustment.take-claims.text"), amount);
            broadcast(text);
        }
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
        return this.dataFile.isNode(key);
    }

    @Override
    public Node getNode(String key) {
        return this.dataFile.getNode(key);
    }

    @Override
    public Set<String> getKeys(boolean deep) {
        return this.dataFile.getKeys(deep);
    }

    @Override
    public Map<String, Object> getValues(boolean deep) {
        return this.dataFile.getValues(deep);
    }

    public List<String> getLogo() {
        return DefaultClan.this.getValue("logo");
    }

    @Override
    public void remove() {
        TaskScheduler.of(() -> ClansAPI.getInstance().getClanManager().unload(this)).schedule();
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
            if (ClansAPI.getInstance().getClanManager().getClans().stream().anyMatch(c -> c.equals(this)) && !entity.isTamable()) {
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
    public boolean has(@NotNull UUID uuid) {
        return getMember(associate -> associate.getUniqueId().equals(uuid)) != null;
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
}
