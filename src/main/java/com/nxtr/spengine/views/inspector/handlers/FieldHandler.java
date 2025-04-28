package com.nxtr.spengine.views.inspector.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import com.ngeneration.miengine.scene.Transform;

import lombok.Getter;

public class FieldHandler {

	private PropertyHandler handler;
	@Getter
	private String name;

	public FieldHandler(String name, PropertyHandler handler) {
		this.name = name;
		this.handler = handler;
	}

	public void apply(Object object) {
		try {
			var field = object.getClass().getField(name);
			field.setAccessible(true);
			if (Modifier.isFinal(field.getModifiers()))
				handler.handleFinal(field.get(object), handler.getValue());
			else
				field.set(object, handler.getValue());
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			var name = "set" + this.name.substring(0, 1).toUpperCase() + this.name.substring(1);
			try {
				var cl = handler.getValue().getClass();
				if (cl.getSimpleName().endsWith("Boolean"))
					cl = boolean.class;
				var method = object.getClass().getMethod(name, cl);
				if (object instanceof Transform) {
					if (this.name.equals("location"))
						method = object.getClass().getMethod("setLocalLocation", cl);
					else if (this.name.equals("scale"))
						method = object.getClass().getMethod("setLocalScale", cl);
					else if (this.name.equals("rotation"))
						method = object.getClass().getMethod("setLocalRotation", cl);
				}

				method.invoke(object, handler.getValue());
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException
					| InvocationTargetException e1) {
				e1.printStackTrace();
			}
		}
	}

	public Object getValue() {
		return handler.getValue();
	}

}
