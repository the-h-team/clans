package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.Book;
import com.github.sanctum.panther.file.Primitive;
import java.util.List;
import org.bukkit.entity.Player;

public class CommandAsk extends ClanSubCommand {
	public CommandAsk() {
		super("ask");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {

		if (args.length == 0) {
			Book book = new Book(1, false).setTitle("&7[&3&lClans&7] &6&lInfo").setAuthor("Hempfest");
			book.append("&lYou may have a few questions so we'll answer them here.")
					.append("")
					.append("&6&lQ.) &0&nHow do i make a clan?");
			if (Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("create")).deploy()) {
				book.append("&2&lA.] &8" + "To make a clan simply type '/clan create <nameHere>', if you have permission this message will be gray.");
			} else {
				book.append("&2&lA.] &cTo make a clan simply type '/clan create <nameHere>', if you have permission this message will be gray.");
			}
			book.append("&6&lQ.) &0&nHow do i get power?")
					.append("&2&lA.] &8You can gain power by increasing the overall size of your clan (more members), having more money in the clan bank (optional through server), owning more clan claims & killing enemy clan associates as well a clans reservoir.")
					.append("&6&lQ.) &0&nHow do i raid?");
			if (Claim.ACTION.isEnabled()) {
				book.append("&2&lA.] &8Raiding can be achieved by simply overpowering a target clan's land, un-claiming an individual chunk allows you to access the containers within as well as build/break.");
			} else {
				book.append("&2&lA.] &cClan land claiming is disabled on this server! Therefore raiding becomes inherently impossible.");
			}
			book.append(" ")
					.append("&6&lQ.) &0&nHow do i print my logo?")
					.append("&2&lA.] &8You need to have a logo workspace setup, to do that simply type '/clan logo redraw'. Once done use '/clan logo print' to give yourself a globally usable logo print.")
					.append(" ")
					.append("&6&lQ.) &0&nHow do i use a logo print?")
					.append("&2&lA.] &8When holding a valid clans logo print in your hand, use command '/clan logo upload' to apply it as your clans logo, from there you can choose to share it globally for others to print with '/clan logo share'.")
					.append(" ")
					.append("&6&lQ.) &0&nHow do clan wars work?")
					.append("&2&lA.] &8Clan wars are essentially Team Death Match, but the losing team loses power while visa versa for the winning team.");

			Primitive primitive = ClansAPI.getDataInstance().getConfig().read(c -> c.getNode("Formatting.help-book").toPrimitive());
			if (primitive.isStringList()) {
				for (String s : primitive.getStringList()) {
					book.append(s);
				}
			}
			book.give(p);
			return true;
		}

		return true;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return null;
	}
}
