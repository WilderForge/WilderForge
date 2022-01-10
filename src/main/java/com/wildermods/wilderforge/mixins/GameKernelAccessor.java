package com.wildermods.wilderforge.mixins;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.worldwalkergames.engine.EID;
import com.worldwalkergames.engine.EntitiesCollection;
import com.worldwalkergames.legacy.game.action.ActionOutcomeSummary;
import com.worldwalkergames.legacy.game.action.IReadOnlyRoleMap;
import com.worldwalkergames.legacy.game.action.RoleMap;
import com.worldwalkergames.legacy.game.action.ability.AbilityMatcher;
import com.worldwalkergames.legacy.game.campaign.CalamityDeck;
import com.worldwalkergames.legacy.game.campaign.model.Party;
import com.worldwalkergames.legacy.game.generation.HistoryGenerator;
import com.worldwalkergames.legacy.game.mechanics.CampaignMissionProcessor;
import com.worldwalkergames.legacy.game.mechanics.ChangeWriter;
import com.worldwalkergames.legacy.game.mechanics.EffectResolver;
import com.worldwalkergames.legacy.game.mechanics.ExperienceProcessor;
import com.worldwalkergames.legacy.game.mechanics.GearProcessor;
import com.worldwalkergames.legacy.game.mechanics.JobProcessor;
import com.worldwalkergames.legacy.game.mechanics.NeutralAIDriver;
import com.worldwalkergames.legacy.game.mechanics.PartyLogic;
import com.worldwalkergames.legacy.game.mechanics.PlotProcessor;
import com.worldwalkergames.legacy.game.mechanics.RelationshipProcessor;
import com.worldwalkergames.legacy.game.mechanics.SpatialRelationshipProcessor;
import com.worldwalkergames.legacy.game.model.GridMapBase;
import com.worldwalkergames.legacy.game.model.legacy.LegacyEntityFactory;
import com.worldwalkergames.legacy.game.model.status.AspectIndex;
import com.worldwalkergames.legacy.game.model.status.Status;
import com.worldwalkergames.legacy.server.context.ServerDataContext;
import com.worldwalkergames.serialization.Serializer;

@Mixin(priority = 950, remap = false, targets = "com.worldwalkergames.legacy.game.mechanics.GameKernel")
public interface GameKernelAccessor {

	public @Accessor("isDebug") boolean isDebug();
	public @Accessor ChangeWriter getChangeWriter();
	public @Accessor EntitiesCollection getEntities();
	public @Accessor Random getRandom();
	public @Accessor AbilityMatcher getAbilityMatcher();
	public @Accessor ServerDataContext getDataContext();
	public @Accessor AspectIndex getAspectIndex();
	public @Accessor("isSimulator") boolean isSimulator();
	public @Accessor LegacyEntityFactory getEntityFactory();
	public @Accessor HistoryGenerator getHistoryGenerator();
	public @Accessor EffectResolver getEffectResolver();
	public @Accessor EID getGameId();
	public @Accessor Serializer getSerializer();
	public @Accessor CalamityDeck getCalamityDeck();
	public @Accessor NeutralAIDriver getNeutralAIDriver();
	public @Accessor SpatialRelationshipProcessor getSpatialRelationshipProcessor();
	public @Accessor CampaignMissionProcessor getCampaignMissionProcessor();
	public @Accessor JobProcessor getJobProcessor();
	public @Accessor PartyLogic getPartyLogic();
	//public @Accessor EncounterProcessor getEncounterProcessor();
	public @Accessor PlotProcessor getPlotProcessor();
	public @Accessor ExperienceProcessor getExperienceProcessor();
	public @Accessor RelationshipProcessor getRelationshipProcessor();
	public @Accessor GearProcessor getGearProcessor();
	public @Accessor IReadOnlyRoleMap.LookupHost getLookupHost();
	public @Accessor ActionOutcomeSummary getPrediction();
	public @Accessor("autoSaveRequested") boolean isAutoSaveRequested();
	public @Accessor("dailiesRoles") RoleMap getDailyRoles();
	public @Accessor("isUpdatingPlots") boolean isUpdatingPlots();
	public @Accessor Array<Status.EffectInstance> getCollectImpactTmpEffects();
	public @Accessor Pool<Array<GridMapBase.RegionEntry>> getGridListPool();
	
	public @Accessor("isDebug") void setDebug(boolean debug);
	public @Accessor("isSimulator") void setSimulator(boolean simulator);
	public @Accessor void setEffectResolver(EffectResolver effectResolver);
	public @Accessor void setSpatialRelationshipProcessor(SpatialRelationshipProcessor spatialProcessor);
	public @Accessor void setPrediction(ActionOutcomeSummary getPrediction);
	public @Accessor("autoSaveRequested") void setAutoSaveRequested(boolean autoSave);
	public @Accessor("dailiesRoles") void setDailyRoles(RoleMap dailyRoles);
	public @Accessor("isUpdatingPlots") void setUpdatingPlots(boolean updatePlots);
	
	public @Invoker void invokeDisbandParty(Party party);
	
}
