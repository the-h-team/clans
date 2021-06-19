package com.github.sanctum.mail;

import java.io.Serializable;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

public class GiftObject implements Serializable {

	private final String sender;

	private final ItemStack item;

	public GiftObject(String sender, ItemStack item) {
		this.sender = sender;
		this.item = new ItemStack(item);
	}

	public String getSender() {
		return sender;
	}

	public ItemStack getItem() {
		return item;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GiftObject)) return false;
		GiftObject that = (GiftObject) o;
		return getSender().equals(that.getSender()) &&
				getItem().equals(that.getItem());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSender(), getItem());
	}
}
