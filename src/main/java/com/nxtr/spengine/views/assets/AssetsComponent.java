package com.nxtr.spengine.views.assets;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FMenuItem;
import com.ngeneration.furthergui.drag.DropEvent;
import com.ngeneration.furthergui.drag.Transferable;
import com.ngeneration.furthergui.event.Action;
import com.ngeneration.furthergui.event.MouseAdapter;
import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.graphics.Icon;
import com.ngeneration.furthergui.graphics.ImageIcon;
import com.ngeneration.miengine.scene.GameObjectRef;
import com.nxtr.easymng.Application;
import com.nxtr.easymng.Setup;
import com.nxtr.easymng.View;
import com.nxtr.easymng.hearachy.DefaultHearachyCellRenderer;
import com.nxtr.easymng.hearachy.Explorer;
import com.nxtr.easymng.hearachy.HearachyComponent;
import com.nxtr.easymng.hearachy.Item;
import com.nxtr.easymng.hearachy.ItemsFlavor;
import com.nxtr.easymng.hearachy.SelectionManager;
import com.nxtr.easymng.util.ContextMenuUtil;
import com.nxtr.easymng.workspace.Resource;
import com.nxtr.easymng.workspace.ResourceItem;
import com.nxtr.easymng.workspace.Workspace;
import com.nxtr.spengine.EngineUtil;
import com.nxtr.spengine.MiEngine;
import com.nxtr.spengine.dialogs.MiEngineDialogs;
import com.nxtr.spengine.project.EngineResource;
import com.nxtr.spengine.views.inspector.InspectorView;
import com.nxtr.spengine.views.outline.OutlineView;
import com.nxtr.spengine.views.scene.GameObjectItem;
import com.nxtr.spengine.views.scene.Scene2DEditor;

public class AssetsComponent extends Explorer {

	private static final Logger logger = LoggerFactory.getLogger(AssetsComponent.class);

	private List<FMenuItem> items;
	private Map<String, Icon> icons = new HashMap<>();

	public AssetsComponent(View view) {
		var exts = "folder,png,scn";
		for (String ext : exts.split(",")) {
			try {
				icons.put(ext, new ImageIcon(getClass().getResourceAsStream("/icons/" + ext + ".png")));
			} catch (Exception e) {
				logger.error("icon no found: " + e.getMessage());
			}
		}

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent event) {
				if (event.getButton() == MouseEvent.BUTTON1 && getSelection().size() == 1
						&& getTree().viewToModel(event.getLocation()) != null
						&& getSelection().get(0) instanceof Resource resource) {
					if (!resource.exists()) {
						System.out.println("Resource does not exists: " + resource.getFile());
						return;
					}
					if (Application.getInstance().getViewManager()
							.getViewById(InspectorView.ID) instanceof InspectorView inspector) {
						inspector.setContent(resource);
					}
				}
			}
		});

		setCellRenderer(new DefaultHearachyCellRenderer() {
			@Override
			public FComponent getExplorerCellRendererComponent(HearachyComponent<? extends Item> explorer, Item unit,
					boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				var data = super.getExplorerCellRendererComponent(explorer, unit, selected, expanded, leaf, row,
						hasFocus);
				// icon provider
				if (unit instanceof ResourceItem item) {
					var icon = AssetsComponent.this.getIcon(item.getResource());
					if (icon != null) {
						setIcon(icon);
						if (item.getResource().getItemCount() == 1)
							setText(item.getResource().getSimpleName());
					}
				} else {
					var res = (Resource) unit;
					var icon = AssetsComponent.this.getIcon((Resource) unit);
					if (icon != null) {
						setIcon(icon);
						setText(res.getSimpleName());
					}
				}
				return data;
			}
		});

//		setDragAndDropEnabled(true, false);
	}

	Icon getIcon(Resource ext) {
		if (ext.getFile().isDirectory())
			return icons.get("folder");
		else
			return icons.get(ext.getExt());

	}

	@Override
	protected void onDoubleClick(Item item) {
		// implement ViewResolverManager!!!!!!!!
		if (item instanceof Resource resource && "scn".equals(resource.getExt()))
			Application.getInstance().getViewManager().addViewAndSetActive(new Scene2DEditor(resource));
	}

	@Override
	protected void onDelete() {
		var deleted = new AssetsActions().deleteAction();
		for (var del : deleted) {
			if (del instanceof EngineResource res && res.getExt().equals(EngineUtil.SCENE_EXT)) {
				Application.getInstance().getViewManager().getViews().forEach(v -> {
					if (v instanceof Scene2DEditor editor) {
						deleteRef(editor.getRoot(), Integer.parseInt(del.getId()));
					}
				});
			}
		}
		if (!deleted.isEmpty())
			MiEngine.getInstance().getControl().repaint();
	}

	private void deleteRef(Item root, int id) {
		var object = (GameObjectItem) root;
		if (object.getObject() instanceof GameObjectRef ref) {
			if (ref.getResourceId() == id)
				ref.invalidate();
		} else
			root.getItems().forEach(c -> deleteRef(c, id));
	}

	public void setRoot(Item root) {
		if (root instanceof Workspace ws) {
			super.setRoot(ws.getProjects().get(0).getItemByName("Assets"));
			super.expandPath(List.of(ws.getProjects().get(0).getItemByName("Assets")));
		} else
			super.setRoot(root);
	}

	protected com.ngeneration.furthergui.TreeNode[] toTreePath(Item[] path) {
		return super.toTreePath(Arrays.copyOfRange(path, 2, path.length));
	}

	@Override
	protected List<FMenuItem> getPopupMenuItems() {
		if (items == null) {
			Map<String, Action> actions = Application.getInstance().getActions();
			Setup.getActions(new AssetsActions()).entrySet().forEach(v -> actions.put(v.getKey(), v.getValue()));
			try {
				items = ContextMenuUtil.createItems(new FileInputStream("assets-context-menu.menu"), actions);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return items;
	}

	@Override
	public boolean testDrop(Item target, int index, DropEvent event) {
		var drop = super.testDrop(target, index, event);
		if (!drop && event.getTransferable().isFlavorSupported(ItemsFlavor.class)) {
			var flavor = event.getTransferable().getFlavor(ItemsFlavor.class);
			drop = flavor.getSingle() instanceof GameObjectItem;
		}
		return drop;
	}

	@Override
	public void onDropAccepted(Item target, int targetRow, Transferable transferable) {
		boolean handled = false;
		if (transferable.isFlavorSupported(ItemsFlavor.class)) {
			var flavor = transferable.getFlavor(ItemsFlavor.class);
			if (flavor.getSingle() instanceof GameObjectItem item) {
				var sceneResource = MiEngineDialogs.newScene((Resource) target, item.getObject(), item.getName());
				if (sceneResource != null) {
					item.setGameObject(MiEngine.getProject().load(Integer.parseInt(sceneResource.getId())));
					var view = Application.getInstance().getViewManager().getViewById(OutlineView.ID);
					if (view != null)
						view.getComponent().repaint();
					setSelection(sceneResource);
				}
				handled = true;
			}
		}
		if (!handled) {
			super.onDropAccepted(target, targetRow, transferable);
		}
	}

}
