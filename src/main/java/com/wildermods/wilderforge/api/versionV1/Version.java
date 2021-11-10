package com.wildermods.wilderforge.api.versionV1;

import com.wildermods.wilderforge.api.exception.InvalidVersionException;

public class Version implements Versioned {

	static final String SPLITTER = "\\.";
	private final char[] version;
	private int wildcard;
	
	public static final NoVersion NO_VERSION = new NoVersion();
	public static final Missing MISSING = new Missing();
	
	public Version(String version) throws InvalidVersionException {
		this.version = version.trim().toCharArray();
		wildcard = version.indexOf('*');
		if(!(wildcard == -1 || wildcard == version.length() - 1)) {
			throw new InvalidVersionException("Wildcard must be the last character in a version string (char " + wildcard + "): " + version);
		}
	}
	
	public static Version getVersion(String version) {
		if(version == null || version.isBlank()) {
			return NO_VERSION;
		}
		return new Version(version);
	}
	
	@Override
	public int compareTo(Versioned o) {
		return compareTo(o.getVersion());
	}
	
	/**
	 * Returns 0 if this version matches the target version.
	 */
	public int compareTo(Version o) {
		int min = Math.min(version.length, o.version.length);
		int i = 0;
		for(; i < min; i++) {
			if(version[i] == '*' || o.version[i] == '*') {
				return 0;
			}
			int compare = version[i] - o.version[i];
			if(compare != 0) {
				return compare;
			}
		}
		//special handling for if the longer version matches the comparing version but ends with .*
		//EX: So 1.3 will return 0 when compared to 1.3.*
		if(version.length != o.version.length) { 
			String longerVersion;
			if(version.length > min) {
				longerVersion = new String(version).substring(min);
			}
			else{
				longerVersion = new String(o.version).substring(min);
			}
			if(longerVersion.replace(".", "").replace("*", "").isBlank()) {
				return 0;
			}
		}
		return version.length - o.version.length;
	}
	
	public Version getVersion() {
		return this;
	}
	
	@Override
	public String toString() {
		return new String(version);
	}
	
	public static class NoVersion extends Version {
		
		private NoVersion() {
			super("");
		}
		
		@Override
		public boolean equals(Object o) {
			if(o == null || o instanceof NoVersion) {
				return true;
			}
			if(o instanceof Version) {
				return ((Version) o).version.length == 0;
			}
			return false;
		}
		
		@Override
		public int compareTo(Versioned o) {
			if(o instanceof Version) {
				if(o instanceof NoVersion) {
					return 0;
				}
				return -1;
			}
			throw new IllegalArgumentException(o.getClass().getCanonicalName());
		}
		
		public String toString() {
			return "No version";
		}
		
	}
	
	public static class Missing extends NoVersion {
		public String toString() {
			return "Missing";
		}
	}

}