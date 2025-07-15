package com.nxtr.spengine.views.inspector.resolver;

import java.util.function.Function;

import com.ngeneration.furthergui.FComponent;
import com.nxtr.easymng.workspace.Resource;
import com.nxtr.spengine.views.inspector.AbstractInspectorContent;
import com.nxtr.spengine.views.inspector.InspectorContent;

public class InspectorResourceResolver extends ResourceContentProvider<FComponent, InspectorContent> {

	private Function<Resource, FComponent> consumer;

	public InspectorResourceResolver(String extensions, Function<Resource, FComponent> consumer) {
		super(extensions);
		this.consumer = consumer;
	}

	@Override
	protected InspectorContent resolveResource(Resource item) {
		return new AbstractInspectorContent(item) {
			@Override
			public FComponent getContent() {
				return consumer.apply(item);
			}
		};
	}

}
