package com.nxtr.spengine.views.inspector.handlers;

import com.ngeneration.furthergui.event.ChangeListener;

public abstract class AbstractHandler implements Handler {

	private Object value;

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public void handleFinal(Object object, Object newValue) {

	}

	@Override
	public void setChangeListener(ChangeListener changeListener) {

	}

}
