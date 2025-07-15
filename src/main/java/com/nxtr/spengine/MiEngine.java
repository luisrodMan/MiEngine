package com.nxtr.spengine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.ngeneration.furthergui.FButton;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FFrame;
import com.ngeneration.furthergui.FOptionPane;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.FurtherApp;
import com.ngeneration.furthergui.event.Action;
import com.ngeneration.furthergui.event.KeyStroke;
import com.ngeneration.furthergui.event.WindowAdapter;
import com.ngeneration.furthergui.event.WindowEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.layout.GridBagConstraints;
import com.ngeneration.furthergui.layout.GridBagLayout;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.miengine.Engine;
import com.ngeneration.miengine.scene.GameObjectRef;
import com.ngeneration.miengine.scene.Scene;
import com.ngeneration.miengine.util.EngineSerializer;
import com.ngeneration.miengine.util.Util;
import com.nxtr.easymng.AbstractApplication;
import com.nxtr.easymng.Application;
import com.nxtr.easymng.Editor;
import com.nxtr.easymng.PropertyParser;
import com.nxtr.easymng.Setup;
import com.nxtr.easymng.View;
import com.nxtr.easymng.hearachy.ItemListener;
import com.nxtr.easymng.hearachy.ItemListenerAdapter;
import com.nxtr.easymng.hearachy.PropertyEvent;
import com.nxtr.easymng.view.DefaultViewDescriptor;
import com.nxtr.easymng.view.DefaultViewManager;
import com.nxtr.easymng.view.ViewManagerAdapter;
import com.nxtr.easymng.workspace.Resource;
import com.nxtr.easymng.workspace.SimpleWorkspace;
import com.nxtr.easymng.workspace.WorkspaceDescriptor;
import com.nxtr.spengine.project.EngineResource;
import com.nxtr.spengine.project.MiEngineProject;
import com.nxtr.spengine.views.assets.AssetsView;
import com.nxtr.spengine.views.console.ConsoleView;
import com.nxtr.spengine.views.inspector.InspectorView;
import com.nxtr.spengine.views.outline.OutlineView;
import com.nxtr.spengine.views.scene.GameObjectItem;
import com.nxtr.spengine.views.scene.Scene2DEditor;

public class MiEngine extends AbstractApplication {

	private FFrame frame = new FFrame("Simple Engine");
	private ItemListener resourceListener = new ItemListenerAdapter() {

		@Override
		public void onPropertyChanged(PropertyEvent event) {
			if (event.getName().equals(Resource.CONTENT_PROPERTY)) {
				System.out.println("Content updated for: " + event.getSource().getName());
				var resource = (EngineResource) event.getSource();
				if (resource.getExt().equals("scn")) {

					EngineSerializer.removeCache(Integer.parseInt(resource.getId()));

					var editors = getViewManager().getEditors().stream().filter(Scene2DEditor.class::isInstance)
							.filter(e -> e.getResource() != resource).map(Scene2DEditor.class::cast).toList();

					System.out.println("scene updated xddxxddx!!!");

					editors.forEach(editor -> {

						System.out.println("updating editor: " + editor.getTitle());
						var gameObject = (GameObjectItem) editor.getRoot();
						List<GameObjectItem> affected = new LinkedList<GameObjectItem>();
						check(gameObject, Integer.parseInt(resource.getId()), affected, null);

						affected.forEach(e -> {
							System.out.println("affected: " + e.getName());

							var newObject = getProject().load(((GameObjectRef) e.getObject()).getResourceId());

							// copy values
							var locL = e.getObject().transform.getLocalLocation();
							var locR = e.getObject().transform.getLocalRotation();
							var locS = e.getObject().transform.getLocalScale();

							int idx = e.getObject().getParentGameObject().getChildren().indexOf(e.getObject());

							var parent = e.getParentItem();
							e.getParentItem().remove(e);

							GameObjectItem io = new GameObjectItem((GameObjectItem) parent, newObject);
							parent.add(idx, List.of(io));
							newObject.transform.setLocalLocation(locL);
							newObject.transform.setLocalRotation(locR);
							newObject.transform.setLocalScale(locS);
//							
//							e.getObject().removeAllChildren();
//							e.getObject().removeAllComponents();
//							var comp = newObject.getComponents();
//							comp.remove(newObject.transform);
//							e.getObject().addComponents(comp);
//							
//							var children = newObject.getChildren();
//							newObject.removeAllChildren();
//							e.getObject().addChildren(children);

							getControl().repaint();

						});

					});

				}
			}
		}

		private GameObjectItem check(GameObjectItem item, int id, List<GameObjectItem> affected,
				GameObjectItem topRef) {
			var ori = topRef;
			if (item.getObject() instanceof GameObjectRef ref) {
				if (topRef == null)
					topRef = item;
				if (ref.getResourceId() == id) {
					affected.add(topRef);
					return topRef;
				}
			}

			for (var i : item.getItems()) {

				var result = check((GameObjectItem) i, id, affected, topRef);
				if (result != null) {
					if (ori != null) {
						if (result != ori)
							return result;
					}
				}
			}

			return null;
		}
	};

