package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.model.Channel;
import com.github.sanctum.clans.model.Clan;
import java.util.Set;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a clan associate sends a message to a chat channel.
 */
public class AssociateChatEvent extends AssociateEvent {

	private final Set<Player> recipients;
	private BaseComponent[] components;
	private String message;

	private Sound pingSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

	public AssociateChatEvent(Clan.Associate associate, Set<Player> recipients, String message) {
		super(associate, true);
		this.message = message;
		this.recipients = recipients;
	}

	public Channel getChannel() {
		return getAssociate().getChannel();
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

	public @NotNull String getMessage() {
		return message;
	}

	public Sound getPingSound() {
		return pingSound;
	}

	public void setRecipients(Set<Player> recipients) {
		this.recipients.clear();
		this.recipients.addAll(recipients);
	}

	public void setPingSound(Sound pingSound) {
		this.pingSound = pingSound;
	}

	public void setMessage(String message) {
		if (message == null) {
			this.message = "";
		} else {
			this.message = message;
		}
	}

}
