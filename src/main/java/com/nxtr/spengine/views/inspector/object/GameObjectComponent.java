package com.nxtr.spengine.views.inspector.object;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.ngeneration.furthergui.FButton;
import com.ngeneration.furthergui.FComboBox;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FLabel;
import com.ngeneration.furthergui.FMenu;
import com.ngeneration.furthergui.FMenuItem;
import com.ngeneration.furthergui.FOptionPane;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.FPopupMenu;
import com.ngeneration.furthergui.FScrollPane;
import com.ngeneration.furthergui.FurtherApp;
import com.ngeneration.furthergui.drag.DragInterface;
import com.ngeneration.furthergui.drag.DropEvent;
import com.ngeneration.furthergui.drag.ObjectTransferable;
import com.ngeneration.furthergui.event.MouseAdapter;
import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.layout.GridBagConstraints;
import com.ngeneration.furthergui.layout.GridBagLayout;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.miengine.math.Rectangle;
import com.ngeneration.miengine.math.Vector3;
import com.ngeneration.miengine.scene.Component;
import com.ngeneration.miengine.scene.GameObject;
import com.ngeneration.miengine.scene.Renderer;
import com.ngeneration.miengine.scene.Transform;
import com.ngeneration.miengine.scene.physics.BoxCollider;
import com.ngeneration.miengine.scene.physics.CircleCollider;
import com.ngeneration.miengine.scene.physics.Collider;
import com.nxtr.spengine.EngineUtil;
import com.nxtr.spengine.MiEngine;
import com.nxtr.spengine.dialogs.NewScriptDialog;
import com.nxtr.spengine.project.ComponentWraper;
import com.nxtr.spengine.project.MiEngineProject;
import com.nxtr.spengine.views.inspector.FieldDescriptor;
import com.nxtr.spengine.views.inspector.InspectableObjectParser;
import com.nxtr.spengine.views.inspector.controls.FCollapsableComponent;
import com.nxtr.spengine.views.inspector.controls.FFieldsComponent;
import com.nxtr.spengine.views.inspector.handlers.AbstractHandler;
import com.nxtr.spengine.views.inspector.handlers.BasicDataTypeProvider;
import com.nxtr.spengine.views.inspector.handlers.FieldHandler;
import com.nxtr.spengine.views.inspector.handlers.Handler;
import com.nxtr.spengine.views.scene.GameObjectItem;

public class GameObjectComponent extends FPanel {

	private FPanel container;
	private Map<Object, FFieldsComponent> containers = new HashMap<>();
	private MiEngineProject project;
	private GameObjectItem gameObjectItem;
	private BiConsumer<Object, FieldHandler> consumer;

	public GameObjectComponent(MiEngineProject project, GameObjectItem Item,
			BiConsumer<Object, FieldHandler> consumer) {
		super(new BorderLayout());
		this.project = project;
		this.gameObjectItem = Item;
		this.consumer = consumer;
		createComponent();
	}

	public void updateProperty(GameObject object, Component component, String propertyName, Object value) {
		if (Thread.currentThread() == FurtherApp.getInstance().getThread())
			updateProperty2(object, component, propertyName, value);
		else
			FurtherApp.getInstance().invokeLater(() -> {
				updateProperty2(object, component, propertyName, value);
			});
	}

	private void updateProperty2(GameObject object, Component component, String propertyName, Object value) {
		var container = containers.get(component);
		if (container != null && propertyName != null)
			container.updateProperty(propertyName, value);
		else if (component != null)
			refresh();
	}

	public void refresh() {
		if (Thread.currentThread() == FurtherApp.getInstance().getThread()) {
			createComponent();
			revalidate();
		} else {
			FurtherApp.getInstance().invokeLater(() -> {
				createComponent();
				revalidate();
			});
		}
	}

