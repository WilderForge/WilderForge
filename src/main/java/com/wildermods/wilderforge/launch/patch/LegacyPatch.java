package com.wildermods.wilderforge.launch.patch;

import java.util.function.Consumer;
import java.util.function.Function;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.fabricmc.loader.impl.game.patch.GamePatch;
import net.fabricmc.loader.impl.launch.FabricLauncher;

public class LegacyPatch extends GamePatch {

	@Override
	public void process(FabricLauncher launcher, Function<String, ClassReader> classSource,
			Consumer<ClassNode> classEmitter) {
		String entrypoint = launcher.getEntrypoint();
		String gameEntryPoint;
		ClassNode mainClass = readClass(classSource.apply(entrypoint));
		
		if(mainClass == null) {
			throw new LinkageError ("Could not load main class " + entrypoint + "!");
		}
		
		MethodNode mainMethod = findMethod(mainClass, (method) -> method.name.equals("main") && method.desc.equals("([Ljava/lang/String;)V") && isPublicStatic(method.access));
		
		if(mainMethod == null) {
			throw new NoSuchMethodError("Could not find main method in " + entrypoint +  "!");
		}
		
		System.out.println("entrypoint is " + entrypoint);
		System.out.println("Main method is " + mainMethod.name + mainMethod.desc);
		
		gameEntryPoint = entrypoint;
		
		ClassNode gameClass;
		
		gameClass = readClass(classSource.apply(gameEntryPoint));
		if(gameClass == null) throw new Error("Could not load game class " + gameEntryPoint + "!");
		
		if (gameClass != mainClass) {
			classEmitter.accept(gameClass);
		} else {
			classEmitter.accept(mainClass);
		}
		
	}
	
	boolean getName(MethodInsnNode method, String name) {
		if(method.name.equals(name)) {
			return true;
		}
		else {
			System.out.println(method.name + method.desc + " is not the main method");
			return false;
		}
	}

}
