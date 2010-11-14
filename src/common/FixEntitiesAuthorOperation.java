package common;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorDocument;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;

/**
 * @author OHA
 *
 */
public class FixEntitiesAuthorOperation extends BaseAuthorOperation {

	private static String ARG_ENTITIES = "entities to fix";
	protected List<String> entities;
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_ENTITIES, ArgumentDescriptor.TYPE_STRING, "list of entities to fix - space seperated")
		};
	}

	@Override
	public String getDescription() {
		return "Fix character entities";
	}
	
	private void fixEntities(Node nod) {
		NodeList children = nod.getChildNodes();
		for (int i=0; i<children.getLength();i++) {
			Node child = children.item(i);
			if (child instanceof EntityReference) {
				String text = child.getTextContent();
				if (text!=null) {
					Text repl = nod.getOwnerDocument().createTextNode(child.getTextContent());
					nod.replaceChild(child, repl);
				}
				else {
					showMessage("Text content of entity is null");
				}
			}
		}
	
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		AuthorDocument dtbookDoc = docCtrl.getAuthorDocumentNode();
		if (dtbookDoc==null) {
			showMessage("AuthorDocumentNode is null");
		}
		Element dtbookElem = deserialize(serialize(dtbookDoc));
		showMessage("Fixing entities");
		fixEntities(dtbookElem);
		AuthorDocumentFragment frag = docCtrl.createNewDocumentFragmentInContext(serialize(dtbookElem), dtbookDoc.getStartOffset());
		docCtrl.replaceRoot(frag);
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		entities = new ArrayList<String>();
		String ent = (String)args.getArgumentValue(ARG_ENTITIES);
		for (String e : ent.split(" ")) entities.add(e);
	}

}
