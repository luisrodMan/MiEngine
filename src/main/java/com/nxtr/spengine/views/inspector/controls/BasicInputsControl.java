package com.nxtr.spengine.views.inspector.controls;

import java.util.function.Consumer;

import com.ngeneration.furthergui.FCheckbox;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FTextField;
import com.ngeneration.furthergui.event.PropertyEvent;
import com.ngeneration.furthergui.event.PropertyListener;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.furthergui.text.DocumentFilter;
import com.ngeneration.furthergui.text.FTextComponent;
import com.ngeneration.miengine.util.EngineSerializer;

public class BasicInputsControl {
	
	public static FComponent getPrimitiveComponent(Class<?> type, Object initialValue, Consumer<Object> listener) {
		FComponent component = new FTextField();
		if (type.getName().equals("boolean")) {
			var checkBox = new FCheckbox("", (boolean) initialValue);
			checkBox.addItemListener(event -> listener.accept(checkBox.isSelected()));
			component = checkBox;
		} else if (initialValue != null) {
			final var ft = component;
			((FTextField) component).setText(String.valueOf(initialValue));
			((FTextField) component).addPropertyListener(new PropertyListener() {
				@Override
				public void onPropertyChanged(PropertyEvent event) {
					if (event.getProperty().equals(FTextField.TEXT_PROPERTY))
						listener.accept(
								EngineSerializer.getPrimitive(type, ((FTextComponent) ft).getText()));
				}
			});
			String filter = null;
			if (type == Integer.class || type.getName().equals("int"))
				filter = "-?[0-9]*";
			else if (type == Float.class || type.getName().equals("float") || type == Double.class
					|| type.getName().equals("double"))
				filter = "-?[0-9]*\\.?[0-9]*";
			if (filter != null)
				((FTextField) component).setDocumentFilter(new NumberFilter(filter));
		}
		component.setPadding(new Padding(3, 2));
		return component;
	}
	
	private static class NumberFilter implements DocumentFilter {

		private String regex;

		public NumberFilter(String regex) {
			this.regex = regex;
		}

		@Override
		public void remove(Bypass filter, int offset, int length) {
			filter.remove(offset, length);
		}

		@Override
		public void insertString(Bypass filter, int offset, String string) {
			var doc = filter.getComponent().getDocumentText(0, offset) + string
					+ filter.getComponent().getDocumentText(offset, filter.getComponent().getLength());
			if (doc.matches(regex))
				filter.insertString(offset, string);
		}

		@Override
		public void replace(Bypass filter, int offset, int length, String string) {
			var doc = filter.getComponent().getDocumentText(0, offset) + string
					+ filter.getComponent().getDocumentText(offset + length, filter.getComponent().getLength());
			if (doc.matches(regex))
				filter.replace(offset, length, string);
		}

	}

}
