package com.nxtr.spengine.project;

import com.ngeneration.miengine.scene.Component;

public class ComponentWraper {

	private String qualifiedName;
	private ClassLoader cl;

	public ComponentWraper(String qualifiedName, ClassLoader cl) {
		this.qualifiedName = qualifiedName;
		this.cl = cl;
	}

	public String getQualifiedName() {
		return qualifiedName;
	}

	public String getName() {
		var path = getQualifiedName().split("\\.");
		return path[path.length - 1];
	}

	public Component newInstance() {
		try {
			if (cl == null)
				return (Component) Class.forName(qualifiedName).newInstance();
			return (Component) cl.loadClass(getQualifiedName()).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isInstance(Object object) {
		return object.getClass().getName().equals(getQualifiedName());
	}

}
