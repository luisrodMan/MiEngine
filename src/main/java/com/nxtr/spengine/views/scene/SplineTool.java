package com.nxtr.spengine.views.scene;

import com.ngeneration.furthergui.event.KeyEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.miengine.math.MathUtils;
import com.ngeneration.miengine.math.Spline;
import com.ngeneration.miengine.math.Spline.SplinePoint;
import com.ngeneration.miengine.math.Vector2;
import com.ngeneration.miengine.scene.Component;
import com.ngeneration.miengine.scene.GameObject;

public class SplineTool extends AbstractTool {

	private enum Mode {
		BROWSER, START, END
	};

	private Mode mode = Mode.BROWSER;
	private SplinePoint selected;
	private int selectedMode = 0;// 0, 1, 2
	private Spline spline;
	private boolean dragging;

	private float selectionDelta = 6;
	private GameObject object;

	public SplineTool(Component object, Spline spline) {
		this.object = object.gameObject;
		this.spline = spline;
	}

	@Override
	public void onKeyPressed(ToolManager toolManager, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.VK_1)
			mode = Mode.START;
		else if (event.getKeyCode() == KeyEvent.VK_2)
			mode = Mode.END;
		else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
			if (mode == Mode.BROWSER)
				toolManager.setTool(null);
			else {
				mode = Mode.BROWSER;
				toolManager.repaintCanvas();
			}
		} else if (event.getKeyCode() == KeyEvent.VK_SPACE) {
			mode = Mode.BROWSER;
			toolManager.repaintCanvas();
		}
	}

	@Override
	public void mousePressed(ToolManager toolManager, int button, int clickCount) {
		if (mode != Mode.BROWSER) {
			if (mode == Mode.START)
				spline.addPoint(0, toolManager.getMouse().sub(object.transform.getLocation2()), new Vector2(-100, 0),
						SplinePoint.SINGLE);
			else
				spline.addPoint(toolManager.getMouse().sub(object.transform.getLocation2()), new Vector2(-100, 0),
						SplinePoint.SINGLE);
			toolManager.repaintCanvas();
		} else
			super.mousePressed(toolManager, button, clickCount);
	}

	@Override
	public void mouseReleased(ToolManager toolManager, int button) {
		dragging = false;
	}

	@Override
	public void mouseDragged(ToolManager toolManager) {
		if (dragging) {
			if (selectedMode == 0)
				selected.position.add(toolManager.getMouseOffset());
			else if (selectedMode == 1)
				selected.handler1.add(toolManager.getMouseOffset());
			else
				selected.handler2.add(toolManager.getMouseOffset());
			toolManager.repaintCanvas();
		} else if (mode == Mode.BROWSER) {
			// selected point
			Vector2 p = new Vector2();
			selectedMode = -1;
			Vector2 mouse = toolManager.getMouse().sub(object.transform.getLocation2());
			for (var point : spline.getPoints()) {
				if (point.handler1 != null && (point.handler1.x != 0 || point.handler1.y != 0)) {
					p.set(point.position).add(point.handler1);
					if (p.dist2(mouse) < selectionDelta * selectionDelta / toolManager.getCamera().scale.x)
						selectedMode = 1;
				}
				if (point.handler2 != null && (point.handler2.x != 0 || point.handler2.y != 0)) {
					p.set(point.position).add(point.handler2);
					if (p.dist2(mouse) < selectionDelta * selectionDelta / toolManager.getCamera().scale.x)
						selectedMode = 2;
				}
				if (selectedMode == -1) {
					p.set(point.position);
					if (p.dist2(mouse) < selectionDelta * selectionDelta / toolManager.getCamera().scale.x)
						selectedMode = 0;
				}
				if (selectedMode != -1) {
					selected = point;
					break;
				}
			}
			if (selectedMode > -1) {
				dragging = true;
				toolManager.repaintCanvas();
			} else
				super.mouseDragged(toolManager);
		}
	}

	@Override
	public void paint(ToolManager mng, Graphics g) {
		var points = spline.getPoints();
		SplinePoint lastPoint = null;
		int hw = 4;
		int hs = 6;
		int hs2 = 4;
		int smothCount = 50;
		float step = 1.0f / smothCount;
		Vector2 result = new Vector2();
		int i = 0;
		var p = new Vector2();

		Vector2 globalPosition = object.transform.getLocation2();
		for (var point : points) {

			// draw spline
			if (lastPoint != null) {
				g.setColor(Color.WHITE);
				var pp = new Vector2(lastPoint.position).add(globalPosition);
				for (int j = 1; j < smothCount - 1; j++) {
					MathUtils.spline(j * step, lastPoint, point, result);
					result.add(globalPosition);
					mng.getCamera().toLocal(pp);
					mng.toGui(pp);
					p.set(result);
					mng.getCamera().toLocal(p);
					mng.toGui(p);
					g.drawLine(pp.x, pp.y, p.x, p.y);
					pp.set(result);
				}
				mng.getCamera().toLocal(result);
				mng.toGui(result);
				p.set(point.position).add(globalPosition);
				mng.getCamera().toLocal(p);
				mng.toGui(p);
				g.drawLine(result.x, result.y, p.x, p.y);
			}

			// lines
			boolean drawHandler1 = i > 0 && point.handler1.x != 0 || point.handler1.y != 0;
			boolean drawHandler2 = i < points.size() - 1 && (point.handler2.x != 0 || point.handler2.y != 0);

			var local = mng.getCamera().getLocal(point.position.x + globalPosition.x,
					point.position.y + globalPosition.y);
			p = mng.getGui(local.toVec2());

			g.setColor(Color.BLUE);
			if (drawHandler1) {
				local.set(point.position).add(point.handler1).add(globalPosition);
				mng.getCamera().toLocal(local);
				mng.toGui(local);
				g.drawLine(p.x, p.y, local.x, local.y);
			}
			if (drawHandler2) {
				local.set(point.position).add(point.handler2).add(globalPosition);
				mng.getCamera().toLocal(local);
				mng.toGui(local);
				g.drawLine(p.x, p.y, local.x, local.y);
			}

			// draw points
			g.setColor(Color.BLACK);
			g.fillRect(p.x - hs, p.y - hs, hs * 2, hs * 2);
			g.setColor(Color.WHITE);
			g.fillRect(p.x - hw, p.y - hw, hw * 2, hw * 2);

			// draw handlers
			Color handlerColor = Color.BLUE;
			g.setColor(handlerColor);
			if (drawHandler1) {
				p.set(point.position).add(point.handler1).add(globalPosition);
				mng.getCamera().toLocal(p);
				mng.toGui(p);
				g.fillRect(p.x - hs2, p.y - hs2, hs2 * 2, hs2 * 2);
			}
			if (drawHandler2) {
				p.set(point.position).add(point.handler2).add(globalPosition);
				mng.getCamera().toLocal(p);
				mng.toGui(p);
				g.fillRect(p.x - hs2, p.y - hs2, hs2 * 2, hs2 * 2);
			}

			lastPoint = point;
			i++;
		}

	}

}
