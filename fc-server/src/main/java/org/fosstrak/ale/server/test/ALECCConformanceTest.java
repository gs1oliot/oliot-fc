/**
 * Copyright (C) 2014 KAIST
 * @author Wondeuk Yoon <wdyoon@resl.kaist.ac.kr> 
 * 
 */

package org.fosstrak.ale.server.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;
import org.fosstrak.ale.exception.CCSpecValidationException;
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
import org.fosstrak.ale.server.test.ALETMConformanceTest;
import org.fosstrak.ale.server.tm.ALETM;
import org.fosstrak.ale.util.DeserializerUtil;
import org.fosstrak.ale.util.SerializerUtil;
import org.fosstrak.ale.xsd.ale.epcglobal.CCReports;
import org.fosstrak.ale.xsd.ale.epcglobal.CCSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReports;
import org.fosstrak.ale.xsd.ale.epcglobal.ECSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.LRSpec;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;



public class ALECCConformanceTest {

	/** logger. */
	private static final Logger LOG = Logger.getLogger(ALECCConformanceTest.class);

	/** socket. */
	private Socket socket=null;
	private InputStream is = null;
	private BufferedReader in = null;

	public void SocketClient() throws IOException {
		try {
			socket = new Socket("127.0.0.1", 9999);
			System.out.println("Socket is connected.");
		} catch (IOException e) {
			throw e;
		}
	}
	public void startSocket() {

		try {		
			is = socket.getInputStream();
			in = new BufferedReader(new InputStreamReader(is));
		} catch (IOException e) { 
			System.out.println("Fail to connection."); 
		}
	}

	static ALE ale;
	static ALECC alecc;
	static ALETM aletm;

	static LogicalReaderManager lrm;

