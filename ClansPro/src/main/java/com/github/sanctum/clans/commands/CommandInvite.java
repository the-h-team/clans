package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandInvite extends ClanSubCommand implements Message.Factory {
	public CommandInvite() {
		super("invite");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.invite.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("invite")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("invite")));
				return true;
			}
			lib.sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("invite"));
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("invite")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("invite")));
				return true;
			}
			Player target = Bukkit.getPlayer(args[0]);
			if (target != null) {
				if (ClansAPI.getInstance().getAssociate(target).isPresent()) {
					lib.sendMessage(p, "&c&oThis user is already in a clan.");
					return true;
				}

				if (associate == null) {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				if (Clearance.INVITE_PLAYERS.test(associate)) {
					if (CommandBlock.blockedUsers.containsKey(target)) {
						List<UUID> users = CommandBlock.blockedUsers.get(target);
						if (users.contains(p.getUniqueId())) {
							lib.sendMessage(p, MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("blocked"), target.getName()));
							return true;
						}
					}
					ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("invite-out"), p.getName(), target.getName()));
					lib.sendMessage(target, MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("invite-in.message"), p.getName()));
					if (associate.getClan().getPassword() != null) {
						message().append(text(ClansAPI.getDataInstance().getMessageResponse("invite-in.text")))
								.append(text(ClansAPI.getDataInstance().getMessageResponse("invite-in.accept-button.text"))
										.bind(hover(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("invite-in.accept-button.hover"), p.getName())))
										.bind(command("clan join " + ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).getName() + " " + associate.getClan().getPassword())))
								.append(text(ClansAPI.getDataInstance().getMessageResponse("invite-in.deny-button.text"))
										.bind(hover(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("invite-in.deny-button.hover"), p.getName())))
										.bind(command("msg " + p.getName() + " No thank you.")))
								.send(target).deploy();
					} else {
						message().append(text(ClansAPI.getDataInstance().getMessageResponse("invite-in.text")))
								.append(text(ClansAPI.getDataInstance().getMessageResponse("invite-in.accept-button.text"))
										.bind(hover(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("invite-in.accept-button.hover"), p.getName())))
										.bind(command("clan join " + ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).getName())))
								.append(text(ClansAPI.getDataInstance().getMessageResponse("invite-in.deny-button.text"))
										.bind(hover(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("invite-in.deny-button.hover"), p.getName())))
										.bind(command("msg " + p.getName() + " No thank you.")))
								.send(target).deploy();
					}
				} else {
					lib.sendMessage(p, "&c&oYou do not have clan clearance.");
					return true;
				}
			} else {
				lib.sendMessage(p, "&c&oTarget not found.");
				return true;
			}
			return true;
		}


		return true;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, getBaseCompletion(args))
				.then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()))
				.get();
	}
}
