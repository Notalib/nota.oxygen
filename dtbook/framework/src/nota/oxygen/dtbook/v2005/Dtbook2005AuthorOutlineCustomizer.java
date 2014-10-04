package nota.oxygen.dtbook.v2005;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.structure.AuthorOutlineCustomizer;
import ro.sync.ecss.extensions.api.structure.RenderingInformation;

/**
 * Configures outline functionality for dtbook2005
 * @author OHA
 */
public class Dtbook2005AuthorOutlineCustomizer extends AuthorOutlineCustomizer {

	@Override
	public void customizeRenderingInformation(RenderingInformation renderInfo) {
		// TODO Auto-generated method stub
		super.customizeRenderingInformation(renderInfo);
		AuthorNode node = renderInfo.getNode();
		if (node.getType()==AuthorNode.NODE_TYPE_ELEMENT)
		{
			AuthorElement elem = (AuthorElement)node;
			if (hasLocalName(elem, "level"))
			{
				renderInfo.setAdditionalRenderedText(getChildElementText(elem, "hd"));
			}
			if (hasLocalName(elem, "level1"))
			{
				renderInfo.setAdditionalRenderedText(getChildElementText(elem, "h1"));
			}
			if (hasLocalName(elem, "level2"))
			{
				renderInfo.setAdditionalRenderedText(getChildElementText(elem, "h2"));
			}
			if (hasLocalName(elem, "level3"))
			{
				renderInfo.setAdditionalRenderedText(getChildElementText(elem, "h3"));
			}
			if (hasLocalName(elem, "level4"))
			{
				renderInfo.setAdditionalRenderedText(getChildElementText(elem, "h4"));
			}
			if (hasLocalName(elem, "level5"))
			{
				renderInfo.setAdditionalRenderedText(getChildElementText(elem, "h5"));
			}
			if (hasLocalName(elem, "level6"))
			{
				renderInfo.setAdditionalRenderedText(getChildElementText(elem, "h6"));
			}
			if (hasLocalName(elem, "frontmatter"))
			{
				renderInfo.setAdditionalRenderedText("");
			}
			if (hasLocalName(elem, "bodymatter"))
			{
				renderInfo.setAdditionalRenderedText("");
			}
			if (hasLocalName(elem, "readmatter"))
			{
				renderInfo.setAdditionalRenderedText("");
			}
			if (hasLocalName(elem, "imggroup"))
			{
				renderInfo.setAdditionalRenderedText(getChildElementText(elem, "caption"));
			}
			if (hasLocalName(elem, "img"))
			{
				renderInfo.setAdditionalRenderedAttributeValue(
						"\"".concat(elem.getAttribute("src").getValue()).concat("\""));
				renderInfo.setAdditionalRenderedText(elem.getAttribute("alt").getValue());
			}
			if (hasLocalName(elem, "table"))
			{
				renderInfo.setAdditionalRenderedText(getChildElementText(elem, "caption"));
			}
		}
	}
	
	protected String getChildElementText(AuthorElement elem, String childLocalName)
	{
		AuthorElement[] children = elem.getElementsByLocalName(childLocalName);
		if (children.length>0)
		{
			try {
				return children[0].getTextContent();
			} catch (BadLocationException e) {
				return "";
			}
		}
		return "";
	}
	
	protected boolean hasLocalName(AuthorNode node, String localName)
	{
		if (node.getName()==localName) return true;
		if (node.getName().endsWith(":".concat(localName))) return true;
		return false;
	}

	@Override
	public boolean ignoreNode(AuthorNode node) {
		if (hasLocalName(node, "head")) return true;
		return super.ignoreNode(node);
	}
	
}
