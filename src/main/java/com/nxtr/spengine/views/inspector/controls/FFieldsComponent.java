package com.nxtr.spengine.views.inspector.controls;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FLabel;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.layout.GridBagConstraints;
import com.ngeneration.furthergui.layout.GridBagLayout;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.Padding;
import com.nxtr.spengine.views.inspector.FieldDescriptor;
import com.nxtr.spengine.views.inspector.handlers.FieldHandler;
import com.nxtr.spengine.views.inspector.handlers.PropertyHandler;

public class FFieldsComponent extends FPanel {

	private Consumer<FieldHandler> listener;
	private Map<String, PropertyHandler> fields;

	public FFieldsComponent(Map<FieldDescriptor, PropertyHandler> map) {
		GridBagLayout layout = new GridBagLayout(map.size() + 2, 2);
		setPadding(new Padding(0, 0, 0, 5));
		setLayout(layout);
		this.fields = map.entrySet().stream().collect(Collectors.toMap(d -> d.getKey().getName(), d -> d.getValue()));

		int[] row = new int[] { 0 };
		int[] maxWidth = new int[] { 0 };

		map.entrySet().forEach(entry -> {

			var field = entry.getKey();
			var handler = entry.getValue();

			handler.setChangeListener(event -> {
				if (listener != null)
					listener.accept(new FieldHandler(entry.getKey().getName(), handler));
			});

			FLabel label = new FLabel(field.getName());
			maxWidth[0] = Math.max(maxWidth[0], label.getPrefferedSize().getWidth());
			var constraints = new GridBagConstraints();
			constraints.row = row[0];
			constraints.anchor = GridBagConstraints.LEFT;
			constraints.weightH = 0.1f;
			constraints.margin = new Padding(4, 0, 0, constraints.row == 0 ? 5 : 0);
			add(label, constraints);

			constraints = new GridBagConstraints();
			constraints.col = 1;
			constraints.row = row[0]++;
			constraints.weightH = 1f;
			constraints.fillHorizontal = true;
			constraints.margin = new Padding(0, 1);
			var inputComponent = handler.getComponent();
			add(inputComponent, constraints);
		});

		// invisible component
		FComponent comp = new FPanel();
		comp.setBackground(Color.TRANSLUCENT);
		comp.setPrefferedSize(new Dimension(maxWidth[0], 10));
		var constraints = new GridBagConstraints();
		constraints.col = 0;
		constraints.cols = 2;
		constraints.row = row[0]++;
		add(comp, constraints);
	}

	public void setListener(Consumer<FieldHandler> listener) {
		this.listener = listener;
	}

	public void updateProperty(String propertyName, Object value) {
		var field = fields.get(propertyName);
		if (field != null)
			field.setValue(value);
	}

}
