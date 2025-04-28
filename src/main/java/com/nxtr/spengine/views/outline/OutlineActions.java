package com.nxtr.spengine.views.outline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ngeneration.furthergui.FOptionPane;
import com.ngeneration.miengine.math.Vector2;
import com.ngeneration.miengine.scene.GameObject;
import com.ngeneration.miengine.scene.RectangleRenderer;
import com.ngeneration.miengine.scene.ui.Button;
import com.ngeneration.miengine.scene.ui.Panel;
import com.ngeneration.miengine.scene.ui.Text;
import com.nxtr.easymng.util.Util;
import com.nxtr.spengine.MiEngine;
import com.nxtr.spengine.views.scene.GameObjectItem;
import com.nxtr.spengine.views.scene.Scene2DEditor;

public class OutlineActions {

	private OutlineView outlineView;

	public OutlineActions(OutlineView view) {
		this.outlineView = view;
	}

	private Scene2DEditor getEditor() {
		return ((Scene2DEditor) outlineView.getCurrentView());
	}

	private GameObjectItem getRoot() {
		return (GameObjectItem) ((Scene2DEditor) outlineView.getCurrentView()).getRoot();
	}

	private GameObjectItem getSingleSelection() {
		Scene2DEditor editor = ((Scene2DEditor) outlineView.getCurrentView());
		return (GameObjectItem) editor.getSelectionManager().getSingleSelection();
	}

	private Collection<GameObjectItem> getRootSelection() {
		Scene2DEditor editor = ((Scene2DEditor) outlineView.getCurrentView());
		return editor.getSelectionManager().getRootSelection().stream().map(GameObjectItem.class::cast).toList();
	}

	private void createChild(GameObjectItem parent, GameObject child) {
		var name = child.getName();
		name = name != null && !name.isBlank() ? name.trim() : "GameObject";
		String suffix = MiEngine.getProject().getConfiguration().getPropertyParser("misc")
				.getString("objectCopyPattern", " (#)");

		var valid = name.matches("[^<>:\"/\\|?*]+") && !name.isBlank();
		if (!valid)
			name = "GameObject";
		// next name
		while (parent.getItemByName(name) != null)
			name = Util.nextName(suffix, name);
		name = FOptionPane.showInputDialog("Name", "", name);

		while (true && name != null) {
			valid = name.matches("[^<>:\"/\\|?*]+") && !name.isBlank();
			if (valid)
				break;
			else
				name = FOptionPane.showInputDialog("Name Conflict", "Enter new name for '" + name + "':" + name, "");
		}

		if (name != null) {
			child.setName(name);
			GameObjectItem newObject;
			parent.add(List.of(newObject = new GameObjectItem(parent, child)));

			// center position
			var canvas = getEditor().getCanvas();
			newObject.getObject().transform.setLocation(
					canvas.getCamera().getWorld2D(new Vector2(canvas.getWidth() / 2, canvas.getHeight() / 2)));

			outlineView.getSelectionManager().setSelection(newObject);
			outlineView.getSelectionManager().setOpen(newObject);
			outlineView.getSelectionManager().setToVisible(newObject);
		}
	}

	public void newGameObjectAction() {
		var parent = getSingleSelection();
		createChild(parent, new GameObject("GameObject"));
	}

	public void newRectangleAction() {
		var parent = getSingleSelection();
		GameObject child = new GameObject("Rectangle");
		child.addComponent(new RectangleRenderer());
		createChild(parent, child);
	}

	public void newPanelAction() {
		var parent = getSingleSelection();
		GameObject child = new GameObject("Panel");
		child.addComponent(new Panel());
		createChild(parent, child);
	}

	public void newTextAction() {
		var parent = getSingleSelection();
		GameObject child = new GameObject("Text");
		child.addComponent(new Text("Text"));
		createChild(parent, child);
	}

	public void newButtonAction() {
		var parent = getSingleSelection();
		GameObject child = new GameObject("Button");
		child.addComponent(new Button());
		GameObject text = new GameObject("Text");
		text.addComponent(new Text("Button"));
		child.addChild(text);
		child.setAsSelectionRoot(true);
		createChild(parent, child);
		child.getChild(0).getComponent(Text.class).text = child.getName();
	}

	public void newSceneAction() {
//		var assets = getAssets();
//		Resource resource = getSingleSelectedFolder(assets);
//		String value = FOptionPane.showInputDialog("New Scene");
//		if (value != null && !value.isBlank()) {
//			File file = new File(resource.getFile(), value + ".scn");
//			try (FileWriter writer = new FileWriter(file)) {
//				GameObject go = new GameObject();
//				go.setName(value);
//				writer.write(EngineSerializer.serialize(go, null));
//				SimpleResource newResource;
//				resource.add(List.of(newResource = new SimpleResource(resource, file)));
//				assets.setSelection(List.of(newResource));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
	}

	public void renameAction() {
		var object = getSingleSelection();
		String value = FOptionPane.showInputDialog("Rename Object", object.getName());
		if (value != null && !value.isBlank())
			object.rename(value);
	}

	public void deleteAction() {
		var parentSelection = new ArrayList<>(getRootSelection());
		parentSelection.remove(getRoot());
		parentSelection.forEach(toDelete -> {
			((GameObjectItem) toDelete.getParentItem()).remove(toDelete);
		});
	}

	public void duplicateAction() {
		var parentSelection = new ArrayList<>(getRootSelection());
		parentSelection.remove(getRoot());
		if (!parentSelection.isEmpty()) {
			parentSelection.forEach(toClone -> {
				var parent = (GameObjectItem) toClone.getParentItem();
				GameObjectItem newItem = null;
				parent.add(
						newItem = new GameObjectItem(parent, MiEngine.getProject().cloneObject(toClone.getObject())));
				getEditor().getSelectionManager().setSelection(newItem);
			});
		}
	}

	public void blockObjectAction() {
		var object = getSingleSelection();
		if (object != null) {
			object.getObject().setSelectionBlocked(!object.getObject().isSelectionBlocked());
			outlineView.getComponent().repaint();
		}
	}

	public void setAsSelectionRootAction() {
		var object = getSingleSelection();
		if (object != null) {
			object.getObject().setAsSelectionRoot(!object.getObject().isSelectionRoot());
			outlineView.getComponent().repaint();
		}
	}

}
