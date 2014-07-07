/*
 * Copyright (C) 2007 ETH Zurich
 *
 * This file is part of Fosstrak (www.fosstrak.org).
 *
 * Fosstrak is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 *
 * Fosstrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Fosstrak; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */
package org.fosstrak.ale.server.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.fosstrak.ale.exception.CCSpecValidationException;
import org.fosstrak.ale.exception.ImplementationException;
import org.fosstrak.ale.server.Pattern;
import org.fosstrak.ale.server.PatternUsage;
import org.fosstrak.ale.server.readers.LogicalReaderManager;
import org.fosstrak.ale.xsd.ale.epcglobal.CCBoundarySpec;
import org.fosstrak.ale.xsd.ale.epcglobal.CCBoundarySpec.StartTriggerList;
import org.fosstrak.ale.xsd.ale.epcglobal.CCBoundarySpec.StopTriggerList;
import org.fosstrak.ale.xsd.ale.epcglobal.CCCmdSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.CCCmdSpec.OpSpecs;
import org.fosstrak.ale.xsd.ale.epcglobal.CCFilterSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.CCOpSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECBoundarySpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFilterSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECGroupSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReportOutputSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReportSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.CCSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.CCSpec.LogicalReaders;
import org.fosstrak.ale.xsd.ale.epcglobal.CCSpec.CmdSpecs;
import org.fosstrak.ale.xsd.ale.epcglobal.ECSpec.ReportSpecs;
import org.fosstrak.ale.xsd.ale.epcglobal.ECTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * helper utility validating an CCSpec.
 * @author sbw
 *
 */
@Service("ccSpecValidator")
public class CCSpecValidator {

	/**
	 * private handle onto the logical reader manager.
	 */
	private LogicalReaderManager logicalReaderManager;

	/** logger */
	private static final Logger LOG = Logger.getLogger(CCSpecValidator.class);

	/**
	 *  constructor for class.
	 */
	public CCSpecValidator() {		
	}	

	/**
	 * inject the autowired bean logical reader manager into the static utility class.
	 * @param lrm
	 */
	@Autowired
	public void setLogicalReaderManager(LogicalReaderManager lrm) {
		LOG.debug("setting logical reader manager" + lrm.getClass().getCanonicalName());
		logicalReaderManager = lrm;
	}

	/**
	 * This method validates an ec specification under criterias of chapter 
	 * 8.2.11 of ALE specification version 1.0.
	 * @param spec to validate
	 * @throws CCSpecValidationException if the specification is invalid
	 * @throws ImplementationException if an implementation exception occurs
	 */
	public void validateSpec(CCSpec spec) throws CCSpecValidationException, ImplementationException {

		if (logicalReaderManager == null) {
			throw new IllegalStateException("Logical Reader Manager is null - aborting");
		}

		// check if the logical readers are known to the implementation
		checkReadersAvailable(spec.getLogicalReaders(), logicalReaderManager);

		checkBoundarySpec(spec.getBoundarySpec());
		checkCmdSpecs(spec.getCmdSpecs());
	}

	/**
	 * verifies that the spec contains some readers and that those readers are available in the ALE.
	 * @param logicalReaders the logical reader definition from the CCSpec.
	 * @param readerManager handle to the logical reader manager.
	 * @return true if OK, throws exception otherwise.
	 * @throws CCSpecValidationException if no reader specified or the specified readers do not exist in the ALE.
	 */
	public boolean checkReadersAvailable(LogicalReaders logicalReaders, LogicalReaderManager readerManager) throws CCSpecValidationException {
		if ((null == logicalReaders) || (logicalReaders.getLogicalReader().size() == 0)) {
			throw logAndCreateCCSpecValidationException("CCSpec does not specify at least one reader"); 
		}
		for (String logicalReaderName : logicalReaders.getLogicalReader()) {
			if (!readerManager.contains(logicalReaderName)) {
				throw logAndCreateCCSpecValidationException("LogicalReader '" + logicalReaderName + "' is unknown.");
			}
		}
		return true;
	}

