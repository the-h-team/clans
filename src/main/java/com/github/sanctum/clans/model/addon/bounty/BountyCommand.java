package com.github.sanctum.clans.model.addon.bounty;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BountyCommand extends ClanSubCommand {
	public BountyCommand(String label) {
		super(label);
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		if (args.length == 2) {
			if (ClansAPI.getInstance().getAssociate(p.getUniqueId()).isPresent()) {
				Clan c = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
				UUID id = Clan.ACTION.getId(args[0]).deploy();
				if (id != null) {
					if (c.getMember(a -> a.getId().equals(id)) != null) {
						Clan.ACTION.sendMessage(p, "&c&oYou cannot put bounties on clan members.");
						return true;
					}
					if (BountyList.get(c, id) == null) {
						try {
							Double.parseDouble(args[1]);
						} catch (NumberFormatException ex) {
							Clan.ACTION.sendMessage(p, "&cInvalid amount chosen must be ##.## format.");
						}
						double amount = Double.parseDouble(args[1]);
						boolean has = EconomyProvision.getInstance().has(BigDecimal.valueOf(amount), p, p.getWorld().getName()).orElse(false);
						if (has) {
							EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(amount), p, p.getWorld().getName());
							String format = ClansAPI.getDataInstance().getConfig().getRoot().getString("Addon.Bounty.settings.called-message")
									.replace("{PLAYER}", p.getName())
									.replace("{CLAN}", c.getName())
									.replace("{TARGET}", args[0])
									.replace("{BOUNTY}", args[1]);
							Bukkit.broadcastMessage(StringUtils.use(format).translate());
							FileManager clanFile = ClansAPI.getDataInstance().getClanFile(c);
							clanFile.getRoot().set("bounties." + Clan.ACTION.getId(args[0]).deploy().toString(), amount);
							clanFile.getRoot().save();
						} else {
							Clan.ACTION.sendMessage(p, "&c&oYou don't have enough money for a bounty this big!");
						}
					} else {
						Clan.ACTION.sendMessage(p, "&c&oYour clan has already called a bounty on this target.");
					}
				} else {
					Clan.ACTION.sendMessage(p, Clan.ACTION.playerUnknown(args[0]));
				}
			} else {
				Clan.ACTION.sendMessage(p, Clan.ACTION.notInClan());
			}

			return true;
		}
		Clan.ACTION.sendMessage(p, "&cUsage: &6/clan &fbounty <playerName> <amount>");

		return true;
	}

	@Override
	public boolean console(CommandSender sender, String label, String[] args) {
		return false;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return SimpleTabCompletion.of(args).then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList())).get();
	}
}
