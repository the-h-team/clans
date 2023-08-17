package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.panther.file.MemorySpace;
import java.util.List;

public class DefaultFancyInfoSection {

	final MemorySpace space;

	public DefaultFancyInfoSection(MemorySpace space) {
		this.space = space;
	}

	public String getText() {
		return space.getNode("text").toPrimitive().getString();
	}

	public List<String> getHover() {
		return space.getNode("hover").toPrimitive().getStringList();
	}

	public String getCommand() {
		return space.getNode("command").toPrimitive().getString();
	}

	public String getSuggestion() {
		return space.getNode("suggestion").toPrimitive().getString();
	}

	public String getCopyText() {
		return space.getNode("copy").toPrimitive().getString();
	}

	public String getUrlCopyText() {
		return space.getNode("url").toPrimitive().getString();
	}

	public String getPrefix() {
		return space.getNode("prefix").toPrimitive().getString();
	}

	public String getSuffix() {
		return space.getNode("suffix").toPrimitive().getString();
	}

	public Message toMsg() {
		FancyMessage msg = new FancyMessage();
		if (getPrefix() != null) msg.then(getPrefix());
		if (getText() != null) {
			msg.then(getText());
			if (!getHover().isEmpty()) {
				getHover().forEach(msg::hover);
			}
			if (getCommand() != null) {
				msg.command(getCommand());
			}
			if (getSuggestion() != null) {
				msg.suggest(getSuggestion());
			}
			if (getCopyText() != null) {
				msg.copy(getCopyText());
			}
			if (getUrlCopyText() != null) {
				msg.url(getUrlCopyText());
			}
		}
		if (getSuffix() != null) msg.then(getSuffix());

		return msg;
	}

	public DefaultFancyInfoSection getAppendage() {
		if (space.isNode("appendage")) {
			return new DefaultFancyInfoSection(space.getNode("appendage"));
		}
		return null;
	}


	public boolean isValid() {
		return getText() != null;
	}
}
