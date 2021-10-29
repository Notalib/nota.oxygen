package dk.nota.epub;

import java.net.URI;
import java.util.HashMap;

public class EpubAccessProvider {
	
	private static final HashMap<URI,EpubAccess> epubAccessMap =
			new HashMap<URI,EpubAccess>();
	
	private EpubAccessProvider() {
		
	}
	
	public static EpubAccess getEpubAccess(URI epubArchiveUri)
			throws EpubException {
		EpubAccess epubAccess = epubAccessMap.get(epubArchiveUri);
		if (epubAccess == null) {
			epubAccess = new EpubAccess(epubArchiveUri);
			epubAccessMap.put(epubArchiveUri, epubAccess);
		}
		return epubAccess;
	}

}
