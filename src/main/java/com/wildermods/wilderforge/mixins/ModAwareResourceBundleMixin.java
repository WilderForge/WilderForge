package com.wildermods.wilderforge.mixins;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.badlogic.gdx.files.FileHandle;
import static com.wildermods.wilderforge.api.mixins.v1.Initializer.*;
import static com.wildermods.wilderforge.api.mixins.v1.Descriptor.*;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.wildermods.wilderforge.launch.logging.Logger;
import com.wildermods.wilderforge.launch.resources.CoremodCompatibleResourceBundle;

import com.worldwalkergames.legacy.server.context.ServerDataContext;
import com.worldwalkergames.legacy.server.context.ServerDataContext.ModAwareResourceBundle;

@Mixin(value = ModAwareResourceBundle.class, remap = false)
public class ModAwareResourceBundleMixin implements CoremodCompatibleResourceBundle {

	private static final Logger LOGGER = new Logger(ModAwareResourceBundle.class);
	
	private @Shadow Map<String, Object> lookup; 
	public @Shadow HashMap<String, HashMap<String, Object>> contentByMod;
	
	
	@Inject(at = @At("RETURN"), method = 
			CONSTRUCTOR + "("
				+ "Lcom/worldwalkergames/legacy/server/context/ServerDataContext;"
				+ STRING
				+ "Lcom/badlogic/gdx/files/FileHandle;"
				+ BOOLEAN
				+ "Ljava/util/Locale;"
			+ ")" + VOID,
		require = 1)
	public void constructor(ServerDataContext parentObj, String assetPath, FileHandle bundleForDependencies, boolean fromAllMods, Locale locale, CallbackInfo c) {
		LOGGER.debug("ASSET PATH: " + assetPath);
		for(CoremodInfo coremod : Coremods.getAllCoremods()) {
			addResources(coremod, assetPath, locale);
		}
	}
	
	@Unique
	@Override
	@InternalOnly
	public void addResources(CoremodInfo coremod, String assetPath, Locale locale) {
		String newAssetPath = "assets/" + coremod.modId + "/" + assetPath.replace("assets/", "");
		LOGGER.debug("Loading resources for coremod " + coremod + " in " + newAssetPath);
		ResourceBundle resources = coremod.getResourceBundle(newAssetPath, locale);
		HashMap<String, Object> coremodValues = new HashMap<String, Object>();
		
		if(resources != null) {
			Iterator<String> keys = resources.getKeys().asIterator();
			while(keys.hasNext()) {
				String key = keys.next();
				LOGGER.debug(key);
				Object value = resources.getObject(key);
				lookup.put(key, value);
				coremodValues.put(key, value);
			}
			this.contentByMod.put(coremod.modId, coremodValues);
		}
		else {
			LOGGER.debug("Coremod " + coremod + " has no resources in " + assetPath);
		}
	}
	
}
