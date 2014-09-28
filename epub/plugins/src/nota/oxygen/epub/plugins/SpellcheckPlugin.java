package nota.oxygen.epub.plugins;

import ro.sync.exml.plugin.Plugin;
import ro.sync.exml.plugin.PluginDescriptor;

public class SpellcheckPlugin extends Plugin {
	
	private static SpellcheckPlugin instance = null;

	public SpellcheckPlugin(PluginDescriptor descriptor) {
		super(descriptor);
		if (instance != null)
		{
			throw new IllegalStateException("Already instantiated !");
		}
		instance = this;
	}
    
    /**
    * Get the plugin instance.
    * 
    * @return the shared plugin instance.
    */
    public static SpellcheckPlugin getInstance() {
        return instance;
    }

}
