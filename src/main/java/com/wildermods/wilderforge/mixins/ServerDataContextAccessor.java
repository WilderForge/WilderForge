package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.worldwalkergames.legacy.server.context.ServerDataContext;
import com.worldwalkergames.serialization.Serializer;

@Mixin(ServerDataContext.class)
public interface ServerDataContextAccessor {

	public @Accessor Serializer getSerializer();
	public @Accessor Serializer getForgivingSerializer();
	
	
}
