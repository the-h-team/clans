package com.github.sanctum.clans.construct.util;

import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import com.github.sanctum.labyrinth.gui.unity.construct.MenuRegistration;
import com.github.sanctum.labyrinth.gui.unity.construct.PaginatedMenu;
import com.github.sanctum.labyrinth.gui.unity.construct.SingularMenu;
import com.github.sanctum.labyrinth.gui.unity.impl.BorderElement;
import com.github.sanctum.labyrinth.gui.unity.impl.FillerElement;
import com.github.sanctum.labyrinth.gui.unity.impl.InventoryElement;
import com.github.sanctum.labyrinth.gui.unity.impl.ItemElement;
import com.github.sanctum.labyrinth.gui.unity.impl.ListElement;
import com.github.sanctum.labyrinth.gui.unity.impl.MenuType;
import com.github.sanctum.labyrinth.gui.unity.simple.MemoryDocket;
import com.github.sanctum.labyrinth.gui.unity.simple.MemoryItem;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.panther.file.MemorySpace;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.util.Check;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LiveDocket<T> extends MemoryDocket<T> {


	public LiveDocket(MemorySpace memorySpace) {
		super(memorySpace);
	}

	@Override
	public @NotNull MemoryDocket<T> load() {
		this.title = Check.forNull(memory.getNode("title").toPrimitive().getString(), "Configured menus cannot have null titles please correct under path '" + memory.getPath() + "'");
		if (this.uniqueData != null) {
			this.title = uniqueDataConverter.apply(title, uniqueData);
		}
		this.rows = Check.forNull(Menu.Rows.valueOf(memory.getNode("rows").toPrimitive().getString()), "Configured menus need a row size please correct under path '" + memory.getPath() + "'");
		this.type = Check.forNull(Menu.Type.valueOf(memory.getNode("type").toPrimitive().getString()), "Configured menus need a valid type please correct under path '" + memory.getPath() + "'");
		this.shared = memory.getNode("shared").toPrimitive().getBoolean();
		if (memory.getNode("id").toPrimitive().isString()) {
			if (this.uniqueData != null) {
				this.key = uniqueDataConverter.apply(memory.getNode("id").toPrimitive().getString(), uniqueData);
			} else {
				this.key = memory.getNode("id").toPrimitive().getString();
			}
		}
		if (memory.isNode("filler")) {
			this.filler = new MemoryItem(memory.getNode("filler"));
		}
		if (memory.isNode("border")) {
			this.border = new MemoryItem(memory.getNode("border"));
		}
		if (memory.isNode("pagination")) {
			pagination = new MemoryItem(memory.getNode("pagination"));
			if (memory.getNode("pagination").isNode("navigation")) {
				Node parent = memory.getNode("pagination").getNode("navigation");
				next = new MemoryItem(parent.getNode("next"));
				previous = new MemoryItem(parent.getNode("previous"));
				if (parent.isNode("exit")) {
					exit = new MemoryItem(parent.getNode("exit"));
				}
			}
		}
		if (memory.isNode("items")) {
			for (String item : memory.getNode("items").getKeys(false)) {
				MemoryItem i = new MemoryItem(memory.getNode("items").getNode(item));
				ItemStack result = i.toItem();
				ItemElement<?> element = new ItemElement<>();
				element.setElement(result);
				handlePlayerHeadLookup(true, result, element);
				if (i.getSlot() > -1) {
					element.setSlot(i.getSlot());
				}
				handleClickEvent(i, element);
				if (element.getElement().hasItemMeta() && element.getElement().getItemMeta().hasLore()) {
					List<String> lore = new ArrayList<>();
					for (String s : element.getElement().getItemMeta().getLore()) {
						String res = s;
						if (uniqueData != null) {
							res = uniqueDataConverter.apply(res, uniqueData);
						}
						lore.add(res);
					}
					element.setElement(edit -> edit.setLore(lore).build());
				}
				String res = element.getName();
				if (uniqueData != null ) {
					res = uniqueDataConverter.apply(res, uniqueData);
				}
				String finalRes = res;
				element.setElement(edit -> edit.setTitle(finalRes).build());
				items.add(element);
			}
		}
		if (key != null) {
			Menu test = MenuRegistration.getInstance().get(key).deploy().get();
			if (test != null) {
				this.instance = test;
				return this;
			}
		}
		if (type == Menu.Type.PAGINATED) {
			Menu.Builder<PaginatedMenu, InventoryElement.Paginated> paginated = MenuType.PAGINATED.build().setHost(plugin).setTitle(title).setSize(rows);
			if (key != null) paginated.setKey(key).setProperty(Menu.Property.CACHEABLE);
			paginated.setProperty(Menu.Property.LIVE_META);
			paginated.setStock(i -> {
				items.forEach(i::addItem);
				ListElement<T> element = new ListElement<>(supplier);
				if (this.comparator != null) {
					element.setComparator((o1, o2) -> comparator.compare(o1.getData().orElse(null), o2.getData().orElse(null)));
				}
				if (this.predicate != null) {
					element.setFilter(tItemElement -> predicate.test(tItemElement.getData().orElse(null)));
				}
				if (this.border != null) {
					BorderElement<?> border = new BorderElement<>(i);
					for (Menu.Panel p : Menu.Panel.values()) {
						if (p == Menu.Panel.MIDDLE) continue;
						border.add(p, ed -> {
							ItemStack built = this.border.toItem();
							ed.setElement(built);
							handleClickEvent(this.border, ed);
							ed.setType(ItemElement.ControlType.ITEM_BORDER);
						});
					}
					i.addItem(border);
				}
				if (this.filler != null) {
					FillerElement<?> filler = new FillerElement<>(i);
					filler.add(ed -> {
						ItemStack built = this.filler.toItem();
						ed.setElement(built);
						handleClickEvent(this.filler, ed);
						ed.setType(ItemElement.ControlType.ITEM_FILLER);
					});
					i.addItem(filler);
				}

				final ItemStack built = pagination.toItem();
				element.setLimit(pagination.getLimit());
				element.setPopulate((value, item) -> {
					item.setElement(built);
					handlePlayerHeadLookup(false, built, item, value);
					String title = item.getName();
					if (pagination != null) {
						if (pagination.isNotRemovable()) {
							item.setClick(click -> {
								click.setCancelled(true);
								if (pagination.isExitOnClick())
									click.getParent().getParent().getParent().close(click.getElement());
								if (pagination.getMessage() != null) {
									String res = handlePaginationReplacements(pagination, pagination.getMessage(), value);
									if (dataConverter != null) {
										res = dataConverter.apply(res, value);
									}
									Mailer.empty(click.getElement()).chat(res).deploy();
								}
								if (pagination.getOpenOnClick() != null) {
									String open = pagination.getOpenOnClick();
									String r = handlePaginationReplacements(pagination, open, value);
									if (dataConverter != null) {
										r = dataConverter.apply(r, value);
									}
									MenuRegistration registration = MenuRegistration.getInstance();
									Menu registered = registration.get(r).deploy().get();
									if (registered != null) {
										registered.open(click.getElement());
									} else {
										if (pagination.getOpenOnClick().startsWith("/")) {
											String command = pagination.getOpenOnClick().replace("/", "");
											String res = handlePaginationReplacements(pagination, command, value);
											if (dataConverter != null) {
												res = dataConverter.apply(res, value);
											}
											click.getElement().performCommand(res);
										}
									}
								}
							});
						}
						if (pagination.getReplacements() != null) {
							if (item.getElement().hasItemMeta() && item.getElement().getItemMeta().hasLore()) {
								List<String> lore = new ArrayList<>();
								for (String s : item.getElement().getItemMeta().getLore()) {
									String res = handlePaginationReplacements(pagination, s, value);
									if (dataConverter != null) {
										res = dataConverter.apply(res, value);
									}
									lore.add(res);
								}
								item.setElement(edit -> edit.setLore(lore).build());
							}
							String res = handlePaginationReplacements(pagination, title, value);
							if (dataConverter != null) {
								res = dataConverter.apply(res, value);
							}
							String finalRes = res;
							item.setElement(edit -> edit.setTitle(finalRes).build());
						}
					}
				});
				i.addItem(element);
				if (!Check.isNull(next, previous)) {
					i.addItem(b -> {
								b.setElement(it -> it.setItem(next.toItem()).build()).setType(ItemElement.ControlType.BUTTON_NEXT).setSlot(next.getSlot());
								handleClickEvent(next, b);
							})
							.addItem(b -> {
								b.setElement(it -> it.setItem(previous.toItem()).build()).setType(ItemElement.ControlType.BUTTON_BACK).setSlot(previous.getSlot());
								handleClickEvent(previous, b);
							});
				}
				if (exit != null) {
					i.addItem(b -> {
						b.setElement(it -> it.setItem(exit.toItem()).build()).setType(ItemElement.ControlType.BUTTON_EXIT).setSlot(exit.getSlot());
						handleClickEvent(exit, b);
					});
				}
			});
			this.instance = paginated.join();
		} else {
			Menu.Builder<SingularMenu, InventoryElement.Normal> singular = MenuType.SINGULAR.build().setHost(plugin).setTitle(title).setSize(rows);
			if (key != null) singular.setKey(key).setProperty(Menu.Property.CACHEABLE);
			singular.setProperty(Menu.Property.LIVE_META);
			singular.setStock(i -> {
				items.forEach(i::addItem);
				if (this.border != null) {
					BorderElement<?> border = new BorderElement<>(i);
					for (Menu.Panel p : Menu.Panel.values()) {
						if (p == Menu.Panel.MIDDLE) continue;
						border.add(p, ed -> {
							ItemStack built = this.border.toItem();
							ed.setElement(built);
							if (this.border != null) {
								handleClickEvent(this.border, ed);
							}
							ed.setType(ItemElement.ControlType.ITEM_BORDER);
						});
					}
					i.addItem(border);
				}
				if (this.filler != null) {
					FillerElement<?> filler = new FillerElement<>(i);
					filler.add(ed -> {
						ItemStack built = this.filler.toItem();
						ed.setElement(built);
						if (this.filler != null) {
							handleClickEvent(this.filler, ed);
						}
						ed.setType(ItemElement.ControlType.ITEM_FILLER);
					});
					i.addItem(filler);
				}
			});
			this.instance = singular.join();
		}
		return this;
	}

}
