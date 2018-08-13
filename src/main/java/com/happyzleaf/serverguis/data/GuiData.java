package com.happyzleaf.serverguis.data;

import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;

public interface GuiData extends DataManipulator<GuiData, GuiData.Immutable> {
	Value<Gui> data();
	
	interface Immutable extends ImmutableDataManipulator<Immutable, GuiData> {
		ImmutableValue<Gui> data();
	}
	
	interface Builder extends DataManipulatorBuilder<GuiData, Immutable> {}
}
