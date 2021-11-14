package com.github.sanctum.clans.construct.bank;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.event.bank.AsyncNewBankEvent;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.HFEncoded;
import com.github.sanctum.labyrinth.library.HUID;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.plugin.java.JavaPlugin;

public class BankMeta implements Serializable {
	private static final long serialVersionUID = 4445662686153606368L;
	private static final Map<Clan, BankMeta> instances = new HashMap<>();
	private transient final JavaPlugin providingPlugin = JavaPlugin.getProvidingPlugin(BankMeta.class);
	private transient Clan clan;
	private final String clanId;
	private String bank = "";
	private String accessMap = "";
	private String bankLog = "";

	private BankMeta(Clan clan) {
		this.clan = clan;
		this.clanId = clan.getId().toString();
		loadMetaFromClan();
	}

	public Clan getClan() {
		if (clan == null) {
			clan = ClansAPI.getInstance().getClanManager().getClan(HUID.fromString(clanId));
		}
		return clan;
	}

	public void storeBank(Bank bank) {
		try {
			this.bank = new HFEncoded(bank).serialize();
		} catch (IOException e) {
			e.printStackTrace();
		}
		storeMetaToClan();
	}

	public void storeAccessMap(BankAction.AccessMap accessMap) {
		try {
			this.accessMap = new HFEncoded(accessMap).serialize();
		} catch (IOException e) {
			e.printStackTrace();
		}
		storeMetaToClan();
	}

	public void storeBankLog(BankLog bankLog) {
		try {
			this.bankLog = new HFEncoded(bankLog).serialize();
		} catch (IOException e) {
			e.printStackTrace();
		}
		storeMetaToClan();
	}

	public Optional<Bank> getBank() {
		if (bank.isEmpty()) {
			final Bank bank = new Bank(clanId);
			new Vent.Call<>(Vent.Runtime.Asynchronous, new AsyncNewBankEvent(getClan(), bank)).complete().join();
			return Optional.of(bank);
		}
		try {
			return Optional.ofNullable((Bank) new HFEncoded(bank).deserialized());
		} catch (IOException | ClassNotFoundException e) {
			providingPlugin.getLogger().severe("Unable to load clan bank file! Prepare for NPEs.");
			return Optional.empty();
		}
	}

	public Optional<BankAction.AccessMap> getAccessMap() {
		if (!this.accessMap.isEmpty()) {
			try {
				return Optional.ofNullable((BankAction.AccessMap) new HFEncoded(this.accessMap).deserialized());
			} catch (IOException | ClassNotFoundException e) {
				providingPlugin.getLogger().warning("Unable to load bank access map. Generating new.");
			}
		}
		return Optional.empty();
	}

	public Optional<BankLog> getBankLog() {
		if (!this.bankLog.isEmpty()) {
			try {
				return Optional.ofNullable((BankLog) new HFEncoded(this.bankLog).deserialized());
			} catch (IOException | ClassNotFoundException e) {
				providingPlugin.getLogger().warning("Unable to load bank log. Generating new.");
			}
		}
		return Optional.empty();
	}

	private synchronized void storeMetaToClan() {
		clan.setValue("bank", this, false);
	}

	private synchronized void loadMetaFromClan() {
		BankMeta saved = clan.getValue(getClass(), "bank");
		if (saved != null) {
			this.bank = saved.bank;
			this.accessMap = saved.accessMap;
			this.bankLog = saved.bankLog;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BankMeta bankMeta = (BankMeta) o;
		return clanId.equals(bankMeta.clanId) &&
				bank.equals(bankMeta.bank) &&
				accessMap.equals(bankMeta.accessMap) &&
				bankLog.equals(bankMeta.bankLog);
	}

	@Override
	public int hashCode() {
		return Objects.hash(clanId, bank, accessMap, bankLog);
	}

	public static BankMeta get(Clan clan) {
		return instances.computeIfAbsent(clan, BankMeta::new);
	}

	public static BankMeta get(ClanBank bank) {
		return instances.values().stream().filter(b -> b.getBank().map(ba -> ba.equals(bank)).orElse(false)).findFirst().orElse(null);
	}

	public static void clearManagerCache() {
		instances.clear();
	}
}
