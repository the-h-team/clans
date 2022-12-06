package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.event.player.PlayerEvent;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * The parent abstraction for all clan associate related events.
 */
public abstract class AssociateEvent extends PlayerEvent {
	private final Clan.Associate associate;

	public AssociateEvent(Clan.Associate associate, boolean isAsync) {
		super(associate.getId(), isAsync);
		this.associate = associate;
	}

	// This constructor allows us to specify the default backing Player for the PlayerEvent abstraction
	public AssociateEvent(Clan.Associate associate, UUID id, boolean isAsync) {
		super(id, isAsync);
		this.associate = associate;
	}

	public AssociateEvent(Clan.Associate associate, UUID id, @NotNull State state, boolean isAsync) {
		super(id, state, isAsync);
		this.associate = associate;
	}

	@Override
	public Clan getClan() {
		return getAssociate().getClan();
	}

	public Clan.Associate getAssociate() {
		return associate != null && !associate.isValid() ? null : associate;
	}

	public boolean isOwner() {
		return getAssociate().getPriority().toLevel() == 3;
	}

}
