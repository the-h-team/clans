package com.github.sanctum.clans.bridge.internal.kingdoms;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.internal.KingdomAddon;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.data.Node;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Items;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Kingdom extends Progressive implements Iterable<Clan> {

	private final List<Clan> members;

	private String name;

	private final List<Quest> quests;

	public Kingdom(String name, KingdomAddon addon) {
		this.name = name;
		this.quests = new LinkedList<>();
		this.members = new LinkedList<>();

		ClansAPI API = ClansAPI.getInstance();

		FileManager section = addon.getFile(FileType.JSON, "kingdoms", "data");

		if (section.getRoot().exists()) {

			if (section.getRoot().getKeys(false).contains(name)) {

				for (String id : section.getRoot().getStringList(name + ".members")) {
					Clan c = API.getClan(id);
					if (c != null) {
						this.members.add(c);
					}
				}

				FileManager data = addon.getFile(FileType.JSON, "achievements", "data");

				if (data.getRoot().exists()) {
					if (data.getRoot().isNode("memory.kingdom." + name)) {
						for (String a : data.getRoot().getNode("memory.kingdom." + name).getKeys(false)) {
							Quest achievement = Quest.newQuest(a, data.getRoot().getString("memory.kingdom." + name + "." + a + ".info"), data.getRoot().getDouble("memory.kingdom." + name + "." + a + ".progression"), data.getRoot().getDouble("memory.kingdom." + name + "." + a + ".requirement"));
							if (data.getRoot().isNode("memory.kingdom." + name + "." + a + ".reward")) {
								Node reward = data.getRoot().getNode("memory.kingdom." + name + "." + a + ".reward");
								boolean money = reward.getNode("type").toPrimitive().getString().equals("MONEY");
								if (money) {
									achievement.setReward(Reward.MONEY, reward.getNode("value").toPrimitive().getDouble());
								} else {
									achievement.setReward(Reward.ITEM, reward.getNode("value").toBukkit().getItemStack());
								}
							}
							loadQuest(achievement);
						}
					}
				}

			}

		}

		if (this.quests.isEmpty()) {
			loadQuest(getDefaults());
		}
		PROGRESSIVES.add(this);

	}

	public static Quest[] getDefaults() {
		Quest walls = Quest.newQuest("Walls", "Build a wall to contain your kingdom.", 0, 2480);
		walls.setReward(Reward.MONEY, 48.50);
		Quest gate = Quest.newQuest("Gate", "Build a gate for your kingdom.", 0, 120);
		walls.setReward(Reward.MONEY, 24.15);
		Quest kills = Quest.newQuest("Killer", "Kill at-least 12 enemies within their own land.", 0, 12);
		walls.setReward(Reward.MONEY, 88.95);
		Quest spawner = Quest.newQuest("Monsters Box", "Locate a spawner", 0, 1);
		spawner.setReward(Reward.ITEM, Items.edit().setType(Material.SPAWNER).setAmount(1).build());
		Quest farmer = Quest.newQuest("The Farmer", "Make a stack of bread or obtain all sorts of crops", 0, 4);
		farmer.setReward(Reward.MONEY, 114.95);
		Quest beef = Quest.newQuest("Tainted Beef", "Crucially murder a baby pigmen", 0, 1);
		beef.setReward(Reward.ITEM, Items.edit().setType(Material.ZOMBIE_SPAWN_EGG).setAmount(1).build());
		Quest sky = Quest.newQuest("Skylight", "Launch fireworks in the sky", 0, 12);
		sky.setReward(Reward.ITEM, Items.edit().setType(Material.GUNPOWDER).setAmount(32).build());
		Quest color = Quest.newQuest("Colorful Child", "Breed colored sheeps", 0, 1);
		Quest miner = Quest.newQuest("The Miner", "Obtain 32 obsidian", 0, 32);
		return new Quest[]{walls, gate, kills, spawner, farmer, beef, sky, color, miner};
	}

	public static Kingdom getKingdom(String name) {
		return Progressive.getProgressives().stream().filter(p -> p.getName().equalsIgnoreCase(name)).map(p -> (Kingdom) p).findFirst().orElse(null);
	}

	public void setName(String name) {

		ClanAddon cycle = ClanAddonQuery.getAddon("Kingdoms");

		FileManager section = cycle.getFile(FileType.JSON, "kingdoms", "data");

		section.getRoot().set(getName(), null);

		for (Quest achievement : quests) {
			achievement.delete("memory.kingdom." + getName());
		}

		section.getRoot().save();

		this.name = name;
		for (Clan c : getMembers()) {
			c.setValue("kingdom", name, false);
		}

	}

	@Override
	public @NotNull String getName() {
		return name;
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
		return getQuests().stream().filter(a -> a.getTitle().equalsIgnoreCase(title)).findFirst().orElse(null);
	}

	@Override
	public @NotNull List<Quest> getQuests() {
		return this.quests;
	}

	@Override
	public void loadQuest(Quest... quests) {

		for (Quest q : quests) {
			if (getQuests().stream().noneMatch(a -> a.getTitle().equalsIgnoreCase(q.getTitle()))) {
				q.setParent(this);
				this.quests.add(q);
			}
		}

	}

	@Override
	public void save(ClanAddon cycle) {

		FileManager section = cycle.getFile(FileType.JSON, "kingdoms", "data");

		List<String> ids = getMembers().stream().map(Clan::getId).map(HUID::toString).collect(Collectors.toList());

		section.getRoot().set(getName() + ".members", ids);

		for (Quest achievement : getQuests()) {
			achievement.saveProgress("memory.kingdom." + getName());
		}

		section.getRoot().save();

	}

	public void remove(ClanAddon cycle) {

		FileManager section = cycle.getFile(FileType.JSON, "kingdoms", "data");

		section.getRoot().set(getName(), null);

		for (Quest achievement : quests) {
			achievement.delete("memory.kingdom." + getName());
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
}
