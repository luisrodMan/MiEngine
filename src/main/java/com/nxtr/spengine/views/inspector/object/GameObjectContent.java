package com.nxtr.spengine.views.inspector.object;

import java.util.List;
import java.util.function.BiConsumer;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.miengine.scene.Component;
import com.nxtr.easymng.hearachy.Item;
import com.nxtr.easymng.hearachy.ItemListener;
import com.nxtr.spengine.MiEngine;
import com.nxtr.spengine.views.inspector.InspectorContent;
import com.nxtr.spengine.views.inspector.InspectorView;
import com.nxtr.spengine.views.inspector.handlers.FieldHandler;
import com.nxtr.spengine.views.scene.GameObjectItem;
import com.nxtr.spengine.views.scene.PropertyChangeListener;
import com.nxtr.spengine.views.scene.PropertyEvent;

public class GameObjectContent implements InspectorContent {

	private InspectorView inspector;
	private GameObjectItem item;
	private GameObjectComponent component;

	public GameObjectContent(InspectorView inspector, GameObjectItem item) {
		this.inspector = inspector;
		this.item = item;
		item.addChangeListener(itemChangedListener);
	}

	@Override
	public FComponent getContent() {
		if (this.component != null)
			return this.component;
		BiConsumer<Object, FieldHandler> listener = (instance, handler) -> {
			handler.apply(instance);
			GameObjectItem.fireEvent(new PropertyEvent(MiEngine.getInstance(), inspector, item,
					(Component) (instance instanceof Component ? instance : null), handler.getName(),
					handler.getValue()));
		};
		component = new GameObjectComponent(MiEngine.getProject(), item, listener);
		GameObjectItem.addPropertyChangeListener(propertyChangeListener);
		return component;
	}

	@Override
	public Item getItem() {
		return item;
	}

	@Override
	public void release() {
		item.removeChangeListener(itemChangedListener);
		GameObjectItem.removePropertyChangeListener(propertyChangeListener);
	}

	private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
		@Override
		public void onPropertyChanged(PropertyEvent event) {
			if (event.getSource() != inspector) {
				component.updateProperty(event.getObject().getObject(), event.getComponent(), event.getPropertyName(),
						event.getValue());
			}
		}
	};

	private ItemListener itemChangedListener = new ItemListener() {

		@Override
		public void onWorkspaceItemRemoved(Item path, List<Item> removed) {
		}

		@Override
		public void onWorkspaceItemAdded(Item path, int index, List<? extends Item> items) {
		}

		@Override
		public void onPropertyChanged(com.nxtr.easymng.hearachy.PropertyEvent event) {

		}
	};

}
