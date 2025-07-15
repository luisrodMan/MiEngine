package com.nxtr.spengine.views.inspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ngeneration.furthergui.FButton;
import com.ngeneration.furthergui.FComboBox;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FLabel;
import com.ngeneration.furthergui.event.ChangeListener;
import com.ngeneration.miengine.scene.Component;
import com.ngeneration.miengine.scene.annotations.Select;
import com.ngeneration.miengine.scene.annotations.Tool;
import com.ngeneration.miengine.util.EngineSerializer;
import com.nxtr.easymng.Application;
import com.nxtr.spengine.views.inspector.handlers.AbstractHandler;
import com.nxtr.spengine.views.inspector.handlers.BasicDataTypeProvider;
import com.nxtr.spengine.views.inspector.handlers.ListHandlerProvider;
import com.nxtr.spengine.views.inspector.handlers.Handler;
import com.nxtr.spengine.views.inspector.handlers.HandlerProvider;
import com.nxtr.spengine.views.inspector.handlers.ReferenceHandler;
import com.nxtr.spengine.views.inspector.resolver.ObjectsHandlerProvider;
import com.nxtr.spengine.views.scene.Scene2DEditor;

public class InspectableObjectParser {

	private static List<HandlerProvider> propertyHandlerProviders = new LinkedList<HandlerProvider>();

	static {
		propertyHandlerProviders.add(new BasicDataTypeProvider());
//		propertyHandlerProviders.add(new ResourceHandler());
		propertyHandlerProviders.add(new ReferenceHandler());

		propertyHandlerProviders.add(new ListHandlerProvider());
		
		propertyHandlerProviders.add(new ObjectsHandlerProvider());
	}

	public static LinkedHashMap<FieldDescriptor, Handler> parse(Component component, List<FieldDescriptor> fields) {
		var map = new LinkedHashMap<FieldDescriptor, Handler>();
		fields.forEach(field -> {
			var parsed = parse(component, field);
			if (parsed != null)
				map.put(field,
						parsed.getHandler(field.getType(),
								List.of(field.getAnnotations() == null ? new Annotation[0] : field.getAnnotations())
										.stream().collect(Collectors.toMap(a -> a.annotationType(), a -> a)),
								field.getValue()));
			else
				System.out.println("could not parse field: " + field.getName());
		});
		return map;
	}

	public static HandlerProvider parse(Component component, FieldDescriptor field) {
		var annotations = List.of(field.getAnnotations() == null ? new Annotation[0] : field.getAnnotations()).stream()
				.collect(Collectors.toMap(a -> a.annotationType(), a -> a));

		var annotations1 = List
				.of(field.getType().getAnnotations() == null ? new Annotation[0] : field.getType().getAnnotations())
				.stream().collect(Collectors.toMap(a -> a.annotationType(), a -> a));

		var annotation = annotations.get(Tool.class);
		if (annotation == null)
			annotation = annotations1.get(Tool.class);

		if (component != null && annotation instanceof Tool tool) {
			HandlerProvider handler = new HandlerProvider() {
				@Override
				public Handler getHandler(Class<?> type,
						Map<? extends Class<? extends Annotation>, Annotation> annotations, Object initialValue) {
					var toolLabel = tool.label();
					var toolClass = tool.tool();
					var btn = new FButton(toolLabel);
					btn.addActionListener(event -> {
						var editorView = Application.getInstance().getViewManager().getActiveViews().stream()
								.filter(Scene2DEditor.class::isInstance).findAny().orElse(null);
						if (editorView instanceof Scene2DEditor editor) {
							try {
								com.nxtr.spengine.views.scene.Tool tool = (com.nxtr.spengine.views.scene.Tool) Class
										.forName(toolClass).getConstructor(Component.class, type)
										.newInstance(component, initialValue);
								editor.getCanvas().setTool(tool);
							} catch (InstantiationException | IllegalAccessException | ClassNotFoundException
									| IllegalArgumentException | InvocationTargetException | NoSuchMethodException
									| SecurityException e) {
								e.printStackTrace();
							}
						}
					});
					return new Handler() {

						@Override
						public void setValue(Object value) {

						}

						@Override
						public void setChangeListener(ChangeListener changeListener) {
						}

						@Override
						public void handleFinal(Object object, Object newValue) {
						}

						@Override
						public Object getValue() {
							return null;
						}

						@Override
						public FComponent getComponent() {
							return btn;
						}
					};
				}

				@Override
				public boolean canHandleFinal() {
					return false;
				}

				@Override
				public boolean canHandeType(Class<?> type,
						Map<? extends Class<? extends Annotation>, Annotation> annotations) {
					return false;
				}
			};
			return handler;
		} else if (annotations.get(Select.class) instanceof Select select) {
			var combo = new FComboBox<String>();
			List.of(select.value()).forEach(option -> combo.addItem(option));
			// change selected value
		} else if (propertyHandlerProviders.stream().filter(h -> h.canHandeType(field.getType(), annotations)).findAny()
				.orElse(null) instanceof HandlerProvider handler) {
			return handler;
		}

		// default handler -- show as label!!!
		return new HandlerProvider() {

			@Override
			public Handler getHandler(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations,
					Object initialValue) {
				return new AbstractHandler() {

					FLabel label = new FLabel(String.valueOf(initialValue));

					@Override
					public void setValue(Object value) {
						super.setValue(value);
						label.setText(String.valueOf(value));
					}

					@Override
					public FComponent getComponent() {
						return label;
					}
				};
			}

			@Override
			public boolean canHandleFinal() {
				return false;
			}

			@Override
			public boolean canHandeType(Class<?> type,
					Map<? extends Class<? extends Annotation>, Annotation> annotations) {
				return true;
			}
		};
	}

	public static LinkedHashMap<FieldDescriptor, Handler> parseObject(Object component) {
		return parse(component instanceof Component ? (Component) component : null, getDescritors(component));
	}

	public static List<FieldDescriptor> getDescritors(Object component) {
		List<FieldDescriptor> fields = new LinkedList<>();
		for (Field field : EngineSerializer.getSerializableFields(component.getClass())) {
			var descriptor = new FieldDescriptor();
			try {
				descriptor.setType(field.getType());
				descriptor.setName(field.getName());
				descriptor.setValue(field.get(component));
				descriptor.setAnnotations(field.getAnnotations());
				fields.add(descriptor);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return fields;
	}

}
