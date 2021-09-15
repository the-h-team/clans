package com.github.sanctum.clans.events.core;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.events.AsyncClanEventBuilder;
import java.util.Set;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ClanChatEvent extends AsyncClanEventBuilder {

	private final Clan.Associate associate;
	private final Set<Player> recipients;
	private BaseComponent[] components;
	private String message;

	private Sound pingSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

	public ClanChatEvent(Clan.Associate associate, Set<Player> recipients, String message) {
		this.message = message;
		this.recipients = recipients;
		this.associate = associate;
	}

	public String getChannel() {
		return associate.getChat();
	}

	public BaseComponent[] getComponents() {
		return components;
	}

	public void setComponents(BaseComponent... components) {
		this.components = components;
	}

	public Set<Player> getRecipients() {
		return recipients;
	}

	public Clan.Associate getAssociate() {
		return associate;
	}

	public String getMessage() {
		return message;
	}

	public Sound getPingSound() {
		return pingSound;
	}

	public void setPingSound(Sound pingSound) {
		this.pingSound = pingSound;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
