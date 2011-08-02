package nota.oxygen.common.dtbook.v2005;

import java.util.List;

import ro.sync.contentcompletion.xml.CIAttribute;
import ro.sync.contentcompletion.xml.CIElement;
import ro.sync.contentcompletion.xml.CIValue;
import ro.sync.contentcompletion.xml.Context;
import ro.sync.contentcompletion.xml.SchemaManagerFilter;
import ro.sync.contentcompletion.xml.WhatAttributesCanGoHereContext;
import ro.sync.contentcompletion.xml.WhatElementsCanGoHereContext;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;

/**
 * Schema Manager Filter for dtbook v2005
 * @author OHA
 *
 */
public class Dtbook2005SchemaManagerFilter implements SchemaManagerFilter {

	@Override
	public List<CIValue> filterAttributeValues(List<CIValue> values,
			WhatPossibleValuesHasAttributeContext context) {
		String attrName = context.getAttributeName();
		String elemName = context.getParentElement().getQName();
		if (elemName.indexOf(':')!=-1) elemName = elemName.substring(elemName.indexOf(':')+1);
//		if ("page".equals(attrName))
//		{
//			if ("pagenum".equals(elemName))
//			{
//				values.clear();
//				values.add(new CIValue("normal"));
//				values.add(new CIValue("front"));
//			}
//					
//		}
		return values;
	}

	@Override
	public List<CIAttribute> filterAttributes(List<CIAttribute> attributes,
			WhatAttributesCanGoHereContext context) {
		return attributes;
	}

	@Override
	public List<CIValue> filterElementValues(List<CIValue> elementValues,
			Context context) {
		return elementValues;
	}

	@Override
	public List<CIElement> filterElements(List<CIElement> elements,
			WhatElementsCanGoHereContext context) {
		return elements;
	}

	@Override
	public String getDescription() {
		return "Schema Manager Filter for dtbook v2005";
	}

}
