package com.wildermods.wilderforge.launch;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.google.gson.JsonObject;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.versionV1.Version;
import com.wildermods.wilderforge.api.versionV1.Versioned;
import com.worldwalkergames.legacy.game.campaign.model.GameSettings.ModEntry;
import com.worldwalkergames.legacy.game.mods.IModAware;

@InternalOnly
@SuppressWarnings("intfAnnotation")
public abstract class Coremod implements com.wildermods.wilderforge.api.modLoadingV1.Coremod, IModAware, Versioned{

	protected String modid;
	protected Version version;
	protected CoremodInfo coremodInfo;
	
	protected void construct(String modid, Version version) {
		this.modid = modid;
		this.version = version;
		this.coremodInfo = new CoremodInfo(this);
	}
	
	@Override
	public final String value() {
		return modid;
	}
	
	@Override
	public final String getModId() {
		return value();
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
	public final int compareTo(Versioned o) {
		if(o instanceof com.wildermods.wilderforge.api.modLoadingV1.Coremod) {
			if(value().equals(((com.wildermods.wilderforge.api.modLoadingV1.Coremod) o).value())) {
				return getVersion().compareTo(o);
			}
			throw new UnsupportedOperationException("Cannot compare versions of two diffrent coremods: " + this + " and " + o);
		}
		return Versioned.super.compareTo(o);
	}
	
	protected abstract JsonObject getModJson() throws IOException;
	
	@Override
	public final Class<? extends Annotation> annotationType() {
		return com.wildermods.wilderforge.api.modLoadingV1.Coremod.class;
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
	
	@Deprecated
	public ModEntry getModEntry() {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public CoremodInfo getCoremodInfo() {
		return coremodInfo;
	}
	
	public ResourceBundle getResourceBundle(String path, Locale locale) {
		try {
			return ResourceBundle.getBundle(value() + "/" + path.replace(".properties", ""), locale);
		}
		catch (MissingResourceException e) {
			return null;
		}
	}
}
