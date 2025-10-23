package com.wildermods.wilderforge.vanillafixes.standardmods.io;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

public abstract class Pattern implements FileFilter {

	public final String pattern;
	
	public Pattern(String rsyncPattern) {
		this.pattern = rsyncPattern;
	}
	
	@Override
	public String toString() {
		return pattern.toString();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(pattern);
	}
	
	@Override
	public abstract boolean equals(Object o);
	
	public static class RegexBackedPattern extends Pattern {

		protected final java.util.regex.Pattern regexPattern;
		
		public RegexBackedPattern(String rsyncPattern) {
			super(rsyncPattern);
			this.regexPattern = java.util.regex.Pattern.compile(toRegex(rsyncPattern));
		}
		
		public java.util.regex.Pattern getRegex() {
			return regexPattern;
		}
		
		public boolean matches(Path root, Path path) {
			if(!root.isAbsolute()) {
				throw new IllegalArgumentException("Root path must be absolute!");
			}
			
			Path absolutePath = root.resolve(path); //resolve relative paths against root
			
			if(pattern.endsWith("/")) {
				if(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
					if(Files.isSymbolicLink(path)) {
						return false;
					}
				}
				else {
					return false;
				}
			}
			
			Path relative = root.relativize(absolutePath);
			Matcher matcher = regexPattern.matcher(relative.toString());
			return matcher.matches();

		}

		@Override
		public boolean matches(Path path) {
			
			if(!path.isAbsolute()) {
				throw new IllegalArgumentException("Provided path must be absolute!");
			}
			
			return matches(path.getRoot(), path);
		}
		
