package com.github.sanctum.clans.bridge;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ClanAddonLogger {

	private final Logger LOG = Logger.getLogger("Minecraft");
	private final String cycle;

	protected ClanAddonLogger(String cycle) {
		this.cycle = "ClansPro:" + cycle;
	}

	public void log(Level level, String info) {
		this.LOG.log(level, "[" + cycle + "]: " + info);
	}

	public void info(String info) {
		log(Level.INFO, info);
	}

	public void warn(String info) {
		log(Level.WARNING, info);
	}

	public void error(String info) {
		log(Level.SEVERE, info);
	}

}
