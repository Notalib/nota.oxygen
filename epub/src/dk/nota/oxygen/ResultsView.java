package dk.nota.oxygen;

import ro.sync.document.DocumentPositionedInfo;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.results.ResultsManager;

/**
 * <p>A view showing <em>results</em> (of transformations, searches, validation
 * and similar) in a tabular format. Results may or may not include additional
 * information regarding document position, allowing the user to go directly
 * to a document position.</p>
 * <p>Each instance of this class is a distinct results panel identified by its
 * title. The actual administration of the views is handled by
 * {@link ro.sync.exml.workspace.api.results.ResultsManager}.</p>
 */

public class ResultsView {
	
	private ResultsManager resultsManager;
	private String title;
	
	public ResultsView(String title) {
		PluginWorkspace pluginWorkspace = PluginWorkspaceProvider
				.getPluginWorkspace();
		resultsManager = pluginWorkspace.getResultsManager();
		resultsManager.setResults(title, null, null);
		this.title = title;
	}
	
	public void writeResult(DocumentPositionedInfo documentInfo) {
		resultsManager.addResult(title, documentInfo, ResultsManager.ResultType
				.PROBLEM, true, true);
	}
	
	public void writeResult(String string) {
		writeResult(new DocumentPositionedInfo(DocumentPositionedInfo
				.SEVERITY_INFO, string));
	}
	
	public void writeResult(String message, String systemId) {
		writeResult(new DocumentPositionedInfo(DocumentPositionedInfo
				.SEVERITY_INFO, message, systemId));
	}

}
