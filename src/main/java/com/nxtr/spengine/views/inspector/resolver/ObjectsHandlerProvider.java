package com.nxtr.spengine.views.inspector.resolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.ngeneration.furthergui.DefaultListCellRenderer;
import com.ngeneration.furthergui.FComboBox;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FLabel;
import com.ngeneration.furthergui.FList;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.event.ChangeEvent;
import com.ngeneration.furthergui.event.ChangeListener;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.miengine.scene.annotations.Objects;
import com.nxtr.spengine.views.inspector.InspectableObjectParser;
import com.nxtr.spengine.views.inspector.controls.FFieldsComponent;
import com.nxtr.spengine.views.inspector.handlers.AbstractHandler;
import com.nxtr.spengine.views.inspector.handlers.FieldHandler;
import com.nxtr.spengine.views.inspector.handlers.HandlerProviderByAnnotations;

public class ObjectsHandlerProvider extends HandlerProviderByAnnotations {

	public ObjectsHandlerProvider() {
		super(new Class<?>[] { Objects.class });
	}

	@Override
	public Handler getHandler(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations,
			Object initialValue) {
		var handler = new Handler((Objects) annotations.get(Objects.class));
		handler.setValue(initialValue);
		return handler;
	}

	private class Handler extends AbstractHandler {

		private InternalComponent component;

		public Handler(Objects objectAnnotation) {
			component = new InternalComponent(objectAnnotation.value());
		}

		@Override
		public FComponent getComponent() {
			return component;
		}

		@Override
		public void setValue(Object value) {
			super.setValue(value);
			component.setValue(value);
		}

		@Override
		public Object getValue() {
			return component.getValue();
		}

		@Override
		public void setChangeListener(ChangeListener changeListener) {
			component.setChangeListener(changeListener);
		}

		private class InternalComponent extends FPanel {

			private FComboBox<Object> combo = new FComboBox<>();
			private ChangeListener changeListener;

			public InternalComponent(Class<?>[] values) {
				setLayout(new BorderLayout());
				combo.setRenderer(new DefaultListCellRenderer<Object>() {
					@Override
					public FComponent getRendererComponent(FList<Object> list, Object value, boolean isSelected,
							boolean cellHasFocus, int row) {
						super.getRendererComponent(list, value, isSelected, cellHasFocus, row);
						setText(value.getClass().getSimpleName());
						return this;
					}
				});

				combo.addActionListener(event -> {
					if (getComponentCount() > 1)
						remove(1);

					// create object fields
					var shapeItem = combo.getSelectedItem();
					if (shapeItem == null)
						return;
					var map = InspectableObjectParser.parseObject(shapeItem);
					var container = new FFieldsComponent(map);
					container.setListener(h -> {
						h.apply(shapeItem);
						if (changeListener != null)
							changeListener.stateChanged(new ChangeEvent(InternalComponent.this));
					});

					if (changeListener != null)
						changeListener.stateChanged(new ChangeEvent(InternalComponent.this));
					add(container, BorderLayout.CENTER);
					revalidate();
				});

				add(combo, BorderLayout.NORTH);
				for (var item : values) {
					try {
						combo.addItem(item.getConstructor().newInstance());
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
					}
				}
			}

			public Object getValue() {
				return combo.getSelectedItem();
			}

			public void setChangeListener(ChangeListener changeListener) {
				this.changeListener = changeListener;
			}

			public void setValue(Object value) {
				var idx = 0;
				for (var item : combo.getItems()) {
					if (item.getClass() == value.getClass()) {
						break;
					}
					idx++;
				}

				combo.removeItem(idx);
				combo.addItem(idx, value);
				combo.setSelectedItem(value);
			}

		}

	}

}
