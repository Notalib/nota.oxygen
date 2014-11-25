package nota.oxygen.epub;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ro.sync.ecss.extensions.api.CustomAttributeValueEditor;
import ro.sync.ecss.extensions.api.EditedAttribute;

public class XHTMLAttributeValueEditor extends CustomAttributeValueEditor {
	private Document attributeValueListsDocument;
	
	public XHTMLAttributeValueEditor(Document attrsValListDoc) {
		attributeValueListsDocument = attrsValListDoc;
	}

	@Override
	public String getDescription() {
		return "Configurable attribute editor, using an xml document to store list of attribute values";
	}

	@Override
	public String getAttributeValue(EditedAttribute edtAttr, Object parent) {
		String value = edtAttr.getValue();
		Element attrList = getAttributeList(edtAttr);
		if (attrList != null) {
			boolean allowMultipleValues = "true".equals(attrList.getAttribute("allowMultipleValues"));
			boolean allowEdit = "true".equals(attrList.getAttribute("allowEdit"));
			List<String> possibleValues = new ArrayList<String>();
			NodeList valueNodes = attrList.getElementsByTagName("value");
			for(int i = 0; i < valueNodes.getLength(); i++) {
				String v = valueNodes.item(i).getTextContent();
				if (!possibleValues.contains(v)) possibleValues.add(v);
			}
			String newValue = AttributeEditorDialog.showAttributeDialog(
					parent, 
					possibleValues.toArray(new String[0]), value, 
					allowEdit, allowMultipleValues, 
					"Edit Attribute "+edtAttr.getAttributeQName());
			if (newValue != null) {
				return newValue;
			}
		}
		return value;
	}
	
	private static boolean nameMatches(String name, String nameSpec) {
		if (nameSpec != null && name != null) {
			if (nameSpec.equals("*")) {
				return true;
			}
			for (String namePart : nameSpec.split("\\s")) {
				if (namePart.equals(name)) return true;
			}
		}
		return false;
	}
	
	private Element getAttributeList(EditedAttribute edtAttr) {
		if (attributeValueListsDocument!=null) {
			NodeList lists = attributeValueListsDocument.getDocumentElement().getElementsByTagName("attributeValueList");
			for (int i = 0; i < lists.getLength(); i++) {
				Element attributeList = (Element)lists.item(i);
				if (
						nameMatches(edtAttr.getParentElementQName(), attributeList.getAttribute("parentElementNames"))
						&& nameMatches(edtAttr.getAttributeQName(), attributeList.getAttribute("attributeNames"))) {
					return attributeList;
				}
			}
		}
		return null;
	}

	@Override
	public boolean shouldHandleAttribute(EditedAttribute edtAttr) {
		return getAttributeList(edtAttr)!=null;
	}

}
