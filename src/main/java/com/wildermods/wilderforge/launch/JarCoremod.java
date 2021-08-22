package com.wildermods.wilderforge.launch;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.util.HashSet;
import java.util.zip.ZipEntry;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.wildermods.wilderforge.api.versionV1.Version;
import com.wildermods.wilderforge.launch.exception.CoremodFormatError;

class JarCoremod extends LoadableCoremod {
	
	private static final Gson gson = new Gson();
	
	private final JarURLConnection jar;
	private final JsonObject modJson;
	private final String modid;
	private final HashSet<com.wildermods.wilderforge.api.Coremod> dependencies = new HashSet<>();
	
	public JarCoremod(JarURLConnection jar) throws IOException {
		this.jar = jar;
		this.modJson = getModJson();
		construct(modJson.get("modid").getAsString(), Version.getVersion(modJson.get("version").getAsString()));
		JsonObject root = modJson;
		JsonElement modidElement = root.get("modid");
		if(modidElement != null) {
			modid = modidElement.getAsString();
		}
		else {
			throw new CoremodFormatError("No modid in " + jar.getJarFileURL() + " mod.json ");
		}
	}

	@Override
	protected JsonObject getModJson() throws IOException {
		if(jar == null) {
			throw new Error();
		}
		ZipEntry modJsonEntry = jar.getJarFile().getEntry("mod.json");
		if(modJsonEntry != null) {
			try {
				JsonParser.parseReader(new InputStreamReader(jar.getJarFile().getInputStream(modJsonEntry)));
			}
			catch(JsonParseException e) {
				if(e instanceof JsonIOException) {
					throw new IOException("Could not read mod.json file of " + jar.getJarFileURL() + " due to an exception", e);
				}
				throw new CoremodFormatError(jar.getJarFileURL() + " has a mod.json which is does not contain valid json ", e);
			}
		}
		throw new CoremodFormatError("No mod.json in " + jar.getJarFileURL());
	}
	
}
