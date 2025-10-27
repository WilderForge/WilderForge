package com.wildermods.wilderforge.api.mixins.v1;

public class Descriptor {
	//Primitive Types
	public static final String BYTE = "B";
	public static final String CHAR = "C";
	public static final String DOUBLE = "D";
	public static final String FLOAT = "F";
	public static final String INT = "I";
	public static final String LONG = "J";
	public static final String SHORT = "S";
	public static final String BOOLEAN = "Z";
	public static final String VOID = "V";
	public static final String ARRAY_OF = "[";
	
	//common types
	public static final String STRING = "Ljava/lang/String;";
	public static final String OBJECT = "Ljava/lang/Object;";
	public static final String FILE = "Ljava/io/File;";
	public static final String PATH = "Ljava/nio/file/Path;";
	public static final String GDX_ARRAY = "Lcom/badlogic/gdx/utils/Array;";
	public static final String GDX_FILE = "Lcom/badlogic/gdx/files/FileHandle;";
	
	//fields
	public static final String ENUM_VALUES = "$VALUES";
}
