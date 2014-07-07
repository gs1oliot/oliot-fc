package org.fosstrak.ale.server.readers.llrp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.Assert;

import kr.ac.kaist.resl.fosstrak.ale.Reader;
import kr.ac.kaist.resl.fosstrak.ale.ReaderImpl;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.fosstrak.ale.exception.ImplementationException;
import org.fosstrak.ale.server.ALEApplicationContext;
import org.fosstrak.ale.server.Tag;
import org.fosstrak.ale.server.NotificationData;
import org.fosstrak.ale.server.CapabilitiesData;
import org.fosstrak.ale.server.ReadConfigData;
import org.fosstrak.ale.server.Tag.OpReportResult;
import org.fosstrak.ale.server.readers.BaseReader;
import org.fosstrak.ale.server.tm.SymbolicField;
import org.fosstrak.ale.server.tm.SymbolicFieldRepo;
import org.fosstrak.ale.server.util.TagHelper;
import org.fosstrak.ale.xsd.ale.epcglobal.CCCmdSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.CCOpSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.CCSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFilterListMember;
import org.fosstrak.ale.xsd.ale.epcglobal.LRSpec;
import org.fosstrak.hal.HardwareException;
import org.fosstrak.hal.Observation;
import org.fosstrak.llrp.adaptor.Constants;
/*
import org.fosstrak.llrp.adaptor.Reader;
import org.fosstrak.llrp.adaptor.ReaderImpl;
 */
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.tdt.TDTEngine;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import kr.ac.kaist.resl.ltk.generated.LLRPMessageFactory;
import kr.ac.kaist.resl.ltk.generated.enumerations.AccessReportTriggerType;
import kr.ac.kaist.resl.ltk.generated.enumerations.AccessSpecState;
import kr.ac.kaist.resl.ltk.generated.enumerations.AccessSpecStopTriggerType;
import kr.ac.kaist.resl.ltk.generated.enumerations.AirProtocols;
import kr.ac.kaist.resl.ltk.generated.enumerations.C1G2ReadResultType;
import kr.ac.kaist.resl.ltk.generated.enumerations.C1G2WriteResultType;
import kr.ac.kaist.resl.ltk.generated.interfaces.AccessCommandOpSpec;
import kr.ac.kaist.resl.ltk.generated.interfaces.AccessCommandOpSpecResult;
import kr.ac.kaist.resl.ltk.generated.interfaces.AirProtocolTagData;
import kr.ac.kaist.resl.ltk.generated.interfaces.EPCParameter;
import kr.ac.kaist.resl.ltk.generated.messages.ADD_ACCESSSPEC;
import kr.ac.kaist.resl.ltk.generated.messages.ADD_ACCESSSPEC_RESPONSE;
import kr.ac.kaist.resl.ltk.generated.messages.DELETE_ACCESSSPEC;
import kr.ac.kaist.resl.ltk.generated.messages.DELETE_ACCESSSPEC_RESPONSE;
import kr.ac.kaist.resl.ltk.generated.messages.DISABLE_ACCESSSPEC;
import kr.ac.kaist.resl.ltk.generated.messages.ENABLE_ACCESSSPEC;
import kr.ac.kaist.resl.ltk.generated.messages.ENABLE_ACCESSSPEC_RESPONSE;
import kr.ac.kaist.resl.ltk.generated.messages.GET_ACCESSSPECS;
import kr.ac.kaist.resl.ltk.generated.messages.GET_ACCESSSPECS_RESPONSE;
import kr.ac.kaist.resl.ltk.generated.messages.GET_READER_CAPABILITIES_RESPONSE;
import kr.ac.kaist.resl.ltk.generated.messages.GET_READER_CONFIG_RESPONSE;
import kr.ac.kaist.resl.ltk.generated.messages.KEEPALIVE;
import kr.ac.kaist.resl.ltk.generated.messages.READER_EVENT_NOTIFICATION;
import kr.ac.kaist.resl.ltk.generated.messages.RO_ACCESS_REPORT;
import kr.ac.kaist.resl.ltk.generated.parameters.AccessCommand;
import kr.ac.kaist.resl.ltk.generated.parameters.AccessReportSpec;
import kr.ac.kaist.resl.ltk.generated.parameters.AccessSpec;
import kr.ac.kaist.resl.ltk.generated.parameters.AccessSpecStopTrigger;
import kr.ac.kaist.resl.ltk.generated.parameters.AntennaID;
import kr.ac.kaist.resl.ltk.generated.parameters.C1G2Read;
import kr.ac.kaist.resl.ltk.generated.parameters.C1G2ReadOpSpecResult;
import kr.ac.kaist.resl.ltk.generated.parameters.C1G2TagSpec;
import kr.ac.kaist.resl.ltk.generated.parameters.C1G2TargetTag;
import kr.ac.kaist.resl.ltk.generated.parameters.C1G2Write;
import kr.ac.kaist.resl.ltk.generated.parameters.C1G2WriteOpSpecResult;
import kr.ac.kaist.resl.ltk.generated.parameters.C1G2_CRC;
import kr.ac.kaist.resl.ltk.generated.parameters.C1G2_PC;
import kr.ac.kaist.resl.ltk.generated.parameters.ConnectionAttemptEvent;
import kr.ac.kaist.resl.ltk.generated.parameters.EPC_96;
import kr.ac.kaist.resl.ltk.generated.parameters.ReaderEventNotificationData;
import kr.ac.kaist.resl.ltk.generated.parameters.TagReportData;
import org.llrp.ltk.net.LLRPConnection;
import org.llrp.ltk.types.Bit;
import org.llrp.ltk.types.BitArray_HEX;
import org.llrp.ltk.types.Integer96_HEX;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.TwoBitField;
import org.llrp.ltk.types.UnsignedInteger;
import org.llrp.ltk.types.UnsignedShort;
import org.llrp.ltk.types.UnsignedShortArray_HEX;
import org.llrp.ltk.util.Util;
import org.epcglobalinc.tdt.LevelTypeList;


/**
 * this class implements the adaptor from a logical reader in the filtering and 
 * collection to the physical llrp readers. <br/>
 * the management of the readers is performed by the llrp-client-adaptor. This 
 * adaptor maintains a list of all the readers currently configured on this 
 * filtering and collection server and maintains the corresponding connections.<br/>
 * this adaptor will use the LLRPManager from the filtering and collection to 
 * get a reference to these llrp readers. the adaptor just registers itself for 
 * read notifications. whenever such a notification occurs the epc codes (if 
 * contained) are extracted from the notification and are then propagated to 
 * the filtering and collection framework.
 * @author swieland
 * @author wafa.soubra@orange.com
 *
 */
public class LLRPAdaptor extends BaseReader {
	/**
	 * LLRPAdaptor name which will be used as origin reader name in the reports
	 */
	private String originReaderName = null;

	private NotificationData currentNoti = null;

	private CapabilitiesData capabilityData = null;

	private ReadConfigData configData = null;
	/** logger. */
	private static final Logger log = Logger.getLogger(LLRPAdaptor.class);

	/** reference to the singleton of the llrp manager. */
	private LLRPManager manager = null;

	/** reference to the reader */
	private Reader reader = null;

	/** the name of the physical reader that this adaptor shall connect to (name in the llrp gui client adaptor) */
	private String physicalReaderName = null;

	/** the message callback. */
	private Callback callback = null;

	/** ORANGE: the path to the properties file for the LLRPAdaptor. */
	private static final String LLRPADAPTOR_CONFIG_FILE = "/LLRPAdaptorConfig.properties";

	/** ORANGE: the name of the property corresponding to the OpSpecId of the C1G2Read for the MB=3. */
	/** MB=3 is the the memory bank for the user memory. */
	private static final String USER_MEM_C1G2READ_OPSPEC_ID = "UserMemoryC1G2ReadOpSpecId";

	/** ORANGE: the name of the property corresponding to the OpSpecId of the C1G2Write for the MB=3. */
	/** MB=3 is the the memory bank for the user memory. */
	private static final String USER_MEM_C1G2WRITE_OPSPEC_ID = "UserMemoryC1G2WriteOpSpecId";

	/** ORANGE: the tag length. */
	private static final String TAG_LENGTH = "tagLength";

	/** ORANGE: the tag filter. */
	private static final String TAG_FILTER = "tagFilter";

	/** ORANGE: the tag company prefix length. */
	private static final String TAG_COMPANY_PREFIX_LENGTH = "tagCompanyPrefixLength";

	/** ORANGE: the OpSpecID of the C1G2Read for the User Memory (MB=3).*/
	/** Will be initialized by the value of UserMemoryC1G2ReadOpSpecId.*/
	private static int userMemReadOpSpecID = -1;

	/** ORANGE: the OpSpecID of the C1G2Write for the User Memory (MB=3).*/
	/** Will be initialized by the value of UserMemoryC1G2WriteOpSpecId.*/
	private static int userMemWriteOpSpecID = -1;

	/** ORANGE: the tag length. */
	private static String length = null;

	/** ORANGE: the tag filter. */
	private static String filter = null;

	/** ORANGE: the tag company prefix length. */
	private static String companyPrefixLength = null;

	/** 
	 * if the hash set is empty, allow from all the antennas, otherwise only 
	 * tags arriving from the specified antenna IDs.
	 */
	private Set<Integer> acceptTagsFromAntennas = new HashSet<Integer>(); 


	/** Janggwan: identifier of Reader which is either EPC or MAC address */
	private String physicalReaderId = null;

	/**
	 * Janggwan: ip of Reader 
	 */
	String ip = null;

	/**
	 * Janggwan: port of Reader (default: 5084)
	 */
	int port = 5084;

	/**
	 * defined name of reader (readerimpl) name
	 */
	private String definedReaderImplName = null;

	/**
	 * Janggwan: client initiated  
	 */
	boolean clientInitiated = true;

	/**
	 *  Wondeuk: TDTEngine to make accessspec.
	 */
	private TDTEngine engine = null;

	/**
	 *  Wondeuk: TDTEngine to make accessspec.
	 */
	private Map<String,String> params;
	
	/**
	 * FIXME: Wondeuk: to fix llrp bug.
	 */
	TwoBitField tidBank = new TwoBitField("1");
	TwoBitField epcBank = new TwoBitField("2");
	TwoBitField userBank = new TwoBitField("3");
	TwoBitField reservedBank = new TwoBitField("0");

	/**
	 * constructor for the LLRP adaptor.
	 */
	public LLRPAdaptor() {
		super();

		try {
			callback = new Callback(this);
		} catch (RemoteException e) {
			log.debug("caught exception", e);
		}
	}

	/**
	 * initializes a LLRPAdaptor. this method must be called before the Adaptor can
	 * be used.
	 * @param name the name for the reader encapsulated by this adaptor.
	 * @param spec the specification that describes the current reader.
	 * @throws ImplementationException whenever an internal error occurs.

	 */
	public void initialize(String name, LRSpec spec) throws ImplementationException {

		super.initialize(name, spec);

		if ((name == null) || (spec == null)) {
			log.error("reader name or LRSpec is null.");
			throw new ImplementationException("reader name or LRSpec is null.");
		}

		this.setOriginReaderName(name);

		//ORANGE: initialize properties for the LLRPAdaptor
		inititializeLLRPAdaptorProperties (LLRPADAPTOR_CONFIG_FILE);

		physicalReaderName = logicalReaderProperties.get("PhysicalReaderName");



		try {
			log.debug("create a new LLRP reader");
			manager = ALEApplicationContext.getBean(LLRPManager.class);
			if (manager != null) {

				clientInitiated = true;
				if (logicalReaderProperties.get("clientInitiated") == null) {
					log.warn("clientInitiated not set, assuming true");
				} else {
					clientInitiated = Boolean.parseBoolean(logicalReaderProperties.get("clientInitiated"));
				}

				// extract ip and port of LLRP reader depending on the value of clientInitiated
				if(clientInitiated) {
					ip = logicalReaderProperties.get("ip");

					port = Constants.DEFAULT_LLRP_PORT;
					if (logicalReaderProperties.get("port") != null) {
						port = Integer.parseInt(logicalReaderProperties.get("port"));
					}



					if (ip == null) {
						log.error("llrp reader '" + physicalReaderName + "' is missing " +
								"and not enough parameters specified. (Needs PhysicalReaderName, ip, port) !");
						throw new ImplementationException("llrp reader is missing and not " +
								"enough parameters specified. (Needs PhysicalReaderName, ip, port, clientInitiated) !");
					}

					definedReaderImplName = physicalReaderName;

				} else {

					physicalReaderId = logicalReaderProperties.get("PhysicalReaderId");
					if(physicalReaderId == null) {
						log.error("llrp reader '" + physicalReaderName + "' is missing " +
								"and not enough parameters specified. (Needs PhysicalReaderName, epc) in case of reader-initiated connection!");
						throw new ImplementationException("llrp reader is missing and not " +
								"enough parameters specified. (Needs PhysicalReaderName, epc) in case of reader-initiated connection!");
					}

					if( PhysicalReaderAcceptor.mapIdAndReaderInitiatedConnectionEntry.get(physicalReaderId) == null) {
						log.error("llrp reader '" + physicalReaderName + "' is missing " +
								"and not yet connected whose epc is "+physicalReaderId+" in case of reader-initiated connection!");
						throw new ImplementationException("llrp reader is missing and not " +
								"and not yet connected whose epc is "+physicalReaderId+" in case of reader-initiated connection!");
					}

					ip = PhysicalReaderAcceptor.mapIdAndReaderInitiatedConnectionEntry.get(physicalReaderId).getReaderAddr();
					port = PhysicalReaderAcceptor.mapIdAndReaderInitiatedConnectionEntry.get(physicalReaderId).getPort();

					//physicalReaderId = epc;
					definedReaderImplName = physicalReaderId+"___"+logicalReaderProperties.get("antennaID");

				}


				// if the reader is not contained in the manager we just create a 
				// new one...
				if (!manager.getAdaptor().containsReader(definedReaderImplName)) {



					log.debug(String.format(
							"defining new reader with settings:" +
									"name: %s, ip: %s, port: %d, clientinitiated: %b",
									physicalReaderName,
									ip,
									port, 
									clientInitiated));

					// create the reader but do not immediately connect.
					manager.getAdaptor().define(definedReaderImplName, ip, port, clientInitiated, false);
				}

				// get the antenna IDs to read from
				String antennaIDSStr = logicalReaderProperties.get("antennaID");
				if (null != antennaIDSStr) {
					String[] ai = antennaIDSStr.split(",");
					for (String str : ai) {
						try {
							int i = Integer.parseInt(str);
							acceptTagsFromAntennas.add(new Integer(i));
						} catch (Exception e) {
							log.debug(String.format("Illegal antennaID: %s", str));
						}
					}
				}

				//TODO:We need to implement to manage multiple baseReader in LogicalReader.
				//wdYoon
				reader = manager.getAdaptor().getReader(definedReaderImplName);
				ReaderImpl readerImpl = (ReaderImpl)reader;

				if(readerImpl.getLlrpAdaptor() != null) {
					throw new ImplementationException ("ReaderImpl has already defined LLRPAdaptor");
				} else {
					readerImpl.setLlrpAdaptor(this);					
				}


				// register the adaptor for asynchronous notifications
				log.debug("register the adaptor for asynchronous notifications from the llrp reader");
				reader.registerForAsynchronous(callback);

				// if the reader is not yet connected do so
				if (!reader.isConnected()) {
					if(reader.isClientInitiated()) {
						reader.connect(reader.isClientInitiated());
					} else {
						// connection is already made through LogicalReaderAcceptor
						// do nothing for the connection (even though reader.connect() is called, it does nothing for actual connection), but
						// set acceptor's endpoint to the ReaderImpl in order to let it process LLRP message, and
						// ReaderImpl's connection to the acceptor's connection in order to let it send LLRP message

						ReaderInitiatedConnectionEntry connectionEntry = PhysicalReaderAcceptor.mapIdAndReaderInitiatedConnectionEntry.get(physicalReaderId);
						connectionEntry.getAdaptors().add(this);
						readerImpl.setIoSession(connectionEntry.getIoSession());

						// we need to multiplex a reader with a physicalReaderId to multiple LLRPAdaptor
						// connectionEntry.getEndpoint().add(endpoint)
						connectionEntry.getEndpoint().addLLRPEndpoint((ReaderImpl)reader);
						reader.connect(reader.isClientInitiated());
					}

				}
			} else {
				log.error("could not get an instance of the LLRPManager - aborting");
				throw new ImplementationException("could not get an instance of the LLRPManager - aborting");
			}


		} catch (Exception e) {
			log.error("Error when initializing the Reader", e);
			throw new ImplementationException("Error when initializing the Reader "+e.getMessage());
		}


		manager.incReference(definedReaderImplName);

		//connectReader();   Call me if you got trouble, littleanti91@gmail.com

		//log.debug("Reader "+definedReaderImplName+"is defined. define LLRP specs.");
		/*
		if(!clientInitiated && !LogicalReaderAcceptor.mapIdAndReaderInitiatedConnectionEntry.get(physicalReaderId).isSpecDefined()) {
			try {
				File fileDeleteRospec = convertInputStreamToFile(getClass().getResourceAsStream("/llrp/DELETE_ROSPEC.xml"));
				LLRPMessage msgDeleteRospec = Util.loadXMLLLRPMessage(fileDeleteRospec);
				((ReaderImpl)reader).getConnection().send(msgDeleteRospec);

				Thread.sleep(500);

				File fileAddRospec = convertInputStreamToFile(getClass().getResourceAsStream("/llrp/ADD_ROSPEC.xml"));
				LLRPMessage msgAddRospec = Util.loadXMLLLRPMessage(fileAddRospec);
				((ReaderImpl)reader).getConnection().send(msgAddRospec);

				Thread.sleep(500);

				File fileAddAccessspec = convertInputStreamToFile(getClass().getResourceAsStream("/llrp/ADD_ACCESSSPEC3.xml"));
				LLRPMessage msgAddAccessspec = Util.loadXMLLLRPMessage(fileAddAccessspec);
				((ReaderImpl)reader).getConnection().send(msgAddAccessspec);

				Thread.sleep(500);

				ENABLE_ACCESSSPEC msgEnableAccessSpec = new ENABLE_ACCESSSPEC();
				msgEnableAccessSpec.setAccessSpecID(new UnsignedInteger(3));
				((ReaderImpl)reader).getConnection().send(msgEnableAccessSpec);

				Thread.sleep(500);

				File fileEnableRospec = convertInputStreamToFile(getClass().getResourceAsStream("/llrp/ENABLE_ROSPEC.xml"));
				LLRPMessage msgEnableRospec = Util.loadXMLLLRPMessage(fileEnableRospec);
				((ReaderImpl)reader).getConnection().send(msgEnableRospec);

				Thread.sleep(500);

				File fileStartRospec = convertInputStreamToFile(getClass().getResourceAsStream("/llrp/START_ROSPEC.xml"));
				LLRPMessage msgStartRospec = Util.loadXMLLLRPMessage(fileStartRospec);
				((ReaderImpl)reader).getConnection().send(msgStartRospec);

				Thread.sleep(500);

				LogicalReaderAcceptor.mapIdAndReaderInitiatedConnectionEntry.get(physicalReaderId).setSpecDefined(true);

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
		}
		 */
		//GET_ACCESSSPECS get = new GET_ACCESSSPECS();
		//((ReaderImpl)reader).getIoSession().write(get);
		
	}

