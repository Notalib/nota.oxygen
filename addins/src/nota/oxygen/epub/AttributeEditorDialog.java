package nota.oxygen.epub;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
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

@SuppressWarnings("serial")
public class AttributeEditorDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField valueTextField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			AttributeEditorDialog dialog = new AttributeEditorDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String[] selectedValues = new String[0];
	
	public void setSelectedValues(String[] val) {
		if (val == null && selectedValues != null) {
			selectedValues = null;
		}
		else if (!selectedValues.equals(val)) {
			
		}
	}
	
	public String[] getSelectedValues() {
		return selectedValues;
	}

	/**
	 * Create the dialog.
	 */
	public AttributeEditorDialog() {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		{
			valueTextField = new JTextField();
			valueTextField.addInputMethodListener(new InputMethodListener() {
				public void caretPositionChanged(InputMethodEvent arg0) {
				}
				public void inputMethodTextChanged(InputMethodEvent arg0) {
					
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
				JPanel checkBoxPanel = new JPanel();
				scrollPane.setViewportView(checkBoxPanel);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}
