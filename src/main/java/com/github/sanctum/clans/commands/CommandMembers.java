package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.GUI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.pagination.EasyPagination;
import com.github.sanctum.panther.util.HUID;
import org.bukkit.entity.Player;

public class CommandMembers extends ClanSubCommand {
	public CommandMembers() {
		super("members");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {

			if (associate != null) {
				EasyPagination<Clan.Associate> members = new EasyPagination<>(p, associate.getClan().getMembers());
				new FancyMessage(associate.getClan().getPalette().toString("Associates:")).send(p).deploy();
				members.setHeader((player, message) -> {
					message.then("&f&l&m---------------------------------");
				});
				members.setFormat((a, integer, message) -> {
					if (ClansAPI.getDataInstance().getMessages().getRoot().getNode("menu.enabled").toPrimitive().getBoolean()) {
						message.then(" " + a.getRankFull() + " - " + a.getClan().getPalette().toString(a.getNickname()) + " &8*(" + a.getName() + ")").hover("&aClick to view more info.").action(() -> {
							GUI.MEMBER_INFO.get(a).open(p);
						});
					} else {
						message.then(" " + a.getRankFull() + " - " + a.getClan().getPalette().toString(a.getNickname()) + " &8*(" + a.getName() + ")");
					}
				});
				members.setFooter((player, message) -> {
					message.then("&f&l&m---------------------------------");
				});
				members.limit(lib.menuSize());
				members.send(1);
			} else {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}
			return true;
		}

		if (args.length == 1) {

			if (associate != null) {
				try {
					int page = Integer.parseInt(args[0]);
					EasyPagination<Clan.Associate> members = new EasyPagination<>(p, associate.getClan().getMembers());
					new FancyMessage(associate.getClan().getPalette().toString("Associates:")).send(p).deploy();
					members.setHeader((player, message) -> {
						message.then("&f&l&m---------------------------------");
					});
					members.setFormat((a, integer, message) -> {
						if (ClansAPI.getDataInstance().getMessages().getRoot().getNode("menu.enabled").toPrimitive().getBoolean()) {
							message.then(" " + a.getRankFull() + " - " + a.getClan().getPalette().toString(a.getNickname()) + " &8*(" + a.getName() + ")").hover("&aClick to view more info.").action(() -> {
								GUI.MEMBER_INFO.get(a).open(p);
							});
						} else {
							message.then(" " + a.getRankFull() + " - " + a.getClan().getPalette().toString(a.getNickname()) + " &8*(" + a.getName() + ")");
						}
					});
					members.setFooter((player, message) -> {
						message.then("&f&l&m---------------------------------");
					});
					members.limit(lib.menuSize());
					members.send(page);
				} catch (NumberFormatException e) {
					HUID test = ClansAPI.getInstance().getClanManager().getClanID(args[0]);
					if (test != null) {
						EasyPagination<Clan.Associate> members = new EasyPagination<>(p, ClansAPI.getInstance().getClanManager().getClan(test).getMembers());
						new FancyMessage(ClansAPI.getInstance().getClanManager().getClan(test).getPalette().toString("Associates:")).send(p).deploy();
						members.setHeader((player, message) -> {
							message.then("&f&l&m---------------------------------");
						});
						members.setFormat((a, integer, message) -> {
							if (ClansAPI.getDataInstance().getMessages().getRoot().getNode("menu.enabled").toPrimitive().getBoolean()) {
								message.then(" " + a.getRankFull() + " - " + a.getClan().getPalette().toString(a.getNickname())).hover("&aClick to view more info.").action(() -> {
									GUI.MEMBER_INFO.get(a).open(p);
								});
							} else {
								message.then(" " + a.getRankFull() + " - " + a.getClan().getPalette().toString(a.getNickname()));
							}
						});
						members.setFooter((player, message) -> {
							message.then("&f&l&m---------------------------------");
						});
						members.limit(lib.menuSize());
						members.send(1);
					} else {
						lib.sendMessage(p, lib.pageUnknown() + ", " + lib.clanUnknown(args[0]));
					}
				}
			} else {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}
			return true;
		}


		return true;
	}
}
