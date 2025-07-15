package com.nxtr.spengine.views.scene;

import java.util.List;

import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.miengine.math.Rectangle;
import com.ngeneration.miengine.math.Vector2;
import com.ngeneration.miengine.math.Vector3;
import com.ngeneration.miengine.particles.BoxEmitter;
import com.ngeneration.miengine.particles.CircleEmitter;
import com.ngeneration.miengine.particles.EmitterShape2D;
import com.ngeneration.miengine.scene.Renderer;
import com.ngeneration.miengine.scene.physics.BoxCollider;
import com.ngeneration.miengine.scene.physics.CircleCollider;
import com.ngeneration.miengine.scene.physics.Collider;
import com.ngeneration.miengine.scene.ui.CanvasComponent;
import com.nxtr.easymng.hearachy.Item;
import com.nxtr.easymng.hearachy.SelectionListener;

public class SelectionTool extends AbstractTool {

	private static Rectangle rect = new Rectangle();

	private int size = 20;
	private List<GameObjectItem> dragging;
	private float colliderHandlerSize = 8;
	private Handler handler;

	private List<Item> localSelection;
	private ToolManager toolManager;
	
	private SelectionListener selectionListener = (e) -> {
		var newSelection = toolManager.getSelectionManager().getSelection();
		for (var v : newSelection) {
			if (!localSelection.contains(v))
				((GameObjectItem) v).getObject().setAsSelected(true);
		}
		for (var v : localSelection) {
			if (!newSelection.contains(v))
				((GameObjectItem) v).getObject().setAsSelected(false);
		}
		localSelection = newSelection;
		toolManager.repaintCanvas();
	};



	private class Handler {
		private GameObjectItem item;
		private Collider collider;
		private int index;
		private Vector2 offset = new Vector2();
	}
	
	@Override
	public void onAttached(ToolManager toolManager) {
		this.toolManager = toolManager;
		toolManager.getSelectionManager().addSelectionListener(selectionListener);
		localSelection = toolManager.getSelectionManager().getSelection();
	}
	
	@Override
	public void onDettached(ToolManager toolManager) {
		toolManager.getSelectionManager().removeSelectionListener(selectionListener);
	}

	public void mouseDragged(ToolManager mng) {
		if (handler != null) {
			var p = handler.collider.transform.getLocation2().add(handler.collider.offset.cpy()
					.rotate(handler.collider.transform.getRotationZ()).scl(handler.collider.transform.getScale()));
			p = mng.getCamera().getLocal(p);
			var toMouse = mng.getLocalMouse().add(handler.offset).sub(p);

			if (handler.collider instanceof CircleCollider collider) {
				collider.radius = toMouse.length() / collider.transform.getScaleX() / mng.getCamera().scale.z;
				mng.propertyUpdated(handler.item, collider, "radius", collider.radius);
			} else if (handler.collider instanceof BoxCollider collider) {
				toMouse.rotate(-collider.transform.getRotationZ());
				if (handler.index == 0 || handler.index == 2)
					collider.dimension.x = Math
							.abs(toMouse.x / collider.transform.getScaleX() / mng.getCamera().scale.z * 2);
				else
					collider.dimension.y = Math
							.abs(toMouse.y / collider.transform.getScaleY() / mng.getCamera().scale.z * 2);
				mng.propertyUpdated(handler.item, collider, "dimension", collider.dimension);
			}

			mng.repaintCanvas();
			return;
		}
		var selected = mng.getSelectionManager().getSelection().stream().map(GameObjectItem.class::cast).toList();
		if (dragging != null) {
			for (var sel : selected) {
				sel.getObject().transform.addToLocation(mng.getMouseOffset());
				mng.propertyUpdated(sel, sel.getObject().transform, "location",
						sel.getObject().transform.getLocalLocation());
			}
			mng.repaintCanvas();
		} else {
			super.mouseDragged(mng);
		}
	}

