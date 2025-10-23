package com.wildermods.wilderforge.vanillafixes.standardmods.io;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

import com.badlogic.gdx.files.FileHandle;

@FunctionalInterface
public interface FileFilter extends PathMatcher, java.io.FileFilter {
	
	public default boolean matches(String file) {
		return matches(Path.of(file));
	}

	public default boolean matches(FileHandle file) {
		return matches(Path.of(file.path()));
	}
	
	public default boolean matches(File file) {
		return matches(file.toPath());
	}
	
	@Override
	public default boolean accept(File file) {
		return matches(file);
	}
	
	@Override
	public boolean matches(Path path);
	
}
