package com.nxtr.spengine.views.inspector.resolver;

import com.nxtr.easymng.hearachy.Item;

public interface ContentProvider<R, T extends Content<R>> {

	Class<? extends Item>[] getTypes();

	T resolve(Item item);

}