package com.github.sanctum.clans.construct.api;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.reload.FingerPrint;
import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import com.github.sanctum.labyrinth.gui.unity.impl.InventoryElement;
import com.github.sanctum.labyrinth.gui.unity.impl.MenuType;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.panther.annotation.Note;
import com.github.sanctum.panther.util.TypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractGameRule {

	public static final String BLOCKED_WAR_COMMANDS = "blocked_commands_war";
	public static final String WAR_START_TIME = "war_start_time";
	public static final String MAX_CLANS = "max_clans";
	public static final String MAX_POWER = "max_power";
	public static final String DEFAULT_WAR_MODE = "mode_default";
	public static final String CLAN_INFO_SIMPLE = "clan_info_simple";
	public static final String CLAN_INFO_SIMPLE_OTHER = "clan_info_simple_other";

	private final FingerPrint print;

	protected AbstractGameRule(FingerPrint print) {
		this.print = print;
	}

	public static AbstractGameRule of(@NotNull FingerPrint print) {
		return InoperableSpecialMemory.SCANNER_MAP.computeIfAbsent(print, print1 -> new AbstractGameRule(print1) {
		});
	}

	@Note("Local only game rules!")
	public static AbstractGameRule[] of() {
		if (InoperableSpecialMemory.SCANNER_MAP.isEmpty()) {
			LabyrinthProvider.getInstance().getLocalPrintManager().getPrints(ClansAPI.getInstance().getPlugin()).forEach(AbstractGameRule::of);
		}
		return InoperableSpecialMemory.SCANNER_MAP.entries().stream().filter(entry -> entry.getKey().getKey().equals(ClansAPI.getInstance().getLocalPrintKey())).map(Map.Entry::getValue).toArray(AbstractGameRule[]::new);
	}

	public void set(@MagicConstant(valuesFromClass = AbstractGameRule.class) String key, Object o) {
		ClansAPI.getDataInstance().getResetTable().set(key, o);
	}

	public Object get(@MagicConstant(valuesFromClass = AbstractGameRule.class) String key) {
		return print.get(key);
	}

	public void reload(@MagicConstant(valuesFromClass = AbstractGameRule.class) String key) {
		print.reload(key).deploy();
	}

	public InventoryElement.Printable edit(@NotNull Modification modification, @MagicConstant(valuesFromClass = AbstractGameRule.class) String key) {
		Object o = print.get(key);
		if (o instanceof List) {
			switch (modification) {

				case SET:
					return (InventoryElement.Printable) MenuType.PRINTABLE.build()
							.setTitle("&3&lEdit &r" + key.replace("_", " "))
							.setHost(ClansAPI.getInstance().getPlugin())
							.setSize(Menu.Rows.ONE)
							.setStock(i -> i.addItem(be -> be.setElement(it -> it.setType(Material.DIAMOND_SWORD).setTitle(" ").setLore("Click to reset").setFlags(ItemFlag.HIDE_ENCHANTS).build()).setSlot(0).setClick(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
							})).addItem(it -> it.setElement(el -> el.setType(Material.DIAMOND_SWORD).addEnchantment(Enchantment.MENDING, 1).setFlags(ItemFlag.HIDE_ENCHANTS).setTitle("&cClick to go back").build()).setClick(c -> {
								c.setCancelled(true);
								GUI.SETTINGS_GAME_RULE.get().open(c.getElement());
							}).setSlot(1))).join().addAction(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
								if (c.getSlot() == 2) {
									List<String> list = new ArrayList<>();
									list.add(c.getParent().getName());
									ClansAPI.getDataInstance().getResetTable().set(key, list);
									print.reload().deploy();
									Clan.ACTION.sendMessage(c.getElement(), "&6Field &r" + key + " &5overwritten");
								}

							}).getInventory();
				case ADD:
					return (InventoryElement.Printable) MenuType.PRINTABLE.build()
							.setTitle("&aEdit &r" + key.replace("_", " "))
							.setHost(ClansAPI.getInstance().getPlugin())
							.setSize(Menu.Rows.ONE)
							.setStock(i -> i.addItem(be -> be.setElement(it -> it.setType(Material.DIAMOND_SWORD).setTitle(" ").setLore("Click to reset").setFlags(ItemFlag.HIDE_ENCHANTS).build()).setSlot(0).setClick(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
							})).addItem(it -> it.setElement(el -> el.setType(Material.DIAMOND_SWORD).addEnchantment(Enchantment.MENDING, 1).setFlags(ItemFlag.HIDE_ENCHANTS).setTitle("&cClick to go back").build()).setClick(c -> {
								c.setCancelled(true);
								GUI.SETTINGS_GAME_RULE.get().open(c.getElement());
							}).setSlot(1))).join().addAction(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
								if (c.getSlot() == 2) {
									TypeAdapter<List<String>> flag = TypeAdapter.get();
									List<String> list = new ArrayList<>(flag.cast(o));
									list.add(c.getParent().getName());
									ClansAPI.getDataInstance().getResetTable().set(key, list);
									print.reload().deploy();
									Clan.ACTION.sendMessage(c.getElement(), "&6Field &r" + key + " &aadded &r" + c.getParent().getName());
								}

							}).getInventory();
				case REMOVE:
					return (InventoryElement.Printable) MenuType.PRINTABLE.build()
							.setTitle("&cEdit &r" + key.replace("_", " "))
							.setHost(ClansAPI.getInstance().getPlugin())
							.setSize(Menu.Rows.ONE)
							.setStock(i -> i.addItem(be -> be.setElement(it -> it.setType(Material.DIAMOND_SWORD).setTitle(" ").setLore("Click to reset").setFlags(ItemFlag.HIDE_ENCHANTS).build()).setSlot(0).setClick(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
							})).addItem(it -> it.setElement(el -> el.setType(Material.DIAMOND_SWORD).addEnchantment(Enchantment.MENDING, 1).setFlags(ItemFlag.HIDE_ENCHANTS).setTitle("&cClick to go back").build()).setClick(c -> {
								c.setCancelled(true);
								GUI.SETTINGS_GAME_RULE.get().open(c.getElement());
							}).setSlot(1))).join().addAction(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
								if (c.getSlot() == 2) {
									TypeAdapter<List<String>> flag = TypeAdapter.get();
									List<String> list = new ArrayList<>(flag.cast(o));
									list.remove(c.getParent().getName());
									ClansAPI.getDataInstance().getResetTable().set(key, list);
									print.reload().deploy();
									Clan.ACTION.sendMessage(c.getElement(), "&6Field &r" + key + " &cremoved &r" + c.getParent().getName());
								}
							}).getInventory();
				default:
					return null;
			}
		}
		if (o instanceof String) {
			if (modification == Modification.SET) {
				return (InventoryElement.Printable) MenuType.PRINTABLE.build()
						.setTitle("&3&lEdit &r" + key.replace("_", " "))
						.setHost(ClansAPI.getInstance().getPlugin())
						.setSize(Menu.Rows.ONE)
						.setStock(i -> i.addItem(be -> be.setElement(it -> it.setType(Material.DIAMOND_SWORD).setTitle(" ").setLore("Click to reset").setFlags(ItemFlag.HIDE_ENCHANTS).build()).setSlot(0).setClick(c -> {
							c.setCancelled(true);
							c.setHotbarAllowed(false);
						})).addItem(it -> it.setElement(el -> el.setType(Material.DIAMOND_SWORD).addEnchantment(Enchantment.MENDING, 1).setFlags(ItemFlag.HIDE_ENCHANTS).setTitle("&cClick to go back").build()).setClick(c -> {
							c.setCancelled(true);
							GUI.SETTINGS_GAME_RULE.get().open(c.getElement());
						}).setSlot(1))).join().addAction(c -> {
							c.setCancelled(true);
							c.setHotbarAllowed(false);
							if (c.getSlot() == 2) {
								ClansAPI.getDataInstance().getResetTable().set(key, c.getParent().getName());
								print.reload().deploy();
								Clan.ACTION.sendMessage(c.getElement(), "&6Field &r" + key + " &5overwritten");
							}
						}).getInventory();
			}
			return null;
		}
		if (o instanceof Double) {
			switch (modification) {

				case SET:
					return (InventoryElement.Printable) MenuType.PRINTABLE.build()
							.setTitle("&3&lEdit &r" + key.replace("_", " "))
							.setHost(ClansAPI.getInstance().getPlugin())
							.setSize(Menu.Rows.ONE)
							.setStock(i -> i.addItem(be -> be.setElement(it -> it.setType(Material.DIAMOND_SWORD).setTitle(" ").setLore("Click to reset").setFlags(ItemFlag.HIDE_ENCHANTS).build()).setSlot(0).setClick(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
							})).addItem(it -> it.setElement(el -> el.setType(Material.DIAMOND_SWORD).addEnchantment(Enchantment.MENDING, 1).setFlags(ItemFlag.HIDE_ENCHANTS).setTitle("&cClick to go back").build()).setClick(c -> {
								c.setCancelled(true);
								GUI.SETTINGS_GAME_RULE.get().open(c.getElement());
							}).setSlot(1))).join().addAction(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
								if (c.getSlot() == 2) {
									if (!StringUtils.use(c.getParent().getName()).isDouble()) return;
									double test = Double.parseDouble(c.getParent().getName());
									ClansAPI.getDataInstance().getResetTable().set(key, test);
									print.reload().deploy();
									Clan.ACTION.sendMessage(c.getElement(), "&6Field &r" + key + " &5overwritten");
								}
							}).getInventory();
				case ADD:
					return (InventoryElement.Printable) MenuType.PRINTABLE.build()
							.setTitle("&aEdit &r" + key.replace("_", " "))
							.setHost(ClansAPI.getInstance().getPlugin())
							.setSize(Menu.Rows.ONE)
							.setStock(i -> i.addItem(be -> be.setElement(it -> it.setType(Material.DIAMOND_SWORD).setTitle(" ").setLore("Click to reset").setFlags(ItemFlag.HIDE_ENCHANTS).build()).setSlot(0).setClick(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
							})).addItem(it -> it.setElement(el -> el.setType(Material.DIAMOND_SWORD).addEnchantment(Enchantment.MENDING, 1).setFlags(ItemFlag.HIDE_ENCHANTS).setTitle("&cClick to go back").build()).setClick(c -> {
								c.setCancelled(true);
								GUI.SETTINGS_GAME_RULE.get().open(c.getElement());
							}).setSlot(1))).join().addAction(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
								if (c.getSlot() == 2) {
									TypeAdapter<Double> flag = TypeAdapter.get();
									double i = flag.cast(o);
									if (!StringUtils.use(c.getParent().getName()).isDouble()) return;
									double test = Double.parseDouble(c.getParent().getName());
									ClansAPI.getDataInstance().getResetTable().set(key, (i + test));
									print.reload().deploy();
									Clan.ACTION.sendMessage(c.getElement(), "&6Field &r" + key + " &aadded &r" + c.getParent().getName());
								}
							}).getInventory();
				case REMOVE:
					return (InventoryElement.Printable) MenuType.PRINTABLE.build()
							.setTitle("&cEdit &r" + key.replace("_", " "))
							.setHost(ClansAPI.getInstance().getPlugin())
							.setSize(Menu.Rows.ONE)
							.setStock(i -> i.addItem(be -> be.setElement(it -> it.setType(Material.DIAMOND_SWORD).setTitle(" ").setLore("Click to reset").setFlags(ItemFlag.HIDE_ENCHANTS).build()).setSlot(0).setClick(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
							})).addItem(it -> it.setElement(el -> el.setType(Material.DIAMOND_SWORD).addEnchantment(Enchantment.MENDING, 1).setFlags(ItemFlag.HIDE_ENCHANTS).setTitle("&cClick to go back").build()).setClick(c -> {
								c.setCancelled(true);
								GUI.SETTINGS_GAME_RULE.get().open(c.getElement());
							}).setSlot(1))).join().addAction(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
								if (c.getSlot() == 2) {
									TypeAdapter<Double> flag = TypeAdapter.get();
									double i = flag.cast(o);
									if (!StringUtils.use(c.getParent().getName()).isDouble()) return;
									double test = Double.parseDouble(c.getParent().getName());
									ClansAPI.getDataInstance().getResetTable().set(key, (i - test));
									print.reload().deploy();
									Clan.ACTION.sendMessage(c.getElement(), "&6Field &r" + key + " &cremoved &r" + c.getParent().getName());
								}
							}).getInventory();
				default:
					return null;
			}
		}
		if (o instanceof Integer) {
			switch (modification) {

				case SET:
					return (InventoryElement.Printable) MenuType.PRINTABLE.build()
							.setTitle("&3&lEdit &r" + key.replace("_", " "))
							.setHost(ClansAPI.getInstance().getPlugin())
							.setSize(Menu.Rows.ONE)
							.setStock(i -> i.addItem(be -> be.setElement(it -> it.setType(Material.DIAMOND_SWORD).setTitle(" ").setLore("Click to reset").setFlags(ItemFlag.HIDE_ENCHANTS).build()).setSlot(0).setClick(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
							})).addItem(it -> it.setElement(el -> el.setType(Material.DIAMOND_SWORD).addEnchantment(Enchantment.MENDING, 1).setFlags(ItemFlag.HIDE_ENCHANTS).setTitle("&cClick to go back").build()).setClick(c -> {
								c.setCancelled(true);
								GUI.SETTINGS_GAME_RULE.get().open(c.getElement());
							}).setSlot(1))).join().addAction(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
								if (c.getSlot() == 2) {
									if (!StringUtils.use(c.getParent().getName()).isInt()) return;
									int test = Integer.parseInt(c.getParent().getName());
									ClansAPI.getDataInstance().getResetTable().set(key, test);
									print.reload().deploy();
									Clan.ACTION.sendMessage(c.getElement(), "&6Field &r" + key + " &5overwritten");
								}
							}).getInventory();
				case ADD:
					return (InventoryElement.Printable) MenuType.PRINTABLE.build()
							.setTitle("&aEdit &r" + key.replace("_", " "))
							.setHost(ClansAPI.getInstance().getPlugin())
							.setSize(Menu.Rows.ONE)
							.setStock(i -> i.addItem(be -> be.setElement(it -> it.setType(Material.DIAMOND_SWORD).setTitle(" ").setLore("Click to reset").setFlags(ItemFlag.HIDE_ENCHANTS).build()).setSlot(0).setClick(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
							})).addItem(it -> it.setElement(el -> el.setType(Material.DIAMOND_SWORD).addEnchantment(Enchantment.MENDING, 1).setFlags(ItemFlag.HIDE_ENCHANTS).setTitle("&cClick to go back").build()).setClick(c -> {
								c.setCancelled(true);
								GUI.SETTINGS_GAME_RULE.get().open(c.getElement());
							}).setSlot(1))).join().addAction(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
								if (c.getSlot() == 2) {
									TypeAdapter<Integer> flag = TypeAdapter.get();
									int i = flag.cast(o);
									if (!StringUtils.use(c.getParent().getName()).isInt()) return;
									int test = Integer.parseInt(c.getParent().getName());
									ClansAPI.getDataInstance().getResetTable().set(key, (i + test));
									print.reload().deploy();
									Clan.ACTION.sendMessage(c.getElement(), "&6Field &r" + key + " &aadded &r" + c.getParent().getName());
								}
							}).getInventory();
				case REMOVE:
					return (InventoryElement.Printable) MenuType.PRINTABLE.build()
							.setTitle("&cEdit &r" + key.replace("_", " "))
							.setHost(ClansAPI.getInstance().getPlugin())
							.setSize(Menu.Rows.ONE)
							.setStock(i -> i.addItem(be -> be.setElement(it -> it.setType(Material.DIAMOND_SWORD).setTitle(" ").setLore("Click to reset").setFlags(ItemFlag.HIDE_ENCHANTS).build()).setSlot(0).setClick(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
							})).addItem(it -> it.setElement(el -> el.setType(Material.DIAMOND_SWORD).addEnchantment(Enchantment.MENDING, 1).setFlags(ItemFlag.HIDE_ENCHANTS).setTitle("&cClick to go back").build()).setClick(c -> {
								c.setCancelled(true);
								GUI.SETTINGS_GAME_RULE.get().open(c.getElement());
							}).setSlot(1))).join().addAction(c -> {
								c.setCancelled(true);
								c.setHotbarAllowed(false);
								if (c.getSlot() == 2) {
									TypeAdapter<Integer> flag = TypeAdapter.get();
									int i = flag.cast(o);
									if (!StringUtils.use(c.getParent().getName()).isInt()) return;
									int test = Integer.parseInt(c.getParent().getName());
									ClansAPI.getDataInstance().getResetTable().set(key, (i - test));
									print.reload().deploy();
									Clan.ACTION.sendMessage(c.getElement(), "&6Field &r" + key + " &cremoved &r" + c.getParent().getName());
								}
							}).getInventory();
				default:
					return null;
			}
		}
		return null;
	}

	public enum Modification {

		SET, ADD, REMOVE

	}

}
