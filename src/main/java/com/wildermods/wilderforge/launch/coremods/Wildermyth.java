package com.wildermods.wilderforge.launch.coremods;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import org.apache.commons.io.FileUtils;

import com.badlogic.gdx.files.FileHandle;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wildermods.wilderforge.api.modLoadingV1.Coremod;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.modLoadingV1.event.PostInitializationEvent;
import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;
import com.wildermods.wilderforge.api.versionV1.Version;
import com.wildermods.wilderforge.launch.HardCodedCoremod;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.exception.CoremodFormatError;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;

@Coremod("wildermyth")
public final class Wildermyth extends HardCodedCoremod {
	
	public static Wildermyth INSTANCE;
	
	private static LegacyViewDependencies legacyViewDependencies;
	private static final File VERSION_FILE = new File("./version.txt");
	
	Wildermyth() throws IOException {
		if(INSTANCE == null) {
			INSTANCE = this;
		}
		else {
			throw new IllegalStateException();
		}
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
	public Supplier<FileHandle[]> vanillaFolderOverride() {
		FileHandle[] ret = super.vanillaFolderOverride().get();
		ret[0] = null;
		ret[1] = CoremodInfo.files.internal("");
		return () -> ret;
	}

	@Override
	public JsonObject getModJson() throws IOException {
		JsonObject json = new JsonObject();
		json.add(MODID, new JsonPrimitive("wildermyth"));
		json.add(NAME, new JsonPrimitive("Wildermyth"));
		json.add(VERSION, new JsonPrimitive(getVersion().toString()));
		json.add(IMAGE, new JsonPrimitive("assets/ui/icon/wildermythIcon_256.png"));
		JsonArray authors = new JsonArray();
		authors.add(new JsonPrimitive("Worldwalker Games, LLC"));
		json.add(AUTHORS, authors);
		
		json.add(ISSUES_URL, new JsonPrimitive("https://discord.com/invite/wildermyth"));
		json.add(WEBSITE, new JsonPrimitive("https://wildermyth.com/"));
		json.add(LICENSE_URL, new JsonPrimitive("https://wildermyth.com/terms.php"));
		json.add(DESCRIPTION, new JsonPrimitive("A procedural storytelling RPG where tactical combat and story decisions will alter your world and reshape your cast of characters."));
		
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
