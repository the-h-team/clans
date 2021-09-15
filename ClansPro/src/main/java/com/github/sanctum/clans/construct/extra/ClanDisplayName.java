package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criterias;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ClanDisplayName {

	public static Team getTeam(Player player) {
		Team result = null;
		Scoreboard scoreboard = player.getScoreboard();
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(player).orElse(null);
		if (associate != null && associate.isValid()) {
			result = scoreboard.getTeam(associate.getClan().getId().toString());
		}
		return result;
	}

	public static void set(Clan.Associate associate, String prefix) {
		Scoreboard scoreboard = associate.getPlayer().getPlayer().getScoreboard();
		Team team = getTeam(associate.getPlayer().getPlayer());
		if (team == null) {
			scoreboard.registerNewTeam(associate.getClan().getId().toString());
			set(associate, prefix);
		} else {
			Team t = getTeam(associate.getPlayer().getPlayer());
			t.setPrefix(Clan.ACTION.color(prefix));
			t.setDisplayName(associate.getClan().getName());
			t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
			t.addEntry(associate.getName());
			if (scoreboard.getObjective("showhealth") == null) {
				if (ClansAPI.getData().isTrue("Clans.nametag-prefix.show-health")) {
					Objective h = scoreboard.registerNewObjective("showhealth", Criterias.HEALTH, ChatColor.DARK_RED + "❤");
					h.setDisplaySlot(DisplaySlot.BELOW_NAME);
				}
			}
		}
	}

	public static void set(Player player, String prefix) {
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(player).orElse(null);
		Scoreboard scoreboard = player.getScoreboard();

		if (associate == null) {
			return;
		}

		if (getTeam(player) == null) {
			scoreboard.registerNewTeam(associate.getClan().getId().toString());
			set(player, prefix);
		} else {
			Team team = getTeam(player);
			team.setPrefix(Clan.ACTION.color(prefix));
			team.setDisplayName(associate.getClan().getName());
			team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
			team.addEntry(player.getName());
			if (scoreboard.getObjective("showhealth") == null) {
				if (ClansAPI.getData().isTrue("Clans.nametag-prefix.show-health")) {
					Objective h = scoreboard.registerNewObjective("showhealth", Criterias.HEALTH, ChatColor.DARK_RED + "❤");
					h.setDisplaySlot(DisplaySlot.BELOW_NAME);
				}
			}
		}
	}

	public static void update(Player player, String prefix) {
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(player).orElse(null);

		if (associate == null) {
			return;
		}
		if (getTeam(player) != null) {
			Team team = getTeam(player);
			team.setPrefix(Clan.ACTION.color(prefix));
			team.setDisplayName(associate.getClan().getName());
			team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
		}

	}

	public static void remove(Clan.Associate associate) {

		if (!associate.isValid()) return;

		Scoreboard scoreboard = associate.getPlayer().getPlayer().getScoreboard();
		try {
			Team team = getTeam(associate.getPlayer().getPlayer());
			if (team != null) {
				if (!team.getEntries().isEmpty()) {
					if (team.getEntries().contains(associate.getName())) {
						team.removeEntry(associate.getName());
					}
				} else {
					team.unregister();
				}
			} else {
				Team t;
				if (scoreboard.getTeam(associate.getName()) != null) {
					t = scoreboard.getTeam(associate.getName());
				} else {
					t = scoreboard.registerNewTeam(associate.getName());
				}
				t.unregister();
			}
		} catch (IllegalArgumentException e) {
			Team team;
			if (scoreboard.getTeam(associate.getName()) != null) {
				team = scoreboard.getTeam(associate.getName());
			} else {
				team = scoreboard.registerNewTeam(associate.getName());
			}
			team.unregister();
		}
	}

	public static void remove(Player player) {
		Scoreboard scoreboard = player.getScoreboard();
		try {
			if (getTeam(player) != null) {
				Clan.Associate associate = ClansAPI.getInstance().getAssociate(player).orElse(null);

				if (associate == null) {
					return;
				}
				Team team = getTeam(player);
				if (!team.getEntries().isEmpty()) {
					if (team.getEntries().contains(player.getName())) {
						team.removeEntry(player.getName());
					}
				} else {
					team.unregister();
				}
			} else {
				Team team;
				if (scoreboard.getTeam(player.getName()) != null) {
					team = scoreboard.getTeam(player.getName());
				} else {
					team = scoreboard.registerNewTeam(player.getName());
				}
				team.unregister();
			}
		} catch (IllegalArgumentException e) {
			Team team;
			if (scoreboard.getTeam(player.getName()) != null) {
				team = scoreboard.getTeam(player.getName());
			} else {
				team = scoreboard.registerNewTeam(player.getName());
			}
			team.unregister();
		}
	}

}
