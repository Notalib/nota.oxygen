package dk.nota.oxygen.operations.html;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;

public class HeadingOperation extends XhtmlEpubAuthorOperation {
	
	private int depth;
	private boolean dissolve;
	private int newDepth;
	private String sectionTag =
			"<section xmlns='http://www.w3.org/1999/xhtml'/>";
	private boolean shift;
	
	private int determineSectionEnd(AuthorElement section)
			throws AuthorOperationException {
		// Basically decide whether to include following subsections or not
		AuthorElement[] subsections = getElementsByXpath(
				"section[not(matches(@epub:type, '(poem|verse)$'))]", section);
		if (!shift && subsections.length > 0)
			return subsections[0].getStartOffset();
		else return section.getEndOffset();
	}
	
	private int determineSectionStart(AuthorElement heading)
			throws AuthorOperationException {
		// If the heading candidate is preceded by a page number, the number
		// must be included in the section we establish later
		AuthorElement precedingBreak = getFirstElementByXpath(
				"preceding-sibling::*[1][@epub:type = 'pagebreak']", heading);
		if (precedingBreak != null) return precedingBreak.getStartOffset();
		return heading.getStartOffset();
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		// TODO: Make cursor position flexible using the Position class
		int caretOffsetAtStart = getAuthorEditor().getCaretOffset();
		// Get the heading candidate (operation is only available if such a
		// candidate is present, so no need to check for null)
		AuthorElement heading = getFirstElementByXpath("ancestor-or-self::p|"
				+ "ancestor-or-self::*[matches(local-name(), 'h\\d')]");
		try {
			// Get ancestor sections
			AuthorElement[] ancestorSections = getElementsByXpath(
					editingConcatDocument() ? "ancestor::section" :
					"ancestor::body|ancestor::section", heading);
			// Get current depth (= number of ancestor sections)
			depth = ancestorSections.length;
			AuthorElement parentSection = ancestorSections[depth - 1];
			stripSpaceFromElement(heading, true, true);
			int end = determineSectionEnd(parentSection);
			if (dissolve) {
				getDocumentController().renameElement(heading, "p");
				if (shift) normaliseToDepth(parentSection, depth - 2);
				else {
					splitNodeAtOffset(parentSection, end, true);
					parentSection = (AuthorElement)getDocumentController()
							.getNodeAtOffset(end);
				}
				dissolveElement(parentSection);
				return;
			}
			int start = determineSectionStart(heading);
			getDocumentController().renameElement(heading, "h" + newDepth);
			removeSpacedAttrValue(heading, "class", "bridgehead", true);
			removeSpacedAttrValue(heading, "epub:type", "bridgehead", true);
			AuthorElement section = establishSection(parentSection, start, end);
			if (shift) normaliseToDepth(section, newDepth - 1);
			if (newDepth < depth)
				insertSectionAbove(section, depth - newDepth);
			else if (newDepth > depth) insertSectionBelow(section);
		} catch (BadLocationException e) {
			throw new AuthorOperationException(e.toString());
		} finally {
			getAuthorEditor().setCaretPosition(caretOffsetAtStart);
		}
	}
	
	private AuthorElement establishSection(AuthorElement parentSection,
			int start, int end) throws AuthorOperationException,
			BadLocationException {
		// If new section starts at same offset as parent section 
		if (start - parentSection.getStartOffset() <= 1) {
			if (shift) return parentSection;
			else {
				splitNodeAtOffset(parentSection, end, true);
				return (AuthorElement)getDocumentController().getNodeAtOffset(
						end);
			}
		}
		// Establish a section
		getDocumentController().surroundInFragment(sectionTag, ++start,
				--end);
		// With the new section we increase the working depth
		depth++;
		return (AuthorElement)getDocumentController().getNodeAtOffset(start);
	}
	
	private void insertSectionAbove(AuthorElement section, int iterations)
			throws AuthorOperationException, BadLocationException {
		if (section.getParent().getName().equals("body")) {
			floatInterval(section.getStartOffset() + 1,
					section.getEndOffset() - 1);
			return;
		}
		section = (AuthorElement)floatNode(section);
		if (iterations > 1) insertSectionAbove(section, --iterations);
	}
	
	private void insertSectionBelow(AuthorElement section)
			throws AuthorOperationException, BadLocationException {
		// This XPath gets the last section descendant of a preceding sibling
		// section along with its ancestor sections up to and including the
		// preceding sibling section; basically, it retrieves only the 
		// lowermost branch of nested sections
		AuthorElement[] precedingSections = getElementsByXpath(
				"(preceding-sibling::*[1][self::section][not(matches(@epub:type,"
				+ "'(poem|verse)$'))]/descendant-or-self::section[not(matches("
				+ "@epub:type, '(poem|verse)$'))][last()]/ancestor-or-self::"
				+ "section[not(matches(@epub:type, '(poem|verse)$'))]) except "
				+ "ancestor-or-self::section",
				section);
		// If there are no preceding sections to consider, just wrap here until
		// we reach the desired depth
		if (precedingSections.length == 0) {
			wrapSection(section, newDepth - depth);
			return;
		}
		// Extract a fragment of the section, then delete it
		AuthorDocumentFragment sectionFragment = getDocumentController()
				.createDocumentFragment(section, true);
		getDocumentController().deleteNode(section);
		int precedingSectionDepth = precedingSections.length - 1;
		int depthDifference = (depth + precedingSectionDepth) - newDepth;
		// Determine offset for insertion
		// We assume it will be within the lowermost preceding section ...
		int offset = precedingSections[precedingSectionDepth].getEndOffset();
		// ... but it may be the case that we need to move up the branch, which
		// requires us to substract 1 from the difference to account for the
		// 0-based array index, ...
		if (depthDifference > 0) 
			offset = precedingSections[depthDifference - 1].getEndOffset();
		// or we may need to insert after the lowermost preceding section
		else if (depthDifference == 0)
			offset = precedingSections[precedingSectionDepth].getEndOffset() + 1;
		getDocumentController().insertFragment(offset, sectionFragment);
		// The section is now inserted into a preceding section, thus
		// increasing the depth difference by 1
		depthDifference++;
		// If the depth difference is still negative, we need to wrap
		// additional sections around the inserted section until we reach the
		// desired depth
		if (depthDifference < 0)
			wrapSection((AuthorElement)getDocumentController().getNodeAtOffset(
					offset + 1), Math.abs(depthDifference));
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor("newDepth", ArgumentDescriptor
						.TYPE_CONSTANT_LIST, "The desired new heading depth",
						new String[] {"1", "2", "3", "4", "5", "6", "none"},
						"none"),
				new ArgumentDescriptor("shift", ArgumentDescriptor
						.TYPE_CONSTANT_LIST, "Shift subsections up/down",
						new String[] {"true", "false"}, "false"),
		};
	}

	@Override
	public String getDescription() {
		return "Allows manipulation of heading and sectioning elements";
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
		String depthArgument = (String)arguments.getArgumentValue("newDepth");
		dissolve = depthArgument.equals("none");
		if (!dissolve) newDepth = Integer.parseInt(depthArgument);
		String shiftArgument = (String)arguments.getArgumentValue("shift");
		shift = shiftArgument.equals("true");
	}
	
	private void wrapSection(AuthorElement section, int iterations)
			throws AuthorOperationException {
		int start = section.getStartOffset();
 		int end = section.getEndOffset();
 		for (int i = 1; i <= iterations; i++)
 			getDocumentController().surroundInFragment(sectionTag, start,
 					end++);
	}

}
