package com.github.sanctum.clans.util.events.clans;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.events.AsyncClanEventBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ChatChannelClanEvent extends AsyncClanEventBuilder {

	private final Player chatting;

	private final Set<Player> receivers;

	private String message;

	private String static1 = "&7[&3&l&nCC&7] ";

	private String static2 = " &7: ";

	private String highlight = "&f&o%s";

	private String playerMeta = "&3&oClan member &b%s &3&opinged clan chat.";

	private Sound pingSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

	{
		if (Bukkit.getVersion().contains("1.16")) {
			static1 = "&#5b626e[&#16b7db&l&nCC&#5b626e] ";
			static2 = " &#5b626e: &f";
			highlight = "&#1696db&o%s";
		}
	}

	public ChatChannelClanEvent(Player sender, Set<Player> receivers, String message) {
		this.chatting = sender;
		this.receivers = receivers;
		this.message = message;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	public Player getChatting() {
		return chatting;
	}

	public Clan getClan() {
		return ClansAPI.getInstance().getClan(chatting.getUniqueId());
	}

	public List<Player> getReceivers() {
		List<Player> recipients = new ArrayList<>();
		for (Player a : receivers) {
			if (getUtil().getClanID(a.getUniqueId()) != null) {
				if (getUtil().getClanID(a.getUniqueId()).equals(getUtil().getClanID(chatting.getUniqueId()))) {
					recipients.add(a);
				}
			}
		}
		recipients.addAll(ClansAPI.getData().CLAN_SPY);
		return recipients;
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
