package com.nxtr.spengine.views.inspector.resolver;

import com.nxtr.easymng.hearachy.Item;

public interface Content<T> {

	Item getItem();

	T getContent();

	void release();

}
