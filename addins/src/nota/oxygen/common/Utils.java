package nota.oxygen.common;

import java.awt.Component;
import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;
import javax.swing.text.BadLocationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorDocument;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPageBase;

public class Utils {

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
		XPath xpath = getXPath();
		xpath.setNamespaceContext(new ManualNamespaceContext(prefix, namespace));
		return xpath;
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
	
	public static DOMImplementationLS getDOMImplementationLS() throws AuthorOperationException
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
	 * De-serializes a xml document {@link String} representation to a {@link Document} 
	 * @param xml       The xml to de-serailize
	 * @param docUri    The base uri of the de-serializes document
	 * @return          The de-serailized {@link Document}
	 * @throws AuthorOperationException
	 *                  When the given xml could not be de-serialized
	 */
	public static Document deserializeDocument(String xml, String docUri) throws AuthorOperationException {
		try
		{
			DOMImplementationLS impl = getDOMImplementationLS();
			LSParser builder = impl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
			LSInput input = impl.createLSInput();
			input.setStringData(xml);
			Document res = builder.parse(input);
			if (docUri != null) {
				res.setDocumentURI(docUri);
			}
			return res;
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

	/**
	 * De-serializes a xml element {@link String} representation to a {@link Element} 
	 * @param xml	The xml to de-serailize
	 * @return		The de-serailized {@link Element}
	 * @throws AuthorOperationException
	 * 				When the given xml could not be de-serialized
	 */
	public static Element deserializeElement(String xml) throws AuthorOperationException {
		return deserializeDocument(xml, null).getDocumentElement();
	}

	/**
	 * Gets the last child AuthorElement of a parent AuthorElement
	 * @param parent The parent AuthorElement from which to get the last child
	 * @return The last child or null if no last child exists
	 */
	public static AuthorElement getLastChild(AuthorElement parent) {
		List<AuthorNode> children = parent.getContentNodes();
		for (int i = children.size()-1; i>=0; i--)	{
			if (children.get(i) instanceof AuthorElement) return (AuthorElement)children.get(i);
		}
		return null;
	}

	/**
	 * Gets the previous sibling of an AuthorElement
	 * @param elem The AuthorElement for which to get the previous sibling
	 * @return The previous sibling AuthorElement of elem - null if no previous sibling exists
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
	 * @return The next sibling AuthorElement of elem - null if no next sibling exists
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
	 * Recursively removes xmlns:xml attributes on an AuthorDocumentFragment and all it's decendants
	 * @param input		The AuthorDocumentFragment
	 */
	public static void removeXmlnsXmlAttribute(AuthorDocumentFragment input) {
		List<AuthorNode> nodes = input.getContentNodes();
		for (int i=0; i<nodes.size(); i++) {
			removeXmlnsXmlAttribute(nodes.get(i));
		}
	}

	/**
	 * Recursively removes xmlns:xml attributes on an AuthorNode and all it's descendants
	 * @param input		The Node
	 */
	public static void removeXmlnsXmlAttribute(AuthorNode input) {
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
		LSSerializer writer = getDOMImplementationLS().createLSSerializer();
		writer.getDomConfig().setParameter("xml-declaration", false);
		return writer.writeToString(input);
	}

	/**
	 * Serialized a {@link AuthorNode}, optionally including all content nodes
	 * @param authorAccess  The AuthorAccess to use for serializing
	 * @param input			The {@link AuthorNode} to serialize
	 * @param copyContent	A {@link Boolean} indicating if content nodes should also be serailized
	 * @return				The serialized xml representation
	 * @throws AuthorOperationException
	 * 					When the given {@link AuthorNode} unexpectedly could not be serialized
	 */
	public static String serialize(AuthorAccess authorAccess, AuthorNode input, boolean copyContent) throws AuthorOperationException {
		AuthorDocumentController docCtrl = authorAccess.getDocumentController();
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
	 * Serializes a {@link AuthorNode} to it's xml representation, including all content nodes
	 * @param authorAccess  The AuthorAccess to use for serializing
	 * @param input		The {@link AuthorNode} to serialize
	 * @return			The serialized xml representation
	 * @throws AuthorOperationException
	 * 					When the given {@link AuthorNode} unexpectedly could not be serialized
	 */
	public static String serialize(AuthorAccess authorAccess, AuthorNode input) throws AuthorOperationException {
		return serialize(authorAccess, input,	true);
	}

	/**
	 * Serializes the children of an AuthorElement to xml
	 * @param authorAccess  The AuthorAccess to use for serializing
	 * @param elem The parent AuthorElement
	 * @return The xml representing the children 
	 * @throws AuthorOperationException 
	 */
	public static String serializeChildren(AuthorAccess authorAccess, AuthorElement elem) throws AuthorOperationException {
		String res = "";
		List<AuthorNode> children = elem.getContentNodes();
		for (int i=0; i<children.size(); i++) {
			res += serialize(authorAccess, children.get(i));
		}
		return res;
	}

	/**
	 * Serializes all children of a AuthorElement, including text nodes
	 * @param authorAccess  The AuthorAccess to use for serializing
	 * @param aElem The parent AuthorElement
	 * @return the serialized child nodes, an empty string of the AuthorElement is empty
	 * @throws AuthorOperationException
	 */ 
	public static String serializeContent(AuthorAccess authorAccess, AuthorElement aElem) throws AuthorOperationException {
		Element elem = deserializeElement(serialize(authorAccess, aElem));
		String res = "";
		NodeList children = elem.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			res += serialize(children.item(i));
		}
		return res;
	}

	public static void bringFocusToDocumentTab(AuthorAccess authorAccess) {
		bringFocusToDocumentTab(authorAccess.getEditorAccess());
	}
	
	public static void bringFocusToDocumentTab(WSAuthorEditorPageBase editor) {
		Object compObject = editor.getAuthorComponent();
		TabbedPaneChildComponentPair pair = getTabbedPaneAncestor((compObject instanceof Component ? (Component)compObject : null));
		if (pair != null) {
			pair.TabbedPane.setSelectedComponent(pair.ChildComponent);
		}
	}
	
	private static class TabbedPaneChildComponentPair {
		public JTabbedPane TabbedPane;
		public Component ChildComponent;
	}
	
	private static TabbedPaneChildComponentPair getTabbedPaneAncestor(Component component) {
		if (component == null) return null;
		if (component.getParent() == null) return null;
		if (component.getParent() instanceof JTabbedPane) {
			TabbedPaneChildComponentPair res = new TabbedPaneChildComponentPair();
			res.ChildComponent = component;
			res.TabbedPane = (JTabbedPane)component.getParent();
			return res;
		}
		return getTabbedPaneAncestor(component.getParent());
	}

	public static void replaceRoot(Document doc, AuthorAccess authorAccess) 
			throws AuthorOperationException {
		String xml = serialize(doc);
		AuthorDocumentController ctrl = authorAccess.getDocumentController();
		ctrl.beginCompoundEdit();
		try {
			ctrl.replaceRoot(ctrl.createNewDocumentFragmentInContext(xml, 0));
			AuthorDocument authorDoc = ctrl.getAuthorDocumentNode();
			ctrl.getUniqueAttributesProcessor().assignUniqueIDs(authorDoc.getStartOffset(), authorDoc.getEndOffset(), false);
			
		}
		catch (Exception e) {
			ctrl.cancelCompoundEdit();
			throw e;
		}
		ctrl.endCompoundEdit();
	}
	
	public static AuthorElement getDecendantAuthorElementById(AuthorElement parent, String id) {
		if (id == null) return null;
		if (parent == null) return null;
		for (AuthorNode node : parent.getContentNodes()) {
			if (node instanceof AuthorElement) {
				AuthorElement elem = (AuthorElement)node;
				AttrValue val = elem.getAttribute("id");
				if (val != null) {
					if (id.equals(val.getValue())) return elem;
				}
				elem = getDecendantAuthorElementById(elem,  id);
				if (elem != null) return elem;
			}
		}
		return null;
	}

}
