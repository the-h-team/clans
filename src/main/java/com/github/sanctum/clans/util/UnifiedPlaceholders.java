package com.github.sanctum.clans.util;

import com.github.sanctum.clans.model.ClanAddonRegistry;
import com.github.sanctum.clans.model.addon.map.MapController;
import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.*;
import com.github.sanctum.clans.model.Arena;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class UnifiedPlaceholders {

	static UnifiedPlaceholders instance;
	final ClansAPI api = ClansAPI.getInstance();
	final DataManager data = ClansAPI.getDataInstance();

	UnifiedPlaceholders() {
	}

	public @Nullable String translate(@NotNull String context, @Nullable OfflinePlayer player) {
		if (player != null) {
			Clan.Associate associate = api.getAssociate(player).orElse(null);

			if (associate == null) {
				return data.getConfigString("Formatting.empty-placeholder");
			}

			Clan c = associate.getClan();

			for (Clan.Addon cycle : ClanAddonRegistry.getInstance().get()) {
				String place = cycle.onPlaceholder(player, context);
				if (place != null && !place.isEmpty()) {
					return place;
				}
			}

			if (context.equals("clan_name")) {
				return c.getName();
			}

			if (context.equals("clan_nick_name")) {
				return c.getNickname() != null ? c.getNickname() : c.getName();
			}

			if (context.equals("clan_nick_name_colored")) {
				return c.getPalette().isGradient() ? c.getPalette().toString(c.getNickname() != null ? c.getNickname() : c.getName()) : c.getPalette() + (c.getNickname() != null ? c.getNickname() : c.getName());
			}

			if (context.equals("clan_name_colored")) {
				return c.getPalette().isGradient() ? c.getPalette().toGradient().context(c.getName()).translate() : c.getPalette() + c.getName();
			}

			if (context.equals("clan_description")) {
				return c.getDescription();
			}

			if (context.equals("clan_pvp_mode")) {
				String result = "Peace";
				if (!c.isPeaceful()) {
					result = "War";
				}
				return result;
			}

			if (context.equals("clan_members_online")) {
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
			if (context.equals("clan_war_active")) {
				Arena w = api.getArenaManager().get("PRO");
				if (w != null) {
					return String.valueOf(w.isRunning());
				}
				return "false";
			}
			if (context.equals("clan_war_score")) {
				Arena w = api.getArenaManager().get(associate);
				if (w != null) {
					return String.valueOf(w.getPoints(w.getTeam(associate.getClan())));
				} else {
					return "0";
				}
			}
			if (context.equals("clan_war_hours")) {
				Arena w = api.getArenaManager().get(associate);
				if (w != null) {
					if (!w.getTimer().isComplete()) {
						return String.valueOf(w.getTimer().getHours());
					}
				} else {
					return "00";
				}
			}
			if (context.equals("clan_war_minutes")) {
				Arena w = api.getArenaManager().get(associate);
				if (w != null) {
					if (!w.getTimer().isComplete()) {
						return String.valueOf(w.getTimer().getHours());
					}
				} else {
					return "00";
				}
			}
			if (context.equals("clan_war_seconds")) {
				Arena w = api.getArenaManager().get(associate);
				if (w != null) {
					if (!w.getTimer().isComplete()) {
						return String.valueOf(w.getTimer().getSeconds());
					}
				} else {
					return "00";
				}
			}
			if (context.equals("clan_balance")) {
				String result = "0";
				if (!EconomyProvision.getInstance().isValid()) {
					return result;
				}
				result = Clan.ACTION.format(BanksAPI.getInstance().getBank(c).getBalance().doubleValue());
				return result;
			}

			if (context.equals("clan_power")) {
				return Clan.ACTION.format(c.getPower());
			}

			if (context.equals("clan_color")) {
				return c.getPalette().toString();
			}

			if (context.equals("member_rank")) {
				return associate.getRankFull();
			}

			if (context.equals("member_rank_short")) {
				return associate.getRankWordless();
			}

			if (context.equals("member_bio")) {
				return associate.getBiography();
			}

			if (context.equals("raidshield_status")) {
				if (api.getShieldManager().isEnabled()) {
					return "&a&oProtected";
				}
				if (!api.getShieldManager().isEnabled()) {
					return "&c&oRaidable";
				}
			}
			if (player.isOnline()) {
				if (context.equals("land_status")) {
					Claim claim = api.getClaimManager().getClaim(player.getPlayer().getLocation());
					if (claim != null) {
						if (((Clan)claim.getHolder()).getId().equals(c.getId())) {
							return "Owned";
						}
						if (((Clan)claim.getHolder()).getRelation().getAlliance().has(c)) {
							return "Allied";
						}
						if (((Clan)claim.getHolder()).getRelation().getRivalry().has(c)) {
							return "Rivaled";
						}
					}
					return "Neutral";
				}

				if (context.equals("land_chunk_map_line1")) {
					return MapController.getMapLine(player.getPlayer(), 5, 5, 0);
				}

				if (context.equals("land_chunk_map_line2")) {
					return MapController.getMapLine(player.getPlayer(), 5, 5, 1);
				}

				if (context.equals("land_chunk_map_line3")) {
					return MapController.getMapLine(player.getPlayer(), 5, 5, 2);
				}

				if (context.equals("land_chunk_map_line4")) {
					return MapController.getMapLine(player.getPlayer(), 5, 5, 3);
				}

				if (context.equals("land_chunk_map_line5")) {
					return MapController.getMapLine(player.getPlayer(), 5, 5, 4);
				}
			}
		}
		for (int i = 0; i < 101; i++) {
			if (context.equals("clan_top_slot_" + i)) {
				if (i > Clan.ACTION.getMostPowerful().size()) {
					return "Empty";
				}
				Clan target = Clan.ACTION.getMostPowerful().get(i - 1);
				return target != null ? target.getName() : "Empty";
			}
			if (context.equals("clan_top_slot_" + i + "_power")) {
				if (i > Clan.ACTION.getMostPowerful().size()) {
					return "Empty";
				}
				Clan target = Clan.ACTION.getMostPowerful().get(i - 1);
				return target != null ? Clan.ACTION.format(target.getPower()) + "" : "Empty";
			}
			if (context.equals("clan_top_slot_" + i + "_color")) {
				if (i > Clan.ACTION.getMostPowerful().size()) {
					return "&r";
				}
				Clan target = Clan.ACTION.getMostPowerful().get(i - 1);
				return target != null ? target.getPalette().toString() : "&r";
			}
		}
		return null;
	}

	public static UnifiedPlaceholders getInstance() {
		return instance != null ? instance : (instance = new UnifiedPlaceholders());
	}

}
