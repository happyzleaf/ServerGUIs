package com.happyzleaf.serverguis.data;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

import static com.happyzleaf.serverguis.Keys.GUI;

public class GuiDataImpl extends AbstractSingleData<Gui, GuiData, GuiData.Immutable> implements GuiData {
	private static final int CONTENT_VERSION = 1;
	
	protected GuiDataImpl(Gui data) {
		super(data, GUI);
	}
	
	@Override
	public Value<Gui> data() {
		return getValueGetter();
	}
	
	@Override
	protected Value<Gui> getValueGetter() {
		return Sponge.getRegistry().getValueFactory().createValue(GUI, this.getValue(), new Gui());
	}
	
	@Override
	public Optional<GuiData> fill(DataHolder dataHolder, MergeFunction overlap) {
		Optional<GuiDataImpl> optData = dataHolder.get(GuiDataImpl.class);
		if (optData.isPresent()) {
			GuiDataImpl data = optData.get();
			GuiDataImpl finalData = overlap.merge(this, data);
			setValue(finalData.getValue());
		}
		return Optional.of(this);
	}
	
	@Override
	public Optional<GuiData> from(DataContainer container) {
		return from((DataView) container);
	}
	
	public Optional<GuiData> from(DataView view) {
		if (view.contains(GUI.getQuery())) {
			setValue((Gui) view.get(GUI.getQuery()).get());
			return Optional.of(this);
		} else {
			return Optional.empty();
		}
	}
	
	@Override
	public GuiData copy() {
		return new GuiDataImpl(getValue());
	}
	
	@Override
	public DataContainer toContainer() {
		return super.toContainer().set(this.usedKey.getQuery(), getValue());
	}
	
	@Override
	public GuiData.Immutable asImmutable() {
		return new Immutable(getValue());
	}
	
	@Override
	public int getContentVersion() {
		return CONTENT_VERSION;
	}
	
	public static class Immutable extends AbstractImmutableSingleData<Gui, GuiData.Immutable, GuiData> implements GuiData.Immutable {
		private ImmutableValue<Gui> immutableValue;
		
		protected Immutable(Gui data) {
			super(data, GUI);
			immutableValue = Sponge.getRegistry().getValueFactory().createValue(GUI, data, new Gui()).asImmutable();
		}
		
		@Override
		public ImmutableValue<Gui> data() {
			return getValueGetter();
		}
		
		@Override
		protected ImmutableValue<Gui> getValueGetter() {
			return immutableValue;
		}
		
		@Override
		public DataContainer toContainer() {
			return super.toContainer().set(GUI.getQuery(), getValue());
		}
		
		@Override
		public GuiData asMutable() {
			return new GuiDataImpl(getValue());
		}
		
		@Override
		public int getContentVersion() {
			return CONTENT_VERSION;
		}
	}
	
	public static class Builder extends AbstractDataBuilder<GuiData> implements GuiData.Builder {
		public Builder() {
			super(GuiData.class, CONTENT_VERSION);
		}
		
		@Override
		public GuiDataImpl create() {
			return new GuiDataImpl(new Gui()); //default
		}
		
		@Override
		public Optional<GuiData> createFrom(DataHolder dataHolder) {
			return create().fill(dataHolder);
		}
		
		@Override
		protected Optional<GuiData> buildContent(DataView container) throws InvalidDataException {
			return create().from(container);
		}
	}
}
