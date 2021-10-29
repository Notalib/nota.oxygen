package dk.nota.oxygen.operations.html;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;

public class BridgeheadOperation extends XhtmlEpubAuthorOperation {

	@Override
	protected void doOperation() throws AuthorOperationException {
		AuthorElement paragraph = getFirstElementByXpath(
				"ancestor-or-self::p");
		if (!hasClass(paragraph)) resetClass(paragraph, "bridgehead");
		else getDocumentController().removeAttribute("class", paragraph);;
		if (!hasEpubType(paragraph)) resetEpubType(paragraph, "bridgehead");
		else getDocumentController().removeAttribute("epub:type", paragraph);
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null; // No arguments to return
	}

	@Override
	public String getDescription() {
		return "Creates a bridgehead at the current position";
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
		// No arguments to parse
	}

}
