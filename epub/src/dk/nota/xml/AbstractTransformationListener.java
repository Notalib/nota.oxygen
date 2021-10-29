package dk.nota.xml;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import net.sf.saxon.s9api.XdmNode;

public abstract class AbstractTransformationListener
		implements TransformationListener {

	@Override
	public void error(TransformerException exception)
			throws TransformerException {
	}

	@Override
	public void fatalError(TransformerException exception)
			throws TransformerException {
	}

	@Override
	public void warning(TransformerException exception)
			throws TransformerException {
	}

	@Override
	public void message(XdmNode message, boolean terminate,
			SourceLocator sourceLocator) {
	}
	
}
