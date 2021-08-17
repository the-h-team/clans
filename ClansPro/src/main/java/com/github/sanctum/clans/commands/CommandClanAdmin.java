package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.UI;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.ClanBlueprint;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.formatting.PaginatedList;
import com.github.sanctum.labyrinth.library.HFEncoded;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.link.CycleList;
import com.github.sanctum.stashes.StashContainer;
import com.github.sanctum.vaults.VaultContainer;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CommandClanAdmin extends Command {

	final StringLibrary lib = new StringLibrary();

	public CommandClanAdmin() {
		super("clansadmin");
		setDescription("Base command for staff commands.");
		setAliases(Arrays.asList("ca", "cla", "clanadmin"));
		setPermission("clanspro.admin");
	}

	private List<String> helpMenu(String label) {
		List<String> help = new ArrayList<>();
		help.add("&7|&e) &6/" + label + " &freload <&7configName&f>");
		help.add("&7|&e) &6/" + label + " &eupdate");
		help.add("&7|&e) &6/" + label + " &fgetid <&7clanNamef>");
		help.add("&7|&e) &6/" + label + " &fidmode");
		help.add("&7|&e) &6/" + label + " &fspy <&7chatName&f>");
		help.add("&7|&e) &6/" + label + " &fkick <&7playerName&f>");
		help.add("&7|&e) &6/" + label + " &fput <&7playerName&f> <&7clanName&f>");
		help.add("&7|&e) &6/" + label + " &fview <&7clanName&f> <&7power, money, claims, vault, stash&f>");
		help.add("&7|&e) &6/" + label + " &fsetspawn <&9blue&f, &cred&f>");
		help.add("&7|&e) &6/" + label + " &dsettings");
		help.add("&7|&e) &6/" + label + " &ftphere <&7claneName&f>");
		help.add("&7|&e) &6/" + label + " &fgive <&7clanName&f> <power, money, claims, color> <&7amount&f, &7color&f>");
		help.add("&7|&e) &6/" + label + " &ftake <&7clanName&f> <power, money, claims> <&7amount&f>");
		help.add("&7|&e) &6/" + label + " &fset <&7clanName&f> <money> <&7amount&f>");
		return help;
	}

	private final List<String> arguments = new ArrayList<>();

	@Override
	public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) throws IllegalArgumentException {


		List<String> result = new ArrayList<>();
		if (args.length == 1) {
			arguments.clear();
			arguments.addAll(Arrays.asList("reload", "kick", "tphere", "set", "put", "settings", "setspawn", "update", "getid", "idmode", "give", "take", "spy", "view", "purge", "tphere"));
			for (String a : arguments) {
				if (a.toLowerCase().startsWith(args[0].toLowerCase()))
					result.add(a);
			}
			return result;
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("give")) {
				arguments.clear();
				arguments.addAll(Clan.ACTION.getAllClanNames());
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("kick")) {
				arguments.clear();
				arguments.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList()));
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("put")) {
				arguments.clear();
				arguments.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList()));
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("setspawn")) {
				arguments.clear();
				arguments.addAll(Arrays.asList("blue", "red", "blue_death", "red_death"));
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("spy")) {
				arguments.clear();
				arguments.addAll(Arrays.asList("clan", "ally", "custom"));
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("take")) {
				arguments.clear();
				arguments.addAll(Clan.ACTION.getAllClanNames());
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("tphere")) {
				arguments.clear();
				arguments.addAll(Clan.ACTION.getAllClanNames());
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("view")) {
				arguments.clear();
				arguments.addAll(Clan.ACTION.getAllClanNames());
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("close") || args[0].equalsIgnoreCase("set")) {
				arguments.clear();
				arguments.addAll(Clan.ACTION.getAllClanNames());
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("purge")) {
				arguments.clear();
				arguments.addAll(Clan.ACTION.getAllClanNames());
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
		}
		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("give")) {
				arguments.clear();
				arguments.addAll(Arrays.asList("money", "claims", "power"));
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[2].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("put")) {
				arguments.clear();
				arguments.addAll(Clan.ACTION.getAllClanNames());
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[2].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("set")) {
				arguments.clear();
				arguments.addAll(Arrays.asList("money", "logo"));
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[2].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("take")) {
				arguments.clear();
				arguments.addAll(Arrays.asList("money", "claims", "power"));
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[2].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("view")) {
				arguments.clear();
				arguments.addAll(Arrays.asList("money", "claims", "power", "stash", "vault"));
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[2].toLowerCase()))
						result.add(a);
				}
				return result;
			}
		}
		return super.tabComplete(sender, alias, args);
	}

	@Override
	public boolean execute(@NotNull CommandSender commandSender, @NotNull String commandLabel, String[] args) {
		if (!(commandSender instanceof Player)) {
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("reload")) {
					FileManager file = DataManager.FileType.MISC_FILE.get(args[1], "Configuration");
					if (file.exists()) {
						file.reload();
						ClansAPI.getInstance().getPlugin().getLogger().info("File by the name of " + '"' + args[1] + '"' + " was reloaded.");
					} else {
						ClansAPI.getInstance().getPlugin().getLogger().info("File by the name of " + '"' + args[1] + '"' + " was not found.");
						return true;
					}
					return true;
				}
			}
			return true;
		}

        /*
        // VARIABLE CREATION
        //  \/ \/ \/ \/ \/ \/
         */
		int length = args.length;
		Player p = (Player) commandSender;
        /*
        //  /\ /\ /\ /\ /\ /\
        //
         */
		if (!p.hasPermission(this.getPermission())) {
			lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + '"');
			return true;
		}
		if (length == 0) {
			new PaginatedList<>(helpMenu(commandLabel))
					.limit(lib.menuSize())
					.start((pagination, page, max) -> {
						lib.sendMessage(p, lib.menuTitle());
						Message.form(p).send(lib.menuBorder());
					}).finish(builder -> builder.setPlayer(p).setPrefix(lib.menuBorder())).decorate((pagination, string, page, max, placement) -> Message.form(p).send(string)).get(1);
			return true;
		}
		if (!p.hasPermission(this.getPermission())) {
			lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + '"');
			return true;
		}
		if (length == 1) {
			String args0 = args[0];
			if (args0.equalsIgnoreCase("reload")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /" + commandLabel + " reload <fileName>");
				return true;
			}
			if (args0.equalsIgnoreCase("getid")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /" + commandLabel + " getid <playerName>");
				return true;
			}
			if (args0.equalsIgnoreCase("kick")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /" + commandLabel + " kick <playerName>");
				return true;
			}
			if (args0.equalsIgnoreCase("tphere")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /" + commandLabel + " tphere <clanName>");
				return true;
			}
			if (args0.equalsIgnoreCase("put")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /" + commandLabel + " put <playerName> <clanName> | [password]");
				return true;
			}
			if (args0.equalsIgnoreCase("spy")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /" + commandLabel + " spy <chatType>");
				return true;
			}
			if (args0.equalsIgnoreCase("settings")) {
				UI.select(UI.Singular.SETTINGS_WINDOW).open(p);
				return true;
			}
			if (args0.equalsIgnoreCase("give")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /" + commandLabel + " give <clanName> <power, money, claims, color> <&7amount&f, &7color&f>");
				return true;
			}
			if (args0.equalsIgnoreCase("take")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /" + commandLabel + " take <clanName> <power, money, claims> <&7amount&f>");
				return true;
			}
			if (args0.equalsIgnoreCase("setspawn")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /" + commandLabel + " setspawn <&9blue&f, &cred&f, &9blue_death&f, &cred_death&f>");
				return true;
			}
			if (args0.equalsIgnoreCase("view")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /" + commandLabel + " view <clanName> <money, claims, power, vault, stash>");
				return true;
			}
			if (args0.equalsIgnoreCase("update")) {
				FileManager main = ClansAPI.getInstance().getFileList().find("Config", "Configuration");
				FileManager messages = ClansAPI.getInstance().getFileList().find("Messages", "Configuration");
				if (ClansAPI.getInstance().getPlugin().getDescription().getVersion().equals(main.getConfig().getString("Version"))) {
					lib.sendMessage(p, "&3&oThe configuration is already up to date.");
					return true;
				} else {
					FileManager mainOld = ClansAPI.getInstance().getFileList().find("config_old", "Configuration");
					FileManager messOld = ClansAPI.getInstance().getFileList().find("messages_old", "Configuration");
					mainOld.getConfig().options().copyDefaults(true);
					mainOld.getConfig().setDefaults(main.getConfig());
					main.refreshConfig();
					messOld.getConfig().options().copyDefaults(true);
					messOld.getConfig().setDefaults(messages.getConfig());
					messages.refreshConfig();
					InputStream mainGrab = ClansAPI.getInstance().getPlugin().getResource("Config.yml");
					if (mainGrab == null) throw new IllegalStateException("Unable to load Config.yml from the jar!");
					FileManager.copy(mainGrab, main.getFile());
					lib.sendMessage(p, "&b&oUpdated configuration to the latest plugin version.");
				}
				return true;
			}
			if (args0.equalsIgnoreCase("idmode")) {
				if (!ClansAPI.getData().ID_MODE.containsKey(p)) {
					ClansAPI.getData().ID_MODE.put(p, "ENABLED");
					lib.sendMessage(p, "&f[&a&oADMIN&f] &6&lID &fmode &aENABLED.");
					return true;
				}
				if (ClansAPI.getData().ID_MODE.get(p).equals("ENABLED")) {
					ClansAPI.getData().ID_MODE.put(p, "DISABLED");
					lib.sendMessage(p, "&f[&a&oADMIN&f] &6&lID &fmode &cDISABLED.");
					return true;
				}
				if (ClansAPI.getData().ID_MODE.get(p).equals("DISABLED")) {
					ClansAPI.getData().ID_MODE.put(p, "ENABLED");
					lib.sendMessage(p, "&f[&a&oADMIN&f] &6&lID &fmode &aENABLED.");
					return true;
				}
				return true;
			}
			PaginatedList<String> list = new PaginatedList<>(helpMenu(commandLabel))
					.limit(lib.menuSize())
					.start((pagination, page, max) -> {
						lib.sendMessage(p, lib.menuTitle());
						Message.form(p).send(lib.menuBorder());
					}).finish(builder -> builder.setPlayer(p).setPrefix(lib.menuBorder())).decorate((pagination, string, page, max, placement) -> Message.form(p).send(string));
			try {
				list.get(Integer.parseInt(args0));
			} catch (NumberFormatException e) {
				lib.sendMessage(p, "&c&oInvalid page number!");
			}
			return true;
		}

		if (length == 2) {
			String args0 = args[0];
			String args1 = args[1];
			if (args0.equalsIgnoreCase("close")) {
				if (ClansAPI.getInstance().getClanID(args1) != null) {
					Clan target = ClansAPI.getInstance().getClan(ClansAPI.getInstance().getClanID(args1));
					for (ClanAssociate id : target.getMembers()) {
						if (id.getPriority().toInt() == 3) {
							target.broadcast("&8(&e!&8) &4&oOur clan has been forcibly closed by a staff member.");
							Clan.ACTION.removePlayer(id.getPlayer().getUniqueId());
							break;
						}
					}
				} else {
					lib.sendMessage(p, lib.clanUnknown(args1));
					return true;
				}
			}
			if (args0.equalsIgnoreCase("purge")) {
				if (ClansAPI.getInstance().getClanID(args1) != null) {
					Clan target = ClansAPI.getInstance().getClan(ClansAPI.getInstance().getClanID(args1));
					int amount = 0;
					for (String data : target.getDataKeys()) {
						target.removeValue(data);
						amount++;
					}
					lib.sendMessage(p, "&3&l" + amount + " &aobject(s) were successfully removed.");
				} else {

					if (args1.equalsIgnoreCase("all")) {
						int amount = 0;
						for (Clan target : ClansAPI.getInstance().getClanManager().getClans().list()) {
							for (String data : target.getDataKeys()) {
								target.removeValue(data);
								amount++;
							}
						}
						lib.sendMessage(p, "&3&l" + amount + " &aobject(s) were successfully removed.");
						return true;
					}

					lib.sendMessage(p, lib.clanUnknown(args1));
					return true;
				}
			}
			if (args0.equalsIgnoreCase("tphere")) {
				String targetId = ClansAPI.getInstance().getClanID(args1);
				Clan c = ClansAPI.getInstance().getClan(targetId);
				if (targetId != null) {
					for (String id : c.getMemberIds()) {
						UUID u = UUID.fromString(id);
						if (Bukkit.getOfflinePlayer(u).isOnline()) {
							Bukkit.getOfflinePlayer(u).getPlayer().teleport(p.getLocation());
							lib.sendMessage(Bukkit.getOfflinePlayer(u).getPlayer(), "&aYou've been teleported by a staff member.");
						}
					}
				} else {
					lib.sendMessage(p, lib.clanUnknown(args1));
				}
				return true;
			}
			if (args0.equalsIgnoreCase("put")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /" + commandLabel + " put <playerName> <clanName>");
				return true;
			}
			if (args0.equalsIgnoreCase("kick")) {
				if (Clan.ACTION.getUserID(args1) == null) {
					lib.sendMessage(p, lib.playerUnknown(args1));
					return true;
				}
				UUID target = Clan.ACTION.getUserID(args1);
				if (target.equals(p.getUniqueId())) {
					lib.sendMessage(p, "&c&oInvalid usage, try &6/c leave");
					return true;
				}
				if (!ClansAPI.getInstance().kickUser(target)) {
					lib.sendMessage(p, "&c&oPlayer " + args1 + " isn't in a clan or is the leader (if so use &6/cla close <clanName>&c&o).");
				} else {
					lib.sendMessage(p, "&3&oPlayer " + args1 + " was kicked from their clan.");
					if (Bukkit.getOfflinePlayer(target).isOnline()) {
						lib.sendMessage(Bukkit.getPlayer(target), "&5&oA staff member has removed you from your current clan.");
					}
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("setspawn")) {
				if (args1.equalsIgnoreCase("blue")) {
					FileManager blue = ClansAPI.getData().arenaBlueTeamFile();
					try {
						String loc = new HFEncoded(p.getLocation()).serialize();
						blue.getConfig().set("spawn", loc);
						blue.saveConfig();
						lib.sendMessage(p, "&9&oBlue spawn set.");
					} catch (IOException e) {
						e.printStackTrace();
					}

					return true;
				}
				if (args1.equalsIgnoreCase("blue_death")) {
					FileManager blue = ClansAPI.getData().arenaBlueTeamFile();
					try {
						String loc = new HFEncoded(p.getLocation()).serialize();
						blue.getConfig().set("re-spawn", loc);
						blue.saveConfig();
						lib.sendMessage(p, "&9&oBlue re-spawn location set.");
					} catch (IOException e) {
						e.printStackTrace();
					}

					return true;
				}
				if (args1.equalsIgnoreCase("red")) {
					FileManager red = ClansAPI.getData().arenaRedTeamFile();
					try {
						String loc = new HFEncoded(p.getLocation()).serialize();
						red.getConfig().set("spawn", loc);
						red.saveConfig();
						lib.sendMessage(p, "&c&oRed spawn set.");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return true;
				}
				if (args1.equalsIgnoreCase("red_death")) {
					FileManager red = ClansAPI.getData().arenaRedTeamFile();
					try {
						String loc = new HFEncoded(p.getLocation()).serialize();
						red.getConfig().set("re-spawn", loc);
						red.saveConfig();
						lib.sendMessage(p, "&c&oRed re-spawn location set.");
					} catch (IOException e) {
						e.printStackTrace();
					}

					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("spy")) {
				switch (args1.toLowerCase()) {
					case "ally":
						if (ClansAPI.getData().ALLY_SPY.contains(p)) {
							ClansAPI.getData().ALLY_SPY.remove(p);
							lib.sendMessage(p, "&5&oNo longer spying on ally channels.");
						} else {
							lib.sendMessage(p, "&5&oNow spying on ally channels.");
							ClansAPI.getData().ALLY_SPY.add(p);
						}
						break;
					case "clan":
						if (ClansAPI.getData().CLAN_SPY.contains(p)) {
							ClansAPI.getData().CLAN_SPY.remove(p);
							lib.sendMessage(p, "&5&oNo longer spying on clan channels.");
						} else {
							lib.sendMessage(p, "&5&oNow spying on clan channels.");
							ClansAPI.getData().CLAN_SPY.add(p);
						}
						break;
					case "custom":
						if (ClansAPI.getData().CUSTOM_SPY.contains(p)) {
							ClansAPI.getData().CUSTOM_SPY.remove(p);
							lib.sendMessage(p, "&5&oNo longer spying on custom channels.");
						} else {
							lib.sendMessage(p, "&5&oNow spying on custom channels.");
							ClansAPI.getData().CUSTOM_SPY.add(p);
						}
						break;

					default:
						lib.sendMessage(p, "&cUnknown channel type.");
						break;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("getid")) {
				Player target = Bukkit.getPlayer(args1);
				if (target == null) {

					try {
						lib.sendMessage(p, "&7#&fID &7of clan " + '"' + args1 + '"' + " is: &e&o" + ClansAPI.getInstance().getClanID(args1));
					} catch (NullPointerException e) {
						lib.sendMessage(p, "&c&oUh-oh there was an issue finding the clan.. Check console for errors");
						ClansAPI.getInstance().getPlugin().getLogger().severe(String.format("[%s] - Illegal use of ID retrieval. Clan directory non-existent.", ClansAPI.getInstance().getPlugin().getDescription().getName()));
					}
					return true;
				}

				lib.sendMessage(p, "&7|&e) &6&l" + target.getName() + "'s &e&oclan ID is &f" + ClansAPI.getInstance().getClanID(target.getUniqueId()).toString());
				return true;
			}
			if (args0.equalsIgnoreCase("reload")) {
				FileManager file = DataManager.FileType.MISC_FILE.get(args[1], "Configuration");
				if (file.exists()) {
					file.reload();
					lib.sendMessage(p, "&a&oFile by the name of " + '"' + args1 + '"' + " was reloaded.");
				} else {
					lib.sendMessage(p, "&c&oFile by the name of " + '"' + args1 + '"' + " not found.");
					return true;
				}
				return true;
			}
			return true;
		}

		if (length == 3) {
			String args0 = args[0];
			String args1 = args[1];
			String args2 = args[2];
			if (args0.equalsIgnoreCase("put")) {
				if (Clan.ACTION.getUserID(args1) == null) {
					lib.sendMessage(p, lib.playerUnknown(args1));
					return true;
				}
				UUID target = Clan.ACTION.getUserID(args1);
				if (target.equals(p.getUniqueId())) {
					lib.sendMessage(p, "&c&oWhat are you even trying to test?");
					return true;
				}
				if (!ClansAPI.getInstance().obtainUser(target, args2)) {
					lib.sendMessage(p, "&c&oPlayer " + args1 + " is already in a clan or the clan specified doesn't exist.");
				} else {
					lib.sendMessage(p, "&3&oPlayer " + args1 + " was placed into clan " + args2);
					if (Bukkit.getOfflinePlayer(target).isOnline()) {
						lib.sendMessage(Bukkit.getPlayer(target), "&5&oA staff member has placed you into clan " + args2);
					}
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("set")) {
				String id = ClansAPI.getInstance().getClanID(args1);
				switch (args2.toLowerCase()) {
					case "logo":
						ItemStack item = p.getInventory().getItemInMainHand();

						if (item.getType() != Material.PAPER) {
							lib.sendMessage(p, "&cInvalid insignia request. Not an insignia print.");
							return true;
						}

						if (!item.hasItemMeta()) {
							lib.sendMessage(p, "&cInvalid insignia request. No lore to process.");
							return true;
						}

						if (item.getItemMeta().getLore() != null) {

							for (String lore : item.getItemMeta().getLore()) {
								if (ChatColor.stripColor(lore).matches("^[a-zA-Z0-9]*$")) {
									lib.sendMessage(p, "&cInvalid insignia request. Error 420");
									return true;
								}
							}

							if (id != null) {
								Clan target = ClansAPI.getInstance().getClan(id);
								target.setValue("logo", new ArrayList<>(item.getItemMeta().getLore()));
							} else {
								lib.sendMessage(p, lib.clanUnknown(args1));
								return true;
							}

							lib.sendMessage(p, "&aPrinted insignia applied to clan " + args1 + " container.");

						} else {

							lib.sendMessage(p, "&cInvalid insignia request. No lore to process.");

							return true;
						}
						break;
				}
			}
			if (args0.equalsIgnoreCase("view")) {
				String id = ClansAPI.getInstance().getClanID(args1);
				switch (args2.toLowerCase()) {
					case "power":
						if (id != null) {
							Clan target = ClansAPI.getInstance().getClan(id);
							lib.sendMessage(p, "Clan: &5" + target.getName() + " &dhas a power level of &5&l" + target.format(String.valueOf(target.getPower())));
						} else {
							lib.sendMessage(p, lib.clanUnknown(args1));
							return true;
						}
						break;
					case "money":
						if (id != null) {
							Clan target = ClansAPI.getInstance().getClan(id);
							if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
								lib.sendMessage(p, "&c&oNo economy interface found. Bank feature disabled.");
								return true;
							}
							lib.sendMessage(p, "Clan: &5" + target.getName() + " &dhas a bank balance of &6&l" + target.format(String.valueOf(target.getBalanceDouble())));
						} else {
							lib.sendMessage(p, lib.clanUnknown(args1));
							return true;
						}
						break;
					case "claims":
						if (id != null) {
							Clan target = ClansAPI.getInstance().getClan(id);
							lib.sendMessage(p, "Clan: &5" + target.getName() + " &dhas a claim limit of &c&l" + target.format(String.valueOf(target.getMaxClaims())));
						} else {
							lib.sendMessage(p, lib.clanUnknown(args1));
							return true;
						}
						break;
					case "vault":
						if (id != null) {
							Clan target = ClansAPI.getInstance().getClan(id);
							if (CycleList.getUsedAddons().contains("Vaults")) {
								p.openInventory(VaultContainer.getVault(target.getName()));
								return true;
							} else {
								lib.sendMessage(p, "&c&oAddon not installed.");
							}
						} else {
							lib.sendMessage(p, lib.clanUnknown(args1));
							return true;
						}

						break;

					case "stash":
						if (id != null) {
							Clan target = ClansAPI.getInstance().getClan(id);
							if (CycleList.getUsedAddons().contains("Stashes")) {
								p.openInventory(StashContainer.getStash(target.getName()));
								return true;
							} else {
								lib.sendMessage(p, "&c&oAddon not installed.");
							}
						} else {
							lib.sendMessage(p, lib.clanUnknown(args1));
							return true;
						}

						break;
					default:
						lib.sendMessage(p, "&c&oUnknown result query.");
						return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("give")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /" + commandLabel + " give <clanName> <power, money, claims, color> <&7amount&f, &7color&f>");
				return true;
			}
			if (args0.equalsIgnoreCase("take")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /" + commandLabel + " take <clanName> <power, money, claims> <&7amount&f>");
				return true;
			}
		}

		if (args.length == 4) {
			String args0 = args[0];
			String args1 = args[1];
			String args2 = args[2];
			String amountPre = args[3];
			if (args0.equalsIgnoreCase("put")) {
				if (Clan.ACTION.getUserID(args1) == null) {
					lib.sendMessage(p, lib.playerUnknown(args1));
					return true;
				}
				UUID target = Clan.ACTION.getUserID(args1);

				if (ClansAPI.getInstance().isInClan(target)) {
					lib.sendMessage(p, "&c&oPlayer " + args1 + " is already in a clan.");
					return true;
				}

				if (ClansAPI.getInstance().getClanID(args2) != null) {
					lib.sendMessage(p, "&c&oClan " + args2 + " already exists.");
				} else {
					lib.sendMessage(p, "&3&oPlayer " + args1 + " was placed into clan " + args2);
					new ClanBlueprint(args2, true).setLeader(Bukkit.getOfflinePlayer(target))
							.setPassword(amountPre)
							.toBuilder().supply().givePower(1.0).getClan();
					if (Bukkit.getOfflinePlayer(target).isOnline()) {
						lib.sendMessage(Bukkit.getPlayer(target), "&5&oA staff member has placed you into clan " + args2);
					}
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("set")) {
				String id = ClansAPI.getInstance().getClanID(args1);
				switch (args2.toLowerCase()) {
					case "logo":
						ItemStack item = p.getInventory().getItemInMainHand();

						if (item.getType() != Material.PAPER) {
							lib.sendMessage(p, "&cInvalid insignia request. Not an insignia print.");
							return true;
						}

						if (!item.hasItemMeta()) {
							lib.sendMessage(p, "&cInvalid insignia request. No lore to process.");
							return true;
						}

						if (item.getItemMeta().getLore() != null) {

							for (String lore : item.getItemMeta().getLore()) {
								if (lore.matches("^[a-zA-Z0-9]*$")) {
									lib.sendMessage(p, "&cInvalid insignia request. Error 420");
									return true;
								}
							}

							if (id != null) {
								Clan target = ClansAPI.getInstance().getClan(id);
								target.setValue("logo", new ArrayList<>(item.getItemMeta().getLore()));
							} else {
								lib.sendMessage(p, lib.clanUnknown(args1));
								return true;
							}

							lib.sendMessage(p, "&aPrinted insignia applied to clan " + args1 + " container.");

						} else {

							lib.sendMessage(p, "&cInvalid insignia request. No lore to process.");

							return true;
						}
						break;
					case "power":
						break;
					case "money":
						try {
							double amount = Double.parseDouble(amountPre);
							if (id != null) {
								Clan target = ClansAPI.getInstance().getClan(id);
								if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
									lib.sendMessage(p, "&c&oNo economy interface found. Bank feature disabled.");
									return true;
								}
								target.setBalanceDouble(amount);
								target.broadcast("Our clan bank was just adjusted to: &a&o" + target.getBalanceDouble());
								lib.sendMessage(p, "Clan: &5" + target.getName() + " &dbank adjusted to &5&l" + amount);
							} else {
								lib.sendMessage(p, lib.clanUnknown(args1));
								return true;
							}
						} catch (NumberFormatException ignored) {

						}
						break;
					case "claims":
						break;
				}
			}
			if (args0.equalsIgnoreCase("give")) {
				String id = ClansAPI.getInstance().getClanID(args1);
				switch (args2.toLowerCase()) {
					case "power":
						try {
							double amount = Double.parseDouble(amountPre);
							if (id != null) {
								Clan target = ClansAPI.getInstance().getClan(id);
								target.givePower(amount);
								lib.sendMessage(p, "Clan: &5" + target.getName() + " &dhas been given &5&l" + amount + " &dpower.");
							} else {
								lib.sendMessage(p, lib.clanUnknown(args1));
								return true;
							}
						} catch (NumberFormatException ignored) {

						}
						break;

					case "money":
						try {
							double amount = Double.parseDouble(amountPre);
							if (id != null) {
								Clan target = ClansAPI.getInstance().getClan(id);
								if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
									lib.sendMessage(p, "&c&oNo economy interface found. Bank feature disabled.");
									return true;
								}
								double result = target.getBalanceDouble() + amount;
								target.setBalanceDouble(result);
								target.broadcast("Our clan was just payed: &a&o" + amountPre);
								lib.sendMessage(p, "Clan: &5" + target.getName() + " &dhas been paid &5&l" + amountPre + " &dand now has &6" + target.format(String.valueOf(target.getBalanceDouble())));
							} else {
								lib.sendMessage(p, lib.clanUnknown(args1));
								return true;
							}
						} catch (NumberFormatException ignored) {
						}
						break;

					case "claims":
						try {
							int amount = Integer.parseInt(amountPre);
							if (id != null) {
								Clan target = ClansAPI.getInstance().getClan(id);
								target.addMaxClaim(amount);
								lib.sendMessage(p, "Clan: &5" + target.getName() + " &dhas been given &5&l" + amount + " &dclaim(s).");
							} else {
								lib.sendMessage(p, lib.clanUnknown(args1));
								return true;
							}
						} catch (NumberFormatException ignored) {

						}
						break;
					case "color":
						if (id != null) {
							Clan target = ClansAPI.getInstance().getClan(id);

							if (!args[3].matches("(&#[a-zA-Z0-9]{6})+(&[a-zA-Z0-9])+") && !args[3].matches("(&#[a-zA-Z0-9]{6})+|(&[a-zA-Z0-9])+")) {
								lib.sendMessage(p, "&c&oInvalid color format.");
								return true;
							}

							for (String s : ClansAPI.getData().getMain().getConfig().getStringList("Clans.color-blacklist")) {

								if (StringUtils.use(args[3]).containsIgnoreCase(s)) {
									lib.sendMessage(p, "&c&oInvalid color format. Code: '" + s + "' is not allowed.");
									return true;
								}
							}

							target.setColor(args[3]);
							lib.sendMessage(p, "Clan: &5" + target.getName() + " &dhas had their color changed to " + StringUtils.use(args[3] + "EXAMPLE").translate());
						} else {
							lib.sendMessage(p, lib.clanUnknown(args1));
							return true;
						}
						break;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("take")) {
				String id = ClansAPI.getInstance().getClanID(args1);
				switch (args2.toLowerCase()) {
					case "power":
						try {
							double amount = Double.parseDouble(amountPre);
							if (id != null) {
								Clan target = ClansAPI.getInstance().getClan(id);
								target.takePower(amount);
								lib.sendMessage(p, "Clan: &5" + target.getName() + " &dhas had &5&l" + amount + " &dpower taken.");
							} else {
								lib.sendMessage(p, lib.clanUnknown(args1));
								return true;
							}
						} catch (NumberFormatException ignored) {

						}
						break;

					case "money":
						try {
							double amount = Double.parseDouble(amountPre);
							if (id != null) {
								Clan target = ClansAPI.getInstance().getClan(id);
								double result = target.getBalanceDouble() - amount;
								target.setBalanceDouble(result);
								target.broadcast("Our clan was just charged: &c&o" + amountPre);
								lib.sendMessage(p, "Clan: &5" + target.getName() + " &dhas been charged &5&l" + amountPre + " &dand now has &6" + target.format(String.valueOf(target.getBalanceDouble())));
							} else {
								lib.sendMessage(p, lib.clanUnknown(args1));
								return true;
							}
						} catch (NumberFormatException ignored) {

						}
						break;

					case "claims":
						try {
							int amount = Integer.parseInt(amountPre);
							if (id != null) {
								Clan target = ClansAPI.getInstance().getClan(id);
								target.takeMaxClaim(amount);
								lib.sendMessage(p, "Clan: &5" + target.getName() + " &dhas had &5&l" + amount + " &d max claim(s) removed.");
							} else {
								lib.sendMessage(p, lib.clanUnknown(args1));
								return true;
							}
						} catch (NumberFormatException ignored) {

						}
						break;
				}
				return true;
			}
			return true;
		}


		return false;
	}
}
