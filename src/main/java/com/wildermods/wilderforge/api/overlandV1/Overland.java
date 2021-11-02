package com.wildermods.wilderforge.api.overlandV1;

import java.util.Arrays;

import com.wildermods.wilderforge.api.enumV1.EnumExtensionError;
import com.wildermods.wilderforge.api.enumV1.ExtendableEnum;
import com.wildermods.wilderforge.api.enumV1.ExtendableEnum.EnumValue;

import com.worldwalkergames.legacy.game.world.model.OverlandTile.Biome;

public class Overland {

	public ExtendableEnum<Biome> BIOMES = new ExtendableEnum<Biome>(Biome.class){		
		@Override
		public BiomeEnumValue newEnumValue(String name, Object[] parameters, String[] names, Class<?>[] types) {
			if(parameters.length == 1 && parameters[0].getClass() == Boolean.class) { //cannot be boolean.class due to autoboxing
				return new BiomeEnumValue(this, name, ((Boolean)parameters[0]).booleanValue());
			}
			throw new EnumExtensionError(new IllegalArgumentException("Biome enum cannot accept the parameters " + Arrays.toString(parameters)));
		}
	};
	
	public static class BiomeEnumValue extends EnumValue<Biome> {

		private boolean passable;
		
		protected BiomeEnumValue(ExtendableEnum<Biome> extendedEnum, Biome enumValue) {
			super(extendedEnum, enumValue);
			this.passable = enumValue.passable;
		}
		
		public BiomeEnumValue(ExtendableEnum<Biome> extendedEnum, String name, boolean passable) {
			super(extendedEnum, name);
			this.passable = passable;
		}
		
		public boolean isPassable() {
			return passable;
		}
		
	}
	
}
