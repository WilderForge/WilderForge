package com.wildermods.wilderforge.api.modLoadingV1;

import com.worldwalkergames.legacy.game.mods.IModAware;
import com.worldwalkergames.legacy.game.mods.ModInfo;
import com.worldwalkergames.legacy.server.context.ServerDataContext;
import com.worldwalkergames.legacy.server.context.ServerDataContext.ModLocation;

import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.CustomValue.CvArray;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.ModOrigin;
import net.fabricmc.loader.api.metadata.Person;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import static com.wildermods.wilderforge.api.modLoadingV1.ModConstants.*;

import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.coremods.Configuration;
import com.wildermods.wilderforge.api.modLoadingV1.config.Config;

public class CoremodInfo extends ModInfo implements ModContainer, Mod, Config, IModAware {

	public static Files files = Gdx.files;
	
	public transient final ModContainer coremod;
	
	@InternalOnly
	public CoremodInfo(ModContainer coremod) {
		ModMetadata metadata = coremod.getMetadata();
		
		modId = metadata.getId();
		modLocation = ServerDataContext.ModLocation.core;
		modSteamWorkshopId = null;
		alwaysOn = true;
		showInStoryDialog = true;
		showInModConfig = false;
		listInCredits = true;
		
		this.folder = getFolder();
		
		CustomValue HOMEPAGEV = metadata.getCustomValue(HOMEPAGE);
		
		if(modId.equals("java")) {
			author = System.getProperty("java.vendor");
			if(author == null) {
				author = "Unknown vendor";
			}
			url = System.getProperty("java.vendor.url");
		}
		else {
			author = grammaticallyCorrectAuthorList((Person[]) metadata.getAuthors().toArray(new Person[]{}));
			if(HOMEPAGEV != null) {
				url = metadata.getCustomValue(HOMEPAGE).getAsString();
			}
		}
		
		metadata.getContact();
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
		
		name = metadata.getName();
		modLocation = ModLocation.core;
		this.coremod = coremod;
	}
	
	protected CoremodInfo() {this.coremod = null;}; //constructor for missing coremods
	
	public FileHandle getFolder() {
		if(files != null) {
			return files.classpath("");
		}
		return null;
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
			ret.append("and ");
			ret.append(authors[authors.length - 1].getName());
			return ret.toString();
		}
		throw new IllegalArgumentException(Arrays.toString(authors));
	}


	@Override
	public ModMetadata getMetadata() {
		return coremod.getMetadata();
	}

	@Override
	@Deprecated
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

	@Override
	public List<Path> getRootPaths() {
		return coremod.getRootPaths();
	}

	@Override
	public ModOrigin getOrigin() {
		return coremod.getOrigin();
	}

	@Override
	public Optional<ModContainer> getContainingMod() {
		return coremod.getContainingMod();
	}

	@Override
	public Collection<ModContainer> getContainedMods() {
		return coremod.getContainedMods();
	}

	@Override
	@Deprecated
	public Path getPath(String file) {
		return coremod.getPath(file);
	}
	
	@Override
	public Optional<Path> findPath(String path) {
		return coremod.findPath(path);
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return Mod.class;
	}

	@Override
	public String modid() {
		return coremod.getMetadata().getId();
	}

	@Override
	public String version() {
		return coremod.getMetadata().getVersion().toString();
	}
	
	public Object getConfig() {
		return Configuration.getConfig(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Mod) {
			Mod otherMod = Cast.from(o);
			return modid().equals(otherMod.modid()) && version().equals(otherMod.version());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(modid(), version());
	}

	/**
	 * use {@link #modid()} instead
	 */
	@Override
	@Deprecated(forRemoval = false)
	public String getModId() {
		return modid();
	}
	
}
