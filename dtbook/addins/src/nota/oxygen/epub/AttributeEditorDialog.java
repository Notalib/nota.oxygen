package nota.oxygen.epub;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class AttributeEditorDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private String[] possibleValues = new String[0];
	private boolean allowMultipleValues;
	
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
			if (allowMultipleValues) {
				selectedValues.addAll(Arrays.asList(getValue().split("\\s+")));
			}
			else {
				selectedValues.add(getValue());
			}
		}
		for (int i = 0; i < selectedValues.size(); i++) {
			if (!values.contains(selectedValues.get(i))) {
				values.add(selectedValues.get(i));
			}
		}
		toggleButtonPanel.invalidate();
		toggleButtonPanel.removeAll();
		for (int i = 0; i < values.size(); i++) {
			JToggleButton toggleButton = allowMultipleValues ? new JCheckBox(values.get(i)) : new JRadioButton(values.get(i));
			toggleButton.setSelected(selectedValues.contains(values.get(i)));
			toggleButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (e.getSource() instanceof JRadioButton) {
						JRadioButton button = (JRadioButton)e.getSource();
						for (Component c : button.getParent().getComponents()) {
							if (c == button) {
								continue;
							}
							else if (c instanceof JRadioButton) {
								((JRadioButton)c).setSelected(false);
							}
						}
					}
					setValue(getValueFromCheckBoxes());
				}
			});
			toggleButtonPanel.add(toggleButton);
		}
		toggleButtonPanel.validate();
		toggleButtonPanel.repaint();
	}
	
	private String getValueFromCheckBoxes() {
		String result = "";
		for (Component c : toggleButtonPanel.getComponents()) {
			if (c instanceof JToggleButton) {
				JToggleButton box = (JToggleButton)c;
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
	private JPanel toggleButtonPanel;
	
	public static String showAttributeDialog(
			Object owner, String[] valueList, String initialValue, 
			boolean allowEdit, boolean allowMultipleValues, String title) {
		AttributeEditorDialog dialog = new AttributeEditorDialog(owner);
		dialog.setTitle(title);
		dialog.possibleValues = valueList;
		dialog.valueTextField.setEditable(allowEdit);
		dialog.allowMultipleValues = allowMultipleValues;
		dialog.setValue(initialValue);
		dialog.setModal(true);
		dialog.setVisible(true);
		if (dialog.okPressed) {
			return dialog.getValue();
		}
		return null;
	}
	
	private boolean okPressed = false;
	
	
	private static Window getAncestralWindow(Object obj) {
		if (obj instanceof Window) {
			return (Window)obj;
		}
		else if (obj instanceof Component) {
			return getAncestralWindow(((Component)obj).getParent());
		}
		return null;
	}

	private static JScrollPane getAncestralJScrollPane(Object obj) {
		if (obj instanceof JScrollPane) {
			return (JScrollPane)obj;
		}
		else if (obj instanceof Component) {
			return getAncestralJScrollPane(((Component)obj).getParent());
		}
		return null;
	}
	
	private Rectangle getOptimalBounds(Object parent) {
		Rectangle result = new Rectangle(getPreferredSize()); 
		Rectangle parentBounds = null;
		JScrollPane parentScrollPane = getAncestralJScrollPane(parent);
		if (parentScrollPane != null) {
			parentBounds = parentScrollPane.getBounds();
			parentBounds.setLocation(parentScrollPane.getLocationOnScreen());
			if (parentBounds.width>=getPreferredSize().width && parentBounds.height>=getPreferredSize().height) {
				result.x = parentBounds.x;
				result.y = parentBounds.y;
			}
			else if (getOwner() != null) {
				parentBounds = getOwner().getBounds();
				if (parentBounds.width>=getPreferredSize().width && parentBounds.height>=getPreferredSize().height) {
					result.x = parentBounds.x + (parentBounds.width-getPreferredSize().width)/2;
					result.y = parentBounds.y + (parentBounds.height-getPreferredSize().height)/2;
				}				
			}
		}
		return result;
	}

	/**
	 * Create the dialog.
	 */
	private AttributeEditorDialog(Object owner) {
		super(getAncestralWindow(owner));
		setPreferredSize(new Dimension(300, 400));
		setBounds(getOptimalBounds(owner));
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
				toggleButtonPanel = new JPanel();
				scrollPane.setViewportView(toggleButtonPanel);
				toggleButtonPanel.setLayout(new BoxLayout(toggleButtonPanel, BoxLayout.PAGE_AXIS));
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
