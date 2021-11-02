package com.wildermods.wilderforge.api.overlandV1;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.worldwalkergames.legacy.game.campaign.model.CampaignTemplate;
import com.worldwalkergames.legacy.game.campaign.model.GameSettings;
import com.worldwalkergames.legacy.game.world.tiles.OverlandGenContext;
import com.worldwalkergames.legacy.game.world.tiles.OverlandTileMapGenerator;
import com.worldwalkergames.legacy.platform.model.NewGameRequest;

public class OverlandMapGenerationEvent extends Event {

	private final boolean isFreshGeneration;
	private final OverlandTileMapGenerator initalGenerator;
	private OverlandTileMapGenerator generator;
	private OverlandGenContext context;
	private CampaignTemplate campaignTemplate;
	private GameSettings gameSettings;
	private NewGameRequest newGameRequest;
	
	public OverlandMapGenerationEvent(OverlandTileMapGenerator generator, OverlandGenContext context, boolean isFreshGeneration) {
		super(false);
		this.initalGenerator = generator;
		this.generator = generator;
		this.context = context;
		this.isFreshGeneration = isFreshGeneration;
	}
	
	public OverlandMapGenerationEvent(OverlandTileMapGenerator generator, OverlandGenContext context, CampaignTemplate campaignTemplate, GameSettings settings, NewGameRequest request) {
		this(generator, context, true);
		this.campaignTemplate = campaignTemplate;
		this.gameSettings = settings;
		this.newGameRequest = request;
	}
	
	public boolean isFreshGeneration() {
		return isFreshGeneration;
	}
	
	public OverlandTileMapGenerator getGenerator() {
		return generator;
	}
	
	public void setGenerator(OverlandTileMapGenerator generator) {
		this.generator = generator;
	}
	
	public OverlandTileMapGenerator getInitialGenerator() {
		return initalGenerator;
	}
	
	public OverlandGenContext getGenerationContext() {
		return context;
	}
	
	public CampaignTemplate getCampaignTemplate() {
		return campaignTemplate;
	}
	
	public GameSettings getGameSettings() {
		return gameSettings;
	}
	
	public NewGameRequest getNewGameRequest() {
		return newGameRequest;
	}
	
	
}
