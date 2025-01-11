package com.nxtr.spengine.views.scene;

import com.ngeneration.furthergui.FComponent;
import com.nxtr.easymng.ViewAdapter;

public class Scene2DView extends ViewAdapter {

	private SceneHolder sceneHolder;
	private Canvas2D component = new Canvas2D();

	public Scene2DView() {
		this(null);
	}

	public Scene2DView(SceneHolder scene) {
		super(scene == null ? "null xd" : scene.getScene().getName());
		this.sceneHolder = scene;
	}

	@Override
	public FComponent getComponent() {
		return component;
	}

	public SceneHolder getSceneHolder() {
		return sceneHolder;
	}

}
