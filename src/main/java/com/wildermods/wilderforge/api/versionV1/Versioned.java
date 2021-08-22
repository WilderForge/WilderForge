package com.wildermods.wilderforge.api.versionV1;

public interface Versioned extends Comparable<Version> {

	public Version getVersion();
	
	public default int compareTo(Versioned versioned) {
		return getVersion().compareTo(versioned.getVersion());
	}
	
}
