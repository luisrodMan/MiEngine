package com.nxtr.spengine.views.inspector;

import java.lang.annotation.Annotation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldDescriptor {

	private String name;
	private Class<?> type;
	private Object value;
	private Annotation[] annotations;

}
