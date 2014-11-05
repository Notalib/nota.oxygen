package tests.nota.oxygen.common;

import static org.junit.Assert.*;
import nota.oxygen.common.Utils;

import org.junit.Test;

public class UtilsTests {

	@Test
	public void testGetZipUrl() {
		String url = "zip:file:/c:/test.zip!/abe.test";
		String zip = Utils.getZipUrl(url);
		assertTrue("file:/c:/test.zip".equals(zip));
	}

}
