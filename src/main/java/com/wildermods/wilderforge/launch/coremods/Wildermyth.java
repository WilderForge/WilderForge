package com.wildermods.wilderforge.launch.coremods;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonObject;
import com.wildermods.wilderforge.api.Version;
import com.wildermods.wilderforge.launch.HardCodedCoremod;
import com.wildermods.wilderforge.launch.LoadableCoremod;
import com.wildermods.wilderforge.launch.exception.CoremodFormatError;

public class Wildermyth extends HardCodedCoremod {
	
	private static final File VERSION_FILE = new File("./version.txt");
	
	public Wildermyth() throws IOException {
		construct("wildermyth", getWildermythVersion(new File(".")));
		parseDependencies();
	}
	
	protected void parseDependencies() {
		LoadableCoremod.dependencyGraph.addVertex(this);
	}

	@Override
	protected JsonObject getModJson() throws IOException {
		throw new AssertionError(new UnsupportedOperationException());
	}
	
	private static Version getWildermythVersion(File gameDir) throws IOException {
		File versionFile = new File(gameDir.getAbsolutePath() + "/version.txt");
		if(versionFile.exists()) {
			return new Version(FileUtils.readFileToString(versionFile).split(" ")[0]);
		}
		else {
			throw new CoremodFormatError("No version.txt detected for wildermyth!");
		}
	}

}
