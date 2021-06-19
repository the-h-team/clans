package com.github.sanctum.mail.controller;

import com.github.sanctum.clans.construct.DefaultClan;
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
			if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
				if (MailBox.getMailBox(ClansAPI.getInstance().getClan(p.getUniqueId())) == null) {
					new MailBox(ClansAPI.getInstance().getClan(p.getUniqueId()));
				}
				p.sendMessage(" ");
				if (MailBox.getMailBox(ClansAPI.getInstance().getClan(p.getUniqueId())).getMailList().isEmpty()) {
					DefaultClan.action.sendMessage(p, "&e&oYour clan has &f0 &e&ounread mail.");
				} else {
					DefaultClan.action.sendMessage(p, "&6&oYour clan has &a" + MailBox.getMailBox(ClansAPI.getInstance().getClan(p.getUniqueId())).getMailList().size() + " &6&ounread mail.");
					DefaultClan.action.sendMessage(p, "&e&oUse &6/clan mail view &e&oto see who from.");
				}
				p.sendMessage(" ");
				if (GiftBox.getGiftBox(ClansAPI.getInstance().getClan(p.getUniqueId())) == null) {
					new GiftBox(ClansAPI.getInstance().getClan(p.getUniqueId()));
				}
				if (GiftBox.getGiftBox(ClansAPI.getInstance().getClan(p.getUniqueId())).getSenders().isEmpty()) {
					msg.send("&b&o0 &3&onew gifts were accounted for.");
				} else {
					msg.send("&b&oYour clan has &3" + GiftBox.getGiftBox(ClansAPI.getInstance().getClan(p.getUniqueId())).getSenders().size() + " &b&oun-opened gifts.");
					msg.send("&3&oUse &b/clan gifts &3&oto see who from.");
				}
				p.sendMessage(" ");
			}
		}).debug().wait(4 * 20);
	}

}
