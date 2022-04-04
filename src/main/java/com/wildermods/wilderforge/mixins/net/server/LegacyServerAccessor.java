package com.wildermods.wilderforge.mixins.net.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.worldwalkergames.legacy.platform.services.AccountService;
import com.worldwalkergames.legacy.platform.services.PlatformService;
import com.worldwalkergames.legacy.server.ChatService;
import com.worldwalkergames.legacy.server.LegacyServer;
import com.worldwalkergames.legacy.server.ServerContext;
import com.worldwalkergames.legacy.server.routers.ClientRouter;
import com.worldwalkergames.legacy.server.routers.InstanceRouter;

@Mixin(LegacyServer.class)
public interface LegacyServerAccessor {
	
	@Accessor
	public abstract ServerContext getServerContext();
	
	@Accessor
	public abstract InstanceRouter getInstanceRouter();
	
	@Accessor
	public abstract AccountService getAccountService();
	
	@Accessor
	public abstract PlatformService getPlatformService();
	
	@Accessor
	public abstract ClientRouter getClientRouter();
	
	@Accessor
	public abstract ChatService getChatService();
	
}
