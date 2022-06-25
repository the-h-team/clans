package com.github.sanctum.clans.bridge.internal.traits.structure;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.container.LabyrinthCollection;
import com.github.sanctum.labyrinth.data.container.LabyrinthList;
import com.github.sanctum.labyrinth.data.service.PlayerSearch;
import com.github.sanctum.labyrinth.library.Deployable;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.RandomObject;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AbilityPool {

	public static Trait.Ability PICK_POCKET = new Trait.Ability() {

		int level = 0;

		@Override
		public @NotNull String getName() {
			return "Pick Pocket";
		}

		@Override
		public @NotNull String getDescription() {
			return "Sneak + Right-click an entity to steal.";
		}

		@Override
		public int getLevel() {
			return level;
		}

		@Override
		public int getMaxLevel() {
			return 100;
		}

		@Override
		public void setLevel(int level) {
			this.level = level;
		}

		@Override
		public Deployable<Trait.Ability> run(@NotNull Object... args) {
			return Deployable.of(() -> {
				if (args.length > 1) {
					Object o = args[0];
					Object o2 = args[1];
					Random r = new Random();
					if (o instanceof Player) {
						Player p = (Player) o;
						Mailer player = Mailer.empty(p);
						if (o2 instanceof Player) {
							Player target = (Player) o2;
							if (p.getLocation().distance(target.getLocation()) < 6) {
								int random = r.nextInt(Math.max(2, 100 - getLevel()));
								if (random == 1) {
									List<ItemStack> items = Arrays.stream(target.getInventory().getContents()).filter(i -> i != null && i.getType() != Material.AIR).collect(Collectors.toList());
									if (items.isEmpty()) {
										player.chat("&cTarget " + target.getName() + " doesn't seem to be carrying much...").queue();
										return this;
									}
									final ItemStack nabbed = new RandomObject<>(items).get(random);
									final ItemStack copied = new ItemStack(nabbed);
									LabyrinthProvider.getInstance().getItemComposter().add(copied, p);
									nabbed.setAmount(0);
									p.playSound(p.getEyeLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 10, 1);
									if (!isMaxLevel()) {
										this.level++;
										player.chat("&a&oAbility " + getName() + " leveled up to " + this.level).queue();
										player.chat("&aYou found something...").queue();
										player.action("&a+1").queue();
									} else {
										player.chat("&aYou found something...").queue();
									}
									if (r.nextInt(3) == 2) {
										Mailer.empty(target).chat("&cYou begin to feel lighter all of a sudden...").queue();
									}
								} else {
									player.action("&c&oAbility " + getName() + " failed.").queue();
								}
							}
						}
						if (o2 instanceof ItemStack[]) {
							int random = r.nextInt(Math.max(2, 100 - getLevel()));
							if (random == 1) {
								final ItemStack nabbed = new RandomObject<>((ItemStack[]) o2).get(r.nextInt(29));
								final ItemStack copied = new ItemStack(nabbed);
								LabyrinthProvider.getInstance().getItemComposter().add(copied, p);
								nabbed.setAmount(0);
								p.playSound(p.getEyeLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 10, 1);
								if (!isMaxLevel()) {
									this.level++;
									player.chat("&a&oAbility " + getName() + " leveled up to " + this.level).queue();
									player.chat("&aYou found something...").queue();
									player.action("&a+1").queue();
								} else {
									player.chat("&aYou found something...").queue();
								}
							} else {
								player.action("&c&oAbility " + getName() + " failed.").queue();
							}
						}
					}
				}
				return this;
			});
		}
	};

	public static Trait.Ability SELECTIVE_KILL = new Trait.Ability() {
		int level = 2;
		final List<Entity> entities = new ArrayList<>();

		@Override
		public @NotNull String getName() {
			return "Selective Kill";
		}

		@Override
		public @NotNull String getDescription() {
			return "Left-click entities to select, Sneak + attack to early pull.";
		}

		@Override
		public int getLevel() {
			return level;
		}

		@Override
		public int getMaxLevel() {
			return 7;
		}

		@Override
		public void setLevel(int level) {
			this.level = level;
		}

		@Override
		public Deployable<Trait.Ability> run(@NotNull Object... args) {
			return Deployable.of(() -> {
				TaskScheduler.of(() -> {
					double damage = ClansAPI.getDataInstance().getConfig().read(c -> c.getNode("Addon.Traits.abilities.SELECTIVE_KILL.damage").toPrimitive().getDouble());
					if (args.length == 1) {
						Player p = (Player) args[0];
						if (entities.size() >= getLevel() && !entities.isEmpty()) {
							entities.forEach(entity -> {
								p.playSound(entity.getLocation(), Sound.AMBIENT_UNDERWATER_LOOP_ADDITIONS_RARE, 10, 1);
								entity.setGlowing(false);
								if (entity instanceof LivingEntity) {
									LivingEntity living = ((LivingEntity) entity);
									living.damage(damage);
								}
								TaskScheduler.of(() -> entities.remove(entity)).schedule();
							});
							if (!isMaxLevel()) {
								this.level++;
								Mailer.empty(p).chat("&aAbility " + getName() + " leveled up to " + this.level).queue();
								Mailer.empty(p).action("&a+1").queue();
							}
						} else {
							entities.forEach(entity -> {
								p.playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 1);
								entity.setGlowing(false);
								if (entity instanceof LivingEntity) {
									LivingEntity living = ((LivingEntity) entity);
									living.damage(damage);
								}
								TaskScheduler.of(() -> entities.remove(entity)).schedule();
							});
						}
					}
					if (args.length == 2) {
						Player p = (Player) args[0];
						Entity e = (Entity) args[1];
						if (entities.size() >= getLevel() && !entities.isEmpty()) {
							entities.forEach(entity -> {
								p.playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 1);
								entity.setGlowing(false);
								if (entity instanceof LivingEntity) {
									LivingEntity living = ((LivingEntity) entity);
									living.damage(damage);
								}
								TaskScheduler.of(() -> entities.remove(entity)).schedule();
							});
							if (!isMaxLevel()) {
								this.level++;
								Mailer.empty(p).chat("&aAbility " + getName() + " leveled up to " + this.level).queue();
								Mailer.empty(p).action("&a+1").queue();
							}
						}
						if (!entities.contains(e)) {
							entities.add(e);
							e.setGlowing(true);
						}
					}
				}).schedule();
				return this;
			});
		}
	};

	public static Trait.Ability FULL_STRENGTH = new Trait.Ability() {
		int level;
		boolean cooldown;

		@Override
		public @NotNull String getName() {
			return "Full Strength";
		}

		@Override
		public @NotNull String getDescription() {
			return "Use your full might when attacking, Sneak + attack.";
		}

		@Override
		public int getLevel() {
			return level;
		}

		@Override
		public int getMaxLevel() {
			return 155;
		}

		@Override
		public void setLevel(int level) {
			this.level = level;
		}

		@Override
		public Deployable<Trait.Ability> run(@NotNull Object... args) {
			return Deployable.of(() -> {
				if (args.length > 1) {
					Object o = args[0];
					if (o instanceof Entity) {
						Object o2 = args[1];
						if (o2 instanceof Player) {
							Player p = (Player) o2;
							Entity e = (Entity) o;
							if (cooldown) {
								Mailer.empty(p).action("&cAbility " + getName() + " on cooldown.").queue();
								return this;
							}
							if (e instanceof LivingEntity) {
								LivingEntity living = (LivingEntity) e;
								if (p.isSneaking()) {
									if (!isMaxLevel()) {
										level++;
										Mailer.empty(p).chat("&aAbility " + getName() + " leveled up to " + this.level).queue();
									}
									living.damage(getLevel() * 1.5);
									p.spawnParticle(Particle.SMOKE_LARGE, living.getEyeLocation(), 1);
									for (Entity surrounding : living.getNearbyEntities(3, 3, 3)) {
										if (surrounding instanceof Monster) {
											((Monster) surrounding).damage(getLevel() * 1.1);
											p.spawnParticle(Particle.SMOKE_LARGE, ((Monster) surrounding).getEyeLocation(), 1);
										}
									}
									Mailer.empty(p).action("&aYou strike with full force!");
									p.playSound(e.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 10, 1);
									cooldown = true;
									long cooldown = ClansAPI.getDataInstance().getConfigInt("Addon.Traits.abilities.FULL_STRENGTH.cooldown");
									TaskScheduler.of(() -> {
										this.cooldown = false;
										if (p != null && p.isOnline()) {
											Mailer.empty(p).chat("&aAbility " + getName() + " no longer on cooldown.").deploy();
										}
									}).scheduleLater(20 * cooldown);
								}
							}
						}
					}
				}
				return this;
			});
		}
	};

	public static Trait.Ability SUMMON_WOLVES = new Trait.Ability() {
		int level;
		@Override
		public @NotNull String getName() {
			return "Summon Wolves";
		}

		@Override
		public @NotNull String getDescription() {
			return "Sneak + Right-click to summon an army of wolves. Works on chance.";
		}

		@Override
		public int getLevel() {
			return level;
		}

		@Override
		public int getMaxLevel() {
			return 10;
		}

		@Override
		public void setLevel(int level) {
			this.level = level;
		}

		int getAmount(World world, TraitHolder holder) {
			int amount = 0;
			for (Wolf w : world.getEntitiesByClass(Wolf.class)) {
				if (w.isTamed()) {
					if (w.getOwner().getName().equalsIgnoreCase(holder.getName())) amount++;
				}
			}
			return amount;
		}

		@Override
		public Deployable<Trait.Ability> run(@NotNull Object... args) {
			return Deployable.of(() -> {
				if (args.length > 1) {
					Object o = args[0];
					if (o instanceof TraitHolder) {
						TraitHolder holder = (TraitHolder) o;
						Object o2 = args[1];
						if (o2 instanceof Location) {
							if (new Random().nextInt(28) == 13) {
								int amount = ClansAPI.getDataInstance().getConfigInt("Addon.Traits.abilities.SUMMON_WOLVES.max");
								if (getAmount(((Location) o2).getWorld(), holder) >= amount) {
									PlayerSearch tan = PlayerSearch.of(holder.getName());
									if (tan != null && tan.isOnline()) {
										Mailer.empty(tan.getPlayer().getPlayer()).chat("&c&oAbility " + getName() + " failed, limited to " + amount + " wolves per world.").deploy();
									}
									return this;
								}
								LabyrinthCollection<TamerWolf> toSpawn = new LabyrinthList<>();
								for (int i = 0; i < getLevel(); i++) {
									TamerWolf wolf = new TamerWolf(holder);
									toSpawn.add(wolf);
								}
								toSpawn.forEach(t -> t.spawn((Location) o2));
								this.level++;
								PlayerSearch tan = PlayerSearch.of(holder.getName());
								if (tan != null && tan.isOnline()) {
									Mailer.empty(tan.getPlayer().getPlayer()).chat("&aAbility " + getName() + " leveled up to " + this.level).queue();
								}
							} else {
								PlayerSearch tan = PlayerSearch.of(holder.getName());
								if (tan != null && tan.isOnline()) {
									Mailer.empty(tan.getPlayer().getPlayer()).action("&c&oAbility " + getName() + " failed.").deploy();
								}
							}
						}
					}
				}
				return this;
			});
		}
	};

	public static @Nullable Trait.Ability valueOf(@NotNull String name) {
		if (name.equalsIgnoreCase("Pick Pocket") || name.equalsIgnoreCase("PICK_POCKET")) return PICK_POCKET;
		if (name.equalsIgnoreCase("Selective Kill") || name.equalsIgnoreCase("SELECTIVE_KILL")) return SELECTIVE_KILL;
		if (name.equalsIgnoreCase("Full Strength") || name.equalsIgnoreCase("FULL_STRENGTH")) return FULL_STRENGTH;
		if (name.equalsIgnoreCase("Summon Wolves") || name.equalsIgnoreCase("SUMMON_WOLVES")) return SUMMON_WOLVES;
		return null;
	}

	public static Trait.Ability[] values() {
		return new Trait.Ability[]{PICK_POCKET, SELECTIVE_KILL, FULL_STRENGTH, SUMMON_WOLVES};
	}

}