	@BeforeClass
	public static void beforeClass() throws Exception {

		LOG.info("Initializing Spring context.");

		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext.xml");

		LOG.info("Spring context initialized.");

		aletm = (ALETM) applicationContext.getBean("aletm");
		lrm = (LogicalReaderManager) applicationContext.getBean("logicalReaderManager");
		alecc = (ALECC) applicationContext.getBean("alecc");
		ale = (ALE) applicationContext.getBean("ale");

		LRSpec lrspec = DeserializerUtil.deserializeLRSpec(ALETMConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_W.xml"));

		System.out.println("Please connect reader... (timeout: 5000ms)");
		Thread.sleep(5000);

		lrm.define("LogicalReader1", lrspec);

		System.out.println("Waiting for reader initialization...");
		Thread.sleep(5000);
	}

	@Before
	public void beforeEachTest() throws NoSuchNameException, InterruptedException {
		String[] specs = alecc.getCCSpecNames();
		for (String tccspec : specs) alecc.undefine(tccspec);
		
	}

	//@Test
	public void test_W1() {
		assertEquals("1.1", alecc.getStandardVersion());
		assertEquals("", alecc.getVendorVersion());
	}

	//@Test
	public void test_W2() throws DuplicateNameException, ECSpecValidationException, ImplementationException, Exception {
		/** 
		 * precondition
		 * No CCSpecs are defined.
		 * Ensure all specName parameters accept as a name any non-empty string of Unicode characters that does not include Pattern_White_Space or Pattern_Syntax characters (see GM6)
		 * For step 7, the ALE implementation should support reading APIs.
		 **/

		CCSpec ccspec = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W2_2.xml"));
		ECSpec ecspec = DeserializerUtil.deserializeECSpec(getClass().getResourceAsStream("/ecspecs/ECSpec_W2_1.xml"));

		//System.out.println(spec.getBoundarySpec().getExtension().isWhenDataAvailable());
		//SerializerUtil.serializeECSpec(spec, System.out);
		//System.out.println(spec.getBoundarySpec().getExtension().isWhenDataAvailable());

		//step 1, 6
		alecc.define("commandcycle1", ccspec);
		String[] ccspecNames = alecc.getCCSpecNames();
		for(String ccspecName : ccspecNames) assertEquals("commandcycle1", ccspecName);

		//step 2, 6
		CCSpec defined = alecc.getCCSpec("commandcycle1");

		//step 3, 6
		StringWriter expectedsw = new StringWriter();
		SerializerUtil.serializeCCSpec(ccspec, expectedsw);
		String outExpectedString = expectedsw.toString();

		StringWriter sw = new StringWriter();
		SerializerUtil.serializeCCSpec(defined, sw);
		String outString = sw.toString();

		//SerializerUtil.serializeECReportsPretty(ecreports, new OutputStreamWriter(System.out));
		Assert.assertEquals(outExpectedString, outString);

		//step 4, 6
		alecc.undefine("commandcycle1");

		//step 5, 6
		String[] ccspecNames2 = alecc.getCCSpecNames();
		if (ccspecNames2.length != 0)
			Assert.fail();

		//step 7
		try
		{
			alecc.define("foo", ccspec);
			ale.define("foo", ecspec);
		}
		catch (DuplicateNameException e)
		{
			Assert.fail();
		}

		alecc.undefine("foo");
		ale.undefine("foo");

	}

	//@Test
	public void test_W3() throws Exception {

		int check = 0;
		CCSpec ccspec = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W2_2.xml"));
		alecc.define("commandcycle1", ccspec);

		//step 1
		try {
			alecc.getCCSpec("unknown");
		} catch (NoSuchNameException e) {
			check++;
		}

		//step 2
		try {
			alecc.poll("unknown");
		} catch (NoSuchNameException e) {
			check++;
		}

		//step 3
		try {
			alecc.subscribe("unknown", "http://localhost:9999");
		} catch (NoSuchNameException e) {
			check++;
		} catch (InvalidURIException e) {
			
			e.printStackTrace();
		} catch (DuplicateSubscriptionException e) {
			
			e.printStackTrace();
		}

		//step 4
		try {
			alecc.unsubscribe("commandcycle1", "http://localhost:80");
		} catch (NoSuchNameException e) {
			
			e.printStackTrace();
		} catch (NoSuchSubscriberException e) {
			check++;
		} catch (InvalidURIException e) {
			
			e.printStackTrace();
		}

		//step 5
		try {
			alecc.subscribe("commandcycle1", "localhost:80");
		} catch (NoSuchNameException e1) {
			
			e1.printStackTrace();
		} catch (InvalidURIException e1) {
			check++;
		} catch (DuplicateSubscriptionException e1) {
			
			e1.printStackTrace();
		}

		//step 6
		try {
			alecc.unsubscribe("unknown", "http://loaclhost:9999");
		} catch (NoSuchNameException e1) {
			check++;
		} catch (NoSuchSubscriberException e1) {
			
			e1.printStackTrace();
		} catch (InvalidURIException e1) {
			
			e1.printStackTrace();
		}

		//step 7
		try {
			alecc.getSubscribers("unknown");
		} catch (NoSuchNameException e1) {
			check++;
		}

		//step 8
		try {
			alecc.undefine("unknown");
		} catch (NoSuchNameException e1) {
			check++;
		}

		//step 9
		try {
			alecc.subscribe("commandcycle1", "htp://localhost:9999");
		} catch (NoSuchNameException e1) {
			
			e1.printStackTrace();
		} catch (InvalidURIException e1) {
			check++;
		} catch (DuplicateSubscriptionException e1) {
			
			e1.printStackTrace();
		}

		//step 10
		alecc.subscribe("commandcycle1", "http://localhost:9999");

		try {
			alecc.unsubscribe("commandcycle1", "ftp//localhost:9878");
		} catch (NoSuchNameException e1) {
			
			e1.printStackTrace();
		} catch (NoSuchSubscriberException e1) {
			
			e1.printStackTrace();
		} catch (InvalidURIException e1) {
			check++;
		}
		Thread.sleep(10000);
		alecc.unsubscribe("commandcycle1", "http://localhost:9999");

		//step 11
		alecc.define("commandcycle11", ccspec);

		//step 12
		String[] ccspecNames = alecc.getCCSpecNames();
		int c = 0;
		for(String ccspecName : ccspecNames) 
		{
			if (ccspecName.equals("commandcycle11"))
				c = 1;
		}
		Assert.assertEquals(1, c);

		//step 13
		try {
			alecc.define("commandcycle11", ccspec);
		} catch (DuplicateNameException e1) {
			check++;
		} catch (CCSpecValidationException e1) {
			
			e1.printStackTrace();
		} catch (ImplementationException e1) {
			
			e1.printStackTrace();
		}

		//step 14
		try {
			alecc.subscribe("commandcycle1", "http://localhost:9999");
			alecc.subscribe("commandcycle1", "http://localhost:9999");
		} catch (NoSuchNameException e1) {
			
			e1.printStackTrace();
		} catch (InvalidURIException e1) {
			
			e1.printStackTrace();
		} catch (DuplicateSubscriptionException e1) {
			check++;
		}
		Thread.sleep(10000);
		alecc.unsubscribe("commandcycle1", "http://localhost:9999");

		//step 15
		CCSpec ccspec15 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_15.xml"));
		try {
			alecc.immediate(ccspec15);
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}

		//step 16
		CCSpec ccspec16 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_16.xml"));
		try {
			alecc.define("commandcycle16", ccspec16);
		} catch (DuplicateNameException e) {
			
			e.printStackTrace();
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}

		//step 17
		CCSpec ccspec17 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_17.xml"));
		try {
			alecc.define("commandcycle17", ccspec17);
		} catch (DuplicateNameException e) {
			
			e.printStackTrace();
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}

		//step 18
		CCSpec ccspec18 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_18.xml"));
		try {
			alecc.define("commandcycle18", ccspec18);
		} catch (DuplicateNameException e) {
			
			e.printStackTrace();
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}

		//step 19
		CCSpec ccspec19 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_19.xml"));
		try {
			alecc.define("commandcycle19", ccspec19);
		} catch (DuplicateNameException e) {
			
			e.printStackTrace();
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}

		//step 20
		CCSpec ccspec20 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_20.xml"));
		try {
			alecc.define("commandcycle20", ccspec20);
		} catch (DuplicateNameException e) {
			
			e.printStackTrace();
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}

		// TODO : Implement start and Stop trigger
		//step 21: Fosstrak doesn't implement Start and Stop trigger.
		/*CCSpec ccspec21 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_21.xml"));
		try {
			alecc.immediate(ccspec21);
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}*/

		//step 22
		CCSpec ccspec22 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_22.xml"));
		try {
			alecc.define("commandcycle22", ccspec22);
		} catch (DuplicateNameException e) {
			
			e.printStackTrace();
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}

		//step 23
		CCSpec ccspec23 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_23.xml"));
		try {
			alecc.define("commandcycle23", ccspec23);
		} catch (DuplicateNameException e) {
			
			e.printStackTrace();
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}

		//step 24
		CCSpec ccspec24 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_24.xml"));
		try {
			alecc.define("commandcycle24", ccspec24);
		} catch (DuplicateNameException e) {
			
			e.printStackTrace();
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}

		//step 25
		CCSpec ccspec25 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_25.xml"));
		try {
			alecc.define("commandcycle25", ccspec25);
		} catch (DuplicateNameException e) {
			
			e.printStackTrace();
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}

		//step 26
		CCSpec ccspec26 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_26.xml"));
		try {
			alecc.define("commandcycle26", ccspec26);
		} catch (DuplicateNameException e) {
			
			e.printStackTrace();
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}

		//step 27
		CCSpec ccspec27 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_27.xml"));
		try {
			alecc.define("commandcycle27", ccspec27);
		} catch (DuplicateNameException e) {
			
			e.printStackTrace();
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}

		// TODO: 
		//step 28: Testing invalid for the specified operation.
		/*CCSpec ccspec28 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_28.xml"));
		try {
			alecc.define("commandcycle28", ccspec28);
		} catch (DuplicateNameException e) {
			
			e.printStackTrace();
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}

		// TODO: 
		//step 29: Testing contain an element with unknown statistics profile.
		CCSpec ccspec29 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_29.xml"));
		try {
			alecc.define("commandcycle29", ccspec29);
		} catch (DuplicateNameException e) {
			
			e.printStackTrace();
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}
		
		// TODO:
		//step 30: Testing filterList with patList that does not conform to syntax rules for patterns.
		CCSpec ccspec30 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_30.xml"));
		try {
			alecc.define("commandcycle30", ccspec30);
		} catch (DuplicateNameException e) {
			
			e.printStackTrace();
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}

		// TODO:
		//step 31: Testing filterList with fieldspec with unknown datatype and format.
		CCSpec ccspec31 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_31.xml"));
		try {
			alecc.define("commandcycle31", ccspec31);
		} catch (DuplicateNameException e) {
			
			e.printStackTrace();
		} catch (CCSpecValidationException e) {
			check++;
		} catch (ImplementationException e) {
			
			e.printStackTrace();
		}

		// TODO:
		//step 32: Testing diacritical mark.
		CCSpec ccspec32 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W2_2.xml"));
		alecc.define("commandcycle32", ccspec32);
		try {
			alecc.undefine("commāndcycle32");
		} catch (NoSuchNameException e) {
			check++;
		}

		//step 33: Testing CCParameterEntryList intstances wit the same name.
		//It is not existed parameter in fosstrak.
		CCSpec ccspec33 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W3_33.xml"));
		try {
			alecc.poll("commandcycle33");
		} catch (NoSuchNameException e) {
			
			e.printStackTrace();
		}

		// TODO:
		//step 34: Invoke the immediate method which includes a CCOpDataSpec of type PARAMETER.

		// TODO:
		//step 35: Define a CCSpec that includes a CCOpDataSpec of type PARAMETER and then Invoke the the subscribe method using the CCSpec just defined.

		 */
		Assert.assertEquals(24, check);


	}

	//@Test
	public void test_W4() throws Exception {
		CCSpec ccspec1 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W4_1.xml"));
		alecc.define("commandcycle1", ccspec1);

		Thread.sleep(21000);
		// step 1
		alecc.subscribe("commandcycle1", "http://localhost:9995");

		// step 2
		String[] subscriberNames = alecc.getSubscribers("commandcycle1");

		containsInStringArray(subscriberNames, "http://localhost:9995");

		String ccReportsFromSocket = receiveEcreportsFromSocket(9995);

		CCReports ccreports_4_expected = DeserializerUtil.deserializeCCReports(ALEConformanceTest.class.getResourceAsStream("/ccreports/CCReports_W4_1.xml"));
		StringWriter expectedsw = new StringWriter();
		SerializerUtil.serializeCCReports(ccreports_4_expected, expectedsw);
		String outExpectedString = expectedsw.toString();

		Assert.assertEquals(outExpectedString.substring(outExpectedString.indexOf("<cmdReports")), ccReportsFromSocket.substring(ccReportsFromSocket.indexOf("<cmdReports")));

		// step 3
		alecc.subscribe("commandcycle1", "http://localhost:9996");

		// step 4
		subscriberNames = alecc.getSubscribers("commandcycle1");
		assertTrue(containsInStringArray(subscriberNames, "http://localhost:9995"));
		assertTrue(containsInStringArray(subscriberNames, "http://localhost:9996"));

		ccReportsFromSocket = receiveEcreportsFromSocket(9996);

		ccreports_4_expected = DeserializerUtil.deserializeCCReports(ALEConformanceTest.class.getResourceAsStream("/ccreports/CCReports_W4_1.xml"));
		expectedsw = new StringWriter();
		SerializerUtil.serializeCCReports(ccreports_4_expected, expectedsw);
		outExpectedString = expectedsw.toString();

		Assert.assertEquals(outExpectedString.substring(outExpectedString.indexOf("<cmdReports")), ccReportsFromSocket.substring(ccReportsFromSocket.indexOf("<cmdReports")));

		// step 5
		alecc.unsubscribe("commandcycle1", "http://localhost:9995");

		// step 6
		subscriberNames = alecc.getSubscribers("commandcycle1");
		assertTrue(!containsInStringArray(subscriberNames, "http://localhost:9995"));

		// step 7
		alecc.unsubscribe("commandcycle1", "http://localhost:9996");

		// step 8
		subscriberNames = alecc.getSubscribers("commandcycle1");
		assertTrue(!containsInStringArray(subscriberNames, "http://localhost:9996"));

		// step 9 : repeat 1~8 with TCP Notification URI => skip
		// step 10 : repeat 1~8 with File Notification URI => skip

	}
	
	// TODO:
	// Lock is not implemented.
	//@Test
	public void test_W5() throws Exception {
		
	}

	//@Test
	public void test_W6() throws Exception {
		
		Thread.sleep(21000);
		CCSpec ccspec1 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W6_1.xml"));
		alecc.define("commandcycle1", ccspec1);

		//step 1
		//step 2
		//alecc.subscribe("commandcycle1", "http://localhost:9999");	
		CCReports ccreport2 = alecc.poll("commandcycle1");
		Thread.sleep(1000);
		DeserializerUtil.deserializeCCReports(getClass().getResourceAsStream("/ccreports/CCReports_W6_1.xml"));
		SerializerUtil.serializeCCReports(ccreport2, new OutputStreamWriter(System.out));

		CCReports expectedCcreports2 = DeserializerUtil.deserializeCCReports(getClass().getResourceAsStream("/ccreports/CCReports_W6_1.xml"));
		StringWriter expectedSw2 = new StringWriter();
		SerializerUtil.serializeCCReports(expectedCcreports2, expectedSw2);
		String outExpectedString2 = expectedSw2.toString();
		outExpectedString2 = outExpectedString2.replaceAll("\\n","").replaceAll("\\t","");

		StringWriter sw2 = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport2, sw2);
		String outString2 = sw2.toString();

		Assert.assertEquals(outExpectedString2.substring(outExpectedString2.indexOf("<cmdReports")), outString2.substring(outString2.indexOf("<cmdReports")));


		//step 3: remove tags from reader.
		/*alecc.undefine("commandcycle1");
		Thread.sleep(10000);
		CCSpec ccspec4 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W6_2.xml"));
		alecc.define("commandcycle4", ccspec4);
		//step 4: skip because we cannot remove tags from reader.
		CCReports ccreport4 = alecc.poll("commandcycle4");
		Thread.sleep(1000);

		CCReports expectedCcreports4 = DeserializerUtil.deserializeCCReports(getClass().getResourceAsStream("/ccreports/CCReports_W6_2.xml"));
		StringWriter expectedSw4 = new StringWriter();
		SerializerUtil.serializeCCReports(expectedCcreports4, expectedSw4);
		String outExpectedString4 = expectedSw4.toString();
		outExpectedString4 = outExpectedString4.replaceAll("\\n","").replaceAll("\\t","");

		StringWriter sw4 = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport4, sw4);
		String outString4 = sw4.toString();

		Assert.assertEquals(outExpectedString4.substring(outExpectedString4.indexOf("<cmdReports")), outString4.substring(outString4.indexOf("<cmdReports")));

		//step 5: place a tags set in the reader field.
		//step 6
		alecc.undefine("commandcycle4");
		Thread.sleep(10000);*/
		CCSpec ccspec5 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W6_3.xml"));
		alecc.define("commandcycle5", ccspec5);

		//alecc.subscribe("commandcycle5", "http://localhost:9999");
		CCReports ccreport5 = alecc.poll("commandcycle5");
		Thread.sleep(1000);

		CCReports expectedCcreports5 = DeserializerUtil.deserializeCCReports(getClass().getResourceAsStream("/ccreports/CCReports_W6_1.xml"));
		StringWriter expectedSw5 = new StringWriter();
		SerializerUtil.serializeCCReports(expectedCcreports5, expectedSw5);
		String outExpectedString5 = expectedSw5.toString();
		outExpectedString5 = outExpectedString5.replaceAll("\\n","").replaceAll("\\t","");

		StringWriter sw5 = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport5, sw5);
		String outString5 = sw5.toString();

