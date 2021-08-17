package com.github.sanctum.clans.util;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.impl.CooldownArena;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.link.CycleList;
import com.github.sanctum.link.EventCycle;
import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {

	public ClansJavaPlugin plugin;

	public Placeholders(ClansJavaPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Because this is an internal class,
	 * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
	 * PlaceholderAPI is reloaded
	 *
	 * @return true to persist through reloads
	 */
	@Override
	public boolean persist() {
		return true;
	}

	/**
	 * Because this is a internal class, this check is not needed
	 * and we can simply return {@code true}
	 *
	 * @return Always true since it's an internal class.
	 */
	@Override
	public boolean canRegister() {
		return true;
	}

	/**
	 * The name of the person who created this expansion should go here.
	 * <br>For convienience do we return the author from the plugin.yml
	 *
	 * @return The name of the author as a String.
	 */
	@Override
	public @NotNull String getAuthor() {
		return plugin.getDescription().getAuthors().toString();
	}

	/**
	 * The placeholder identifier should go here.
	 * <br>This is what tells PlaceholderAPI to call our onRequest
	 * method to obtain a value if a placeholder starts with our
	 * identifier.
	 * <br>This must be unique and can not contain % or _
	 *
	 * @return The identifier in {@code %<identifier>_<value>%} as String.
	 */
	@Override
	public @NotNull String getIdentifier() {
		return "clanspro";
	}

	/**
	 * This is the version of the expansion.
	 * <br>You don't have to use numbers, since it is set as a String.
	 * <p>
	 * For convienience do we return the version from the plugin.yml
	 *
	 * @return The version as a String.
	 */
	@Override
	public @NotNull String getVersion() {
		return plugin.getDescription().getVersion();
	}

	/**
	 * This is the method called when a placeholder with our identifier
	 * is found and needs a value.
	 * <br>We specify the value identifier in this method.
	 * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
	 *
	 * @param player     A player link
	 * @param identifier A String containing the identifier/value.
	 * @return possibly-null String of the requested identifier.
	 */
	@Override
	public String onPlaceholderRequest(Player player, @NotNull String identifier) {

		if (player == null) {
			return "";
		}

		ClanAssociate associate = ClansAPI.getInstance().getAssociate(player).orElse(null);

		if (associate == null) {
			return "";
		}

		Clan c = associate.getClan();

		for (EventCycle cycle : CycleList.getRegisteredCycles()) {
			String place = cycle.onPlaceholder(player, identifier);
			if (!place.isEmpty()) {
				return place;
			}
		}

		for (int i = 0; i < 101; i++) {
			if (identifier.equals("clan_top_slot_" + i)) {
				if (i > Clan.ACTION.getMostPowerful().size()) {
					return "Empty";
				}
				Clan target = Clan.ACTION.getMostPowerful().get(i - 1);
				return target != null ? target.getName() : "Empty";
			}
			if (identifier.equals("clan_top_slot_" + i + "_power")) {
				if (i > Clan.ACTION.getMostPowerful().size()) {
					return "Empty";
				}
				Clan target = Clan.ACTION.getMostPowerful().get(i - 1);
				return target != null ? target.format(String.valueOf(target.getPower())) + "" : "Empty";
			}
			if (identifier.equals("clan_top_slot_" + i + "_color")) {
				if (i > Clan.ACTION.getMostPowerful().size()) {
					return "&r";
				}
				Clan target = Clan.ACTION.getMostPowerful().get(i - 1);
				return target != null ? target.getColor() + "" : "&r";
			}
		}

		// %someplugin_placeholder1%
		if (identifier.equals("clan_name")) {
			return c.getName();
		}

		if (identifier.equals("clan_description")) {
			return c.getDescription();
		}

		if (identifier.equals("land_status")) {
			Claim claim = Claim.from(player.getLocation());
			if (claim != null) {
				if (claim.getClan().getId().equals(c.getId())) {
					return "Owned";
				}
				if (claim.getClan().getAllies().list().contains(c)) {
					return "Allied";
				}
				if (claim.getClan().getEnemies().list().contains(c)) {
					return "Rivaled";
				}
			}
			return "Neutral";
		}

		if (identifier.equals("clan_pvp_mode")) {
			String result = "Peace";
			if (!c.isPeaceful()) {
				result = "War";
			}
			return result;
		}

		if (identifier.equals("clan_members_online")) {
			String result;
			int count = 0;
			for (String member : c.getMemberIds()) {
				if (Bukkit.getOfflinePlayer(UUID.fromString(member)).isOnline()) {
					count++;
				}
			}
			if (count == 0) {
				result = "Just you";
			} else {
				result = String.valueOf(count);
			}
			return result;
		}
		if (identifier.equals("clan_war_active")) {
			boolean result = false;
			if (ClansAPI.getData().arenaFile().exists()) {
				result = true;
			}
			return String.valueOf(result);
		}
		if (identifier.equals("clan_war_score")) {
			if (ClansAPI.getData().arenaFile().exists()) {
				return ((DefaultClan) associate.getClan()).getCurrentWar().getPoints() + "";
			} else {
				return "0";
			}
		}
		if (identifier.equals("clan_war_hours")) {
			String result = "";
			if (ClansAPI.getData().arenaFile().exists()) {
				CooldownArena arena = Clan.ACTION.ARENA;
				if (!arena.isComplete()) {
					result = String.valueOf(arena.getHoursLeft());
				}
			} else {
				result = "00";
			}
			return result;
		}
		if (identifier.equals("clan_war_minutes")) {
			String result = "";
			if (ClansAPI.getData().arenaFile().exists()) {
				CooldownArena arena = Clan.ACTION.ARENA;
				if (!arena.isComplete()) {
					result = String.valueOf(arena.getMinutesLeft());
				}
			} else {
				result = "00";
			}
			return result;
		}
		if (identifier.equals("clan_war_seconds")) {
			String result = "";
			if (ClansAPI.getData().arenaFile().exists()) {
				CooldownArena arena = Clan.ACTION.ARENA;
				if (!arena.isComplete()) {
					result = String.valueOf(arena.getSecondsLeft());
				}
			} else {
				result = "00";
			}
			return result;
		}
		if (identifier.equals("clan_balance")) {
			String result = "0";
			if (c.getBalance() == null) {
				return result;
			}
			result = c.format(String.valueOf(c.getBalance().doubleValue()));
			return result;
		}

		if (identifier.equals("clan_power")) {
			return c.format(String.valueOf(c.getPower()));
		}

		if (identifier.equals("clan_color")) {
			return c.getColor();
		}

		if (identifier.equals("member_rank")) {
			return associate.getRankTag();
		}

		if (identifier.equals("member_rank_short")) {
			return associate.getRankShort();
		}

		if (identifier.equals("member_bio")) {
			return associate.getBiography();
		}

		if (identifier.equals("raidshield_status")) {
			if (ClansAPI.getInstance().getShieldManager().isEnabled()) {
				return "&a&oProtected";
			}
			if (!ClansAPI.getInstance().getShieldManager().isEnabled()) {
				return "&c&oRaidable";
			}
		}

		return null;
	}
}
	
	

