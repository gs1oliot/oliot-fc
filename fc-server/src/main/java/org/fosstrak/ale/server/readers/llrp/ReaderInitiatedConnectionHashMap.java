/**
 *	Copyright (C) 2014 KAIST
 *	@author Janggwan Im <limg00n@kaist.ac.kr> 
 *
 */

package org.fosstrak.ale.server.readers.llrp;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ReaderInitiatedConnectionHashMap {
	
	private ConcurrentHashMap<String, ReaderInitiatedConnectionEntry> mapConnection = new ConcurrentHashMap<String, ReaderInitiatedConnectionEntry>();
	

	public ReaderInitiatedConnectionEntry get(Object key) {
		String toUpperCase = ((String) key).toUpperCase();
		return mapConnection.get(toUpperCase);
	}

	public ReaderInitiatedConnectionEntry put(String key,
			ReaderInitiatedConnectionEntry value) {
		String toUpperCase = key.toUpperCase();
		return mapConnection.put(toUpperCase, value);
	}

	public boolean containsKey(Object key) {
		String toUpperCase = ((String) key).toUpperCase();
		return mapConnection.containsKey(toUpperCase);
	}

	public ReaderInitiatedConnectionEntry remove(Object key) {
		String toUpperCase = ((String) key).toUpperCase();
		return mapConnection.remove(toUpperCase);
	}

	public Set<String> keySet() {
		Set<String> toReturn = new HashSet<String>();
		for (Enumeration<String> e = mapConnection.keys(); e.hasMoreElements();) {
			toReturn.add(e.nextElement().toUpperCase());
		}
		return toReturn;
	}
	
	public String findPhysicalReaderIdByIpPort(String addr, int port) {
		for(ReaderInitiatedConnectionEntry e : mapConnection.values()) {
			if(e.getReaderAddr().equalsIgnoreCase(addr) && e.getPort() == port) {
				return e.getPhysicalReaderId();
			}
		}
		return null;
	}
	
	public MultipleLLRPEndpoint getMultipleLLRPEndpointByIpPort(String addr, int port) {
		for(ReaderInitiatedConnectionEntry e : mapConnection.values()) {
			if(e.getReaderAddr().equalsIgnoreCase(addr) && e.getPort() == port) {
				return e.getEndpoint();
			}
		}
		return null;
	}
	
}
