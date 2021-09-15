package com.github.sanctum.clans.bridge.internal.mail.controller;

import com.github.sanctum.clans.bridge.internal.mail.GiftBox;
import com.github.sanctum.clans.bridge.internal.mail.MailBox;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.task.Schedule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MailListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		Message msg = Message.form(p).setPrefix(" ");
		Schedule.sync(() -> {
			if (ClansAPI.getInstance().getAssociate(p.getName()).isPresent()) {
				Clan c = ClansAPI.getInstance().getAssociate(p.getName()).get().getClan();
				MailBox box;
				try {
					box = MailBox.getMailBox(c);
					if (box == null) {
						box = new MailBox(c);
					}
				} catch (Exception ig) {
					ClansAPI.getInstance().getPlugin().getLogger().warning("- Don't be alarmed, issue now resolved.");
					box = new MailBox(c);
					c.removeValue("mail-box");
				}
				p.sendMessage(" ");
				if (box.getMailList().isEmpty()) {
					Clan.ACTION.sendMessage(p, "&e&oYour clan has &f0 &e&ounread mail.");
				} else {
					Clan.ACTION.sendMessage(p, "&6&oYour clan has &a" + box.getMailList().size() + " &6&ounread mail.");
					Clan.ACTION.sendMessage(p, "&e&oUse &6/clan mail view &e&oto see who from.");
				}
				p.sendMessage(" ");
				GiftBox gifts;
				try {
					gifts = GiftBox.getGiftBox(c);
					if (gifts == null) {
						gifts = new GiftBox(c);
					}
				} catch (Exception ig) {
					ClansAPI.getInstance().getPlugin().getLogger().warning("- Don't be alarmed, issue now resolved.");
					gifts = new GiftBox(c);
					c.removeValue("gift-box");
				}
				if (gifts.getSenders().isEmpty()) {
					msg.send("&b&o0 &3&onew gifts were accounted for.");
				} else {
					msg.send("&b&oYour clan has &3" + gifts.getSenders().size() + " &b&oun-opened gifts.");
					msg.send("&3&oUse &b/clan gifts &3&oto see who from.");
				}
				p.sendMessage(" ");
			}
		}).debug().wait(4 * 20);
	}

}
