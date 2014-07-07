/**
 *	Copyright (C) 2014 KAIST
 *	@author Janggwan Im <limg00n@kaist.ac.kr> 
 *
 */

package org.fosstrak.ale.server.readers.llrp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Properties;

import kr.ac.kaist.resl.fosstrak.ale.ReaderImpl;
import kr.ac.kaist.resl.ltk.generated.LLRPMessageFactory;
import kr.ac.kaist.resl.ltk.generated.messages.GET_READER_CAPABILITIES_RESPONSE;
import kr.ac.kaist.resl.ltk.generated.messages.GET_READER_CONFIG;
import kr.ac.kaist.resl.ltk.generated.messages.GET_READER_CONFIG_RESPONSE;
import kr.ac.kaist.resl.ltk.generated.messages.KEEPALIVE;
import kr.ac.kaist.resl.ltk.generated.messages.KEEPALIVE_ACK;
import kr.ac.kaist.resl.ltk.generated.messages.READER_EVENT_NOTIFICATION;
import kr.ac.kaist.resl.ltk.generated.messages.SET_READER_CONFIG_RESPONSE;
import kr.ac.kaist.resl.ltk.generated.parameters.ConnectionAttemptEvent;
import kr.ac.kaist.resl.ltk.generated.parameters.ReaderEventNotificationData;
import kr.ac.kaist.resl.ltk.net.LLRPEndpoint;
import kr.ac.kaist.resl.ltk.net.LLRPIoHandlerAdapterImpl;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.fosstrak.ale.server.ALEApplicationContext;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.llrp.ltk.types.LLRPMessage;

public class ReaderInitiatedLLRPIoHandlerAdapter extends LLRPIoHandlerAdapterImpl implements LLRPEndpoint {//LLRPIoHandlerAdapterImpl implements LLRPEndpoint {
	/** logger. */
	private static final Logger log = Logger.getLogger(ReaderInitiatedLLRPIoHandlerAdapter.class);
	
	/** the path to the properties file for the LLRPAdaptor. */
	private static final String LLRPADAPTOR_CONFIG_FILE = "/LLRPAdaptorConfig.properties";
	
	/**
	 * to define and undefine ReaderImpl object
	 */
	private LLRPManager manager = null;
	
	public LLRPManager getManager() {
		if(manager == null) {
			manager = ALEApplicationContext.getBean(LLRPManager.class);
		}
		return manager;
	}
	
