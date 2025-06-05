package com.wildermods.wilderforge.mixins.vanillafixes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.fmod.FMODLoader;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wildermods.provider.util.logging.Logger;
import com.wildermods.wilderforge.api.mixins.v1.Descriptor;
import com.wildermods.wilderforge.api.mixins.v1.Impossible;

/**
 * Mixin patch to native library loading failures caused by the executable stack flag
 * (PF_X) being set on FMOD native libraries on Linux systems.
 * 
 * <p>
 * Some Linux distributions enforce security policies that prevent loading native libraries
 * with an executable stack, i.e., those with the PF_X flag set on the PT_GNU_STACK program
 * header segment of ELF binaries. The version of FMOD Wildermyth comes with was compiled 
 * with this flag enabled, causing an {@link UnsatisfiedLinkError} when the JVM attempts
 * to load them via {@link System#load(String)}. The game catches this error, but no
 * sound will be available as the library can't be loaded.
 * </p>
 * 
 * <p>
 * This mixin intercepts the native library loading call inside {@link FMODLoader#tryLoad(String)}
 * by wrapping the {@code System.load} invocation. If a load failure due to
 * {@link UnsatisfiedLinkError} occurs, it checks whether the failing library is an FMOD
 * native library (.so file inside a 'fmod_runtimes' path on Linux). If so, it reads the
 * library file, verifies it is a 64-bit little-endian ELF binary, and patches the ELF
 * program header in-place to clear the PF_X (executable) flag on the PT_GNU_STACK segment.
 * The patched file is then overwritten on disk, and the loading can be retried.
 * </p>
 * 
 * <p>
 * If the library already does not have the PF_X flag set, no changes are made and a
 * corresponding log message is recorded.
 * </p>
 * 
 * <p>
 * This approach fixes the issue without requiring the FMOD binaries to be rebuilt or 
 * manually patched. It enables FMOD to load  Linux distributions with strict 
 * executable stack policies.
 * </p>
 * 
 * <p>If patching fails for some reason, an UnsatisifedLinkError is thrown, with it's
 * cause being the underlying exception that caused the patch to fail. Additionally,
 * it's suppressed exception will be the original UnsatisfiedLinkError that occurred
 * before the patching was attempted.
 * </p>
 * 
 * @see <a href="https://github.com/NateAustin/fmod-jni/issues/4">NateAustin/fmod-jni#4</a>  
 * @see <a href="https://github.com/WilderForge/WilderForge/issues/107">WilderForge/WilderForge#107</a>  
 * 
 */
@Mixin(FMODLoader.class)
public abstract class FModBinaryFixerMixin {

	private static final Logger LOGGER = new Logger("FMOD-FIXER");
	
	@WrapOperation(
		method = "tryLoad",
		at = @At(
			value = "INVOKE",
			target = "Ljava/lang/System;" +
				"load(" +
					Descriptor.STRING +
				")" +
				Descriptor.VOID
		),
		expect = 1
	)
	private static void onLibraryLoad(String library, Operation<Void> original){
		try {
			original.call(library);
		}
		catch(UnsatisfiedLinkError e) {
			try {
				if(isFMODBinary(library) && isLinuxBinary(library)) {
					stripLibrary(library);
					original.call(library);
				}
			}
			catch(Throwable t) {
				
				UnsatisfiedLinkError e2 = new UnsatisfiedLinkError();
				e2.addSuppressed(e);
				e2.initCause(t);
				throw e2;
			}
		}
	}
	
	private static @Unique void stripLibrary(String library) {
		LOGGER.warn("Patching " + library);
		try {
			try {
				Path libraryPath = Path.of(library);
				if(!Files.exists(libraryPath)) {
					Impossible.error();
				}
				
				byte[] content = Files.readAllBytes(Path.of(library));
				ByteBuffer buf = ByteBuffer.wrap(content);
				
				if(!isELFFile(buf)) {
					Impossible.error();
				}
				
				stripExecStackFlag(buf);
				
				Files.write(libraryPath, content, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
			}
			catch(IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}
		catch(Throwable t) {
			LOGGER.fatal("Unable to load " + library);
			throw t;
		}
	}
	
	private static @Unique boolean isFMODBinary(String libPath) {
		return libPath.contains("/fmod_runtimes/");
	}
	
	private static @Unique boolean isLinuxBinary(String libPath) {
		return libPath.endsWith(".so");
	}

	private static @Unique boolean isELFFile(ByteBuffer buf) {
		if (buf.limit() < 16) return false;

		ByteBuffer slice = buf.duplicate(); // safe copy for inspection
		byte[] eIdent = new byte[4];
		slice.get(eIdent);
		return eIdent[0] == 0x7F && eIdent[1] == 'E' && eIdent[2] == 'L' && eIdent[3] == 'F';
	}

	private static @Unique void stripExecStackFlag(ByteBuffer inputBuf) throws UnsatisfiedLinkError {
		try {
			// Create a duplicate to avoid modifying caller's buffer state
			ByteBuffer buf = inputBuf.duplicate().order(ByteOrder.LITTLE_ENDIAN);

			if (buf.get(0) != 0x7F || buf.get(1) != 'E' || buf.get(2) != 'L' || buf.get(3) != 'F') {
				throw new IOException("Not a valid ELF file.");
			}

			boolean is64Bit = buf.get(4) == 2;
			boolean isLittleEndian = buf.get(5) == 1;
			if (!is64Bit) throw new IOException("Only 64-bit ELF files supported.");
			if (!isLittleEndian) throw new IOException("Only little-endian ELF files supported.");

			// e_phoff = offset 32, 8 bytes
			long phoff = getLongLE(buf, 32);
			int phentsize = getShortLE(buf, 54);
			int phnum = getShortLE(buf, 56);

			final int PT_GNU_STACK = 0x6474e551;
			final int PF_X = 0x1;

			for (int i = 0; i < phnum; i++) {
				int entryOffset = (int) (phoff + i * phentsize);
				int type = getIntLE(buf, entryOffset);

				if (type == PT_GNU_STACK) {
					int flags = getIntLE(buf, entryOffset + 4);
					if ((flags & PF_X) != 0) {
						int newFlags = flags & ~PF_X;
						putIntLE(buf, entryOffset + 4, newFlags);
						LOGGER.warn("Cleared PF_X flag on PT_GNU_STACK.");
					} else {
						LOGGER.warn("PF_X flag was already not set.");
					}
					return;
				}
			}

			throw new IOException("No PT_GNU_STACK segment found.");
		} catch (Throwable t) {
			UnsatisfiedLinkError err = new UnsatisfiedLinkError();
			err.initCause(t);
			throw err;
		}
	}

	// Helper methods
	private static @Unique int getShortLE(ByteBuffer buf, int offset) {
		return buf.getShort(offset) & 0xFFFF;
	}

	private static @Unique int getIntLE(ByteBuffer buf, int offset) {
		return buf.getInt(offset);
	}

	private static @Unique long getLongLE(ByteBuffer buf, int offset) {
		return buf.getLong(offset);
	}

	private static @Unique void putIntLE(ByteBuffer buf, int offset, int value) {
		buf.putInt(offset, value);
	}
	
}
