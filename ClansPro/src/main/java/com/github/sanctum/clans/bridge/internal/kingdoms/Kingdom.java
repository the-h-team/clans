package com.github.sanctum.clans.bridge.internal.kingdoms;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.internal.KingdomAddon;
import com.github.sanctum.clans.bridge.internal.kingdoms.achievement.KingdomAchievement;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.library.HUID;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Kingdom extends Progressable implements Iterable<Clan> {

	private final List<Clan> members;

	private String name;

	private final List<KingdomAchievement> achievements;

	public Kingdom(String name, KingdomAddon cycle) {
		this.name = name;
		this.achievements = new LinkedList<>();
		this.members = new LinkedList<>();

		ClansAPI API = ClansAPI.getInstance();

		FileManager section = cycle.getFile(FileType.JSON, "kingdoms", "data");

		if (section.getRoot().exists()) {

			if (section.getRoot().getKeys(false).contains(name)) {

				for (String id : section.getRoot().getStringList(name + ".members")) {
					Clan c = API.getClan(id);
					if (c != null) {
						this.members.add(c);
					}
				}

				FileManager data = cycle.getFile(FileType.JSON, "achievements", "data");

				if (data.getRoot().exists()) {
					if (data.getRoot().isNode("memory.kingdom." + name)) {
						for (String a : data.getRoot().getNode("memory.kingdom." + name).getKeys(false)) {
							if (!loadAchievement(KingdomAchievement.newInstance(a, data.getRoot().getString("memory.kingdom." + name + "." + a + ".info"), data.getRoot().getDouble("memory.kingdom." + name + "." + a + ".progression"), data.getRoot().getDouble("memory.kingdom." + name + "." + a + ".requirement")))) {
								cycle.getLogger().log(Level.SEVERE, "- Unable to load achievement " + a);
							}
						}
					}
				}

			}

		}

		if (this.achievements.isEmpty()) {
			loadAchievement(getDefaultAchievements());
		}
		PROGRESSABLES.add(this);

	}

	public static KingdomAchievement[] getDefaultAchievements() {
		KingdomAchievement walls = KingdomAchievement.newInstance("Walls", "Build a wall to contain your kingdom.", 0, 2480);
		walls.setReward(Reward.MONEY, 48.50);
		KingdomAchievement gate = KingdomAchievement.newInstance("Gate", "Build a gate for your kingdom.", 0, 120);
		walls.setReward(Reward.MONEY, 24.15);
		KingdomAchievement kills = KingdomAchievement.newInstance("Killer", "Kill at-least 12 enemies within their own land.", 0, 12);
		walls.setReward(Reward.MONEY, 88.95);
		return new KingdomAchievement[]{walls, gate, kills};
	}

	public static Kingdom getKingdom(String name) {
		return Progressable.getProgressables().stream().filter(p -> p.getName().equalsIgnoreCase(name)).map(p -> (Kingdom) p).findFirst().orElse(null);
	}

	public void setName(String name) {

		ClanAddon cycle = ClanAddonQuery.getAddon("Kingdoms");

		FileManager section = cycle.getFile(FileType.JSON, "kingdoms", "data");

		section.getRoot().set(getName(), null);

		for (KingdomAchievement achievement : achievements) {
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
		for (KingdomAchievement achievement : achievements) {
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
	public @Nullable KingdomAchievement getAchievement(String title) {
		return getAchievements().stream().filter(a -> a.getTitle().equalsIgnoreCase(title)).findFirst().orElse(null);
	}

	@Override
	public @NotNull List<KingdomAchievement> getAchievements() {
		return this.achievements;
	}

	@Override
	public void loadAchievement(KingdomAchievement... achievement) {

		for (KingdomAchievement achiev : achievement) {
			loadAchievement(achiev);
		}

	}

	@Override
	public boolean loadAchievement(KingdomAchievement achievement) {

		if (getAchievements().stream().noneMatch(a -> a.getTitle().equalsIgnoreCase(achievement.getTitle()))) {
			achievement.setParent(this);
			return this.achievements.add(achievement);
		}

		return false;

	}

	@Override
	public void save(ClanAddon cycle) {

		FileManager section = cycle.getFile(FileType.JSON, "kingdoms", "data");

		List<String> ids = getMembers().stream().map(Clan::getId).map(HUID::toString).collect(Collectors.toList());

		section.getRoot().set(getName() + ".members", ids);

		for (KingdomAchievement achievement : getAchievements()) {
			achievement.saveProgress("memory.kingdom." + getName());
		}

		section.getRoot().save();

	}

	public void remove(ClanAddon cycle) {

		FileManager section = cycle.getFile(FileType.JSON, "kingdoms", "data");

		section.getRoot().set(getName(), null);

		for (KingdomAchievement achievement : achievements) {
			achievement.delete("memory.kingdom." + getName());
		}

		section.getRoot().save();

		Progressable.PROGRESSABLES.remove(this);
	}

	public static Set<Kingdom> entrySet() {
		return Progressable.PROGRESSABLES.stream().filter(p -> p instanceof Kingdom).map(p -> (Kingdom) p).collect(Collectors.toSet());
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
