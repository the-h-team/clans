package com.github.sanctum.clans.model;

import org.jetbrains.annotations.NotNull;

public interface ClanAddonLoadAttempt {

	int count();

	String[] read();

	void println(String text);

	void println(Number number);

	boolean load(@NotNull Object o);

}
