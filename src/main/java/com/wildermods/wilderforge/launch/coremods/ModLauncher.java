package com.wildermods.wilderforge.launch.coremods;

import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wildermods.wilderforge.api.modLoadingV1.Coremod;
import com.wildermods.wilderforge.api.versionV1.Version;
import com.wildermods.wilderforge.launch.HardCodedCoremod;
import com.wildermods.wilderforge.launch.InternalOnly;

import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;

@Coremod("modlauncher")
@InternalOnly
public class ModLauncher extends HardCodedCoremod {

	public static String VERSION;
	
	ModLauncher() throws IOException {
		construct("modlauncher", "ModLauncher", new Version(VERSION));
	}

	@Override
	public JsonObject getModJson() throws IOException {
		JsonObject json = new JsonObject();
		
		json.add(MODID, new JsonPrimitive(modid));
		json.add(NAME, new JsonPrimitive(name));
		json.add(VERSION, new JsonPrimitive(version.toString()));
		
		JsonArray authors = new JsonArray();
		authors.add(new JsonPrimitive("cpw"));
		
		json.add(AUTHORS, authors);
		
		json.add(DESCRIPTION, new JsonPrimitive("Provides a java service which allows for class transformations at classload time."));
		
		json.add(SOURCE_URL, new JsonPrimitive("https://github.com/cpw/modlauncher"));
		json.add(ISSUES_URL, new JsonPrimitive("https://github.com/cpw/modlauncher/issues"));
		json.add(LICENSE_URL, new JsonPrimitive("https://github.com/cpw/modlauncher/blob/master/COPYING.LGPL"));
		
		json.add(LICENSE, new JsonPrimitive("LGPL v3"));
		
		return json;
		
	}

}
