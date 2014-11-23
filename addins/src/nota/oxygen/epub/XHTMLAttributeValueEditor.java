package nota.oxygen.epub;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

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
	
	private String selectFromComboBox(List<String> values, String initialValue, boolean allowEdit, Component parentComponent) {
		if (!values.contains(initialValue)) {
			values.add(initialValue);
		}
		JComboBox<String> comboBox = new JComboBox<String>(values.toArray(new String[0]));
		comboBox.setEditable(allowEdit);
		comboBox.setSelectedItem(initialValue);
		if (JOptionPane.showConfirmDialog(parentComponent, comboBox, "Enter value", JOptionPane.OK_CANCEL_OPTION)==0) {
			return comboBox.getSelectedItem().toString();
		}
		return initialValue;
	}
	
	private String selectFromListBox(List<String> values, String initialValue, Component parentComponent) {
		String[] initialValues = initialValue.split("\\s");
		for (String iv : initialValues) {
			if (!values.contains(iv)) {
				values.add(iv);
			}
		}
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
		Dictionary<String, JCheckBox> valueCheckBoxes = new Hashtable<String, JCheckBox>();
		for (String v : values.toArray(new String[0])) {
			JCheckBox box = new JCheckBox(v);
			valueCheckBoxes.put(v, box);
			checkBoxPanel.add(box);
		}
		for (String iv : initialValues) {
			valueCheckBoxes.get(iv).setSelected(true);
		}
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(checkBoxPanel);
//		JOptionPane pane = new JOptionPane(scrollPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
//		JDialog dialog = pane.createDialog(parentComponent, "Select value");
//		dialog.setVisible(true);
//		if (pane.getValue()==0) {
		if (JOptionPane.showConfirmDialog(parentComponent, scrollPane, "Select value", JOptionPane.OK_CANCEL_OPTION)==0) {
			String result = "";
			for (int i = 0; i < values.size(); i++) {
				if (valueCheckBoxes.get(values.get(i)).isSelected()) {
					result += values.get(i) + " ";
				}
			}
//			for (int i : listBox.getSelectedIndices()) {
//				result += values.get(i) + " ";
//			}
			return result.trim();
		}
		return initialValue;
	}

	@Override
	public String getAttributeValue(EditedAttribute edtAttr, Object parent) {
		Component parentComponent = null;
		if (parent instanceof Component) {
			parentComponent = (Component)parent;
		}
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
			if (allowMultipleValues) {
				if (allowEdit) {
					
				}
				else {
					value = selectFromListBox(possibleValues, value, parentComponent);
				}				
			}
			else {
				value = selectFromComboBox(possibleValues, value, allowEdit, parentComponent);
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
