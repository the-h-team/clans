package com.github.sanctum.clans.model.addon.kingdoms;

import com.github.sanctum.clans.model.addon.KingdomAddon;
import com.github.sanctum.clans.model.addon.kingdoms.impl.LocalFileQuest;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.library.Items;
import com.github.sanctum.panther.file.MemorySpace;
import java.util.Random;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A memory space dedicated to mission progression/completion and reward metadata bundled with the {@link KingdomAddon}
 */
public interface Quest extends MemorySpace {

	static Quest[] getDefaults() {
		Quest walls = newQuest("Walls", "Build a wall to contain your kingdom.", 0, 2480);
		walls.setReward(Reward.MONEY, 48.50);
		Quest gate = newQuest("Gate", "Build a gate for your kingdom.", 0, 120);
		gate.setReward(Reward.MONEY, 24.15);
		Quest kills = newQuest("Killer", "Kill at-least 12 enemies within their own land.", 0, 12);
		kills.setReward(Reward.MONEY, 88.95);
		Quest spawner = newQuest("Monsters Box", "Locate a spawner", 0, 1);
		spawner.setReward(Reward.ITEM, Items.edit().setType(Material.SPAWNER).setAmount(1).build());
		Quest farmer = newQuest("The Farmer", "Make a stack of bread or obtain all sorts of crops", 0, 4);
		farmer.setReward(Reward.MONEY, 114.95);
		Quest beef = newQuest("Tainted Beef", "Brutally murder a baby pigmen", 0, 1);
		beef.setReward(Reward.ITEM, Items.edit().setType(Material.ZOMBIE_SPAWN_EGG).setAmount(1).build());
		Quest sky = newQuest("Skylight", "Launch fireworks in the sky", 0, 12);
		sky.setReward(Reward.ITEM, Items.edit().setType(Material.GUNPOWDER).setAmount(32).build());
		Quest color = newQuest("Colorful Child", "Breed colored sheep", 0, 1);
		color.setReward(Reward.MONEY, 6000.69);
		Quest miner = newQuest("The Miner", "Obtain 32 obsidian", 0, 32);
		miner.setReward(Reward.ITEM, Items.edit().setType(Material.DIAMOND_PICKAXE).setAmount(1).addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 2).addEnchantment(Enchantment.DIG_SPEED, 3).build());
		Quest breaker = newQuest("The Back Breaker", "Obtain 16 crying obsidian", 0, 16);
		breaker.setReward(Reward.ITEM, Items.edit().setType(Material.DIAMOND).setAmount(new Random().nextInt(27)).build());
		Quest hotfeet = newQuest("Hot Feet", "Kill 58 blaze", 0, 58);
		hotfeet.setReward(Reward.ITEM_ARRAY, new ItemStack[]{new ItemStack(Material.BLAZE_SPAWN_EGG), Items.edit().setType(Material.SPECTRAL_ARROW).setAmount(new Random().nextInt(32)).build()});
		Quest souless = newQuest("Soulless Driver", "Kill 2 ghast's with their own fire charge", 0, 2);
		souless.setReward(Reward.MONEY, 3816.42);
		Quest dirt = newQuest("Dirty Hands", "Find and dig one piece of mycelium", 0, 1);
		dirt.setReward(Reward.ITEM_ARRAY, new ItemStack[]{new ItemStack(Material.MOOSHROOM_SPAWN_EGG), new ItemStack(Material.COOKED_BEEF, 128)});
		Quest barter = newQuest("The Trade", "Initiate a barter with a piglin", 0, 1);
		barter.setReward(Reward.ITEM_ARRAY, new ItemStack[]{new ItemStack(Material.MAP), Items.edit().setType(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.MENDING, 1).build()});
		Quest diamond = newQuest("Diamond Back", "Mine 250 diamonds", 0, 250);
		diamond.setReward(Reward.MONEY, 2569.69);
		Quest lumberjack = newQuest("Lumberjack", "Obtain 2 stacks of wood", 0, 128);
		lumberjack.setReward(Reward.ITEM, Items.edit().setType(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.DIG_SPEED, 3).build());
		Quest dark = newQuest("Dark Soldier", "Kill 25 wither skeleton", 0, 25);
		dark.setReward(Reward.ITEM, Items.edit().setType(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3).build());
		Quest city = newQuest("Down Upside", "Locate and traverse an end city", 0, 1);
		city.setReward(Reward.ITEM_ARRAY, new ItemStack[]{new ItemStack(Material.MAP), Items.edit().setType(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.MENDING, 1).build()});
		Quest chunk = newQuest("Chunk 007", "Locate and conquer 7 enemy claims.", 0, 7);
		chunk.setReward(Reward.ITEM_ARRAY, new ItemStack[]{new ItemStack(Material.DIAMOND, 8), new ItemStack(Material.IRON_INGOT, 128), new ItemStack(Material.EXPERIENCE_BOTTLE, Math.max(1, new Random().nextInt(14)))});
		Quest commando = newQuest("Kill The King", "Kill the kingdom resident with the most money.", 0, 1);
		commando.setReward(Reward.MONEY, 5849.81);
		Quest cook = newQuest("Feed The Family", "Everyone online in the kingdom eats food.", 0, 1);
		cook.setReward(Reward.ITEM, new ItemStack(Material.DIAMOND));
		return new Quest[]{walls, gate, kills, spawner, farmer, beef, sky, color, miner, breaker, hotfeet, souless, dirt, barter, diamond, lumberjack, dark, city, chunk, commando};
	}

	@NotNull String getTitle();

	@NotNull String getDescription();

	/**
	 * Get the progression tracking parent for this quest, in most cases this is a kingdom but this could return the roundtable aswell.
	 *
	 * @return The parent progressive on this quest object.
	 */
	@Nullable Progressive getParent();

	double getRequirement();

	double getProgression();

	LabyrinthUser getCompleter();

	Reward<?> getReward();

	Set<Player> getActiveUsers();

	double progress(double amount);

	double unprogress(double amount);

	void save();

	void delete();

	double getPercentage();

	boolean activated(Player p);

	boolean activate(Player p);

	boolean deactivate(Player p);

	boolean isComplete();

	void setReward(Reward<?> type, Object reward);

	void setParent(Progressive k);

	default double limit(int add) {
		if (getProgression() + add > getRequirement()) {
			return getRequirement() - (getProgression() + add);
		}
		return add;
	}

	static Quest newQuest(String name, String description, double initialProgress, double requirement) {
		return new LocalFileQuest(name, description, initialProgress, requirement);
	}

}
