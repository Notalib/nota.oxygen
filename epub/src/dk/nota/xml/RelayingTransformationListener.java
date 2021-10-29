package dk.nota.xml;

import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import net.sf.saxon.s9api.XdmNode;

public class RelayingTransformationListener implements TransformationListener {
	
	private Set<TransformationListener> listeners;
	
	public RelayingTransformationListener() {
		listeners = new HashSet<TransformationListener>();
	}
	
	public RelayingTransformationListener(Set<TransformationListener> listeners) {
		this.listeners = listeners;
	}
	
	public void addListener(TransformationListener listener) {
		listeners.add(listener);
	}

	@Override
	public void error(TransformerException exception)
			throws TransformerException {
		for (TransformationListener listener : listeners)
			listener.error(exception);
	}

	@Override
	public void fatalError(TransformerException exception)
			throws TransformerException {
		for (TransformationListener listener : listeners)
			listener.fatalError(exception);
	}
	
	public Set<TransformationListener> getListeners() {
		return listeners;
	}

	@Override
	public void warning(TransformerException exception)
			throws TransformerException {
		for (TransformationListener listener : listeners)
			listener.warning(exception);
		
	}

	@Override
	public void message(XdmNode message, boolean terminate,
			SourceLocator sourceLocator) {
		for (TransformationListener listener : listeners)
			listener.message(message, terminate, sourceLocator);
	}
	
	public void removeListener(TransformationListener listener) {
		listeners.remove(listener);
	}
	
	public void resetListeners() {
		listeners.clear();
	}

}
