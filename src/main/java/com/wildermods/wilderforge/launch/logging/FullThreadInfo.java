package com.wildermods.wilderforge.launch.logging;

import java.lang.management.ThreadInfo;

public class FullThreadInfo {

	private final ThreadInfo parent;
	
	private FullThreadInfo(ThreadInfo parent) {
		this.parent = parent;
	}

	public static FullThreadInfo from(ThreadInfo parent) {
		return new FullThreadInfo(parent);
	}
	
	public ThreadInfo getParent() {
		return parent;
	}
	
	public String toString() {
		// Get the original string output from ThreadInfo
		String original = parent.toString();

		// Get the complete stack trace
		StackTraceElement[] fullStackTrace = parent.getStackTrace();

		// Check if the output contains the ellipsis indicating truncation
		if (original.contains("\t...")) {
			// Remove the existing truncated part
			int truncateIndex = original.indexOf("\t...");
			String beforeTruncation = original.substring(0, truncateIndex); // Keep everything before the truncation

			// Append the remaining stack trace elements that were cut off
			StringBuilder modifiedOutput = new StringBuilder(beforeTruncation);
			for (int i = 8; i < fullStackTrace.length; i++) { // Start appending from frame 8
				modifiedOutput.append("\tat ").append(fullStackTrace[i]).append("\n");
			}
			
			modifiedOutput.append('\n');
			
			// Append the rest of the original string after the ellipsis
			int afterIndex = truncateIndex + 4; // 4 for the length of "\t..."
			modifiedOutput.append(original.substring(afterIndex)); // Append the rest (locked synchronizers, etc.)
			
			return modifiedOutput.toString();
		}

		return original; 
	}
	
}