		@Override
		public String toString() {
			return regexPattern.toString();
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(toString());
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof RegexBackedPattern) {
				return o.toString().equals(toString());
			}
			return false;
		}
		
	}
	
	public static Pattern compile(String rsyncPattern) {
		if(rsyncPattern == null || rsyncPattern.isBlank()) {
			return new Empty(rsyncPattern);
		}
		
		String trimmed = rsyncPattern.trim();
		if(trimmed.startsWith("#") || trimmed.startsWith(";")) {
			return new Comment(rsyncPattern);
		}
		
		return new RegexBackedPattern(rsyncPattern);
	}
	
	/*
	 * Eyes up buddy. There's nothing you want to see below.
	 */
	
	protected static String toRegex(String rsyncPattern) {
		if(rsyncPattern == null || rsyncPattern.isBlank()) return "";
		
		StringBuilder ret = new StringBuilder();
		
		if(rsyncPattern.startsWith("- ")) {
			rsyncPattern = rsyncPattern.substring(2);
		}
		if(rsyncPattern.startsWith("+ ")) {
			throw new PatternSyntaxException("Include operations unsupported (cannot start with + and space)", rsyncPattern, 0);
		}
		
		/**
		 * if the rsync pattern starts with a /, then the matching is anchored to the start of the path string
		 */
		if(rsyncPattern.charAt(0) == '/') {
			ret.append("^");
			System.out.println("AAA");
			rsyncPattern = rsyncPattern.substring(1);
		}
		else {
			ret.append(".*"); //otherwise, we only match against the tail
		}
		
		if(rsyncPattern.length() > 1) {
			/**
			 * if the pattern contains a / anywhere except the last character, or contains a ** anywhere, then the pattern is
			 * matched against the full pathname, including any leading directories within the transfer.
			 */
			if(rsyncPattern.substring(0, rsyncPattern.length() - 1).contains("/") || rsyncPattern.contains("**")) { 
				ret.append("(^|.*/)");  // matches from root or any subdir
			}
		}
		
		ret.append(toRegexImpl(rsyncPattern));
		
		if(rsyncPattern.endsWith("/")) {
			if(!rsyncPattern.endsWith("//")) {
				ret.append("?"); //$ is already added later
			}
		}
		
		ret.append("$"); //ensure the end of string is met so we don't match things we should not.
		
		return ret.toString();
	}
	
	/*
	 * Well, if you insist...
	 * 
	 * Edit this only if you enjoy hours of suffering and regex/rsync/posix induced nightmares.
	 */
	
	protected static String toRegexImpl(String rsyncPattern) {
		// 19 hours were 
		if(rsyncPattern == null || rsyncPattern.isBlank()) return "";
		
		boolean hasWildcards = rsyncPattern.contains("*") || rsyncPattern.contains("?") || rsyncPattern.contains("[");
		
		String glob = rsyncPattern;
		
		StringBuilder ret = new StringBuilder();
		
		for(int i = 0; i < glob.length(); i++) {

			char c = glob.charAt(i);
			
			switch(c) {
				case '*':
					{
						int j = i + 1;
						if(j < glob.length()) {
							char next = glob.charAt(j);
							if(next == '*') {
								ret.append(".*"); //double asterisks in rsync are the same as .* in regex
								i = j;
								continue;
							}
						}
						ret.append("[^/]*"); //a single asterisk matches every character EXCEPT /
						continue;
					}
				case '\\':
					ret.append('\\'); //
					if(hasWildcards) {
						if(i < glob.length()) {
							i++;
							char next = glob.charAt(i);
							switch(next) { //make sure we preserve escape sequences that exist in both regex and rsync
								case '*':
								case '?':
								case '[':
								case '-':
								case ']':
								case '#': //comments
								case ';': //comments
								case '\\':
								case '/': //windows
								ret.append(next);
								continue;
							}
							
							ret.append('\\'); //Okay, we have encountered a backslash that doesn't escape anything. It could mess up the regex. We must make it literal.
							ret.append(next);
							continue;
						}
					}
					else {
						ret.append('\\'); //if no wildcards, then all backslashes are treated as literals per the rsync man page
					}
					continue;
				case '?':
					ret.append('.'); //a question mark in rsync behaves the same as a single . in regex
					continue;
				case '[':
					String sub = rsyncPattern.substring(i);
					if(sub.startsWith("[[:")) {
						int end = rsyncPattern.indexOf(":]]");
						if(end > 0) {
							String posixClass = rsyncPattern.substring(i, end + 3);
							String regex = posixToRegex(posixClass);
							ret.append(regex);

							
							i = end + 2;
							break;
						}
					}
					ret.append(c);
					break;
					
					
				//case ']': a special case, closing brackets must already be escaped in the rsync pattern, otherwise the rsync itself is invalid anyway. No special handling needed.
					
				case '.': //these characters have a special meaning in regex, but not in rsync
				case '^': //so if they appear in rsync we need to escape them when converting to regex
				case '$':
				case '+':
				case '{':
				case '}':
				case '|':
				case '(':
				case ')':
					ret.append('\\');
					ret.append(c);
					continue;
				default:
					ret.append(c);
			}
		}
		
		return ret.toString();
	}
	
	//Huge thanks to: https://www.regular-expressions.info/posixbrackets.html - you have literally saved me tens of hours
	private static final Map<String, String> POSIX_TO_REGEX = Map.ofEntries(
			Map.entry("alnum",  "[a-zA-Z0-9]"),
			Map.entry("alpha",  "[a-zA-Z]"),
			Map.entry("ascii",  "[\\x00-\\x7F]"), // [\x00-\x7F]
			Map.entry("blank",  "[ \\t]"), // [ \t]
			Map.entry("cntrl",  "[\\x00-\\x1F\\x7F]"), // [\x00-\x1F\x7F]
			Map.entry("digit",  "[0-9]"),
			Map.entry("graph",  "[\\x21-\\x7E]"), // [\x21-\x7E]
			Map.entry("lower",  "[a-z]"),
			Map.entry("print",  "[\\x20-\\x7E]"), // [\x20-\x7E]
			Map.entry("punct",  "[!\\\"\\#$%&'()*+,\\-./:;<=>?@\\[\\\\\\]^_`{|}~]"), // [!\"\#$%&'()*+,\-./:;<=>?@\[\\\]^_`{|}~] --WARNING THE WEBSITE IS WRONG, YOU NEED TO ESCAPE THE QUOTATION MARK
			Map.entry("space",  "[ \\t\\r\\n\\v\\f]"), // [ \t\r\n\v\f]
			Map.entry("upper",  "[A-Z]"),
			Map.entry("word",   "[A-Za-z0-9_]"),
			Map.entry("xdigit", "[A-Fa-f0-9]")
		);
	
	private static String posixToRegex(String posixClass) {
		if (posixClass == null || !posixClass.startsWith("[[:") || !posixClass.endsWith(":]]")) {
			throw new IllegalArgumentException("Invalid POSIX class format: " + posixClass);
		}
		

		String key = posixClass.substring(3, posixClass.length() - 3); // extract CLASS
		String replacement = POSIX_TO_REGEX.get(key.toLowerCase());
		
		if(replacement == null) {
			throw new IllegalArgumentException("Unknown POSIX class:" + key);
		}
		
		return replacement;
	}

	//TODO: Fix WilderWorkspace gradle plugin so JUnit can actually be used.
	public static class Test {

		public static void main(String[] args) {
			int passed = 0, failed = 0;

			try {
				// Each call runs one test case
				check("foo/bar.txt", "foo/bar\\.txt");
				check("dir/file.txt", "dir/file\\.txt");
				check("*.txt", "[^/]*\\.txt");
				check("**\\*.java", ".*\\*\\.java");
				check("**/.java", ".*/\\.java");
				check("**/thing.java", ".*/thing\\.java");
				check("**.java", ".*\\.java");
				check("**/*.java", ".*/[^/]*\\.java");
				check("/", "/");
				
				check("\\", "\\\\");
				check("\\f", "\\\\f");
				check("\\foo", "\\\\foo");
				check("\\*", "\\*");

				check("foo\\[bar]", "foo\\[bar]");
				throwz(PatternSyntaxException.class, "foo[bar\\]");
				check("file\\*name.txt", "file\\*name\\.txt");
				check("question\\?mark", "question\\?mark");
				check("back\\\\slash", "back\\\\\\\\slash");
				check("back\\\\slash*", "back\\\\slash[^/]*");

				check("[a-z]", "[a-z]");
				check("data\\[a-zA-Z]\\file", "data\\[a-zA-Z]\\\\file");

				check("[a-z\\]]", "[a-z\\]]");
				check("[\\[]", "[\\[]");
				
				check("[\\[\\-\\]]", "[\\[\\-\\]]");

				check("{foo}", "\\{foo\\}");
				check("(test)", "\\(test\\)");
				check("foo+bar", "foo\\+bar");
				check("foo|bar", "foo\\|bar");
				check("price$100", "price\\$100");
				check("^start", "\\^start");
				check("dot.", "dot\\.");
				
				check("[a-z0-9\\]]", "[a-z0-9\\]]");

				check("foo[ \\]asdf]aaa", "foo[ \\]asdf]aaa");
				check("foo[bar]", "foo[bar]");
				check("foo]bar", "foo]bar");
				throwz(PatternSyntaxException.class, "foo[bar");

				throwz(IllegalArgumentException.class, "[[:unknown:]]");
				
				for(Entry<String, String> entries : POSIX_TO_REGEX.entrySet()) {
					String value = entries.getValue();
					check("[[:" + entries.getKey() + ":]]", value);
				}
				check("[[:alpha:]]aa", POSIX_TO_REGEX.get("alpha") + "aa");

				check("path/", "path/");
				
				check("\\/", "\\\\/");
				check("/\\\\", "/\\\\\\\\");

				check("C:/Users/foo/bar", "C:/Users/foo/bar");
				check("dir/subdir/file", "dir/subdir/file");

				check("**\\*.txt", ".*\\*\\.txt");
				check("???\\?", "...\\?");
				check("/foo?/", "/foo./");
				
				{
					Pattern pattern = compile("**.txt");
					System.out.println(pattern.matches(Path.of("/tmp/bar.txt")));
					System.out.println(pattern.matches(Path.of("/tmp/foo/bar.txt")));
					System.out.println(pattern.matches(Path.of("/tmp/foo/bar.txt/boo")));
				}
				
				Pattern pattern = compile("tmp/");
				if(pattern instanceof RegexBackedPattern) {
					System.out.println(pattern);
				}
				System.out.println(Path.of("tmp/"));
				System.out.println("Exists: " + Files.exists(Path.of("/tmp")));
				
				System.out.println(pattern.matches(Path.of("/tmp")));
				System.out.println(pattern.matches(Path.of("/tmp/")));
				System.out.println(pattern.matches(Path.of("/tmp/bar/")));
				
				System.out.println(pattern.matches(Path.of("/bar")));
				
				passed = passedCount;
				failed = failedCount;

			} catch (Throwable t) {
				System.err.println("Unexpected exception: " + t);
				t.printStackTrace();
			}

			System.out.printf("%n=== SUMMARY ===%nPassed: %d%nFailed: %d%n", passed, failed);
			if (failed > 0) System.exit(1);
		}

		// --- Internal testing methods ---
		private static int passedCount = 0;
		private static int failedCount = 0;

		private static void check(String input, String expected) {
			try {
				String result = toRegexImpl(input);
				if (!expected.equals(result)) {
					fail(input, expected, result);
					return;
				} else {
					
				}
				
				java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(result);
				pass(input, result);
			} catch (Exception e) {
				fail(input, expected, e.toString());
			}
		}
		
		public static void throwz(Class<? extends Throwable> t, String input) {
			try {
				String result = toRegexImpl(input);
				java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(result);
				fail(input, "(" + t.getSimpleName() + ")", "NO EXCEPTION");
			}
			catch(Throwable caught) {
				if(t.isAssignableFrom(caught.getClass())) {
					pass(input, t.getSimpleName() + " OK");
				}
				else {
					fail(input, "(" + t.getSimpleName() + ")", caught.getClass().getSimpleName());
				}
			}
		}

		private static void pass(String input, String result) {
			passedCount++;
			System.out.printf("✅ PASS: %-25s → %s%n", quote(input), quote(result));
		}

		private static void fail(String input, String expected, String actual) {
			failedCount++;
			System.out.printf("❌ FAIL: %-25s → expected %s but got %s%n", quote(input), quote(expected), quote(actual));
		}

		private static String quote(String s) {
			return '"' + s.replace("\n", "\\n ").replace("\r", "\\r ") + '"';
		}
	}
	
	private static abstract class FalsePattern extends Pattern {

		public FalsePattern(String rsyncPattern) {
			super(rsyncPattern);
		}
		
		@Override
		public final boolean matches(Path path) {
			return false;
		}
		
	}
	
	private static class Empty extends FalsePattern  {
		public Empty(String rsyncPattern) {
			super(rsyncPattern);
		}
		
		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof Empty;
		}
	}
	
	private static class Comment extends FalsePattern {

		public Comment(String rsyncPattern) {
			super(rsyncPattern);
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof Comment) {
				return o.toString().equals(toString());
			}
			
			return false;
		}
		
	}
	
}
