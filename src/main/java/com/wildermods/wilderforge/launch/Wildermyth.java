package com.wildermods.wilderforge.launch;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wildermods.wilderforge.api.modLoadingV1.Coremod;
import com.wildermods.wilderforge.api.modLoadingV1.event.PostInitializationEvent;
import com.wildermods.wilderforge.api.versionV1.Version;
import com.wildermods.wilderforge.launch.exception.CoremodFormatError;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;

@Coremod("wildermyth")
public final class Wildermyth extends HardCodedCoremod {
	
	private static LegacyViewDependencies legacyViewDependencies;
	private static final File VERSION_FILE = new File("./version.txt");
	
	Wildermyth() throws IOException {
		construct("wildermyth", getWildermythVersion(new File(".")));
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
	protected JsonObject getModJson() throws IOException {
		JsonObject json = new JsonObject();
		json.add("modid", new JsonPrimitive("wildermyth"));
		json.add("version", new JsonPrimitive(getVersion().toString()));
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

}
