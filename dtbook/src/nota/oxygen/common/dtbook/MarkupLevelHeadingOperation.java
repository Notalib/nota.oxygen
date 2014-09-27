package nota.oxygen.common.dtbook;

import java.util.List;

import javax.swing.text.BadLocationException;

import nota.oxygen.common.BaseAuthorOperation;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 * @author OHA
 * Markup current element as a level heading, including surrounding level
 */
public class MarkupLevelHeadingOperation extends BaseAuthorOperation {

	private static String ARG_LEVEL_TYPE = "level-type";
	private static String ARG_NS = "namespace";
	private static String ARG_GENERIC_LEVEL_TAG = "generic-level-tag";
	private static String ARG_GENERIC_HEADING_TAG = "generic-heading-tag";
	private static String ARG_FIXED_LEVEL_TAGS = "fixed-level-tags";
	private static String ARG_FIXED_HEADING_TAGS = "fixed-heading-tags";
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_LEVEL_TYPE, ArgumentDescriptor.TYPE_STRING, "Type of level to markup - possible values are 'sub-level' and 'same-level'"),
				new ArgumentDescriptor(ARG_NS, ArgumentDescriptor.TYPE_STRING, "Namespace of level container and heading elements"),
				new ArgumentDescriptor(ARG_GENERIC_LEVEL_TAG, ArgumentDescriptor.TYPE_STRING, "Name of the generic level container element"),
				new ArgumentDescriptor(ARG_GENERIC_HEADING_TAG, ArgumentDescriptor.TYPE_STRING, "Name of the generic level heading element"),
				new ArgumentDescriptor(ARG_FIXED_LEVEL_TAGS, ArgumentDescriptor.TYPE_STRING, "Names of the fixed-depth level container elements, separated by |"),
				new ArgumentDescriptor(ARG_FIXED_HEADING_TAGS, ArgumentDescriptor.TYPE_STRING, "Name of the fixed-depth level heading elements, separated by |")};
	}
	
	String levelType;
	String ns;
	String genericLevelTag;
	String genericHeadingTag;
	String[] fixedLevelTags;
	String[] fixedHeadingTags;

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		levelType = (String)args.getArgumentValue(ARG_LEVEL_TYPE);
		ns = (String)args.getArgumentValue(ARG_NS);
		if (ns==null) ns = "";
		genericLevelTag = (String)args.getArgumentValue(ARG_GENERIC_LEVEL_TAG);
		if (genericLevelTag==null) genericLevelTag = "";
		genericHeadingTag = (String)args.getArgumentValue(ARG_GENERIC_HEADING_TAG); 
		if (genericHeadingTag==null) genericHeadingTag = "";
		fixedLevelTags = ((String)args.getArgumentValue(ARG_FIXED_LEVEL_TAGS)).split("\\|");
		fixedHeadingTags = ((String)args.getArgumentValue(ARG_FIXED_HEADING_TAGS)).split("\\|");
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
	
	protected boolean isLevelElement(AuthorElement elem) {
		if (elem.getNamespace()!=ns) return false;
		if (elem.getLocalName().equals(genericLevelTag)) return true;
		for (int i=0; i<fixedLevelTags.length; i++) {
			if (elem.getLocalName().equals(fixedLevelTags[i])) return true;
		}
		return false;
	}
	
	protected void doSubLevelOperation() throws AuthorOperationException, BadLocationException {
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		int startSel = getSelectionStart();
		AuthorElement firstAthElem = (AuthorElement)docCtrl.getNodeAtOffset(startSel);
		if (isLevelElement(firstAthElem)) {
			showMessage("Current element is a level container can therefore not become a level heading ");
		}
		int depth = getLevelDepth(firstAthElem)+1;
		AuthorElement parent = (AuthorElement)firstAthElem.getParent();
		String newLevelName = "";
		String newHeadingName = "";
		if (parent.getNamespace().equals(ns)) {
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
			}
		}
		if (newHeadingName.isEmpty()) {
			showMessage("The current element is not the child of a level container that can have sub-level container children");
			return;
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
		String nsAttr = "";
		if (!ns.isEmpty()) nsAttr = " xmlns='"+ns+"'";
		docCtrl.surroundInFragment("<"+newLevelName+nsAttr+" depth='"+depth+"'></"+newLevelName+">", firstAthElem.getStartOffset(), lastAthElem.getEndOffset());
		docCtrl.renameElement(firstAthElem, newHeadingName);
		docCtrl.setAttribute("depth", new AttrValue(""+depth), firstAthElem);
		getAuthorAccess().getEditorAccess().setCaretPosition(firstAthElem.getStartOffset()+1);
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		if (fixedLevelTags==null) throw new AuthorOperationException("Names of fixed level container elements are missing");
		if (fixedHeadingTags==null) throw new AuthorOperationException("Names of fixed heading elements are missing");
		if (fixedLevelTags.length!=fixedHeadingTags.length) {
			//showMessage("fixed levels: "+flt+" fixed headings: "+fht);
			throw new AuthorOperationException(
					"Different number names of elements for fixed level containers and headings are different"
					+"- counts are "+fixedLevelTags.length+" and "+fixedHeadingTags.length+" respectively");
		}
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
		if (isLevelElement(firstAthElem)) {
			showMessage("Current element is a level container can therefore not become a level heading ");
		}
		int depth = getLevelDepth(firstAthElem);
		AuthorElement parent = (AuthorElement)firstAthElem.getParent();
		String newLevelName = parent.getLocalName();
		String newHeadingName = "";
		if (parent.getNamespace().equals(ns)) {
			if (newLevelName.equals(genericLevelTag)) {
				newHeadingName = genericHeadingTag;
			}
			for (int i=0; i<fixedLevelTags.length; i++) {
				if (newLevelName.equals(fixedLevelTags[i])) {
					newHeadingName = fixedHeadingTags[i];
				}
			}
		}
		if (newHeadingName.isEmpty()) {
			showMessage("The current element is not the child of a level container element");
			return;
		}
		docCtrl.renameElement(firstAthElem, newHeadingName);
		docCtrl.setAttribute("depth", new AttrValue(""+depth), firstAthElem);
		parent = (AuthorElement)firstAthElem.getParent();
		AuthorDocumentFragment newLevelContent 
			= docCtrl.createDocumentFragment(firstAthElem.getStartOffset(), getLastChild(parent).getEndOffset());
		String nsAttr = "";
		if (!ns.isEmpty()) nsAttr = " xmlns='"+ns+"'";
		String newLevelXml = "<"+newLevelName+nsAttr+" depth='"+depth+"'>"+docCtrl.serializeFragmentToXML(newLevelContent)+"</"+newLevelName+">";
		docCtrl.delete(firstAthElem.getStartOffset(), getLastChild(parent).getEndOffset());
		docCtrl.insertXMLFragment(newLevelXml, parent.getEndOffset()+1);		
	}

}
