package common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.*;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public abstract class BaseAuthorOperation implements AuthorOperation {
	
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
	
	public static AuthorNode getCommonParentNodeOfSelection(AuthorAccess access) throws AuthorOperationException
	{
		AuthorDocumentController docCtrl = access.getDocumentController();
		AuthorEditorAccess edtAccess = access.getEditorAccess();
		try
		{
			return docCtrl.getCommonParentNode(
					docCtrl.getAuthorDocumentNode(), edtAccess.getSelectionStart(), edtAccess.getSelectionEnd());
		}
		catch (BadLocationException e)
		{
			throw new AuthorOperationException("Current select does not have a common parent node", e);
		}
	}
	
	public static AuthorElement getCommonParentElementOfSelection(AuthorAccess access) throws AuthorOperationException
	{
		return getAncestorOrSelfElement(getCommonParentNodeOfSelection(access));
	}
	
	public static AuthorElement getNamedCommonParentElementOfSelection(AuthorAccess access, String ln, String ns) throws AuthorOperationException
	{
		AuthorElement curElem = getCommonParentElementOfSelection(access);
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
		
}
