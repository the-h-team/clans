package com.github.sanctum.clans.construct.api;

public enum Permission {

	INVITE_PLAYERS(Clan.ACTION.invitationClearance()),
	KICK_MEMBERS(Clan.ACTION.kickClearance()),
	LAND_USE(0),
	LAND_USE_INTRACTABLE(0),
	LOGO_APPLY(ClansAPI.getData().getInt("Clans.logo-apply-clearance")),
	LOGO_COLOR(ClansAPI.getData().getInt("Clans.logo-color-clearance")),
	LOGO_EDIT(ClansAPI.getData().getInt("Clans.logo-edit-clearance")),
	LOGO_PRINT(ClansAPI.getData().getInt("Clans.logo-apply-clearance")),
	LOGO_UPLOAD(ClansAPI.getData().getInt("Clans.logo-upload-clearance")),
	MANAGE_ALL_LAND(Clan.ACTION.unclaimAllClearance()),
	MANAGE_BASE(Clan.ACTION.baseClearance()),
	MANAGE_COLOR(Clan.ACTION.colorChangeClearance()),
	MANAGE_DESCRIPTION(Clan.ACTION.descriptionChangeClearance()),
	MANAGE_FRIENDLY_FIRE(Clan.ACTION.friendlyFireClearance()),
	MANAGE_GIFTING(ClansAPI.getData().getInt("Addon.Mail.gift.clearance")),
	MANAGE_LAND(Clan.ACTION.claimingClearance()),
	MANAGE_MAILING(ClansAPI.getData().getInt("Addon.Mail.mail.clearance")),
	MANAGE_MODE(Clan.ACTION.modeChangeClearance()),
	MANAGE_NAME(Clan.ACTION.tagChangeClearance()),
	MANAGE_NICKNAMES(2),
	MANAGE_PASSWORD(Clan.ACTION.passwordClearance()),
	MANAGE_PERMS(3),
	MANAGE_POSITIONS(Clan.ACTION.positionClearance()),
	MANAGE_RELATIONS(2),
	MANAGE_STASH(ClansAPI.getData().getInt("Addon.Stashes.clearance")),
	MANAGE_VAULT(ClansAPI.getData().getInt("Addon.Vaults.clearance"));
	private final int def;

	Permission(int i) {
		this.def = i;
	}

	public int getDefault() {
		return def;
	}

	public boolean test(Clan.Associate associate) {
		if (associate == null) return false;
		PermissionLog log = associate.getClan().getValue(PermissionLog.class, "permissions");
		return associate.getPriority().toInt() >= log.get(this);
	}

}
