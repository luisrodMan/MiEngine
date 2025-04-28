package com.nxtr.spengine.project;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ngeneration.miengine.scene.Component;
import com.ngeneration.miengine.scene.GameObject;
import com.ngeneration.miengine.scene.GameObjectRef;
import com.ngeneration.miengine.util.EngineSerializer;
import com.ngeneration.miengine.util.Util;
import com.ngeneration.miengine.util.indexer.ResourceIndexer;
import com.nxtr.easymng.Application;
import com.nxtr.easymng.ApplicationListenerAdapter;
import com.nxtr.easymng.View;
import com.nxtr.easymng.hearachy.Item;
import com.nxtr.easymng.hearachy.ItemListener;
import com.nxtr.easymng.view.ViewManager;
import com.nxtr.easymng.view.ViewManagerAdapter;
import com.nxtr.easymng.workspace.ProjectDescriptor;
import com.nxtr.easymng.workspace.Resource;
import com.nxtr.easymng.workspace.SimpleProject;
import com.nxtr.spengine.MiEngine;
import com.nxtr.spengine.views.assets.AssetsView;
import com.nxtr.spengine.views.inspector.InspectableObjectParser;
import com.nxtr.spengine.views.scene.GameObjectItem;
import com.nxtr.spengine.views.scene.PropertyEvent;
import com.nxtr.spengine.views.scene.Scene2DEditor;

// Files   refresh to see moved files
// For Java Sources scan continuously
public class MiEngineProject extends SimpleProject {

	private static final Logger logger = LoggerFactory.getLogger(MiEngineProject.class);

	private ResourceIndexer resourceIndexer = new ResourceIndexer();

	private FileScanner scriptsScanner;
	private FileScanner filesScanner;

	private Map<String, ComponentWraper> components = new HashMap<>();
	private ClassLoader classLoader;
	private ItemListener itemListener;

