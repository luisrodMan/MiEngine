package com.nxtr.spengine.views.inspector.resolver;

import com.nxtr.easymng.hearachy.Item;

public abstract class AbstractContent<T> implements Content<T> {

	private Item item;

	public AbstractContent(Item item) {
		this.item = item;
	}

	@Override
	public Item getItem() {
		return item;
	}

}
