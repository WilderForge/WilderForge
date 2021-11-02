package com.wildermods.wilderforge.mixins;

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

import com.wildermods.wilderforge.api.TraitV1.Trait;
import com.wildermods.wilderforge.api.overlandV1.WFBiome;
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
		Biome lastBiome = biomes.get(biomes.size() - 1);
		Biome biome = newBiome("testBiome", lastBiome.ordinal() + 1, true);
		biomes.add(biome);
		$VALUES = biomes.toArray(new Biome[0]);
	}
	
	@Unique
	private static void setVanillaWeights() {
		for(Biome vanillaBiome : $VALUES) {
			WFBiome biome = (WFBiome)(Object)vanillaBiome;
			if((Biome)(Object)biome == none) {
				setDefaultTraits(biome, 0f);
			}
			else if ((Biome)(Object)biome == forestConiferous) {
				setDefaultTraits(biome, 0.6f);
			}
			else if ((Biome)(Object)biome == forestDeciduous) {
				setDefaultTraits(biome, 0.7f);
			}
			else if ((Biome)(Object)biome == grassland) {
				setDefaultTraits(biome, 1f);
			}
			else if ((Biome)(Object)biome == swamp) {
				setDefaultTraits(biome, 0.4f);
			}
			else if ((Biome)(Object)biome == hills) {
				setDefaultTraits(biome, 0.6f);
			}
			else {
				throw new AssertionError("Unexpected Biome " + vanillaBiome + " were new biomes added by vanilla? If not, make sure you're adding biomes AFTER Wilderforge.");
			}
		}
	}
	
	@Inject(method = "createWeightedList",
			at = @At(
				value = "HEAD"
			),
			require = 1
		)
	private static void createWeightedList(Random random, CallbackInfoReturnable<WeightedList<Biome>> c) {
		WeightedList<Biome> list = new WeightedList<Biome>(random);
		for(Biome biome : $VALUES) {
			list.add(biome, ((WFBiome)(Object)biome).getWeight());
		}
		c.setReturnValue(list);
	}
	
	@Unique
	private static void setDefaultTraits(WFBiome biome, float weight) {
		biome.setTrait(WEIGHT, weight);
		biome.setTrait(PASSABLE, true);
		biome.setTrait(IS_WATER, false);
	}

	@Unique
	@Override
	public boolean isPassable() {
		return getTrait("passable");
	}
	
	@Unique
	@Override
	public void setPassable(boolean passable) {
		this.passable = passable;
		setTrait("passable", passable);
	}

	@Unique
	@Override
	public boolean hasTrait(String name) {
		return traits.containsKey(name);
	}
	

	@Unique
	@Override
	public boolean hasTrait(String name, Object value) {
		Trait<?> trait = traits.get(name);
		if(trait != null) {
			return trait.getValue().equals(value);
		}
		return false;
	}

	@Unique
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getTrait(String name) {
		return ((Trait<T>) traits.get(name)).getValue();
	}

	@Unique
	@Override
	@SuppressWarnings("unchecked")
	public <T> void setTrait(String name, T value) throws IllegalArgumentException {
		((Trait<T>)traits.get(name)).setValue(value);
	}

	@Unique
	@Override
	@SuppressWarnings("unchecked")
	public <T> Trait<T> getRawTrait(String name) {
		return (Trait<T>) traits.get(name);
	}

	@Unique
	@Override
	public boolean isWater() {
		return hasTrait("water", true);
	}

	@Unique
	@Override
	public float getWeight() {
		return getTrait("weight");
	}

}
