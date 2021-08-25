package com.github.sanctum.clans.events.core;

import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.events.AsyncClanEventBuilder;
import java.util.Set;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ClanChatEvent extends AsyncClanEventBuilder {

	private final ClanAssociate associate;
	private final Set<Player> recipients;
	private String message;
	private String static1 = "&7[&3&l&nChat&7] ";
	private String static2 = " &7: ";
	private String highlight = "&f&o{0}";
	private String playerMeta = "&3&oClan member &b{0} &3&opinged clan chat.";

	private Sound pingSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

	public ClanChatEvent(ClanAssociate associate, Set<Player> recipients, String message) {
		this.message = message;
		this.recipients = recipients;
		this.associate = associate;
	}

	public String getChannel() {
		return associate.getChat();
	}

	public Set<Player> getRecipients() {
		return recipients;
	}

	public ClanAssociate getAssociate() {
		return associate;
	}

	public String getMessage() {
		return message;
	}

	public Sound getPingSound() {
		return pingSound;
	}

	public String getPrefix() {
		return static1;
	}

	public String getDivider() {
		return static2;
	}

	public String getHighlight() {
		return highlight;
	}

	public String getHoverMeta() {
		return playerMeta;
	}

	public void setPingSound(Sound pingSound) {
		this.pingSound = pingSound;
	}

	public void setHoverMeta(String playerMeta) {
		this.playerMeta = playerMeta;
	}

	public void setPrefix(String static1) {
		this.static1 = static1;
	}

	public void setDivider(String static2) {
		this.static2 = static2;
	}

	public void setHighlight(String highlight) {
		this.highlight = highlight;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
