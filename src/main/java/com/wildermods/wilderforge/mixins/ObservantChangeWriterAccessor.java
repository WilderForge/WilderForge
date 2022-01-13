package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.badlogic.gdx.utils.Array;
import com.worldwalkergames.engine.EntitiesCollection;
import com.worldwalkergames.legacy.game.campaign.ParticipantHostDomain;
import com.worldwalkergames.legacy.game.mechanics.ObservantChangeWriter;
import com.worldwalkergames.legacy.game.mission.VisibilityManager;

@Mixin(ObservantChangeWriter.class)
public interface ObservantChangeWriterAccessor {

	public @Accessor ParticipantHostDomain getDomain();
	public @Accessor EntitiesCollection getEntities();
	public @Accessor Array<VisibilityManager> getVisibilityManagers();
	
}
