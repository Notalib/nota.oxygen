package dk.nota.oxygen.operations.html;

import javax.swing.text.BadLocationException;

import dk.nota.oxygen.operations.AbstractAuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.content.TextContentIterator;
import ro.sync.ecss.extensions.api.content.TextContext;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;

public abstract class XhtmlEpubAuthorOperation extends AbstractAuthorOperation {
	
	public boolean editingConcatDocument() {
		return getAuthorEditor().getEditorLocation().toString().endsWith(
				"concat.xhtml");
	}
	
	public boolean hasClass(AuthorElement element) {
		return (element.getAttribute("class") != null);
	}
	
	public boolean hasEpubType(AuthorElement element) {
		return (element.getAttribute("epub:type") != null);
	}
	
	public void normaliseToDepth(AuthorElement element, int depth)
			throws AuthorOperationException, BadLocationException {
		if (element.getName().matches("h\\d")) {
			if (depth > 6) {
				getDocumentController().renameElement(element, "p");
				resetClass(element, "bridgehead");
				resetEpubType(element, "bridgehead");
			} else getDocumentController().renameElement(element,
					"h" + depth);
			return;
		}
		for (AuthorElement childElement : getElementsByXpath(
				"h1|h2|h3|h4|h5|h6|section", element))
			normaliseToDepth(childElement, depth + 1);
		if (element.getName().matches("section")) {
			if (depth > 6 && !hasEpubType(element)) dissolveElement(element);
		}
	}
	
	public void normaliseToDepth(AuthorElement[] elements, int depth)
			throws AuthorOperationException, BadLocationException {
		for (AuthorElement element : elements) normaliseToDepth(element,
				depth);
	}
	
	public void removeSpacedAttrValue(AuthorElement element, String attrName,
			String valueToRemove, boolean discardIfEmpty) {
		if (element.getAttribute(attrName) == null) return;
		String oldString = element.getAttribute(attrName).getValue();
		getDocumentController().removeAttribute(attrName, element);
		String newString = "";
		for (String value : oldString.split(" "))
			if (!value.equals(valueToRemove)) newString += value;
		if (!discardIfEmpty || newString.length() > 0)
			getDocumentController().setAttribute(attrName, new AttrValue(
					newString), element);
	}
	
	public void resetClass(AuthorElement element, String classValue) {
		getDocumentController().setAttribute("class", new AttrValue(classValue),
				element);
	}
	
	public void resetEpubType(AuthorElement element, String epubTypeValue) {
		getDocumentController().setAttribute("epub:type",
				new AttrValue(epubTypeValue), element);
	}
	
	public void stripSpaceFromElement(AuthorElement element,
			boolean stripLeading, boolean stripTrailing)
			throws AuthorOperationException, BadLocationException {
		int elementStart = element.getStartOffset();
		int elementEnd = element.getEndOffset();
		// Iterate on text contant across node limits
		TextContentIterator contentIterator = getDocumentController()
				.getTextContentIterator(elementStart, elementEnd);
		int characterOffset, nonWhitespaceStart, nonWhitespaceEnd;
		characterOffset = nonWhitespaceStart = elementStart;
		nonWhitespaceEnd = elementEnd;
		boolean foundNonwhitespace = false;
		while (contentIterator.hasNext()) {
			TextContext context = contentIterator.next();
			characterOffset = context.getTextStartOffset();
			for (char c: ((String)context.getText()).toCharArray()) {
				if (!foundNonwhitespace && !Character.isWhitespace(c)) {
					nonWhitespaceStart = nonWhitespaceEnd = characterOffset;
					foundNonwhitespace = true;
				} else if (!Character.isWhitespace(c))
					nonWhitespaceEnd = characterOffset;
				characterOffset++;
			}
		}
		// Delete intervals in reverse order to avoid offsets shifting
		if (stripTrailing) getDocumentController().delete(++nonWhitespaceEnd,
				element.getEndOffset());
		if (stripLeading) getDocumentController().delete(element
				.getStartOffset(), --nonWhitespaceStart);
	}

}
