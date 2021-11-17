package com.wildermods.wilderforge.launch.coremods;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wildermods.wilderforge.api.modLoadingV1.Coremod;
import com.wildermods.wilderforge.api.modLoadingV1.event.PostInitializationEvent;
import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;
import com.wildermods.wilderforge.api.versionV1.Version;
import com.wildermods.wilderforge.launch.HardCodedCoremod;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.exception.CoremodFormatError;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;

@Coremod("wildermyth")
public final class Wildermyth extends HardCodedCoremod {
	
	private static LegacyViewDependencies legacyViewDependencies;
	private static final File VERSION_FILE = new File("./version.txt");
	
	Wildermyth() throws IOException {
		construct("wildermyth", "Wildermyth", getWildermythVersion(new File(".")));
	}
	
	@Override
	protected void construct(String modid, String name, Version version) {
		super.construct(modid, name, version);
		this.coremodInfo.listInCredits = false;
	}
	
	@InternalOnly
	public static void init(PostInitializationEvent e, LegacyViewDependencies legacyViewDependencies) {
		if(Wildermyth.legacyViewDependencies == null) {
			Wildermyth.legacyViewDependencies = legacyViewDependencies;
			WilderForge.EVENT_BUS.fire(e);
		}
		else {
			throw new IllegalStateException("Game already initialized!");
		}
	}

	@Override
	public JsonObject getModJson() throws IOException {
		JsonObject json = new JsonObject();
		json.add(MODID, new JsonPrimitive("wildermyth"));
		json.add(NAME, new JsonPrimitive("Wildermyth"));
		json.add(VERSION, new JsonPrimitive(getVersion().toString()));
		JsonArray authors = new JsonArray();
		authors.add(new JsonPrimitive("Worldwalker Games, LLC"));
		json.add(AUTHORS, authors);
		return json;
	}
	
	private static Version getWildermythVersion(File gameDir) throws IOException {
		File versionFile = new File(gameDir.getAbsolutePath() + "/version.txt");
		if(versionFile.exists()) {
			return Version.getVersion(FileUtils.readFileToString(versionFile).split(" ")[0]);
		}
		else {
			throw new CoremodFormatError("No version.txt detected for wildermyth!");
		}
	}
	
	public static LegacyViewDependencies getViewDependencies() {
		return legacyViewDependencies;
	}
	
	@Override
	public ResourceBundle getResourceBundle(String assetPath, Locale locale) {
		return null;
	}

}
