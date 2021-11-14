package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.bridge.internal.stashes.events.StashInteractEvent;
import com.github.sanctum.clans.bridge.internal.vaults.events.VaultInteractEvent;
import com.github.sanctum.clans.construct.GUI;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBlueprint;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Consultant;
import com.github.sanctum.clans.construct.api.InvasiveEntity;
import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.clans.construct.extra.AnimalConsultantListener;
import com.github.sanctum.clans.construct.impl.CooldownCreate;
import com.github.sanctum.clans.construct.impl.SimpleEntry;
import com.github.sanctum.clans.event.associate.AssociateChatEvent;
import com.github.sanctum.clans.event.associate.AssociateDisplayInfoEvent;
import com.github.sanctum.clans.event.associate.AssociateFromAnimalEvent;
import com.github.sanctum.clans.event.associate.AssociateMessageReceiveEvent;
import com.github.sanctum.clans.event.associate.AssociateObtainLandEvent;
import com.github.sanctum.clans.event.claim.ClaimInteractEvent;
import com.github.sanctum.clans.event.claim.ClaimResidentEvent;
import com.github.sanctum.clans.event.claim.WildernessInhabitantEvent;
import com.github.sanctum.clans.event.clan.ClanFreshlyFormedEvent;
import com.github.sanctum.clans.event.clan.ClansLoadingProcedureEvent;
import com.github.sanctum.clans.event.player.PlayerCreateClanEvent;
import com.github.sanctum.clans.event.war.WarActiveEvent;
import com.github.sanctum.clans.event.war.WarStartEvent;
import com.github.sanctum.clans.event.war.WarWonEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.FancyMessageChain;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TimeWatch;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
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
	public void onInteract(ClaimInteractEvent e) {
		if (e.getAssociate() != null) {
			if (e.getAssociate().getClan().equals(e.getClan())) {
				Claim.Flag f = e.getClaim().getFlag("owner-only");
				if (f.isValid()) {
					if (f.isEnabled()) {
						if (e.getAssociate().getPriority().toInt() != 3) {
							Clan.ACTION.sendMessage(e.getPlayer(), "&cThis is a clan owner only chunk! You can't do this here.");
							e.setCancelled(true);
						}
					}
				}
				Claim.Flag water = e.getClaim().getFlag("infinite-water");
				if (water.isValid()) {
					if (water.isEnabled()) {
						if (e.getItemInMainHand().getType() == Material.BUCKET) {
							if (e.getBlock().getType() == Material.WATER) {
								e.getItemInMainHand().setType(Material.WATER_BUCKET);
								e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WATER_AMBIENT, 10, 1);
								Clan.ACTION.sendMessage(e.getPlayer(), "&eHere's some magic water!");
								e.setCancelled(true);
							}
						}
					}
				}
				Claim.Flag flam = e.getClaim().getFlag("no-flammables");
				if (flam.isValid()) {
					if (flam.isEnabled()) {
						if (e.getInteraction() == ClaimInteractEvent.Type.USE) {
							if (e.getItemInMainHand().getType() == Material.LAVA_BUCKET) {
								Clan.ACTION.sendMessage(e.getPlayer(), "&cFlammables aren't allowed within this chunk.");
								e.setCancelled(true);
							}
							return;
						}
						if (e.getItemInMainHand().getType() == Material.FLINT_AND_STEEL || e.getItemInMainHand().getType() == Material.FIRE_CHARGE) {
							Clan.ACTION.sendMessage(e.getPlayer(), "&cFlammables aren't allowed within this chunk.");
							e.setCancelled(true);
						}
					}
				}
			}
		}

	}

	@Subscribe
	public void onAnimal(AssociateFromAnimalEvent e) {
		Consultant consultant = e.getAssociate().getConsultant();
		if (!consultant.hasIncomingListener(e.getAssociate().getTag())) {
			AnimalConsultantListener listener = new AnimalConsultantListener(e.getAssociate());
			consultant.registerOutgoingListener(e.getAssociate().getTag(), listener);
			consultant.registerIncomingListener(e.getAssociate().getTag(), listener);
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEntityEvent e) {
		Entity entity = e.getRightClicked();
		Player p = e.getPlayer();
		ClansAPI.getInstance().getAssociate(p).ifPresent(associate -> {
			Clan c = associate.getClan();
			Clan.Associate test = ClansAPI.getInstance().getAssociate(entity.getUniqueId()).orElse(null);
			if (test != null) {
				if (test.getClan().equals(associate.getClan())) {
					// TODO: check for item on removal.
					ItemStack item = p.getInventory().getItemInMainHand();
					if (item.getType() == Material.STICK) {
						if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
							if (StringUtils.use(StringUtils.use("&r[&bRemover stick&r]").translate()).containsIgnoreCase(item.getItemMeta().getDisplayName())) {
								test.remove();
								item.setAmount(Math.max(0, item.getAmount() - 1));
								if (item.getAmount() == 0) {
									p.getInventory().remove(item);
								}
								c.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-leave"), test.getName()));
							}
						}
					}
				}
			} else {
				if (!(entity instanceof Tameable)) return;
				if (!((Tameable)entity).isTamed()) return;
				if (((Tameable)entity).getOwner() == null) return;
				if (((Tameable)entity).getOwner().getName() == null) return;
				if (!((Tameable)entity).getOwner().getName().equals(associate.getName())) return;
				ItemStack item = p.getInventory().getItemInMainHand();
				if (item.getType() == Material.BLAZE_ROD) {
					if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
						if (StringUtils.use(StringUtils.use("&r[&6Tamer stick&r]").translate()).containsIgnoreCase(item.getItemMeta().getDisplayName())) {
							item.setAmount(Math.max(0, item.getAmount() - 1));
							if (item.getAmount() == 0) {
								p.getInventory().remove(item);
							}
							// do entity stuff
							int count = 0;
							for (Clan.Associate a : c.getMembers()) {
								if (StringUtils.use(a.getName()).containsIgnoreCase(((Tameable) entity).getOwner().getName() + "'s " + entity.getName())) {
									count++;
								}
							}
							InvasiveEntity conversion = InvasiveEntity.wrapNonAssociated(entity, count == 0 ? ((Tameable) entity).getOwner().getName() + "'s " + entity.getName() : ((Tameable) entity).getOwner().getName() + "'s " + entity.getName() + " x" + count);

							Clan.Associate newAssociate = c.newAssociate(conversion);

							if (newAssociate != null) {
								c.add(newAssociate);
								// TODO: make ClanBroadcastMessageEvent
								c.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-join"), newAssociate.getName()));
							}
						}
					}
				}
			}
		});
	}

	@Subscribe(priority = Vent.Priority.READ_ONLY)
	public void onClaim(AssociateObtainLandEvent e) {
		for (Claim.Flag f : ClansAPI.getInstance().getClaimManager().getFlagManager().getFlags()) {
			if (e.getClaim().getFlag(f.getId()) == null) {
				e.getClaim().register(f);
			} else {
				if (!e.getClaim().getFlag(f.getId()).isValid()) {
					e.getClaim().remove(e.getClaim().getFlag(f.getId()));
					e.getClaim().register(f);
				}
			}
		}
	}

	@Subscribe
	public void onLoad(ClansLoadingProcedureEvent e) {
		if (e.getClans().stream().noneMatch(c -> c.getName().equals("Labyrinth"))) {
			UUID server = e.getApi().getSessionId();
			ClanBlueprint blueprint = new ClanBlueprint("Labyrinth", true).setLeader(server);
			Clan clan = blueprint.toBuilder().supply().givePower(4.2).getClan();
			e.insert(clan);
		}
		if (ClansAPI.getDataInstance().isTrue("Formatting.console-debug")) {
			e.getClans().forEach(clan -> e.getApi().debugConsole(clan));
		}
	}

	@Subscribe(priority = Vent.Priority.READ_ONLY)
	public void onChat(AssociateChatEvent e) {
		Consultant server = e.getApi().getConsultant();
		if (server != null) {
			server.sendMessage(e::getMessage);
		}
	}

	@Subscribe(priority = Vent.Priority.READ_ONLY)
	public void onChat(AssociateMessageReceiveEvent e) {
		Consultant server = e.getAssociate().getConsultant();
		if (server != null) {
			server.sendMessage(() -> new SimpleEntry<>(e.getMessage(), new SimpleEntry<>(e.getChannel(), e.getSender())));
		}
	}

	@Subscribe(priority = Vent.Priority.LOW)
	public void onInfo(AssociateDisplayInfoEvent e) {
		Player p = e.getPlayer();
		Clan c = e.getClan();

		if (ClansAPI.getDataInstance().isTrue("Formatting.pretty-info")) {
			e.setCancelled(true);
			//=======================
			//
			//       (ClanName)
			//
			//        [Stats]
			// [Roster]     [Perms]
			//        [Mode]
			// [Bank] [Base] [Vault]
			//       [Stash]
			//
			//=======================
			String color;
			FancyMessageChain chain = null;
			switch (e.getType()) {
				case OTHER:
					color = "&2";
					chain = new FancyMessageChain()
							.append(space1 -> space1.then(" "))
							.append(top -> top.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then("[")
									.then("Stats").color(Color.GREEN).style(ChatColor.BOLD).hover(color + "Name: &f" + c.getName()).hover(color + "&rDescription: &f" + c.getDescription()).hover(color + "&rPower: &f" + Clan.ACTION.format(c.getPower())).hover(color + "&rColor: &f" + (c.getPalette().isGradient() ? (c.getPalette().toArray()[0] + c.getPalette().toArray()[1]).replace("&", "").replace("#", "&f»" + color + "&r") : color.replace("&", "&f»" + color + "&r").replace("#", "&f»" + color + "&r"))).hover(color + "&rClaims: &f" + c.getClaims().length + "/" + c.getClaimLimit())
									.then("]")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" "))
							.append(space1 -> space1.then(" "))
							.append(middle -> middle.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then("[")
									.then("Roster").color(Color.GREEN).style(ChatColor.BOLD).hover(color + "Click to view our roster.").action(() -> e.getApi().getMenu(GUI.MEMBER_LIST, c).open(p))
									.then("]")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" "))
							.append(space1 -> space1.then(" "));
					break;
				case PERSONAL:
					color = c.getPalette().toString();
					chain = new FancyMessageChain()
							.append(top -> top.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then("[")
									.then("Stats").color(Color.RED).style(ChatColor.BOLD).hover(color + "Name: &f" + c.getName()).hover(color + "&rDescription: &f" + c.getDescription()).hover(color + "&rPower: &f" + Clan.ACTION.format(c.getPower())).hover(color + "&rColor: &f" + (c.getPalette().isGradient() ? (c.getPalette().toArray()[0] + c.getPalette().toArray()[1]).replace("&", "").replace("#", "&f»" + color + "&r") : color.replace("&", "&f»" + color + "&r").replace("#", "&f»" + color + "&r"))).hover(color + "&rClaims: &f" + c.getClaims().length + "/" + c.getClaimLimit())
									.then("]")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" "))
							.append(space1 -> space1.then(" "))
							.append(top_middle -> top_middle.then(" ")
									.then("[")
									.then("Roster").color(Color.RED).style(ChatColor.BOLD).action(() -> GUI.MEMBER_LIST.get(c).open(p)).hover(color + "&oClick to view the clan roster.")
									.then("]")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then("[")
									.then("Perms").color(Color.RED).style(ChatColor.BOLD).hover(color + "Click to manage clan permissions.").command("/c perms")
									.then("]")
									.then(" "))
							.append(space2 -> space2.then(" "))
							.append(middle -> middle.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then("[")
									.then("Mode").color(Color.RED).style(ChatColor.BOLD).hover(color + "Click to toggle our pvp mode.").command("/c mode")
									.then("]")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" "))
							.append(space3 -> space3.then(" "))
							.append(bottom_middle -> bottom_middle.then(" ")
									.then("[")
									.then("Bank").color(Color.RED).style(ChatColor.BOLD).command("/c bank").hover(color + "&oClick to view the clan bank.")
									.then("]")
									.then(" ")
									.then("[")
									.then("Base").color(Color.RED).style(ChatColor.BOLD).hover(color + "Click to teleport to base.").command("/c base")
									.then("]")
									.then(" ")
									.then("[")
									.then("Vault").color(Color.RED).style(ChatColor.BOLD).hover(color + "Click to open the clan vault.").command("/c vault")
									.then("]")
									.then(" "))
							.append(space4 -> space4.then(" "))
							.append(bottom -> bottom.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then(" ")
									.then("[")
									.then("Stash").color(Color.RED).style(ChatColor.BOLD).hover(color + "Click to open the clan stash.").command("/c stash")
									.then("]")
									.then(" ")
									.then(" ")
									.then(" "));
					break;
			}
			chain.send(e.getPlayer()).deploy();
		}
	}

	@Subscribe
	public void onWarStart(WarStartEvent e) {
		War w = e.getWar();
		TimeWatch.Recording r = e.getRecording();
		Cooldown test = LabyrinthProvider.getService(Service.COOLDOWNS).getCooldown("war-" + w.getId() + "-start");
		if (test != null) {
			String time = calc(r.getMinutes()) + ":" + calc(r.getSeconds());
			if (time.equals("01:00")) {
				e.start();
				Cooldown.remove(test);
			} else {
				Mailer m = LabyrinthProvider.getService(Service.MESSENGER).getEmptyMailer();
				String t = calc(test.getMinutesLeft()) + ":" + calc(test.getSecondsLeft());
				w.forEach(a -> {
					Player p = a.getUser().toBukkit().getPlayer();
					if (p != null) {
						m.accept(p).action("&2War start&f: &e" + t).deploy();
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
		} else {
			if (e.getResident().getPlayer().hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
				e.getResident().getPlayer().removePotionEffect(PotionEffectType.SLOW_DIGGING);
			}
		}
		e.getApi().getAssociate(e.getResident().getPlayer()).ifPresent(a -> {
			for (Clan ally : owner.getRelation().getAlliance().get(Clan.class)) {
				if (ally.getName().equals(a.getClan().getName())) {
					if (e.getResident().getPlayer().hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
						e.getResident().getPlayer().removePotionEffect(PotionEffectType.SLOW_DIGGING);
					}
					break;
				}
			}
		});
	}

	@Subscribe
	public void onWilderness(WildernessInhabitantEvent e) {
		if (e.getPlayer().hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
			e.getPlayer().removePotionEffect(PotionEffectType.SLOW_DIGGING);
		}
	}

	@Subscribe
	public void onWarWatch(WarActiveEvent e) {
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
	public void onWarWin(WarWonEvent e) {

		double reward = new Random().nextInt(e.getWinner().getValue()) + 0.17;
		e.getWinner().getKey().givePower(reward);
		e.getLosers().forEach((clan, integer) -> clan.takePower(reward));
	}

	@Subscribe
	public void onClanCreated(ClanFreshlyFormedEvent e) {
		Clan c = e.getClan();
		if (ClansAPI.getDataInstance().isTrue("Clans.land-claiming.claim-influence.allow")) {
			if (ClansAPI.getDataInstance().getConfigString("Clans.land-claiming.claim-influence.dependence").equalsIgnoreCase("LOW")) {
				c.giveClaims(12);
			}
		}
	}


	@NotNull ClanCooldown creationCooldown(UUID id) {
		ClanCooldown target = null;
		for (ClanCooldown c : ClansAPI.getDataInstance().getCooldowns()) {
			if (c.getAction().equals("Clans:create-limit") && c.getId().equals(id.toString())) {
				target = c;
				break;
			}
		}
		if (target == null) {
			target = new CooldownCreate(id);
			if (!ClansAPI.getDataInstance().getCooldowns().contains(target)) {
				target.save();
			}
		}
		return target;
	}

	@Subscribe
	public void onClanCreate(PlayerCreateClanEvent event) {
		if (event.getPlayer() != null) {
			Player p = event.getPlayer();
			if (ClansAPI.getInstance().isNameBlackListed(event.getClanName())) {
				String command = ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.name-blacklist." + event.getClanName().toLowerCase() + ".action");
				event.getUtil().sendMessage(p, "&c&oThis name is not allowed!");
				if (command != null && !command.isEmpty()) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Clan.ACTION.format(command, "{PLAYER}", p.getName()));
				}
				event.setCancelled(true);
			}
			if (p != null && ClansAPI.getDataInstance().isTrue("Clans.creation.cooldown.enabled")) {
				if (creationCooldown(p.getUniqueId()).isComplete()) {
					creationCooldown(p.getUniqueId()).setCooldown();
				} else {
					event.setCancelled(true);
					event.getUtil().sendMessage(p, "&c&oYou can't do this right now.");
					event.getUtil().sendMessage(p, creationCooldown(p.getUniqueId()).fullTimeLeft());
					return;
				}
			}
			if (ClansAPI.getDataInstance().isTrue("Clans.creation.charge")) {
				double amount = ClansAPI.getDataInstance().getConfig().getRoot().getDouble("Clans.creation.amount");

				boolean success = EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(amount), p, p.getWorld().getName()).orElse(false);
				if (!success) {
					event.setCancelled(true);
					event.getUtil().sendMessage(p, "&c&oYou don't have enough money. Amount needed: &6" + amount);
				}
			}
		}
	}

	@Subscribe
	public void onVault(VaultInteractEvent e) {
		Bukkit.getOnlinePlayers().forEach(p -> {
			if (ClansAPI.getDataInstance().isSpy(p)) {
				ItemStack item = e.getClickedItem();
				if (item != null) {
					if (e.getAction().name().contains("PICKUP") || e.getAction().name().contains("DROP") || e.getAction().name().contains("MOVE")) {
						if (item.getType() == Material.AIR) return;
						Message m = new FancyMessage().then("&b[&7Vaults&b] &f(" + e.getClan().getName() + ")").then(" ").then((item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name().toLowerCase())).color(ChatColor.AQUA).hover(item).then(" ").then("&fx" + item.getAmount() + " removed by &b" + e.getWhoClicked().getName());
						Mailer.empty(p).chat(m.build()).deploy();
					}
					if (e.getAction().name().contains("PLACE")) {
						Message m = new FancyMessage().then("&b[&7Vaults&b] &f(" + e.getClan().getName() + ")").then(" ").then((item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name().toLowerCase())).color(ChatColor.GREEN).hover(item).then(" ").then("&fx" + item.getAmount() + " added by &a" + e.getWhoClicked().getName());
						Mailer.empty(p).chat(m.build()).deploy();
					}
				}
			}
		});
	}

	@Subscribe
	public void onVault(StashInteractEvent e) {
		Bukkit.getOnlinePlayers().forEach(p -> {
			if (ClansAPI.getDataInstance().isSpy(p)) {
				ItemStack item = e.getClickedItem();
				if (item != null) {
					if (e.getAction().name().contains("PICKUP") || e.getAction().name().contains("DROP") || e.getAction().name().contains("MOVE")) {
						if (item.getType() == Material.AIR) return;
						Message m = new FancyMessage().then("&2[&7Stashes&2] &f(" + e.getClan().getName() + ")").then(" ").then((item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name().toLowerCase())).color(ChatColor.AQUA).hover(item).then(" ").then("&fx" + item.getAmount() + " removed by &b" + e.getWhoClicked().getName());
						Mailer.empty(p).chat(m.build()).deploy();
					}
					if (e.getAction().name().contains("PLACE")) {
						Message m = new FancyMessage().then("&2[&7Stashes&2] &f(" + e.getClan().getName() + ")").then(" ").then((item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name().toLowerCase())).color(ChatColor.GREEN).hover(item).then(" ").then("&fx" + item.getAmount() + " added by &a" + e.getWhoClicked().getName());
						Mailer.empty(p).chat(m.build()).deploy();
					}
				}
			}
		});
	}

}
