package com.nxtr.spengine.views.inspector.handlers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.ngeneration.furthergui.FCheckbox;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FLabel;
import com.ngeneration.furthergui.FOptionPane;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.FTextField;
import com.ngeneration.furthergui.event.ChangeEvent;
import com.ngeneration.furthergui.event.ChangeListener;
import com.ngeneration.furthergui.event.MouseAdapter;
import com.ngeneration.furthergui.layout.GridBagConstraints;
import com.ngeneration.furthergui.layout.GridBagLayout;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.furthergui.text.FTextComponent;
import com.ngeneration.miengine.graphics.Color;
import com.ngeneration.miengine.util.EngineSerializer;
import com.nxtr.spengine.views.inspector.controls.BasicInputsControl;

public class BasicDataTypeProvider implements HandlerProvider {

	@Override
	public Handler getHandler(Class<?> type,
			Map<? extends Class<? extends Annotation>, Annotation> annotations, Object initialValue) {
		return new InternalComponent(type, initialValue);
	}

	@Override
	public boolean canHandleFinal() {
		return true;
	}

	@Override
	public boolean canHandeType(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
		return type.isPrimitive() || EngineSerializer.isBasicType(type);
	}

	private class InternalComponent implements Handler {
		private Object template;
		private Field[] fields;
		private Class<?> type;
		private FComponent component;
		private ChangeListener changeListener;

		public InternalComponent(Class<?> type, Object initialValue) {
			try {
				this.type = type;
				component = createComponent(type, initialValue, v -> {
					if (changeListener != null)
						changeListener.stateChanged(new ChangeEvent(this.component));
				});
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		public FComponent createComponent(Class<?> type, Object initialValue, Consumer<Object> listener)
				throws InstantiationException, IllegalAccessException {
			if (type.isPrimitive() || type.getName().equals(String.class.getName())) {
				return BasicInputsControl.getPrimitiveComponent(type, initialValue, listener);
			} else {
				var ff = List.of(type.getDeclaredFields()).stream().filter(f -> {
					return !(Modifier.isTransient(f.getModifiers()) || Modifier.isStatic(f.getModifiers()));
				}).toList();
				fields = ff.toArray(new Field[ff.size()]);
				GridBagLayout grid = new GridBagLayout(1, fields.length * 2);
				var container = new FPanel(grid);
				template = type.newInstance();
				if (initialValue == null)
					initialValue = type.newInstance();
				if (initialValue instanceof Color initial) {
					FPanel btn = new FPanel();
					btn.setPrefferedSize(new Dimension(20, 20));
					btn.setBackground(new com.ngeneration.furthergui.graphics.Color(initial.toInt()));
					btn.addMouseListener(new MouseAdapter() {
						public void mouseReleased(com.ngeneration.furthergui.event.MouseEvent event) {
							if (btn.containsOnLocal(event.getLocation()) && FOptionPane.showColorDialog(btn, btn
									.getBackground()) instanceof com.ngeneration.furthergui.graphics.Color newColor) {
								template = new Color(newColor.toInt());
								btn.setBackground(newColor);
								btn.repaint();
								listener.accept(newColor);
							}
						};
					});
					return btn;
				}

				for (int i = 0; i < fields.length * 2; i++) {
					var field = fields[i / 2];
					field.setAccessible(true);
					FComponent component = null;
					var constraints = new GridBagConstraints();
					constraints.col = i;
					if (i % 2 == 0) {
						var label = new FLabel();
						label.setText(field.getName().toUpperCase());
						constraints.weightH = 0;
						constraints.margin = new Padding(5, 0, 3, 0);
						constraints.anchor = GridBagConstraints.LEFT;
						component = label;
					} else {
						var value = field.get(initialValue);
						field.set(template, value);
						constraints.weightH = 1;
						constraints.fillHorizontal = true;
						component = BasicInputsControl.getPrimitiveComponent(field.getType(), value, l -> {
							try {
								field.set(template, l);
								listener.accept(l);
							} catch (IllegalArgumentException | IllegalAccessException e) {
								e.printStackTrace();
							}
						});
						Dimension inputPreffered = component.getPrefferedSize();
						inputPreffered.width = 10;/// ??????????????
						component.setPrefferedSize(inputPreffered);
					}
					container.add(component, constraints);
				}
				return container;
			}
		}

		@Override
		public FComponent getComponent() {
			return component;
		}

		@Override
		public void setValue(Object value) {
			if (type.isPrimitive() || type.getName().equals(String.class.getName())) {
				if (value != null)
					setPrimitive(component, value);
			} else if (value != null) {
				if (value instanceof Color color) {
					getComponent().setBackground(new com.ngeneration.furthergui.graphics.Color(color.toInt()));
					getComponent().repaint();
				} else {
					for (int i = 0; i < fields.length; i++) {
						var input = getComponent().getComponent(i * 2 + 1);
						try {
							setPrimitive(input, fields[i].get(value));
						} catch (IllegalArgumentException | IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		private void setPrimitive(FComponent component, Object value) {
			if (component instanceof FCheckbox ch)
				ch.setSelected((boolean) value);
			else if (component instanceof FTextField tf)
				tf.setText(String.valueOf(value));
		}

		@Override
		public Object getValue() {
			if (type.isPrimitive()) {
				if (component instanceof FCheckbox ch)
					return ch.isSelected();
				else
					return EngineSerializer.getPrimitive(type, ((FTextComponent) component).getText());
			} else {
				// create new instance
				try {
					if (type == String.class)
						return ((FTextField) component).getText();
					Object newInstance = type.getConstructor().newInstance();
					for (var field : fields) {
						field.setAccessible(true);
						field.set(newInstance, field.get(template));
					}
					return newInstance;
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		public void handleFinal(Object object, Object newValue) {
			List.of(object.getClass().getDeclaredFields()).forEach(field -> {
				field.setAccessible(true);
				try {
					field.set(object, field.get(newValue));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			});
		}

		@Override
		public void setChangeListener(ChangeListener changeListener) {
			this.changeListener = changeListener;
		}

	}

}
