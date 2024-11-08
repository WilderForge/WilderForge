package com.wildermods.wilderforge.launch;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.reflections8.Reflections;
import org.reflections8.scanners.MethodAnnotationsScanner;
import org.reflections8.scanners.SubTypesScanner;
import org.reflections8.scanners.TypeAnnotationsScanner;
import org.reflections8.util.ConfigurationBuilder;

public class ReflectionsHelper {

	private final Reflections reflections;
	
	public ReflectionsHelper(ClassLoader classLoader) {
		ConfigurationBuilder builder = new ConfigurationBuilder().addClassLoader(classLoader).addScanners(new TypeAnnotationsScanner(), new MethodAnnotationsScanner(), new SubTypesScanner());

		Set<Package> packages = Set.of(classLoader.getDefinedPackages());
		Set<String> packageNames = new HashSet<String>();
		for(Package packag : packages) {
			String name = packag.getName();
			if(name.startsWith("net")) {
				continue;
			}
			packageNames.add(packag.getName());
		}
		
		builder.forPackages(packageNames.toArray(new String[] {}));
		
		reflections = new Reflections(builder);
	}
	
	public Set<Class<?>> getAllClassesAnnotatedWith(Annotation annotation) {
		return reflections.getTypesAnnotatedWith(annotation);
	}
	
	public Set<Class<?>> getAllClassesAnnotatedWith(Class<? extends Annotation> annotation) {
		return reflections.getTypesAnnotatedWith(annotation);
	}
	
	public Set<Method> getAllMethodsAnnotatedWith(Annotation annotation) {
		return reflections.getMethodsAnnotatedWith(annotation);
	}
	
	public Set<Method> getAllMethodsAnnotatedWith(Class<? extends Annotation> annotation) {
		return reflections.getMethodsAnnotatedWith(annotation);
	}
	
	@SuppressWarnings("rawtypes")
	public Set<Method> getAllMethodsInAnnotatedWith(Class clazz, Class<? extends Annotation> annotation) {
		HashSet<Method> methods = new HashSet<Method>();
		for(Method m : clazz.getDeclaredMethods()) {
			if(m.isAnnotationPresent(annotation)) {
				m.setAccessible(true);
				methods.add(m);
			}
		}
		return methods;
	}
	
	@SuppressWarnings("rawtypes")
	public Set<Method> getAllMethodsInAnnotatedWith(Class clazz, Annotation annotation) {
		return getAllMethodsInAnnotatedWith(clazz, annotation.annotationType());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Set<Method> getAllMethodsInAnnotatedWithParams(Class clazz, Class<? extends Annotation> annotation, boolean strict, Class... params) {
		HashSet<Method> methods = new HashSet<Method>();
		methodloop:
		for(Method m : clazz.getDeclaredMethods()) {
			if(m.isAnnotationPresent(annotation)) {
				Class[] methodParams = m.getParameterTypes();
				if(methodParams.length == params.length) {
					for(int i = 0; i < methodParams.length; i++) {
						if (!methodParams[i].equals(params[i]) || (!strict && !methodParams[i].isAssignableFrom(params[i]))) {
							continue methodloop;
						}
					}
					methods.add(m);
				}
			}
		}
		return methods;
	}
	
	@SuppressWarnings("rawtypes")
	public Set<Method> getAllMethodsInAnnotatedWithParams(Class clazz, Annotation annotation, boolean strict, Class... params) {
		return getAllMethodsInAnnotatedWithParams(clazz, annotation.annotationType(), strict, params);
	}
	
	public Set<Field> getAllFieldsInAnnotatedWith(Class clazz, Annotation annotation) {
		return getAllFieldsInAnnotatedWith(clazz, annotation.annotationType());
	}
	
	public Set<Field> getAllFieldsInAnnotatedWith(Class clazz, Class<? extends Annotation> annotation) {
		HashSet<Field> fields = new HashSet<Field>();
		for(Field f : clazz.getDeclaredFields()) {
			if(f.isAnnotationPresent(annotation)) {
				fields.add(f);
				f.setAccessible(true);
			}
		}
		return fields;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Set<Class<?>> getTypeAndSubTypesOf(Object o) {
		return (Set<Class<?>>)(Object)getTypeAndSubTypesOf(o.getClass());
	}
	
	public <A extends Annotation> A getAnnotation(Class<A> annotation, Class<?> from) {
		return from.getAnnotation(annotation);
	}
	
	public <T> Set<Class<? extends T>> getTypeAndSubTypesOf(Class<T> clazz) {
		Set<Class<? extends T>> classes = reflections.getSubTypesOf(clazz);
		classes.add(clazz);
		return classes;
	}
	
}
