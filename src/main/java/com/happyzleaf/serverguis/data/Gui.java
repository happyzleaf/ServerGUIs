package com.happyzleaf.serverguis.data;

import com.flowpowered.math.vector.Vector3i;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author happyzleaf
 * @since 13/08/2018
 */
@ConfigSerializable
public class Gui {
	private static int lastId = 0;
	
	public int id = lastId++;;
	
	@Setting
	public String dimensionId;
	
	@Setting
	public Vector3i position;
	
	@Setting
	public ItemStackSnapshot upperGui = null;
	
	@Setting
	public ItemStackSnapshot lowerGui = null;
	
	@Setting
	public Map<Integer, ItemStackSnapshot> items = new HashMap<>(); //Yes, i would have preferred an array but they're not natively of ninja.leaping and i don't want to mess with it
	
	@Setting
	public boolean showPlayerInventory = true;
}
