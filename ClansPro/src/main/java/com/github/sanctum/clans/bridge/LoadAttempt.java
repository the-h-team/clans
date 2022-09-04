package com.github.sanctum.clans.bridge;

import org.jetbrains.annotations.NotNull;

public interface LoadAttempt {

	int count();

	String[] read();

	void println(String text);

	void println(Number number);

	boolean load(@NotNull Object o);

}
