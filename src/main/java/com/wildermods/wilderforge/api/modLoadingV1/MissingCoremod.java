package com.wildermods.wilderforge.api.modLoadingV1;

import java.nio.file.Path;

import net.fabricmc.loader.api.metadata.ModMetadata;

public class MissingCoremod extends CoremodInfo {

	public MissingCoremod() {
		super();
	}
	
	@Override
	public ModMetadata getMetadata() {
		return null;
	}


	@Override
	public Path getRootPath() {
		return null;
	}

}
