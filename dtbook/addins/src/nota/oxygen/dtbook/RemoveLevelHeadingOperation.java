package nota.oxygen.dtbook;

import nota.oxygen.common.BaseAuthorOperation;
import nota.oxygen.common.Utils;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 * @author OHA
 *
 */
public class RemoveLevelHeadingOperation extends BaseAuthorOperation {

	private static String ARG_NS = "namespace";
	private static String ARG_BLOCK_REPLACEMENT_NAME = "block-replacement-name";
	private static String ARG_GENERIC_LEVEL_TAG = "generic-level-tag";
	private static String ARG_GENERIC_HEADING_TAG = "generic-heading-tag";
	private static String ARG_FIXED_LEVEL_TAGS = "fixed-level-tags";
	private static String ARG_FIXED_HEADING_TAGS = "fixed-heading-tags";
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
			
				new ArgumentDescriptor(ARG_NS, ArgumentDescriptor.TYPE_STRING, "Namespace of level container and heading elements"),
				new ArgumentDescriptor(ARG_BLOCK_REPLACEMENT_NAME, ArgumentDescriptor.TYPE_STRING, "Name of the block element to replace the heading element with"),
				new ArgumentDescriptor(ARG_GENERIC_LEVEL_TAG, ArgumentDescriptor.TYPE_STRING, "Name of the generic level container element"),
				new ArgumentDescriptor(ARG_GENERIC_HEADING_TAG, ArgumentDescriptor.TYPE_STRING, "Name of the generic level heading element"),
				new ArgumentDescriptor(ARG_FIXED_LEVEL_TAGS, ArgumentDescriptor.TYPE_STRING, "Names of the fixed-depth level container elements, separated by |"),
				new ArgumentDescriptor(ARG_FIXED_HEADING_TAGS, ArgumentDescriptor.TYPE_STRING, "Name of the fixed-depth level heading elements, separated by |")};
	}
	
	String ns;
	String blockReplacementName;
	String genericLevelTag;
	String genericHeadingTag;
	String[] fixedLevelTags;
	String[] fixedHeadingTags;

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		ns = (String)args.getArgumentValue(ARG_NS);
		if (ns==null) ns = "";
		blockReplacementName = (String)args.getArgumentValue(ARG_BLOCK_REPLACEMENT_NAME);
		genericLevelTag = (String)args.getArgumentValue(ARG_GENERIC_LEVEL_TAG);
		if (genericLevelTag==null) genericLevelTag = "";
		genericHeadingTag = (String)args.getArgumentValue(ARG_GENERIC_HEADING_TAG); 
		if (genericHeadingTag==null) genericHeadingTag = "";
		fixedLevelTags = ((String)args.getArgumentValue(ARG_FIXED_LEVEL_TAGS)).split("\\|");
		fixedHeadingTags = ((String)args.getArgumentValue(ARG_FIXED_HEADING_TAGS)).split("\\|");
	}
	
	protected boolean isHeadingElement(AuthorElement elem) {
		if (elem==null) return false;
		if (elem.getNamespace().equals(ns)) {
			String ln = elem.getLocalName();
			if (ln.equals(genericHeadingTag)) return true;
			for (int i=0; i<fixedHeadingTags.length; i++) {
				if (ln.equals(fixedHeadingTags[i])) return true;
			}
		}
		return false;
	}
	
	
	protected boolean isLevelElement(AuthorElement elem) {
		if (elem==null) return false;
		if (elem.getNamespace().equals(ns)) {
			if (elem.getLocalName().equals(genericLevelTag)) return true;
			for (int i=0; i<fixedLevelTags.length; i++) {
				if (elem.getLocalName().equals(fixedLevelTags[i])) return true;
			}
		}
		return false;
	}
	
	


	@Override
	public String getDescription() {
		return "Merges the level in which the current element is heading with it's surrounding levels";
	}
	
	protected int getLastChildOrSelfEndOffset(AuthorElement elem) {
		if (elem!=null) return elem.getEndOffset();
		AuthorNode lastChild = Utils.getLastChild(elem);
		if (lastChild!=null) return lastChild.getEndOffset();
		return 0;
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			if (fixedLevelTags==null) throw new AuthorOperationException("Nanes of fixed level container elements are missing");
			if (fixedHeadingTags==null) throw new AuthorOperationException("Nanes of fixed heading elements are missing");
			if (fixedLevelTags.length!=fixedHeadingTags.length) {
				//showMessage("fixed levels: "+flt+" fixed headings: "+fht);
				throw new AuthorOperationException(
						"Different number names of elements for fixed level containers and headings are different"
						+"- counts are "+fixedLevelTags.length+" and "+fixedHeadingTags.length+" respectively");
			}
			AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
			int startSel = getSelectionStart();
			AuthorElement firstAthElem = (AuthorElement)docCtrl.getNodeAtOffset(startSel);
			if (!isHeadingElement(firstAthElem)) {
				showMessage("Current element is not a level heading");
				return;
			}
			AuthorElement parentLevel = (AuthorElement)firstAthElem.getParent();
			if (!isLevelElement(parentLevel)) {
				showMessage("Current heading is unexpectedly not the child of a level element (parent is a "+parentLevel.getLocalName()+")");
				return;
			}			
			AuthorElement previousSibling = Utils.getPreviousSibling(parentLevel);
			AuthorElement grandParent = (AuthorElement)parentLevel.getParent();
			if (isLevelElement(previousSibling)) {
				docCtrl.renameElement(firstAthElem, blockReplacementName);
				docCtrl.removeAttribute("depth", firstAthElem);
				int pos = getLastChildOrSelfEndOffset(previousSibling);
				docCtrl.insertXMLFragment(serializeChildren(parentLevel), previousSibling, AuthorConstants.POSITION_INSIDE_LAST);
				docCtrl.deleteNode(parentLevel);
				getAuthorAccess().getEditorAccess().setCaretPosition(pos+1);
			}
			else if (isLevelElement(grandParent)) {
				int pos = parentLevel.getStartOffset();
				docCtrl.renameElement(firstAthElem, blockReplacementName);
				docCtrl.removeAttribute("depth", firstAthElem);
				docCtrl.insertXMLFragment(serializeChildren(parentLevel), parentLevel, AuthorConstants.POSITION_AFTER);
				docCtrl.deleteNode(parentLevel);
				getAuthorAccess().getEditorAccess().setCaretPosition(pos+1);
			}
			else {
				showMessage(
						"The level of which the current element is heading is the first at level 1 in "+grandParent.getLocalName()
						+" and therefore cannot be removed");
				return;
			}
			
			
		}
		catch (Exception e) {
			if (e instanceof AuthorOperationException) {
				throw (AuthorOperationException)e;
			}
			else {
				throw new AuthorOperationException(
						"Unexpected "+e.getClass().getName()+" occured: "+e.getMessage(),
						e);
			}
		}
	}

}