		Assert.assertEquals(outExpectedString5.substring(outExpectedString5.indexOf("<cmdReports")), outString5.substring(outString5.indexOf("<cmdReports")));

		//step 7: remove the all tags.
		/*alecc.undefine("commandcycle5");
		Thread.sleep(10000);
		CCSpec ccspec8 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W6_4.xml"));
		alecc.define("commandcycle8", ccspec8);
		//step 8: skip because we cannot remove tags from reader_emulator.

		CCReports ccreport8 = alecc.poll("commandcycle8");
		Thread.sleep(1000);

		CCReports expectedCcreports8 = DeserializerUtil.deserializeCCReports(getClass().getResourceAsStream("/ccreports/CCReports_W6_2.xml"));
		StringWriter expectedSw8 = new StringWriter();
		SerializerUtil.serializeCCReports(expectedCcreports8, expectedSw8);
		String outExpectedString8 = expectedSw8.toString();
		outExpectedString8 = outExpectedString8.replaceAll("\\n","").replaceAll("\\t","");

		StringWriter sw8 = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport8, sw8);
		String outString8 = sw8.toString();

		Assert.assertEquals(outExpectedString8.substring(outExpectedString8.indexOf("<cmdReports")), outString8.substring(outString8.indexOf("<cmdReports")));
		 */
	}

	//TODO: WEAK CCParameter List.
	//@Test
	public void test_W7() throws Exception {
		//step 1
		//step 2
		CCSpec ccspec2 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W7_1.xml"));
		alecc.define("commandcycle2", ccspec2);

		CCReports ccreport2 = alecc.poll("commandcycle2"); //param1

		CCReports expectedCcreports2 = DeserializerUtil.deserializeCCReports(getClass().getResourceAsStream("/ccreports/CCReports_W7_1.xml"));
		StringWriter expectedSw2 = new StringWriter();
		SerializerUtil.serializeCCReports(expectedCcreports2, expectedSw2);
		String outExpectedString2 = expectedSw2.toString();

		StringWriter sw2 = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport2, sw2);
		String outString2 = sw2.toString();

		Assert.assertEquals(outExpectedString2.substring(outExpectedString2.indexOf("<cmdReports")), outString2.substring(outString2.indexOf("<cmdReports")));

		//step 3
		//step 4
		CCReports ccreport4 = alecc.poll("commandcycle2"); //param2

		CCReports expectedCcreports4 = DeserializerUtil.deserializeCCReports(getClass().getResourceAsStream("/ccreports/CCReports_W7_2.xml"));
		StringWriter expectedSw4 = new StringWriter();
		SerializerUtil.serializeCCReports(expectedCcreports4, expectedSw4);
		String outExpectedString4 = expectedSw4.toString();

		StringWriter sw4 = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport4, sw4);
		String outString4 = sw4.toString();

		Assert.assertEquals(outExpectedString4.substring(outExpectedString4.indexOf("<cmdReports")), outString4.substring(outString4.indexOf("<cmdReports")));

		//step 5
		CCReports ccreport5 = alecc.poll("commandcycle2"); //param2

		CCReports expectedCcreports5 = DeserializerUtil.deserializeCCReports(getClass().getResourceAsStream("/ccreports/CCReports_W7_3.xml"));
		StringWriter expectedSw5 = new StringWriter();
		SerializerUtil.serializeCCReports(expectedCcreports5, expectedSw5);
		String outExpectedString5 = expectedSw5.toString();

		StringWriter sw5 = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport5, sw5);
		String outString5 = sw5.toString();

		Assert.assertEquals(outExpectedString5.substring(outExpectedString5.indexOf("<cmdReports")), outString5.substring(outString5.indexOf("<cmdReports")));

		//step 6
		//step 7
		CCReports ccreport7 = alecc.poll("commandcycle2"); //param2

		CCReports expectedCcreports7 = DeserializerUtil.deserializeCCReports(getClass().getResourceAsStream("/ccreports/CCReports_W7_3.xml"));
		StringWriter expectedSw7 = new StringWriter();
		SerializerUtil.serializeCCReports(expectedCcreports7, expectedSw7);
		String outExpectedString7 = expectedSw7.toString();

		StringWriter sw7 = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport7, sw7);
		String outString7 = sw7.toString();

		Assert.assertEquals(outExpectedString7.substring(outExpectedString7.indexOf("<cmdReports")), outString7.substring(outString7.indexOf("<cmdReports")));

	}


	//@Test
	public void test_W8() throws Exception {

		//step 1
		//step 2
		Thread.sleep(21000);
		CCSpec ccspec2 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W8_1.xml"));

		CCReports ccreport2 = alecc.immediate(ccspec2);

		CCReports expectedCcreports2 = DeserializerUtil.deserializeCCReports(getClass().getResourceAsStream("/ccreports/CCReports_W8_1.xml"));
		StringWriter expectedSw2 = new StringWriter();
		SerializerUtil.serializeCCReports(expectedCcreports2, expectedSw2);
		String outExpectedString2 = expectedSw2.toString();

		StringWriter sw2 = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport2, sw2);
		String outString2 = sw2.toString();

		Assert.assertEquals(outExpectedString2.substring(outExpectedString2.indexOf("<cmdReports")), outString2.substring(outString2.indexOf("<cmdReports")));

	}

	//TODO:MUST Start, Stop trigger
	//@Test
	public void test_W9() throws Exception {

		//step 1 URI1, URI3
		CCSpec ccspec2 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W9_1.xml"));
		alecc.define("commandcycle2", ccspec2);

		CCReports ccreport2 = alecc.poll("commandcycle2");

		//step 2
		//step 3

		CCReports expectedCcreports2 = DeserializerUtil.deserializeCCReports(getClass().getResourceAsStream("/ccreports/CCReports_W9_1.xml"));
		StringWriter expectedSw2 = new StringWriter();
		SerializerUtil.serializeCCReports(expectedCcreports2, expectedSw2);
		String outExpectedString2 = expectedSw2.toString();

		StringWriter sw2 = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport2, sw2);
		String outString2 = sw2.toString();

		Assert.assertEquals(outExpectedString2.substring(outExpectedString2.indexOf("<cmdReports")), outString2.substring(outString2.indexOf("<cmdReports")));

		//step 4 URI2, URI4
		CCReports ccreport4 = alecc.poll("commandcycle2");

		//step 5
		//step 6

		CCReports expectedCcreports4 = DeserializerUtil.deserializeCCReports(getClass().getResourceAsStream("/ccreports/CCReports_W9_2.xml"));
		StringWriter expectedSw4 = new StringWriter();
		SerializerUtil.serializeCCReports(expectedCcreports4, expectedSw4);
		String outExpectedString4 = expectedSw4.toString();

		StringWriter sw4 = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport4, sw4);
		String outString4 = sw4.toString();

		Assert.assertEquals(outExpectedString4.substring(outExpectedString4.indexOf("<cmdReports")), outString4.substring(outString4.indexOf("<cmdReports")));

	}

	//@Test
	public void test_W10() throws Exception {

		//step 1
		Thread.sleep(21000);
		CCSpec ccspec1 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W10_1.xml"));
		alecc.define("commandcycle1", ccspec1);
		alecc.subscribe("commandcycle1", "http://localhost:9991");

		//step 2
		String[] list = alecc.getSubscribers("commandcycle1");
		assertTrue(containsInStringArray(list, "http://localhost:9991"));

		String ccReportsFromSocket = receiveEcreportsFromSocket(9991);

		CCReports ccreports_4_expected = DeserializerUtil.deserializeCCReports(ALEConformanceTest.class.getResourceAsStream("/ccreports/CCReports_W10_1.xml"));
		StringWriter expectedsw = new StringWriter();
		SerializerUtil.serializeCCReports(ccreports_4_expected, expectedsw);
		String outExpectedString = expectedsw.toString();

		Assert.assertEquals(outExpectedString.substring(outExpectedString.indexOf("<cmdReports")), ccReportsFromSocket.substring(ccReportsFromSocket.indexOf("<cmdReports")));

		//TODO: KILL operation.
		//step 3
		alecc.unsubscribe("commandcycle1", "http://localhost:9991");

		//step 4 : Skip KILL is not implemented.
		//CCSpec ccspec4 = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W10_2.xml"));
		//alecc.define("commandcycle4", ccspec4);
		//alecc.subscribe("commandcycle4", "http://localhost:9999");

		//step 5
		//String[] list2 = alecc.getSubscribers("commandcycle4");
		//for(String subcriber : list2) assertEquals("http://localhost:9999", subcriber);

		//checking ccreprot4 is correct.

		//step 6
		//alecc.unsubscribe("commandcycle4", "http://localhost:9999");

	}
	
	// TODO : EPCCache is not implemented.
	//@Test
	public void test_W11() throws Exception {
		
	}
	
	// TODO : Association Table is not implemented.
	//@Test
	public void test_W12() throws Exception {
		
	}
	
	// TODO : RNG is not implemented.
	//@Test
	public void test_W13() throws Exception {
		
	}

	@Test
	public void test_W14() throws Exception {

		CCReports expectedCcreports = null;
		StringWriter expectedSw = new StringWriter();
		String outExpectedString = null;
		StringWriter sw = new StringWriter();
		String outString = null;
		CCSpec ccspec = null;
		CCReports ccreport = null;

		//step 1: reset tags. Except tidBank.
		//step 2
		ccspec = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W14_1.xml"));

		ccreport = alecc.immediate(ccspec);
		
		sw = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport, sw);
		outString = sw.toString();

		//step 3
		Thread.sleep(21000);
		ccspec = null;
		ccreport = null;
		ccspec = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W14_2.xml"));

		ccreport = alecc.immediate(ccspec);
		
		expectedCcreports = DeserializerUtil.deserializeCCReports(getClass().getResourceAsStream("/ccreports/CCReports_W14_3.xml"));
		expectedSw = new StringWriter();
		SerializerUtil.serializeCCReports(expectedCcreports, expectedSw);
		outExpectedString = expectedSw.toString();

		sw = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport, sw);
		outString = sw.toString();

		Assert.assertEquals(outExpectedString.substring(outExpectedString.indexOf("<cmdReports")), outString.substring(outString.indexOf("<cmdReports")));

		//step 4: reset tags. Except tidBank.
		//step 5
		expectedCcreports = null;
		outExpectedString = null;
		outString = null;
		ccspec = null;
		ccreport = null;

		ccspec = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W14_3.xml"));

		ccreport = alecc.immediate(ccspec);

		sw = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport, sw);
		outString = sw.toString();
		
		//step 6
		Thread.sleep(21000);
		ccspec = null;
		ccreport = null;	
		ccspec = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W14_4.xml"));

		ccreport = alecc.immediate(ccspec);

		expectedCcreports = DeserializerUtil.deserializeCCReports(getClass().getResourceAsStream("/ccreports/CCReports_W14_6.xml"));
		expectedSw = new StringWriter();
		SerializerUtil.serializeCCReports(expectedCcreports, expectedSw);
		outExpectedString = expectedSw.toString();

		sw = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport, sw);
		outString = sw.toString();

		Assert.assertEquals(outExpectedString.substring(outExpectedString.indexOf("<cmdReports")), outString.substring(outString.indexOf("<cmdReports")));

		//TODO : LOCK operation.
		//step 7: reset tags. afi field writing is not implemented.
		//step 8
