package com.nxtr.spengine.views.assets;

import java.util.LinkedList;
import java.util.List;

import com.ngeneration.furthergui.FOptionPane;
import com.ngeneration.miengine.graphics.OrthographicCamera;
import com.ngeneration.miengine.scene.Camera;
import com.ngeneration.miengine.scene.GameObject;
import com.nxtr.easymng.Application;
import com.nxtr.easymng.Editor;
import com.nxtr.easymng.hearachy.Item;
import com.nxtr.easymng.workspace.Resource;
import com.nxtr.spengine.EngineUtil;
import com.nxtr.spengine.MiEngine;
import com.nxtr.spengine.dialogs.MiEngineDialogs;
import com.nxtr.spengine.project.MiEngineProject;

public class AssetsActions {

	public void mainSceneAction() {
		var assets = getAssets();
		Resource resource = getSingleSelectedResource(assets);
		if (resource != null && resource.getExt().endsWith(EngineUtil.SCENE_EXT)) {
			getProject().getConfiguration().put("initialScene", resource.getId());
			getProject().saveConfiguration();
		}
	}

	public void newFolderAction() {
		var assets = getAssets();
		Resource resource = getSingleSelectedFolder(assets);
		String value = FOptionPane.showInputDialog("New Folder");
		if (value != null && !value.isBlank())
			assets.createFolder(resource, value);
	}

	public void newSceneAction() {
		Resource folder = getSingleSelectedFolder(getAssets());
		var scene = new GameObject();
		GameObject child = new GameObject("Main Camera");
		Camera camera = new Camera();
		camera.setCamera(new OrthographicCamera());
		child.addComponent(camera);
		scene.addChild(child);
		var sceneResource = MiEngineDialogs.newScene(folder, scene, null);
		if (sceneResource != null) {
//			getAssets().expandAndSelect(null);
			getAssets().getSelectionManager().setSelection(List.of(sceneResource));

		}
	}

	public void renameAction() {
		var assets = getAssets();
		Resource resource = getSingleSelectedResource(assets);
		if (resource.getPath().length > 3) {
			// W/P/ASSETS
			var ext = resource.getExt();
			String value = FOptionPane.showInputDialog("Rename", resource.getSimpleName());
			if (value == null)
				return;
			var newName = value + ((ext == null || ext.isEmpty()) ? "" : "." + ext);
			resource.rename(newName);
		}
	}

	public List<Resource> deleteAction() {
		var assets = getAssets();
		int selectionCount = assets.getSelectionManager().getSelectionCount();
		var roots = assets.getSelectionManager().getRootSelection(List.of(assets.getAssetsRoot()));

		List<Resource> deleted = new LinkedList<>();
		if (!roots.isEmpty() && FOptionPane.showConfirmDialog(null, "Delete",
				String.format("Delete these %d elements?", selectionCount)) == FOptionPane.YES) {
			roots.forEach(d -> {
				if (d != assets.getAssetsRoot())
					delete(d, deleted);
			});
		}
		return deleted;
	}

	private void delete(Item item, List<Resource> deleted) {
		var resource = (Resource) item;
		if (resource.isDirectory()) {
			for (Item i : resource.getItems()) {
				if (i instanceof Resource) {
					delete(i, deleted);
				}
			}
		}
		resource.getFile().delete();
		resource.getParentItem().remove(resource);
		Application.getInstance().getViewManager().getViews().forEach(v -> {
			if (v instanceof Editor editor && editor.getResource().getId().equals(resource.getId()))
				Application.getInstance().getViewManager().closeView(resource.getId());// force close xd!!!
		});
		deleted.add(resource);
	}

	public void refreshAction() {
		var assets = getAssets();
		Resource resource = getSingleSelectedResource(assets);
		var project = MiEngine.getProject();
		reload(resource, project);
	}

	private void reload(Resource parent, MiEngineProject project) {
		if (parent.isDirectory()) {
			for (var vv : project.sortFiles(parent.getFile().listFiles())) {
				var found = parent.getItems().stream()
						.filter(f -> ((Resource) f).getFile().getName().equals(vv.getName())).findAny().orElse(null);
				if (found == null) {
					found = project.createResource(parent, vv);
					parent.add(found);
				}
				reload((Resource) found, project);
			}
		}
	}

	private Resource getSingleSelectedFolder(AssetsView assets) {
		var resource = getSingleSelectedResource(assets);
		if (resource.getFile().isFile())
			resource = (Resource) resource.getParentItem();
		return resource;
	}

	private Resource getSingleSelectedResource(AssetsView assets) {
		var selection = assets.getSelectionManager().getSelection();
		Resource resource = null;
		if (selection.size() == 1) {
			if (selection.get(0) instanceof Resource res)
				resource = res;
			else
				resource = selection.get(0).getProject();
		}
		if (resource == null)
			resource = assets.getProject();
		return resource;
	}

	private static AssetsView getAssets() {
		return (AssetsView) Application.getInstance().getViewManager().getViewById(AssetsView.ID);
	}

	private static MiEngineProject getProject() {
		return MiEngine.getProject();
	}

}
