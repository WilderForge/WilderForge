package com.wildermods.wilderforge.launch;

import java.io.IOException;
import java.lang.annotation.Annotation;

import com.google.gson.JsonObject;
import com.wildermods.wilderforge.api.versionV1.Version;

@SuppressWarnings("intfAnnotation")
public abstract class Coremod implements Comparable<Coremod>, com.wildermods.wilderforge.api.Coremod{

	protected String modid;
	protected Version version;
	
	protected void construct(String modid, Version version) {
		this.modid = modid;
		this.version = version;
	}
	
	@Override
	public final String value() {
		return modid;
	}
	
	@Override
	public final String toString() {
		return value();
	}
	
	public final String getVersionString() {
		return modid + " " + version.toString();
	}
	
	public final Version getVersion() {
		return version;
	}
	
	@Override
	public final int compareTo(Coremod o) {
		if(value().equals(o.value())) {
			return getVersion().compareTo(o);
		}
		throw new UnsupportedOperationException("Cannot compare versions of two diffrent coremods: " + this + " and " + o);
	}
	
	protected abstract JsonObject getModJson() throws IOException;
	
	@Override
	public final Class<? extends Annotation> annotationType() {
		return com.wildermods.wilderforge.api.Coremod.class;
	}
	
	@Override 
	public final int hashCode() {
		return value().hashCode();
	}
	
	public final boolean equals(Object o) {
		if(o instanceof Coremod) {
			return value().equals(((Coremod) o).value());
		}
		else if (o.toString().equals(value())) {
			return (value().equals(o.toString()));
		}
		return false;
	}
}
