package com.github.sanctum.mail.controller;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.mail.GiftBox;
import com.github.sanctum.mail.MailBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MailListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		Message msg = new Message(p, "");
		Schedule.sync(() -> {
			if (ClansAPI.getInstance().getAssociate(p.getName()).isPresent()) {
				Clan c = ClansAPI.getInstance().getAssociate(p.getName()).get().getClan();
				if (MailBox.getMailBox(c) == null) {
					new MailBox(c);
				}
				p.sendMessage(" ");
				if (MailBox.getMailBox(c).getMailList().isEmpty()) {
					Clan.ACTION.sendMessage(p, "&e&oYour clan has &f0 &e&ounread mail.");
				} else {
					Clan.ACTION.sendMessage(p, "&6&oYour clan has &a" + MailBox.getMailBox(c).getMailList().size() + " &6&ounread mail.");
					Clan.ACTION.sendMessage(p, "&e&oUse &6/clan mail view &e&oto see who from.");
				}
				p.sendMessage(" ");
				if (GiftBox.getGiftBox(c) == null) {
					new GiftBox(c);
				}
				if (GiftBox.getGiftBox(c).getSenders().isEmpty()) {
					msg.send("&b&o0 &3&onew gifts were accounted for.");
				} else {
					msg.send("&b&oYour clan has &3" + GiftBox.getGiftBox(c).getSenders().size() + " &b&oun-opened gifts.");
					msg.send("&3&oUse &b/clan gifts &3&oto see who from.");
				}
				p.sendMessage(" ");
			}
		}).debug().wait(4 * 20);
	}

}
