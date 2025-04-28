package com.nxtr.spengine.views.scene;

import com.ngeneration.furthergui.event.KeyEvent;
import com.ngeneration.furthergui.graphics.Graphics;

public interface Tool {

	void onAttached(ToolManager toolManager);

	void onDettached(ToolManager toolManager);

	void mouseMoved(ToolManager toolManager);

	void mouseDragged(ToolManager toolManager);

	void mouseWheelMoved(ToolManager toolManager);

	void paint(ToolManager mng, Graphics g);

	void mousePressed(ToolManager toolManager, int button, int clickCount);

	void mouseReleased(ToolManager toolManager, int button);

	void onKeyPressed(ToolManager toolManager, KeyEvent event);

	void onKeyReleased(ToolManager toolManager, KeyEvent event);

}