	@Override
	public void mousePressed(ToolManager mng, int button, int clickCount) {
		var selected = mng.getSelectionManager().getSelection().stream().map(GameObjectItem.class::cast).toList();

		boolean handlerFound = false;
		if (handler == null && selected.size() == 1) {
			float handlerSize = colliderHandlerSize / 2 * 1.5f;
			handlerSize *= handlerSize;
			var colliders = selected.get(0).getObject().getComponents(Collider.class);
			for (var collider : colliders) {
				var p = collider.transform.getLocation2().add(collider.offset.cpy()
						.rotate(collider.transform.getRotationZ()).scl(collider.transform.getScale()));
				if (collider instanceof CircleCollider circle) {
					p.add(new Vector2(circle.radius * collider.transform.getScaleX(), 0).rotate(45));
					p = mng.getCamera().getLocal(p);
					if (p.sub(mng.getLocalMouse()).length2() < handlerSize) {
						handler = new Handler();
						handler.collider = collider;
						handler.item = selected.get(0);
						handler.offset = p.scl(1);
						handlerFound = true;
						break;
					}
				} else if (collider instanceof BoxCollider box) {

					for (int i = 0; i < 4; i++) {
						var rp = new Vector2(box.dimension).scl(-0.5f).scl(collider.transform.getScale());
						if (i == 0 || i == 2)
							rp.y = 0;
						else
							rp.x = 0;
						rp.rotate(collider.transform.getRotationZ());

						if (i == 2 || i == 3)
							rp.scl(-1);

						var px = p.cpy().add(rp);
						var pp = mng.getCamera().getLocal(px);

						if (pp.dist2(mng.getLocalMouse()) < handlerSize) {
							handler = new Handler();
							handler.collider = collider;
							handler.item = selected.get(0);
							handler.offset = pp.sub(mng.getLocalMouse());
							handler.index = i;
							handlerFound = true;
							break;
						}
					}
				}
			}
		}
		if (handlerFound)
			return;

		handler = null;
		dragging = null;
		for (var sel : selected) {
			if (rect.set(mng.getCamera().getLocal(sel.getObject().transform.getLocation()), size)
					.contains(mng.getLocalMouse())) {
				dragging = selected;
			}
		}
		if (dragging == null) {
			var root = (GameObjectItem) mng.getRoot();
			var found = checkCollision(root, mng, selected);
			if (found != null) {
				if (mng.isControlDown()) {
					if (!selected.contains(found))
						mng.getSelectionManager().addToSelection(found);
					else
						mng.getSelectionManager().removeFromSelection(found);
				} else
					mng.getSelectionManager().setSelection(found);
				dragging = List.of(found);
			}
		}
		if (dragging == null && !mng.isControlDown()) {
			mng.getSelectionManager().clearSelection();
		}
	}

	@Override
	public void mouseReleased(ToolManager toolManager, int button) {
		dragging = null;
		handler = null;
	}

	private GameObjectItem checkCollision(GameObjectItem item, ToolManager mng, List<GameObjectItem> selection) {
		var object = item.getObject();

		if (!object.isSelectionBlocked()) {
			var iterator = ((List<?>) item.getItems()).listIterator(item.getItemCount());
			while (iterator.hasPrevious()) {
				var v = checkCollision((GameObjectItem) iterator.previous(), mng, selection);
				if (v != null) {
					var xx = v;
					while (xx != null) {
						if (xx.getObject().isSelectionRoot())
							return xx;
						xx = (GameObjectItem) xx.getParentItem();
					}
					return v;
				}
			}
		}
		if (object.isSelectionBlocked() && !selection.contains(item)) {
			return null;
		}

		if (object.getComponent(Renderer.class) instanceof Renderer renderer) {
			renderer.getLocalBounds(rect);
			if (rect.width > 0) {
				rect.x *= object.transform.getScaleX();
				rect.y *= object.transform.getScaleY();
				rect.width *= object.transform.getScaleX();
				rect.height *= object.transform.getScaleY();
				var mouse = mng.getMouse().sub(object.transform.getLocation());
				if (object.transform.getRotationZ() != 0)
					mouse.rotate(-object.transform.getRotationZ());
				if (rect.contains(mouse))
					return item;
			}
		} else if (object.getComponent(CanvasComponent.class) instanceof CanvasComponent canvasItem) {
			rect.width = canvasItem.size.x;
			rect.height = canvasItem.size.y;
			rect.x = -canvasItem.pivot.x * rect.width;
			rect.y = -canvasItem.pivot.y * rect.height;

			rect.x *= object.transform.getScaleX();
			rect.y *= object.transform.getScaleY();
			rect.width *= object.transform.getScaleX();
			rect.height *= object.transform.getScaleY();
			var mouse = mng.getMouse().sub(object.transform.getLocation());
			if (object.transform.getRotationZ() != 0)
				mouse.rotate(-object.transform.getRotationZ());
			if (rect.contains(mouse))
				return item;
		}
		return null;
	}

