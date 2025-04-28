package com.nxtr.spengine.views.outline;

import java.util.List;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.nxtr.easymng.MarkupEditor;
import com.nxtr.easymng.View;
import com.nxtr.easymng.hearachy.HearachyComponent;
import com.nxtr.easymng.hearachy.Item;
import com.nxtr.easymng.hearachy.SelectionEvent;
import com.nxtr.easymng.hearachy.SelectionListener;
import com.nxtr.spengine.views.scene.Scene2DEditor;

class OutlineComponent {

	private FPanel panel = new FPanel(new BorderLayout());
	private HearachyComponent<Item> hearachy;
	private OutlineView outline;
	private View view;
	private com.nxtr.easymng.hearachy.SelectionListener selectionListener = event -> {
		if (this.view instanceof Scene2DEditor scene)
			hearachy.setSelection(scene.getSelectionManager().getSelection());
	};

	public OutlineComponent(OutlineView outlineView) {
		outline = outlineView;
	}

	public FComponent getComponent() {
		return panel;
	}

	HearachyComponent<Item> getHearachy() {
		return hearachy;
	}

	public void setView(MarkupEditor view) {
		if (this.view instanceof Scene2DEditor scene)
			scene.getSelectionManager().removeSelectionListener(selectionListener);
		this.view = view;
		if (this.view instanceof Scene2DEditor scene)
			scene.getSelectionManager().addSelectionListener(selectionListener);
		hearachy = new OutlineHearachy(outline, view.getRoot());
		panel.removeAll();
		hearachy.expandPath(List.of(view.getRoot()));
		panel.add(hearachy.getComponent());
		panel.revalidate();
		hearachy.getSelectionManager().addSelectionListener(new SelectionListener() {
			@Override
			public void onSelectionChanged(SelectionEvent event) {
				if (view instanceof Scene2DEditor scene) {
					scene.getSelectionManager().setSelection(hearachy.getSelection());
				}
			}
		});
		panel.revalidate();
	}

}
