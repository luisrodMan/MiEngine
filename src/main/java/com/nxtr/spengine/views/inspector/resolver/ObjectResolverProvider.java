package com.nxtr.spengine.views.inspector.resolver;

import com.nxtr.easymng.hearachy.Item;

public abstract class ObjectResolverProvider<T1, T2 extends Content<T1>> implements ContentProvider<T1, T2> {

	private Class<? extends Item> type;

	public ObjectResolverProvider(Class<? extends Item> type) {
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Item>[] getTypes() {
		return new Class[] { type };
	}
//
//	@Override
//	public void release(InspectorView inspector, Item item) {
//		
//	}

}
