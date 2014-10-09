package nota.oxygen.common.table;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.w3c.dom.Element;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import nota.oxygen.common.BaseAuthorOperation;

/**
 * @author OHA
 *
 */
public class InsertTableOperation extends BaseAuthorOperation {


	private static String ARG_TABLE_FRAGMENT = "table fragment";
	private String tableFragment;

	private static String ARG_TABLE_HEADER_FRAGMENT = "table header fragment";
	private String tableHeaderFragment;

	private static String ARG_TABLE_BODY_FRAGMENT = "table body fragment";
	private String tableBodyFragment;

	private static String ARG_TABLE_FOOTER_FRAGMENT = "table footer fragment";
	private String tableFooterFragment;

	private static String ARG_TABLE_ROW_FRAGMENT = "table row fragment";
	private String tableRowFragment;

	private static String ARG_TABLE_CELL_FRAGMENT = "table cell fragment";
	private String tableCellFragment;

	private static String ARG_TABLE_HEADER_CELL_FRAGMENT = "table header cell fragment";
	private String tableHeaderCellFragment;
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[]{
				new ArgumentDescriptor(ARG_TABLE_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Table fragment"),
				new ArgumentDescriptor(ARG_TABLE_HEADER_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Table header fragment"),
				new ArgumentDescriptor(ARG_TABLE_FOOTER_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Table footer fragment"),
				new ArgumentDescriptor(ARG_TABLE_BODY_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Table body fragment"),
				new ArgumentDescriptor(ARG_TABLE_ROW_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Table row fragment"),
				new ArgumentDescriptor(ARG_TABLE_CELL_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Table call fragment"),
				new ArgumentDescriptor(ARG_TABLE_HEADER_CELL_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Table header cell fragment")
				};
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		tableFragment = (String)args.getArgumentValue(ARG_TABLE_FRAGMENT);
		tableHeaderFragment = (String)args.getArgumentValue(ARG_TABLE_HEADER_FRAGMENT);
		tableFooterFragment = (String)args.getArgumentValue(ARG_TABLE_FOOTER_FRAGMENT);
		tableBodyFragment = (String)args.getArgumentValue(ARG_TABLE_BODY_FRAGMENT);
		tableRowFragment = (String)args.getArgumentValue(ARG_TABLE_ROW_FRAGMENT);
		tableCellFragment = (String)args.getArgumentValue(ARG_TABLE_CELL_FRAGMENT);
		tableHeaderCellFragment = (String)args.getArgumentValue(ARG_TABLE_HEADER_CELL_FRAGMENT);

	}

	@Override
	public String getDescription() {
		return "Inserts a table, prompting the user for number of rows and columns (and header rows/footer rows)";
	}
	
	private void insertRows(int rows, int cols, String cellFragment, Element rowContainer) throws AuthorOperationException {
		for (int r = 0; r < rows; r++) {
			Element row = deserializeElement(tableRowFragment);
			for (int c = 0; c < cols; c++) {
				Element cell = deserializeElement(tableCellFragment);
				row.appendChild(row.getOwnerDocument().importNode(cell,  true));
			}
			rowContainer.appendChild(rowContainer.getOwnerDocument().importNode(row, true));
		}
			
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		SpinnerNumberModel colsModel = new SpinnerNumberModel(3, 1, 100, 1);
		SpinnerNumberModel rowsModel = new SpinnerNumberModel(2, 1, 100, 1);
		SpinnerNumberModel headerRowsModel = new SpinnerNumberModel(1, 0, 100, 1);
		SpinnerNumberModel footerRowsModel = new SpinnerNumberModel(0, 0, 100, 1);
		JPanel panel = new JPanel(new GridLayout(4,  2));
		panel.add(new JLabel(getAuthorAccess().getAuthorResourceBundle().getMessage("columns")));
		panel.add(new JSpinner(colsModel));
		panel.add(new JLabel(getAuthorAccess().getAuthorResourceBundle().getMessage("rows")));
		panel.add(new JSpinner(rowsModel));
		panel.add(new JLabel(getAuthorAccess().getAuthorResourceBundle().getMessage("header.rows")));
		panel.add(new JSpinner(headerRowsModel));
		panel.add(new JLabel(getAuthorAccess().getAuthorResourceBundle().getMessage("footer.rows")));
		panel.add(new JSpinner(footerRowsModel));
		int res = JOptionPane.showConfirmDialog(
				(Component)getAuthorAccess().getWorkspaceAccess().getParentFrame(), 
				panel, 
				getAuthorAccess().getAuthorResourceBundle().getMessage("insert.table"), 
				JOptionPane.OK_CANCEL_OPTION);
		if (res != JOptionPane.OK_OPTION) return;
		getAuthorAccess().getDocumentController().beginCompoundEdit();
		int cols = colsModel.getNumber().intValue();
		int rows = rowsModel.getNumber().intValue();
		int headerRows = headerRowsModel.getNumber().intValue();
		int footerRows = footerRowsModel.getNumber().intValue();
		Element table = deserializeElement(tableFragment);
		if (headerRows > 0)
		{
			Element header = deserializeElement(tableHeaderFragment);
			insertRows(headerRows,  cols, tableHeaderCellFragment, header);
			table.appendChild(table.getOwnerDocument().importNode(header, true));
		}
		Element body = deserializeElement(tableBodyFragment);
		insertRows(rows,  cols, tableCellFragment, body);
		table.appendChild(table.getOwnerDocument().importNode(body, true));
		if (footerRows > 0)
		{
			Element footer = deserializeElement(tableFooterFragment);
			insertRows(footerRows,  cols, tableHeaderCellFragment, footer);
			table.appendChild(table.getOwnerDocument().importNode(footer, true));
		}
		getAuthorAccess().getDocumentController().insertXMLFragment(serialize(table), getAuthorAccess().getEditorAccess().getCaretOffset());
		getAuthorAccess().getDocumentController().endCompoundEdit(); 
	}

}
