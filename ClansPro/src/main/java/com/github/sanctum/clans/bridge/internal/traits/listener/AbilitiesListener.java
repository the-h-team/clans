package com.github.sanctum.clans.bridge.internal.traits.listener;

import com.github.sanctum.clans.bridge.internal.traits.TraitManager;
import com.github.sanctum.clans.bridge.internal.traits.structure.Trait;
import com.github.sanctum.clans.bridge.internal.traits.structure.TraitHolder;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.event.custom.DefaultEvent;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.formatting.string.FormattedString;
import com.github.sanctum.labyrinth.library.DirectivePoint;
import com.github.sanctum.labyrinth.library.Item;
import com.github.sanctum.labyrinth.library.RandomObject;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class AbilitiesListener implements Listener {

	final Supplier<ItemStack[]> lootPool = () -> new ItemStack[]{new Item.Edit(Material.DIAMOND).build(),
			new Item.Edit(new RandomObject<>(Arrays.stream(Material.values()).filter(Material::isBlock).collect(Collectors.toList())).get(new Random().nextInt(24))).build(),
			new Item.Edit(new RandomObject<>(Arrays.stream(Material.values()).filter(m -> new FormattedString(m.name()).contains("chest", "helmet", "boot", "leg", "pant")).collect(Collectors.toList())).get(new Random().nextInt(100))).build(),
			new Item.Edit(Material.SLIME_BALL).setAmount(new Random().nextInt(24)).build()
	};

	@Subscribe
	public void onJoin(DefaultEvent.Join e) {
		TraitManager.getInstance().getOrCreate(e.getPlayer());
	}

	@EventHandler
	public void onHit(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player || e.getDamager() instanceof Projectile && ((Projectile)e.getDamager()).getShooter() instanceof Player) {
			Player p = (Player) (e.getDamager() instanceof Player ? e.getDamager() : ((Projectile)e.getDamager()).getShooter());
			TraitHolder holder = TraitManager.getInstance().get(p);
			Trait primary = holder.getPrimary();
			switch (primary.getName().toLowerCase(Locale.ROOT)) {
				case "assassin":
					Trait.Ability selective = primary.getAbility("Selective Kill");
					if (selective != null) {
						if (p.isSneaking()) {
							selective.run(p).deploy();
						} else {
							selective.run(p, e.getEntity()).deploy();
						}
					}
					break;

				case "brute":
					Trait.Ability full = primary.getAbility("Full Strength");
					if (full != null) {
						full.run(e.getEntity(), p).deploy();
					}
					break;
			}
		}
	}

	@EventHandler
	public void onRightClick(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_AIR) {
			if (e.getPlayer().isSneaking()) {
				TraitHolder holder = TraitManager.getInstance().get(e.getPlayer());
				Trait primary = holder.getPrimary();
				if (primary.getName().equalsIgnoreCase("tamer")) {
					Trait.Ability summon = primary.getAbility("Summon Wolves");
					if (summon != null) {
						DirectivePoint point = DirectivePoint.get(e.getPlayer());
						summon.run(holder, e.getPlayer().getLocation()).deploy();
					}
				}
			}
		}
	}

	@EventHandler
	public void onInteractPlayer(PlayerInteractEntityEvent e) {
		TraitHolder holder = TraitManager.getInstance().get(e.getPlayer());
		Trait primary = holder.getPrimary();
		if (primary.getName().equalsIgnoreCase("Assassin")) {
			Trait.Ability pick_pocket = primary.getAbility("Pick Pocket");
			if (pick_pocket != null) {
				if (e.getPlayer().isSneaking()) {
					if (e.getRightClicked() instanceof Player) {
						pick_pocket.run(e.getPlayer(), e.getRightClicked()).deploy();

					} else {
						if (!ClansAPI.getDataInstance().getConfig().read(c -> c.getNode("Addon.Traits.abilities.PICK_POCKET.entity-blacklist").toPrimitive().getStringList()).contains(e.getRightClicked().getName())) {
							pick_pocket.run(e.getPlayer(), lootPool.get()).deploy();
						}
					}
					e.setCancelled(true);
				}
			}
		}
	}

}
