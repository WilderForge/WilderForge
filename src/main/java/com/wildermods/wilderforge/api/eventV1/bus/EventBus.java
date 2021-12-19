package com.wildermods.wilderforge.api.eventV1.bus;

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

import com.wildermods.wilderforge.api.eventV1.Event;
import com.wildermods.wilderforge.launch.WilderForge;

public class EventBus {
	private static final ReferenceQueue<ObjectEventListener<? extends Event>> refQueue = new ReferenceQueue<ObjectEventListener<? extends Event>>();
	private static final HashMap<Class<? extends Event>, Set<IEventListener<? extends Event>>> LISTENERS = new HashMap<Class<? extends Event>, Set<IEventListener<? extends Event>>>();
	
	
	public EventBus() {
		
	}
	
	/**
	 * Registers a class to receive events.
	 * <p>
	 * All <code>static</code> <code>{@link com.wildermods.wilderforge.api.eventV1.bus.SubscribeEvent @SubscribeEvent}</code> methods become
	 * eligible to receive their events.
	 * <p>
	 * Unlike standard objects, Once registered, classes are always eligible to receive events, and they will not become
	 * eligible to be garbage collected. EventBus will keep a strong reference to the Class instance to prevent garbage collection.
	 * 
	 */
	@SuppressWarnings("rawtypes")
	public final void register(Class c) {
		removeCollectedReferences();
		registerListeners(c);
	}
	
	/**
	 * Registers an object instance to receive events.
	 * <p>
	 * All non-<code>static</code> <code>{@link com.wildermods.wilderforge.api.eventV1.bus.SubscribeEvent @SubscribeEvent}</code> methods become
	 * eligible to receive their events.
	 * <p>
	 * Note that EventBus will only keep {@link java.lang.ref.WeakReference weak references} to your object in order to prevent memory leaks. This means you 
	 * must keep a strong reference to your object until the object no longer needs to receive events, otherwise your object may be garbage collected, and
	 * will not receive events.
	 * <p>For example, suppose <code>Foo</code> instances are eligible to receive <code>BarEvent</code> events:
	 * <pre>
	 * <code>
	 * registerSubscribers() {
	 *     EVENT_BUS.register(new Foo());
	 *     Foo f = new Foo();
	 *     Thread.sleep(1000);
	 *     f = null;
	 *     Thread.sleep(1000);
	 *     EVENT_BUS.fire(new BarEvent());
	 * }
	 * </code>
	 * </pre>
	 * <p>Both the <code>new Foo()</code> and <code>f</code> instances might not receive the event because there is not a strong reference to them when
	 * <code>EVENT_BUS.fire</code> is called.
	 * <p><b>Implementation detail:</b> passing an instance of <code>Class</code>
	 * I.E. Calling 
	 * <p>
	 * <code>register((Object)Example.class);</code>
	 * <p>
	 * will not provide the behavior stated above. This method will
	 * instead behave as if {@link #register(Class)} was called
	 */
	public final void register(Object o) {
		removeCollectedReferences();
		registerListeners(o);
	}
	
	/**
	 * <p>Fires an event to all subscribers that haven't been garbage collected, in {@link conm.wildermods.wilderforge.api.event.bus.SubScribeEvent#priority() priority order}.
	 * 
	 * <p>Note that it is still possible for subscribers which have no strong
	 * references to receive the event.
	 * 
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
			for(Method m : WilderForge.getReflectionsHelper().getAllMethodsAnnotatedWith(SubscribeEvent.class)) {
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
			for(Method m : WilderForge.getReflectionsHelper().getAllMethodsAnnotatedWith(SubscribeEvent.class)) {
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
