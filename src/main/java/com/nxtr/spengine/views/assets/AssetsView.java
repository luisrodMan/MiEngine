package com.nxtr.spengine.views.assets;

import com.ngeneration.furthergui.FComponent;
import com.nxtr.easymng.ViewAdapter;

public class AssetsView extends ViewAdapter {

	private AssetsComponent component = new AssetsComponent();

	public AssetsView() {
		super("Resources");
	}


	@Override
	public FComponent getComponent() {
		return component.getComponent();
	}
}
