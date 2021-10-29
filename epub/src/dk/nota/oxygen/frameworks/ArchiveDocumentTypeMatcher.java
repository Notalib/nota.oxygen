package dk.nota.oxygen.frameworks;

import org.xml.sax.Attributes;

import ro.sync.ecss.extensions.api.DocumentTypeCustomRuleMatcher;

public class ArchiveDocumentTypeMatcher implements DocumentTypeCustomRuleMatcher {

	@Override
	public String getDescription() {
		return "A matcher for checking if a system ID is archive-based";
	}

	@Override
	public boolean matches(String systemId, String rootNamespace,
			String rootLocalName, String publicDoctype,
			Attributes rootAttributes) {
		if (systemId.startsWith("zip:")) return true;
		return false;
	}

}
