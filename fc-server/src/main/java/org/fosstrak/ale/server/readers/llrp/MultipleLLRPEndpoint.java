/**
 *	Copyright (C) 2014 KAIST
 *	@author Janggwan Im <limg00n@kaist.ac.kr> 
 *
 */

package org.fosstrak.ale.server.readers.llrp;

import java.util.ArrayList;
import java.util.List;

import org.llrp.ltk.types.LLRPMessage;

import kr.ac.kaist.resl.ltk.net.LLRPEndpoint;

public class MultipleLLRPEndpoint implements LLRPEndpoint {
	
	private List<LLRPEndpoint> listEndpoint = new ArrayList<LLRPEndpoint>();
	/**
	 * add LLRP endpoint to LLRPEndpoint list. 
	 * If there already exists LLRPEndpoint to add, do nothing
	 * @param e
	 * @return
	 */
	public boolean addLLRPEndpoint(LLRPEndpoint e) {
		for(LLRPEndpoint entry : listEndpoint) {
			if(entry.equals(e)) {
				return false;
			}
		}
		return listEndpoint.add(e);
	}
	
	public boolean removeLLRPEndpoint(LLRPEndpoint e) {
		for(LLRPEndpoint entry : listEndpoint) {
			if(entry.equals(e)) {
				listEndpoint.remove(entry);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void messageReceived(LLRPMessage message) {
		for(LLRPEndpoint endpoint : listEndpoint) {
			endpoint.messageReceived(message);
		}
	}

	@Override
	public void errorOccured(String message) {
		for(LLRPEndpoint endpoint : listEndpoint) {
			endpoint.errorOccured(message);
		}
	}

}
