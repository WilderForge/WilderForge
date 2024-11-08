package com.wildermods.wilderforge.api.eventV1.bus;

import java.lang.annotation.Annotation;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.wildermods.wilderforge.launch.ReflectionsHelper;
import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.launch.exception.EventTargetError;
import com.wildermods.wilderforge.launch.logging.LogLevel;
import com.wildermods.wilderforge.launch.logging.Logger;

public class EventBus {
	
	private static final HashSet<EventBus> busses = new HashSet<>();
	private static final Thread BUS_REFERENCE_CLEANER;
	static {
		final String threadName = "EventBus Cleaner";
		BUS_REFERENCE_CLEANER = new Thread(() -> {
			final Logger logger = new Logger(threadName);
			while(true) {
				try {
					Thread.sleep(10000);
					for(EventBus bus : busses) {
						int removed = bus.removeCollectedReferences();
						if(removed > 0) {
							logger.info("Removed " + removed + " phantom references from the " + bus + " event bus.");
						}
					}
				} catch (Throwable t) {
					logger.catching(LogLevel.FATAL, t);
					break;
				}
			}
			logger.warn("Cleaner thread stopping!");
		}); 
		BUS_REFERENCE_CLEANER.setDaemon(true);
		BUS_REFERENCE_CLEANER.setName(threadName);
	}
	
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final ReferenceQueue<ObjectEventListener<? extends Event>> refQueue = new ReferenceQueue<ObjectEventListener<? extends Event>>();
	private final HashMap<Class<? extends Event>, Set<IEventListener<? extends Event>>> listeners = new HashMap<Class<? extends Event>, Set<IEventListener<? extends Event>>>();
	private final String name;
	private final Logger logger;
	
	public EventBus(String name) {
		this.name = name;
		this.logger = new Logger(EventBus.class + "/" + name);
		busses.add(this);
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
		lock.writeLock().lock();
		try {
			registerListeners(c);
		}
		finally {
			lock.writeLock().unlock();
		}
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
		lock.writeLock().lock();
		try {
			registerListeners(o);
		}
		finally {
			lock.writeLock().unlock();
		}
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
		lock.readLock().lock();
		try {
			Set<IEventListener<? extends Event>> foundListeners = listeners.get(e.getClass());
			if(foundListeners == null) { //nothing is subscribing to the event
				return false;
			}
			for(IEventListener listener: foundListeners) {
				if(e.isCancelled() && !listener.acceptCancelled()) {
					continue;
				}
				try {
					listener.fire(e);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
					throw new Error(e1);
				}
			}
		}
		finally {
			lock.readLock().unlock();
		}
		return e.isCancelled();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private final void registerListeners(Object o) {
		if(o == null) {
			throw new NullPointerException();
		}
		ReflectionsHelper reflectionHelper = WilderForge.getReflectionsHelper();

		if(o instanceof Class) {
			Set<Class<?>> types = reflectionHelper.getTypeAndSubTypesOf((Class)o);
			for(Class c : types) {
				for(Method m : reflectionHelper.getAllMethodsInAnnotatedWith(c, SubscribeEvent.class)) {
					if(Modifier.isStatic(m.getModifiers())) {
						EventListener listener = new StaticEventListener(m);
						Set<Class> subclasses = WilderForge.getReflectionsHelper().getTypeAndSubTypesOf(listener.subscribedTo);
						for(Class eventType : subclasses) {
							if(!listeners.containsKey(eventType)) {
								listeners.put(eventType, new TreeSet<IEventListener<? extends Event>>());
							}
							Set<IEventListener<? extends Event>> staticListeners = listeners.get(eventType);
							staticListeners.add(listener);
						}
					}
				}
			}
		}
		else {
			Set<Class<?>> types = reflectionHelper.getTypeAndSubTypesOf(o);
			for(Class c : types) {
				for(Method m : reflectionHelper.getAllMethodsInAnnotatedWith(c, SubscribeEvent.class)) {
					if(Modifier.isStatic(m.getModifiers())) {
						continue;
					}
					if(Modifier.isAbstract(m.getModifiers())) {
						throw new EventTargetError("Abstract methods cannot subscribe to events. " + m.getClass().getCanonicalName() + "." + m.getName());
					}
					ObjectEventListener listener = new ObjectEventListener(this, o, m);
					Set<Class> subclasses = WilderForge.getReflectionsHelper().getTypeAndSubTypesOf(listener.subscribedTo);
					for(Class eventType : subclasses) {
						if(!listeners.containsKey(eventType)) {
							listeners.put(eventType, new TreeSet<IEventListener<? extends Event>>());
						}
						Set<IEventListener<? extends Event>> objectListeners = listeners.get(eventType);
						objectListeners.add(listener);
					}
				}
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	int removeCollectedReferences() {
		int collected = 0;
		ObjectEventListener reference;
		while((reference = (ObjectEventListener) refQueue.poll()) != null) {
			Class<? extends Event> event = (Class<? extends Event>) reference.method.getParameters()[0].getType();
			if(!listeners.get(event).remove(reference)) {
				throw new AssertionError();
			}
			collected++;
		}
		return collected;
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
		protected final EventBus bus;
		protected final SubscribeEvent subscriberInfo;
		protected final Method method;
		protected final Class<? extends T> subscribedTo;
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ObjectEventListener(EventBus bus, Object o, Method method) throws EventTargetError  {
			super(o, (ReferenceQueue)bus.refQueue);
			this.bus = bus;
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
				bus.logger.warn(event.getClass().getSimpleName() + " fired on garbage collected object. Hopefully this weakreference will be removed!");
				return;
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
