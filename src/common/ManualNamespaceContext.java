package common;

import java.util.*;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

public class ManualNamespaceContext implements NamespaceContext {

	Map<String,String> _prefixNSMap = new HashMap<String,String>(); 
	
	public ManualNamespaceContext()
	{
		
	}
	
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
		Iterator prefixes = getPrefixes(namespaceURI);
		if (prefixes.hasNext()) return (String)prefixes.next();
		return null;
	}
	
	@Override
	public Iterator getPrefixes(String namespaceURI) {
		Iterator<String> prefixes = _prefixNSMap.keySet().iterator();
		Set<String> res = new HashSet<String>();
		while (prefixes.hasNext()){
			String p = prefixes.next();
			if (_prefixNSMap.get(p)==namespaceURI) res.add(p);
		}
		return res.iterator();
	}
	
	public void mapPrefix(String prefix, String namespace)
	{
		if (namespace==null)
		{
			if (_prefixNSMap.containsKey(namespace)) _prefixNSMap.remove(namespace);
		}
		else
		{
			_prefixNSMap.put(namespace, prefix);
		}
	}

}
