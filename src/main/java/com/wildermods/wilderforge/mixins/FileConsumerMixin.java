package com.wildermods.wilderforge.mixins;

import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.worldwalkergames.logging.FileConsumer;

@Mixin(value = FileConsumer.class, remap = false)
public class FileConsumerMixin {
	
	@Redirect(
			method = "<init>("																				//method (constructor)
				+ "Ljava/lang/String;"																			//param1
				+ "Lcom/worldwalkergames/logging/FileConsumer$FileConsumerConfig;"								//param2
				+ "[Lcom/worldwalkergames/logging/FilteringConsumer$Filter;)V", 								//param3
			at = @At(value = "INVOKE", 
				target = "Ljava/nio/channels/FileChannel;"		 											//class
					+ "open("																				//method
						+ "Ljava/nio/file/Path;"																//param1
						+ "[Ljava/nio/file/OpenOption;"															//param2
					+ ")Ljava/nio/channels/FileChannel;"													//return type
			),
			require = 2
	)
	public FileChannel dontCreateLogChannels(Path path, OpenOption... options) {
		return null;
	}
	
}
