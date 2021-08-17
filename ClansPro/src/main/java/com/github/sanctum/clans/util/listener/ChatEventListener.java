package com.github.sanctum.clans.util.listener;

import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.events.clans.ClanChatEvent;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.link.ClanVentBus;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
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
			ClanAssociate associate = e.getAssociate();
			e.setPrefix(StringUtils.use(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.ally.prefix")).replaceIgnoreCase("{PLAYER}", associate.getNickname()));
			e.setDivider(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.ally.divider"));
			e.setHighlight(StringUtils.use(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.ally.highlight")).replaceIgnoreCase("{CLAN}", associate.getClan().getName()));
			e.setHoverMeta(StringUtils.use(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.ally.hover")).replaceIgnoreCase("{RANK}", associate.getRankTag()));
			e.setPingSound(Sound.ENTITY_VILLAGER_YES);
		}
	}

	@Subscribe(priority = Vent.Priority.MEDIUM)
	public void onClanChat(ClanChatEvent e) {
		if (e.getChannel().equalsIgnoreCase("CLAN")) {
			ClanAssociate associate = e.getAssociate();
			e.setPrefix(StringUtils.use(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.clan.prefix")).replaceIgnoreCase("{PLAYER}", associate.getNickname()));
			e.setDivider(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.clan.divider"));
			e.setHighlight(StringUtils.use(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.clan.highlight")).replaceIgnoreCase("{CLAN}", associate.getClan().getName()));
			e.setHoverMeta(StringUtils.use(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.clan.hover")).replaceIgnoreCase("{RANK}", associate.getRankTag()));
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
				ClanChatEvent e = ClanVentBus.queue(new ClanChatEvent(associate, associate.getClan().getMembers().stream().filter(m -> m.getPlayer().isOnline()).map(ClanAssociate::getPlayer).map(OfflinePlayer::getPlayer).collect(Collectors.toSet()), event.getMessage())).get();
				if (!e.isCancelled()) {
					if (ClansAPI.getData().getEnabled("Formatting.chat-spy-console")) {
						ClansAPI.getInstance().getPlugin().getLogger().info("- [CLAN] " + e.getAssociate().getName() + " : " + e.getMessage());
					}
					for (Player toGet : e.getRecipients()) {
						e.getUtil().sendComponent(toGet, TextLib.getInstance().textHoverable(MessageFormat.format(e.getPrefix(), e.getAssociate().getName(), associate.getClan().getName(), associate.getClan().getColor(), associate.getRankTag()), StringUtils.use(MessageFormat.format(e.getHighlight(), e.getAssociate().getName(), associate.getClan().getName(), associate.getClan().getColor(), associate.getRankTag())).papi(e.getAssociate().getPlayer()), e.getDivider() + e.getMessage(), StringUtils.use(MessageFormat.format(e.getHoverMeta(), e.getAssociate().getName(), associate.getClan().getName(), associate.getClan().getColor(), associate.getRankTag())).papi(e.getAssociate().getPlayer())));
						toGet.playSound(toGet.getLocation(), e.getPingSound(), 10, 1);
					}
				}
				event.setCancelled(true);
				return;
			}
			if (associate.getChat().equalsIgnoreCase("ALLY")) {
				ClanChatEvent e = ClanVentBus.queue(new ClanChatEvent(associate, associate.getClan().getMembers().stream().filter(m -> m.getPlayer().isOnline()).map(ClanAssociate::getPlayer).map(OfflinePlayer::getPlayer).collect(Collectors.toSet()), event.getMessage())).get();
				if (!e.isCancelled()) {
					if (ClansAPI.getData().getEnabled("Formatting.chat-spy-console")) {
						ClansAPI.getInstance().getPlugin().getLogger().info("- [ALLY] " + e.getAssociate().getName() + " : " + e.getMessage());
					}
					for (Player toGet : e.getRecipients()) {
						e.getUtil().sendComponent(toGet, TextLib.getInstance().textHoverable(MessageFormat.format(e.getPrefix(), e.getAssociate().getName(), associate.getClan().getName(), associate.getClan().getColor(), associate.getRankTag()), StringUtils.use(MessageFormat.format(e.getHighlight(), e.getAssociate().getName(), associate.getClan().getName(), associate.getClan().getColor(), associate.getRankTag())).papi(e.getAssociate().getPlayer()), e.getDivider() + e.getMessage(), StringUtils.use(MessageFormat.format(e.getHoverMeta(), e.getAssociate().getName(), associate.getClan().getName(), associate.getClan().getColor(), associate.getRankTag())).papi(e.getAssociate().getPlayer())));
						toGet.playSound(toGet.getLocation(), e.getPingSound(), 10, 1);
					}
				}
				event.setCancelled(true);
				return;
			}
			List<String> defaults = new ArrayList<>(Arrays.asList("GLOBAL", "CLAN", "ALLY"));
			if (!defaults.contains(associate.getChat())) {
				ClanChatEvent e = ClanVentBus.queue(new ClanChatEvent(associate, associate.getClan().getMembers().stream().filter(m -> m.getPlayer().isOnline()).map(ClanAssociate::getPlayer).map(OfflinePlayer::getPlayer).collect(Collectors.toSet()), event.getMessage())).get();
				if (!e.isCancelled()) {
					if (ClansAPI.getData().getEnabled("Formatting.chat-spy-console")) {
						ClansAPI.getInstance().getPlugin().getLogger().info("- [" + associate.getChat() + "] " + e.getAssociate().getName() + " : " + e.getMessage());
					}
					for (Player toGet : e.getRecipients()) {
						e.getUtil().sendComponent(toGet, TextLib.getInstance().textHoverable(MessageFormat.format(e.getPrefix(), e.getAssociate().getName(), associate.getClan().getName(), associate.getClan().getColor(), associate.getRankTag()), StringUtils.use(MessageFormat.format(e.getHighlight(), e.getAssociate().getName(), associate.getClan().getName(), associate.getClan().getColor(), associate.getRankTag())).papi(e.getAssociate().getPlayer()), e.getDivider() + e.getMessage(), StringUtils.use(MessageFormat.format(e.getHoverMeta(), e.getAssociate().getName(), associate.getClan().getName(), associate.getClan().getColor(), associate.getRankTag())).papi(e.getAssociate().getPlayer())));
						toGet.playSound(toGet.getLocation(), e.getPingSound(), 10, 1);
					}
				}
				event.setCancelled(true);
			}

		}
	}

}
