package com.github.sanctum.clans.model.addon.traits.command;

import com.github.sanctum.clans.model.ClanVentBus;
import com.github.sanctum.clans.model.addon.traits.TraitManager;
import com.github.sanctum.clans.model.addon.traits.event.TraitSelectEvent;
import com.github.sanctum.clans.model.addon.traits.structure.DefaultTrait;
import com.github.sanctum.clans.model.addon.traits.structure.Trait;
import com.github.sanctum.clans.model.addon.traits.structure.TraitHolder;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.formatting.pagination.EasyPagination;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class TraitCommand extends ClanSubCommand {

	public TraitCommand() {
		super("trait");
		setAliases(Collections.singletonList("traits"));
	}

	@Override
	public boolean player(Player player, String label, String[] args) {

		TraitHolder holder = TraitManager.getInstance().get(player);

		if (args.length == 0) {
			FancyMessage message = new FancyMessage();
			message.then(" ");
			message.then("\n");
			message.then("&6Trait Information");
			message.then("\n");
			message.then("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
			message.then("\n");
			if (holder.getPrimary().getName().equals("Novice")) {
				message.then("&2Primary: ").then("&f" + holder.getPrimary().getName()).hover("&eClick to view traits.").action(() -> player.performCommand("c trait select"));
			} else {
				message.then("&2Primary: &f" + holder.getPrimary().getName()).then(" ").then("&b[Stats]");
				for (Trait.Ability a : holder.getPrimary().getAbilities()) {
					message.hover(a.getName() + ": Lvl." + a.getLevel() + "/" + a.getMaxLevel());
					message.hover("&7" + a.getDescription());
				}
				message.hover("&eClick to re-assign.");
				message.action(() -> player.performCommand("c trait select"));
			}
			message.then("\n");
			message.then(" ");
			message.then("\n");
			if (holder.getSecondary() != null) {
				message.then("&2Secondary: &f" + holder.getSecondary().getName()).then(" ").then("&b[Stats]");
				for (Trait.Ability a : holder.getSecondary().getAbilities()) {
					message.hover(a.getName() + ": Lvl." + a.getLevel() + "/" + a.getMaxLevel());
					message.hover("&7" + a.getDescription());
				}
				message.hover("&eClick to re-assign.");
				message.action(() -> player.performCommand("c trait select secondary"));
			} else {
				message.then("&2Secondary: ").then("&6Click to set").hover("&eClick to view traits.").action(() -> player.performCommand("c trait select secondary"));
			}
			message.then("\n");
			message.then("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
			message.send(player).deploy();
		}

		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("select")) {
				if (!Clan.ACTION.test(player, "clans.trait.primary").deploy()) {
					sendMessage(player, "&cYou don't have access to setting your primary trait!");
					sendMessage(player, "&cPermission required: &fclans.trait.primary");
					return true;
				}
				TraitSelectEvent event = ClanVentBus.call(new TraitSelectEvent(DefaultTrait.values()));
				EasyPagination<Trait> traits = new EasyPagination<>(player, event.getTraits());
				traits.setHeader((player1, message) -> {
					message.then("&6Click a trait to set it as primary").then("\n").then(Clan.ACTION.menuBorder());
				});
				traits.setFormat((trait, integer, message) -> {
					message.then("#&6" + integer + " ").then(trait.getName()).hover("&bClick to set.").action(() -> {
						if (holder.getSecondary() != null && holder.getSecondary().getName().equals(trait.getName())) {
							final Trait prim = holder.getPrimary();
							final Trait sec = holder.getSecondary();
							holder.setPrimary(sec);
							holder.setSecondary(prim);
							sendMessage(player, "&aPrimary trait set to &f" + trait.getName() + " &awith abilities &f[&e" + Arrays.stream(trait.getAbilities()).map(Trait.Ability::getName).collect(Collectors.joining(", ")) + "&f]");
						} else {
							new FancyMessage().then(ClansAPI.getInstance().getPrefix().toString()).then(" ").then("Setting &e" + trait.getName() + " &fas your primary trait might undo wanted progress from &6" + holder.getPrimary().getName()).then("\n").then("&7&l&m---------------------").then("\n").then(" &fDo you wish to ").then("&6Continue?").hover("&aClick to confirm.").action(() -> {
								holder.setPrimary(trait);
								sendMessage(player, "&aPrimary trait set to &f" + trait.getName() + " &awith abilities &f[&e" + Arrays.stream(trait.getAbilities()).map(Trait.Ability::getName).collect(Collectors.joining(", ")) + "&f]");
							}).send(player).deploy();
						}
					});
				});
				traits.setFooter((player1, message) -> {
					message.then(Clan.ACTION.menuBorder());
				});
				traits.limit(5);
				traits.send(1);
			}
		}

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("select")) {
				if (args[1].equalsIgnoreCase("secondary")) {
					if (!Clan.ACTION.test(player, "clans.trait.primary").deploy() || !Clan.ACTION.test(player, "clans.trait.secondary").deploy()) {
						sendMessage(player, "&cYou don't have access to setting your secondary trait!");
						sendMessage(player, "&cPermissions required: &fclans.trait.primary, clans.trait.secondary");
						return true;
					}
					TraitSelectEvent event = ClanVentBus.call(new TraitSelectEvent(DefaultTrait.values()));
					EasyPagination<Trait> traits = new EasyPagination<>(player, event.getTraits());
					traits.setHeader((player1, message) -> {
						message.then("&6Click a trait to set it as secondary").then("\n").then(Clan.ACTION.menuBorder());
					});
					traits.setFormat((trait, integer, message) -> {
						message.then("#&6" + integer + " ").then(trait.getName()).hover("&bClick to set.").action(() -> {
							if (!holder.getPrimary().getName().equals(trait.getName()) && holder.getSecondary() != null && !holder.getSecondary().getName().equals(trait.getName())) {
								if (holder.getSecondary() != null) {
									new FancyMessage().then(ClansAPI.getInstance().getPrefix().toString()).then(" ").then("Setting &e" + trait.getName() + " &fas your secondary trait might undo wanted progress from &6" + holder.getSecondary().getName()).then("\n").then("&7&l&m---------------------").then("\n").then(" &fDo you wish to ").then("&6Continue?").hover("&aClick to confirm.").action(() -> {
										holder.setSecondary(trait);
										sendMessage(player, "&aSecondary trait set to &f" + trait.getName() + " &awith abilities &f[&e" + Arrays.stream(trait.getAbilities()).map(Trait.Ability::getName).collect(Collectors.joining(", ")) + "&f]");
									}).send(player).deploy();
								} else {
									holder.setSecondary(trait);
									sendMessage(player, "&aSecondary trait set to &f" + trait.getName() + " &awith abilities &f[&e" + Arrays.stream(trait.getAbilities()).map(Trait.Ability::getName).collect(Collectors.joining(", ")) + "&f]");
								}
							} else {
								if (!holder.getPrimary().getName().equals(trait.getName())) {
									if (holder.getSecondary() != null) {
										new FancyMessage().then(ClansAPI.getInstance().getPrefix().toString()).then(" ").then("Setting &e" + trait.getName() + " &fas your secondary trait might undo wanted progress from &6" + holder.getSecondary().getName()).then("\n").then("&7&l&m---------------------").then("\n").then(" &fDo you wish to ").then("&6Continue?").hover("&aClick to confirm.").action(() -> {
											holder.setSecondary(trait);
											sendMessage(player, "&aSecondary trait set to &f" + trait.getName() + " &awith abilities &f[&e" + Arrays.stream(trait.getAbilities()).map(Trait.Ability::getName).collect(Collectors.joining(", ")) + "&f]");
										}).send(player).deploy();
									} else {
										holder.setSecondary(trait);
										sendMessage(player, "&aSecondary trait set to &f" + trait.getName() + " &awith abilities &f[&e" + Arrays.stream(trait.getAbilities()).map(Trait.Ability::getName).collect(Collectors.joining(", ")) + "&f]");
									}
								} else {
									sendMessage(player, "&cYou already have this trait selected!");
								}
							}
						});
					});
					traits.setFooter((player1, message) -> {
						message.then(Clan.ACTION.menuBorder());
					});
					traits.limit(5);
					traits.send(1);
				}
			}
		}

		return false;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, getBaseCompletion(args))
				.then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, "select")
				.then(TabCompletionIndex.THREE, "select", TabCompletionIndex.TWO, "secondary")
				.get();
	}

}
