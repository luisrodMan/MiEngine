package com.nxtr.spengine.views.inspector.controls;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FLabel;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.ImageIcon;
import com.ngeneration.furthergui.layout.BorderLayout;

public class FCollapsableComponent extends FPanel {

	private FComponent headerComponent;
	private FComponent arrow;

	public FCollapsableComponent(ImageIcon icon, FComponent header, FComponent content) {
		super(new BorderLayout());
		arrow = new FLabel(">");

		FPanel head = new FPanel(new BorderLayout());
		head.setBackground(Color.GRAY);
		head.add(arrow, BorderLayout.WEST);
		head.add(headerComponent = header, BorderLayout.CENTER);

		add(head, BorderLayout.NORTH);
		add(content);
	}

	public FComponent getHeaderComponent() {
		return headerComponent;
	}

}
