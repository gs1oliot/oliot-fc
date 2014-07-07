/*
 * Copyright (C) 2014 KAIST
 * @author Wondeuk Yoon <wdyoon@resl.kaist.ac.kr> 
 * 
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

package org.fosstrak.ale.server.cc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.fosstrak.ale.exception.CCSpecValidationException;
import org.fosstrak.ale.exception.ECSpecValidationException;
import org.fosstrak.ale.exception.ImplementationException;
import org.fosstrak.ale.server.Pattern;
import org.fosstrak.ale.server.PatternUsage;
import org.fosstrak.ale.server.Tag;
import org.fosstrak.ale.server.Tag.OpReportResult;
import org.fosstrak.ale.server.util.TagHelper;
import org.fosstrak.alecc.util.CCReportSetEnum;
import org.fosstrak.ale.xsd.ale.epcglobal.CCCmdReport;
import org.fosstrak.ale.xsd.ale.epcglobal.CCCmdReport.TagReports;
import org.fosstrak.ale.xsd.ale.epcglobal.CCCmdSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.CCCmdSpec.StatProfileNames;
import org.fosstrak.ale.xsd.ale.epcglobal.CCFilterSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.CCOpReport;
import org.fosstrak.ale.xsd.ale.epcglobal.CCOpSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.CCTagReport;
import org.fosstrak.ale.xsd.ale.epcglobal.CCTagReport.OpReports;
import org.fosstrak.ale.xsd.ale.epcglobal.CCTagReport.Stats;
import org.fosstrak.ale.xsd.ale.epcglobal.CCTagStat;
import org.fosstrak.ale.xsd.ale.epcglobal.CCTagStat.StatBlocks;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFilterListMember;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReaderStat;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReaderStat.Sightings;
import org.fosstrak.ale.xsd.ale.epcglobal.ECSightingStat;
import org.fosstrak.tdt.TDTException;

/**
 * This class represents a report.
 * It filters and groups tags, add them to the report and build cc reports.
 * 
 * @author regli
 * @author swieland
 * @author wafa.soubra@orange.com
 * @author Wondeuk Yoon
 */
public class Report {

	/** logger. */
	private static final Logger LOG = Logger.getLogger(Report.class);
	
	/** name of this report. */
	private final String name;
	/** current event cycle delivers tags. */
	private final CommandCycle currentCommandCycle;
	
	/** report generator which contains this command cycle. */
	private final ReportsGenerator generator;
	
	/** patterns of tags which are included in this report. */
	private final Set<Pattern> includePatterns = new HashSet<Pattern>();
	/** patterns of tags which are excluded from this report. */
	private final Set<Pattern> excludePatterns = new HashSet<Pattern>();
	/** type of this report (current). */
	private String reportType;

	/** cc report. */
	private CCCmdReport report;
	/** cc report specification. */
	private CCCmdSpec reportSpec;
	
	private Hashtable<Integer, CCOpSpec> OpSpecTable = new Hashtable<Integer, CCOpSpec>();
	
	/**
	 * Constructor set parameters, read specifiaction and initializes patterns.
	 * 
	 * @param reportSpec defines how the report should be generated
	 * @param currentEventCycle this report belongs to
	 * @throws ImplementationException if an implementation exception occurs
	 * @throws CCSpecValidationException 
	 */
	public Report(CCCmdSpec reportSpec, CommandCycle currentCommandCycle, ReportsGenerator generator) throws ImplementationException, CCSpecValidationException {
		
		// set ReportGenerator
		this.generator = generator;
		
		this.OpSpecTable = generator.getOpSpecTable();
		
		// set name
		name = reportSpec.getName();
		
		LOG.debug("Create report '" + name + "'");
		
		// create CCReport
		report = new CCCmdReport();
		
		// set CCReport name
		report.setCmdSpecName(name);
		
		// set TagReports
		report.setTagReports(new TagReports());
		
		// set type
		reportType = "CURRENT";

		// set CCReportSpec
		this.reportSpec = reportSpec;
		
		// set currentCommandCycle
		this.currentCommandCycle = currentCommandCycle;
				
		// init patterns
		initFilterPatterns();
		
	}

