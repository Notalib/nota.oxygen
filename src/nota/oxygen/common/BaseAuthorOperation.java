package nota.oxygen.common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.BadLocationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;

import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
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
	 * Gets a descendant {@link Element} by id
	 * @param root	The root of the descendant tree 
	 * @param id	The id
	 * @return		The first descendant {@link Element} with the given id or {@code null} if no such element exists
	 */
	public static Element getDescentantElementById(Element root, String id) {
		for (int i=0; i<root.getChildNodes().getLength(); i++) {
			if (root.getChildNodes().item(i) instanceof Element) {
				Element child = (Element)root.getChildNodes().item(i);
				if (id.equals(child.getAttribute("id"))) return child;
				Element tmp = getDescentantElementById(child, id);
				if (tmp!=null) return tmp;
			}
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
			docCtrl.endCompoundEdit();
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
	
	protected void showMessage(String message) {
		
		getAuthorAccess().getWorkspaceAccess().showConfirmDialog(
				getDescription(),
				message,
				new String[] {"OK"},
				new int[] {0});
	}
	
	/**
	 * Creates an {@link XPath}
	 * @return The created {@link XPath}
	 */
	public static XPath getXPath() {
		return XPathFactory.newInstance().newXPath();
	}
	
	/**
	 * Create an {@link XPath} initialized with the given namespace/prefix pair 
	 * @param prefix		The prefix
	 * @param namespace		The namespace
	 * @return				The created {@link XPath}
	 */
	public static XPath getXPath(String prefix, String namespace)
	{
		Map<String,String> pNSMap = new HashMap<String,String>();
		if (prefix!=null && namespace!=null) pNSMap.put(prefix, namespace);
		return getXPath(pNSMap);
	}
	
	
	/**
	 * Creates a {@link XPath} inistalized with the given prefix/namespace {@link Map}
	 * @param prefixNSMap	The given {@link Map}
	 * @return				The created {@link XPath}
	 */
	public static XPath getXPath(Map<String,String> prefixNSMap)
	{
		XPath xpath = getXPath();
		xpath.setNamespaceContext(new ManualNamespaceContext(prefixNSMap));
		return xpath;
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
		return getAncestorOrSelfElement(getCommonParentNodeOfSelection());
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
	 * Gets the nearest ancestor or self {@link AuthorElement} of a given {@link AuthorNode}
	 * @param node	The given {@link AuthorNode}
	 * @return		The nearest ancestor or self {@link AuthorElement}
	 */
	public static AuthorElement getAncestorOrSelfElement(AuthorNode node)
	{
		while (node!=null)
		{
			if (node instanceof AuthorElement) return (AuthorElement)node;
			node = node.getParent();
		}
		return null;
	}
	
	private static DOMImplementationLS getDOMImplementation() throws AuthorOperationException
	{
		try
		{
			return (DOMImplementationLS)DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
		}
		catch (Exception e)
		{
			throw new AuthorOperationException("Unexpected exception occured while instantiating DOM implementation: "+e.getMessage(), e);
		}
		
	}
	
	/**
	 * Serializes a {@link AuthorNode} to it's xml representation, including all content nodes
	 * @param input		The {@link AuthorNode} to serialize
	 * @return			The serialized xml representation
	 * @throws AuthorOperationException
	 * 					When the given {@link AuthorNode} unexpectedly could not be serialized
	 */
	public String serialize(AuthorNode input) throws AuthorOperationException {
		return serialize(input,	true);
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
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		try {
			return docCtrl.serializeFragmentToXML(docCtrl.createDocumentFragment(input, copyContent));
		}
		catch (BadLocationException e) {
			throw new AuthorOperationException(
					"Unexpected BadLocationException: "+e.getMessage(),
					e);
		}
	}
	
	/**
	 * Recursively removes xmlns:xml attributes on an AuthorDocumentFragment and all it's decendants
	 * @param input		The AuthorDocumentFragment
	 */
	public void removeXmlnsXmlAttribute(AuthorDocumentFragment input) {
		List<AuthorNode> nodes = input.getContentNodes();
		for (int i=0; i<nodes.size(); i++) {
			removeXmlnsXmlAttribute(nodes.get(i));
		}
	}
	
	/**
	 * Recursively removes xmlns:xml attributes on an AuthorNode and all it's decendants
	 * @param input		The Node
	 */
	public void removeXmlnsXmlAttribute(AuthorNode input) {
		if (input instanceof AuthorElement) {
			AuthorElement elem = (AuthorElement)input;
			elem.removeAttribute("xmlns:xml");
			List<AuthorNode> children = elem.getContentNodes();
			for (int i=0; i<children.size(); i++) {
				removeXmlnsXmlAttribute(children.get(i));
			}
		}
	}
	
	/**
	 * Serializes a {@link Node} to it's xml representation
	 * @param input		The {@link Node} to serailize
	 * @return			The serialized xml
	 * @throws AuthorOperationException
	 * 					When a {@link LSSerializer} for some reason could not be created
	 */
	public static String serialize(Node input) throws AuthorOperationException
	{
		LSSerializer writer = getDOMImplementation().createLSSerializer();
		writer.getDomConfig().setParameter("xml-declaration", false);
		return writer.writeToString(input);
	}
	
	/**
	 * De-serializes a xml element {@link String} representation to a {@link Element} 
	 * @param xml	The xml to de-serailize
	 * @return		The de-serailized {@link Element}
	 * @throws AuthorOperationException
	 * 				When the given xml could not be de-serialized
	 */
	public static Element deserializeElement(String xml) throws AuthorOperationException
	{
		try
		{
			InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			DOMImplementationLS impl = getDOMImplementation();
			LSParser builder = impl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
			LSInput input = impl.createLSInput();
			input.setByteStream(is);
			input.setEncoding("UTF-8");
			Document doc =  builder.parse(input);
			return doc.getDocumentElement();
		}
		catch (AuthorOperationException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new AuthorOperationException(
					"Unexpected exception occured while deserializing node from xml:\n"+xml+"\n"+e.getMessage(), e);
		}
	}

	protected AuthorAccess getAuthorAccess() {
		return authorAccess;
	}
	
	/**
	 * Gets the last child AuthorElement of a parent AuthorElement
	 * @param parent The parent AuthorElement from which to get the last child
	 * @return The last child or null if no last child exists
	 */
	public static AuthorElement getLastChild(AuthorElement parent){
		List<AuthorNode> children = parent.getContentNodes();
		for (int i = children.size()-1; i>=0; i--)	{
			if (children.get(i) instanceof AuthorElement) return (AuthorElement)children.get(i);
		}
		return null;
	}
	
	/**
	 * Gets the previous sibling of an AuthorElement
	 * @param elem The AuthorElement for which to get the previous sibling
	 * @return The previous sibling of elem - null if no previous sibling exists
	 */
	public static AuthorElement getPreviousSibling(AuthorElement elem) {
		if (elem.getParent()!=null) {
			AuthorElement parent = (AuthorElement)elem.getParent();
			List<AuthorNode> siblings = parent.getContentNodes();
			int thisIndex = siblings.indexOf(elem);
			for (int i=thisIndex-1; i>=0; i--) {
				if (siblings.get(i) instanceof AuthorElement) return (AuthorElement)siblings.get(i);
			}
		}
		return null; 
	}
	
	/**
	 * Gets the next sibling of an AuthorElement
	 * @param elem The AuthorElement for which to get the next sibling
	 * @return The next sibling of elem - null if no previous sibling exists
	 */
	public static AuthorElement getNextSibling(AuthorElement elem) {
		if (elem.getParent()!=null) {
			AuthorElement parent = (AuthorElement)elem.getParent();
			List<AuthorNode> siblings = parent.getContentNodes();
			int thisIndex = siblings.indexOf(elem);
			for (int i=thisIndex+1; i<siblings.size(); i++) {
				if (siblings.get(i) instanceof AuthorElement) return (AuthorElement)siblings.get(i);
			}
		}
		return null;
	}
	
	
	/**
	 * Serializes the children of an AuthorElement to xml
	 * @param elem The parent AuthorElement
	 * @return The xml representing the children 
	 * @throws AuthorOperationException 
	 */
	public String serializeChildren(AuthorElement elem) throws AuthorOperationException {
		String res = "";
		List<AuthorNode> children = elem.getContentNodes();
		for (int i=0; i<children.size(); i++) {
			res += serialize(children.get(i));
		}
		return res;
	}

	
		
}
