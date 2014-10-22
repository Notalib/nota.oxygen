package nota.oxygen.common;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 * Abstract base class for {@link AuthorOperation} providing convenience methods
 * @author Ole Holst Andersen (oha@nota.nu)
 */
public abstract class BaseAuthorOperation implements AuthorOperation {
	
	private AuthorAccess authorAccess;
	
	protected abstract void doOperation() 
		throws AuthorOperationException; 
	
	protected abstract void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException;
	
	/**
	 * Finds a AuthorElement by XPath - the search is context based using a given context
	 * @param xpath      The XPath statement
	 * @param context    The AuthorNode used as context
	 * @return The matching AuthorElement or null if no match is found
	 * @throws AuthorOperationException 
	 */
	public AuthorElement findElementByXPath(String xpath, AuthorNode context) throws AuthorOperationException {
		return getFirstElement(getAuthorAccess().getDocumentController().findNodesByXPath(xpath, context, true, true, true, true));
	}
	
	/**
	 * Finds a AuthorElement by XPath - the search is global and finds the first matching element in the document
	 * @param xpath
	 * @return The matching AuthorElement or null if no match is found
	 * @throws AuthorOperationException 
	 */
	public AuthorElement findElementByXPath(String xpath) throws AuthorOperationException {
		return getFirstElement(getAuthorAccess().getDocumentController().findNodesByXPath(xpath,  true, true, true));
	}
	
	private AuthorElement getFirstElement(AuthorNode[] nodes) {
		for (int i=0; i<nodes.length; i++) {
			if (nodes[i] instanceof AuthorElement) return (AuthorElement)nodes[i];
		}
		return null;
	}
	
	
	/**
	 * Gets the start of the current selection
	 * @return The start of the selection
	 */
	public int getSelectionStart()
	{
		int selStart = getAuthorAccess().getEditorAccess().getSelectionStart();
		int selEnd = getAuthorAccess().getEditorAccess().getSelectionEnd();
		if (selStart<selEnd) return selStart;
		return selEnd;
	}
	
	/**
	 * Gets the end of the current selection
	 * @return The end of the current selection
	 */
	public int getSelectionEnd()
	{
		int selStart = getAuthorAccess().getEditorAccess().getSelectionStart();
		int selEnd = getAuthorAccess().getEditorAccess().getSelectionEnd();
		if (selStart<selEnd) return selEnd;
		return selStart;
	}

	@Override
	public final void doOperation(AuthorAccess aa, ArgumentsMap args)
			throws IllegalArgumentException, AuthorOperationException {
		authorAccess = aa;
		parseArguments(args);
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		docCtrl.beginCompoundEdit();
		try {
			doOperation();
		}
		catch (AuthorOperationException e) {
			docCtrl.cancelCompoundEdit();
  			throw e;
		}
		docCtrl.endCompoundEdit();
	}
	
	protected boolean showOkCancelMessage(String title, String message) {
		int answer = getAuthorAccess().getWorkspaceAccess().showConfirmDialog(
				title, 
				message, 
				new String[] {"OK", "Cancel"}, 
				new int[] {0, 1});
		return answer==0;
	}
	
	protected boolean showOkCancelMessage(String message) {
		return showOkCancelMessage(getDescription(), message);
	}
	
	protected int showYesNoCancelMessage(String title, String message, int defaultButton) {
		int index = 0;
		if (defaultButton==0) index = 1;
		if (defaultButton==-1) index = 2;
		return getAuthorAccess().getWorkspaceAccess().showConfirmDialog(
				title, 
				message, 
				new String[] {"Yes", "No", "Cancel"}, 
				new int[] {1, 0, -1}, 
				index);
	}
	
	protected int showYesNoCancelMessage(String message, int defaultButton) {
		return showYesNoCancelMessage(getDescription(), message, defaultButton);
	}
	
	protected void showMessage(String title, String message){
		getAuthorAccess().getWorkspaceAccess().showConfirmDialog(
				title,
				message,
				new String[] {"OK"},
				new int[] {0});
		
	}
	
	protected void showMessage(String message) {
		showMessage(getDescription(), message);
	}
	
	/**
	 * Gets the common parent {@link AuthorNode} of the current selection
	 * @return		The common parent {@link AuthorNode}
	 * @throws AuthorOperationException
	 * 				When the current does not have a common parent {@link AuthorNode} 
	 */
	public AuthorNode getCommonParentNodeOfSelection() throws AuthorOperationException
	{
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		AuthorEditorAccess edtAccess = getAuthorAccess().getEditorAccess();
		try
		{
			AuthorNode parent = docCtrl.getCommonParentNode(
					docCtrl.getAuthorDocumentNode(), edtAccess.getSelectionStart(), edtAccess.getSelectionEnd());
			if (parent.getType()==AuthorNode.NODE_TYPE_ELEMENT) {
				AuthorElement elem = (AuthorElement)parent;
				for (AuthorNode c : elem.getContentNodes()) {
					if (c.getStartOffset()==edtAccess.getSelectionStart() 
							&& c.getEndOffset()==edtAccess.getSelectionEnd()-1) {
						return c;
					}
				}
			}
			return parent;
		}
		catch (BadLocationException e)
		{
			throw new AuthorOperationException("Current selection does not have a common parent node", e);
		}
	}
	
