package com.wildermods.wilderforge.launch.resources;

import java.util.Locale;

import com.wildermods.wilderforge.launch.Coremod;
import com.wildermods.wilderforge.launch.InternalOnly;

@InternalOnly
public interface CoremodCompatibleResourceBundle {

	@InternalOnly
	public void addResources(Coremod coremod, String assetPath, Locale locale);
	
}
