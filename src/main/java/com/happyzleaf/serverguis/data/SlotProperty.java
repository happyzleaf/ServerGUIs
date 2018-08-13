package com.happyzleaf.serverguis.data;

import org.spongepowered.api.data.Property;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;

import java.util.Iterator;
import java.util.List;

/**
 * @author happyzleaf
 * @since 13/08/2018
 */
public class SlotProperty extends AbstractInventoryProperty<String, List<ItemStack>> {
	public SlotProperty(List<ItemStack> slots) {
		super(slots);
	}
	
	public SlotProperty(List<ItemStack> slots, Operator op) {
		super(slots, op);
	}
	
	public void setSlots(List<ItemStack> slots) {
		this.value = slots;
	}
	
	@Override
	public int compareTo(Property<?, ?> o) {
		if (getValue() == null) {
			return o.getValue() == null ? 0 : 1;
		}
		
		if (o.getValue() instanceof List) {
			Iterator<ItemStack> slots = getValue().iterator();
			Iterator compared = ((Iterable) o.getValue()).iterator();
			if (slots.hasNext() && compared.hasNext()) {
				if (!slots.next().equals(compared.next())) {
					return 1;
				}
			} else if (!slots.hasNext() && !compared.hasNext()) {
				return 0;
			}
			return 1;
		}
		
		return 1;
	}
}
