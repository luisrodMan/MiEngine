package com.nxtr.spengine.views.inspector.handlers;

import java.lang.annotation.Annotation;
import java.util.Map;

public abstract class HandlerProviderByType implements HandlerProvider {

	protected Class<?>[] types;

	public HandlerProviderByType(Class<?>[] types) {
		this.types = types;
	}

	@Override
	public boolean canHandeType(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
		for (var myType : types) {
			if (myType.isAssignableFrom(type))
				return true;
		}
		return false;
	}

	@Override
	public boolean canHandleFinal() {
		return false;
	}

}
