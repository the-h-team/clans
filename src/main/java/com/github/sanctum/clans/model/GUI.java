package com.github.sanctum.clans.model;

import com.github.sanctum.clans.impl.DefaultDocketRegistry;
import com.github.sanctum.clans.util.ReloadUtility;
import com.github.sanctum.clans.util.SimpleLogoCarrier;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.reload.PrintManager;
import com.github.sanctum.labyrinth.data.service.Constant;
import com.github.sanctum.labyrinth.formatting.string.FormattedString;
import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import com.github.sanctum.labyrinth.gui.unity.construct.PaginatedMenu;
import com.github.sanctum.labyrinth.gui.unity.construct.SingularMenu;
import com.github.sanctum.labyrinth.gui.unity.impl.BorderElement;
import com.github.sanctum.labyrinth.gui.unity.impl.FillerElement;
import com.github.sanctum.labyrinth.gui.unity.impl.InventoryElement;
import com.github.sanctum.labyrinth.gui.unity.impl.ItemElement;
import com.github.sanctum.labyrinth.gui.unity.impl.ListElement;
import com.github.sanctum.labyrinth.gui.unity.impl.MenuType;
import com.github.sanctum.labyrinth.gui.unity.simple.Docket;
import com.github.sanctum.labyrinth.gui.unity.simple.MemoryDocket;
import com.github.sanctum.labyrinth.interfacing.UnknownGeneric;
import com.github.sanctum.labyrinth.library.Entities;
import com.github.sanctum.labyrinth.library.Item;
import com.github.sanctum.labyrinth.library.Items;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.container.PantherSet;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.util.HUID;
import com.github.sanctum.panther.util.RandomObject;
import com.github.sanctum.skulls.CustomHead;
import com.github.sanctum.skulls.CustomHeadLoader;
import com.github.sanctum.skulls.SkullType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum GUI {

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
	ADDONS_SELECTION,
	/**
	 * Modify arena team spawning locations.
	 */
	ARENA_SPAWN,
	/**
	 * Call a vote to surrender the current war.
	 */
	ARENA_SURRENDER,
	/**
	 * Call a vote to truce the current war.
	 */
	ARENA_TRUCE,
	/**
	 * Access clan bank options. (WIP)
	 */
	BANK,
	/**
	 * A clan's list of claims
	 */
	CLAIM_LIST,
	/**
	 * A clan's titles
	 */
	CLAIM_TITLES,
	/**
	 * Entire clan roster.
	 */
	CLAN_ROSTER,
	/**
	 * Roster category selection
	 */
	CLAN_ROSTER_SELECTION,
	/**
	 * Most power clans on the server in ordered by rank.
	 */
	CLAN_ROSTER_TOP,
	/**
	 * A clan's list of logo carriers
	 */
	HOLOGRAM_LIST,
	/**
	 * List all known heads on the server
	 */
	HEAD_LIBRARY,
	/**
	 * The main menu for head related actions.
	 */
	HEAD_MAIN_MENU,
	/**
	 * The public logo market
	 */
	LOGO_LIST,
	/**
	 * The main menu.
	 */
	MAIN_MENU,
	/**
	 * Edit a clan member
	 */
	MEMBER_EDIT,
	/**
	 * View a clan member's info.
	 */
	MEMBER_INFO,
	/**
	 * View a clan's member list.
	 */
	MEMBER_LIST,
	/**
	 * View a clan's relation list.
	 */
	RELATIONS_MENU,
	/**
	 * View a clans reservoir.
	 */
	RESERVOIR,
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
	 * Modify game rule settings.
	 */
	SETTINGS_GAME_RULE,
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

		private final ClansAPI api = ClansAPI.getInstance();
	private final ClanAddonRegistry addonQueue = ClanAddonRegistry.getInstance();
	private final PrintManager printManager = LabyrinthProvider.getInstance().getLocalPrintManager();
	private final ItemStack special = CustomHeadLoader.provide("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWFjMTVmNmZjZjJjZTk2M2VmNGNhNzFmMWE4Njg1YWRiOTdlYjc2OWUxZDExMTk0Y2JiZDJlOTY0YTg4OTc4YyJ9fX0=");

	public Menu get(Player p) {
		if (this == GUI.MAIN_MENU) {
			MemoryDocket<UnknownGeneric> docket = new MemoryDocket<>(ClansAPI.getDataInstance().getMessages().getRoot().getNode("menu.home"));
			docket.setNamePlaceholder("%player_name%");
			docket.setUniqueDataConverter(p, (s, player) -> new FormattedString(s).translate(player).get());
			docket.load();
			return docket.toMenu();
		}
		if (this == GUI.HEAD_MAIN_MENU) {
			MemoryDocket<UnknownGeneric> docket = new MemoryDocket<>(ClansAPI.getDataInstance().getMessages().getRoot().getNode("menu.head-home"));
			docket.setNamePlaceholder("%player_name%");
			docket.setUniqueDataConverter(p, (s, player) -> new FormattedString(s).translate(player).get());
			docket.load();
			return docket.toMenu();
		}
		return MenuType.SINGULAR.build().join();
	}

	public Menu get() {
		switch (this) {
			case SETTINGS_GAME_RULE:
				return MenuType.PAGINATED.build()
						.setHost(api.getPlugin())
						.setTitle("&3Session Game Rules &0&l»")
						.setSize(getSize())
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (LabyrinthProvider.getInstance().isLegacy()) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							i.addItem(border);
							ListElement<String> rules = new ListElement<>(Constant.values(AbstractGameRule.class, String.class));
							rules.setLimit(getLimit());
							rules.setPopulate((flag, element) -> {
								Material mat = new RandomObject<>(Arrays.stream(Material.values()).filter(material -> !material.name().contains("LEGACY")).collect(Collectors.toList())).get(flag);
								if (mat.isAir() || !mat.isItem()) mat = Material.DIAMOND;
								Material finalMat = mat;
								element.setElement(edit -> edit.setType(finalMat).setTitle("&6Edit &r" + flag).setLore(" ", "&eLeft-click to &aadd&e stuff", "&eRight-click to &cremove&e stuff", "&eShift-click to &3&loverwrite&e stuff").build()).setClick(c -> {
									c.setCancelled(true);
									AbstractGameRule rule = AbstractGameRule.of(printManager.getPrint(api.getLocalPrintKey()));
									if (c.getClickType().isShiftClick()) {
										InventoryElement inventory = rule.edit(AbstractGameRule.Modification.SET, flag);
										if (inventory != null) {
											inventory.open(c.getElement());
										}

									} else if (c.getClickType().isLeftClick()) {
										InventoryElement inventory = rule.edit(AbstractGameRule.Modification.ADD, flag);
										if (inventory != null) {
											inventory.open(c.getElement());
										}

									} else if (c.getClickType().isRightClick()) {
										InventoryElement inventory = rule.edit(AbstractGameRule.Modification.REMOVE, flag);
										if (inventory != null) {
											inventory.open(c.getElement());
										}

									}
								});
							});
							i.addItem(rules);
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
						})
						.join();
			case HEAD_LIBRARY:
				String id = ClansAPI.getDataInstance().getMessageString("menu.head-library.id");
				MemoryDocket<?> docket = DefaultDocketRegistry.get(id);
				if (docket != null) return docket.toMenu();
			case LOGO_LIST:
				return MenuType.PAGINATED.build()
						.setHost(api.getPlugin())
						.setKey("Clans:logo-list")
						.setSize(getSize())
						.setTitle("&e&lLogo Gallery &0&l»")
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (LabyrinthProvider.getInstance().isLegacy()) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
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
								click.getElement().closeInventory();
							}));
							ListElement<SimpleLogoCarrier> list = new ListElement<>(new ArrayList<>(api.getLogoGallery().getLogos()));
							list.setLimit(getLimit()).setComparator(Comparator.comparingInt(value -> value.getData().get().getLines().size()));
							list.setPopulate((stand, item) -> {
								List<String> set = Arrays.asList(stand.toRaw());
								item.setElement(ed -> ed.setTitle("&e# &f(" + stand.getId() + ")").setLore(set).build());
								item.setClick(click -> {
									click.setCancelled(true);
									if (click.getElement().hasPermission("clans.admin")) {
										if (click.getClickType().isShiftClick()) {
											api.getLogoGallery().remove(set);
											TaskScheduler.of(() -> GUI.LOGO_LIST.get().open(click.getElement())).scheduleLater(1);
											return;
										}
									}
									if (click.getClickType().isLeftClick()) {
										api.getAssociate(click.getElement()).ifPresent(a -> {
											if (Clearance.LOGO_UPLOAD.test(a) && Clearance.LOGO_APPLY.test(a)) {
												a.getClan().setValue("logo", set, false);
												Clan.ACTION.sendMessage(click.getElement(), "&aClan insignia successfully updated.");
											}
										});
									}
								});
							});
							i.addItem(list);
						}).orGet(m -> m instanceof PaginatedMenu && m.getKey().map(("Clans:logo-list")::equals).orElse(false));

			case CLAN_ROSTER:
				String rosterId = ClansAPI.getDataInstance().getMessageString("menu.roster.id");
				MemoryDocket<?> rosterDocket = DefaultDocketRegistry.get(rosterId);
				if (rosterDocket != null) return rosterDocket.toMenu();
			case SETTINGS_CLAN_ROSTER:
				return MenuType.PAGINATED.build().setHost(api.getPlugin())
						.setProperty(Menu.Property.CACHEABLE, Menu.Property.RECURSIVE)
						.setTitle("&0&l» &3&lSelect a clan")
						.setSize(getSize())
						.setKey("Clans:Roster_edit")
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (LabyrinthProvider.getInstance().isLegacy()) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
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
									String title = "&6Clan: &r" + (c.getNickname() != null ? c.getPalette().toString(c.getNickname()) : c.getPalette().toString(c.getName()));
									ItemStack it;
									if (c.isConsole()) {
										it = new ItemStack(special);
										title = title + "&b*";
									} else {
										it = Optional.ofNullable(CustomHead.Manager.get("Clan")).orElse(new ItemStack(Material.PAPER));
									}
									b.setItem(it);
									b.setTitle(title);
									if (c.getLogo() != null) {
										PantherSet<String> logo = new PantherSet<>();
										logo.add("&bTag: " + c.getPalette() + c.getName());
										logo.add("&bMembers: " + c.getPalette() + c.getMembers().size());
										logo.add("&bDescription: " + c.getPalette() + c.getDescription());
										logo.addAll(c.getLogo());
										b.setLore(logo.toArray(new String[0]));
									} else {
										b.setLore("&bTag: " + c.getPalette() + (c.getNickname() != null ? c.getNickname() : c.getName()), "&bMembers: " + c.getPalette() + c.getMembers().size(), "&bDescription: " + c.getPalette() + c.getDescription());
									}
									return b.build();
								}).setClick(click -> {
									click.setCancelled(true);
									click.setHotbarAllowed(false);
									SETTINGS_CLAN.get(c).open(click.getElement());
								});
							}).setLimit(getLimit()));

						}).orGet(m -> m instanceof PaginatedMenu && m.getKey().isPresent() && m.getKey().get().equals("Clans:Roster_edit"));
			case CLAN_ROSTER_TOP:
				String rosterTopId = ClansAPI.getDataInstance().getMessageString("menu.roster-top.id");
				MemoryDocket<?> docket2 = DefaultDocketRegistry.get(rosterTopId);
				if (docket2 != null) return docket2.toMenu();
			case CLAN_ROSTER_SELECTION:
				String selectID = ClansAPI.getDataInstance().getMessageString("menu.roster-select.id");
				MemoryDocket<?> docket3 = DefaultDocketRegistry.get(selectID);
				if (docket3 != null) return docket3.toMenu();
			case SETTINGS_SELECT:
				return MenuType.SINGULAR.build()
						.setHost(api.getPlugin())
						.setProperty(Menu.Property.CACHEABLE)
						.setKey("Clans:Settings")
						.setSize(Menu.Rows.SIX)
						.setTitle(" &0&l» &2&oManagement Area")
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (LabyrinthProvider.getInstance().isLegacy()) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							i.addItem(border);
							i.addItem(b -> b.setElement(new ItemStack(Items.findMaterial("NAUTILUS_SHELL") != null ? Items.findMaterial("NAUTILUS_SHELL") : Items.findMaterial("NETHERSTAR"))).setSlot(33).setElement(ed -> ed.setLore("&bClick to manage all &dclans addons").setTitle("&7[&5Addon Management&7]").build()).setClick(click -> {
								click.setCancelled(true);
								ADDONS_SELECTION.get().open(click.getElement());
							}));
							i.addItem(b -> b.setElement(new ItemStack(Items.findMaterial("CLOCK") != null ? Items.findMaterial("CLOCK") : Items.findMaterial("WATCH"))).setElement(ed -> ed.setTitle("&7[&2Shield Edit&7]").setLore("&bClick to edit the &3raid-shield").build()).setSlot(29).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_SHIELD.get().open(c.getElement());
							}));
							i.addItem(b -> b.setElement(new ItemStack(new ItemStack(Material.ENCHANTED_BOOK))).setElement(ed -> ed.setTitle("&7[&cAll Spy&7]").setLore("&7Click to toggle spy ability on all clan chat channels.").build()).setSlot(12).setClick(c -> {
								Player p = c.getElement();
								c.setCancelled(true);
								Bukkit.dispatchCommand(p, "cla spy");
							}));
							i.addItem(b -> b.setElement(new ItemStack(Items.findMaterial("HEARTOFTHESEA") != null ? Items.findMaterial("HEARTOFTHESEA") : Items.findMaterial("SLIMEBALL"))).setElement(ed -> ed.setTitle("&7[&eGame Rules&7]").setLore("&7Click to manage this sessions game rules.").build()).setSlot(40).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_GAME_RULE.get().open(c.getElement());
							}));
							i.addItem(b -> b.setElement(new ItemStack(Material.ANVIL)).setElement(ed -> ed.setTitle("&7[&eClan Edit&7]").setLore("&7Click to manage clans.").build()).setSlot(10).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_CLAN_ROSTER.get().open(c.getElement());
							}));
							i.addItem(b -> b.setElement(new ItemStack(Material.DIAMOND_SWORD)).setElement(ed -> ed.setTitle("&7[&2War&7]").setLore("&7Click to manage arena spawns.").build()).setSlot(16).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_ARENA.get().open(c.getElement());
							}));
							i.addItem(b -> b.setElement(Optional.ofNullable(CustomHead.Manager.get("Clan")).orElse(new ItemStack(Material.PAPER))).setElement(ed -> ed.setTitle("&7[&eClan List&7]").setLore("&bClick to view the entire &6clan roster").build()).setSlot(14).setClick(c -> {
								c.setCancelled(true);
								CLAN_ROSTER_SELECTION.get().open(c.getElement());
							}));
							i.addItem(b -> b.setElement(getBackItem()).setElement(ed -> ed.setTitle("&7[&4Close&7]").setLore("&cClick to close the gui.").build()).setSlot(49).setClick(c -> {
								c.setCancelled(true);
								c.getElement().closeInventory();
							}));
							i.addItem(b -> b.setElement(new ItemStack(Items.findMaterial("HEARTOFTHESEA") != null ? Items.findMaterial("HEARTOFTHESEA") : Items.findMaterial("SLIMEBALL"))).setElement(ed -> ed.setTitle("&7[&cReload&7]").setLore("&eClick to reload data.").build()).setSlot(31).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_RELOAD.get().open(c.getElement());
							}));
						}).orGet(m -> m instanceof SingularMenu && m.getKey().map("Clans:Settings"::equals).orElse(false));
			case ADDONS_SELECTION:
				return MenuType.SINGULAR.build().setHost(api.getPlugin())
						.setSize(Menu.Rows.ONE)
						.setTitle("&2&oManage Addon Cycles &0&l»")
						.setKey("Clans:Addons")
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
						.orGet(m -> m instanceof SingularMenu && m.getKey().map("Clans:Addons"::equals).orElse(false));

			case SETTINGS_ARENA:
				return MenuType.SINGULAR.build().setHost(api.getPlugin())
						.setSize(Menu.Rows.ONE)
						.setTitle("&2&oArena Spawns &0&l»")
						.setKey("Clans:War")
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
						.orGet(m -> m instanceof SingularMenu && m.getKey().map("Clans:War"::equals).orElse(false));
			case SETTINGS_RELOAD:
				return MenuType.SINGULAR.build().setHost(api.getPlugin())
						.setSize(Menu.Rows.ONE)
						.setTitle("&0&l» &eReload Files")
						.setKey("Clans:Reload")
						.setProperty(Menu.Property.CACHEABLE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.POTION).setTitle("&aConfig.yml").build()).setSlot(0).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_SELECT.get().open(c.getElement());
								api.getFileList().get("Config", "Configuration").getRoot().reload();
								Mailer.empty(c.getElement()).prefix().start(api.getPrefix().toString()).finish().chat("&aConfig file 'Config' reloaded.").deploy();
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.POTION).setTitle("&5Messages.yml").build()).setSlot(2).setClick(c -> {
								c.setCancelled(true);
								api.getFileList().get("Messages", "Configuration").getRoot().reload();
								Mailer.empty(c.getElement()).prefix().start(api.getPrefix().toString()).finish().chat("&aConfig file 'Messages' reloaded.").deploy();
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.POTION).setTitle("&2&lAll").build()).setSlot(4).setClick(c -> {
								c.setCancelled(true);
								Player p = c.getElement();
								p.performCommand("clanadmin reload");
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
						.orGet(m -> m instanceof SingularMenu && m.getKey().map("Clans:Reload"::equals).orElse(false));
			case SETTINGS_SHIELD:
				return MenuType.SINGULAR.build().setHost(api.getPlugin())
						.setSize(Menu.Rows.ONE)
						.setTitle("&2&oRaid-Shield Settings &0&l»")
						.setKey("Clans:shield-edit")
						.setProperty(Menu.Property.CACHEABLE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(b -> b.setElement(ed -> ed.setType(Items.findMaterial("CLOCK") != null ? Items.findMaterial("CLOCK") : Items.findMaterial("WATCH")).setTitle("&a&oUp: Mid-day").setLore("&bClick to change the raid-shield to enable mid-day").build()).setSlot(4).setClick(c -> {
								c.setCancelled(true);
								api.getShieldManager().getTamper().setUpOverride(6000);
								api.getShieldManager().getTamper().setDownOverride(18000);
								Clan.ACTION.sendMessage(c.getElement(), "&aRaid-shield engagement changed to mid-day.");
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Items.findMaterial("CLOCK") != null ? Items.findMaterial("CLOCK") : Items.findMaterial("WATCH")).setTitle("&a&oUp: Sunrise").setLore("&bClick to change the raid-shield to enable on sunrise").build()).setSlot(3).setClick(c -> {
								c.setCancelled(true);
								api.getShieldManager().getTamper().setUpOverride(0);
								api.getShieldManager().getTamper().setDownOverride(13000);
								Clan.ACTION.sendMessage(c.getElement(), "&aRaid-shield engagement changed to sunrise.");
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Items.findMaterial("CLOCK") != null ? Items.findMaterial("CLOCK") : Items.findMaterial("WATCH")).setTitle("&a&oPermanent protection.").setLore("&bClick to freeze the raid-shield @ its current status").build()).setSlot(5).setClick(c -> {
								c.setCancelled(true);
								Player p = c.getElement();
								if (api.getShieldManager().getTamper().isOff()) {
									p.closeInventory();
									Clan.ACTION.sendMessage(p, "&aRaid-shield block has been lifted.");
									api.getShieldManager().getTamper().setIsOff(false);
								} else {
									Clan.ACTION.sendMessage(p, "&cRaid-shield has been blocked.");
									api.getShieldManager().getTamper().setIsOff(true);
								}
							}));
							i.addItem(b -> b.setElement(ed -> ed.setItem(getBackItem()).build()).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_SELECT.get().open(c.getElement());
							}).setSlot(8));
						})
						.orGet(m -> m instanceof SingularMenu && m.getKey().map("Clans:shield-edit"::equals).orElse(false));

			case SETTINGS_LANGUAGE:
				return MenuType.SINGULAR.build().setHost(api.getPlugin())
						.setSize(Menu.Rows.ONE)
						.setTitle("&0&l» &ePick a language")
						.setKey("Clans:Lang")
						.setProperty(Menu.Property.CACHEABLE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.BOOK).setTitle("&aEnglish").build()).setSlot(0).setClick(c -> {
								c.setCancelled(true);
								c.getElement().closeInventory();
								ReloadUtility.reload(ReloadUtility.Lang.EN_US, c.getElement());
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.BOOK).setTitle("&bPortuguese").build()).setSlot(2).setClick(c -> {
								c.setCancelled(true);
								c.getElement().closeInventory();
								ReloadUtility.reload(ReloadUtility.Lang.PT_BR, c.getElement());
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Material.BOOK).setTitle("&eSpanish").build()).setSlot(4).setClick(c -> {
								c.setCancelled(true);
								c.getElement().closeInventory();
								ReloadUtility.reload(ReloadUtility.Lang.ES_ES, c.getElement());
							}));
							i.addItem(b -> b.setElement(ed -> ed.setItem(getBackItem()).build()).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_RELOAD.get().open(c.getElement());
							}).setSlot(8));
						})
						.orGet(m -> m instanceof SingularMenu && m.getKey().map("Clans:Lang"::equals).orElse(false));

			case ADDONS_ACTIVATED:
				return MenuType.PAGINATED.build()
						.setHost(api.getPlugin())
						.setProperty(Menu.Property.CACHEABLE, Menu.Property.RECURSIVE)
						.setSize(Menu.Rows.SIX)
						.setTitle("&3&oRegistered Cycles &f(&2RUNNING&f) &8&l»")
						.setStock(i -> {
							ListElement<Clan.Addon> list = new ListElement<>(addonQueue.getEnabled().stream().map(addonQueue::get).collect(Collectors.toList()));
							list.setLimit(28);
							list.setPopulate((addon, item) -> {
								ItemStack stack = new ItemStack(Material.CHEST);

								ItemMeta meta = stack.getItemMeta();

								meta.setLore(color("&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oPersistent: &f" + addon.isPersistent(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oDescription: &f" + addon.getDescription(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oVersion: &f" + addon.getVersion(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oAuthors: &f" + Arrays.toString(addon.getAuthors()), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oActive: &6&o" + addonQueue.getEnabled().contains(addon.getName())));

								meta.setDisplayName(StringUtils.use("&3&o " + addon.getName() + " &8&l»").translate());

								stack.setItemMeta(meta);
								item.setElement(stack);
								item.setClick(click -> {
									click.setCancelled(true);
									Player p = click.getElement();
									ClanAddonLoadResult result = addonQueue.disable(addon);
									if (result.get()) {
										for (String d : result.read()) {
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
									ADDONS_SELECTION.get().open(click.getElement());
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
						.setHost(api.getPlugin())
						.setProperty(Menu.Property.CACHEABLE, Menu.Property.RECURSIVE)
						.setSize(Menu.Rows.SIX)
						.setTitle("&3&oRegistered Cycles &f(&4DISABLED&f) &8&l»")
						.setStock(i -> {
							ListElement<Clan.Addon> list = new ListElement<>(addonQueue.getDisabled().stream().map(addonQueue::get).collect(Collectors.toList()));
							list.setLimit(28);
							list.setPopulate((addon, item) -> {
								ItemStack stack = new ItemStack(Material.CHEST);

								ItemMeta meta = stack.getItemMeta();

								meta.setLore(color("&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oPersistent: &f" + addon.isPersistent(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oDescription: &f" + addon.getDescription(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oVersion: &f" + addon.getVersion(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oAuthors: &f" + Arrays.toString(addon.getAuthors()), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oActive: &6&o" + addonQueue.getEnabled().contains(addon.getName())));

								meta.setDisplayName(StringUtils.use("&3&o " + addon.getName() + " &8&l»").translate());

								stack.setItemMeta(meta);
								item.setElement(stack);
								item.setClick(click -> {
									click.setCancelled(true);
									Player p = click.getElement();
									ClanAddonLoadResult result = addonQueue.enable(addon);
									if (result.get()) {
										for (String d : result.read()) {
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
									ADDONS_SELECTION.get().open(click.getElement());
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
						.setHost(api.getPlugin())
						.setProperty(Menu.Property.CACHEABLE, Menu.Property.RECURSIVE)
						.setSize(Menu.Rows.SIX)
						.setTitle("&3&oRegistered Cycles &f(&6&lCACHE&f) &8&l»")
						.setStock(i -> {
							ListElement<Clan.Addon> list = new ListElement<>(new ArrayList<>(addonQueue.get()));
							list.setLimit(28);
							list.setPopulate((addon, item) -> {
								ItemStack stack = new ItemStack(Material.CHEST);

								ItemMeta meta = stack.getItemMeta();

								meta.setLore(color("&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oPersistent: &f" + addon.isPersistent(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oDescription: &f" + addon.getDescription(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oVersion: &f" + addon.getVersion(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oAuthors: &f" + Arrays.toString(addon.getAuthors()), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oActive: &6&o" + addonQueue.getEnabled().contains(addon.getName()), "&7Clicking these icons won't do anything."));

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
									ADDONS_SELECTION.get().open(click.getElement());
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

	void addBackground(InventoryElement i) {
		FillerElement<?> filler = new FillerElement<>(i);
		filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
		i.addItem(filler);
		BorderElement<?> border = new BorderElement<>(i);
		for (Menu.Panel p : Menu.Panel.values()) {
			if (p == Menu.Panel.MIDDLE) continue;
			if (LabyrinthProvider.getInstance().isLegacy()) {
				border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
			} else {
				border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
			}
		}
		i.addItem(border);
	}

	public Menu get(Clan.Associate associate) {
		Clan cl = associate.getClan();
		String o = cl.getPalette().toString();
		String balance;
		try {
			if (associate.isEntity()) {
				balance = "3.50";
			} else {
				balance = Clan.ACTION.format(EconomyProvision.getInstance().balance(associate.getTag().getPlayer()).orElse(0.0));
			}
		} catch (NoClassDefFoundError | NullPointerException e) {
			balance = "Un-Known";
		}
		String stats;
		String rank = associate.getRankFull();
		/*
		ZonedDateTime time = associate.getJoinDate().toInstant().atZone(ZoneId.systemDefault());
		Calendar cal = Calendar.getInstance();
		cal.setTime(associate.getJoinDate());
		String temporal = cal.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm";
		 */
		String date = associate.getJoinDate().toLocaleString();//time.getMonthValue() + "/" + time.getDayOfMonth() + "/" + time.getYear() + " @ " + time.getHour() + ":" + time.getMinute() + temporal;
		String bio = associate.getBiography();
		String kd = "" + associate.getKD();
		if (Bukkit.getVersion().contains("1.15") || LabyrinthProvider.getInstance().isNew()) {
			if (associate.isEntity()) {
				stats = "|&fStatistic's unattainable.";
			} else {
				OfflinePlayer p = associate.getTag().getPlayer();
				stats = o + "Banners washed: &f" + p.getStatistic(Statistic.BANNER_CLEANED) + "|" +
						o + "Bell's rang: &f" + p.getStatistic(Statistic.BELL_RING) + "|" +
						o + "Chest's opened: &f" + p.getStatistic(Statistic.CHEST_OPENED) + "|" +
						o + "Creeper death's: &f" + p.getStatistic(Statistic.ENTITY_KILLED_BY, Entities.getEntity("Creeper")) + "|" +
						o + "Beat's dropped: &f" + p.getStatistic(Statistic.RECORD_PLAYED) + "|" +
						o + "Animal's bred: &f" + p.getStatistic(Statistic.ANIMALS_BRED);
			}
		} else {
			stats = "&c&oVersion under &61.15 |" +
					"&fOffline stat's unattainable.";
		}

		String[] statist = Clan.ACTION.color(stats).split("\\|");
		switch (this) {
			case RELATIONS_MENU:
				return MenuType.SINGULAR.build().setHost(api.getPlugin())
						.setTitle("&d&oRelations &0&l»")
						.setSize(Menu.Rows.THREE)
						.setProperty(Menu.Property.CACHEABLE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (LabyrinthProvider.getInstance().isLegacy()) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							i.addItem(border);

							// 9, 17

							i.addItem(builder -> builder.setElement(edit -> edit.setType(Material.BOOK).setTitle("&aAllies").setFlags(ItemFlag.HIDE_ENCHANTS).addEnchantment(Enchantment.MENDING, 1).build()).setSlot(9).setClick(click -> {

								click.setCancelled(true);
								MenuType.PAGINATED.build()
										.setTitle("&6&lAllies {0}/{1}")
										.setSize(Menu.Rows.THREE)
										.setHost(api.getPlugin())
										.setStock(paginated -> {
											addBackground(paginated);
											ListElement<Clan> list = new ListElement<>(associate.getClan().getRelation().getAlliance().get(Clan.class)).setLimit(7).setPopulate((c, element) -> {
												element.setElement(edit -> edit.setTitle(c.getPalette() + (c.getNickname() != null ? c.getNickname() : c.getName())).setItem(Optional.ofNullable(CustomHead.Manager.get("Clan")).orElse(new ItemStack(Material.PAPER))).build());
												element.setClick(clickElement -> {
													clickElement.setCancelled(true);
													clickElement.setHotbarAllowed(false);

													clickElement.getElement().closeInventory();
													clickElement.getElement().performCommand("c info " + c.getName());

												});
											});
											paginated.addItem(list).addItem(b -> b.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle("&5Next").build()).setType(ItemElement.ControlType.BUTTON_NEXT).setSlot(5))
													.addItem(b -> b.setElement(it -> it.setItem(SkullType.ARROW_BLUE_LEFT.get()).setTitle("&5Previous").build()).setType(ItemElement.ControlType.BUTTON_BACK).setSlot(3))
													.addItem(b -> b.setElement(getBackItem()).setSlot(4).setClick(clickElement -> {
														Player p = clickElement.getElement();
														clickElement.setCancelled(true);
														api.getAssociate(p.getName()).ifPresent(a -> RELATIONS_MENU.get(a).open(p));
													}));
										})
										.join().open(click.getElement());

							}));

							i.addItem(builder -> builder.setElement(edit -> edit.setType(Material.BOOK).setTitle("&cEnemies").setFlags(ItemFlag.HIDE_ENCHANTS).addEnchantment(Enchantment.MENDING, 1).build()).setSlot(17).setClick(click -> {
								click.setCancelled(true);
								MenuType.PAGINATED.build()
										.setTitle("&6&lEnemies {0}/{1}")
										.setSize(Menu.Rows.THREE)
										.setHost(api.getPlugin())
										.setStock(paginated -> {
											addBackground(paginated);
											ListElement<Clan> list = new ListElement<>(associate.getClan().getRelation().getRivalry().get(Clan.class)).setLimit(7).setPopulate((c, element) -> {
												element.setElement(edit -> edit.setTitle(c.getPalette() + (c.getNickname() != null ? c.getNickname() : c.getName())).setItem(Optional.ofNullable(CustomHead.Manager.get("Clan")).orElse(new ItemStack(Material.PAPER))).build());
												element.setClick(clickElement -> {
													clickElement.setCancelled(true);
													clickElement.setHotbarAllowed(false);

													clickElement.getElement().closeInventory();
													clickElement.getElement().performCommand("c info " + c.getName());

												});
											});
											paginated.addItem(list).addItem(b -> b.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle("&5Next").build()).setType(ItemElement.ControlType.BUTTON_NEXT).setSlot(5))
													.addItem(b -> b.setElement(it -> it.setItem(SkullType.ARROW_BLUE_LEFT.get()).setTitle("&5Previous").build()).setType(ItemElement.ControlType.BUTTON_BACK).setSlot(3))
													.addItem(b -> b.setElement(getBackItem()).setSlot(4).setClick(clickElement -> {
														Player p = clickElement.getElement();
														clickElement.setCancelled(true);
														api.getAssociate(p.getName()).ifPresent(a -> RELATIONS_MENU.get(a).open(p));
													}));
										})
										.join().open(click.getElement());
							}));

							i.addItem(builder -> builder.setElement(edit -> edit.setType(Material.ANVIL).setTitle("&aAlly +").build()).setSlot(11).setClick(click -> {
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&a&lAlly +")
										.setSize(Menu.Rows.ONE)
										.setHost(api.getPlugin())
										.setStock(printable -> printable.addItem(b -> b.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												String name = c.getParent().getName().trim();
												c.getElement().performCommand("c ally " + name);
												c.getElement().closeInventory();
											}

										}).open(click.getElement());
							}));
							i.addItem(builder -> builder.setElement(edit -> edit.setType(Material.ANVIL).setTitle("&cAlly -").build()).setSlot(12).setClick(click -> {
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&c&lAlly -")
										.setSize(Menu.Rows.ONE)
										.setHost(api.getPlugin())
										.setStock(printable -> printable.addItem(b -> b.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												String name = c.getParent().getName().trim();
												c.getElement().performCommand("c ally remove " + name);
												c.getElement().closeInventory();
											}

										}).open(click.getElement());
							}));
							i.addItem(builder -> builder.setElement(edit -> edit.setType(Material.ANVIL).setTitle("&cEnemy +").build()).setSlot(14).setClick(click -> {
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&c&lEnemy +")
										.setSize(Menu.Rows.ONE)
										.setHost(api.getPlugin())
										.setStock(printable -> printable.addItem(b -> b.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												String name = c.getParent().getName().trim();
												c.getElement().performCommand("c enemy " + name);
												c.getElement().closeInventory();
											}

										}).open(click.getElement());
							}));
							i.addItem(builder -> builder.setElement(edit -> edit.setType(Material.ANVIL).setTitle("&eEnemy -").build()).setSlot(15).setClick(click -> {
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&e&lEnemy -")
										.setSize(Menu.Rows.ONE)
										.setHost(api.getPlugin())
										.setStock(printable -> printable.addItem(b -> b.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												String name = c.getParent().getName().trim();
												c.getElement().performCommand("c enemy remove " + name);
												c.getElement().closeInventory();
											}

										}).open(click.getElement());
							}));

							i.addItem(b -> b.setElement(getBackItem()).setSlot(13).setClick(clickElement -> {
								Player p = clickElement.getElement();
								clickElement.setCancelled(true);
								MAIN_MENU.get(p).open(p);
							}));

						})
						.join();
			case MEMBER_INFO:
				MemoryDocket<Clan.Associate> docket = new MemoryDocket<>(ClansAPI.getDataInstance().getMessages().getRoot().getNode("menu.member"));
				docket.setUniqueDataConverter(associate, Clan.Associate.memoryDocketReplacer());
				docket.setNamePlaceholder(":member_name:");
				return Docket.toMenu(docket);
			case MEMBER_EDIT:
				return MenuType.SINGULAR.build().setHost(api.getPlugin())
						.setProperty(Menu.Property.CACHEABLE)
						.setTitle("&0&l» " + cl.getPalette() + associate.getName() + " settings")
						.setKey("Clans:member-" + associate.getName() + "-edit")
						.setSize(Menu.Rows.THREE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (LabyrinthProvider.getInstance().isLegacy()) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							i.addItem(border);
							i.addItem(b -> b.setElement(it -> it.setItem(associate.getHead()).setFlags(ItemFlag.HIDE_ENCHANTS).addEnchantment(Enchantment.ARROW_DAMAGE, 100)
									.setTitle("&cGo back.").build()).setSlot(13).setClick(click -> {
								click.setHotbarAllowed(false);
								click.setCancelled(true);
								Player p = click.getElement();
								MEMBER_LIST.get(associate.getClan()).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.ENCHANTED_BOOK).setTitle("&7[&6&lName Change&7]").setLore("&5Click to change my name.").build()).setSlot(22).setClick(click -> {
								Player p = click.getElement();
								api.getAssociate(p).ifPresent(a -> {
									if (Clearance.MANAGE_NICKNAMES.test(a)) {
										MenuType.PRINTABLE.build()
												.setTitle("&2Type a name")
												.setSize(Menu.Rows.ONE)
												.setHost(api.getPlugin())
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
							i.addItem(b -> b.setElement(it -> it.setType(Material.ENCHANTED_BOOK).setTitle("&7[&6&lBio Change&7]").setLore("&5Click to change my biography.").build()).setSlot(4).setClick(click -> {
								Player p = click.getElement();
								api.getAssociate(p).ifPresent(a -> {
									if (Clearance.MANAGE_NICKNAMES.test(a)) {
										MenuType.PRINTABLE.build()
												.setTitle("&2Type a bio")
												.setSize(Menu.Rows.ONE)
												.setHost(api.getPlugin())
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
									}
								});
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.IRON_BOOTS).setTitle("&7[&4Kick&7]").setLore("&5Click to kick me.").build()).setSlot(11).setClick(click -> {
								Player p = click.getElement();
								api.getAssociate(p).ifPresent(a -> {
									if (Clearance.KICK_MEMBERS.test(a)) {
										associate.remove();
										TaskScheduler.of(() -> MEMBER_LIST.get(a.getClan()).open(p)).scheduleLater(1);
									}
								});
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.DIAMOND).setTitle("&7[&aPromotion&7]").setLore("&5Click to promote me.").build()).setSlot(14).setClick(click -> {
								Player p = click.getElement();
								api.getAssociate(p).ifPresent(a -> {
									if (Clearance.MANAGE_POSITIONS.test(a)) {
										Clan.ACTION.promote(associate.getId()).deploy();
									}
								});
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.REDSTONE).setTitle("&7[&cDemotion&7]").setLore("&5Click to demote me.").build()).setSlot(15).setClick(click -> {
								Player p = click.getElement();
								api.getAssociate(p).ifPresent(a -> {
									if (Clearance.MANAGE_POSITIONS.test(a)) {
										Clan.ACTION.demote(associate.getId()).deploy();
									}
								});
							}));

						}).orGet(m -> m instanceof SingularMenu && m.getKey().isPresent() && m.getKey().get().equals("Clans:member-" + associate.getName() + "-edit")).addAction(c -> c.setCancelled(true));

			case SETTINGS_MEMBER:
				return MenuType.SINGULAR.build().setHost(api.getPlugin())
						.setProperty(Menu.Property.RECURSIVE, Menu.Property.CACHEABLE)
						.setTitle("&0&l» " + cl.getPalette().toString(associate.getName()) + " settings")
						.setKey("Clans:member-" + associate.getName() + "-edit-settings")
						.setSize(Menu.Rows.THREE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (LabyrinthProvider.getInstance().isLegacy()) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							i.addItem(border);
							i.addItem(b -> b.setElement(it -> it.setItem(associate.getHead() != null ? associate.getHead() : SkullType.PLAYER.get()).setFlags(ItemFlag.HIDE_ENCHANTS).addEnchantment(Enchantment.ARROW_DAMAGE, 100)
									.setTitle("&cGo back.").build()).setSlot(13).setClick(click -> {
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
										.setHost(api.getPlugin())
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
										.setHost(api.getPlugin())
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
										.setHost(api.getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												HUID id = api.getClanManager().getClanID(c.getParent().getName());
												final UUID uid = associate.getId();
												if (id != null) {
													Clan clan = api.getClanManager().getClan(id);
													Clan.ACTION.remove(uid, true).deploy();
													Clan.Associate newAssociate = clan.newAssociate(uid);
													if (newAssociate != null) {
														clan.add(newAssociate);
														newAssociate.save();
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
								associate.remove();
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.DIAMOND).setTitle("&7[&aPromotion&7]").setLore("&5Click to promote me.").build()).setSlot(14).setClick(click -> {
								Player p = click.getElement();
								Clan.ACTION.promote(associate.getId()).deploy();
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.REDSTONE).setTitle("&7[&cDemotion&7]").setLore("&5Click to demote me.").build()).setSlot(15).setClick(click -> {
								Player p = click.getElement();
								Clan.ACTION.demote(associate.getId()).deploy();
							}));

						}).orGet(m -> m instanceof SingularMenu && m.getKey().isPresent() && m.getKey().get().equals("Clans:member-" + associate.getName() + "-edit-settings")).addAction(c -> c.setCancelled(true));

			default:
				throw new IllegalArgumentException("GUI type " + name() + " not valid, contact developers.");
		}

	}

	public Menu get(Clan clan) {
		switch (this) {
			case BANK:
				return MenuType.SINGULAR.build().setHost(api.getPlugin())
						.setTitle("&d&oBank &0&l»")
						.setSize(Menu.Rows.THREE)
						.setProperty(Menu.Property.CACHEABLE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (LabyrinthProvider.getInstance().isLegacy()) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							i.addItem(border);

							// 9, 17

							i.addItem(builder -> builder.setElement(edit -> edit.setType(Material.BOOK).setTitle("&6Balance: &f" + BanksAPI.getInstance().getBank(clan).getBalanceDouble()).setFlags(ItemFlag.HIDE_ENCHANTS).addEnchantment(Enchantment.MENDING, 1).build()).setSlot(9).setType(ItemElement.ControlType.DISPLAY));
							i.addItem(builder -> builder.setElement(edit -> edit.setType(Material.ANVIL).setTitle("&aDeposit +").build()).setSlot(11).setClick(click -> {
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&a&lDeposit +")
										.setSize(Menu.Rows.ONE)
										.setHost(api.getPlugin())
										.setStock(printable -> printable.addItem(b -> b.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												String name = c.getParent().getName().trim();
												if (StringUtils.use(name).isDouble()) {
													c.getElement().performCommand("c bank deposit " + name);
													c.getElement().closeInventory();
												}
											}

										}).open(click.getElement());
							}));
							i.addItem(builder -> builder.setElement(edit -> edit.setType(Material.ANVIL).setTitle("&cWithdraw -").build()).setSlot(12).setClick(click -> {
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&c&lWithdraw -")
										.setSize(Menu.Rows.ONE)
										.setHost(api.getPlugin())
										.setStock(printable -> printable.addItem(b -> b.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												String name = c.getParent().getName().trim();
												if (StringUtils.use(name).isDouble()) {
													c.getElement().performCommand("c bank withdraw " + name);
													c.getElement().closeInventory();
												}
											}

										}).open(click.getElement());
							}));

							i.addItem(b -> b.setElement(getBackItem()).setSlot(13).setClick(clickElement -> {
								Player p = clickElement.getElement();
								clickElement.setCancelled(true);
								MAIN_MENU.get(p).open(p);
							}));

						})
						.join();
			case CLAIM_TITLES:
				return MenuType.SINGULAR.build().setHost(api.getPlugin())
						.setProperty(Menu.Property.RECURSIVE, Menu.Property.CACHEABLE)
						.setTitle("&0&l» " + (clan.getPalette().isGradient() ? clan.getPalette().toGradient().context(clan.getName()).translate() : clan.getPalette().toString() + clan.getName()) + " claim titles")
						.setKey("Clans:titles-" + clan.getName())
						.setSize(Menu.Rows.THREE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (LabyrinthProvider.getInstance().isLegacy()) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
							i.addItem(border);
							i.addItem(b -> b.setElement(it -> it.setItem(clan.getOwner().getHead()).setFlags(ItemFlag.HIDE_ENCHANTS).addEnchantment(Enchantment.ARROW_DAMAGE, 100)
									.setLore("&5Click to go back.").setTitle("&7[&6Back to Clan Edit&7]").build()).setSlot(13).setClick(click -> {
								click.setHotbarAllowed(false);
								click.setCancelled(true);
								Player p = click.getElement();
								SETTINGS_CLAN.get(clan).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.BOOK).setTitle("&7[&bEnter title change&7]").setLore("&5Click to change our enter title.").build()).setSlot(4).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&2Type a title")
										.setSize(Menu.Rows.ONE)
										.setHost(api.getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												clan.setValue("claim_title", c.getParent().getName(), false);
												CLAIM_TITLES.get(clan).open(c.getElement());
											}

										}).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.NAME_TAG).setTitle("&7[&bEnter sub-title change&7]").setLore("&5Click to change our enter sub-title.").build()).setSlot(22).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&2Type a title")
										.setSize(Menu.Rows.ONE)
										.setHost(api.getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												clan.setValue("claim_sub_title", c.getParent().getName(), false);
												CLAIM_TITLES.get(clan).open(c.getElement());
											}

										}).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.WRITTEN_BOOK).setTitle("&7[&bLeave title change&7]").setLore("&5Click to change our leave title.").build()).setSlot(12).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&2Type a title")
										.setSize(Menu.Rows.ONE)
										.setHost(api.getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												clan.setValue("leave_claim_title", c.getParent().getName(), false);
												CLAIM_TITLES.get(clan).open(c.getElement());
											}

										}).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.WRITTEN_BOOK).setTitle("&7[&bLeave sub-title change&7]").setLore("&5Click to change our leave sub-title.").build()).setSlot(14).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&2Type a title")
										.setSize(Menu.Rows.ONE)
										.setHost(api.getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(" ").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												clan.setValue("leave_claim_sub_title", c.getParent().getName(), false);
												CLAIM_TITLES.get(clan).open(c.getElement());
											}

										}).open(p);
							}));
						}).orGet(m -> m instanceof SingularMenu && m.getKey().isPresent() && m.getKey().get().equals("Clans:titles-" + clan.getName())).addAction(c -> c.setCancelled(true));
			case CLAIM_LIST:
				return MenuType.PAGINATED.build()
						.setHost(api.getPlugin())
						.setKey("Clans:" + clan.getName() + "-claims")
						.setSize(getSize())
						.setProperty(Menu.Property.LIVE_META)
						.setTitle("&3&lCLAIMS &0&l»")
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (LabyrinthProvider.getInstance().isLegacy()) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
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
								click.getElement().closeInventory();
							}));
							ListElement<Claim> list = new ListElement<>(Arrays.asList(clan.getClaims()));
							list.setLimit(getLimit());
							list.setPopulate((claim, item) -> {
								item.setElement(ed -> ed.setTitle("&e# &f(" + claim.getId() + ")").setLore("&bCarriers: &f" + clan.getCarriers(claim.getChunk()).size(), "&bActive Residents: &f" + claim.getResidents().size(), "&bActive Flags: &f" + Arrays.stream(claim.getFlags()).filter(Claim.Flag::isEnabled).count()).build());
								item.setClick(click -> {
									click.setCancelled(true);
									api.getAssociate(click.getElement()).ifPresent(a -> {
										Location loc = claim.getLocation();
										loc.setY(claim.getLocation().getWorld().getHighestBlockYAt(claim.getLocation()));
										Clan.ACTION.teleport(click.getElement(), loc).deploy();
									});
								});
							});
							i.addItem(list);
						}).orGet(m -> m instanceof PaginatedMenu && m.getKey().map(("Clans:" + clan.getName() + "-claims")::equals).orElse(false));
			case HOLOGRAM_LIST:
				return MenuType.PAGINATED.build()
						.setHost(api.getPlugin())
						.setSize(getSize())
						.setTitle("&b&lHOLOGRAMS &0&l»")
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (LabyrinthProvider.getInstance().isLegacy()) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
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
								click.getElement().closeInventory();
							}));
							ListElement<LogoHolder.Carrier> list = new ListElement<>(clan.getCarriers());
							list.setLimit(getLimit()).setComparator(Comparator.comparingInt(value -> value.getData().get().getLines().size()));
							list.setPopulate((stand, item) -> {
								List<String> set = stand.getLines().stream().map(line -> line.getStand().getCustomName()).collect(Collectors.toCollection(ArrayList::new));
								Collections.reverse(set);
								item.setElement(ed -> ed.setTitle("&e# &f(" + stand.getId() + ")").setLore(set).build());
								item.setClick(click -> {
									click.setCancelled(true);
									api.getAssociate(click.getElement()).ifPresent(a -> {
										Clan.ACTION.teleport(click.getElement(), stand.getLines().stream().findFirst().get().getStand().getLocation()).deploy();
									});
								});
							});
							i.addItem(list);
						}).orGet(m -> m instanceof PaginatedMenu && m.getKey().map(("Clans:" + clan.getName() + "-holograms")::equals).orElse(false));
			case SETTINGS_CLAN:
				return MenuType.SINGULAR.build().setHost(api.getPlugin())
						.setTitle("&0&l» " + (clan.getPalette().isGradient() ? clan.getPalette().toGradient().context(clan.getName()).translate() : clan.getPalette().toString() + clan.getName()) + " settings")
						.setSize(Menu.Rows.THREE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setType(ItemElement.ControlType.ITEM_FILLER).setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (LabyrinthProvider.getInstance().isLegacy()) {
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
										.setHost(api.getPlugin())
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
										.setHost(api.getPlugin())
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
													clan.giveClaims(amount);
													p.performCommand("c i " + clan.getName());
												} catch (NumberFormatException ignored) {
												}
											}

										}).open(p);
							}));
							String co = clan.isPeaceful() ? "&a" : "&4";
							i.addItem(b -> b.setElement(it -> it.setType(Material.HOPPER).setTitle("&7[" + co + "Mode&7]").setLore("&5Click to toggle our mode..").build()).setSlot(9).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								if (clan.isPeaceful()) {
									Clan.ACTION.sendMessage(p, "&aClan &r" + clan.getName() + " &atoggle to &cWAR");
									clan.setPeaceful(false);
								} else {
									Clan.ACTION.sendMessage(p, "&aClan &r" + clan.getName() + " &atoggle to &f&lPEACE");
									clan.setPeaceful(true);
								}
								GUI.SETTINGS_CLAN.get(api.getClanManager().getClan(clan.getId())).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.BOOK).setTitle("&7[&dClaim Titles&7]").setLore("&5Click to modify our claim titles..").build()).setSlot(0).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								GUI.CLAIM_TITLES.get(clan).open(p);
							}));
							i.addItem(b -> b.setElement(it -> it.setType(Material.SLIME_BALL).setTitle("&7[&a+Power&7]").setLore("&5Click to give us power..").build()).setSlot(10).setClick(click -> {
								Player p = click.getElement();
								click.setCancelled(true);
								MenuType.PRINTABLE.build()
										.setTitle("&2Type an amount")
										.setSize(Menu.Rows.ONE)
										.setHost(api.getPlugin())
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
										.setHost(api.getPlugin())
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
										.setHost(api.getPlugin())
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
													clan.takeClaims(amount);
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
										.setHost(api.getPlugin())
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
										.setHost(api.getPlugin())
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
										.setHost(api.getPlugin())
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
										.setHost(api.getPlugin())
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
										.setHost(api.getPlugin())
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
										.setHost(api.getPlugin())
										.setStock(inv -> inv.addItem(be -> be.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle("0").build()).setSlot(0).setClick(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
										}))).join()
										.addAction(c -> {
											c.setCancelled(true);
											c.setHotbarAllowed(false);
											if (c.getSlot() == 2) {
												if (c.getParent().getName().equals("1")) {
													Clan.ACTION.remove(clan.getOwner().getId(), false).deploy();
													p.closeInventory();
												} else {
													p.closeInventory();
													Clan.ACTION.sendMessage(p, "&cFailed to confirm deletion.");
												}
											}

										}).open(p);
							}));

						}).join().addAction(c -> c.setCancelled(true));
			case SETTINGS_MEMBER_LIST:
				return MenuType.PAGINATED.build()
						.setHost(api.getPlugin())
						.setKey("Clans:" + clan.getName() + "-members-edit")
						.setSize(getSize())
						.setTitle(Clan.ACTION.color("Members"))
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							BorderElement<?> border = new BorderElement<>(i);
							for (Menu.Panel p : Menu.Panel.values()) {
								if (p == Menu.Panel.MIDDLE) continue;
								if (LabyrinthProvider.getInstance().isLegacy()) {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Items.findMaterial("STAINED_GLASS_PANE")).setTitle(" ").build()));
								} else {
									border.add(p, ed -> ed.setType(ItemElement.ControlType.ITEM_BORDER).setElement(it -> it.setType(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build()));
								}
							}
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
							i.addItem(new ListElement<>(new ArrayList<>(clan.getMembers())).setLimit(getLimit()).setPopulate((value, element) -> element.setElement(it -> it.setItem(value.getHead()).setTitle(clan.getPalette().toString() + value.getName()).setLore("&5Click to view my information.").build()).setClick(c -> {
								c.setCancelled(true);
								SETTINGS_MEMBER.get(value).open(c.getElement());
							})));
						}).orGet(m -> m instanceof PaginatedMenu && m.getKey().map(("Clans:" + clan.getName() + "-members-edit")::equals).orElse(false));

			case RESERVOIR:
				MemoryDocket<UnknownGeneric> docket = new MemoryDocket<>(ClansAPI.getDataInstance().getMessages().getRoot().getNode("menu.home.reservoir"));
				docket.setUniqueDataConverter(clan, Clan.memoryDocketReplacer());
				docket.setNamePlaceholder(":owner_name:");
				return docket.load().toMenu();
			case MEMBER_LIST:
				Node h = ClansAPI.getDataInstance().getMessages().getRoot().getNode("menu.members");
				MemoryDocket<Clan.Associate> dm = DefaultDocketRegistry.get(Clan.memoryDocketReplacer().apply(h.getNode("id").toPrimitive().getString(), clan));
				if (dm != null) {
					return dm.toMenu();
				}
		}
		throw new IllegalArgumentException("GUI type " + name() + " not valid, contact developers.");
	}

	public Menu get(Arena arena) {
		switch (this) {
			case ARENA_SPAWN:
				return MenuType.SINGULAR.build().setHost(api.getPlugin())
						.setSize(Menu.Rows.ONE)
						.setTitle("&2&oTeleport to &a" + arena.getId() + " &0&l»")
						.setKey("Clans:war-" + arena.getId())
						.setProperty(Menu.Property.CACHEABLE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(b -> b.setElement(ed -> ed.setType(Items.findMaterial("CLOCK") != null ? Items.findMaterial("CLOCK") : Items.findMaterial("WATCH")).setTitle("&7[&6&lClick&7]").setLore("&bClick to teleport to your spawn in arena &e" + arena.getId()).build()).setSlot(4).setClick(c -> {
								c.setCancelled(true);
								api.getAssociate(c.getElement()).ifPresent(a -> {
									Arena.Team t = arena.getTeam(a.getClan());
									Location loc = t.getSpawn();
									if (loc == null) {
										Clan.ACTION.sendMessage(c.getElement(), "&cYour team's spawn location isn't properly setup. Contact staff for support.");
										return;
									}
									c.getElement().teleport(loc);
								});
							}));
						})
						.orGet(m -> m instanceof SingularMenu && m.getKey().map(("Clans:war-" + arena.getId())::equals).orElse(false));
			case ARENA_TRUCE:
				return MenuType.SINGULAR.build().setHost(api.getPlugin())
						.setSize(Menu.Rows.ONE)
						.setTitle("&2&oTruce Vote &0&l»")
						.setKey("Clans:war-" + arena.getId() + "-truce")
						.setProperty(Menu.Property.CACHEABLE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(b -> b.setElement(ed -> ed.setType(Items.findMaterial("reddye") != null ? Items.findMaterial("reddye") : Items.findMaterial("lavabucket")).setTitle("&7[&4&lNO&7]").setLore("&cClick to vote no").build()).setSlot(2).setClick(c -> {
								c.setCancelled(true);
								api.getAssociate(c.getElement()).ifPresent(a -> {
									Vote v = arena.getVote();
									v.cast(Vote.NO);
									for (Clan cl : arena.getQueue().getTeams()) {
										cl.broadcast("&aWar participant " + a.getNickname() + " voted &cno &aon a truce.");
									}
									int acount = arena.getQueue().size();
									if (acount > 1) {
										acount = Math.floorDiv(acount, 2);
									}
									if (v.count(Vote.NO) >= acount) {
										v.clear();
										LabyrinthProvider.getService(Service.MESSENGER).getEmptyMailer().prefix().start(api.getPrefix().toString()).finish().announce(p -> true, "&c&oTruce amongst the clans failed. Not enough votes yes.").deploy();
									}
								});
								c.getElement().closeInventory();
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Items.findMaterial("greendye") != null ? Items.findMaterial("greendye") : Items.findMaterial("waterbucket")).setTitle("&7[&2&lYES&7]").setLore("&cClick to vote yes").build()).setSlot(6).setClick(c -> {
								c.setCancelled(true);
								api.getAssociate(c.getElement()).ifPresent(a -> {
									Arena.Team t = arena.getTeam(a.getClan());
									if (t != null) {
										Vote v = arena.getVote(t);
										v.cast(Vote.YES);
										for (Clan cl : arena.getQueue().getTeams()) {
											cl.broadcast("&aWar participant " + a.getNickname() + " voted &6yes &aon a truce.");
										}
										int acount = arena.getQueue().size();
										if (acount > 1) {
											acount = Math.floorDiv(acount, 2);
										}
										if (v.count(Vote.YES) >= acount) {
											if (v.isUnanimous()) {
												v.clear();
												LabyrinthProvider.getService(Service.MESSENGER).getEmptyMailer().prefix().start(api.getPrefix().toString()).finish().announce(p -> true, "&c&oTruce amongst the clans failed . Not enough votes yes.").deploy();
											} else {
												// good to go cancel
												if (arena.stop()) {
													arena.reset();
													a.getClan().takePower(8.6);
													LabyrinthProvider.getService(Service.MESSENGER).getEmptyMailer().prefix().start(api.getPrefix().toString()).finish().announce(p -> true, "&3&oA truce was called and the war is over.").deploy();
												}
											}
										}
									}
								});
								c.getElement().closeInventory();
							}));
						})
						.orGet(m -> m instanceof SingularMenu && m.getKey().map(("Clans:war-" + arena.getId() + "-truce")::equals).orElse(false));
			case ARENA_SURRENDER:
				return MenuType.SINGULAR.build().setHost(api.getPlugin())
						.setSize(Menu.Rows.ONE)
						.setTitle("&2&oSurrender Vote &0&l»")
						.setKey("Clans:war-" + arena.getId() + "-surrender")
						.setProperty(Menu.Property.CACHEABLE)
						.setStock(i -> {
							FillerElement<?> filler = new FillerElement<>(i);
							filler.add(ed -> ed.setElement(it -> it.setType(Optional.ofNullable(Items.findMaterial("bluestainedglasspane")).orElse(Items.findMaterial("stainedglasspane"))).setTitle(" ").build()));
							i.addItem(filler);
							i.addItem(b -> b.setElement(ed -> ed.setType(Items.findMaterial("reddye") != null ? Items.findMaterial("reddye") : Items.findMaterial("lavabucket")).setTitle("&7[&4&lNO&7]").setLore("&cClick to vote no").build()).setSlot(2).setClick(c -> {
								c.setCancelled(true);
								api.getAssociate(c.getElement()).ifPresent(a -> {
									Arena.Team t = arena.getTeam(a.getClan());
									if (t != null) {
										Vote v = arena.getVote(t);
										v.cast(Vote.NO);
										for (Clan cl : arena.getQueue().getTeams()) {
											cl.broadcast("&aWar participant " + a.getNickname() + " voted &cno &aon surrendering.");
										}
										int acount = arena.getQueue().count(a.getClan());
										if (acount > 1) {
											acount = Math.floorDiv(acount, 2);
										}
										if (v.count(Vote.NO) >= acount) {
											v.clear();
											LabyrinthProvider.getService(Service.MESSENGER).getEmptyMailer().prefix().start(api.getPrefix().toString()).finish().announce(p -> true, "&c&oClan " + a.getClan().getName() + " failed to surrender. Not enough votes yes.").deploy();
										}
									}
								});
								c.getElement().closeInventory();
							}));
							i.addItem(b -> b.setElement(ed -> ed.setType(Items.findMaterial("greendye") != null ? Items.findMaterial("greendye") : Items.findMaterial("waterbucket")).setTitle("&7[&2&lYES&7]").setLore("&cClick to vote yes").build()).setSlot(6).setClick(c -> {
								c.setCancelled(true);
								api.getAssociate(c.getElement()).ifPresent(a -> {
									Arena.Team t = arena.getTeam(a.getClan());
									if (t != null) {
										Vote v = arena.getVote(t);
										v.cast(Vote.YES);
										for (Clan cl : arena.getQueue().getTeams()) {
											cl.broadcast("&aWar participant " + a.getNickname() + " voted &6yes &aon surrendering.");
										}
										int acount = arena.getQueue().count(a.getClan());
										if (acount > 1) {
											acount = Math.floorDiv(acount, 2);
										}
										if (v.count(Vote.YES) >= acount) {
											if (v.isUnanimous()) {
												v.clear();
												// Mutual votes. cancel voting
												LabyrinthProvider.getService(Service.MESSENGER).getEmptyMailer().prefix().start(api.getPrefix().toString()).finish().announce(p -> true, "&c&oClan " + a.getClan().getName() + " failed to surrender. Voting came to a draw.").deploy();
											} else {
												// good to go cancel
												if (arena.stop()) {
													arena.reset();
													a.getClan().takePower(8.6);
													LabyrinthProvider.getService(Service.MESSENGER).getEmptyMailer().prefix().start(api.getPrefix().toString()).finish().announce(p -> true, "&5&oClan " + a.getClan().getName() + " has surrendered.").deploy();
												}
											}
										}
									}
								});
								c.getElement().closeInventory();
							}));
						})
						.orGet(m -> m instanceof SingularMenu && m.getKey().map(("Clans:war-" + arena.getId() + "-surrender")::equals).orElse(false));

			default:
				throw new IllegalArgumentException("GUI type " + name() + " not valid, contact developers.");
		}
	}

	Menu.Rows getSize() {
		return Menu.Rows.valueOf(ClansAPI.getDataInstance().getMessageString("default-size"));
	}

	List<String> color(String... text) {
		ArrayList<String> convert = new ArrayList<>();
		for (String t : text) {
			convert.add(StringUtils.use(t).translate());
		}
		return convert;
	}

	ItemStack getRightItem() {
		ItemStack right = new Item.Edit(Material.DIRT).setItem(SkullType.ARROW_BLUE_RIGHT.get()).build();
		ItemMeta meta = right.getItemMeta();
		meta.setDisplayName(StringUtils.use("&aNext.").translate());
		right.setItemMeta(meta);
		return right;
	}

	ItemStack getLeftItem() {
		ItemStack left = new Item.Edit(Material.DIRT).setItem(SkullType.ARROW_BLUE_LEFT.get()).build();
		ItemMeta meta = left.getItemMeta();
		meta.setDisplayName(StringUtils.use("&aPrevious.").translate());
		left.setItemMeta(meta);
		return left;
	}

	ItemStack getBackItem() {
		ItemStack back = new Item.Edit(Material.DIRT).setItem(SkullType.ARROW_BLACK_DOWN.get()).build();
		;
		ItemMeta meta = back.getItemMeta();
		meta.setDisplayName(StringUtils.use("&cBack.").translate());
		back.setItemMeta(meta);
		return back;
	}

	int getLimit() {
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

	int getBack() {
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

	int getLeft() {
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

	int getRight() {
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
