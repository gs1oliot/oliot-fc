/**
 *	Copyright (C) 2014 KAIST
 *	@author Janggwan Im <limg00n@kaist.ac.kr> 
 *
 */

package org.fosstrak.ale.server.readers.llrp;

import java.util.Properties;

import kr.ac.kaist.resl.ltk.net.LLRPAcceptor;
import kr.ac.kaist.resl.ltk.net.LLRPConnection;
import kr.ac.kaist.resl.ltk.net.LLRPConnectionAttemptFailedException;
import org.apache.log4j.Logger;

import org.springframework.stereotype.Service;

@Service("logicalReaderAcceptor")
public class PhysicalReaderAcceptor {
	
	/** logger. */
	private static final Logger log = Logger.getLogger(PhysicalReaderAcceptor.class);
	
	public static ReaderInitiatedConnectionHashMap mapIdAndReaderInitiatedConnectionEntry = new ReaderInitiatedConnectionHashMap();
	
	/** ORANGE: the path to the properties file for the LLRPAdaptor. */
	private static final String LLRPADAPTOR_CONFIG_FILE = "/LLRPAdaptorConfig.properties";
	
	/**
	 * default listen port for llrp reader
	 */
	private int listenPort;
	
	/** 
	 * connection object of this reader
	 */
	LLRPConnection connection;
	
	/**
	 * A reference to an object which plays a role of both LLRPIoHandlerAdapter and LLRPEndpoint
	 */
	ReaderInitiatedLLRPIoHandlerAdapter readerInitiatedHandler;
	
	public static long recentReportTime;
	
	public PhysicalReaderAcceptor() {
		log.debug("LogicalReaderAcceptor init");
		listenPort = getAcceptorPort();
		
		readerInitiatedHandler = new ReaderInitiatedLLRPIoHandlerAdapter();
		connection = new LLRPAcceptor(readerInitiatedHandler, readerInitiatedHandler);
		readerInitiatedHandler.setConnection(connection);
		readerInitiatedHandler.setKeepAliveForward(true);
		
		log.debug("LogicalReaderAcceptor bind to port "+listenPort);
		this.bind(listenPort);
	}
	private int getAcceptorPort() {
		int port;
		Properties props = new Properties();
		try {
			props.load(LLRPAdaptor.class.getResourceAsStream(LLRPADAPTOR_CONFIG_FILE));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String acceptorPort = props.getProperty("readerAcceptorPort");
		port = Integer.parseInt(acceptorPort);
		return port;
	}
	
	
	public void bind(int port) {
		
		((LLRPAcceptor) connection).setPort(port);
		try {
			// wait for connection without timeout
			((LLRPAcceptor) connection).bind();
			
			// wait for connection with timeout
			//((LLRPAcceptor) connection).bind(10000);
			
		} catch (LLRPConnectionAttemptFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	
	

}
