package dk.nota.oxygen.operations.html;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;

public class AnchorOperation extends XhtmlEpubAuthorOperation {
	
	public String determineReference(String text) {
		if (text.contains(":")) return text;
		if (text.matches("[^\\s]+@[^\\s]+\\.[^\\s]+"))
			return String.format("mailto:%s", text.replaceAll("\\s+", ""));
		else return String.format("http://%s", text);
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		String selectedText = getAuthorEditor().getSelectedText();
		String reference = determineReference(selectedText.trim());
		String fragment = String.format(
				"<a xmlns='http://www.w3.org/1999/xhtml' href='%s'/>",
				getAuthorAccess().getXMLUtilAccess().escapeAttributeValue(
						reference));
		getDocumentController().surroundInFragment(fragment,
				getSelectionStart(), getSelectionEnd() - 1);
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null; // No arguments to return
	}

	@Override
	public String getDescription() {
		return "Marks up selected word or phrase as an anchor";
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
		// No arguments to parse
	}

}
