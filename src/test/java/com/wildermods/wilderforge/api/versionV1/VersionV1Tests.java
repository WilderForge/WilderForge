package com.wildermods.wilderforge.api.versionV1;

import org.junit.Test;

import com.wildermods.wilderforge.api.exception.InvalidVersionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

public class VersionV1Tests {

	private static final Logger LOGGER = LogManager.getLogger();
	
	@Test
	public void testNoVersions() {
		Version NO_VERSION = Version.NO_VERSION;
		Version nullVersion = Version.getVersion(null);
		Version noVersion = Version.getVersion("");
		Version blankVersion = Version.getVersion(" ");
		Version blankVersion2 = Version.getVersion(" \n");
		
		Assert.assertEquals(NO_VERSION, nullVersion);
		Assert.assertEquals(NO_VERSION, noVersion);
		Assert.assertEquals(NO_VERSION, blankVersion);
		Assert.assertEquals(NO_VERSION, blankVersion2);
	}
	
	@Test
	public void testVersionComparator() {
		Version[] versions = getVersions("1", "1.1", "1.10", "1.2", "1.3", "1.7.10", "1.70.10");
		for(int i = 0, j = 1; j < versions.length; i++, j++) {
			LOGGER.info("Comparing [" + versions[i] + "] & [" + versions[j] + "]");
			Assert.assertTrue(versions[i].compareTo(versions[j]) < 0);
		}
		
		versions = getVersions("1", "1", "1.1", "1.1", "1.10", "1.10", "v2.512 beta", "v2.512 beta");
		for(int i = 0, j = 1; j < versions.length; i++, i++, j++, j++) {
			LOGGER.info("Comparing [" + versions[i] + "] & [" + versions[j] + "]");
			Assert.assertTrue(versions[i].compareTo(versions[j]) == 0);
		}
		Version wildcard = Version.getVersion("*");
		for(int i = 0; i < versions.length; i++) {
			LOGGER.info(versions[i].compareTo(wildcard));
			Assert.assertTrue(versions[i].compareTo(wildcard) == 0);
			Assert.assertTrue(wildcard.compareTo(versions[i]) == 0);
		}
	}
	
	@Test
	public void testInvalidVersionRanges() {
		Assert.assertThrows(InvalidVersionException.class, () -> {new MultiVersionRange("");});
		Assert.assertThrows(InvalidVersionException.class, () -> {new MultiVersionRange("1");});
		Assert.assertThrows(InvalidVersionException.class, () -> {new MultiVersionRange("1.22.3.4");});
		Assert.assertThrows(InvalidVersionException.class, () -> {new MultiVersionRange("1.0,1.1");});
		Assert.assertThrows(InvalidVersionException.class, () -> {new MultiVersionRange("(*]");});
		Assert.assertThrows(InvalidVersionException.class, () -> {new MultiVersionRange("[*)");});
		Assert.assertThrows(InvalidVersionException.class, () -> {new MultiVersionRange("(1.*.2,1.2]");});
		Assert.assertThrows(InvalidVersionException.class, () -> {new MultiVersionRange("[1.*.2]");});
		Assert.assertThrows(InvalidVersionException.class, () -> {new MultiVersionRange("[1.2,1.*.2]");});
		Assert.assertThrows(InvalidVersionException.class, () -> {new MultiVersionRange("[2,1]");});
	}
	
	@Test
	public void testRanges() {
		Assert.assertTrue(new MultiVersionRange("[*]").isWithinRange(Version.getVersion("999999999.99999")));
		Assert.assertTrue(new MultiVersionRange("[1.2.5]").isWithinRange(Version.getVersion("1.2.5")));
		Assert.assertFalse(new MultiVersionRange("[1.2.5]").isWithinRange(Version.getVersion("1.2.4")));
		
		Assert.assertTrue(new MultiVersionRange("[1.3.*]").isWithinRange(new Version("1.3")));
		
		Assert.assertTrue(new MultiVersionRange("[1.2.5,1.2.9]").isWithinRange(Version.getVersion("1.2.8")));
		Assert.assertTrue(new MultiVersionRange("[1.2.5,1.2.9]").isWithinRange(Version.getVersion("1.2.9")));
		Assert.assertFalse(new MultiVersionRange("[1.2.5,1.2.9)").isWithinRange(Version.getVersion("1.2.9")));
		Assert.assertFalse(new MultiVersionRange("[1.2.5,1.2.9]").isWithinRange(Version.getVersion("1.2.91")));
		
		Assert.assertFalse(new MultiVersionRange("(1.2.5,1.2.9)").isWithinRange(Version.getVersion("1.2.5")));
		Assert.assertTrue(new MultiVersionRange("(1.2.5,1.2.9)").isWithinRange(Version.getVersion("1.2.5.1")));
		
		MultiVersionRange splitRange = new MultiVersionRange("[1.2.5,1.2.7][1.3.*][1.4.*]");
		
		Assert.assertFalse(splitRange.isWithinRange(new Version("1.2.4")));
		Assert.assertTrue(splitRange.isWithinRange(new Version("1.2.5")));
		Assert.assertTrue(splitRange.isWithinRange(new Version("1.2.6")));
		Assert.assertTrue(splitRange.isWithinRange(new Version("1.2.7")));
		Assert.assertFalse(splitRange.isWithinRange(new Version("1.2.7.1")));
		Assert.assertTrue(splitRange.isWithinRange(new Version("1.3")));
		Assert.assertTrue(splitRange.isWithinRange(new Version("1.3.9")));
		Assert.assertFalse(splitRange.isWithinRange(new Version("1.39")));
		Assert.assertTrue(splitRange.isWithinRange(new Version("1.4")));
		Assert.assertTrue(splitRange.isWithinRange(new Version("1.4.1")));
		Assert.assertTrue(splitRange.isWithinRange(new Version("1.4.1 beta")));
		Assert.assertFalse(splitRange.isWithinRange(new Version("1.5.0")));
		
		Assert.assertTrue(new MultiVersionRange("[1.0.*,1.1.*][1.0+*,1.1+*]").isWithinRange(new Version("1.1+345")));
	}
	
	private Version[] getVersions(String... versions) {
		Version[] ret = new Version[versions.length];
		for(int i = 0; i < versions.length; i++) {
			ret[i] = Version.getVersion(versions[i]);
		}
		return ret;
	}
	
	private MultiVersionRange[] getRanges(String... ranges) {
		MultiVersionRange[] ret = new MultiVersionRange[ranges.length];
		for(int i = 0; i < ranges.length; i++) {
			ret[i] = new MultiVersionRange(ranges[i]);
		}
		return ret;
	}
	
}
