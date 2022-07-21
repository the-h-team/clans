package com.github.sanctum.clans.bridge.internal.kingdoms;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.internal.KingdomAddon;
import com.github.sanctum.clans.bridge.internal.kingdoms.impl.LocalFileQuest;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.library.Items;
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
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Kingdom extends Progressive implements Controllable, Iterable<Clan> {

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
								Arrays.stream(getDefaults()).forEach(q -> {
									if (q.getTitle().equals(achievement.getTitle())) {
										Reward<?> type = q.getReward().get() instanceof Double ? Reward.MONEY : Reward.ITEM;
										achievement.setReward(type, q.getReward().get());
									}
								});
							}
							loadQuest(achievement);
						}
					}
				}
			}
		}
		loadQuest(getDefaults());
	}

	public static Quest[] getDefaults() {
		Quest walls = Quest.newQuest("Walls", "Build a wall to contain your kingdom.", 0, 2480);
		walls.setReward(Reward.MONEY, 48.50);
		Quest gate = Quest.newQuest("Gate", "Build a gate for your kingdom.", 0, 120);
		gate.setReward(Reward.MONEY, 24.15);
		Quest kills = Quest.newQuest("Killer", "Kill at-least 12 enemies within their own land.", 0, 12);
		kills.setReward(Reward.MONEY, 88.95);
		Quest spawner = Quest.newQuest("Monsters Box", "Locate a spawner", 0, 1);
		spawner.setReward(Reward.ITEM, Items.edit().setType(Material.SPAWNER).setAmount(1).build());
		Quest farmer = Quest.newQuest("The Farmer", "Make a stack of bread or obtain all sorts of crops", 0, 4);
		farmer.setReward(Reward.MONEY, 114.95);
		Quest beef = Quest.newQuest("Tainted Beef", "Brutally murder a baby pigmen", 0, 1);
		beef.setReward(Reward.ITEM, Items.edit().setType(Material.ZOMBIE_SPAWN_EGG).setAmount(1).build());
		Quest sky = Quest.newQuest("Skylight", "Launch fireworks in the sky", 0, 12);
		sky.setReward(Reward.ITEM, Items.edit().setType(Material.GUNPOWDER).setAmount(32).build());
		Quest color = Quest.newQuest("Colorful Child", "Breed colored sheep", 0, 1);
		color.setReward(Reward.MONEY, 6000.69);
		Quest miner = Quest.newQuest("The Miner", "Obtain 32 obsidian", 0, 32);
		miner.setReward(Reward.ITEM, Items.edit().setType(Material.DIAMOND_PICKAXE).setAmount(1).addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 2).addEnchantment(Enchantment.DIG_SPEED, 3).build());
		Quest breaker = Quest.newQuest("The Back Breaker", "Obtain 16 crying obsidian", 0, 16);
		breaker.setReward(Reward.ITEM, Items.edit().setType(Material.DIAMOND).setAmount(new Random().nextInt(27)).build());
		Quest hotfeet = Quest.newQuest("Hot Feet", "Kill 58 blaze", 0, 58);
		hotfeet.setReward(Reward.ITEM_ARRAY, new ItemStack[]{new ItemStack(Material.BLAZE_SPAWN_EGG), Items.edit().setType(Material.SPECTRAL_ARROW).setAmount(new Random().nextInt(32)).build()});
		Quest souless = Quest.newQuest("Soulless Driver", "Kill 2 ghast's with their own fire charge", 0, 2);
		souless.setReward(Reward.MONEY, 3816.42);
		Quest dirt = Quest.newQuest("Dirty Hands", "Find and dig one piece of mycelium", 0, 1);
		dirt.setReward(Reward.ITEM_ARRAY, new ItemStack[]{new ItemStack(Material.MOOSHROOM_SPAWN_EGG), new ItemStack(Material.COOKED_BEEF, 128)});
		Quest barter = Quest.newQuest("The Trade", "Initiate a barter with a piglin", 0, 1);
		barter.setReward(Reward.ITEM_ARRAY, new ItemStack[]{new ItemStack(Material.MAP), Items.edit().setType(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.MENDING, 1).build()});
		Quest diamond = Quest.newQuest("Diamond Back", "Mine 250 diamonds", 0, 250);
		diamond.setReward(Reward.MONEY, 2569.69);
		Quest lumberjack = Quest.newQuest("Lumberjack", "Obtain 2 stacks of wood", 0, 128);
		lumberjack.setReward(Reward.ITEM, Items.edit().setType(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.DIG_SPEED, 3).build());
		Quest dark = Quest.newQuest("Dark Soldier", "Kill 25 wither skeleton", 0, 25);
		dark.setReward(Reward.ITEM, Items.edit().setType(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3).build());
		Quest city = Quest.newQuest("Down Upside", "Locate and traverse an end city", 0, 1);
		city.setReward(Reward.ITEM_ARRAY, new ItemStack[]{new ItemStack(Material.MAP), Items.edit().setType(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.MENDING, 1).build()});
		Quest chunk = Quest.newQuest("Chunk 007", "Locate and conquer 7 enemy claims.", 0, 7);
		chunk.setReward(Reward.ITEM_ARRAY, new ItemStack[]{new ItemStack(Material.DIAMOND, 8), new ItemStack(Material.IRON_INGOT, 128), new ItemStack(Material.EXPERIENCE_BOTTLE, Math.max(1, new Random().nextInt(14)))});
		Quest commando = Quest.newQuest("Kill The King", "Kill the kingdom resident with the most money.", 0, 1);
		commando.setReward(Reward.MONEY, 5849.81);
		Quest cook = Quest.newQuest("Feed The Family", "Everyone online in the kingdom eats food.", 0, 1);
		cook.setReward(Reward.ITEM, new ItemStack(Material.DIAMOND));
		return new Quest[]{walls, gate, kills, spawner, farmer, beef, sky, color, miner, breaker, hotfeet, souless, dirt, barter, diamond, lumberjack, dark, city, chunk, commando};
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

		ClanAddon cycle = ClanAddonQuery.getAddon("Kingdoms");

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

	public void remove(ClanAddon cycle) {

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
		FileManager section = ClanAddonQuery.getAddon("Kingdoms").getFile(Configurable.Type.JSON, "kingdoms", "data");
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
