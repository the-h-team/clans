package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.ClanDisplayName;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.clans.event.associate.AssociateRenameClanEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import java.util.regex.Pattern;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class CommandTag extends ClanSubCommand {
	public CommandTag() {
		super("tag");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("tag")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("tag")));
				return true;
			}
			lib.sendMessage(p, lib.commandTag());
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("tag")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("tag")));
				return true;
			}
			if (associate != null) {
				Clan clan = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
				if (associate.getPriority().toLevel() >= Clan.ACTION.tagChangeClearance()) {
					if (!isAlphaNumeric(args[0])) {
						lib.sendMessage(p, lib.nameInvalid(args[0]));
						return true;
					}
					if (args[0].length() > ClansAPI.getDataInstance().getConfig().read(f -> f.getInt("Formatting.tag-size"))) {
						Clan.ACTION.sendMessage(p, lib.nameTooLong(args[0]));
						return true;
					}
					if (Clan.ACTION.getAllClanNames().contains(args[0])) {
						lib.sendMessage(p, lib.alreadyMade(args[0]));
						return true;
					}
					for (String s : ClansAPI.getDataInstance().getConfig().getRoot().getStringList("Clans.name-blacklist")) {
						if (Pattern.compile(Pattern.quote(args[0]), Pattern.CASE_INSENSITIVE).matcher(s).find()) {
							lib.sendMessage(p, "&c&oThis name is not allowed!");
							return true;
						}
					}
					AssociateRenameClanEvent ev = ClanVentBus.call(new AssociateRenameClanEvent(p, clan.getName(), args[0]));
					if (!ev.isCancelled()) {
						clan.setName(ev.getTo());
					}
					if (!LabyrinthProvider.getInstance().isLegacy()) {
						clan.getMembers().forEach(a -> {
							OfflinePlayer op = a.getTag().getPlayer();
							if (op.isOnline()) {
								if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
									if (clan.getPalette().isGradient()) {
										Clan c = a.getClan();
										ClanDisplayName.set(op.getPlayer(), ClansAPI.getDataInstance().formatDisplayTag("", c.getPalette().toGradient().context(c.getName()).translate()));
									} else {
										ClanDisplayName.set(op.getPlayer(), ClansAPI.getDataInstance().formatDisplayTag(ClansAPI.getInstance().getClanManager().getClan(op.getUniqueId()).getPalette().toString(), ClansAPI.getInstance().getClanManager().getClan(op.getUniqueId()).getName()));

									}
								}
							}
						});
					}
				} else {
					lib.sendMessage(p, lib.noClearance());
				}
			} else {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}
			return true;
		}


		return true;
	}
}
