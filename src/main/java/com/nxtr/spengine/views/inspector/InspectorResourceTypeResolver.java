package com.nxtr.spengine.views.inspector;

import com.ngeneration.furthergui.FComponent;
import com.nxtr.easymng.hearachy.Item;
import com.nxtr.spengine.views.inspector.resolver.ObjectResolverProvider;

public class InspectorResourceTypeResolver extends ObjectResolverProvider<FComponent, InspectorContent> {

	public InspectorResourceTypeResolver(Class<? extends Item> type) {
		super(type);
	}

	@Override
	public InspectorContent resolve(Item item) {
		return null;
	}

}
