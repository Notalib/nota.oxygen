package nota.oxygen.epub;

import ro.sync.ecss.extensions.api.AttributeChangedEvent;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorListener;
import ro.sync.ecss.extensions.api.AuthorOperationException;
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
	
	private void updateLangFromXmlLang(AuthorElement elem, boolean recursive) {
		AttrValue xmlLang = elem.getAttribute("xml:lang");
		AttrValue lang = elem.getAttribute("lang");
		if (xmlLang == null && lang != null) {
			authorAccess.getDocumentController().removeAttribute("lang", elem);
		}
		else if (!xmlLang.equals(lang)) {
			authorAccess.getDocumentController().setAttribute("lang", xmlLang, elem);
		}
		if (recursive) {
			for (AuthorNode node : elem.getContentNodes()) {
				if (node instanceof AuthorElement) updateLangFromXmlLang((AuthorElement)node, true);
			}
		}
	}
	
	public void updateAllLangsFromXmlLangs() {
		try {
			for (AuthorNode node : authorAccess.getDocumentController().findNodesByXPath("//*[@xml:lang or @lang]", true, true, true)) {
				updateLangFromXmlLang((AuthorElement)node, false);
			}
		} catch (AuthorOperationException e) {
		}
	}

	@Override
	public void attributeChanged(AttributeChangedEvent event) {
		if (event.getAttributeName().equals("xml:lang")) {
			updateLangFromXmlLang((AuthorElement)event.getOwnerAuthorNode(), false);
		}
	}

	@Override
	public void authorNodeNameChanged(AuthorNode arg0) {
	}

	@Override
	public void authorNodeStructureChanged(AuthorNode node) {
		if (node instanceof AuthorElement) {
			updateLangFromXmlLang((AuthorElement)node, true);
		}
	}

	@Override
	public void beforeAttributeChange(AttributeChangedEvent arg0) {
	}

	@Override
	public void beforeAuthorNodeNameChange(AuthorNode arg0) {
	}

	@Override
	public void beforeAuthorNodeStructureChange(AuthorNode arg0) {
	}

	@Override
	public void beforeContentDelete(DocumentContentDeletedEvent arg0) {
	}

	@Override
	public void beforeContentInsert(DocumentContentInsertedEvent arg0) {
	}

	@Override
	public void beforeDoctypeChange() {
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
