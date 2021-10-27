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
		method = "<init>("
			+ "Ljava/lang/String;"
			+ "Lcom/worldwalkergames/logging/FileConsumer$FileConsumerConfig;"
			+ "[Lcom/worldwalkergames/logging/FilteringConsumer$Filter;)V",
		at = @At(value = "INVOKE", 
			target = "Ljava/nio/channels/FileChannel;"
				+ "open("
					+ "Ljava/nio/file/Path;"
					+ "[Ljava/nio/file/OpenOption;"
				+ ")Ljava/nio/channels/FileChannel;"
		),
		require = 2
	)
	/*
	 * So wildermyth doesn't create empty log files
	 */
	public FileChannel dontCreateLogChannels(Path path, OpenOption... options) {
		return null;
	}
	
}
