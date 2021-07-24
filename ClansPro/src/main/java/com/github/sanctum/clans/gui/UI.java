package com.github.sanctum.clans.gui;

import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.RankPriority;
import com.github.sanctum.clans.util.data.DataManager;
import com.github.sanctum.labyrinth.data.AdvancedHook;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.VaultHook;
import com.github.sanctum.labyrinth.formatting.string.ColoredString;
import com.github.sanctum.labyrinth.formatting.string.Paragraph;
import com.github.sanctum.labyrinth.gui.InventoryRows;
import com.github.sanctum.labyrinth.gui.menuman.Menu;
import com.github.sanctum.labyrinth.gui.menuman.MenuBuilder;
import com.github.sanctum.labyrinth.gui.menuman.PaginatedBuilder;
import com.github.sanctum.labyrinth.gui.menuman.PaginatedClickAction;
import com.github.sanctum.labyrinth.gui.menuman.PaginatedCloseAction;
import com.github.sanctum.labyrinth.gui.printer.AnvilBuilder;
import com.github.sanctum.labyrinth.gui.printer.AnvilMenu;
import com.github.sanctum.labyrinth.library.Entities;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Item;
import com.github.sanctum.labyrinth.library.Items;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.link.CycleList;
import com.github.sanctum.link.EventCycle;
import com.github.sanctum.skulls.CustomHead;
import com.github.sanctum.skulls.SkullType;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class UI {

	private static final Map<UUID, ClanEditOperation.Option> CLAN_OPTION_MAP = new HashMap<>();
	private static final Map<UUID, ClanEditOperation> CLAN_EDIT_OPERATION_MAP = new HashMap<>();
	private static final Map<UUID, MemberEditOperation.Option> MEMBER_OPTION_MAP = new HashMap<>();
	private static final Map<UUID, MemberEditOperation> MEMBER_EDIT_OPERATION_MAP = new HashMap<>();
	private static final Map<UUID, Paginated> LAST_MENU = new HashMap<>();


	private static ItemStack getLeft() {
		ItemStack left = ClansAPI.getData().getItem("navigate_left");
		ItemMeta meta = left.getItemMeta();
		meta.setDisplayName(StringUtils.use(ClansAPI.getData().getNavigate("left")).translate());
		left.setItemMeta(meta);
		return left;
	}

	private static ItemStack getRight() {
		ItemStack right = ClansAPI.getData().getItem("navigate_right");
		ItemMeta meta = right.getItemMeta();
		meta.setDisplayName(StringUtils.use(ClansAPI.getData().getNavigate("right")).translate());
		right.setItemMeta(meta);
		return right;
	}

	private static ItemStack getBack() {
		ItemStack back = ClansAPI.getData().getItem("back");
		ItemMeta meta = back.getItemMeta();
		meta.setDisplayName(StringUtils.use(ClansAPI.getData().getNavigate("back")).translate());
		back.setItemMeta(meta);
		return back;
	}

	public static InventoryRows getRows() {
		return InventoryRows.valueOf(ClansAPI.getData().getPath("pagination-size"));
	}

	public static int getSwitchSlot() {
		int amnt = 0;
		switch (getRows().getSlotCount()) {
			case 9:
				amnt = 5;
				break;
			case 18:
				amnt = 14;
				break;
			case 27:
				amnt = 18;
				break;
			case 36:
				amnt = 27;
				break;
			case 45:
				amnt = 36;
				break;
			case 54:
				amnt = 45;
				break;
		}
		return amnt;
	}

	public static int getBackSlot() {
		int amnt = 0;
		switch (getRows().getSlotCount()) {
			case 9:
				amnt = 7;
				break;
			case 18:
				amnt = 16;
				break;
			case 27:
				amnt = 22;
				break;
			case 36:
				amnt = 31;
				break;
			case 45:
				amnt = 40;
				break;
			case 54:
				amnt = 49;
				break;
		}
		return amnt;
	}

	public static int getRightSlot() {
		int amnt = 0;
		switch (getRows().getSlotCount()) {
			case 9:
				amnt = 8;
				break;
			case 18:
				amnt = 17;
				break;
			case 27:
				amnt = 23;
				break;
			case 36:
				amnt = 32;
				break;
			case 45:
				amnt = 41;
				break;
			case 54:
				amnt = 50;
				break;
		}
		return amnt;
	}

	public static int getMiscSlot() {
		int amnt = 0;
		switch (getRows().getSlotCount()) {
			case 9:
				amnt = 4;
				break;
			case 18:
				amnt = 13;
				break;
			case 27:
				amnt = 19;
				break;
			case 36:
				amnt = 28;
				break;
			case 45:
				amnt = 37;
				break;
			case 54:
				amnt = 46;
				break;
		}
		return amnt;
	}

	public static int getLeftSlot() {
		int amnt = 0;
		switch (getRows().getSlotCount()) {
			case 9:
				amnt = 6;
				break;
			case 18:
				amnt = 15;
				break;
			case 27:
				amnt = 21;
				break;
			case 36:
				amnt = 30;
				break;
			case 45:
				amnt = 39;
				break;
			case 54:
				amnt = 48;
				break;
		}
		return amnt;
	}

	public static int getAmntPer() {
		int amnt = 0;
		switch (getRows().getSlotCount()) {
			case 9:
				amnt = 6;
				break;
			case 18:
				amnt = 15;
				break;
			case 27:
				amnt = 7;
				break;
			case 36:
				amnt = 14;
				break;
			case 45:
				amnt = 21;
				break;
			case 54:
				amnt = 28;
				break;
		}
		return amnt;
	}

	public static void clearOperations(UUID id) {
		if (CLAN_OPTION_MAP.containsKey(id)) {
			CLAN_OPTION_MAP.remove(id);
			CLAN_EDIT_OPERATION_MAP.remove(id);
		}
		if (MEMBER_OPTION_MAP.containsKey(id)) {
			MEMBER_EDIT_OPERATION_MAP.remove(id);
			MEMBER_OPTION_MAP.remove(id);
		}
	}

	public static ClanEditOperation.Option getClanEditOption(UUID id) {
		return CLAN_OPTION_MAP.getOrDefault(id, null);
	}

	public static ClanEditOperation getClanEditOperation(UUID id) {
		return CLAN_EDIT_OPERATION_MAP.getOrDefault(id, null);
	}

	public static MemberEditOperation.Option getMemberEditOption(UUID id) {
		return MEMBER_OPTION_MAP.getOrDefault(id, null);
	}

	public static MemberEditOperation getMemberEditOperation(UUID id) {
		return MEMBER_EDIT_OPERATION_MAP.getOrDefault(id, null);
	}

	protected static List<String> color(String... text) {
		ArrayList<String> convert = new ArrayList<>();
		for (String t : text) {
			if (Bukkit.getVersion().contains("1.16")) {
				convert.add(new ColoredString(t, ColoredString.ColorType.HEX).toString());
			} else {
				convert.add(new ColoredString(t, ColoredString.ColorType.MC).toString());
			}
		}
		return convert;
	}

	public static AnvilMenu write() {
		return AnvilBuilder
				.from(StringUtils.use("&3Search for a clan.").translate())
				.setLeftItem(builder -> {
					ItemStack paper = new ItemStack(Material.PAPER);
					ItemMeta meta = paper.getItemMeta();
					meta.setDisplayName(StringUtils.use("&b&oType a clan name then click the other paper &a&l→").translate());
					paper.setItemMeta(meta);
					builder.setItem(paper);
					builder.setClick((player, text, args) -> {
						if (args.length == 0) {
							String clanId = ClansAPI.getInstance().getClanID(text.replace("?", ""));
							if (clanId != null) {
								Clan c = ClansAPI.getInstance().getClan(clanId);
								UI.view(c).open(player);
								return;
							} else {
								DefaultClan.action.sendMessage(player, "&3This clan name is available.");
							}
							return;
						}
						for (String arg : args) {
							String clanId = ClansAPI.getInstance().getClanID(arg.replace("?", ""));
							if (clanId != null) {
								Clan c = ClansAPI.getInstance().getClan(clanId);
								UI.view(c).open(player);
								return;
							} else {
								DefaultClan.action.sendMessage(player, "&3Clan name '" + arg + "' is available.");
							}
						}
					});
				}).get();
	}

	public static Menu select(Singular type, HUID id) {
		MenuBuilder builder;
		Clan c = ClansAPI.getInstance().getClan(id.toString());
		if (type == Singular.CLAN_EDIT) {
			builder = new MenuBuilder(InventoryRows.THREE, StringUtils.use("&0&l» " + c.getColor() + c.getName() + " settings").translate())
					.cancelLowerInventoryClicks(false)
					.addElement(c.getMembers().filter(m -> m.getPriority() == RankPriority.HIGHEST).findFirst().get().getHead())
					.setLore(StringUtils.use("&5Click to manage clan members.").translate())
					.setText(StringUtils.use("&7[&6Member Edit&7]").translate())
					.setAction(click -> {
						Player p = click.getPlayer();
						UI.edit(c).open(p);
					})
					.assignToSlots(13)
					.addElement(new ItemStack(Material.BOOK))
					.setLore(StringUtils.use("&5Click to change our password.").translate())
					.setText(StringUtils.use("&7[&bPassword Change&7]").translate())
					.setAction(click -> {
						Player p = click.getPlayer();
						CLAN_OPTION_MAP.put(p.getUniqueId(), ClanEditOperation.Option.PASSWORD);
						CLAN_EDIT_OPERATION_MAP.put(p.getUniqueId(), new ClanEditOperation(c, p));
						DefaultClan.action.sendMessage(p, "&3Enter a new password with &f/c respond <context...>");
						p.closeInventory();
					})
					.assignToSlots(4)
					.addElement(new ItemStack(Material.ENDER_CHEST))
					.setLore(StringUtils.use("&5Click to live manage our stash.").translate())
					.setText(StringUtils.use("&7[&5Stash&7]").translate())
					.setAction(click -> {
						Player p = click.getPlayer();
						p.closeInventory();
						Bukkit.dispatchCommand(p, "cla view " + c.getName() + " stash");
					})
					.assignToSlots(3)
					.addElement(new ItemStack(Material.CHEST))
					.setLore(StringUtils.use("&5Click to live manage our vault.").translate())
					.setText(StringUtils.use("&7[&5Vault&7]").translate())
					.setAction(click -> {
						Player p = click.getPlayer();
						p.closeInventory();
						Bukkit.dispatchCommand(p, "cla view " + c.getName() + " vault");
					})
					.assignToSlots(5)
					.addElement(new ItemStack(Material.SLIME_BALL))
					.setLore(StringUtils.use("&5Click to give us claims.").translate())
					.setText(StringUtils.use("&7[&bGive Claims&7]").translate())
					.setAction(click -> {
						Player p = click.getPlayer();
						CLAN_OPTION_MAP.put(p.getUniqueId(), ClanEditOperation.Option.CLAIMS_GIVE);
						CLAN_EDIT_OPERATION_MAP.put(p.getUniqueId(), new ClanEditOperation(c, p));
						DefaultClan.action.sendMessage(p, "&3Enter an amount with &f/c respond <context...>");
						p.closeInventory();
					})
					.assignToSlots(1)
					.addElement(new ItemStack(Material.SLIME_BALL))
					.setLore(StringUtils.use("&5Click to give us power.").translate())
					.setText(StringUtils.use("&7[&bGive Power&7]").translate())
					.setAction(click -> {
						Player p = click.getPlayer();
						CLAN_OPTION_MAP.put(p.getUniqueId(), ClanEditOperation.Option.POWER_GIVE);
						CLAN_EDIT_OPERATION_MAP.put(p.getUniqueId(), new ClanEditOperation(c, p));
						DefaultClan.action.sendMessage(p, "&3Enter an amount with &f/c respond <context...>");
						p.closeInventory();
					})
					.assignToSlots(10)
					.addElement(new ItemStack(Material.GOLD_INGOT))
					.setLore(StringUtils.use("&5Click to give us money.").translate())
					.setText(StringUtils.use("&7[&bGive Money&7]").translate())
					.setAction(click -> {
						Player p = click.getPlayer();
						CLAN_OPTION_MAP.put(p.getUniqueId(), ClanEditOperation.Option.MONEY_GIVE);
						CLAN_EDIT_OPERATION_MAP.put(p.getUniqueId(), new ClanEditOperation(c, p));
						DefaultClan.action.sendMessage(p, "&3Enter an amount with &f/c respond <context...>");
						p.closeInventory();
					})
					.assignToSlots(19)
					.addElement(new ItemStack(Material.SLIME_BALL))
					.setLore(StringUtils.use("&5Click to take claims from us.").translate())
					.setText(StringUtils.use("&7[&bTake Claims&7]").translate())
					.setAction(click -> {
						Player p = click.getPlayer();
						CLAN_OPTION_MAP.put(p.getUniqueId(), ClanEditOperation.Option.CLAIMS_TAKE);
						CLAN_EDIT_OPERATION_MAP.put(p.getUniqueId(), new ClanEditOperation(c, p));
						DefaultClan.action.sendMessage(p, "&3Enter an amount with &f/c respond <context...>");
						p.closeInventory();
					})
					.assignToSlots(7)
					.addElement(new ItemStack(Material.SLIME_BALL))
					.setLore(StringUtils.use("&5Click to take power from us.").translate())
					.setText(StringUtils.use("&7[&bTake Power&7]").translate())
					.setAction(click -> {
						Player p = click.getPlayer();
						CLAN_OPTION_MAP.put(p.getUniqueId(), ClanEditOperation.Option.POWER_TAKE);
						CLAN_EDIT_OPERATION_MAP.put(p.getUniqueId(), new ClanEditOperation(c, p));
						DefaultClan.action.sendMessage(p, "&3Enter an amount with &f/c respond <context...>");
						p.closeInventory();
					})
					.assignToSlots(16)
					.addElement(new ItemStack(Material.IRON_INGOT))
					.setLore(StringUtils.use("&5Click to take money from us.").translate())
					.setText(StringUtils.use("&7[&bTake Money&7]").translate())
					.setAction(click -> {
						Player p = click.getPlayer();
						CLAN_OPTION_MAP.put(p.getUniqueId(), ClanEditOperation.Option.MONEY_TAKE);
						CLAN_EDIT_OPERATION_MAP.put(p.getUniqueId(), new ClanEditOperation(c, p));
						DefaultClan.action.sendMessage(p, "&3Enter an amount with &f/c respond <context...>");
						p.closeInventory();
					})
					.assignToSlots(25)
					.addElement(new ItemStack(Material.WRITTEN_BOOK))
					.setLore(StringUtils.use("&5Click to change our name.").translate())
					.setText(StringUtils.use("&7[&6&lTag Change&7]").translate())
					.setAction(click -> {
						Player p = click.getPlayer();
						CLAN_OPTION_MAP.put(p.getUniqueId(), ClanEditOperation.Option.TAG);
						CLAN_EDIT_OPERATION_MAP.put(p.getUniqueId(), new ClanEditOperation(c, p));
						DefaultClan.action.sendMessage(p, "&3Enter a new name with &f/c respond <context...>");
						p.closeInventory();
					})
					.assignToSlots(22)
					.addElement(new ItemStack(Material.WRITTEN_BOOK))
					.setLore(StringUtils.use("&5Click to change our description.").translate())
					.setText(StringUtils.use("&7[&3Description Change&7]").translate())
					.setAction(click -> {
						Player p = click.getPlayer();
						CLAN_OPTION_MAP.put(p.getUniqueId(), ClanEditOperation.Option.DESCRIPTION);
						CLAN_EDIT_OPERATION_MAP.put(p.getUniqueId(), new ClanEditOperation(c, p));
						DefaultClan.action.sendMessage(p, "&3Enter a new description with &f/c respond <context...>");
						p.closeInventory();
					})
					.assignToSlots(12)
					.addElement(new ItemStack(Material.WRITTEN_BOOK))
					.setLore(StringUtils.use("&5Click to change our color.").translate())
					.setText(StringUtils.use("&7[&aC&2o&6l&eo&fr &8Change&7]").translate())
					.setAction(click -> {
						Player p = click.getPlayer();
						CLAN_OPTION_MAP.put(p.getUniqueId(), ClanEditOperation.Option.COLOR);
						CLAN_EDIT_OPERATION_MAP.put(p.getUniqueId(), new ClanEditOperation(c, p));
						DefaultClan.action.sendMessage(p, "&3Enter a new color with &f/c respond <context...>");
						p.closeInventory();
					})
					.assignToSlots(14)
					.addElement(getBack())
					.setAction(click -> {
						Player p = click.getPlayer();
						UI.browseEdit().open(p);
					})
					.assignToSlots(17)
					.addElement(new ItemStack(Material.HOPPER))
					.setText(StringUtils.use("&7[&6Update Base&7]").translate())
					.setLore(StringUtils.use("&5Click to update our base location").translate(), StringUtils.use("&5to your current location.").translate())
					.setAction(click -> {
						Player p = click.getPlayer();
						CLAN_OPTION_MAP.put(p.getUniqueId(), ClanEditOperation.Option.UPDATE_BASE);
						new ClanEditOperation(c, p).execute();
						clearOperations(p.getUniqueId());
						p.closeInventory();
					})
					.assignToSlots(8)
					.addElement(new ItemStack(Material.LAVA_BUCKET))
					.setText(StringUtils.use("&7[&4&lClose&7]").translate())
					.setLore(StringUtils.use("&5Click to forcibly close our clan.").translate())
					.setAction(click -> {
						Player p = click.getPlayer();
						CLAN_OPTION_MAP.put(p.getUniqueId(), ClanEditOperation.Option.CLOSE);
						CLAN_EDIT_OPERATION_MAP.put(p.getUniqueId(), new ClanEditOperation(c, p));
						DefaultClan.action.sendMessage(p, "&3Confirm deletion with &f/c respond confirm");
						DefaultClan.action.sendMessage(p, "&cCancel with &f/c respond cancel");
						p.closeInventory();
					})
					.assignToSlots(26);

			if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
				builder.setFiller(new ItemStack(Items.getMaterial("STAINED_GLASS_PANE")))
						.setText(" ")
						.set();
			} else {
				builder.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
						.setText(" ")
						.set();
			}

			return builder.create(ClansPro.getInstance());
		}
		throw new IllegalStateException("Unable to create UI builder!");
	}

	public static Menu select(Singular type, UUID... uuids) {
		MenuBuilder builder;
		List<UUID> ids = new ArrayList<>(Arrays.asList(uuids));
		UUID uuid = null;
		if (!ids.isEmpty()) {
			uuid = ids.get(0);
		}

		ClanAssociate associate = Optional.ofNullable(uuid)
				.flatMap(ClansAPI.getInstance()::getAssociate).orElse(null);

		switch (type) {
			case ROSTER_ORGANIZATION:
				builder = new MenuBuilder(InventoryRows.ONE, StringUtils.use(ClansAPI.getData().getTitle("list-types")).translate())
						.cancelLowerInventoryClicks(false)
						.addElement(new ItemStack(ClansAPI.getData().getMaterial("top-list") != null ? ClansAPI.getData().getMaterial("top-list") : Material.PAPER))
						.setLore("")
						.setText(StringUtils.use(ClansAPI.getData().getCategory("top-list")).translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							UI.browse(Paginated.CLAN_TOP).open(p);
						})
						.assignToSlots(3)
						.addElement(new ItemStack(Material.ENCHANTED_BOOK))
						.setText(StringUtils.use("&7[&f&oSearch&7]").translate())
						.setLore(StringUtils.use("&7Search for a clan.").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							write().setViewer(p).open();
						})
						.assignToSlots(4)
						.addElement(new ItemStack(ClansAPI.getData().getMaterial("roster-list") != null ? ClansAPI.getData().getMaterial("roster-list") : Material.PAPER))
						.setLore("")
						.setText(StringUtils.use(ClansAPI.getData().getCategory("roster-list")).translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							UI.browse(Paginated.CLAN_ROSTER).open(p);
						})
						.assignToSlots(5);

				if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
					builder.setFiller(new ItemStack(Material.valueOf("STAINED_GLASS_PANE")))
							.setText(" ")
							.set();
				} else {
					builder.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
							.setText(" ")
							.set();
				}

				break;

			case MEMBER_INFO:


				Clan c = associate.getClan();

				String o = c.getColor();
				String balance;
				try {
					balance = c.format(String.valueOf(AdvancedHook.getEconomy() != null ? AdvancedHook.getEconomy().getWallet(Bukkit.getOfflinePlayer(uuid)).getBalance().doubleValue() : VaultHook.getEconomy().getBalance(Bukkit.getOfflinePlayer(uuid))));
				} catch (NoClassDefFoundError | NullPointerException e) {
					balance = "Un-Known";
				}
				String stats;
				String rank = associate.getRankTag();
				String date = associate.getJoinDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
				String bio = associate.getBiography();
				String kd = "" + associate.getKD();
				if (Bukkit.getVersion().contains("1.16") || Bukkit.getVersion().contains("1.15")) {
					OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
					stats = o + "Banners washed: &f" + p.getStatistic(Statistic.BANNER_CLEANED) + "|" +
							o + "Bell's rang: &f" + p.getStatistic(Statistic.BELL_RING) + "|" +
							o + "Chest's opened: &f" + p.getStatistic(Statistic.CHEST_OPENED) + "|" +
							o + "Creeper death's: &f" + p.getStatistic(Statistic.ENTITY_KILLED_BY, Entities.getEntity("Creeper")) + "|" +
							o + "Beat's dropped: &f" + p.getStatistic(Statistic.RECORD_PLAYED) + "|" +
							o + "Animal's bred: &f" + p.getStatistic(Statistic.ANIMALS_BRED);
				} else {
					stats = "&c&oVersion under &61.15 |" +
							"&fOffline stat's unattainable.";
				}

				String[] statist = DefaultClan.action.color(stats).split("\\|");

				UUID finalUuid = uuid;

				String test = MessageFormat.format(ClansAPI.getData().getTitle("member-information"), Bukkit.getOfflinePlayer(uuid).getName());

				if (test.length() > 32)
					test = associate.getClan().getColor() + associate.getPlayer().getName() + " &7Info";

				builder = new MenuBuilder(InventoryRows.THREE, DefaultClan.action.color(test))
						.cancelLowerInventoryClicks(false)
						.addElement(Item.ColoredArmor.select(Item.ColoredArmor.Piece.TORSO).setColor(Color.RED).build())
						.setLore(StringUtils.use(c.getColor() + c.getName()).translate())
						.setText(StringUtils.use("Clan:").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							Bukkit.dispatchCommand(p, "c info " + c.getName());
						})
						.assignToSlots(13)
						.addElement(CustomHead.Manager.get(associate.getPlayer()) != null ? new Item.Edit(CustomHead.Manager.get(associate.getPlayer())).setFlags(ItemFlag.HIDE_ENCHANTS).addEnchantment(Enchantment.ARROW_DAMAGE, 69).build() : new ItemStack(ClansAPI.getData().getItem("player")))
						.setLore(StringUtils.use(bio + " &f-" + associate.getNickname()).translate())
						.setText(StringUtils.use(" ").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
						})
						.assignToSlots(4)
						.addElement(new ItemStack(Material.BOOK))
						.setLore(statist)
						.setText(StringUtils.use(o + "Statistics:").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
						})
						.assignToSlots(12)
						.addElement(new ItemStack(Material.BARRIER))
						.setLore("")
						.setText(StringUtils.use("&eClick to: &cgo back.").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							UI.view(c).open(p);
						})
						.assignToSlots(19)
						.addElement(new ItemStack(Items.getMaterial("GOLDENPICKAXE") != null ? Items.getMaterial("GOLDENPICKAXE") : Items.getMaterial("GOLDPICKAXE")))
						.setLore(StringUtils.use(o + rank).translate())
						.setText(StringUtils.use("Rank:").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
						})
						.assignToSlots(14)
						.addElement(new ItemStack(Items.getMaterial("ENDPORTALFRAME") != null ? Items.getMaterial("ENDPORTALFRAME") : Items.getMaterial("IRONINGOT")))
						.setLore(StringUtils.use(o + date).translate())
						.setText(StringUtils.use("Join Date:").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
						})
						.assignToSlots(22)
						.addElement(new ItemStack(Material.NAME_TAG))
						.setText(StringUtils.use("&eEdit").translate())
						.setAction(click -> {
							Player p = click.getPlayer();

							if (p.hasPermission("clanspro.admin")) {
								select(Singular.MEMBER_EDIT, finalUuid).open(p);
							} else {
								click.getInventoryView().getTopInventory().setItem(26, new ItemStack(Items.getMaterial("stainedglasspane")));
							}

						})
						.assignToSlots(26)
						.addElement(new ItemStack(Material.DIAMOND_SWORD))
						.setLore(StringUtils.use(o + kd).translate())
						.setText(StringUtils.use("K/D:").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
						})
						.assignToSlots(10)
						.addElement(new ItemStack(Material.GOLD_INGOT))
						.setLore(StringUtils.use(o + balance).translate())
						.setText(StringUtils.use("Wallet:").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
						})
						.assignToSlots(16);

				if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
					builder.setFiller(new ItemStack(Items.getMaterial("STAINED_GLASS_PANE")))
							.setText(" ")
							.set();
				} else {
					builder.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
							.setText(" ")
							.set();
				}

				break;
			case MEMBER_EDIT:
				Clan cl = Optional.ofNullable(uuid)
						.map(DefaultClan.action::getClanID)
						.map(ClansAPI.getInstance()::getClan)
						.orElseThrow(() -> new IllegalStateException("Error loading clan data for member"));

				OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				builder = new MenuBuilder(InventoryRows.THREE, StringUtils.use("&0&l» " + cl.getColor() + player.getName() + " settings").translate())
						.cancelLowerInventoryClicks(false)
						.addElement(new Item.Edit(Optional.ofNullable(CustomHead.Manager.get(player)).orElse(new ItemStack(SkullType.PLAYER.get()))).setFlags(ItemFlag.HIDE_ENCHANTS).addEnchantment(Enchantment.ARROW_DAMAGE, 69).build())
						.setText(StringUtils.use(ClansAPI.getData().getNavigate("back")).translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							UI.edit(cl).open(p);
						})
						.assignToSlots(13)
						.addElement(new ItemStack(Material.BOOK))
						.setLore(StringUtils.use("&5Click to change my bio.").translate())
						.setText(StringUtils.use("&7[&bBio change&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							MEMBER_OPTION_MAP.put(p.getUniqueId(), MemberEditOperation.Option.BIO);
							MEMBER_EDIT_OPERATION_MAP.put(p.getUniqueId(), new MemberEditOperation(cl, p, player.getUniqueId()));
							DefaultClan.action.sendMessage(p, "&3Enter a new bio with &f/c respond <context...>");
							p.closeInventory();
						})
						.assignToSlots(4)
						.addElement(new ItemStack(Material.ENCHANTED_BOOK))
						.setLore(StringUtils.use("&5Click to change my nickname.").translate())
						.setText(StringUtils.use("&7[&6&lName Change&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							MEMBER_OPTION_MAP.put(p.getUniqueId(), MemberEditOperation.Option.NICKNAME);
							MEMBER_EDIT_OPERATION_MAP.put(p.getUniqueId(), new MemberEditOperation(cl, p, player.getUniqueId()));
							DefaultClan.action.sendMessage(p, "&3Enter a new name with &f/c respond <context...>");
							p.closeInventory();
						})
						.assignToSlots(22)
						.addElement(new ItemStack(Material.WRITTEN_BOOK))
						.setLore(StringUtils.use("&5Click to put me in another clan.").translate())
						.setText(StringUtils.use("&7[&4Switch Clans&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							MEMBER_OPTION_MAP.put(p.getUniqueId(), MemberEditOperation.Option.SWITCH_CLANS);
							MEMBER_EDIT_OPERATION_MAP.put(p.getUniqueId(), new MemberEditOperation(cl, p, player.getUniqueId()));
							DefaultClan.action.sendMessage(p, "&3Enter a clan name with &f/c respond <context...>");
							p.closeInventory();
						})
						.assignToSlots(12)
						.addElement(new ItemStack(Material.IRON_BOOTS))
						.setLore(StringUtils.use("&5Click to kick me.").translate())
						.setText(StringUtils.use("&7[&4Kick&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							MEMBER_OPTION_MAP.put(p.getUniqueId(), MemberEditOperation.Option.KICK);
							new MemberEditOperation(cl, p, player.getUniqueId()).execute();
							clearOperations(p.getUniqueId());
							p.closeInventory();
						})
						.assignToSlots(11)
						.addElement(new ItemStack(Material.DIAMOND))
						.setLore(StringUtils.use("&5Click to promote me.").translate())
						.setText(StringUtils.use("&7[&aPromotion&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							MEMBER_OPTION_MAP.put(p.getUniqueId(), MemberEditOperation.Option.PROMOTE);
							new MemberEditOperation(cl, p, player.getUniqueId()).execute();
							clearOperations(p.getUniqueId());
							p.closeInventory();
						})
						.assignToSlots(14)
						.addElement(new ItemStack(Material.REDSTONE))
						.setLore(StringUtils.use("&5Click to demote me.").translate())
						.setText(StringUtils.use("&7[&cDemotion&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							MEMBER_OPTION_MAP.put(p.getUniqueId(), MemberEditOperation.Option.DEMOTE);
							new MemberEditOperation(cl, p, player.getUniqueId()).execute();
							clearOperations(p.getUniqueId());
							p.closeInventory();
						})
						.assignToSlots(15);

				if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
					builder.setFiller(new ItemStack(Items.getMaterial("STAINED_GLASS_PANE")))
							.setText(" ")
							.set();
				} else {
					builder.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
							.setText(" ")
							.set();
				}

				break;
			case SHIELD_TAMPER:
				builder = new MenuBuilder(InventoryRows.ONE, StringUtils.use("&2&oRaid-Shield Settings &0&l»").translate())
						.cancelLowerInventoryClicks(false)
						.addElement(new ItemStack(Items.getMaterial("CLOCK") != null ? Items.getMaterial("CLOCK") : Items.getMaterial("WATCH")))
						.setLore(StringUtils.use("&bClick to change the raid-shield to enable on sunrise").translate())
						.setText(StringUtils.use("&a&oUp: Sunrise").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							ClansAPI.getInstance().getShieldManager().getTamper().setUpOverride(0);
							ClansAPI.getInstance().getShieldManager().getTamper().setDownOverride(13000);
						})
						.assignToSlots(3)
						.addElement(new ItemStack(Items.getMaterial("CLOCK") != null ? Items.getMaterial("CLOCK") : Items.getMaterial("WATCH")))
						.setLore(StringUtils.use("&bClick to change the raid-shield to enable mid-day").translate())
						.setText(StringUtils.use("&a&oUp: Mid-day").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							ClansAPI.getInstance().getShieldManager().getTamper().setUpOverride(6000);
							ClansAPI.getInstance().getShieldManager().getTamper().setDownOverride(18000);
						})
						.assignToSlots(4)
						.addElement(new ItemStack(Items.getMaterial("CLOCK") != null ? Items.getMaterial("CLOCK") : Items.getMaterial("WATCH")))
						.setLore(StringUtils.use("&bClick to freeze the raid-shield @ its curent status").translate())
						.setText(StringUtils.use("&a&oPermanent protection.").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							if (ClansAPI.getInstance().getShieldManager().getTamper().isOff()) {
								p.closeInventory();
								DefaultClan.action.sendMessage(p, "&aRaid-shield block has been lifted.");
								ClansAPI.getInstance().getShieldManager().getTamper().setIsOff(false);
							} else {
								DefaultClan.action.sendMessage(p, "&cRaid-shield has been blocked.");
								ClansAPI.getInstance().getShieldManager().getTamper().setIsOff(true);
							}
						})
						.assignToSlots(5)
						.addElement(getBack())
						.setAction(click -> {
							Player p = click.getPlayer();
							UI.select(Singular.SETTINGS_WINDOW).open(p);
						})
						.assignToSlots(8);

				if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
					builder.setFiller(new ItemStack(Items.getMaterial("STAINED_GLASS_PANE")))
							.setText(" ")
							.set();
				} else {
					builder.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
							.setText(" ")
							.set();
				}

				break;
			case ARENA_SETUP:
				builder = new MenuBuilder(InventoryRows.ONE, StringUtils.use("&2&oArena Spawns &0&l»").translate())
						.cancelLowerInventoryClicks(false)
						.addElement(new ItemStack(Material.SLIME_BALL))
						.setLore(StringUtils.use("&bClick to update the red team start location.").translate())
						.setText(StringUtils.use("&7[&cRed Start&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							Bukkit.dispatchCommand(p, "cla setspawn red");
						})
						.assignToSlots(2)
						.addElement(new ItemStack(Material.SLIME_BALL))
						.setLore(StringUtils.use("&bClick to update the red team re-spawn location.").translate())
						.setText(StringUtils.use("&7[&cRed Spawn&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							Bukkit.dispatchCommand(p, "cla setspawn red_death");
						})
						.assignToSlots(3)
						.addElement(new ItemStack(Material.SLIME_BALL))
						.setLore(StringUtils.use("&bClick to update the blue team start location.").translate())
						.setText(StringUtils.use("&7[&9Blue Start&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							Bukkit.dispatchCommand(p, "cla setspawn blue");
						})
						.assignToSlots(5)
						.addElement(new ItemStack(Material.SLIME_BALL))
						.setLore(StringUtils.use("&bClick to update the blue team re-spawn location.").translate())
						.setText(StringUtils.use("&7[&9Blue Spawn&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							Bukkit.dispatchCommand(p, "cla setspawn blue_death");
						})
						.assignToSlots(6)
						.addElement(getBack())
						.setAction(click -> {
							Player p = click.getPlayer();
							UI.select(Singular.SETTINGS_WINDOW).open(p);
						})
						.assignToSlots(8);

				if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
					builder.setFiller(new ItemStack(Items.getMaterial("STAINED_GLASS_PANE")))
							.setText(" ")
							.set();
				} else {
					builder.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
							.setText(" ")
							.set();
				}

				break;
			case SETTINGS_WINDOW:
				builder = new MenuBuilder(InventoryRows.SIX, StringUtils.use(" &0&l» &2&oManagement Area").translate())
						.cancelLowerInventoryClicks(false)
						.addElement(new ItemStack(Items.getMaterial("NAUTILUS_SHELL") != null ? Items.getMaterial("NAUTILUS_SHELL") : Items.getMaterial("NETHERSTAR")))
						.setLore(StringUtils.use("&bClick to manage all &dclans addons").translate())
						.setText(StringUtils.use("&7[&5Addon Management&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							// TODO: open the addon inventory.
							UI.select(Singular.CYCLE_ORGANIZATION).open(p);
						})
						.assignToSlots(33)
						.addElement(new ItemStack(Items.getMaterial("CLOCK") != null ? Items.getMaterial("CLOCK") : Items.getMaterial("WATCH")))
						.setLore(StringUtils.use("&bClick to edit the &3raid-shield").translate())
						.setText(StringUtils.use("&7[&2Shield Edit&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							UI.select(Singular.SHIELD_TAMPER).open(p);
						})
						.assignToSlots(29)
						.addElement(new ItemStack(Material.ENCHANTED_BOOK))
						.setLore(StringUtils.use("&7Click to toggle spy ability on all clan chat channels.").translate())
						.setText(StringUtils.use("&7[&cAll Spy&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							Bukkit.dispatchCommand(p, "cla spy clan");
							Bukkit.dispatchCommand(p, "cla spy ally");
							Bukkit.dispatchCommand(p, "cla spy custom");
						})
						.assignToSlots(12)
						.addElement(new ItemStack(Material.ANVIL))
						.setLore(StringUtils.use("&7Click to manage clans.").translate())
						.setText(StringUtils.use("&7[&eClan Edit&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							UI.browseEdit().open(p);
						})
						.assignToSlots(10)
						.addElement(new ItemStack(Material.DIAMOND_SWORD))
						.setLore(StringUtils.use("&7Click to manage arena spawns.").translate())
						.setText(StringUtils.use("&7[&2War Arena&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							UI.select(Singular.ARENA_SETUP).open(p);
						})
						.assignToSlots(16)
						.addElement(new ItemStack(ClansAPI.getData().getMaterial("clan") != null ? ClansAPI.getData().getMaterial("clan") : Material.PAPER))
						.setLore(StringUtils.use("&bClick to view the entire &6clan roster").translate())
						.setText(StringUtils.use("&7[&eClan List&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							UI.select(Singular.ROSTER_ORGANIZATION).open(p);
						})
						.assignToSlots(14)
						.addElement(getBack())
						.setText(StringUtils.use("&7[&4Close&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							p.closeInventory();
						})
						.assignToSlots(49)
						.addElement(new ItemStack(Items.getMaterial("HEARTOFTHESEA") != null ? Items.getMaterial("HEARTOFTHESEA") : Items.getMaterial("SLIMEBALL")))
						.setText(StringUtils.use("&7[&cReload&7]").translate())
						.setLore(StringUtils.use("&7Reload all configuration files.").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							FileManager config = ClansAPI.getInstance().getFileList().find("Config", "Configuration");
							FileManager message = ClansAPI.getInstance().getFileList().find("Messages", "Configuration");

							List<String> format = message.getConfig().getStringList("menu-format.clan");

							DataManager dataManager = ClansAPI.getData();

							dataManager.CLAN_FORMAT.clear();

							dataManager.CLAN_FORMAT.addAll(format);

							FileManager regions = ClansAPI.getInstance().getFileList().find("Regions", "Configuration");
							config.reload();
							message.reload();
							regions.reload();
							UI.select(Singular.SETTINGS_WINDOW).open(p);

							ClansAPI.getInstance().getClanManager().refresh();

							DefaultClan.action.sendMessage(p, "&b&oAll configuration files reloaded.");
						})
						.assignToSlots(31);
				if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
					builder.setFiller(new ItemStack(Items.getMaterial("STAINED_GLASS_PANE")))
							.setText(" ")
							.set();
				} else {
					builder.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
							.setText(" ")
							.set();
				}
				break;

			case CYCLE_ORGANIZATION:
				builder = new MenuBuilder(InventoryRows.ONE, StringUtils.use("&2&oManage Addon Cycles &0&l»").translate())
						.cancelLowerInventoryClicks(false)
						.addElement(new ItemStack(Material.WATER_BUCKET))
						.setLore(StringUtils.use("&2&oTurn off running addons.").translate())
						.setText(StringUtils.use("&7[&3&lRunning&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							UI.moderate(Paginated.ACTIVATED_CYCLES).open(p);
						})
						.assignToSlots(3)
						.addElement(new ItemStack(Material.BUCKET))
						.setLore(StringUtils.use("&a&oTurn on disabled addons.").translate(), StringUtils.use("&f&oMay require players to re-log with specific addons.").translate())
						.setText(StringUtils.use("&7[&c&lDisabled&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							UI.moderate(Paginated.DEACTIVATED_CYCLES).open(p);
						})
						.assignToSlots(5)
						.addElement(new ItemStack(Material.LAVA_BUCKET))
						.setLore(StringUtils.use("&b&oView a list of all currently persistently cached addons.").translate())
						.setText(StringUtils.use("&7[&e&lLoaded&7]").translate())
						.setAction(click -> {
							Player p = click.getPlayer();
							UI.moderate(Paginated.REGISTERED_CYCLES).open(p);
						})
						.assignToSlots(4)
						.addElement(getBack())
						.setAction(click -> {
							Player p = click.getPlayer();
							UI.select(Singular.SETTINGS_WINDOW).open(p);
						})
						.assignToSlots(8);
				if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
					builder.setFiller(new ItemStack(Items.getMaterial("STAINED_GLASS_PANE")))
							.setText(" ")
							.set();
				} else {
					builder.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
							.setText(" ")
							.set();
				}
				break;
			default:
				throw new IllegalStateException("Illegal menu type present.");
		}
		return builder.create(ClansPro.getInstance());
	}

	public static Menu.Paginated<EventCycle> moderate(Paginated type) {
		switch (type) {
			case REGISTERED_CYCLES:
				return new PaginatedBuilder<>(new LinkedList<>(CycleList.getRegisteredCycles()))
						.forPlugin(ClansPro.getInstance())
						.setTitle(DefaultClan.action.color("&3&oRegistered Cycles &f(&6&lCACHE&f) &8&l»"))
						.setAlreadyFirst(DefaultClan.action.color(DefaultClan.action.alreadyFirstPage()))
						.setAlreadyLast(DefaultClan.action.color(DefaultClan.action.alreadyLastPage()))
						.setNavigationLeft(getLeft(), 45, PaginatedClickAction::sync)
						.setNavigationRight(getRight(), 53, PaginatedClickAction::sync)
						.setNavigationBack(getBack(), 49, click -> UI.select(Singular.CYCLE_ORGANIZATION).open(click.getPlayer()))
						.setSize(InventoryRows.SIX)
						.setCloseAction(PaginatedCloseAction::clear)
						.setupProcess(e -> e.setItem(() -> {
							EventCycle cycle = e.getContext();
							ItemStack i = new ItemStack(Material.CHEST);

							ItemMeta meta = i.getItemMeta();

							meta.setLore(color("&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oPersistent: &f" + cycle.persist(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oDescription: &f" + cycle.getDescription(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oVersion: &f" + cycle.getVersion(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oAuthors: &f" + Arrays.toString(cycle.getAuthors()), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oActive: &6&o" + CycleList.getUsedAddons().contains(cycle.getName()), "&7Clicking these icons won't do anything."));

							meta.setDisplayName(StringUtils.use("&3&o " + e.getContext().getName() + " &8&l»").translate());

							i.setItemMeta(meta);

							return i;
						}).setClick(click -> {
							Player p = click.getPlayer();
						}))
						.setupBorder()
						.setBorderType(Arrays.stream(Material.values()).anyMatch(m -> m.name().equals("LIGHT_GRAY_STAINED_GLASS_PANE")) ? Items.getMaterial("LIGHT_GRAY_STAINED_GLASS_PANE") : Items.getMaterial("STAINEDGLASSPANE"))
						.setFillType(new Item.Edit(SkullType.COMMAND_BLOCK.get()).setTitle(" ").build())
						.build()
						.limit(28)
						.build();
			case ACTIVATED_CYCLES:
				return new PaginatedBuilder<>(CycleList.getUsedAddons().stream().map(CycleList::getAddon).collect(Collectors.toList()))
						.forPlugin(ClansPro.getInstance())
						.setTitle(DefaultClan.action.color("&3&oRegistered Cycles &f(&2RUNNING&f) &8&l»"))
						.setAlreadyFirst(DefaultClan.action.color(DefaultClan.action.alreadyFirstPage()))
						.setAlreadyLast(DefaultClan.action.color(DefaultClan.action.alreadyLastPage()))
						.setNavigationLeft(getLeft(), 45, PaginatedClickAction::sync)
						.setNavigationRight(getRight(), 53, PaginatedClickAction::sync)
						.setNavigationBack(getBack(), 49, click -> UI.select(Singular.CYCLE_ORGANIZATION).open(click.getPlayer()))
						.setSize(InventoryRows.SIX)
						.setCloseAction(PaginatedCloseAction::clear)
						.setupProcess(e -> {
							e.setItem(() -> {
								EventCycle cycle = e.getContext();
								ItemStack i = new ItemStack(Material.CHEST);

								ItemMeta meta = i.getItemMeta();

								meta.setLore(color("&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oPersistent: &f" + cycle.persist(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oDescription: &f" + cycle.getDescription(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oVersion: &f" + cycle.getVersion(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oAuthors: &f" + Arrays.toString(cycle.getAuthors()), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oActive: &6&o" + CycleList.getUsedAddons().contains(cycle.getName())));

								meta.setDisplayName(StringUtils.use("&3&o " + e.getContext().getName() + " &8&l»").translate());

								i.setItemMeta(meta);

								return i;
							}).setClick(click -> {
								Player p = click.getPlayer();
								EventCycle ec = e.getContext();
								CycleList.unregisterAll(ec);
								for (String d : CycleList.getDataLog()) {
									p.sendMessage(DefaultClan.action.color("&b" + d.replace("Clans [Pro]", "&3Clans &7[&6Pro&7]&b")));
								}
								UI.moderate(Paginated.ACTIVATED_CYCLES).open(p);
							});
						})
						.setupBorder()
						.setBorderType(Arrays.stream(Material.values()).anyMatch(m -> m.name().equals("LIGHT_GRAY_STAINED_GLASS_PANE")) ? Items.getMaterial("LIGHT_GRAY_STAINED_GLASS_PANE") : Items.getMaterial("STAINEDGLASSPANE"))
						.setFillType(new Item.Edit(SkullType.COMMAND_BLOCK.get()).setTitle(" ").build())
						.build()
						.limit(28)
						.build();
			case DEACTIVATED_CYCLES:
				return new PaginatedBuilder<>(CycleList.getUnusedAddons().stream().map(CycleList::getAddon).collect(Collectors.toList()))
						.forPlugin(ClansPro.getInstance())
						.setTitle(DefaultClan.action.color("&3&oRegistered Cycles &f(&4DISABLED&f) &8&l»"))
						.setAlreadyFirst(DefaultClan.action.color(DefaultClan.action.alreadyFirstPage()))
						.setAlreadyLast(DefaultClan.action.color(DefaultClan.action.alreadyLastPage()))
						.setNavigationLeft(getLeft(), 45, PaginatedClickAction::sync)
						.setNavigationRight(getRight(), 53, PaginatedClickAction::sync)
						.setNavigationBack(getBack(), 49, click -> UI.select(Singular.CYCLE_ORGANIZATION).open(click.getPlayer()))
						.setSize(InventoryRows.SIX)
						.setCloseAction(PaginatedCloseAction::clear)
						.setupProcess(e -> {
							e.setItem(() -> {
								EventCycle cycle = e.getContext();
								ItemStack i = new ItemStack(Material.CHEST);

								ItemMeta meta = i.getItemMeta();

								meta.setLore(color("&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oPersistent: &f" + cycle.persist(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oDescription: &f" + cycle.getDescription(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oVersion: &f" + cycle.getVersion(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oAuthors: &f" + Arrays.toString(cycle.getAuthors()), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oActive: &6&o" + CycleList.getUsedAddons().contains(cycle.getName())));

								meta.setDisplayName(StringUtils.use("&3&o " + e.getContext().getName() + " &8&l»").translate());

								i.setItemMeta(meta);

								return i;
							}).setClick(click -> {
								Player p = click.getPlayer();
								EventCycle ec = e.getContext();
								CycleList.registerAll(ec);
								for (String d : CycleList.getDataLog()) {
									p.sendMessage(DefaultClan.action.color("&b" + DefaultClan.action.format(d, "Clans [Pro]", "&3Clans &7[&6Pro&7]&b")));
								}
								UI.moderate(Paginated.DEACTIVATED_CYCLES).open(p);
							});
						})
						.setupBorder()
						.setBorderType(Arrays.stream(Material.values()).anyMatch(m -> m.name().equals("LIGHT_GRAY_STAINED_GLASS_PANE")) ? Items.getMaterial("LIGHT_GRAY_STAINED_GLASS_PANE") : Items.getMaterial("STAINEDGLASSPANE"))
						.setFillType(new Item.Edit(SkullType.COMMAND_BLOCK.get()).setTitle(" ").build())
						.build()
						.limit(28)
						.build();
			default:
				throw new IllegalStateException("Invalid menu request!");
		}
	}

	public static Menu.Paginated<Clan> browse(Paginated type) {
		switch (type) {
			case CLAN_ROSTER:
				return new PaginatedBuilder<>(ClansAPI.getData().CLANS)
						.forPlugin(ClansPro.getInstance())
						.setTitle(DefaultClan.action.color(ClansAPI.getData().getTitle("roster-list")))
						.setAlreadyFirst(DefaultClan.action.color(DefaultClan.action.alreadyFirstPage()))
						.setAlreadyLast(DefaultClan.action.color(DefaultClan.action.alreadyLastPage()))
						.setNavigationLeft(getLeft(), getLeftSlot(), PaginatedClickAction::sync)
						.setNavigationRight(getRight(), getRightSlot(), PaginatedClickAction::sync)
						.setNavigationBack(getBack(), getBackSlot(), click -> UI.select(Singular.ROSTER_ORGANIZATION).open(click.getPlayer()))
						.setSize(getRows())
						.setCloseAction(close -> {
							LAST_MENU.put(close.getPlayer().getUniqueId(), Paginated.CLAN_ROSTER);
							close.clear();
						})
						.setupProcess(e -> {
							e.setItem(() -> {
								ItemStack i = new ItemStack(ClansAPI.getData().getItem("clan"));
								Clan c = e.getContext();
								int a1 = 0;
								int a2 = 0;
								int a3 = 0;
								StringBuilder members = new StringBuilder("&b&o");
								for (String id : c.getMembersList()) {
									a1++;
									if (a1 == 1) {
										members.append("&b&o").append(Bukkit.getOfflinePlayer(UUID.fromString(id)).getName());
									} else {
										members.append("&f, &b&o").append(Bukkit.getOfflinePlayer(UUID.fromString(id)).getName());
									}
								}

								StringBuilder allies = new StringBuilder("&a&o");
								for (String id : c.getAllyList()) {
									a2++;
									if (a2 == 1) {
										allies.append("&b&o").append(ClansAPI.getInstance().getClanName(id));
									} else {
										allies.append("&f, &b&o").append(ClansAPI.getInstance().getClanName(id));
									}
								}
								StringBuilder enemies = new StringBuilder("&a&o");
								for (String id : c.getAllyList()) {
									a3++;
									if (a3 == 1) {
										enemies.append("&b&o").append(ClansAPI.getInstance().getClanName(id));
									} else {
										enemies.append("&f, &b&o").append(ClansAPI.getInstance().getClanName(id));
									}
								}
								String memlist = members.toString();
								if (memlist.length() > 44) {
									memlist = memlist.substring(0, 44) + "...";
								}
								String allylist = allies.toString();
								if (allylist.length() > 44) {
									allylist = allylist.substring(0, 44) + "...";
								}
								String enemylist = enemies.toString();
								if (enemylist.length() > 44) {
									enemylist = enemylist.substring(0, 44) + "...";
								}

								String color = c.getColor().replace("&k", "");

								double power = c.getPower();

								String pvp;

								if (c.isPeaceful()) {
									pvp = "&a&lPEACE";
								} else {
									pvp = "&4&lWAR";
								}

								int ownedLand = c.getOwnedClaimsList().length;

								StringBuilder idShort = new StringBuilder();
								for (int j = 0; j < 4; j++) {
									idShort.append(c.getId().toString().charAt(j));
								}
								String id = idShort.toString();

								boolean baseSet = c.getBase() != null;

								String desc = c.getDescription();

								if (c.getAllyList().isEmpty()) {
									allylist = "&cEmpty";
								}

								if (c.getEnemyList().isEmpty()) {
									enemylist = "&cEmpty";
								}

								ItemMeta meta = i.getItemMeta();


								String[] par = new Paragraph(desc).setRegex(Paragraph.COMMA_AND_PERIOD).get();

								final String memEdit = color + memlist.replace("&b&o", color).replace("&f, &b...", color + "...");
								final String allEdit = color + allylist.replace("&b&o", color).replace("&f, &b...", color + "...");
								final String enemEdit = color + enemylist.replace("&b&o", color).replace("&f, &b...", color + "...");
								List<String> result = new LinkedList<>();
								for (String a : ClansAPI.getData().CLAN_FORMAT) {
									result.add(MessageFormat.format(a, color.replace("&", "&f»" + color), color + par[0], color + c.format(String.valueOf(power)), baseSet, color + ownedLand, pvp, memEdit, allEdit, enemEdit, color));
								}
								meta.setLore(color(result.toArray(new String[0])));

								String title = MessageFormat.format(ClansAPI.getData().getCategory("clan"), c.getColor(), c.getName(), id);

								meta.setDisplayName(StringUtils.use(title).translate());

								i.setItemMeta(meta);

								return i;
							}).setClick(click -> {
								Player p = click.getPlayer();
								UI.view(e.getContext()).open(p);
							});
						})
						.setupBorder()
						.setBorderType(Arrays.stream(Material.values()).anyMatch(m -> m.name().equals("LIGHT_GRAY_STAINED_GLASS_PANE")) ? Items.getMaterial("LIGHT_GRAY_STAINED_GLASS_PANE") : Items.getMaterial("STAINEDGLASSPANE"))
						.setFillType(new Item.Edit(SkullType.COMMAND_BLOCK.get()).setTitle(" ").build())
						.build()
						.limit(getAmntPer())
						.build();
			case CLAN_TOP:
				return new PaginatedBuilder<>(DefaultClan.action.getTop())
						.forPlugin(ClansPro.getInstance())
						.setTitle(DefaultClan.action.color(ClansAPI.getData().getTitle("top-list")))
						.setAlreadyFirst(DefaultClan.action.color(DefaultClan.action.alreadyFirstPage()))
						.setAlreadyLast(DefaultClan.action.color(DefaultClan.action.alreadyLastPage()))
						.setNavigationLeft(getLeft(), getLeftSlot(), PaginatedClickAction::sync)
						.setNavigationRight(getRight(), getRightSlot(), PaginatedClickAction::sync)
						.setNavigationBack(getBack(), getBackSlot(), click -> UI.select(Singular.ROSTER_ORGANIZATION).open(click.getPlayer()))
						.setSize(getRows())
						.setCloseAction(close -> {
							LAST_MENU.put(close.getPlayer().getUniqueId(), Paginated.CLAN_TOP);
							close.clear();
						})
						.setupProcess(e -> {
							e.setItem(() -> {
								ItemStack i = new ItemStack(ClansAPI.getData().getItem("clan"));
								Clan c = e.getContext();
								int a1 = 0;
								int a2 = 0;
								int a3 = 0;
								StringBuilder members = new StringBuilder("&b&o");
								for (String id : c.getMembersList()) {
									a1++;
									if (a1 == 1) {
										members.append("&b&o").append(Bukkit.getOfflinePlayer(UUID.fromString(id)).getName());
									} else {
										members.append("&f, &b&o").append(Bukkit.getOfflinePlayer(UUID.fromString(id)).getName());
									}
								}

								StringBuilder allies = new StringBuilder("&a&o");
								for (String id : c.getAllyList()) {
									a2++;
									if (a2 == 1) {
										allies.append("&b&o").append(ClansAPI.getInstance().getClanName(id));
									} else {
										allies.append("&f, &b&o").append(ClansAPI.getInstance().getClanName(id));
									}
								}
								StringBuilder enemies = new StringBuilder("&a&o");
								for (String id : c.getAllyList()) {
									a3++;
									if (a3 == 1) {
										enemies.append("&b&o").append(ClansAPI.getInstance().getClanName(id));
									} else {
										enemies.append("&f, &b&o").append(ClansAPI.getInstance().getClanName(id));
									}
								}
								String memlist = members.toString();
								if (memlist.length() > 44) {
									memlist = memlist.substring(0, 44) + "...";
								}
								String allylist = allies.toString();
								if (allylist.length() > 44) {
									allylist = allylist.substring(0, 44) + "...";
								}
								String enemylist = enemies.toString();
								if (enemylist.length() > 44) {
									enemylist = enemylist.substring(0, 44) + "...";
								}

								String color = c.getColor().replace("&k", "");

								double power = c.getPower();

								String pvp;

								if (c.isPeaceful()) {
									pvp = "&a&lPEACE";
								} else {
									pvp = "&4&lWAR";
								}

								int ownedLand = c.getOwnedClaimsList().length;

								StringBuilder idShort = new StringBuilder();
								for (int j = 0; j < 4; j++) {
									idShort.append(c.getId().toString().charAt(j));
								}
								String id = idShort.toString();

								boolean baseSet = c.getBase() != null;

								String desc = c.getDescription();

								if (c.getAllyList().isEmpty()) {
									allylist = "&cEmpty";
								}

								if (c.getEnemyList().isEmpty()) {
									enemylist = "&cEmpty";
								}

								ItemMeta meta = i.getItemMeta();

								String[] par = new Paragraph(desc).setRegex(Paragraph.COMMA_AND_PERIOD).get();

								List<String> result = new LinkedList<>();
								for (String a : ClansAPI.getData().CLAN_FORMAT) {
									result.add(MessageFormat.format(a, color.replace("&", "&f»" + color), color + par[0], color + c.format(String.valueOf(power)), baseSet, color + ownedLand, pvp, memlist, allylist, enemylist, color));
								}
								meta.setLore(color(result.toArray(new String[0])));

								String title = MessageFormat.format(ClansAPI.getData().getCategory("clan"), c.getColor(), c.getName(), id);

								meta.setDisplayName(StringUtils.use(title).translate());

								i.setItemMeta(meta);

								return i;
							}).setClick(click -> {
								Player p = click.getPlayer();
								UI.view(e.getContext()).open(p);
							});
						})
						.setupBorder()
						.setBorderType(Arrays.stream(Material.values()).anyMatch(m -> m.name().equals("LIGHT_GRAY_STAINED_GLASS_PANE")) ? Items.getMaterial("LIGHT_GRAY_STAINED_GLASS_PANE") : Items.getMaterial("STAINEDGLASSPANE"))
						.setFillType(new Item.Edit(SkullType.COMMAND_BLOCK.get()).setTitle(" ").build())
						.build()
						.limit(getAmntPer())
						.build();
			default:
				throw new IllegalStateException("Invalid menu type proposed!");
		}
	}

	public static Menu.Paginated<ClanAssociate> view(Clan c) {
		List<String> s = c.getValue(List.class, "logo");
		return new PaginatedBuilder<>(c.getMembers().list())
				.forPlugin(ClansPro.getInstance())
				.setTitle(DefaultClan.action.color(ClansAPI.getData().getTitle("member-list")))
				.setAlreadyFirst(DefaultClan.action.color(DefaultClan.action.alreadyFirstPage()))
				.setAlreadyLast(DefaultClan.action.color(DefaultClan.action.alreadyLastPage()))
				.setNavigationLeft(getLeft(), getLeftSlot(), PaginatedClickAction::sync)
				.setNavigationRight(getRight(), getRightSlot(), PaginatedClickAction::sync)
				.setNavigationBack(getBack(), getBackSlot(), click -> UI.browse(LAST_MENU.getOrDefault(click.getPlayer().getUniqueId(), Paginated.CLAN_ROSTER)).open(click.getPlayer()))
				.setSize(getRows())
				.setCloseAction(PaginatedCloseAction::clear)
				.setupProcess(e -> {
					e.setItem(() -> {
						ItemStack copy = e.getContext().getHead();
						if (copy == null) {

							ItemStack backup = CustomHead.Manager.get(e.getContext().getPlayer());

							if (backup != null) {
								copy = backup;
							} else {
								copy = new ItemStack(ClansAPI.getData().getItem("player"));
							}
						}
						ItemStack i = new ItemStack(copy);
						ClanAssociate associate = e.getContext();
						ItemMeta meta = Objects.requireNonNull(i.getItemMeta());

						meta.setLore(color("&b&oClick to reveal player information."));

						meta.setDisplayName(StringUtils.use("&f&lDisplay name: " + c.getColor() + associate.getNickname()).translate());

						i.setItemMeta(meta);

						return i;
					}).setClick(click -> {
						Player p = click.getPlayer();
						UI.select(Singular.MEMBER_INFO, e.getContext().getPlayer().getUniqueId()).open(p);
					});
				})
				.extraElements()
				.invoke(() -> {
					Item.Edit edit = new Item.Edit(Arrays.stream(Material.values()).anyMatch(m -> m.name().equals("WHITE_BANNER")) ? Items.getMaterial("WHITEBANNER") : Items.getMaterial("PAPER")).setTitle("&7Logo &3►");

					List<String> result = new LinkedList<>();

					if (s != null) {
						result.add(" ");
						result.addAll(s);
						result.add(" ");
						edit.setLore(result.toArray(new String[0]));
					}

					return edit.build();

				}, getMiscSlot(), paginatedClick -> {

				})
				.add()
				.setupBorder()
				.setBorderType(Arrays.stream(Material.values()).anyMatch(m -> m.name().equals("LIGHT_GRAY_STAINED_GLASS_PANE")) ? Items.getMaterial("LIGHT_GRAY_STAINED_GLASS_PANE") : Items.getMaterial("STAINEDGLASSPANE"))
				.setFillType(new Item.Edit(SkullType.COMMAND_BLOCK.get()).setTitle(" ").build())
				.build()
				.limit(getAmntPer())
				.build();
	}

	public static Menu.Paginated<Clan> browseEdit() {
		return new PaginatedBuilder<>(DefaultClan.action.getTop())
				.forPlugin(ClansPro.getInstance())
				.setTitle(DefaultClan.action.color("&0&l» &3&lSelect a clan"))
				.setAlreadyFirst(DefaultClan.action.color(DefaultClan.action.alreadyFirstPage()))
				.setAlreadyLast(DefaultClan.action.color(DefaultClan.action.alreadyLastPage()))
				.setNavigationLeft(getLeft(), getLeftSlot(), PaginatedClickAction::sync)
				.setNavigationRight(getRight(), getRightSlot(), PaginatedClickAction::sync)
				.setNavigationBack(getBack(), getBackSlot(), click -> UI.select(Singular.SETTINGS_WINDOW).open(click.getPlayer()))
				.setSize(getRows())
				.setCloseAction(close -> {
					LAST_MENU.put(close.getPlayer().getUniqueId(), Paginated.CLAN_ROSTER);
					close.clear();
				})
				.setupProcess(e -> {
					Material mat = ClansAPI.getData().getMaterial("clan");
					if (mat == null) {
						mat = Material.PAPER;
					}
					Material finalMat = mat;
					e.setItem(() -> {
						ItemStack i = new ItemStack(finalMat);
						i.setType(finalMat);
						Clan cl = e.getContext();
						int a1 = 0;
						StringBuilder members = new StringBuilder("&b&o");
						for (String id : cl.getMembersList()) {
							a1++;
							if (a1 == 1) {
								members.append("&b&o").append(Bukkit.getOfflinePlayer(UUID.fromString(id)).getName());
							} else {
								members.append("&f, &b&o").append(Bukkit.getOfflinePlayer(UUID.fromString(id)).getName());
							}
						}
						String memlist = members.toString();
						if (memlist.length() > 44) {
							memlist = memlist.substring(0, 44) + "...";
						}

						String color = cl.getColor().replace("&k", "");

						double power = cl.getPower();

						String pvp;

						if (cl.isPeaceful()) {
							pvp = "&a&lPEACE";
						} else {
							pvp = "&4&lWAR";
						}

						int ownedLand = cl.getOwnedClaimsList().length;

						StringBuilder idShort = new StringBuilder();
						for (int j = 0; j < 4; j++) {
							idShort.append(cl.getId().toString().charAt(j));
						}
						String id = idShort.toString();

						boolean baseSet = cl.getBase() != null;

						String desc = cl.getDescription();

						ItemMeta meta = i.getItemMeta();

						meta.setLore(color("&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
								"&3Color: " + color.replace("&", "&f»" + color),
								"&3Description: " + color + desc,
								"&3Power: " + color + cl.format(String.valueOf(power)),
								"&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
								"&3Has base: " + color + baseSet,
								"&3Land Owned: &f(" + color + ownedLand + "&f)",
								"&3Mode: " + pvp,
								"&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
								"&3Members: " + color + memlist.replace("&b&o", color).replace("&f, &b...", color + "..."),
								"&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
								"&b&oClick to edit this clan."));

						meta.setDisplayName(StringUtils.use("&5&lClan: " + cl.getColor() + cl.getName() + "&r &f(" + "&3#" + color + id + "&f)").translate());

						i.setItemMeta(meta);

						return i;
					}).setClick(click -> {
						Player p = click.getPlayer();
						UI.select(Singular.CLAN_EDIT, e.getContext().getId()).open(p);
					});
				})
				.setupBorder()
				.setBorderType(Arrays.stream(Material.values()).anyMatch(m -> m.name().equals("LIGHT_GRAY_STAINED_GLASS_PANE")) ? Items.getMaterial("LIGHT_GRAY_STAINED_GLASS_PANE") : Items.getMaterial("STAINEDGLASSPANE"))
				.setFillType(new Item.Edit(SkullType.COMMAND_BLOCK.get()).setTitle(" ").build())
				.build()
				.limit(getAmntPer())
				.build();
	}

	public static Menu.Paginated<ClanAssociate> edit(Clan c) {
		return new PaginatedBuilder<>(c.getMembers().list())
				.forPlugin(ClansPro.getInstance())
				.setTitle(DefaultClan.action.color("&0&l» &3&lSelect a member"))
				.setAlreadyFirst(DefaultClan.action.color(DefaultClan.action.alreadyFirstPage()))
				.setAlreadyLast(DefaultClan.action.color(DefaultClan.action.alreadyLastPage()))
				.setNavigationLeft(getLeft(), getLeftSlot(), PaginatedClickAction::sync)
				.setNavigationRight(getRight(), getRightSlot(), PaginatedClickAction::sync)
				.setNavigationBack(getBack(), getBackSlot(), click -> UI.select(Singular.CLAN_EDIT, c.getId()).open(click.getPlayer()))
				.setSize(getRows())
				.setCloseAction(PaginatedCloseAction::clear)
				.setupProcess(e -> e.setItem(() -> {
					ItemStack copy = e.getContext().getHead();
					if (copy == null)
						copy = new ItemStack(Material.PLAYER_HEAD);
					ItemStack i = new ItemStack(copy);
					ClanAssociate associate = e.getContext();
					ItemMeta meta = Objects.requireNonNull(i.getItemMeta());

					meta.setLore(color("&e&oClick to edit me."));

					meta.setDisplayName(StringUtils.use("&6&oDisplay name:&r " + c.getColor() + associate.getNickname()).translate());

					i.setItemMeta(meta);

					return i;
				}).setClick(click -> {
					Player p = click.getPlayer();
					UI.select(Singular.MEMBER_EDIT, e.getContext().getPlayer().getUniqueId()).open(p);
				}))
				.setupBorder()
				.setBorderType(Arrays.stream(Material.values()).anyMatch(m -> m.name().equals("LIGHT_GRAY_STAINED_GLASS_PANE")) ? Items.getMaterial("LIGHT_GRAY_STAINED_GLASS_PANE") : Items.getMaterial("STAINEDGLASSPANE"))
				.setFillType(new Item.Edit(SkullType.COMMAND_BLOCK.get()).setTitle(" ").build())
				.build()
				.limit(getAmntPer())
				.build();
	}

	public enum Paginated {
		CLAN_ROSTER, CLAN_ROSTER_EDIT, CLAN_TOP, MEMBER_LIST, MEMBER_LIST_EDIT, REGISTERED_CYCLES, ACTIVATED_CYCLES, DEACTIVATED_CYCLES
	}

	public enum Singular {
		ROSTER_ORGANIZATION, MEMBER_INFO, CYCLE_ORGANIZATION, SETTINGS_WINDOW, SHIELD_TAMPER, CLAN_EDIT, MEMBER_EDIT, ARENA_SETUP
	}

}
