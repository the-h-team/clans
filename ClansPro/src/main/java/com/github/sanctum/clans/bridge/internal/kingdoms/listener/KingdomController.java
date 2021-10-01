package com.github.sanctum.clans.bridge.internal.kingdoms.listener;

import com.github.sanctum.clans.bridge.internal.KingdomAddon;
import com.github.sanctum.clans.bridge.internal.kingdoms.Kingdom;
import com.github.sanctum.clans.bridge.internal.kingdoms.Quest;
import com.github.sanctum.clans.bridge.internal.kingdoms.Reward;
import com.github.sanctum.clans.bridge.internal.kingdoms.event.KingdomQuestCompletionEvent;
import com.github.sanctum.clans.construct.RankPriority;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.events.core.ClaimInteractEvent;
import com.github.sanctum.clans.events.core.ClanLeaveEvent;
import com.github.sanctum.labyrinth.event.custom.DefaultEvent;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class KingdomController implements Listener {

	private final KingdomAddon addon;

	public KingdomController(KingdomAddon kingdomAddon) {
		this.addon = kingdomAddon;
	}

	@Subscribe(priority = Vent.Priority.READ_ONLY, processCancelled = true)
	public void onJobComplete(KingdomQuestCompletionEvent e) {
		Reward<?> reward = e.getQuest().getReward();
		if (reward != null) {
			reward.give(e.getKingdom());
			e.getKingdom().forEach(c -> {
				c.broadcast("&bQuest &e" + e.getQuest().getTitle() + " &bcomplete.");
				c.broadcast(reward.getMessage());
			});
		}
	}

	@Subscribe(priority = Vent.Priority.LOW)
	public void onSpawner(DefaultEvent.BlockBreak e) {
		if (e.isCancelled()) return;
		Block b = e.getBlock();
		if (b.getType() == Material.SPAWNER) {
			ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {
				Clan c = a.getClan();
				String name = c.getValue(String.class, "kingdom");
				if (name != null) {
					Kingdom kingdom = Kingdom.getKingdom(name);
					if (kingdom != null) {
						Quest q = kingdom.getQuest("Monsters Box");
						if (q != null) {
							if (q.activated(e.getPlayer())) {
								q.progress(1.0);
								if (q.isComplete()) {
									c.forEach(ass -> {
										Player n = ass.getUser().toBukkit().getPlayer();
										if (n != null) {
											q.deactivate(n);
										}
									});
								}
							}
						}
					}
				}
			});
		}

	}

	@Subscribe
	public void onClanLeave(ClanLeaveEvent e) {
		if (e.getAssociate().getPriority() == RankPriority.HIGHEST) {

			Clan c = e.getAssociate().getClan();

			String key = c.getValue(String.class, "kingdom");

			if (key != null) {

				Kingdom k = Kingdom.getKingdom(key);

				if (k.getMembers().size() == 1) {

					addon.getMailer().prefix().start(Clan.ACTION.getPrefix()).finish().announce(player -> true, "&rKingdom &6" + k.getName() + " &rhas fallen").deploy();

					k.remove(addon);

				} else {
					k.getMembers().remove(c);
				}

			}

		}
	}

	@Subscribe
	public void onInteract(ClaimInteractEvent e) {
		Player p = e.getPlayer();

		Block b = e.getBlock();

		ClansAPI API = ClansAPI.getInstance();

		if (e.getInteraction() == ClaimInteractEvent.Type.BUILD) {

			if (API.isInClan(p.getUniqueId())) {

				Clan c = API.getClan(p.getUniqueId());

				String name = c.getValue(String.class, "kingdom");

				if (name != null) {

					Kingdom k = Kingdom.getKingdom(name);

					Quest achievement;

					Stream<Material> stream = Stream.of(Material.STONE_BRICKS, Material.STONE, Material.ANDESITE, Material.COBBLESTONE, Material.DIORITE, Material.ANDESITE);

					if (stream.anyMatch(m -> StringUtils.use(m.name()).containsIgnoreCase(b.getType().name()))) {

						achievement = k.getQuest("Walls");

						if (achievement == null) return;

						if (achievement.activated(p)) {

							if (!achievement.isComplete()) {

								achievement.progress(1);

								c.forEach(a -> {
									Player online = a.getUser().toBukkit().getPlayer();
									if (online != null) {
										addon.getMailer().accept(online).action("&eWalls&r: " + achievement.getPercentage() + "% &bcomplete").deploy();
									}
								});


							} else {

								c.forEach(ass -> {
									Player n = ass.getUser().toBukkit().getPlayer();
									if (n != null) {
										achievement.deactivate(n);
									}
								});

							}
						}

					}

				}

			}

		}

		if (e.getInteraction() == ClaimInteractEvent.Type.BREAK) {

			if (API.isInClan(p.getUniqueId())) {

				Clan c = API.getClan(p.getUniqueId());

				String name = c.getValue(String.class, "kingdom");

				if (name != null) {

					Kingdom k = Kingdom.getKingdom(name);

					Quest achievement;

					Stream<Material> stream = Stream.of(Material.STONE_BRICKS, Material.STONE, Material.ANDESITE, Material.COBBLESTONE, Material.DIORITE, Material.ANDESITE);

					if (stream.anyMatch(m -> StringUtils.use(m.name()).containsIgnoreCase(b.getType().name()))) {

						achievement = k.getQuest("Walls");

						if (achievement == null) return;

						if (achievement.activated(p)) {

							if (!achievement.isComplete()) {

								achievement.unprogress(1);

								addon.getMailer().accept(p).action("&eWalls&r: " + achievement.getPercentage() + "% &bcomplete").deploy();


							}
						}

					}

				}

			}

		}
	}

}
