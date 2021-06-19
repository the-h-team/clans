package com.github.sanctum.clans.util.listener;

import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.events.clans.ChatChannelAllyEvent;
import com.github.sanctum.clans.util.events.clans.ChatChannelClanEvent;
import com.github.sanctum.clans.util.events.clans.ChatChannelOtherEvent;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.link.ClanVentBus;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEventListener implements Listener {

	private String chatMode(Player p) {
		return ClansPro.getInstance().dataManager.CHAT_MODE.get(p);
	}

	public ChatEventListener() {

		ClanVentBus.subscribeAsync(ChatChannelAllyEvent.class, Vent.Priority.MEDIUM, (e, subscription) -> {
			ClanAssociate associate = ClansAPI.getInstance().getAssociate(e.getChatting()).orElse(null);
			if (associate != null) {
				e.setPrefix(StringUtils.use(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.ally.prefix")).replaceIgnoreCase("{PLAYER}", associate.getNickname()));
				e.setDivider(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.ally.divider"));
				e.setHighlight(StringUtils.use(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.ally.highlight")).replaceIgnoreCase("{CLAN}", e.getClan().getName()));
				e.setHoverMeta(StringUtils.use(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.ally.hover")).replaceIgnoreCase("{RANK}", associate.getRankTag()));
				e.setPingSound(Sound.ENTITY_VILLAGER_YES);
			}

		});

		ClanVentBus.subscribeAsync(ChatChannelClanEvent.class, Vent.Priority.MEDIUM, (e, subscription) -> {
			ClanAssociate associate = ClansAPI.getInstance().getAssociate(e.getChatting()).orElse(null);
			if (associate != null) {
				e.setPrefix(StringUtils.use(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.clan.prefix")).replaceIgnoreCase("{PLAYER}", associate.getNickname()));
				e.setDivider(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.clan.divider"));
				e.setHighlight(StringUtils.use(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.clan.highlight")).replaceIgnoreCase("{CLAN}", e.getClan().getName()));
				e.setHoverMeta(StringUtils.use(ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Channel.clan.hover")).replaceIgnoreCase("{RANK}", associate.getRankTag()));
			}
		});

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPrefixApply(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();
		if (chatMode(p).equals("GLOBAL")) {
			if (ClansAPI.getData().getEnabled("Formatting.allow")) {
				ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
				if (associate != null) {
					Clan clan = associate.getClan();
					String clanName = clan.getName();
					StringLibrary lib = new StringLibrary();
					String rank;
					clanName = clan.getColor() + clanName;
					switch (lib.getRankStyle().toUpperCase()) {
						case "WORDLESS":
							rank = lib.getWordlessStyle(associate.getRank());

							event.setFormat(lib.color(MessageFormat.format(lib.getChatFormat(), rank, clanName) + " " + event.getFormat()));
							break;
						case "FULL":
							rank = lib.getFullStyle(associate.getRank());
							event.setFormat(lib.color(MessageFormat.format(lib.getChatFormat(), rank, clanName) + " " + event.getFormat()));
							break;
					}
				}
			}
			return;
		}
		if (chatMode(p).equals("CLAN")) {
			ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElseThrow(() -> {
				ClansPro.getInstance().dataManager.CHAT_MODE.put(p, "GLOBAL");
				return new IllegalStateException("(CLAN) Associate cannot be null, this is a warning. Re-adjusting chat channel.");
			});
			ChatChannelClanEvent e = ClanVentBus.queue(new ChatChannelClanEvent(p, event.getRecipients(), event.getMessage())).join();
			if (!e.isCancelled()) {
				if (ClansAPI.getData().getEnabled("Formatting.chat-spy-console")) {
					ClansPro.getInstance().getLogger().info("- [CLAN] " + e.getChatting().getName() + " : " + e.getMessage());
				}
				for (Player toGet : e.getReceivers()) {
					e.getUtil().sendComponent(toGet, TextLib.getInstance().textHoverable(MessageFormat.format(e.getPrefix(), e.getChatting().getName(), associate.getClan().getName(), associate.getClan().getColor(), associate.getRankTag()), StringUtils.use(MessageFormat.format(e.getHighlight(), e.getChatting().getName(), associate.getClan().getName(), associate.getClan().getColor(), associate.getRankTag())).papi(e.getChatting()), e.getDivider() + e.getMessage(), StringUtils.use(MessageFormat.format(e.getHoverMeta(), e.getChatting().getName(), associate.getClan().getName(), associate.getClan().getColor(), associate.getRankTag())).papi(e.getChatting())));
					toGet.playSound(toGet.getLocation(), e.getPingSound(), 10, 1);
				}
			}
			event.setCancelled(true);
			return;
		}
		if (chatMode(p).equals("ALLY")) {
			ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElseThrow(() -> {
				ClansPro.getInstance().dataManager.CHAT_MODE.put(p, "GLOBAL");
				return new IllegalStateException("(ALLY) Associate cannot be null, this is a warning. Re-adjusting chat channel.");
			});
			ChatChannelAllyEvent e = ClanVentBus.queue(new ChatChannelAllyEvent(p, event.getRecipients(), event.getMessage())).join();
			if (!e.isCancelled()) {
				if (ClansAPI.getData().getEnabled("Formatting.chat-spy-console")) {
					ClansPro.getInstance().getLogger().info("- [ALLY] " + e.getChatting().getName() + " : " + e.getMessage());
				}
				for (Player toGet : e.getReceivers()) {
					e.getUtil().sendComponent(toGet, TextLib.getInstance().textHoverable(MessageFormat.format(e.getPrefix(), e.getChatting().getName(), associate.getClan().getName(), associate.getClan().getColor(), associate.getRankTag()), StringUtils.use(MessageFormat.format(e.getHighlight(), e.getChatting().getName(), associate.getClan().getName(), associate.getClan().getColor(), associate.getRankTag())).papi(e.getChatting()), e.getDivider() + e.getMessage(), StringUtils.use(MessageFormat.format(e.getHoverMeta(), e.getChatting().getName(), associate.getClan().getName(), associate.getClan().getColor(), associate.getRankTag())).papi(e.getChatting())));
					toGet.playSound(toGet.getLocation(), e.getPingSound(), 10, 1);
				}
			}
			event.setCancelled(true);
		}
		List<String> defaults = new ArrayList<>(Arrays.asList("GLOBAL", "CLAN", "ALLY"));
		if (!defaults.contains(chatMode(p))) {
			// new event
			ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElseThrow(() -> {
				ClansPro.getInstance().dataManager.CHAT_MODE.put(p, "GLOBAL");
				return new IllegalStateException("(CUSTOM) Associate cannot be null, this is a warning. Re-adjusting chat channel.");
			});
			ChatChannelOtherEvent e = ClanVentBus.queue(new ChatChannelOtherEvent(p, event.getRecipients(), event.getMessage())).join();
			if (!e.isCancelled()) {
				if (ClansAPI.getData().getEnabled("Formatting.chat-spy-console")) {
					ClansPro.getInstance().getLogger().info("- [CUSTOM] " + e.getChatting().getName() + " : " + e.getMessage());
				}
				for (Player toGet : e.getReceivers()) {
					e.getUtil().sendComponent(toGet, TextLib.getInstance().textHoverable(MessageFormat.format(e.getPrefix(), associate.getNickname()), StringUtils.use(MessageFormat.format(e.getHighlight(), associate.getClan().getColor(), associate.getClan().getName())).papi(e.getChatting()), e.getDivider() + e.getMessage(), StringUtils.use(MessageFormat.format(e.getHoverMeta(), associate.getRankTag(), associate.getNickname(), p.getName())).papi(e.getChatting())));
					toGet.playSound(toGet.getLocation(), e.getPingSound(), 10, 1);
				}
			}
			event.setCancelled(true);
		}
	}

}
