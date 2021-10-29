package dk.nota.xml;

import java.util.HashMap;
import java.util.concurrent.Callable;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;

public abstract class AbstractXsltTransformation implements Callable<XdmValue> {
	
	protected HashMap<QName,XdmValue> parameters =
			new HashMap<QName,XdmValue>();
	protected RelayingTransformationListener relayingListener;
	protected Xslt30Transformer transformer;
	
	public AbstractXsltTransformation(Xslt30Transformer transformer) {
		this.transformer = transformer;
		relayingListener = new RelayingTransformationListener();
		transformer.setErrorListener(relayingListener);
		transformer.setMessageListener(relayingListener);
	}
	
	public void addListener(TransformationListener listener) {
		relayingListener.addListener(listener);
	}
	
	public void addParameter(String name, XdmValue value) {
		parameters.put(new QName(name), value);
	}

	@Override
	public XdmValue call() throws SaxonApiException {
		transformer.setStylesheetParameters(parameters);
		return transformer.callTemplate(new QName("OUTPUT"));
	}

}
