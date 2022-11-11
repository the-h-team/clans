package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.construct.api.Clan;
import org.bukkit.entity.Player;

/**
 * Called when a clan associate displays either their own or another clans information.
 *
 * @apiNote Alternatively called when a normal (non-associate) player views another clans information.
 *
 */
public class AssociateDisplayInfoEvent extends AssociateEvent {

	private final Type type;
	private final Clan c;

	public AssociateDisplayInfoEvent(Clan.Associate associate, Type type) {
		super(associate, true);
		this.type = type;
		this.c = null;
	}

	public AssociateDisplayInfoEvent(Clan.Associate associate, Player player, Clan c, Type type) {
		super(associate, player.getUniqueId(), true);
		this.type = type;
		this.c = c;
	}

	@Override
	public Clan getClan() {
		return c != null ? c : super.getClan();
	}

	public final Type getType() {
		return type;
	}

	public enum Type {
		PERSONAL, OTHER
	}

}
