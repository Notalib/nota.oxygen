package nota.oxygen.common.dtbook.v110;

import java.util.List;

import javax.swing.text.BadLocationException;

import nota.oxygen.common.BaseAuthorOperation;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 * @author OHA
 * Markup current element as a level heading, including surrounding level
 */
public class MarkupLevelHeadingOperation extends BaseAuthorOperation {

	private static String ARG_LEVEL_TYPE = "level-type";
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_LEVEL_TYPE, ArgumentDescriptor.TYPE_STRING, "Type of level to markup - possible values are 'sub-level' and 'same-level'"),
				new ArgumentDescriptor(ARG_GENERIC_LEVEL_TAG, ArgumentDescriptor.TYPE_STRING, "Name of the generic level container element"),
				new ArgumentDescriptor(ARG_GENERIC_HEADING_TAG, ArgumentDescriptor.TYPE_STRING, "Name of the generic level heading element"),
				new ArgumentDescriptor(ARG_FIXED_LEVEL_TAGS, ArgumentDescriptor.TYPE_CONSTANT_LIST, "Names of the fixed-depth level container elements"),
				new ArgumentDescriptor(ARG_FIXED_HEADING_TAGS, ArgumentDescriptor.TYPE_CONSTANT_LIST, "Name of the fixed-depth level heading elements")};
	}
	
	String levelType;
	String genericLevelTag;
	String genericHeadingTag;
	String[] fixedLevelTags;
	String[] fixedHeadingTags;

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		levelType = (String)args.getArgumentValue(ARG_LEVEL_TYPE);
		genericLevelTag = (String)args.getArgumentValue(ARG_GENERIC_LEVEL_TAG);
		genericHeadingTag = (String)args.getArgumentValue(ARG_GENERIC_HEADING_TAG);
		fixedLevelTags = (String[])args.getArgumentValue(ARG_FIXED_LEVEL_TAGS);
		fixedHeadingTags = (String[])args.getArgumentValue(ARG_FIXED_HEADING_TAGS);
	}

	@Override
	public String getDescription() {
		return "Markup current element as a level heading, including surrounding level";
	}
	
	protected int getLevelDepth(AuthorNode node) {
		int res = 0;
		if (node!=null)	{
			res = getLevelDepth(node.getParent());
			if (node instanceof AuthorElement) {
				if (((AuthorElement) node).getLocalName().equals("level")) res++;
			}
		}
		return res;
	}
	
	protected boolean isLevelTag(String localName) {
		if (localName.equals(genericLevelTag)) return true;
		for (int i=0; i<fixedLevelTags.length; i++) {
			if (localName.equals(fixedLevelTags[i])) return true;
		}
		return false;
	}
	
	protected void doSubLevelOperation() throws AuthorOperationException, BadLocationException {
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		if (fixedLevelTags==null) throw new AuthorOperationException("Nanes of fixed level container elements are missing");
		if (fixedHeadingTags==null) throw new AuthorOperationException("Nanes of fixed heading elements are missing");
		if (fixedLevelTags.length!=fixedHeadingTags.length) {
			throw new AuthorOperationException("Different number names of elements for fixed level containers and headings are different");
		}
		int startSel = getSelectionStart();
		AuthorElement firstAthElem = (AuthorElement)docCtrl.getNodeAtOffset(startSel);
		if (isLevelTag(firstAthElem.getLocalName())) {
			showMessage("Current element is a level container can therefore not become a level heading ");
		}
		int depth = getLevelDepth(firstAthElem)+1;
		AuthorElement parent = (AuthorElement)firstAthElem.getParent();
		String newLevelName = "";
		String newHeadingName = "";
		String localName = parent.getLocalName();
		if (localName.equals(genericLevelTag)) {
			newLevelName = genericLevelTag;
			newHeadingName = genericHeadingTag;
		}
		else {
			for (int i=0; i<fixedLevelTags.length-1; i++) {
				if (localName.equals(fixedLevelTags[i])) {
					newLevelName = fixedLevelTags[i+1];
					newHeadingName = fixedHeadingTags[i+1];
				}
			}
			if (newHeadingName.isEmpty()) {
				showMessage("The current element does not have a level container parent element");
				return;
			}
		}
		List<AuthorNode> siblings = parent.getContentNodes();
		AuthorElement lastAthElem = firstAthElem;
		for (int i = siblings.indexOf(firstAthElem)+1; i<siblings.size(); i++) {
			if (siblings.get(i) instanceof AuthorElement)
			{
				AuthorElement elem = (AuthorElement)siblings.get(i);
				if (elem.getLocalName().equals(newLevelName)) break;
				lastAthElem = elem;
			}
		}
		docCtrl.surroundInFragment("<"+newLevelName+" depth='"+depth+"'></"+newLevelName+">", firstAthElem.getStartOffset(), lastAthElem.getEndOffset());
		docCtrl.renameElement(firstAthElem, newHeadingName);		
		getAuthorAccess().getEditorAccess().setCaretPosition(firstAthElem.getStartOffset()+1);
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		try
		{
			if (levelType.equalsIgnoreCase("sub-level"))
			{
				doSubLevelOperation();
			}
			else if (levelType.equalsIgnoreCase("same-level"))
			{
				doSameLevelOperation();
			}
			else
			{
				throw new AuthorOperationException("Unknown levelType "+levelType);
			}
		}
		catch (Exception e)
		{
			if (e instanceof AuthorOperationException)
			{
				throw (AuthorOperationException)e;
			}
			else
			{
				throw new AuthorOperationException(
						"Unexpected "+e.getClass().getName()+" occured: "+e.getMessage(),
						e);
			}
		}
	}

	protected void doSameLevelOperation() throws AuthorOperationException, BadLocationException {
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		int startSel = getSelectionStart();
		AuthorElement firstAthElem = (AuthorElement)docCtrl.getNodeAtOffset(startSel);
		if (isLevelTag(firstAthElem.getLocalName())) {
			showMessage("Current element is a level container can therefore not become a level heading ");
		}
		int depth = getLevelDepth(firstAthElem);
		AuthorElement parent = (AuthorElement)firstAthElem.getParent();
		String newLevelName = parent.getLocalName();
		String newHeadingName = "";
		String localName = parent.getLocalName();
		if (localName=="level") {
			newHeadingName = "levelhd";
		}
		else if (localName=="level1") {
			newHeadingName = "h1";
		}
		else if (localName=="level2") {
			newHeadingName = "h2";
		}
		else if (localName=="level3") {
			newHeadingName = "h3";
		}
		else if (localName=="level4") {
			newHeadingName = "h4";
		}
		else if (localName=="level5") {
			newHeadingName = "h5";
		}
		else if (localName=="level6") {
			newHeadingName = "h6";
		}
		else {
			showMessage("The current element does not have a level or level1-6 parent element");
			return;
		}
		docCtrl.renameElement(firstAthElem, newHeadingName);
		parent = (AuthorElement)firstAthElem.getParent();
		AuthorDocumentFragment newLevelContent 
			= docCtrl.createDocumentFragment(firstAthElem.getStartOffset(), getLastChild(parent).getEndOffset());
		String newLevelXml = "<"+newLevelName+" depth='"+depth+"'>"+docCtrl.serializeFragmentToXML(newLevelContent)+"</"+newLevelName+">";
		docCtrl.delete(firstAthElem.getStartOffset(), getLastChild(parent).getEndOffset());
		docCtrl.insertXMLFragment(newLevelXml, parent.getEndOffset()+1);		
	}

}
