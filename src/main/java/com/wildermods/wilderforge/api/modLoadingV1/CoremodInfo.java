package com.wildermods.wilderforge.api.modLoadingV1;

import com.worldwalkergames.legacy.game.mods.ModInfo;
import com.worldwalkergames.legacy.server.context.ServerDataContext;
import com.worldwalkergames.legacy.server.context.ServerDataContext.ModLocation;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.wildermods.wilderforge.api.eventV1.bus.EventPriority;
import com.wildermods.wilderforge.api.eventV1.bus.SubscribeEvent;
import com.wildermods.wilderforge.api.modLoadingV1.event.PostInitializationEvent;
import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.LoadStatus;
import com.wildermods.wilderforge.launch.Main;
import com.wildermods.wilderforge.launch.coremods.Coremod;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.wildermods.wilderforge.launch.coremods.WilderForge;
import com.wildermods.wilderforge.launch.exception.CoremodLinkageError;

public class CoremodInfo extends ModInfo implements com.wildermods.wilderforge.api.modLoadingV1.Coremod {

	public static Files files;
	
	public final Coremod coremod;
	
	@InternalOnly
	public CoremodInfo(Coremod coremod) {
		JsonObject json;
		try {
			json = coremod.getModJson();
		} catch (IOException e) {
			throw new CoremodLinkageError(e);
		}
		this.coremod = coremod;
		super.modId = coremod.value();
		if(Coremods.getStatus(coremod) == LoadStatus.LOADED) {
			modLocation = ServerDataContext.ModLocation.core;
		}
		else {
			modLocation = ServerDataContext.ModLocation.missing;
		}
		modSteamWorkshopId = null;
		alwaysOn = true;
		showInStoryDialog = true;
		showInModConfig = true;
		listInCredits = true;
		JsonElement creditsEle = json.get(CREDITS);
		customCreditLines = creditsEle != null ? WilderForge.gson.fromJson(creditsEle, String[].class) : null;
		JsonElement authorsEle = json.get(AUTHORS);
		String[] authors = WilderForge.gson.fromJson(authorsEle, String[].class);
		if(authors != null && authors.length > 0) {
			author = grammaticallyCorrectAuthorList(authors);
		}
		else {
			author = "?";
		}
		Main.LOGGER.info(Arrays.toString(customCreditLines));
		name = coremod.getName();
		modLocation = ModLocation.core;
	}


	@Override
	public Class<? extends Annotation> annotationType() {
		return com.wildermods.wilderforge.api.modLoadingV1.Coremod.class;
	}


	@Override
	public String value() {
		return modId;
	}
	
	
	public FileHandle getFolder() {
		return getFolder(false);
	}
	
	public FileHandle getFolder(boolean vanilla) {
		return coremod.vanillaFolderOverride().get()[vanilla ? 0 : 1];
	}
	
	@SubscribeEvent(priority = EventPriority.LOWER - 1000)
	public static void onPostInitialization(PostInitializationEvent e) {
		files = Gdx.files;
		for(Coremod coremod : Coremods.getCoremodsByStatus(LoadStatus.LOADED, LoadStatus.NOT_LOADED, LoadStatus.DISCOVERED)) {
			if(files == null) {
				throw new AssertionError();
			}
			coremod.getCoremodInfo().folder = coremod.getCoremodInfo().getFolder(true);
		}
	}
	
	private String grammaticallyCorrectAuthorList(String[] authors) {
		if(authors.length == 1) {
			return authors[0];
		}
		if(authors.length == 2) {
			return authors[0] + " and " + authors[1];
		}
		if(authors.length > 2) {
			StringBuilder ret = new StringBuilder();
			int i = 0;
			for(; i < authors.length - 1; i++) {
				ret.append(authors[i]);
				ret.append(", ");
			}
			ret.append(" and ");
			ret.append(authors.length - 1);
			return ret.toString();
		}
		throw new IllegalArgumentException(Arrays.toString(authors));
	}
	
}
