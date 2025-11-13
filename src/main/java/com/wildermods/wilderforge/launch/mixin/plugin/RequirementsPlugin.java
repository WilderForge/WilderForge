package com.wildermods.wilderforge.launch.mixin.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.wildermods.wilderforge.api.mixins.v1.Require;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;

public class RequirementsPlugin implements IMixinConfigPlugin {

	private final LogCategory CAT = LogCategory.createCustom("WilderForge", "RequirementsPlugin");
	
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
	    List<RequireData> requirements = getRequirements(mixinClassName);
	    if (requirements.isEmpty()) {
	    	Log.info(CAT, "No mod requirements detected for mixin " + mixinClassName);
	    	return true;
	    }

	    int i = 0;
	    for (RequireData req : requirements) {
	    	i++;
	        String modid = req.modid;
	        String versionSpec = req.version;

	        boolean loaded = FabricLoader.getInstance().isModLoaded(modid);

	        if (Require.ABSENT.equals(versionSpec)) {
	            if (loaded) {
	                logSkip(mixinClassName, modid, "present but needs to be absent");
	                return false;
	            }
	            continue;
	        }

	        if (!loaded) {
	            logSkip(mixinClassName, modid, "is not present");
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
	                    logSkip(mixinClassName, modid, "version " + version + " does not satisfy " + versionSpec);
	                    return false;
	                }
	            } catch (VersionParsingException e) {
	                System.err.println("[RequirementsPlugin] Invalid version predicate: " + versionSpec);
	                e.printStackTrace();
	                return false;
	            }
	        }
	        else {
	        	throw new AssertionError();
	        }
	    }
	    Log.info(CAT, "All " + i + " conditions satisfied for " + mixinClassName);
	    return true;
	}
	
    private void logSkip(String mixin, String modid, String reason) {
    	Log.warn(CAT, "Skipping " + mixin + " because " + modid + " " + reason);
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
	

	private static class RequireData {
	    final String modid;
	    final String version;

	    RequireData(String modid, String version) {
	        this.modid = modid;
	        this.version = version;
	    }
	}
	
    private List<RequireData> getRequirements(String mixinClassName) {
        String resource = mixinClassName.replace('.', '/') + ".class";
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                return Collections.emptyList();
            }

            ClassReader reader = new ClassReader(in);
            RequireCollector collector = new RequireCollector();
            reader.accept(collector, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return collector.requires;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

	private static class RequireCollector extends ClassVisitor {
	    final List<RequireData> requires = new ArrayList<>();

	    public RequireCollector() {
	        super(Opcodes.ASM9);
	    }

	    @Override
	    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
	        if (desc.endsWith("/Require;")) {
	            return new RequireVisitor(requires);
	        } else if (desc.endsWith("/Requires;")) {
	            return new RequiresVisitor(requires);
	        }
	        return super.visitAnnotation(desc, visible);
	    }
	}

	private static class RequireVisitor extends AnnotationVisitor {
	    final List<RequireData> out;

	    RequireVisitor(List<RequireData> out) {
	        super(Opcodes.ASM9);
	        this.out = out;
	    }

	    @Override
	    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
	        if (descriptor.endsWith("/Mod;")) {
	            return new ModVisitor(out);
	        }
	        return super.visitAnnotation(name, descriptor);
	    }
	}

	private static class RequiresVisitor extends AnnotationVisitor {
	    final List<RequireData> out;

	    RequiresVisitor(List<RequireData> out) {
	        super(Opcodes.ASM9);
	        this.out = out;
	    }

	    @Override
	    public AnnotationVisitor visitArray(String name) {
	        if ("value".equals(name)) {
	            return new AnnotationVisitor(Opcodes.ASM9) {
	                @Override
	                public AnnotationVisitor visitAnnotation(String name, String descriptor) {
	                    if (descriptor.endsWith("/Require;")) {
	                        return new RequireVisitor(out);
	                    }
	                    return super.visitAnnotation(name, descriptor);
	                }
	            };
	        }
	        return super.visitArray(name);
	    }
	}

	private static class ModVisitor extends AnnotationVisitor {
	    private String modid;
	    private String version;
	    private final List<RequireData> out;

	    ModVisitor(List<RequireData> out) {
	        super(Opcodes.ASM9);
	        this.out = out;
	    }

	    @Override
	    public void visit(String name, Object value) {
	        if ("modid".equals(name)) {
	            this.modid = (String) value;
	        } else if ("version".equals(name)) {
	            this.version = (String) value;
	        }
	    }

	    @Override
	    public void visitEnd() {
	        out.add(new RequireData(modid, version));
	    }
	}

}
