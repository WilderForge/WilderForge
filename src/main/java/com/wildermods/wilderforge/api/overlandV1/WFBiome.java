package com.wildermods.wilderforge.api.overlandV1;

import com.wildermods.wilderforge.api.TraitV1.Traited;

public interface WFBiome extends Traited {
	
	public static final String WEIGHT = "weight";
	public static final String PASSABLE = "passable";
	public static final String IS_WATER = "isWater";
	
	public boolean isPassable();
	
	public boolean isWater();
	
	public float getWeight();
	
	public void setPassable(boolean passable);
	
}
