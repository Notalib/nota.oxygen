package tests.nota.oxygen.epub;

import static org.junit.Assert.*;
import nota.oxygen.epub.AttributeEditorDialog;

import org.junit.Test;

public class AttributeEditorDialogTests {
	@Test
	public void testShowAttributeDialog()  {
		String value = AttributeEditorDialog.showAttributeDialog(null, new String[] {"a",  "b",  "c",  "d"}, "a b", true, true, "Enter attribute value and press OK");
		assertNotNull(value);
		value = AttributeEditorDialog.showAttributeDialog(null, new String[] {"a",  "b",  "c",  "d"}, "a b", true, true, "Enter attribute value and press Cancel");
		assertNull(value);
		value = AttributeEditorDialog.showAttributeDialog(null, new String[] {"a",  "b",  "c",  "d"}, "a b", true, false, "Enter attribute value and press OK");
		assertNotNull(value);
		value = AttributeEditorDialog.showAttributeDialog(null, new String[] {"a",  "b",  "c",  "d"}, "a b", false, true, "Enter attribute value and press OK");
		assertNotNull(value);
		value = AttributeEditorDialog.showAttributeDialog(null, new String[] {"a",  "b",  "c",  "d"}, "a b", false, false, "Enter attribute value and press OK");
		assertNotNull(value);
	}
}
