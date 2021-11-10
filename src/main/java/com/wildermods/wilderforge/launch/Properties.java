package com.wildermods.wilderforge.launch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOError;

@InternalOnly
class Properties {

	private static final File PROPERTIES = new File(new File(".").getAbsoluteFile().getParentFile().getParentFile().getAbsolutePath() + "/gradle.properties");
	
	public static String getProperty(String name) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(PROPERTIES));
			String property = reader.lines().filter((p) -> p.replace(" ", "").startsWith(name + "=")).findFirst().get();
			property = property.replace(" ", "");
			property = property.substring(property.indexOf('=') + 1, property.length());
			return property;
			
		} catch (FileNotFoundException e) {
			throw new IOError(e);
		}
	}
	
}
