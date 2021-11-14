package com.github.sanctum.clans.construct.api;

import java.util.Arrays;

/**
 * @deprecated To be replaced by {@link Clearance}
 */
@Deprecated
public enum Permission {

	INVITE_PLAYERS(Clan.ACTION.invitationClearance()),
	KICK_MEMBERS(Clan.ACTION.kickClearance()),
	LAND_USE(0),
	LAND_USE_INTRACTABLE(0),
	LOGO_APPLY(ClansAPI.getDataInstance().getConfigInt("Clans.logo-apply-clearance")),
	LOGO_COLOR(ClansAPI.getDataInstance().getConfigInt("Clans.logo-color-clearance")),
	LOGO_EDIT(ClansAPI.getDataInstance().getConfigInt("Clans.logo-edit-clearance")),
	LOGO_PRINT(ClansAPI.getDataInstance().getConfigInt("Clans.logo-print-clearance")),
	LOGO_DISPLAY(2),
	LOGO_SHARE(0),
	LOGO_UPLOAD(ClansAPI.getDataInstance().getConfigInt("Clans.logo-upload-clearance")),
	MANAGE_ALL_LAND(Clan.ACTION.unclaimAllClearance()),
	MANAGE_BASE(Clan.ACTION.baseClearance()),
	MANAGE_COLOR(Clan.ACTION.colorChangeClearance()),
	MANAGE_DESCRIPTION(Clan.ACTION.descriptionChangeClearance()),
	MANAGE_FRIENDLY_FIRE(Clan.ACTION.friendlyFireClearance()),
	MANAGE_GIFTING(ClansAPI.getDataInstance().getConfigInt("Addon.Mail.gift.clearance")),
	MANAGE_LAND(Clan.ACTION.claimingClearance()),
	MANAGE_MAILING(ClansAPI.getDataInstance().getConfigInt("Addon.Mail.mail.clearance")),
	MANAGE_MODE(Clan.ACTION.modeChangeClearance()),
	MANAGE_NAME(Clan.ACTION.tagChangeClearance()),
	MANAGE_NICKNAMES(2),
	MANAGE_PASSWORD(Clan.ACTION.passwordClearance()),
	MANAGE_PERMS(3),
	MANAGE_POSITIONS(Clan.ACTION.positionClearance()),
	MANAGE_RELATIONS(2),
	MANAGE_STASH(ClansAPI.getDataInstance().getConfigInt("Addon.Stashes.clearance")),
	MANAGE_VAULT(ClansAPI.getDataInstance().getConfigInt("Addon.Vaults.clearance"));
	private int def;

	Permission(int i) {
		this.def = i;
	}

	public static void updateAll() {
		Arrays.stream(values()).forEach(Permission::update);
	}

	public int getDefault() {
		return def;
	}

	public void update() {
		switch (this) {
			case INVITE_PLAYERS:
				this.def = Clan.ACTION.invitationClearance();
				break;
			case KICK_MEMBERS:
				this.def = Clan.ACTION.kickClearance();
				break;
			case LAND_USE:
			case LAND_USE_INTRACTABLE:
				this.def = 0;
				break;
			case LOGO_APPLY:
				this.def = ClansAPI.getDataInstance().getConfigInt("Clans.logo-apply-clearance");
				break;
			case LOGO_COLOR:
				this.def = ClansAPI.getDataInstance().getConfigInt("Clans.logo-color-clearance");
				break;
			case LOGO_EDIT:
				this.def = ClansAPI.getDataInstance().getConfigInt("Clans.logo-edit-clearance");
				break;
			case LOGO_PRINT:
				this.def = ClansAPI.getDataInstance().getConfigInt("Clans.logo-print-clearance");
				break;
			case LOGO_UPLOAD:
				this.def = ClansAPI.getDataInstance().getConfigInt("Clans.logo-upload-clearance");
				break;
			case MANAGE_ALL_LAND:
				this.def = Clan.ACTION.unclaimAllClearance();
				break;
			case MANAGE_BASE:
				this.def = Clan.ACTION.baseClearance();
				break;
			case MANAGE_COLOR:
				this.def = Clan.ACTION.colorChangeClearance();
				break;
			case MANAGE_DESCRIPTION:
				this.def = Clan.ACTION.descriptionChangeClearance();
				break;
			case MANAGE_FRIENDLY_FIRE:
				this.def = Clan.ACTION.friendlyFireClearance();
				break;
			case MANAGE_GIFTING:
				this.def = ClansAPI.getDataInstance().getConfigInt("Addon.Mail.gift.clearance");
				break;
			case MANAGE_LAND:
				this.def = Clan.ACTION.claimingClearance();
				break;
			case MANAGE_MAILING:
				this.def = ClansAPI.getDataInstance().getConfigInt("Addon.Mail.mail.clearance");
				break;
			case MANAGE_MODE:
				this.def = Clan.ACTION.modeChangeClearance();
				break;
			case MANAGE_NAME:
				this.def = Clan.ACTION.tagChangeClearance();
				break;
			case MANAGE_NICKNAMES:
			case MANAGE_RELATIONS:
				this.def = 2;
				break;
			case MANAGE_PASSWORD:
				this.def = Clan.ACTION.passwordClearance();
				break;
			case MANAGE_PERMS:
				this.def = 3;
				break;
			case MANAGE_POSITIONS:
				this.def = Clan.ACTION.positionClearance();
				break;
			case MANAGE_STASH:
				this.def = ClansAPI.getDataInstance().getConfigInt("Addon.Stashes.clearance");
				break;
			case MANAGE_VAULT:
				this.def = ClansAPI.getDataInstance().getConfigInt("Addon.Vaults.clearance");
				break;
		}
	}

}
