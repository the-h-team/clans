package com.github.sanctum.clans.model.addon.traits.structure;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.addon.TraitsAddon;
import com.github.sanctum.clans.model.Savable;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.interfacing.Nameable;
import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.file.Node;
import java.util.Arrays;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TraitHolder implements Nameable, Savable {

	final String name;
	final UUID id;
	Trait primary;
	Trait secondary;

	public TraitHolder(@NotNull OfflinePlayer player) {
		this.name = player.getName();
		this.id = player.getUniqueId();
		Clan.Addon addon = Clan.Addon.getAddon(TraitsAddon.class);
		if (addon != null) {
			FileManager file = addon.getFile(Configurable.Type.JSON, "holders");
			Node user = file.getRoot().getNode(id.toString());
			if (user.exists()) {
				String prim = user.getNode("primary").toPrimitive().getString();
				Arrays.stream(DefaultTrait.values()).filter(d -> d.getName().equals(prim)).findFirst().ifPresent(this::setPrimary);
				Trait p = primary;
				if (p != null) {
					for (String ab : user.getNode("primary-abilities").getKeys(false)) {
						Trait.Ability test = AbilityPool.valueOf(ab);
						if (test != null) {
							test.setLevel(user.getNode("primary-abilities").getNode(ab).toPrimitive().getInt());
							p.add(test);
						}
					}
				}
				String sec = user.getNode("secondary").toPrimitive().getString();
				if (sec != null) {
					Arrays.stream(DefaultTrait.values()).filter(d -> d.getName().equals(sec)).findFirst().ifPresent(this::setSecondary);
					Trait s = secondary;
					if (s != null) {
						for (String ab : user.getNode("secondary-abilities").getKeys(false)) {
							Trait.Ability test = AbilityPool.valueOf(ab);
							if (test != null) {
								test.setLevel(user.getNode("secondary-abilities").getNode(ab).toPrimitive().getInt());
								s.add(test);
							}
						}
					}
				}
			}
		}
		if (primary == null) primary = DefaultTrait.NOVICE;
	}

	@Override
	public @NotNull String getName() {
		return name;
	}

	public @NotNull UUID getId() {
		return id;
	}

	public @NotNull Trait getPrimary() {
		return primary;
	}

	public @Nullable Trait getSecondary() {
		return secondary;
	}

	public void setPrimary(Trait primary) {
		if (!(primary instanceof UserTrait)) {
			this.primary = primary.toUserTrait();
		} else this.primary = primary;
	}

	public void setSecondary(Trait secondary) {
		if (!(secondary instanceof  UserTrait)) {
			this.secondary = secondary.toUserTrait();
		} else this.secondary = secondary;
	}

	@Override
	public void save() {
		Clan.Addon addon = Clan.Addon.getAddon(TraitsAddon.class);
		if (addon != null) {
			FileManager file = addon.getFile(Configurable.Type.JSON, "holders");
			Node user = file.getRoot().getNode(id.toString());
			user.getNode("primary").set(primary.getName());
			user.getNode("primary-abilities").set(null);
			for (Trait.Ability a : primary.getAbilities()) {
				user.getNode("primary-abilities").getNode(a.getName()).set(a.getLevel());
			}
			if (secondary != null) {
				user.getNode("secondary").set(secondary.getName());
				user.getNode("secondary-abilities").set(null);
				for (Trait.Ability a : secondary.getAbilities()) {
					user.getNode("secondary-abilities").getNode(a.getName()).set(a.getLevel());
				}
			}
			file.getRoot().save();
		}
	}

	@Override
	public void remove() {

	}
}
