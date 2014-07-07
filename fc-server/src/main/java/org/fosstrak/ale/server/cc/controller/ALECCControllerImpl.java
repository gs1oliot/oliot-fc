/**
 * Copyright (C) 2014 KAIST
 * @author Wondeuk Yoon <wdyoon@resl.kaist.ac.kr>
 * 
 */

package org.fosstrak.ale.server.cc.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.fosstrak.ale.server.cc.ALECC;
import org.fosstrak.ale.server.cc.ReportsGenerator;
import org.fosstrak.ale.server.readers.LogicalReader;
import org.fosstrak.ale.server.readers.LogicalReaderManager;
import org.fosstrak.alecc.server.controller.ALECCController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * this class is a webservice wich is control ALE. Can stop/start/... ECSpec.
 * @author benoit.plomion@orange.com
 */
public class ALECCControllerImpl implements ALECCController {

	/**	logger. */
	private static final Logger LOG = Logger.getLogger(ALECCControllerImpl.class.getName());
		
	//FIXME => modifier tous les NoSuchNameGPIOException avec les bons NoSuchNameException
	
	@Autowired
	private ALECC alecc;
	
	@Autowired
	private LogicalReaderManager logicalReaderManager;
	
	@Override
	public boolean ccSpecIsStarted(String specName) throws org.fosstrak.ale.exception.NoSuchNameException {			
		
		LOG.info("check if " + specName + " CCSpec is started");
		
		if (!alecc.getReportGenerators().containsKey(specName)) {
			throw new org.fosstrak.ale.exception.NoSuchNameException();			
		}
		
		boolean result = false;
		
		ReportsGenerator reportsGenerator = alecc.getReportGenerators().get(specName);
		
		if (reportsGenerator != null) {
		
			if (reportsGenerator.isStateRequested()){
				result = true;
			}
			
		}
		
		LOG.debug("CCSpec" + specName + " is started = " + result);
		
		return result;
		
	}
	
	@Override
	public List<String> getAllCCSpecNameStarted() {

		LOG.info("get all CCSpec started");
		
		List<String> result = new ArrayList<String>();
		
		Map<String, ReportsGenerator> reportGeneratorList = alecc.getReportGenerators();		
		Set<String> reportGeneratorNameList = reportGeneratorList.keySet();
		
		for (String reportGeneratorName : reportGeneratorNameList) {
			
			if (reportGeneratorList.get(reportGeneratorName).isStateRequested()){
				result.add(reportGeneratorName);
			}
			
		}
		
		LOG.debug("list of all CCSpec started = " + result);
		
		return result;
		
	}

	@Override
	public void startCCSpec(String specName) throws org.fosstrak.ale.exception.NoSuchNameException {
		
		LOG.info("start CCSpec " + specName);
		
		if (!alecc.getReportGenerators().containsKey(specName)) {
			throw new org.fosstrak.ale.exception.NoSuchNameException();			
		}
		
		ReportsGenerator reportsGenerator = alecc.getReportGenerators().get(specName);		
		
		if (reportsGenerator != null) {			
			reportsGenerator.setStateRequested();			
		}	
		
	}

	@Override
	public void stopCCSpec(String specName) throws org.fosstrak.ale.exception.NoSuchNameException {
		
		LOG.info("stop CCSpec " + specName);
				
		if (!alecc.getReportGenerators().containsKey(specName)) {
			throw new org.fosstrak.ale.exception.NoSuchNameException();			
		}
		
		ReportsGenerator reportsGenerator = alecc.getReportGenerators().get(specName);	
		
		if (reportsGenerator != null) {			
			reportsGenerator.setStateUnRequested();			
		}		
		
	}
	
	@Override
	public void stopAllCCSpec() {
		
		LOG.info("stop all CCSpec");
		
		List<String> ccSpecsName = getAllCCSpecNameStarted();
		
		for (String ccSpecName : ccSpecsName) {
				
			try {
				LOG.info("stop ECSepcs " + ccSpecName);
				stopCCSpec(ccSpecName);
			} catch (org.fosstrak.ale.exception.NoSuchNameException e) {
				LOG.error("error to stop ecspec at startup", e);
			}
		
		}
		
	}
	
	@Override
	public void stopAllCCSpec4LogicalReader(String logicalReaderName) throws org.fosstrak.ale.exception.NoSuchNameException {
		
		LOG.info("stop all CCSpec for the logical reader " + logicalReaderName);
		
		boolean logicalReaderFind = false;
		
		Map<String, ReportsGenerator> reportGeneratorList = alecc.getReportGenerators();		
		Set<String> reportGeneratorNameList = reportGeneratorList.keySet();
		
		for (String reportGeneratorName : reportGeneratorNameList) {
			
			ReportsGenerator reportsGenerator = reportGeneratorList.get(reportGeneratorName);
			
			List<String> logicalReaderList = reportsGenerator.getCCSpec().getLogicalReaders().getLogicalReader();
			
			for (String logicalReaderName_for : logicalReaderList) {
				
				if (logicalReaderName.equalsIgnoreCase(logicalReaderName_for)) {
					
					reportsGenerator.setStateUnRequested();
					logicalReaderFind = true;
					
				}
				
			}
			
		}
		
		if (!logicalReaderFind) {
			throw new org.fosstrak.ale.exception.NoSuchNameException();
		}
		
	}
	@Override
	public void stopAllCCSpec4LogicalReaderByCCSpecName(String specName) throws org.fosstrak.ale.exception.NoSuchNameException {
		
		LOG.info("stop all CCSpec for the logical reader by spec name " + specName);
		
		if (!alecc.getReportGenerators().containsKey(specName)) {
			throw new org.fosstrak.ale.exception.NoSuchNameException();			
		}
		
		List<String> logicalReaderList = alecc.getReportGenerators().get(specName).getCCSpec().getLogicalReaders().getLogicalReader();
		
		if (logicalReaderList.size() > 0) {	
			
			String logicalReaderName = logicalReaderList.get(0);	
			
			Map<String, ReportsGenerator> reportGeneratorList = alecc.getReportGenerators();
			Set<String> reportGeneratorNameList = reportGeneratorList.keySet();
			
			for (String reportGeneratorName : reportGeneratorNameList) {
				
				ReportsGenerator reportsGenerator = reportGeneratorList.get(reportGeneratorName);
				
				List<String> logicalReaderList_for = reportsGenerator.getCCSpec().getLogicalReaders().getLogicalReader();
				
				for (String logicalReaderName_for : logicalReaderList_for) {
					
					if (logicalReaderName.equalsIgnoreCase(logicalReaderName_for)) {
						
						reportsGenerator.setStateUnRequested();
						
					}
				}
			}
		}
	}
	
	@Override
	public String[] getLogicalReaderNames(boolean isComposite) {
		
		ArrayList<String> result = new ArrayList<String>();		
		Collection<LogicalReader> logicalReaders = logicalReaderManager.getLogicalReaders();
		
		for (LogicalReader logicalReader : logicalReaders) {			
			if (isComposite == logicalReader.getLRSpec().isIsComposite()) {				
				result.add(logicalReader.getName());			
			}			
		}
		
		return result.toArray(new String[0]);
		
	}
	
}
