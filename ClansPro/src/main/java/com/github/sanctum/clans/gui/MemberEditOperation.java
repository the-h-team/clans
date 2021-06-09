package com.github.sanctum.clans.gui;

import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.RankPriority;
import com.github.sanctum.labyrinth.task.Schedule;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MemberEditOperation {

	private final Clan target;

	private String context;

	private final Option option;

	private final Player executor;

	private final UUID member;

	protected MemberEditOperation(Clan target, Player executor, UUID member) {
		this.target = target;
		this.member = member;
		this.executor = executor;
		this.option = UI.getMemberEditOption(executor.getUniqueId());
	}

	public void setContext(String context) {
		this.context = context;
	}

	public void execute() {
		ClanAssociate associate = ClansAPI.getInstance().getAssociate(member).orElse(null);
		switch (option) {
			case BIO:
				if (associate != null) {
					associate.changeBio(context);
					DefaultClan.action.sendMessage(executor, "&a&oPlayer " + Bukkit.getOfflinePlayer(member).getName() + " bio changed to:&r " + context);
				}
				break;
			case NICKNAME:
				if (associate != null) {
					associate.changeNickname(context);
					DefaultClan.action.sendMessage(executor, "&a&oPlayer " + Bukkit.getOfflinePlayer(member).getName() + " nickname changed to:&r " + context);
				}
				break;
			case PROMOTE:
				RankPriority before1 = DefaultClan.action.getRankPriority(DefaultClan.action.getRank(member));
				if (before1.toInt() == 2) {
					DefaultClan.action.sendMessage(executor, "&a&oPlayer " + Bukkit.getOfflinePlayer(member).getName() + " is already the highest rank:&r " + DefaultClan.action.getRankTag(DefaultClan.action.getRank(member)));
					break;
				}
				DefaultClan.action.promotePlayer(member);
				DefaultClan.action.sendMessage(executor, "&a&oPlayer " + Bukkit.getOfflinePlayer(member).getName() + " promoted to:&r " + DefaultClan.action.getRankTag(DefaultClan.action.getRank(member)));
				break;
			case KICK:
				Bukkit.dispatchCommand(executor, "cla kick " + Bukkit.getOfflinePlayer(member).getName());
				break;
			case DEMOTE:
				RankPriority before = DefaultClan.action.getRankPriority(DefaultClan.action.getRank(member));
				if (before.toInt() == 0) {
					DefaultClan.action.sendMessage(executor, "&a&oPlayer " + Bukkit.getOfflinePlayer(member).getName() + " is already the lowest rank:&r " + DefaultClan.action.getRankTag(DefaultClan.action.getRank(member)));
					break;
				}
				DefaultClan.action.demotePlayer(member);
				DefaultClan.action.sendMessage(executor, "&a&oPlayer " + Bukkit.getOfflinePlayer(member).getName() + " demoted to:&r " + DefaultClan.action.getRankTag(DefaultClan.action.getRank(member)));
				break;
			case SWITCH_CLANS:
				Bukkit.dispatchCommand(executor, "cla kick " + Bukkit.getOfflinePlayer(member).getName());

				Schedule.sync(() -> Bukkit.dispatchCommand(executor, "cla put " + Bukkit.getOfflinePlayer(member).getName() + " " + context)).wait(1);
				break;
		}
	}

	public Clan getTarget() {
		return target;
	}

	public enum Option {
		BIO, NICKNAME, PROMOTE, KICK, DEMOTE, SWITCH_CLANS
	}

}
