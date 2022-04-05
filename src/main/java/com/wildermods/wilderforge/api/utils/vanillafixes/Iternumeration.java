package com.wildermods.wilderforge.api.utils.vanillafixes;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

public class Iternumeration<T> implements Iterator<T>, Enumeration<T> {

	private final Iterator<T> parent;
	
	private Iternumeration(Iterator<T> iterator) {
		this.parent = iterator;
	}
	
	@Override
	public boolean hasMoreElements() {
		return hasNext();
	}

	@Override
	public T nextElement() {
		return next();
	}

	@Override
	public boolean hasNext() {
		return parent.hasNext();
	}

	@Override
	public T next() {
		return parent.next();
	}
	
	public static <T> Iternumeration<T> get(Collection<T> collection) {
		return new Iternumeration<T>(collection.iterator());
	}
	
	public static <T> Iternumeration<T>get(Iterator<T> iterator) {
		return new Iternumeration<T>(iterator);
	}

}
