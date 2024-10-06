package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.Claim;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.Clearance;
import com.github.sanctum.clans.model.GUI;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class CommandClaim extends ClanSubCommand {
	public CommandClaim() {
		super("claim");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.claim.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("claim")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("claim")));
				return true;
			}
			if (Claim.ACTION.isAllowed().deploy()) {
				if (associate != null) {
					if (Clearance.MANAGE_LAND.test(associate)) {
						if (!ClansAPI.getInstance().getClaimManager().testBlockBuildPermission(p, p.getLocation().getBlock())) {
							lib.sendMessage(p, MessageFormat.format(lib.notClaimOwner("Third Party"), "Third Party"));
							return true;
						}
						if (ClansAPI.getDataInstance().getConfigString("Clans.raid-shield.mode").equals("TEMPORARY")) {
							if (!ClansAPI.getInstance().getShieldManager().isEnabled()) {
								lib.sendMessage(p, "&cYou cannot do this while the shield is down.");
								return true;
							}
						}
						Claim.ACTION.claim(p).run();
					} else {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
			} else {
				lib.sendMessage(p, "&c&oYour server doesn't allow the use of clan land-claiming.");
				return true;
			}
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("claim")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("claim")));
				return true;
			}
			if (args[0].equalsIgnoreCase("enter-title")) {
				if (associate != null) {
					if (Clearance.MANAGE_LAND.test(associate)) {
						String result = associate.getClan().getValue(String.class, "claim_title");
						if (result != null) {
							lib.sendMessage(p, "&aOur current enter claim title is: &r" + result);
						} else {
							lib.sendMessage(p, "&cWe have no custom enter title setup.");
						}
					} else {
						lib.sendMessage(p, lib.noClearance());
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("enter-sub-title")) {
				if (associate != null) {
					if (Clearance.MANAGE_LAND.test(associate)) {
						String result = associate.getClan().getValue(String.class, "claim_sub_title");
						if (result != null) {
							lib.sendMessage(p, "&aOur current enter claim sub-title is: &r" + result);
						} else {
							lib.sendMessage(p, "&cWe have no custom enter sub-title setup.");
						}
					} else {
						lib.sendMessage(p, lib.noClearance());
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("leave-title")) {
				if (associate != null) {
					if (Clearance.MANAGE_LAND.test(associate)) {
						String result = associate.getClan().getValue(String.class, "leave_claim_title");
						if (result != null) {
							lib.sendMessage(p, "&aOur current leave claim title is: &r" + result);
						} else {
							lib.sendMessage(p, "&cWe have no custom leave title setup.");
						}
					} else {
						lib.sendMessage(p, lib.noClearance());
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("leave-sub-title")) {
				if (associate != null) {
					if (Clearance.MANAGE_LAND.test(associate)) {
						String result = associate.getClan().getValue(String.class, "leave_claim_sub_title");
						if (result != null) {
							lib.sendMessage(p, "&aOur current leave claim sub-title is: &r" + result);
						} else {
							lib.sendMessage(p, "&cWe have no custom leave sub-title setup.");
						}
					} else {
						lib.sendMessage(p, lib.noClearance());
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("list")) {
				if (associate != null) {
					if (Clearance.MANAGE_LAND.test(associate)) {
						GUI.CLAIM_LIST.get(associate.getClan()).open(p);
					} else {
						lib.sendMessage(p, lib.noClearance());
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("flags")) {

				if (Claim.ACTION.isAllowed().deploy()) {
					if (associate != null) {
						if (!Clearance.MANAGE_LAND.test(associate)) {
							lib.sendMessage(p, lib.noClearance());
							return true;
						}
						Claim test = ClansAPI.getInstance().getClaimManager().getClaim(p.getLocation());
						if (test != null) {
							Set<Claim.Flag> set = Arrays.stream(test.getFlags().clone()).sorted((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getId(), o2.getId())).sorted(Claim.Flag::compareTo).sorted(Comparator.reverseOrder()).collect(Collectors.toCollection(LinkedHashSet::new));
							Claim.ACTION.getFlags(p, test, set).deploy().send(1);
						} else {
							lib.sendMessage(p, lib.alreadyWild());
						}
					} else {
						lib.sendMessage(p, lib.notInClan());
						return true;
					}
				} else {
					lib.sendMessage(p, "&c&oYour server doesn't allow the use of clan land-claiming.");
					return true;
				}
			}
			return true;
		}

		if (args.length == 2) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("claim")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("claim")));
				return true;
			}
			if (args[0].equalsIgnoreCase("enter-title")) {
				if (associate != null) {
					if (Clearance.MANAGE_ALL_LAND.test(associate)) {
						associate.getClan().setValue("claim_title", args[1], false);
						lib.sendMessage(p, "&aTitle updated.");
					} else {
						lib.sendMessage(p, lib.noClearance());
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("enter-sub-title")) {
				if (associate != null) {
					if (Clearance.MANAGE_ALL_LAND.test(associate)) {
						associate.getClan().setValue("claim_sub_title", args[1], false);
						lib.sendMessage(p, "&aTitle updated.");
					} else {
						lib.sendMessage(p, lib.noClearance());
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("leave-title")) {
				if (associate != null) {
					if (Clearance.MANAGE_ALL_LAND.test(associate)) {
						associate.getClan().setValue("leave_claim_title", args[1], false);
						lib.sendMessage(p, "&aTitle updated.");
					} else {
						lib.sendMessage(p, lib.noClearance());
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("leave-sub-title")) {
				if (associate != null) {
					if (Clearance.MANAGE_ALL_LAND.test(associate)) {
						associate.getClan().setValue("leave_claim_sub_title", args[1], false);
						lib.sendMessage(p, "&aTitle updated.");
					} else {
						lib.sendMessage(p, lib.noClearance());
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
		}

		if (args.length == 3) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("claim")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("claim")));
				return true;
			}
			if (args[0].equalsIgnoreCase("enter-title")) {
				if (Clearance.MANAGE_ALL_LAND.test(associate)) {
					associate.getClan().setValue("claim_title", args[1] + " " + args[2], false);
					lib.sendMessage(p, "&aTitle updated.");
				} else {
					lib.sendMessage(p, lib.noClearance());
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("enter-sub-title")) {
				if (Clearance.MANAGE_ALL_LAND.test(associate)) {
					associate.getClan().setValue("claim_sub_title", args[1] + " " + args[2], false);
					lib.sendMessage(p, "&aTitle updated.");
				} else {
					lib.sendMessage(p, lib.noClearance());
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("leave-title")) {
				if (Clearance.MANAGE_ALL_LAND.test(associate)) {
					associate.getClan().setValue("leave_claim_title", args[1] + " " + args[2], false);
					lib.sendMessage(p, "&aTitle updated.");
				} else {
					lib.sendMessage(p, lib.noClearance());
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("leave-sub-title")) {
				if (Clearance.MANAGE_ALL_LAND.test(associate)) {
					associate.getClan().setValue("leave_claim_sub_title", args[1] + " " + args[2], false);
					lib.sendMessage(p, "&aTitle updated.");
				} else {
					lib.sendMessage(p, lib.noClearance());
				}
				return true;
			}
		}
		StringBuilder rsn = new StringBuilder();
		for (int i = 1; i < args.length; i++)
			rsn.append(args[i]).append(" ");
		if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("claim")).deploy()) {
			lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("claim")));
			return true;
		}
		if (args[0].equalsIgnoreCase("enter-title")) {
			if (associate != null) {
				if (Clearance.MANAGE_ALL_LAND.test(associate)) {
					String result = rsn.toString().trim();
					if (result.length() >= 34) {
						lib.sendMessage(p, "&cYour title message is too long!");
						return true;
					}
					associate.getClan().setValue("claim_title", result, false);
					lib.sendMessage(p, "&aEnter title updated.");
				} else {
					lib.sendMessage(p, lib.noClearance());
				}
			} else {
				lib.sendMessage(p, lib.notInClan());
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("enter-sub-title")) {
			if (associate != null) {
				if (Clearance.MANAGE_ALL_LAND.test(associate)) {
					String result = rsn.toString().trim();
					if (result.length() >= 34) {
						lib.sendMessage(p, "&cYour title message is too long!");
						return true;
					}
					associate.getClan().setValue("claim_sub_title", result, false);
					lib.sendMessage(p, "&aEnter sub-title updated.");
				} else {
					lib.sendMessage(p, lib.noClearance());
				}
			} else {
				lib.sendMessage(p, lib.notInClan());
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("leave-title")) {
			if (associate != null) {
				if (Clearance.MANAGE_ALL_LAND.test(associate)) {
					String result = rsn.toString().trim();
					if (result.length() >= 34) {
						lib.sendMessage(p, "&cYour title message is too long!");
						return true;
					}
					associate.getClan().setValue("leave_claim_title", result, false);
					lib.sendMessage(p, "&aLeave title updated.");
				} else {
					lib.sendMessage(p, lib.noClearance());
				}
			} else {
				lib.sendMessage(p, lib.notInClan());
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("leave-sub-title")) {
			if (associate != null) {
				if (Clearance.MANAGE_ALL_LAND.test(associate)) {
					String result = rsn.toString().trim();
					if (result.length() >= 34) {
						lib.sendMessage(p, "&cYour title message is too long!");
						return true;
					}
					associate.getClan().setValue("leave_claim_sub_title", result, false);
					lib.sendMessage(p, "&aLeave sub-title updated.");
				} else {
					lib.sendMessage(p, lib.noClearance());
				}
			} else {
				lib.sendMessage(p, lib.notInClan());
			}
			return true;
		}
		return true;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, getBaseCompletion(args))
				.then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, "flags", "list", "enter-title", "enter-sub-title", "leave-title", "leave-sub-title")
				.get();
	}
}
