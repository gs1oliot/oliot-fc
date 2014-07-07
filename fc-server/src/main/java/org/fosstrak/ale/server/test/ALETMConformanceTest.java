/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */

package org.fosstrak.ale.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.log4j.Logger;
import org.fosstrak.ale.exception.ECSpecValidationException;
import org.fosstrak.ale.exception.ImmutableReaderException;
import org.fosstrak.ale.exception.ImplementationException;
import org.fosstrak.ale.exception.InUseException;
import org.fosstrak.ale.exception.NoSuchNameException;
import org.fosstrak.ale.exception.SecurityException;
import org.fosstrak.ale.server.ALE;
import org.fosstrak.ale.server.readers.LogicalReaderManager;
import org.fosstrak.ale.server.tm.ALETM;
import org.fosstrak.ale.util.DeserializerUtil;
import org.fosstrak.ale.util.SerializerUtil;
import org.fosstrak.ale.wsdl.aletm.epcglobal.DuplicateNameExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.ImplementationExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.NoSuchNameExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.TMSpecValidationExceptionResponse;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReports;
import org.fosstrak.ale.xsd.ale.epcglobal.ECSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.LRSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.TMFixedFieldListSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.TMSpec;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ALETMConformanceTest {
	/** logger. */
	private static final Logger LOG = Logger.getLogger(ALETMConformanceTest.class);
	
	
	static ALETM aletm;
	
	static ALE ale;
	
	static LogicalReaderManager lrm;
	
	@BeforeClass
	public static void beforeClass() throws Exception {

		LOG.info("Initializing Spring context.");
        
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext.xml");
        
        LOG.info("Spring context initialized.");
        
        aletm = (ALETM) applicationContext.getBean("aletm");
        ale = (ALE) applicationContext.getBean("ale");
        lrm = (LogicalReaderManager) applicationContext.getBean("logicalReaderManager");
        
        LRSpec lrspec = DeserializerUtil.deserializeLRSpec(ALETMConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_T4.xml"));
        
        System.out.println("Please connect reader... (timeout: 5000ms)");
        Thread.sleep(5000);
        
        lrm.define("limg00n_emulator", lrspec);
        
        System.out.println("Waiting for reader initialization...");
        Thread.sleep(5000);
	}
	
	@Before
	public void beforeEachTest() {
		for(String name : aletm.getTMSpecNames()) {
			try {
				aletm.undefineTMSpec(name);
			} catch (NoSuchNameExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	@Test
	public void test_T1() {
		assertEquals("1.1", aletm.getStandardVersion());
		assertEquals("", aletm.getVendorVersion());
	}
	
	@Test
	public void test_T2() throws TMSpecValidationExceptionResponse, ImplementationExceptionResponse, Exception {
		TMSpec spec = DeserializerUtil.deserializeTMSpec(getClass().getResourceAsStream("/tmspecs/TMFixedFieldListSpec.xml"));
		TMSpec spec2 = DeserializerUtil.deserializeTMSpec(getClass().getResourceAsStream("/tmspecs/TMFixedFieldListSpec2.xml"));
		
		// step 1
		aletm.defineTMSpec("tempHumid", spec);
		
		// step 2
		assertEquals("tempHumid", aletm.getTMSpecNames().get(0));
		
		// step 3
		assertEquals(spec, aletm.getTMSpec("tempHumid"));
		
		// step 4
		aletm.defineTMSpec("manufactureCirculationDate", spec2);
		
		// step 5
		List<String> specNames = aletm.getTMSpecNames();
		assertTrue(specNames.size() == 2);
		for(String specName : specNames) {
			assertTrue(specName.equals("tempHumid") || specName.equals("manufactureCirculationDate"));
		}

		// step 6: repeat 1 to 5 with a valid TMVariableFieldListSpec => skip
		
		// step 7
		aletm.undefineTMSpec("tempHumid");
		
		// step 8
		List<String> specNamesAfterUndefine = aletm.getTMSpecNames();
		assertTrue(specNamesAfterUndefine.size() == 1);
		for(String specName : specNamesAfterUndefine) {
			assertTrue(specName.equals("manufactureCirculationDate"));
		}
		
		// step 9
		aletm.undefineTMSpec("manufactureCirculationDate");
		
		// step 10: returned list should be empty
		assertTrue(aletm.getTMSpecNames().size() == 0);
		
		// step 11: repeat 7-10 for TMVariableFieldListSpec TMSpec => skip
	}
	@Test
	public void test_T3() throws Exception {
		TMFixedFieldListSpec validSpec = (TMFixedFieldListSpec) DeserializerUtil.deserializeTMSpec(getClass().getResourceAsStream("/tmspecs/TMFixedFieldListSpec.xml"));
		TMFixedFieldListSpec validSpec2 = (TMFixedFieldListSpec) DeserializerUtil.deserializeTMSpec(getClass().getResourceAsStream("/tmspecs/TMFixedFieldListSpec2.xml"));
		
		
		// step 1
		aletm.defineTMSpec("TM1", validSpec);
		
		// step 2
		boolean successFlag = false;
		try {
			aletm.defineTMSpec("TM1", validSpec2);
			successFlag = true;
		} catch(DuplicateNameExceptionResponse e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 3
		TMFixedFieldListSpec invalidSpec = (TMFixedFieldListSpec) DeserializerUtil.deserializeTMSpec(getClass().getResourceAsStream("/tmspecs/TMFixedFieldListSpec2.xml"));
		invalidSpec.getFixedFields().getFixedField().get(0).setBank(-1);
		invalidSpec.getFixedFields().getFixedField().get(0).setLength(-3);
		invalidSpec.getFixedFields().getFixedField().get(0).setOffset(-1);
		try {
			aletm.defineTMSpec("TM2", invalidSpec);
			successFlag = true;
		} catch(TMSpecValidationExceptionResponse e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 4
		try {
			aletm.undefineTMSpec("TM2");
			successFlag = true;
		} catch(NoSuchNameExceptionResponse e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 5
		try {
			aletm.getTMSpec("TM2");
			successFlag = true;
		} catch(NoSuchNameExceptionResponse e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 6
		aletm.undefineTMSpec("TM1");
		
		// step 7: defineTMSpec with TMVariableFieldListSpec => skip
		
		// step 8
		TMFixedFieldListSpec spec_symbol = (TMFixedFieldListSpec) DeserializerUtil.deserializeTMSpec(getClass().getResourceAsStream("/tmspecs/TMFixedFieldListSpec_T3.xml"));
		aletm.defineTMSpec("TM1", spec_symbol);
		
		// step 9
		try {
			aletm.defineTMSpec("TM2", spec_symbol);
			successFlag = true;
		} catch(TMSpecValidationExceptionResponse e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 10
		TMFixedFieldListSpec spec_builtin = (TMFixedFieldListSpec) DeserializerUtil.deserializeTMSpec(getClass().getResourceAsStream("/tmspecs/TMFixedFieldListSpec_T3.xml"));
		spec_builtin.getFixedFields().getFixedField().get(0).setFieldname("epc");
		try {
			aletm.defineTMSpec("TM2", spec_builtin);
			successFlag = true;
		} catch(TMSpecValidationExceptionResponse e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		spec_builtin.getFixedFields().getFixedField().get(0).setFieldname("killPwd");
		try {
			aletm.defineTMSpec("TM2", spec_builtin);
			successFlag = true;
		} catch(TMSpecValidationExceptionResponse e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		spec_builtin.getFixedFields().getFixedField().get(0).setFieldname("accessPwd");
		try {
			aletm.defineTMSpec("TM2", spec_builtin);
			successFlag = true;
		} catch(TMSpecValidationExceptionResponse e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		spec_builtin.getFixedFields().getFixedField().get(0).setFieldname("epcBank");
		try {
			aletm.defineTMSpec("TM2", spec_builtin);
			successFlag = true;
		} catch(TMSpecValidationExceptionResponse e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		spec_builtin.getFixedFields().getFixedField().get(0).setFieldname("tidBank");
		try {
			aletm.defineTMSpec("TM2", spec_builtin);
			successFlag = true;
		} catch(TMSpecValidationExceptionResponse e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		spec_builtin.getFixedFields().getFixedField().get(0).setFieldname("userBank");
		try {
			aletm.defineTMSpec("TM2", spec_builtin);
			successFlag = true;
		} catch(TMSpecValidationExceptionResponse e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		spec_builtin.getFixedFields().getFixedField().get(0).setFieldname("afi");
		try {
			aletm.defineTMSpec("TM2", spec_builtin);
			successFlag = true;
		} catch(TMSpecValidationExceptionResponse e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		spec_builtin.getFixedFields().getFixedField().get(0).setFieldname("nsi");
		try {
			aletm.defineTMSpec("TM2", spec_builtin);
			successFlag = true;
		} catch(TMSpecValidationExceptionResponse e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 11
		TMFixedFieldListSpec spec_wrongat = (TMFixedFieldListSpec) DeserializerUtil.deserializeTMSpec(getClass().getResourceAsStream("/tmspecs/TMFixedFieldListSpec_T3.xml"));
		spec_wrongat.getFixedFields().getFixedField().get(0).setFieldname("@symbol");
		try {
			aletm.defineTMSpec("TM2", spec_wrongat);
			successFlag = true;
		} catch(TMSpecValidationExceptionResponse e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		// step 12: repeat step 1~11 with TMVariableFieldListSpec => skip
	}
	
	@Test
	public void test_T4() throws Exception {
		TMFixedFieldListSpec tmspec = (TMFixedFieldListSpec) DeserializerUtil.deserializeTMSpec(getClass().getResourceAsStream("/tmspecs/TMFixedFieldListSpec_T4.xml"));
		ECSpec ecspec = (ECSpec) DeserializerUtil.deserializeECSpec(getClass().getResourceAsStream("/ecspecs/ECSpec_T4.xml"));
		
		//System.out.println(ecspec.getReportSpecs().getReportSpec().get(0).getFilterSpec().getExtension().getFilterList().getFilter().get(0).getFieldspec().getFieldname());
		
		// step 1
		aletm.defineTMSpec("TM1", tmspec);
		
		// step 2
		Assert.assertEquals("TM1", aletm.getTMSpecNames().get(0));
		
		// step 3
		ale.define("EC1", ecspec);
		
		// step 4
		ECReports ecreports = ale.poll("EC1");
		
		// step 5
		ECReports expectedEcreports = DeserializerUtil.deserializeECReports(getClass().getResourceAsStream("/ecreports/ECReports_T4.xml"));
		StringWriter expectedSw = new StringWriter();
		SerializerUtil.serializeECReportsPretty(expectedEcreports, expectedSw);
		String outExpectedString = expectedSw.toString();
		
		StringWriter sw = new StringWriter();
		SerializerUtil.serializeECReportsPretty(ecreports, sw);
		String outString = sw.toString();
		
		//SerializerUtil.serializeECReportsPretty(ecreports, new OutputStreamWriter(System.out));
		
		//Assert.assertEquals(expectedEcreports, ecreports);
		Assert.assertEquals(outExpectedString.substring(outExpectedString.indexOf("<reports>")), outString.substring(outString.indexOf("<reports>")));
		
		// step 6
		ale.undefine("EC1");
		
		// step 7
		aletm.undefineTMSpec("TM1");
		
		// step 8
		boolean successFlag = true;
		try {
			ale.define("EC1", ecspec);
			successFlag = true;
		} catch(ECSpecValidationException e) {
			successFlag = false;
		} finally {
			if(successFlag) Assert.fail();
		}
		
		
	}
	
}
