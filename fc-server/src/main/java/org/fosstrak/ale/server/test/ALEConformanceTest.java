/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */


package org.fosstrak.ale.server.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.fosstrak.ale.exception.DuplicateNameException;
import org.fosstrak.ale.exception.DuplicateSubscriptionException;
import org.fosstrak.ale.exception.ECSpecValidationException;
import org.fosstrak.ale.exception.ImplementationException;
import org.fosstrak.ale.exception.InvalidURIException;
import org.fosstrak.ale.exception.NoSuchNameException;
import org.fosstrak.ale.exception.NoSuchSubscriberException;
import org.fosstrak.ale.exception.SecurityException;
import org.fosstrak.ale.server.ALE;
import org.fosstrak.ale.server.cc.ALECC;
import org.fosstrak.ale.server.readers.LogicalReaderManager;
import org.fosstrak.ale.server.tm.ALETM;
import org.fosstrak.ale.util.DeserializerUtil;
import org.fosstrak.ale.util.SerializerUtil;
import org.fosstrak.ale.xsd.ale.epcglobal.CCReports;
import org.fosstrak.ale.xsd.ale.epcglobal.CCSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECBoundarySpecExtension;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFieldSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFilterListMember;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFilterSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFilterSpecExtension;
import org.fosstrak.ale.xsd.ale.epcglobal.TMSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFilterSpecExtension.FilterList;
import org.fosstrak.ale.xsd.ale.epcglobal.ECGroupSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECBoundarySpecExtension.StartTriggerList;
import org.fosstrak.ale.xsd.ale.epcglobal.ECBoundarySpecExtension.StopTriggerList;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFilterListMember.PatList;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFilterSpec.ExcludePatterns;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFilterSpec.IncludePatterns;
import org.fosstrak.ale.xsd.ale.epcglobal.ECGroupSpecExtension;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReportSpecExtension;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReports;
import org.fosstrak.ale.xsd.ale.epcglobal.ECSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.LRSpec;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;



public class ALEConformanceTest {

	/** logger. */
	private static final Logger LOG = Logger.getLogger(ALEConformanceTest.class);

	/** socket. */
	private Socket socket=null;
	private InputStream is = null;
	private BufferedReader in = null;

	static ALE ale;
	static ALECC alecc;
	static ALETM aletm;

	static LogicalReaderManager lrm;

