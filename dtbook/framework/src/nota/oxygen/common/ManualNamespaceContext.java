package nota.oxygen.common;

import java.util.*;

import javax.xml.namespace.NamespaceContext;

/**
 * {@link NamespaceContext} based on a {@link Map}, that the user manually adds prefix/name-space pairs to
 * @author OHA
 */
public class ManualNamespaceContext implements NamespaceContext {

	Map<String,String> _prefixNSMap = new HashMap<String,String>(); 

	/**
	 * Creates a {@link ManualNamespaceContext} initialized with prefix/name-space pairs from a given {@link Map}
	 * @param map	The given {@link Map}, mapping prefixes to name-spaces
	 */
	public ManualNamespaceContext(Map<String,String> map)
	{
		Iterator<String> itr = map.keySet().iterator();
		while (itr.hasNext())
		{
			String prefix = itr.next();
			mapPrefix(prefix, map.get(prefix));
		}
	}
	
	@Override
	public String getNamespaceURI(String prefix) {
		if (_prefixNSMap.containsKey(prefix)) return _prefixNSMap.get(prefix);
		return null;
	}

	@Override
	public String getPrefix(String namespaceURI) {
		Iterator<String> prefixes = getPrefixes(namespaceURI);
		if (prefixes.hasNext()) return prefixes.next();
		return null;
	}
	
	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		Iterator<String> prefixes = _prefixNSMap.keySet().iterator();
		Set<String> res = new HashSet<String>();
		while (prefixes.hasNext()){
			String p = prefixes.next();
			if (_prefixNSMap.get(p)==namespaceURI) res.add(p);
		}
		return res.iterator();
	}
	
	/**
	 * Maps a given prefix to a given name-space
	 * @param prefix		The prefix
	 * @param namespace		The name-space
	 */
	public void mapPrefix(String prefix, String namespace)
	{
		if (namespace==null)
		{
			if (_prefixNSMap.containsKey(prefix)) _prefixNSMap.remove(prefix);
		}
		else
		{
			_prefixNSMap.put(prefix, namespace);
		}
	}

}
