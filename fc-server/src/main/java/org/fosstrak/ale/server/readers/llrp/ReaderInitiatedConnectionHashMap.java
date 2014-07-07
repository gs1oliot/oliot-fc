/**
 *	Copyright (C) 2014 KAIST
 *	@author Janggwan Im <limg00n@kaist.ac.kr> 
 *
 */

package org.fosstrak.ale.server.readers.llrp;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ReaderInitiatedConnectionHashMap extends ConcurrentHashMap<String, ReaderInitiatedConnectionEntry> {

	@Override
	public ReaderInitiatedConnectionEntry get(Object key) {
		String toUpperCase = ((String) key).toUpperCase();
		return super.get(toUpperCase);
	}

	@Override
	public ReaderInitiatedConnectionEntry put(String key,
			ReaderInitiatedConnectionEntry value) {
		String toUpperCase = key.toUpperCase();
		return super.put(toUpperCase, value);
	}

	@Override
	public boolean containsKey(Object key) {
		String toUpperCase = ((String) key).toUpperCase();
		return super.containsKey(toUpperCase);
	}

	@Override
	public ReaderInitiatedConnectionEntry remove(Object key) {
		String toUpperCase = ((String) key).toUpperCase();
		return super.remove(toUpperCase);
	}

	@Override
	public Set<String> keySet() {
		Set<String> toReturn = new HashSet<String>();
		for(String key : super.keySet()) {
			toReturn.add(key.toUpperCase());
		}
		return toReturn;
	}
	
	public String findPhysicalReaderIdByIpPort(String addr, int port) {
		for(ReaderInitiatedConnectionEntry e : super.values()) {
			if(e.getReaderAddr().equalsIgnoreCase(addr) && e.getPort() == port) {
				return e.getPhysicalReaderId();
			}
		}
		return null;
	}
	
	public MultipleLLRPEndpoint getMultipleLLRPEndpointByIpPort(String addr, int port) {
		for(ReaderInitiatedConnectionEntry e : super.values()) {
			if(e.getReaderAddr().equalsIgnoreCase(addr) && e.getPort() == port) {
				return e.getEndpoint();
			}
		}
		return null;
	}
	
}
