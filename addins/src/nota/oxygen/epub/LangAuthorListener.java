package nota.oxygen.epub;

import ro.sync.ecss.extensions.api.AttributeChangedEvent;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorExtensionStateListenerDelegator;
import ro.sync.ecss.extensions.api.AuthorListener;
import ro.sync.ecss.extensions.api.DocumentContentDeletedEvent;
import ro.sync.ecss.extensions.api.DocumentContentInsertedEvent;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorDocument;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class LangAuthorListener implements AuthorListener {
	
	public LangAuthorListener(AuthorAccess aa) {
		authorAccess = aa;
	}
	
	private AuthorAccess authorAccess; 
	
	private void updateLangFromXmlLang(AuthorElement elem) {
		AttrValue xmlLang = elem.getAttribute("xml:lang");
		if (xmlLang == null) {
			authorAccess.getDocumentController().removeAttribute("lang", elem);
		}
		else {
			authorAccess.getDocumentController().setAttribute("lang", elem.getAttribute("xml:lang"), elem);
		}
		
	}

	@Override
	public void attributeChanged(AttributeChangedEvent arg0) {
		if (arg0.getAttributeName().equals("xml:lang")) {
			updateLangFromXmlLang((AuthorElement)arg0.getOwnerAuthorNode());
		}

	}

	@Override
	public void authorNodeNameChanged(AuthorNode arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void authorNodeStructureChanged(AuthorNode arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeAttributeChange(AttributeChangedEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeAuthorNodeNameChange(AuthorNode arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeAuthorNodeStructureChange(AuthorNode arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeContentDelete(DocumentContentDeletedEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeContentInsert(DocumentContentInsertedEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeDoctypeChange() {
		// TODO Auto-generated method stub

	}

	@Override
	public void contentDeleted(DocumentContentDeletedEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contentInserted(DocumentContentInsertedEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doctypeChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void documentChanged(AuthorDocument arg0, AuthorDocument arg1) {
		// TODO Auto-generated method stub

	}

}
