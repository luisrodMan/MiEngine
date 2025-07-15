package com.nxtr.spengine.views.inspector.handlers;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ngeneration.furthergui.FButton;
import com.ngeneration.furthergui.FComboBox;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FLabel;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.drag.DragInterface;
import com.ngeneration.furthergui.drag.DropEvent;
import com.ngeneration.furthergui.event.ChangeEvent;
import com.ngeneration.furthergui.event.ChangeListener;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.layout.GridBagConstraints;
import com.ngeneration.furthergui.layout.GridBagLayout;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.miengine.scene.annotations.ListType;
import com.ngeneration.miengine.scene.invoke.Function;
import com.ngeneration.miengine.scene.invoke.ObjectCallback;
import com.nxtr.easymng.hearachy.ItemsFlavor;
import com.nxtr.spengine.views.scene.GameObjectItem;

public class ListHandlerProvider extends HandlerProviderByType {

	public ListHandlerProvider() {
		super(new Class<?>[] { Collection.class });
	}

	@Override
	public boolean canHandeType(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
		boolean can = super.canHandeType(type, annotations);
		if (can) {
			var annotation = (ListType) annotations.get(ListType.class);
			if (annotation != null && annotation.value() == ObjectCallback.class)
				return true;
		}
		return false;
	}

	@Override
	public Handler getHandler(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations,
			Object initialValue) {
		return new CallbackHandler(initialValue);
	}

	private class CallbackHandler implements Handler {

		private Object value;
		private InternalComponent component;
		private ChangeListener changeListener;

		public CallbackHandler(Object value) {
			this.value = value;
		}

		@Override
		public Object getValue() {
			List<ObjectCallback> list = new ArrayList<>();
			for (var comp : component.list.getComponents()) {
				var item = (Item) comp;
				if (item.callback != null && item.callback.getObject() != null && item.callback.getComponent() != null
						&& item.callback.getMethod() != null && item.callback.getMethod().name != null
						&& !item.callback.getMethod().name.equals("None"))
					list.add(item.callback);
			}
			return list;
		}

		@Override
		public void setValue(Object value) {
			this.value = value;
			component.setValue(value);
		}

		@Override
		public FComponent getComponent() {
			return component != null ? component : (component = new InternalComponent(value));
		}

		@Override
		public void handleFinal(Object object, Object newValue) {
			throw new RuntimeException("Not implemented!!");
		}

		@Override
		public void setChangeListener(ChangeListener changeListener) {
			this.changeListener = changeListener;
		}

		private class InternalComponent extends FPanel {

			FPanel list;

			public InternalComponent(Object value) {
				setValue(value);
			}

			public void setValue(Object value) {
				if (value == null)
					removeAll();

				list = new FPanel(new FlowLayout(FlowLayout.TOP_TO_BOTTOM, 5, true));
				list.setBackground(new Color(50, 50, 55));
				list.setPadding(new Padding(5));

				if (value != null) {
					for (var v : (List<?>) value) {
						FPanel item = null;
						if (v instanceof ObjectCallback c) {
							item = new Item(c, changeListener);
						}
						if (item != null)
							list.add(item);
					}
				}
				FPanel buttons = new FPanel(new FlowLayout(FlowLayout.LEFT, 0));
				buttons.setPadding(new Padding(20, 0));
				FButton addBtn = new FButton("+");
				addBtn.setPadding(new Padding(15, 5));
				FButton delBtn = new FButton("-");
				delBtn.setPadding(new Padding(15, 5));
				buttons.add(addBtn);
				buttons.add(delBtn);

				addBtn.addActionListener(event -> {
					list.add(new Item(null, changeListener));
					list.revalidate();
				});

				setLayout(new FlowLayout(FlowLayout.TOP_TO_BOTTOM, 0, true));
				add(list);
				add(buttons);
				setPadding(new Padding(0, 20));
			}

		}

	}

	private class Item extends FPanel {

		public ObjectCallback callback;

		public Item(ObjectCallback callback, ChangeListener changeListener) {
			this.callback = callback;
			if (callback == null) {
				callback = new ObjectCallback();
				this.callback = callback;
			}

			setBackground(new Color(50, 50, 55));
			GridBagLayout layout = new GridBagLayout(1, 2);
			setLayout(layout);

			FComboBox<String> right = new FComboBox<>();
			right.setBackground(Color.DARK_GRAY);
			var name = callback.object == null ? "None" : callback.getObject().getName();

			var left = new ObjectContainer(name, callback, right);
			left.setOpaque(true);
			left.setTextAlign(FLabel.RIGHT);
			left.setBackground(new Color(30, 40, 60));

			fillCombo(right, this.callback);
			right.addActionListener(event -> {
				var item = right.getSelectedItem();
				if (item != null && !item.equals("None")) {
					var values = item.split("\\.");
					this.callback.component = this.callback.object.getComponents().stream()
							.filter(c -> c.getClass().getSimpleName().equals(values[0])).findAny().orElse(null);
					if (this.callback.component != null)
						this.callback.method = new Function(values[1]);
				}
				changeListener.stateChanged(new ChangeEvent(this));
			});

			GridBagConstraints constraints = new GridBagConstraints();
			constraints.weightH = 0.5f;
			constraints.fillHorizontal = true;
			constraints.fillVertical = true;
			add(left, constraints);
			constraints.col = 1;
			constraints.weightH = 0.5F;
			constraints.fillHorizontal = true;
			add(right, constraints);
		}

	}

	private void fillCombo(FComboBox<String> right, ObjectCallback callback) {
		right.addItem("None");
		if (callback != null && callback.getObject() != null) {
			for (var comp : callback.getObject().getComponents()) {
				for (var method : comp.getClass().getDeclaredMethods()) {
					if (method.getParameterCount() == 0)
						right.addItem(comp.getClass().getSimpleName() + "." + method.getName());
				}
			}
		}
		if (callback != null && callback.getComponent() != null && callback.getMethod() != null) {
			right.setSelectedItem(callback.getComponent().getClass().getSimpleName() + "." + callback.getMethod().name);
		}

	}

	class ObjectContainer extends FLabel implements DragInterface {

		private FComboBox<String> combo;
		private ObjectCallback callback;

		public ObjectContainer(String string, ObjectCallback callback, FComboBox<String> methods) {
			super(string);
			this.combo = methods;
			this.callback = callback;
		}

		@Override
		public void onDrag(DropEvent event) {

		}

		@Override
		public void onDrop(DropEvent event) {
			if (event.getEventType() == DropEvent.TEST_EVENT) {
				var l = event.getTransferable();
				if (l.isFlavorSupported(ItemsFlavor.class)) {
					var itemFlavor = l.getFlavor(ItemsFlavor.class);
					if (itemFlavor.getSingle() instanceof GameObjectItem) {
						event.consume();
					}
				}
			}
			if (event.getEventType() == DropEvent.ACCEPTED_EVENT) {
				var object = (GameObjectItem) event.getTransferable().getFlavor(ItemsFlavor.class).getSingle();
				combo.removeAllItems();
				callback.object = object.getObject();
				setText(object.getName());
				fillCombo(combo, callback);
				event.consume();
			}
		}
	}

}
