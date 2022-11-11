package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.construct.api.Clan;
import org.bukkit.entity.EnderCrystal;

public class EnderCrystalData {

	Clan.Associate associate;
	EnderCrystal crystal;

	public Clan.Associate getAssociate() {
		return associate;
	}

	public EnderCrystal getCrystal() {
		return crystal;
	}

	public void setCrystal(EnderCrystal crystal) {
		this.crystal = crystal;
	}

	public void setAssociate(Clan.Associate associate) {
		this.associate = associate;
	}
}
