package com.wildermods.wilderforge.api.utils.io;

public final class ByteUtils {

	private ByteUtils() {throw new AssertionError();}
	
	public static String humanReadableByteCount(long bytes) {
		return humanReadableByteCount(bytes, false);
	}

	public static String humanReadableByteCount(long bytes, boolean decimal) {
		int unit = decimal ? 1000 : 1024;
		if (bytes < unit) return bytes + " bytes";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = new String[] {"K", "M", "G", "T", "P", "E"}[exp - 1];
		pre = decimal ? pre : pre + "i";
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
}
