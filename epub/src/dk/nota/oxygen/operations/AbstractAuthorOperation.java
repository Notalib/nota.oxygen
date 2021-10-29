package dk.nota.oxygen.operations;

import java.util.LinkedList;
import java.util.List;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.access.AuthorWorkspaceAccess;
import ro.sync.ecss.extensions.api.content.OffsetInformation;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public abstract class AbstractAuthorOperation implements AuthorOperation {
	
	private AuthorAccess authorAccess;
	
	public void dissolveElement(AuthorElement element)
			throws AuthorOperationException, BadLocationException {
		floatInterval(element.getStartOffset() + 1, element.getEndOffset() - 1);
	}
	
	protected abstract void doOperation() throws AuthorOperationException;

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap arguments)
			throws IllegalArgumentException, AuthorOperationException {
		this.authorAccess = authorAccess;
		parseArguments(arguments);
		doOperation();
	}
	
	public Object evaluateXpathSingle(String xpath, AuthorNode contextNode)
			throws AuthorOperationException {
		return getDocumentController().evaluateXPath(xpath, contextNode, true,
				true, true, true)[0];
	}
	
	public int floatInterval(int start, int end) throws BadLocationException {
		AuthorDocumentFragment content = getDocumentController()
				.createDocumentFragment(start, end);
		getDocumentController().delete(start, end);
		AuthorNode parentNode = getDocumentController().getNodeAtOffset(start);
		if (parentNode.getEndOffset() - parentNode.getStartOffset() == 1) {
			getDocumentController().deleteNode(parentNode);
			getDocumentController().insertFragment(--start, content);
			return start;
		}
		splitNodeAtOffset(parentNode, start, true);
		OffsetInformation offsetInformation = getDocumentController()
				.getContentInformationAtOffset(start);
		switch (offsetInformation.getPositionType()) {
		case OffsetInformation.ON_START_MARKER:
			start--;
			break;
		case OffsetInformation.ON_END_MARKER:
			start++;
		}
		getDocumentController().insertFragment(start++, content);
		return start;
	}
	
	public AuthorNode floatNode(AuthorNode node)
			throws AuthorOperationException, BadLocationException {
		int start = floatInterval(node.getStartOffset(), node.getEndOffset());
		return getDocumentController().getNodeAtOffset(start);
	}
	
	public boolean fragmentIsEmptyElement(AuthorDocumentFragment fragment) {
		if (fragment.getContentNodes().size() > 1) return false;
		AuthorNode fragmentNode = fragment.getContentNodes().get(0);
		if (!(fragmentNode instanceof AuthorElement)) return false;
		return ((AuthorElement)fragmentNode).getContentNodes().isEmpty();
	}

	@Override
	public abstract ArgumentDescriptor[] getArguments();
	
	public AuthorAccess getAuthorAccess() {
		return authorAccess;
	}
	
	public AuthorEditorAccess getAuthorEditor() {
		return getAuthorAccess().getEditorAccess();
	}
	
	@Override
	public abstract String getDescription();
	
	public AuthorDocumentController getDocumentController() {
		return authorAccess.getDocumentController();
	}
	
	public AuthorElement[] getElements(AuthorNode[] nodes) {
		LinkedList<AuthorElement> elements = new LinkedList<AuthorElement>();
		for (AuthorNode node : nodes) {
			if (node instanceof AuthorElement)
				elements.add((AuthorElement)node);
		}
		return elements.toArray(new AuthorElement[elements.size()]);
	}
	
	public AuthorElement[] getElementsByXpath(String xpath)
			throws AuthorOperationException {
		return getElements(getNodesByXpath(xpath, false));
	}
	
	public AuthorElement[] getElementsByXpath(String xpath,
			AuthorNode contextNode) throws AuthorOperationException {
		return getElements(getNodesByXpath(xpath, false, contextNode));
	}
	
	public AuthorElement getFirstElement(AuthorNode[] nodes) {
		for (AuthorNode node : nodes)
			if (node instanceof AuthorElement) return (AuthorElement)node;
		return null;
	}
	
	public AuthorElement getFirstElementByXpath(String xpath)
			throws AuthorOperationException {
		return getFirstElement(getNodesByXpath(xpath, false));
	}
	
	public AuthorElement getFirstElementByXpath(String xpath,
			AuthorNode contextNode) throws AuthorOperationException {
		return getFirstElement(getNodesByXpath(xpath, false, contextNode));
	}
	
	public AuthorNode[] getNodesByXpath(String xpath, boolean includeText)
			throws AuthorOperationException {
		return getDocumentController().findNodesByXPath(xpath, includeText,
				true, true);
	}
	
	public AuthorNode[] getNodesByXpath(String xpath, boolean includeText,
			AuthorNode contextNode) throws AuthorOperationException {
		return getDocumentController().findNodesByXPath(xpath, contextNode,
				includeText, true, true, true);
	}
	
	public List<AuthorNode> getSelectedNodes() throws BadLocationException {
		int start = getSelectionStart();
		int end = getSelectionEnd();
		if (start == end) {
			LinkedList<AuthorNode> nodes = new LinkedList<AuthorNode>();
			nodes.add(getDocumentController().getNodeAtOffset(start));
			return nodes;
		}
		return getDocumentController().getNodesToSelect(start, end);
	}
	
	public int getSelectionEnd() {
		return getAuthorEditor().getSelectionEnd();
	}
	
	public int getSelectionStart() {
		return getAuthorEditor().getSelectionStart();
	}
	
	public AuthorWorkspaceAccess getWorkspace() {
		return getAuthorAccess().getWorkspaceAccess();
	}
	
	public boolean hasBlockContent(AuthorElement element)
			throws AuthorOperationException, BadLocationException {
		for (AuthorNode node : element.getContentNodes()) {
			if (!getDocumentController().inInlineContext(node.getStartOffset()))
				return true;
		}
		return false;
	}
	
	public AuthorNode moveNodeToOffset(AuthorNode node, int offset)
			throws BadLocationException {
		AuthorDocumentFragment nodeFragment = getDocumentController()
				.createDocumentFragment(node, true);
		getDocumentController().deleteNode(node);
		getDocumentController().insertFragment(offset++, nodeFragment);
		return getDocumentController().getNodeAtOffset(offset);
	}
	
	protected abstract void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException;
	
	public boolean selectionIsSiblingsOnly() throws BadLocationException {
		AuthorNode nodeAtStart = getDocumentController().getNodeAtOffset(
				getSelectionStart());
		AuthorNode nodeAtEnd = getDocumentController().getNodeAtOffset(
				getSelectionEnd());
		return nodeAtStart.getParent() == nodeAtEnd.getParent();
	}
	
	public void showErrorMessage(String message) {
		getWorkspace().showErrorMessage(message);
	}
	
	public void showInformationMessage(String message) {
		getWorkspace().showInformationMessage(message);
	}
	
	public void showStatusMessage(String message) {
		getWorkspace().showStatusMessage(message);
	}
	
	public void splitNodeAtOffset(AuthorNode node, int offset,
			boolean discardEmptyRemainders) throws BadLocationException {
		getDocumentController().split(node, offset);
		AuthorNode nodeBefore = getDocumentController().getNodeAtOffset(offset);
		AuthorNode nodeAfter = getDocumentController().getNodeAtOffset(offset + 2);
		// Avoid duplicate id on element after split
		if (nodeAfter instanceof AuthorElement)
			getDocumentController().removeAttribute("id",
					(AuthorElement)nodeAfter);
		if (discardEmptyRemainders) {
			// Delete empty elements last to first
			if (nodeAfter.getEndOffset() - nodeAfter.getStartOffset() == 1)
				getDocumentController().deleteNode(nodeAfter);
			if (nodeBefore.getEndOffset() - nodeBefore.getStartOffset() == 1)
				getDocumentController().deleteNode(nodeBefore);
		}
	}
	
	public void stripElements(AuthorElement element, String... elementNames)
			throws AuthorOperationException, BadLocationException {
		for (AuthorElement childElement : getElementsByXpath("*", element)) {
			boolean matches = false;
			for (String elementName : elementNames) {
				if (childElement.getName().equals(elementName)) {
					matches = true;
					break;
				}
			}
			stripElements(childElement, elementNames);
			if (matches) dissolveElement(childElement);
		}
	}

}
