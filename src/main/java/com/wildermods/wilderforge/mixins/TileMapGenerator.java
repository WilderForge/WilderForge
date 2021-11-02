package com.wildermods.wilderforge.mixins;

import java.util.HashMap;
import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import com.worldwalkergames.engine.EntitiesCollection;
import com.worldwalkergames.legacy.game.campaign.model.OverlandMapSecrets;
import com.worldwalkergames.legacy.game.generation.BitmapLayer;
import com.worldwalkergames.legacy.game.model.status.AspectIndex;
import com.worldwalkergames.legacy.game.world.model.OverlandPathfindingState;
import com.worldwalkergames.legacy.game.world.model.OverlandTile;
import com.worldwalkergames.legacy.game.world.model.WorldMapInfo;
import com.worldwalkergames.legacy.game.world.tiles.OverlandGenContext;
import com.worldwalkergames.legacy.game.world.tiles.OverlandTileMapGenerator;
import com.worldwalkergames.logging.ALogger;

@Mixin(value = OverlandTileMapGenerator.class, remap = false)
public interface TileMapGenerator {
	
	@Accessor("LOGGER")
	public ALogger getDefaultLogger();

	@Accessor
	public OverlandGenContext getContext();
	
	@Accessor
	public Random getRandom();
	
	@Accessor
	public EntitiesCollection getEntities();
	
	@Accessor
	public Vector2 getOverallSlope();
	
	@Accessor
	public WorldMapInfo.MapConfig getMapConfig();
	
	@Accessor("tmp")
	public Vector2 getTempVec1();
	
	@Accessor("tmp2")
	public Vector2 getTempVec2();
	
	@Invoker
	public void callAddTileAspects(Array<OverlandTile> tiles);
	
	@Invoker
	public void callValidateBasics(Array<OverlandTile> tiles, OverlandPathfindingState pathfinding);
	
	@Invoker
	public void callValidatePlayableChapters(OverlandPathfindingState pathfinding);
	
	@Invoker
	public OverlandTile callPlaceOriginTown(Array<OverlandTile> tiles);
	
	@Invoker
	public void callAssignRandomColors(Array<OverlandTile> tiles);
	
	@Invoker
	public void callGenerateTiles(float size, float edgeBuffer, float minNodeSize, float nodeSizeRange, HashMap<Vector2, Array<Vector2>> pointMap, EntitiesCollection entities, float distanceRange);
	
	@Invoker
	public void callFixShortEdges(float minNodeSize, Array<OverlandTile> tiles, HashMap<Vector2, Array<Vector2>> pointMap);
	
	@Invoker
	public BitmapLayer callAssignElevation(float size, Array<OverlandTile> tiles);
	
	@Invoker
	public void callAssignBiomes(Array<OverlandTile> tiles, BitmapLayer elevation);
	
	@Invoker
	public void callCacheNeighborBiomes(Array<OverlandTile> tiles);
	
	@Invoker
	public void callAssignAspects(AspectIndex aspectIndex, OverlandTile tile);
	
	@Invoker
	public void callAssignHills(Array<OverlandTile> tiles);
	
	@Invoker
	public void callAssignLakesAndSwamps(Array<OverlandTile> tiles, int numLakeTiles);
	
	@Invoker
	public void callAssignMountains(Array<OverlandTile> tiles, int numMountainTiles);
	
	@Invoker
	public void callJaggedize(float minNodeSize, Array<OverlandTile> tiles);
	
	@Invoker
	public void callInsetEdges(Array<OverlandTile> tiles, float minNodeSize);
	
	@Invoker
	public void callMove(HashMap<Vector2, Array<Vector2>> pointMap, Vector2 old, Vector2 neww);
	
	@Invoker
	public void callAddPoint(HashMap<Vector2, Array<Vector2>> pointMap, Vector2 point);
	
	@Invoker
	public void callSetupChapterTiles(Array<OverlandTile> tiles, OverlandPathfindingState pathfinding);
	
	@Invoker
	public void callPlaceExtraBarriers(OverlandMapSecrets mapSecrets);
	
	@Invoker
	public void callWriteBorderTypeAndElevation(WorldMapInfo.ChapterBarrier extraBarrier, OverlandTile.Border edgeA, OverlandTile a, OverlandTile b);
	
	@Invoker
	public WorldMapInfo.ChapterTileGrouping callGetChapterTileGrouping(int chapter);
	
	@Invoker
	public int[] callGetChapterTileNumbers(int numChapterTiles);
	
	@Invoker
	public void callAssignTileDoodads(Array<OverlandTile> tiles);
	
	@Invoker
	public void callSpawnMapFeatures(Array<OverlandTile> tiles);
	
	@Invoker
	public void callNameTiles(Array<OverlandTile> tiles);
	
	@Invoker
	public void callRandomFounders(Array<OverlandTile> tiles);
	
}
