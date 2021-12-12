package com.github.sanctum.clans.bridge.external.bounty;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BountyCommand extends ClanSubCommand {
	public BountyCommand(String label) {
		super(label);
	}

	@Override
	public boolean player(Player p, String label, String[] args) {

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("bounty")) {
				if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
					Clan c = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
					UUID id = Clan.ACTION.getUserID(args[1]);
					if (id != null) {
						if (c.getMember(a -> a.getId().equals(id)) != null) {
							Clan.ACTION.sendMessage(p, "&c&oYou cannot put bounties on clan members.");

							return true;
						}
						if (BountyList.get(c, id) == null) {
							try {
								Double.parseDouble(args[2]);
							} catch (NumberFormatException ex) {
								Clan.ACTION.sendMessage(p, "&cInvalid amount chosen must be ##.## format.");
							}
							double amount = Double.parseDouble(args[2]);
							boolean has = EconomyProvision.getInstance().has(BigDecimal.valueOf(amount), p, p.getWorld().getName()).orElse(false);
							if (has) {
								EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(amount), p, p.getWorld().getName());
								String format = ClansAPI.getDataInstance().getConfig().getRoot().getString("Addon.Bounty.settings.called-message")
										.replace("{PLAYER}", p.getName())
										.replace("{CLAN}", c.getName())
										.replace("{TARGET}", args[1])
										.replace("{BOUNTY}", args[2]);
								Bukkit.broadcastMessage(StringUtils.use(format).translate());
								FileManager clanFile = ClansAPI.getDataInstance().getClanFile(c);
								clanFile.getRoot().set("bounties." + Clan.ACTION.getUserID(args[1]).toString(), amount);
								clanFile.getRoot().save();
							} else {
								Clan.ACTION.sendMessage(p, "&c&oYou don't have enough money for a bounty this big!");
							}
						} else {
							Clan.ACTION.sendMessage(p, "&c&oYour clan has already called a bounty on this target.");
						}
					} else {
						Clan.ACTION.sendMessage(p, Clan.ACTION.playerUnknown(args[1]));
					}
				} else {
					Clan.ACTION.sendMessage(p, Clan.ACTION.notInClan());
				}

				return true;
			}
		}
		if (args.length > 0) {
			Clan.ACTION.sendMessage(p, "&cUsage: &6/clan &fbounty <playerName> <amount>");
		}
		
		return true;
	}

	@Override
	public boolean console(CommandSender sender, String label, String[] args) {
		return false;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return null;
	}
}
