package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ChatChannel;

/**
 * Called when a clan associate receives a message from a chat channel on successful submission from {@link AssociateChatEvent}.
 */
public class AssociateMessageReceiveEvent extends AssociateEvent {

	private final Clan.Associate sender;
	private final String message;
	private final ChatChannel chatChannel;

	public AssociateMessageReceiveEvent(Clan.Associate associate, Clan.Associate sender, ChatChannel chatChannel, String message) {
		super(associate, true);
		this.sender = sender;
		this.message = message;
		this.chatChannel = chatChannel;
	}

	public Clan.Associate getSender() {
		return sender;
	}

	public String getMessage() {
		return message;
	}

	public ChatChannel getChannel() {
		return this.chatChannel;
	}
}
