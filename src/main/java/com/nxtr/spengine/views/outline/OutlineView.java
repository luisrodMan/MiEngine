package com.nxtr.spengine.views.outline;

import com.ngeneration.furthergui.FComponent;
import com.nxtr.easymng.Bundle;
import com.nxtr.easymng.MarkupEditor;
import com.nxtr.easymng.View;
import com.nxtr.easymng.ViewAdapter;
import com.nxtr.easymng.hearachy.SelectionManager;
import com.nxtr.easymng.view.ViewManager;
import com.nxtr.easymng.view.ViewManagerAdapter;
import com.nxtr.spengine.views.scene.GameObjectItem;
import com.nxtr.spengine.views.scene.PropertyChangeListener;
import com.nxtr.spengine.views.scene.PropertyEvent;

public class OutlineView extends ViewAdapter {

	public static String ID = "Outline";
	private OutlineComponent component = new OutlineComponent(this);
	private MarkupEditor currentView;

	public OutlineView() {
		super(ID, "Outline");
		GameObjectItem.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void onPropertyChanged(PropertyEvent event) {
				if (event.getComponent() == null) {
					if (event.getPropertyName().equals("active"))
						component.getHearachy().getComponent().repaint();
					else if (event.getPropertyName().equals("name"))
						component.getHearachy().revalidate();
				}
			}
		});
	}

	@Override
	public FComponent getComponent() {
		return component.getComponent();
	}

	@Override
	public void restore(Bundle bundle) {
		bundle.getApplication().getViewManager().addViewManagerListener(new ViewManagerAdapter() {
			@Override
			public void onFocusedViewChanged(ViewManager viewMng) {
				checkViews(viewMng);
			}

			public void onViewAdded(ViewManager viewMng, View view) {
				checkViews(viewMng);
			}
		});
		checkViews(bundle.getApplication().getViewManager());
	}

	private void checkViews(ViewManager viewManager) {
		if (canHandle(viewManager.getFocusedView()))
			setContent(viewManager.getFocusedView());
		else {
			View view = viewManager.getActiveViews().stream().filter(this::canHandle).findAny().orElse(null);
			if (view != null)
				setContent(view);
		}
	}

	private void setContent(View view) {
		if (view == currentView)
			return;
		currentView = (MarkupEditor) view;
		component.setView((MarkupEditor) view);
	}

	public MarkupEditor getCurrentView() {
		return currentView;
	}

	private boolean canHandle(View view) {
		return view instanceof MarkupEditor;
	}

	public SelectionManager getSelectionManager() {
		return component.getHearachy().getSelectionManager();
	}

}
