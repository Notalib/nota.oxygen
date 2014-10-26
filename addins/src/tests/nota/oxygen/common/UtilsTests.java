package tests.nota.oxygen.common;

import static org.junit.Assert.assertEquals;
import nota.oxygen.common.Utils;

import org.junit.Test;

public class UtilsTests {

	@Test
	public void testRelativizeURI() {
		String actual;
		actual = Utils.relativizeURI("http://www.nota.nu/beta/", "http://www.nota.nu/beta/gamma.html");
		assertEquals(actual, "gamma.html");
		actual = Utils.relativizeURI("zip:file:/C:/Users/oha/Documents/Xml/Oxygen/epub/sample.epub!/EPUB/", "zip:file:/C:/Users/oha/Documents/Xml/Oxygen/epub/sample.epub!/EPUB/dk-nota-sample.xhtml");
		assertEquals(actual, "dk-nota-sample.xhtml");
		actual = Utils.relativizeURI("zip:file:/C:/Users/oha/Documents/Xml/Oxygen/epub/sample.epub!/EPUB/", "zip:file:/C:/Users/oha/Documents/Xml/Oxygen/epub/sample.epub!/dk-nota-sample.xhtml");
		assertEquals(actual, "/dk-nota-sample.xhtml");
		actual = Utils.relativizeURI("zip:file:/C:/Users/oha/Documents/Xml/Oxygen/epub/sample.epub!/EPUB/package.opf", "zip:file:/C:/Users/oha/Documents/Xml/Oxygen/epub/sample.epub!/EPUB/dk-nota-sample.xhtml");
		assertEquals(actual, "dk-nota-sample.xhtml");
	}

}
