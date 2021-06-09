package com.github.sanctum.clans.gui;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.api.Clan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ClanEditOperation {

	private final Clan target;

	private String context;

	private final Option option;

	private final Player executor;

	protected ClanEditOperation(Clan target, Player executor) {
		this.target = target;
		this.executor = executor;
		this.option = UI.getClanEditOption(executor.getUniqueId());
	}

	public void setContext(String context) {
		this.context = context;
	}

	public void execute() {
		switch (option) {
			case PASSWORD:
				getTarget().setPassword(context);
				DefaultClan.action.sendMessage(executor, "&aClan " + getTarget().getName() + " password changed to &f: " + context);
				break;
			case TAG:
				DefaultClan.action.sendMessage(executor, "&aClan " + getTarget().getName() + " name changed to &f: " + context);
				getTarget().setName(context);
				break;
			case COLOR:
				getTarget().setColor(context);
				DefaultClan.action.sendMessage(executor, "&aClan " + getTarget().getName() + " color changed to &r: " + context + "Example");
				break;
			case DESCRIPTION:
				getTarget().setDescription(context);
				DefaultClan.action.sendMessage(executor, "&aClan " + getTarget().getName() + " description changed to &f: " + context);
				break;
			case CLAIMS_GIVE:
				Bukkit.dispatchCommand(executor, "cla give " + getTarget().getName() + " claims " + context);
				break;
			case MONEY_GIVE:
				Bukkit.dispatchCommand(executor, "cla give " + getTarget().getName() + " money " + context);
				break;
			case POWER_GIVE:
				Bukkit.dispatchCommand(executor, "cla give " + getTarget().getName() + " power " + context);
				break;
			case CLAIMS_TAKE:
				Bukkit.dispatchCommand(executor, "cla take " + getTarget().getName() + " claims " + context);
				break;
			case MONEY_TAKE:
				Bukkit.dispatchCommand(executor, "cla take " + getTarget().getName() + " money " + context);
				break;
			case POWER_TAKE:
				Bukkit.dispatchCommand(executor, "cla take " + getTarget().getName() + " power " + context);
				break;
			case UPDATE_BASE:
				getTarget().setBase(executor.getLocation());
				DefaultClan.action.sendMessage(executor, "&aClan " + getTarget().getName() + " base location updated to current location");
				break;
			case CLOSE:
				Bukkit.dispatchCommand(executor, "cla close " + getTarget().getName());
				break;
		}
	}

	public Clan getTarget() {
		return target;
	}

	public enum Option {
		PASSWORD, TAG, COLOR, DESCRIPTION, CLAIMS_GIVE, MONEY_GIVE, POWER_GIVE, CLAIMS_TAKE, MONEY_TAKE, POWER_TAKE, UPDATE_BASE, CLOSE
	}

}
