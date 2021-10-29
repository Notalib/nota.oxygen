package dk.nota.oxygen.listeners.html;

import ro.sync.ecss.extensions.api.AttributeChangedEvent;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorListenerAdapter;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class LangAuthorListener extends AuthorListenerAdapter {
	
	private AuthorAccess authorAccess;
	
	public LangAuthorListener(AuthorAccess authorAccess) {
		super();
		this.authorAccess = authorAccess;
	}
	
	@Override
	public void attributeChanged(AttributeChangedEvent attributeChangedEvent) {
		if (attributeChangedEvent.getAttributeName().equals("lang")) {
			AuthorNode node = attributeChangedEvent.getOwnerAuthorNode();
			if (!(node instanceof AuthorElement)) return;
			authorAccess.getDocumentController().setAttribute("xml:lang",
					((AuthorElement)node).getAttribute("lang"),
					(AuthorElement)node);
		}
	}

}
