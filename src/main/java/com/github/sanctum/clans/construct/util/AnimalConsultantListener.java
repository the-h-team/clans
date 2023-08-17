package com.github.sanctum.clans.construct.util;

import com.github.sanctum.clans.construct.api.Channel;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.IncomingConsultationListener;
import com.github.sanctum.clans.construct.api.OutgoingConsultationListener;
import com.github.sanctum.clans.construct.api.Ticket;
import com.github.sanctum.clans.construct.impl.DefaultMapEntry;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public final class AnimalConsultantListener implements IncomingConsultationListener, OutgoingConsultationListener {

	private final Clan.Associate tag;
	private final Set<String> recents = new HashSet<>();

	public AnimalConsultantListener(Clan.Associate tag) {
		this.tag = tag;
	}

	@Override
	public @NotNull Ticket onReceiveMessage(@NotNull Object object) {
		Ticket ticket = new Ticket();
		if (object instanceof DefaultMapEntry) {
			DefaultMapEntry<String, DefaultMapEntry<Channel, Clan.Associate>> entry = (DefaultMapEntry<String, DefaultMapEntry<Channel, Clan.Associate>>) object;
			StringUtils utils = StringUtils.use(entry.getKey());
			String user = entry.getValue().getValue().getName();
			if ((utils.containsIgnoreCase(tag.getName(), tag.getNickname()) || utils.containsIgnoreCase(tag.getName().replace("'", ""), tag.getNickname())) && utils.containsIgnoreCase("welcome")) {
				if (!recents.contains(user)) {
					String response;
					if (new Random().nextInt(2) == 1) {
						response = "You talking to me? " + user;
					} else {
						response = "Thank you " + user + "!";
					}

					ticket.setType(Ticket.Field.STRING, response);
					ticket.setType(Ticket.Field.CUSTOM, entry.getValue());
					recents.add(user);
					TaskScheduler.of(() -> recents.remove(user)).scheduleLater(40);
				}
				return ticket;
			}
			if ((utils.containsIgnoreCase(tag.getName(), tag.getNickname()) || utils.containsIgnoreCase(tag.getName().replace("'", ""), tag.getNickname())) && utils.containsIgnoreCase("hi", "hello", "how are")) {
				if (!recents.contains(user)) {
					String response;
					if (new Random().nextInt(2) == 1) {
						response = "Hello to you to";
					} else {
						response = "Im feeling great!";
					}

					ticket.setType(Ticket.Field.STRING, response);
					ticket.setType(Ticket.Field.CUSTOM, entry.getValue());
					recents.add(user);
					TaskScheduler.of(() -> recents.remove(user)).scheduleLater(40);
				}
				return ticket;
			}
			if ((utils.containsIgnoreCase(tag.getName(), tag.getNickname()) || utils.containsIgnoreCase(tag.getName().replace("'", ""), tag.getNickname())) && utils.containsIgnoreCase("where", "where are", "location", "locate")) {
				if (!recents.contains(user)) {
					Location loc = tag.getAsEntity().getLocation();
					String response = "I am currently @ X: " + loc.getX() + " Y: " + loc.getY() + " Z: " + loc.getZ() + " in world " + loc.getWorld().getName();
					ticket.setType(Ticket.Field.STRING, response);
					ticket.setType(Ticket.Field.CUSTOM, entry.getValue());
					recents.add(user);
					TaskScheduler.of(() -> recents.remove(user)).scheduleLater(40);
				}
				return ticket;
			}
			if ((utils.containsIgnoreCase(tag.getName(), tag.getNickname()) || utils.containsIgnoreCase(tag.getName().replace("'", ""), tag.getNickname()))) {
				if (!recents.contains(user)) {
					ticket.setType(Ticket.Field.STRING, "*" + tag.getAsEntity().getName() + " noises*");
					ticket.setType(Ticket.Field.CUSTOM, entry.getValue());
					recents.add(user);
					TaskScheduler.of(() -> recents.remove(user)).scheduleLater(40);
					return ticket;
				}
			}
		}
		return ticket;
	}

	@Override
	public void onReceiveResponse(@NotNull Ticket response) {
		if (response.isEmpty()) return;
		String r = response.get(Ticket.Field.STRING).toString();
		DefaultMapEntry<Channel, Clan.Associate> chan = (DefaultMapEntry<Channel, Clan.Associate>) response.get(Ticket.Field.CUSTOM);
		Message ar = chan.getKey().tryFormat(tag);
		for (Message.Chunk ch : ar) {
			ch.replace("%MESSAGE%", r);
		}
		for (Clan.Associate a : tag.getClan().getMembers()) {
			if (a.getTag().isPlayer() && a.getTag().getPlayer().isOnline()) {
				if (a.getChannel().getId().equals(chan.getKey().getId())) {
					Mailer.empty(a.getTag().getPlayer().getPlayer()).chat(ar.build()).queue(TimeUnit.SECONDS.toMillis(Math.max(1, new Random().nextInt(2))));
				}
			}
		}
	}

}
