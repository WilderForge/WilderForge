package com.wildermods.wilderforge.launch;

import java.io.IOException;
import java.io.InputStreamReader;

import java.net.JarURLConnection;

import java.util.zip.ZipEntry;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;
import com.wildermods.wilderforge.api.versionV1.Version;
import com.wildermods.wilderforge.launch.exception.CoremodFormatError;

@SuppressWarnings("deprecation")
class JarCoremod extends Coremod {
	
	private final JarURLConnection jar;
	private final JsonObject modJson;
	
	JarCoremod(JarURLConnection jar) throws IOException {
		this.jar = jar;
		this.modJson = getModJson();
		construct(modJson.get(MODID).getAsString(), modJson.get(NAME).getAsString(), Version.getVersion(modJson.get(VERSION).getAsString()));
		JsonObject root = modJson;
		JsonElement modidElement = root.get(MODID);
		if(modidElement != null) {
			modid = modidElement.getAsString();
		}
		else {
			throw new CoremodFormatError("No modid in " + jar.getJarFileURL() + " mod.json ");
		}
	}

	@Override
	public JsonObject getModJson() throws IOException {
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
