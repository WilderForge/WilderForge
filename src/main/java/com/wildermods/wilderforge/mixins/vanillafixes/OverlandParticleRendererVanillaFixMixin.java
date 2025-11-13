package com.wildermods.wilderforge.mixins.vanillafixes;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.wildermods.wilderforge.api.mixins.v1.Require;
import com.wildermods.wilderforge.api.modLoadingV1.Mod;
import com.worldwalkergames.legacy.game.campaign.render.OverlandParticleRenderer;
import com.worldwalkergames.legacy.render.particles.ParticleData;
import com.worldwalkergames.render.MeshWriter;

/**
 * This mixin fixes a JVM crash caused by incorrect particle count handling in the {@code renderBucket()} method. 
 * The crash occurs because {@code particlesToDraw} is incremented unconditionally, even when no particle data is written 
 * in {@code writeParticleVerts()}, leading to invalid rendering data being passed to the native code and eventually causing 
 * a segmentation fault.
 * <p>
 * The patch works by redirecting the field access to {@code particlesToDraw} in {@code renderBucket()} to prevent it from 
 * being incremented. Instead, the {@code particlesToDraw} count is increased only if {@code writeParticleVerts()} 
 * successfully writes particle data to the vertex writer. This is accomplished through the {@code increaseParticlesIfSuccessful} 
 * method, which compares the vertex position before and after the {@code writeParticleVerts()} call to determine if any vertices 
 * were written.
 * <p>
 * Additionally, a safeguard is added to ensure that {@code particlesToDraw} is not incremented twice in case the developers 
 * fix the underlying issue in {@code writeParticleVerts()} in future versions of the game. The check 
 * {@code if (particlesToDraw <= prevParticlesToDraw)} ensures that the particle count is only incremented if it hasn't 
 * already been increased by any other fix, preventing double counting.
 */
@Debug(export = true)
@Mixin(OverlandParticleRenderer.class)
@Require(@Mod(modid = "wildermyth", version = "<1.16.559")) //patched in 1.16+559
public class OverlandParticleRendererVanillaFixMixin {

	private @Shadow int particlesToDraw;
	protected @Shadow MeshWriter writer;
	
	@Redirect(
		method = "renderBucket",
		at = @At(
			value = "FIELD",
			target = "particlesToDraw",
			opcode = Opcodes.PUTFIELD
		),
		expect = -1 //so the game doesn't crash when mixin debugging is enabled
	)
	public void dontIncreaseParticlesToDraw(OverlandParticleRenderer thiz, int particlesToDraw) {
		//NO-OP
	}
	
	@WrapMethod(
		method = "writeParticleVerts",
		require = 1
	)
	public void increaseParticlesIfSuccessful(ParticleData data, int i, Operation<Void> original) {
		int prevParticlesToDraw = particlesToDraw;
		int prevVertexPos = writer.vertexPosition;
		original.call(data, i);
		{
			/*
			 * If the developers fix this issue in their own code by incrementing particlesToDraw in
			 * the writeParticleVerts method, we don't want to increase the particle count again!
			 */
			if(particlesToDraw <= prevParticlesToDraw) { 
				
				/**
				 * If particle data was successfully written, increase particlesToDraw 
				 */
				if(writer.vertexPosition > prevVertexPos) {
					particlesToDraw++;
				}
			}
		}
	}
	
	
}
