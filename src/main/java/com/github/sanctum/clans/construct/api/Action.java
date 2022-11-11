package com.github.sanctum.clans.construct.api;

public interface Action<O> extends Runnable {

	O deploy();

	@Override
	default void run() {
		deploy();
	}

}
