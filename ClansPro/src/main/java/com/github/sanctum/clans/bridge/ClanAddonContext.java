package com.github.sanctum.clans.bridge;

import com.github.sanctum.clans.construct.api.ClanSubCommand;
import org.bukkit.event.Listener;

public interface ClanAddonContext {

	Listener[] getListeners();

	ClanSubCommand[] getCommands();

	String[] getDependencies();

	String[] getLoadBefore();

	String[] getLoadAfter();

	int getLevel();

	boolean isActive();

	void setActive(boolean active);

	void setLevel(int importance);

	void stage(ClanSubCommand command);

	void stage(Listener listener);

}
