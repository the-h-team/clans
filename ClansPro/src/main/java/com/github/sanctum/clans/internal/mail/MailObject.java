package com.github.sanctum.clans.internal.mail;

import java.io.Serializable;
import java.util.Objects;

public class MailObject implements Serializable {

	private final String sender;

	private final String topic;

	private final String context;

	public MailObject(String sender, String topic, String context) {
		this.topic = topic;
		this.context = context;
		this.sender = sender;
	}

	public String getTopic() {
		return topic;
	}

	public String getSender() {
		return sender;
	}

	public String getContext() {
		return context;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MailObject)) return false;
		MailObject that = (MailObject) o;
		return getSender().equals(that.getSender()) &&
				getTopic().equals(that.getTopic()) &&
				getContext().equals(that.getContext());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSender(), getTopic(), getContext());
	}
}
