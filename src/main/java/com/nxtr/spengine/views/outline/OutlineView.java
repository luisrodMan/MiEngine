package com.nxtr.spengine.views.outline;

import com.ngeneration.furthergui.FComponent;
import com.nxtr.easymng.Bundle;
import com.nxtr.easymng.View;
import com.nxtr.easymng.ViewAdapter;
import com.nxtr.easymng.view.ViewManager;
import com.nxtr.easymng.view.ViewManagerAdapter;
import com.nxtr.spengine.views.scene.Scene2DView;

public class OutlineView extends ViewAdapter {

	private OutlineComponent component = new OutlineComponent();

	public OutlineView() {
		super("Outline");
	}

	@Override
	public FComponent getComponent() {
		return component.getComponent();
	}

	@Override
	public void onAttached(Bundle bundle) {
		View view = bundle.getApplication().getViewManager().getActiveViews().stream().filter(this::canHandle).findAny()
				.orElse(null);
		if (view != null)
			component.setView((Scene2DView) view);
		bundle.getApplication().getViewManager().addViewManagerListener(new ViewManagerAdapter() {
			@Override
			public void onViewFocused(ViewManager viewMng) {
				if (canHandle(viewMng.getFocusedView())) {
					component.setView((Scene2DView) viewMng.getFocusedView());
				}
			}
		});
	}

	private boolean canHandle(View view) {
		return view instanceof Scene2DView;
	}

}