	@Override
	public void addTag(Tag tag) {
		tag.setOrigin(getOriginReaderName());
		tag.setReader(getName());

		setChanged();
		notifyObservers(tag);
	}

	@Override
	public void addTags(List<Tag> tags) {
		setChanged();
		for (Tag tag : tags) {
			tag.addTrace(getName());
		}

		notifyObservers(tags);
	}

	@Override
	public void connectReader() throws ImplementationException {
		// get the required reader and register for asynchronous messages.
		try {
			Reader llrpReader = manager.getAdaptor().getReader(definedReaderImplName);
			llrpReader.registerForAsynchronous(callback);
			setConnected();

		} catch (RemoteException e) {
			log.error("could not connect to the reader");
			throw new ImplementationException("Error when connecting the Reader");
		}
	}

	@Override
	public void disconnectReader() throws ImplementationException {
		// get the required reader and register for asynchronous messages.
		try {
			Reader llrpReader = manager.getAdaptor().getReader(definedReaderImplName);
			llrpReader.deregisterFromAsynchronous(callback);
			setDisconnected();

		} catch (RemoteException e) {
			log.error("could not disconnect from the reader");
			throw new ImplementationException("Error when disconnecting the Reader");
		}
	}

	@Override
	public void start() {
		if (isConnected()) {
			setStarted();
		}
	}

	@Override
	public void stop() {
		setStopped();
	}

