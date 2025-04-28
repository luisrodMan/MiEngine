package com.nxtr.spengine.project;

import com.ngeneration.miengine.util.indexer.ResourceItem;
import com.nxtr.easymng.workspace.SimpleItem;

public class DefaultEngineResourceItem extends SimpleItem implements EngineResourceItem {

	private EngineResource engineResource;
	private ResourceItem resourceItem;

	public DefaultEngineResourceItem(EngineResource engineResource, ResourceItem resourceItem) {
		super(engineResource, "" + resourceItem.getId());
		this.engineResource = engineResource;
		this.resourceItem = resourceItem;
	}

	@Override
	public String getName() {
		return resourceItem.getName();
	}

	@Override
	public EngineResource getResource() {
		return engineResource;
	}

	@Override
	public ResourceItem getResourceItem() {
		return resourceItem;
	}

}
