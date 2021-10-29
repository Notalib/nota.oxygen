package dk.nota.xml;

public class XmlAccessProvider {
	
	private static XmlAccess epubXmlAccess = new XmlAccess(ProcessorProvider
			.getProcessor());
	
	private XmlAccessProvider() {
		
	}
	
	public static XmlAccess getXmlAccess() {
		return epubXmlAccess;
}

}
