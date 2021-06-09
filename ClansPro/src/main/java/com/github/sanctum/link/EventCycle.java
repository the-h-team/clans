package com.github.sanctum.link;

import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.task.Schedule;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

public abstract class EventCycle {

	private boolean ACTIVE;
	private final Collection<Listener> LISTENERS;
	private final Collection<ClanSubCommand> COMMANDS;

	public EventCycle() {
		this.LISTENERS = new HashSet<>();
		this.COMMANDS = new HashSet<>();
		this.ACTIVE = true;
	}

	public final boolean isActive() {
		return this.ACTIVE;
	}

	public abstract boolean persist();

	public HUID getId() {
		return HUID.randomID();
	}

	public abstract String getName();

	public abstract String getDescription();

	public abstract String getVersion();

	public abstract String[] getAuthors();

	public String onPlaceholder(Player player, String param) {
		return "";
	}

	public Collection<Listener> getAdditions() {
		return this.LISTENERS;
	}

	public Collection<ClanSubCommand> getCommands() {
		return this.COMMANDS;
	}

	public EventCycle getInstance() {
		return this;
	}

	public abstract void onLoad();

	public abstract void onEnable();

	public abstract void onDisable();

	public final Plugin getPlugin() {
		return ClansPro.getInstance();
	}

	public final FileManager getFile(String name, String... directory) {
		String dir = null;
		if (directory.length > 0) {
			dir = directory[0];
		}
		return getApi().getFileList().find(name, "Addons/" + dir + "/");
	}

	public final Message getMessenger() {
		return Message.loggedFor(getPlugin());
	}

	protected final ClansAPI getApi() {
		return ClansAPI.getInstance();
	}

	protected final Logger getLogger() {
		return getPlugin().getLogger();
	}

	protected final void register(Listener listener) {
		if (!this.LISTENERS.contains(listener)) {
			this.LISTENERS.add(listener);
		}
	}

	protected final void unregister(Listener listener) {
		this.LISTENERS.remove(listener);
	}

	protected final void register(ClanSubCommand command) {
		if (!this.COMMANDS.contains(command)) {
			this.COMMANDS.add(command);
		}
	}

	protected final void unregister(ClanSubCommand command) {
		this.COMMANDS.remove(command);
	}

	public final void register() {
		CycleList.getRegisteredCycles().add(this);
	}

	protected final void setActive(boolean active) {
		this.ACTIVE = active;
	}

	public final void remove() {
		ClansPro.getInstance().getLogger().info("- Scheduling addon " + '"' + getName() + '"' + " for removal.");
		for (RegisteredListener l : HandlerList.getRegisteredListeners(ClansPro.getInstance())) {
			if (getAdditions().contains(l.getListener())) {
				HandlerList.unregisterAll(l.getListener());
			}
		}
		onDisable();
		Schedule.sync(() -> CycleList.getRegisteredCycles().removeIf(c -> c.getName().equals(getName()))).wait(1);
	}

}
