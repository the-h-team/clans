package com.github.sanctum.clans.event.claim;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.panther.container.PantherList;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WildernessNotificationFormatEvent extends ClaimEvent {

	private final PantherList<String> list = new PantherList<>();
	private final Player player;
	private String title, sub_title;
	private boolean titlesAllowed = DataManager.isTitlesAllowed();
	private boolean messagesAllowed;

	public WildernessNotificationFormatEvent(@Nullable Claim previous, @NotNull Player player) {
		super(previous);
		this.player = player;
		this.messagesAllowed = ClansAPI.getDataInstance().isTrue("Clans.land-claiming.send-messages");
	}

	@Override
	public Clan getClan() {
		return ((Clan) getClaim().getHolder());
	}

	public void clearMessages() {
		list.clear();
	}

	public void setTitle(@NotNull String title) {
		this.title = title;
	}

	public void setSubTitle(@NotNull String title) {
		this.sub_title = title;
	}

	public void addMessage(@NotNull String message) {
		list.add(message);
	}

	@Override
	public Player getPlayer() {
		return this.player;
	}

	public String[] getMessages() {
		return list.toArray(new String[0]);
	}

	public String getTitle() {
		return title;
	}

	public String getSubTitle() {
		return sub_title;
	}

	public void setTitlesAllowed(boolean titlesAllowed) {
		this.titlesAllowed = titlesAllowed;
	}

	public void setMessagesAllowed(boolean messagesAllowed) {
		this.messagesAllowed = messagesAllowed;
	}

	public boolean isTitlesAllowed() {
		return titlesAllowed;
	}

	public boolean isMessagesAllowed() {
		return messagesAllowed;
	}
}
