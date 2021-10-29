package dk.nota.xml;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.s9api.Processor;

public final class ProcessorProvider {
	
	private static final Processor processor;
	
	static {
		processor = new Processor(Configuration.makeLicensedConfiguration(
				ProcessorProvider.class.getClassLoader(),
				"com.saxonica.config.EnterpriseConfiguration"));
		processor.setConfigurationProperty(FeatureKeys.LINE_NUMBERING, true);
	}
	
	private ProcessorProvider() {
		
	}
	
	public static final Processor getProcessor() {
		return processor;
	}

}
