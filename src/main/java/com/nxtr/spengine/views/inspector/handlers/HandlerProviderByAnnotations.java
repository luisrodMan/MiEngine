package com.nxtr.spengine.views.inspector.handlers;

import java.lang.annotation.Annotation;
import java.util.Map;

public abstract class HandlerProviderByAnnotations extends HandlerProviderByType {

	public HandlerProviderByAnnotations(Class<?>[] annotationTypes) {
		super(annotationTypes);
	}

	@Override
	public boolean canHandeType(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
		for (var annotationType : this.types) {
			if (!annotations.containsKey(annotationType))
				return false;
		}
		return true;
	}

}
