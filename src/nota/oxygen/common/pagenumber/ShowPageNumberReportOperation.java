package nota.oxygen.common.pagenumber;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.text.BadLocationException;

import nota.oxygen.common.BaseAuthorOperation;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 * Displays a report on page numbers that are present in the document
 * @author OHA
 *
 */
public class ShowPageNumberReportOperation extends BaseAuthorOperation {

	private static String ARG_PAGENUM_XPATH = "pagenum xpath";
	private String pagenumXPath;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_PAGENUM_XPATH, ArgumentDescriptor.TYPE_XPATH_EXPRESSION, "XPath expression to find pagenumbers", null, "//pagenum")
		};
	}
 
	@Override
	public String getDescription() {
		return "Generate page number report";
	}


	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		pagenumXPath = (String)args.getArgumentValue(ARG_PAGENUM_XPATH);
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		AuthorNode[] pagenums = getAuthorAccess().getDocumentController().findNodesByXPath(pagenumXPath, true, true, true);
		if (pagenums.length==0) {
			showMessage("Found no page numbers in document");
			return;
		}
		Dictionary<String, ArrayList<String>> pnDict = new Hashtable<String, ArrayList<String>>();
		for (int i=0; i<pagenums.length; i++) {
			if (pagenums[i] instanceof AuthorElement) {
				AuthorElement pn = (AuthorElement)pagenums[i];
				String page = "";
				if (pn.getAttribute("page")!=null) page = pn.getAttribute("page").getValue();
				if (pnDict.get(page)==null) pnDict.put(page,  new ArrayList<String>());
				try {
					pnDict.get(page).add(pn.getTextContent());
				} catch (BadLocationException e) {
					// Do nothing
				}
			}	
		}
		for (Enumeration<String> e = pnDict.keys(); e.hasMoreElements();) {
			String page = e.nextElement();
			String message = page+" page numbers:\n";
			List<String> pages = pnDict.get(page);
			for (int i=0; i<pages.size(); i++) {
				int firstInSeq = parsePagenumber(pages.get(i));
				if (firstInSeq>0) {
					int prevInSeq = firstInSeq;
					while (i+1<pages.size()) {
						if (prevInSeq+1!=parsePagenumber(pages.get(i+1))) break;
						prevInSeq++;
						i++;
					}
					if (firstInSeq<prevInSeq) {
						message += ""+firstInSeq+"-"+prevInSeq+",";
					}
					else {
						message += ""+firstInSeq+",";
					}
					if (parsePagenumber(pages.get(i))>0) {
						continue;
					}
				}
				message += pages.get(i)+",";
			}
			if (message.endsWith(",")) message = message.substring(0, message.length()-1);
			showMessage(message);
		}
		
	}
	
	private int parsePagenumber(String pn) {
		try {
			return Integer.parseInt(pn);
		}
		catch (NumberFormatException e) {
			return -1;
		}
	}

}
