package com.wildermods.wilderforge.api.modLoadingV1;

import com.worldwalkergames.legacy.game.mods.ModInfo;
import com.worldwalkergames.legacy.server.context.ServerDataContext;
import com.worldwalkergames.legacy.server.context.ServerDataContext.ModLocation;

import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.CustomValue.CvArray;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.WilderForge;

public class CoremodInfo extends ModInfo implements ModContainer {

	public static Files files = Gdx.files;
	
	public final ModContainer coremod;
	
	@InternalOnly
	public CoremodInfo(ModContainer coremod) {

		ModMetadata metadata = coremod.getMetadata();
		
		modId = metadata.getId();
		modLocation = ServerDataContext.ModLocation.core;
		modSteamWorkshopId = null;
		alwaysOn = true;
		showInStoryDialog = true;
		showInModConfig = true;
		listInCredits = true;
		
		this.folder = getFolder();
		
		CustomValue HOMEPAGEV = metadata.getCustomValue(HOMEPAGE);
		
		if(modId.equals("java")) {
			author = System.getProperty("java.vendor");
			if(author == null) {
				author = "Unknown vendor";
			}
			url = "java.vendor.url";
		}
		else {
			author = grammaticallyCorrectAuthorList((Person[]) metadata.getAuthors().toArray(new Person[]{}));
			if(HOMEPAGEV != null) {
				url = metadata.getCustomValue(HOMEPAGE).getAsString();
			}
		}
		
		CustomValue creditArrayV = metadata.getCustomValue(CREDITS);
		CvArray creditArray = null;
		if(creditArrayV != null) {
			creditArray = creditArrayV.getAsArray();
		}
		customCreditLines = null;
		if(creditArray != null) {
			customCreditLines = new String[creditArray.size()];
			for(int i = 0; i < creditArray.size(); i++) {
				customCreditLines[i] = creditArray.get(i).getAsString();
			}
		}
		
		WilderForge.LOGGER.info(Arrays.toString(customCreditLines));
		name = metadata.getName();
		modLocation = ModLocation.core;
		this.coremod = coremod;
	}
	
	protected CoremodInfo() {this.coremod = null;};
	
	public FileHandle getFolder() {
		return files.classpath("");
	}
	
	private String grammaticallyCorrectAuthorList(Person[] authors) {
		if(authors.length == 0) {
			return "?";
		}
		if(authors.length == 1) {
			return authors[0].getName();
		}
		if(authors.length == 2) {
			return authors[0].getName() + " and " + authors[1].getName();
		}
		if(authors.length > 2) {
			StringBuilder ret = new StringBuilder();
			int i = 0;
			for(; i < authors.length - 1; i++) {
				ret.append(authors[i].getName());
				ret.append(", ");
			}
			ret.append(" and ");
			ret.append(authors.length - 1);
			return ret.toString();
		}
		throw new IllegalArgumentException(Arrays.toString(authors));
	}


	@Override
	public ModMetadata getMetadata() {
		return coremod.getMetadata();
	}


	@Override
	public Path getRootPath() {
		return coremod.getRootPath();
	}
	
	@Override
	public String toString() {
		return modId;
	}
	
	public ResourceBundle getResourceBundle(String path, Locale locale) {
		try {
			return ResourceBundle.getBundle(path.replace(".properties", ""), locale);
		}
		catch (MissingResourceException e) {
			return null;
		}
	}
	
}
