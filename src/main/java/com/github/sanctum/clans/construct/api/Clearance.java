package com.github.sanctum.clans.construct.api;

import com.github.sanctum.labyrinth.data.service.Constant;
import com.github.sanctum.labyrinth.interfacing.JsonIntermediate;
import com.github.sanctum.labyrinth.interfacing.Nameable;
import com.github.sanctum.panther.annotation.Json;
import com.github.sanctum.panther.util.Check;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;


// all fields here are persistently cached and available like a normal enum.
public final class Clearance implements Nameable, Comparable<Clearance>, JsonIntermediate, Serializable {
	private static final long serialVersionUID = -8205377987907270525L;

	public static final Clearance INVITE_PLAYERS = new Clearance("INVITE_PLAYERS");
	public static final Clearance KICK_MEMBERS = new Clearance("KICK_MEMBERS");
	public static final Clearance CREATE_KINGDOM = new Clearance("CREATE_KINGDOM");
	public static final Clearance LEAVE_KINGDOM = new Clearance("LEAVE_KINGDOM");
	public static final Clearance JOIN_KINGDOM = new Clearance("JOIN_KINGDOM");
	public static final Clearance RENAME_KINGDOM = new Clearance("RENAME_KINGDOM");
	public static final Clearance RESEAT_KINGDOM = new Clearance("RESEAT_KINGDOM");
	public static final Clearance LAND_USE = new Clearance("LAND_USE");
	public static final Clearance LAND_USE_INTERACTABLE = new Clearance("LAND_USE_INTERACTABLE");
	public static final Clearance LOGO_APPLY = new Clearance("LOGO_APPLY");
	public static final Clearance LOGO_COLOR = new Clearance("LOGO_COLOR");
	public static final Clearance LOGO_EDIT = new Clearance("LOGO_EDIT");
	public static final Clearance LOGO_PRINT = new Clearance("LOGO_PRINT");
	public static final Clearance LOGO_DISPLAY = new Clearance("LOGO_DISPLAY");
	public static final Clearance LOGO_SHARE = new Clearance("LOGO_SHARE");
	public static final Clearance LOGO_UPLOAD = new Clearance("LOGO_UPLOAD");
	public static final Clearance MANAGE_ALL_LAND = new Clearance("MANAGE_ALL_LAND");
	public static final Clearance MANAGE_BASE = new Clearance("MANAGE_BASE");
	public static final Clearance MANAGE_COLOR = new Clearance("MANAGE_COLOR");
	public static final Clearance MANAGE_DESCRIPTION = new Clearance("MANAGE_DESCRIPTION");
	public static final Clearance MANAGE_FRIENDLY_FIRE = new Clearance("MANAGE_FRIENDLY_FIRE");
	public static final Clearance MANAGE_GIFTING = new Clearance("MANAGE_GIFTING");
	public static final Clearance MANAGE_LAND = new Clearance("MANAGE_LAND");
	public static final Clearance MANAGE_MAILING = new Clearance("MANAGE_MAILING");
	public static final Clearance MANAGE_MODE = new Clearance("MANAGE_MODE");
	public static final Clearance MANAGE_NAME = new Clearance("MANAGE_NAME");
	public static final Clearance MANAGE_NICK_NAME = new Clearance("MANAGE_NICK_NAME");
	public static final Clearance MANAGE_NICKNAMES = new Clearance("MANAGE_NICKNAMES");
	public static final Clearance MANAGE_PASSWORD = new Clearance("MANAGE_PASSWORD");
	public static final Clearance MANAGE_PERMS = new Clearance("MANAGE_PERMS");
	public static final Clearance MANAGE_POSITIONS = new Clearance("MANAGE_POSITIONS");
	public static final Clearance MANAGE_RELATIONS = new Clearance("MANAGE_RELATIONS");
	public static final Clearance MANAGE_STASH = new Clearance("MANAGE_STASH");
	public static final Clearance MANAGE_VAULT = new Clearance("MANAGE_VAULT");
	public static final Clearance VIEW_BANK_LOG = new Clearance("VIEW_BANK_LOG");
	public static final Clearance BANK_BALANCE = new Clearance("BANK_BALANCE");
	public static final Clearance BANK_DEPOSIT = new Clearance("BANK_DEPOSIT");
	public static final Clearance BANK_WITHDRAW = new Clearance("BANK_WITHDRAW");

