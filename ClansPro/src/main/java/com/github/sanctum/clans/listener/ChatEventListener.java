package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.clans.events.core.ClanChatEvent;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.TextLib;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEventListener implements Listener {


	@Subscribe(priority = Vent.Priority.MEDIUM)
	public void onAllyChat(ClanChatEvent e) {
		if (e.getChannel().equalsIgnoreCase("ALLY")) {
			List<BaseComponent> list = new ArrayList<>();
			list.add(TextLib.getInstance().textHoverable(MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.ally.prefix"), e.getAssociate().getNickname(), e.getAssociate().getClan().getName(), e.getAssociate().getClan().getColor(), e.getAssociate().getRankTag()), MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.ally.highlight"), e.getAssociate().getNickname(), e.getAssociate().getClan().getName(), e.getAssociate().getClan().getColor(), e.getAssociate().getRankTag()), MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.ally.divider") + e.getMessage(), e.getAssociate().getNickname(), e.getAssociate().getClan().getName(), e.getAssociate().getClan().getColor(), e.getAssociate().getRankTag()), MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.ally.hover"), e.getAssociate().getNickname(), e.getAssociate().getClan().getName(), e.getAssociate().getClan().getColor(), e.getAssociate().getRankTag())));
			e.setPingSound(Sound.ENTITY_VILLAGER_YES);
			e.setComponents(list.toArray(new BaseComponent[0]));
		}
		if (e.getChannel().equalsIgnoreCase("CLAN")) {
			ClanAssociate associate = e.getAssociate();
			List<BaseComponent> list = new ArrayList<>();
			list.add(TextLib.getInstance().textHoverable(MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.clan.prefix"), e.getAssociate().getNickname(), e.getAssociate().getClan().getName(), e.getAssociate().getClan().getColor(), e.getAssociate().getRankTag()), MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.clan.highlight"), e.getAssociate().getNickname(), e.getAssociate().getClan().getName(), e.getAssociate().getClan().getColor(), e.getAssociate().getRankTag()), MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.clan.divider") + e.getMessage(), e.getAssociate().getNickname(), e.getAssociate().getClan().getName(), e.getAssociate().getClan().getColor(), e.getAssociate().getRankTag()), MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.clan.hover"), e.getAssociate().getNickname(), e.getAssociate().getClan().getName(), e.getAssociate().getClan().getColor(), e.getAssociate().getRankTag())));
			e.setPingSound(Sound.ENTITY_VILLAGER_YES);
			e.setComponents(list.toArray(new BaseComponent[0]));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onFormat(AsyncPlayerChatEvent event) throws ExecutionException, InterruptedException {
		Player p = event.getPlayer();
		ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (associate != null) {
			if (ClansAPI.getData().getEnabled("Formatting.allow")) {
				if (associate.getChat().equalsIgnoreCase("global")) {
					Clan clan = associate.getClan();
					String clanName = clan.getName();
					StringLibrary lib = Clan.ACTION;
					String rank;
					clanName = clan.getColor() + clanName;
					switch (lib.getRankStyle().toUpperCase()) {
						case "WORDLESS":
							rank = associate.getRankShort();

							event.setFormat(lib.color(MessageFormat.format(lib.getChatFormat(), rank, clanName) + " " + event.getFormat()));
							break;
						case "FULL":
							rank = associate.getRankTag();
							event.setFormat(lib.color(MessageFormat.format(lib.getChatFormat(), rank, clanName) + " " + event.getFormat()));
							break;
					}
					return;
				}
			}

			if (associate.getChat().equalsIgnoreCase("CLAN")) {
				Set<Player> players = associate.getClan().getMembers().stream().filter(m -> m.getPlayer().isOnline()).map(ClanAssociate::getPlayer).map(OfflinePlayer::getPlayer).collect(Collectors.toSet());
				players.addAll(ClansAPI.getData().CLAN_SPY);
				ClanChatEvent e = ClanVentBus.queue(new ClanChatEvent(associate, players, event.getMessage())).get();
				if (!e.isCancelled()) {
					if (ClansAPI.getData().getEnabled("Formatting.chat-spy-console")) {
						ClansAPI.getInstance().getPlugin().getLogger().info("- [CLAN] " + e.getAssociate().getName() + " : " + e.getMessage());
					}
					for (Player toGet : e.getRecipients()) {
						Message.form(toGet).build(e.getComponents());
						toGet.playSound(toGet.getLocation(), e.getPingSound(), 10, 1);
					}
				}
				event.setCancelled(true);
				return;
			}
			if (associate.getChat().equalsIgnoreCase("ALLY")) {
				Set<Player> players = new HashSet<>(ClansAPI.getData().ALLY_SPY);
				for (Clan c : associate.getClan().getAllies().list()) {
					players.addAll(c.getMembers().stream().filter(m -> m.getPlayer().isOnline()).map(ClanAssociate::getPlayer).map(OfflinePlayer::getPlayer).collect(Collectors.toSet()));
				}
				players.add(p);
				ClanChatEvent e = ClanVentBus.queue(new ClanChatEvent(associate, players, event.getMessage())).get();
				if (!e.isCancelled()) {
					if (ClansAPI.getData().getEnabled("Formatting.chat-spy-console")) {
						ClansAPI.getInstance().getPlugin().getLogger().info("- [ALLY] " + e.getAssociate().getName() + " : " + e.getMessage());
					}
					for (Player toGet : e.getRecipients()) {
						Message.form(toGet).build(e.getComponents());
						toGet.playSound(toGet.getLocation(), e.getPingSound(), 10, 1);
					}
				}
				event.setCancelled(true);
				return;
			}
			List<String> defaults = new ArrayList<>(Arrays.asList("GLOBAL", "CLAN", "ALLY"));
			if (!defaults.contains(associate.getChat())) {
				ClanChatEvent e = ClanVentBus.queue(new ClanChatEvent(associate, Bukkit.getOnlinePlayers().stream().filter(pl -> ClansAPI.getInstance().isInClan(pl.getUniqueId()) && ClansAPI.getInstance().getAssociate(pl).get().getChat().equals(associate.getChat())).collect(Collectors.toSet()), event.getMessage())).get();
				if (!e.isCancelled()) {
					if (ClansAPI.getData().getEnabled("Formatting.chat-spy-console")) {
						ClansAPI.getInstance().getPlugin().getLogger().info("- [" + associate.getChat() + "] " + e.getAssociate().getName() + " : " + e.getMessage());
					}
					for (Player toGet : e.getRecipients()) {
						Message.form(toGet).build(e.getComponents());
						toGet.playSound(toGet.getLocation(), e.getPingSound(), 10, 1);
					}
				}
				event.setCancelled(true);
			}

		}
	}

}
