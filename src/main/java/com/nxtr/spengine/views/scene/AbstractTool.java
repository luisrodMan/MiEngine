package com.nxtr.spengine.views.scene;

import com.ngeneration.furthergui.event.KeyEvent;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.miengine.math.MathUtils;

public class AbstractTool implements Tool {

	private float zoomUnit = 0.1f;

	public void mouseWheelMoved(ToolManager mng) {
		var camera = mng.getCamera();
		var scale = camera.scale.x + mng.getScrollAmount() * zoomUnit * (mng.isShiftDown() ? 3 : 1);
		scale = MathUtils.clamp(scale, 0.05f, 5);
		camera.zoom(mng.getLocalMouse(), scale, 0);
		mng.repaintCanvas();
	}

	public void onAttached(ToolManager toolManager) {
	}

	@Override
	public void onDettached(ToolManager toolManager) {

	}

	@Override
	public void mouseMoved(ToolManager toolManager) {

	}

	@Override
	public void mouseDragged(ToolManager toolManager) {
		toolManager.getCamera().position.sub(toolManager.getMouseOffset());
		toolManager.getCamera().update();
		toolManager.repaintCanvas();
	}

	@Override
	public void paint(ToolManager mng, Graphics g) {

	}

	@Override
	public void mousePressed(ToolManager toolManager, int button, int clickCount) {

	}

	@Override
	public void mouseReleased(ToolManager toolManager, int button) {

	}

	@Override
	public void onKeyPressed(ToolManager toolManager, KeyEvent event) {

	}

	@Override
	public void onKeyReleased(ToolManager toolManager, KeyEvent event) {

	}

}
