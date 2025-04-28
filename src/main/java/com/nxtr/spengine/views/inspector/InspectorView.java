package com.nxtr.spengine.views.inspector;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ngeneration.furthergui.FComponent;
import com.nxtr.easymng.ViewAdapter;
import com.nxtr.easymng.hearachy.Item;
import com.nxtr.easymng.workspace.Resource;
import com.nxtr.spengine.MiEngine;
import com.nxtr.spengine.views.inspector.object.GameObjectContent;
import com.nxtr.spengine.views.inspector.resolver.ContentManager;
import com.nxtr.spengine.views.inspector.resolver.ContentProvider;
import com.nxtr.spengine.views.scene.GameObjectItem;

public class InspectorView extends ViewAdapter {

	private static final Logger logger = LoggerFactory.getLogger(InspectorView.class);

	public static final String ID = "INSPECTOR_";

	private InspectorContent content;
	private ContentManager<FComponent, InspectorContent, ContentProvider<FComponent, InspectorContent>> contentManager = new ContentManager<>();

	private InspectorComponent component = new InspectorComponent();

	public InspectorView() {
		super(ID, "Inspector");
		// default resolvers
		contentManager.addResolver(new InspectorTypeResolver(GameObjectItem.class) {
			@Override
			public InspectorContent resolve(Item item) {
				return new GameObjectContent(InspectorView.this, (GameObjectItem) item);
			}
		});
		addResolver("png", // can contain all resources here? text files etc
				item -> new ResourceComponent(item.getRelativePath(), MiEngine.getProject().getResourceIndexer()));
	}

	private void addResolver(String string, Function<Resource, FComponent> function) {
		getContentManager().addResolver(new InspectorResourceResolver(string, function));
	}

	@Override
	public FComponent getComponent() {
		return component.getComponent();
	}

	public ContentManager<FComponent, InspectorContent, ContentProvider<FComponent, InspectorContent>> getContentManager() {
		return contentManager;
	}

	public void setContent(Item item) {
		if (content != null && content.getItem() == item)
			return;
		if (content != null)
			content.release();
		content = null;
		if (item != null)
			content = contentManager.getContent(item);
		component.setContent(content == null ? null : content.getContent());
	}

	public Item getItem() {
		return content == null ? null : content.getItem();
	}

}
