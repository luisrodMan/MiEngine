package com.nxtr.spengine.views.scene;

import com.ngeneration.furthergui.FComponent;

public interface DropHandler<P, R> {

	FComponent getDragComponent();

	R onAcepted(P resource);

}