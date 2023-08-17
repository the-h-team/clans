package com.github.sanctum.clans.construct.util;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class AboveHeadDisplayName {

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
		Scoreboard scoreboard = associate.getTag().getPlayer().getPlayer().getScoreboard();
		Team team = getTeam(associate.getTag().getPlayer().getPlayer());
		if (team == null) {
			scoreboard.registerNewTeam(associate.getClan().getId().toString());
			try {
				Team t = getTeam(associate.getTag().getPlayer().getPlayer());
				t.setPrefix(Clan.ACTION.color(prefix));
				t.setDisplayName(associate.getClan().getName());
				t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
				t.addEntry(associate.getName());
			} catch (IllegalArgumentException tooLong) {
				ClansAPI.getInstance().getPlugin().getLogger().warning("- Unable to set above head display name for clan " + associate.getClan().getName());
			}
		} else {
			try {
				Team t = getTeam(associate.getTag().getPlayer().getPlayer());
				t.setPrefix(Clan.ACTION.color(prefix));
				t.setDisplayName(associate.getClan().getName());
				t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
				t.addEntry(associate.getName());
			} catch (IllegalArgumentException tooLong) {
				ClansAPI.getInstance().getPlugin().getLogger().warning("- Unable to set above head display name for clan " + associate.getClan().getName());
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
			try {
				Team team = getTeam(player);
				team.setPrefix(Clan.ACTION.color(prefix));
				team.setDisplayName(associate.getClan().getName());
				team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
				team.addEntry(player.getName());
			} catch (IllegalArgumentException tooLong) {
				ClansAPI.getInstance().getPlugin().getLogger().warning("- Unable to set above head display name for clan " + associate.getClan().getName());
			}
		}
	}

	public static void update(Player player, String prefix) {
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(player).orElse(null);

		if (associate == null) {
			return;
		}
		if (getTeam(player) != null) {
			try {
			Team team = getTeam(player);
			team.setPrefix(Clan.ACTION.color(prefix));
			team.setDisplayName(associate.getClan().getName());
			team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
			} catch (IllegalArgumentException tooLong) {
				ClansAPI.getInstance().getPlugin().getLogger().warning("- Unable to set above head display name for clan " + associate.getClan().getName());
			}
		}

	}

	public static void remove(Clan.Associate associate) {

		if (!associate.isValid()) return;

		Scoreboard scoreboard = associate.getTag().getPlayer().getPlayer().getScoreboard();
		try {
			Team team = getTeam(associate.getTag().getPlayer().getPlayer());
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
