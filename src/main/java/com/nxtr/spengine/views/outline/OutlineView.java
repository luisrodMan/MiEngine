package com.nxtr.spengine.views.outline;

import com.ngeneration.furthergui.FComponent;
import com.nxtr.easymng.Application;
import com.nxtr.easymng.Bundle;
import com.nxtr.easymng.View;
import com.nxtr.easymng.ViewAdapter;
import com.nxtr.spengine.views.scene.Scene2DView;

public class OutlineView extends ViewAdapter {

	private OutlineComponent component = new OutlineComponent();

	public OutlineView() {

	}

	@Override
	public String getTitle() {
		return "Outline";
	}

	@Override
	public FComponent getComponent() {
		return component.getComponent();
	}

	@Override
	public void onAttached(Bundle bundle) {
		View view = bundle.getApplication().getViewManager().getActiveViews().stream().filter(this::canHandle)
				.findAny().orElse(null);
		if (view != null)
			component.setView(getView(view));
		bundle.getApplication().getViewManager().addViewListener(new ViewAdapterListener() {
			public void onViewFocused(View focusedView) {
				if (canHandle(focusedView)) {
					component.setView(getView(focusedView));
				}
			}
		});
	}

	private View getView(View view) {
		if (view instanceof Scene2DView) {

		}
		return null;
	}

	private boolean canHandle(View view) {
		return view instanceof Scene2DView;
	}

}