	private Rectangle getSelectionRectangle(GameObjectItem item, Rectangle rect) {
		var object = item.getObject();
//		var iterator = ((List<?>) item.getItems()).listIterator(item.getItemCount());
//		while (iterator.hasPrevious()) {
//			var v = check((GameObjectItem) iterator.previous(), mng);
//			if (v != null)
//				return v;
//		}
		if (object.getComponent(Renderer.class) instanceof Renderer renderer) {
			renderer.getLocalBounds(rect);
			if (rect.width > 0) {
				rect.x *= object.transform.getScaleX();
				rect.y *= object.transform.getScaleY();
				rect.width *= object.transform.getScaleX();
				rect.height *= object.transform.getScaleY();
				return rect;
			}
		} else if (object.getComponent(CanvasComponent.class) instanceof CanvasComponent canvasItem) {
			rect.width = canvasItem.size.x;
			rect.height = canvasItem.size.y;
			rect.x = -canvasItem.pivot.x * rect.width;
			rect.y = -canvasItem.pivot.y * rect.height;

			rect.x *= object.transform.getScaleX();
			rect.y *= object.transform.getScaleY();
			rect.width *= object.transform.getScaleX();
			rect.height *= object.transform.getScaleY();
			return rect;
		}
		return null;
	}

	private void drawRect(ToolManager mng, Graphics g, Rectangle rect, Vector2 loc, float rotation) {
		var camera = mng.getCamera();
		var vector1 = new Vector2();
		var vector2 = new Vector2();
		if (rotation != 0) {

			vector1 = mng.getGui(camera.getLocal(vector1.set(rect.x, rect.y).rotate(rotation).add(loc)));
			vector2 = mng.getGui(camera.getLocal(vector2.set(rect.x + rect.width, rect.y).rotate(rotation).add(loc)));
			g.drawLine(vector1.x, vector1.y, vector2.x, vector2.y);

			vector1 = mng.getGui(
					camera.getLocal(vector1.set(rect.x + rect.width, rect.y + rect.height).rotate(rotation).add(loc)));
			g.drawLine(vector2.x, vector2.y, vector1.x, vector1.y);

			vector2 = mng.getGui(camera.getLocal(vector2.set(rect.x, rect.y + rect.height).rotate(rotation).add(loc)));
			g.drawLine(vector1.x, vector1.y, vector2.x, vector2.y);

			vector1 = mng.getGui(camera.getLocal(vector1.set(rect.x, rect.y).rotate(rotation).add(loc)));
			g.drawLine(vector2.x, vector2.y, vector1.x, vector1.y);

		} else {
			loc = loc.cpy().add(rect.x, rect.y);
			var xx = mng.getGui(camera.getLocal(loc));
			g.drawRect((int) xx.x, (int) xx.y, (int) (rect.width * camera.scale.x),
					(int) (-rect.height * camera.scale.y));
		}
	}

