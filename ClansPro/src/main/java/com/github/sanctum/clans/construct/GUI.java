package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.formatting.string.ColoredString;
import com.github.sanctum.labyrinth.formatting.string.Paragraph;
import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import com.github.sanctum.labyrinth.gui.unity.construct.PaginatedMenu;
import com.github.sanctum.labyrinth.gui.unity.construct.SingularMenu;
import com.github.sanctum.labyrinth.gui.unity.impl.BorderElement;
import com.github.sanctum.labyrinth.gui.unity.impl.FillerElement;
import com.github.sanctum.labyrinth.gui.unity.impl.ItemElement;
import com.github.sanctum.labyrinth.gui.unity.impl.ListElement;
import com.github.sanctum.labyrinth.gui.unity.impl.MenuType;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum GUI {

	/**
	 * Entire clan roster.
	 */
	CLAN_ROSTER,
	/**
	 * Most power clans on the server in ordered by rank.
	 */
	CLAN_ROSTER_TOP,
	/**
	 * Roster category selection
	 */
	CLAN_ROSTER_SELECT,
	/**
	 * All activated event cycles (pro addons)
	 */
	EVENT_CYCLES_ACTIVATED,
	/**
	 * All deactivated event cycles (pro addons)
	 */
	EVENT_CYCLES_DEACTIVATED,
	/**
	 * All registered event cycles (pro addons)
	 */
	EVENT_CYCLES_REGISTERED,
	/**
	 * Event cycle category selection.
	 */
	EVENT_CYCLES_SELECT,
	/**
	 * View a clan member's info.
	 */
	MEMBER_INFO,
	/**
	 * View a clan's member list.
	 */
	MEMBER_LIST,
	/**
	 * Edit a clans settings.
	 */
	SETTINGS_CLAN_EDIT,
	/**
	 * View a list of clans to edit.
	 */
	SETTINGS_CLAN_ROSTER_EDIT,
	/**
	 * Change the plugin language.
	 */
	SETTINGS_LANGUAGE,
	/**
	 * Edit a clan member's settings.
	 */
	SETTINGS_MEMBER_EDIT,
	/**
	 * View a clan's member list to edit.
	 */
	SETTINGS_MEMBER_LIST_EDIT,
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
	SETTINGS_SHIELD,
	/**
	 * Modify clan arena settings live
	 */
	SETTINGS_ARENA;

	public Menu get() {
		switch (this) {
			case CLAN_ROSTER:
				return MenuType.PAGINATED.build().setHost(ClansAPI.getInstance().getPlugin())
						.setProperty(Menu.Property.CACHEABLE, Menu.Property.RECURSIVE)
						.setTitle(Clan.ACTION.color(ClansAPI.getData().getTitle("roster-list")))
						.setSize(getSize())
						.setKey("ClansPro:Roster")
						.setStock(i -> {
							i.addItem(b -> b.setElement(getLeftItem()).setSlot(getLeft()).setType(ItemElement.ControlType.BUTTON_BACK).setClick(click -> {
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

							i.addItem(b -> b.setElement(getRightItem()).setSlot(getRight()).setType(ItemElement.ControlType.BUTTON_NEXT).setClick(click -> {
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

							i.addItem(b -> b.setElement(getBackItem()).setSlot(getBack()).setType(ItemElement.ControlType.BUTTON_EXIT).setClick(click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								click.setConsumer((target, success) -> CLAN_ROSTER_SELECT.get().open(target));
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

									ItemMeta meta = it.getItemMeta();

									String[] par = new Paragraph(desc).setRegex(Paragraph.COMMA_AND_PERIOD).get();

									List<String> result = new LinkedList<>();
									for (String a : ClansAPI.getData().CLAN_GUI_FORMAT) {
										result.add(MessageFormat.format(a, color.replace("&", "&f»" + color), color + par[0], color + c.format(String.valueOf(power)), baseSet, color + ownedLand, pvp, memlist, allylist, enemylist, color));
									}
									meta.setLore(color(result.toArray(new String[0])));

									String title = MessageFormat.format(ClansAPI.getData().getCategory("clan"), c.getColor(), c.getName(), id);

									meta.setDisplayName(StringUtils.use(title).translate());

									it.setItemMeta(meta);
									b.setItem(it);
									return b.build();
								}).setClick(click -> {
									click.setCancelled(true);
									click.setHotbarAllowed(false);
									UI.view(c).open(click.getElement());
								});
							}));
						}).orGet(m -> m instanceof PaginatedMenu && m.getKey().isPresent() && m.getKey().get().equals("ClansPro:Roster"));
			case CLAN_ROSTER_TOP:
				return MenuType.PAGINATED.build().setHost(ClansAPI.getInstance().getPlugin())
						.setProperty(Menu.Property.CACHEABLE, Menu.Property.RECURSIVE)
						.setTitle(Clan.ACTION.color(ClansAPI.getData().getTitle("top-list")))
						.setSize(getSize())
						.setKey("ClansPro:Roster_top")
						.setStock(i -> {
							i.addItem(b -> b.setElement(getLeftItem()).setSlot(getLeft()).setType(ItemElement.ControlType.BUTTON_BACK).setClick(click -> {
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

							i.addItem(b -> b.setElement(getRightItem()).setSlot(getRight()).setType(ItemElement.ControlType.BUTTON_NEXT).setClick(click -> {
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

							i.addItem(b -> b.setElement(getBackItem()).setSlot(getBack()).setType(ItemElement.ControlType.BUTTON_EXIT).setClick(click -> {
								click.setCancelled(true);
								click.setHotbarAllowed(false);
								click.setConsumer((target, success) -> {

									if (!success) {
										CLAN_ROSTER_SELECT.get().open(target);
									}

								});
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

									ItemMeta meta = it.getItemMeta();

									String[] par = new Paragraph(desc).setRegex(Paragraph.COMMA_AND_PERIOD).get();

									List<String> result = new LinkedList<>();
									for (String a : ClansAPI.getData().CLAN_GUI_FORMAT) {
										result.add(MessageFormat.format(a, color.replace("&", "&f»" + color), color + par[0], color + c.format(String.valueOf(power)), baseSet, color + ownedLand, pvp, memlist, allylist, enemylist, color));
									}
									meta.setLore(color(result.toArray(new String[0])));

									String title = MessageFormat.format(ClansAPI.getData().getCategory("clan"), c.getColor(), c.getName(), id);

									meta.setDisplayName(StringUtils.use(title).translate());

									it.setItemMeta(meta);
									b.setItem(it);
									return b.build();
								}).setClick(click -> {
									click.setCancelled(true);
									click.setHotbarAllowed(false);
									UI.view(c).open(click.getElement());
								});
							}));

							BorderElement<?> element = new BorderElement<>(i);

							for (Menu.Panel p : Arrays.stream(Menu.Panel.values()).filter(p -> p != Menu.Panel.MIDDLE).collect(Collectors.toSet())) {
								element.add(p, it -> it.setType(ItemElement.ControlType.ITEM_BORDER).setElement(b -> b.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
							}

							i.addItem(element);

							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(it -> it.setElement(b -> b.setType(Material.GREEN_STAINED_GLASS_PANE).setTitle(" ").build()));
							i.addItem(filler);

						}).orGet(m -> m instanceof PaginatedMenu && m.getKey().isPresent() && m.getKey().get().equals("ClansPro:Roster_top"));
			case CLAN_ROSTER_SELECT:
				return MenuType.SINGULAR.build().setHost(ClansAPI.getInstance().getPlugin())
						.setProperty(Menu.Property.RECURSIVE, Menu.Property.CACHEABLE)
						.setTitle(StringUtils.use(ClansAPI.getData().getTitle("list-types")).translate())
						.setKey("ClansPro:Settings_select")
						.setSize(Menu.Rows.ONE)
						.setStock(i -> {

							i.addItem(b -> b.setElement(it -> it.setType(ClansAPI.getData().getMaterial("top-list") != null ? ClansAPI.getData().getMaterial("top-list") : Material.PAPER)
									.setTitle(StringUtils.use(ClansAPI.getData().getCategory("top-list")).translate()).build()).setSlot(3).setClick(click -> {
								click.setHotbarAllowed(false);
								click.setCancelled(true);
								Player p = click.getElement();
								CLAN_ROSTER_TOP.get().open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.ENCHANTED_BOOK).setTitle("&7[&f&oSearch&7]").setLore("&7Search for a clan.").build()).setSlot(4).setClick(click -> {
								// do something else with this button.
							}));
							i.addItem(b -> b.setElement(it -> it.setType(ClansAPI.getData().getMaterial("roster-list") != null ? ClansAPI.getData().getMaterial("roster-list") : Material.PAPER)
									.setTitle(ClansAPI.getData().getCategory("roster-list")).build()).setSlot(5).setClick(click -> {
								click.setHotbarAllowed(false);
								click.setCancelled(true);
								Player p = click.getElement();
								CLAN_ROSTER.get().open(p);
							}));

						}).orGet(m -> m instanceof SingularMenu && m.getKey().isPresent() && m.getKey().get().equals("ClansPro:Settings_Select")).addAction(c -> c.setCancelled(true));
			case SETTINGS_SELECT:
			case EVENT_CYCLES_SELECT:
			case SETTINGS_ARENA:
			case SETTINGS_RELOAD:
			case SETTINGS_SHIELD:
			case SETTINGS_LANGUAGE:
			case EVENT_CYCLES_ACTIVATED:
			case EVENT_CYCLES_DEACTIVATED:
			case EVENT_CYCLES_REGISTERED:
		}
		return MenuType.SINGULAR.build().join();
	}

	public Menu get(ClanAssociate associate) {
		switch (this) {
			case SETTINGS_MEMBER_EDIT:
			case MEMBER_INFO:
		}
		return MenuType.SINGULAR.build().join();
	}

	public Menu get(Clan clan) {
		switch (this) {
			case SETTINGS_CLAN_EDIT:
			case MEMBER_LIST:
		}
		return MenuType.SINGULAR.build().join();
	}

	public Menu.Rows getSize() {
		return Menu.Rows.valueOf(ClansAPI.getData().getPath("pagination-size"));
	}

	protected List<String> color(String... text) {
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

	public ItemStack getRightItem() {
		ItemStack right = ClansAPI.getData().getItem("navigate_right");
		ItemMeta meta = right.getItemMeta();
		meta.setDisplayName(StringUtils.use(ClansAPI.getData().getNavigate("right")).translate());
		right.setItemMeta(meta);
		return right;
	}

	public ItemStack getLeftItem() {
		ItemStack left = ClansAPI.getData().getItem("navigate_left");
		ItemMeta meta = left.getItemMeta();
		meta.setDisplayName(StringUtils.use(ClansAPI.getData().getNavigate("left")).translate());
		left.setItemMeta(meta);
		return left;
	}

	public ItemStack getBackItem() {
		ItemStack back = ClansAPI.getData().getItem("back");
		ItemMeta meta = back.getItemMeta();
		meta.setDisplayName(StringUtils.use(ClansAPI.getData().getNavigate("back")).translate());
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