	/**
	 * This method adds a tag to the report.
	 * 
	 * @param tag to add
	 * @throws CCSpecValidationException if the tag is invalid
	 * @throws ImplementationException if an implementation exception occurs
	 */
	public void addTag(Tag tag) throws CCSpecValidationException, ImplementationException {
		try {
		String epcInTagFormat = TagHelper.convert_to_TAG_ENCODING(tag.getTagLength(), tag.getFilter(), tag.getCompanyPrefixLength(), tag.getTagAsBinary(), TagHelper.getTDTEngine());
		
		// check if the tag is a member of this report (use filter patterns and set spec)
		
			if(isMember(epcInTagFormat)) {

					LOG.debug("Command '" + tag + "' is member of report '" + name + "'");
				
					// add tag to report
					addTagToCCTag(tag);
			}
		} catch (ECSpecValidationException e) {
			e.printStackTrace();
			throw new CCSpecValidationException(e);
		} catch (TDTException e) {
			LOG.debug("Tag "+TagHelper.getTDTEngine().bin2hex(tag.getTagAsBinary())+" cannot be converted. skip this tag.");
			return;
		}
	}
	
	/**
	 * this method is for compatibility reasons such that eg ReportTest is not broken.
	 * @param tag to add
	 * @throws CCSpecValidationException if the tag is invalid
	 * @throws ImplementationException if an implementation exception occurs
	 */
	public void addTag(org.fosstrak.reader.rprm.core.msg.notification.TagType tag) throws CCSpecValidationException, ImplementationException {
		Tag newtag = new Tag();
		newtag.setTagID(tag.getTagID());
		newtag.setTagIDAsPureURI(tag.getTagIDAsPureURI());
		addTag(newtag);
	}

	/**
	 * This method returns the new cc report.
	 * 
	 * @return cc report
	 * @throws CCSpecValidationException if a tag is invalid
	 * @throws ImplementationException if an implementation exception occurs
	 */
	public CCCmdReport getCCCmdReport() throws CCSpecValidationException, ImplementationException {
		Set<Tag> currentCycleTags = currentCommandCycle.getTags();
		
		//generate new CCReport
		if (CCReportSetEnum.isSameCCReportSet(CCReportSetEnum.CURRENT, reportType)) {

			// get tags from current CommandCycle 
			for (Tag tag : currentCycleTags) {
				addTag(tag);
			}
		} else {
			LOG.info("unknown reportType: " + reportType);
		}
		

		if (reportSpec.isReportIfEmpty() || !isEmpty()) {
			CCCmdReport temp = report;	
			report = new CCCmdReport();
			// set TagReports
			report.setTagReports(new TagReports());
			List<CCTagReport> listTagReport = report.getTagReports().getTagReport();
			listTagReport = new ArrayList<CCTagReport>();
			report.setCmdSpecName(name);	
			return temp;
		} else {
			report = new CCCmdReport();
			// set TagReports
			report.setTagReports(new TagReports());
			List<CCTagReport> listTagReport = report.getTagReports().getTagReport();
			listTagReport = new ArrayList<CCTagReport>();
			report.setCmdSpecName(name);
			return null;
		}
	}

	//
	// private methods
	//
	