	private void createComponent() {
		super.removeAll();
		var containerLayout = new FlowLayout(FlowLayout.TOP_TO_BOTTOM);
		containerLayout.setGap(0);
		containerLayout.setFilled(true);
		container = new FPanel(containerLayout);
		setPadding(new Padding(0));

		// Object Name
		// components
		// add btn

		var gameObject = gameObjectItem.getObject();

		FPanel nameContainer = new FPanel();
		nameContainer.setPadding(new Padding(5, 10, 5, 5));
		nameContainer.setLayout(new GridBagLayout(2, 5));

		FButton iconBtn = new FButton("Icon");
		var enableDisableObject = new BasicDataTypeProvider().getHandler(boolean.class, null, gameObject.isActive());
		enableDisableObject.setChangeListener(event -> {
			consumer.accept(gameObject, new FieldHandler("active", enableDisableObject));
		});
		var nameField = new BasicDataTypeProvider().getHandler(String.class, null,
				gameObject.getName() == null ? "" : gameObject.getName());
		nameField.setChangeListener(event -> {
			consumer.accept(gameObject, new FieldHandler("name", nameField));
		});
//		nameField.setPadding(new Padding(5, 1));
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.rows = 2;
		nameContainer.add(iconBtn, constraints);

		constraints = new GridBagConstraints(0, 1, 1, 1, 0, 0);
		constraints.anchor = GridBagConstraints.CENTER;
		nameContainer.add(enableDisableObject.getComponent(), constraints);

		constraints = new GridBagConstraints(0, 2, 1, 3, 1, 0);
		constraints.fillHorizontal = true;
		constraints.margin = new Padding(3, 7);
		nameContainer.add(nameField.getComponent(), constraints);

		FLabel tagLb = new FLabel("Tag");
		FComboBox<String> tagCb = new FComboBox<>();
		FLabel layerLb = new FLabel("Layer");
		FComboBox<String> layerCb = new FComboBox<>();

		constraints = new GridBagConstraints(1, 1, 1, 1, 0, 0);
		constraints.margin = new Padding(7, 2);
		constraints.anchor = GridBagConstraints.CENTER;
		nameContainer.add(tagLb, constraints);

		constraints = new GridBagConstraints(1, 2, 1, 1, 1, 0);
		constraints.fillHorizontal = true;
		nameContainer.add(tagCb, constraints);

		constraints = new GridBagConstraints(1, 3, 1, 1, 0, 0);
		constraints.margin = new Padding(7, 2);
		constraints.anchor = GridBagConstraints.CENTER;
		nameContainer.add(layerLb, constraints);

		constraints = new GridBagConstraints(1, 4, 1, 1, 1, 0);
		constraints.fillHorizontal = true;
		nameContainer.add(layerCb, constraints);

		tagCb.addItem("Untagged");
		tagCb.addItem("Respawn");
		tagCb.addItem("EditorOnly");
		tagCb.addItem("MainCamera");
		tagCb.addItem("Player");
		for (var tag : MiEngine.getProject().getConfiguration().getPropertyParser("misc").getArray("tags"))
			tagCb.addItem(tag);
		tagCb.addItem("Add Tag...");
		gameObject.setTag(tagCb.containsItem(gameObject.getTag()) ? gameObject.getTag() : null);
		tagCb.setSelectedItem(gameObject.getTag() == null ? "Untagged" : gameObject.getTag());
		tagCb.addActionListener(event -> {
			if (tagCb.getSelectedIndex() == tagCb.getItemCount() - 1) {
				var nTag = FOptionPane.showInputDialog("New Tag");
				if (nTag != null && !nTag.isBlank() && !tagCb.containsItem(nTag.trim())) {
					var tags = MiEngine.getProject().getConfiguration().getPropertyParser("misc").getArray("tags");
					var ntags = new String[tags.length + 1];
					for (int i = 0; i < tags.length; i++)
						ntags[i] = tags[i];
					ntags[ntags.length - 1] = nTag.trim();
					MiEngine.getProject().getConfiguration().getPropertyParser("misc").put("tags", ntags);
					MiEngine.getProject().saveConfiguration();
					tagCb.addItem(tagCb.getItemCount() - 1, nTag.trim());
					gameObject.setTag(nTag.trim());
				}
			} else {
				gameObject.setTag(tagCb.getSelectedItem());
				// falta property listener xd
			}
		});

		layerCb.addItem("Default");

		container.add(nameContainer);

		// parse template and show
		int[] row = new int[] { 0 };
		gameObject.getComponents().forEach(component -> {

			boolean isScript = project.isScript(component);

			FPanel titleContainer = new FPanel(new GridBagLayout(1, 3));
			titleContainer.setBackground(Color.TRANSLUCENT);
			var enableDisableComponent = new BasicDataTypeProvider().getHandler(boolean.class, null,
					component.isEnabled());
			enableDisableComponent.setChangeListener(event -> {
				consumer.accept(component, new FieldHandler("enabled", enableDisableComponent));
			});
			var titleLb = new DraggableLabel(
					EngineUtil.toTitle(component.getClass().getSimpleName()) + (!isScript ? "" : " (Script)"),
					component) {
				@Override
				protected void processMouseEvent(MouseEvent event) {
					super.processMouseEvent(event);
					if (!event.isConsumed() && event.getClickCount() == 3
							&& event.getEventType() == MouseEvent.MOUSE_PRESSED) {
						if (isScript)
							MiEngine.getInstance().openScript(component.getClass().getName());
						else
							MiEngine.getInstance().openOtherScript(component.getClass().getName());
					}
				}
			};

			FPanel optionsPanel = new FPanel(new FlowLayout(FlowLayout.RIGHT));
			FButton options = new FButton(":");
			optionsPanel.add(options);

			var c = new GridBagConstraints();
			c.col = 0;
			c.weightH = 0;
			c.fillVertical = true;
			titleContainer.add(enableDisableComponent.getComponent(), c);
			c = new GridBagConstraints();
			c.col = 1;
			c.weightH = 1;
			c.fillVertical = true;
			c.fillHorizontal = true;
			titleContainer.add(titleLb, c);
			c = new GridBagConstraints();
			c.col = 2;
			c.weightH = 0;
			c.fillVertical = true;
			titleContainer.add(optionsPanel, c);

			LinkedHashMap<FieldDescriptor, Handler> map = null;
			component.gameObject = gameObject;
			if (row[0] == 0) {
				List<FieldDescriptor> data = new ArrayList<>(3);
				Transform t = (Transform) component;
				data.add(new FieldDescriptor("location", Vector3.class, t.getLocalLocation(), null));
				data.add(new FieldDescriptor("scale", Vector3.class, t.getLocalScale(), null));
				data.add(new FieldDescriptor("rotation", Vector3.class, t.getLocalRotation(), null));
				map = InspectableObjectParser.parse(component, data);
			} else
				map = InspectableObjectParser.parseObject(component);

			if (isScript) {
				var mm = new LinkedHashMap<FieldDescriptor, Handler>();
				mm.put(new FieldDescriptor("Script", null, null, null), new AbstractHandler() {
					private FComponent comp;

					@Override
					public FComponent getComponent() {
						if (comp == null) {
							FLabel label = new FLabel(component.getClass().getSimpleName());
							label.setOpaque(true);
							label.setBackground(Color.GRAY);
							label.setPadding(new Padding(5, 0));
							label.addMouseListener(new MouseAdapter() {
								public void mouseClicked(MouseEvent event) {
									MiEngine.getInstance().openScript(component.getClass().getName());
								}
							});
							comp = label;
						}
						return comp;
					}
				});
				mm.putAll(map);
				map = mm;
			}
			var container = new FFieldsComponent(map);
			container.setListener(h -> consumer.accept(component, h));
			containers.put(component, container);
			var componentControl = new FCollapsableComponent(null, titleContainer, container);
			this.container.add(createSeparator());
			this.container.add(componentControl);

			options.addActionListener(event -> {
				var menu = new FPopupMenu();
				if (component != gameObject.transform) {
					menu.add(new FMenuItem("Remove")).addActionListener(e -> {
						gameObjectItem.removeComponent(component);
						int idx = this.container.getComponentIndex(componentControl);
						this.container.remove(idx - 1);// -1 separator
						this.container.remove(idx - 1);
						this.container.revalidate();
					});
					int componentIndex = gameObject.getComponents().indexOf(component);
					FMenuItem moveUpOption = null;
					menu.add(moveUpOption = new FMenuItem("Move Up")).addActionListener(e -> {
						gameObject.removeComponent(component);
						gameObject.addComponent(componentIndex - 1, component);
						int idx = this.container.getComponentIndex(componentControl);
						var deletedComponent = this.container.getComponent(idx - 2);
						this.container.remove(idx - 2);// -1 separator
						this.container.remove(idx - 2);
						this.container.add(idx - 1, createSeparator());
						this.container.add(idx, deletedComponent);
						this.container.revalidate();
					});
					moveUpOption.setEnabled(componentIndex > 1);
					FMenuItem moveDownOption = null;
					menu.add(moveDownOption = new FMenuItem("Move Down")).addActionListener(e -> {
						gameObject.removeComponent(component);
						gameObject.addComponent(componentIndex + 1, component);
						int idx = this.container.getComponentIndex(componentControl);
						var deletedComponent = this.container.getComponent(idx);
						this.container.remove(idx);// -1 separator
						this.container.remove(idx);
						this.container.add(idx + 1, createSeparator());
						this.container.add(idx + 2, deletedComponent);
						this.container.revalidate();
					});
					moveDownOption.setEnabled(componentIndex < gameObject.getComponentCount() - 1);
				}
				if (menu.getItemCount() > 0)
					menu.showVisible(options, 0, options.getHeight());
			});

			row[0]++;
		});

		container.add(createSeparator());
		var addBtn = new FButton("Add Component");
		container.add(addBtn);

		List<ComponentWraper> defaultComponents = new LinkedList<>();
		defaultComponents.add(new ComponentWraper("com.ngeneration.miengine.scene.SpriteRenderer", null));
		defaultComponents.add(new ComponentWraper("com.ngeneration.miengine.scene.RectangleRenderer", null));
		defaultComponents.add(new ComponentWraper("com.ngeneration.miengine.scene.PathComponent", null));
		defaultComponents.add(new ComponentWraper("com.ngeneration.miengine.scene.physics.CircleCollider", null));
		defaultComponents.add(new ComponentWraper("com.ngeneration.miengine.scene.physics.BoxCollider", null));
		defaultComponents.add(new ComponentWraper("com.ngeneration.miengine.scene.physics.RigidBody", null));
		defaultComponents.add(new ComponentWraper("com.ngeneration.miengine.scene.ui.Button", null));
		defaultComponents.add(new ComponentWraper("com.ngeneration.miengine.scene.ui.Panel", null));
		defaultComponents.add(new ComponentWraper("com.ngeneration.miengine.scene.ui.Text", null));

		addBtn.addActionListener(event -> {
			FPopupMenu menu = new FPopupMenu();
			var complist = project.getComponents();
			// sort
			Map<String, List<ComponentWraper>> subFolders = new HashMap<>();
			for (var i : complist) {
				var path = i.getQualifiedName();
				var subs = path.split("\\.");
				if (subs.length == 2)
					subFolders.computeIfAbsent("", k -> new LinkedList<>()).add(i);
				else
					subFolders.computeIfAbsent(subs[1], k -> new LinkedList<>()).add(i);
			}

			defaultComponents.forEach(c -> {
				menu.add(createComponentMenuItem(c.getName(), c, gameObject));
			});
			var gameFolder = (FMenu) menu.add(new FMenu("game"));
			for (var folder : subFolders.entrySet().stream().sorted((v1, v2) -> v1.getKey().compareTo(v2.getKey()))
					.toList()) {
				folder.getValue().sort((v1, v2) -> v1.getQualifiedName().compareTo(v2.getQualifiedName()));
				var parent = folder.getKey().equals("") ? gameFolder
						: (FMenu) gameFolder.add(new FMenu(folder.getKey()));
				var replace = "game.";
				folder.getValue().forEach(v -> {
					var name = v.getQualifiedName();
					name = name.substring(replace.length());
					int idx = name.indexOf(".");
					if (idx > 0)
						name = name.substring(idx + 1);
					parent.add(createComponentMenuItem(name, v, gameObject));
				});
			}
			menu.add(new FMenuItem("New Component")).addActionListener(e -> {
				// packages
				var root = new File(project.getSourcesDirectory(), "game");
				var rootPath = root.getAbsolutePath();
				List<File> collector = new LinkedList<>();
				collector.add(root);
				collectDirectories(root, collector);
				var packages = collector.stream()
						.map(f -> f.getAbsolutePath().replace(rootPath, "game").replace("\\", "."))
						.sorted((v1, v2) -> v1.compareTo(v2)).toList();
				var dialog = new NewScriptDialog(packages);
				dialog.setSelection("game");
				if (dialog.setVisible() == FOptionPane.YES) {
					// compile class
					// assign to object
					// open script on editor
					var cannonicalName = dialog.getCannonicalName();
					project.assignScript(gameObject, cannonicalName);
					refresh();
				}
			});

			menu.showVisible(addBtn, 0, addBtn.getHeight());
		});
		//

		add(new FScrollPane(container));
	}

