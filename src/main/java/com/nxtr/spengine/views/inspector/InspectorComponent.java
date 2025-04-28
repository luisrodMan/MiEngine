package com.nxtr.spengine.views.inspector;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.layout.BorderLayout;

public class InspectorComponent {

	private FPanel panel = new FPanel(new BorderLayout());
	private FComponent component;

	public FComponent getComponent() {
		return panel;
	}

	public void setContent(FComponent component) {
		this.component = component;
		panel.removeAll();
		if (component != null)
			panel.add(component);
		panel.revalidate();
	}

	public FComponent getContent() {
		return component;
	}

}
