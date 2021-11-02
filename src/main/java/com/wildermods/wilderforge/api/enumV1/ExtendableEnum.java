package com.wildermods.wilderforge.api.enumV1;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.apache.commons.collections4.list.SetUniqueList;
import org.jetbrains.annotations.Nullable;

import com.wildermods.wilderforge.launch.LoadStage;
import com.wildermods.wilderforge.launch.Main;

/**
 * To be implemented with Wildermyth enums that may need to have additional values added for
 * coremodding purposes
 * 
 * Values must be added before Post-Initialization.
 * 
 * If adding values after Post-Initialization, an IllegalStateException should be thrown.
 * @param <T>
 */
public class ExtendableEnum<X extends Enum<X>> implements EnumConstructor<X>{
	private static final HashMap<Class<?>, ExtendableEnum<? extends Enum<?>>> extendableEnums = new HashMap<Class<?>, ExtendableEnum<? extends Enum<?>>>();
	private final Class<X> type;
	private SetUniqueList<EnumValue> values = SetUniqueList.setUniqueList(new ArrayList<EnumValue>());
	
	@SuppressWarnings("unchecked")
	protected ExtendableEnum(Class<X> enumClass) {
		this.type = enumClass;
		if(extendableEnums.containsKey(enumClass)) {
			throw new EnumExtensionError("Multiple ExtendableEnums of type " + enumClass.getSimpleName() + " constructed.");
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <X extends Enum<X>> ExtendableEnum<X> getExtendableEnum(Class<X> enumClass) {
		try {
			ExtendableEnum<X> eEnum = (ExtendableEnum<X>) extendableEnums.get(enumClass);
			if(eEnum == null) {
				eEnum = new ExtendableEnum<X>(enumClass);
				Method valuesMethod = enumClass.getDeclaredMethod("values");
				for(X enumValue : (X[]) valuesMethod.invoke(null)) {
					eEnum.newEnumValue(enumValue);
				}
			}
			return eEnum;
		}
		catch(Throwable t) {
			throw new EnumExtensionError(t);
		}
	}
	
	public final Class<X> getType() {
		return type;
	}
	
	public EnumValue<X> newEnumValue(String name) {
		return newEnumValue(name, new Object[0], new String[0]);
	}
	
	public EnumValue<X> newEnumValue(String name, Object[] parameters, String[] names) {
		Class<?>[] types = new Class<?>[names.length];
		if(parameters.length == names.length) {
			for(int i = 0; i < names.length; i++) {
				if(parameters[i] == null) {
					throw new NullPointerException("Must provide type for null enum parameter: " + names[i]);
				}
				types[i] = parameters[i].getClass();
			}
		}
		return newEnumValue(name, parameters, names, types);
	}
	
	public EnumValue<X> newEnumValue(String name, Object[] parameters, String[] names, Class<?>[] types) {
		return new EnumValue<X>(this, name);
	}
	
	@SuppressWarnings("unchecked")
	public final EnumValue<X>[] values() {
		return values.toArray((EnumValue[])Array.newInstance(EnumValue.class, 0));
	}
	
	private final EnumValue<X> newEnumValue(X enumValue) {
		return new EnumValue<X>(this, enumValue);
	}
	
	public final int hashCode() {
		return type.hashCode();
	}
	
	@SuppressWarnings("rawtypes")
	public boolean equals(Object o) {
		if(o instanceof ExtendableEnum) {
			if(((ExtendableEnum) o).getType().equals(getType())) {
				return true;
			}
		}
		return false;
	}
	
	public static class EnumValue<X extends Enum<X>> {
		
		private final ExtendableEnum<X> extendedEnum;
		private final String name;
		private final X enumValue;
		
		protected EnumValue(ExtendableEnum<X> extendedEnum, X enumValue) {
			this.name = enumValue.name();
			this.enumValue = enumValue;
			this.extendedEnum = extendedEnum;
			if(!extendedEnum.values.add(this)) {
				throw new AssertionError("Multiple enum values with same name?!");
			}
		}
		
		protected EnumValue(ExtendableEnum<X> extendedEnum, String name) {
			this.name = name;
			this.enumValue = null;
			this.extendedEnum = extendedEnum;
			if(!extendedEnum.values.add(this)) {
				Main.LOGGER.warn("Creation of duplicate enum value " + name + " in ExtendableEnum " + extendedEnum.getType().getSimpleName() + " was attempted. Subsuquent value discarded");
			}
		}
		
		@Nullable
		public X getEnumValue() {
			return enumValue;
		}
		
		public String name() {
			return name;
		}
		
		@Override
		public String toString() {
			return name();
		}
		
		public int ordinal() {
			return extendedEnum.values.indexOf(this);
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public boolean equals(Object o) {
			if(o instanceof ExtendableEnum.EnumValue) {
				return name.equals(((ExtendableEnum.EnumValue) o).name());
			}
			else if (extendedEnum.getType().isInstance(o)) {
				return name.equals(((X)o).name());
			}
			return false;
		}
	}
	
	@SuppressWarnings("serial")
	protected final class LockableSetList<E> extends SetUniqueList<E> {
		
		protected LockableSetList(List<E> list, Set<E> set) {
			super(list, set);
		}

		@Override
		public boolean add(E e) {
			checkLock();
			if(contains(e)) {
				return false;
			}
			return super.add(e);
		}
		
		@Override
		public void add(int index, E e) {
			checkLock();
			if(contains(e)) {
				return;
			}
			super.add(index, e);
		}
		
		@Override
		public boolean addAll(Collection<? extends E> c) {
			checkLock();
			boolean success = c.size() > 0;
			for(E element : c) {
				if(!add(element)) {
					success = false;
				}
			}
			return success;
		}
		
		@Override
		public boolean addAll(int index, Collection<? extends E> c) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void clear() {
			checkLock();
			super.clear();
		}
		
		@Override
		public E remove(int index) {
			checkLock();
			return super.remove(index);
		}
		
		@Override
		public boolean remove(Object o) {
			checkLock();
			return super.remove(o);
		}
		
		@Override
		public boolean removeAll(Collection<?> c) {
			checkLock();
			return super.removeAll(c);
		}
		
		@Override
		public boolean removeIf(Predicate<? super E> filter) {
			checkLock();
			return super.removeIf(filter);
		}
		
		@Override
		public void replaceAll(UnaryOperator<E> operator) {
			checkLock();
			super.replaceAll(operator);
		}
		
		@Override
		public boolean retainAll(Collection<?> c) {
			checkLock();
			return super.retainAll(c);
		}
		
		@Override
		public E set(int index, E element) {
			checkLock();
			return super.set(index, element);
		}
		
		@Override
		public void sort(Comparator<? super E> c) {
			throw new UnsupportedOperationException();
		}
		
		private void checkLock() throws IllegalStateException {
			if(LoadStage.getLoadStage().compareTo(LoadStage.PRE_INIT) > 0) {
				throw new IllegalStateException("Enum values cannot be added or removed after postinitialization");
			}
		}
		
	}
}