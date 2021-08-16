package com.wildermods.wilderforge.api.event.bus;

import java.lang.annotation.Annotation;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import com.wildermods.wilderforge.api.event.Event;
import com.wildermods.wilderforge.launch.Main;

@SuppressWarnings("deprecation")
public class EventBus {
	private static final ReferenceQueue<ObjectEventListener<? extends Event>> refQueue = new ReferenceQueue<ObjectEventListener<? extends Event>>();
	private static final HashMap<Class<? extends Event>, Set<IEventListener<? extends Event>>> LISTENERS = new HashMap<Class<? extends Event>, Set<IEventListener<? extends Event>>>();
	
	
	public EventBus() {
		
	}
	
	public final void register(Object o) {
		removeCollectedReferences();
		registerListeners(o);
	}
	
	/**
	 * @param e event
	 * @return true if the event was cancelled, false otherwise
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean fire(Event e) {
		removeCollectedReferences();
		Set<IEventListener<? extends Event>> listeners = LISTENERS.get(e.getClass());
		if(listeners == null) { //nothing is subscribing to the event
			return false;
		}
		for(IEventListener listener: listeners) {
			if(e.isCancelled() && !listener.acceptCancelled()) {
				continue;
			}
			try {
				listener.fire(e);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
				throw new Error(e1);
			}
		}
		return e.isCancelled();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final void registerListeners(Object o) {
		removeCollectedReferences();
		if(o == null) {
			throw new NullPointerException();
		}
		else if(o instanceof Class) {
			for(Method m : Main.getReflectionsHelper().getAllMethodsAnnotatedWith(SubscribeEvent.class)) {
				if(Modifier.isStatic(m.getModifiers())) {
					EventListener listener = new StaticEventListener(m);
					if(!LISTENERS.containsKey(listener.subscribedTo)) {
						LISTENERS.put(listener.subscribedTo, new TreeSet<IEventListener<? extends Event>>());
					}
					Set<IEventListener<? extends Event>> staticListeners = LISTENERS.get(listener.subscribedTo);
					staticListeners.add(listener);
				}
			}
		}
		else {
			for(Method m : Main.getReflectionsHelper().getAllMethodsAnnotatedWith(SubscribeEvent.class)) {
				if(Modifier.isStatic(m.getModifiers())) {
					continue;
				}
				if(Modifier.isAbstract(m.getModifiers())) {
					throw new EventTargetError("Abstract methods cannot subscribe to events. " + m.getClass().getCanonicalName() + "." + m.getName());
				}
				ObjectEventListener listener = new ObjectEventListener(o, m);
				if(!LISTENERS.containsKey(listener.subscribedTo)) {
					LISTENERS.put(listener.subscribedTo, new TreeSet<IEventListener<? extends Event>>());
				}
				Set<IEventListener<? extends Event>> objectListeners = LISTENERS.get(listener.subscribedTo);
				objectListeners.add(listener);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static void removeCollectedReferences() {
		ObjectEventListener reference;
		while((reference = (ObjectEventListener) refQueue.poll()) != null) {
			Class<? extends Event> event = (Class<? extends Event>) reference.method.getParameters()[0].getType();
			if(!LISTENERS.get(event).remove(reference)) {
				throw new AssertionError();
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	static interface IEventListener<T extends Event> extends SubscribeEvent, Comparable<IEventListener> {
		public Method getMethod();
		
		public abstract void fire(T event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
	}
	
	@SuppressWarnings("rawtypes")
	static abstract class EventListener<T extends Event> implements IEventListener<T> {
		
		protected final SubscribeEvent subscriberInfo;
		protected final Method method;
		protected final Class<? extends T> subscribedTo;
		
		@SuppressWarnings("unchecked")
		private EventListener(Method method) throws EventTargetError {
			try {
				this.method = method;
				method.setAccessible(true);
				Parameter[] parameters = method.getParameters();
				if(method.getParameterCount() != 1 || !Event.class.isAssignableFrom(parameters[0].getType())) {
					throw new EventTargetError("@SubscribeEvent can only target methods with exactly one parameter, and that parameter must be a subclass of Event. " + method.getDeclaringClass().getCanonicalName() + "." + method.getName());
				}
				this.subscribedTo = (Class<T>) parameters[0].getType();
				this.subscriberInfo = method.getAnnotation(SubscribeEvent.class);
				if(subscriberInfo == null) {
					throw new EventTargetError(method.getDeclaringClass().getCanonicalName() + "." + method.toString() + " has no @SubscribeEvent annotation");
				}
			}
			catch(EventTargetError e) {
				throw e;
			}
			catch(Throwable t) {
				throw new EventTargetError(t);
			}
		}

		@Override
		public final int compareTo(IEventListener e) {
			return priority() - e.priority();
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return EventListener.class;
		}

		@Override
		public boolean acceptCancelled() {
			return subscriberInfo.acceptCancelled();
		}

		@Override
		public int priority() {
			return subscriberInfo.priority();
		}
		
		public final Method getMethod() {
			return method;
		}
		
		public abstract void fire(T event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
	}
	
	static class StaticEventListener<T extends Event> extends EventListener<T> {
		public StaticEventListener(Method method) throws EventTargetError {
			super(method);
			if(!Modifier.isStatic(method.getModifiers())) {
				throw new AssertionError("StaticEventListener can only make static methods listen! This should not have happened. This is a bug!");
			}
		}

		@Override
		public void fire(Event event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			method.invoke(null, event);
		}

	}
	
	static class ObjectEventListener<T extends Event> extends WeakReference<Object> implements IEventListener<T> {
		protected final SubscribeEvent subscriberInfo;
		protected final Method method;
		protected final Class<? extends T> subscribedTo;
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ObjectEventListener(Object o, Method method) throws EventTargetError  {
			super(o, (ReferenceQueue)refQueue);
			if(o == null) {
				throw new NullPointerException();
			}
			this.method = method;
			method.setAccessible(true);
			Parameter[] parameters = method.getParameters();
			if(method.getParameterCount() != 1 || !Event.class.isAssignableFrom(parameters[0].getType())) {
				throw new EventTargetError("@SubscribeEvent can only target methods with exactly one parameter, and that parameter must be a subclass of Event. " + method.getDeclaringClass().getCanonicalName() + "." + method.getName());
			}
			this.subscribedTo = (Class<T>) parameters[0].getType();
			this.subscriberInfo = method.getAnnotation(SubscribeEvent.class);
			if(subscriberInfo == null) {
				throw new EventTargetError(method.getDeclaringClass().getCanonicalName() + "." + method.toString() + " has no @SubscribeEvent annotation");
			}
		}
		
		public Object getObject() {
			return this.get();
		}

		@Override
		public void fire(Event event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			if(getObject() == null) {
				System.out.println("Event fired on garbage collected object. Hopefully this weakreference will be removed!");
			}
			method.invoke(getObject(), event);
		}

		@Override
		public boolean acceptCancelled() {
			return subscriberInfo.acceptCancelled();
		}

		@Override
		public int priority() {
			return subscriberInfo.priority();
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return ObjectEventListener.class;
		}


		@Override
		@SuppressWarnings("rawtypes")
		public int compareTo(IEventListener e) {
			return priority() - e.priority();
		}

		@Override
		public Method getMethod() {
			return method;
		}
	}
	
}
