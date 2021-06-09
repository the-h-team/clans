package com.github.sanctum.clans.construct.bank;

import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.util.events.clans.bank.AsyncNewBankEvent;
import com.github.sanctum.clans.util.events.clans.bank.BankPreTransactionEvent;
import com.github.sanctum.clans.util.events.clans.bank.BankSetBalanceEvent;
import com.github.sanctum.clans.util.events.clans.bank.BankTransactionEvent;
import com.github.sanctum.clans.util.events.clans.bank.messaging.Messages;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import java.math.BigDecimal;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BankListener implements Listener {

	private static final JavaPlugin P = JavaPlugin.getProvidingPlugin(Bank.class);

	@EventHandler
	public void onCreate(AsyncNewBankEvent e) {
		if (!(e.getClanBank() instanceof Bank)) return; // Only react on our ClanBank implementation
		new BukkitRunnable() {
			@Override
			public void run() {
				BankMeta.get(e.getClan()).storeBank((Bank) e.getClanBank());
			}
		}.runTask(P);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPreTransactionMonitor(BankPreTransactionEvent event) {
		switch (ClanBank.API.defaultImpl.logToConsole()) {
			case SILENT:
				return;
			case QUIET:
				if (event.isCancelled()) P.getLogger().info(event.toString());
				return;
			case VERBOSE:
				P.getLogger().info(event.toString() + " " +
						Messages.TRANSACTION_VERBOSE_CLAN_ID.toString()
								.replace("{0}", event.getClanId())
				);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onTransaction(BankTransactionEvent e) {
		if (e instanceof BankPreTransactionEvent) return;
		new BukkitRunnable() {
			@Override
			public void run() {
				switch (ClanBank.API.defaultImpl.logToConsole()) {
					case SILENT:
						break;
					case QUIET:
						P.getLogger().info(e.toString());
						break;
					case VERBOSE:
						P.getLogger().info(e.toString() + " " +
								Messages.TRANSACTION_VERBOSE_CLAN_ID.toString()
										.replace("{0}", e.getClanId())
						);
				}
				if (!(e.getClanBank() instanceof Bank)) return; // Only react on our ClanBank implementation
				BankMeta.get(e.getClan()).storeBank((Bank) e.getClanBank());
			}
		}.runTask(P);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onTransactionInGameLog(BankTransactionEvent e) {
		new BukkitRunnable() {
			@Override
			public void run() {
				BankLog.getForClan(e.getClan()).addTransaction(e);
			}
		}.runTaskAsynchronously(P);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDeposit(BankPreTransactionEvent event) {
		if (event.getType() != BankTransactionEvent.Type.DEPOSIT) return;
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
		Bank.PM.callEvent(new BankTransactionEvent(player, bank, amount, bank.clanId, success, BankTransactionEvent.Type.DEPOSIT));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onWithdrawal(BankPreTransactionEvent event) {
		if (event.getType() != BankTransactionEvent.Type.WITHDRAWAL) return;
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
		Bank.PM.callEvent(new BankTransactionEvent(player, bank, amount, bank.clanId, success, BankTransactionEvent.Type.WITHDRAWAL));
	}

	@EventHandler(ignoreCancelled = true)
	public void onSetBalance(BankSetBalanceEvent event) {
		if (!(event.getClanBank() instanceof Bank)) return; // Only react on our ClanBank implementation
		final BigDecimal maxBalance = ClanBank.API.defaultImpl.maxBalance();
		if (maxBalance != null && event.getNewBalance().compareTo(maxBalance) > 0) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSetBalanceMonitor(BankSetBalanceEvent event) {
		if (!(event.getClanBank() instanceof Bank)) return; // Only react on our ClanBank implementation
		final Bank bank = (Bank) event.getClanBank();
		bank.balance = event.getNewBalance();
		BankMeta.get(event.getClan()).storeBank(bank);
	}

}