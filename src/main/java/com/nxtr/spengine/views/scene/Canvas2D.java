package com.nxtr.spengine.views.scene;

import static org.lwjgl.opengl.GL11.glViewport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.FurtherApp;
import com.ngeneration.furthergui.drag.DragInterface;
import com.ngeneration.furthergui.drag.DropEvent;
import com.ngeneration.furthergui.event.KeyStroke;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.math.Point;
import com.ngeneration.miengine.graphics.OrthographicCamera;
import com.ngeneration.miengine.math.Vector3;
import com.ngeneration.miengine.scene.GameObject;
import com.nxtr.easymng.hearachy.Item;
import com.nxtr.easymng.hearachy.ItemsFlavor;
import com.nxtr.easymng.hearachy.SelectionManager;
import com.nxtr.easymng.workspace.ResourceItem;
import com.nxtr.spengine.MiEngine;
import com.nxtr.spengine.project.EngineResourceItem;

public class Canvas2D extends FPanel implements DragInterface {
	private Scene2DEditor view;
	private SelectionManager selectionManager;
	private OrthographicCamera camera = new OrthographicCamera();

	private ToolManager mng;

	private Map<String, DropHandler<EngineResourceItem, GameObject>> dropHandlers = new HashMap<>();

	public Canvas2D(Scene2DEditor view) {
		this.view = view;
		this.mng = new ToolManager(this, view);
		selectionManager = new SelectionManager() {

			private Set<Item> selection = new HashSet<>();

			@Override
			public List<Item> getSelection() {
				return new ArrayList<>(selection);
			}

			@Override
			public void setSelection(Collection<Item> node) {
				if (selection.addAll(node) || (!selection.isEmpty() && node.isEmpty())) {
					selection.clear();
					selection.addAll(node);
					fireEvent(view);
				}
			}

		};
		selectionManager.addSelectionListener(e -> repaintCanvas());
		setTool(new SelectionTool());
		setFocusable(true);

		getActionMap().put("centerSelection", (e) -> {
			if (getSelectionManager().getSingleSelection() instanceof GameObjectItem item) {
				var objectLoc = item.getObject().transform.getLocation().toVec2();
				camera.position
						.set(objectLoc.sub(camera.getWorld2D(getWidth() / 2, getHeight() / 2).sub(camera.position)));
				repaintCanvas();
			}
		});
		getInputMap(WHEN_IN_FOCUSED_WINDOWS).put(KeyStroke.getKeyStroke("C"), "centerSelection");
	}

	public void setTool(Tool selectionTool) {
		mng.setTool(selectionTool);
	}

	public void repaintCanvas() {
		repaint();
	}

	boolean started = false;

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.end();

		// local viewport
		glViewport(getScreenLocation().getX(),
				FurtherApp.getInstance().getHeight() - (getScreenLocation().getY() + getHeight()), getWidth(),
				getHeight());

//		System.out.println("cam: " + camera.position);
		camera.setToOrtho(false, getWidth(), getHeight());

		camera.update();
		GameObject.setRootCamera(camera);
		GameObjectItem root = (GameObjectItem) view.getRoot();
		if (!started) {
			try {
//				root.getObject().start();
				started = true;
			} catch (java.lang.Error e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
//		root.getObject().update();
		}
		if (started) {
			try {
				root.getObject().render();
			} catch (Error e) {
				e.printStackTrace();
			}
		}

		// restore GUI viewport
		glViewport(0, 0, FurtherApp.getInstance().getWidth(), FurtherApp.getInstance().getHeight());
		g.begin();

		// draw viewport game width
		String[] viewport = MiEngine.getProject().getConfiguration().getString("viewport").split(",");
		int viewportWidth = Integer.parseInt(viewport[0]);
		int viewportHeight = Integer.parseInt(viewport[1]);

		var localOrigin = camera.getLocal(Vector3.ZERO);
		g.setColor(Color.LIGTH_GRAY);
		g.drawRect((int) localOrigin.x, getHeight() - (int) localOrigin.y, (int) (viewportWidth * camera.scale.x),
				(int) (-viewportHeight * camera.scale.x));

		mng.paint(g);
	}

	public SelectionManager getSelectionManager() {
		return selectionManager;
	}

	public OrthographicCamera getCamera() {
		return camera;
	}

	public GameObjectItem getRoot() {
		return (GameObjectItem) view.getRoot();
	}

	@Override
	public void onDrag(DropEvent event) {

	}

	@Override
	public void onDrop(DropEvent event) {
		if (event.getTransferable().isFlavorSupported(ItemsFlavor.class)) {
			var item = event.getTransferable().getFlavor(ItemsFlavor.class).getValue().get(0);
			if (item instanceof EngineResourceItem resource && canDrop(resource)) {
				if (event.getEventType() == DropEvent.TEST_EVENT || event.getEventType() == DropEvent.CANCEL_EVENT)
					event.consume();
				else {
					var handler = dropHandlers.get(resource.getResource().getExt());
					if (handler != null)
						drop(null, new Point(event.getLocation().getX(), getHeight() - event.getLocation().getY()),
								resource);
				}
			}
		}

	}

	public boolean canDrop(ResourceItem item) {
		return dropHandlers.containsKey(item.getResource().getExt());
	}

	public void drop(GameObjectItem parent, Point location, EngineResourceItem item) {
		if (item instanceof EngineResourceItem resource) {
			if (dropHandlers.get(
					resource.getResource().getExt()) instanceof DropHandler<EngineResourceItem, GameObject> handler) {
				var object = handler.onAcepted(resource);
				if (parent == null)
					parent = this.getRoot();
				if (location == null)
					location = new Point(getWidth() / 2, getHeight() / 2);

				var newGameObjectItem = new GameObjectItem(parent, object);
				object.transform.setLocation(this.getCamera().getWorld2D(location.getX(), location.getY()));
				parent.add(newGameObjectItem);
				getSelectionManager().setSelection(newGameObjectItem);
			}
		}
	}

	public void addDropHandler(String ext, DropHandler<EngineResourceItem, GameObject> dropHandler) {
		dropHandlers.put(ext, dropHandler);
	}

	public void removeDropHandler(String ext, DefaultDropHandler dropHandler) {
		dropHandlers.remove(ext);
	}

	public float getScale() {
		return camera.scale.x;
	}

	public void setScale(float float1) {
		camera.scale.set(float1);
	}

}