	/**
	 * Get the common parent {@link AuthorElement} of the current selection
	 * @return		The common parent {@link AuthorElement}
	 * @throws AuthorOperationException
	 * 				When the current selection does not have a common parent {@link AuthorElement}
	 */
	public AuthorElement getCommonParentElementOfSelection() throws AuthorOperationException
	{
		return Utils.getAncestorOrSelfElement(getCommonParentNodeOfSelection());
	}
	
	/**
	 * Gets the common parent {@link AuthorElement} with the given QName of the current selection
	 * @param ln	The local-name part of the QName
	 * @param ns	The name-space part of the QName 
	 * 				- if {@code null} the common parent {@link AuthorElement} with the given local-name is returned
	 * @return		The common parent {@link AuthorElement} with the given QName 
	 * 				or {@code null} if no such common parent {@link AuthorElement} exists
	 * @throws AuthorOperationException
	 * 				When the current selection has no common parent {@link AuthorElement} (with any QName)
	 */
	public AuthorElement getNamedCommonParentElementOfSelection(String ln, String ns) throws AuthorOperationException
	{
		AuthorElement curElem = getCommonParentElementOfSelection();
		while (true)
		{
			if (ns==null) {
				if (curElem.getLocalName().equals(ln)) return curElem;
			}
			else {
				if (curElem.getLocalName().equals(ln) && curElem.getNamespace().equals(ns)) return curElem;
			}
			if (!(curElem.getParent() instanceof AuthorElement)) return null;
			curElem = (AuthorElement)curElem.getParent();
		}
	}
	
	
	/**
	 * Gets the element at a given offset
	 * @param offset - the offset
	 * @return The AuthorElement at the given offset - or null if non-existant
	 * @throws BadLocationException
	 */
	public AuthorElement getElementAtOffset(int offset) throws BadLocationException {
		AuthorNode aNode = getAuthorAccess().getDocumentController().getNodeAtOffset(offset);
		while (aNode != null) {
			if (aNode instanceof AuthorElement) {
				return (AuthorElement)aNode;
			}
			aNode = aNode.getParent();
		}
		return null;
		
	}
	
	/**
	 * Gets the element at the caret position
	 * @return The AuthorElement at the caret position or null if no such AuthorElement exists
	 * @throws BadLocationException 
	 */
	public AuthorElement getCurrentElement() throws BadLocationException {
		return getElementAtOffset(getAuthorAccess().getEditorAccess().getBalancedSelectionStart()+1);
	}
	
	
		
	protected AuthorAccess getAuthorAccess() {
		return authorAccess;
	}

	/**
	 * Serializes a {@link AuthorNode} to it's xml representation, including all content nodes
	 * @param input		The {@link AuthorNode} to serialize
	 * @return			The serialized xml representation
	 * @throws AuthorOperationException
	 * 					When the given {@link AuthorNode} unexpectedly could not be serialized
	 */
	public String serialize(AuthorNode input) throws AuthorOperationException {
		return Utils.serialize(getAuthorAccess(), input);
	}

	/**
	 * Serialized a {@link AuthorNode}, optionally including all content nodes
	 * @param input			The {@link AuthorNode} to serialize
	 * @param copyContent	A {@link Boolean} indicating if content nodes should also be serailized
	 * @return				The serialized xml representation
	 * @throws AuthorOperationException
	 * 					When the given {@link AuthorNode} unexpectedly could not be serialized
	 */
	public String serialize(AuthorNode input, boolean copyContent) throws AuthorOperationException {
		return Utils.serialize(getAuthorAccess(), input, copyContent);
	}

	/**
	 * Serializes the children of an AuthorElement to xml
	 * @param elem The parent AuthorElement
	 * @return The xml representing the children 
	 * @throws AuthorOperationException 
	 */
	public String serializeChildren(AuthorElement elem) throws AuthorOperationException {
		return Utils.serializeChildren(getAuthorAccess(), elem);
	}

	/**
	 * Serializes all children of a AuthorElement, including text nodes
	 * @param aElem The parent AuthorElement
	 * @return the serialized child nodes, an empty string of the AuthorElement is empty
	 * @throws AuthorOperationException
	 */ 
	public String serializeContent(AuthorElement aElem) throws AuthorOperationException {
		return Utils.serializeContent(getAuthorAccess(), aElem);
	}

	
		
}