	@Override
	public void paint(ToolManager mng, Graphics g) {
		var selection = mng.getSelectionManager().getSelection();
		if (selection.isEmpty())
			return;

		var camera = mng.getCamera();

		for (var item : mng.getSelectionManager().getSelection()) {
			var gameItem = (GameObjectItem) item;
			GameObjectItem selected = gameItem;
			var object = selected.getObject();
			var rotation = object.transform.getRotationZ();
			var scale = object.transform.getScale();

			var rect1 = getSelectionRectangle(gameItem, rect);

			// selection bounds
			if (rect1 != null) {
				g.setPenSize(2);
				g.setColor(Color.LIGTH_GRAY);

				Vector3 loc = camera.getLocal(gameItem.getObject().transform.getLocation().add(rect1.x, rect1.y));
				loc = mng.getGui(loc);
				drawRect(mng, g, rect, object.transform.getLocation2(), object.transform.getRotationZ());
			}

			if (selection.size() == 1) {

				// collider handlers
				for (var collider : selected.getObject().getComponents(Collider.class)) {
					// draw handlers
					g.setColor(Color.CYAN);
					float hSize = colliderHandlerSize / 2;
					g.setPenSize(1);

					if (collider instanceof BoxCollider box) {
						var offset = object.transform.getLocation2()
								.add(box.offset.cpy().rotate(object.transform.getRotationZ()).scl(scale));

						var boxLeft = new Vector2(-box.dimension.x * 0.5f * scale.x, 0).rotate(rotation);
						var boxBottom = new Vector2(0, -box.dimension.y * 0.5f * scale.y).rotate(rotation);

						var p1 = mng.getGui(camera.getLocal(new Vector2(boxLeft).add(offset)));
						g.fillRect((int) (p1.x - hSize), (int) (p1.y - hSize), (int) (hSize * 2), (int) (hSize * 2));
						var p2 = mng.getGui(camera.getLocal(new Vector2(boxBottom).add(offset)));
						g.fillRect((int) (p2.x - hSize), (int) (p2.y - hSize), (int) (hSize * 2), (int) (hSize * 2));
						p1 = mng.getGui(camera.getLocal(new Vector2(boxLeft.scl(-1)).add(offset)));
						g.fillRect((int) (p1.x - hSize), (int) (p1.y - hSize), (int) (hSize * 2), (int) (hSize * 2));
						p2 = mng.getGui(camera.getLocal(new Vector2(boxBottom.scl(-1)).add(offset)));
						g.fillRect((int) (p2.x - hSize), (int) (p2.y - hSize), (int) (hSize * 2), (int) (hSize * 2));

					} else if (collider instanceof CircleCollider circle) {
						var p1 = mng.getGui(camera.getLocal(new Vector2(circle.radius * scale.x, 0).rotate(45)
								.add(object.transform.getLocation2().add(circle.offset.cpy().scl(scale)))));

						g.fillRect((int) (p1.x - hSize), (int) (p1.y - hSize), (int) (hSize * 2), (int) (hSize * 2));
					}

				}

				// particle shapes
				for (var component : selected.getObject().getComponents()) {
					if (component.getClass().getCanonicalName().equals("game.ParticleSystem")) {
						try {
							EmitterShape2D emitter = (EmitterShape2D) component.getClass().getField("shape")
									.get(component);
							g.setColor(Color.MAGENTA);
							if (emitter instanceof BoxEmitter box) {
								var rect = new Rectangle(-box.width / 2 * scale.x, -box.height / 2 * scale.y,
										box.width * scale.x, box.height * scale.y);
								drawRect(mng, g, rect, object.transform.getLocation2(),
										object.transform.getRotationZ());
							} else if (emitter instanceof CircleEmitter shape) {
								var p = mng.getGui(camera.getLocal(object.transform.getLocalLocation()));
								g.drawCircle(p.x, p.y, shape.radius * object.transform.getScaleX() * camera.scale.x, 3);
							}

						} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
								| SecurityException e) {
							e.printStackTrace();
						}
					}
				}
			}

			// draw axes
			Vector3 loc = camera.getLocal(selected.getObject().transform.getLocation());
			loc = mng.getGui(loc);
			g.setPenSize(2);
			g.setColor(Color.GREEN);
			g.drawLine(loc.x, loc.y, loc.x, loc.y - size * 5);
			g.setColor(Color.RED);
			g.drawLine(loc.x, loc.y, loc.x + size * 5, loc.y);
			g.setColor(new Color(20, 30, 250, 150));
			g.fillRect(loc.x, loc.y, size, -size);
		}

	}

}
