package com.wildermods.wilderforge.launch.coremods;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.wildermods.wilderforge.api.Version;
import com.wildermods.wilderforge.launch.HardCodedCoremod;
import com.wildermods.wilderforge.launch.Main;
import com.wildermods.wilderforge.launch.exception.CoremodFormatError;

public class WilderForge extends HardCodedCoremod {
	
	private static final URL versionURL;
	static {
		try {
			versionURL = new URL("https://wildermods.com/WilderForge/master/src/main/resources/versions.json");
		} catch (MalformedURLException e) {
			throw new AssertionError(e);
		}
	}
	
	public WilderForge() throws IOException {
		construct(getModJson().get("modid").getAsString(), new Version(getModJson().get("version").getAsString()));
		parseDependencies();
	}
	
	@Override
	public void parseDependencies() throws IOException {
		modid = getModJson().get("modid").getAsString();
		super.parseDependencies();
	}

	@Override
	protected JsonObject getModJson() throws IOException {
		try {
			JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(Main.class.getResourceAsStream("/mod.json")));
			return JsonParser.parseReader(new InputStreamReader(Main.class.getResourceAsStream("/mod.json"))).getAsJsonObject();
		}
		catch(Throwable t) {
			throw new AssertionError(new CoremodFormatError(t));
		}
	}

}
