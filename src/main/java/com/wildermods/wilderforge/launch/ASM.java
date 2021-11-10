package com.wildermods.wilderforge.launch;

import java.io.IOException;

import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wildermods.wilderforge.api.versionV1.Version;

class ASM extends HardCodedCoremod {

	ASM() throws IOException {
		construct("asm", "ASM", Version.getVersion(getASMVersion()));
	}

	@Override
	public JsonObject getModJson() throws IOException {
		JsonObject json = new JsonObject();
		json.add(MODID, new JsonPrimitive(modid));
		json.add(NAME, new JsonPrimitive(name));
		json.add(VERSION, new JsonPrimitive(version.toString()));
		return json;
	}
	
	private String getASMVersion() {
		String version = org.spongepowered.asm.util.asm.ASM.getVersionString();
		return version.substring(version.indexOf(' ') + 1, version.lastIndexOf(' '));
	}

}
