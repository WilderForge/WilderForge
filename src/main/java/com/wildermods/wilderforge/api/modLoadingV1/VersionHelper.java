package com.wildermods.wilderforge.api.modLoadingV1;

import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

public class VersionHelper {

	public static final Version parseVersion(String version) {
		try {
			return Version.parse(version);
		} catch (VersionParsingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static final int compareVersionIncludingBuild(Version version1, Version version2) {
		int ret = version1.compareTo(version2);
		if(ret != 0) {
			return ret;
		}
		if(version1 instanceof SemanticVersion && version2 instanceof SemanticVersion) {
			String version1Build = ((SemanticVersion)version1).getBuildKey().orElse(null);
			String version2Build = ((SemanticVersion)version2).getBuildKey().orElse(null);
			if(version1Build != null) {
				if(version2Build != null) {
					return parseVersion(version1Build).compareTo(parseVersion(version2Build));
				}
				return 1;
			}
			else if (version2Build != null) {
				return -1;
			}
		}
		return ret;
	}
	
}
