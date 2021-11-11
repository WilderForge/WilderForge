package com.wildermods.wilderforge.launch.coremods;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.wildermods.wilderforge.api.versionV1.Version;
import com.wildermods.wilderforge.launch.InternalOnly;

public class MissingCoremod extends Coremod {

	MissingCoremod(String modid) {
		construct(modid, modid, Version.MISSING);
	}
	
	@Override
	public @InternalOnly JsonObject getModJson() throws IOException {
		return new JsonObject();
	}

}
