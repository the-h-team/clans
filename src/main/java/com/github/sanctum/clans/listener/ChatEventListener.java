package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.model.ClanVentBus;
import com.github.sanctum.clans.model.Channel;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.util.ChatChannelBackend;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.event.associate.AssociateChatEvent;
import com.github.sanctum.clans.event.associate.AssociateMessageReceiveEvent;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.panther.event.Subscribe;
import com.github.sanctum.panther.event.Vent;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEventListener implements Listener {

	final ChatChannelBackend globalChat = new ChatChannelBackend(ClansAPI.getDataInstance().getConfig().getRoot().getNode("Formatting.Chat.Channel.global"));
	final ChatChannelBackend allyChat = new ChatChannelBackend(ClansAPI.getDataInstance().getConfig().getRoot().getNode("Formatting.Chat.Channel.ally"));
	final ChatChannelBackend clanChat = new ChatChannelBackend(ClansAPI.getDataInstance().getConfig().getRoot().getNode("Formatting.Chat.Channel.clan"));

	@Subscribe(priority = Vent.Priority.MEDIUM)
	public void onAllyChat(AssociateChatEvent e) {
		String message = e.getMessage();
		for (Channel.Filter f : e.getChannel().getFilters()) {
			message = f.run(message);
		}
		e.setMessage(message);
		Clan.Associate associate = e.getAssociate();
		String color = associate.getClan().getPalette().toString();
		String name = associate.getClan().getPalette().isGradient() ? associate.getClan().getPalette().toString(associate.getClan().getNickname() != null ? associate.getClan().getNickname() : associate.getClan().getName()) : color + (associate.getClan().getNickname() != null ? associate.getClan().getNickname() : associate.getClan().getName());
		if (e.getChannel().equals(Channel.ALLY)) {
			String finalMessage = message;
			allyChat.setFormat(s -> MessageFormat.format(StringUtils.use(s).laby(e.getPlayer()), associate.getNickname(), name, color, associate.getRankFull(), (associate.getClan().getNickname() != null ? associate.getClan().getNickname() : associate.getClan().getName()), finalMessage));
			e.setComponents(allyChat.toMessage().build());
			e.setPingSound(Sound.ENTITY_VILLAGER_YES);
		}
		if (e.getChannel().equals(Channel.CLAN)) {
			String finalMessage1 = message;
			clanChat.setFormat(s -> MessageFormat.format(StringUtils.use(s).laby(e.getPlayer()), associate.getNickname(), name, color, associate.getRankFull(), associate.getClan().getName(), finalMessage1));
			e.setComponents(clanChat.toMessage().build());
			e.setPingSound(Sound.ENTITY_VILLAGER_YES);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onFormat(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p.getName()).orElse(null);
		if (associate != null) {
			if (ClansAPI.getDataInstance().isTrue("Formatting.allow")) {
				if (associate.getChannel().equals(Channel.GLOBAL)) {
					Clan clan = associate.getClan();
					StringLibrary lib = Clan.ACTION;
					String clanName = clan.getPalette().isGradient() ? clan.getPalette().toString(clan.getName()) : clan.getPalette() + clan.getName();
					for (Channel.Filter f : Channel.GLOBAL.getFilters()) {
						event.setMessage(f.run(event.getMessage()));
					}
					String rank = lib.getRankStyle().equalsIgnoreCase("WORDLESS") ? associate.getRank().getSymbol() : associate.getRank().getName();
					if (ClansAPI.getDataInstance().isTrue("Formatting.Chat.standalone")) { // use own format
						if (!event.isCancelled()) {
							globalChat.setFormat(s -> MessageFormat.format(StringUtils.use(s).laby(p), rank, clanName, event.getMessage()));
							globalChat.toMessage().send(player -> true).queue();
							event.setCancelled(true);
						}
					} else { // add to format
						globalChat.setFormat(s -> MessageFormat.format(StringUtils.use(s).translate(p), rank, clanName));
						event.setFormat(globalChat.getDefault() + " " + event.getFormat());
					}
					return;
				}
			}

			if (associate.getChannel().equals(Channel.CLAN)) {
				Set<Player> players = associate.getClan().getMembers().stream().filter(m -> m.getTag().isPlayer() && m.getTag().getPlayer().isOnline()).map(associate1 -> associate1.getTag().getPlayer().getPlayer()).collect(Collectors.toSet());
				players.addAll(ClansAPI.getDataInstance().getSpies());
				AssociateChatEvent e = ClanVentBus.call(new AssociateChatEvent(associate, players, event.getMessage()));
				if (!e.isCancelled()) {
					if (ClansAPI.getDataInstance().isTrue("Formatting.chat-spy-console")) {
						ClansAPI.getInstance().getPlugin().getLogger().info("- [CLAN] " + e.getAssociate().getName() + " : " + e.getMessage());
					}
					for (Player toGet : e.getRecipients()) {
						Mailer.empty(toGet).chat(e.getComponents()).deploy();
						toGet.playSound(toGet.getLocation(), e.getPingSound(), 10, 1);
					}

					associate.getClan().getMembers().forEach(a -> ClanVentBus.call(new AssociateMessageReceiveEvent(a, associate, Channel.CLAN, e.getMessage())));
				}
				event.setCancelled(true);
				return;
			}
			if (associate.getChannel().equals(Channel.ALLY)) {
				Set<Player> players = new HashSet<>(ClansAPI.getDataInstance().getSpies());
				for (Clan c : associate.getClan().getRelation().getAlliance().get(Clan.class)) {
					players.addAll(c.getMembers().stream().filter(m -> m.getTag().isPlayer() && m.getTag().getPlayer().isOnline()).map(associate1 -> associate1.getTag().getPlayer().getPlayer()).collect(Collectors.toSet()));
				}
				players.addAll(associate.getClan().getMembers().stream().filter(m -> m.getTag().isPlayer() && m.getTag().getPlayer().isOnline()).map(associate1 -> associate1.getTag().getPlayer().getPlayer()).collect(Collectors.toSet()));
				AssociateChatEvent e = ClanVentBus.call(new AssociateChatEvent(associate, players, event.getMessage()));
				if (!e.isCancelled()) {
					if (ClansAPI.getDataInstance().isTrue("Formatting.chat-spy-console")) {
						ClansAPI.getInstance().getPlugin().getLogger().info("- [ALLY] " + e.getAssociate().getName() + " : " + e.getMessage());
					}
					for (Player toGet : e.getRecipients()) {
						Mailer.empty(toGet).chat(e.getComponents()).deploy();
						toGet.playSound(toGet.getLocation(), e.getPingSound(), 10, 1);
					}
					for (Clan c : associate.getClan().getRelation().getAlliance().get(Clan.class)) {
						c.getMembers().forEach(a -> ClanVentBus.call(new AssociateMessageReceiveEvent(a, associate, Channel.ALLY, e.getMessage())));
					}

					associate.getClan().getMembers().forEach(a -> ClanVentBus.call(new AssociateMessageReceiveEvent(a, associate, Channel.ALLY, e.getMessage())));
				}
				event.setCancelled(true);
				return;
			}
			if (!associate.getChannel().isDefault()) {
				Set<Player> players = new HashSet<>(ClansAPI.getDataInstance().getSpies());
				players.addAll(associate.getChannel().getAudience().stream().filter(associate1 -> associate1.getTag().isPlayer() && associate1.getTag().getPlayer().isOnline()).map(associate1 -> associate1.getTag().getPlayer().getPlayer()).collect(Collectors.toSet()));
				players.add(p);
				AssociateChatEvent e = ClanVentBus.call(new AssociateChatEvent(associate, players, event.getMessage()));
				if (!e.isCancelled()) {
					if (ClansAPI.getDataInstance().isTrue("Formatting.chat-spy-console")) {
						ClansAPI.getInstance().getPlugin().getLogger().info("- [" + associate.getChannel() + "] " + e.getAssociate().getName() + " : " + e.getMessage());
					}
					for (Player toGet : e.getRecipients()) {
						Mailer.empty(toGet).chat(e.getComponents()).deploy();
						toGet.playSound(toGet.getLocation(), e.getPingSound(), 10, 1);
					}

					associate.getChannel().getAudience().forEach(associate1 -> ClanVentBus.call(new AssociateMessageReceiveEvent(associate1, associate, associate.getChannel(), e.getMessage())));
				}
				event.setCancelled(true);
			}

		}
	}

}
