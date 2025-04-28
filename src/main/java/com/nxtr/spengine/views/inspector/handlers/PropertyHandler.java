package com.nxtr.spengine.views.inspector.handlers;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.event.ChangeListener;

public interface PropertyHandler {

	Object getValue();

	void setValue(Object value);

	FComponent getComponent();

	void handleFinal(Object object, Object newValue);

	void setChangeListener(ChangeListener changeListener);

}
