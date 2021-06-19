package com.github.sanctum.mail;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class MailBox implements Serializable {

	private final int maxSize = 50;

	private final String id;

	private final Collection<MailObject> inbox = new HashSet<>();

	public MailBox(Clan c) {
		this.id = c.getId().toString();
		saveChanges();
	}

	public Clan getClan() {
		return ClansAPI.getInstance().getClan(this.id);
	}

	public int getMaxSize() {
		return maxSize;
	}

	public Collection<MailObject> getInbox() {
		return inbox;
	}

	public void sendMail(MailObject mail) {
		if (inbox.size() <= maxSize) {
			if (inbox.stream().noneMatch(m -> m.equals(mail))) {
				inbox.add(mail);
				saveChanges();
			}
		}
	}

	public void markRead(MailObject mail) {
		inbox.removeIf(m -> m.equals(mail));
		saveChanges();
	}

	private void saveChanges() {
		getClan().setValue("mail-box", this);
	}

	public List<String> getMailList() {
		return getInbox().stream().map(MailObject::getSender).collect(Collectors.toList());
	}

	public Collection<MailObject> getInboxByClan(String clanName) {
		return getInbox().stream().filter(m -> m.getSender().equals(clanName)).collect(Collectors.toList());
	}

	public MailObject getMailByTopic(String topic) {
		MailObject result = null;
		for (MailObject mail : inbox) {
			if (mail.getTopic().equals(topic)) {
				result = mail;
				break;
			}
		}
		return result;
	}

	public static MailBox getMailBox(Clan c) {
		return c.getValue(MailBox.class, "mail-box");
	}

}
