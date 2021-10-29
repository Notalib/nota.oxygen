package dk.nota.xml;

import javax.xml.transform.ErrorListener;

import net.sf.saxon.s9api.MessageListener;

public interface TransformationListener extends ErrorListener, MessageListener {

}