	/**
	 * verifies the boundary spec of an CCSpec.
	 * @param boundarySpec the boundary spec to verify.
	 * @throws CCSpecValidationException if the specification does not meet the specification.
	 * @return true if OK, throws exception otherwise.
	 */
	public boolean checkBoundarySpec(CCBoundarySpec boundarySpec) throws CCSpecValidationException {

		// boundaries parameter of CCSpec is null or omitted
		if (boundarySpec == null) {
			throw logAndCreateCCSpecValidationException("The boundaries parameter of CCSpec is null.");
		}

		// start and stop tiggers
		checkStartTrigger(boundarySpec.getStartTriggerList());
		checkStopTrigger(boundarySpec.getStopTriggerList());

		// check if duration, stableSetInterval or repeatPeriod is negative
		checkTimeNotNegative(boundarySpec.getDuration(), "The duration field of CCBoundarySpec is negative.");
		checkTimeNotNegative(boundarySpec.getNoNewTagsInterval(), "The NoNewTagsInterval field of CCBoundarySpec is negative.");
		checkTimeNotNegative(boundarySpec.getRepeatPeriod(), "The repeatPeriod field of CCBoundarySpec is negative.");
		if (boundarySpec.getTagsProcessedCount() != null)
			checkCountNotNegative(boundarySpec.getTagsProcessedCount(), "The tagsProcessedCount field of CCBoundarySpec is negative.");
		
		
		// check if start trigger is non-empty and repeatPeriod is non-zero
		checkStartTriggerConstraintsOnRepeatPeriod(boundarySpec);

		// check if a stopping condition is specified
		checkBoundarySpecStoppingCondition(boundarySpec);

		return true;
	}

	/**
	 * check that the provided time value is not negative.
	 * @param duration the time value to check.
	 * @param string a message string to show in the exception.
	 * @return true if OK, throws Exception otherwise.
	 * @throws CCSpecValidationException if the time value is negative.
	 */
	public boolean checkTimeNotNegative(ECTime duration, String string) throws CCSpecValidationException {		
		if (duration != null) {
			if (duration.getValue() < 0) {
				throw logAndCreateCCSpecValidationException("The duration field of CCBoundarySpec is negative.");
			}
		}
		return true;
	}
	
	/**
	 * check that the provided count value is not negative.
	 * @param cout value to check.
	 * @param string a message string to show in the exception.
	 * @return true if OK, throws Exception otherwise.
	 * @throws CCSpecValidationException if the time value is negative.
	 */
	public boolean checkCountNotNegative(int count, String string) throws CCSpecValidationException {		
		if (count < 0) {
			throw logAndCreateCCSpecValidationException("The tagsProcessedCount field of CCBoundarySpec is negative.");
		}
		return true;
	}

	/**
	 * if a start trigger is specified, then the repeat period must be 0.
	 * @param boundarySpec the boundary spec to test.
	 * @return true if OK, throws exception otherwise.
	 * @throws CCSpecValidationException if a start trigger is specified, then the repeat period must be 0. if not, throw an exception.
	 */
	public boolean checkStartTriggerConstraintsOnRepeatPeriod(CCBoundarySpec boundarySpec) throws CCSpecValidationException {
		if ((boundarySpec.getStartTriggerList() != null) && (boundarySpec.getRepeatPeriod().getValue() != 0)) {
			throw logAndCreateCCSpecValidationException("The startTrigger field of CCBoundarySpec is non-empty and the repeatPeriod field of CCBoundarySpec is non-zero.");
		}
		return true;
	}

	/**
	 * check the stopping condition of the CC boundary spec:<br/>
	 * if there is no stop trigger or no duration value or no stableSetInterval, throw an exception.
	 * @param boundarySpec the boundary spec to test.
	 * @return true if OK, throws exception otherwise.
	 * @throws CCSpecValidationException if there is no stop trigger or no duration value or no stableSetInterval, throw an exception.
	 */
	public boolean checkBoundarySpecStoppingCondition(CCBoundarySpec boundarySpec) throws CCSpecValidationException {
		if ((boundarySpec.getStopTriggerList() == null) && (boundarySpec.getDuration() == null) && (boundarySpec.getNoNewTagsInterval() == null)) {
			throw logAndCreateCCSpecValidationException("No stopping condition is specified in CCBoundarySpec.");
		}
		return true;		
	}

