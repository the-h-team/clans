package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.clans.construct.impl.CooldownCreate;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.events.core.ClaimResidentEvent;
import com.github.sanctum.clans.events.core.ClanCreateEvent;
import com.github.sanctum.clans.events.core.ClanCreatedEvent;
import com.github.sanctum.clans.events.core.ClanWarActiveEvent;
import com.github.sanctum.clans.events.core.ClanWarStartEvent;
import com.github.sanctum.clans.events.core.ClanWarWonEvent;
import com.github.sanctum.clans.events.core.WildernessInhabitantEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.TimeWatch;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class ClanEventListener implements Listener {


	private String calc(long i) {
		String val = String.valueOf(i);
		int size = String.valueOf(i).length();
		if (size == 1) {
			val = "0" + i;
		}
		return val;
	}

	@Subscribe
	public void onWarStart(ClanWarStartEvent e) {
		War w = e.getWar();
		TimeWatch.Recording r = e.getRecording();
		Cooldown test = LabyrinthProvider.getService(Service.COOLDOWNS).getCooldown("war-" + w.getId() + "-start");
		if (test != null) {
			String time = calc(r.getMinutes()) + ":" + calc(r.getSeconds());
			if (time.equals("01:00")) {
				e.start();
				Cooldown.remove(test);
			} else {
				Message m = LabyrinthProvider.getService(Service.MESSENGER).getNewMessage();
				String t = calc(test.getMinutesLeft()) + ":" + calc(test.getSecondsLeft());
				w.forEach(a -> {
					Player p = a.getUser().toBukkit().getPlayer();
					if (p != null) {
						m.setPlayer(p).action("&2War start&f: &e" + t);
					}
				});
			}
		}
	}

	@Subscribe
	public void onResidency(ClaimResidentEvent e) {
		Clan owner = e.getClaim().getClan();
		if (owner.getMember(m -> m.getName().equals(e.getResident().getPlayer().getName())) == null) {
			if (!e.getResident().getPlayer().hasPermission("clanspro.claim.bypass")) {
				e.getResident().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 225, -1, false, false));
			}
		}
	}

	@Subscribe
	public void onWilderness(WildernessInhabitantEvent e) {
		e.getPlayer().removePotionEffect(PotionEffectType.SLOW_DIGGING);
	}

	@Subscribe
	public void onWarWatch(ClanWarActiveEvent e) {
		Cooldown timer = e.getWar().getTimer();
		Mailer msg = LabyrinthProvider.getService(Service.MESSENGER).getEmptyMailer();
		e.getWar().forEach(a -> {
			Player p = a.getUser().toBukkit().getPlayer();
			if (p != null) {
				War.Team t = e.getWar().getTeam(a.getClan());
				int points = e.getWar().getPoints(t);
				String time = calc(timer.getMinutesLeft()) + ":" + calc(timer.getSecondsLeft());
				msg.accept(p).action("&3Points&f:&b " + points + " &6| &3Time left&f:&e " + time).deploy();
			}
		});
	}

	@Subscribe
	public void onWarWin(ClanWarWonEvent e) {
		double reward = new Random().nextInt(e.getWinner().getValue()) + 0.17;
		e.getWinner().getKey().givePower(reward);
		e.getLosers().forEach((clan, integer) -> clan.takePower(reward));
	}

	@Subscribe
	public void onClanCreated(ClanCreatedEvent e) {
		DefaultClan c = e.getClan();
		if (ClansAPI.getData().isTrue("Clans.land-claiming.claim-influence.allow")) {
			if (ClansAPI.getData().getConfigString("Clans.land-claiming.claim-influence.dependence").equalsIgnoreCase("LOW")) {
				c.addMaxClaim(12);
			}
		}
	}


	@NotNull ClanCooldown creationCooldown(UUID id) {
		ClanCooldown target = null;
		for (ClanCooldown c : ClansAPI.getData().COOLDOWNS) {
			if (c.getAction().equals("Clans:create-limit") && c.getId().equals(id.toString())) {
				target = c;
				break;
			}
		}
		if (target == null) {
			target = new CooldownCreate(id);
			if (!ClansAPI.getData().COOLDOWNS.contains(target)) {
				target.save();
			}
		}
		return target;
	}

	@Subscribe
	public void onClanCreate(ClanCreateEvent event) {
		if (event.getMaker().isOnline()) {
			Player p = event.getMaker().getPlayer();
			if (ClansAPI.getInstance().isNameBlackListed(event.getClanName())) {
				String command = ClansAPI.getData().getMain().getRoot().getString("Clans.name-blacklist." + event.getClanName() + ".action");
				event.getUtil().sendMessage(p, "&c&oThis name is not allowed!");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Clan.ACTION.format(command, "{PLAYER}", p.getName()));
				event.setCancelled(true);
			}
			if (p != null && ClansAPI.getData().isTrue("Clans.creation.cooldown.enabled")) {
				if (creationCooldown(p.getUniqueId()).isComplete()) {
					creationCooldown(p.getUniqueId()).setCooldown();
				} else {
					event.setCancelled(true);
					event.stringLibrary().sendMessage(p, "&c&oYou can't do this right now.");
					event.stringLibrary().sendMessage(p, creationCooldown(p.getUniqueId()).fullTimeLeft());
					return;
				}
			}
			if (ClansAPI.getData().isTrue("Clans.creation.charge")) {
				double amount = ClansAPI.getData().getMain().getRoot().getDouble("Clans.creation.amount");
				Optional<Boolean> opt = EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(amount), p, p.getWorld().getName());

				boolean success = opt.orElse(false);
				if (!success) {
					event.setCancelled(true);
					event.stringLibrary().sendMessage(p, "&c&oYou don't have enough money. Amount needed: &6" + amount);
				}
			}
		}
	}

}
