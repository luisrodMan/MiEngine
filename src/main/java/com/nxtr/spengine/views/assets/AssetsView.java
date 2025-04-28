package com.nxtr.spengine.views.assets;

import java.io.File;
import java.util.List;

import com.ngeneration.furthergui.FComponent;
import com.nxtr.easymng.Application;
import com.nxtr.easymng.ApplicationListenerAdapter;
import com.nxtr.easymng.Bundle;
import com.nxtr.easymng.ViewAdapter;
import com.nxtr.easymng.hearachy.Item;
import com.nxtr.easymng.hearachy.SelectionManager;
import com.nxtr.easymng.workspace.Resource;
import com.nxtr.easymng.workspace.SimpleResource;
import com.nxtr.easymng.workspace.Workspace;
import com.nxtr.spengine.project.MiEngineProject;

public class AssetsView extends ViewAdapter {

	public static final String ID = "AssetsView";
	private AssetsComponent assetsComponent = new AssetsComponent(this);

	public AssetsView() {
		super(ID, "Resources");
	}

	@Override
	public void restore(Bundle bundle) {
		super.restore(bundle);
		setRoot(bundle.getApplication().getWorkspace());
		bundle.getApplication().addApplicationListener(new ApplicationListenerAdapter() {
			@Override
			public void onWorkspaceChanged(Application application, Workspace old) {
				setRoot(application.getWorkspace());
			}
		});
	}

	private void setRoot(Workspace workspace) {
		assetsComponent.setRoot(workspace);
		assetsComponent.getComponent().revalidate();
	}

	public Resource getAssetsRoot() {
		return (Resource) assetsComponent.getRoot();
	}

	@Override
	public FComponent getComponent() {
		return assetsComponent.getComponent();
	}

	public SelectionManager getSelectionManager() {
		return assetsComponent.getSelectionManager();
	}

	public MiEngineProject getProject() {
		return (MiEngineProject) Application.getInstance().getWorkspace().getProjects().get(0);
	}

	public void createFolder(Resource resource, String value) {
		File mwqDir = new File(resource.getFile(), value);
		mwqDir.mkdir();
		List<Item> list = null;
		resource.add(list = List.of(new SimpleResource(resource, mwqDir)));
		assetsComponent.expandAndSelect(list);
	}
}
