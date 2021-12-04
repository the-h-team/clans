package com.github.sanctum.clans.bridge.internal.kingdoms;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.internal.KingdomAddon;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.data.Node;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoundTable extends Progressive implements Iterable<Clan.Associate> {

	public static final String INVITE = "INVITE";
	public static final String TAG = "TAG";
	public static final String PROMOTE = "PROMOTE";
	public static final String DEMOTE = "DEMOTE";
	public static final String KICK = "KICK";

	private final String name;
	private final Map<UUID, Clan.Rank> users = new HashMap<>();
	private final Set<UUID> invites = new HashSet<>();
	private final List<Quest> quests = new LinkedList<>();
	private final Map<String, Clearance> perms = new HashMap<>();

	public RoundTable(KingdomAddon cycle) {

		this.name = ClansAPI.getDataInstance().getConfigString("Addon.Kingdoms.roundtable.name");

		FileManager data = cycle.getFile(FileType.JSON, "achievements", "data");

		FileManager users = cycle.getFile(FileType.JSON, "users", "data");

		if (data.getRoot().exists()) {
			if (data.getRoot().isNode("memory.table")) {
				for (String name : data.getRoot().getNode("memory.table").getKeys(false)) {
					loadQuest(Quest.newQuest(name, data.getRoot().getString("memory.table." + name + ".info"), data.getRoot().getDouble("memory.table." + name + ".progression"), data.getRoot().getDouble("memory.table." + name + ".requirement")));
				}
			}
		}

		if (users.getRoot().exists()) {

			if (!users.getRoot().getKeys(false).isEmpty()) {
				for (String user : users.getRoot().getKeys(false)) {
					UUID id = UUID.fromString(user);
					this.users.put(id, Clan.Rank.valueOf(users.getRoot().getString(user + ".rank")));
				}
			}

		}

		if (quests.isEmpty()) {
			loadQuest(Kingdom.getDefaults());
		}

		if (perms.isEmpty()) {
			Clearance invite = new Clearance(2, "INVITE");
			Clearance tag = new Clearance(1, "TAG");
			Clearance promote = new Clearance(3, "PROMOTE");
			Clearance demote = new Clearance(3, "DEMOTE");
			Clearance kick = new Clearance(3, "KICK");
			perms.put(invite.getName(), invite);
			perms.put(tag.getName(), tag);
			perms.put(promote.getName(), promote);
			perms.put(demote.getName(), demote);
			perms.put(kick.getName(), kick);
		}

		PROGRESSIVES.add(this);
	}

	@NotNull
	@Override
	public Iterator<Clan.Associate> iterator() {
		return users.keySet().stream().map(u -> ClansAPI.getInstance().getAssociate(u).get()).collect(Collectors.toList()).iterator();
	}

	@Override
	public void forEach(Consumer<? super Clan.Associate> action) {
		users.keySet().stream().map(u -> ClansAPI.getInstance().getAssociate(u).get()).collect(Collectors.toList()).forEach(action);
	}

	@Override
	public Spliterator<Clan.Associate> spliterator() {
		return users.keySet().stream().map(u -> ClansAPI.getInstance().getAssociate(u).get()).collect(Collectors.toList()).spliterator();
	}

	public boolean isMember(UUID target) {
		return users.containsKey(target);
	}

	public boolean isInvited(UUID target) {
		return this.invites.contains(target);
	}

	public boolean invite(UUID target) {
		if (isMember(target)) return false;
		if (this.invites.contains(target)) return false;
		return this.invites.add(target);
	}

	public boolean join(UUID target) {
		if (!this.invites.contains(target)) return false;

		if (isMember(target)) return false;

		take(target, Clan.Rank.NORMAL);
		this.invites.remove(target);

		return true;
	}

	public void take(UUID target, Clan.Rank rank) {
		this.users.put(target, rank);
	}

	public boolean leave(UUID target) {
		if (!isMember(target)) return false;
		users.remove(target);
		return true;
	}

	@Override
	public @NotNull String getName() {
		return this.name;
	}

	@Override
	public int getLevel() {
		int level = 1;
		for (Quest achievement : quests) {
			if (achievement.isComplete()) {
				level += 1;
			}
		}
		return level;
	}

	@Override
	public @Nullable Quest getQuest(String title) {
		return getQuests().stream().filter(a -> a.getTitle().equalsIgnoreCase(title)).findFirst().orElse(null);
	}

	public Set<UUID> getUsers() {

		return this.users.keySet();

	}

	public Clearance getClearance(@MagicConstant(valuesFromClass = RoundTable.class) String id) {
		return perms.get(id);
	}

	public Clan.Rank getRank(UUID user) {
		return this.users.get(user);
	}

	@Override
	public @NotNull List<Quest> getQuests() {
		return quests;
	}

	public List<Claim> getLandPool() {
		List<Claim> list = new LinkedList<>();
		for (UUID id : this.users.keySet()) {
			Clan c = ClansAPI.getInstance().getClanManager().getClan(id);
			if (c != null) {
				list.addAll(Arrays.asList(c.getClaims()));
			} else {
				leave(id);
			}
		}
		return list;
	}

	public boolean isOurs(Claim c) {
		return getLandPool().contains(c);
	}

	@Override
	public void loadQuest(Quest... quests) {

		for (Quest q : quests) {
			if (this.quests.stream().noneMatch(a -> a.getTitle().equalsIgnoreCase(q.getTitle()))) {
				q.setParent(this);
				this.quests.add(q);
			}
		}

	}

	@Override
	public void save(ClanAddon cycle) {

		FileManager users = cycle.getFile(FileType.JSON, "users", "data");

		for (Map.Entry<UUID, Clan.Rank> entry : this.users.entrySet()) {
			users.getRoot().set(entry.getKey().toString() + ".rank", entry.getValue().name());
		}

		users.getRoot().save();

		for (Quest achievement : getQuests()) {
			achievement.save();
		}

	}

	@Override
	public String getPath() {
		return getName();
	}

	Node getParentNode() {
		FileManager section = ClanAddonQuery.getAddon("Kingdoms").getFile(FileType.JSON, "table", "data");
		return section.getRoot().getNode(getPath());
	}

	@Override
	public boolean isNode(String key) {
		return getParentNode().isNode(key);
	}

	@Override
	public Node getNode(String key) {
		return getParentNode().getNode(key);
	}

	@Override
	public Set<String> getKeys(boolean deep) {
		return getParentNode().getKeys(deep);
	}

	@Override
	public Map<String, Object> getValues(boolean deep) {
		return getParentNode().getValues(deep);
	}


}
