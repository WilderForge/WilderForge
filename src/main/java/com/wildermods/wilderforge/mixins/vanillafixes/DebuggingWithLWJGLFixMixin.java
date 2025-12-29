package com.wildermods.wilderforge.mixins.vanillafixes;

import org.lwjgl.system.ThreadLocalUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import com.wildermods.provider.util.logging.Logger;
import com.wildermods.wilderforge.api.mixins.v1.Descriptor;
import com.wildermods.wilderforge.api.mixins.v1.Initializer;
import com.wildermods.wilderforge.api.mixins.v1.Require;
import com.wildermods.wilderforge.api.modLoadingV1.Mod;
import com.wildermods.wilderforge.launch.coremods.Coremods;

/**
 * This mixin patches LWJGL's {@link ThreadLocalUtil} initialization to prevent
 * native crashes when running under newer Java versions while a debugger is attached.
 *
 * <p>
 * LWJGL relies on HotSpot implementation details to inject ThreadLocal OpenGL
 * capability pointers into the {@code JNIEnv} structure. To do this safely, it
 * must know the <em>exact</em> number of function pointers present in the
 * {@code JNINativeInterface} table for the running JVM.
 * </p>
 *
 * <p>
 * The value of {@code JNI_NATIVE_INTERFACE_FUNCTION_COUNT} is computed during
 * {@link ThreadLocalUtil}'s static initialization based on the JNI version
 * reported by the JVM. If this count is incorrect, LWJGL may attempt to write outside
 * of the allocated function table, causing a segmentation fault. In practice, this
 * has manifested as a JVM crash when the Java Debug Wire Protocol (JDWP) is
 * active when OpenGL capabilities are initialized (e.g. during {@link GL.createCapabilities()}).
 * </p>
 *
 * <p>
 * This mixin intercepts the static write to
 * {@link ThreadLocalUtil#JNI_NATIVE_INTERFACE_FUNCTION_COUNT} and overrides it with a value that
 * is known to be correct for specific Java major versions (20–24), accounting
 * for the number of reserved {@code JNIEnv} slots detected at runtime.
 * </p>
 *
 * <p>
 * The fix is applied only when running on Java 20 or newer, as earlier versions
 * (notably Java 8-17) do not exhibit the debugger induced crash. For unknown or
 * future Java versions, a warning is logged and the latest known safe layout is
 * assumed to reduce the likelihood of native memory corruption.
 * </p>
 */
@Mixin(ThreadLocalUtil.class)
@Require(@Mod(modid = "java", version = ">=20"))
public class DebuggingWithLWJGLFixMixin {

	private static @Mutable @Shadow int JNI_NATIVE_INTERFACE_FUNCTION_COUNT;
	private static Logger LOGGER;
	
	@WrapOperation(
		method = Initializer.STATIC_INIT,
		at = @At(
			value = "FIELD",
			target = "Lorg/lwjgl/system/ThreadLocalUtil;"
					+ "JNI_NATIVE_INTERFACE_FUNCTION_COUNT:" + Descriptor.INT
		)
	)
	private static void onFunctionCountSet(int value, Operation<Void> original, @Local(name = "reservedCount") int reservedCount) {
		LOGGER = new Logger(ThreadLocalUtil.class);
		final String javaVersion = Coremods.getCoremod("java").getMetadata().getVersion().toString();
		switch(javaVersion) {
			case "20":
				original.call(231 + reservedCount);
				break;
			case "21":
			case "22":
			case "23":
				original.call(232 + reservedCount);
				break;
			default:
				LOGGER.fatal("******************************************************");
				LOGGER.fatal("Unknown java version " + javaVersion + " may cause native crashes");
				LOGGER.fatal("If you are experiencing a native crash, ensure org.lwjgl.system.ThreadLocalUtil#JNI_NATIVE_INTERFACE_FUNCTION_COUNT is correct for your version of java");
				LOGGER.fatal("******************************************************");
			case "24":
				original.call(233 + reservedCount);
				break;
		}
	}
	
}
