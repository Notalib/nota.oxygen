package nota.oxygen.epub;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.BoxLayout;

import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.InputMethodListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

@SuppressWarnings("serial")
public class AttributeEditorDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private String[] possibleValues = new String[0];
	
	private String value;
	
	public void setValue(String val) {
		if (value == null) {
			if (val == null) {
				return;
			}
		}
		else if (value.equals(val)) {
			return;
		}
		value = val;
		valueTextField.setText(value);
		updateCheckBoxes();
	}
	
	private void updateCheckBoxes() {
		List<String> values = new ArrayList<String>(Arrays.asList(possibleValues));
		List<String> selectedValues = new ArrayList<String>(); 
		if (!("".equals(getValue().trim()))) {
			selectedValues.addAll(Arrays.asList(getValue().split("\\s+")));
		}
		for (int i = 0; i < selectedValues.size(); i++) {
			if (!values.contains(selectedValues.get(i))) {
				values.add(selectedValues.get(i));
			}
		}
		checkBoxPanel.invalidate();
		checkBoxPanel.removeAll();
		for (int i = 0; i < values.size(); i++) {
			JCheckBox box = new JCheckBox(values.get(i));
			box.setSelected(selectedValues.contains(values.get(i)));
			box.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setValue(getValueFromCheckBoxes());
				}
			});
			checkBoxPanel.add(box);
		}
		checkBoxPanel.validate();
		checkBoxPanel.repaint();
	}
	
	private String getValueFromCheckBoxes() {
		String result = "";
		for (Component c : checkBoxPanel.getComponents()) {
			if (c instanceof JCheckBox) {
				JCheckBox box = (JCheckBox)c;
				if (box.isSelected()) {
					result += box.getText() + " ";
				}
			}
		}
		return result.trim();
	}
	
	public String getValue() {
		return valueTextField.getText();
	}

	private JTextField valueTextField;
	private JPanel checkBoxPanel;
	
	public static String showAttributeDialog(
			Window owner, String[] valueList, String initialValue, 
			boolean allowEdit, boolean allowMultipleValues, String title) {
		AttributeEditorDialog dialog = new AttributeEditorDialog(owner);
		dialog.setTitle(title);
		dialog.possibleValues = valueList;
		dialog.setValue(initialValue);
		dialog.setModal(true);
		dialog.setVisible(true);
		if (dialog.okPressed) {
			return dialog.getValue();
		}
		return null;
	}
	
	private boolean okPressed = false;

	/**
	 * Create the dialog.
	 */
	private AttributeEditorDialog(Window owner) {
		super(owner);
		setBounds(100, 100, 300, 400);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		{
			valueTextField = new JTextField();
			valueTextField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent event) {
					setValue(valueTextField.getText());
				}
			});
			valueTextField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setValue(valueTextField.getText());
				}
			});
			valueTextField.setMaximumSize(new Dimension(2147483647, 20));
			contentPanel.add(valueTextField);
			valueTextField.setColumns(10);
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane);
			{
				checkBoxPanel = new JPanel();
				scrollPane.setViewportView(checkBoxPanel);
				checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.PAGE_AXIS));
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						okPressed = true;
						setVisible(false);
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}
