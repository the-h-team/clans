package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.GUI;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import java.util.List;

import com.github.sanctum.labyrinth.library.Item;
import com.github.sanctum.labyrinth.library.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class CommandReservoir extends ClanSubCommand {
	public CommandReservoir() {
		super("reservoir");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (associate != null) {
				GUI.RESERVOIR.get(associate.getClan()).open(p);
			} else {
				sendMessage(p, lib.notInClan());
			}
			return true;
		}

		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("buy")) {
				ItemStack item = new Item.Edit(Material.END_CRYSTAL).setTitle("&3&lRESERVOIR").editContainer(lc -> {
					lc.set(new NamespacedKey(ClansAPI.getInstance().getPlugin(), "clans_reservoir"), PersistentDataType.STRING, associate.getClan().getId().toString());
				}).build();
				p.getWorld().dropItem(p.getLocation(), item);
				return true;
			}
		}
		return true;
	}

	@Override
	public List<String> tab(Player p, String label, String[] args) {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, getBaseCompletion(args))
				.get();
	}
}
