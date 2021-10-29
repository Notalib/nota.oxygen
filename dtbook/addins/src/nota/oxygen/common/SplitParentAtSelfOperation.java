package nota.oxygen.common;

import java.util.Random;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 * Operation that splits the parent of the current element at the current element, 
 * making the current element a sibling between the two parts of the parent
 * @author OHA
 *
 */
public class SplitParentAtSelfOperation extends BaseAuthorOperation {
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return null;
	}

	@Override
	public String getDescription() {
		return "Operation that splits the parent of the current element at the current element, making the current element a sibling between the two parts of the parent";
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		try
		{
			AuthorElement eCur = getCurrentElement();
//			showMessage("Current element: "+serialize(eCur));
			if (eCur == null) {
				throw new AuthorOperationException("Unexpectedly could not find current element");
			}
			Random rnd = new Random();
			String idVal = "splitter"+rnd.nextInt(1000000);
			docCtrl.setAttribute("dk.nota.oxygen.id", new AttrValue(idVal), eCur);
			AuthorNode aNode = eCur.getParent();
			if (aNode instanceof AuthorElement) {
				AuthorElement eParent = (AuthorElement)aNode;
//				showMessage("Parent element: "+serialize(eParent));
//				showMessage("Grand parent: "+serialize(eParent.getParent()));
				Element parent = Utils.deserializeElement(serialize(eParent));
				int insertPoint = eParent.getStartOffset();
				docCtrl.deleteNode(eParent);
				Element cur = null;
				for (int i=0; i<parent.getChildNodes().getLength(); i++) {
					Node n = parent.getChildNodes().item(i); 
					if (n instanceof Element) {
						if (((Element)n).getAttribute("dk.nota.oxygen.id").equals(idVal)) {
							cur = (Element)n;
							break;
						}
					}
				}
				if (cur==null) {
					throw new AuthorOperationException("Unexpectedly could not find current element in DOM implementation");
				}
				Element parentSplit = (Element)parent.cloneNode(false);
				for (int i=0; i<parentSplit.getAttributes().getLength(); i++) {
					parentSplit.removeAttributeNode((Attr)parentSplit.getAttributes().item(i));
				}
				Node next = cur.getNextSibling();
				while (next!=null) {
					parent.removeChild(next);
					parentSplit.appendChild(next);
					next = cur.getNextSibling();
				}
				parent.removeChild(cur);
				docCtrl.insertXMLFragment(Utils.serialize(parent)+Utils.serialize(cur)+Utils.serialize(parentSplit), insertPoint);
				eCur = findElementByXPath("//*[@dk.nota.oxygen.id='"+idVal+"']");
				if (eCur==null) throw new AuthorOperationException("Unexpectedly could not find the element at which the split was made");
				getAuthorAccess().getEditorAccess().select(eCur.getStartOffset(), eCur.getEndOffset());
				docCtrl.removeAttribute("dk.nota.oxygen.id", eCur);
			}
			else {
				showMessage("Current element has no parent element");
				docCtrl.removeAttribute("dk.nota.oxygen.id", eCur);
			}
		}
		catch (Exception e) {
			throw new AuthorOperationException(
					"Unexpected "+e.getClass().getName()+" occured: "+e.getMessage(),
					e);
		}
		
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
	}

}
