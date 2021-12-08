package com.wildermods.wilderforge.launch.coremods;

import java.io.IOException;

import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;
import com.wildermods.wilderforge.api.modLoadingV1.Coremod;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wildermods.wilderforge.api.versionV1.Version;
import com.wildermods.wilderforge.launch.HardCodedCoremod;

@Coremod("asm")
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
		JsonArray authors = new JsonArray();
		authors.add(new JsonPrimitive("INRIA, France Telecom"));
		json.add(AUTHORS, authors);
		json.add(WEBSITE, new JsonPrimitive("https://asm.ow2.io/"));
		json.add(SOURCE_URL, new JsonPrimitive("https://gitlab.ow2.org/asm/asm"));
		json.add(ISSUES_URL, new JsonPrimitive("https://gitlab.ow2.org/asm/asm/-/issues"));
		json.add(LICENSE_URL, new JsonPrimitive("https://gitlab.ow2.org/asm/asm/-/blob/master/LICENSE.txt"));
		
		json.add(DESCRIPTION, new JsonPrimitive("ASM is an all purpose Java bytecode manipulation and analysis framework. It can be used to modify existing classes or to dynamically generate classes, directly in binary form.\n\nASM provides some common bytecode transformations and analysis algorithms from which custom complex transformations and code analysis tools can be built.\n\nASM offers similar functionality as other Java bytecode frameworks, but is focused on performance."));
		
		return json;
	}
	
	private String getASMVersion() {
		String version = org.spongepowered.asm.util.asm.ASM.getVersionString();
		return version.substring(version.indexOf(' ') + 1, version.lastIndexOf(' '));
	}

}