	@Override
	public void cleanup() {

		try {
			manager.decReferenceCount(definedReaderImplName);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LLRPRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ReaderInitiatedConnectionEntry entry = null;
		if((entry = PhysicalReaderAcceptor.mapIdAndReaderInitiatedConnectionEntry.get(physicalReaderId)) != null) {
			MultipleLLRPEndpoint endpoints = entry.getEndpoint();
			endpoints.removeLLRPEndpoint((ReaderImpl)reader);
		}
	}

	@Override
	public void update(LRSpec spec) throws ImplementationException {
		log.info("you cannot update the reader through fc yet. use the llrp gui client for this purpose please.");
	}

	@Override
	public Observation[] identify(String[] readPointNames)
			throws HardwareException {
		// the llrp readers do not support identify threads.
		return null;
	}

	public void notify(byte[] binaryMessage, String readerName) throws RemoteException {
		log.debug("notify from the reader "+readerName);

		try {
			List<Tag> tags = new LinkedList<Tag>();
			LLRPMessage message = LLRPMessageFactory.createLLRPMessage(binaryMessage);

			if (message instanceof RO_ACCESS_REPORT) {
				RO_ACCESS_REPORT report = (RO_ACCESS_REPORT)message;
				List<TagReportData> tagDataList = report.getTagReportDataList();

				System.out.println(report.toXMLString());
				//GET_ACCESSSPECS get = new GET_ACCESSSPECS();
				//((ReaderImpl)reader).getIoSession().write(get);

				for (TagReportData tagData : tagDataList) {
					boolean include = false;
					if (0 == acceptTagsFromAntennas.size()) {
						include = true;
					} else {
						AntennaID antennaID = tagData.getAntennaID();
						if ((null != antennaID) && 
								(null != antennaID.getAntennaID())) {

							int id = antennaID.getAntennaID().intValue();
							if (acceptTagsFromAntennas.contains(new Integer(id))) {
								include = true;
							}
						}
					}
					EPCParameter epcParameter = tagData.getEPCParameter();
					if ((include) && (epcParameter instanceof EPC_96)) {
						EPC_96 epc96 = (EPC_96) epcParameter;
						Integer96_HEX hex = epc96.getEPC();
						String hx = hex.toString();

						Tag tag = null;
						TDTEngine tdt = TagHelper.getTDTEngine();
						try {
							String binary = tdt.hex2bin(hx);
							if (binary.startsWith("1") && 
									(binary.length() < 96)) {

								binary = "00" + binary;
							}

							tag = new Tag(getOriginReaderName());
							tag.setTagAsHex(hx);
							tag.setTagAsBinary(binary);
							tag.setTagID(binary.getBytes());
							tag.setReader(getName());
							tag.addTrace(getName());
							if(tagData.getLastSeenTimestampUTC() != null) {
								if(tagData.getLastSeenTimestampUTC().getMicroseconds() != null) {
									tag.setTimestamp(tagData.getLastSeenTimestampUTC().getMicroseconds().toLong()/1000);
								}
							} else {
								tag.setTimestamp(System.currentTimeMillis());
							}
							tag.setAccessSpecID(tagData.getAccessSpecID());

							//ORANGE: add additional values if they exist
							tag.setTagLength(length);
							tag.setFilter(filter);
							tag.setCompanyPrefixLength(companyPrefixLength);

							//ORANGE End.
							String epc_tag = TagHelper.convert_to_TAG_ENCODING(tag.getTagLength(), tag.getFilter(), tag.getCompanyPrefixLength(), tag.getTagAsBinary(), tdt);
							tag.setTagIDAsTagURI(epc_tag);

							String pc_hex = "0000";
							String crc_hex = "0000";
							if(tagData.getAirProtocolTagDataList() != null) {
								for(AirProtocolTagData airTagData : tagData.getAirProtocolTagDataList()) {
									if(airTagData instanceof C1G2_CRC) {
										crc_hex = ((C1G2_CRC)airTagData).getCRC().toString(16);
										while(crc_hex.length() < 4) {
											crc_hex = "0"+crc_hex;
										}
									} else if(airTagData instanceof C1G2_PC) {
										pc_hex = ((C1G2_PC)airTagData).getPC_Bits().toString(16);
										while(pc_hex.length() < 4) {
											pc_hex = "0"+pc_hex;
										}
									}
								}								
							}
							tag.setEpcBank(crc_hex+pc_hex+hx);

							// add the tag.
							tags.add(tag);
						} catch (Exception e) {
							log.debug("bad error, ignoring tag: " + e.getMessage());
						}	

						//ORANGE: managing the User Memory in the RO_ACCESS_REPORT
						List<AccessCommandOpSpecResult> accessResultList = tagData.getAccessCommandOpSpecResultList();
						for (AccessCommandOpSpecResult accessResult : accessResultList) {

							//ORANGE: in case of reading the User Memory of a tag, 
							//retrieve the user memory from the RO_ACCESS_REPORT and store it in the tag.
							if (accessResult instanceof C1G2ReadOpSpecResult) {
								C1G2ReadOpSpecResult op = (C1G2ReadOpSpecResult)accessResult;
								if ((op.getResult().intValue() == C1G2ReadResultType.Success) && 
									(op.getOpSpecID().intValue() < 1000)){
									UnsignedShortArray_HEX userMemoryHex = op.getReadData();
									log.debug ("User Memory read from the tag is = " + userMemoryHex.toString());
									tag.setUserMemory(userMemoryHex.toString());
								}
								if ((op.getResult().intValue()== C1G2ReadResultType.Success) && 
										(op.getOpSpecID().intValue() >= 1000)) {
									log.debug ("Reading in the User Memory of the tag is = " + op.getReadData().toString());
									tag.addOpresult(op.getOpSpecID().intValue(), op.getReadData().toString(), 0);
									
									for (OpReportResult temp : tag.getopresult())
									{
										System.out.println("@@@@@@@@@@"+temp.OpSpecID+"\t"+temp.Data+"@@@@@@@@@@@@@@@@@@@");
									}
									//TODO: Dealing each CCStatus.
								}
								else if ((op.getResult().intValue()== C1G2ReadResultType.Nonspecific_Tag_Error) && 
										(op.getOpSpecID().intValue() >= 1000)) {
									log.debug ("Reading in the User Memory of the tag has failed by Nonspecific_Tag_Error.");
									tag.addOpresult(op.getOpSpecID().intValue(), op.getReadData().toString(), 4);
									//TODO: Dealing each CCStatus.
								}
								else if ((op.getResult().intValue()== C1G2ReadResultType.No_Response_From_Tag) && 
										(op.getOpSpecID().intValue() >= 1000)) {
									log.debug ("Reading in the User Memory of the tag has failed by No_Response_From_Tag.");
									tag.addOpresult(op.getOpSpecID().intValue(), op.getReadData().toString(), 5);
									//TODO: Dealing each CCStatus.
								}
								else if ((op.getResult().intValue()== C1G2WriteResultType.Nonspecific_Reader_Error) && 
										(op.getOpSpecID().intValue() >= 1000)) {
									log.debug ("Reading in the User Memory of the tag has failed by Nonspecific_Reader_Error.");
									tag.addOpresult(op.getOpSpecID().intValue(), op.getReadData().toString(), 3);
									//TODO: Dealing each CCStatus.
								}
								else if ((op.getResult().intValue()== C1G2WriteResultType.Tag_Memory_Overrun_Error) && 
										(op.getOpSpecID().intValue() >= 1000)) {
									log.debug ("Reading in the User Memory of the tag has failed by Tag_Memory_Overrun_Error.");
									tag.addOpresult(op.getOpSpecID().intValue(), op.getReadData().toString(), 1);
									//TODO: Dealing each CCStatus.
								}
								else if ((op.getResult().intValue()== C1G2WriteResultType.Tag_Memory_Locked_Error) && 
										(op.getOpSpecID().intValue() >= 1000)) {
									log.debug ("Reading in the User Memory of the tag has failed by Tag_Memory_Locked_Error.");
									tag.addOpresult(op.getOpSpecID().intValue(), op.getReadData().toString(), 2);
									//TODO: Dealing each CCStatus.
								}
								else if ((op.getResult().intValue()!= C1G2WriteResultType.Success) && 
										(op.getOpSpecID().intValue() >= 1000)) {
									log.debug ("Reading in the User Memory of the tag has failed.");
									tag.addOpresult(op.getOpSpecID().intValue(), op.getReadData().toString(), 7);
									//TODO: Dealing each CCStatus.
								}
							}

							//ORANGE: in case of writing in the User Memory of the tag,
							//log if needed that the C1G2Write Operation on the tag has succeeded. 
							if (accessResult instanceof C1G2WriteOpSpecResult) {
								C1G2WriteOpSpecResult op = (C1G2WriteOpSpecResult)accessResult;
								if ((op.getResult().intValue()== C1G2WriteResultType.Success)/*&&
									(op.getOpSpecID().intValue() == userMemWriteOpSpecID)*/) {
									log.debug ("Writing in the User Memory of the tag has succeed. OpSpecID is = " + op.getOpSpecID().toString());
									tag.addOpresult(op.getOpSpecID().intValue(), op.getNumWordsWritten().toString(), C1G2WriteResultType.Success);
								}
								else if ((op.getResult().intValue()== C1G2WriteResultType.Tag_Memory_Overrun_Error)/*&&
										(op.getOpSpecID().intValue() == userMemWriteOpSpecID)*/) {
									log.debug ("Writing in the User Memory of the tag has failed by Tag_Memory_Overrun_Error.");
									tag.addOpresult(op.getOpSpecID().intValue(), "Tag_Memory_Overrun_Error", C1G2WriteResultType.Tag_Memory_Overrun_Error);
									//TODO: Dealing each CCStatus.
								}
								else if ((op.getResult().intValue()== C1G2WriteResultType.Tag_Memory_Locked_Error)/*&&
										(op.getOpSpecID().intValue() == userMemWriteOpSpecID)*/) {
									log.debug ("Writing in the User Memory of the tag has failed by Tag_Memory_Locked_Error.");
									tag.addOpresult(op.getOpSpecID().intValue(), "Tag_Memory_Locked_Error", C1G2WriteResultType.Tag_Memory_Locked_Error);
									//TODO: Dealing each CCStatus.
								}
								else if ((op.getResult().intValue()== C1G2WriteResultType.Insufficient_Power)/*&&
										(op.getOpSpecID().intValue() == userMemWriteOpSpecID)*/) {
									log.debug ("Writing in the User Memory of the tag has failed by Insufficient_Power.");
									tag.addOpresult(op.getOpSpecID().intValue(), "Insufficient_Power", C1G2WriteResultType.Insufficient_Power);
									//TODO: Dealing each CCStatus.
								}
								else if ((op.getResult().intValue()== C1G2WriteResultType.Nonspecific_Tag_Error)/*&&
										(op.getOpSpecID().intValue() == userMemWriteOpSpecID)*/) {
									log.debug ("Writing in the User Memory of the tag has failed by Nonspecific_Tag_Error.");
									tag.addOpresult(op.getOpSpecID().intValue(), "Nonspecific_Tag_Error", C1G2WriteResultType.Nonspecific_Tag_Error);
									//TODO: Dealing each CCStatus.
								}
								else if ((op.getResult().intValue()== C1G2WriteResultType.No_Response_From_Tag)/*&&
										(op.getOpSpecID().intValue() == userMemWriteOpSpecID)*/) {
									log.debug ("Writing in the User Memory of the tag has failed by No_Response_From_Tag.");
									tag.addOpresult(op.getOpSpecID().intValue(), "No_Response_From_Tag", C1G2WriteResultType.No_Response_From_Tag);
									//TODO: Dealing each CCStatus.
								}
								else if ((op.getResult().intValue()== C1G2WriteResultType.Nonspecific_Reader_Error)/*&&
										(op.getOpSpecID().intValue() == userMemWriteOpSpecID)*/) {
									log.debug ("Writing in the User Memory of the tag has failed by Nonspecific_Reader_Error.");
									tag.addOpresult(op.getOpSpecID().intValue(), "Nonspecific_Reader_Error", C1G2WriteResultType.Nonspecific_Reader_Error);
									//TODO: Dealing each CCStatus.
								}
								else if ((op.getResult().intValue()!= C1G2WriteResultType.Success)/*&&
										(op.getOpSpecID().intValue() == userMemWriteOpSpecID)*/) {
									log.debug ("Writing in the User Memory of the tag has failed.");
									tag.addOpresult(op.getOpSpecID().intValue(), "Fail", 7);
									//TODO: Dealing each CCStatus.
								}
							}
						}	
						//ORANGE End	

						// try to run a conversion on the tag...
						if (null != tag) {
							try {		
								//ORANGE : replace the following code ...
								//								String pureID = Tag.convert_to_PURE_IDENTITY(
								//										null, 
								//										null, 
								//										null, 
								//										tag.getTagAsBinary());

								//ORANGE : by this one more generic.
								String pureID =	TagHelper.convert_to_PURE_IDENTITY(
										tag.getTagLength(),
										tag.getFilter(),
										tag.getCompanyPrefixLength(),
										tag.getTagAsBinary());	
								//ORANGE End.

								tag.setTagIDAsPureURI(pureID);
							} catch (Exception e) {
								log.debug("could not convert provided tag: " + e.getMessage());
							}
						}
					}
				}
			} else if(message instanceof READER_EVENT_NOTIFICATION) {

				ReaderEventNotificationData data = null;
				if((data = ((READER_EVENT_NOTIFICATION)message).getReaderEventNotificationData()) != null) {
					currentNoti = new NotificationData();
					if(data.getHoppingEvent() != null)
						currentNoti.setHoppingEvent(data.getHoppingEvent());
					if(data.getAISpecEvent() != null)
						currentNoti.setAISpecEvent(data.getAISpecEvent());
					if(data.getAntennaEvent() != null)
						currentNoti.setAntennaEvent(data.getAntennaEvent());
					if(data.getGPIEvent() != null)
						currentNoti.setGPIEvent(data.getGPIEvent());
					if(data.getROSpecEvent() != null)
						currentNoti.setROSpecEvent(data.getROSpecEvent());

					ConnectionAttemptEvent connectionAttemptEvent = null;
					if((connectionAttemptEvent = data.getConnectionAttemptEvent()) != null) {
						currentNoti.setConnectionAttemptEvent(connectionAttemptEvent);
					}

					if((data.getConnectionCloseEvent()) != null) {
						//((ReaderImpl)reader).disconnect();
						//((ReaderImpl)reader).errorOccured("disconnected");

						//getConnectionAttemptEventQueue().clear();
						//getSynMessageQueue().clear();
						//session.close();
					}
				}

			} else if(message instanceof GET_READER_CAPABILITIES_RESPONSE) {

				GET_READER_CAPABILITIES_RESPONSE resp = ((GET_READER_CAPABILITIES_RESPONSE)message);

				if (resp.getLLRPStatus() != null)
					capabilityData.setLLRPStatus(resp.getLLRPStatus());
				if (resp.getGeneralDeviceCapabilities() != null)
					capabilityData.setGeneralDeviceCapabilities(resp.getGeneralDeviceCapabilities());
				if (resp.getLLRPCapabilities() != null)
					capabilityData.setLLRPCapabilities(resp.getLLRPCapabilities());
				if (resp.getRegulatoryCapabilities() != null)
					capabilityData.setRegulatoryCapabilities(resp.getRegulatoryCapabilities());
				if (resp.getAirProtocolLLRPCapabilities() != null)
					capabilityData.setAirProtocolLLRPCapabilities(resp.getAirProtocolLLRPCapabilities());


			} else if(message instanceof GET_READER_CONFIG_RESPONSE) {

				GET_READER_CONFIG_RESPONSE resp = ((GET_READER_CONFIG_RESPONSE)message);

				if(configData == null)
					configData = new ReadConfigData();

				if(resp.getAccessReportSpec() != null)
					configData.setAccessReportSpec(resp.getAccessReportSpec());
				if(resp.getAntennaConfigurationList() != null)
					configData.setAntennaConfiguration(resp.getAntennaConfigurationList());
				if(resp.getAntennaPropertiesList() != null)
					configData.setAntennaProperties(resp.getAntennaPropertiesList());
				if(resp.getEventsAndReports() != null)
					configData.setEventsAndReports(resp.getEventsAndReports());
				if(resp.getGPIPortCurrentStateList() != null)
					configData.setGPIPortCurrentState(resp.getGPIPortCurrentStateList());
				if(resp.getGPOWriteDataList() != null)
					configData.setGPOWriteData(resp.getGPOWriteDataList());
				if(resp.getIdentification() != null)
					configData.setIdentification(resp.getIdentification());
				if(resp.getKeepaliveSpec() != null)
					configData.setKeepaliveSpec(resp.getKeepaliveSpec());
				if(resp.getLLRPConfigurationStateValue() != null)
					configData.setLLRPConfigurationStateValue(resp.getLLRPConfigurationStateValue());
				if(resp.getLLRPStatus() != null)
					configData.setLLRPStatus(resp.getLLRPStatus());
				if(resp.getReaderEventNotificationSpec() != null)
					configData.setReaderEventNotificationSpec(resp.getReaderEventNotificationSpec());
				if(resp.getROReportSpec() != null)
					configData.setROReportSpec(resp.getROReportSpec());


			} else if(message instanceof KEEPALIVE) {
				System.out.println("Keep Alive Message Arrived");
			} else if(message instanceof GET_ACCESSSPECS_RESPONSE) {
				GET_ACCESSSPECS_RESPONSE resp = ((GET_ACCESSSPECS_RESPONSE) message);

				//System.out.println(resp.toXMLString());	
			} else if(message instanceof ADD_ACCESSSPEC_RESPONSE) {
				ADD_ACCESSSPEC_RESPONSE resp = ((ADD_ACCESSSPEC_RESPONSE) message);

				//System.out.println(resp.toXMLString());	
			} else if(message instanceof ENABLE_ACCESSSPEC_RESPONSE) {
				ENABLE_ACCESSSPEC_RESPONSE resp = ((ENABLE_ACCESSSPEC_RESPONSE) message);

				//GET_ACCESSSPECS get = new GET_ACCESSSPECS();
				//((ReaderImpl)reader).getIoSession().write(get);
				
				System.out.println(resp.toXMLString());	
			} else if(message instanceof DELETE_ACCESSSPEC_RESPONSE) {
				DELETE_ACCESSSPEC_RESPONSE resp = ((DELETE_ACCESSSPEC_RESPONSE) message);

				//System.out.println(resp.toXMLString());	
			}


			// send the tags to fc
			addTags(tags);
		} catch (InvalidLLRPMessageException e) {
			log.info("received invalid llrp message that could not be converted from binary");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * ORANGE: This method initalizes properties needed to manage an LLRPAdaptor.
	 * Properties are used to read the User Memory of a tag from an RO_ACCESS_REPORT or to log that
	 * a write operation in the User Memory of a tag has succeeded. 
	 * There are also properties linked to the creation of a tag like : length, filter and companyPrefixLength. 
	 * @param propertiesFilePath the filepath to the properties file
	 * @throws ImplementationException if properties could not be loaded
	 */
	public void inititializeLLRPAdaptorProperties (String propertiesFilePath) throws ImplementationException {
		Properties props = new Properties();
		//TODO : to test the different cases !!!!
		try {
			props.load(LLRPAdaptor.class.getResourceAsStream(propertiesFilePath));
		} catch (Exception e) {
			throw new ImplementationException
			("Error loading properties from LLRPAdaptor '" + propertiesFilePath + "'");
		}
		// we need to initialize the User Memory OpSpecID
		String readOpSpecID = props.getProperty(USER_MEM_C1G2READ_OPSPEC_ID);
		String writeOpSpecID = props.getProperty(USER_MEM_C1G2WRITE_OPSPEC_ID);
		if (readOpSpecID != null) {
			userMemReadOpSpecID = java.lang.Integer.parseInt(readOpSpecID);
		}
		if (writeOpSpecID != null) {
			userMemWriteOpSpecID = java.lang.Integer.parseInt(writeOpSpecID);
		}
		// init the parameters of a tag from the properties file
		length = props.getProperty(TAG_LENGTH);
		filter = props.getProperty(TAG_FILTER);
		companyPrefixLength = props.getProperty(TAG_COMPANY_PREFIX_LENGTH);
	}

	private File convertInputStreamToFile(InputStream is) throws IOException {
		File file = File.createTempFile("llrp", "llrp");
		OutputStream outputStream = new FileOutputStream(file);
		IOUtils.copy(is, outputStream);
		outputStream.close();
		return file;
	}

	@Override
	public void ADDACCESSSPECfromCCSpec(CCSpec ccspec, Hashtable<Integer, CCOpSpec> OpSpecTable) {

		params = new HashMap<String,String>();
		if (engine == null) {
			try {
				engine = new TDTEngine();
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
				//System.exit(1);
			}
		}
		
		DELETEACCESSSPEC();

		if (ccspec.getCmdSpecs() != null && (!ccspec.getCmdSpecs().getCmdSpec().isEmpty()))
		{
			for (CCCmdSpec cmdspec : ccspec.getCmdSpecs().getCmdSpec())
			{
				if (cmdspec.getFilterSpec() != null && (!cmdspec.getFilterSpec().getFilterList().getFilter().isEmpty()))
				{
					for (ECFilterListMember filterList : cmdspec.getFilterSpec().getFilterList().getFilter())
					{	
						for (String pat : filterList.getPatList().getPat())
						{
							AccessSpec accessSpec = new AccessSpec();
							accessSpec.setAccessSpecID(new UnsignedInteger(3));

							// Set ROSpec ID to zero.
							// This means that the AccessSpec will apply to all ROSpecs.
							accessSpec.setROSpecID(new UnsignedInteger(1));
							// Antenna ID of zero means all antennas.
							accessSpec.setAntennaID(new UnsignedShort(0));
							accessSpec.setProtocolID(new AirProtocols(AirProtocols.EPCGlobalClass1Gen2));
							// AccessSpecs must be disabled when you add them.
							accessSpec.setCurrentState(new AccessSpecState(AccessSpecState.Disabled));
							AccessSpecStopTrigger stopTrigger = new AccessSpecStopTrigger();
							// Stop after the operating has been performed a certain number of times.
							// That number is specified by the Operation_Count parameter or null.
							stopTrigger.setAccessSpecStopTrigger(new AccessSpecStopTriggerType(AccessSpecStopTriggerType.Null));
							// OperationCountValue indicate the number of times this Spec is
							// executed before it is deleted. If set to 0, this is equivalent
							// to no stop trigger defined.
							stopTrigger.setOperationCountValue(new UnsignedShort(0));
							accessSpec.setAccessSpecStopTrigger(stopTrigger);

							// Create a new AccessCommand.
							// We use this to specify which tags we want to operate on.
							AccessCommand accessCommand = new AccessCommand();

							// Create a new tag spec.
							C1G2TagSpec tagSpec = new C1G2TagSpec();
							C1G2TargetTag targetTag = new C1G2TargetTag();
							if (filterList.getIncludeExclude().equals("INCLUDE"))
							{
								targetTag.setMatch(new Bit(1));

								String fieldName = filterList.getFieldspec().getFieldname();
								if (fieldName==null)
									fieldName = "epc";
								if(fieldName.equalsIgnoreCase("killPwd")) {
									// killPwd fieldname
									// same as "@0.32"
									// We want to check memory bank.
									TwoBitField memBank = reservedBank;
									// Clear bit 0 and set bit 1 (bank 1 in binary).
									targetTag.setMB(memBank);

									// The EPC data starts at offset 0x20.
									// Start reading or writing from there.
									targetTag.setPointer(new UnsignedShort(0));
									// This is the mask we'll use to compare the EPC.
									// We want to match all bits of the EPC, so all mask bits are set.
									String TAG_MASK = "";
									for (int i=0; i<32; i++)
										TAG_MASK = TAG_MASK + "F";

									BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
									targetTag.setTagMask(tagMask);
									// We only only to operate on tags with this EPC.
									String TARGET_EPC = pat.toString();
									BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
									targetTag.setTagData(tagData);

									// Add a list of target tags to the tag spec.
									List <C1G2TargetTag> targetTagList =
											new ArrayList<C1G2TargetTag>();
									targetTagList.add(targetTag);
									tagSpec.setC1G2TargetTagList(targetTagList);

									// Add the tag spec to the access command.
									accessCommand.setAirProtocolTagSpec(tagSpec);

								} else if(fieldName.equalsIgnoreCase("accessPwd")) {
									// accessPwd fieldname
									// same as "@0.32.32"
									// We want to check memory bank.
									TwoBitField memBank = reservedBank;
									targetTag.setMB(memBank);

									// The EPC data starts at offset 0x20.
									// Start reading or writing from there.
									targetTag.setPointer(new UnsignedShort(32));
									// This is the mask we'll use to compare the EPC.
									// We want to match all bits of the EPC, so all mask bits are set.
									String TAG_MASK = "";
									for (int i=0; i<32; i++)
										TAG_MASK = TAG_MASK + "F";

									BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
									targetTag.setTagMask(tagMask);
									// We only only to operate on tags with this EPC.
									String TARGET_EPC = pat.toString();
									BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
									targetTag.setTagData(tagData);

									// Add a list of target tags to the tag spec.
									List <C1G2TargetTag> targetTagList =
											new ArrayList<C1G2TargetTag>();
									targetTagList.add(targetTag);
									tagSpec.setC1G2TargetTagList(targetTagList);

									// Add the tag spec to the access command.
									accessCommand.setAirProtocolTagSpec(tagSpec);

								} else if(fieldName.equalsIgnoreCase("epc")) {
									// FIXME : fieldname "epcBank", is it right?
									// return the contents of epc bank
									// datatype: "bits", format: "hex"
									// We want to check memory bank.
									TwoBitField memBank = epcBank;
									targetTag.setMB(memBank);

									// The EPC data starts at offset 0x20.
									// Start reading or writing from there.
									targetTag.setPointer(new UnsignedShort(32));
									// This is the mask we'll use to compare the EPC.
									// We want to match all bits of the EPC, so all mask bits are set.
									String TAG_MASK = "";
									for (int i=0; i<96; i++)
										TAG_MASK = TAG_MASK + "F";

									BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
									targetTag.setTagMask(tagMask);
									// We only only to operate on tags with this EPC.
									String TARGET_EPC = pat.toString();
									BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
									targetTag.setTagData(tagData);

									// Add a list of target tags to the tag spec.
									List <C1G2TargetTag> targetTagList =
											new ArrayList<C1G2TargetTag>();
									targetTagList.add(targetTag);
									tagSpec.setC1G2TargetTagList(targetTagList);

									// Add the tag spec to the access command.
									accessCommand.setAirProtocolTagSpec(tagSpec);

								} else if(fieldName.equalsIgnoreCase("epcBank")) {
									// FIXME : fieldname "epcBank", is it right?
									// return the contents of epc bank
									// datatype: "bits", format: "hex"
									// We want to check memory bank.
									TwoBitField memBank = epcBank;
									targetTag.setMB(memBank);

									// The EPC data starts at offset 0x20.
									// Start reading or writing from there.
									targetTag.setPointer(new UnsignedShort(0));
									// This is the mask we'll use to compare the EPC.
									// We want to match all bits of the EPC, so all mask bits are set.
									String TAG_MASK = "";
									for (int i=0; i<128; i++)
										TAG_MASK = TAG_MASK + "F";

									BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
									targetTag.setTagMask(tagMask);
									// We only only to operate on tags with this EPC.
									String TARGET_EPC = pat.toString();
									BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
									targetTag.setTagData(tagData);

									// Add a list of target tags to the tag spec.
									List <C1G2TargetTag> targetTagList =
											new ArrayList<C1G2TargetTag>();
									targetTagList.add(targetTag);
									tagSpec.setC1G2TargetTagList(targetTagList);

									// Add the tag spec to the access command.
									accessCommand.setAirProtocolTagSpec(tagSpec);

								} else if(fieldName.equalsIgnoreCase("tidBank")) {
									// fieldname "tidBank"
									// return the contents of tid bank
									// datatype: "bits", format: "hex"

									try {
										throw new ImplementationException("fieldname \"tidBank\": access to tid bank is not implemented");
									} catch (ImplementationException e) {
										e.printStackTrace();
									}

								} else if(fieldName.equalsIgnoreCase("userBank")) {
									// return the contents of user memory bank
									// datatype: "bits", format: "hex"
									/*ECReportMemberField ecReportMemberField = new ECReportMemberField();
										ecReportMemberField.setName(fieldName);
										ecReportMemberField.setValue(tag.getUserMemory());	// tag.getUserMemory() is already hex string, no need to convert

										ecReportMemberFields.add(ecReportMemberField);
									 */
									try {
										throw new ImplementationException("fieldname \"userBank\": access to user bank is not implemented");
									} catch (ImplementationException e) {
										e.printStackTrace();
									}

								} else if(fieldName.equalsIgnoreCase("afi")) {
									// application family identifier
									// same as "@1.8.24"
									// datatype: "uint", format: "hex"

									// We want to check memory bank.
									TwoBitField memBank = epcBank;
									targetTag.setMB(memBank);

									// The EPC data starts at offset 0x20.
									// Start reading or writing from there.
									targetTag.setPointer(new UnsignedShort(24));
									// This is the mask we'll use to compare the EPC.
									// We want to match all bits of the EPC, so all mask bits are set.
									String TAG_MASK = "";
									for (int i=0; i<8; i++)
										TAG_MASK = TAG_MASK + "F";

									BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
									targetTag.setTagMask(tagMask);
									// We only only to operate on tags with this EPC.
									String TARGET_EPC = pat.toString();
									BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
									targetTag.setTagData(tagData);

									// Add a list of target tags to the tag spec.
									List <C1G2TargetTag> targetTagList =
											new ArrayList<C1G2TargetTag>();
									targetTagList.add(targetTag);
									tagSpec.setC1G2TargetTagList(targetTagList);

									// Add the tag spec to the access command.
									accessCommand.setAirProtocolTagSpec(tagSpec);

								} else if(fieldName.equalsIgnoreCase("nsi")) {
									// Numbering System Identifier (NSI)
									// same as "@1.9.23"
									// datatype: "uint", format: "hex"

									// We want to check memory bank.
									TwoBitField memBank = epcBank;
									targetTag.setMB(memBank);

									// The EPC data starts at offset 0x20.
									// Start reading or writing from there.
									targetTag.setPointer(new UnsignedShort(23));
									// This is the mask we'll use to compare the EPC.
									// We want to match all bits of the EPC, so all mask bits are set.
									String TAG_MASK = "";
									for (int i=0; i<9; i++)
										TAG_MASK = TAG_MASK + "F";

									BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
									targetTag.setTagMask(tagMask);
									// We only only to operate on tags with this EPC.
									String TARGET_EPC = pat.toString();
									BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
									targetTag.setTagData(tagData);

									// Add a list of target tags to the tag spec.
									List <C1G2TargetTag> targetTagList =
											new ArrayList<C1G2TargetTag>();
									targetTagList.add(targetTag);
									tagSpec.setC1G2TargetTagList(targetTagList);

									// Add the tag spec to the access command.
									accessCommand.setAirProtocolTagSpec(tagSpec);

								} else if (fieldName.startsWith("@")) {
									// fieldnames start with "@"

									String[] part = fieldName.substring(1).split("\\.");
									String bank = part[0];
									int length = Integer.parseInt(part[1])/4;
									int offset = Integer.parseInt(part[2]);

									if (filterList.getFieldspec().getDatatype().equalsIgnoreCase("epc"))
									{
										if (filterList.getFieldspec().getFormat().equalsIgnoreCase("epc-pure"))
										{
											// We want to check memory bank 1 (the EPC memory bank).
											TwoBitField memBank = new TwoBitField();
											if (bank.equals("0"))
												memBank = reservedBank;
											else if (bank.equals("1"))
												memBank = epcBank;
											else if (bank.equals("2"))
												memBank = tidBank;
											else if (bank.equals("3"))
												memBank = userBank;
											
											targetTag.setMB(memBank);

											params.put("taglength", "96");
											params.put("filter", "3");
											params.put("companyprefixlength", "7");

											String validEpc = pat.toString();

											if (validEpc.contains("*"))
											{
												String[] parts = validEpc.split(":");
												String[] tfc = parts[4].split("\\.");
												if (tfc[0].contains("*") && tfc[1].contains("*") && tfc[2].contains("*"))
												{
													targetTag.setPointer(new UnsignedShort(offset));
													BitArray_HEX tagMask = new BitArray_HEX("FFFC");
													targetTag.setTagMask(tagMask);

													validEpc = validEpc.replaceFirst("\\*", "0000000");
													validEpc = validEpc.replaceFirst("\\*", "000000");
													validEpc = validEpc.replaceFirst("\\*", "0");

													String bin = engine.convert(validEpc,
															params,
															LevelTypeList.BINARY);

													String TARGET_EPC = engine.bin2hex(bin.substring(0, 16));

													BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
													targetTag.setTagData(tagData);

													// Add a list of target tags to the tag spec.
													List <C1G2TargetTag> targetTagList =
															new ArrayList<C1G2TargetTag>();
													targetTagList.add(targetTag);
													tagSpec.setC1G2TargetTagList(targetTagList);

													// Add the tag spec to the access command.
													accessCommand.setAirProtocolTagSpec(tagSpec);
												}
												else if (tfc[1].contains("*") && tfc[2].contains("*"))
												{
													targetTag.setPointer(new UnsignedShort(offset));
													BitArray_HEX tagMask = new BitArray_HEX("FFFFFFFFFC");
													targetTag.setTagMask(tagMask);

													validEpc = validEpc.replaceFirst("\\*", "000000");
													validEpc = validEpc.replaceFirst("\\*", "0");

													String bin = engine.convert(validEpc,
															params,
															LevelTypeList.BINARY);

													String TARGET_EPC = engine.bin2hex(bin.substring(0, 40));

													BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
													targetTag.setTagData(tagData);

													// Add a list of target tags to the tag spec.
													List <C1G2TargetTag> targetTagList =
															new ArrayList<C1G2TargetTag>();
													targetTagList.add(targetTag);
													tagSpec.setC1G2TargetTagList(targetTagList);

													// Add the tag spec to the access command.
													accessCommand.setAirProtocolTagSpec(tagSpec);
												}
												else if (tfc[2].contains("*"))
												{
													targetTag.setPointer(new UnsignedShort(offset));
													BitArray_HEX tagMask = new BitArray_HEX("FFFFFFFFFFFFFFC");
													targetTag.setTagMask(tagMask);

													validEpc = validEpc.replaceFirst("\\*", "0");

													String bin = engine.convert(validEpc,
															params,
															LevelTypeList.BINARY);

													String TARGET_EPC = engine.bin2hex(bin.substring(0, 60));

													BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
													targetTag.setTagData(tagData);

													// Add a list of target tags to the tag spec.
													List <C1G2TargetTag> targetTagList =
															new ArrayList<C1G2TargetTag>();
													targetTagList.add(targetTag);
													tagSpec.setC1G2TargetTagList(targetTagList);

													// Add the tag spec to the access command.
													accessCommand.setAirProtocolTagSpec(tagSpec);
												}

											}
											else if (validEpc.contains("[") || validEpc.contains("]"))
											{
												try {
													throw new ImplementationException("epc-pure Syntax [lo-hi] is not implemented.");
												} catch (ImplementationException e) {
													e.printStackTrace();
												}
											}
											else 
											{
												// The EPC data starts at offset 0x20.
												// Start reading or writing from there.
												targetTag.setPointer(new UnsignedShort(offset));
												// This is the mask we'll use to compare the EPC.
												// We want to match all bits of the EPC, so all mask bits are set.
												String TAG_MASK = "";
												for (int i=0; i<length; i++)
													TAG_MASK = TAG_MASK + "F";

												BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
												targetTag.setTagMask(tagMask);

												String bin = engine.convert(validEpc,
														params,
														LevelTypeList.BINARY);

												String TARGET_EPC = engine.bin2hex(bin);

												BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
												targetTag.setTagData(tagData);

												// Add a list of target tags to the tag spec.
												List <C1G2TargetTag> targetTagList =
														new ArrayList<C1G2TargetTag>();
												targetTagList.add(targetTag);
												tagSpec.setC1G2TargetTagList(targetTagList);

												// Add the tag spec to the access command.
												accessCommand.setAirProtocolTagSpec(tagSpec);
											}
										}
										else if (filterList.getFieldspec().getFormat().equalsIgnoreCase("epc-tag"))
										{
											// We want to check memory bank 1 (the EPC memory bank).
											TwoBitField memBank = new TwoBitField();
											if (bank.equals("0"))
												memBank = reservedBank;
											else if (bank.equals("1"))
												memBank = epcBank;
											else if (bank.equals("2"))
												memBank = tidBank;
											else if (bank.equals("3"))
												memBank = userBank;
											
											targetTag.setMB(memBank);

											// We only only to operate on tags with this EPC.
											params.put("taglength", "96");
											params.put("filter", "3");
											params.put("companyprefixlength", "7");
											String validEpc = pat.toString();

											if (validEpc.contains("*"))
											{
												String[] parts = validEpc.split(":");
												String[] tfc = parts[4].split("\\.");
												if (tfc[1].contains("*") && tfc[2].contains("*") && tfc[3].contains("*"))
												{
													targetTag.setPointer(new UnsignedShort(offset));
													BitArray_HEX tagMask = new BitArray_HEX("FFFC");
													targetTag.setTagMask(tagMask);

													validEpc = validEpc.replaceFirst("\\*", "0000000");
													validEpc = validEpc.replaceFirst("\\*", "000000");
													validEpc = validEpc.replaceFirst("\\*", "0");

													String bin = engine.convert(validEpc,
															params,
															LevelTypeList.BINARY);

													String TARGET_EPC = engine.bin2hex(bin.substring(0, 16));

													BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
													targetTag.setTagData(tagData);

													// Add a list of target tags to the tag spec.
													List <C1G2TargetTag> targetTagList =
															new ArrayList<C1G2TargetTag>();
													targetTagList.add(targetTag);
													tagSpec.setC1G2TargetTagList(targetTagList);

													// Add the tag spec to the access command.
													accessCommand.setAirProtocolTagSpec(tagSpec);
												}
												else if (tfc[2].contains("*") && tfc[3].contains("*"))
												{
													targetTag.setPointer(new UnsignedShort(offset));
													BitArray_HEX tagMask = new BitArray_HEX("FFFFFFFFFC");
													targetTag.setTagMask(tagMask);

													validEpc = validEpc.replaceFirst("\\*", "000000");
													validEpc = validEpc.replaceFirst("\\*", "0");

													String bin = engine.convert(validEpc,
															params,
															LevelTypeList.BINARY);

													String TARGET_EPC = engine.bin2hex(bin.substring(0, 40));

													BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
													targetTag.setTagData(tagData);

													// Add a list of target tags to the tag spec.
													List <C1G2TargetTag> targetTagList =
															new ArrayList<C1G2TargetTag>();
													targetTagList.add(targetTag);
													tagSpec.setC1G2TargetTagList(targetTagList);

													// Add the tag spec to the access command.
													accessCommand.setAirProtocolTagSpec(tagSpec);
												}
												else if (tfc[3].contains("*"))
												{
													targetTag.setPointer(new UnsignedShort(offset));
													BitArray_HEX tagMask = new BitArray_HEX("FFFFFFFFFFFFFFC");
													targetTag.setTagMask(tagMask);

													validEpc = validEpc.replaceFirst("\\*", "0");

													String bin = engine.convert(validEpc,
															params,
															LevelTypeList.BINARY);

													String TARGET_EPC = engine.bin2hex(bin.substring(0, 60));

													BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
													targetTag.setTagData(tagData);

													// Add a list of target tags to the tag spec.
													List <C1G2TargetTag> targetTagList =
															new ArrayList<C1G2TargetTag>();
													targetTagList.add(targetTag);
													tagSpec.setC1G2TargetTagList(targetTagList);

													// Add the tag spec to the access command.
													accessCommand.setAirProtocolTagSpec(tagSpec);
												}
											}
											else if (validEpc.contains("[") || validEpc.contains("]"))
											{
												try {
													throw new ImplementationException("epc-tag Syntax [lo-hi] is not implemented.");
												} catch (ImplementationException e) {
													e.printStackTrace();
												}
											}
											else 
											{
												// The EPC data starts at offset 0x20.
												// Start reading or writing from there.
												targetTag.setPointer(new UnsignedShort(offset));
												// This is the mask we'll use to compare the EPC.
												// We want to match all bits of the EPC, so all mask bits are set.
												String TAG_MASK = "";
												for (int i=0; i<length; i++)
													TAG_MASK = TAG_MASK + "F";

												BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
												targetTag.setTagMask(tagMask);

												String bin = engine.convert(validEpc,
														params,
														LevelTypeList.BINARY);

												String TARGET_EPC = engine.bin2hex(bin);

												BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
												targetTag.setTagData(tagData);

												// Add a list of target tags to the tag spec.
												List <C1G2TargetTag> targetTagList =
														new ArrayList<C1G2TargetTag>();
												targetTagList.add(targetTag);
												tagSpec.setC1G2TargetTagList(targetTagList);

												// Add the tag spec to the access command.
												accessCommand.setAirProtocolTagSpec(tagSpec);
											}
										}
										else if (filterList.getFieldspec().getFormat().equalsIgnoreCase("epc-hex"))
										{
											// We want to check memory bank 1 (the EPC memory bank).
											TwoBitField memBank = new TwoBitField();
											if (bank.equals("0"))
												memBank = reservedBank;
											else if (bank.equals("1"))
												memBank = epcBank;
											else if (bank.equals("2"))
												memBank = tidBank;
											else if (bank.equals("3"))
												memBank = userBank;
											
											targetTag.setMB(memBank);

											String validEpc = pat.toString();
											int indexof = validEpc.indexOf("*");

											// The EPC data starts at offset 0x20.
											// Start reading or writing from there.
											targetTag.setPointer(new UnsignedShort(offset));
											// This is the mask we'll use to compare the EPC.
											// We want to match all bits of the EPC, so all mask bits are set.
											String TAG_MASK = "";
											if (indexof == 1)
											{
												break;
											}
											else
											{
												for (int i=0; i<length; i++)
													TAG_MASK = TAG_MASK + "F";
											}

											BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
											targetTag.setTagMask(tagMask);
											// We only only to operate on tags with this EPC.
											String TARGET_EPC = pat.toString();
											BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
											targetTag.setTagData(tagData);

											// Add a list of target tags to the tag spec.
											List <C1G2TargetTag> targetTagList =
													new ArrayList<C1G2TargetTag>();
											targetTagList.add(targetTag);
											tagSpec.setC1G2TargetTagList(targetTagList);

											// Add the tag spec to the access command.
											accessCommand.setAirProtocolTagSpec(tagSpec);
										}
										else if (filterList.getFieldspec().getFormat().equalsIgnoreCase("epc-decimal"))
										{
											// We want to check memory bank 1 (the EPC memory bank).
											TwoBitField memBank = new TwoBitField();
											if (bank.equals("0"))
												memBank = reservedBank;
											else if (bank.equals("1"))
												memBank = epcBank;
											else if (bank.equals("2"))
												memBank = tidBank;
											else if (bank.equals("3"))
												memBank = userBank;
											
											targetTag.setMB(memBank);

											String validEpc = pat.toString();
											int indexof = validEpc.indexOf("*");

											// The EPC data starts at offset 0x20.
											// Start reading or writing from there.
											targetTag.setPointer(new UnsignedShort(offset));
											// This is the mask we'll use to compare the EPC.
											// We want to match all bits of the EPC, so all mask bits are set.
											String TAG_MASK = "";
											if (indexof == 1)
											{
												break;
											}
											else
											{
												for (int i=0; i<length; i++)
													TAG_MASK = TAG_MASK + "F";
											}

											BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
											targetTag.setTagMask(tagMask);
											// We only only to operate on tags with this EPC.
											String bin = engine.dec2bin(validEpc);
											String TARGET_EPC = engine.bin2hex(bin);
											BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
											targetTag.setTagData(tagData);

											// Add a list of target tags to the tag spec.
											List <C1G2TargetTag> targetTagList =
													new ArrayList<C1G2TargetTag>();
											targetTagList.add(targetTag);
											tagSpec.setC1G2TargetTagList(targetTagList);

											// Add the tag spec to the access command.
											accessCommand.setAirProtocolTagSpec(tagSpec);
										}
									}
									else if (filterList.getFieldspec().getDatatype().equalsIgnoreCase("uint"))
									{
										if (filterList.getFieldspec().getFormat().equalsIgnoreCase("hex"))
										{
											// We want to check memory bank 1 (the EPC memory bank).
											TwoBitField memBank = new TwoBitField();
											if (bank.equals("0"))
												memBank = reservedBank;
											else if (bank.equals("1"))
												memBank = epcBank;
											else if (bank.equals("2"))
												memBank = tidBank;
											else if (bank.equals("3"))
												memBank = userBank;
											
											targetTag.setMB(memBank);

											String validdata = pat.toString();
											if (validdata.contains("*"))
											{
												int indexof = validdata.indexOf("*");

												// The EPC data starts at offset 0x20.
												// Start reading or writing from there.
												targetTag.setPointer(new UnsignedShort(offset));
												// This is the mask we'll use to compare the EPC.
												// We want to match all bits of the EPC, so all mask bits are set.
												String TAG_MASK = "";
												if (indexof == 1)
												{
													break;
												}
												else
												{
													for (int i=0; i<indexof; i++)
														TAG_MASK = TAG_MASK + "F";
												}
												BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
												targetTag.setTagMask(tagMask);

												// We only only to operate on tags with this EPC.
												String TARGET_EPC = validdata.substring(0, indexof);
												BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
												targetTag.setTagData(tagData);

												// Add a list of target tags to the tag spec.
												List <C1G2TargetTag> targetTagList =
														new ArrayList<C1G2TargetTag>();
												targetTagList.add(targetTag);
												tagSpec.setC1G2TargetTagList(targetTagList);

												// Add the tag spec to the access command.
												accessCommand.setAirProtocolTagSpec(tagSpec);
											}
											else if (validdata.contains("[") || validdata.contains("]"))
											{
												try {
													throw new ImplementationException("Hex Syntax [lo-hi] is not implemented.");
												} catch (ImplementationException e) {
													e.printStackTrace();
												}
											}
											else if (validdata.contains("&") || validdata.contains("="))
											{
												try {
													throw new ImplementationException("Hex Syntax &x=x is not implemented.");
												} catch (ImplementationException e) {
													e.printStackTrace();
												}
											}
											else 
											{
												// The EPC data starts at offset 0x20.
												// Start reading or writing from there.
												targetTag.setPointer(new UnsignedShort(offset));
												// This is the mask we'll use to compare the EPC.
												// We want to match all bits of the EPC, so all mask bits are set.
												String TAG_MASK = "";
												for (int i=0; i<length; i++)
													TAG_MASK = TAG_MASK + "F";

												BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
												targetTag.setTagMask(tagMask);

												// We only only to operate on tags with this EPC.
												String TARGET_EPC = validdata;
												BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
												targetTag.setTagData(tagData);

												// Add a list of target tags to the tag spec.
												List <C1G2TargetTag> targetTagList =
														new ArrayList<C1G2TargetTag>();
												targetTagList.add(targetTag);
												tagSpec.setC1G2TargetTagList(targetTagList);

												// Add the tag spec to the access command.
												accessCommand.setAirProtocolTagSpec(tagSpec);
											}
										}
										else if (filterList.getFieldspec().getFormat().equalsIgnoreCase("decimal"))
										{
											// We want to check memory bank 1 (the EPC memory bank).
											TwoBitField memBank = new TwoBitField();
											if (bank.equals("0"))
												memBank = reservedBank;
											else if (bank.equals("1"))
												memBank = epcBank;
											else if (bank.equals("2"))
												memBank = tidBank;
											else if (bank.equals("3"))
												memBank = userBank;
											
											targetTag.setMB(memBank);

											String validdata = pat.toString();
											if (validdata.contains("*"))
											{
												/*int indexof = validdata.indexOf("*");

											    	// The EPC data starts at offset 0x20.
												    // Start reading or writing from there.
												    targetTag.setPointer(new UnsignedShort(offset));
												    // This is the mask we'll use to compare the EPC.
												    // We want to match all bits of the EPC, so all mask bits are set.
												    String TAG_MASK = "";
												    for (int i=0; i<indexof; i++)
												       	TAG_MASK = TAG_MASK + "F";

												    BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
												    targetTag.setTagMask(tagMask);

												    // We only only to operate on tags with this EPC.
												 	String TARGET_EPC = validdata.substring(0, indexof);
												    BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
												    targetTag.setTagData(tagData);

												    // Add a list of target tags to the tag spec.
												    List <C1G2TargetTag> targetTagList =
												        new ArrayList<C1G2TargetTag>();
												    targetTagList.add(targetTag);
												    tagSpec.setC1G2TargetTagList(targetTagList);

												    // Add the tag spec to the access command.
												    accessCommand.setAirProtocolTagSpec(tagSpec);*/

												try {
													throw new ImplementationException("Decimal Syntax * is not implemented.");
												} catch (ImplementationException e) {
													e.printStackTrace();
												}
											}
											else if (validdata.contains("[") || validdata.contains("]"))
											{
												try {
													throw new ImplementationException("Decimal Syntax [lo-hi] is not implemented.");
												} catch (ImplementationException e) {
													e.printStackTrace();
												}
											}
											else 
											{	    
												// The EPC data starts at offset 0x20.
												// Start reading or writing from there.
												targetTag.setPointer(new UnsignedShort(offset));
												// This is the mask we'll use to compare the EPC.
												// We want to match all bits of the EPC, so all mask bits are set.
												String TAG_MASK = "";
												for (int i=0; i<length; i++)
													TAG_MASK = TAG_MASK + "F";

												BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
												targetTag.setTagMask(tagMask);

												// We only only to operate on tags with this EPC.
												String bin = engine.dec2bin(validdata);
												String TARGET_EPC = engine.bin2hex(bin);
												BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
												targetTag.setTagData(tagData);

												// Add a list of target tags to the tag spec.
												List <C1G2TargetTag> targetTagList =
														new ArrayList<C1G2TargetTag>();
												targetTagList.add(targetTag);
												tagSpec.setC1G2TargetTagList(targetTagList);

												// Add the tag spec to the access command.
												accessCommand.setAirProtocolTagSpec(tagSpec);
											}
										}
									}
									else if (filterList.getFieldspec().getDatatype().equalsIgnoreCase("bits"))
									{
										try {
											throw new ImplementationException("bits Datatype is not implemented.");
										} catch (ImplementationException e) {
											e.printStackTrace();
										}
									}
									else if (filterList.getFieldspec().getDatatype().equalsIgnoreCase("iso-15962-string"))
									{
										try {
											throw new ImplementationException("iso-15962-string Datatype is not implemented.");
										} catch (ImplementationException e) {
											e.printStackTrace();
										}
									}

								} else {
									// symbolic fieldnames
									// datatype: "uint", format: "hex"

									// user-defined symbolic field name
									SymbolicField symbolicfield = SymbolicFieldRepo.getInstance().getSymbolicField(fieldName);
									if(symbolicfield == null) {
										try {
											throw new ImplementationException("symbolic field does not exist for the fieldname "+ fieldName);
										} catch (ImplementationException e) {
											e.printStackTrace();
										}
									}

									String bank = symbolicfield.getBank()+"";
									int length = symbolicfield.getLength();
									int offset = symbolicfield.getOffset();

									// We want to check memory bank.
									TwoBitField memBank = new TwoBitField();
									if (bank.equals("0"))
										memBank = reservedBank;
									else if (bank.equals("1"))
										memBank = epcBank;
									else if (bank.equals("2"))
										memBank = tidBank;
									else if (bank.equals("3"))
										memBank = userBank;
									
									targetTag.setMB(memBank);

									// The EPC data starts at offset 0x20.
									// Start reading or writing from there.
									targetTag.setPointer(new UnsignedShort(offset));
									// This is the mask we'll use to compare the EPC.
									// We want to match all bits of the EPC, so all mask bits are set.
									String TAG_MASK = "";
									for (int i=0; i<length; i++)
										TAG_MASK = TAG_MASK + "F";

									BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
									targetTag.setTagMask(tagMask);
									// We only only to operate on tags with this EPC.
									String TARGET_EPC = pat.toString();
									BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
									targetTag.setTagData(tagData);

									// Add a list of target tags to the tag spec.
									List <C1G2TargetTag> targetTagList =
											new ArrayList<C1G2TargetTag>();
									targetTagList.add(targetTag);
									tagSpec.setC1G2TargetTagList(targetTagList);

									// Add the tag spec to the access command.
									accessCommand.setAirProtocolTagSpec(tagSpec);
								}
							}
							else if (filterList.getIncludeExclude().equals("EXCLUDE"))
							{
								targetTag.setMatch(new Bit(0));

								String fieldName = filterList.getFieldspec().getFieldname(); 
								if (fieldName==null)
									fieldName = "epc";
								if(fieldName.equalsIgnoreCase("killPwd")) {
									// killPwd fieldname
									// same as "@0.32"
									// We want to check memory bank.
									TwoBitField memBank = reservedBank;
									// Clear bit 0 and set bit 1 (bank 1 in binary).
									targetTag.setMB(memBank);

									// The EPC data starts at offset 0x20.
									// Start reading or writing from there.
									targetTag.setPointer(new UnsignedShort(0));
									// This is the mask we'll use to compare the EPC.
									// We want to match all bits of the EPC, so all mask bits are set.
									String TAG_MASK = "";
									for (int i=0; i<32; i++)
										TAG_MASK = TAG_MASK + "F";

									BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
									targetTag.setTagMask(tagMask);
									// We only only to operate on tags with this EPC.
									String TARGET_EPC = pat.toString();
									BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
									targetTag.setTagData(tagData);

									// Add a list of target tags to the tag spec.
									List <C1G2TargetTag> targetTagList =
											new ArrayList<C1G2TargetTag>();
									targetTagList.add(targetTag);
									tagSpec.setC1G2TargetTagList(targetTagList);

									// Add the tag spec to the access command.
									accessCommand.setAirProtocolTagSpec(tagSpec);

								} else if(fieldName.equalsIgnoreCase("accessPwd")) {
									// accessPwd fieldname
									// same as "@0.32.32"
									// We want to check memory bank.
									TwoBitField memBank = reservedBank;
									targetTag.setMB(memBank);

									// The EPC data starts at offset 0x20.
									// Start reading or writing from there.
									targetTag.setPointer(new UnsignedShort(32));
									// This is the mask we'll use to compare the EPC.
									// We want to match all bits of the EPC, so all mask bits are set.
									String TAG_MASK = "";
									for (int i=0; i<32; i++)
										TAG_MASK = TAG_MASK + "F";

									BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
									targetTag.setTagMask(tagMask);
									// We only only to operate on tags with this EPC.
									String TARGET_EPC = pat.toString();
									BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
									targetTag.setTagData(tagData);

									// Add a list of target tags to the tag spec.
									List <C1G2TargetTag> targetTagList =
											new ArrayList<C1G2TargetTag>();
									targetTagList.add(targetTag);
									tagSpec.setC1G2TargetTagList(targetTagList);

									// Add the tag spec to the access command.
									accessCommand.setAirProtocolTagSpec(tagSpec);

								} else if(fieldName.equalsIgnoreCase("epc")) {
									// FIXME : fieldname "epcBank", is it right?
									// return the contents of epc bank
									// datatype: "bits", format: "hex"
									// We want to check memory bank.
									TwoBitField memBank = epcBank;
									targetTag.setMB(memBank);

									// The EPC data starts at offset 0x20.
									// Start reading or writing from there.
									targetTag.setPointer(new UnsignedShort(32));
									// This is the mask we'll use to compare the EPC.
									// We want to match all bits of the EPC, so all mask bits are set.
									String TAG_MASK = "";
									for (int i=0; i<96; i++)
										TAG_MASK = TAG_MASK + "F";

									BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
									targetTag.setTagMask(tagMask);
									// We only only to operate on tags with this EPC.
									String TARGET_EPC = pat.toString();
									BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
									targetTag.setTagData(tagData);

									// Add a list of target tags to the tag spec.
									List <C1G2TargetTag> targetTagList =
											new ArrayList<C1G2TargetTag>();
									targetTagList.add(targetTag);
									tagSpec.setC1G2TargetTagList(targetTagList);

									// Add the tag spec to the access command.
									accessCommand.setAirProtocolTagSpec(tagSpec);

								}  else if(fieldName.equalsIgnoreCase("epcBank")) {
									// FIXME : fieldname "epcBank", is it right?
									// return the contents of epc bank
									// datatype: "bits", format: "hex"
									// We want to check memory bank.
									TwoBitField memBank = epcBank;
									targetTag.setMB(memBank);

									// The EPC data starts at offset 0x20.
									// Start reading or writing from there.
									targetTag.setPointer(new UnsignedShort(0));
									// This is the mask we'll use to compare the EPC.
									// We want to match all bits of the EPC, so all mask bits are set.
									String TAG_MASK = "";
									for (int i=0; i<128; i++)
										TAG_MASK = TAG_MASK + "F";

									BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
									targetTag.setTagMask(tagMask);
									// We only only to operate on tags with this EPC.
									String TARGET_EPC = pat.toString();
									BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
									targetTag.setTagData(tagData);

									// Add a list of target tags to the tag spec.
									List <C1G2TargetTag> targetTagList =
											new ArrayList<C1G2TargetTag>();
									targetTagList.add(targetTag);
									tagSpec.setC1G2TargetTagList(targetTagList);

									// Add the tag spec to the access command.
									accessCommand.setAirProtocolTagSpec(tagSpec);

								} else if(fieldName.equalsIgnoreCase("tidBank")) {
									// fieldname "tidBank"
									// return the contents of tid bank
									// datatype: "bits", format: "hex"

									try {
										throw new ImplementationException("fieldname \"tidBank\": access to tid bank is not implemented");
									} catch (ImplementationException e) {
										e.printStackTrace();
									}

								} else if(fieldName.equalsIgnoreCase("userBank")) {
									// return the contents of user memory bank
									// datatype: "bits", format: "hex"
									/*ECReportMemberField ecReportMemberField = new ECReportMemberField();
										ecReportMemberField.setName(fieldName);
										ecReportMemberField.setValue(tag.getUserMemory());	// tag.getUserMemory() is already hex string, no need to convert

										ecReportMemberFields.add(ecReportMemberField);
									 */
									try {
										throw new ImplementationException("fieldname \"userBank\": access to user bank is not implemented");
									} catch (ImplementationException e) {
										e.printStackTrace();
									}

								} else if(fieldName.equalsIgnoreCase("afi")) {
									// application family identifier
									// same as "@1.8.24"
									// datatype: "uint", format: "hex"

									// We want to check memory bank.
									TwoBitField memBank = epcBank;
									targetTag.setMB(memBank);

									// The EPC data starts at offset 0x20.
									// Start reading or writing from there.
									targetTag.setPointer(new UnsignedShort(24));
									// This is the mask we'll use to compare the EPC.
									// We want to match all bits of the EPC, so all mask bits are set.
									String TAG_MASK = "";
									for (int i=0; i<8; i++)
										TAG_MASK = TAG_MASK + "F";

									BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
									targetTag.setTagMask(tagMask);
									// We only only to operate on tags with this EPC.
									String TARGET_EPC = pat.toString();
									BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
									targetTag.setTagData(tagData);

									// Add a list of target tags to the tag spec.
									List <C1G2TargetTag> targetTagList =
											new ArrayList<C1G2TargetTag>();
									targetTagList.add(targetTag);
									tagSpec.setC1G2TargetTagList(targetTagList);

									// Add the tag spec to the access command.
									accessCommand.setAirProtocolTagSpec(tagSpec);

								} else if(fieldName.equalsIgnoreCase("nsi")) {
									// Numbering System Identifier (NSI)
									// same as "@1.9.23"
									// datatype: "uint", format: "hex"

									// We want to check memory bank.
									TwoBitField memBank = epcBank;
									targetTag.setMB(memBank);

									// The EPC data starts at offset 0x20.
									// Start reading or writing from there.
									targetTag.setPointer(new UnsignedShort(23));
									// This is the mask we'll use to compare the EPC.
									// We want to match all bits of the EPC, so all mask bits are set.
									String TAG_MASK = "";
									for (int i=0; i<9; i++)
										TAG_MASK = TAG_MASK + "F";

									BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
									targetTag.setTagMask(tagMask);
									// We only only to operate on tags with this EPC.
									String TARGET_EPC = pat.toString();
									BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
									targetTag.setTagData(tagData);

									// Add a list of target tags to the tag spec.
									List <C1G2TargetTag> targetTagList =
											new ArrayList<C1G2TargetTag>();
									targetTagList.add(targetTag);
									tagSpec.setC1G2TargetTagList(targetTagList);

									// Add the tag spec to the access command.
									accessCommand.setAirProtocolTagSpec(tagSpec);

								} else if (fieldName.startsWith("@")) {
									// fieldnames start with "@"

									String[] part = fieldName.substring(1).split("\\.");
									String bank = part[0];
									int length = Integer.parseInt(part[1])/4;
									int offset = Integer.parseInt(part[2]);

									if (filterList.getFieldspec().getDatatype().equalsIgnoreCase("epc"))
									{
										if (filterList.getFieldspec().getFormat().equalsIgnoreCase("epc-pure"))
										{
											// We want to check memory bank 1 (the EPC memory bank).
											TwoBitField memBank = new TwoBitField();
											if (bank.equals("0"))
												memBank = reservedBank;
											else if (bank.equals("1"))
												memBank = epcBank;
											else if (bank.equals("2"))
												memBank = tidBank;
											else if (bank.equals("3"))
												memBank = userBank;
											
											targetTag.setMB(memBank);

											params.put("taglength", "96");
											params.put("filter", "3");
											params.put("companyprefixlength", "7");
											String validEpc = pat.toString();

											if (validEpc.contains("*"))
											{
												String[] parts = validEpc.split(":");
												String[] tfc = parts[4].split("\\.");
												if (tfc[0].contains("*") && tfc[1].contains("*") && tfc[2].contains("*"))
												{
													targetTag.setPointer(new UnsignedShort(offset));
													BitArray_HEX tagMask = new BitArray_HEX("FFFC");
													targetTag.setTagMask(tagMask);

													validEpc = validEpc.replaceFirst("\\*", "0000000");
													validEpc = validEpc.replaceFirst("\\*", "000000");
													validEpc = validEpc.replaceFirst("\\*", "0");

													String bin = engine.convert(validEpc,
															params,
															LevelTypeList.BINARY);

													String TARGET_EPC = engine.bin2hex(bin.substring(0, 16));

													BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
													targetTag.setTagData(tagData);

													// Add a list of target tags to the tag spec.
													List <C1G2TargetTag> targetTagList =
															new ArrayList<C1G2TargetTag>();
													targetTagList.add(targetTag);
													tagSpec.setC1G2TargetTagList(targetTagList);

													// Add the tag spec to the access command.
													accessCommand.setAirProtocolTagSpec(tagSpec);
												}
												else if (tfc[1].contains("*") && tfc[2].contains("*"))
												{
													targetTag.setPointer(new UnsignedShort(offset));
													BitArray_HEX tagMask = new BitArray_HEX("FFFFFFFFFC");
													targetTag.setTagMask(tagMask);

													validEpc = validEpc.replaceFirst("\\*", "000000");
													validEpc = validEpc.replaceFirst("\\*", "0");

													String bin = engine.convert(validEpc,
															params,
															LevelTypeList.BINARY);

													String TARGET_EPC = engine.bin2hex(bin.substring(0, 40));

													BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
													targetTag.setTagData(tagData);

													// Add a list of target tags to the tag spec.
													List <C1G2TargetTag> targetTagList =
															new ArrayList<C1G2TargetTag>();
													targetTagList.add(targetTag);
													tagSpec.setC1G2TargetTagList(targetTagList);

													// Add the tag spec to the access command.
													accessCommand.setAirProtocolTagSpec(tagSpec);
												}
												else if (tfc[2].contains("*"))
												{
													targetTag.setPointer(new UnsignedShort(offset));
													BitArray_HEX tagMask = new BitArray_HEX("FFFFFFFFFFFFFFC");
													targetTag.setTagMask(tagMask);

													validEpc = validEpc.replaceFirst("\\*", "0");

													String bin = engine.convert(validEpc,
															params,
															LevelTypeList.BINARY);

													String TARGET_EPC = engine.bin2hex(bin.substring(0, 60));

													BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
													targetTag.setTagData(tagData);

													// Add a list of target tags to the tag spec.
													List <C1G2TargetTag> targetTagList =
															new ArrayList<C1G2TargetTag>();
													targetTagList.add(targetTag);
													tagSpec.setC1G2TargetTagList(targetTagList);

													// Add the tag spec to the access command.
													accessCommand.setAirProtocolTagSpec(tagSpec);
												}

											}
											else if (validEpc.contains("[") || validEpc.contains("]"))
											{
												try {
													throw new ImplementationException("epc-pure Syntax [lo-hi] is not implemented.");
												} catch (ImplementationException e) {
													e.printStackTrace();
												}
											}
											else 
											{
												// The EPC data starts at offset 0x20.
												// Start reading or writing from there.
												targetTag.setPointer(new UnsignedShort(offset));
												// This is the mask we'll use to compare the EPC.
												// We want to match all bits of the EPC, so all mask bits are set.
												String TAG_MASK = "";
												for (int i=0; i<length; i++)
													TAG_MASK = TAG_MASK + "F";

												BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
												targetTag.setTagMask(tagMask);

												String bin = engine.convert(validEpc,
														params,
														LevelTypeList.BINARY);

												String TARGET_EPC = engine.bin2hex(bin);

												BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
												targetTag.setTagData(tagData);

												// Add a list of target tags to the tag spec.
												List <C1G2TargetTag> targetTagList =
														new ArrayList<C1G2TargetTag>();
												targetTagList.add(targetTag);
												tagSpec.setC1G2TargetTagList(targetTagList);

												// Add the tag spec to the access command.
												accessCommand.setAirProtocolTagSpec(tagSpec);
											}
										}
										else if (filterList.getFieldspec().getFormat().equalsIgnoreCase("epc-tag"))
										{
											// We want to check memory bank 1 (the EPC memory bank).
											TwoBitField memBank = new TwoBitField();
											if (bank.equals("0"))
												memBank = reservedBank;
											else if (bank.equals("1"))
												memBank = epcBank;
											else if (bank.equals("2"))
												memBank = tidBank;
											else if (bank.equals("3"))
												memBank = userBank;
											
											targetTag.setMB(memBank);

											// We only only to operate on tags with this EPC.
											params.put("taglength", "96");
											params.put("filter", "3");
											params.put("companyprefixlength", "7");
											String validEpc = pat.toString();

											if (validEpc.contains("*"))
											{
												String[] parts = validEpc.split(":");
												String[] tfc = parts[4].split("\\.");
												if (tfc[1].contains("*") && tfc[2].contains("*") && tfc[3].contains("*"))
												{
													targetTag.setPointer(new UnsignedShort(offset));
													BitArray_HEX tagMask = new BitArray_HEX("FFFC");
													targetTag.setTagMask(tagMask);

													validEpc = validEpc.replaceFirst("\\*", "0000000");
													validEpc = validEpc.replaceFirst("\\*", "000000");
													validEpc = validEpc.replaceFirst("\\*", "0");

													String bin = engine.convert(validEpc,
															params,
															LevelTypeList.BINARY);

													String TARGET_EPC = engine.bin2hex(bin.substring(0, 16));

													BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
													targetTag.setTagData(tagData);

													// Add a list of target tags to the tag spec.
													List <C1G2TargetTag> targetTagList =
															new ArrayList<C1G2TargetTag>();
													targetTagList.add(targetTag);
													tagSpec.setC1G2TargetTagList(targetTagList);

													// Add the tag spec to the access command.
													accessCommand.setAirProtocolTagSpec(tagSpec);
												}
												else if (tfc[2].contains("*") && tfc[3].contains("*"))
												{
													targetTag.setPointer(new UnsignedShort(offset));
													BitArray_HEX tagMask = new BitArray_HEX("FFFFFFFFFC");
													targetTag.setTagMask(tagMask);

													validEpc = validEpc.replaceFirst("\\*", "000000");
													validEpc = validEpc.replaceFirst("\\*", "0");

													String bin = engine.convert(validEpc,
															params,
															LevelTypeList.BINARY);

													String TARGET_EPC = engine.bin2hex(bin.substring(0, 40));

													BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
													targetTag.setTagData(tagData);

													// Add a list of target tags to the tag spec.
													List <C1G2TargetTag> targetTagList =
															new ArrayList<C1G2TargetTag>();
													targetTagList.add(targetTag);
													tagSpec.setC1G2TargetTagList(targetTagList);

													// Add the tag spec to the access command.
													accessCommand.setAirProtocolTagSpec(tagSpec);
												}
												else if (tfc[3].contains("*"))
												{
													targetTag.setPointer(new UnsignedShort(offset));
													BitArray_HEX tagMask = new BitArray_HEX("FFFFFFFFFFFFFFC");
													targetTag.setTagMask(tagMask);

													validEpc = validEpc.replaceFirst("\\*", "0");

													String bin = engine.convert(validEpc,
															params,
															LevelTypeList.BINARY);

													String TARGET_EPC = engine.bin2hex(bin.substring(0, 60));

													BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
													targetTag.setTagData(tagData);

													// Add a list of target tags to the tag spec.
													List <C1G2TargetTag> targetTagList =
															new ArrayList<C1G2TargetTag>();
													targetTagList.add(targetTag);
													tagSpec.setC1G2TargetTagList(targetTagList);

													// Add the tag spec to the access command.
													accessCommand.setAirProtocolTagSpec(tagSpec);
												}
											}
											else if (validEpc.contains("[") || validEpc.contains("]"))
											{
												try {
													throw new ImplementationException("epc-tag Syntax [lo-hi] is not implemented.");
												} catch (ImplementationException e) {
													e.printStackTrace();
												}
											}
											else 
											{
												// The EPC data starts at offset 0x20.
												// Start reading or writing from there.
												targetTag.setPointer(new UnsignedShort(offset));
												// This is the mask we'll use to compare the EPC.
												// We want to match all bits of the EPC, so all mask bits are set.
												String TAG_MASK = "";
												for (int i=0; i<length; i++)
													TAG_MASK = TAG_MASK + "F";

												BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
												targetTag.setTagMask(tagMask);

												String bin = engine.convert(validEpc,
														params,
														LevelTypeList.BINARY);

												String TARGET_EPC = engine.bin2hex(bin);

												BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
												targetTag.setTagData(tagData);

												// Add a list of target tags to the tag spec.
												List <C1G2TargetTag> targetTagList =
														new ArrayList<C1G2TargetTag>();
												targetTagList.add(targetTag);
												tagSpec.setC1G2TargetTagList(targetTagList);

												// Add the tag spec to the access command.
												accessCommand.setAirProtocolTagSpec(tagSpec);
											}
										}
										else if (filterList.getFieldspec().getFormat().equalsIgnoreCase("epc-hex"))
										{
											// We want to check memory bank 1 (the EPC memory bank).
											TwoBitField memBank = new TwoBitField();
											if (bank.equals("0"))
												memBank = reservedBank;
											else if (bank.equals("1"))
												memBank = epcBank;
											else if (bank.equals("2"))
												memBank = tidBank;
											else if (bank.equals("3"))
												memBank = userBank;
											
											targetTag.setMB(memBank);

											String validEpc = pat.toString();
											int indexof = validEpc.indexOf("*");

											// The EPC data starts at offset 0x20.
											// Start reading or writing from there.
											targetTag.setPointer(new UnsignedShort(offset));
											// This is the mask we'll use to compare the EPC.
											// We want to match all bits of the EPC, so all mask bits are set.
											String TAG_MASK = "";
											if (indexof == 1)
											{
												break;
											}
											else
											{												
												for (int i=0; i<length; i++)
													TAG_MASK = TAG_MASK + "F";
											}

											BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
											targetTag.setTagMask(tagMask);
											// We only only to operate on tags with this EPC.
											String TARGET_EPC = pat.toString();
											BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
											targetTag.setTagData(tagData);

											// Add a list of target tags to the tag spec.
											List <C1G2TargetTag> targetTagList =
													new ArrayList<C1G2TargetTag>();
											targetTagList.add(targetTag);
											tagSpec.setC1G2TargetTagList(targetTagList);

											// Add the tag spec to the access command.
											accessCommand.setAirProtocolTagSpec(tagSpec);
										}
										else if (filterList.getFieldspec().getFormat().equalsIgnoreCase("epc-decimal"))
										{
											// We want to check memory bank 1 (the EPC memory bank).
											TwoBitField memBank = new TwoBitField();
											if (bank.equals("0"))
												memBank = reservedBank;
											else if (bank.equals("1"))
												memBank = epcBank;
											else if (bank.equals("2"))
												memBank = tidBank;
											else if (bank.equals("3"))
												memBank = userBank;
											
											targetTag.setMB(memBank);

											String validEpc = pat.toString();
											int indexof = validEpc.indexOf("*");

											// The EPC data starts at offset 0x20.
											// Start reading or writing from there.
											targetTag.setPointer(new UnsignedShort(offset));
											// This is the mask we'll use to compare the EPC.
											// We want to match all bits of the EPC, so all mask bits are set.
											String TAG_MASK = "";
											if (indexof == 1)
											{
												break;
											}
											else
											{												
												for (int i=0; i<length; i++)
													TAG_MASK = TAG_MASK + "F";
											}

											BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
											targetTag.setTagMask(tagMask);
											// We only only to operate on tags with this EPC.
											String bin = engine.dec2bin(validEpc);
											String TARGET_EPC = engine.bin2hex(bin);
											BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
											targetTag.setTagData(tagData);

											// Add a list of target tags to the tag spec.
											List <C1G2TargetTag> targetTagList =
													new ArrayList<C1G2TargetTag>();
											targetTagList.add(targetTag);
											tagSpec.setC1G2TargetTagList(targetTagList);

											// Add the tag spec to the access command.
											accessCommand.setAirProtocolTagSpec(tagSpec);
										}
									}
									else if (filterList.getFieldspec().getDatatype().equalsIgnoreCase("uint"))
									{
										if (filterList.getFieldspec().getFormat().equalsIgnoreCase("hex"))
										{
											// We want to check memory bank 1 (the EPC memory bank).
											TwoBitField memBank = new TwoBitField();
											if (bank.equals("0"))
												memBank = reservedBank;
											else if (bank.equals("1"))
												memBank = epcBank;
											else if (bank.equals("2"))
												memBank = tidBank;
											else if (bank.equals("3"))
												memBank = userBank;
											
											targetTag.setMB(memBank);

											String validdata = pat.toString();
											if (validdata.contains("*"))
											{
												int indexof = validdata.indexOf("*");

												// The EPC data starts at offset 0x20.
												// Start reading or writing from there.
												targetTag.setPointer(new UnsignedShort(offset));
												// This is the mask we'll use to compare the EPC.
												// We want to match all bits of the EPC, so all mask bits are set.
												String TAG_MASK = "";
												if (indexof == 1)
												{
													break;
												}
												else
												{												
													for (int i=0; i<indexof; i++)
														TAG_MASK = TAG_MASK + "F";
												}

												BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
												targetTag.setTagMask(tagMask);

												// We only only to operate on tags with this EPC.
												String TARGET_EPC = validdata.substring(0, indexof);
												BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
												targetTag.setTagData(tagData);

												// Add a list of target tags to the tag spec.
												List <C1G2TargetTag> targetTagList =
														new ArrayList<C1G2TargetTag>();
												targetTagList.add(targetTag);
												tagSpec.setC1G2TargetTagList(targetTagList);

												// Add the tag spec to the access command.
												accessCommand.setAirProtocolTagSpec(tagSpec);
											}
											else if (validdata.contains("[") || validdata.contains("]"))
											{
												try {
													throw new ImplementationException("Hex Syntax [lo-hi] is not implemented.");
												} catch (ImplementationException e) {
													e.printStackTrace();
												}
											}
											else if (validdata.contains("&") || validdata.contains("="))
											{
												try {
													throw new ImplementationException("Hex Syntax &x=x is not implemented.");
												} catch (ImplementationException e) {
													e.printStackTrace();
												}
											}
											else 
											{
												// The EPC data starts at offset 0x20.
												// Start reading or writing from there.
												targetTag.setPointer(new UnsignedShort(offset));
												// This is the mask we'll use to compare the EPC.
												// We want to match all bits of the EPC, so all mask bits are set.
												String TAG_MASK = "";
												for (int i=0; i<length; i++)
													TAG_MASK = TAG_MASK + "F";

												BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
												targetTag.setTagMask(tagMask);

												// We only only to operate on tags with this EPC.
												String TARGET_EPC = validdata;
												BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
												targetTag.setTagData(tagData);

												// Add a list of target tags to the tag spec.
												List <C1G2TargetTag> targetTagList =
														new ArrayList<C1G2TargetTag>();
												targetTagList.add(targetTag);
												tagSpec.setC1G2TargetTagList(targetTagList);

												// Add the tag spec to the access command.
												accessCommand.setAirProtocolTagSpec(tagSpec);
											}
										}
										else if (filterList.getFieldspec().getFormat().equalsIgnoreCase("decimal"))
										{
											// We want to check memory bank 1 (the EPC memory bank).
											TwoBitField memBank = new TwoBitField();
											if (bank.equals("0"))
												memBank = reservedBank;
											else if (bank.equals("1"))
												memBank = epcBank;
											else if (bank.equals("2"))
												memBank = tidBank;
											else if (bank.equals("3"))
												memBank = userBank;
											
											targetTag.setMB(memBank);

											String validdata = pat.toString();
											if (validdata.contains("*"))
											{
												/*int indexof = validdata.indexOf("*");

											    	// The EPC data starts at offset 0x20.
												    // Start reading or writing from there.
												    targetTag.setPointer(new UnsignedShort(offset));
												    // This is the mask we'll use to compare the EPC.
												    // We want to match all bits of the EPC, so all mask bits are set.
												    String TAG_MASK = "";
												    for (int i=0; i<indexof; i++)
												       	TAG_MASK = TAG_MASK + "F";

												    BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
												    targetTag.setTagMask(tagMask);

												    // We only only to operate on tags with this EPC.
												 	String TARGET_EPC = validdata.substring(0, indexof);
												    BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
												    targetTag.setTagData(tagData);

												    // Add a list of target tags to the tag spec.
												    List <C1G2TargetTag> targetTagList =
												        new ArrayList<C1G2TargetTag>();
												    targetTagList.add(targetTag);
												    tagSpec.setC1G2TargetTagList(targetTagList);

												    // Add the tag spec to the access command.
												    accessCommand.setAirProtocolTagSpec(tagSpec);*/

												try {
													throw new ImplementationException("Decimal Syntax * is not implemented.");
												} catch (ImplementationException e) {
													e.printStackTrace();
												}
											}
											else if (validdata.contains("[") || validdata.contains("]"))
											{
												try {
													throw new ImplementationException("Decimal Syntax [lo-hi] is not implemented.");
												} catch (ImplementationException e) {
													e.printStackTrace();
												}
											}
											else 
											{	    
												// The EPC data starts at offset 0x20.
												// Start reading or writing from there.
												targetTag.setPointer(new UnsignedShort(offset));
												// This is the mask we'll use to compare the EPC.
												// We want to match all bits of the EPC, so all mask bits are set.
												String TAG_MASK = "";
												for (int i=0; i<length; i++)
													TAG_MASK = TAG_MASK + "F";

												BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
												targetTag.setTagMask(tagMask);

												// We only only to operate on tags with this EPC.
												String bin = engine.dec2bin(validdata);
												String TARGET_EPC = engine.bin2hex(bin);
												BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
												targetTag.setTagData(tagData);

												// Add a list of target tags to the tag spec.
												List <C1G2TargetTag> targetTagList =
														new ArrayList<C1G2TargetTag>();
												targetTagList.add(targetTag);
												tagSpec.setC1G2TargetTagList(targetTagList);

												// Add the tag spec to the access command.
												accessCommand.setAirProtocolTagSpec(tagSpec);
											}
										}
									}
									else if (filterList.getFieldspec().getDatatype().equalsIgnoreCase("bits"))
									{
										try {
											throw new ImplementationException("bits Datatype is not implemented.");
										} catch (ImplementationException e) {
											e.printStackTrace();
										}
									}
									else if (filterList.getFieldspec().getDatatype().equalsIgnoreCase("iso-15962-string"))
									{
										try {
											throw new ImplementationException("iso-15962-string Datatype is not implemented.");
										} catch (ImplementationException e) {
											e.printStackTrace();
										}
									}

								} else {
									// symbolic fieldnames
									// datatype: "uint", format: "hex"

									// user-defined symbolic field name
									SymbolicField symbolicfield = SymbolicFieldRepo.getInstance().getSymbolicField(fieldName);
									if(symbolicfield == null) {
										try {
											throw new ImplementationException("symbolic field does not exist for the fieldname "+ fieldName);
										} catch (ImplementationException e) {
											e.printStackTrace();
										}
									}

									String bank = symbolicfield.getBank()+"";
									int length = symbolicfield.getLength();
									int offset = symbolicfield.getOffset();

									// We want to check memory bank.
									TwoBitField memBank = new TwoBitField();
									if (bank.equals("0"))
										memBank = reservedBank;
									else if (bank.equals("1"))
										memBank = epcBank;
									else if (bank.equals("2"))
										memBank = tidBank;
									else if (bank.equals("3"))
										memBank = userBank;
									
									targetTag.setMB(memBank);

									// The EPC data starts at offset 0x20.
									// Start reading or writing from there.
									targetTag.setPointer(new UnsignedShort(offset));
									// This is the mask we'll use to compare the EPC.
									// We want to match all bits of the EPC, so all mask bits are set.
									String TAG_MASK = "";
									for (int i=0; i<length; i++)
										TAG_MASK = TAG_MASK + "F";

									BitArray_HEX tagMask = new BitArray_HEX(TAG_MASK);
									targetTag.setTagMask(tagMask);
									// We only only to operate on tags with this EPC.
									String TARGET_EPC = pat.toString();
									BitArray_HEX tagData = new BitArray_HEX(TARGET_EPC);
									targetTag.setTagData(tagData);

									// Add a list of target tags to the tag spec.
									List <C1G2TargetTag> targetTagList =
											new ArrayList<C1G2TargetTag>();
									targetTagList.add(targetTag);
									tagSpec.setC1G2TargetTagList(targetTagList);

									// Add the tag spec to the access command.
									accessCommand.setAirProtocolTagSpec(tagSpec);
								}
							}

							// A list to hold the op specs for this access command.
							List <AccessCommandOpSpec> opSpecList =
									new ArrayList<AccessCommandOpSpec>();

							// Set default opspec which for eventcycle of accessspec 3.
							C1G2Read opSpec1 = new C1G2Read();
							// Set the OpSpecID to a unique number.
							opSpec1.setOpSpecID(new UnsignedShort(1));
							opSpec1.setAccessPassword(new UnsignedInteger(0));

							// For this demo, we'll read from user memory (bank 3).
							TwoBitField opMemBank = userBank;
							opSpec1.setMB(opMemBank);

							// We'll read from the base of this memory bank (0x00).
							opSpec1.setWordPointer(new UnsignedShort(0));
							// Read two words.
							opSpec1.setWordCount(new UnsignedShort(0));
							
							opSpecList.add(opSpec1);
							
							// Are we reading or writing to the tag?
							// Add the appropriate op spec to the op spec list.
							List<CCOpSpec> opspecs = cmdspec.getOpSpecs().getOpSpec();					
							for (CCOpSpec ccopspec : opspecs)
							{
								Enumeration<Integer> enumKey = OpSpecTable.keys();
								while(enumKey.hasMoreElements()) {
									Integer key = enumKey.nextElement();
									CCOpSpec val = OpSpecTable.get(key);
									if(ccopspec.getOpName().equals(val.getOpName()))
									{
										if (ccopspec.getOpType().equalsIgnoreCase("READ"))
										{
											opSpecList.add(buildReadOpSpec(key, ccopspec));
										}
										else if (ccopspec.getOpType().equalsIgnoreCase("WRITE"))
										{
											opSpecList.add(buildWriteOpSpec(key, ccopspec));
										}
									}
								}
							}


							accessCommand.setAccessCommandOpSpecList(opSpecList);

							// Add access command to access spec.
							accessSpec.setAccessCommand(accessCommand);

							// Add an AccessReportSpec.
							// We want to get notification when the operation occurs.
							// Tell the reader to sent it to us with the ROSpec.
							AccessReportSpec reportSpec = new AccessReportSpec();
							reportSpec.setAccessReportTrigger
							(new AccessReportTriggerType(
									AccessReportTriggerType.Whenever_ROReport_Is_Generated));

							accessSpec.setAccessReportSpec(reportSpec);

							ADD_ACCESSSPEC accessSpecMsg = new ADD_ACCESSSPEC();
							accessSpecMsg.setAccessSpec(accessSpec);

							//System.out.println("ADDED SPECID is " + accessSpecMsg.getAccessSpec().getAccessSpecID().toString());

							//((ReaderImpl)reader).getConnection().send(accessSpecMsg);
							((ReaderImpl)reader).getIoSession().write(accessSpecMsg);
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							
							GET_ACCESSSPECS get = new GET_ACCESSSPECS();
							((ReaderImpl)reader).getIoSession().write(get);

							ENABLE_ACCESSSPEC msgEnableAccessSpec = new ENABLE_ACCESSSPEC();
							msgEnableAccessSpec.setAccessSpecID(new UnsignedInteger(3));
							((ReaderImpl)reader).getIoSession().write(msgEnableAccessSpec);
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

						}

					}
				}
				else
				{
					AccessSpec accessSpec = new AccessSpec();
					accessSpec.setAccessSpecID(new UnsignedInteger(3));

					// Set ROSpec ID to zero.
					// This means that the AccessSpec will apply to all ROSpecs.
					accessSpec.setROSpecID(new UnsignedInteger(1));
					// Antenna ID of zero means all antennas.
					accessSpec.setAntennaID(new UnsignedShort(0));
					accessSpec.setProtocolID(
							new AirProtocols(AirProtocols.EPCGlobalClass1Gen2));
					// AccessSpecs must be disabled when you add them.
					accessSpec.setCurrentState(
							new AccessSpecState(AccessSpecState.Disabled));
					AccessSpecStopTrigger stopTrigger = new AccessSpecStopTrigger();
					// Stop after the operating has been performed a certain number of times.
					// That number is specified by the Operation_Count parameter.
					stopTrigger.setAccessSpecStopTrigger
					(new AccessSpecStopTriggerType(
							AccessSpecStopTriggerType.Null));
					// OperationCountValue indicate the number of times this Spec is
					// executed before it is deleted. If set to 0, this is equivalent
					// to no stop trigger defined.
					stopTrigger.setOperationCountValue(new UnsignedShort(0));
					accessSpec.setAccessSpecStopTrigger(stopTrigger);

					// Create a new AccessCommand.
					// We use this to specify which tags we want to operate on.
					AccessCommand accessCommand = new AccessCommand();

					// Create a new tag spec.
					C1G2TagSpec tagSpec = new C1G2TagSpec();
					C1G2TargetTag targetTag = new C1G2TargetTag();
					targetTag.setMatch(new Bit(1));
					// We want to check memory bank 1 (the EPC memory bank).
					TwoBitField memBank = epcBank;
					targetTag.setMB(memBank);
					// The EPC data starts at offset 0x20.
					// Start reading or writing from there.
					targetTag.setPointer(new UnsignedShort(0));
					// This is the mask we'll use to compare the EPC.
					// We want to match all bits of the EPC, so all mask bits are set.
					BitArray_HEX tagMask = new BitArray_HEX("00");
					targetTag.setTagMask(tagMask);
					// We only only to operate on tags with this EPC.
					BitArray_HEX tagData = new BitArray_HEX("00");
					targetTag.setTagData(tagData);

					// Add a list of target tags to the tag spec.
					List <C1G2TargetTag> targetTagList =
							new ArrayList<C1G2TargetTag>();
					targetTagList.add(targetTag);
					tagSpec.setC1G2TargetTagList(targetTagList);

					// Add the tag spec to the access command.
					accessCommand.setAirProtocolTagSpec(tagSpec);

					// A list to hold the op specs for this access command.
					List <AccessCommandOpSpec> opSpecList =
							new ArrayList<AccessCommandOpSpec>();

					// Set default opspec which for eventcycle of accessspec 3.
					C1G2Read opSpec1 = new C1G2Read();
					// Set the OpSpecID to a unique number.
					opSpec1.setOpSpecID(new UnsignedShort(1));
					opSpec1.setAccessPassword(new UnsignedInteger(0));

					// For this demo, we'll read from user memory (bank 3).
					TwoBitField opMemBank = userBank;
					opSpec1.setMB(opMemBank);

					// We'll read from the base of this memory bank (0x00).
					opSpec1.setWordPointer(new UnsignedShort(0));
					// Read two words.
					opSpec1.setWordCount(new UnsignedShort(0));
					
					opSpecList.add(opSpec1);

					// Are we reading or writing to the tag?
					// Add the appropriate op spec to the op spec list.
					List<CCOpSpec> opspecs = cmdspec.getOpSpecs().getOpSpec();					
					for (CCOpSpec ccopspec : opspecs)
					{
						Enumeration<Integer> enumKey = OpSpecTable.keys();
						while(enumKey.hasMoreElements()) {
							Integer key = enumKey.nextElement();
							CCOpSpec val = OpSpecTable.get(key);
							if(ccopspec.getOpName().equals(val.getOpName()))
							{
								if (ccopspec.getOpType().equalsIgnoreCase("READ"))
								{
									opSpecList.add(buildReadOpSpec(key, ccopspec));
								}
								else if (ccopspec.getOpType().equalsIgnoreCase("WRITE"))
								{
									opSpecList.add(buildWriteOpSpec(key, ccopspec));
								}
							}
						}
					}

					accessCommand.setAccessCommandOpSpecList(opSpecList);

					// Add access command to access spec.
					accessSpec.setAccessCommand(accessCommand);

					// Add an AccessReportSpec.
					// We want to get notification when the operation occurs.
					// Tell the reader to sent it to us with the ROSpec.
					AccessReportSpec reportSpec = new AccessReportSpec();
					reportSpec.setAccessReportTrigger
					(new AccessReportTriggerType(
							AccessReportTriggerType.Whenever_ROReport_Is_Generated));

					accessSpec.setAccessReportSpec(reportSpec);

					ADD_ACCESSSPEC accessSpecMsg = new ADD_ACCESSSPEC();
					accessSpecMsg.setAccessSpec(accessSpec);
					((ReaderImpl)reader).getIoSession().write(accessSpecMsg);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					GET_ACCESSSPECS get = new GET_ACCESSSPECS();
					((ReaderImpl)reader).getIoSession().write(get);

					ENABLE_ACCESSSPEC msgEnableAccessSpec = new ENABLE_ACCESSSPEC();
					msgEnableAccessSpec.setAccessSpecID(new UnsignedInteger(3));
					((ReaderImpl)reader).getIoSession().write(msgEnableAccessSpec);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void DELETEACCESSSPEC() {

		System.out.println("Deleting AccessSpecs3.");
		
		DELETE_ACCESSSPEC del = new DELETE_ACCESSSPEC();
		del.setAccessSpecID(new UnsignedInteger(3));
		try
		{
			((ReaderImpl)reader).getIoSession().write(del);
		}
		catch (Exception e)
		{
			System.out.println("Error deleting AccessSpec.");
			e.printStackTrace();
		}
		
	}

	@Override
	public void recoveryACCESSSPEC3() {

		System.out.println("Recover AccessSpecs3.");
		
		LLRPMessage msgAddAccessspec = null;
		try {
			msgAddAccessspec = loadXMLLLRPMessage(PhysicalReaderAcceptor.class.getResourceAsStream("/llrp/ADD_ACCESSSPEC3.xml"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JDOMException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidLLRPMessageException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try
		{
			((ReaderImpl)reader).getIoSession().write(msgAddAccessspec);			
			ENABLE_ACCESSSPEC msgEnableAccessSpec = new ENABLE_ACCESSSPEC();
			msgEnableAccessSpec.setAccessSpecID(new UnsignedInteger(3));
			((ReaderImpl)reader).getIoSession().write(msgEnableAccessSpec);
		}
		catch (Exception e)
		{
			System.out.println("Error revcover AccessSpec3.");
			e.printStackTrace();
		}
		
	}
	
	// Create a OpSpec that writes to user memory
	private C1G2Write buildWriteOpSpec(int OPSPEC_ID, CCOpSpec ccopspec)
	{
		// Create a new OpSpec.
		// This specifies what operation we want to perform on the
		// tags that match the specifications above.
		// In this case, we want to write to the tag.
		C1G2Write opSpec = new C1G2Write();
		// Set the OpSpecID to a unique number.
		opSpec.setOpSpecID(new UnsignedShort(OPSPEC_ID));
		opSpec.setAccessPassword(new UnsignedInteger(0));

		String fieldName = null;
		if (ccopspec.getFieldspec().getFieldname() != null)
			fieldName = ccopspec.getFieldspec().getFieldname();
		else 
			fieldName = "@3.0.0";

		if(fieldName.equalsIgnoreCase("killPwd")) {
			// killPwd fieldname
			// same as "@0.32"
			// We want to check memory bank.
			fieldName = "@0.32.0"; 
		} else if(fieldName.equalsIgnoreCase("accessPwd")) {
			// accessPwd fieldname
			// same as "@0.32.32"
			// We want to check memory bank.
			fieldName = "@0.32.32";    
		} else if(fieldName.equalsIgnoreCase("epc")) {
			// FIXME : fieldname "epcBank", is it right?
			// return the contents of epc bank
			// datatype: "bits", format: "hex"
			// We want to check memory bank.
			fieldName = "@1.96.32";	
		} else if(fieldName.equalsIgnoreCase("epcBank")) {
			// FIXME : fieldname "epcBank", is it right?
			// return the contents of epc bank
			// datatype: "bits", format: "hex"
			// We want to check memory bank.
			fieldName = "@1.0.0";	
		} else if(fieldName.equalsIgnoreCase("tidBank")) {
			// fieldname "tidBank"
			// return the contents of tid bank
			// datatype: "bits", format: "hex"
			fieldName = "@2.0.0";	
		} else if(fieldName.equalsIgnoreCase("userBank")) {
			// return the contents of user memory bank
			// datatype: "bits", format: "hex"
			fieldName = "@3.0.0";
		} else if(fieldName.equalsIgnoreCase("afi")) {
			// application family identifier
			// same as "@1.8.24"
			// datatype: "uint", format: "hex"
			fieldName = "@1.8.24";
		} else if(fieldName.equalsIgnoreCase("nsi")) {
			// Numbering System Identifier (NSI)
			// same as "@1.9.23"
			// datatype: "uint", format: "hex"
			fieldName = "@1.9.23";
		}

		String Data = ccopspec.getDataSpec().getData();
		if (fieldName.startsWith("@")) {
			// fieldnames start with "@"
			// datatype: "uint", format: "hex"

			String middleData = new String();
			String[] part = fieldName.substring(1).split("\\.");
			String bank = Integer.parseInt(part[0])+"";
			//int length = Integer.parseInt(part[1]);
			int offset;
			if (part.length == 2)
				offset = 0;
			else
				offset = Integer.parseInt(part[2]);
			
			if (ccopspec.getFieldspec().getDatatype() == null && ccopspec.getFieldspec().getFormat() == null)
			{
				
			}
			else if (ccopspec.getFieldspec().getDatatype().equalsIgnoreCase("uint") && ccopspec.getFieldspec().getFormat().equalsIgnoreCase("hex"))
			{
				
			}
			else if (ccopspec.getFieldspec().getDatatype().equalsIgnoreCase("uint") && ccopspec.getFieldspec().getFormat().equalsIgnoreCase("decimal"))
			{
				Data = engine.dec2bin(Data);
				Data = engine.bin2hex(Data);
			}
			else if (ccopspec.getFieldspec().getDatatype().equalsIgnoreCase("epc"))
			{
				
				String bin = engine.convert(Data,
		                params,
		                LevelTypeList.BINARY);
				Data = engine.bin2hex(bin);
				
				while (!Data.equals(""))
				{
					String temp = null;
					if (Data.length() < 4)
					{
						middleData = Data.substring(0);				
					}
					else
					{
						temp = Data.substring(0,4);
						Data = Data.substring(4);
						middleData = middleData + " " + temp;
					}
				}
				Data = middleData.substring(1);
			}
			
			// For this demo, we'll write to user memory (bank 3).
			TwoBitField opMemBank = new TwoBitField();
			if (bank.equals("0"))
				opMemBank = reservedBank;
			else if (bank.equals("1"))
				opMemBank = epcBank;
			else if (bank.equals("2"))
				opMemBank = tidBank;
			else if (bank.equals("3"))
				opMemBank = userBank;
			
			opSpec.setMB(opMemBank);
			// We'll write to the base of this memory bank (0x00).
			opSpec.setWordPointer(new UnsignedShort(offset/16));
			UnsignedShortArray_HEX writeData = new UnsignedShortArray_HEX(Data);
			// We'll write 8 bytes or two words.
			opSpec.setWriteData(writeData);

		} else {
			// symbolic fieldnames
			// datatype: "uint", format: "hex"

			// user-defined symbolic field name
			SymbolicField symbolicfield = SymbolicFieldRepo.getInstance().getSymbolicField(fieldName);
			if(symbolicfield == null) {
				try {
					throw new ImplementationException("symbolic field does not exist for the fieldname "+ fieldName);
				} catch (ImplementationException e) {
					e.printStackTrace();
				}
			}

			String bank = symbolicfield.getBank()+"";
			//int length = symbolicfield.getLength();
			int offset = symbolicfield.getOffset();

			// For this demo, we'll write to user memory (bank 3).
			TwoBitField opMemBank = new TwoBitField();
			if (bank.equals("0"))
				opMemBank = reservedBank;
			else if (bank.equals("1"))
				opMemBank = epcBank;
			else if (bank.equals("2"))
				opMemBank = tidBank;
			else if (bank.equals("3"))
				opMemBank = userBank;
			
			opSpec.setMB(opMemBank);
			// We'll write to the base of this memory bank (0x00).
			opSpec.setWordPointer(new UnsignedShort(offset/16));
			UnsignedShortArray_HEX writeData = new UnsignedShortArray_HEX(Data);
			// We'll write 8 bytes or two words.
			opSpec.setWriteData(writeData);
		}

		return opSpec;
	}

	// Create a OpSpec that reads from user memory
	private C1G2Read buildReadOpSpec(int OPSPEC_ID, CCOpSpec ccopspec)
	{
		// Create a new OpSpec.
		// This specifies what operation we want to perform on the
		// tags that match the specifications above.
		// In this case, we want to read from the tag.
		C1G2Read opSpec = new C1G2Read();
		// Set the OpSpecID to a unique number.
		opSpec.setOpSpecID(new UnsignedShort(OPSPEC_ID));
		opSpec.setAccessPassword(new UnsignedInteger(0));

		String fieldName = null;
		if (ccopspec.getFieldspec() != null)
		{
			if(ccopspec.getFieldspec().getFieldname() != null)
				fieldName = ccopspec.getFieldspec().getFieldname();
		}
		else 
			fieldName = "@3.0.0";

		if(fieldName.equalsIgnoreCase("killPwd")) {
			// killPwd fieldname
			// same as "@0.32"
			// We want to check memory bank.
			fieldName = "@0.32.0"; 
		} else if(fieldName.equalsIgnoreCase("accessPwd")) {
			// accessPwd fieldname
			// same as "@0.32.32"
			// We want to check memory bank.
			fieldName = "@0.32.32";    
		} else if(fieldName.equalsIgnoreCase("epc")) {
			// FIXME : fieldname "epcBank", is it right?
			// return the contents of epc bank
			// datatype: "bits", format: "hex"
			// We want to check memory bank.
			fieldName = "@1.96.32";	
		} else if(fieldName.equalsIgnoreCase("epcBank")) {
			// FIXME : fieldname "epcBank", is it right?
			// return the contents of epc bank
			// datatype: "bits", format: "hex"
			// We want to check memory bank.
			fieldName = "@1.0.0";	
		} else if(fieldName.equalsIgnoreCase("tidBank")) {
			// fieldname "tidBank"
			// return the contents of tid bank
			// datatype: "bits", format: "hex"
			fieldName = "@2.0.0";	
		} else if(fieldName.equalsIgnoreCase("userBank")) {
			// return the contents of user memory bank
			// datatype: "bits", format: "hex"
			fieldName = "@3.0.0";
		} else if(fieldName.equalsIgnoreCase("afi")) {
			// application family identifier
			// same as "@1.8.24"
			// datatype: "uint", format: "hex"
			fieldName = "@1.8.24";
		} else if(fieldName.equalsIgnoreCase("nsi")) {
			// Numbering System Identifier (NSI)
			// same as "@1.9.23"
			// datatype: "uint", format: "hex"
			fieldName = "@1.9.23";
		}

		if (fieldName.startsWith("@")) {
			// fieldnames start with "@"
			// datatype: "uint", format: "hex"

			String[] part = fieldName.substring(1).split("\\.");
			String bank = Integer.parseInt(part[0])+"";
			int length = Integer.parseInt(part[1]);
			int offset;
			if (part.length == 2)
				offset = 0;
			else
				offset = Integer.parseInt(part[2]);
			
			// For this demo, we'll read from user memory (bank 3).
			TwoBitField opMemBank = new TwoBitField();
			if (bank.equals("0"))
				opMemBank = reservedBank;
			else if (bank.equals("1"))
				opMemBank = epcBank;
			else if (bank.equals("2"))
				opMemBank = tidBank;
			else if (bank.equals("3"))
				opMemBank = userBank;
			
			opSpec.setMB(opMemBank);

			// We'll read from the base of this memory bank (0x00).
			opSpec.setWordPointer(new UnsignedShort(offset/16));
			// Read two words.
			opSpec.setWordCount(new UnsignedShort(length/16));

		} else {
			// symbolic fieldnames
			// datatype: "uint", format: "hex"

			// user-defined symbolic field name
			SymbolicField symbolicfield = SymbolicFieldRepo.getInstance().getSymbolicField(fieldName);
			if(symbolicfield == null) {
				try {
					throw new ImplementationException("symbolic field does not exist for the fieldname "+ fieldName);
				} catch (ImplementationException e) {
					e.printStackTrace();
				}
			}

			String bank = symbolicfield.getBank()+"";
			int length = symbolicfield.getLength();
			int offset = symbolicfield.getOffset();

			// For this demo, we'll read from user memory (bank 3).
			TwoBitField opMemBank = new TwoBitField();
			if (bank.equals("0"))
				opMemBank = reservedBank;
			else if (bank.equals("1"))
				opMemBank = epcBank;
			else if (bank.equals("2"))
				opMemBank = tidBank;
			else if (bank.equals("3"))
				opMemBank = userBank;
			
			opSpec.setMB(opMemBank);

			// We'll read from the base of this memory bank (0x00).
			opSpec.setWordPointer(new UnsignedShort(offset/16));
			// Read two words.
			opSpec.setWordCount(new UnsignedShort(length/16)); 
		}

		return opSpec;
	}

	public String getOriginReaderName() {
		return originReaderName;
	}

	public void setOriginReaderName(String originReaderName) {
		this.originReaderName = originReaderName;
	}
	public void restoreLLRPSpecs() {
		log.debug("restore LLRP specs defined in the logical reader "+originReaderName);

	}
	
	public LLRPMessage loadXMLLLRPMessage(InputStream is) throws FileNotFoundException, IOException, JDOMException, InvalidLLRPMessageException {
		
		Document doc = new org.jdom.input.SAXBuilder().build(new
				InputStreamReader(is));
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		log.debug("Loaded XML Message: " + outputter.outputString(doc));
		LLRPMessage message = LLRPMessageFactory.createLLRPMessage(doc);
	
		return message;
	}

}
