package com.wildermods.wilderforge.launch;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wildermods.wilderforge.api.versionV1.MultiVersionRange;
import com.wildermods.wilderforge.launch.exception.CoremodFormatError;

public class Dependency extends Coremod {

	private final JsonObject modJson;
	private final boolean required;
	private final MultiVersionRange versionRange;
	
	public Dependency(boolean required, JsonObject json) {
		this.required = required;
		this.modJson = json;
		JsonElement modid = json.get("modid");
		JsonElement versionRange = json.get("version");
		if(modid != null) {
			this.modid = modid.getAsString();
		}
		else {
			throw new CoremodFormatError("No modid detected for a dependency");
		}
		if(versionRange != null) {
			this.versionRange = new MultiVersionRange(versionRange.getAsString());
		}
		else {
			throw new CoremodFormatError("No version detected for dependency" + this.modid);
		}
	}
	
	public boolean required() {
		return required;
	}

	@Override
	protected JsonObject getModJson() throws IOException {
		return modJson;
	}
	
}
