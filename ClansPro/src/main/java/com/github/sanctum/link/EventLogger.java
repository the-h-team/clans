package com.github.sanctum.link;

import java.util.logging.Level;
import java.util.logging.Logger;

public class EventLogger {

	private final Logger LOG = Logger.getLogger("Minecraft");
	private final String cycle;

	protected EventLogger(String cycle) {
		this.cycle = cycle;
	}

	public void log(Level level, String info) {
		this.LOG.log(level, "[" + cycle + "]: " + info);
	}

}
