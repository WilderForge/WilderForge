package com.wildermods.wilderforge.serialization;

import java.io.IOException;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializer;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.modLoadingV1.MissingCoremod;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.worldwalkergames.legacy.game.mods.ModInfo;

public class ModInfoSerializer implements Serializer<ModInfo> {

	@Override
	@SuppressWarnings("rawtypes")
	public ModInfo read(Json json, JsonValue val, Class knownType) {
		if(knownType == null) {
			throw new AssertionError();
		}
		if(knownType == CoremodInfo.class) {
			throw new AssertionError("Coremod info was serialized? Should have been serialized as a regular ModInfo!");
		}
		if(knownType == ModInfo.class) {
			ModInfo info = (ModInfo) WJson.create(ModInfo.class);
			json.readFields(info, val);
			CoremodInfo coremod = Coremods.getCoremod(info.modId);
			if(coremod instanceof MissingCoremod) {
				return info; //Not a coremod, or a missing coremod.
			}
			return coremod;
		}
		else {
			throw new AssertionError("I don't know about mod info type " + knownType + " ?");
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void write(Json json, ModInfo val, Class actualType) {
		if(actualType == null) {
			throw new AssertionError();
		}
		else if(actualType == CoremodInfo.class) {
			actualType = ModInfo.class;
		}
		else if(actualType != ModInfo.class) {
			throw new AssertionError("Don't know how to handle a " + actualType);
		}
		try {
			json.getWriter().object();
			json.writeType(actualType);
			json.writeObjectEnd();
		}
		catch(IOException e) {
			throw new SerializationException(e);
		}
	}

	private static final class WJson extends Json {
		
		public static Object create(Class type) {
			return new WJson().newInstance(type);
		}
		
		public Object newInstance(Class type) {
			return super.newInstance(type);
		}
	}
	
}
