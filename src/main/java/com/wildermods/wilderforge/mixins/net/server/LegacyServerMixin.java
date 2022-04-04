package com.wildermods.wilderforge.mixins.net.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import com.wildermods.wilderforge.api.serverV1.ServerDeathEvent;
import com.wildermods.wilderforge.api.serverV1.ServerInstantiationEvent;
import com.wildermods.wilderforge.launch.WilderForge;

import com.worldwalkergames.legacy.server.LegacyServer;

@Mixin(LegacyServer.class)
public abstract class LegacyServerMixin {
	
	@Inject(
		at = @At("RETURN"),
		method = "<init>"
	)
	public void onConstructionPost(CallbackInfo c) {
		WilderForge.EVENT_BUS.fire(new ServerInstantiationEvent(thiz()));
	}
	
	@Inject(
		at = @At("RETURN"),
		method = "shutDown"
	)
	public void onShutDownPost(CallbackInfo c) {
		WilderForge.EVENT_BUS.fire(new ServerDeathEvent(thiz()));
	}
	
	@Unique
	private LegacyServer thiz() {
		return (LegacyServer)(Object)this;
	}
}