	@BeforeClass
	public static void beforeClass() throws Exception {
		
		// please connect reader whose epc is 000011112222333344445555 and aaaa11112222333344445555

		LOG.info("Initializing Spring context.");

		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext.xml");

		LOG.info("Spring context initialized.");

		aletm = (ALETM) applicationContext.getBean("aletm");
		lrm = (LogicalReaderManager) applicationContext.getBean("logicalReaderManager");
		alecc = (ALECC) applicationContext.getBean("alecc");
		ale = (ALE) applicationContext.getBean("ale");

		LRSpec lrspec = DeserializerUtil.deserializeLRSpec(ALETMConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_R.xml"));
		LRSpec lrspec2 = DeserializerUtil.deserializeLRSpec(ALETMConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_R2.xml"));

		System.out.println("Please connect reader... (timeout: 5000ms)");
		Thread.sleep(5000);

		lrm.define("LogicalReader1", lrspec);
		lrm.define("LogicalReader2", lrspec2);

		System.out.println("Waiting for reader initialization...");
		Thread.sleep(5000);

	}

	@Before
	public void beforeEachTest() throws SecurityException, NoSuchNameException, ImplementationException {
		for(String name : ale.getECSpecNames()) {
			ale.undefine(name);
		}
	}

	@Test
	public void test_R1() {
		assertEquals("1.1", ale.getStandardVersion());
		assertEquals("", ale.getVendorVersion());
	}

	@Test
	public void test_R2() throws DuplicateNameException, ECSpecValidationException, ImplementationException, Exception {
		/** 
		 * precondition
		 * No ECSpecs are defined.
		 * Ensure all specName parameters accept as a name any non-empty string of Unicode characters that does not include Pattern_White_Space or Pattern_Syntax characters (see GM6)
		 * The Writing API must be supported for Step 7. Otherwise, Step 7 is optional.
		 **/ 

		CCSpec ccspec = DeserializerUtil.deserializeCCSpec(ALEConformanceTest.class.getResourceAsStream("/ccspecs/CCSpec_R2_1.xml"));
		ECSpec ecspec = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R2_1.xml"));

		//System.out.println(spec.getBoundarySpec().getExtension().isWhenDataAvailable());
		
		//System.out.println(spec.getBoundarySpec().getExtension().isWhenDataAvailable());

		//step 1, 6
		ale.define("eventcycle1", ecspec);
		String[] ecspecNames = ale.getECSpecNames();
		for(String ecspecName : ecspecNames) assertEquals("eventcycle1", ecspecName);

		//step 2, 6
		ECSpec defined = ale.getECSpec("eventcycle1");

		//step 3, 6
		StringWriter expectedsw = new StringWriter();
		SerializerUtil.serializeECSpec(ecspec, expectedsw);
		String outExpectedString = expectedsw.toString();

		StringWriter sw = new StringWriter();
		SerializerUtil.serializeECSpec(defined, sw);
		String outString = sw.toString();

		//SerializerUtil.serializeECReportsPretty(ecreports, new OutputStreamWriter(System.out));
		Assert.assertEquals(outExpectedString, outString);

		//step 4, 6
		//step 4, 6
		ale.undefine("eventcycle1");

		//step 5, 6
		String[] ecspecNames2 = ale.getECSpecNames();
		if (ecspecNames2.length != 0)
			Assert.fail();

		//step 7
		try
		{
			alecc.define("foo", ccspec);
			ale.define("foo", ecspec);
		}
		catch (DuplicateNameException e)
		{
			System.out.println("fsdafd");
			Assert.fail();
		}

		alecc.undefine("foo");
		ale.undefine("foo");

	}

	@Test
	public void test_R3() throws Exception {

		int check = 0;
		ECSpec ecspec = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R3_1.xml"));
		ale.define("eventcycle1", ecspec);

		//step 1
		try {
			ale.getECSpec("unknown");
		} catch (NoSuchNameException e) {
			check++;
		}

		//step 2
		try {
			ale.poll("unknown");
		} catch (NoSuchNameException e) {
			check++;
		}

		//step 3
		try {
			ale.subscribe("unknown", "http://localhost:9999");
		} catch (NoSuchNameException e) {
			check++;
		} catch (InvalidURIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DuplicateSubscriptionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//step 4
		try {
			ale.unsubscribe("eventcycle1", "http://localhost:80");
		} catch (NoSuchNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchSubscriberException e) {
			check++;
		} catch (InvalidURIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//step 5
		try {
			ale.subscribe("eventcycle1", "localhost:80");
		} catch (NoSuchNameException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidURIException e1) {
			check++;
		} catch (DuplicateSubscriptionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//step 6
		try {
			ale.unsubscribe("unknown", "http://loaclhost:9999");
		} catch (NoSuchNameException e1) {
			check++;
		} catch (NoSuchSubscriberException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidURIException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//step 7
		try {
			ale.getSubscribers("unknown");
		} catch (NoSuchNameException e1) {
			check++;
		}

		//step 8
		try {
			ale.undefine("unknown");
		} catch (NoSuchNameException e1) {
			check++;
		}

		//step 9
		try {
			ale.subscribe("eventcycle1", "htp://localhost:9999");
		} catch (NoSuchNameException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidURIException e1) {
			check++;
		} catch (DuplicateSubscriptionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//step 10
		ale.subscribe("eventcycle1", "http://localhost:9999");
		Thread.sleep(5000);
		try {
			ale.unsubscribe("eventcycle1", "ftp//localhost:9878");
		} catch (NoSuchNameException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchSubscriberException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidURIException e1) {
			check++;
		}
		ale.unsubscribe("eventcycle1", "http://localhost:9999");

		//step 11
		ale.define("eventcycle11", ecspec);

		//step 12
		String[] ecspecNames = ale.getECSpecNames();
		int c = 0;
		for(String ecspecName : ecspecNames) 
		{
			if (ecspecName.equals("eventcycle11"))
				c = 1;
		}
		Assert.assertEquals(1, c);

		//step 13
		try {
			ale.define("eventcycle11", ecspec);
		} catch (DuplicateNameException e1) {
			check++;
		} catch (ECSpecValidationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ImplementationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//step 14
		try {
			ale.subscribe("eventcycle1", "http://localhost:9999");
			ale.subscribe("eventcycle1", "http://localhost:9999");
		} catch (NoSuchNameException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidURIException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (DuplicateSubscriptionException e1) {
			check++;
		}
		ale.unsubscribe("eventcycle1", "http://localhost:9999");

		//step 15
		ECSpec ecspec15 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R3_15.xml"));
		try {
			ale.immediate(ecspec15);
		} catch (ECSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//step 16
		ECSpec ecspec16 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R3_16.xml"));
		try {
			ale.define("eventcycle16", ecspec16);
		} catch (DuplicateNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ECSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//step 17
		ECSpec ecspec17 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R3_17.xml"));
		try {
			ale.immediate(ecspec17);
		} catch (ECSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//step 18
		ECSpec ecspec18 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R3_18.xml"));
		try {
			ale.define("eventcycle18", ecspec18);
		} catch (DuplicateNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ECSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//step 19
		ECSpec ecspec19 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R3_19.xml"));
		try {
			ale.define("eventcycle19", ecspec19);
		} catch (DuplicateNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ECSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//step 20
		/*ECSpec ecspec21 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R3_21.xml"));
		try {
			ale.immediate(ecspec21);
		} catch (ECSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//step 21
		ECSpec ecspec21 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R3_21.xml"));
		try {
			ale.define("eventcycle21", ecspec21);
		} catch (DuplicateNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ECSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//step 22
		ECSpec ecspec22 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R3_22.xml"));
		try {
			ale.define("eventcycle22", ecspec22);
		} catch (DuplicateNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ECSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//step 23
		ECSpec ecspec23 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R3_23.xml"));
		try {
			ale.immediate(ecspec23);
		} catch (ECSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//step 24: We don't use primary key field.
		/*ECSpec ecspec24 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R3_24.xml"));
		try {
			ale.define("eventcycle24", ecspec24);
		} catch (DuplicateNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ECSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		//step 25
		ECSpec ecspec25 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R3_25.xml"));
		try {
			ale.define("eventcycle25", ecspec25);
		} catch (DuplicateNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ECSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//step 26
		ECSpec ecspec26 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R3_26.xml"));
		try {
			ale.define("eventcycle26", ecspec26);
		} catch (DuplicateNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ECSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//step 27
		ECSpec ecspec27 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R3_27.xml"));
		try {
			ale.define("eventcycle27", ecspec27);
		} catch (DuplicateNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ECSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//step 28: Invoke the define method with an ECSpec Whose ECFilterSpec has filterList with fieldspec with unknown datatype and format.
		// Hard to define unknown datatype.

		//step 29: Invoke the define method with an ECSpec Whose ECgroupSpec has fieldspec with unknown datatype and format
		// Hard to define unknown datatype.

		//step 30: Invoke the define method with an ECSpec Whose ECgroupSpec has patternList that does not conform to the syntax rules for grouping patterns
		ECSpec ecspec30 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R3_30.xml"));
		try {
			ale.define("eventcycl31", ecspec30);
		} catch (DuplicateNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ECSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//step 31: Invoke the define method with an ECSpec Whose ECgroupSpec has patternList of non disjoint pattern => skip

		//step 32: Invoke the immediate method with an ECSpec whose ECBoundarySpec has a startTriggerList (containing a startTrigger) or a stopTriggerList (containing a stopTrigger). The elements of the startTriggerList and stopTriggerList does not conform to the URI syntax.
		// Start and Stop trigger is not implemented yet.

		//step 33: Invoke the define method with a specName that uses a diacritical letter (e.g. embarcadère). Then invoke the undefine method with a specName that looks equivalent but does not contain the diacritical mark (e.g. embarcadere)
		// Hard to distinguish diacritical letter.

		//step 34: Invoke the define method with an ECSpec with a primaryKeyFields whose implementation does not support the primaryKeyFields value with the specificed logical readers.
		// PrimaryKeyField is not implemented yet.

		//step 35: Invoke the define method with a fieldspec that specifies a fieldname of epc and specifies a datatype that is not an epc.
		// Hard to define unknown datatype.
		
		//step 36: Invoke the define method with a fieldspec that specifies a fieldname beginning with an @ character but not conforming to any syntax specified in Section 6.1.9 of the specification
		// Hard to distinguish validation in forsstrak.
		
		Assert.assertEquals(24, check); //24
		
	}

	@Test
	public void test_R4() throws Exception {
		// precondition : a valid ECSpec has been defined
		ECSpec ecspec1 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R4_1.xml"));
		ale.define("ECSpec_R4_1", ecspec1);

		// step 1
		ale.subscribe("ECSpec_R4_1", "http://localhost:9999");
		
		
		// step 2
		String[] subscriberNames = ale.getSubscribers("ECSpec_R4_1");
		
		containsInStringArray(subscriberNames, "http://localhost:9999");
		
		String ecReportsFromSocket = receiveEcreportsFromSocket(9999);

        ECReports ecreports_4_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R4_1_expected.xml"));
        StringWriter expectedsw = new StringWriter();
		SerializerUtil.serializeECReports(ecreports_4_expected, expectedsw);
		String outExpectedString = expectedsw.toString();
		
		Assert.assertEquals(outExpectedString.substring(outExpectedString.indexOf("<reports>")), ecReportsFromSocket.substring(ecReportsFromSocket.indexOf("<reports>")));
		
		
		// step 3
		ale.subscribe("ECSpec_R4_1", "http://localhost:10000");
		
		// step 4
		subscriberNames = ale.getSubscribers("ECSpec_R4_1");
		assertTrue(containsInStringArray(subscriberNames, "http://localhost:9999"));
		assertTrue(containsInStringArray(subscriberNames, "http://localhost:10000"));
		
		// step 5
		ale.unsubscribe("ECSpec_R4_1", "http://localhost:9999");
		
		// step 6
		subscriberNames = ale.getSubscribers("ECSpec_R4_1");
		assertTrue(!containsInStringArray(subscriberNames, "http://localhost:9999"));
		
		// step 7
		ale.unsubscribe("ECSpec_R4_1", "http://localhost:10000");
		
		// step 8
		subscriberNames = ale.getSubscribers("ECSpec_R4_1");
		assertTrue(!containsInStringArray(subscriberNames, "http://localhost:10000"));
		
		// step 9 : repeat 1~8 with TCP Notification URI => skip
		// step 10 : repeat 1~8 with File Notification URI => skip
		// step 11 : repeat 1~8 with HTTPS Notification URI => skip
		
		// test ended : undefine pre-condition
		ale.undefine("ECSpec_R4_1");
	}

	

	@Test
	public void test_R5() throws Exception {
		ECSpec ecspec = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R5_1.xml"));
		ale.define("eventcycle1", ecspec);

		//step 1 : place a tag set in the reader field
		//step 2
		ECReports ecreport2 = ale.poll("eventcycle1");
		ECReports expectedEcreports2 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R5_1.xml"));
		//assertTrue(compareEquals(ecreport2, expectedEcreports2));
		assertEquals(convertToComparableString(ecreport2), convertToComparableString(expectedEcreports2));
		

		//step 3: remove tags from reader.
		//step 4: skip because we cannot remove tags from reader_emulator.

		
		//step 5: place a tags set in the reader field. Do nothing in simulator.
		//step 6
		ECSpec ecspec5 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R5_2.xml"));
		ale.define("eventcycle5", ecspec5);

		ECReports ecreport5 = ale.poll("eventcycle5");
		ECReports expectedEcreports5 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R5_1.xml"));
		assertEquals(convertToComparableString(ecreport5), convertToComparableString(expectedEcreports5));

		//step 7: remove the all tags.
		//step 8: skip because we cannot remove tags from reader_emulator.
		
	}
	
	@Test
	public void test_R6() throws Exception {
		ECSpec ecspec_2 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R6_2.xml"));
		ECSpec ecspec_3 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R6_3.xml"));
		
		// step 1: place a tag 
		
		// step 2 : 
		ECReports ecreports_2 = ale.immediate(ecspec_2);
		ECReports expectedEcreports_2 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R6_2_expected.xml"));
		assertEquals(convertToComparableString(ecreports_2), convertToComparableString(expectedEcreports_2));
		
		// step 3 : FIXME we cannot compare ECReports because of time stamp information, the rest is OK.
		ECReports ecreports_3 = ale.immediate(ecspec_3);
		ECReports expectedEcreports_3 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R6_3_expected.xml"));
		//assertTrue(compareEquals(ecreports_3, expectedEcreports_3));
	}

	private void printECReports(ECReports ecreports) throws Exception {
		SerializerUtil.serializeECReports(ecreports, new OutputStreamWriter(System.out));
		System.out.println();
	}
	
	@Test
	public void test_R7() throws Exception {
		// pre-condition
		ECSpec ecspec = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R7_1.xml"));
		ale.define("ECSpec_R7_1", ecspec);
		
		// step 1
		ECReports ecreports_1 = ale.poll("ECSpec_R7_1");
		printECReports(ecreports_1);
		
		// step 2: move a set of tags into the reader field and trip the start trigger URI1
		
		// step 3: trip the stop trigger URI3 after a sufficient time has passed for all the tags to have been read and reported to the ALE implementation
		
		// step 4
		ECReports ecreports_4 = ale.poll("ECSpec_R7_1");
		printECReports(ecreports_4);

		// step 5: move a set of tags into the reader field and trip the start trigger URI2
		
		// step 6: trip the stop trigger URI4 after a sufficient time has passed for all the tags to have been read and reported to the ALE implementation
		
		// step 7 
		ECReports ecreports_7 = ale.poll("ECSpec_R7_1");
		printECReports(ecreports_7);
		
		// step 8: move a set of tags into the reader field and trip the start trigger URI5
		
		// step 9: trip the stop trigger URI6 after a sufficient time has passed for all the tags to have been read and reported to the ALE implementation
		
		
	}
	
	@Test
	public void test_R8() throws Exception {
		ECSpec ecspec = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R8_1.xml"));
		
		
		ECFieldSpec field1 = new ECFieldSpec();
		field1.setFieldname("epc");
		field1.setDatatype("epc");
		field1.setFormat("epc-tag");
		
		ECFieldSpec field2 = new ECFieldSpec();
		field2.setFieldname("killPwd");
		
		ECFieldSpec field3 = new ECFieldSpec();
		field3.setFieldname("accessPwd");
		
		ECFieldSpec field4 = new ECFieldSpec();
		field4.setFieldname("afi");
		
		ECFieldSpec field5 = new ECFieldSpec();
		field5.setFieldname("nsi");

		
		ECFilterListMember member1 = new ECFilterListMember();
		member1.setIncludeExclude("exclude");
		member1.setPatList(new PatList());
		member1.getPatList().getPat().add("urn:epc:pat:gid-96:203907500.*.*");
		member1.setFieldspec(field1);
		
		ECFilterListMember member2 = new ECFilterListMember();
		member2.setIncludeExclude("exclude");
		member2.setPatList(new PatList());
		member2.getPatList().getPat().add("pat1");
		member2.setFieldspec(field2);
		
		ECFilterListMember member3 = new ECFilterListMember();
		member3.setIncludeExclude("exclude");
		member3.setPatList(new PatList());
		member3.getPatList().getPat().add("pat1");
		member3.setFieldspec(field3);
		
		ECFilterListMember member4 = new ECFilterListMember();
		member4.setIncludeExclude("exclude");
		member4.setPatList(new PatList());
		member4.getPatList().getPat().add("BB");
		member4.setFieldspec(field4);
		
		ECFilterListMember member5 = new ECFilterListMember();
		member5.setIncludeExclude("exclude");
		member5.setPatList(new PatList());
		member5.getPatList().getPat().add("BB");
		member5.setFieldspec(field5);
		
		
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().add(member1);
		//ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().add(member2);
		//ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().add(member3);
		//ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().add(member4);
		//ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().add(member5);
		//ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().get(0).getFieldspec().setFieldname("");
		
		//SerializerUtil.serializeECSpecPretty(ecspec, System.out);
		
		// step 1
		ale.define("eventcycle1", ecspec);
		ECReports ecreports_1 = ale.poll("eventcycle1");
		ale.undefine("eventcycle1");
		//SerializerUtil.serializeECReportsPretty(ecreports_1, new OutputStreamWriter(System.out));
		
		// step 2
		ECReports expectedEcreports_1 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R8_1_expected.xml"));
		assertEquals(convertToComparableString(expectedEcreports_1), convertToComparableString(ecreports_1));
		
		// step 3
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().get(0).getPatList().getPat().remove(0);
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().get(0).getPatList().getPat().add("urn:epc:pat:gid-96:*.*.*");
		
		ale.define("eventcycle1", ecspec);
		ECReports ecreports_3 = ale.poll("eventcycle1");
		ale.undefine("eventcycle1");
		
		ECReports expectedEcreports_3 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R8_3_expected.xml"));
		assertTrue(compareEquals(ecreports_3, expectedEcreports_3));
		
		// step 4 : killPwd value is not supported => skip
		// step 5 : accessPwd value is not supported => skip
		
		// step 6
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().remove(0);
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().add(member4);
		
		ale.define("eventcycle1", ecspec);
		ECReports ecreports_6 = ale.poll("eventcycle1");
		ale.undefine("eventcycle1");
		
		// compare
		ECReports expectedEcreports_6 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R8_6_expected.xml"));
		assertTrue(compareEquals(ecreports_6, expectedEcreports_6));
		
		// filter out with value "BC"
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().get(0).getPatList().getPat().remove(0);
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().get(0).getPatList().getPat().add("BC");
		ale.define("eventcycle1", ecspec);
		ECReports ecreports_6_2 = ale.poll("eventcycle1");
		ale.undefine("eventcycle1");
		ECReports expectedEcreports_6_2 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R8_6_2_expected.xml"));
		assertTrue(compareEquals(ecreports_6_2, expectedEcreports_6_2));
		
		
		// step 7
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().remove(0);
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().add(member5);
		
		ale.define("eventcycle1", ecspec);
		ECReports ecreports_7 = ale.poll("eventcycle1");
		ale.undefine("eventcycle1");
		
		// compare
		ECReports expectedEcreports_7 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R8_7_expected.xml"));
		assertTrue(compareEquals(ecreports_7, expectedEcreports_7));
		
		// filter out with value "BC"
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().get(0).getPatList().getPat().remove(0);
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().get(0).getPatList().getPat().add("BC");
		ale.define("eventcycle1", ecspec);
		ECReports ecreports_7_2 = ale.poll("eventcycle1");
		ale.undefine("eventcycle1");
		ECReports expectedEcreports_7_2 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R8_7_2_expected.xml"));
		assertTrue(compareEquals(ecreports_7_2, expectedEcreports_7_2));
		
		
		/*
		// step 8
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().remove(0);
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().add(member2);
		
		ale.define("eventcycle1", ecspec);
		ECReports ecreports_8 = ale.poll("eventcycle1");
		ale.undefine("eventcycle1");
		
		// compare
		ECReports expectedEcreports_8 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R8_6_expected.xml"));
		SerializerUtil.serializeECReportsPretty(ecreports_8, new OutputStreamWriter(System.out));
		assertTrue(compareEquals(ecreports_8, expectedEcreports_8));
		
		// filter out with value "BC"
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().get(0).getPatList().getPat().remove(0);
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().get(0).getPatList().getPat().add("BC");
		ale.define("eventcycle1", ecspec);
		ECReports ecreports_8_2 = ale.poll("eventcycle1");
		ale.undefine("eventcycle1");
		ECReports expectedEcreports_8_2 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R8_6_2_expected.xml"));
		assertTrue(compareEquals(ecreports_8_2, expectedEcreports_8_2));
		
		
		
		// step 9
		
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().remove(0);
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().add(member3);
		
		ale.define("eventcycle1", ecspec);
		ECReports ecreports_9 = ale.poll("eventcycle1");
		ale.undefine("eventcycle1");
		
		// compare
		ECReports expectedEcreports_9 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R8_6_expected.xml"));
		assertTrue(compareEquals(ecreports_9, expectedEcreports_9));
		
		// filter out with value "BC"
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().get(0).getPatList().getPat().remove(0);
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().get(0).getPatList().getPat().add("BC");
		ale.define("eventcycle1", ecspec);
		ECReports ecreports_9_2 = ale.poll("eventcycle1");
		ale.undefine("eventcycle1");
		ECReports expectedEcreports_9_2 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R8_6_2_expected.xml"));
		assertTrue(compareEquals(ecreports_9_2, expectedEcreports_9_2));
		
		
		
		// step 10
		
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().remove(0);
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().add(member4);
		
		ale.define("eventcycle1", ecspec);
		ECReports ecreports_10 = ale.poll("eventcycle1");
		ale.undefine("eventcycle1");
		
		// compare
		ECReports expectedEcreports_10 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R8_6_expected.xml"));
		assertTrue(compareEquals(ecreports_10, expectedEcreports_10));
		
		// filter out with value "BC"
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().get(0).getPatList().getPat().remove(0);
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().get(0).getPatList().getPat().add("BC");
		ale.define("eventcycle1", ecspec);
		ECReports ecreports_10_2 = ale.poll("eventcycle1");
		ale.undefine("eventcycle1");
		ECReports expectedEcreports_10_2 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R8_6_2_expected.xml"));
		assertTrue(compareEquals(ecreports_10_2, expectedEcreports_10_2));
		
		
		
		// step 11
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().remove(0);
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().add(member5);
		
		ale.define("eventcycle1", ecspec);
		ECReports ecreports_11 = ale.poll("eventcycle1");
		ale.undefine("eventcycle1");
		
		// compare
		ECReports expectedEcreports_11 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R8_6_expected.xml"));
		assertTrue(compareEquals(ecreports_11, expectedEcreports_11));
		
		// filter out with value "BC"
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().get(0).getPatList().getPat().remove(0);
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().get(0).getPatList().getPat().add("BC");
		ale.define("eventcycle1", ecspec);
		ECReports ecreports_11_2 = ale.poll("eventcycle1");
		ale.undefine("eventcycle1");
		ECReports expectedEcreports_11_2 = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R8_6_2_expected.xml"));
		assertTrue(compareEquals(ecreports_11_2, expectedEcreports_11_2));
		*/
	}
	@Test
	public void test_R9() throws Exception {
		
		// precondition
		ECSpec ecspec_1a = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R9_1_A.xml"));
		ECSpec ecspec_1b = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R9_1_B.xml"));
		
		ECSpec ecspec_2a = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R9_2_A.xml"));
		ECSpec ecspec_2b = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R9_2_B.xml"));
		
		ECSpec ecspec_3a = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R9_3_A.xml"));
		ECSpec ecspec_3b = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R9_3_B.xml"));
		
		ale.define("ecspec_R9_1a", ecspec_1a);
		ale.define("ecspec_R9_1b", ecspec_1b);
		
		ale.define("ecspec_R9_2a", ecspec_2a);
		ale.define("ecspec_R9_2b", ecspec_2b);
		
		ale.define("ecspec_R9_3a", ecspec_3a);
		ale.define("ecspec_R9_3b", ecspec_3b);
		
		// step 1
		ale.subscribe("ecspec_R9_1a", "http://localhost:9999");
		
		// step 2 : put a tag that does not satisfy includeFilter condition within time < N sec
		// we should verify that an empty ECReports, but in emulator skip this step
		String ecreports_2 = receiveEcreportsFromSocket(9999);
		ECReports ecreports_2_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R9_2_expected.xml"));
		assertTrue(compareEquals(ecreports_2_expected, ecreports_2));
		
		// step 3 : put a tag that satisfies includefilter condition within time < N sec
		System.out.println(receiveEcreportsFromSocket(9999));
		
		
		// step 4 : put a tag tag that satisfies include filter condition before the expiration of the repeat period M (N<M)
		System.out.println(receiveEcreportsFromSocket(9999));
		
		// step 5
		ale.unsubscribe("ecspec_R9_1a", "http://localhost:9999");
		
		// step 6
		ale.subscribe("ecspec_R9_1b", "http://localhost:9999");
		
		// step 7 : put a tag that does not satisfy include filter condition within time < N sec
		String ecreports_7 = receiveEcreportsFromSocket(9999);
		ECReports ecreports_7_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R9_7_expected.xml"));
		assertEquals(convertToComparableString(ecreports_7_expected), convertToComparableString(ecreports_7));
		
		// step 8 : put a tag satisfy include filter condition within time < N sec during the next event cycle
		System.out.println(receiveEcreportsFromSocket(9999));
		
		// step 9
		ale.unsubscribe("ecspec_R9_1b", "http://localhost:9999");
		
		// step 10
		ale.subscribe("ecspec_R9_2a", "http://localhost:9999");
		
		// step 11 : put a tag that satisfies the exclude filter condition within time < N sec
		String ecreports_11 = receiveEcreportsFromSocket(9999);
		ECReports ecreports_11_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R9_11_expected.xml"));
		assertEquals(convertToComparableString(ecreports_11_expected), convertToComparableString(ecreports_11));
		
		// step 12 : put a tag does not satisfy exclude filter condition within time < N sec during the next event cycle
		System.out.println(receiveEcreportsFromSocket(9999));
		
		// step 13 : put a tag that satisfies exclude filter condition before the expiry of duration, i.e., within M sec of the start of the event cycle in step 3
		System.out.println(receiveEcreportsFromSocket(9999));
		
		// step 14
		ale.unsubscribe("ecspec_R9_2a", "http://localhost:9999");
		
		// step 15
		ale.subscribe("ecspec_R9_2b", "http://localhost:9999");
		
		// step 16 : put a tag that satisfy exclude filter condition within time < N sec during the next event cycle
		String ecreports_16 = receiveEcreportsFromSocket(9999);
		ECReports ecreports_16_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R9_16_expected.xml"));
		assertEquals(convertToComparableString(ecreports_16_expected), convertToComparableString(ecreports_16));
		
		// step 17 : put a tag that does not satisfy exclude filter condition within time < N sec
		System.out.println(receiveEcreportsFromSocket(9999));
		
		// step 18
		ale.unsubscribe("ecspec_R9_2b", "http://localhost:9999");
		
		// step 19
		ale.subscribe("ecspec_R9_3a", "http://localhost:9999");
		
		// step 20 : put tags that satisfies filter list include and exclude conditions within time < N sec
		String ecreports_20 = receiveEcreportsFromSocket(9999);
		ECReports ecreports_20_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R9_20_expected.xml"));
		assertEquals(convertToComparableString(ecreports_20_expected), convertToComparableString(ecreports_20));
		
		// step 21 : put tags that do not satisfy eclude and include conditions within time < N sec during the next event cycle. (Note: Tags from step 20 should be removed before this step is executed)
		// NOTE: if a tag satisfies both filter list include and exclude conditions, it must not be reported
		System.out.println(receiveEcreportsFromSocket(9999));
		
		// step 22
		ale.unsubscribe("ecspec_R9_3a", "http://localhost:9999");
		
		// test end : undefine ECSpecs
		ale.undefine("ecspec_R9_1a");
		ale.undefine("ecspec_R9_1b");
		
		ale.undefine("ecspec_R9_2a");
		ale.undefine("ecspec_R9_2b");
		
		ale.undefine("ecspec_R9_3a");
		ale.undefine("ecspec_R9_3b");
		
	}
	@Test
	public void test_R13() throws Exception {
		// step 1
		ECSpec ecspec_1 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R13_1.xml"));
		ale.define("ECSpec_R13_1", ecspec_1);
		
		// step 2 : place a set of tag in the reader field
		
		// step 3
		ECReports ecreports = ale.poll("ECSpec_R13_1");
		printECReports(ecreports);
	}
	
	@Test
	public void test_R15() throws Exception {
		// precondition
		ECSpec ecspec_1 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R15_1.xml"));
		ECSpec ecspec_7 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R15_7.xml"));
		
		ale.define("ECSpec_R15_1", ecspec_1);
		ale.define("ECSpec_R15_7", ecspec_7);
		
		// step 1
		ale.subscribe("ECSpec_R15_1", "http://localhost:9999");
		
		// step 2 : move a set of tags into the reader field 
		String outExpectedString_2 = receiveEcreportsFromSocket(9999);
		ECReports ecreports_2_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R15_2_expected.xml"));
		assertTrue(compareEquals(ecreports_2_expected, outExpectedString_2));
		
		// step 3 : remove some but not all of the tags in the set starting before the next event cycle begins
		String outExpectedString3 = receiveEcreportsFromSocket(9999);
		ECReports ecreports_3_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R15_3_expected.xml"));
		assertTrue(compareEquals(ecreports_3_expected, outExpectedString3));
		
		// step 4 : add the tags that were removed in step 3
		System.out.println(receiveEcreportsFromSocket(9999));
		
		// step 5 : keep the tags in the reader field the same. do not add or remove any tags
		System.out.println(receiveEcreportsFromSocket(9999));
		
		// step 6
		ale.unsubscribe("ECSpec_R15_1", "http://localhost:9999");
		
		// step 7
		ale.subscribe("ECSpec_R15_7", "http://localhost:9999");
		
		// step 8 : move a set of tags into the reader field
		String outExpectedString8 = receiveEcreportsFromSocket(9999);
		ECReports ecreports_8_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R15_8_expected.xml"));
		assertTrue(compareEquals(ecreports_8_expected, outExpectedString8));
		
		// step 9 : add tags to the reader field
		String outExpectedString9 = receiveEcreportsFromSocket(9999);
		ECReports ecreports_9_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R15_9_expected.xml"));
		assertTrue(compareEquals(ecreports_9_expected, outExpectedString9));
		
		// step 10
		ale.unsubscribe("ECSpec_R15_7", "http://localhost:9999");
		
		
	}
	@Test
	public void test_R16() throws Exception {
		// precondition
		ECSpec ecspec_1 = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R16_1.xml"));
		ale.define("ECSpec_R16_1", ecspec_1);
		
		// step 1
		ale.subscribe("ECSpec_R16_1", "http://localhost:9999");
		
		String ecreports_1 = receiveEcreportsFromSocket(9999);
		ECReports ecreports_1_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R16_1_expected.xml"));
		compareEquals(ecreports_1_expected, ecreports_1);
		
		// step 2 : the set of tags should be in the reader feld of at least one of the logical readers.
		String ecreports_2 = receiveEcreportsFromSocket(9999);
		ECReports ecreports_2_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R16_2_expected.xml"));
		compareEquals(ecreports_2_expected, ecreports_2);
		
		// step 3 : remove all tags that would pass the include filter. 
		System.out.println(receiveEcreportsFromSocket(9999));
		
		// step 4 : the tag set should remain unchanged from step 3 for the next event cycle
		System.out.println(receiveEcreportsFromSocket(9999));
		
		// step 5 : add tags back to the tag set for the next event cycle. The tags should be able to pass the include filter
		System.out.println(receiveEcreportsFromSocket(9999));
		
		// step 6
		ale.unsubscribe("ECSpec_R16_1", "http://localhost:9999");
	}
	@Test
	public void test_R17() throws Exception {
		ECSpec ecspec = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R17_1.xml"));
		
		ECFieldSpec fieldspec1 = new ECFieldSpec();
		fieldspec1.setFieldname("@1.96.32");
		fieldspec1.setDatatype("uint");
		fieldspec1.setFormat("hex");
		
		ECFieldSpec fieldspec2 = new ECFieldSpec();
		fieldspec2.setFieldname("@4.1.0");
		fieldspec2.setDatatype("uint");
		fieldspec2.setFormat("hex");
		
		ECFieldSpec fieldspec3 = new ECFieldSpec();
		fieldspec3.setFieldname("epc");
		fieldspec3.setDatatype("epc");
		fieldspec3.setFormat("epc-tag");
		
		ECFieldSpec fieldspec4 = new ECFieldSpec();
		fieldspec4.setFieldname("killPwd");
		
		ECFieldSpec fieldspec5 = new ECFieldSpec();
		fieldspec5.setFieldname("accessPwd");
		
		ECFieldSpec fieldspec6 = new ECFieldSpec();
		fieldspec6.setFieldname("temperature");
		
		ECFilterListMember filterListMember1 = new ECFilterListMember();
		filterListMember1.setIncludeExclude("include");
		filterListMember1.setFieldspec(fieldspec1);
		filterListMember1.setPatList(new PatList());
		filterListMember1.getPatList().getPat().add("35C2761AC2B7F3FC70364A27");
		
		
		ECFilterListMember filterListMember2 = new ECFilterListMember();
		filterListMember2.setIncludeExclude("include");
		filterListMember2.setFieldspec(fieldspec2);
		filterListMember2.setPatList(new PatList());
		filterListMember2.getPatList().getPat().add("35C2761AC2B7F3FC70364A27");
		
		
		ECFilterListMember filterListMember3 = new ECFilterListMember();
		filterListMember3.setIncludeExclude("include");
		filterListMember3.setFieldspec(fieldspec3);
		filterListMember3.setPatList(new PatList());
		filterListMember3.getPatList().getPat().add("urn:epc:tag:gid-96:1178462.*.*");
		
		
		ECFilterListMember filterListMember4 = new ECFilterListMember();
		filterListMember4.setIncludeExclude("include");
		filterListMember4.setFieldspec(fieldspec4);
		filterListMember4.setPatList(new PatList());
		filterListMember4.getPatList().getPat().add("");
		
		
		ECFilterListMember filterListMember5 = new ECFilterListMember();
		filterListMember5.setIncludeExclude("include");
		filterListMember5.setFieldspec(fieldspec5);
		filterListMember5.setPatList(new PatList());
		filterListMember5.getPatList().getPat().add("");
		
		
		ECFilterListMember filterListMember6 = new ECFilterListMember();
		filterListMember6.setIncludeExclude("include");
		filterListMember6.setFieldspec(fieldspec6);
		filterListMember6.setPatList(new PatList());
		filterListMember6.getPatList().getPat().add("305419896");
		
		
		
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().setExtension(new ECFilterSpecExtension());
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().setFilterList(new FilterList());
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().add(filterListMember1);

		TMSpec tmspec = DeserializerUtil.deserializeTMSpec(ALEConformanceTest.class.getResourceAsStream("/tmspecs/TMFixedFieldListSpec_R17.xml"));
		aletm.defineTMSpec("temphumid", tmspec);

		ale.define("ECSpec_R17_1", ecspec);
		
		// step 1
		ECReports reports_1 = ale.poll("ECSpec_R17_1");
		
		// step 2
		ECReports ecreports_1_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R17_1_expected.xml"));
		assertTrue(compareEquals(ecreports_1_expected, reports_1));
		
		// step 3 : remove tags which were included in the report in step 2, poll again => skip
		ale.undefine("ECSpec_R17_1");
		
		// step 4
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().remove(0);
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().add(filterListMember2);
		ale.define("ECSpec_R17_4", ecspec);
		ECReports reports_4 = ale.poll("ECSpec_R17_4");
		ECReports ecreports_4_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R17_4_expected.xml"));
		assertTrue(compareEquals(ecreports_4_expected, reports_4));
		ale.undefine("ECSpec_R17_4");
		
		// step 5
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().remove(0);
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().add(filterListMember3);
		ale.define("ECSpec_R17_5", ecspec);
		ECReports reports_5 = ale.poll("ECSpec_R17_5");
		ECReports ecreports_5_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R17_5_expected.xml"));
		assertTrue(compareEquals(ecreports_5_expected, reports_5));
		ale.undefine("ECSpec_R17_5");
		
		// step 6 : killPwd => skip
		
		// step 7 : accessPwd => skip
		
		// step 8
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().remove(0);
		ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().add(filterListMember6);
		ale.define("ECSpec_R17_8", ecspec);
		ECReports reports_8 = ale.poll("ECSpec_R17_8");
		ECReports ecreports_8_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R17_8_expected.xml"));
		assertTrue(compareEquals(ecreports_8_expected, reports_8));
		printECReports(reports_8);
		ale.undefine("ECSpec_R17_8");
		
	}
	@Test
	public void test_R19() throws Exception {
		// pre-condition
		ECSpec ecspec = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R19.xml"));
		
		ale.define("ECSpec_R19", ecspec);
		

		// step 1
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				synchronized(this) {
					try {
						this.wait(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					ale.undefine("ECSpec_R19");
				} catch (NoSuchNameException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ImplementationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});		
		t.start();
		
		try {
			ECReports reports = ale.poll("ECSpec_R19");
			ECReports ecreports_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R19_expected.xml"));
			
			StringWriter expectedSw5 = new StringWriter();
			SerializerUtil.serializeECReports(ecreports_expected, expectedSw5);
			String outExpectedString5 = expectedSw5.toString();

			StringWriter sw5 = new StringWriter();
			SerializerUtil.serializeECReports(reports, sw5);
			String outString5 = sw5.toString();

			assertEquals(outExpectedString5.substring(outExpectedString5.indexOf("<reports")), (outString5.substring(outString5.indexOf("<reports"))));
			//assertTrue(compareEquals(ecreports_expected, reports));
		} catch (NoSuchNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	@Test
	public void test_R20() throws Exception {
		ECSpec ecspec = DeserializerUtil.deserializeECSpec(ALEConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_R20.xml"));
		ale.define("ECSpec_R20", ecspec);
		
		// step 1
		ale.subscribe("ECSpec_R20", "http://localhost:9999");
		
		// step 2 : wait for real-time clock to trigger
		String ecreports = receiveEcreportsFromSocket(9999);
		System.out.println(ecreports);
		ECReports ecreports_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R20_expected.xml"));
		assertEquals(convertToComparableString(ecreports_expected), convertToComparableString(ecreports));
		
		// step 3
		ale.undefine("ECSpec_R20");
		
	}
	
	private void printECSpec(ECSpec ecspec_1) throws Exception {
		SerializerUtil.serializeECSpecPretty(ecspec_1, System.out);
	}
	
	private boolean compareEquals(ECReports ecreport5, ECReports expectedEcreports5)
			throws Exception {
		StringWriter expectedSw5 = new StringWriter();
		SerializerUtil.serializeECReports(expectedEcreports5, expectedSw5);
		String outExpectedString5 = expectedSw5.toString();

		StringWriter sw5 = new StringWriter();
		SerializerUtil.serializeECReports(ecreport5, sw5);
		String outString5 = sw5.toString();

		return outExpectedString5.substring(outExpectedString5.indexOf("<reports")).equals(outString5.substring(outString5.indexOf("<reports")));
	}
	
	private boolean compareEquals(ECReports ecreport5, String outExpectedString5)
			throws Exception {
		
		StringWriter sw5 = new StringWriter();
		SerializerUtil.serializeECReports(ecreport5, sw5);
		String outString5 = sw5.toString();

		return outExpectedString5.substring(outExpectedString5.indexOf("<reports")).equals(outString5.substring(outString5.indexOf("<reports")));
	}
	
	private boolean containsInStringArray(String[] subscriberNames, String compare) {
		boolean exist = false;
		for(String subscriberName : subscriberNames) {
			if(subscriberName.equals(compare)) {
				exist = true;
			}
		}
		return exist;
	}

	private String convertToString(ECReports reports) throws Exception {
		// convert expected report to string
        StringWriter expectedsw = new StringWriter();
		SerializerUtil.serializeECReports(reports, expectedsw);
		String outExpectedString = expectedsw.toString();
		
		return outExpectedString;
	}
	
	private String convertToComparableString(ECReports reports) throws Exception {
		// convert expected report to string
        StringWriter expectedsw = new StringWriter();
		SerializerUtil.serializeECReports(reports, expectedsw);
		String outExpectedString = expectedsw.toString();
		
		return outExpectedString.substring(outExpectedString.indexOf("<reports>"));
	}
	
	private String convertToComparableString(String reports) throws Exception {
		
		return reports.substring(reports.indexOf("<reports>"));
	}
	
	private String receiveEcreportsFromSocket(int port) throws IOException {
		String ecReportsFromSocket = null;
		ServerSocket ss = null;
		Socket server = null;
		
		try {
			ss = new ServerSocket(port);
			server = ss.accept();
			
			System.out.println("Just connected to "
	                  + server.getRemoteSocketAddress());
				BufferedReader br = new BufferedReader(new InputStreamReader(server.getInputStream()));
	            
				// ignore HTTP header
	            br.readLine();
	            br.readLine();
	            br.readLine();
	            br.readLine();

	            // ignore newline
	            br.readLine();
	            
	            ecReportsFromSocket = br.readLine();
	            
	    		
		} catch(SocketTimeoutException s) {
            System.out.println("Socket timed out!");
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
        	if(server != null)
        		server.close();
        	if(ss != null)
        		ss.close();
        	
        }
		return ecReportsFromSocket;
	}
}
