package com.github.sanctum.clans.model.addon.borders;

import com.github.sanctum.clans.model.addon.borders.task.BorderTask;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.library.Mailer;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TerritoryCommand extends ClanSubCommand {
	public TerritoryCommand() {
		super("territory");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		int length = args.length;
		String prefix = "&f[&6&lX&f]&r";
		Mailer msg = Mailer.empty(p).prefix().start(prefix).finish();
		if (length == 0) {
			if (!BorderListener.toggled.containsKey(p.getUniqueId())) {
				msg.chat(BorderListener.coToggle(Clan.ACTION.getPrefix().replace("&6", "&#eb9534&l") + " &#3ee67c&lChunk borders on &#ffffff| &#3ee6ddClick to toggle ", prefix.replace("&6", "&#eb9534"))).deploy();
				BorderTask.run(p);
				BorderListener.toggled.put(p.getUniqueId(), true);
				return true;
			}
			if (BorderListener.toggled.get(p.getUniqueId())) {
				msg.chat(BorderListener.coToggle(Clan.ACTION.getPrefix().replace("&6", "&#eb9534&l") + " &#eb4034&lChunk borders off &#ffffff| &#3ee6ddClick to toggle ", prefix.replace("&6", "&#eb9534"))).deploy();
				BorderListener.toggled.remove(p.getUniqueId());
				BorderListener.baseLocate.remove(p.getUniqueId());
				BorderListener.spawnLocate.remove(p.getUniqueId());
				BorderListener.playerLocate.remove(p.getUniqueId());
				return true;
			}
			return true;
		}

		if (length == 1) {

			return true;
		}

		if (length == 2) {
			if (args[0].equalsIgnoreCase("-f")) {
				if (args[1].equalsIgnoreCase("base")) {
					if (ClansAPI.getInstance().getClanManager().getClanID(p.getUniqueId()) != null) {
						if (!BorderListener.baseLocate.contains(p.getUniqueId())) {
							BorderListener.baseLocate.add(p.getUniqueId());
							BorderListener.spawnLocate.remove(p.getUniqueId());
							BorderListener.playerLocate.remove(p.getUniqueId());
							if (!BorderListener.toggled.containsKey(p.getUniqueId())) {
								msg.chat(BorderListener.coToggle(Clan.ACTION.getPrefix().replace("&6", "&#eb9534&l") + " &#3ee67c&lChunk borders on &#ffffff| &#3ee6ddClick to toggle ", prefix.replace("&6", "&#eb9534"))).deploy();
								BorderTask.run(p);
								BorderListener.toggled.put(p.getUniqueId(), true);
							}
							msg.chat("&6&oBase flag has been enabled.").deploy();
							msg.chat("&7You are now locating your clans base location.").deploy();
						} else {
							BorderListener.baseLocate.remove(p.getUniqueId());
							msg.chat("&c&oBase flag has been disabled.").deploy();
						}
					} else {
						Clan.ACTION.sendMessage(p, Clan.ACTION.notInClan());
					}
					return true;
				}
				if (args[1].equalsIgnoreCase("player")) {
					if (!BorderListener.playerLocate.contains(p.getUniqueId())) {
						BorderListener.playerLocate.add(p.getUniqueId());
						BorderListener.spawnLocate.remove(p.getUniqueId());
						BorderListener.baseLocate.remove(p.getUniqueId());
						if (!BorderListener.toggled.containsKey(p.getUniqueId())) {
							msg.chat(BorderListener.coToggle(Clan.ACTION.getPrefix().replace("&6", "&#eb9534&l") + " &#3ee67c&lChunk borders on &#ffffff| &#3ee6ddClick to toggle ", prefix.replace("&6", "&#eb9534"))).deploy();
							BorderTask.run(p);
							BorderListener.toggled.put(p.getUniqueId(), true);
						}
						msg.chat("&6&oPlayer flag has been enabled.").deploy();
						msg.chat("&7You are now locating any player within 500 blocks").deploy();
					} else {
						BorderListener.playerLocate.remove(p.getUniqueId());
						msg.chat("&c&oPlayer flag has been disabled.").deploy();
					}
					return true;
				}
				if (args[1].equalsIgnoreCase("spawn")) {
					if (!BorderListener.spawnLocate.contains(p.getUniqueId())) {
						BorderListener.spawnLocate.add(p.getUniqueId());
						BorderListener.playerLocate.remove(p.getUniqueId());
						BorderListener.baseLocate.remove(p.getUniqueId());
						if (!BorderListener.toggled.containsKey(p.getUniqueId())) {
							msg.chat(BorderListener.coToggle(Clan.ACTION.getPrefix().replace("&6", "&#eb9534&l") + " &#3ee67c&lChunk borders on &#ffffff| &#3ee6ddClick to toggle ", prefix.replace("&6", "&#eb9534"))).deploy();
							BorderTask.run(p);
							BorderListener.toggled.put(p.getUniqueId(), true);
						}
						msg.chat("&6&oSpawn flag has been enabled.").deploy();
						msg.chat("&7You are now locating the spawn location.").deploy();
					} else {
						BorderListener.spawnLocate.remove(p.getUniqueId());
						msg.chat("&c&oSpawn flag has been disabled.").deploy();
					}
					return true;
				}
			}
			return true;
		}

		return true;
	}

	@Override
	public boolean console(CommandSender sender, String label, String[] args) {
		return false;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return SimpleTabCompletion.empty().fillArgs(args)
				.then(TabCompletionIndex.ONE, "territory")
				.then(TabCompletionIndex.TWO, "territory", TabCompletionIndex.ONE, "-f")
				.then(TabCompletionIndex.THREE, "-f", TabCompletionIndex.TWO, "base", "player", "spawn")
				.get();
	}
}