	public MiEngineProject(Item parent) {
		super(parent);
		itemListener = new ItemListener() {
			@Override
			public void onWorkspaceItemRemoved(Item path, List<Item> removed) {
				removed.forEach(MiEngineProject.this::clearItem);
			}

			@Override
			public void onWorkspaceItemAdded(Item path, int index, List<? extends Item> items) {
				items.forEach(MiEngineProject.this::keepTrack);
			}

			@Override
			public void onPropertyChanged(com.nxtr.easymng.hearachy.PropertyEvent event) {
				if (event.getSource() instanceof Resource resource && event.getName().equals(Item.NAME_PROPERTY)) {
					var directory = resource.getRelativePathDirectory() + "/";
					int id = resourceIndexer.getIndex(directory + event.getOldValue());
					logger.info("trying to update resource path: " + directory + event.getOldValue());
					if (resource.getFile().isDirectory()) {
						resourceIndexer.renameDirectory(directory + event.getOldValue(),
								directory + resource.getName());
						try {
							resourceIndexer.persists();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else if (resourceIndexer.updateResourcePath(id, directory + resource.getName())) {
						try {
							logger.info("resource renamed: " + directory + resource.getName());
							resourceIndexer.persists();

							// update nodes not needed??? just inspector xddxdx???
							// Application.getInstance().getViewManager().getViews().stream().

						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		keepTrack(parent);
	}

	private void keepTrack(Item item) {
		item.addChangeListener(itemListener);
		if (item.getItemCount() > 0)
			item.getItems().forEach(this::keepTrack);
	}

	private void clearItem(Item item) {
		item.removeChangeListener(itemListener);
		if (item.getItemCount() > 0)
			item.getItems().forEach(this::clearItem);
	}

	public ResourceIndexer getResourceIndexer() {
		return resourceIndexer;
	}

	public File getAssetsDirectory() {
		return new File(getDirectory(), "Assets");
	}

	public File getSourcesDirectory() {
		return new File(getDirectory(), "src/main/java/game");
	}

	public File getCompiledDirectory() {
		return new File(getDirectory().getAbsolutePath(),
				String.join(File.separator, "target/classes/game".split("/")));
	}

	public Resource getResource(int id) {
		return getResource("" + id);
	}

	public Resource getResource(String id) {
		return (Resource) getProject().findItemById(id);
	}

	@Override
	public Resource addResource(Item parent, File file) {
		var res = super.addResource(parent, file);
		try {
			resourceIndexer.persists();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public Resource createResource(Item parent, File file) {
		Resource item = null;
		if (file.getAbsolutePath().startsWith(new File(getDirectory(), "Assets").getAbsolutePath())) {
			if (file.isFile()) {
				String resourcePath = Resource.getRelativePathDirectory(getDirectory(), file.getParentFile()) + "/"
						+ file.getName();
				int idx = resourceIndexer.getIndex(resourcePath);
				if (idx < 1)
					idx = resourceIndexer.register(resourcePath);
				if (idx > 0) {
					var template = resourceIndexer.getTemplate(idx);
					item = new EngineResource(parent, file, template);
				}
			}
		}
		return item != null ? item : super.createResource(parent, file);
	}

	@Override
	public void load(ProjectDescriptor descriptor) {
		// load index
		try {
			var dir = new File(new File(descriptor.getFile()).getParentFile(), "index");
			resourceIndexer.loadIndex(dir);
			resourceIndexer.setBasePath(dir.getParentFile().getAbsolutePath());
			super.load(descriptor);
			resourceIndexer.persists();
		} catch (IOException e) {
			e.printStackTrace();
		}
		createClassLoader();
	}

	private void createClassLoader() {
		try {
			classLoader = new URLClassLoader(new URL[] { getCompiledDirectory().getParentFile().toURL() });
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void release(Application application) {
		scriptsScanner.stop();
		filesScanner.stop();
	}

	@Override
	public void restored(Application application) {
		super.restored(application);
		application.getViewManager().addViewManagerListener(new ViewManagerAdapter() {
			@Override
			public void onViewAdded(ViewManager viewMng, View view) {
				super.onViewAdded(viewMng, view);
				checkScanner(application);
			}

			public void onViewRemoved(ViewManager viewMng, View view) {
				super.onViewRemoved(viewMng, view);
				checkScanner(application);
			}
		});
		application.addApplicationListener(new ApplicationListenerAdapter() {

		});

		scriptsScanner = new FileScanner(getCompiledDirectory(), this::parseScriptsResults, 1000);
		filesScanner = new FileScanner(getAssetsDirectory(), this::parseFilesResults, 2000);
		checkScanner(application);
		filesScanner.skipe(FileScanner.ALL);
		filesScanner.setMode(FileScanner.ALL);
//		filesScanner.start();
	}

	private void checkScanner(Application application) {
		var editorsCount = application.getViewManager().getActiveViews().stream()
				.filter(Scene2DEditor.class::isInstance).count();
		if (editorsCount == 0) {
			scriptsScanner.stop();
		} else if (editorsCount > 0) {
			scriptsScanner.start();
		}
	}

	private void addComponent(ComponentWraper componentWraper) {
		components.put(componentWraper.getQualifiedName(), componentWraper);
	}

	public Collection<ComponentWraper> getComponents() {
		return new ArrayList<>(components.values());
	}

	private static AssetsView getAssets() {
		return (AssetsView) Application.getInstance().getViewManager().getViewById(AssetsView.ID);
	}

	private void parseFilesResults(FileScanner scanner) {

		if (!scanner.getNewFiles().isEmpty()) {
			scanner.getNewFiles().entrySet().forEach(entry -> {
//				getAssets().getAssetsRoot().getItem
			});
		}

		System.out.println("new Objects: " + scanner.getNewFiles().size());
		scanner.getNewFiles().entrySet().forEach(v -> {
			System.out.println("new - " + v.getKey() + ":" + v.getValue().getPath());
		});
	}

	private void parseScriptsResults(FileScanner scanner) {

		scanner.getNewFiles().entrySet().forEach(entry -> {
			if (entry.getKey().endsWith(".class") && !entry.getKey().contains("$")) {
				var component = getComponent(entry.getKey());
				if (component != null) {
					if (components.containsKey(component.getQualifiedName()))
						updateComponent(component);
					else
						addComponent(component);
				}
			}
		});
		scanner.getModifiedFiles().entrySet().forEach(entry -> {
			if (entry.getKey().endsWith(".class") && !entry.getKey().contains("$")) {// $ inner classes
				var component = getComponent(entry.getKey());
				if (component != null) {
					if (components.containsKey(component.getQualifiedName()))
						updateComponent(component);
					else
						updateComponent(component);
				}
//				else if (components.get("game." + entry.getKey().replace(".class", "").replace(File.separator,
//						".")) instanceof ComponentWraper wrapper)
//					removeComponent(wrapper);
			}
		});
		scanner.getDeletedFiles().entrySet().forEach(entry -> {
			// remove component
			if (components.get("game." + entry.getKey().replace(".class", "").replace(File.separator,
					".")) instanceof ComponentWraper wrapper)
				removeComponent(wrapper);
		});
	}

	private void updateComponent(ComponentWraper component) {
		var activeObjects = getActiveObjects();
		activeObjects.stream().forEach(object -> {
			object.getObject().getComponents().stream().filter(component::isInstance).findAny().ifPresent(c -> {
				updateComponent(object, component);
			});
		});
		activeObjects.stream().forEach(active -> {
			traverseObjects(active.getObjectItems(), object -> {
				object.getObject().getComponents().stream().filter(component::isInstance).findAny().ifPresent(c -> {
					updateComponent(object, component);
				});
			});
		});
	}

	public GameObject load(int id) {
		return EngineSerializer.deserialize(id, resourceIndexer, classLoader);
	}

	public GameObject cloneObject(GameObject object) {
		if (object instanceof GameObjectRef ref) {
			GameObject o = EngineSerializer.deserialize(ref.getResourceId(), resourceIndexer, classLoader);
			o.transform.set(object.transform);
			o.setName(object.getName() + "xd");
			return o;
		} else
			return EngineSerializer.deserialize(0, EngineSerializer.serializeToJsonObject(object), resourceIndexer,
					classLoader);
	}

	private void copyValues(Component oldComponent, Component newComponent) {
		for (var descriptor : InspectableObjectParser.parseObject(oldComponent).keySet()) {
			try {
				Field newField = newComponent.getClass().getField(descriptor.getName());
				if (newField != null && newField.getType().getName().equals(descriptor.getType().getName())) {
					newField.setAccessible(true);
					newField.set(newComponent, descriptor.getValue());
				}
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				if (e instanceof NoSuchFieldException)
					System.out.println("Field not found in updated object: " + e.getMessage());
				else
					e.printStackTrace();
			}
		}
	}

	private void updateComponent(GameObjectItem objectWrapper, ComponentWraper componentWrapper) {
		var object = objectWrapper.getObject();
		var oldIndex = object.getComponents();
		object.getComponents().stream().filter(componentWrapper::isInstance).forEach(oldComponent -> {
			Component newComponent = componentWrapper.newInstance();
			copyValues(oldComponent, newComponent);
			object.removeComponent(oldComponent);
			try {
				object.addComponent(oldIndex.indexOf(oldComponent), (Component) newComponent);
			} catch (java.lang.Error error) {
				// compilation problem| checked on start()??
				error.printStackTrace();

			}
			GameObjectItem.fireEvent(
					new PropertyEvent(MiEngine.getInstance(), null, objectWrapper, newComponent, null, null));
		});
		components.put(componentWrapper.getQualifiedName(), componentWrapper);
	}

	private void removeComponent(ComponentWraper wrapper) {
		components.remove(wrapper.getQualifiedName());
		var activeObjects = getActiveObjects();
		activeObjects.stream().forEach(object -> {
			object.getObject().getComponents().stream().filter(wrapper::isInstance).forEach(object::removeComponent);
		});
		activeObjects.stream().forEach(object -> {
			traverseObjects(object.getObjectItems(), child -> {
				child.getObject().getComponents().stream().filter(wrapper::isInstance).forEach(object::removeComponent);
			});
		});
	}

	private void traverseObjects(Collection<GameObjectItem> objects, Consumer<GameObjectItem> consumer) {
		for (var gameObject : objects) {
			consumer.accept(gameObject);
			traverseObjects(gameObject.getObjectItems(), consumer);
		}
	}

	private Collection<GameObjectItem> getActiveObjects() {
		return Application.getInstance().getViewManager().getActiveViews().stream()
				.filter(Scene2DEditor.class::isInstance).map(Scene2DEditor.class::cast)
				.map(e -> (GameObjectItem) e.getRoot()).toList();
	}

	private ComponentWraper getComponent(String clazz) {
		boolean isComponent = false;
		try {
			try {
				classLoader = new URLClassLoader(new URL[] { getCompiledDirectory().getParentFile().toURL() });
				System.out.println("class loader: " + getCompiledDirectory().getParentFile().toURL());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

			var source = Util.readText(new File(getSourcesDirectory(), clazz.replace(".class", ".java")));
			// is component

			isComponent = source.contains("extends Component") || source.contains("extends Collider");
			if (!isComponent)
				return null;
		} catch (IOException e) {
			e.printStackTrace();
		}
//		return !isComponent ? null
//				: new ComponentWraper("game." + clazz.replace(".class", "").replace(File.separator, "."),
//						getClassLoader());

		try {
//			classLoader = new URLClassLoader(new URL[] { getCompiledDirectory().getParentFile().toURL() });
			var ins = new ComponentWraper("game." + clazz.replace(".class", "").replace(File.separator, "."),
					getClassLoader());
			return ins.newInstance() instanceof Component ? ins : null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public boolean isScript(Component component) {
		return !component.getClass().getName().startsWith(Component.class.getPackageName());
	}

}