	private void collectDirectories(File file, List<File> collector) {
		for (var f : file.listFiles())
			if (f.isDirectory()) {
				collector.add(f);
				collectDirectories(f, collector);
			}
	}

	private FMenuItem createComponentMenuItem(String name, ComponentWraper componentWrapper, GameObject gameObject) {
		var item = new FMenuItem(name);
		item.addActionListener(e -> {
			var component = componentWrapper.newComponent();
			if (component != null) {
				var rect = new Rectangle();
				if (component instanceof Collider
						&& gameObject.getComponent(Renderer.class) instanceof Renderer render) {
					render.getLocalBounds(rect);
					if (component instanceof BoxCollider box) {
						box.dimension = rect.getSize();
					} else if (component instanceof CircleCollider circle) {
						circle.radius = Math.min(rect.width, rect.height) * 0.5f;
					}
				}
				gameObjectItem.addComponent(component);
			}
		});
		return item;
	}

	private FComponent createSeparator() {
		var component = new FPanel();
		component.setPrefferedSize(new Dimension(10, 3));
		component.setBackground(Color.DARK_GRAY.darker().darker().darker());
		return component;
	}

	private class DraggableLabel extends FLabel implements DragInterface {

		private Component component;

		public DraggableLabel(String title, Component component) {
			super(title);
			this.component = component;
		}

		@Override
		public void onDrag(DropEvent event) {
			event.acept(new ObjectTransferable(component), DropEvent.COPY_MODE);
		}

		@Override
		public void onDrop(DropEvent event) {

		}

	}

}
