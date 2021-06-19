package com.github.sanctum.clans.util;

import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.misc.ClanPrefix;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TextLib;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StringLibrary {

	public void sendMessage(Player p, String message) {
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			message = PlaceholderAPI.setPlaceholders(p, message);
		}
		p.sendMessage(color(getPrefix() + " " + message));
	}

	protected String[] color(String... text) {
		List<String> convert = new ArrayList<>();
		for (String t : text) {
			convert.add(StringUtils.use(t).translate());
		}
		return convert.toArray(new String[0]);
	}

	public String color(String text) {
		return StringUtils.use(text).translate();
	}

	public void sendComponent(CommandSender s, TextComponent text) {
		s.spigot().sendMessage(text);
	}

	public String getPrefix() {
		ClansAPI API = ClansAPI.getInstance();
		ClanPrefix prefix = API.getPrefix();
		return prefix.getPrefix() + prefix.getText() + prefix.getSuffix();
	}

	public String alreadyInClan() {
		return ClansAPI.getData().getMessage("already-occupied");
	}

	public String notInClan() {
		return ClansAPI.getData().getMessage("no-clan");
	}

	public String noClearance() {
		return ClansAPI.getData().getMessage("no-clearance");
	}

	public String clanUnknown(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("clan-unknown"), name);
	}

	public String passwordInvalid() {
		return ClansAPI.getData().getMessage("password-invalid");
	}

	public String playerUnknown(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("player-unknown"), name);
	}

	public String commandCreate() {
		return ClansAPI.getData().getMessage("create");
	}

	public String commandPassword() {
		return ClansAPI.getData().getMessage("password");
	}

	public String commandJoin() {
		return ClansAPI.getData().getMessage("join");
	}

	public String commandKick() {
		return ClansAPI.getData().getMessage("kick");
	}

	public String commandTag() {
		return ClansAPI.getData().getMessage("tag");
	}

	public String commandPassowner() {
		return ClansAPI.getData().getMessage("passowner");
	}

	public String commandNick() {
		return ClansAPI.getData().getMessage("nick");
	}

	public String commandChat(String channel) {
		return MessageFormat.format(ClansAPI.getData().getMessage("chat"), channel);
	}

	public String commandPromote() {
		return ClansAPI.getData().getMessage("promote");
	}

	public String commandDemote() {
		return ClansAPI.getData().getMessage("demote");
	}

	public String commandAlly() {
		return ClansAPI.getData().getMessage("ally");
	}

	public String commandEnemy() {
		return ClansAPI.getData().getMessage("enemy");
	}

	public String commandColor() {
		return ClansAPI.getData().getMessage("color");
	}

	public String commandMessage() {
		return ClansAPI.getData().getMessage("message");
	}

	public String commandBase() {
		return ClansAPI.getData().getMessage("base");
	}

	public String commandMode() {
		return ClansAPI.getData().getMessage("mode");
	}

	public String commandSetbase() {
		return ClansAPI.getData().getMessage("base-changed");
	}

	public String commandUnknown(String label) {
		return MessageFormat.format(ClansAPI.getData().getMessage("command-unknown"), label);
	}

	public String nameInvalid(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("name-invalid"), name);
	}

	public String nameTooLong(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("too-long"), name);
	}

	public String alreadyMade(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("already-made"), name);
	}

	public String alreadyEnemies(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("already-enemies"), name);
	}

	public String alreadyAllies(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("already-allies"), name);
	}

	public String alreadyNeutral(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("already-neutral"), name);
	}

	public String neutral(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("neutral"), name);
	}

	public String ally(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("allies"), name);
	}

	public String waiting(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("already-requested"), name);
	}

	public String enemy(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("enemies"), name);
	}

	public String noRemoval(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("no-removal"), name);
	}

	public String breach(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("claim-breach"), name);
	}

	public String higherpower(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("claim-higherpower"), name);
	}

	public String alreadyMax(String instert1, String insert2) {
		return MessageFormat.format(ClansAPI.getData().getMessage("already-max"), instert1, insert2);
	}

	public String claimed(int x, int z, String world) {
		return MessageFormat.format(ClansAPI.getData().getMessage("claim"), x, z, world);
	}

	public String unclaimed(int x, int z, String world) {
		return MessageFormat.format(ClansAPI.getData().getMessage("un-claim"), x, z, world);
	}

	public String overpowered(int x, int z, String world) {
		return MessageFormat.format(ClansAPI.getData().getMessage("claim-overpowered"), x, z, world);
	}

	public String alreadyWild() {
		return ClansAPI.getData().getMessage("already-wild");
	}

	public String noClaims() {
		return ClansAPI.getData().getMessage("no-claims");
	}

	public String unclaimedAll(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("un-claim-all"), name);
	}

	public String alreadyMaxClaims() {
		return ClansAPI.getData().getMessage("already-max-claims");
	}

	public String shieldDeny() {
		return ClansAPI.getData().getMessage("shield-deny");
	}

	public String tooWeak() {
		return ClansAPI.getData().getMessage("too-weak");
	}

	public String selfDenial() {
		return ClansAPI.getData().getMessage("self-denial");
	}

	public String peacefulDeny() {
		return ClansAPI.getData().getMessage("peaceful-deny");
	}

	public String defaultMode() {
		return ClansAPI.getData().getMain().getConfig().getString("Clans.mode-change.default");
	}

	public String peacefulDenyOther(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("peaceful-deny-other"), name);
	}

	public String friendlyFire() {
		return ClansAPI.getData().getMessage("friendly-fire");
	}

	public String friendlyFireOn(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("friendly-fire-on"), name);
	}

	public String friendlyFireOff(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("friendly-fire-off"), name);
	}

	public String peaceful() {
		return ClansAPI.getData().getMessage("peace-mode");
	}

	public String modeAnnounce(String mode, String clanName) {
		String result = null;
		switch (mode.toLowerCase()) {
			case "war":
				result = MessageFormat.format(ClansAPI.getData().getMessage("war"), clanName);
				break;

			case "peace":
				result = MessageFormat.format(ClansAPI.getData().getMessage("peace"), clanName);
				break;
		}
		return result;
	}

	public String war() {
		return ClansAPI.getData().getMessage("war-mode");
	}

	public String claimHint() {
		return ClansAPI.getData().getMessage("claim-hint");
	}

	public String alreadyPeaceful() {
		return ClansAPI.getData().getMessage("already-peace");
	}

	public String alreadyWar() {
		return ClansAPI.getData().getMessage("already-war");
	}

	public String alreadyLastPage() {
		return ClansAPI.getData().getMessage("already-last-page");
	}

	public String alreadyFirstPage() {
		return ClansAPI.getData().getMessage("already-first-page");
	}

	public String allianceRequested() {
		return ClansAPI.getData().getMessage("alliance-requested-in");
	}

	public String allianceRequestedOut(String insert1, String insert2) {
		return MessageFormat.format(ClansAPI.getData().getMessage("alliance-requested-out"), insert1, insert2);
	}

	public String allianceDenial() {
		return ClansAPI.getData().getMessage("alliance-denial");
	}

	public String menuBorder() {
		return ClansAPI.getData().getPath("Border");
	}

	public int menuSize() {
		return Integer.parseInt(ClansAPI.getData().getPath("Lines"));
	}

	public String menuTitle() {
		return ClansAPI.getData().getPath("Title");
	}

	public String pageUnknown() {
		return ClansAPI.getData().getMessage("page-unknown");
	}

	public String noPermission(String permission) {
		return MessageFormat.format(ClansAPI.getData().getMessage("no-permission"), permission);
	}

	public String alreadyOwnClaim() {
		return ClansAPI.getData().getMessage("already-owned");
	}

	public String notEnemies(String name) {
		return MessageFormat.format(ClansAPI.getData().getMessage("not-enemies"), name);
	}

	public String notEnough(double needed) {
		return MessageFormat.format(ClansAPI.getData().getMessage("not-enough"), needed);
	}

	public String notClaimOwner(String actualOwner) {
		return MessageFormat.format(ClansAPI.getData().getMessage("not-owner"), actualOwner);
	}

	public String wrongPassword() {
		return ClansAPI.getData().getMessage("password-wrong");
	}

	public String getRankStyle() {
		FileManager main = ClansAPI.getData().getMain();
		String type = main.getConfig().getString("Formatting.Chat.rank-style");
		String result;
		if (type.equalsIgnoreCase("WORDLESS")) {
			result = "WORDLESS";
		} else {
			result = "FULL";
		}
		return result;
	}

	public String getWordlessStyle(String rank) {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getString("Formatting.Chat.Styles.Wordless." + rank);
	}

	public String getFullStyle(String rank) {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getString("Formatting.Chat.Styles.Full." + rank);
	}

	public String getChatFormat() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getString("Formatting.Chat.Channel.global");
	}

	public void paginatedClanList(Player p, List<String> listToPaginate, String command, int page, int contentLinesPerPage) {
		int totalPageCount = 1;
		if ((listToPaginate.size() % contentLinesPerPage) == 0) {
			if (listToPaginate.size() > 0) {
				totalPageCount = listToPaginate.size() / contentLinesPerPage;
			}
		} else {
			totalPageCount = (listToPaginate.size() / contentLinesPerPage) + 1;
		}

		if (page <= totalPageCount) {

			if (listToPaginate.isEmpty()) {
				sendMessage(p, color("&fThe list is empty!"));
			} else {
				int i = 0, k = 0;
				page--;
				p.sendMessage(color("&7&o&m============================"));
				for (String entry : listToPaginate) {
					k++;
					if ((((page * contentLinesPerPage) + i + 1) == k) && (k != ((page * contentLinesPerPage) + contentLinesPerPage + 1))) {
						i++;
						String c = "";
						ClanAction clanAction = DefaultClan.action;
						if (clanAction.getClanID(p.getUniqueId()) != null) {
							c = clanAction.clanRelationColor(clanAction.getClanID(p.getUniqueId()), clanAction.getClanID(entry)) + entry;
						} else {
							c = "&f" + entry;
						}
						p.sendMessage(color(c));
					}
				}
				int point;
				point = page + 1;
				if (page >= 1) {
					int last;
					last = point - 1;
					point = point + 1;
					p.sendMessage(color("&7&o&m============================"));
					if (page < (totalPageCount - 1)) {
						sendComponent(p, TextLib.getInstance().textRunnable("&7Navigate &b&o&m--&b> &7[", "&c&oBACK&7]", "&7 : [", "&b&oNEXT&7]", "&b&oClick to go &d&oback a page", "&b&oClick to goto the &5&onext page", command + " " + last, command + " " + point));
					}
					if (page == (totalPageCount - 1)) {
						sendComponent(p, TextLib.getInstance().textRunnable("&7Navigate &b&o&m--&b> &7[", "&c&oBACK", "&7]", "&b&oClick to go &d&oback a page", command + " " + last));
					}
				}
				if (page == 0) {
					point = page + 1 + 1;
					p.sendMessage(color("&7&o&m============================"));
					sendComponent(p, TextLib.getInstance().textRunnable("&7Navigate &b&o&m--&b> &7[", "&b&oNEXT", "&7]", "&b&oClick to goto the &5&onext page", command + " " + point));
				}
			}
		} else {
			sendMessage(p, color("&eThere are only &f" + totalPageCount + " &epages!"));
		}
	}

	public void paginatedMemberList(Player p, List<String> listToPaginate, int page) {
		int totalPageCount = 1;
		if ((listToPaginate.size() % 6) == 0) {
			if (listToPaginate.size() > 0) {
				totalPageCount = listToPaginate.size() / 6;
			}
		} else {
			totalPageCount = (listToPaginate.size() / 6) + 1;
		}

		if (page <= totalPageCount) {

			if (listToPaginate.isEmpty()) {
				sendMessage(p, color("&fThe list is empty!"));
			} else {
				int i = 0, k = 0;
				page--;
				for (String entry : listToPaginate) {
					k++;
					if ((((page * 6) + i + 1) == k) && (k != ((page * 6) + 6 + 1))) {
						i++;
						UUID id = UUID.fromString(entry);
						ClanAssociate associate = ClansAPI.getInstance().getAssociate(id).orElse(null);
						if (associate != null) {
							sendComponent(p, TextLib.getInstance().textRunnable("&f- ", "&b&l" + Bukkit.getOfflinePlayer(UUID.fromString(entry)).getName(), "", "&rRank: " + '"' + "&b" + associate.getRankTag() + "&r" + '"' + "\nK/D: &b&o" + associate.getKD(), "c i " + associate.getPlayer().getName()));
						}
					}
				}
				int point;
				point = page + 1;
				if (page >= 1) {
					int last;
					last = point - 1;
					point = point + 1;
					sendComponent(p, TextLib.getInstance().textRunnable("&7Navigate &b&o&m--&b> &7[", "&c&oBACK&7]", "&7 : [", "&b&oNEXT&7]", "&b&oClick to go &d&oback a page", "&b&oClick to goto the &5&onext page", "c members" + " " + last, "c members" + " " + point));
				}
				if (listToPaginate.size() > 6 && page == 0) {
					point = page + 1 + 1;
					sendComponent(p, TextLib.getInstance().textRunnable("&7Navigate &b&o&m--&b> &7[", "&b&oNEXT", "&7]", "&b&oClick to goto the &5&onext page", "c members" + " " + point));
				}
			}
		} else {
			sendMessage(p, color("&eThere are only &f" + totalPageCount + " &epages!"));
		}
	}

	public String format(String string, String target, String replacement) {
		int targetLength = target.length();
		if (targetLength == 0) {
			return string;
		}
		int idx2 = string.indexOf(target);
		if (idx2 < 0) {
			return string;
		}
		StringBuilder buffer = new StringBuilder(targetLength > replacement.length() ? string.length() : string.length() * 2);
		int idx1 = 0;
		do {
			buffer.append(string, idx1, idx2);
			buffer.append(replacement);
			idx1 = idx2 + targetLength;
			idx2 = string.indexOf(target, idx1);
		} while (idx2 > 0);
		buffer.append(string, idx1, string.length());
		return buffer.toString();
	}

	public String format(String string, String target1, String replacement1, String target2, String replacement2) {
		int targetLength = target1.length();
		if (targetLength == 0) {
			return string;
		}
		int idx2 = string.indexOf(target1);
		if (idx2 < 0) {
			return string;
		}
		StringBuilder buffer = new StringBuilder(targetLength > replacement1.length() ? string.length() : string.length() * 2);
		int idx1 = 0;
		do {
			buffer.append(string, idx1, idx2);
			buffer.append(replacement1);
			idx1 = idx2 + targetLength;
			idx2 = string.indexOf(target1, idx1);
		} while (idx2 > 0);
		buffer.append(string, idx1, string.length());

		int targetLength2 = target2.length();
		if (targetLength2 == 0) {
			return buffer.toString();
		}
		int idx22 = buffer.toString().indexOf(target2);
		if (idx22 < 0) {
			return buffer.toString();
		}
		StringBuilder buffer2 = new StringBuilder(targetLength2 > replacement2.length() ? buffer.toString().length() : buffer.toString().length() * 2);
		int idx12 = 0;
		do {
			buffer2.append(buffer.toString(), idx12, idx22);
			buffer2.append(replacement2);
			idx12 = idx22 + targetLength2;
			idx22 = buffer.toString().indexOf(target2, idx12);
		} while (idx22 > 0);
		buffer2.append(buffer.toString(), idx12, buffer.toString().length());

		return buffer2.toString();
	}

	public void chunkBorderHint(Player p) {
		Random r = new Random();
		int send = r.nextInt(3);
		if (send == 2) {
			sendMessage(p, claimHint());
		}
	}


}