	@Override
	public void messageReceived(IoSession arg0, Object arg1)
			throws Exception {
		
		InetSocketAddress inetAddr = (InetSocketAddress)arg0.getRemoteAddress();
		String addr = inetAddr.getHostString();
		int port = inetAddr.getPort();
		
		LLRPMessage llrpMessage = (LLRPMessage) arg1;
		log.info("message "+arg1.getClass()+" received in session "+arg0);
		if (log.isDebugEnabled()) {
			if(!(llrpMessage instanceof KEEPALIVE)) log.debug(llrpMessage.toXMLString());
		}
		
		
		
		if(arg1 instanceof READER_EVENT_NOTIFICATION) {

			ReaderEventNotificationData data = null;
			if((data = ((READER_EVENT_NOTIFICATION)arg1).getReaderEventNotificationData()) != null) {
				ConnectionAttemptEvent connectionAttemptEvent = null;
				if((connectionAttemptEvent = data.getConnectionAttemptEvent()) != null) {
					//System.out.println(connectionAttemptEvent.getStatus().getName(ConnectionAttemptStatusType.Success));
					log.debug("READER_EVENT_NOTIFICATION received. send GET_READER_CONFIG");
					GET_READER_CONFIG msg = (GET_READER_CONFIG) loadXMLLLRPMessage(PhysicalReaderAcceptor.class.getResourceAsStream("/llrp/GET_READER_CONFIG.xml"));
					arg0.write(msg);
					return;
				}
			}
			
		} else if(arg1 instanceof GET_READER_CAPABILITIES_RESPONSE) {
			
			// do nothing
			
		} else if(arg1 instanceof GET_READER_CONFIG_RESPONSE) {
		
			GET_READER_CONFIG_RESPONSE resp = ((GET_READER_CONFIG_RESPONSE)arg1);
			
			if(resp.getLLRPStatus() != null) {
				if(resp.getLLRPStatus().getStatusCode() != null) {
					if(resp.getLLRPStatus().getStatusCode().toString().equals("M_Success")) {
						
						if(resp.getIdentification() != null) {
							String idType = null;
							String readerId = null; 
							
							if(resp.getIdentification().getIDType() != null) {
								idType = resp.getIdentification().getIDType().toString();
							}
							if(resp.getIdentification().getReaderID() != null) {
								readerId = resp.getIdentification().getReaderID().toString();
							}
							
							if(idType != null && readerId != null) {

								String physicalReaderId = readerId;
								
								log.debug("reader (id: "+readerId+", ip: "+addr+" port: "+port+") is connected");

								// create reader-initiated connection and store it in the map
								ReaderInitiatedConnectionEntry entry =  new ReaderInitiatedConnectionEntry(physicalReaderId, idType, addr, port, this, this, arg0);
								PhysicalReaderAcceptor.mapIdAndReaderInitiatedConnectionEntry.put(physicalReaderId, entry);
								
								// if there exists ReaderImpl (internal object which is 1:1 correspondence with LLRPAdaptor/LRSpec) which is related to this reader,
								// set this reader's endpoint to ReaderImpl,
								// and ReaderImpl's connection to this reader's connection
								// because there is 1:N relationship between this reader and ReaderImpl, endpoints need to be multiplexed 
								for(String definedReaderName : getManager().getAdaptor().getReaderNames()) {
									log.debug("checking this reader's ReaderImpl object");
									// definedReaderName has the form of physicalReaderId+"___"+AntennaId
									// for example, definedReaderName is 000011112222333344445555___1,2
									
									// check whether the name starts with physicalReaderId
									// if so, set connection for the reader and endpoint (reader) for the connection 
									if(definedReaderName.startsWith(physicalReaderId)) {
										log.debug("this reader's ReaderImpl object: "+definedReaderName);
										ReaderImpl reader = (ReaderImpl)getManager().getAdaptor().getReader(definedReaderName);
										//reader.setConnection(getConnection());
										reader.setIoSession(arg0);
										
										// we need to multiplex one reader connection to multiple ReaderImpls 
										// because there can be multiple ReaderImpls due to different antennas
										entry.getEndpoint().addLLRPEndpoint(reader);
									}
								}
								
								// restore previously-defined LLRP Specs
							
								try {
									String[] llrpSpecList = getLlrpSpecList();
									
									
									for(String llrpSpecToDefine : llrpSpecList) {
										log.info("define "+llrpSpecToDefine);
										if(llrpSpecToDefine.equals("")) continue;
										LLRPMessage msg = loadXMLLLRPMessage(PhysicalReaderAcceptor.class.getResourceAsStream("/llrp/"+llrpSpecToDefine));
										arg0.write(msg);
										Thread.sleep(500);
									}
									/*
									
									SET_READER_CONFIG msgSetReaderConfigKeepalive = (SET_READER_CONFIG) loadXMLLLRPMessage(PhysicalReaderAcceptor.class.getResourceAsStream("/llrp/SET_READER_CONFIG_KEEPALIVE.xml"));
									//arg0.write(msgSetReaderConfigKeepalive); 	// set keep alive message
									
									Thread.sleep(500);
									
									DELETE_ROSPEC msgDeleteRospec = (DELETE_ROSPEC) loadXMLLLRPMessage(PhysicalReaderAcceptor.class.getResourceAsStream("/llrp/DELETE_ROSPEC.xml"));
									arg0.write(msgDeleteRospec); 	// delete ROSpec whose message ID is 1
									
									Thread.sleep(500);
									
									DELETE_ROSPEC msgDeleteRospec2 = (DELETE_ROSPEC) loadXMLLLRPMessage(PhysicalReaderAcceptor.class.getResourceAsStream("/llrp/DELETE_ROSPEC_2.xml"));
									arg0.write(msgDeleteRospec2);	// delete ROSpec whose message ID is 1
									
									Thread.sleep(500);
									
									LLRPMessage msgAddRospec = loadXMLLLRPMessage(PhysicalReaderAcceptor.class.getResourceAsStream("/llrp/ADD_ROSPEC.xml"));
									arg0.write(msgAddRospec);
									
									Thread.sleep(500);
									
									LLRPMessage msgAddRospecGpi = loadXMLLLRPMessage(PhysicalReaderAcceptor.class.getResourceAsStream("/llrp/ADD_ROSPEC_GPIEvent.xml"));
									arg0.write(msgAddRospecGpi);
									
									Thread.sleep(500);
									
									DELETE_ACCESSSPEC msgDelAccessspec = new DELETE_ACCESSSPEC();
									msgDelAccessspec.setAccessSpecID(new UnsignedInteger(3));
									arg0.write(msgDelAccessspec);
									
									Thread.sleep(500);
									
									LLRPMessage msgAddAccessspec = loadXMLLLRPMessage(PhysicalReaderAcceptor.class.getResourceAsStream("/llrp/ADD_ACCESSSPEC3.xml"));
									arg0.write(msgAddAccessspec);
									
									Thread.sleep(500);
									
									ENABLE_ACCESSSPEC msgEnableAccessSpec = new ENABLE_ACCESSSPEC();
									msgEnableAccessSpec.setAccessSpecID(new UnsignedInteger(3));
									arg0.write(msgEnableAccessSpec);
									
									Thread.sleep(500);
									
									LLRPMessage msgEnableRospec = loadXMLLLRPMessage(PhysicalReaderAcceptor.class.getResourceAsStream("/llrp/ENABLE_ROSPEC.xml"));
									arg0.write(msgEnableRospec);
									
									Thread.sleep(500);
									

									ENABLE_ROSPEC msgEnableRospec2 = (ENABLE_ROSPEC) loadXMLLLRPMessage(PhysicalReaderAcceptor.class.getResourceAsStream("/llrp/ENABLE_ROSPEC.xml"));
									msgEnableRospec2.setROSpecID(new UnsignedInteger(2));
									arg0.write(msgEnableRospec2);
									
									Thread.sleep(500);
									
									//File fileStartRospec = convertInputStreamToFile(getClass().getResourceAsStream("/llrp/START_ROSPEC.xml"));
									//LLRPMessage msgStartRospec = Util.loadXMLLLRPMessage(fileStartRospec);
									//getConnection().send(msgStartRospec);
									
									//Thread.sleep(500);
									*/
									
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (JDOMException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InvalidLLRPMessageException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							
								// restore LLRP specs which were defined in LLRPAdaptor
								for(String definedReaderName : getManager().getAdaptor().getReaderNames()) {
									if(definedReaderName.startsWith(physicalReaderId)) {
										ReaderImpl reader = (ReaderImpl)getManager().getAdaptor().getReader(definedReaderName);
										
										reader.getLlrpAdaptor().restoreLLRPSpecs();
									}
								}
								
								
							}
						}
					} else {
						throw new Exception("GET_READER_CONFIG_RESPONSE is not successful");
					}
				}
			}
		}
		else if (arg1 instanceof KEEPALIVE) {
			log.debug("KEEPALIVE message received. write KEEPALIVE_ACK back");
			KEEPALIVE_ACK kack = new KEEPALIVE_ACK();
			arg0.write(kack);
		} else if(arg1 instanceof SET_READER_CONFIG_RESPONSE) {
			log.debug("SET_READER_CONFIG_RESPONSE is received: "+((SET_READER_CONFIG_RESPONSE)arg1).toXMLString());
		}
				
	
		
		// instead of super.messageReceived(arg0, arg1);
		MultipleLLRPEndpoint endpoint = PhysicalReaderAcceptor.mapIdAndReaderInitiatedConnectionEntry.getMultipleLLRPEndpointByIpPort(addr, port);
		if(endpoint != null)
			endpoint.messageReceived(llrpMessage);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
		
		log.debug("Reader-initiated connection session opened ("+((InetSocketAddress)session.getRemoteAddress()).getHostString()+":"+((InetSocketAddress)session.getRemoteAddress()).getPort()+")");

		// send GET_READER_CONFIG
		//File file = convertInputStreamToFile(getClass().getResourceAsStream("/llrp/GET_READER_CONFIG.xml"));
		//GET_READER_CONFIG msg = (GET_READER_CONFIG) Util.loadXMLLLRPMessage(file);
		
	}
	
	public LLRPMessage loadXMLLLRPMessage(InputStream is) throws FileNotFoundException, IOException, JDOMException, InvalidLLRPMessageException {
		
		Document doc = new org.jdom.input.SAXBuilder().build(new
				InputStreamReader(is));
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		log.debug("Loaded XML Message: " + outputter.outputString(doc));
		LLRPMessage message = LLRPMessageFactory.createLLRPMessage(doc);
	
		return message;
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		if(session != null && session.getRemoteAddress() != null) {
			log.debug("exception in the connection ("+((InetSocketAddress)session.getRemoteAddress()).getHostString()+":"+((InetSocketAddress)session.getRemoteAddress()).getPort()+")");
		} else {
			log.debug("exception in the connection but session is null");
		}
		
		
		//InetSocketAddress addr = (InetSocketAddress)session.getRemoteAddress();
		
		cause.printStackTrace();
		//getConnectionAttemptEventQueue().clear();
		//getSynMessageQueue().clear();
		session.close(true);//.close();
		
		super.exceptionCaught(session, cause);
	}

	@Override
	public void errorOccured(String arg0) {
		log.debug("error occured in default handler : "+arg0);
		
	}

	@Override
	public void messageReceived(LLRPMessage arg0) {
		log.debug("message received in default handler : "+arg0);
	}
	
	private String[] getLlrpSpecList() {
		Properties props = new Properties();
		try {
			props.load(LLRPAdaptor.class.getResourceAsStream(LLRPADAPTOR_CONFIG_FILE));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String llrpSpecFiles = props.getProperty("llrpSpecFiles");
		llrpSpecFiles = llrpSpecFiles.replaceAll("\\s","");
		String[] specFilenames = llrpSpecFiles.split(",");
		return specFilenames;
	}
	
}