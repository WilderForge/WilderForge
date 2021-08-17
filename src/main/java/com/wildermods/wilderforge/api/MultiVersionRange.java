package com.wildermods.wilderforge.api;

import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wildermods.wilderforge.api.exception.InvalidVersionException;

public class MultiVersionRange {
	
	//Regex: [\[\(].*?[\)\]]
	private static final Pattern pattern = Pattern.compile("[\\[\\(].*?[\\)\\]]");
	
	private final String versionRange;
	private final HashSet<VersionRange> children = new HashSet<VersionRange>();

	public MultiVersionRange(String versionRange) throws InvalidVersionException {
		this.versionRange = versionRange;
		Matcher matcher = pattern.matcher(versionRange);
		if(matcher.matches()) {
			Iterator<MatchResult> iterator = matcher.reset().results().iterator();
			while(iterator.hasNext()) {
				children.add(new VersionRange(iterator.next().group()));
			}
		}
		else {
			throw new InvalidVersionException(versionRange + " does not match regex " + pattern);
		}
	}

	public boolean isWithinRange(Version version) {
		for(VersionRange versionRange : children) {
			if(versionRange.isWithinRange(version)) {
				return true;
			}
		}
		return false;
	}
	
	public static final class VersionRange {
		
		private Version start;
		private Version end;
		
		boolean startInclusive;
		boolean endInclusive;
		
		private VersionRange(String versionRange) {
			char startChar = versionRange.charAt(0);
			char endChar = versionRange.charAt(versionRange.length() - 1);
			if(startChar == '(') {
				startInclusive = false;
			}
			else if (startChar == '[') {
				startInclusive = true;
			}
			else {
				throw new InvalidVersionException("Version range must start with inclusive token '[' or exclusive token '('");
			}
			
			if(endChar == ')') {
				endInclusive = false;
			}
			else if (endChar == ']') {
				endInclusive = true;
			}
			else {
				throw new InvalidVersionException("Version range must end with inclusive token ']' or exclusive token ')' " + versionRange);
			}
			
			int splitIndex = versionRange.indexOf(',');
			if(splitIndex != -1) {
				start = new Version(versionRange.substring(1, splitIndex - 1));
				end = new Version(versionRange.substring(splitIndex + 1, versionRange.length() - 2));
				int startWildCard = start.toString().indexOf('*');
				int endWildCard = end.toString().indexOf('*');
				if(startWildCard != -1) {
					if(startWildCard != start.toString().length()) {
						throw new InvalidVersionException("Wildcard can only be at the end of a version: " + start);
					}
				}
				if(endWildCard != -1) {
					if(endWildCard != end.toString().length()) {
						throw new InvalidVersionException("Wildcard can only be at the end of a version: " + end);
					}
				}
			}
			else {
				if(startChar != '[' || endChar != ']') {
					throw new InvalidVersionException("Version range must include a non-zero range of versions! (One version is compatible? Use only inclusive tokens!)");
				}
				String versionString = versionRange.substring(1, versionRange.length() - 1);
				start = new Version(versionString);
				end = new Version(versionString);
			}
			
			if(start.compareTo(end) > 0) {
				throw new InvalidVersionException("Start version must be less than or equal to the end version:" + start + ">" + end);
			}
		}
		
		public boolean isWithinRange(Version version) {
			int beginCompare = getMin(start).compareTo(version);
			int endCompare = getMax(end).compareTo(version);
			if(beginCompare == 0 && startInclusive || endCompare == 0 && endInclusive) {
				return true;
			}
			else if(beginCompare < 0 && endCompare > 0) {
				return true;
			}
			return false;
		}
		
		private Version getMin(Version version) {
			return new Version(version.toString().replace('*', '0'));
		}
		
		private Version getMax(Version version) {
			return new Version(version.toString().replace('*', '9'));
		}
		
	}
	
}
