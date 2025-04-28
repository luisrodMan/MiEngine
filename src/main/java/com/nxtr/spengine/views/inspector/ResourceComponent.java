package com.nxtr.spengine.views.inspector;

import com.ngeneration.furthergui.FButton;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.FScrollPane;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.miengine.util.indexer.ResourceIndexer;
import com.ngeneration.miengine.util.indexer.ResourceTemplate;
import com.ngeneration.miengine.util.indexer.templates.TextureResource;
import com.nxtr.spengine.views.inspector.controls.FFieldsComponent;

public class ResourceComponent extends FPanel {

	public ResourceComponent(String resourcePath, ResourceIndexer indexer) {
		super(new BorderLayout());
		setPadding(new Padding(5));
		// parse template and show

		var id = indexer.getIndex(resourcePath);
		if (id < 1)
			id = indexer.register(resourcePath);

		if (id < 1)
			return;

		ResourceTemplate template = indexer.getTemplate(id, TextureResource.class);
		add(new FScrollPane(parse(template)));

		FPanel bottom = new FPanel(new FlowLayout(FlowLayout.RIGHT));
		FButton saveBtn = new FButton("Save");
		saveBtn.addActionListener(e -> {
			template.apply();
			indexer.persistTemplate(template);
		});
		bottom.add(saveBtn);
		add(bottom, BorderLayout.SOUTH);
	}

	public FComponent parse(Object component) {
//		FPanel container = new FPanel(new GridBagLayout(0 + gameObjectItem.getObject().getComponentCount() * 2, 1));
//		setPadding(new Padding(5));

		// Object Name
		// components
		// add btn

		// parse template and show
//		int[] row = new int[] { 0 };
//		gameObjectItem.getObject().getComponents().forEach(component -> {

//		FPanel titleContainer = new FPanel(new GridBagLayout(1, 2));
//		titleContainer.setBackground(Color.TRANSLUCENT);
//		FCheckbox check = new FCheckbox(EngineUtil.toTitle(component.getClass().getSimpleName()));
//		FPanel optionsPanel = new FPanel();
//		var c = new GridBagConstraints();
//		c.col = 0;
//		titleContainer.add(check, c);
//		c = new GridBagConstraints();
//		c.col = 1;
//		c.weightH = 0;
//		titleContainer.add(optionsPanel, c);

		var map = InspectableObjectParser.parseObject(component);
		var container1 = new FFieldsComponent(map);
		// container1.setListener(h -> consumer.accept(component, h));
//		row[0] += addSection(new FCollapsableComponent(null, titleContainer, container1), row[0]);
//		});

		return container1;
	}
}