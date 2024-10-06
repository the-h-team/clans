package com.github.sanctum.clans.model.addon.kingdoms;

import com.github.sanctum.clans.model.ClanAddonRegistry;
import com.github.sanctum.clans.model.addon.KingdomAddon;
import com.github.sanctum.clans.model.addon.kingdoms.impl.LocalFileQuest;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.Clearance;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.util.HUID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Kingdom extends Progressive implements Permissive, Iterable<Clan> {

	private final List<Clan> members;

	private String name;
	private boolean peaceful = true;
	private Location castle;
	private Clan.Associate king;

	private final List<Quest> quests;

	public Kingdom(String name, KingdomAddon addon) {
		this.name = name;
		this.quests = new LinkedList<>();
		this.members = new LinkedList<>();

		ClansAPI API = ClansAPI.getInstance();

		FileManager section = addon.getFile(Configurable.Type.JSON, "kingdoms", "data");

		if (section.getRoot().exists()) {

			if (section.getRoot().getKeys(false).contains(name)) {

				if (section.getRoot().isNode(name + ".castle")) {
					castle = section.getRoot().getNode(name + ".castle").get(Location.class);
				}

				if (section.getRoot().getNode(name + ".pvp").toPrimitive().isBoolean()) {
					peaceful = section.getRoot().getNode(name + ".pvp").toPrimitive().getBoolean();
				}

				for (String id : section.getRoot().getStringList(name + ".members")) {
					Clan c = API.getClanManager().getClan(HUID.parseID(id).toID());
					if (c != null) {
						this.members.add(c);
					}
				}

				String king = section.getRoot().getNode(name + ".king").toPrimitive().getString();

				if (king != null) {
					this.king = getMembers().stream().filter(c -> c.getMember(a -> a.getId().toString().equalsIgnoreCase(king)) != null).findFirst().map(clan -> clan.getMember(a -> a.getId().toString().equalsIgnoreCase(king))).orElse(null);
				}

				FileManager data = addon.getFile(Configurable.Type.JSON, "achievements", "data");

				if (data.getRoot().exists()) {
					if (data.getRoot().isNode("memory.kingdom." + name)) {
						for (String a : data.getRoot().getNode("memory.kingdom." + name).getKeys(false)) {
							Quest achievement = new LocalFileQuest(a, data.getRoot().getString("memory.kingdom." + name + "." + a + ".info"), data.getRoot().getDouble("memory.kingdom." + name + "." + a + ".progression"), data.getRoot().getDouble("memory.kingdom." + name + "." + a + ".requirement"), data.getRoot().getBoolean("memory.kingdom." + name + "." + a + ".complete"));
							if (data.getRoot().isNode("memory.kingdom." + name + "." + a + ".reward")) {
								Node reward = data.getRoot().getNode("memory.kingdom." + name + "." + a + ".reward");
								boolean money = reward.getNode("type").toPrimitive().getString().equals("MONEY");
								if (money) {
									achievement.setReward(Reward.MONEY, reward.getNode("value").toPrimitive().getDouble());
								} else {
									if (reward.getNode("value").isNode("org.bukkit.inventory.ItemStack")) {
										achievement.setReward(Reward.ITEM, reward.getNode("value").get(ItemStack.class));
									} else {
										List<ItemStack> items = new ArrayList<>();
										for (String s : reward.getNode("value").getKeys(false)) {
											Node item = reward.getNode("value").getNode(s);
											if (reward.getNode("value").isNode(s)) {
												items.add(item.get(ItemStack.class));
											}
										}
										achievement.setReward(Reward.ITEM_ARRAY, items.toArray(new ItemStack[0]));
									}
								}
							} else {
								Arrays.stream(Quest.getDefaults()).forEach(q -> {
									if (q.getTitle().equals(achievement.getTitle())) {
										Reward<?> type = q.getReward().get() instanceof Double ? Reward.MONEY : Reward.ITEM;
										achievement.setReward(type, q.getReward().get());
									}
								});
							}
							giveQuest(achievement);
						}
					}
				}
			}
		}
		giveQuest(Quest.getDefaults());
	}

	public static Kingdom getKingdom(Clan clan) {
		String kingdomName = clan.getValue(String.class, "kingdom");
		if (kingdomName != null) {
			return getKingdom(kingdomName);
		}
		return null;
	}

	public static Kingdom getKingdom(String name) {
		return Progressive.getProgressives().stream().filter(p -> p.getName().equalsIgnoreCase(name)).map(p -> (Kingdom) p).findFirst().orElse(null);
	}

	public void setName(String name) {

		Clan.Addon cycle = ClanAddonRegistry.getInstance().get("Kingdoms");

		FileManager section = cycle.getFile(Configurable.Type.JSON, "kingdoms", "data");

		section.getRoot().set(getName(), null);

		for (Quest achievement : quests) {
			achievement.delete();
		}

		section.getRoot().save();

		this.name = name;
		for (Clan c : getMembers()) {
			c.setValue("kingdom", name, false);
		}

	}

	public boolean isPeaceful() {
		return peaceful;
	}

	public void setPeaceful(boolean peaceful) {
		this.peaceful = peaceful;
	}

	public Location getCastle() {
		return castle;
	}

	public void setCastle(Location castle) {
		this.castle = castle;
	}

	@Override
	public @NotNull String getName() {
		return name;
	}

	public Optional<Clan.Associate> getKing() {
		return Optional.ofNullable(king);
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

	public List<Clan> getMembers() {
		return members;
	}

	@Override
	public @Nullable Quest getQuest(String title) {
		return getQuests().stream().filter(a -> StringUtils.use(a.getTitle()).containsIgnoreCase(title)).findFirst().orElse(null);
	}

	@Override
	public @NotNull List<Quest> getQuests() {
		return this.quests;
	}

	public void setKing(Clan.Associate king) {
		this.king = king;
	}

	@Override
	public void giveQuest(Quest... quests) {

		for (Quest q : quests) {
			if (getQuests().stream().noneMatch(a -> a.getTitle().equalsIgnoreCase(q.getTitle()))) {
				q.setParent(this);
				this.quests.add(q);
			}
		}

	}

	@Override
	public void save(Clan.Addon cycle) {

		FileManager section = cycle.getFile(Configurable.Type.JSON, "kingdoms", "data");

		List<String> ids = getMembers().stream().map(Clan::getId).map(HUID::toString).collect(Collectors.toList());

		if (king != null) {
			section.getRoot().set(getName() + ".king", king.getId().toString());
		}

		section.getRoot().set(getName() + ".pvp", peaceful);

		section.getRoot().set(getName() + ".castle", castle);

		section.getRoot().set(getName() + ".members", ids);

		for (Quest achievement : getQuests()) {
			achievement.save();
		}

		section.getRoot().save();

	}

	public void remove(Clan.Addon cycle) {

		FileManager section = cycle.getFile(Configurable.Type.JSON, "kingdoms", "data");

		section.getRoot().set(getName(), null);

		for (Quest achievement : quests) {
			achievement.delete();
		}

		section.getRoot().save();

		Progressive.PROGRESSIVES.remove(this);
	}

	public static Set<Kingdom> entrySet() {
		return Progressive.PROGRESSIVES.stream().filter(p -> p instanceof Kingdom).map(p -> (Kingdom) p).collect(Collectors.toSet());
	}

	@NotNull
	@Override
	public Iterator<Clan> iterator() {
		return getMembers().iterator();
	}

	@Override
	public void forEach(Consumer<? super Clan> action) {
		getMembers().forEach(action);
	}

	@Override
	public Spliterator<Clan> spliterator() {
		return getMembers().spliterator();
	}

	@Override
	public String getPath() {
		return getName();
	}

	Node getParentNode() {
		FileManager section = ClanAddonRegistry.getInstance().get("Kingdoms").getFile(Configurable.Type.JSON, "kingdoms", "data");
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

	@Override
	public boolean test(Clearance clearance, Clan.Associate associate) {
		if (clearance.getName().equalsIgnoreCase("kingdom_king") && getKing().map(associate::equals).orElse(false)) {
			return true;
		}
		return clearance.test(associate);
	}
}
