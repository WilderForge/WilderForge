package com.wildermods.wilderforge.launch.coremods;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.spongepowered.asm.launch.MixinBootstrap;

import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;
import com.wildermods.wilderforge.api.modLoadingV1.Coremod;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wildermods.wilderforge.api.versionV1.Version;
import com.wildermods.wilderforge.launch.HardCodedCoremod;
import com.wildermods.wilderforge.launch.InternalOnly;

@Coremod("mixin")
class Mixin extends HardCodedCoremod {

	Mixin() throws IOException {
		construct("mixin", "Spongepowered Mixin", getMixinVersion());
	}
	
	@Override
	@InternalOnly
	public JsonObject getModJson() throws IOException {
		JsonObject json = new JsonObject();
		json.add(MODID, new JsonPrimitive("mixin"));
		json.add(VERSION, new JsonPrimitive(getVersion().toString()));
		json.add(NAME, new JsonPrimitive("Spongepowered Mixin"));
		JsonArray authors = new JsonArray();
		authors.add("Mumfrey");
		json.add(AUTHORS, authors);
		json.add(DESCRIPTION, new JsonPrimitive("A bytecode weaving framework for Java using ASM"));
		
		JsonArray incompatabilities = new JsonArray();
		
		JsonObject modLauncher = new JsonObject();
		modLauncher.add(MODID, new JsonPrimitive("modlauncher"));
		modLauncher.add(VERSION, new JsonPrimitive("[0.*,8.*)[9.*]")); //Only mod launcher 8 is compatible
		
		incompatabilities.add(modLauncher);
		
		json.add(INCOMPATIBLE, incompatabilities);
		
		JsonArray requires = new JsonArray();
		
		JsonObject asm = new JsonObject();
		asm.add(MODID, new JsonPrimitive("asm"));
		asm.add(VERSION, new JsonPrimitive("[*]"));
		
		requires.add(asm);
		
		json.add(REQUIRES, requires);
		
		JsonArray credits = new JsonArray();
		
		credits.add("[title]Programming[]");
		credits.add("");
		credits.add("Mumfrey");
		
		json.add(CREDITS, credits);
		
		json.add(URL, new JsonPrimitive("https://github.com/SpongePowered/Mixin"));
		
		return json;
	}
	
	private static Version getMixinVersion() {
		return new Version(MixinBootstrap.VERSION);
	}
	
	@Override
	public ResourceBundle getResourceBundle(String assetPath, Locale locale) {
		return null;
	}

}