	public MiEngine() {
		// setup engine
		Engine.env = Engine.ENV.EDITOR;

		restore("settings.json");
		if (getWorkspace() == null)
			loadDefaultUI();

		// restore keys
		getActions().entrySet()
				.forEach(a -> frame.getContainerComponent().getActionMap().put(a.getKey(), a.getValue()));
		getKeys().forEach(key -> {
			frame.getContainerComponent().getInputMap(FFrame.WHEN_IN_FOCUSED_WINDOWS)
					.put(KeyStroke.getKeyStroke(key.getBinding()), key.getCommand());
		});

		frame.addWindowsListener(new WindowAdapter() {
			@Override
			public void windowClossin(WindowEvent event) {
				onClossingApplication();
			}
		});

		frame.getContainerComponent().add(((DefaultViewManager) getViewManager()).getComponent());

		// toolbar
		FPanel toolbar = new FPanel(new GridBagLayout(1, 3), new Padding(6));
		toolbar.setBackground(new Color(20, 20, 20));
		// execute btn centered
		// play/ pause /increase speed?
		var playBtn = new FButton("Play");
		var playCurrentBtn = new FButton("Current");
		var tempFile = new File(getProject().getDirectory(), "temp");

		playBtn.addActionListener(event -> {
			var initial = getProject().getConfiguration().getInt("initialScene", -1);
			String content = null;
			if (initial == -1) {
				var active = getActiveScene();
				if (active != null) {
					if (FOptionPane.showConfirmDialog(null, "No Scene",
							"Set current scene as main?") == FOptionPane.YES) {
						getProject().getConfiguration().put("initialScene", active.getResource().getId());
						getProject().saveConfiguration();
						initial = Integer.parseInt(active.getResource().getId());
						content = active.getContent();
					}
				}
			}
			if (initial != -1) {
				if (content == null) {
					if (getViewManager().getViewById("" + initial) instanceof Scene2DEditor editor)
						content = editor.getContent();
					else {
						var template = getProject().getResourceIndexer().getTemplate(initial);
						if (template != null && new File(template.getPath()).exists()) {
							Path originalPath = new File(template.getPath()).toPath();
							try {
								Files.copy(originalPath, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
							} catch (IOException e) {
								e.printStackTrace();
								return;
							}
							executeScene(tempFile.getName());
						}
					}
				}
			}
			if (content != null) {
				saveToTempScene(tempFile, content);
				executeScene(tempFile.getName());
			}
		});
		playCurrentBtn.addActionListener(event -> {
			Scene2DEditor editor = getActiveScene();
			saveToTempScene(tempFile, editor.getContent());
			executeScene(tempFile.getName());
		});

		getViewManager().addViewManagerListener(new ViewManagerAdapter() {

			public void onViewAdded(com.nxtr.easymng.view.ViewManager viewMng, com.nxtr.easymng.View view) {
				setupView(view);
			};

			public void onViewRemoved(com.nxtr.easymng.view.ViewManager viewMng, com.nxtr.easymng.View view) {
				removeView(view);
			}

		});
		getViewManager().getViews().forEach(this::setupView);

		FPanel executePanel = new FPanel(new FlowLayout(FlowLayout.CENTER, 1));
		executePanel.add(playBtn);
		executePanel.add(playCurrentBtn);

		GridBagConstraints constraints = new GridBagConstraints(0, 1, 1, 1, 1, 0);
		constraints.anchor = GridBagConstraints.CENTER;
		toolbar.add(executePanel, constraints);
		frame.add(toolbar, BorderLayout.NORTH);
		//

		frame.setDefaultCloseOperation(FFrame.EXIT_ON_CLOSE);
		frame.setDimension(1100, 850);
//		frame.setPrefferedSize(new Dimension(200, 200));
		frame.getContainerComponent().setBackground(Color.RED);
		frame.setLocationRelativeTo(null);
		frame.validate();
	}

	private void saveToTempScene(File tempFile, String content) {
		try {
			Util.write(tempFile, content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Scene2DEditor getActiveScene() {
		Scene2DEditor editor = null;
		if (getViewManager().getFocusedView() instanceof Scene2DEditor edit)
			editor = edit;
		if (editor == null)
			editor = (Scene2DEditor) getViewManager().getActiveViews().stream().filter(Scene2DEditor.class::isInstance)
					.findAny().orElse(null);
		return editor;
	}

	private void setupView(View view) {
		if (view instanceof Editor editor) {
			editor.getResource().addChangeListener(resourceListener);
		}
	}

	private void removeView(View view) {
		if (view instanceof Editor editor) {
			editor.getResource().removeChangeListener(resourceListener);
		}
	};

	public void setVisible() {
		frame.setVisible(true);
	}

	protected Map<String, Action> loadActions() {
		return Setup.getActions(new Actions());
	}

	protected void loadDefaultUI() {
		System.out.println("loading default workspace...");
		WorkspaceDescriptor descriptor = new WorkspaceDescriptor();
		descriptor.setConfiguration(PropertyParser.getParser(new JsonObject()));
		descriptor.setDirectory("game/sapito");
		var workspace = new SimpleWorkspace(descriptor);
		setWorkspace(workspace);
		getViewManager().addView(new OutlineView(), new DefaultViewDescriptor("LT"));
		getViewManager().addView(new AssetsView(), new DefaultViewDescriptor("LB"));
		getViewManager().addView(new InspectorView(), new DefaultViewDescriptor("R"));
		Scene scene = new Scene();
		scene.setName("Mi Scene");
		Scene scene2 = new Scene();
		scene2.setName("Mi Scene 2");
//		getViewManager().addView(new Scene2DEditor(new GameObjectItem(scene)), new DefaultViewDescriptor("C"));
//		getViewManager().addView(new Scene2DEditor(new GameObjectItem(scene2)), new DefaultViewDescriptor("C"));
//		getViewManager().getViews().forEach(v -> {
//			System.out.println("path: " + ((DefaultViewManager) getViewManager()).getPath(v));
//		});
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		FurtherApp app = new FurtherApp();
		app.setWidth(1200);
		app.setHeight(900);
		app.run((xd) -> {
			var engine = new MiEngine();
			engine.frame.setDimension(app.getWidth(), app.getHeight());
			engine.setVisible();
			app.addPropertyListener(event -> {
				if (event.getProperty().equals(FComponent.DIMENSION_PROPERTY)) {
					engine.frame.setDimension(app.getWidth(), app.getHeight());
					engine.frame.revalidate();
				}
			});
		});
	}

	@Override
	public FComponent getControl() {
		return frame;
	}

	public static MiEngineProject getProject() {
		return (MiEngineProject) Application.getInstance().getWorkspace().getFirstProject();
	}

	public void openOtherScript(String classPath) {
		String fullpath = "C:\\Users\\luisr\\git\\MiEngineLib\\" + "\\src\\main\\java\\" + classPath.replace(".", "\\")
				+ ".java";
		openScriptFullpath(fullpath, 0);
	}

	public void openScript(String classPath) {
		openScript(classPath, -1);
	}

	public void openScript(String classPath, int line) {
		openScriptFullpath(getProject().getDirectory() + "\\src\\main\\java\\" + classPath.replace(".", "\\") + ".java",
				line);
	}

	private void openScriptFullpath(String fullpath, int line) {
		try {
			var a = 0;
			var commands = new String[3];
			commands[a++] = "C:\\eclipse\\eclipse";
			commands[a++] = "--launcher.openFile";
			commands[a++] = fullpath + (line < 0 ? "" : ":" + line);
			new ProcessBuilder(commands).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String compilerDir = "C:\\eclipse\\plugins\\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.5.v20241023-1957\\jre\\bin";

	private String getClassPath() {
		var prjPath = getProject().getDirectory().getAbsolutePath();
		return "\"" + prjPath
				+ "\\target\\classes;C:\\Users\\luisr\\git\\MiEngineLib\\target\\classes;C:\\Users\\luisr\\.m2\\repository\\org\\lwjgl\\lwjgl\\3.3.3\\lwjgl-3.3.3.jar;C:\\Users\\luisr\\.m2\\repository\\org\\lwjgl\\lwjgl-glfw\\3.3.3\\lwjgl-glfw-3.3.3.jar;C:\\Users\\luisr\\.m2\\repository\\org\\lwjgl\\lwjgl-nfd\\3.3.3\\lwjgl-nfd-3.3.3.jar;C:\\Users\\luisr\\.m2\\repository\\org\\lwjgl\\lwjgl-openal\\3.3.3\\lwjgl-openal-3.3.3.jar;C:\\Users\\luisr\\.m2\\repository\\org\\lwjgl\\lwjgl-opengl\\3.3.3\\lwjgl-opengl-3.3.3.jar;C:\\Users\\luisr\\.m2\\repository\\org\\lwjgl\\lwjgl-stb\\3.3.3\\lwjgl-stb-3.3.3.jar;C:\\Users\\luisr\\.m2\\repository\\org\\lwjgl\\lwjgl\\3.3.3\\lwjgl-3.3.3-natives-windows.jar;C:\\Users\\luisr\\.m2\\repository\\org\\lwjgl\\lwjgl-glfw\\3.3.3\\lwjgl-glfw-3.3.3-natives-windows.jar;C:\\Users\\luisr\\.m2\\repository\\org\\lwjgl\\lwjgl-nfd\\3.3.3\\lwjgl-nfd-3.3.3-natives-windows.jar;C:\\Users\\luisr\\.m2\\repository\\org\\lwjgl\\lwjgl-openal\\3.3.3\\lwjgl-openal-3.3.3-natives-windows.jar;C:\\Users\\luisr\\.m2\\repository\\org\\lwjgl\\lwjgl-opengl\\3.3.3\\lwjgl-opengl-3.3.3-natives-windows.jar;C:\\Users\\luisr\\.m2\\repository\\org\\lwjgl\\lwjgl-stb\\3.3.3\\lwjgl-stb-3.3.3-natives-windows.jar;C:\\Users\\luisr\\.m2\\repository\\com\\google\\code\\gson\\gson\\2.8.9\\gson-2.8.9.jar\"";
	}

	public void compileScript(String cannonicalName) {
		var prjPath = getProject().getDirectory().getAbsolutePath();
		var command = new String[6];
		command[0] = compilerDir + "\\javac.exe";
		command[1] = "-classpath";
		command[2] = getClassPath();
		command[3] = "-d";
		command[4] = prjPath + "/target/classes";
		command[5] = prjPath + "/src/main/java/" + cannonicalName.replace(".", "/") + ".java";
		try {
			var p =new ProcessBuilder().command(command).directory(new File(prjPath)).redirectErrorStream(true).start();
			var bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private void executeScene(String sceneRelativePath) {

		if (true) {
			getViewManager().addView(new ConsoleView(), new DefaultViewDescriptor("CB", "0.5,0.5:0.5,0.5"));
		}
		var prjPath = getProject().getDirectory().getAbsolutePath();
		var command = new String[9];
		command[0] = compilerDir + "\\javaw.exe";
		command[1] = "-Dfile.encoding=UTF-8";
		command[2] = "-Dstdout.encoding=UTF-8";
		command[3] = "-Dstderr.encoding=UTF-8";
		command[4] = "-classpath";
		command[5] = getClassPath();
		command[6] = "-XX:+ShowCodeDetailsInExceptionMessages";
		command[7] = "Launcher";
		command[8] = sceneRelativePath;

		try {
			var process = new ProcessBuilder().command(command).directory(new File(prjPath)).redirectErrorStream(true)
					.start();
			new Thread(() -> {
				var bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = null;
				try {
					if (getViewManager().getViewById(ConsoleView.ID) instanceof ConsoleView console) {
						console.clear();
						int linesCount = 0;
						StringBuilder buffer = new StringBuilder();
						while ((line = bufferedReader.readLine()) != null) {
							if (!buffer.isEmpty())
								buffer.append(System.lineSeparator());
							buffer.append(line);
							linesCount++;
							if (linesCount == 60) {
								console.appendLine(buffer.toString());
								buffer.delete(0, buffer.length());
								Thread.sleep(5);
								linesCount = 0;
							}
						}
						if (!buffer.isEmpty())
							console.appendLine(buffer.toString());
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}).start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static MiEngine getInstance() {
		return (MiEngine) Application.getInstance();
	}

}
