package com.nxtr.spengine.views.inspector;

import com.ngeneration.furthergui.FComponent;
import com.nxtr.easymng.ViewAdapter;

public class InspectorView extends ViewAdapter {

	private InspectorComponent component = new InspectorComponent();

	public InspectorView() {
		super("Inspector");
	}

	@Override
	public FComponent getComponent() {
		return component.getComponent();
	}
}
