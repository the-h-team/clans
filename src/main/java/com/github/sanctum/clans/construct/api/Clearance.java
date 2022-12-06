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
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class Clearance implements Nameable, Comparable<Clearance>, JsonIntermediate, Serializable {
	private static final long serialVersionUID = -8205377987907270525L;

	public static final Clearance INVITE_PLAYERS = new Clearance(Clan.ACTION.invitationClearance(), "INVITE_PLAYERS");
	public static final Clearance KICK_MEMBERS = new Clearance(Clan.ACTION.kickClearance(), "KICK_MEMBERS");
	public static final Clearance LAND_USE = new Clearance(0, "LAND_USE");
	public static final Clearance LAND_USE_INTRACTABLE = new Clearance(0, "LAND_USE_INTRACTABLE");
	public static final Clearance LOGO_APPLY = new Clearance(ClansAPI.getDataInstance().getConfigInt("Clans.logo-apply-clearance"), "LOGO_APPLY");
	public static final Clearance LOGO_COLOR = new Clearance(ClansAPI.getDataInstance().getConfigInt("Clans.logo-color-clearance"), "LOGO_COLOR");
	public static final Clearance LOGO_EDIT = new Clearance(ClansAPI.getDataInstance().getConfigInt("Clans.logo-edit-clearance"), "LOGO_EDIT");
	public static final Clearance LOGO_PRINT = new Clearance(ClansAPI.getDataInstance().getConfigInt("Clans.logo-print-clearance"), "LOGO_PRINT");
	public static final Clearance LOGO_DISPLAY = new Clearance(2, "LOGO_DISPLAY");
	public static final Clearance LOGO_SHARE = new Clearance(0, "LOGO_SHARE");
	public static final Clearance LOGO_UPLOAD = new Clearance(ClansAPI.getDataInstance().getConfigInt("Clans.logo-upload-clearance"), "LOGO_UPLOAD");
	public static final Clearance MANAGE_ALL_LAND = new Clearance(Clan.ACTION.unclaimAllClearance(), "MANAGE_ALL_LAND");
	public static final Clearance MANAGE_BASE = new Clearance(Clan.ACTION.baseClearance(), "MANAGE_BASE");
	public static final Clearance MANAGE_COLOR = new Clearance(Clan.ACTION.colorChangeClearance(), "MANAGE_COLOR");
	public static final Clearance MANAGE_DESCRIPTION = new Clearance(Clan.ACTION.descriptionChangeClearance(), "MANAGE_DESCRIPTION");
	public static final Clearance MANAGE_FRIENDLY_FIRE = new Clearance(Clan.ACTION.friendlyFireClearance(), "MANAGE_FRIENDLY_FIRE");
	public static final Clearance MANAGE_GIFTING = new Clearance(ClansAPI.getDataInstance().getConfigInt("Addon.Mail.gift.clearance"), "MANAGE_GIFTING");
	public static final Clearance MANAGE_LAND = new Clearance(Clan.ACTION.claimingClearance(), "MANAGE_LAND");
	public static final Clearance MANAGE_MAILING = new Clearance(ClansAPI.getDataInstance().getConfigInt("Addon.Mail.mail.clearance"), "MANAGE_MAILING");
	public static final Clearance MANAGE_MODE = new Clearance(Clan.ACTION.modeChangeClearance(), "MANAGE_MODE");
	public static final Clearance MANAGE_NAME = new Clearance(Clan.ACTION.tagChangeClearance(), "MANAGE_NAME");
	public static final Clearance MANAGE_NICK_NAME = new Clearance(2, "MANAGE_NICK_NAME");
	public static final Clearance MANAGE_NICKNAMES = new Clearance(2, "MANAGE_NICKNAMES");
	public static final Clearance MANAGE_PASSWORD = new Clearance(Clan.ACTION.passwordClearance(), "MANAGE_PASSWORD");
	public static final Clearance MANAGE_PERMS = new Clearance(3, "MANAGE_PERMS");
	public static final Clearance MANAGE_POSITIONS = new Clearance(Clan.ACTION.positionClearance(), "MANAGE_POSITIONS");
	public static final Clearance MANAGE_RELATIONS = new Clearance(2, "MANAGE_RELATIONS");
	public static final Clearance MANAGE_STASH = new Clearance(ClansAPI.getDataInstance().getConfigInt("Addon.Stashes.clearance"), "MANAGE_STASH");
	public static final Clearance MANAGE_VAULT = new Clearance(ClansAPI.getDataInstance().getConfigInt("Addon.Vaults.clearance"), "MANAGE_VAULT");

	private int def;
	private final String name;

	public Clearance(int defaultLevel, String name) {
		this.def = defaultLevel;
		this.name = name;
	}

	@Json(key = "name")
	@Override
	public @NotNull String getName() {
		return this.name;
	}

	@Json(key = "default")
	public int getDefault() {
		return def;
	}

	public boolean test(InvasiveEntity entity) {
		if (entity == null) return false;
		if (!entity.isAssociate()) return false;
		ClearanceLog log = entity.getAsAssociate().getClan().getPermissions();
		return entity.getAsAssociate().getPriority().toLevel() >= log.get(this);
	}

	public boolean test(Clan.Rank priority) {
		if (priority == null) return false;
		return priority.toLevel() >= getDefault();
	}

	public void update() {
		if (INVITE_PLAYERS.equals(this)) {
			this.def = Clan.ACTION.invitationClearance();
		} else if (KICK_MEMBERS.equals(this)) {
			this.def = Clan.ACTION.kickClearance();
		} else if (LAND_USE.equals(this) || LAND_USE_INTRACTABLE.equals(this)) {
			this.def = 0;
		} else if (LOGO_APPLY.equals(this)) {
			this.def = ClansAPI.getDataInstance().getConfigInt("Clans.logo-apply-clearance");
		} else if (LOGO_COLOR.equals(this)) {
			this.def = ClansAPI.getDataInstance().getConfigInt("Clans.logo-color-clearance");
		} else if (LOGO_EDIT.equals(this)) {
			this.def = ClansAPI.getDataInstance().getConfigInt("Clans.logo-edit-clearance");
		} else if (LOGO_PRINT.equals(this)) {
			this.def = ClansAPI.getDataInstance().getConfigInt("Clans.logo-print-clearance");
		} else if (LOGO_UPLOAD.equals(this)) {
			this.def = ClansAPI.getDataInstance().getConfigInt("Clans.logo-upload-clearance");
		} else if (MANAGE_ALL_LAND.equals(this)) {
			this.def = Clan.ACTION.unclaimAllClearance();
		} else if (MANAGE_BASE.equals(this)) {
			this.def = Clan.ACTION.baseClearance();
		} else if (MANAGE_COLOR.equals(this)) {
			this.def = Clan.ACTION.colorChangeClearance();
		} else if (MANAGE_DESCRIPTION.equals(this)) {
			this.def = Clan.ACTION.descriptionChangeClearance();
		} else if (MANAGE_FRIENDLY_FIRE.equals(this)) {
			this.def = Clan.ACTION.friendlyFireClearance();
		} else if (MANAGE_GIFTING.equals(this)) {
			this.def = ClansAPI.getDataInstance().getConfigInt("Addon.Mail.gift.clearance");
		} else if (MANAGE_LAND.equals(this)) {
			this.def = Clan.ACTION.claimingClearance();
		} else if (MANAGE_MAILING.equals(this)) {
			this.def = ClansAPI.getDataInstance().getConfigInt("Addon.Mail.mail.clearance");
		} else if (MANAGE_MODE.equals(this)) {
			this.def = Clan.ACTION.modeChangeClearance();
		} else if (MANAGE_NAME.equals(this)) {
			this.def = Clan.ACTION.tagChangeClearance();
		} else if (MANAGE_NICKNAMES.equals(this) || MANAGE_RELATIONS.equals(this)) {
			this.def = 2;
		} else if (MANAGE_PASSWORD.equals(this)) {
			this.def = Clan.ACTION.passwordClearance();
		} else if (MANAGE_PERMS.equals(this)) {
			this.def = 3;
		} else if (MANAGE_POSITIONS.equals(this)) {
			this.def = Clan.ACTION.positionClearance();
		} else if (MANAGE_STASH.equals(this)) {
			this.def = ClansAPI.getDataInstance().getConfigInt("Addon.Stashes.clearance");
		} else if (MANAGE_VAULT.equals(this)) {
			this.def = ClansAPI.getDataInstance().getConfigInt("Addon.Vaults.clearance");
		}
	}

	public static void updateAll() {
		Arrays.stream(values()).forEach(Clearance::update);
	}

	public static Clearance[] values() {
		return Constant.values(Clearance.class, Clearance.class).toArray(new Clearance[0]);
	}

	/**
	 * @param name The name of the clearance or the clearance object itself as json.
	 * @return A valid clearance object.
	 * @apiNote Can be used like the normal {@link Enum#valueOf(Class, String)} method but ALSO accepts Json.
	 */
	public static Clearance valueOf(@Json String name) {
		if (Check.isJson(name)) {
			JsonObject object = new JsonParser().parse(name).getAsJsonObject();
			if (object.get(Clearance.class.getName()) != null) {
				JsonObject o = object.get(Clearance.class.getName()).getAsJsonObject();
				Map<String, Object> map = JsonIntermediate.convertToMap(o);
				String n = (String) map.get("name");
				int def = (int) map.get("default");
				return Constant.values(Clearance.class).stream().filter(c -> c.getName().equals(n)).findFirst().map(Constant::getValue).orElse(new Clearance(def, n));
			}
			throw new IllegalArgumentException("Object not related to progress bar.");
		}
		return Constant.values(Clearance.class).stream().filter(c -> c.getName().equals(name)).findFirst().map(Constant::getValue).orElse(null);
	}

	@Override
	public int compareTo(@NotNull Clearance o) {
		return String.CASE_INSENSITIVE_ORDER.compare(getName(), o.getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Clearance)) return false;
		Clearance ob = (Clearance) obj;
		return getName().equals(ob.getName()) && getDefault() == ob.getDefault();
	}

	@Override
	public JsonObject toJsonObject() {
		JsonObject o = new JsonObject();
		o.add(getClass().getName(), JsonIntermediate.toJsonObject(this));
		return o;
	}

	public static class Level {
		public static final int EMPTY = -1;
		public static final int MEMBER = 0;
		public static final int MODERATOR = 1;
		public static final int ADMIN = 2;
		public static final int OWNER = 3;

		public static Integer[] values() {
			return Constant.values(Level.class, Integer.class).toArray(new Integer[0]);
		}

	}


}
