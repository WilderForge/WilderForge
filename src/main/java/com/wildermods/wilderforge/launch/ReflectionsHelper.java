package com.wildermods.wilderforge.launch;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.reflections8.Reflections;
import org.reflections8.scanners.MethodAnnotationsScanner;
import org.reflections8.scanners.SubTypesScanner;
import org.reflections8.scanners.TypeAnnotationsScanner;
import org.reflections8.util.ClasspathHelper;
import org.reflections8.util.ConfigurationBuilder;

public class ReflectionsHelper {

	private final Reflections reflections;
	
	public ReflectionsHelper(ClassLoader classLoader) {
		
		ConfigurationBuilder builder = new ConfigurationBuilder().addClassLoader(classLoader).addScanners(new TypeAnnotationsScanner(), new MethodAnnotationsScanner(), new SubTypesScanner());

        Set<String> packageNames = new HashSet<>();

        ClasspathHelper.forClassLoader(classLoader).forEach(url -> {
            try {
                Path path = Path.of(url.toURI());

                if (Files.isRegularFile(path) && path.toString().endsWith(".jar")) {
                    try (JarFile jarFile = new JarFile(path.toFile())) {
                        jarFile.stream()
                               .filter(entry -> entry.getName().endsWith(".class") && !entry.getName().startsWith("META-INF/versions"))
                               .map(this::extractPackageFromJarEntry)
                               .forEach(packageNames::add);
                    }
                } else if (Files.isDirectory(path)) {
                    Files.walkFileTree(path, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (file.toString().endsWith(".class")) {
                                String packageName = extractPackageFromDirectory(file, path);
                                packageNames.add(packageName);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            } catch (Exception e) {
                throw new IllegalStateException("Error processing classpath: " + url, e);
            }
        });
		
        
        packageNames.forEach(pkg -> 
        	WilderForge.LOGGER.info("Discovered package: " + pkg, "ReflectionsHelper")
        );
        
        
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
	
    private String extractPackageFromJarEntry(JarEntry entry) {
        String name = entry.getName();
        int lastSlash = name.lastIndexOf('/');
        return lastSlash > 0 ? name.substring(0, lastSlash).replace('/', '.') : "";
    }

    private String extractPackageFromDirectory(Path file, Path root) {
        Path relativePath = root.relativize(file.getParent());
        return relativePath.toString().replace(File.separatorChar, '.');
    }
	
}
