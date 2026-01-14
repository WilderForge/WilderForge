package com.wildermods.wilderforge.launch.mixin.plugin;

import java.lang.annotation.AnnotationFormatError;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import com.llamalad7.mixinextras.transformer.MixinTransformer;
import com.llamalad7.mixinextras.utils.MixinInternals;
import com.wildermods.wilderforge.api.mixins.v1.Require;
import com.wildermods.wilderforge.launch.mixin.plugin.Requirements.RequireData;

import static com.wildermods.wilderforge.launch.mixin.plugin.Requirements.*;
import com.wildermods.wilderforge.launch.exception.UnsatisifiedRequirementError;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;

public class RequirementsPlugin implements IMixinConfigPlugin {

	private static final LogCategory CAT = LogCategory.createCustom("WilderForge", "RequirementsPlugin");
	
	static {
		MixinInternals.registerExtension(new RequirementMixinTransformer());
	}
	
	@Override
	public void onLoad(String mixinPackage) {
		//NO-OP
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		ClassNode node;
		String resource = mixinClassName.replace('.', '/') + ".class";
		if(mixinClassName.endsWith("MainMenuMixin")) {
			new Object().toString();
		}
		try (InputStream in = Requirements.class.getClassLoader().getResourceAsStream(resource)) {
			if (in == null) {
				throw new NoClassDefFoundError(resource);
			}

			node = new ClassNode();
			new ClassReader(in).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to read mixin class " + mixinClassName, e);
		}
		
		return RequirementMixinTransformer.shouldApplyElement("class " + mixinClassName, node.visibleAnnotations, null);
	}
	
	private static void logSkip(String mixin, String modid, String reason, Object context, boolean shouldCrash) {
		if(context instanceof IMixinInfo) {
			context = ((IMixinInfo) context).getConfig().getName() + " - " + ((IMixinInfo) context).getName();
		}
		if(shouldCrash) {
			throw new UnsatisifiedRequirementError("Failed to apply " + mixin + " because '" + modid + "' " + reason + " (" + context + ")");
		}
		Log.warn(CAT, "Skipping " + mixin + " because '" + modid + "' " + reason + " (" + context + ")");
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
		//NO-OP
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		//NO-OP
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		//NO-OP
	}
	
	/*
	 * 
	 * This class contains significant portions from MixinExtras by LlamaLad7
	 * licensed under the MIT license. The license is provided below per the license
	 * requirements.
	 * 
	 * https://github.com/LlamaLad7/MixinExtras/blob/master/LICENSE
	 * 
	 * MIT License
	 * 
	 * Copyright (c) 2022-present LlamaLad7
	 * 
	 * Permission is hereby granted, free of charge, to any person obtaining a copy
	 * of this software and associated documentation files (the "Software"), to deal
	 * in the Software without restriction, including without limitation the rights
	 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	 * copies of the Software, and to permit persons to whom the Software is
	 * furnished to do so, subject to the following conditions:
	 * 
	 * The above copyright notice and this permission notice shall be included in all
	 * copies or substantial portions of the Software.
	 * 
	 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	 * SOFTWARE.
	 */
	static class RequirementMixinTransformer implements IExtension, MixinTransformer {

		private final Set<ClassNode> preparedMixins = Collections.newSetFromMap(new WeakHashMap<>());
		private final List<MixinTransformer> transformers = Arrays.asList(
			this
		);
		
		@Override
		public boolean checkActive(MixinEnvironment environment) {
			return true;
		}

		@Override
		public void preApply(ITargetClassContext context) {
			for (Pair<IMixinInfo, ClassNode> pair : MixinInternals.getMixinsFor(context)) {
				IMixinInfo info = pair.getLeft();
				ClassNode node = pair.getRight();
				if (preparedMixins.contains(node)) {
					// Don't scan the whole class again
					continue;
				}
				for (MixinTransformer transformer : transformers) {
					transformer.transform(info, node);
				}
				preparedMixins.add(node);
			}
		}

		@Override
		public void postApply(ITargetClassContext context) {
			//NO-OP
		}

		@Override
		public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {
			//NO-OP
		}

		@Override
		public void transform(IMixinInfo mixinInfo, ClassNode mixinNode) {
			Log.error(CAT, "Transforming " + mixinInfo.getClassName());
			if(mixinInfo.getClassName().endsWith("MainMenuMixin")) {
				new Object().toString();
			}
			mixinNode.fields.removeIf(field -> !shouldApplyElement("field " + field.name + field.signature, field.visibleAnnotations, mixinInfo));
			mixinNode.methods.removeIf(method -> !shouldApplyElement("method " + getSignature(method), method.visibleAnnotations, mixinInfo));
		}
		
		private static boolean shouldApplyElement(String name, List<AnnotationNode> annotations, Object context) {
			List<RequireData> requirements = getRequirements(annotations);
			if(requirements.size() == 0) {
				return true;
			}
			int i = 0;
			for (RequireData req : requirements) {
				i++;
				String modid = req.modid();
				String versionSpec = req.version();
				boolean shouldCrash = req.shouldCrash();

				boolean loaded = FabricLoader.getInstance().isModLoaded(modid);

				if (Require.ABSENT.equals(versionSpec)) {
					if (loaded) {
						logSkip(name, modid, "is present but needs to be absent", context, shouldCrash);
						return false;
					}
					continue;
				}

				if (!loaded) {
					logSkip(name, modid, "is not present", context, shouldCrash);
					return false;
				}

				if (Require.ANY.equals(versionSpec)) {
					continue;
				}

				Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(modid);
				if (container.isPresent()) {
					String version = container.get().getMetadata().getVersion().getFriendlyString();
					try {
						VersionPredicate predicate = VersionPredicate.parse(versionSpec);
						if (!predicate.test(container.get().getMetadata().getVersion())) {
							logSkip(name, modid, "version " + version + " does not satisfy " + versionSpec, context, shouldCrash);
							return false;
						}
					} catch (VersionParsingException e) {
						throw new AnnotationFormatError(new IllegalArgumentException("Invalid version predicate: " + versionSpec, e));
					}
				} else {
					throw new AssertionError();
				}
			}

			Log.info(CAT, "All " + i + " conditions satisfied for " + name);
			return true;
		}
		
	}
	
	private static String getSignature(MethodNode mn) {
		StringBuilder sb = new StringBuilder();
		sb.append(mn.name).append("(");

		Type[] args = Type.getArgumentTypes(mn.desc);
		for (int i = 0; i < args.length; i++) {
			if (i > 0) sb.append(", ");

			Type t = args[i];
			String name;

			if (t.getSort() == Type.ARRAY) {
				// get the element type
				Type elem = t.getElementType();
				name = elem.getClassName().substring(elem.getClassName().lastIndexOf('.') + 1);

				// append brackets for array dimension
				for (int d = 0; d < t.getDimensions(); d++) {
					name += "[]";
				}
			} else {
				// primitive or object
				name = t.getClassName();
				if (t.getSort() == Type.OBJECT) {
					name = name.substring(name.lastIndexOf('.') + 1); // strip package
				}
			}

			sb.append(name);
		}

		sb.append(")");
		return sb.toString();
	}
}
