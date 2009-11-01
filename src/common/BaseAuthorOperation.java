package common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
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
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public abstract class BaseAuthorOperation implements AuthorOperation {
	
	private AuthorAccess authorAccess;
	
	protected abstract void doOperation() 
		throws AuthorOperationException; 
	
	protected abstract void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException;

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
			try {
				docCtrl.getUndoManager().undo();
			} catch (Exception e2) {
				// Do nothing
			}
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
	
	public static XPath getXPath(String prefix, String namespace)
	{
		Map<String,String> pNSMap = new HashMap<String,String>();
		if (prefix!=null && namespace!=null) pNSMap.put(prefix, namespace);
		return getXPath(pNSMap);
	}
	
	public static XPath getXPath(Map<String,String> prefixNSMap)
	{
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new ManualNamespaceContext(prefixNSMap));
		return xpath;
	}
	
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
	
	public AuthorElement getCommonParentElementOfSelection() throws AuthorOperationException
	{
		return getAncestorOrSelfElement(getCommonParentNodeOfSelection());
	}
	
	public AuthorElement getNamedCommonParentElementOfSelection(String ln, String ns) throws AuthorOperationException
	{
		AuthorElement curElem = getCommonParentElementOfSelection();
		while (true)
		{
			if (ns==null) {
				if (curElem.getLocalName()==ln) return curElem;
			}
			else {
				if (curElem.getLocalName()==ln && curElem.getNamespace()==ns) return curElem;
			}
			if (!(curElem.getParent() instanceof AuthorElement)) return null;
			curElem = (AuthorElement)curElem.getParent();
		}
	}
	
	
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
	
	public String serialize(AuthorNode input) throws AuthorOperationException {
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		try {
			return docCtrl.serializeFragmentToXML(docCtrl.createDocumentFragment(input, true));
		}
		catch (BadLocationException e) {
			throw new AuthorOperationException(
					"Unexpected BadLocationException: "+e.getMessage(),
					e);
		}
	}
	
	public static String serialize(Node input) throws AuthorOperationException
	{
		LSSerializer writer = getDOMImplementation().createLSSerializer();
		writer.getDomConfig().setParameter("xml-declaration", false);
		return writer.writeToString(input);
	}
	
	public static Element deserialize(String xml) throws AuthorOperationException
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
	
	
		
}
