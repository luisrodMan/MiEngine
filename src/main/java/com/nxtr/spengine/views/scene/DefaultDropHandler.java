package com.nxtr.spengine.views.scene;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.miengine.scene.GameObject;
import com.ngeneration.miengine.scene.SpriteRenderer;
import com.ngeneration.miengine.scene.SpriteResource;
import com.nxtr.spengine.MiEngine;
import com.nxtr.spengine.project.EngineResourceItem;

public class DefaultDropHandler implements DropHandler<EngineResourceItem, GameObject> {

	public DefaultDropHandler() {
	}

	@Override
	public FComponent getDragComponent() {
		return null;
	}

	@Override
	public GameObject onAcepted(EngineResourceItem resourceItem) {
		GameObject gameObject = new GameObject();
		switch (resourceItem.getResource().getExt()) {
		case "png":
			gameObject.setName(resourceItem.getResourceItem().getName());
			var spriteRenderer = new SpriteRenderer();
			spriteRenderer.sprite = (SpriteResource) resourceItem.getResourceItem();
			gameObject.addComponent(spriteRenderer);
			break;
		case "scn":
			gameObject = MiEngine.getProject().load(Integer.parseInt(resourceItem.getResource().getId()));
			break;
		}

		return gameObject;
	}

}
