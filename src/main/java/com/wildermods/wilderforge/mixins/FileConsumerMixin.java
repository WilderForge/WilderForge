package com.wildermods.wilderforge.mixins;

import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import static com.wildermods.wilderforge.api.mixins.v1.Initializer.*;
import static com.wildermods.wilderforge.api.mixins.v1.Descriptor.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.worldwalkergames.logging.FileConsumer;

@Mixin(value = FileConsumer.class, remap = false)
public class FileConsumerMixin {
	
	@Redirect(
		method = CONSTRUCTOR + "("
			+ STRING
			+ "Lcom/worldwalkergames/logging/FileConsumer$FileConsumerConfig;"
			+ ARRAY_OF + "Lcom/worldwalkergames/logging/FilteringConsumer$Filter;)" + VOID,
		at = @At(value = "INVOKE", 
			target = "Ljava/nio/channels/FileChannel;"
				+ "open("
					+ "Ljava/nio/file/Path;"
					+ ARRAY_OF + "Ljava/nio/file/OpenOption;"
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
