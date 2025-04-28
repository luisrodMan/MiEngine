package com.nxtr.spengine.views.inspector;

import com.ngeneration.furthergui.FComponent;
import com.nxtr.easymng.hearachy.Item;
import com.nxtr.spengine.views.inspector.resolver.AbstractContent;

public abstract class AbstractInspectorContent extends AbstractContent<FComponent> implements InspectorContent {

	public AbstractInspectorContent(Item item) {
		super(item);
	}

	@Override
	public void release() {

	}

}
