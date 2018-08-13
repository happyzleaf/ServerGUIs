package com.happyzleaf.serverguis;

import com.happyzleaf.serverguis.data.Gui;
import com.happyzleaf.serverguis.data.GuiData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Plugin(id = "serverguis", name = "ServerGUIs", version = "1.0.0", description = "Allows you to set up some custom guis with the help of a Resource Pack.",
		url = "https://happyzleaf.com/", authors = {"happyzleaf"})
public class ServerGUIs {
	public static PluginContainer container;
	
	@Listener
	public void init(GameInitializationEvent event) {
		container = Sponge.getPluginManager().getPlugin("serverguis").get();
		Keys.register();
	}
	
	@Listener
	public void onInteractBlock(InteractBlockEvent.Secondary.MainHand event) {
		if (event.getSource() instanceof Player) {
			Player player = (Player) event.getSource();
			ItemStack is = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
			if (is != null && is.getType().equals(ItemTypes.STICK)) {
				TileEntity te = event.getTargetBlock().getLocation().get().getTileEntity().orElse(null);
				if (te == null) {
					//FUCK
					Gui gui = .get(Keys.GUI).orElse(null);
					if (gui == null) {
						player.sendMessage(Text.of(TextColors.GREEN, "Successfully added data to the block."));
					}
				}
			}
		}
	}
}
