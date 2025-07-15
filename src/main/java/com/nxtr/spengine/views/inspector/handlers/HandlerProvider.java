package com.nxtr.spengine.views.inspector.handlers;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface HandlerProvider {

	boolean canHandeType(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations);

	boolean canHandleFinal();

	Handler getHandler(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations,
			Object initialValue);

}