/*		expectedCcreports = null;
		outExpectedString = null;
		outString = null;
		ccspec = null;
		ccreport = null;

		ccspec = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W14_5.xml"));

		ccreport = alecc.immediate(ccspec);

		sw = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport, sw);
		outString = sw.toString();

		//step 9
		Thread.sleep(21000);
		ccspec = null;
		ccreport = null;	
		ccspec = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W14_6.xml"));

		ccreport = alecc.immediate(ccspec);

		expectedCcreports = DeserializerUtil.deserializeCCReports(getClass().getResourceAsStream("/ccreports/CCReports_W14_9.xml"));
		expectedSw = new StringWriter();
		SerializerUtil.serializeCCReports(expectedCcreports, expectedSw);
		outExpectedString = expectedSw.toString();

		sw = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport, sw);
		outString = sw.toString();

		Assert.assertEquals(outExpectedString.substring(outExpectedString.indexOf("<cmdReports")), outString.substring(outString.indexOf("<cmdReports")));
*/		
		//step 10: reset tags. nsi field writing is not implemented.
		//step 11
		/*
		expectedCcreports = null;
		outExpectedString = null;
		outString = null;
		ccspec = null;
		ccreport = null;

		ccspec = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W14_7.xml"));

		ccreport = alecc.immediate(ccspec);

		sw = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport, sw);
		outString = sw.toString();

		//step 12
		Thread.sleep(21000);
		ccspec = null;
		ccreport = null;	
		ccspec = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_W14_8.xml"));

		ccreport = alecc.immediate(ccspec);

		expectedCcreports = DeserializerUtil.deserializeCCReports(getClass().getResourceAsStream("/ccreports/CCReports_W14_12.xml"));
		expectedSw = new StringWriter();
		SerializerUtil.serializeCCReports(expectedCcreports, expectedSw);
		outExpectedString = expectedSw.toString();

		sw = new StringWriter();
		SerializerUtil.serializeCCReports(ccreport, sw);
		outString = sw.toString();

		Assert.assertEquals(outExpectedString.substring(outExpectedString.indexOf("<cmdReports")), outString.substring(outString.indexOf("<cmdReports")));
		*/
		
		//TODO : ADD, CHECK, DELETE operation.
		//step 13 ~ 20 : Not implemented ADD, CHECK, DELETE operation.
		
	}
	
	//TODO: MUST initialization and termination condition.
	//@Test
	public void test_W15() throws Exception {
		// pre-condition
		CCSpec ccspec = DeserializerUtil.deserializeCCSpec(ALECCConformanceTest.class.getResourceAsStream("/ccspecs/CCSpec_W15_1.xml"));
		
		alecc.define("commandcycle15_1", ccspec);
		

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
					alecc.undefine("commandcycle15_1");
				} catch (NoSuchNameException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			
		});		
		t.start();
		
		try {
			ECReports reports = ale.poll("ECSpec_R19");
			ECReports ecreports_expected = DeserializerUtil.deserializeECReports(ALEConformanceTest.class.getResourceAsStream("/ecreports/ECReports_R19_expected.xml"));
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

	private boolean containsInStringArray(String[] subscriberNames, String compare) {
		boolean exist = false;
		for(String subscriberName : subscriberNames) {
			if(subscriberName.equals(compare)) {
				exist = true;
			}
		}
		return exist;
	}
}