	/**
	 * checks the cmd specs.
	 * @param reportSpecs the cmd specs to verify.
	 * @throws CCSpecValidationException when the specifications do not meet the requirements.
	 * @return true if the specification is OK, throws exception otherwise.
	 */
	public boolean checkCmdSpecs(CmdSpecs cmdSpecs) throws CCSpecValidationException {
		// check if there is a CCReportSpec instance
		if ((cmdSpecs == null) || (cmdSpecs.getCmdSpec().isEmpty())) {
			throw logAndCreateCCSpecValidationException("List of CCCmdSpec is empty or null.");
		}
		final List<CCCmdSpec> cmdSpecList = cmdSpecs.getCmdSpec();

		// check that no two CCReportSpec instances have identical names
		checkCmdSpecNoDuplicateReportSpecNames(cmdSpecList);

		// check filters
		for (CCCmdSpec cmdSpec : cmdSpecList) {
			checkFilterSpec(cmdSpec.getFilterSpec());
		}

		// check grouping patterns
		for (CCCmdSpec cmdSpec : cmdSpecList) {
			checkOpSpec(cmdSpec.getOpSpecs());
		}

		return true;
	}

	/**
	 * verify that no two cmd specs have the same name.
	 * @param reportSpecList the list of cmd specs to check.
	 * @return a list containing all the names of the different cmd specs.
	 * @throws CCSpecValidationException when there are two cmd specs with the same name.
	 */
	public Set<String> checkCmdSpecNoDuplicateReportSpecNames(List<CCCmdSpec> cmdSpecList) throws CCSpecValidationException {
		Set<String> cmdSpecNames = new HashSet<String>();
		for (CCCmdSpec cmdSpec : cmdSpecList) {
			LOG.debug("Verify report spec name not specified twice: " + cmdSpec.getName());
			if (cmdSpecNames.contains(cmdSpec.getName())) {
				throw logAndCreateCCSpecValidationException("Two CmdSpecs instances have identical names '" + cmdSpec.getName() + "'.");
			} else {
				cmdSpecNames.add(cmdSpec.getName());
			}
		}
		return cmdSpecNames;
	}

	/**
	 * verify the report output specification.
	 * @param outputSpec the output specification.
	 * @return true if the specification is OK, otherwise throws exception.
	 * @throws CCSpecValidationException violates the specification.
	 */
	public boolean checkReportOutputSpec(String reportName, ECReportOutputSpec outputSpec) throws CCSpecValidationException {
		if (null == outputSpec) {
			throw logAndCreateCCSpecValidationException("there is no output spec for report spec: " + reportName);
		}
		if (!outputSpec.isIncludeEPC() && !outputSpec.isIncludeTag() && !outputSpec.isIncludeRawHex() && !outputSpec.isIncludeRawDecimal() && !outputSpec.isIncludeCount()) {
			throw logAndCreateCCSpecValidationException("The ECReportOutputSpec of ReportSpec '" + reportName + "' has no output type specified.");
		}
		return true;
	}

