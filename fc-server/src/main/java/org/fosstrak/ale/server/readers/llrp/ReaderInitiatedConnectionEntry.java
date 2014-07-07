/**
 *	Copyright (C) 2014 KAIST
 *	@author Janggwan Im <limg00n@kaist.ac.kr> 
 *
 */

package org.fosstrak.ale.server.readers.llrp;

import java.util.ArrayList;
import java.util.List;

import kr.ac.kaist.resl.ltk.net.LLRPEndpoint;

import org.apache.mina.core.session.IoSession;

public class ReaderInitiatedConnectionEntry {
		private String physicalReaderId;
		private String identificationType;		// "MAC" or "EPC"
		private String readerAddr;
		private int port;
		
		private ReaderInitiatedLLRPIoHandlerAdapter handler;
		
		private MultipleLLRPEndpoint endpoint = new MultipleLLRPEndpoint();
		private IoSession ioSession;
		private List<LLRPAdaptor> adaptors = new ArrayList<LLRPAdaptor>();
		
		public ReaderInitiatedConnectionEntry(String id, String idType, String addr, int port, ReaderInitiatedLLRPIoHandlerAdapter handler, LLRPEndpoint conn, IoSession ioSession) {
			this.setPhysicalReaderId(id);
			this.setIdentificationType(idType);
			this.setReaderAddr(addr);
			this.setPort(port);
			this.setHandler(handler);
			//this.setConn(conn);
			endpoint.addLLRPEndpoint(conn);
			this.setIoSession(ioSession);
		}

		public String getPhysicalReaderId() {
			return physicalReaderId;
		}

		public void setPhysicalReaderId(String readerId) {
			this.physicalReaderId = readerId;
		}

		public String getIdentificationType() {
			return identificationType;
		}

		public void setIdentificationType(String identificationType) {
			this.identificationType = identificationType;
		}

		public String getReaderAddr() {
			return readerAddr;
		}

		public void setReaderAddr(String readerAddr) {
			this.readerAddr = readerAddr;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public ReaderInitiatedLLRPIoHandlerAdapter getHandler() {
			return handler;
		}

		public void setHandler(ReaderInitiatedLLRPIoHandlerAdapter handler) {
			this.handler = handler;
		}
		
		public void checkRospecAndDefine() {
			
		}

		public List<LLRPAdaptor> getAdaptors() {
			return adaptors;
		}

		public IoSession getIoSession() {
			return ioSession;
		}

		public void setIoSession(IoSession ioSession) {
			this.ioSession = ioSession;
		}

		public MultipleLLRPEndpoint getEndpoint() {
			return endpoint;
		}
	}