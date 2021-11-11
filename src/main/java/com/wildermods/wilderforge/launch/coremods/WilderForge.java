package com.wildermods.wilderforge.launch.coremods;

import java.io.IOException;
import java.io.InputStreamReader;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.wildermods.wilderforge.api.eventV1.bus.EventBus;
import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;
import com.wildermods.wilderforge.api.modLoadingV1.Coremod;
import com.wildermods.wilderforge.api.versionV1.Version;
import com.wildermods.wilderforge.launch.HardCodedCoremod;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.Main;
import com.wildermods.wilderforge.launch.Properties;
import com.wildermods.wilderforge.launch.exception.CoremodFormatError;

@Coremod("wilderforge")
@SuppressWarnings("deprecation")
public final class WilderForge extends HardCodedCoremod {
	
	@InternalOnly
	public static Gson gson = new Gson();
	
	public static final EventBus EVENT_BUS = new EventBus();
	
	private static final URL versionURL;
	static {
		try {
			versionURL = new URL("https://wildermods.com/WilderForge/master/src/main/resources/versions.json");
		} catch (MalformedURLException e) {
			throw new AssertionError(e);
		}
	}
	
	WilderForge() throws IOException {
		Version version = Version.getVersion(getModJson().get(VERSION).getAsString());
		if(version.toString().startsWith("$")) {
			version = new Version(Properties.getProperty("wilderForgeVersion"));
		}
		construct(getModJson().get(MODID).getAsString(), getModJson().get(NAME).getAsString(), version);
	}

	@Override
	public JsonObject getModJson() throws IOException {
		try {
			return JsonParser.parseReader(new InputStreamReader(Main.class.getResourceAsStream("/mod.json"))).getAsJsonObject();
		}
		catch(Throwable t) {
			throw new CoremodFormatError(t);
		}
	}

}
