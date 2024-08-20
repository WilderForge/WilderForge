package com.wildermods.wilderforge.mixins.serialization;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.badlogic.gdx.utils.Json;
import com.wildermods.wilderforge.serialization.ModInfoSerializer;
import com.worldwalkergames.legacy.game.mods.ModInfo;
import com.worldwalkergames.serialization.Serializer;

@Mixin(Serializer.class)
public class SerializerMixin {
	
	protected @Final @Shadow Json json;
	
	@Inject(
		at = @At("RETURN")
	)
	public void onConstruct(boolean ignoreUnknownFields, CallbackInfo c) {
		this.json.setSerializer(ModInfo.class, new ModInfoSerializer());
	}
	
}
