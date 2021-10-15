package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Permission;
import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.clans.construct.extra.MessagePrefix;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.formatting.string.Paragraph;
import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import com.github.sanctum.labyrinth.gui.unity.construct.PaginatedMenu;
import com.github.sanctum.labyrinth.gui.unity.construct.SingularMenu;
import com.github.sanctum.labyrinth.gui.unity.impl.BorderElement;
import com.github.sanctum.labyrinth.gui.unity.impl.FillerElement;
import com.github.sanctum.labyrinth.gui.unity.impl.ItemElement;
import com.github.sanctum.labyrinth.gui.unity.impl.ListElement;
import com.github.sanctum.labyrinth.gui.unity.impl.MenuType;
import com.github.sanctum.labyrinth.library.Entities;
import com.github.sanctum.labyrinth.library.Item;
import com.github.sanctum.labyrinth.library.Items;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.skulls.SkullType;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum GUI {

	/**
	 *
	 */
	ARENA_SPAWN,
	/**
	 *
	 */
	ARENA_SURRENDER,
	/**
	 *
	 */
	ARENA_TRUCE,
	/**
	 * All activated clans addons
	 */
	ADDONS_ACTIVATED,
	/**
	 * All deactivated clans addons
	 */
	ADDONS_DEACTIVATED,
	/**
	 * All registered clans addons
	 */
	ADDONS_REGISTERED,
	/**
	 * Clan addon registration category selection.
	 */
	ADDONS_SELECT,
	/**
	 * Entire clan roster.
	 */
	CLAN_ROSTER,
	/**
	 * Roster category selection
	 */
	CLAN_ROSTER_SELECT,
	/**
	 * Most power clans on the server in ordered by rank.
	 */
	CLAN_ROSTER_TOP,
	/**
	 * View a clan member's run.
	 */
	MEMBER_INFO,

	MEMBER_EDIT,
	/**
	 * View a clan's member list.
	 */
	MEMBER_LIST,
	/**
	 * Modify clan arena settings live
	 */
	SETTINGS_ARENA,
	/**
	 * Edit a clans settings.
	 */
	SETTINGS_CLAN,
	/**
	 * View a list of clans to edit.
	 */
	SETTINGS_CLAN_ROSTER,
	/**
	 * Change the plugin language.
	 */
	SETTINGS_LANGUAGE,
	/**
	 * Edit a clan member's settings.
	 */
	SETTINGS_MEMBER,
	/**
	 * View a clan's member list to edit.
	 */
	SETTINGS_MEMBER_LIST,
	/**
	 * Select files to reload.
	 */
	SETTINGS_RELOAD,
	/**
	 * Select a category to manage.
	 */
	SETTINGS_SELECT,
	/**
	 * Modify the raid shield up/down times live
	 */
	SETTINGS_SHIELD;

	private static final Map<Player, String> tempSpot = new HashMap<>();

	private static Menu getTemp(Player player) {
		if (!tempSpot.containsKey(player)) {
			tempSpot.put(player, CLAN_ROSTER_TOP.name());
		}
		switch (tempSpot.get(player).toLowerCase()) {
			case "clan_roster_top":
				return CLAN_ROSTER_TOP.get();
			case "clan_roster":
				return CLAN_ROSTER.get();
		}
		return MenuType.SINGULAR.build().join();
	}

	public Menu get() {
		switch (this) {
			case CLAN_ROSTER:
				return MenuType.PAGINATED.build().setHost(ClansAPI.getInstance().getPlugin())
						.setProperty(Menu.Property.CACHEABLE, Menu.Property.RECURSIVE)
						.setTitle(Clan.ACTION.color(ClansAPI.getData().getMenuTitle("roster-list")))
						.setSize(getSize())
						.setProcessEvent(open -> tempSpot.put(open.getElement(), CLAN_ROSTER.name()))
						.setOpenEvent(open -> tempSpot.put(open.getElement(), CLAN_ROSTER.name()))
						.setKey("ClansPro:Roster")
						.setStock(i -> {
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setItem(SkullType.COMMAND_BLOCK.get()).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(border);
							i.addItem(b -> b.setElement(getLeftItem()).setSlot(getLeft()).setTypeAndAddAction(ItemElement.ControlType.BUTTON_BACK, click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								click.setConsumer((target, success) -> {

									if (success) {
										i.open(target);
									} else {
										Clan.ACTION.sendMessage(target, Clan.ACTION.color(Clan.ACTION.alreadyFirstPage()));
									}

								});
							}));

							i.addItem(b -> b.setElement(getRightItem()).setSlot(getRight()).setTypeAndAddAction(ItemElement.ControlType.BUTTON_NEXT, click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								click.setConsumer((target, success) -> {

									if (success) {
										i.open(target);
									} else {
										Clan.ACTION.sendMessage(target, Clan.ACTION.color(Clan.ACTION.alreadyLastPage()));
									}

								});
							}));

							i.addItem(b -> b.setElement(getBackItem()).setSlot(getBack()).setTypeAndAddAction(ItemElement.ControlType.BUTTON_EXIT, click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								CLAN_ROSTER_SELECT.get().open(click.getElement());
							}));

							i.addItem(new ListElement<>(ClansAPI.getInstance().getClanManager().getClans().list()).setLimit(getLimit()).setPopulate((c, element) -> {
								element.setElement(b -> {
									ItemStack it = new ItemStack(ClansAPI.getData().getItem("clan"));
									int a1 = 0;
									int a2 = 0;
									int a3 = 0;
									StringBuilder members = new StringBuilder("&b&o");
									for (String id : c.getMemberIds()) {
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

									String color = c.getPalette().getStart().replace("&k", "");

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

									ItemMeta meta = it.getItemMeta();

									String[] par = new Paragraph(desc).setRegex(Paragraph.COMMA_AND_PERIOD).get();

									List<String> result = new LinkedList<>();
									for (String a : ClansAPI.getData().CLAN_GUI_FORMAT) {
										result.add(MessageFormat.format(a, color.replace("&", "&f»" + color).replace("#", "&f»" + color + " "), (c.getPalette().isGradient() ? c.getPalette().toGradient().context(par[0]).join() : c.getPalette().getStart() + par[0]), (c.getPalette().isGradient() ? c.getPalette().toGradient().context(c.format(String.valueOf(c.getPower()))).join() : c.getPalette().getStart() + c.format(String.valueOf(c.getPower()))), baseSet, c.getPalette().getStart() + ownedLand, pvp, memlist, allylist, enemylist, color));
									}
									meta.setLore(color(result.toArray(new String[0])));

									String title = MessageFormat.format(ClansAPI.getData().getMenuCategory("clan"), color, c.getName(), id);

									meta.setDisplayName(StringUtils.use(title).translate());

									it.setItemMeta(meta);
									b.setItem(it);
									return b.build();
								}).setClick(click -> {
									click.setCancelled(true);
									click.setHotbarAllowed(false);
									MEMBER_LIST.get(c).open(click.getElement());
								});
							}).setLimit(getLimit()));
						}).orGet(m -> m instanceof PaginatedMenu && m.getKey().isPresent() && m.getKey().get().equals("ClansPro:Roster"));
			case SETTINGS_CLAN_ROSTER:
				return MenuType.PAGINATED.build().setHost(ClansAPI.getInstance().getPlugin())
						.setProperty(Menu.Property.CACHEABLE, Menu.Property.RECURSIVE)
						.setTitle("&0&l» &3&lSelect a clan")
						.setSize(getSize())
						.setKey("ClansPro:Roster_edit")
						.setOpenEvent(open -> tempSpot.put(open.getElement(), CLAN_ROSTER_TOP.name()))
						.setProcessEvent(open -> tempSpot.put(open.getElement(), CLAN_ROSTER_TOP.name()))
						.setStock(i -> {
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setItem(SkullType.COMMAND_BLOCK.get()).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(border);
							i.addItem(b -> b.setElement(getLeftItem()).setSlot(getLeft()).setTypeAndAddAction(ItemElement.ControlType.BUTTON_BACK, click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								click.setConsumer((target, success) -> {

									if (success) {
										i.open(target);
									} else {
										Clan.ACTION.sendMessage(target, Clan.ACTION.color(Clan.ACTION.alreadyFirstPage()));
									}

								});
							}));

							i.addItem(b -> b.setElement(getRightItem()).setSlot(getRight()).setTypeAndAddAction(ItemElement.ControlType.BUTTON_NEXT, click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								click.setConsumer((target, success) -> {

									if (success) {
										i.open(target);
									} else {
										Clan.ACTION.sendMessage(target, Clan.ACTION.color(Clan.ACTION.alreadyLastPage()));
									}

								});
							}));

							i.addItem(b -> b.setElement(getBackItem()).setSlot(getBack()).setTypeAndAddAction(ItemElement.ControlType.BUTTON_EXIT, click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								SETTINGS_SELECT.get().open(click.getElement());
							}));

							i.addItem(new ListElement<>(Clan.ACTION.getMostPowerful()).setLimit(getLimit()).setPopulate((c, element) -> {
								element.setElement(b -> {
									ItemStack it = new ItemStack(ClansAPI.getData().getItem("clan"));
									int a1 = 0;
									int a2 = 0;
									int a3 = 0;
									StringBuilder members = new StringBuilder("&b&o");
									for (String id : c.getMemberIds()) {
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

									String color = c.getPalette().getStart().replace("&k", "");

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

									String[] par = new Paragraph(desc).setRegex(Paragraph.COMMA_AND_PERIOD).get();

									List<String> result = new LinkedList<>();
									for (String a : ClansAPI.getData().CLAN_GUI_FORMAT) {
										result.add(MessageFormat.format(a, color.replace("&", "&f»" + color).replace("#", "&f»" + color + " "), (c.getPalette().isGradient() ? c.getPalette().toGradient().context(par[0]).join() : c.getPalette().getStart() + par[0]), (c.getPalette().isGradient() ? c.getPalette().toGradient().context(c.format(String.valueOf(c.getPower()))).join() : c.getPalette().getStart() + c.format(String.valueOf(c.getPower()))), baseSet, c.getPalette().getStart() + ownedLand, pvp, memlist, allylist, enemylist, color));
									}
									return b.setItem(it).setLore(result).setTitle(MessageFormat.format(ClansAPI.getData().getMenuCategory("clan"), color, c.getName(), id)).build();
								}).setClick(click -> {
									click.setCancelled(true);
									click.setHotbarAllowed(false);
									SETTINGS_CLAN.get(c).open(click.getElement());
								});
							}).setLimit(getLimit()));

						}).orGet(m -> m instanceof PaginatedMenu && m.getKey().isPresent() && m.getKey().get().equals("ClansPro:Roster_edit"));
			case CLAN_ROSTER_TOP:
				return MenuType.PAGINATED.build().setHost(ClansAPI.getInstance().getPlugin())
						.setProperty(Menu.Property.CACHEABLE, Menu.Property.RECURSIVE)
						.setTitle(Clan.ACTION.color(ClansAPI.getData().getMenuTitle("top-list")))
						.setSize(getSize())
						.setKey("ClansPro:Roster_top")
						.setOpenEvent(open -> tempSpot.put(open.getElement(), CLAN_ROSTER_TOP.name()))
						.setProcessEvent(open -> tempSpot.put(open.getElement(), CLAN_ROSTER_TOP.name()))
						.setStock(i -> {
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setItem(SkullType.COMMAND_BLOCK.get()).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(border);
							i.addItem(b -> b.setElement(getLeftItem()).setSlot(getLeft()).setTypeAndAddAction(ItemElement.ControlType.BUTTON_BACK, click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								click.setConsumer((target, success) -> {

									if (success) {
										i.open(target);
									} else {
										Clan.ACTION.sendMessage(target, Clan.ACTION.color(Clan.ACTION.alreadyFirstPage()));
									}

								});
							}));

							i.addItem(b -> b.setElement(getRightItem()).setSlot(getRight()).setTypeAndAddAction(ItemElement.ControlType.BUTTON_NEXT, click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								click.setConsumer((target, success) -> {

									if (success) {
										i.open(target);
									} else {
										Clan.ACTION.sendMessage(target, Clan.ACTION.color(Clan.ACTION.alreadyLastPage()));
									}

								});
							}));

							i.addItem(b -> b.setElement(getBackItem()).setSlot(getBack()).setTypeAndAddAction(ItemElement.ControlType.BUTTON_EXIT, click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								CLAN_ROSTER_SELECT.get().open(click.getElement());
							}));

							i.addItem(new ListElement<>(Clan.ACTION.getMostPowerful()).setLimit(getLimit()).setComparator((o1, o2) -> Double.compare(o2.getData().get().getPower(), o1.getData().get().getPower())).setPopulate((c, element) -> {
								element.setElement(b -> {
									ItemStack it = new ItemStack(ClansAPI.getData().getItem("clan"));
									int a1 = 0;
									int a2 = 0;
									int a3 = 0;
									StringBuilder members = new StringBuilder("&b&o");
									for (String id : c.getMemberIds()) {
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

									String color = c.getPalette().getStart().replace("&k", "");

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

									ItemMeta meta = it.getItemMeta();

									String[] par = new Paragraph(desc).setRegex(Paragraph.COMMA_AND_PERIOD).get();

									List<String> result = new LinkedList<>();
									for (String a : ClansAPI.getData().CLAN_GUI_FORMAT) {
										result.add(MessageFormat.format(a, color.replace("&", "&f»" + color).replace("#", "&f»" + color + " "), (c.getPalette().isGradient() ? c.getPalette().toGradient().context(par[0]).join() : c.getPalette().getStart() + par[0]), (c.getPalette().isGradient() ? c.getPalette().toGradient().context(c.format(String.valueOf(c.getPower()))).join() : c.getPalette().getStart() + c.format(String.valueOf(c.getPower()))), baseSet, c.getPalette().getStart() + ownedLand, pvp, memlist, allylist, enemylist, color));
									}
									meta.setLore(color(result.toArray(new String[0])));

									String title = MessageFormat.format(ClansAPI.getData().getMenuCategory("clan"), color, c.getName(), id);

									meta.setDisplayName(StringUtils.use(title).translate());

									it.setItemMeta(meta);
									b.setItem(it);
									return b.build();
								}).setClick(click -> {
									click.setCancelled(true);
									click.setHotbarAllowed(false);
									MEMBER_LIST.get(c).open(click.getElement());
								});
							}).setLimit(getLimit()));

						}).orGet(m -> m instanceof PaginatedMenu && m.getKey().isPresent() && m.getKey().get().equals("ClansPro:Roster_top"));
			case CLAN_ROSTER_SELECT:
				return MenuType.SINGULAR.build().setHost(ClansAPI.getInstance().getPlugin())
						.setProperty(Menu.Property.RECURSIVE, Menu.Property.CACHEABLE)
						.setTitle(StringUtils.use(ClansAPI.getData().getMenuTitle("list-types")).translate())
						.setKey("ClansPro:Settings_select")
						.setSize(Menu.Rows.ONE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("ironbars")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(b -> b.setElement(it -> it.setType(ClansAPI.getData().getMaterial("top-list") != null ? ClansAPI.getData().getMaterial("top-list") : Material.PAPER)
									.setTitle(StringUtils.use(ClansAPI.getData().getMenuCategory("top-list")).translate()).build()).setSlot(3).setClick(click -> {
								click.setHotbarAllowed(false);
								click.setCancelled(true);
								Player p = click.getElement();
								CLAN_ROSTER_TOP.get().open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(ClansAPI.getData().getMaterial("roster-list") != null ? ClansAPI.getData().getMaterial("roster-list") : Material.PAPER)
									.setTitle(ClansAPI.getData().getMenuCategory("roster-list")).build()).setSlot(5).setClick(click -> {
								click.setHotbarAllowed(false);
								click.setCancelled(true);
								Player p = click.getElement();
								CLAN_ROSTER.get().open(p);
							}));

						}).orGet(m -> m instanceof SingularMenu && m.getKey().isPresent() && m.getKey().get().equals("ClansPro:Settings_Select")).addAction(c -> c.setCancelled(true));
			case SETTINGS_SELECT:
				return MenuType.SINGULAR.build()
						.setHost(ClansAPI.getInstance().getPlugin())
						.setProperty(Menu.Property.CACHEABLE)
						.setKey("ClansPro:Settings")
						.setSize(Menu.Rows.SIX)
						.setTitle(" &0&l» &2&oManagement Area")
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							i.addItem(border);
							i.addItem(b -> b.setElement(new ItemStack(Items.findMaterial("NAUTILUS_SHELL") != null ? Items.findMaterial("NAUTILUS_SHELL") : Items.findMaterial("NETHERSTAR"))).setSlot(33).setElement(ed -> ed.setLore("&bClick to manage all &dclans addons").setTitle("&7[&5Addon Management&7]").build()).setClick(click -> {
								click.setCancelled(true);
								ADDONS_SELECT.get().open(click.getElement());
							}));
							i.addItem(b -> b.setElement(new ItemStack(Items.findMaterial("CLOCK") != null ? Items.findMaterial("CLOCK") : Items.findMaterial("WATCH"))).setElement(ed -> ed.setTitle("&7[&2Shield Edit&7]").setLore("&bClick to edit the &3raid-shield").build()).setSlot(29).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_SHIELD.get().open(c.getElement());
							}));
							i.addItem(b -> b.setElement(new ItemStack(new ItemStack(Material.ENCHANTED_BOOK))).setElement(ed -> ed.setTitle("&7[&cAll Spy&7]").setLore("&7Click to toggle spy ability on all clan chat channels.").build()).setSlot(12).setClick(c -> {
								Player p = c.getElement();
								c.setCancelled(true);
								Bukkit.dispatchCommand(p, "cla spy clan");
								Bukkit.dispatchCommand(p, "cla spy ally");
								Bukkit.dispatchCommand(p, "cla spy custom");
							}));
							i.addItem(b -> b.setElement(new ItemStack(Material.ANVIL)).setElement(ed -> ed.setTitle("&7[&eClan Edit&7]").setLore("&7Click to manage clans.").build()).setSlot(10).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_CLAN_ROSTER.get().open(c.getElement());
							}));
							i.addItem(b -> b.setElement(new ItemStack(Material.DIAMOND_SWORD)).setElement(ed -> ed.setTitle("&7[&2War&7]").setLore("&7Click to manage arena spawns.").build()).setSlot(16).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_ARENA.get().open(c.getElement());
							}));
							i.addItem(b -> b.setElement(new ItemStack(ClansAPI.getData().getMaterial("clan") != null ? ClansAPI.getData().getMaterial("clan") : Material.PAPER)).setElement(ed -> ed.setTitle("&7[&eClan List&7]").setLore("&bClick to view the entire &6clan roster").build()).setSlot(14).setClick(c -> {
								c.setCancelled(true);
								CLAN_ROSTER_SELECT.get().open(c.getElement());
							}));
							i.addItem(b -> b.setElement(getBackItem()).setElement(ed -> ed.setTitle("&7[&4Close&7]").setLore("&cClick to close the gui.").build()).setSlot(49).setClick(c -> {
								c.setCancelled(true);
								c.getElement().closeInventory();
							}));
							i.addItem(b -> b.setElement(new ItemStack(Items.findMaterial("HEARTOFTHESEA") != null ? Items.findMaterial("HEARTOFTHESEA") : Items.findMaterial("SLIMEBALL"))).setElement(ed -> ed.setTitle("&7[&cReload&7]").setLore("&eClick to reload data.").build()).setSlot(31).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_RELOAD.get().open(c.getElement());
							}));
						}).orGet(m -> m instanceof SingularMenu && m.getKey().map("ClansPro:Settings"::equals).orElse(false));
			case ADDONS_SELECT:
				return MenuType.SINGULAR.build().setHost(ClansAPI.getInstance().getPlugin())
						.setSize(Menu.Rows.ONE)
						.setTitle("&2&oManage Addon Cycles &0&l»")
						.setKey("ClansPro:Addons")
						.setProperty(Menu.Property.CACHEABLE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.LAVA_BUCKET).setTitle("&7[&c&lDisabled&7]").setLore("&a&oTurn on disabled addons.").build()).setSlot(4).setClick(c -> {
								c.setCancelled(true);
								ADDONS_DEACTIVATED.get().open(c.getElement());
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.WATER_BUCKET).setTitle("&7[&3&lRunning&7]").setLore("&2&oTurn off running addons.").build()).setSlot(3).setClick(c -> {
								c.setCancelled(true);
								ADDONS_ACTIVATED.get().open(c.getElement());
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.BUCKET).setTitle("&7[&e&lLoaded&7]").setLore("&b&oView a list of all currently persistently cached addons.").build()).setSlot(5).setClick(c -> {
								c.setCancelled(true);
								ADDONS_REGISTERED.get().open(c.getElement());
							}));
							i.addItem(b -> b.setElement(ed -> ed.setItem(getBackItem()).build()).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_SELECT.get().open(c.getElement());
							}).setSlot(8));
						})
						.orGet(m -> m instanceof SingularMenu && m.getKey().map("ClansPro:Addons"::equals).orElse(false));
			case SETTINGS_ARENA:
				return MenuType.SINGULAR.build().setHost(ClansAPI.getInstance().getPlugin())
						.setSize(Menu.Rows.ONE)
						.setTitle("&2&oArena Spawns &0&l»")
						.setKey("ClansPro:War")
						.setProperty(Menu.Property.CACHEABLE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.SLIME_BALL).setTitle("&7[&cA&7]").setLore("&bClick to update the a team start location.").build()).setSlot(2).setClick(c -> {
								c.setCancelled(true);
								c.getElement().performCommand("cla setspawn a");
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.SLIME_BALL).setTitle("&7[&6B&7]").setLore("&bClick to update the b team start location.").build()).setSlot(3).setClick(c -> {
								c.setCancelled(true);
								c.getElement().performCommand("cla setspawn b");
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.SLIME_BALL).setTitle("&7[&eC&7]").setLore("&bClick to update the c team start location.").build()).setSlot(5).setClick(c -> {
								c.setCancelled(true);
								c.getElement().performCommand("cla setspawn c");
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.SLIME_BALL).setTitle("&7[&fD&7]").setLore("&bClick to update the d team start location.").build()).setSlot(6).setClick(c -> {
								c.setCancelled(true);
								c.getElement().performCommand("cla setspawn d");
							}));
							i.addItem(b -> b.setElement(ed -> ed.setItem(getBackItem()).build()).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_SELECT.get().open(c.getElement());
							}).setSlot(8));
						})
						.orGet(m -> m instanceof SingularMenu && m.getKey().map("ClansPro:War"::equals).orElse(false));
			case SETTINGS_RELOAD:
				return MenuType.SINGULAR.build().setHost(ClansAPI.getInstance().getPlugin())
						.setSize(Menu.Rows.ONE)
						.setTitle("&0&l» &eReload Files")
						.setKey("ClansPro:Reload")
						.setProperty(Menu.Property.CACHEABLE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.POTION).setTitle("&aConfig.yml").build()).setSlot(0).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_SELECT.get().open(c.getElement());
								DataManager.FileType.MISC_FILE.get("Config", "Configuration").reload();
								Message.form(c.getElement()).setPrefix(ClansAPI.getInstance().getPrefix().joined()).send("&aConfig file 'Config' reloaded.");
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.POTION).setTitle("&5Messages.yml").build()).setSlot(2).setClick(c -> {
								c.setCancelled(true);
								DataManager.FileType.MISC_FILE.get("Messages", "Configuration").reload();
								Message.form(c.getElement()).setPrefix(ClansAPI.getInstance().getPrefix().joined()).send("&aConfig file 'Messages' reloaded.");
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.POTION).setTitle("&2&lAll").build()).setSlot(4).setClick(c -> {
								c.setCancelled(true);
								Player p = c.getElement();
								FileManager config = ClansAPI.getInstance().getFileList().get("Config", "Configuration");
								FileManager message = ClansAPI.getInstance().getFileList().get("Messages", "Configuration");

								List<String> format = message.getRoot().getStringList("menu-format.clan");

								DataManager dataManager = ClansAPI.getData();

								dataManager.CLAN_GUI_FORMAT.clear();

								dataManager.CLAN_GUI_FORMAT.addAll(format);

								FileManager regions = ClansAPI.getInstance().getFileList().get("Regions", "Configuration");
								config.getRoot().reload();
								message.getRoot().reload();
								regions.getRoot().reload();

								if (config.read(fc -> fc.getString("Clans.lang").equalsIgnoreCase("en-US"))) {

									if (config.read(fc -> fc.getString("Clans.lang").equalsIgnoreCase("pt-BR"))) {

										ClansAPI.getData().getMain().getRoot().delete();
										DataManager.FileType.MISC_FILE.get("Messages", "Configuration").getRoot().delete();
										FileManager nc = FileList.search(ClansAPI.getInstance().getPlugin()).get("Config", "Configuration");
										FileManager nm = FileList.search(ClansAPI.getInstance().getPlugin()).get("Messages", "Configuration");
										FileList.search(ClansAPI.getInstance().getPlugin()).copyYML("Config_pt_br", nc);
										nc.getRoot().reload();
										FileList.search(ClansAPI.getInstance().getPlugin()).copyYML("Messages_pt_br", nm);
										nm.getRoot().reload();
										Message.form(p).setPrefix(ClansAPI.getInstance().getPrefix().joined()).send("&a&oAgora traduzido para o brasil!");

									}

								}

								SETTINGS_SELECT.get().open(p);

								ClansAPI.getInstance().getClanManager().refresh();

								Clan.ACTION.sendMessage(p, "&b&oAll configuration files reloaded.");
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.POTION).setTitle("&eLang Change").build()).setSlot(6).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_LANGUAGE.get().open(c.getElement());
							}));
							i.addItem(b -> b.setElement(ed -> ed.setItem(getBackItem()).build()).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_SELECT.get().open(c.getElement());
							}).setSlot(8));
						})
						.orGet(m -> m instanceof SingularMenu && m.getKey().map("ClansPro:Reload"::equals).orElse(false));
			case SETTINGS_SHIELD:
				return MenuType.SINGULAR.build().setHost(ClansAPI.getInstance().getPlugin())
						.setSize(Menu.Rows.ONE)
						.setTitle("&2&oRaid-Shield Settings &0&l»")
						.setKey("ClansPro:shield-edit")
						.setProperty(Menu.Property.CACHEABLE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(b -> b.setElement(ed -> ed.setType(Items.findMaterial("CLOCK") != null ? Items.findMaterial("CLOCK") : Items.findMaterial("WATCH")).setTitle("&a&oUp: Mid-day").setLore("&bClick to change the raid-shield to enable mid-day").build()).setSlot(4).setClick(c -> {
								c.setCancelled(true);
								ClansAPI.getInstance().getShieldManager().getTamper().setUpOverride(6000);
								ClansAPI.getInstance().getShieldManager().getTamper().setDownOverride(18000);
								Clan.ACTION.sendMessage(c.getElement(), "&aRaid-shield engagement changed to mid-day.");
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Items.findMaterial("CLOCK") != null ? Items.findMaterial("CLOCK") : Items.findMaterial("WATCH")).setTitle("&a&oUp: Sunrise").setLore("&bClick to change the raid-shield to enable on sunrise").build()).setSlot(3).setClick(c -> {
								c.setCancelled(true);
								ClansAPI.getInstance().getShieldManager().getTamper().setUpOverride(0);
								ClansAPI.getInstance().getShieldManager().getTamper().setDownOverride(13000);
								Clan.ACTION.sendMessage(c.getElement(), "&aRaid-shield engagement changed to sunrise.");
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Items.findMaterial("CLOCK") != null ? Items.findMaterial("CLOCK") : Items.findMaterial("WATCH")).setTitle("&a&oPermanent protection.").setLore("&bClick to freeze the raid-shield @ its current status").build()).setSlot(5).setClick(c -> {
								c.setCancelled(true);
								Player p = c.getElement();
								if (ClansAPI.getInstance().getShieldManager().getTamper().isOff()) {
									p.closeInventory();
									Clan.ACTION.sendMessage(p, "&aRaid-shield block has been lifted.");
									ClansAPI.getInstance().getShieldManager().getTamper().setIsOff(false);
								} else {
									Clan.ACTION.sendMessage(p, "&cRaid-shield has been blocked.");
									ClansAPI.getInstance().getShieldManager().getTamper().setIsOff(true);
								}
							}));
							i.addItem(b -> b.setElement(ed -> ed.setItem(getBackItem()).build()).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_SELECT.get().open(c.getElement());
							}).setSlot(8));
						})
						.orGet(m -> m instanceof SingularMenu && m.getKey().map("ClansPro:shield-edit"::equals).orElse(false));
			case SETTINGS_LANGUAGE:
				return MenuType.SINGULAR.build().setHost(ClansAPI.getInstance().getPlugin())
						.setSize(Menu.Rows.ONE)
						.setTitle("&0&l» &ePick a language")
						.setKey("ClansPro:Lang")
						.setProperty(Menu.Property.CACHEABLE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.BOOK).setTitle("&aEnglish").build()).setSlot(0).setClick(c -> {
								c.setCancelled(true);
								c.getElement().closeInventory();
								FileManager config = ClansAPI.getInstance().getFileList().get("Config", "Configuration");
								config.getRoot().delete();
								FileManager messages = ClansAPI.getInstance().getFileList().get("Messages", "Configuration");
								messages.getRoot().delete();
								FileManager nc = FileList.search(ClansAPI.getInstance().getPlugin()).get("Config", "Configuration");
								FileManager nm = FileList.search(ClansAPI.getInstance().getPlugin()).get("Messages", "Configuration");
								String type = config.read(f -> f.getNode("Formatting").getNode("file-type").toPrimitive().getString());
								if (type != null) {
									if (type.equals("JSON")) {
										FileList.search(ClansAPI.getInstance().getPlugin()).copyYML("Config_json", nc);
									} else {
										FileList.search(ClansAPI.getInstance().getPlugin()).copyYML("Config", nc);
									}
								} else {
									FileList.search(ClansAPI.getInstance().getPlugin()).copyYML("Config", nc);
								}
								nc.getRoot().reload();
								FileList.search(ClansAPI.getInstance().getPlugin()).copyYML("Messages", nm);
								nm.getRoot().reload();
								Message.form(c.getElement()).setPrefix(ClansAPI.getInstance().getPrefix().joined()).send("&a&oTranslated back to default english.");
								FileManager main = ClansAPI.getData().getMain();

								((ClansJavaPlugin) ClansAPI.getInstance().getPlugin()).setPrefix(new MessagePrefix(main.getRoot().getString("Formatting.prefix.prefix"), main.getRoot().getString("Formatting.prefix.text"), main.getRoot().getString("Formatting.prefix.suffix")));
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.BOOK).setTitle("&bPortuguese").build()).setSlot(2).setClick(c -> {
								c.setCancelled(true);
								c.getElement().closeInventory();
								FileManager config = ClansAPI.getInstance().getFileList().get("Config", "Configuration");
								config.getRoot().delete();
								FileManager messages = ClansAPI.getInstance().getFileList().get("Messages", "Configuration");
								messages.getRoot().delete();
								FileManager nc = FileList.search(ClansAPI.getInstance().getPlugin()).get("Config", "Configuration");
								FileManager nm = FileList.search(ClansAPI.getInstance().getPlugin()).get("Messages", "Configuration");
								String type = config.read(f -> f.getNode("Formatting").getNode("file-type").toPrimitive().getString());
								if (type != null) {
									if (type.equals("JSON")) {
										FileList.search(ClansAPI.getInstance().getPlugin()).copyYML("Config_pt_br_json", nc);
									} else {
										FileList.search(ClansAPI.getInstance().getPlugin()).copyYML("Config_pt_br", nc);
									}
								} else {
									FileList.search(ClansAPI.getInstance().getPlugin()).copyYML("Config_pt_br", nc);
								}
								nc.getRoot().reload();
								FileList.search(ClansAPI.getInstance().getPlugin()).copyYML("Messages_pt_br", nm);
								nm.getRoot().reload();
								Message.form(c.getElement()).setPrefix(ClansAPI.getInstance().getPrefix().joined()).send("&a&oAgora traduzido para o brasil!");

								FileManager main = ClansAPI.getData().getMain();

								((ClansJavaPlugin) ClansAPI.getInstance().getPlugin()).setPrefix(new MessagePrefix(main.getRoot().getString("Formatting.prefix.prefix"), main.getRoot().getString("Formatting.prefix.text"), main.getRoot().getString("Formatting.prefix.suffix")));
							}));
							i.addItem(b -> b.setElement(ed -> ed.setItem(getBackItem()).build()).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_RELOAD.get().open(c.getElement());
							}).setSlot(8));
						})
						.orGet(m -> m instanceof SingularMenu && m.getKey().map("ClansPro:Lang"::equals).orElse(false));
			case ADDONS_ACTIVATED:
				return MenuType.PAGINATED.build()
						.setHost(ClansAPI.getInstance().getPlugin())
						.setProperty(Menu.Property.CACHEABLE, Menu.Property.RECURSIVE)
						.setSize(Menu.Rows.SIX)
						.setTitle("&3&oRegistered Cycles &f(&2RUNNING&f) &8&l»")
						.setStock(i -> {
							ListElement<ClanAddon> list = new ListElement<>(ClanAddonQuery.getUsedNames().stream().map(ClanAddonQuery::getAddon).collect(Collectors.toList()));
							list.setLimit(28);
							list.setPopulate((addon, item) -> {
								ItemStack stack = new ItemStack(Material.CHEST);

								ItemMeta meta = stack.getItemMeta();

								meta.setLore(color("&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oPersistent: &f" + addon.isPersistent(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oDescription: &f" + addon.getDescription(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oVersion: &f" + addon.getVersion(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oAuthors: &f" + Arrays.toString(addon.getAuthors()), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oActive: &6&o" + ClanAddonQuery.getUsedNames().contains(addon.getName())));

								meta.setDisplayName(StringUtils.use("&3&o " + addon.getName() + " &8&l»").translate());

								stack.setItemMeta(meta);
								item.setElement(stack);
								item.setClick(click -> {
									click.setCancelled(true);
									Player p = click.getElement();
									if (ClanAddonQuery.disable(addon)) {
										for (String d : ClanAddonQuery.getDataLog()) {
											p.sendMessage(Clan.ACTION.color("&b" + d.replace("Clans [Pro]", "&3Clans &7[&6Pro&7]&b")));
										}
									}
									ADDONS_ACTIVATED.get().open(p);
								});

							});
							i.addItem(list);

							i.addItem(ed -> {
								ed.setElement(it -> it.setItem(getLeftItem()).build());
								ed.setType(ItemElement.ControlType.BUTTON_BACK);
								ed.setSlot(45);
							});

							i.addItem(ed -> {
								ed.setElement(it -> it.setItem(getRightItem()).build());
								ed.setType(ItemElement.ControlType.BUTTON_NEXT);
								ed.setSlot(53);
							});

							i.addItem(ed -> {
								ed.setElement(it -> it.setItem(getBackItem()).build());
								ed.setClick(click -> {
									click.setCancelled(true);
									ADDONS_SELECT.get().open(click.getElement());
								});
								ed.setSlot(49);
							});

							BorderElement<?> border = new BorderElement<>(i);

							for (Menu.Panel p : Menu.Panel.values()) {
								if (p != Menu.Panel.MIDDLE) {
									border.add(p, ed -> {
										Material mat = Arrays.stream(Material.values()).anyMatch(m -> m.name().equals("LIGHT_GRAY_STAINED_GLASS_PANE")) ? Items.findMaterial("LIGHT_GRAY_STAINED_GLASS_PANE") : Items.findMaterial("STAINEDGLASSPANE");
										ed.setElement(it -> it.setType(mat).setTitle(" ").build());
									});
								}
							}

							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> {
								ed.setElement(it -> it.setItem(SkullType.COMMAND_BLOCK.get()).setTitle(" ").build());
							});
							i.addItem(border);
							i.addItem(filler);

						})
						.join();
			case ADDONS_DEACTIVATED:
				return MenuType.PAGINATED.build()
						.setHost(ClansAPI.getInstance().getPlugin())
						.setProperty(Menu.Property.CACHEABLE, Menu.Property.RECURSIVE)
						.setSize(Menu.Rows.SIX)
						.setTitle("&3&oRegistered Cycles &f(&4DISABLED&f) &8&l»")
						.setStock(i -> {
							ListElement<ClanAddon> list = new ListElement<>(ClanAddonQuery.getUnusedNames().stream().map(ClanAddonQuery::getAddon).collect(Collectors.toList()));
							list.setLimit(28);
							list.setPopulate((addon, item) -> {
								ItemStack stack = new ItemStack(Material.CHEST);

								ItemMeta meta = stack.getItemMeta();

								meta.setLore(color("&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oPersistent: &f" + addon.isPersistent(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oDescription: &f" + addon.getDescription(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oVersion: &f" + addon.getVersion(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oAuthors: &f" + Arrays.toString(addon.getAuthors()), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oActive: &6&o" + ClanAddonQuery.getUsedNames().contains(addon.getName())));

								meta.setDisplayName(StringUtils.use("&3&o " + addon.getName() + " &8&l»").translate());

								stack.setItemMeta(meta);
								item.setElement(stack);
								item.setClick(click -> {
									click.setCancelled(true);
									Player p = click.getElement();
									if (ClanAddonQuery.enable(addon)) {
										for (String d : ClanAddonQuery.getDataLog()) {
											p.sendMessage(Clan.ACTION.color("&b" + Clan.ACTION.format(d, "Clans [Pro]", "&3Clans &7[&6Pro&7]&b")));
										}
									}
									ADDONS_ACTIVATED.get().open(p);
								});

							});
							i.addItem(list);

							i.addItem(ed -> {
								ed.setElement(it -> it.setItem(getLeftItem()).build());
								ed.setType(ItemElement.ControlType.BUTTON_BACK);
								ed.setSlot(45);
							});

							i.addItem(ed -> {
								ed.setElement(it -> it.setItem(getRightItem()).build());
								ed.setType(ItemElement.ControlType.BUTTON_NEXT);
								ed.setSlot(53);
							});

							i.addItem(ed -> {
								ed.setElement(it -> it.setItem(getBackItem()).build());
								ed.setClick(click -> {
									click.setCancelled(true);
									ADDONS_SELECT.get().open(click.getElement());
								});
								ed.setSlot(49);
							});

							BorderElement<?> border = new BorderElement<>(i);

							for (Menu.Panel p : Menu.Panel.values()) {
								if (p != Menu.Panel.MIDDLE) {
									border.add(p, ed -> {
										Material mat = Arrays.stream(Material.values()).anyMatch(m -> m.name().equals("LIGHT_GRAY_STAINED_GLASS_PANE")) ? Items.findMaterial("LIGHT_GRAY_STAINED_GLASS_PANE") : Items.findMaterial("STAINEDGLASSPANE");
										ed.setElement(it -> it.setType(mat).setTitle(" ").build());
									});
								}
							}

							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> {
								ed.setElement(it -> it.setItem(SkullType.COMMAND_BLOCK.get()).setTitle(" ").build());
							});
							i.addItem(border);
							i.addItem(filler);

						})
						.join();
			case ADDONS_REGISTERED:
				return MenuType.PAGINATED.build()
						.setHost(ClansAPI.getInstance().getPlugin())
						.setProperty(Menu.Property.CACHEABLE, Menu.Property.RECURSIVE)
						.setSize(Menu.Rows.SIX)
						.setTitle("&3&oRegistered Cycles &f(&6&lCACHE&f) &8&l»")
						.setStock(i -> {
							ListElement<ClanAddon> list = new ListElement<>(new ArrayList<>(ClanAddonQuery.getRegisteredAddons()));
							list.setLimit(28);
							list.setPopulate((addon, item) -> {
								ItemStack stack = new ItemStack(Material.CHEST);

								ItemMeta meta = stack.getItemMeta();

								meta.setLore(color("&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oPersistent: &f" + addon.isPersistent(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oDescription: &f" + addon.getDescription(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oVersion: &f" + addon.getVersion(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oAuthors: &f" + Arrays.toString(addon.getAuthors()), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oActive: &6&o" + ClanAddonQuery.getUsedNames().contains(addon.getName()), "&7Clicking these icons won't do anything."));

								meta.setDisplayName(StringUtils.use("&3&o " + addon.getName() + " &8&l»").translate());

								stack.setItemMeta(meta);
								item.setElement(stack);

							});
							i.addItem(list);

							i.addItem(ed -> {
								ed.setElement(it -> it.setItem(getLeftItem()).build());
								ed.setType(ItemElement.ControlType.BUTTON_BACK);
								ed.setSlot(45);
							});

							i.addItem(ed -> {
								ed.setElement(it -> it.setItem(getRightItem()).build());
								ed.setType(ItemElement.ControlType.BUTTON_NEXT);
								ed.setSlot(53);
							});

							i.addItem(ed -> {
								ed.setElement(it -> it.setItem(getBackItem()).build());
								ed.setClick(click -> {
									click.setCancelled(true);
									ADDONS_SELECT.get().open(click.getElement());
								});
								ed.setSlot(49);
							});

							BorderElement<?> border = new BorderElement<>(i);

							for (Menu.Panel p : Menu.Panel.values()) {
								if (p != Menu.Panel.MIDDLE) {
									border.add(p, ed -> {
										Material mat = Arrays.stream(Material.values()).anyMatch(m -> m.name().equals("LIGHT_GRAY_STAINED_GLASS_PANE")) ? Items.findMaterial("LIGHT_GRAY_STAINED_GLASS_PANE") : Items.findMaterial("STAINEDGLASSPANE");
										ed.setElement(it -> it.setType(mat).setTitle(" ").build());
									});
								}
							}

							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> {
								ed.setElement(it -> it.setItem(SkullType.COMMAND_BLOCK.get()).setTitle(" ").build());
							});
							i.addItem(border);
							i.addItem(filler);

						})
						.join();
		}
		return MenuType.SINGULAR.build().join();
	}

	public Menu get(Clan.Associate associate) {
		Clan cl = associate.getClan();
		String o = cl.getPalette().getStart();
		String balance;
		try {
			balance = cl.format(String.valueOf(EconomyProvision.getInstance().balance(associate.getUser().toBukkit()).orElse(0.0)));
		} catch (NoClassDefFoundError | NullPointerException e) {
			balance = "Un-Known";
		}
		String stats;
		String rank = associate.getRankTag();
		ZonedDateTime time = associate.getJoinDate().toInstant().atZone(ZoneId.systemDefault());
		Calendar cal = Calendar.getInstance();
		cal.setTime(associate.getJoinDate());
		String temporal = cal.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm";
		String date = time.getMonthValue() + "/" + time.getDayOfMonth() + "/" + time.getYear() + " @ " + time.getHour() + ":" + time.getMinute() + temporal;
		String bio = associate.getBiography();
		String kd = "" + associate.getKD();
		if (Bukkit.getVersion().contains("1.15") || LabyrinthProvider.getInstance().isNew()) {
			OfflinePlayer p = associate.getUser().toBukkit();
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

		String[] statist = Clan.ACTION.color(stats).split("\\|");

		String test = MessageFormat.format(ClansAPI.getData().getMenuTitle("member-information"), associate.getName());

		if (test.length() > 32)
			test = "&0&l» " + associate.getClan().getPalette().getStart() + associate.getUser().getName() + " &7Info";
		switch (this) {
			case MEMBER_INFO:
				String finalBalance = balance;
				return MenuType.SINGULAR.build().setHost(ClansAPI.getInstance().getPlugin())
						.setProperty(Menu.Property.RECURSIVE, Menu.Property.CACHEABLE)
						.setTitle(test)
						.setKey("ClansPro:member-" + associate.getName())
						.setSize(Menu.Rows.THREE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							i.addItem(border);
							i.addItem(b -> b.setElement(it -> it.setItem(Item.ColoredArmor.select(Item.ColoredArmor.Piece.TORSO).setColor(Color.RED).build())
									.setTitle("Clan:").setLore(o + cl.getName()).build()).setSlot(13).setClick(click -> {
								click.setHotbarAllowed(false);
								click.setCancelled(true);
								Player p = click.getElement();
								p.performCommand("c info " + cl.getName());
							}));
							i.addItem(b -> b.setElement(it -> it.setItem(associate.getHead()).setTitle("&6Click to teleport.").setLore(new Paragraph(bio + " &r- " + associate.getNickname()).setRegex(Paragraph.COMMA_AND_PERIOD).get()).build()).setSlot(4).setClick(click -> {
								click.setCancelled(true);
								if (associate.getUser().toBukkit().isOnline()) {
									Clan.Associate a = ClansAPI.getInstance().getAssociate(click.getElement()).orElse(null);

									if (a != null) {
										Clan.Associate.Teleport request = Clan.Associate.Teleport.get(a);
										if (request == null) {
											if (Objects.equals(associate.getUser().getName(), a.getUser().getName()))
												return;
											if (!associate.getClan().getMembers().contains(a)) {
												Clan.ACTION.sendMessage(click.getElement(), "&cYou're not in our clan.");
												return;
											}

											Clan.Associate.Teleport r = new Clan.Associate.Teleport.Impl(a, associate.getUser().toBukkit().getPlayer());
											r.teleport();
										} else {
											click.getElement().closeInventory();
											request.cancel();
											Message.form(click.getElement()).setPrefix(ClansAPI.getInstance().getPrefix().joined()).send("&cYou already have a teleport request pending, cancelling...");
										}
									}

								} else {
									Message.form(click.getElement()).setPrefix(ClansAPI.getInstance().getPrefix().joined()).send("&cIm not online at the moment, hit me up later!");
								}
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.BOOK).setTitle(o + "Statistics:").setLore(statist).build()).setSlot(12).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								p.closeInventory();
							}));
							i.addItem(b -> b.setElement(getBackItem()).setSlot(19).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MEMBER_LIST.get(cl).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setItem(new ItemStack(Items.findMaterial("GOLDENPICKAXE") != null ? Items.findMaterial("GOLDENPICKAXE") : Items.findMaterial("GOLDPICKAXE"))).setTitle(o + "Rank:").setLore(o + rank).build()).setSlot(14).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								p.closeInventory();
							}));
							i.addItem(b -> b.setElement(it -> it.setItem(new ItemStack(Items.findMaterial("ENDPORTALFRAME") != null ? Items.findMaterial("ENDPORTALFRAME") : Items.findMaterial("IRONINGOT"))).setTitle(o + "Join Date:").setLore(date).build()).setSlot(22).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								p.closeInventory();
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.NAME_TAG).setTitle("&c&lEdit").build()).setSlot(26).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);

								if (p.hasPermission("clanspro.admin")) {
									SETTINGS_MEMBER.get(associate).open(p);
								} else {
									click.getParent().remove(p, true);
									click.getParent().getParent().getParent().open(p);
								}
								p.closeInventory();
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.DIAMOND_SWORD).setTitle(o + "K/D:").setLore(o + kd).build()).setSlot(10).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								p.closeInventory();
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.GOLD_INGOT).setTitle(o + "Wallet:").setLore(o + finalBalance).build()).setSlot(16).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								p.closeInventory();
							}));

						}).orGet(m -> m instanceof SingularMenu && m.getKey().isPresent() && m.getKey().get().equals("ClansPro:member-" + associate.getName())).addAction(c -> c.setCancelled(true));
			case MEMBER_EDIT:
				return MenuType.SINGULAR.build().setHost(ClansAPI.getInstance().getPlugin())
						.setProperty(Menu.Property.RECURSIVE, Menu.Property.CACHEABLE)
						.setTitle("&0&l» " + cl.getPalette().getStart() + associate.getName() + " settings")
						.setKey("ClansPro:member-" + associate.getName() + "-edit")
						.setSize(Menu.Rows.THREE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							i.addItem(border);
							i.addItem(b -> b.setElement(it -> it.setItem(associate.getHead()).setFlags(ItemFlag.HIDE_ENCHANTS).addEnchantment(Enchantment.ARROW_DAMAGE, 100)
									.setTitle(ClansAPI.getData().getMenuNavigation("back")).build()).setSlot(13).setClick(click -> {
								click.setHotbarAllowed(false);
								click.setCancelled(true);
								Player p = click.getElement();
								MEMBER_LIST.get(associate.getClan()).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.ENCHANTED_BOOK).setTitle("&7[&6&lName Change&7]").setLore("&5Click to change my name.").build()).setSlot(22).setClick(click -> {
								Player p = click.getElement();
								ClansAPI.getInstance().getAssociate(p).ifPresent(a -> {
									if (Permission.MANAGE_NICKNAMES.test(a)) {
										MenuType.PRINTABLE.build()
												.setTitle("&2Type a name")
												.setSize(Menu.Rows.ONE)
												.setHost(ClansAPI.getInstance().getPlugin())
												.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
													c.setCancelled(true);
													c.setHotbarAllowed(false);
												}))).join()
												.addAction(c -> {
													c.setCancelled(true);
													c.setHotbarAllowed(false);
													if (c.getSlot() == 2) {
														associate.setNickname(c.getParent().getName());
														MEMBER_INFO.get(associate).open(c.getElement());
													}

												}).open(p);
									}
								});
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.IRON_BOOTS).setTitle("&7[&4Kick&7]").setLore("&5Click to kick me.").build()).setSlot(11).setClick(click -> {
								Player p = click.getElement();
								ClansAPI.getInstance().getAssociate(p).ifPresent(a -> {
									if (Permission.KICK_MEMBERS.test(a)) {
										associate.kick();
										MEMBER_LIST.get(a.getClan()).open(p);
									}
								});
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.DIAMOND).setTitle("&7[&aPromotion&7]").setLore("&5Click to promote me.").build()).setSlot(14).setClick(click -> {
								Player p = click.getElement();
								ClansAPI.getInstance().getAssociate(p).ifPresent(a -> {
									if (Permission.MANAGE_POSITIONS.test(a)) {
										Clan.ACTION.promotePlayer(associate.getUser().getId());
									}
								});
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.REDSTONE).setTitle("&7[&cDemotion&7]").setLore("&5Click to demote me.").build()).setSlot(15).setClick(click -> {
								Player p = click.getElement();
								ClansAPI.getInstance().getAssociate(p).ifPresent(a -> {
									if (Permission.MANAGE_POSITIONS.test(a)) {
										Clan.ACTION.demotePlayer(associate.getUser().getId());
									}
								});
							}));

						}).orGet(m -> m instanceof SingularMenu && m.getKey().isPresent() && m.getKey().get().equals("ClansPro:member-" + associate.getName() + "-edit")).addAction(c -> c.setCancelled(true));
			case SETTINGS_MEMBER:
				return MenuType.SINGULAR.build().setHost(ClansAPI.getInstance().getPlugin())
						.setProperty(Menu.Property.RECURSIVE, Menu.Property.CACHEABLE)
						.setTitle("&0&l» " + cl.getPalette().getStart() + associate.getName() + " settings")
						.setKey("ClansPro:member-" + associate.getName() + "-edit-settings")
						.setSize(Menu.Rows.THREE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							i.addItem(border);
							i.addItem(b -> b.setElement(it -> it.setItem(associate.getHead()).setFlags(ItemFlag.HIDE_ENCHANTS).addEnchantment(Enchantment.ARROW_DAMAGE, 100)
									.setTitle(ClansAPI.getData().getMenuNavigation("back")).build()).setSlot(13).setClick(click -> {
								click.setHotbarAllowed(false);
								click.setCancelled(true);
								Player p = click.getElement();
								SETTINGS_CLAN.get(cl).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.BOOK).setTitle("&7[&bBio change&7]").setLore("&5Click to change my bio.").build()).setSlot(4).setClick(click -> {
								Player p = click.getElement();
								MenuType.PRINTABLE.build()
										.setTitle("&2Type a bio")
										.setSize(Menu.Rows.ONE)
										.setHost(ClansAPI.getInstance().getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												associate.setBio(c.getParent().getName());
												MEMBER_INFO.get(associate).open(c.getElement());
											}

										}).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.ENCHANTED_BOOK).setTitle("&7[&6&lName Change&7]").setLore("&5Click to change my name.").build()).setSlot(22).setClick(click -> {
								Player p = click.getElement();
								MenuType.PRINTABLE.build()
										.setTitle("&2Type a name")
										.setSize(Menu.Rows.ONE)
										.setHost(ClansAPI.getInstance().getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												associate.setNickname(c.getParent().getName());
												MEMBER_INFO.get(associate).open(c.getElement());
											}

										}).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.WRITTEN_BOOK).setTitle("&7[&4Switch Clans&7]").setLore("&5Click to put me in another clan.").build()).setSlot(12).setClick(click -> {
								Player p = click.getElement();
								MenuType.PRINTABLE.build()
										.setTitle("&2Type a clan name")
										.setSize(Menu.Rows.ONE)
										.setHost(ClansAPI.getInstance().getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												String id = ClansAPI.getInstance().getClanID(c.getParent().getName());
												final UUID uid = associate.getUser().getId();
												if (id != null) {
													Clan clan = ClansAPI.getInstance().getClan(id);
													Clan.ACTION.removePlayer(uid);
													Clan.Associate newAssociate = clan.accept(uid);
													if (newAssociate != null) {
														MEMBER_INFO.get(newAssociate).open(c.getElement());
													}
												} else {
													Clan.ACTION.sendMessage(c.getElement(), Clan.ACTION.clanUnknown(c.getParent().getName()));
												}
											}

										}).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.IRON_BOOTS).setTitle("&7[&4Kick&7]").setLore("&5Click to kick me.").build()).setSlot(11).setClick(click -> {
								Player p = click.getElement();
								associate.kick();
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.DIAMOND).setTitle("&7[&aPromotion&7]").setLore("&5Click to promote me.").build()).setSlot(14).setClick(click -> {
								Player p = click.getElement();
								Clan.ACTION.promotePlayer(associate.getUser().getId());
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.REDSTONE).setTitle("&7[&cDemotion&7]").setLore("&5Click to demote me.").build()).setSlot(15).setClick(click -> {
								Player p = click.getElement();
								Clan.ACTION.demotePlayer(associate.getUser().getId());
							}));

						}).orGet(m -> m instanceof SingularMenu && m.getKey().isPresent() && m.getKey().get().equals("ClansPro:member-" + associate.getName() + "-edit-settings")).addAction(c -> c.setCancelled(true));
		}
		return MenuType.SINGULAR.build().join();
	}

	public Menu get(Clan clan) {
		switch (this) {
			case SETTINGS_CLAN:
				return MenuType.SINGULAR.build().setHost(ClansAPI.getInstance().getPlugin())
						.setProperty(Menu.Property.RECURSIVE, Menu.Property.CACHEABLE)
						.setTitle("&0&l» " + (clan.getPalette().isGradient() ? clan.getPalette().toString() : clan.getPalette().getStart() + clan.getName()) + " settings")
						.setKey("ClansPro:edit-" + clan.getName())
						.setSize(Menu.Rows.THREE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							i.addItem(border);
							i.addItem(b -> b.setElement(it -> it.setItem(clan.getOwner().getHead()).setFlags(ItemFlag.HIDE_ENCHANTS).addEnchantment(Enchantment.ARROW_DAMAGE, 100)
									.setLore("&5Click to manage clan members.").setTitle("&7[&6Member Edit&7]").build()).setSlot(13).setClick(click -> {
								click.setHotbarAllowed(false);
								click.setCancelled(true);
								Player p = click.getElement();
								SETTINGS_MEMBER_LIST.get(clan).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.BOOK).setTitle("&7[&bPassword change&7]").setLore("&5Click to change our password.").build()).setSlot(4).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&2Type a bio")
										.setSize(Menu.Rows.ONE)
										.setHost(ClansAPI.getInstance().getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												clan.setPassword(c.getParent().getName());
												p.performCommand("c i " + clan.getName());
											}

										}).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.ENDER_CHEST).setTitle("&7[&5Stash&7]").setLore("&5Click to live manage our stash.").build()).setSlot(3).setClick(click -> {
								Player p = click.getElement();
								Bukkit.dispatchCommand(p, "cla view " + clan.getName() + " stash");
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.CHEST).setTitle("&7[&6Vault&7]").setLore("&5Click to live manage our vault.").build()).setSlot(5).setClick(click -> {
								Player p = click.getElement();
								Bukkit.dispatchCommand(p, "cla view " + clan.getName() + " vault");
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.SLIME_BALL).setTitle("&7[&a+Claims&7]").setLore("&5Click to give us claims.").build()).setSlot(1).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&2Type an amount")
										.setSize(Menu.Rows.ONE)
										.setHost(ClansAPI.getInstance().getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												try {
													int amount = Integer.parseInt(c.getParent().getName());
													clan.addMaxClaim(amount);
													p.performCommand("c i " + clan.getName());
												} catch (NumberFormatException ignored) {
												}
											}

										}).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.SLIME_BALL).setTitle("&7[&a+Power&7]").setLore("&5Click to give us power..").build()).setSlot(10).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&2Type an amount")
										.setSize(Menu.Rows.ONE)
										.setHost(ClansAPI.getInstance().getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												try {
													double amount = Double.parseDouble(c.getParent().getName());
													clan.givePower(amount);
													p.performCommand("c i " + clan.getName());
												} catch (NumberFormatException ignored) {
												}
											}

										}).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.GOLD_INGOT).setTitle("&7[&a+Money&7]").setLore("&5Click to give us money.").build()).setSlot(19).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&2Type an amount")
										.setSize(Menu.Rows.ONE)
										.setHost(ClansAPI.getInstance().getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												try {
													double amount = Double.parseDouble(c.getParent().getName());
													p.closeInventory();
													p.performCommand("cla give " + clan.getName() + " money " + amount);
												} catch (NumberFormatException ignored) {
												}
											}

										}).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.SLIME_BALL).setTitle("&7[&c-Claims&7]").setLore("&5Click to take claims from us..").build()).setSlot(7).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&2Type an amount")
										.setSize(Menu.Rows.ONE)
										.setHost(ClansAPI.getInstance().getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												try {
													int amount = Integer.parseInt(c.getParent().getName());
													clan.takeMaxClaim(amount);
													p.performCommand("c i " + clan.getName());
												} catch (NumberFormatException ignored) {
												}
											}

										}).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.SLIME_BALL).setTitle("&7[&c-Power&7]").setLore("&5Click to take power from us.").build()).setSlot(16).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&2Type an amount")
										.setSize(Menu.Rows.ONE)
										.setHost(ClansAPI.getInstance().getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												try {
													double amount = Double.parseDouble(c.getParent().getName());
													clan.takePower(amount);
													p.performCommand("c i " + clan.getName());
												} catch (NumberFormatException ignored) {
												}
											}

										}).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.IRON_INGOT).setTitle("&7[&c-Money&7]").setLore("&5Click to take money from us.").build()).setSlot(25).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&2Type an amount")
										.setSize(Menu.Rows.ONE)
										.setHost(ClansAPI.getInstance().getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												try {
													double amount = Double.parseDouble(c.getParent().getName());
													p.performCommand("cla take " + clan.getName() + " money " + amount);
												} catch (NumberFormatException ignored) {
												}
											}

										}).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.NAME_TAG).setTitle("&7[&e&lTag&7]").setLore("&5Click to change our tag.").build()).setSlot(22).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&2Type a new tag")
										.setSize(Menu.Rows.ONE)
										.setHost(ClansAPI.getInstance().getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												clan.setName(c.getParent().getName());
												p.performCommand("c i " + c.getParent().getName());
											}

										}).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.WRITTEN_BOOK).setTitle("&7[&cDescription&7]").setLore("&5Click to change our description.").build()).setSlot(12).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&2Type a description")
										.setSize(Menu.Rows.ONE)
										.setHost(ClansAPI.getInstance().getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												clan.setDescription(c.getParent().getName());
												p.performCommand("c i " + c.getParent().getName());
											}

										}).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.WRITTEN_BOOK).setTitle("&7[&9Color&7]").setLore("&5Click to change our color.").build()).setSlot(14).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&2Type a color")
										.setSize(Menu.Rows.ONE)
										.setHost(ClansAPI.getInstance().getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												clan.setColor(c.getParent().getName());
												p.performCommand("c i " + clan.getName());
											}

										}).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setItem(getBackItem()).build()).setSlot(17).setClick(click -> {
								Player p = click.getElement();
								SETTINGS_CLAN_ROSTER.get().open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.HOPPER).setTitle("&7[&6Base&7]").setLore("&5Click to update our base location.").build()).setSlot(8).setClick(click -> {
								Player p = click.getElement();
								clan.setBase(p.getLocation());
								Clan.ACTION.sendMessage(p, "&e" + clan.getName() + " base location updated");
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.LAVA_BUCKET).setTitle("&7[&4Close&7]").setLore("&5Click to close our clan.").build()).setSlot(26).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&01 for &aYES &02 for &cNO")
										.setSize(Menu.Rows.ONE)
										.setHost(ClansAPI.getInstance().getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle("0").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												if (c.getParent().getName().equals("1")) {
													Clan.ACTION.removePlayer(clan.getOwner().getUser().getId());
													p.closeInventory();
												} else {
													p.closeInventory();
													Clan.ACTION.sendMessage(p, "&cFailed to confirm deletion.");
												}
											}

										}).open(p);
							}));

						}).orGet(m -> m instanceof SingularMenu && m.getKey().isPresent() && m.getKey().get().equals("ClansPro:edit-" + clan.getName())).addAction(c -> c.setCancelled(true));
			case SETTINGS_MEMBER_LIST:
				return MenuType.PAGINATED.build()
						.setHost(ClansAPI.getInstance().getPlugin())
						.setKey("ClansPro:" + clan.getName() + "-members-edit")
						.setSize(getSize())
						.setTitle(Clan.ACTION.color(ClansAPI.getData().getMenuTitle("member-list")))
						.setStock(i -> {
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setItem(SkullType.COMMAND_BLOCK.get()).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(border);
							i.addItem(b -> b.setElement(getLeftItem()).setSlot(getLeft()).setTypeAndAddAction(ItemElement.ControlType.BUTTON_BACK, click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								click.setConsumer((target, success) -> {

									if (success) {
										i.open(target);
									} else {
										Clan.ACTION.sendMessage(target, Clan.ACTION.color(Clan.ACTION.alreadyFirstPage()));
									}

								});
							}));

							i.addItem(b -> b.setElement(getRightItem()).setSlot(getRight()).setTypeAndAddAction(ItemElement.ControlType.BUTTON_NEXT, click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								click.setConsumer((target, success) -> {
									if (success) {
										i.open(target);
									} else {
										Clan.ACTION.sendMessage(target, Clan.ACTION.color(Clan.ACTION.alreadyLastPage()));
									}

								});
							}));

							i.addItem(b -> b.setElement(getBackItem()).setSlot(getBack()).setTypeAndAddAction(ItemElement.ControlType.BUTTON_EXIT, click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								SETTINGS_CLAN.get(clan).open(click.getElement());
							}));
							i.addItem(new ListElement<>(new ArrayList<>(clan.getMembers())).setLimit(getLimit()).setPopulate((value, element) -> element.setElement(it -> it.setItem(value.getHead()).setTitle(clan.getPalette().getStart() + value.getName()).setLore("&5Click to view my information.").build()).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_MEMBER.get(value).open(c.getElement());
							})));
						}).orGet(m -> m instanceof PaginatedMenu && m.getKey().map(("ClansPro:" + clan.getName() + "-members-edit")::equals).orElse(false));
			case MEMBER_LIST:
				return MenuType.PAGINATED.build()
						.setHost(ClansAPI.getInstance().getPlugin())
						.setKey("ClansPro:" + clan.getName() + "-members")
						.setSize(getSize())
						.setTitle(Clan.ACTION.color(ClansAPI.getData().getMenuTitle("member-list")))
						.setStock(i -> {
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setItem(SkullType.COMMAND_BLOCK.get()).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(border);
							i.addItem(b -> b.setElement(getLeftItem()).setSlot(getLeft()).setTypeAndAddAction(ItemElement.ControlType.BUTTON_BACK, click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								click.setConsumer((target, success) -> {

									if (success) {
										i.open(target);
									} else {
										Clan.ACTION.sendMessage(target, Clan.ACTION.color(Clan.ACTION.alreadyFirstPage()));
									}

								});
							}));

							i.addItem(b -> b.setElement(getRightItem()).setSlot(getRight()).setTypeAndAddAction(ItemElement.ControlType.BUTTON_NEXT, click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								click.setConsumer((target, success) -> {
									if (success) {
										i.open(target);
									} else {
										Clan.ACTION.sendMessage(target, Clan.ACTION.color(Clan.ACTION.alreadyLastPage()));
									}

								});
							}));

							i.addItem(b -> b.setElement(getBackItem()).setSlot(getBack()).setTypeAndAddAction(ItemElement.ControlType.BUTTON_EXIT, click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								getTemp(click.getElement()).open(click.getElement());
							}));
							i.addItem(new ListElement<>(new ArrayList<>(clan.getMembers())).setLimit(getLimit()).setPopulate((value, element) -> element.setElement(it -> it.setItem(value.getHead()).setTitle(clan.getPalette().getStart() + value.getName()).setLore("&5Click to view my information.", "", "&c&oShift-Right click to edit (Member).").build()).setClick(c -> {
								c.setCancelled(true);
								if (clan.getMember(a -> a.getName().equals(c.getElement().getName())) != null) {
									if (c.getClickType() == ClickType.SHIFT_RIGHT) {
										MEMBER_EDIT.get(value).open(c.getElement());
									} else {
										MEMBER_INFO.get(value).open(c.getElement());
									}
								} else {
									MEMBER_INFO.get(value).open(c.getElement());
								}
							})));
						}).orGet(m -> m instanceof PaginatedMenu && m.getKey().map(("ClansPro:" + clan.getName() + "-members")::equals).orElse(false));
		}
		return MenuType.SINGULAR.build().join();
	}

	public Menu get(War war) {
		switch (this) {
			case ARENA_SPAWN:
				return MenuType.SINGULAR.build().setHost(ClansAPI.getInstance().getPlugin())
						.setSize(Menu.Rows.ONE)
						.setTitle("&2&oTeleport to &a" + war.getId() + " &0&l»")
						.setKey("ClansPro:war-" + war.getId())
						.setProperty(Menu.Property.CACHEABLE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(b -> b.setElement(ed -> ed.setType(Items.findMaterial("CLOCK") != null ? Items.findMaterial("CLOCK") : Items.findMaterial("WATCH")).setTitle("&7[&6&lClick&7]").setLore("&bClick to teleport to your spawn in arena &e" + war.getId()).build()).setSlot(4).setClick(c -> {
								c.setCancelled(true);
								ClansAPI.getInstance().getAssociate(c.getElement()).ifPresent(a -> {
									War.Team t = war.getTeam(a.getClan());
									Location loc = t.getSpawn();
									if (loc == null) {
										Clan.ACTION.sendMessage(c.getElement(), "&cYour team's spawn location isn't properly setup. Contact staff for support.");
										return;
									}
									c.getElement().teleport(loc);
								});
							}));
						})
						.orGet(m -> m instanceof SingularMenu && m.getKey().map(("ClansPro:war-" + war.getId())::equals).orElse(false));
			case ARENA_TRUCE:
				return MenuType.SINGULAR.build().setHost(ClansAPI.getInstance().getPlugin())
						.setSize(Menu.Rows.ONE)
						.setTitle("&2&oTruce Vote &0&l»")
						.setKey("ClansPro:war-" + war.getId() + "-truce")
						.setProperty(Menu.Property.CACHEABLE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(b -> b.setElement(ed -> ed.setType(Items.findMaterial("reddye") != null ? Items.findMaterial("reddye") : Items.findMaterial("lavabucket")).setTitle("&7[&4&lNO&7]").setLore("&cClick to vote no").build()).setSlot(2).setClick(c -> {
								c.setCancelled(true);
								ClansAPI.getInstance().getAssociate(c.getElement()).ifPresent(a -> {
									War.Vote v = war.getVote();
									v.cast(War.Vote.NO);
									for (Clan cl : war.getQueue().teams()) {
										cl.broadcast("&aWar participant " + a.getNickname() + " voted &cno &aon a truce.");
									}
									int acount = war.getQueue().size();
									if (acount > 1) {
										acount = Math.floorDiv(acount, 2);
									}
									if (v.count(War.Vote.NO) >= acount) {
										v.clear();
										LabyrinthProvider.getService(Service.MESSENGER).getNewMessage().setPrefix(ClansAPI.getInstance().getPrefix().joined()).broadcast("&c&oTruce amongst the clans failed . Not enough votes yes.");
									}
								});
								c.getElement().closeInventory();
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Items.findMaterial("greendye") != null ? Items.findMaterial("greendye") : Items.findMaterial("waterbucket")).setTitle("&7[&2&lYES&7]").setLore("&cClick to vote yes").build()).setSlot(6).setClick(c -> {
								c.setCancelled(true);
								ClansAPI.getInstance().getAssociate(c.getElement()).ifPresent(a -> {
									War.Team t = war.getTeam(a.getClan());
									if (t != null) {
										War.Vote v = war.getVote(t);
										v.cast(War.Vote.YES);
										for (Clan cl : war.getQueue().teams()) {
											cl.broadcast("&aWar participant " + a.getNickname() + " voted &6yes &aon a truce.");
										}
										int acount = war.getQueue().size();
										if (acount > 1) {
											acount = Math.floorDiv(acount, 2);
										}
										if (v.count(War.Vote.YES) >= acount) {
											if (v.isUnanimous()) {
												v.clear();
												LabyrinthProvider.getService(Service.MESSENGER).getNewMessage().setPrefix(ClansAPI.getInstance().getPrefix().joined()).broadcast("&c&oTruce amongst the clans failed . Not enough votes yes.");
											} else {
												// good to go cancel
												if (war.stop()) {
													war.reset();
													a.getClan().takePower(8.6);
													LabyrinthProvider.getService(Service.MESSENGER).getNewMessage().setPrefix(ClansAPI.getInstance().getPrefix().joined()).broadcast("&3&oA truce was called and the war is over.");
												}
											}
										}
									}
								});
								c.getElement().closeInventory();
							}));
						})
						.orGet(m -> m instanceof SingularMenu && m.getKey().map(("ClansPro:war-" + war.getId() + "-truce")::equals).orElse(false));
			case ARENA_SURRENDER:
				return MenuType.SINGULAR.build().setHost(ClansAPI.getInstance().getPlugin())
						.setSize(Menu.Rows.ONE)
						.setTitle("&2&oSurrender Vote &0&l»")
						.setKey("ClansPro:war-" + war.getId() + "-surrender")
						.setProperty(Menu.Property.CACHEABLE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(b -> b.setElement(ed -> ed.setType(Items.findMaterial("reddye") != null ? Items.findMaterial("reddye") : Items.findMaterial("lavabucket")).setTitle("&7[&4&lNO&7]").setLore("&cClick to vote no").build()).setSlot(2).setClick(c -> {
								c.setCancelled(true);
								ClansAPI.getInstance().getAssociate(c.getElement()).ifPresent(a -> {
									War.Team t = war.getTeam(a.getClan());
									if (t != null) {
										War.Vote v = war.getVote(t);
										v.cast(War.Vote.NO);
										for (Clan cl : war.getQueue().teams()) {
											cl.broadcast("&aWar participant " + a.getNickname() + " voted &cno &aon surrendering.");
										}
										int acount = war.getQueue().count(a.getClan());
										if (acount > 1) {
											acount = Math.floorDiv(acount, 2);
										}
										if (v.count(War.Vote.NO) >= acount) {
											v.clear();
											LabyrinthProvider.getService(Service.MESSENGER).getNewMessage().setPrefix(ClansAPI.getInstance().getPrefix().joined()).broadcast("&c&oClan " + a.getClan().getName() + " failed to surrender. Not enough votes yes.");
										}
									}
								});
								c.getElement().closeInventory();
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Items.findMaterial("greendye") != null ? Items.findMaterial("greendye") : Items.findMaterial("waterbucket")).setTitle("&7[&2&lYES&7]").setLore("&cClick to vote yes").build()).setSlot(6).setClick(c -> {
								c.setCancelled(true);
								ClansAPI.getInstance().getAssociate(c.getElement()).ifPresent(a -> {
									War.Team t = war.getTeam(a.getClan());
									if (t != null) {
										War.Vote v = war.getVote(t);
										v.cast(War.Vote.YES);
										for (Clan cl : war.getQueue().teams()) {
											cl.broadcast("&aWar participant " + a.getNickname() + " voted &6yes &aon surrendering.");
										}
										int acount = war.getQueue().count(a.getClan());
										if (acount > 1) {
											acount = Math.floorDiv(acount, 2);
										}
										if (v.count(War.Vote.YES) >= acount) {
											if (v.isUnanimous()) {
												v.clear();
												// Mutual votes. cancel voting
												LabyrinthProvider.getService(Service.MESSENGER).getNewMessage().setPrefix(ClansAPI.getInstance().getPrefix().joined()).broadcast("&c&oClan " + a.getClan().getName() + " failed to surrender. Voting came to a draw.");
											} else {
												// good to go cancel
												if (war.stop()) {
													war.reset();
													a.getClan().takePower(8.6);
													LabyrinthProvider.getService(Service.MESSENGER).getNewMessage().setPrefix(ClansAPI.getInstance().getPrefix().joined()).broadcast("&5&oClan " + a.getClan().getName() + " has surrendered.");
												}
											}
										}
									}
								});
								c.getElement().closeInventory();
							}));
						})
						.orGet(m -> m instanceof SingularMenu && m.getKey().map(("ClansPro:war-" + war.getId() + "-surrender")::equals).orElse(false));

			default:
				throw new IllegalArgumentException("Invalid GUI type provided for retrieval.");
		}
	}

	public Menu.Rows getSize() {
		return Menu.Rows.valueOf(ClansAPI.getData().getPath("pagination-size"));
	}

	protected List<String> color(String... text) {
		ArrayList<String> convert = new ArrayList<>();
		for (String t : text) {
			convert.add(StringUtils.use(t).translate());
		}
		return convert;
	}

	public ItemStack getRightItem() {
		ItemStack right = ClansAPI.getData().getItem("navigate_right");
		ItemMeta meta = right.getItemMeta();
		meta.setDisplayName(StringUtils.use(ClansAPI.getData().getMenuNavigation("right")).translate());
		right.setItemMeta(meta);
		return right;
	}

	public ItemStack getLeftItem() {
		ItemStack left = ClansAPI.getData().getItem("navigate_left");
		ItemMeta meta = left.getItemMeta();
		meta.setDisplayName(StringUtils.use(ClansAPI.getData().getMenuNavigation("left")).translate());
		left.setItemMeta(meta);
		return left;
	}

	public ItemStack getBackItem() {
		ItemStack back = ClansAPI.getData().getItem("back");
		ItemMeta meta = back.getItemMeta();
		meta.setDisplayName(StringUtils.use(ClansAPI.getData().getMenuNavigation("back")).translate());
		back.setItemMeta(meta);
		return back;
	}

	public int getLimit() {
		int amnt = 0;
		switch (getSize().getSize()) {
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

	public int getBack() {
		int amnt = 0;
		switch (getSize().getSize()) {
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

	public int getLeft() {
		int amnt = 0;
		switch (getSize().getSize()) {
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

	public int getRight() {
		int amnt = 0;
		switch (getSize().getSize()) {
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

}
