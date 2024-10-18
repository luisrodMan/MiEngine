package com.nxtr.spengine.views.inspector;

import com.ngeneration.furthergui.FComponent;
import com.nxtr.easymng.ViewAdapter;

public class InspectorView extends ViewAdapter {

	private InspectorComponent component = new InspectorComponent();

	@Override
	public String getTitle() {
		return "Inspector";
	}

	@Override
	public FComponent getComponent() {
		return component.getComponent();
	}
}
