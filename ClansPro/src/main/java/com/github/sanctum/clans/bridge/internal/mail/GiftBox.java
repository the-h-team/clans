package com.github.sanctum.clans.bridge.internal.mail;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.library.HUID;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;

public class GiftBox implements Serializable {

	private final int maxSize = 50;

	private final String id;

	private final Collection<GiftObject> inbox = new HashSet<>();

	public GiftBox(Clan c) {
		this.id = c.getId().toString();
		saveChanges();
	}

	public Clan getClan() {
		return ClansAPI.getInstance().getClanManager().getClan(HUID.fromString(this.id));
	}

	public int getMaxSize() {
		return maxSize;
	}

	public Collection<GiftObject> get() {
		return inbox;
	}

	public boolean receive(GiftObject mail) {
		if (inbox.size() <= maxSize) {
			if (inbox.stream().noneMatch(m -> m.equals(mail))) {
				inbox.add(mail);
				saveChanges();
				return true;
			}
		}
		return false;
	}

	public void mark(GiftObject mail) {
		inbox.removeIf(m -> m.equals(mail));
		saveChanges();
	}

	private void saveChanges() {
		getClan().setValue("gift-box", this, false);
	}

	public List<String> getSenders() {
		return get().stream().map(GiftObject::getSender).collect(Collectors.toList());
	}

	public Collection<GiftObject> getInboxByClan(String clanName) {
		return get().stream().filter(m -> m.getSender().equals(clanName)).collect(Collectors.toList());
	}

	public GiftObject getGiftByMaterial(Material item) {
		return get().stream().filter(m -> m.getItem().getType().equals(item)).findFirst().orElse(null);
	}

	public static GiftBox getGiftBox(Clan c) {
		return c.getValue(GiftBox.class, "gift-box");
	}

}
