/*
 * Copyright 2008 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.sf.jpasecurity.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.security.DeclareRoles;

/**
 * This parser parses a specified <tt>Set</tt> of classes for the {@link DeclareRoles} annotation.
 * @author Arne Limburg
 */
public class AnnotationParser<V> {

	private Class<? extends Annotation> annotationClass;
	
	public AnnotationParser(Class<? extends Annotation> annotationClass) {
		this.annotationClass = annotationClass;
	}
	
	/**
	 * Parses the specified classes for the annotation
	 * and returns a collection of found values.
	 * @param classes
	 * @return values
	 */
	public Set<V> parse(Collection<Class<?>> classes) {
		Set<V> values = null;
		for (Class<?> annotatedClass: classes) {
			if (values == null) {
				values = parse(annotatedClass);
			} else {
				values.addAll(parse(annotatedClass));
			}
		}
		return values;
	}  
	
	/**
	 * Parses the specified class for the annotation
	 * and returns a collection of found values.
	 * @param classes
	 * @return values
	 */
	public Set<V> parse(Class<?> annotatedClass) {
		if (annotatedClass == null) {
			return new HashSet<V>();
		}
		Set<V> values = parse(annotatedClass.getSuperclass());
		Annotation annotation = annotatedClass.getAnnotation(annotationClass);
		if (annotation != null) {
			for (Object value: getValues(annotation)) {
				values.add((V)value);
			}
		}
		for (Class<?> persistentInterface: annotatedClass.getInterfaces()) {
			values.addAll(parse(persistentInterface));
		}
		return values;
	}
	
	private Object[] getValues(Annotation annotation) {
		try {
			Object value = annotation.getClass().getMethod("value").invoke(annotation);
			if (value.getClass().isArray()) {
				return (Object[])value;
			} else {
				return new Object[] {value};
			}
		} catch (IllegalAccessException e) {
			return new Object[0];
		} catch (InvocationTargetException e) {
			return new Object[0];
		} catch (NoSuchMethodException e) {
			return new Object[0];
		}
	}
}
