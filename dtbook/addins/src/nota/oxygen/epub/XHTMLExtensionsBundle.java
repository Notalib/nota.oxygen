package nota.oxygen.epub;

import java.net.URL;

import nota.oxygen.common.Utils;

import org.w3c.dom.Document;

import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;
import ro.sync.ecss.extensions.api.AuthorExtensionStateListenerDelegator;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.CustomAttributeValueEditor;
import ro.sync.ecss.extensions.api.UniqueAttributesRecognizer;

public class XHTMLExtensionsBundle extends ro.sync.ecss.extensions.xhtml.XHTMLExtensionsBundle {
	
	@Override
	public AuthorExtensionStateListener createAuthorExtensionStateListener() {
		if (stateListener == null) {
			stateListener = new AuthorExtensionStateListenerDelegator();
			stateListener.addListener(getUniqueAttributesIdentifier());
			stateListener.addListener(getLangStateListener());
		}
		
		return stateListener;
	}
	
	XHTMLUniqueAttributesRecognizer uniqueAttributesRecognizer;
	
	LangExtensionStateListener langStateListener;
	
	AuthorExtensionStateListenerDelegator stateListener;

	@Override
	public UniqueAttributesRecognizer getUniqueAttributesIdentifier() {
		if (uniqueAttributesRecognizer == null) uniqueAttributesRecognizer = new XHTMLUniqueAttributesRecognizer();
		return uniqueAttributesRecognizer;
	}
	
	public LangExtensionStateListener getLangStateListener() {
		if (langStateListener == null) langStateListener = new LangExtensionStateListener();
		return langStateListener;
	}
	
	private static Document attributeValueListsDocument;
	
	public static Document getAttributeValueListsDocument() {
		if (attributeValueListsDocument == null) {
			try {
				try {
					attributeValueListsDocument = Utils.loadDocument(new URL("http://notalib.github.io/nota.oxygen/attributeValueLists.xml"));
				}
				catch (Exception e) {
					attributeValueListsDocument = Utils.deserializeDocument("<attributeValueLists/>", null);
				}
			}
			catch (AuthorOperationException e) {
				attributeValueListsDocument = null;
			}
		}
		return attributeValueListsDocument;
	}
	
	XHTMLAttributeValueEditor attributesValueEditor;

	@Override
	public CustomAttributeValueEditor createCustomAttributeValueEditor(
			boolean arg0) {
		if (attributesValueEditor == null) {
			attributesValueEditor = new XHTMLAttributeValueEditor(getAttributeValueListsDocument());
		}
		return attributesValueEditor;
	}
	
	
	
}
