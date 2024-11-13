package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.GUI;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.service.PlayerSearch;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.skulls.CustomHead;
import com.github.sanctum.skulls.CustomHeadLoader;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CommandHead extends ClanSubCommand {
	public CommandHead() {
		super("head");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		boolean chargeToUpload = ClansAPI.getDataInstance().isTrue("Clans.heads.charge-to-upload");
		boolean chargeToGet = ClansAPI.getDataInstance().isTrue("Clans.heads.charge-to-get");
		double costToUpload = ClansAPI.getDataInstance().getConfig().getRoot().getDouble("Clans.heads.cost-to-upload");
		double costToGet = ClansAPI.getDataInstance().getConfig().getRoot().getDouble("Clans.heads.cost-to-get");
		if (args.length == 0) {
			GUI.HEAD_MAIN_MENU.get(p).open(p);
			return true;
		}
		String args0 = args[0];
		if (args.length == 1) {
			if (args0.equalsIgnoreCase("input")) {
				lib.sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("input-head"));
				return true;
			}
			if (args0.equalsIgnoreCase("list")) {
				GUI.HEAD_LIBRARY.get().open(p);
				return true;
			}
			return true;
		}

		if (args.length == 2) {
			if (args0.equalsIgnoreCase("input")) {
				lib.sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("input-head"));
				return true;
			}
			if (args0.equalsIgnoreCase("buy")) {
				ItemStack target = CustomHead.Manager.get(args[1]);
				if (chargeToGet) {
					if (target != null) {
						if (EconomyProvision.getInstance().isValid()) {
							BigDecimal cost = BigDecimal.valueOf(costToGet);
							EconomyProvision provision = EconomyProvision.getInstance();
							if (provision.has(cost, p).orElse(false)) {
								if (provision.withdraw(cost, p).orElse(false)) {
									p.getLocation().getWorld().dropItem(p.getEyeLocation(), target);
									sendMessage(p, MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("purchased-head"), cost));
								}
							} else {
								sendMessage(p, MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("not-enough-money"), (cost.subtract(BigDecimal.valueOf(provision.balance(p).orElse(0.0))))));
							}
						} else {
							sendMessage(p, "&cNo economy found feature disabled.");
						}
					}
				} else {
					if (target != null) {
						p.getLocation().getWorld().dropItem(p.getEyeLocation(), target);
						sendMessage(p, MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("purchased-head"), 0.0));
					}
				}
				return true;
			}
			// look for player here
			PlayerSearch search = PlayerSearch.of(args0);
			if (search != null && search.isOnline()) {
				Player foundYa = search.getPlayer().getPlayer();
				CustomHead head = CustomHead.Manager.pick(args0);
				// announce retrieval of head
			}
			return true;
		}

		if (args.length == 3) {
			if (args0.equalsIgnoreCase("input")) {
				String name = args[1];
				String head = args[2];
				if (chargeToUpload) {
					if (EconomyProvision.getInstance().isValid()) {
						BigDecimal cost = BigDecimal.valueOf(costToUpload);
						EconomyProvision provision = EconomyProvision.getInstance();
						if (provision.has(cost, p).orElse(false)) {
							if (provision.withdraw(cost, p).orElse(false)) {
								CustomHead.Manager.load(new CustomHead() {
									final ItemStack item;

									{
										item = CustomHeadLoader.provide(head);
									}

									@Override
									public @NotNull ItemStack get() {
										return item;
									}

									@Override
									public @NotNull String name() {
										return name;
									}

									@Override
									public @NotNull String category() {
										return "Clans";
									}
								});
								sendMessage(p, MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("cached-head"), cost));
							}
						} else {
							sendMessage(p, MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("not-enough-money"), (cost.subtract(BigDecimal.valueOf(provision.balance(p).orElse(0.0))))));
						}
					}
				} else {
					CustomHead.Manager.load(new CustomHead() {
						final ItemStack item;

						{
							item = CustomHeadLoader.provide(head);
						}

						@Override
						public @NotNull ItemStack get() {
							return item;
						}

						@Override
						public @NotNull String name() {
							return name;
						}

						@Override
						public @NotNull String category() {
							return "Clans";
						}
					});
					sendMessage(p, MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("cached-head"), 0.0));
				}
				return true;
			}

		}

		return true;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, getBaseCompletion(args))
				.then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, "input", "list", "buy")
				.get();
	}

}
