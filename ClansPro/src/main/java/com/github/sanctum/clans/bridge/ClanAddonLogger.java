package com.github.sanctum.clans.bridge;

import java.util.function.Supplier;
import java.util.logging.Level;

public interface ClanAddonLogger {

	public void log(Level level, String info);

	public void info(Supplier<String> info);

	public void warn(Supplier<String> info);

	public void error(Supplier<String> info);

	public void info(String info);

	public void warn(String info);

	public void error(String info);

}
