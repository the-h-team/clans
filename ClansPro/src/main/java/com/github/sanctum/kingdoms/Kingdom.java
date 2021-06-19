package com.github.sanctum.kingdoms;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.kingdoms.achievement.KingdomAchievement;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.link.CycleList;
import com.github.sanctum.link.EventCycle;
import com.github.sanctum.link.cycles.KingdomCycle;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Kingdom extends Progressable {

	private final List<Clan> members;

	private String name;

	private final List<KingdomAchievement> achievements;

	public Kingdom(String name, KingdomCycle cycle) {
		this.name = name;
		this.achievements = new LinkedList<>();
		this.members = new LinkedList<>();

		ClansAPI API = ClansAPI.getInstance();

		FileManager section = cycle.getFile("Kingdoms", "Data");

		if (section.exists()) {

			if (section.getConfig().contains(name)) {

				for (String id : section.getConfig().getStringList(name + ".members")) {
					Clan c = API.getClan(id);
					if (c != null) {
						this.members.add(c);
					}
				}

				FileManager data = cycle.getFile("Achievements", "Data");

				if (data.exists()) {
					if (data.getConfig().isConfigurationSection("memory.kingdom." + name)) {
						for (String a : data.getConfig().getConfigurationSection("memory.kingdom." + name).getKeys(false)) {
							if (!loadAchievement(KingdomAchievement.newInstance(a, data.getConfig().getString("memory.kingdom." + name + "." + a + ".info"), data.getConfig().getDouble("memory.kingdom." + name + "." + a + ".progression"), data.getConfig().getDouble("memory.kingdom." + name + "." + a + ".requirement")))) {
								cycle.getLogger().log(Level.SEVERE, "- Unable to load achievement " + a);
							}
						}
					}
				}

			}

		}


		if (this.achievements.isEmpty()) {

			KingdomAchievement walls = KingdomAchievement.newInstance("Walls", "Build a wall to contain your kingdom.", 0, 2480);

			KingdomAchievement gate = KingdomAchievement.newInstance("Gate", "Build a gate for your kingdom.", 0, 120);

			KingdomAchievement kills = KingdomAchievement.newInstance("Killer", "Kill at-least 12 enemies within their own land.", 0, 12);


			loadAchievement(walls, gate, kills);
		}
		PROGRESSABLES.add(this);

	}

	public static Kingdom getKingdom(String name) {
		return Progressable.getProgressables().stream().filter(p -> p.getName().equalsIgnoreCase(name)).map(p -> (Kingdom) p).findFirst().orElse(null);
	}

	public void setName(String name) {

		EventCycle cycle = CycleList.getAddon("Kingdoms");

		FileManager section = cycle.getFile("Kingdoms", "Data");

		section.getConfig().set(getName(), null);

		for (KingdomAchievement achievement : achievements) {
			achievement.delete("memory.kingdom." + getName());
		}

		section.saveConfig();

		this.name = name;
		for (Clan c : getMembers()) {
			c.setValue("kingdom", name);
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
			return this.achievements.add(achievement);
		}

		return false;

	}

	@Override
	public void save(EventCycle cycle) {

		FileManager section = cycle.getFile("Kingdoms", "Data");

		List<String> ids = getMembers().stream().map(Clan::getId).map(HUID::toString).collect(Collectors.toList());

		section.getConfig().set(getName() + ".members", ids);

		for (KingdomAchievement achievement : getAchievements()) {
			achievement.saveProgress("memory.kingdom." + getName());
		}

		section.saveConfig();

	}

	public void remove(EventCycle cycle) {

		FileManager section = cycle.getFile("Kingdoms", "Data");

		section.getConfig().set(getName(), null);

		for (KingdomAchievement achievement : achievements) {
			achievement.delete("memory.kingdom." + getName());
		}

		section.saveConfig();

		Progressable.PROGRESSABLES.remove(this);
	}

	public static Set<Kingdom> entrySet() {
		return Progressable.PROGRESSABLES.stream().filter(p -> p instanceof Kingdom).map(p -> (Kingdom) p).collect(Collectors.toSet());
	}
}
