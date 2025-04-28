package com.nxtr.spengine.views.scene;

import com.ngeneration.furthergui.event.KeyAdapter;
import com.ngeneration.furthergui.event.KeyEvent;
import com.ngeneration.furthergui.event.MouseAdapter;
import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.event.MouseWheelEvent;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.math.Point;
import com.ngeneration.miengine.graphics.OrthographicCamera;
import com.ngeneration.miengine.math.Vector2;
import com.ngeneration.miengine.math.Vector3;
import com.ngeneration.miengine.scene.Component;
import com.nxtr.easymng.hearachy.SelectionManager;

public class ToolManager {

	private Tool tool;
	private Canvas2D canvas;
	private Point lastPosition = new Point();
	private Point position = new Point();
	private com.ngeneration.furthergui.event.Event event;
	protected int scrollAmount;
	private Scene2DEditor editor;
	private int mods;

	public ToolManager(Canvas2D canvas2d, Scene2DEditor view) {
		this.canvas = canvas2d;
		this.editor = view;
		var mouseAdapter = new MouseAdapter() {

			@Override
			public void mouseMoved(MouseEvent event1) {
				event = event1;
				lastPosition = position;
				position = event1.getLocation();
				if (tool != null)
					tool.mouseMoved(ToolManager.this);
				event1.consume();
			}

			@Override
			public void mouseDragged(MouseEvent event1) {
				event = event1;
				lastPosition = position;
				position = event1.getLocation();
				if (tool != null)
					tool.mouseDragged(ToolManager.this);
				event1.consume();
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent event1) {
				event = event1;
				scrollAmount = event1.getAmount();
				if (tool != null)
					tool.mouseWheelMoved(ToolManager.this);
				event1.consume();
			}

			@Override
			public void mousePressed(MouseEvent event1) {
				event = event1;
				mods = event1.getMods();
				if (tool != null)
					tool.mousePressed(ToolManager.this, event1.getButton(), event1.getClickCount());
			}

			@Override
			public void mouseReleased(MouseEvent event1) {
				event = event1;
				mods = event1.getMods();
				if (tool != null)
					tool.mouseReleased(ToolManager.this, event1.getButton());
			}

		};
		var keyAdapter = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (tool != null)
					tool.onKeyPressed(ToolManager.this, event);
			}

			@Override
			public void keyReleased(KeyEvent event) {
				if (tool != null)
					tool.onKeyReleased(ToolManager.this, event);
			}
		};
		canvas.addMouseListener(mouseAdapter);
		canvas.addMouseMotionListener(mouseAdapter);
		canvas.addMouseWheelListener(mouseAdapter);
		canvas.setFocusable(true);
		canvas.addKeyListener(keyAdapter);
	}

	public void setTool(Tool tool) {
		if (this.tool != null)
			this.tool.onDettached(this);
		if (tool == null) {
			tool = new SelectionTool();
		}
		this.tool = tool;
		if (tool != null)
			tool.onAttached(this);
		repaintCanvas();
	}

	public SelectionManager getSelectionManager() {
		return canvas.getSelectionManager();
	}

	public OrthographicCamera getCamera() {
		return canvas.getCamera();
	}

	public GameObjectItem getRoot() {
		return canvas.getRoot();
	}

	public Vector2 getMouse() {
		return getCamera().getWorld2D(toGUICoord(position));
	}

	public Vector2 getLastMouse() {
		return getCamera().getWorld2D(toGUICoord(lastPosition));
	}

	public Vector2 getMouseOffset() {
		return getMouse().sub(getLastMouse());
	}

	public Vector2 getLocalMouse() {
		return toGUICoord(position);
	}

	public Vector2 getLastLocalMouse() {
		return toGUICoord(lastPosition);
	}

	public Vector2 getLocalMouseOffset() {
		return toGUICoord(position).sub(toGUICoord(lastPosition));
	}

	public void repaintCanvas() {
		canvas.repaintCanvas();
	}

	public int getScrollAmount() {
		return scrollAmount;
	}

	public boolean isShiftDown() {
		return event != null && event.isShiftDown();
	}

	public boolean isControlDown() {
		return event != null && event.isControlDown();
	}

	private Vector2 toGUICoord(Point position) {
		return new Vector2(position.getX(), canvas.getHeight() - position.getY());
	}

	public void propertyUpdated(GameObjectItem object, Component component, String propertyName, Object value) {
		editor.onPropertyUpdated(object, component, propertyName, value);
	}

	public Vector2 getGui(Vector2 loc) {
		var vec = new Vector2().set(loc);
		vec.y = canvas.getHeight() - loc.y;
		return vec;
	}

	public Vector3 getGui(Vector3 loc) {
		var vec = new Vector3().set(loc);
		vec.y = canvas.getHeight() - loc.y;
		return vec;
	}

	public void toGui(Vector2 loc) {
		loc.y = canvas.getHeight() - loc.y;
	}

	public void toGui(Vector3 loc) {
		loc.y = canvas.getHeight() - loc.y;
	}

	public void paint(Graphics g) {
		if (tool != null)
			tool.paint(this, g);
	}

}
