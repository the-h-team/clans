package com.github.sanctum.clans.construct.extra;

public class MessagePrefix {

	private final String text;
	private final String prefix;
	private final String suffix;

	public MessagePrefix(String prefix, String text, String suffix) {
		this.prefix = prefix;
		this.text = text;
		this.suffix = suffix;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return getPrefix() + getText() + getSuffix();
	}

}
