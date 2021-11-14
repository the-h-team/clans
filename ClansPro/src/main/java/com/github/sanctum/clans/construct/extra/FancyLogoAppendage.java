package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.md_5.bungee.api.chat.BaseComponent;

public class FancyLogoAppendage {

	private final List<FancyMessage> list = new ArrayList<>();

	public FancyLogoAppendage append(List<String> logo, Consumer<FancyMessage> consumer) {

		logo.forEach(s -> {
			FancyMessage message = new FancyMessage();
			consumer.accept(message.then(s));
			list.add(message);
		});

		return this;
	}

	public FancyLogoAppendage append(List<String> logo, Consumer<FancyMessage> consumer, String... message) {
		for (String s : ClansAPI.getDataInstance().appendStringsToLogo(logo, DataManager.Side.RIGHT, message)) {
			FancyMessage m = new FancyMessage();
			consumer.accept(m.then(s));
			list.add(m);
		}
		return this;
	}

	public FancyLogoAppendage append(List<String> logo, Consumer<FancyMessage> consumer, Message.Chunk... message) {
		for (String s : logo) {
			FancyMessage m = new FancyMessage();
			consumer.accept(m.then(s));
			list.add(m);
		}
		return append(message);
	}

	public FancyLogoAppendage append(Message.Chunk... message) {
		int position = getCenter(list.size());
		for (int i = 0; i < list.size(); i++) {
			FancyMessage mess = list.get(i);
			if (i >= position) {
				if ((Math.max(0, i - (position))) <= message.length - 1) {
					Message.Chunk c = message[Math.max(0, i - (position))];
					if (c != null) {
						mess.then("   ").append(c);
					}
				}
			}
		}
		return this;
	}

	int getCenter(int size) {
		switch (size) {
			case 1:
				return 0;
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
				return 1;
			case 8:
			case 9:
				return 3;
			case 10:
			case 11:
			case 12:
			case 13:
				return 4;
			case 14:
			case 15:
				return 5;
			case 16:
				return 6;

		}
		return 0;
	}

	public BaseComponent[][] get() {
		List<BaseComponent[]> list = new ArrayList<>();
		this.list.forEach(m -> list.add(m.build()));
		return list.toArray(new BaseComponent[0][0]);
	}

}
