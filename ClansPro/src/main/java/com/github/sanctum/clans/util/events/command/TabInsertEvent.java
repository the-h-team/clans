package com.github.sanctum.clans.util.events.command;

import com.github.sanctum.clans.util.events.ClanEventBuilder;
import java.util.ArrayList;
import java.util.List;

public class TabInsertEvent extends ClanEventBuilder {

	private final List<String> args1 = new ArrayList<>();

	private final List<String> args2 = new ArrayList<>();

	private final List<String> args3 = new ArrayList<>();

	private final String[] commandArgs;

	public TabInsertEvent(String[] commandArgs) {
		this.commandArgs = commandArgs;
	}

	public String[] getCommandArgs() {
		return commandArgs;
	}

	public List<String> getArgs(int index) {
		List<String> result = null;
		switch (index) {
			case 1:
				result = args1;
				break;

			case 2:
				result = args2;
				break;

			case 3:
				result = args3;
				break;
		}
		return result;
	}

	public void add(int index, String value) {
		switch (index) {
			case 1:
				args1.add(value);
				break;

			case 2:
				args2.add(value);
				break;

			case 3:
				args3.add(value);
				break;
		}
	}

	public void remove(int index, String value) {
		switch (index) {
			case 1:
				args1.remove(value);
				break;

			case 2:
				args2.remove(value);
				break;

			case 3:
				args3.remove(value);
				break;
		}
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}


}
