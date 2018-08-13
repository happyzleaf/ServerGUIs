package com.happyzleaf.serverguis;

import com.google.common.reflect.TypeToken;
import com.happyzleaf.serverguis.data.Gui;
import com.happyzleaf.serverguis.data.GuiData;
import com.happyzleaf.serverguis.data.GuiDataImpl;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;

/**
 * @author happyzleaf
 * @since 13/08/2018
 */
public class Keys {
	public static Key<Value<Gui>> GUI = DummyObjectProvider.createExtendedFor(Key.class, "GUI");
	
	public static void register() {
		GUI = Key.builder()
				.type(new TypeToken<Value<Gui>>() {})
				.id("gui")
				.name("Gui")
				.query(DataQuery.of("gui"))
				.build();
		DataRegistration.builder()
				.dataName("Gui")
				.manipulatorId("gui")
				.dataClass(GuiData.class)
				.dataImplementation(GuiDataImpl.class)
				.immutableClass(GuiData.Immutable.class)
				.immutableImplementation(GuiDataImpl.Immutable.class)
				.builder(new GuiDataImpl.Builder())
				.buildAndRegister(ServerGUIs.container);
	}
}
