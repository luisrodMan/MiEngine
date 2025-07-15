package com.nxtr.spengine.views.scene;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.math.Point;
import com.ngeneration.miengine.scene.Component;
import com.ngeneration.miengine.scene.GameObject;
import com.ngeneration.miengine.util.EngineSerializer;
import com.nxtr.easymng.Application;
import com.nxtr.easymng.Bundle;
import com.nxtr.easymng.EditorAdapter;
import com.nxtr.easymng.MarkupEditor;
import com.nxtr.easymng.ViewException;
import com.nxtr.easymng.hearachy.Item;
import com.nxtr.easymng.hearachy.SelectionManager;
import com.nxtr.easymng.view.ViewManager;
import com.nxtr.easymng.view.ViewManagerAdapter;
import com.nxtr.easymng.workspace.Resource;
import com.nxtr.spengine.MiEngine;
import com.nxtr.spengine.views.inspector.InspectorView;
import com.nxtr.spengine.views.outline.OutlineView;

public class Scene2DEditor extends EditorAdapter implements MarkupEditor {

	private static final Logger logger = LoggerFactory.getLogger(Scene2DEditor.class);

	private GameObjectItem sceneHolder;
	private Canvas2D canvas = new Canvas2D(this);
	private DefaultDropHandler dropHandler = new DefaultDropHandler();
	private PropertyChangeListener propertyChangeListener = event -> {
		if (event.getSource() != this && getComponent().isVisible()) {

			var component = event.getComponent();
			if (event.getPropertyName() != null) {
				switch (event.getName()) {
				case GameObjectItem.COMPONENT_ADDED:
				case GameObjectItem.COMPONENT_REMOVED:
					// refresh view
					break;
				default:
					if (component != null)
						component.onPropertyUpdated(event.getPropertyName());
					break;
				}
			}
			canvas.repaintCanvas();
		}
	};


	public Scene2DEditor() {
		setScene(new GameObjectItem(null, new GameObject()));
	}

	public Scene2DEditor(Resource resource) {
		super.setResource(resource);
		try {
			setScene(openResource(resource));
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	@Override
	public FComponent getComponent() {
		return canvas;
	}

	public Item getSceneHolder() {
		return sceneHolder;
	}

	@Override
	public void save(Bundle bundle) {
		super.save(bundle);
		bundle.put("zoom", canvas.getScale());
		bundle.put("location", new Point((int) canvas.getCamera().position.x, (int) canvas.getCamera().position.y));
	}

	@Override
	public void restore(Bundle bundle) throws ViewException {
		super.restore(bundle);
		try {
			setScene(openResource(getResource()));
			canvas.setScale(bundle.getFloat("zoom", 1));
			var loc = bundle.getPoint("location", new Point());
			canvas.getCamera().position.set(loc.getX(), loc.getY(), 0);
		} catch (IOException e) {
			throw new ViewException(bundle.getString("title"), getResource().getRelativePath());
		}
	}

	private void setScene(GameObjectItem openResource) {
		// remove when view closed!!
		getSelectionManager().addSelectionListener(listener -> updateInspectorForGameObject());
		Application.getInstance().getViewManager().addViewManagerListener(new ViewManagerAdapter() {
			@Override
			public void onFocusedViewChanged(ViewManager viewMng) {
				if (viewMng.getFocusedView() == Scene2DEditor.this
						|| (viewMng.getFocusedView() instanceof OutlineView out
								&& out.getCurrentView() == Scene2DEditor.this))
					updateInspectorForGameObject();
			}
		});

		GameObjectItem.addPropertyChangeListener(propertyChangeListener);
		sceneHolder = openResource;

		canvas.removeDropHandler("png", dropHandler);
		canvas.addDropHandler("png", dropHandler);
		canvas.addDropHandler("scn", dropHandler);
	}

	private void updateInspectorForGameObject() {
		if (Application.getInstance().getViewManager().getViewById(InspectorView.ID) instanceof InspectorView view) {
			if (getSelectionManager().getSingleSelection() instanceof GameObjectItem item)
				view.setContent(item);
			else
				view.setContent(null);
		}
	}

	private GameObjectItem openResource(Resource resource) throws FileNotFoundException, IOException {
		var prj = MiEngine.getProject();
		return new GameObjectItem(null,
				EngineSerializer.deserialize(getResource().getFile(), prj.getResourceIndexer(), prj.getClassLoader()));
	}

	@Override
	public Item getRoot() {
		return sceneHolder;
	}

	public SelectionManager getSelectionManager() {
		return canvas.getSelectionManager();
	}

	void onPropertyUpdated(GameObjectItem object, Component component, String propertyName, Object value) {
		GameObjectItem.fireEvent(
				new PropertyEvent((MiEngine) Application.getInstance(), this, object, component, propertyName, value));
	}

	public Canvas2D getCanvas() {
		return canvas;
	}

	public String getContent() {
		return EngineSerializer.serialize(sceneHolder.getObject());
	}

	@Override
	public void doSave() {
		try {
			getResource().saveAsText((String) getContent());
			logger.info("File saved: " + getResource().getFile().getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