	/**
	 * check the op spec patterns do not have intersecting ops -> all op patterns have to be disjoint:<br/>
	 * <ul>
	 * 	<li>the same pattern is not allowed to occur twice</li>
	 * 	<li>two different pattern with intersecting selectors are not allowed</li>
	 * 	<li>no pattern at all is allowed</li>
	 * </ul>
	 * @param opSpec the op spec to tested.
	 * @throws CCSpecValidationException upon violation.
	 * @return true if filter op spec is valid. exception otherwise.
	 */
	public boolean checkOpSpec(OpSpecs opSpec) throws CCSpecValidationException {
		if (opSpec != null) {
			if (opSpec.getOpSpec() != null) {
				if (!opSpec.getOpSpec().isEmpty()) {
					List<CCOpSpec> opspecs = opSpec.getOpSpec();
					for (CCOpSpec opspec : opspecs)
					{
						String temp = opspec.getOpType();
						if (temp.equalsIgnoreCase("READ")||temp.equalsIgnoreCase("CHECK")||temp.equalsIgnoreCase("INITIALIZE")||temp.equalsIgnoreCase("ADD")||temp.equalsIgnoreCase("WRITE")||temp.equalsIgnoreCase("DELETE")||temp.equalsIgnoreCase("PASSWORD")||temp.equalsIgnoreCase("KILL")||temp.equalsIgnoreCase("LOCK")) 
						{
							if (temp.equalsIgnoreCase("READ")||temp.equalsIgnoreCase("CHECK")||temp.equalsIgnoreCase("INITIALIZE")||temp.equalsIgnoreCase("ADD")||temp.equalsIgnoreCase("WRITE")||temp.equalsIgnoreCase("DELETE")||temp.equalsIgnoreCase("LOCK")) 
							{
								if (opspec.getFieldspec() != null) {
									
								}
								else
									throw logAndCreateCCSpecValidationException("No FieldSpec.");
							}
							if (temp.equalsIgnoreCase("PASSWORD")||temp.equalsIgnoreCase("KILL")) {
								if (opspec.getFieldspec() != null) {
									throw logAndCreateCCSpecValidationException("Exist FieldSpec.");
								}
							}
							if (temp.equalsIgnoreCase("CHECK")||temp.equalsIgnoreCase("INITIALIZE")||temp.equalsIgnoreCase("ADD")||temp.equalsIgnoreCase("WRITE")||temp.equalsIgnoreCase("PASSWORD")||temp.equalsIgnoreCase("KILL")||temp.equalsIgnoreCase("LOCK")) 
							{
								if (opspec.getDataSpec() != null) {
								
								}
								else
									throw logAndCreateCCSpecValidationException("No DataSpec.");
							}
							if (temp.equalsIgnoreCase("READ")||temp.equalsIgnoreCase("DELETE")) {
								if (opspec.getDataSpec() != null) {
									throw logAndCreateCCSpecValidationException("Exist DataSpec.");
								}
							}
								
						}
						else
							throw logAndCreateCCSpecValidationException("Incorrect OpType.");
					}
				}	
			}
		}
		return true;
	}

	/**
	 * check the filter spec. if the filter spec is null, it is ignored.
	 * @param filterSpec the filter spec to verify.
	 * @throws CCSpecValidationException upon violation of the filter pattern.
	 * @return true if filter spec is valid. exception otherwise.
	 */
	public boolean checkFilterSpec(CCFilterSpec filterSpec) throws CCSpecValidationException {			
		if (filterSpec != null) {
			// check include patterns
			if (filterSpec.getFilterList() != null)
			{
				if(filterSpec.getFilterList().getFilter() != null) 
				{
					if(filterSpec.getFilterList().getFilter().get(0) != null)
					{
						if(filterSpec.getFilterList().getFilter().get(0).getPatList() != null)
						{
							if(!filterSpec.getFilterList().getFilter().get(0).getPatList().getPat().isEmpty())
							{

							}
							else
							{
								throw logAndCreateCCSpecValidationException("No Pat.");
							}
						}
						else
							throw logAndCreateCCSpecValidationException("No PatList.");
					}
				} 
			} 
		}
		return true; 
	}

	/**
	 * test if two pattern are disjoint.
	 * @param pattern1 the first and reference pattern.
	 * @param pattern2 the second pattern.
	 * @return true if pattern not disjoint, false otherwise.
	 */
	/*public boolean patternDisjoint(String pattern1, String pattern2) throws CCSpecValidationException {
		Pattern pattern = new Pattern(pattern1, PatternUsage.GROUP);
		return pattern.isDisjoint(pattern2);
	}*/

	/**
	 * log the given string and then create from the string an CCSpecValidationException.
	 * @param string the log and exception string.
	 * @return the CCSpecValidationException created from the input string.
	 */
	private CCSpecValidationException logAndCreateCCSpecValidationException(String string) {
		LOG.debug(string);
		return new CCSpecValidationException(string);
	}

	/**
	 * This method checks if the trigger is valid or not.
	 * 
	 * @param stopTriggerList to check
	 * @throws CCSpecValidationException if the trigger is invalid.
	 */
	private void checkStopTrigger(StopTriggerList stopTriggerList) throws CCSpecValidationException {		
		// TODO: implement checkTrigger
		LOG.debug("CHECK TRIGGER not implemented");
	}

	/**
	 * This method checks if the trigger is valid or not.
	 * 
	 * @param startTriggerList to check
	 * @throws CCSpecValidationException if the trigger is invalid.
	 */
	private void checkStartTrigger(StartTriggerList startTriggerList) throws CCSpecValidationException {		
		// TODO: implement checkTrigger
		LOG.debug("CHECK TRIGGER not implemented");
	}
}
