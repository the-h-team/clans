package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.GUI;
import com.github.sanctum.clans.construct.api.QnA;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import com.github.sanctum.labyrinth.gui.unity.impl.MenuType;
import com.github.sanctum.labyrinth.library.Deployable;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.skulls.SkullType;
import java.util.List;
import org.bukkit.entity.Player;

public class CommandAsk extends ClanSubCommand {
	public CommandAsk() {
		super("ask");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;

		if (args.length == 0) {
			ClansAPI api = ClansAPI.getInstance();
			MenuType.PRINTABLE.build()
					.setTitle("&6&lAsk Question")
					.setSize(Menu.Rows.ONE)
					.setHost(api.getPlugin())
					.setStock(inventoryElement -> inventoryElement.addItem(b -> b.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
						c.setCancelled(true);
						c.setHotbarAllowed(false);
					}))).join()
					.addAction(clickElement -> {
						clickElement.setCancelled(true);
						clickElement.setHotbarAllowed(false);
						if (clickElement.getSlot() == 2) {
							for (QnA qnA : QnA.getAll()) {
								if (!qnA.test(clickElement.getElement(), clickElement.getParent().getName()))
									return;
							}
							Deployable.of(clickElement.getElement(), player -> {
								player.closeInventory();
								String response = "&cI don't have an answer.";
								if (StringUtils.use(clickElement.getParent().getName()).containsIgnoreCase("nigger", "fuck", "dumb", "retard", "fag")) {
									response = "&cThat's not very nice.";
								}
								Mailer.empty(player).title(api.getPrefix().joined(), response).deploy(mailer -> {
									TaskScheduler.of(() -> GUI.MAIN_MENU.get(player).open(player)).scheduleLater(75);
								});
							}).deploy();
						}

					}).open(p);
			return true;
		}

		return true;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return null;
	}
}
