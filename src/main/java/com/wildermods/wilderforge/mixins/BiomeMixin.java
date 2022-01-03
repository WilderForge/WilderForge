package com.wildermods.wilderforge.mixins;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import static org.spongepowered.asm.mixin.injection.At.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.wildermods.wilderforge.api.overlandV1.WFBiome;
import com.wildermods.wilderforge.api.traitV1.Trait;
import com.worldwalkergames.collection.WeightedList;
import com.worldwalkergames.legacy.game.world.model.OverlandTile.Biome;
import static com.worldwalkergames.legacy.game.world.model.OverlandTile.Biome.*;

@Mixin(value = Biome.class, remap = false)
public class BiomeMixin implements WFBiome {

	static {
		Biome.values();
	}
	
	private static @Shadow @Final @Mutable Biome[] $VALUES;
	private @Shadow @Final @Mutable boolean passable;
	private @Unique HashMap<String, Trait<?>> traits = new HashMap<String, Trait<?>>();
	
	@Invoker("<init>")
	private static Biome newBiome(String internalName, int internalId, boolean passable) {
		throw new AssertionError();
	}
	
	
	@Inject(method = "<clinit>",
		at = @At(
			value = "FIELD", 
			opcode = Opcodes.PUTSTATIC,
			target = 
				"Lcom/worldwalkergames/legacy/game/world/model/OverlandTile$Biome;"
				+ "$VALUES:"
				+ "[Lcom/worldwalkergames/legacy/game/world/model/OverlandTile$Biome;",
			shift = Shift.AFTER),
		require = 1
	)
	private static void addCustomBiome(CallbackInfo ci) {
		List<Biome> biomes = new ArrayList<Biome>(Arrays.asList($VALUES));
		setVanillaWeights();
		//TODO: CreateCustomBiomesEvent
		
		//TODO: Add the custom biomes
		
		//Biome lastBiome = biomes.get(biomes.size() - 1);
		//Biome biome = newBiome("testBiome", lastBiome.ordinal() + 1, true);
		//((WFBiome)(Object)biome).setTrait(IS_WATER, false);
		//((WFBiome)(Object)biome).setTrait(PASSABLE, true);
		//((WFBiome)(Object)biome).setTrait(WEIGHT, 1f);
		//biomes.add(biome);
		
		$VALUES = biomes.toArray(new Biome[0]);
	}
	
	@Unique
	private static void setVanillaWeights() {
		for(Biome vBiome : $VALUES) {
			WFBiome biome = (WFBiome)(Object)vBiome;
			if (vBiome == forestConiferous) {
				setDefaultTraits(biome, 0.6f);
			}
			else if (vBiome == forestDeciduous) {
				setDefaultTraits(biome, 0.7f);
			}
			else if (vBiome == grassland) {
				setDefaultTraits(biome, 1f);
			}
			else if (vBiome == swamp) {
				setDefaultTraits(biome, 0.4f);
			}
			else if (vBiome == hills) {
				setDefaultTraits(biome, 0.6f);
			}
			else if (vBiome == none || vBiome == mountains || vBiome == ocean || vBiome == lake) {
				setDefaultTraits(biome, 0f);
			}
			else {
				throw new AssertionError("Unexpected Biome " + vBiome + " were new biomes added by vanilla? If not, make sure you're adding biomes AFTER Wilderforge's BiomeMixin.");
			}
		}
	}
	
	
	@Inject(method = "createWeightedList",
			at = @At(
				value = "HEAD"
			),
			require = 1,
			cancellable = true
		)
	private static void createWeightedList(Random random, CallbackInfoReturnable<WeightedList<Biome>> c) {
		try {
			WeightedList<Biome> list = new WeightedList<Biome>(random);
			for(Biome biome : $VALUES) {
				if(biome != none && biome != mountains && biome != ocean && biome != lake) {
					list.add(biome, 1f);
				}
			}
			c.setReturnValue(list);
		}
		catch(Throwable t) {
			throw new Error(t); //Wildermyth is swallowing exceptions, so throw an error instead.
		}
	}
	
	
	private static void setDefaultTraits(WFBiome biome, float weight) {
		biome.setTrait(WEIGHT, weight);
		biome.setTrait(PASSABLE, true);
		biome.setTrait(IS_WATER, false);
	}
	
	public Biome setTraits(boolean passable, float weight, boolean isWater) {
		setTrait(WEIGHT, weight);
		setTrait(PASSABLE, passable);
		setTrait(IS_WATER, isWater);
		return (Biome)(Object)this;
	}

	@Override
	public boolean isPassable() {
		return getTrait("passable");
	}
	
	@Override
	public void setPassable(boolean passable) {
		this.passable = passable;
		setTrait("passable", passable);
	}

	public boolean hasTrait(String name) {
		return traits.containsKey(name);
	}
	

	@Unique
	public boolean hasTrait(String name, Object value) {
		Trait<?> trait = traits.get(name);
		if(trait != null) {
			return trait.getValue().equals(value);
		}
		return false;
	}

	@Unique
	@SuppressWarnings("unchecked")
	public <T> T getTrait(String name) {
		if(hasTrait(name)) {
			return ((Trait<T>) traits.get(name)).getValue();
		}
		return null;
	}

	@Unique
	@Override
	@SuppressWarnings("unchecked")
	public <T> void setTrait(String name, T value) throws IllegalArgumentException {
		if(!hasTrait(name)) {
			traits.put(name, new Trait<T>(name, value));
		}
		else {
			((Trait<T>)traits.get(name)).setValue(value);
		}
	}

	@Unique
	@Nullable
	@SuppressWarnings("unchecked")
	public <T> Trait<T> getRawTrait(String name) {
		return (Trait<T>) traits.get(name);
	}

	@Override
	public boolean isWater() {
		return hasTrait("water", true);
	}

	@Unique
	@Override
	public float getWeight() {
		return getTrait("weight");
	}

	@Unique
	@Override
	public HashMap<String, Trait<?>> getTraits() {
		return traits;
	}

}
