package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.internal.map.MapController;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.labyrinth.placeholders.Placeholder;
import com.github.sanctum.labyrinth.placeholders.PlaceholderIdentifier;
import com.github.sanctum.labyrinth.placeholders.PlaceholderTranslation;
import com.github.sanctum.labyrinth.placeholders.PlaceholderVariable;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class LabyrinthPlaceholders implements PlaceholderTranslation {

	public ClansJavaPlugin plugin;
	private final PlaceholderIdentifier identifier = () -> "clanspro";
	private final Placeholder[] placeholders = new Placeholder[]{Placeholder.ANGLE_BRACKETS, Placeholder.CURLEY_BRACKETS, Placeholder.PERCENT};

	public LabyrinthPlaceholders(ClansJavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public @NotNull PlaceholderIdentifier getIdentifier() {
		return identifier;
	}

	@Override
	public @NotNull Placeholder[] getPlaceholders() {
		return placeholders;
	}

	@Override
	public String onTranslation(String parameter, PlaceholderVariable variable) {

		for (int i = 0; i < 101; i++) {
			if (parameter.equals("clan_top_slot_" + i)) {
				if (i > Clan.ACTION.getMostPowerful().size()) {
					return "Empty";
				}
				Clan target = Clan.ACTION.getMostPowerful().get(i - 1);
				return target != null ? target.getName() : "Empty";
			}
			if (parameter.equals("clan_top_slot_" + i + "_power")) {
				if (i > Clan.ACTION.getMostPowerful().size()) {
					return "Empty";
				}
				Clan target = Clan.ACTION.getMostPowerful().get(i - 1);
				return target != null ? Clan.ACTION.format(target.getPower()) : "Empty";
			}
			if (parameter.equals("clan_top_slot_" + i + "_color")) {
				if (i > Clan.ACTION.getMostPowerful().size()) {
					return "&r";
				}
				Clan target = Clan.ACTION.getMostPowerful().get(i - 1);
				return target != null ? target.getPalette().toString() : "&r";
			}
		}

		if (!variable.isPlayer()) {
			return "";
		}

		OfflinePlayer player = variable.getAsPlayer();

		Clan.Associate associate = ClansAPI.getInstance().getAssociate(player).orElse(null);

		if (player.isOnline()) {

			if (parameter.equals("land_chunk_map_line1")) {
				return MapController.getMapLine(player.getPlayer(), 5, 5, 0);
			}

			if (parameter.equals("land_chunk_map_line2")) {
				return MapController.getMapLine(player.getPlayer(), 5, 5, 1);
			}

			if (parameter.equals("land_chunk_map_line3")) {
				return MapController.getMapLine(player.getPlayer(), 5, 5, 2);
			}

			if (parameter.equals("land_chunk_map_line4")) {
				return MapController.getMapLine(player.getPlayer(), 5, 5, 3);
			}

			if (parameter.equals("land_chunk_map_line5")) {
				return MapController.getMapLine(player.getPlayer(), 5, 5, 4);
			}

			for (ClanAddon cycle : ClanAddonQuery.getRegisteredAddons()) {
				String place = cycle.onPlaceholder(player.getPlayer(), parameter);
				if (!place.isEmpty()) {
					return place;
				}
			}

			if (parameter.equals("land_status")) {

				if (associate == null) {
					return "";
				}

				Clan c = associate.getClan();

				Claim claim = ClansAPI.getInstance().getClaimManager().getClaim(player.getPlayer().getLocation());
				if (claim != null) {
					if (((Clan) claim.getHolder()).getId().equals(c.getId())) {
						return "Owned";
					}
					if (((Clan) claim.getHolder()).getRelation().getAlliance().has(c)) {
						return "Allied";
					}
					if (((Clan) claim.getHolder()).getRelation().getRivalry().has(c)) {
						return "Rivaled";
					}
				}
				return "Neutral";
			}
		}

		if (associate == null) {
			return "N/A";
		}

		Clan c = associate.getClan();

		// %someplugin_placeholder1%
		if (parameter.equals("clan_name")) {
			return c.getName();
		}

		if (parameter.equals("clan_nick_name")) {
			return c.getNickname() != null ? c.getNickname() : c.getName();
		}

		if (parameter.equals("clan_nick_name_colored")) {
			return c.getPalette().isGradient() ? c.getPalette().toString(c.getNickname() != null ? c.getNickname() : c.getName()) : c.getPalette() + (c.getNickname() != null ? c.getNickname() : c.getName());
		}

		if (parameter.equals("clan_name_colored")) {
			return c.getPalette().isGradient() ? c.getPalette().toGradient().context(c.getName()).translate() : c.getPalette() + c.getName();
		}

		if (parameter.equals("clan_description")) {
			return c.getDescription();
		}

		if (parameter.equals("clan_pvp_mode")) {
			String result = "Peace";
			if (!c.isPeaceful()) {
				result = "War";
			}
			return result;
		}

		if (parameter.equals("clan_members_online")) {
			String result;
			int count = 0;
			for (Clan.Associate associate1 : c.getMembers()) {
				if (associate1.getTag().isPlayer() && associate1.getTag().getPlayer().isOnline()) {
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
		if (parameter.equals("clan_war_active")) {
			War w = ClansAPI.getInstance().getArenaManager().get("PRO");
			if (w != null) {
				return String.valueOf(w.isRunning());
			}
			return "false";
		}
		if (parameter.equals("clan_war_score")) {
			War w = ClansAPI.getInstance().getArenaManager().get(associate);
			if (w != null) {
				return String.valueOf(w.getPoints(w.getTeam(associate.getClan())));
			} else {
				return "0";
			}
		}
		if (parameter.equals("clan_war_hours")) {
			War w = ClansAPI.getInstance().getArenaManager().get(associate);
			if (w != null) {
				if (!w.getTimer().isComplete()) {
					return String.valueOf(w.getTimer().getHours());
				}
			} else {
				return "00";
			}
		}
		if (parameter.equals("clan_war_minutes")) {
			War w = ClansAPI.getInstance().getArenaManager().get(associate);
			if (w != null) {
				if (!w.getTimer().isComplete()) {
					return String.valueOf(w.getTimer().getMinutes());
				}
			} else {
				return "00";
			}
		}
		if (parameter.equals("clan_war_seconds")) {
			War w = ClansAPI.getInstance().getArenaManager().get(associate);
			if (w != null) {
				if (!w.getTimer().isComplete()) {
					return String.valueOf(w.getTimer().getSeconds());
				}
			} else {
				return "00";
			}
		}
		if (parameter.equals("clan_balance")) {
			String result = "0";
			if (c.getBalance() == null) {
				return result;
			}
			result = Clan.ACTION.format(c.getBalance().doubleValue());
			return result;
		}

		if (parameter.equals("clan_power")) {
			return Clan.ACTION.format(c.getPower());
		}

		if (parameter.equals("clan_color")) {
			return c.getPalette().toString();
		}

		if (parameter.equals("member_rank")) {
			return associate.getRankFull();
		}

		if (parameter.equals("member_rank_short")) {
			return associate.getRankWordless();
		}

		if (parameter.equals("member_bio")) {
			return associate.getBiography();
		}

		if (parameter.equals("raidshield_status")) {
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
	
	

