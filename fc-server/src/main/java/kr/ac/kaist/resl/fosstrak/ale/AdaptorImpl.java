/*
 *  
 *  Fosstrak LLRP Commander (www.fosstrak.org)
 * 
 *  Copyright (C) 2014 KAIST
 *	@author Janggwan Im <limg00n@kaist.ac.kr>
 * 
 *  Copyright (C) 2008 ETH Zurich
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/> 
 *
 */

package kr.ac.kaist.resl.fosstrak.ale;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.fosstrak.llrp.adaptor.AsynchronousNotifiable;
import org.fosstrak.llrp.adaptor.exception.LLRPDuplicateNameException;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.adaptor.util.AsynchronousNotifiableList;

/**
 * This adaptor implements the Adaptor interface.  
 * @author sawielan
 *
 */
public class AdaptorImpl extends UnicastRemoteObject implements Adaptor {
	
	/**
	 * default serial for serialization.
	 */
	private static final long serialVersionUID = -5896254195502117705L;

	/** a map holding all the readers contained in this adaptor. */
	protected Map<String, ReaderImpl> readers = new HashMap<String, ReaderImpl> ();
	
	/** a list with all the receivers of asynchronous messages. */
	private AsynchronousNotifiableList toNotify = new AsynchronousNotifiableList();
	
	/** the name of this adaptor. */
	protected String adaptorName = null;
	
	private AdaptorManagement adaptorManagement = null;
	
	/**
	 * Constructor for a adaptor. 
	 * @param adaptorName the name of this adaptor.
	 * @throws RemoteException whenever there is an rmi exception.
	 */
	public AdaptorImpl(String adaptorName) throws RemoteException {
		super();
		this.adaptorName = adaptorName;
	}
	
	public boolean containsReader(String readerName) throws RemoteException {
		return readers.containsKey(readerName);
	}
	
	public void define(String readerName, 
			String readerAddress, 
			boolean clientInitiatedConnection,
			boolean connectImmediately)
			throws RemoteException, LLRPRuntimeException {
		
		if (containsReader(readerName)) {
			throw new LLRPDuplicateNameException(readerName, "Reader '" + readerName + "' already exists.");
		}
		
		ReaderImpl reader = new ReaderImpl(this, readerName, readerAddress);
		reader.setClientInitiated(clientInitiatedConnection);
		reader.setConnectImmediate(connectImmediately);
		
		// run the connection setup only when requested.
		if (connectImmediately) {
			reader.connect(clientInitiatedConnection);
		}
		readers.put(readerName, reader);
		commit();
	}

	public void define(String readerName, 
			String readerAddress,
			int port, 
			boolean clientInitiatedConnection,
			boolean connectImmediately) 
		throws RemoteException, LLRPRuntimeException {
		
		if (containsReader(readerName)) {
			throw new LLRPDuplicateNameException(readerName, "Reader '" + readerName + "' already exists.");
		}
		
		ReaderImpl reader = new ReaderImpl(this, readerName, readerAddress, port);	
		reader.setClientInitiated(clientInitiatedConnection);
		reader.setConnectImmediate(connectImmediately);
		
		// run the connection setup only when requested.
		if (connectImmediately) {
			reader.connect(clientInitiatedConnection);
		}
		readers.put(readerName, reader);
		commit();
	}

	public String getAdaptorName() throws RemoteException {
		return adaptorName;
	}

	public List<String> getReaderNames() throws RemoteException {
		// we create a copy, no leakage!
		List<String> readerNames = new LinkedList<String> ();
		
		for (String name : readers.keySet()) {
			readerNames.add(name);
		}
		return readerNames;
	}

	public void undefine(String readerName) throws RemoteException,
			LLRPRuntimeException {
		
		if (!containsReader(readerName)) {
			throw new LLRPRuntimeException("Reader '" + readerName + "' does not exist.");
		}
		Reader reader = readers.remove(readerName);
		
		// do not disconnect in case of reader-initiated connection
		//reader.disconnect();
		// TODO: by janggwan
		// how about client-initiated connection?

		commit();
	}
		
	public void undefineAll() throws RemoteException, LLRPRuntimeException {
		for (String readerName : getReaderNames()) {
			try {
				undefine(readerName);
			} catch (LLRPRuntimeException e) {
				// remove the reader from the list nevertheless
				readers.remove(readerName);
				
				// notify the error
				errorCallback(e, readerName);
			}
		}
		commit();
	}
	
	
	public void disconnectAll() throws RemoteException, LLRPRuntimeException {
		for (String readerName : getReaderNames()) {
			readers.get(readerName).disconnect();
		}
	}
	
	public void sendLLRPMessage(String readerName, byte[] message)
			throws RemoteException, LLRPRuntimeException {
		
		if (!containsReader(readerName)) {
			throw new LLRPRuntimeException("Reader '" + readerName + "' does not exist.");
		}
		
		readers.get(readerName).send(message);
	}

	public void sendLLRPMessageToAllReaders(byte[] message)
			throws RemoteException, LLRPRuntimeException {
		
		for (Reader reader : readers.values()) {
			reader.send(message);
		}
		
	}

	
	public void registerForAsynchronous(AsynchronousNotifiable receiver)
			throws RemoteException {
		
		toNotify.add(receiver);
	}

	
	public void messageReceivedCallback(byte[] message, String readerName)
			throws RemoteException {
		
		toNotify.notify(message, readerName);
	}

	
	public void deregisterFromAsynchronous(AsynchronousNotifiable receiver)
			throws RemoteException {
		
		toNotify.remove(receiver);
	}

	
	public void errorCallback(LLRPRuntimeException e, String readerName)
		throws RemoteException {
		
		toNotify.notifyError(e, readerName);	
	}

	
	public Reader getReader(String readerName) throws RemoteException {
		return readers.get(readerName);
	}

	
	public void setAdaptorName(String adaptorName) throws RemoteException {
		this.adaptorName = adaptorName;
	}

	private void commit() {
		if (adaptorManagement != null) {
			adaptorManagement.commit();
		}
	}
	
	public void setAdaptorManagement(AdaptorManagement management) {
		this.adaptorManagement = management;
	}

}
