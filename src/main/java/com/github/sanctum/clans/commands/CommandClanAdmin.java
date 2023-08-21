package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.bridge.ClanAddonQueue;
import com.github.sanctum.clans.bridge.internal.StashesAddon;
import com.github.sanctum.clans.bridge.internal.VaultsAddon;
import com.github.sanctum.clans.construct.ClanManager;
import com.github.sanctum.clans.construct.api.BanksAPI;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBlueprint;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.GUI;
import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.clans.construct.util.ReloadUtility;
import com.github.sanctum.clans.construct.util.StringLibrary;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.formatting.pagination.EasyPagination;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.util.HUID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
		setDescription("Used to modify clan settings.");
		setAliases(Arrays.asList("ca", "cla", "clanadmin"));
		setPermission("clanspro.admin");
	}

	private List<String> helpMenu(String label) {
		List<String> help = new ArrayList<>();
		help.add("&7|&e) &6/" + label + " &freload <&7configName&f>");
		help.add("&7|&e) &6/" + label + " &eupdate");
		help.add("&7|&e) &6/" + label + " &6claim <&7remove, current, surrounding&6>");
		help.add("&7|&e) &6/" + label + " &6edit <&7clan/player&6>");
		help.add("&7|&e) &6/" + label + " &fgetid <&7clanName&f>");
		help.add("&7|&e) &6/" + label + " &fidmode");
		help.add("&7|&e) &6/" + label + " &fpurge (&4Resets ALL persistent data&r)");
		help.add("&7|&e) &6/" + label + " &fspy <&7chatName&f>");
		help.add("&7|&e) &6/" + label + " &fkick <&7playerName&f>");
		help.add("&7|&e) &6/" + label + " &fput <&7playerName&f> <&7clanName&f>");
		help.add("&7|&e) &6/" + label + " &fview <&7clanName&f> <&7power, money, claims, vault, stash, password&f>");
		help.add("&7|&e) &6/" + label + " &dsettings");
		help.add("&7|&e) &6/" + label + " &ftphere <&7claneName&f>");
		help.add("&7|&e) &6/" + label + " &fgive <&7clanName&f> <power, money, claims, color> <&7amount&f, &7color&f>");
		help.add("&7|&e) &6/" + label + " &ftake <&7clanName&f> <power, money, claims> <&7amount&f>");
		help.add("&7|&e) &6/" + label + " &fset <&7clanName&f> <money> <&7amount&f> | &8[logo]");
		return help;
	}

	private final List<String> arguments = new ArrayList<>();

	Optional<Clan.Associate> obtainUser(UUID uuid, String clanName) {
		final ClanManager manager = ClansAPI.getInstance().getClanManager();
		if (!ClansAPI.getInstance().getAssociate(uuid).isPresent()) {
			HUID id = manager.getClanID(clanName);
			if (id != null) {
				Clan toJoin = manager.getClan(id);
				Clan.ACTION.join(uuid, clanName, toJoin.getPassword() != null ? toJoin.getPassword() : null, false).deploy();
				return Optional.ofNullable(toJoin.getMember(m -> m.getId().equals(uuid)));
			}
		}
		return Optional.empty();
	}

	boolean kickUser(UUID uuid) {
		boolean success = false;
		Clan test = ClansAPI.getInstance().getClanManager().getClan(uuid);
		if (test != null && test.getOwner().getId().equals(uuid)) {
			success = true;
			Clan.ACTION.remove(uuid, true).deploy();
		}
		return success;
	}

	@Override
	public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) throws IllegalArgumentException {


		List<String> result = new ArrayList<>();
		if (args.length == 1) {
			arguments.clear();
			arguments.addAll(Arrays.asList("reload", "kick", "tphere", "claim", "edit", "set", "put", "settings", "setspawn", "update", "getid", "idmode", "give", "take", "spy", "view", "purge", "tphere"));
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
				arguments.addAll(Arrays.asList("A", "B", "C", "D"));
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("claim")) {
				arguments.clear();
				arguments.addAll(Arrays.asList("current", "remove", "surrounding"));
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
			if (args[0].equalsIgnoreCase("edit")) {
				arguments.clear();
				List<String> toAdd = new ArrayList<>(Clan.ACTION.getAllClanNames());
				toAdd.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList()));
				arguments.addAll(toAdd);
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
			if (args[0].equalsIgnoreCase("claim")) {
				if (args[1].equalsIgnoreCase("remove")) {
					arguments.clear();
					arguments.addAll(Collections.singletonList("surrounding"));
					for (String a : arguments) {
						if (a.toLowerCase().startsWith(args[2].toLowerCase()))
							result.add(a);
					}
					return result;
				}
				arguments.clear();
				arguments.addAll(Clan.ACTION.getAllClanNames());
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[2].toLowerCase()))
						result.add(a);
				}
				return result;
			}
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
				arguments.addAll(Arrays.asList("money", "claims", "power", "stash", "vault", "password"));
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
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					ClansAPI.getInstance().getPlugin().getLogger().info("&7|&e) &fAlternative usage : /" + commandLabel + " reload <fileName>");
					ReloadUtility.reload();
					ClansAPI.getInstance().getPlugin().getLogger().info("&aPlugin reloaded!");
					return true;
				}
				if (args[0].equalsIgnoreCase("debug")) {
					ClansAPI.getInstance().debugConsole(ClansAPI.getInstance().getClanManager().getClans().toArray(Clan[]::new));
					return true;
				}
				return true;
			}
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("reload")) {
					FileManager file = ClansAPI.getInstance().getFileList().get(args[1], "Configuration");
					if (file.getRoot().exists()) {
						file.getRoot().reload();
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
			EasyPagination<String> pag = new EasyPagination<>(p, helpMenu(commandLabel));
			pag.limit(lib.menuSize());
			pag.setHeader((player, message) -> {
				message.then(lib.menuTitle());
				message.then("\n");
				message.then(lib.menuBorder());
			});
			pag.setFooter((player, message) -> {
				message.then(lib.menuBorder());
			});
			pag.setFormat((s, integer, message) -> {
				message.then(s);
			});
			pag.send(1);
			return true;
		}
		if (!Clan.ACTION.test(p, getPermission()).deploy()) {
			lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + '"');
			return true;
		}
		if (length == 1) {
			String args0 = args[0];
			if (args0.equalsIgnoreCase("copy")) {
				FileManager reg = ClansAPI.getInstance().getClaimManager().getFile();
				if (reg.getRoot().getType() != Configurable.Type.JSON) {
					if (reg.toJSON("regions", "Configuration").getRoot().save()) {
						lib.sendMessage(p, "&aRegions file copied to Json");
					}
				} else {
					if (!reg.getRoot().exists()) {
						FileManager m = ClansAPI.getInstance().getFileList().get("Regions", "Configuration");
						if (m.toJSON("regions", "Configuration").getRoot().save()) {
							lib.sendMessage(p, "&aRegions file copied to Json");
						}
					}
				}
				for (Clan c : ClansAPI.getInstance().getClanManager().getClans()) {
					FileManager f = ClansAPI.getDataInstance().getClanFile(c);
					if (f.getRoot().getType() != Configurable.Type.JSON) {
						if (f.toJSON().getRoot().save()) {
							lib.sendMessage(p, "&aClan " + c.getName() + " file copied to Json");
						}
					}
				}
				return true;
			}
			if (args0.equalsIgnoreCase("reload")) {
				lib.sendMessage(p, "&7|&e) &fAlternative usage : /" + commandLabel + " reload <fileName>");
				ReloadUtility.reload();
				lib.sendMessage(p, "&aPlugin reloaded!");
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
				if (ClansAPI.getDataInstance().isSpy(p)) {
					ClansAPI.getDataInstance().removeSpy(p);
					lib.sendMessage(p, "&cNo longer spying on chat channels.");
				} else {
					ClansAPI.getDataInstance().addSpy(p);
					lib.sendMessage(p, "&aNow spying on chat channels.");
				}
				return true;
			}
			if (args0.equalsIgnoreCase("settings")) {
				GUI.SETTINGS_SELECT.get().open(p);
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
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /" + commandLabel + " setspawn <&9A, B, C, D&f>");
				return true;
			}
			if (args0.equalsIgnoreCase("view")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /" + commandLabel + " view <clanName> <money, claims, power, vault, stash>");
				return true;
			}
			if (args0.equalsIgnoreCase("update")) {
				if (!ClansAPI.getDataInstance().updateConfigs()) {
					lib.sendMessage(p, "&3&oThe configuration is already up to date.");
				} else {
					lib.sendMessage(p, "&aUpdated configuration to latest...");
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("idmode")) {
				if (!ClansAPI.getDataInstance().ID_MODE.containsKey(p)) {
					ClansAPI.getDataInstance().ID_MODE.put(p, "ENABLED");
					lib.sendMessage(p, "&f[&a&oADMIN&f] &6&lID &fmode &aENABLED.");
					return true;
				}
				if (ClansAPI.getDataInstance().ID_MODE.get(p).equals("ENABLED")) {
					ClansAPI.getDataInstance().ID_MODE.put(p, "DISABLED");
					lib.sendMessage(p, "&f[&a&oADMIN&f] &6&lID &fmode &cDISABLED.");
					return true;
				}
				if (ClansAPI.getDataInstance().ID_MODE.get(p).equals("DISABLED")) {
					ClansAPI.getDataInstance().ID_MODE.put(p, "ENABLED");
					lib.sendMessage(p, "&f[&a&oADMIN&f] &6&lID &fmode &aENABLED.");
					return true;
				}
				return true;
			}
			EasyPagination<String> pag = new EasyPagination<>(p, helpMenu(commandLabel));
			pag.limit(lib.menuSize());
			pag.setHeader((player, message) -> {
				message.then(lib.menuTitle());
				message.then("\n");
				message.then(lib.menuBorder());
			});
			pag.setFooter((player, message) -> {
				message.then(lib.menuBorder());
			});
			pag.setFormat((s, integer, message) -> {
				message.then(s);
			});
			try {
				pag.send(Integer.parseInt(args0));
			} catch (NumberFormatException e) {
				lib.sendMessage(p, "&c&oInvalid page number!");
			}
			return true;
		}

		if (length == 2) {
			String args0 = args[0];
			String args1 = args[1];
			if (args0.equalsIgnoreCase("close")) {
				if (ClansAPI.getInstance().getClanManager().getClanID(args1) != null) {
					Clan target = ClansAPI.getInstance().getClanManager().getClan(ClansAPI.getInstance().getClanManager().getClanID(args1));
					for (Clan.Associate id : target.getMembers()) {
						if (id.getRank().getLevel() >= 3) {
							target.broadcast("&8(&e!&8) &4&oOur clan has been forcibly closed by a staff member.");
							Clan.ACTION.remove(id.getId(), false).deploy();
							break;
						}
					}
				} else {
					lib.sendMessage(p, lib.clanUnknown(args1));
					return true;
				}
			}
			if (args0.equalsIgnoreCase("claim")) {
				if (args1.equalsIgnoreCase("remove")) {
					Claim test = ClansAPI.getInstance().getClaimManager().getClaim(p.getLocation());
					if (test != null) {
						TaskScheduler.of(test::remove).schedule();
					} else {
						lib.sendMessage(p, "&cYou're not in a claim!");
					}
				}
				if (args1.equalsIgnoreCase("current")) {
					lib.sendMessage(p, "&cNot enough arguments! Expected a clan name");
				}
				if (args1.equalsIgnoreCase("surrounding")) {
					lib.sendMessage(p, "&cNot enough arguments! Expected a clan name");
				}
				return true;
			}
			if (args0.equalsIgnoreCase("edit")) {
				ClanManager manager = ClansAPI.getInstance().getClanManager();
				HUID test = manager.getClanID(args1);
				if (test != null) {
					Clan clan = manager.getClan(test);
					GUI.SETTINGS_CLAN.get(clan).open(p);
				} else {
					UUID test2 = Clan.ACTION.getId(args1).deploy();
					if (test2 != null) {
						Clan.Associate associate = ClansAPI.getInstance().getAssociate(test2).orElse(null);
						if (associate != null) {
							GUI.SETTINGS_MEMBER.get(associate).open(p);
						} else {
							lib.sendMessage(p, "They're not in a clan!");
						}
					} else {
						lib.sendMessage(p, lib.clanUnknown(args1));
					}
				}
			}
			if (args0.equalsIgnoreCase("purge")) {
				if (ClansAPI.getInstance().getClanManager().getClanID(args1) != null) {
					Clan target = ClansAPI.getInstance().getClanManager().getClan(ClansAPI.getInstance().getClanManager().getClanID(args1));
					int amount = 0;
					for (String data : target.getKeys()) {
						target.removeValue(data);
						amount++;
					}
					lib.sendMessage(p, "&3&l" + amount + " &aobject(s) were successfully removed.");
				} else {

					if (args1.equalsIgnoreCase("all")) {
						int amount = 0;
						for (Clan target : ClansAPI.getInstance().getClanManager().getClans()) {
							for (String data : target.getKeys()) {
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
				HUID targetId = ClansAPI.getInstance().getClanManager().getClanID(args1);
				Clan c = ClansAPI.getInstance().getClanManager().getClan(targetId);
				if (targetId != null) {
					for (Clan.Associate associate : c.getMembers()) {
						if (associate.getTag().getPlayer().isOnline()) {
							associate.getTag().getPlayer().getPlayer().teleport(p);
							associate.getMailer().chat("&aYou've been teleported by a staff member.").deploy();
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
				if (Clan.ACTION.getId(args1).deploy() == null) {
					lib.sendMessage(p, lib.playerUnknown(args1));
					return true;
				}
				UUID target = Clan.ACTION.getId(args1).deploy();
				if (target.equals(p.getUniqueId())) {
					lib.sendMessage(p, "&c&oInvalid usage, try &6/c leave");
					return true;
				}
				if (!kickUser(target)) {
					lib.sendMessage(p, "&c&oPlayer " + args1 + " isn't in a clan or is the leader (if so use &6/cla close <clanName>&c&o).");
				} else {
					lib.sendMessage(p, "&3&oPlayer " + args1 + " was kicked from their clan.");
					if (Bukkit.getOfflinePlayer(target).isOnline()) {
						lib.sendMessage(Bukkit.getPlayer(target), ClansAPI.getDataInstance().getMessageResponse("removed-out"));
					}
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("setspawn")) {
				switch (args1.toUpperCase()) {
					case "A":
						War.setSpawn(War.Team.A, p.getLocation());
						break;
					case "B":
						War.setSpawn(War.Team.B, p.getLocation());
						break;
					case "C":
						War.setSpawn(War.Team.C, p.getLocation());
						break;
					case "D":
						War.setSpawn(War.Team.D, p.getLocation());
						break;
				}
				lib.sendMessage(p, "&aUpdated team '&b" + args1.toUpperCase() + "&a' spawn location.");
				return true;
			}
			if (args0.equalsIgnoreCase("getid")) {
				Player target = Bukkit.getPlayer(args1);
				if (target == null) {

					try {
						lib.sendMessage(p, "&7#&fID &7of clan " + '"' + args1 + '"' + " is: &e&o" + ClansAPI.getInstance().getClanManager().getClanID(args1));
					} catch (NullPointerException e) {
						lib.sendMessage(p, "&c&oUh-oh there was an issue finding the clan.. Check console for errors");
						ClansAPI.getInstance().getPlugin().getLogger().severe(String.format("[%s] - Illegal use of ID retrieval. Clan directory non-existent.", ClansAPI.getInstance().getPlugin().getDescription().getName()));
					}
					return true;
				}

				lib.sendMessage(p, "&7|&e) &6&l" + target.getName() + "'s &e&oclan ID is &f" + ClansAPI.getInstance().getClanManager().getClanID(target.getUniqueId()).toString());
				return true;
			}
			if (args0.equalsIgnoreCase("reload")) {
				FileManager file = ClansAPI.getInstance().getFileList().get(args[1], "Configuration");
				if (file.getRoot().exists()) {
					file.getRoot().reload();
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
			if (args0.equalsIgnoreCase("claim")) {
				if (args1.equalsIgnoreCase("remove")) {
					if (args2.equalsIgnoreCase("surrounding")) {
						final Clan[] owner = {null};
						Claim.ACTION.getSurroundingChunks(p.getLocation().getChunk(), -1, 0, 1).deploy().forEach(chunk -> {
							Claim test = ClansAPI.getInstance().getClaimManager().getClaim(chunk);
							if (test != null) {
								if (owner[0] == null) {
									owner[0] = ((Clan) test.getHolder());
								}
								test.remove();
							}
						});
						if (owner[0] != null) {
							lib.sendMessage(p, "&aUnclaimed surrounding land owned by &r" + owner[0].getName());
						}
					}
				}
				if (args1.equalsIgnoreCase("current")) {
					ClanManager manager = ClansAPI.getInstance().getClanManager();
					HUID test = manager.getClanID(args2);
					if (test != null) {
						Clan clan = manager.getClan(test);
						Claim claim = clan.newClaim(p.getLocation());
						if (claim != null) {
							lib.sendMessage(p, "&aYou claimed this land for clan &r" + clan.getName());
						}
					} else {
						lib.sendMessage(p, lib.clanUnknown(args2));
					}
				}
				if (args1.equalsIgnoreCase("surrounding")) {
					ClanManager manager = ClansAPI.getInstance().getClanManager();
					HUID test = manager.getClanID(args2);
					if (test != null) {
						Clan clan = manager.getClan(test);
						Claim.ACTION.getSurroundingChunks(p.getLocation().getChunk(), -1, 0, 1).deploy().forEach(clan::newClaim);
						lib.sendMessage(p, "&aYou claimed this land for clan &r" + clan.getName());
					} else {
						lib.sendMessage(p, lib.clanUnknown(args2));
					}
				}
				return true;
			}
			if (args0.equalsIgnoreCase("put")) {
				if (Clan.ACTION.getId(args1).deploy() == null) {
					lib.sendMessage(p, lib.playerUnknown(args1));
					return true;
				}
				UUID target = Clan.ACTION.getId(args1).deploy();
				if (target.equals(p.getUniqueId())) {
					lib.sendMessage(p, "&c&oWhat are you even trying to test?");
					return true;
				}
				Clan.Associate n = obtainUser(target, args2).orElse(null);
				if (n == null) {
					lib.sendMessage(p, "&c&oPlayer " + args1 + " is already in a clan or the clan specified doesn't exist.");
				} else {
					lib.sendMessage(p, "&3&oPlayer " + args1 + " was placed into clan " + (n.getClan().getNickname() != null ? n.getClan().getNickname() : n.getClan().getName()));
					if (Bukkit.getOfflinePlayer(target).isOnline()) {
						lib.sendMessage(Bukkit.getPlayer(target), "&5&oA staff member has placed you into clan " + args2);
					}
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("set")) {
				HUID id = ClansAPI.getInstance().getClanManager().getClanID(args1);
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
								Clan target = ClansAPI.getInstance().getClanManager().getClan(id);
								target.setValue("logo", new ArrayList<>(item.getItemMeta().getLore()), false);
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
				HUID id = ClansAPI.getInstance().getClanManager().getClanID(args1);
				switch (args2.toLowerCase()) {
					case "power":
						if (id != null) {
							Clan target = ClansAPI.getInstance().getClanManager().getClan(id);
							lib.sendMessage(p, "Clan: &5" + target.getName() + " &dhas a power level of &5&l" + Clan.ACTION.format(target.getPower()));
						} else {
							lib.sendMessage(p, lib.clanUnknown(args1));
							return true;
						}
						break;
					case "password":
						if (id != null) {
							Clan target = ClansAPI.getInstance().getClanManager().getClan(id);
							lib.sendMessage(p, "Clan: &5" + target.getName() + " &dpassword: &5&l" + target.getPassword());
						} else {
							lib.sendMessage(p, lib.clanUnknown(args1));
							return true;
						}
						break;
					case "money":
						if (id != null) {
							Clan target = ClansAPI.getInstance().getClanManager().getClan(id);
							if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
								lib.sendMessage(p, "&c&oNo economy interface found. Bank feature disabled.");
								return true;
							}
							lib.sendMessage(p, "Clan: &5" + target.getName() + " &dhas a bank balance of &6&l" + Clan.ACTION.format(BanksAPI.getInstance().getBank(target).getBalanceDouble()));
						} else {
							lib.sendMessage(p, lib.clanUnknown(args1));
							return true;
						}
						break;
					case "claims":
						if (id != null) {
							Clan target = ClansAPI.getInstance().getClanManager().getClan(id);
							lib.sendMessage(p, "Clan: &5" + target.getName() + " &dhas a claim limit of &c&l" + Clan.ACTION.format(target.getClaimLimit()));
						} else {
							lib.sendMessage(p, lib.clanUnknown(args1));
							return true;
						}
						break;
					case "vault":
						if (id != null) {
							Clan target = ClansAPI.getInstance().getClanManager().getClan(id);
							if (ClanAddonQueue.getInstance().getEnabled().contains("Vaults")) {
								VaultsAddon.getVault(target.getName()).open(p);
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
							Clan target = ClansAPI.getInstance().getClanManager().getClan(id);
							if (ClanAddonQueue.getInstance().getEnabled().contains("Stashes")) {
								StashesAddon.getStash(target.getName()).open(p);
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
				if (Clan.ACTION.getId(args1).deploy() == null) {
					lib.sendMessage(p, lib.playerUnknown(args1));
					return true;
				}
				UUID target = Clan.ACTION.getId(args1).deploy();

				if (ClansAPI.getInstance().getAssociate(target).isPresent()) {
					lib.sendMessage(p, "&c&oPlayer " + args1 + " is already in a clan.");
					return true;
				}

				if (ClansAPI.getInstance().getClanManager().getClanID(args2) != null) {
					lib.sendMessage(p, "&c&oClan " + args2 + " already exists.");
				} else {
					lib.sendMessage(p, "&3&oPlayer " + args1 + " was placed into clan " + args2);
					ClansAPI.getInstance().getClanManager().load(new ClanBlueprint(args2, true).setLeader(target)
							.setPassword(amountPre)
							.toBuilder().build().givePower(1.0).getClan());
					if (Bukkit.getOfflinePlayer(target).isOnline()) {
						lib.sendMessage(Bukkit.getPlayer(target), "&5&oA staff member has placed you into clan " + args2);
					}
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("set")) {
				HUID id = ClansAPI.getInstance().getClanManager().getClanID(args1);
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
								Clan target = ClansAPI.getInstance().getClanManager().getClan(id);
								target.setValue("logo", new ArrayList<>(item.getItemMeta().getLore()), false);
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
								Clan target = ClansAPI.getInstance().getClanManager().getClan(id);
								if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
									lib.sendMessage(p, "&c&oNo economy interface found. Bank feature disabled.");
									return true;
								}
								BanksAPI.getInstance().getBank(target).setBalanceDouble(amount);
								target.broadcast("Our clan bank was just adjusted to: &a&o" + BanksAPI.getInstance().getBank(target).getBalanceDouble());
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
				HUID id = ClansAPI.getInstance().getClanManager().getClanID(args1);
				switch (args2.toLowerCase()) {
					case "power":
						try {
							double amount = Double.parseDouble(amountPre);
							if (id != null) {
								Clan target = ClansAPI.getInstance().getClanManager().getClan(id);
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
								Clan target = ClansAPI.getInstance().getClanManager().getClan(id);
								if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
									lib.sendMessage(p, "&c&oNo economy interface found. Bank feature disabled.");
									return true;
								}
								double result = BanksAPI.getInstance().getBank(target).getBalanceDouble() + amount;
								BanksAPI.getInstance().getBank(target).setBalanceDouble(result);
								target.broadcast("Our clan was just payed: &a&o" + amountPre);
								lib.sendMessage(p, "Clan: &5" + target.getName() + " &dhas been paid &5&l" + amountPre + " &dand now has &6" + Clan.ACTION.format(BanksAPI.getInstance().getBank(target).getBalanceDouble()));
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
								Clan target = ClansAPI.getInstance().getClanManager().getClan(id);
								target.giveClaims(amount);
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
							Clan target = ClansAPI.getInstance().getClanManager().getClan(id);

							if (!args[3].matches("(&#[a-zA-Z0-9]{6})+(&[a-zA-Z0-9])+") && !args[3].matches("(&#[a-zA-Z0-9]{6})+|(&[a-zA-Z0-9])+")) {
								lib.sendMessage(p, "&c&oInvalid color format.");
								return true;
							}

							for (String s : ClansAPI.getDataInstance().getConfig().getRoot().getStringList("Clans.color-blacklist")) {

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
				HUID id = ClansAPI.getInstance().getClanManager().getClanID(args1);
				switch (args2.toLowerCase()) {
					case "power":
						try {
							double amount = Double.parseDouble(amountPre);
							if (id != null) {
								Clan target = ClansAPI.getInstance().getClanManager().getClan(id);
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
								Clan target = ClansAPI.getInstance().getClanManager().getClan(id);
								double result = BanksAPI.getInstance().getBank(target).getBalanceDouble() - amount;
								BanksAPI.getInstance().getBank(target).setBalanceDouble(result);
								target.broadcast("Our clan was just charged: &c&o" + amountPre);
								lib.sendMessage(p, "Clan: &5" + target.getName() + " &dhas been charged &5&l" + amountPre + " &dand now has &6" + Clan.ACTION.format(BanksAPI.getInstance().getBank(target).getBalanceDouble()));
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
								Clan target = ClansAPI.getInstance().getClanManager().getClan(id);
								target.takeClaims(amount);
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
