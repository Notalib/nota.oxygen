package nota.oxygen.epub;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorWorkspaceAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.editor.WSEditor;
import nota.oxygen.common.BaseAuthorOperation;
import nota.oxygen.common.Utils;

public class SplitEpubOperation extends BaseAuthorOperation
{

	private AuthorAccess opfAccess;
	private String xhtmlFileName = "concatenated.xhtml";
	private String epubFilePath = "";

	private String _ID_Prefix;
	private NodeList _MetaNodes;
	private String _SourceTitle;

	private Document _SourceDoc;
	private AuthorAccess xhtmlAccess;

	private int _DocNumber;
	
	private boolean _stop;
	
	private Map<String, Document> _DocList;
	
	private Map<String, String> _Ids;
	
	private String _ErrMsg;

	@Override
	public ArgumentDescriptor[] getArguments()
	{
		return new ArgumentDescriptor[] {};
	}

	@Override
	public String getDescription()
	{
		return "Splits epub file";
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void doOperation() throws AuthorOperationException
	{
		URL opfUrl = EpubUtils.getPackageUrl(getAuthorAccess());
		if (opfUrl == null)
		{
			showMessage("Could not find pagkage file for document");
			return;
		}

		opfAccess = EpubUtils.getAuthorDocument(getAuthorAccess(), opfUrl);
		if (opfAccess == null)
		{
			showMessage("Could not access pagkage file for document");
			return;
		}

		_DocNumber = 0;

		try
		{

			/*
			 * URL docUrl =
			 * getAuthorAccess().getDocumentController().getAuthorDocumentNode
			 * ().getXMLBaseURL(); String inputFile =
			 * getAuthorAccess().getUtilAccess().getFileName(docUrl.toString());
			 * if (!inputFile.equals(xhtmlFileName)) {
			 * showMessage("Splitting can not start from this file"); return; }
			 */

			epubFilePath = EpubUtils.getEpubFolder(getAuthorAccess());
			if (epubFilePath.equals(""))
			{
				showMessage("Could not access epub folder");
				return;
			}

			xhtmlAccess = EpubUtils.getAuthorDocument(getAuthorAccess(), new URL(epubFilePath + "/" + xhtmlFileName));

			AuthorElement htmlElem = xhtmlAccess.getDocumentController().getAuthorDocumentNode().getRootElement();

			if (htmlElem != null)
			{
				// add attributes to the html element of new xhtml file
				AuthorNode htmlNode = getFirstElement(xhtmlAccess.getDocumentController().findNodesByXPath("/html", htmlElem, true, true, true, true));
				if (htmlNode == null)
				{
					throw new AuthorOperationException("Found no html in xhtml file");
				}

				String htmlContent = Utils.serialize(xhtmlAccess, htmlNode);

				_DocList=new HashMap<String, Document>();
				
				_Ids=new HashMap<String, String>();
				
				_SourceDoc = OpenXmlDocument(htmlContent);

				Node DocEl = _SourceDoc.getDocumentElement();

				// String IXml = GetOuterXml(DocEl);

				// Get meta nodes
				_MetaNodes = _SourceDoc.getElementsByTagName("meta");

				// Find identifier - use xpath. For now: for loop
				String DCIdentifier = "dc:identifier";

				for (int i = 0; i < _MetaNodes.getLength(); i++)
				{
					Node n = _MetaNodes.item(i);

					String Att = GetAttributeFromNode(n, "name");

					if (Att.equals(DCIdentifier))
					{
						_ID_Prefix = GetAttributeFromNode(n, "content");
						break;
					}
				}

				// Find Source body
				XPathFactory factory = XPathFactory.newInstance();
				XPath xpath = factory.newXPath();

				Node SourceBody = null;

				try
				{
					SourceBody = (Node) xpath.evaluate("//*[local-name() = 'body']", _SourceDoc.getDocumentElement(), XPathConstants.NODE);

				}

				catch (XPathExpressionException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// Find Document Title
				_SourceTitle = GetDocTitle(DocEl);
			

				NodeList BodyNodes = SourceBody.getChildNodes();

				for (int i = 0; i < BodyNodes.getLength(); i++)
				{

					Node SectionNodeAtLevel1 = BodyNodes.item(i);

					if (SectionNodeAtLevel1.getNodeType() == Node.ELEMENT_NODE)
					{
						CreateNewEpubDoc(SectionNodeAtLevel1);
					}

				}

				MapRefs();
				
				SaveDocs();
				
				// close xhtml document
				xhtmlAccess.getEditorAccess().close(true);

				// remove xhtml document from opf document
				String fileName = xhtmlAccess.getUtilAccess().getFileName(xhtmlAccess.getEditorAccess().getEditorLocation().toString());
				EpubUtils.removeOpfItem(getAuthorAccess(), fileName);

				// delete xhtml document
				xhtmlAccess.getWorkspaceAccess().delete(xhtmlAccess.getEditorLocation());
			}

		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void SaveDocs()
	{
		for (Map.Entry<String, Document> entry : _DocList.entrySet()) 
		{
			Document D=entry.getValue();
			String F=entry.getKey();
			SaveXml(D, F);
			
		}
	}
	
	private void MapRefs()
	{
		
		for (Map.Entry<String, Document> entry : _DocList.entrySet()) 
		{
			NodeList RefNodes=null;
			Document D=entry.getValue();

			//Get references
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			try
			{
				RefNodes = (NodeList) xpath.evaluate("//*[local-name() = 'a']", D, XPathConstants.NODESET);

			}

			catch (XPathExpressionException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Check referencer mod samlingen af Id'er
			for(int i=0; i < RefNodes.getLength(); i++)
			{
				Node Ref=RefNodes.item(i);
				String Href=GetAttributeFromNode(Ref, "href");
				
				Href=Href.replace("#", "");
				
				String FName=GetReferenceFileName(Href);
				
				if(FName!="")
				{
					//Change reference
					Attr attHref = D.createAttribute("href");
					attHref.setValue(FName + "#" + Href);
					NamedNodeMap BodyAtts = Ref.getAttributes();
					BodyAtts.setNamedItem(attHref);
					
				}
				
			}

		}
		
	}
	
	private String GetReferenceFileName(String Ref)
	{
		if(_Ids.containsKey(Ref))
		{
			String FName=(String)_Ids.get(Ref);

			return FName;
		}
		
		return "";
	}
	

	private String GetDocTitle(Node DocElement)
	{
		NodeList LTit = _SourceDoc.getElementsByTagName("title");

		Node Tit = LTit.item(0);

		if (Tit != null)
		{
			return Tit.getTextContent();
		}

		return "UnKnown title";

	}

	private Document OpenXmlDocument(String Xml)
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try
		{
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Document document = null;
		
		try
		{
			document = builder.parse(new InputSource(new StringReader(Xml)));
		} 
		
		catch (SAXException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TransformerFactory transformerFactory = TransformerFactory.newInstance();

		Transformer transformer = null;

		try
		{
			transformer = transformerFactory.newTransformer();
		} 
		catch (TransformerConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(new StringWriter());

		try
		{
			transformer.transform(source, result);
		}
		catch (TransformerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return document;
	}

	private String GetEpubMainType(String EPType)
	{
		EPType = EPType.replace("  ", " ");

		EPType = EPType.replace("frontmatter", "");
		EPType = EPType.replace("bodymatter", "");
		EPType = EPType.replace("backmatter", "");

		return EPType.trim();
	}

	private String GetAttributeFromNode(Node n, String AttName)
	{
		Element tmp = (Element) n;

		return tmp.getAttribute(AttName);

	}

	private void CreateNewEpubDoc(Node Section)
	{
		_DocNumber = _DocNumber + 1;

		String XmlTemplate = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\" xmlns:nordic=\"http://www.mtm.se/epub/\">\n" + "<head>\n" + "<title>"
				+ _SourceTitle + "</title>\n" + "</head>\n" + " <body/>\n" + "</html>";

		Document Template = OpenXmlDocument(XmlTemplate);

		NodeList temp = Template.getElementsByTagName("body");

		Node TemplateBody = temp.item(0);

		// String IXml = GetOuterXml(TemplateBody);

		temp = Template.getElementsByTagName("head");

		Node TemplateHead = temp.item(0);

		temp = Template.getElementsByTagName("title");

		// insert metanodes
		for (int i = 0; i < _MetaNodes.getLength(); i++)
		{
			Node n = _MetaNodes.item(i);
			Node ImpNode = Template.importNode(n, true);
			TemplateHead.appendChild(ImpNode);
		}

		// Read Section Node
		// Section Classname
		String Classname = GetAttributeFromNode(Section, "class");

		if (Classname != "")
		{
			// Add class to body
			Attr attClass = Template.createAttribute("class");
			attClass.setValue(Classname);
			NamedNodeMap BodyAtts = TemplateBody.getAttributes();
			BodyAtts.setNamedItem(attClass);
		}

		// IXml = GetOuterXml(TemplateBody);

		// Section Id
		String Id = GetAttributeFromNode(Section, "id");

		if (Id != "")
		{
			// Add id to body
			Attr attID = Template.createAttribute("id");
			attID.setValue(Id);
			NamedNodeMap BodyAtts = TemplateBody.getAttributes();
			BodyAtts.setNamedItem(attID);
		}

		// IXml = GetOuterXml(TemplateBody);

		// Section epub:type
		String EpubType = GetAttributeFromNode(Section, "epub:type");

		String EpubMainType = "unknown";

		if (EpubType != "")
		{
			// Add id to body
			Attr attEpubType = Template.createAttributeNS("http://www.idpf.org/2007/ops", "epub:type");
			attEpubType.setValue(EpubType);
			NamedNodeMap BodyAtts = TemplateBody.getAttributes();
			BodyAtts.setNamedItemNS(attEpubType);

			// epub:type might be divided by spaces - only 1 value will be used

			EpubMainType = GetEpubMainType(EpubType);
		}

		// IXml = GetOuterXml(TemplateBody);

		// Add Nodes to body
		NodeList SectionNodes = Section.getChildNodes();

		for (int i = 0; i < SectionNodes.getLength(); i++)
		{
			Node SecNode = SectionNodes.item(i);

			// Import the node
			Node ImportedNode = Template.importNode(SecNode, true);

			TemplateBody.appendChild(ImportedNode);

		}

		// Create FileName
		String strNum = "00" + Integer.toString(_DocNumber);
		strNum = strNum.substring(strNum.length() - 3);
		String NewFileName = _ID_Prefix + "-" + strNum + "-" + EpubMainType + ".xhtml";

		_DocList.put(NewFileName, Template);
		
		_stop=false;
		
		CollectIds(NewFileName, TemplateBody);

	}
	
	private void CollectIds(String FName, Node n)
	{
	//Kør rekursivt
		if(_stop)
		{
			return;
		}
		
		//Check on noden har et id
		
		if(n.getNodeType()==Node.ELEMENT_NODE)
		{
			String id=GetAttributeFromNode(n, "id");
			
			if(id !="")
			{
				//Læg i Map
				if(AddId2Map(id,FName)== false)
				{
					_stop=true;
					return;
					
				}
				else
				{
					_ErrMsg= id + " er findes flere gange i dokumentet.";
				}
				
			}
		}
		
		
				
		if(n.hasChildNodes())
		{
			NodeList lst = n.getChildNodes();

			for (int i = 0; i < lst.getLength(); i++)
			{
				Node ChildNode = lst.item(i);

				CollectIds(FName, ChildNode);
			}
		}
	}
	
	private boolean AddId2Map(String Id, String FName)
	{
		if(_Ids.containsKey(Id))
		{
			_ErrMsg= "Id'et " + Id + " findes flere gange i dokumentet";
			return false;
		}
		
		_Ids.put(Id, FName);
		
		return true;
	}

	private void SaveXml(Document xhtmlDocument, String splitFileName)
	{

		try
		{

			// transform new document to xml content
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			DOMSource source = new DOMSource(xhtmlDocument);
			StreamResult result = new StreamResult(buffer);
			transformer.transform(source, result);
			String xmlContent = buffer.toString();

			// save string into new xhtml concatenated document
			AuthorWorkspaceAccess wa = getAuthorAccess().getWorkspaceAccess();
			URL newEditorUrl = wa.createNewEditor("xhtml", "text/xml", xmlContent);
			WSEditor editor = wa.getEditorAccess(newEditorUrl);
			editor.saveAs(new URL(epubFilePath + "/" + splitFileName));

			wa.close(new URL(epubFilePath + "/" + splitFileName));

			// add xhtml document to opf document
			EpubUtils.addOpfItem(getAuthorAccess(), splitFileName);

		} 
		catch (TransformerConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (TransformerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	protected void parseArguments(ArgumentsMap args) throws IllegalArgumentException
	{
		// Nothing to parse!!!

	}

	private String GetOuterXml(Node n)
	{
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try
		{
			transformer = transFactory.newTransformer();
		} catch (TransformerConfigurationException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return "";
		}
		try
		{
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.transform(new DOMSource(n), new StreamResult(buffer));
			return buffer.toString();

		} catch (TransformerConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		} catch (TransformerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}

	}
}
