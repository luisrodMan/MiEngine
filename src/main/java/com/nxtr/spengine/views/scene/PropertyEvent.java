package com.nxtr.spengine.views.scene;

import com.ngeneration.miengine.scene.Component;
import com.nxtr.easymng.View;
import com.nxtr.spengine.MiEngine;
import com.nxtr.spengine.views.Event;

import lombok.Getter;

@Getter
public class PropertyEvent extends Event {

	private GameObjectItem object;
	private Component component;
	private String propertyName;
	private Object value;

	public PropertyEvent(MiEngine engine, View source, GameObjectItem object, Component component, String propertyName,
			Object value) {
		super(engine, source);
		this.object = object;
		this.component = component;
		this.propertyName = propertyName;
		this.value = value;
	}

	public String getName() {
		return propertyName;
	}

}