	/**
	 * This method initializes the filter patterns on the basis of the cc report specification.
	 * @throws CCSpecValidationException 
	 */
	private void initFilterPatterns() throws CCSpecValidationException {
	
		LOG.debug("Init filter patterns");
		
		// get filter spec
		CCFilterSpec CCFilterSpec = reportSpec.getFilterSpec();
		if (CCFilterSpec != null)
		{
			List<ECFilterListMember> filterSpec = reportSpec.getFilterSpec().getFilterList().getFilter();
			if (!filterSpec.isEmpty()) {
				
				for (ECFilterListMember filter : filterSpec)
				{
					// add CCIncludePatterns from spec to includePatterns set
					if (filter.getIncludeExclude().equals("INCLUDE"))
					{
						for (String pattern : filter.getPatList().getPat())
						{
							try {
								includePatterns.add(new Pattern(pattern, PatternUsage.FILTER));
							} catch (ECSpecValidationException e) {
								LOG.debug("Specification Validation Exception: ", e);
							}
						}
					}
					
					// add CCExcludePatterns from spec to excludePatterns set
					if (filter.getIncludeExclude().equals("EXCLUDE"))
					{
						for (String pattern : filter.getPatList().getPat())
						{
							try {
								excludePatterns.add(new Pattern(pattern, PatternUsage.FILTER));
							} catch (ECSpecValidationException e) {
								LOG.debug("Specification Validation Exception: ", e);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * This method checks on the basis of the filter patterns if the specified tag could be a member of this report.
	 * 
	 * @param tagURI to check for possible membership
	 * @return true if the tag could be a member of this report and false otherwise
	 * @throws CCSpecValidationException if the tag is invalid
	 * @throws ImplementationException if an implementation exception occurs
	 */
	private boolean isMember(String tagURI) throws ECSpecValidationException, ImplementationException {
				
		// check if tagURI is member of an exclude pattern
		for (Pattern pattern : excludePatterns) {
			if (pattern.isMember(tagURI)) {
				return false;
			}
		}
		
		// check if there are include patterns specified
		if (includePatterns.size() == 0) {
			return true;
		} else {
			
			// check if tagURI is a member of an include pattern
			for (Pattern pattern : includePatterns) {
				if (pattern.isMember(tagURI)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * This method adds a tag to the matching group of the report.
	 * 
	 * @param tag to add
	 * @throws CCSpecValidationException if the tag is invalid
	 * @throws ImplementationException if an implementation exception occurs
	 */
	private void addTagToCCTag(Tag tag) throws ImplementationException, ECSpecValidationException {
		
		// get tag URI
		String tagURI = tag.getTagIDAsPureURI();
		// if this one is null, try something different to compense crashes...
		if (null == tagURI) {
			//tagURI = TagHelper.getTDTEngine().bin2hex(tag.getTagAsBinary());
			LOG.debug("Transformation to EPC-pure was failed (in LLRPAdapter.java). skip this tag.");
			return;
		}
		// skip if tagURI is not a member of the CCFilter
		
		String epcInTagFormat = TagHelper.convert_to_TAG_ENCODING(tag.getTagLength(), tag.getFilter(), tag.getCompanyPrefixLength(), tag.getTagAsBinary(), TagHelper.getTDTEngine());
		
		List<CCTagReport> listTagReport = report.getTagReports().getTagReport();

		CCTagReport tagReport = new CCTagReport();
		tagReport.setId(epcInTagFormat);
		
		
		// check if we need to add tag stats
		StatProfileNames ProfileNames = reportSpec.getStatProfileNames();
		if ((null != ProfileNames)) {
			LOG.debug("adding stat profile");
			addStatProfiles(
					tag,
					tagReport,
					ProfileNames.getStatProfileName());
		}
		
		//Add OpReport.
		if (OpSpecTable != null) {
			if (!OpSpecTable.isEmpty()) {
				OpReports temp = new OpReports();
				System.out.println("#############" + tag.getopresult().size());
							
				for (OpReportResult opresult : tag.getopresult())
				{
					if (opresult.OpSpecID >= 1000)
					{
						if (opresult.OpResult == 0)
						{
							if (OpSpecTable.get(opresult.OpSpecID).getOpType().equals("READ"))
							{
								CCOpReport e = new CCOpReport();
								e.setData(opresult.Data);
								e.setOpStatus("SUCCESS"); 
								temp.getOpReport().add(e);
							}
							else if (OpSpecTable.get(opresult.OpSpecID).getOpType().equals("WRITE"))
							{
								CCOpReport e = new CCOpReport();
								e.setData(OpSpecTable.get(opresult.OpSpecID).getDataSpec().getData());
								e.setOpStatus("SUCCESS"); 
								temp.getOpReport().add(e);
							}
							else
							{
								CCOpReport e = new CCOpReport();
								e.setOpStatus("SUCCESS"); 
								temp.getOpReport().add(e);
							}
						}
						else if (opresult.OpResult == 1)
						{
							if (OpSpecTable.get(opresult.OpSpecID).getOpType().equals("READ"))
							{
								CCOpReport e = new CCOpReport();
								e.setData(opresult.Data);
								e.setOpStatus("Tag_Memory_Overrun_Error"); 
								temp.getOpReport().add(e);
							}
							else if (OpSpecTable.get(opresult.OpSpecID).getOpType().equals("WRITE"))
							{
								CCOpReport e = new CCOpReport();
								e.setData(OpSpecTable.get(opresult.OpSpecID).getDataSpec().getData());
								e.setOpStatus("Tag_Memory_Overrun_Error"); 
								temp.getOpReport().add(e);
							}
							else
							{
								CCOpReport e = new CCOpReport();
								e.setOpStatus("Tag_Memory_Overrun_Error"); 
								temp.getOpReport().add(e);
							}
						}
						else if (opresult.OpResult == 2)
						{
							if (OpSpecTable.get(opresult.OpSpecID).getOpType().equals("READ"))
							{
								CCOpReport e = new CCOpReport();
								e.setData(opresult.Data);
								e.setOpStatus("Tag_Memory_Locked_Error"); 
								temp.getOpReport().add(e);
							}
							else if (OpSpecTable.get(opresult.OpSpecID).getOpType().equals("WRITE"))
							{
								CCOpReport e = new CCOpReport();
								e.setData(OpSpecTable.get(opresult.OpSpecID).getDataSpec().getData());
								e.setOpStatus("Tag_Memory_Locked_Error"); 
								temp.getOpReport().add(e);
							}
							else
							{
								CCOpReport e = new CCOpReport();
								e.setOpStatus("Tag_Memory_Locked_Error"); 
								temp.getOpReport().add(e);
							}
						}
						else if (opresult.OpResult == 3)
						{
							if (OpSpecTable.get(opresult.OpSpecID).getOpType().equals("READ"))
							{
								CCOpReport e = new CCOpReport();
								e.setData(opresult.Data);
								e.setOpStatus("Insufficient_Power"); 
								temp.getOpReport().add(e);
							}
							else if (OpSpecTable.get(opresult.OpSpecID).getOpType().equals("WRITE"))
							{
								CCOpReport e = new CCOpReport();
								e.setData(OpSpecTable.get(opresult.OpSpecID).getDataSpec().getData());
								e.setOpStatus("Insufficient_Power"); 
								temp.getOpReport().add(e);
							}
							else
							{
								CCOpReport e = new CCOpReport();
								e.setOpStatus("Insufficient_Power"); 
								temp.getOpReport().add(e);
							}
						}
						else if (opresult.OpResult == 4)
						{
							if (OpSpecTable.get(opresult.OpSpecID).getOpType().equals("READ"))
							{
								CCOpReport e = new CCOpReport();
								e.setData(opresult.Data);
								e.setOpStatus("Nonspecific_Tag_Error"); 
								temp.getOpReport().add(e);
							}
							else if (OpSpecTable.get(opresult.OpSpecID).getOpType().equals("WRITE"))
							{
								CCOpReport e = new CCOpReport();
								e.setData(OpSpecTable.get(opresult.OpSpecID).getDataSpec().getData());
								e.setOpStatus("Nonspecific_Tag_Error"); 
								temp.getOpReport().add(e);
							}
							else
							{
								CCOpReport e = new CCOpReport();
								e.setOpStatus("Nonspecific_Tag_Error"); 
								temp.getOpReport().add(e);
							}
						}
						else if (opresult.OpResult == 5)
						{
							if (OpSpecTable.get(opresult.OpSpecID).getOpType().equals("READ"))
							{
								CCOpReport e = new CCOpReport();
								e.setData(opresult.Data);
								e.setOpStatus("No_Response_From_Tag"); 
								temp.getOpReport().add(e);
							}
							else if (OpSpecTable.get(opresult.OpSpecID).getOpType().equals("WRITE"))
							{
								CCOpReport e = new CCOpReport();
								e.setData(OpSpecTable.get(opresult.OpSpecID).getDataSpec().getData());
								e.setOpStatus("No_Response_From_Tag"); 
								temp.getOpReport().add(e);
							}
							else
							{
								CCOpReport e = new CCOpReport();
								e.setOpStatus("No_Response_From_Tag"); 
								temp.getOpReport().add(e);
							}
						}
						else if (opresult.OpResult == 6)
						{
							if (OpSpecTable.get(opresult.OpSpecID).getOpType().equals("READ"))
							{
								CCOpReport e = new CCOpReport();
								e.setData(opresult.Data);
								e.setOpStatus("Nonspecific_Reader_Error"); 
								temp.getOpReport().add(e);
							}
							else if (OpSpecTable.get(opresult.OpSpecID).getOpType().equals("WRITE"))
							{
								CCOpReport e = new CCOpReport();
								e.setData(OpSpecTable.get(opresult.OpSpecID).getDataSpec().getData());
								e.setOpStatus("Nonspecific_Reader_Error"); 
								temp.getOpReport().add(e);
							}
							else
							{
								CCOpReport e = new CCOpReport();
								e.setOpStatus("Nonspecific_Reader_Error"); 
								temp.getOpReport().add(e);
							}
						}
						else 
						{
							if (OpSpecTable.get(opresult.OpSpecID).getOpType().equals("READ"))
							{
								CCOpReport e = new CCOpReport();
								e.setData(opresult.Data);
								e.setOpStatus("FAIL"); 
								temp.getOpReport().add(e);
							}
							else if (OpSpecTable.get(opresult.OpSpecID).getOpType().equals("WRITE"))
							{
								CCOpReport e = new CCOpReport();
								e.setData(OpSpecTable.get(opresult.OpSpecID).getDataSpec().getData());
								e.setOpStatus("FAIL"); 
								temp.getOpReport().add(e);
							}
							else
							{
								CCOpReport e = new CCOpReport();
								e.setOpStatus("FAIL"); 
								temp.getOpReport().add(e);
							}
						}
					}
				}
				tagReport.setOpReports(temp);
			}	
		}
		//ORANGE End

		if (tagReport.getOpReports().getOpReport().size() > 0) {
			boolean exist = false;
			for (CCTagReport temp : listTagReport) {
				if (temp.getId().equals(tagReport.getId())) {
					exist = true;
					for(CCOpReport opr : tagReport.getOpReports().getOpReport()) {
						temp.getOpReports().getOpReport().add(opr);
					}
				} 
							
			} 
			if(!exist) listTagReport.add(tagReport);	
		}
			
		
		LOG.debug("Tag '" + tagURI + "' successfully added to group '" + epcInTagFormat + "' of report '" + name + "'");
		
	}

	/**
	 * for each statistics profile name add the respective statistics profile.
	 * @param tag the tag holding information the statistics.
	 * @param groupMember the group member where to add the statistics.
	 * @param statProfileName a list of statistic profile names.
	 */
	private void addStatProfiles(Tag tag, CCTagReport tagMember, List<String> statProfileName) {
		
		tagMember.setStats(new Stats());
		List<CCTagStat> ccTagStats = tagMember.getStats().getStat();
		for (String profile : statProfileName) {		
			LOG.debug("adding stat profile: " + profile);
		
			CCTagStat ccTagStat = new CCTagStat();
			ccTagStats.add(ccTagStat);
			
			ccTagStat.setProfile(profile);
			ccTagStat.setStatBlocks(new StatBlocks());
			ECReaderStat readerStat = new ECReaderStat();
			ccTagStat.getStatBlocks().getStatBlock().add(readerStat);
			
			readerStat.setReaderName(tag.getReader());
			readerStat.setSightings(new Sightings());
			readerStat.getSightings().getSighting().add(new ECSightingStat());
		}
	}
	
	
	
	/**
	 * This method indicates if the report contains any tags.
	 * 
	 * @return true if the report is empty and false otherwise
	 */
	private boolean isEmpty() {
		
		List<CCTagReport> TagReports = report.getTagReports().getTagReport();
		if(TagReports.size()>0) return false;
		
		return true;

	}
}