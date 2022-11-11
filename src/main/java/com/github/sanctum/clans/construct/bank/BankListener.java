package com.github.sanctum.clans.construct.bank;

import com.github.sanctum.clans.construct.api.BanksAPI;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.event.bank.AsyncNewBankEvent;
import com.github.sanctum.clans.event.bank.BankPreTransactionEvent;
import com.github.sanctum.clans.event.bank.BankSetBalanceEvent;
import com.github.sanctum.clans.event.bank.BankTransactionEvent;
import com.github.sanctum.clans.event.bank.messaging.Messages;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.event.LabyrinthVentCall;
import com.github.sanctum.panther.event.Subscribe;
import com.github.sanctum.panther.event.Vent;
import java.math.BigDecimal;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BankListener {

	private final JavaPlugin p = JavaPlugin.getProvidingPlugin(Bank.class);

	@Subscribe(priority = Vent.Priority.HIGHEST)
	public void onCreate(AsyncNewBankEvent e) {
		final ClanBank bank = e.getClanBank();
		if (!(bank instanceof Bank)) return; // Only react on our ClanBank implementation
		if (ClansAPI.getInstance().getPlugin().isEnabled()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					BankMeta.get(e.getClan()).storeBank((Bank) bank);
				}
			}.runTask(p);
		}
	}

	@Subscribe(priority = Vent.Priority.HIGHEST)
	public void onPreTransactionMonitor(BankPreTransactionEvent event) {
		switch (BanksAPI.getInstance().logToConsole()) {
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
	}

	@Subscribe(priority = Vent.Priority.HIGHEST)
	public void onTransaction(BankTransactionEvent e) {
		if (e instanceof BankPreTransactionEvent) return;
		new BukkitRunnable() {
			@Override
			public void run() {
				switch (BanksAPI.getInstance().logToConsole()) {
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
	}

	@Subscribe(priority = Vent.Priority.HIGHEST)
	public void onDeposit(BankPreTransactionEvent event) {
		if (event.getTransactionType() != BankTransactionEvent.Type.DEPOSIT) return;
		if (!(event.getClanBank() instanceof Bank)) return; // Only react on our ClanBank implementation
		if (!event.isSuccess()) {
			event.setCancelled(true);
			return; // The player didn't have enough money or is not allowed, no transaction
		}
		final Bank bank = (Bank) event.getClanBank();
		final BigDecimal maxBalance = BanksAPI.getInstance().maxBalance();
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
		new LabyrinthVentCall<>(new BankTransactionEvent(player, bank, amount, bank.clanId, success, BankTransactionEvent.Type.DEPOSIT)).run();
	}

	@Subscribe(priority = Vent.Priority.HIGHEST)
	public void onWithdrawal(BankPreTransactionEvent event) {
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
		new LabyrinthVentCall<>(new BankTransactionEvent(player, bank, amount, bank.clanId, success, BankTransactionEvent.Type.WITHDRAWAL)).run();
	}

	@Subscribe(priority = Vent.Priority.MEDIUM)
	public void onSetBalance(BankSetBalanceEvent event) {
		if (!(event.getClanBank() instanceof Bank)) return; // Only react on our ClanBank implementation
		final BigDecimal maxBalance = BanksAPI.getInstance().maxBalance();
		if (maxBalance != null && event.getNewBalance().compareTo(maxBalance) > 0) {
			event.setCancelled(true);
		}
	}

	@Subscribe(priority = Vent.Priority.READ_ONLY)
	public void onSetBalanceMonitor(BankSetBalanceEvent event) {
		if (!(event.getClanBank() instanceof Bank)) return; // Only react on our ClanBank implementation
		final Bank bank = (Bank) event.getClanBank();
		bank.balance = event.getNewBalance();
		BankMeta.get(event.getClan()).storeBank(bank);
	}

}