	private final String name;

	public Clearance(String name) {
		this.name = name;
	}

	@Json(key = "name")
	@Override
	public @NotNull String getName() {
		return this.name;
	}

	public int getDefault() {
		// get first known group with permission, goes in ordinal order so should work!
		return RankRegistry.getInstance().getRanks().stream().filter(p -> Arrays.asList(p.getDefaultPermissions()).contains(this)).map(Clan.Rank::getLevel).findFirst().orElse(0);
	}

	public boolean test(InvasiveEntity entity) {
		if (entity == null) return false;
		if (!entity.isAssociate()) return false;
		ClearanceOverride override = entity.getAsAssociate().getClan().getPermissiveHandle();
		return override.get(entity.getAsAssociate().getRank()).contains(this);
	}

	public boolean testDefault(Clan.Rank priority) {
		if (priority == null) return false;
		for (Clearance c : priority.getDefaultPermissions()) {
			if (c.equals(this)) return true;
		}
		if (priority.isInheritable()) {
			for (Clan.Rank position : priority.getInheritance()) {
				for (Clearance c : position.getDefaultPermissions()) {
					if (c.equals(this)) return true;
				}
			}
		}
		return false;
	}

	public static Clearance[] values() {
		List<Clearance> list = Constant.values(Clearance.class, Clearance.class);
		RankRegistry.getInstance().getClearances().forEach(list::add);
		return list.toArray(new Clearance[0]);
	}

	/**
	 * @param name The name of the clearance or the clearance object itself as json.
	 * @return A valid clearance object.
	 * @apiNote Can be used like the normal {@link Enum#valueOf(Class, String)} method but ALSO accepts Json.
	 * @throws IllegalArgumentException if the provided string is irrelevant to  any registered clearance.
	 */
	public static Clearance valueOf(@Json String name) {
		if (Check.isJson(name)) {
			JsonObject object = JsonParser.parseString(name).getAsJsonObject();
			if (object.get(Clearance.class.getName()) != null) {
				JsonObject o = object.get(Clearance.class.getName()).getAsJsonObject();
				Map<String, Object> map = JsonIntermediate.convertToMap(o);
				String n = (String) map.get("name");
				return Constant.values(Clearance.class).stream().filter(c -> c.getName().equals(n)).findFirst().map(Constant::getValue).orElse(new Clearance(n));
			}
			throw new IllegalArgumentException("No clearance by the name of " + name + " found");
		}
		for (Clearance c : RankRegistry.getInstance().getClearances()) {
			if (c.name.equals(name)) return c;
		}
		Clearance cl = Constant.values(Clearance.class).stream().filter(c -> c.getName().equals(name)).findFirst().map(Constant::getValue).orElse(null);
		if (cl == null) throw new IllegalStateException("There is no known clearance by the name of " + '"' + name + '"');
		return cl;
	}

	@Override
	public int compareTo(@NotNull Clearance o) {
		return String.CASE_INSENSITIVE_ORDER.compare(getName(), o.getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Clearance)) return false;
		Clearance ob = (Clearance) obj;
		return getName().equals(ob.getName());
	}

	@Override
	public JsonObject toJsonObject() {
		JsonObject o = new JsonObject();
		o.add(getClass().getName(), JsonIntermediate.toJsonObject(this));
		return o;
	}

	public static class Level {
		public static final int EMPTY = -1;

		public static Integer[] values() {
			return Constant.values(Level.class, Integer.class).toArray(new Integer[0]);
		}

	}


}
