package com.github.sanctum.clans.util;

import com.github.sanctum.clans.model.Clan;
import org.bukkit.entity.EnderCrystal;

public class ReservoirMetadata {

	Clan.Associate associate;
	EnderCrystal crystal;

	public Clan.Associate getAssociateWhoSpawned() {
		return associate;
	}

	public EnderCrystal getEnderCrystal() {
		return crystal;
	}

	public void setCrystal(EnderCrystal crystal) {
		this.crystal = crystal;
	}

	public void setAssociate(Clan.Associate associate) {
		this.associate = associate;
	}
}
