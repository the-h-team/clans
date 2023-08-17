package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.api.Channel;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.util.StringLibrary;
import com.github.sanctum.clans.event.associate.AssociateChatEvent;
import com.github.sanctum.clans.event.associate.AssociateMessageReceiveEvent;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.event.Subscribe;
import com.github.sanctum.panther.event.Vent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEventListener implements Listener {


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
			List<BaseComponent> list = new ArrayList<>();
			list.add(TextLib.getInstance().textHoverable(MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Channel.ally.prefix"), associate.getNickname(), name, color, associate.getRankFull(), (associate.getClan().getNickname() != null ? associate.getClan().getNickname() : associate.getClan().getName())), MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Channel.ally.highlight"), associate.getNickname(), name, color, associate.getRankFull(), (associate.getClan().getNickname() != null ? associate.getClan().getNickname() : associate.getClan().getName())), MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Channel.ally.divider") + message, associate.getNickname(), name, color, associate.getRankFull(), (associate.getClan().getNickname() != null ? associate.getClan().getNickname() : associate.getClan().getName())), MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Channel.ally.hover"), associate.getNickname(), name, color, associate.getRankFull(), (associate.getClan().getNickname() != null ? associate.getClan().getNickname() : associate.getClan().getName()))));
			e.setPingSound(Sound.ENTITY_VILLAGER_YES);
			e.setComponents(list.toArray(new BaseComponent[0]));
		}
		if (e.getChannel().equals(Channel.CLAN)) {
			List<BaseComponent> list = new ArrayList<>();
			list.add(TextLib.getInstance().textHoverable(MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Channel.clan.prefix"), associate.getNickname(), name, color, associate.getRankFull(), associate.getClan().getName()), MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Channel.clan.highlight"), associate.getNickname(), name, color, associate.getRankFull(), associate.getClan().getName()), MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Channel.clan.divider") + message, associate.getNickname(), name, color, associate.getRankFull(), associate.getClan().getName()), MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Channel.clan.hover"), associate.getNickname(), name, color, associate.getRankFull(), associate.getClan().getName())));
			e.setPingSound(Sound.ENTITY_VILLAGER_YES);
			e.setComponents(list.toArray(new BaseComponent[0]));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onFormat(AsyncPlayerChatEvent event) throws ExecutionException, InterruptedException {
		Player p = event.getPlayer();
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p.getName()).orElse(null);
		if (associate != null) {
			if (ClansAPI.getDataInstance().isTrue("Formatting.allow")) {
				if (associate.getChannel().equals(Channel.GLOBAL)) {
					Clan clan = associate.getClan();
					StringLibrary lib = Clan.ACTION;
					String rank;
					String clanName = clan.getPalette().isGradient() ? clan.getPalette().toString(clan.getName()) : clan.getPalette() + clan.getName();
					for (Channel.Filter f : Channel.GLOBAL.getFilters()) {
						event.setMessage(f.run(event.getMessage()));
					}
					switch (lib.getRankStyle().toUpperCase()) {
						case "WORDLESS":
							rank = associate.getRank().getSymbol();
							if (ClansAPI.getDataInstance().isTrue("Formatting.Chat.standalone")) {
								event.setFormat(lib.color(MessageFormat.format(StringUtils.use(lib.getChatFormat()).translate(p), rank, clanName, event.getMessage())));
							} else {
								event.setFormat(lib.color(MessageFormat.format(StringUtils.use(lib.getChatFormat()).translate(p), rank, clanName) + " " + event.getFormat()));
							}
							break;
						case "FULL":
							rank = associate.getRank().getName();
							if (ClansAPI.getDataInstance().isTrue("Formatting.Chat.standalone")) {
								event.setFormat(lib.color(MessageFormat.format(StringUtils.use(lib.getChatFormat()).translate(p), rank, clanName, event.getMessage())));
							} else {
								event.setFormat(lib.color(MessageFormat.format(StringUtils.use(lib.getChatFormat()).translate(p), rank, clanName) + " " + event.getFormat()));
							}
							break;
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
