package com.nxtr.spengine.views.scene;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.ngeneration.miengine.scene.Component;
import com.ngeneration.miengine.scene.GameObject;
import com.nxtr.easymng.hearachy.Item;
import com.nxtr.easymng.workspace.SimpleItem;
import com.nxtr.spengine.MiEngine;

public class GameObjectItem extends SimpleItem {

	public static final String COMPONENT_ADDED = "componentAdded";
	public static final String COMPONENT_REMOVED = "componentRemoved";
	public static final String COMPONENT_CHANGED = "componentChanged";

	private GameObject object;
	private static Set<PropertyChangeListener> listeners = new HashSet<>();

	public static void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
		listeners.add(propertyChangeListener);
	}

	public static void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
		listeners.remove(propertyChangeListener);
	}

	public static void fireEvent(PropertyEvent propertyEvent) {
		listeners.forEach(l -> l.onPropertyChanged(propertyEvent));
	}

	public GameObjectItem(GameObject object) {
		this(null, object);
	}

	public GameObjectItem(GameObjectItem parent, GameObject object) {
		super(parent);
		setGameObject(object);
	}

	public void setGameObject(GameObject object) {
		if (object == null)
			throw new RuntimeException("xd");
		int targetIndex = 0;
		if (this.object != null && this.object.getParentGameObject() != null) {
			targetIndex = this.object.getParentGameObject().getChildren().indexOf(this.object);
		}
		removeAllChildren();
		var children = new LinkedList<GameObjectItem>();
		object.getChildren().forEach(c -> {
			if (c.getParentGameObject() != null)
				c.getParentGameObject().remove(c);
			children.add(setUpChildren(this, c));
		});

		var oldParent = this.object != null ? this.object.getParentGameObject() : null;
		this.object = object;
		add(children);

		if (oldParent != null) {
			oldParent.remove(targetIndex);
			oldParent.addChild(targetIndex, object);
		}
	}

	private GameObjectItem setUpChildren(GameObjectItem parent, GameObject object) {
		return new GameObjectItem(parent, object);
	}

	public GameObject getObject() {
		return object;
	}

	@Override
	public String getName() {
		return object.getName();
	}

	@Override
	protected void removeChildren(List<Item> items) {
		items.forEach(i -> {
			getObject().remove(((GameObjectItem) i).getObject());
		});
		super.removeChildren(items);
	}

	@Override
	public void add(int index, List<? extends Item> items) {
		if (!items.stream().allMatch(i -> i instanceof GameObjectItem || i.getParentItem() != null))
			throw new RuntimeException("Invalid parent");
		super.add(index, items);
		var list = new LinkedList<GameObject>();
		items.forEach(i -> {
			var ob = ((GameObjectItem) i).getObject();
			list.add(ob);
		});
		object.addChildren(index, list);
	}

	public void rename(String value) {
		var old = getName();
		object.setName(value);
		firePropertyChange(NAME_PROPERTY, old);
	}

//	public void removeGameObject(GameObjectItem toDelete) {
//		remove(toDelete);
//	}

	public void addComponent(Component component) {
		getObject().addComponent(component);
		PropertyEvent event = new PropertyEvent(MiEngine.getInstance(), null, this, component, COMPONENT_ADDED, null);
		listeners.stream().forEach(c -> c.onPropertyChanged(event));
	}

	public void removeComponent(Component component) {
		getObject().removeComponent(component);
		PropertyEvent event = new PropertyEvent(MiEngine.getInstance(), null, this, component, COMPONENT_REMOVED, null);
		listeners.stream().forEach(c -> c.onPropertyChanged(event));
	}

//	public void fireComponentsChanged() {
//		firePropertyChange(COMPONENT_CHANGED);
//	}

	public Collection<GameObjectItem> getObjectItems() {
		return getItems().stream().map(GameObjectItem.class::cast).toList();
	}

//	public GameObject duplicate() {
////		EngineSerializer.se
////		GameObject object = new 
////		for (var des : InspectableObjectParser.parseObject(getObject())) {
////			
////		}
//	}

}
