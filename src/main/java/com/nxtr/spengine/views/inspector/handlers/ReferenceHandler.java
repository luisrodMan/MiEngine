package com.nxtr.spengine.views.inspector.handlers;

import java.lang.annotation.Annotation;
import java.util.Map;

import com.ngeneration.furthergui.FButton;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FMenuItem;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.FPopupMenu;
import com.ngeneration.furthergui.drag.DragInterface;
import com.ngeneration.furthergui.drag.DropEvent;
import com.ngeneration.furthergui.drag.Flavor.ObjectFlavor;
import com.ngeneration.furthergui.event.ChangeEvent;
import com.ngeneration.furthergui.event.ChangeListener;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.miengine.scene.Component;
import com.ngeneration.miengine.scene.GameObject;
import com.ngeneration.miengine.util.indexer.ResourceItem;
import com.nxtr.easymng.hearachy.ItemsFlavor;
import com.nxtr.spengine.project.EngineResourceItem;
import com.nxtr.spengine.views.scene.GameObjectItem;

public class ReferenceHandler extends HandlerProviderByType {

	public ReferenceHandler() {
		super(new Class<?>[] { Component.class, ResourceItem.class, GameObject.class });
		// resources open !!!
		/// texture? change
		/// shader?? shader options
	}

	@Override
	public boolean canHandleFinal() {
		return false;
	}

	@Override
	public Handler getHandler(Class<?> type,
			Map<? extends Class<? extends Annotation>, Annotation> annotations, Object initialValue) {
		InputComponentInternal component = new InputComponentInternal();
		component.setType(type);
		component.setValue(initialValue);
		return component;
	}

	private class InputComponentInternal implements Handler {

		private Class<?> type;
		private ChangeListener changeListener;
		private Object value;
		private ReferenceInputField field = new ReferenceInputField();
		private FComponent component = new FPanel2();

		private class FPanel2 extends FPanel {
			public FPanel2() {
				super(new BorderLayout());
				final FButton drop = new FButton("o");
				add(field);
				add(drop, BorderLayout.EAST);

				drop.addActionListener(e -> {
					FPopupMenu menu = new FPopupMenu();
					menu.add(new FMenuItem("Select"));
					menu.add(new FMenuItem("Remove")).addActionListener(ee -> setValue(null));
					menu.showVisible(drop, 0, drop.getHeight());
				});
			}
		}

		@Override
		public void setValue(Object value) {
			if (value != null) {
				if (value instanceof ResourceItem res)
					field.setText(res.getName());
				else if (value instanceof Component comp)
					field.setText(comp.gameObject.getName() + " (" + type.getSimpleName() + ")");
				else if (value instanceof GameObject comp)
					field.setText(comp.getName());

			} else
				field.setText("None (" + type.getSimpleName() + ")");
			if (value == this.value)
				return;
			this.value = value;
			if (changeListener != null)
				changeListener.stateChanged(new ChangeEvent(field));
		}

		@Override
		public void setChangeListener(ChangeListener changeListener) {
			this.changeListener = changeListener;
		}

		@Override
		public void handleFinal(Object object, Object newValue) {
		}

		@Override
		public Object getValue() {
			return value;
		}

		@Override
		public FComponent getComponent() {
			return component;
		}

		public void setType(Class<?> type) {
			this.type = type;
		}

		private class ReferenceInputField extends FPanel implements DragInterface {

			private String text = "";

			@Override
			public void onDrag(DropEvent event) {
			}

			@Override
			public void onDrop(DropEvent event) {
				Object single = null;
				if (event.getTransferable().isFlavorSupported(ObjectFlavor.class))
					single = event.getTransferable().getFlavor(ObjectFlavor.class).getValue();
				else if (event.getTransferable().isFlavorSupported(ItemsFlavor.class))
					single = event.getTransferable().getFlavor(ItemsFlavor.class).getSingle();

				if (event.getEventType() == DropEvent.TEST_EVENT) {
					if (single instanceof EngineResourceItem item) {
						if (type.isAssignableFrom(item.getResourceItem().getClass()))
							event.acept(DropEvent.COPY_MODE);
					} else if (single instanceof GameObjectItem item) {
						if (type.isAssignableFrom(GameObject.class) || (Component.class.isAssignableFrom(type)
								&& item.getObject().getComponent((Class<Component>) type) != null))
							event.acept(DropEvent.COPY_MODE);
					} else if (single instanceof Component component && type.isAssignableFrom(component.getClass()))
						event.acept(DropEvent.COPY_MODE);
				} else if (event.getEventType() == DropEvent.ACCEPTED_EVENT) {
					if (single instanceof EngineResourceItem item) {
						setValue(item.getResourceItem());
					} else if (single instanceof GameObjectItem item) {
						if (type.isAssignableFrom(GameObject.class))
							setValue(item.getObject());
						else
							setValue(item.getObject().getComponent((Class<Component>) type));
					} else if (single instanceof Component)
						setValue(single);
				}
			}

			@Override
			public Dimension getPrefferedSize() {
				return getFont().getStringBounds(text).add(2, 2);
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.LIGTH_GRAY);
				g.drawString(2, (getHeight() - getFont().getFontHeight()) / 2, text);
			}

			public void setText(String name) {
				this.text = name;
				repaint();
			}
		};
	};

}
