package com.github.sanctum.clans.construct.bank;

import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.events.core.bank.AsyncNewBankEvent;
import com.github.sanctum.clans.events.core.bank.BankPreTransactionEvent;
import com.github.sanctum.clans.events.core.bank.BankSetBalanceEvent;
import com.github.sanctum.clans.events.core.bank.BankTransactionEvent;
import com.github.sanctum.clans.events.core.bank.messaging.Messages;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.event.custom.Vent;
import java.math.BigDecimal;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BankListener implements Listener {

	private final JavaPlugin p = JavaPlugin.getProvidingPlugin(Bank.class);

	public BankListener() {
		// onCreate
		LabyrinthProvider.getInstance().getEventMap().subscribe(new Vent.Subscription<>(AsyncNewBankEvent.class, p, Vent.Priority.HIGHEST, (e, listener) -> {
			final ClanBank bank = e.getClanBank();
			if (!(bank instanceof Bank)) return; // Only react on our ClanBank implementation
			new BukkitRunnable() {
				@Override
				public void run() {
					BankMeta.get(e.getClan()).storeBank((Bank) bank);
				}
			}.runTask(p);
		}));
		// onPreTransactionMonitor
		LabyrinthProvider.getInstance().getEventMap().subscribe(new Vent.Subscription<>(BankPreTransactionEvent.class, p, Vent.Priority.HIGHEST, (event, listener) -> {
			switch (ClanBank.API.defaultImpl.logToConsole()) {
				case SILENT:
					return;
				case QUIET:
					if (event.isCancelled()) p.getLogger().info(event.toString());
					return;
				case VERBOSE:
					p.getLogger().info(event.toString() + " " +
							Messages.TRANSACTION_VERBOSE_CLAN_ID.toString()
									.replace("{0}", event.getClanId())
					);
			}
		}));
		// onTransaction (original priority = MONITOR), conformed to new priority ordinance
		LabyrinthProvider.getInstance().getEventMap().subscribe(new Vent.Subscription<>(BankTransactionEvent.class, p, Vent.Priority.HIGHEST, (e, listener) -> {
			if (e instanceof BankPreTransactionEvent) return;
			new BukkitRunnable() {
				@Override
				public void run() {
					switch (ClanBank.API.defaultImpl.logToConsole()) {
						case SILENT:
							break;
						case QUIET:
							p.getLogger().info(e.toString());
							break;
						case VERBOSE:
							p.getLogger().info(e.toString() + " " +
									Messages.TRANSACTION_VERBOSE_CLAN_ID.toString()
											.replace("{0}", e.getClanId())
							);
					}
					if (!(e.getClanBank() instanceof Bank)) return; // Only react on our ClanBank implementation
					BankLog.getForClan(e.getClan()).addTransaction(e); // line moved from onTransactionInGameLog
					BankMeta.get(e.getClan()).storeBank((Bank) e.getClanBank());
				}
			}.runTask(p);
		}));
		// onDeposit (HIGHEST, ignoreCancelled = true)
		LabyrinthProvider.getInstance().getEventMap().subscribe(new Vent.Subscription<>(BankPreTransactionEvent.class, p, Vent.Priority.HIGHEST, (event, listener) -> {
			if (event.getTransactionType() != BankTransactionEvent.Type.DEPOSIT) return;
			if (!(event.getClanBank() instanceof Bank)) return; // Only react on our ClanBank implementation
			if (!event.isSuccess()) {
				event.setCancelled(true);
				return; // The player didn't have enough money or is not allowed, no transaction
			}
			final Bank bank = (Bank) event.getClanBank();
			final BigDecimal maxBalance = ClanBank.API.defaultImpl.maxBalance();
			if (maxBalance != null) {
				if (bank.balance.add(event.getAmount()).compareTo(maxBalance) > 0) {
					event.setCancelled(true);
					return;
				}
			}
			final Player player = event.getPlayer();
			final BigDecimal amount = event.getAmount();
			final boolean success;

			Optional<Boolean> opt = EconomyProvision.getInstance().withdraw(amount, player, player.getWorld().getName());

			success = opt.orElse(false);

			if (success) bank.balance = bank.balance.add(amount);
			if (!success) event.setSuccess(false);
			new Vent.Call<>(Vent.Runtime.Synchronous, new BankTransactionEvent(player, bank, amount, bank.clanId, success, BankTransactionEvent.Type.DEPOSIT)).run();
		}));
		// onWithdrawal (HIGHEST, ignoreCancelled = true)
		LabyrinthProvider.getInstance().getEventMap().subscribe(new Vent.Subscription<>(BankPreTransactionEvent.class, p, Vent.Priority.HIGHEST, (event, listener) -> {
			if (event.getTransactionType() != BankTransactionEvent.Type.WITHDRAWAL) return;
			if (!(event.getClanBank() instanceof Bank)) return; // Only react on our ClanBank implementation
			if (!event.isSuccess()) {
				event.setCancelled(true);
				return; // The bank didn't have enough money or is not allowed, no transaction
			}
			final Bank bank = (Bank) event.getClanBank();
			final Player player = event.getPlayer();
			final BigDecimal amount = event.getAmount();
			final boolean success;
			Optional<Boolean> opt = EconomyProvision.getInstance().deposit(amount, player, player.getWorld().getName());

			success = opt.orElse(false);
			if (success) bank.balance = bank.balance.subtract(amount);
			if (!success) event.setSuccess(false);
			new Vent.Call<>(Vent.Runtime.Synchronous, new BankTransactionEvent(player, bank, amount, bank.clanId, success, BankTransactionEvent.Type.WITHDRAWAL)).run();
		}));
		// onSetBalance (ignoreCancelled = true)
		LabyrinthProvider.getInstance().getEventMap().subscribe(new Vent.Subscription<>(BankSetBalanceEvent.class, p, Vent.Priority.MEDIUM, (event, listener) -> {
			if (!(event.getClanBank() instanceof Bank)) return; // Only react on our ClanBank implementation
			final BigDecimal maxBalance = ClanBank.API.defaultImpl.maxBalance();
			if (maxBalance != null && event.getNewBalance().compareTo(maxBalance) > 0) {
				event.setCancelled(true);
			}
		}));
		// onSetBalanceMonitor (MONITOR, ignoreCancelled = true) TODO: this needs to happen last!!
		LabyrinthProvider.getInstance().getEventMap().subscribe(new Vent.Subscription<>(BankSetBalanceEvent.class, p, Vent.Priority.HIGHEST, (event, listener) -> {
			if (!(event.getClanBank() instanceof Bank)) return; // Only react on our ClanBank implementation
			final Bank bank = (Bank) event.getClanBank();
			bank.balance = event.getNewBalance();
			BankMeta.get(event.getClan()).storeBank(bank);
		}));
	}
}