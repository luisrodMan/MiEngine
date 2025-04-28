package com.nxtr.spengine.views.inspector;

import com.ngeneration.furthergui.FComponent;
import com.nxtr.spengine.views.inspector.resolver.Content;

public interface InspectorContent extends Content<FComponent> {

	void release();

}
