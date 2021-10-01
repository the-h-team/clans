package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.clans.events.core.ClanChatEvent;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
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
		Clan.Associate associate = e.getAssociate();
		String color = associate.getClan().getPalette().getStart();
		String name = associate.getClan().getPalette().isGradient() ? associate.getClan().getPalette().toString() : color + associate.getClan().getName();
		if (e.getChannel().equalsIgnoreCase("ALLY")) {
			List<BaseComponent> list = new ArrayList<>();
			list.add(TextLib.getInstance().textHoverable(MessageFormat.format(ClansAPI.getData().getMain().getRoot().getString("Formatting.Chat.Channel.ally.prefix"), associate.getNickname(), name, color, associate.getRankTag(), associate.getClan().getName()), MessageFormat.format(ClansAPI.getData().getMain().getRoot().getString("Formatting.Chat.Channel.ally.highlight"), associate.getNickname(), name, color, associate.getRankTag(), associate.getClan().getName()), MessageFormat.format(ClansAPI.getData().getMain().getRoot().getString("Formatting.Chat.Channel.ally.divider") + e.getMessage(), associate.getNickname(), name, color, associate.getRankTag(), associate.getClan().getName()), MessageFormat.format(ClansAPI.getData().getMain().getRoot().getString("Formatting.Chat.Channel.ally.hover"), associate.getNickname(), name, color, associate.getRankTag(), associate.getClan().getName())));
			e.setPingSound(Sound.ENTITY_VILLAGER_YES);
			e.setComponents(list.toArray(new BaseComponent[0]));
		}
		if (e.getChannel().equalsIgnoreCase("CLAN")) {
			List<BaseComponent> list = new ArrayList<>();
			list.add(TextLib.getInstance().textHoverable(MessageFormat.format(ClansAPI.getData().getMain().getRoot().getString("Formatting.Chat.Channel.clan.prefix"), associate.getNickname(), name, color, associate.getRankTag(), associate.getClan().getName()), MessageFormat.format(ClansAPI.getData().getMain().getRoot().getString("Formatting.Chat.Channel.clan.highlight"), associate.getNickname(), name, color, associate.getRankTag(), associate.getClan().getName()), MessageFormat.format(ClansAPI.getData().getMain().getRoot().getString("Formatting.Chat.Channel.clan.divider") + e.getMessage(), associate.getNickname(), name, color, associate.getRankTag(), associate.getClan().getName()), MessageFormat.format(ClansAPI.getData().getMain().getRoot().getString("Formatting.Chat.Channel.clan.hover"), associate.getNickname(), name, color, associate.getRankTag(), associate.getClan().getName())));
			e.setPingSound(Sound.ENTITY_VILLAGER_YES);
			e.setComponents(list.toArray(new BaseComponent[0]));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onFormat(AsyncPlayerChatEvent event) throws ExecutionException, InterruptedException {
		Player p = event.getPlayer();
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (associate != null) {
			if (ClansAPI.getData().isTrue("Formatting.allow")) {
				if (associate.getChat().equalsIgnoreCase("global")) {
					Clan clan = associate.getClan();
					StringLibrary lib = Clan.ACTION;
					String rank;
					String clanName = clan.getPalette().isGradient() ? clan.getPalette().toString() : clan.getPalette().toString() + clan.getName();
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
				Set<Player> players = associate.getClan().getMembers().stream().filter(m -> m.getUser().toBukkit().isOnline()).map(Clan.Associate::getUser).map(LabyrinthUser::toBukkit).map(OfflinePlayer::getPlayer).collect(Collectors.toSet());
				players.addAll(ClansAPI.getData().CHAT_SPY);
				ClanChatEvent e = ClanVentBus.queue(new ClanChatEvent(associate, players, event.getMessage())).get();
				if (!e.isCancelled()) {
					if (ClansAPI.getData().isTrue("Formatting.chat-spy-console")) {
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
				Set<Player> players = new HashSet<>(ClansAPI.getData().CHAT_SPY);
				for (Clan c : associate.getClan().getAllies().list()) {
					players.addAll(c.getMembers().stream().filter(m -> m.getUser().toBukkit().isOnline()).map(Clan.Associate::getUser).map(LabyrinthUser::toBukkit).map(OfflinePlayer::getPlayer).collect(Collectors.toSet()));
				}
				players.add(p);
				ClanChatEvent e = ClanVentBus.queue(new ClanChatEvent(associate, players, event.getMessage())).get();
				if (!e.isCancelled()) {
					if (ClansAPI.getData().isTrue("Formatting.chat-spy-console")) {
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
				Set<Player> players = new HashSet<>(ClansAPI.getData().CHAT_SPY);
				players.addAll(Bukkit.getOnlinePlayers().stream().filter(pl -> ClansAPI.getInstance().isInClan(pl.getUniqueId()) && ClansAPI.getInstance().getAssociate(pl).get().getChat().equals(associate.getChat())).collect(Collectors.toSet()));
				players.add(p);
				ClanChatEvent e = ClanVentBus.queue(new ClanChatEvent(associate, players, event.getMessage())).get();
				if (!e.isCancelled()) {
					if (ClansAPI.getData().isTrue("Formatting.chat-spy-console")) {
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
