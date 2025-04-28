package com.nxtr.spengine.views.inspector.handlers;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface PropertyHandlerProvider {

	boolean canHandeType(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations);

	boolean canHandleFinal();

	PropertyHandler getHandler(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations,
			Object initialValue);

}
