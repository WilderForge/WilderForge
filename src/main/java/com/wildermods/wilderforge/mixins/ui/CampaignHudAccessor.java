package com.wildermods.wilderforge.mixins.ui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.Array;
import com.worldwalkergames.engine.EID;
import com.worldwalkergames.engine.EntitiesCollection;
import com.worldwalkergames.legacy.controller.ControllerInputHint;
import com.worldwalkergames.legacy.editor.FlashAchievementUnlocked;
import com.worldwalkergames.legacy.game.action.ability.AbilityMatcher;
import com.worldwalkergames.legacy.game.campaign.ClientCampaignDomain;
import com.worldwalkergames.legacy.game.campaign.ui.AbilityTargetDialogs;
import com.worldwalkergames.legacy.game.campaign.ui.CampaignHud;
import com.worldwalkergames.legacy.game.campaign.ui.CurrentActivityCard;
import com.worldwalkergames.legacy.game.campaign.ui.ResourceCard;
import com.worldwalkergames.legacy.game.common.StoryQueue;
import com.worldwalkergames.legacy.game.common.ui.AbilityBar;
import com.worldwalkergames.legacy.game.common.ui.FillIcon;
import com.worldwalkergames.legacy.game.common.ui.GameConsole;
import com.worldwalkergames.legacy.game.common.ui.NiceImageButton;
import com.worldwalkergames.legacy.game.common.ui.TargetDetailCard;
import com.worldwalkergames.legacy.game.common.ui.UnitPortraits;
import com.worldwalkergames.legacy.game.hints.CampaignHints;
import com.worldwalkergames.legacy.game.mission.ui.EnemyTurnModal;
import com.worldwalkergames.legacy.game.ui.ContextBar;
import com.worldwalkergames.legacy.render.particles.ParticleEmitter;
import com.worldwalkergames.legacy.render.particles.ParticleStageUI;
import com.worldwalkergames.legacy.ui.OverlandControllerInput;
import com.worldwalkergames.legacy.ui.browser.EntityBrowser;
import com.worldwalkergames.ui.AutoSwapDrawable;
import com.worldwalkergames.ui.NiceButton;
import com.worldwalkergames.ui.NiceLabel;
import com.worldwalkergames.ui.layout.CanvasGroup;
import com.worldwalkergames.utils.PerformanceScope;

@Mixin(CampaignHud.class)
public interface CampaignHudAccessor {

	public @Accessor ClientCampaignDomain getDomain();
	public @Accessor EntitiesCollection getEntities();
	public @Accessor("abilityTargetDialogs") AbilityTargetDialogs getAbilityTargetDialog();
	public @Accessor Stage getStage();
	public @Accessor PerformanceScope getPerformanceScope();
	public @Accessor OverlandControllerInput getControllerInput();
	public @Accessor CampaignHints getCampaignHints();
	
	public @Accessor CanvasGroup getCanvas();
	public @Accessor Label getCurrentTimeLabel();
	public @Accessor Label getTimePassingLabel();
	public @Accessor Label getCompanyNameLabel();
	//public @Accessor TimeControlLayout getTimeControlsPanel();
	public @Accessor ResourceCard getResourceCard();
	public @Accessor FillIcon getDoomTrackCountdown();
	public @Accessor NiceLabel getDoomTrackCountdownLabel();
	public @Accessor FillIcon getIncursionCountdown();
	public @Accessor NiceLabel getIncursionCountdownLabel();
	public @Accessor UnitPortraits getUnitPortraits();
	public @Accessor TargetDetailCard getTargetDetailCard();
	public @Accessor CurrentActivityCard getCurrentActivityCard();
	public @Accessor AbilityBar getAbilityBar();
	public @Accessor("currentEntityBrowser") EntityBrowser getEntityBrowser();
	//UISignals probably should not be public
	public @Accessor StoryQueue getStoryQueue();
	//public @Accessor("eventMapper") EventButtonMapper getEventButtonMapper();
	public @Accessor Array<NiceButton> getEventButtons();
	public @Accessor AbilityMatcher getAbilityMatcher();
	public @Accessor Stack getTimeButtons();
	public @Accessor NiceImageButton getGoButton();
	public @Accessor NiceImageButton getStopButton();
	public @Accessor("timeParticlesGlowBG") ParticleEmitter getTimeParticleGlow();
	public @Accessor("timeParticlesCircle") ParticleEmitter getTimeParticleCircle();
	public @Accessor ParticleStageUI getTopParticleStageUI();
	public @Accessor ParticleStageUI getBottomParticleStageUI();
	public @Accessor ContextBar getContextBar();
	//public @Accessor TimeButtonsTooltip getTimeButtonsTooltip();
	public @Accessor EnemyTurnModal getTimeRunningModal();
	public @Accessor boolean getFirstTimeUpdate();
	public @Accessor GameConsole getGameConsole();
	public @Accessor float getMenuButtonsWidth();
	public @Accessor("useSecondLineForObjectives") boolean usesSecondLineForObjectives();
	public @Accessor boolean isBuildValid();
	public @Accessor ParticleEmitter getAutoSaveParticles();
	public @Accessor ParticleEmitter getAutoSaveParticles2();
	public @Accessor FlashAchievementUnlocked getFlashAchievementUnlocked();
	public @Accessor ControllerInputHint getTimeHint();
	public @Accessor AutoSwapDrawable getMultiplayerCursor();
	public @Accessor float getAnimIncursionTimerInterpolation();
	public @Accessor float getAnimIncursionPercent();
	public @Accessor float getAnimIncursionDays();
	public @Accessor ParticleEmitter getHudFocusParticles();
	public @Accessor EID getPreviousSelection();
	//public @Accessor TimeDisplayState getTimeDisplayState();
	public @Accessor("tmp") Vector2 getTempVector();
	public @Accessor Vector3 getRemoteOverlandHover();
	
}
