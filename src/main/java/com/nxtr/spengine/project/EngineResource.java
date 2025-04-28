package com.nxtr.spengine.project;

import java.io.File;

import com.ngeneration.miengine.util.indexer.ResourceTemplate;
import com.nxtr.easymng.hearachy.Item;
import com.nxtr.easymng.workspace.SimpleResource;

public class EngineResource extends SimpleResource implements EngineResourceItem {

	private ResourceTemplate template;

	public EngineResource(Item parent, File file, ResourceTemplate template) {
		super(parent, file);
		this.template = template;
		template.getItems().forEach(item1 -> {
			super.add(new DefaultEngineResourceItem(this, item1));
		});
	}

	@Override
	public String getId() {
		return "" + template.getId();
	}

	public ResourceTemplate getTemplate() {
		return template;
	}

	@Override
	public EngineResource getResource() {
		return this;
	}

	@Override
	public com.ngeneration.miengine.util.indexer.ResourceItem getResourceItem() {
		return template.getResource(0);
	}

}
