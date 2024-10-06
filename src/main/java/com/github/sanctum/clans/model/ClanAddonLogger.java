package com.github.sanctum.clans.model;

import java.util.function.Supplier;
import java.util.logging.Level;

public interface ClanAddonLogger {

	void log(Level level, String info);

	void info(Supplier<String> info);

	void warn(Supplier<String> info);

	void error(Supplier<String> info);

	void info(String info);

	void warn(String info);

	void error(String info);

}
