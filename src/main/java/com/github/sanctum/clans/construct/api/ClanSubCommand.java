package com.github.sanctum.clans.construct.api;

import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.library.Mailer;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class ClanSubCommand {

	private List<String> aliases = Collections.emptyList();
	private String permMsg = "&cYou don't have permission <permission>";
	private String permission = "clans";
	private String usage = "&7|&f) &6{label}";
	private String lastLabel;
	private final String label;
	private boolean invisible;

	public ClanSubCommand(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

	protected final List<String> getBaseCompletion(String... args) {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, getLabel())
				.get();
	}

	public void setNoPermissionMessage(String message) {
		this.permMsg = message;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public void setAliases(List<String> ALIASES) {
		this.aliases = ALIASES;
	}

	public String getPermission() {
		return this.permission;
	}

	public String getUsage() {
		return usage;
	}

	public boolean isInvisible() {
		return invisible;
	}

	public void setInvisible(boolean invisible) {
		this.invisible = invisible;
	}

	/**
	 * @return the last known label associated with this command.
	 */
	public String getLastLabel() {
		return lastLabel;
	}

	public void setLastLabel(String lastLabel) {
		this.lastLabel = lastLabel;
	}

	public boolean testPermission(Player target) {
		if (this.permission != null && !this.permission.isEmpty()) {
			if (target.hasPermission(this.permission)) {
				return true;
			} else {
				Mailer.empty(target).chat(this.permMsg.replace("<permission>", permission)).deploy();
				return false;
			}
		} else {
			return true;
		}
	}

	protected boolean isAlphaNumeric(String s) {
		return s != null && (ClansAPI.getDataInstance().isTrue("Formatting.symbols") ? s.matches("([&a-zA-Z0-9])+") : s.matches("([a-zA-Z0-9])+"));
	}

	public void sendMessage(Player player, String message) {
		Clan.ACTION.sendMessage(player, message);
	}

	public boolean playerOnly() {
		Bukkit.getLogger().warning("This is a player only sub-command.");
		return true;
	}

	public List<String> getAliases() {
		return this.aliases;
	}

	public abstract boolean player(Player player, String label, String[] args);

	public boolean console(CommandSender sender, String label, String[] args) {
		return playerOnly();
	}

	public List<String> tab(Player player, String label, String[] args) {
		List<String> completion = getBaseCompletion(args);
		return completion.isEmpty() ? null : completion;
	}

}
