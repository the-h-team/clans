package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.api.GUI;
import com.github.sanctum.clans.construct.api.Insignia;
import com.github.sanctum.clans.construct.extra.FancyLogoAppendage;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.formatting.TextChunk;
import com.github.sanctum.labyrinth.formatting.ToolTip;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.library.Item;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.panther.util.HUID;
import com.github.sanctum.panther.util.RandomID;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandLogo extends ClanSubCommand {
	public CommandLogo() {
		super("logo");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {

			if (associate == null) {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}

			Clan c = associate.getClan();

			List<String> s = c.getValue(List.class, "logo");

			if (s != null) {
				lib.sendMessage(p, "&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				for (String line : s) {
					lib.sendMessage(p, line);
				}
				lib.sendMessage(p, "&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
			} else {
				lib.sendMessage(p, "&cOur clan doesn't have an official insignia");
			}

			return true;
		}

		if (args.length == 1) {

			if (associate == null) {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}

			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("logo")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("logo")));
				return true;
			}

			Clan c = associate.getClan();

			if (args[0].equalsIgnoreCase("edit")) {

				if (!Clearance.LOGO_EDIT.test(associate)) {
					lib.sendMessage(p, lib.noClearance());
					return true;
				}

				new Insignia.Builder("Template:" + p.getUniqueId()).setBorder("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").setColor("&2").setHeight(16).setWidth(16).draw(p);
				lib.sendMessage(p, "&eLoaded personal template.");
				return true;
			}

			if (args[0].equalsIgnoreCase("browse")) {
				GUI.LOGO_LIST.get().open(p);
				return true;
			}

			if (args[0].equalsIgnoreCase("carriers")) {
				GUI.HOLOGRAM_LIST.get(associate.getClan()).open(p);
				return true;
			}

			if (args[0].equalsIgnoreCase("share")) {
				if (!Clearance.LOGO_SHARE.test(associate)) {
					lib.sendMessage(p, lib.noClearance());
					return true;
				}
				String id = new RandomID().generate();
				Insignia i = Insignia.get("Template:" + p.getUniqueId());
				if (i != null) {
					ClansAPI.getInstance().getLogoGallery().load(id, i.getLines().stream().map(Insignia.Line::toString).collect(Collectors.toList()));
					lib.sendMessage(p, "&aInsignia successfully uploaded to the global logo gallery.");
					GUI.LOGO_LIST.get().open(p);
				} else {
					ItemStack item = p.getInventory().getItemInMainHand();

					if (item.getType() != Material.PAPER && item.getType() != Material.FILLED_MAP) {
						lib.sendMessage(p, "&cInvalid insignia request. Not an insignia print.");
						return true;
					}

					if (!item.hasItemMeta()) {
						lib.sendMessage(p, "&cInvalid insignia request. No lore to process.");
						return true;
					}

					if (item.getItemMeta().getLore() != null) {

						for (String lore : item.getItemMeta().getLore()) {
							if (isAlphaNumeric(ChatColor.stripColor(lore))) {
								lib.sendMessage(p, "&cInvalid insignia request. Error 420");
								return true;
							}
						}

						ClansAPI.getInstance().getLogoGallery().load(id, new ArrayList<>(item.getItemMeta().getLore()));
						lib.sendMessage(p, "&aInsignia successfully uploaded to the global logo gallery.");
						GUI.LOGO_LIST.get().open(p);

					}
				}
				return true;
			}

			if (args[0].equalsIgnoreCase("redraw")) {
				lib.sendMessage(p, "&cUsage: &f/c logo &eredraw &r[height] [width]");
				return true;
			}

			if (args[0].equalsIgnoreCase("upload")) {
				if (!Clearance.LOGO_UPLOAD.test(associate)) {
					lib.sendMessage(p, lib.noClearance());
					return true;
				}

				ItemStack item = p.getInventory().getItemInMainHand();

				if (item.getType() != Material.PAPER && item.getType() != Material.FILLED_MAP) {
					lib.sendMessage(p, "&cInvalid insignia request. Not an insignia print.");
					return true;
				}

				if (!item.hasItemMeta()) {
					lib.sendMessage(p, "&cInvalid insignia request. No lore to process.");
					return true;
				}

				if (item.getItemMeta().getLore() != null) {

					for (String lore : item.getItemMeta().getLore()) {
						if (isAlphaNumeric(ChatColor.stripColor(lore))) {
							lib.sendMessage(p, "&cInvalid insignia request. Error 420");
							return true;
						}
					}

					Mailer mail = associate.getMailer();
					List<String> logo = item.getItemMeta().getLore();
					int size = ChatColor.stripColor(logo.get(0)).length();
					mail.chat("&6&m&l" + Strings.repeat("▬", Math.min(38, size * 2))).deploy();
					FancyLogoAppendage appendage = ClansAPI.getDataInstance().appendStringsToLogo(logo, message -> message.hover("&2Do you even MLG?"));
					for (BaseComponent[] b : appendage.append(new TextChunk("Make this"),
							new TextChunk("your clan"),
							new TextChunk("logo?"),
							new TextChunk(" "),
							new TextChunk("&7[&6Yes&7]").bind(new ToolTip.Text("&2Click me to accept")).bind(new ToolTip.Action(() -> {
								c.setValue("logo", new ArrayList<>(logo), false);

								lib.sendMessage(p, "&aPrinted insignia applied to clan container.");
							}))).get()) {
						mail.chat(b).deploy();
					}
					mail.chat("&6&m&l" + Strings.repeat("▬", Math.min(38, size * 2))).deploy();


				} else {
					lib.sendMessage(p, "&cInvalid insignia request. What are you trying to pull...");
					return true;
				}

				return true;
			}

			if (args[0].equalsIgnoreCase("apply")) {
				if (!Clearance.LOGO_APPLY.test(associate)) {
					lib.sendMessage(p, lib.noClearance());
					return true;
				}

				Insignia i = Insignia.get("Template:" + p.getUniqueId());
				Insignia.copy(c.getId().toString(), i);
				if (i != null) {

					Mailer mail = associate.getMailer();
					List<String> logo = i.getLines().stream().map(Insignia.Line::toString).collect(Collectors.toList());
					int size = ChatColor.stripColor(logo.get(0)).length();
					mail.chat("&6&m&l" + Strings.repeat("▬", Math.min(38, size * 2))).deploy();
					FancyLogoAppendage appendage = ClansAPI.getDataInstance().appendStringsToLogo(logo, message -> message.hover("&2Do you even MLG?"));
					for (BaseComponent[] b : appendage.append(new TextChunk("Make this"),
							new TextChunk("your clan"),
							new TextChunk("logo?"),
							new TextChunk(" "),
							new TextChunk("&7[&6Yes&7]").bind(new ToolTip.Text("&2Click me to accept")).bind(new ToolTip.Action(() -> {
								c.setValue("logo", logo, false);

								lib.sendMessage(p, "&aCustom insignia applied to clan container.");
							}))).get()) {
						mail.chat(b).deploy();
					}
					mail.chat("&6&m&l" + Strings.repeat("▬", Math.min(38, size * 2))).deploy();

				} else {
					lib.sendMessage(p, "&cInvalid insignia request. No lore to process.");
				}

				return true;
			}
/*
				if (args1.equalsIgnoreCase("save")) {
					Insignia i = Insignia.get("Template:" + p.getUniqueId().toString());

					if (i != null) {

						lib.sendMessage(p, "&aChanges have been saved as a template.");
						c.setValue("logo_template_" + p.getUniqueId().toString(), i);

					} else {
						lib.sendMessage(p, "&cNo drawing was found.");
					}
					return true;
				}

 */

			if (args[0].equalsIgnoreCase("print")) {
				if (!Clearance.LOGO_PRINT.test(associate)) {
					lib.sendMessage(p, lib.noClearance());
					return true;
				}

				Insignia i = Insignia.get("Template:" + p.getUniqueId().toString());

				if (i != null) {
					p.getWorld().dropItem(p.getEyeLocation(), new Item.Edit(Material.PAPER).setTitle("(#" + HUID.randomID().toString().substring(0, 4) + ")").setLore(i.getLines().stream().map(Insignia.Line::toString).toArray(String[]::new)).build());

					lib.sendMessage(p, "&aInsignia template printed.");

				} else {
					new Insignia.Builder("Template:" + p.getUniqueId().toString()).setBorder("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").setColor("&2").setHeight(8).setWidth(16).draw(p);
					lib.sendMessage(p, "&cInvalid insignia request. No lore to process.");
				}

				return true;
			}

			final boolean b = !args[0].matches("(&#[a-zA-Z0-9]{6})+(&[a-zA-Z0-9])+") && !args[0].matches("(&[a-zA-Z0-9])+") && !args[0].matches("(&#[a-zA-Z0-9])+") && !args[0].matches("(#[a-zA-Z0-9])+");
			if (b) return true;
			new Insignia.Builder(c.getId().toString()).setHeight(16).setWidth(16).setColor(args[0]).draw(p);

			return true;
		}

		if (args.length == 2) {

			if (associate == null) {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}

			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("logo")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("logo")));
				return true;
			}

			if (args[0].equalsIgnoreCase("redraw")) {
				lib.sendMessage(p, "&cUsage: &f/c logo &eredraw &r[height] [width]");
				return true;
			}

			if (args[0].equalsIgnoreCase("color")) {

				if (!Clearance.LOGO_COLOR.test(associate)) {
					lib.sendMessage(p, lib.noClearance());
					return true;
				}

				if (!args[1].matches("(&#[a-zA-Z0-9]{6})+(&[a-zA-Z0-9])+") && !args[1].matches("(&[a-zA-Z0-9])+") && !args[1].matches("(&#[a-zA-Z0-9])+") && !args[1].matches("(#[a-zA-Z0-9])+")) {
					lib.sendMessage(p, "&c&oInvalid color format.");
					return true;
				}

				Insignia i = Insignia.get("Template:" + p.getUniqueId());

				if (i != null) {

					Mailer msg = Mailer.empty(p);

					i.setSelection(args[1]);

					msg.chat("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").deploy();
					for (BaseComponent[] components : i.get()) {
						p.spigot().sendMessage(components);
					}
					msg.chat("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").deploy();

					lib.sendMessage(p, "&aColor pallet updated to " + args[1] + "█");
				} else {
					lib.sendMessage(p, "&cNo drawing was found.");
				}

				return true;
			}
			return true;
		}

		if (args.length == 3) {

			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("logo")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("logo")));
				return true;
			}

			if (args[0].equalsIgnoreCase("redraw")) {
				if (!Clearance.LOGO_EDIT.test(associate)) {
					lib.sendMessage(p, lib.noClearance());
					return true;
				}

				try {

					int height = Integer.parseInt(args[1]);

					int width = Integer.parseInt(args[2]);

					Insignia i = Insignia.get("Template:" + p.getUniqueId());
					if (i != null) {
						i.remove();
						new Insignia.Builder("Template:" + p.getUniqueId()).setHeight(Math.min(Math.max(height, 6), 16)).setWidth(Math.min(Math.max(width, 3), 22)).draw(p);
						lib.sendMessage(p, "&cYou have reset your current insignia work space.");
					} else {
						lib.sendMessage(p, "&cYou have no insignia to reset.");
					}
				} catch (NumberFormatException e) {
					lib.sendMessage(p, "&cYou entered an invalid amount.");
				}
				return true;
			}

			return true;
		}

		return true;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, getBaseCompletion(args))
				.then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, "edit", "apply", "upload", "color", "redraw", "share", "browse", "carriers")
				.get();
	}
}
