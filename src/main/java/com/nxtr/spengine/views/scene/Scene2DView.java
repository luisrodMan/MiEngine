package com.nxtr.spengine.views.scene;

import com.ngeneration.furthergui.FComponent;
import com.nxtr.easymng.ViewAdapter;

public class Scene2DView extends ViewAdapter {

	private SceneHolder sceneHolder;
	private Canvas2D component = new Canvas2D();

	public Scene2DView() {
	}

	public Scene2DView(SceneHolder scene) {
		this.sceneHolder = scene;
	}

	@Override
	public String getTitle() {
		return sceneHolder.getScene().getName();
	}

	@Override
	public FComponent getComponent() {
		return component;
	}

	public SceneHolder getSceneHolder() {
		return sceneHolder;
	}

}
