/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */

package org.fosstrak.ale.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.fosstrak.ale.exception.DuplicateNameException;
import org.fosstrak.ale.exception.ImmutableReaderException;
import org.fosstrak.ale.exception.ImplementationException;
import org.fosstrak.ale.exception.InUseException;
import org.fosstrak.ale.exception.NoSuchNameException;
import org.fosstrak.ale.exception.NonCompositeReaderException;
import org.fosstrak.ale.exception.ReaderLoopException;
import org.fosstrak.ale.exception.SecurityException;
import org.fosstrak.ale.exception.ValidationException;
import org.fosstrak.ale.server.ALE;
import org.fosstrak.ale.server.cc.ALECC;
import org.fosstrak.ale.server.readers.LogicalReaderManager;
import org.fosstrak.ale.util.DeserializerUtil;
import org.fosstrak.ale.util.SerializerUtil;
import org.fosstrak.ale.xsd.ale.epcglobal.CCSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.LRProperty;
import org.fosstrak.ale.xsd.ale.epcglobal.LRSpec;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ALELRConformanceTest {
	/** logger. */
	private static final Logger LOG = Logger.getLogger(ALELRConformanceTest.class);
	
	static LogicalReaderManager alelr;
	
	static ALE ale;
	
	static ALECC alecc;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		// please connect readers of EPC 000011112222333344445555 and aaaa11112222333344445555		
		

		LOG.info("Initializing Spring context.");
        
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext.xml");
        
        LOG.info("Spring context initialized.");
        
        alelr = (LogicalReaderManager) applicationContext.getBean("logicalReaderManager");
        
        ale = (ALE) applicationContext.getBean("ale");
        
        alecc = (ALECC) applicationContext.getBean("alecc");

        LRSpec lrspec = DeserializerUtil.deserializeLRSpec(ALELRConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_LR.xml"));
        
        System.out.println("Please connect reader... (timeout: 5000ms)");
        Thread.sleep(5000);
        
        alelr.define("limg00n_emulator", lrspec);
        
        System.out.println("Waiting for reader initialization...");
        Thread.sleep(5000);
	}
	
	@Before
	public void beforeEachTest() throws Exception, ImplementationException, NoSuchNameException, InUseException, ImmutableReaderException {
		
	}
	
	@Test
	public void test_L1() throws ImplementationException {
		assertEquals("1.1", alelr.getStandardVersion());
		assertEquals("", alelr.getVendorVersion());
	}
	
	@Test
	public void test_L2() throws Exception {

		// step 1
		LRSpec lrspec = DeserializerUtil.deserializeLRSpec(ALELRConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_L2.xml"));
		alelr.define("L1", lrspec);
		
		// step 2
		assertTrue(alelr.getLogicalReaderNames().contains("L1"));
		
		// step 3
		assertEquals(lrspec, alelr.getLRSpec("L1"));
		
		// step 4
		alelr.define("L2", lrspec);
		
		// step 5
		List<String> lrNames = alelr.getLogicalReaderNames();
		assertTrue(lrNames.contains("L1") && lrNames.contains("L2"));
		
		// step 6
		alelr.undefine("L1");
		
		// step 7
		assertEquals("L2", alelr.getLogicalReaderNames().get(0));
		
		// step 8
		LRSpec lrspec2 = DeserializerUtil.deserializeLRSpec(ALELRConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_L2_2.xml"));
		alelr.update("L2", lrspec2);
		
		// step 9
		assertEquals(lrspec2, alelr.getLRSpec("L2"));
		
		// step 10
		alelr.undefine("L2");
		
		// step 11
		lrNames = alelr.getLogicalReaderNames();
		assertTrue(!lrNames.contains("L2"));
	}
	
	@Test
	public void test_L3() throws Exception {
		// step 1
		LRSpec lrspec = DeserializerUtil.deserializeLRSpec(ALELRConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_L3_1_Composite.xml"));
		alelr.define("L1", lrspec);
		
		
		// step 2
		List<String> readerNames = alelr.getLogicalReaderNames();
		assertTrue(readerNames.contains("L1"));
		
		// step 3
		List<String> lrNames = new ArrayList<String>();
		lrNames.add("limg00n_emulator");
		alelr.addReaders("L1", lrNames);
		
		
		// step 4
		LRSpec spec = alelr.getLRSpec("L1");
		//SerializerUtil.serializeLRSpec(spec, new OutputStreamWriter(System.out));
		
		StringWriter sw = new StringWriter();
		SerializerUtil.serializeLRSpec(spec, sw);
		String outString = sw.toString();
		
		LRSpec expected = DeserializerUtil.deserializeLRSpec(ALELRConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_L3_4_Expected.xml"));
		StringWriter expectedSw = new StringWriter();
		SerializerUtil.serializeLRSpec(expected, expectedSw);
		String outExpectedString = expectedSw.toString();
		
		Assert.assertEquals(outExpectedString.substring(outExpectedString.indexOf("<isComposite>")), outString.substring(outString.indexOf("<isComposite>")));
		
		
		// step 5
		alelr.removeReaders("L1", lrNames);
		spec = alelr.getLRSpec("L1");
		//SerializerUtil.serializeLRSpec(spec, new OutputStreamWriter(System.out));
		
		sw = new StringWriter();
		SerializerUtil.serializeLRSpec(spec, sw);
		outString = sw.toString();
		
		expected = DeserializerUtil.deserializeLRSpec(ALELRConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_L3_5_Expected.xml"));
		expectedSw = new StringWriter();
		SerializerUtil.serializeLRSpec(expected, expectedSw);
		outExpectedString = expectedSw.toString();
		
		Assert.assertEquals(outExpectedString.substring(outExpectedString.indexOf("<isComposite>")), outString.substring(outString.indexOf("<isComposite>")));
		
		
		// step 6
		alelr.setReaders("L1", lrNames);
		
		
		// step 7
		spec = alelr.getLRSpec("L1");
		//SerializerUtil.serializeLRSpec(spec, new OutputStreamWriter(System.out));
		
		sw = new StringWriter();
		SerializerUtil.serializeLRSpec(spec, sw);
		outString = sw.toString();
		
		expected = DeserializerUtil.deserializeLRSpec(ALELRConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_L3_7_Expected.xml"));
		expectedSw = new StringWriter();
		SerializerUtil.serializeLRSpec(expected, expectedSw);
		outExpectedString = expectedSw.toString();
		
		Assert.assertEquals(outExpectedString.substring(outExpectedString.indexOf("<isComposite>")), outString.substring(outString.indexOf("<isComposite>")));
		
		
		// step 8
		alelr.undefine("L1");
		assertTrue(!alelr.getLogicalReaderNames().contains("L1"));
	}
	
	@Test
	public void test_L5() throws Exception {
		// step 1
		LRSpec lrspec = DeserializerUtil.deserializeLRSpec(ALELRConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_L5_1.xml"));
		alelr.define("LR1", lrspec);

		
		// step 2
		boolean successFlag = true;
		try {
			alelr.define("LR1", lrspec);
			successFlag = true;
		} catch(DuplicateNameException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 3: parameters for Tag Smoothing => skip
		
		// step 4
		successFlag = true;
		try {
			alelr.update("LR3", lrspec);
			successFlag = true;
		} catch(NoSuchNameException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		//---------------- result confirmed
		// step 5
		LRSpec invalidLrspec = DeserializerUtil.deserializeLRSpec(ALELRConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_L5_invalid.xml"));
		successFlag = true;
		try {
			alelr.update("LR1", invalidLrspec);
			successFlag = true;
		} catch(ValidationException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 6
		LRSpec lrspec_6 = DeserializerUtil.deserializeLRSpec(ALELRConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_L5_6.xml"));
		alelr.define("LR2", lrspec_6);
		
		// step 7
		ECSpec ecspec_7 = DeserializerUtil.deserializeECSpec(ALELRConformanceTest.class.getResourceAsStream("/ecspecs/ECSpec_L5_7.xml"));
		ale.define("EC1", ecspec_7);
		
		// step 8
		ale.subscribe("EC1", "http://localhost:9999");
		
		// step 9
		synchronized(this) {
			this.wait(2000);	
		}
		
		
		// step 10
		successFlag = true;
		try {
			LRSpec lrspec_10 = DeserializerUtil.deserializeLRSpec(ALELRConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_L5_10.xml"));
			alelr.update("LR2", lrspec_10);
			successFlag = true;
		} catch(InUseException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 11
		ale.unsubscribe("EC1", "http://localhost:9999");
		
		// step 12 : externally-defined reader => skip
		
		// step 13
		successFlag = true;
		try {
			LRSpec lrspec_13 = DeserializerUtil.deserializeLRSpec(ALELRConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_L5_13.xml"));
			alelr.update("LR1", lrspec_13);
			successFlag = true;
		} catch(ReaderLoopException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 14
		successFlag = true;
		try {
			alelr.undefine("LR3");
			successFlag = true;
		} catch(NoSuchNameException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		ale.subscribe("EC1", "http://localhost:9999");
		synchronized (this) {
			this.wait(2000);	
		}
		
		successFlag = true;
		try {
			alelr.undefine("LR2");
			successFlag = true;
		} catch(InUseException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		ale.unsubscribe("EC1", "http://localhost:9999");
		
		// step 15
		successFlag = true;
		try {
			alelr.getLRSpec("LR3");
			successFlag = true;
		} catch(NoSuchNameException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 16
		successFlag = true;
		try {
			alelr.addReaders("LR3", new ArrayList<String>());
			successFlag = true;
		} catch(NoSuchNameException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 17
		successFlag = true;
		try {
			List<String> readerList = new ArrayList<String>();
			readerList.add("unknown_reader");
			alelr.addReaders("LR1", readerList);
			successFlag = true;
		} catch(ValidationException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 18
		ale.subscribe("EC1", "http://localhost:9999");
		synchronized (this) {
			this.wait(2000);	
		}
		successFlag = true;
		try {
			List<String> readerList = new ArrayList<String>();
			readerList.add("LR1");
			alelr.addReaders("LR2", readerList);
			successFlag = true;
		} catch(InUseException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		ale.unsubscribe("EC1", "http://localhost:9999");
		
		// step 19 : externally-defined reader name => skip
		
		// step 20
		LRSpec lrspec_20 = DeserializerUtil.deserializeLRSpec(ALELRConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_L5_20.xml"));
		alelr.define("LR4", lrspec_20);
		
		// step 21
		try {
			List<String> readerList = new ArrayList<String>();
			readerList.add("limg00n_emulator");
			alelr.addReaders("LR4", readerList);
			successFlag = true;
		} catch(NonCompositeReaderException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 22
		try {
			List<String> readerList = new ArrayList<String>();
			readerList.add("LR1");
			alelr.addReaders("LR1", readerList);
			successFlag = true;
		} catch(ReaderLoopException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 23
				// step 16
				successFlag = true;
				try {
					alelr.setReaders("LR3", new ArrayList<String>());
					successFlag = true;
				} catch(NoSuchNameException e) {
					successFlag = false;
				} finally {
					if(successFlag) Assert.fail();
				}
				
				// step 17
				successFlag = true;
				try {
					List<String> readerList = new ArrayList<String>();
					readerList.add("unknown_reader");
					alelr.setReaders("LR1", readerList);
					successFlag = true;
				} catch(ValidationException e) {
					successFlag = false;
				} finally {
					if(successFlag) Assert.fail();
				}
				
				// step 18
				ale.subscribe("EC1", "http://localhost:9999");
				synchronized (this) {
					this.wait(2000);	
				}
				successFlag = true;
				try {
					List<String> readerList = new ArrayList<String>();
					readerList.add("LR1");
					alelr.setReaders("LR2", readerList);
					successFlag = true;
				} catch(InUseException e) {
					successFlag = false;
				} finally {
					if(successFlag) Assert.fail();
				}
				ale.unsubscribe("EC1", "http://localhost:9999");
				
				// step 19 : externally-defined reader name => skip
				
				// step 20
				try {
					LRSpec lrspec_23 = DeserializerUtil.deserializeLRSpec(ALELRConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_L5_23.xml"));
					alelr.define("LR4", lrspec_23);
					successFlag = true;
				} catch(DuplicateNameException e) {
					successFlag = false;
				} finally {
					if(successFlag) Assert.fail();
				}
				
				// step 21
				try {
					List<String> readerList = new ArrayList<String>();
					readerList.add("limg00n_emulator");
					alelr.setReaders("LR4", readerList);
					successFlag = true;
				} catch(NonCompositeReaderException e) {
					successFlag = false;
				} finally {
					if(successFlag) Assert.fail();
				}
				
				// step 22
				try {
					List<String> readerList = new ArrayList<String>();
					readerList.add("LR1");
					alelr.setReaders("LR1", readerList);
					successFlag = true;
				} catch(ReaderLoopException e) {
					successFlag = false;
				} finally {
					if(successFlag) Assert.fail();
				}
				
		// step 24
				// step 16
				successFlag = true;
				try {
					alelr.removeReaders("LR3", new ArrayList<String>());
					successFlag = true;
				} catch(NoSuchNameException e) {
					successFlag = false;
				} finally {
					if(successFlag) Assert.fail();
				}
				
				// step 18
				ale.subscribe("EC1", "http://localhost:9999");
				synchronized(this) {
					this.wait(2000);	
				}
				
				successFlag = true;
				try {
					List<String> readerList = new ArrayList<String>();
					readerList.add("LR1");
					alelr.removeReaders("LR2", readerList);
					successFlag = true;
				} catch(InUseException e) {
					successFlag = false;
				} finally {
					if(successFlag) Assert.fail();
				}
				ale.unsubscribe("EC1", "http://localhost:9999");
				
				// step 19 : externally-defined reader name => skip
				
				// step 20
				try {
					LRSpec lrspec_24 = DeserializerUtil.deserializeLRSpec(ALELRConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_L5_24.xml"));
					alelr.define("LR4", lrspec_24);
					successFlag = true;
				} catch(DuplicateNameException e) {
					successFlag = false;
				} finally {
					if(successFlag) Assert.fail();
				}
				
				// step 21
				try {
					List<String> readerList = new ArrayList<String>();
					readerList.add("limg00n_emulator");
					alelr.removeReaders("LR4", readerList);
					successFlag = true;
				} catch(NonCompositeReaderException e) {
					successFlag = false;
				} finally {
					if(successFlag) Assert.fail();
				}
				
		// step 25
		try {
			List<LRProperty> properties = new ArrayList<LRProperty>();
			LRProperty e = new LRProperty();
			e.setName("Description");
			e.setValue("LLRP Reader");
			properties.add(e);
			alelr.setProperties("LR3", properties);
			successFlag = true;
		} catch(NoSuchNameException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 26
		try {
			
			List<LRProperty> properties = new ArrayList<LRProperty>();
			LRProperty e = new LRProperty();
			e.setName("isComposite");
			e.setValue("I don't know");
			properties.add(e);
			alelr.setProperties("LR1", properties);
			successFlag = true;
		} catch(ValidationException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 27
				// step 8
				ale.subscribe("EC1", "http://localhost:9999");
				
				// step 9
				synchronized(this) {
					this.wait(2000);	
				}
				
				
				// step 10
				successFlag = true;
				try {
					List<LRProperty> properties = new ArrayList<LRProperty>();
					LRProperty e = new LRProperty();
					e.setName("Description");
					e.setValue("LLRP Reader");
					properties.add(e);
					alelr.setProperties("LR2", properties);
					successFlag = true;
				} catch(InUseException e) {
					successFlag = false;
				} finally {
					if(successFlag) Assert.fail();
				}
				
				// step 11
				ale.unsubscribe("EC1", "http://localhost:9999");
				
				
		// step 28 : externally-defined reader name => skip
		
		// step 29
		System.out.println(alelr.getLogicalReaderNames());
		successFlag = true;
		try {
			alelr.undefine("LR2");
			successFlag = true;
		} catch(InUseException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 30
		ale.undefine("EC1");
		
		// step 31
		alelr.undefine("LR1");
		alelr.undefine("LR2");
		alelr.undefine("LR4");
		
		
	}
	
	@Test
	public void test_L6() throws Exception {
		
		// step 1
		LRSpec lrspec = DeserializerUtil.deserializeLRSpec(ALELRConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_L6.xml"));
		alelr.define("L1", lrspec);
		
		// step 2
		assertEquals(lrspec, alelr.getLRSpec("L1"));
		
		// step 3
		ECSpec ecspec = (ECSpec) DeserializerUtil.deserializeECSpec(getClass().getResourceAsStream("/ecspecs/ECSpec_L6.xml"));
		ale.define("eventcycle1", ecspec);
		
		// step 4 : TODO repeat step 3 for a CCSpec
		CCSpec ccspec = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_L6.xml"));;
		alecc.define("commandcycle1", ccspec);
	}
}
