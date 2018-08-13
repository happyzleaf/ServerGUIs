package com.happyzleaf.serverguis;

import com.google.inject.Inject;
import com.happyzleaf.serverguis.data.Gui;
import com.happyzleaf.serverguis.data.GuiData;
import com.happyzleaf.serverguis.data.SlotProperty;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.property.InventoryCapacity;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is a bit of a mess but i wanted it to be powerful yet i didn't want to spend too much time on a test plugin.
 */
@Plugin(id = "serverguis", name = "ServerGUIs", version = "1.0.0", description = "Allows you to set up some custom guis with the help of a Resource Pack.",
		url = "https://happyzleaf.com/", authors = {"happyzleaf"})
public class ServerGUIs {
	public static PluginContainer container;
	public static ServerGUIs instance;
	
	@Inject
	@DefaultConfig(sharedRoot = true)
	private File configFile;
	
	private static List<UUID> modifyingPlayers = new ArrayList<>();
	
	@Listener
	public void init(GameInitializationEvent event) {
		container = Sponge.getPluginManager().getPlugin("serverguis").get();
		instance = this;
		GuiData.init(configFile);
		
		CommandSpec serverguis = CommandSpec.builder()
				.permission("serverguis.enable")
				.arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.integer(Text.of("guiId")))),
						GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.string(Text.of("command")))),
						GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of("arguments")))))
				.executor((src, args) -> {
					if (!(src instanceof Player)) {
						throw new CommandException(Text.of(TextColors.RED, "You must be a player to use this command."));
					}
					Player player = (Player) src;
					
					Integer guiId = (Integer) args.getOne("guiId").orElse(null);
					if (guiId == null) {
						if (modifyingPlayers.contains(player.getUniqueId())) {
							modifyingPlayers.remove(player.getUniqueId());
							
							src.sendMessage(Text.of(TextColors.GREEN, "You're no longer in modify mode."));
						} else {
							modifyingPlayers.add(player.getUniqueId());
							
							src.sendMessage(Text.of(TextColors.GREEN, "You're in modify mode. Right-Click a block to attach a GUI. Shift-Right-Click it to remove its GUI."));
						}
					} else {
						Gui gui = GuiData.guiList.stream().filter(g -> g.id == guiId).findFirst().orElse(null);
						if (gui == null) {
							throw new CommandException(Text.of(TextColors.RED, "The gui cannot be found. Please launch this command only when suggested."));
						}
						
						String command = ((String) args.getOne("command").orElse("")).toLowerCase();
						String[] arguments = args.getOne("arguments").map(o -> ((String) o).split(" ")).orElse(new String[0]);
						switch (command) {
							case "settype":
								if (arguments.length != 1) {
									throw new CommandException(Text.of(TextColors.RED, "Not enough arguments. Please follow the suggestions."));
								}
								String archetypeId = arguments[0];
								if (!archetypeId.contains(":")) archetypeId = "minecraft:" + archetypeId;
								gui.archetypeId = Sponge.getRegistry().getType(InventoryArchetype.class, archetypeId).orElseThrow(() -> new CommandException(Text.of(TextColors.RED, "The archetype \'" + arguments[0] + "\' cannot be found."))).getId();
								
								src.sendMessage(Text.of(TextColors.GREEN, "Successfully updated the type."));
								break;
							case "rows":
							case "columns":
								if (arguments.length != 1) {
									throw new CommandException(Text.of(TextColors.RED, "Not enough arguments. Please follow the suggestions."));
								}
								int value;
								try {
									value = Integer.parseInt(arguments[0]);
								} catch (NumberFormatException e) {
									throw new CommandException(Text.of(TextColors.RED, "\'" + arguments[0] + "\' is not a valid number."));
								}
								if (command.equals("rows")) {
									gui.rows = value;
								} else {
									gui.columns = value;
								}
								break;
							case "setlowergui":
							case "setuppergui":
								if (arguments.length != 2) {
									throw new CommandException(Text.of(TextColors.RED, "Not enough arguments. Please follow the suggestions."));
								}
								int damage;
								try {
									damage = Integer.parseInt(arguments[1]);
								} catch (NumberFormatException e) {
									throw new CommandException(Text.of(TextColors.RED, "\'" + arguments[1] + "\' is not a valid damage. (must be a number)."));
								}
								ItemStack is = ItemStack.builder().itemType(Sponge.getGame().getRegistry().getType(ItemType.class, arguments[0]).orElseThrow(() -> new CommandException(Text.of(TextColors.RED, "The item \"" + arguments[0] + "\" cannot be found in the registry.")))).add(Keys.UNBREAKABLE, true).add(Keys.DISPLAY_NAME, Text.EMPTY).build();
								is = ItemStack.builder().fromContainer(is.toContainer().set(DataQuery.of("UnsafeDamage"), damage)).build();
								if (command.equals("setuppergui")) {
									gui.upperGui = is.createSnapshot();
									
									src.sendMessage(Text.of(TextColors.GREEN, "Successfully updated the upper GUI."));
								} else {
									gui.lowerGui = is.createSnapshot();
									
									src.sendMessage(Text.of(TextColors.GREEN, "Successfully updated the lower GUI."));
								}
								break;
							case "setblock":
								if (arguments.length != 1) {
									throw new CommandException(Text.of(TextColors.RED, "Not enough arguments. Please follow the suggestions."));
								}
								int position;
								try {
									position = Integer.parseInt(arguments[0]);
								} catch (NumberFormatException e) {
									throw new CommandException(Text.of(TextColors.RED, "\'" + arguments[0] + "\' is not a valid position. (must be a number)."));
								}
								if (position <= 0) {
									throw new CommandException(Text.of(TextColors.RED, "The position must be start from 1"));
								}
								ItemStack handIs = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
								gui.items.put(position - 1, handIs == null || handIs.getType() == ItemTypes.AIR ? null : handIs.createSnapshot());
								
								src.sendMessage(Text.of(TextColors.GREEN, "Successfully updated slot number " + position + " with your held item."));
								break;
							case "switchvisibility":
								gui.showPlayerInventory = !gui.showPlayerInventory;
								
								src.sendMessage(Text.of(TextColors.GREEN, gui.showPlayerInventory ? "The player's inventory is now visible in the GUI." : "The player's inventory is now invisible in the GUI."));
								break;
						}
					}
					GuiData.saveNode();
					return CommandResult.success();
				})
				.build();
		Sponge.getCommandManager().register(this, serverguis, "serverguis");
	}
	
	@Listener
	public void onGameReload(GameReloadEvent event) {
		GuiData.loadNode();
	}
	
	@Listener
	public void onInteractBlock(InteractBlockEvent.Secondary.MainHand event) {
		if (event.getSource() instanceof Player) {
			Player player = (Player) event.getSource();
			String dimensionId = Sponge.getServer().getWorld(event.getTargetBlock().getWorldUniqueId()).map(w -> w.getDimension().getType().getId()).orElse(null);
			if (dimensionId == null) {
				player.sendMessage(Text.of(TextColors.GREEN, "Cannot retrieve the dimension from the block, try again (trust me)."));
				return;
			}
			Gui gui = GuiData.guiList.stream().filter(g -> g.dimensionId.equals(dimensionId) && g.position.equals(event.getTargetBlock().getPosition())).findFirst().orElse(null);
			
			ItemStack is = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
			if ((is == null || is.getType() == ItemTypes.AIR) && modifyingPlayers.contains(player.getUniqueId())) {
				if (player.get(Keys.IS_SNEAKING).orElse(false)) {
					if (gui != null) {
						GuiData.guiList.remove(gui);
						GuiData.saveNode();
						
						player.sendMessage(Text.of(TextColors.GREEN, "Successfully removed the GUI from this block."));
					} else {
						player.sendMessage(Text.of(TextColors.RED, "This block does not have a GUI."));
					}
				} else {
					if (gui == null) {
						gui = new Gui();
						gui.position = event.getTargetBlock().getPosition();
						gui.dimensionId = dimensionId;
						GuiData.guiList.add(gui);
						GuiData.saveNode();
						
						player.sendMessage(Text.of(TextColors.GREEN, "Successfully transformed this block into a GUI."));
					}
					
					player.sendMessage(Text.of(" "));
					player.sendMessage(Text.builder("Modify your GUI").color(TextColors.DARK_GREEN)
							.append(Text.NEW_LINE)
							.append(Text.builder("[Set Type]").color(TextColors.GOLD)
									.onHover(TextActions.showText(Text.of(TextColors.YELLOW, "Click to change the type of the inventory.\n", TextColors.YELLOW, "You will need to type the id.\n", TextColors.YELLOW, "Available types: " + listArchetypes() + ".")))
									.onClick(TextActions.suggestCommand("/serverguis " + gui.id + " settype <type>"))
									.build())
							.append(Text.NEW_LINE)
							.append(Text.builder("[Set Columns]").color(TextColors.GOLD)
									.onHover(TextActions.showText(Text.of(TextColors.YELLOW, "Click to change the number of columns.\n", TextColors.YELLOW, "You will need to type the number.")))
									.onClick(TextActions.suggestCommand("/serverguis " + gui.id + " setcolumns <columns>"))
									.build())
							.append(Text.NEW_LINE)
							.append(Text.builder("[Set Rows]").color(TextColors.GOLD)
									.onHover(TextActions.showText(Text.of(TextColors.YELLOW, "Click to change the number of rows.\n", TextColors.YELLOW, "You will need to type the number.")))
									.onClick(TextActions.suggestCommand("/serverguis " + gui.id + " setrows <rows>"))
									.build())
							.append(Text.NEW_LINE)
							.append(Text.builder("[Set Upper GUI]").color(TextColors.GOLD)
									.onHover(TextActions.showText(Text.of(TextColors.YELLOW, "Click to set the upper gui.\n", TextColors.YELLOW, "You will need to type the item id and the damage value.")))
									.onClick(TextActions.suggestCommand("/serverguis " + gui.id + " setuppergui minecraft:diamond_hoe <damage>"))
									.build())
							.append(Text.NEW_LINE)
							.append(Text.builder("[Set Lower GUI]").color(TextColors.GOLD)
									.onHover(TextActions.showText(Text.of(TextColors.YELLOW, "Click to set the lower gui.\n", TextColors.YELLOW, "You will need to type the item id and the damage value.")))
									.onClick(TextActions.suggestCommand("/serverguis " + gui.id + " setlowergui minecraft:diamond_hoe <damage>"))
									.build())
							.append(Text.NEW_LINE)
							.append(Text.builder("[Set Item]").color(TextColors.GOLD)
									.onHover(TextActions.showText(Text.of(TextColors.YELLOW, "Click to apply the held item to the given position.\n", TextColors.YELLOW, "You will need to type inventory position.")))
									.onClick(TextActions.suggestCommand("/serverguis " + gui.id + " setblock <position>"))
									.build())
							.append(Text.NEW_LINE)
							.append(Text.builder("[Switch Inventory Visibility]").color(TextColors.GOLD)
									.onHover(TextActions.showText(Text.of(TextColors.YELLOW, "Click to set visible/invisible the player's inventory inside the gui.")))
									.onClick(TextActions.runCommand("/serverguis " + gui.id + " switchvisibility"))
									.build())
							.build());
					player.sendMessage(Text.of(" "));
				}
				event.setCancelled(true);
			} else if (gui != null) {
				Inventory inv = Inventory.builder().of(Sponge.getRegistry().getType(InventoryArchetype.class, gui.archetypeId).get())/*.property(InventoryDimension.of(gui.columns, gui.rows))*/.property(InventoryCapacity.of(gui.columns)).property(new SlotProperty(null, Property.Operator.EQUAL)).build(this);
				int i = 0;
				for (Inventory slot : inv.slots()) {
					slot.peek();
					if (i == 0 && gui.upperGui != null) {
						slot.set(gui.upperGui.createStack());
					} else if (i == 18 && gui.lowerGui != null) {
						slot.set(gui.lowerGui.createStack());
					} else {
						ItemStackSnapshot snapshot = gui.items.get(i);
						slot.set(snapshot == null ? ItemStack.empty() : snapshot.createStack());
					}
					i++;
				}
				if (!gui.showPlayerInventory) {
					List<ItemStack> slots = new ArrayList<>();
					for (Inventory slot : player.getInventory().slots()) {
						slots.add(slot.poll().orElse(ItemStack.empty()));
					}
					inv.getInventoryProperty(SlotProperty.class).get().setSlots(slots);
				}
				Task.builder().execute(() -> player.openInventory(inv)).submit(this);
				event.setCancelled(true);
			}
		}
	}
	
	//Don't sue me pls
	private static String listArchetypes() {
		StringBuilder result = new StringBuilder();
		try {
			for (Field f : InventoryArchetypes.class.getFields()) {
				Object o = f.get(null);
				String id = ((CatalogType) o).getId();
				if (id.startsWith("sponge:") || id.equals("minecraft:unknown") || id.equals("minecraft:player") || id.equals("minecraft:crafting")) continue;
				if (id.startsWith("minecraft:")) id = id.substring(10);
				if (o instanceof InventoryArchetype) {
					if (result.length() == 0) {
						result.append(id);
					} else {
						result.append(", ").append(id);
					}
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return result.toString();
	}
	
	@Listener
	public void onClickInventory(ClickInventoryEvent event) {
		if (event.getTargetInventory().getInventoryProperty(SlotProperty.class).isPresent()) {
			event.setCancelled(true);
		}
	}
	
	@Listener
	public void onInteractInventory(InteractInventoryEvent.Close event, @Root Player player) {
		List<ItemStack> slots = event.getTargetInventory().getInventoryProperty(SlotProperty.class).map(SlotProperty::getValue).orElse(null);
		if (slots != null) {
			Task.builder().execute(() -> {
				Iterator<ItemStack> iterator = slots.iterator();
				for (Inventory s : player.getInventory().slots()) {
					if (iterator.hasNext()) {
						s.set(iterator.next());
					}
				}
			}).submit(this);
		}
	}
	
	@Listener
	public void onBlockBreak(ChangeBlockEvent.Break event) {
		for (BlockSnapshot block : event.getTransactions().stream().map(Transaction::getOriginal).collect(Collectors.toList())) {
			String dimensionId = Sponge.getServer().getWorld(block.getWorldUniqueId()).get().getDimension().getType().getId();
			if (GuiData.guiList.removeIf(g -> g.dimensionId.equals(dimensionId) && g.position.equals(block.getPosition()))) {
				GuiData.saveNode();
			}
		}
	}
	
	@Listener
	public void onChangeInventory(ChangeInventoryEvent event) {
		if (event.getTargetInventory().getInventoryProperty(SlotProperty.class).isPresent()) {
			event.setCancelled(true);
		}
	}
}
