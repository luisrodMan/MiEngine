package com.nxtr.spengine.views.outline;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FMenuItem;
import com.ngeneration.furthergui.drag.DropEvent;
import com.ngeneration.furthergui.drag.Transferable;
import com.ngeneration.furthergui.event.Action;
import com.ngeneration.furthergui.event.KeyEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.miengine.scene.GameObjectRef;
import com.nxtr.easymng.Application;
import com.nxtr.easymng.Setup;
import com.nxtr.easymng.hearachy.DefaultHearachyCellRenderer;
import com.nxtr.easymng.hearachy.HearachyComponent;
import com.nxtr.easymng.hearachy.Item;
import com.nxtr.easymng.hearachy.ItemsFlavor;
import com.nxtr.easymng.util.ContextMenuUtil;
import com.nxtr.spengine.MiEngine;
import com.nxtr.spengine.project.EngineResourceItem;
import com.nxtr.spengine.views.scene.GameObjectItem;
import com.nxtr.spengine.views.scene.Scene2DEditor;

public class OutlineHearachy extends HearachyComponent<Item> {

	private List<FMenuItem> items;
	private OutlineView view;

	public OutlineHearachy(OutlineView outline, Item root) {
		super(root);
		this.view = outline;
		setDragAndropEnabled(true, true);
		DefaultHearachyCellRenderer renderer = null;
		setCellRenderer(renderer = new DefaultHearachyCellRenderer() {

			@Override
			public FComponent getExplorerCellRendererComponent(HearachyComponent<? extends Item> explorer, Item unit,
					boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				var render = super.getExplorerCellRendererComponent(explorer, unit, selected, expanded, leaf, row,
						hasFocus);
				GameObjectItem objectUnit = (GameObjectItem) unit;
				boolean enabled = true;
				var parent = objectUnit.getObject();
				while (parent != null) {
					if (!parent.isActive() || (parent.isSelectionRoot() && parent != objectUnit.getObject())) {
						enabled = false;
						break;
					}
					parent = parent.getParentGameObject();
				}
				var foreGround = getForeground();

				if (((GameObjectItem) unit).getObject() instanceof GameObjectRef ref) {
					if (ref.isValid())
						foreGround = new Color(30, 130, 255);
					else {
						if (!enabled)
							foreGround = new Color(255, 130, 30).darker();
						else
							foreGround = new Color(255, 130, 30);
					}
				} else if (!enabled)
					foreGround = Color.LIGTH_GRAY;

				if (objectUnit.getObject().isSelectionBlocked()) {
					foreGround = new Color(200, 30, 50);
				}

				setForeground(selected && enabled ? Color.WHITE : foreGround);
				return render;
			}
		});
	}

	@Override
	protected void onDoubleClick(Item item) {
		if (((GameObjectItem) item).getObject() instanceof GameObjectRef ref && ref.isValid()) {
			var res = MiEngine.getProject().getResource(ref.getResourceId());
			var vm = Application.getInstance().getViewManager();
			if (vm.containsView(res.getId()))
				vm.setActiveView(res.getId());
			else {
				// better use the resolver!!!
				Application.getInstance().getViewManager().addViewAndSetActive(new Scene2DEditor(res));
			}
		}
	}

	protected List<FMenuItem> getPopupMenuItems() {
		if (items == null) {
			Map<String, Action> actions = Application.getInstance().getActions();
			Setup.getActions(new OutlineActions(view)).entrySet().forEach(v -> actions.put(v.getKey(), v.getValue()));
			try {
				items = ContextMenuUtil.createItems(new FileInputStream("hearachy-scene-context.menu"), actions);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return items;
	}

	@Override
	protected void onDelete() {
		if (view.getCurrentView() instanceof Scene2DEditor)
			new OutlineActions(view).deleteAction();
	}

	@Override
	protected void onKeyPressed(int mods, int code) {
		if (view.getCurrentView() instanceof Scene2DEditor editor && code == KeyEvent.VK_D
				&& !editor.getSelectionManager().getSelection().isEmpty())
			new OutlineActions(view).duplicateAction();
	}

	@Override
	public boolean testDrop(Item target, int index, DropEvent event) {
		boolean v = super.testDrop(target, index, event);
		if (!v && event.getTransferable().isFlavorSupported(ItemsFlavor.class)) {
			var flavor = event.getTransferable().getFlavor(ItemsFlavor.class);
			if (flavor.getSingle() instanceof EngineResourceItem resource) {
				if (view.getCurrentView() instanceof Scene2DEditor editor) {
					return editor.getCanvas().canDrop(resource);
				}
			}
		}
		return v;
	}

	@Override
	public void onDropAccepted(Item target, int targetRow, Transferable transferable) {
		boolean handled = false;
		if (transferable.isFlavorSupported(ItemsFlavor.class)) {
			var flavor = transferable.getFlavor(ItemsFlavor.class);
			if (flavor.getSingle() instanceof EngineResourceItem resource) {
				if (view.getCurrentView() instanceof Scene2DEditor editor) {
					handled = true;
					editor.getCanvas().drop((GameObjectItem) target, null, resource);
				}
			}
		}
		if (!handled)
			super.onDropAccepted(target, targetRow, transferable);
	}

}
