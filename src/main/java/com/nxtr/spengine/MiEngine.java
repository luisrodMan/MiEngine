package com.nxtr.spengine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FFrame;
import com.ngeneration.furthergui.FurtherApp;
import com.ngeneration.furthergui.event.Action;
import com.ngeneration.furthergui.event.WindowAdapter;
import com.ngeneration.furthergui.event.WindowEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.miengine.scene.Scene;
import com.nxtr.easymng.AbstractApplication;
import com.nxtr.easymng.MapPropertyParser;
import com.nxtr.easymng.keys.KeyContainer;
import com.nxtr.easymng.view.DefaultViewDescriptor;
import com.nxtr.easymng.view.DefaultViewManager;
import com.nxtr.easymng.workspace2.SimpleWorkspace;
import com.nxtr.easymng.workspace2.WorkspaceDescriptor;
import com.nxtr.spengine.views.assets.AssetsView;
import com.nxtr.spengine.views.inspector.InspectorView;
import com.nxtr.spengine.views.outline.OutlineView;
import com.nxtr.spengine.views.scene.Scene2DView;
import com.nxtr.spengine.views.scene.SceneHolder;

public class MiEngine extends AbstractApplication {

	private FFrame frame = new FFrame("Simple Engine");

	public MiEngine() {

		if (getWorkspace() == null) {
			loadDefaultUI();
		}

		frame.addWindowsListener(new WindowAdapter() {
			@Override
			public void windowClossin(WindowEvent event) {
				onClossingApplication();
			}
		});

		frame.getContainerComponent().add(((DefaultViewManager) getViewManager()).getComponent());

		frame.setDefaultCloseOperation(FFrame.EXIT_ON_CLOSE);
		frame.setDimension(1100, 850);
//		frame.setPrefferedSize(new Dimension(200, 200));
		frame.getContainerComponent().setBackground(Color.RED);
		frame.setLocationRelativeTo(null);
		frame.validate();
		frame.setVisible(true);
	}

	protected void loadDefaultUI() {
		System.out.println("loading default workspace");
		WorkspaceDescriptor descriptor = new WorkspaceDescriptor();
		descriptor.setConfiguration(new MapPropertyParser());
		descriptor.setDirectory("C:/Users/luis/Desktop/game/sapito/workspace");
		var workspace = new SimpleWorkspace(descriptor);
		setWorkspace(workspace);
		getViewManager().addView(new OutlineView(), new DefaultViewDescriptor("LT"));
		getViewManager().addView(new AssetsView(), new DefaultViewDescriptor("LB"));
		getViewManager().addView(new InspectorView(), new DefaultViewDescriptor("R"));
		Scene scene = new Scene();
		scene.setName("Mi Scene");
		Scene scene2 = new Scene();
		scene2.setName("Mi Scene 2");
		getViewManager().addView(new Scene2DView(new SceneHolder(scene)), new DefaultViewDescriptor("C"));
		getViewManager().addView(new Scene2DView(new SceneHolder(scene2)), new DefaultViewDescriptor("C"));
		getViewManager().getViews().forEach(v -> {
			System.out.println("path: " + ((DefaultViewManager) getViewManager()).getPath(v));
		});
	}

//	public static void main(String[] args) {
//		FurtherApp app = new FurtherApp();
//		app.setWidth(1200);
//		app.setHeight(900);
//
//		app.run((xd) -> {
//			new SimpleEngine();
//		});
//	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		FurtherApp app = new FurtherApp();
		app.setWidth(1200);
		app.setHeight(900);
		app.run((xd) -> {
			new MiEngine();
		});
//
//		WorkspaceDescriptor w = WorkspaceDescriptor.load(new File("C:\\Users\\luis\\Desktop\\game\\sapito\\workspace"));
//
//		System.out.println("xd1: " + w.getName());
//		System.out.println("xd2: " + w.getType());
//		System.out.println("xd3: " + w.getVersion());
//		System.out.println("xd4: " + w.getDirectory());
//		System.out.println("xd5: " + w.getProjects().get(0).getDirectory());
	}

	@Override
	public FComponent getControl() {
		return frame;
	}

	@Override
	protected Map<String, Action> loadActions() {
		return null;
	}

	@Override
	protected KeyContainer loadKeys() {
		return null;
	}

}
