/*
 *
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
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

package org.fosstrak.ale.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.fosstrak.ale.exception.ECSpecValidationException;
import org.fosstrak.ale.exception.ImplementationException;
import org.fosstrak.ale.server.tm.SymbolicField;
import org.fosstrak.ale.server.tm.SymbolicFieldRepo;
import org.fosstrak.ale.server.util.TagFormatHelper;
import org.fosstrak.ale.server.util.TagHelper;
import org.fosstrak.ale.util.ECReportSetEnum;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFilterListMember;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFilterListMember.PatList;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFilterSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFilterSpecExtension;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFilterSpecExtension.FilterList;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReaderStat;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReaderStat.Sightings;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReport;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReportGroup;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReportGroupCount;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReportGroupList;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReportGroupListMember;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReportGroupListMemberExtension;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReportGroupListMemberExtension.FieldList;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReportGroupListMemberExtension.Stats;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReportMemberField;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReportOutputFieldSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReportOutputSpecExtension;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReportSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReportSpecExtension;
import org.fosstrak.ale.xsd.ale.epcglobal.ECSightingStat;
import org.fosstrak.ale.xsd.ale.epcglobal.ECTagStat;
import org.fosstrak.ale.xsd.ale.epcglobal.ECTagTimestampStat;
import org.fosstrak.ale.xsd.ale.epcglobal.ECTagStat.StatBlocks;
import org.fosstrak.ale.xsd.epcglobal.EPC;
import org.fosstrak.tdt.TDTEngine;

/**
 * This class represents a report.
 * It filters and groups tags, add them to the report and build ec reports.
 * 
 * @author regli
 * @author swieland
 * @author wafa.soubra@orange.com
 * @author Janggwan Im
 */
public class Report {

	/** logger. */
	private static final Logger LOG = Logger.getLogger(Report.class);
	
	/** name of this report. */
	private final String name;
	/** current event cycle delivers tags. */
	private final EventCycle currentEventCycle;
	
	
	/** patterns of tags which are included in this report. */
	private final Set<Pattern> includePatterns = new HashSet<Pattern>();
	/** patterns of tags which are excluded from this report. */
	private final Set<Pattern> excludePatterns = new HashSet<Pattern>();
	/** patterns to group the tags of this report. */
	private final Set<Pattern> groupPatterns = new HashSet<Pattern>();
		
	/** type of this report (current, additions or deletions). */
	private String reportType;

	/** ec report. */
	private ECReport report;
	/** ec report specification. */
	private ECReportSpec reportSpec;
	
	/**
	 * Constructor set parameters, read specifiaction and initializes patterns.
	 * 
	 * @param reportSpec defines how the report should be generated
	 * @param currentEventCycle this report belongs to
	 * @throws ImplementationException if an implementation exception occurs
	 */
	public Report(ECReportSpec reportSpec, EventCycle currentEventCycle) throws ImplementationException {
		
		// set name
		name = reportSpec.getReportName();
		
		LOG.debug("Create report '" + name + "'");
		
		// create ECReport
		report = new ECReport();
		
		// set ECReport name
		report.setReportName(name);
		
		// set type
		reportType = reportSpec.getReportSet().getSet();

		// set ECReportSpec
		this.reportSpec = reportSpec;
		
		// set currentEventCycle
		this.currentEventCycle = currentEventCycle;
				
		// init patterns
		initFilterPatterns();
		initGroupPatterns();

	}

	/**
	 * This method adds a tag to the report.
	 * 
	 * @param tag to add
	 * @throws ECSpecValidationException if the tag is invalid
	 * @throws ImplementationException if an implementation exception occurs
	 */
	public void addTag(Tag tag) throws ECSpecValidationException, ImplementationException {
		TDTEngine tdt = TagHelper.getTDTEngine();
		// get tag URI
		String tagURI = tag.getTagIDAsTagURI();//.getTagIDAsPureURI();
		
		boolean isMember = true;
		
		
		
		if(this.reportSpec.getFilterSpec() != null) {
			ECFilterSpec filterSpec = this.reportSpec.getFilterSpec();
			
			if(filterSpec.getIncludePatterns() != null || filterSpec.getExcludePatterns() != null) {
				isMember = isMember && isMember(tagURI);
			}
			
			if(filterSpec.getExtension() != null) {
				ECFilterSpecExtension filterSpecExtension = filterSpec.getExtension();
				
				if(filterSpecExtension.getFilterList() != null) {
					List<ECFilterListMember> filterListMembers = filterSpecExtension.getFilterList().getFilter();
					
					for(ECFilterListMember filterListMember : filterListMembers) {
						
						// represent the user memory to binary format
						String usermemInBinary = tag.getUserMemory();
						if(usermemInBinary != null) {

					    	usermemInBinary = usermemInBinary.replaceAll("\\s", "");
					    	usermemInBinary = new StringBuilder("f").append(usermemInBinary).toString();		//usermem = "f".concat(usermem);
					    	usermemInBinary = tdt.hex2bin(usermemInBinary);
					    	usermemInBinary = usermemInBinary.substring(4);
						}
						
						if(filterListMember.getFieldspec() != null) {
							String fieldName = filterListMember.getFieldspec().getFieldname();
							if(fieldName.equals("epc")) {
								
								
								// check if the tag is a member of this report (use filter patterns and set spec)
								
								isMember = isMember && isMember(tagURI);

							} else if(fieldName.equalsIgnoreCase("killPwd")) {
								//TODO : killPwd fieldname
								// same as "@0.32"
								// datatype : "uint", format: "hex"
								
								System.out.println("fieldname \"killPwd\": access to reserved bank is not implemented");
								

							} else if(fieldName.equalsIgnoreCase("accessPwd")) {
								//TODO : accessPwd fieldname
								// same as "@0.32.32"
								// datatype : "uint", format: "hex"

								System.out.println("fieldname \"accessPwd\": access to reserved bank is not implemented");
								
							} else if(fieldName.equalsIgnoreCase("epcBank")) {
								// return the contents of epc bank
								// datatype: "bits", format: "hex"
								String fieldValue = tag.getEpcBank();
								
							} else if(fieldName.equalsIgnoreCase("tidBank")) {
								// TODO: fieldname "tidBank"
								// return the contents of tid bank
								// datatype: "bits", format: "hex"
								
								System.out.println("fieldname \"tidBank\": access to tid bank is not implemented");
								
								
							} else if(fieldName.equalsIgnoreCase("userBank")) {
								// return the contents of user memory bank
								// datatype: "bits", format: "hex"
								
								String fieldValue = tag.getUserMemory();
								
								
								isMember = computeMembership(isMember, filterListMember,
										fieldValue);
								
								
							} else if(fieldName.equalsIgnoreCase("afi")) {
								// TODO:  application family identifier
								// same as "@1.8.24"
								// datatype: "uint", format: "hex"

								String fieldValue = processEpcBank(tag, tdt, 8,
										24, "uint", "hex");
								isMember = computeMembership(isMember, filterListMember,
										fieldValue);
								
							} else if(fieldName.equalsIgnoreCase("nsi")) {
								// TODO: Numbering System Identifier (NSI)
								// same as "@1.9.23"
								// datatype: "uint", format: "hex"
								
								String fieldValue = processEpcBank(tag, tdt, 9,
										23, "uint", "hex");
								isMember = computeMembership(isMember, filterListMember,
										fieldValue);
								
							} else if (fieldName.startsWith("@")) {
								// TODO: fieldnames start with "@"
								// datatype: "uint", format: "hex"
								
								String[] part = fieldName.substring(1).split("\\.");
								int bank = Integer.parseInt(part[0]);
								int length = Integer.parseInt(part[1]);
								int offset = Integer.parseInt(part[2]);
								String datatype = "uint";
								String format = "hex";
								

								if(bank == 0) {
									System.out.println("Access to bank "+bank+" is not supported.");
									
								} else if(bank == 1) {
									
									String fieldValue = processEpcBank(tag, tdt, length,
											offset, datatype, format);
									isMember = computeMembership(isMember, filterListMember,
											fieldValue);
									
								} else if(bank == 2) {
									System.out.println("Access to bank "+bank+" is not supported.");
									
								} else if(bank == 3) {
									String fieldValue = processUserbank(tdt, usermemInBinary, length,
											offset, datatype, format);
									isMember = computeMembership(isMember, filterListMember,
											fieldValue);
									
								} else {
									System.out.println("field "+fieldName+" not found");
									
								}
								
								

							} else {
								// TODO: symbolic fieldnames
								// datatype: "uint", format: "hex"

								SymbolicField symbolicField = SymbolicFieldRepo.getInstance().getSymbolicField(fieldName);
								if(symbolicField != null) {
									int bank = symbolicField.getBank();
									int length = symbolicField.getLength();
									int offset = symbolicField.getOffset();
									String datatype = symbolicField.getDataType() != null? symbolicField.getDataType(): "uint";
									String format = symbolicField.getFormat() != null? symbolicField.getFormat(): "hex";
									
									if(bank == 0) {
										System.out.println("Access to bank "+bank+" is not supported.");
										
									} else if(bank == 1) {
										String fieldValue = processEpcBank(tag, tdt, length,
												offset, datatype, format);
										isMember = computeMembership(isMember, filterListMember,
												fieldValue);
										
									} else if(bank == 2) {
										System.out.println("Access to bank "+bank+" is not supported.");
										
									} else if(bank == 3) {
										String fieldValue = processUserbank(tdt, usermemInBinary, length,
												offset, datatype, format);
										isMember = computeMembership(isMember, filterListMember,
												fieldValue);
										
									} else {
										System.out.println("field "+fieldName+" not found");
										
									}
									
								} else {
									LOG.debug("There is no such symbolic fieldname "+fieldName+". skip processing userdata of this tag");
								}
							}
						}
						
					}
				}
			}
		}
		if(isMember) {

			LOG.debug("Event '" + tag + "' is member of report '" + name + "'");
		
			// add tag to report
			addTagToReportGroup(tag);
		
		}
	}

	private boolean computeMembership(boolean isMember,
			ECFilterListMember filterListMember, String fieldValue) {
		for(String pat : filterListMember.getPatList().getPat()) {
			if(filterListMember.getIncludeExclude().equalsIgnoreCase("include")) {
				isMember = isMember && pat.equals(fieldValue);
			} else {
				isMember = isMember && !pat.equals(fieldValue);
			}
		}
		return isMember;
	}
	
	/**
	 * this method is for compatibility reasons such that eg ReportTest is not broken.
	 * @param tag to add
	 * @throws ECSpecValidationException if the tag is invalid
	 * @throws ImplementationException if an implementation exception occurs
	 */
	public void addTag(org.fosstrak.reader.rprm.core.msg.notification.TagType tag) throws ECSpecValidationException, ImplementationException {
		Tag newtag = new Tag();
		newtag.setTagID(tag.getTagID());
		newtag.setTagIDAsPureURI(tag.getTagIDAsPureURI());
		addTag(newtag);
	}

	/**
	 * helper method to display tags that were added or deleted.
	 * @param reportTags a map holding the tags that were either added or deleted.
	 */
	private void writeTraceInformation(Map<String, Tag> reportTags) {
		String out = '\n' + "+++++++++++++++++++++++++++++++++++++++++++++++++++++" + '\n';
		out +=  '\t' + "eventcycle " + currentEventCycle.getName() + '\n';
		out +=  '\t' + "round " + currentEventCycle.getRounds() + '\n';
		if (reportTags == null) {
			out += '\t' + "no tags" + '\n';
			out +=  "+++++++++++++++++++++++++++++++++++++++++++++++++++++" + '\n';
			LOG.info(out);
			return;
		}
		
		
		for (Tag tag : reportTags.values()) {
			out += '\t' + tag.getTagIDAsPureURI() + '\n';
		}
		out +=  "+++++++++++++++++++++++++++++++++++++++++++++++++++++" + '\n';
		LOG.trace(out);
	}
	
	/**
	 * This method returns the new ec report.
	 * 
	 * @return ec report
	 * @throws ECSpecValidationException if a tag is invalid
	 * @throws ImplementationException if an implementation exception occurs
	 */
	public ECReport getECReport() throws ECSpecValidationException, ImplementationException {
		Set<Tag> currentCycleTags = currentEventCycle.getTags();
		Set<Tag> lastCycleTags = currentEventCycle.getLastEventCycleTags();
		
		//generate new ECReport
		if (ECReportSetEnum.isSameECReportSet(ECReportSetEnum.ADDITIONS, reportType)) {
			
			// get additional tags
			Map<String, Tag> reportTags = new HashMap<String, Tag>();

			// add tags from current EventCycle 
			for (Tag tag : currentCycleTags) {
				reportTags.put(tag.getTagIDAsPureURI(), tag);
			}
				
			// remove tags from last EventCycle
			if (lastCycleTags != null) {
				for (Tag tag : lastCycleTags) {
					reportTags.remove(tag.getTagIDAsPureURI());
				}
			}

			for (Tag tag : reportTags.values()) {
				addTag(tag);
			}
			//writeDebugInformation(reportTags);
	
		} else if (ECReportSetEnum.isSameECReportSet(ECReportSetEnum.CURRENT, reportType)) {

			// get tags from current EventCycle 
			for (Tag tag : currentCycleTags) {
				addTag(tag);
			}
		} else if (ECReportSetEnum.isSameECReportSet(ECReportSetEnum.DELETIONS, reportType)) {
			
			// get removed tags
			Map<String, Tag> reportTags = new HashMap<String, Tag>();
				
			// add tags from last EventCycle
			if (lastCycleTags != null) {
				for (Tag tag : lastCycleTags) {
					reportTags.put(tag.getTagIDAsPureURI(), tag);
				}
			}
				
			// remove tags from current EventCycle
			for (Tag tag : currentCycleTags) {
				reportTags.remove(tag.getTagIDAsPureURI());
			}
				
			// add tags to report with filtering
			for (Tag tag : reportTags.values()) {
				addTag(tag);
			}
			if (LOG.isTraceEnabled()) {
				writeTraceInformation(reportTags);
			}
		} else {
			LOG.info("unknown reportType: " + reportType);
		}
		

		if (reportSpec.isReportIfEmpty() || !isEmpty()) {
			ECReport temp = report;	
			report = new ECReport();
			report.setReportName(name);	
			return temp;
		} else {
			report = new ECReport();
			report.setReportName(name);
			return null;
		}
	}

	//
	// private methods
	//
	
	/**
	 * This method initializes the filter patterns on the basis of the ec report specification.
	 */
	private void initFilterPatterns() {
	
		LOG.debug("Init filter patterns");
		
		// get filter spec
		ECFilterSpec filterSpec = reportSpec.getFilterSpec();
		if (filterSpec != null) {
			
			// add ECIncludePatterns from spec to includePatterns set
			if(filterSpec.getIncludePatterns() != null) {
				List<String> ecIncludePatterns = filterSpec.getIncludePatterns().getIncludePattern();
				if (ecIncludePatterns != null) {
					for (String pattern : ecIncludePatterns) {
						try {
							includePatterns.add(new Pattern(pattern, PatternUsage.FILTER));
						} catch (ECSpecValidationException e) {
							LOG.debug("Specification Validation Exception: ", e);
						}
					}
				}				
			}
			
			// add ECExcludePatterns from spec to excludePatterns set
			if(filterSpec.getExcludePatterns() != null) {
				List<String> ecExcludePatterns = filterSpec.getExcludePatterns().getExcludePattern();
				if (ecExcludePatterns != null) {
					for (String pattern : ecExcludePatterns) {
						try {
							excludePatterns.add(new Pattern(pattern, PatternUsage.FILTER));
						} catch (ECSpecValidationException e) {
							LOG.debug("Specification Validation Exception: ", e);
						}
					}
				}
			}
			
			ECFilterSpecExtension filterSpecExt = filterSpec.getExtension();
			
			if(filterSpecExt != null) {
				FilterList filterList = filterSpecExt.getFilterList();
				if(filterList != null) {
					 List<ECFilterListMember> filterListMembers = filterList.getFilter();
					 for(ECFilterListMember m : filterListMembers) {
						 if(m.getFieldspec() != null && m.getFieldspec().getFieldname().equalsIgnoreCase("epc")) {
							 if(m.getIncludeExclude().equalsIgnoreCase("INCLUDE")) {
								 PatList plist = m.getPatList();
								 if(plist != null) {
									 List<String> patStrs = plist.getPat();
									 for(String s : patStrs) {
										 try {
											includePatterns.add(new Pattern(s, PatternUsage.FILTER));
										} catch (ECSpecValidationException e) {
											LOG.debug("Specification Validation Exception: ", e);
										}
									 }
								 }
							 } else if(m.getIncludeExclude().equalsIgnoreCase("EXCLUDE")) {
								 PatList plist = m.getPatList();
								 if(plist != null) {
									 List<String> patStrs = plist.getPat();
									 for(String s : patStrs) {
										 try {
											excludePatterns.add(new Pattern(s, PatternUsage.FILTER));
										} catch (ECSpecValidationException e) {
											LOG.debug("Specification Validation Exception: ", e);
										}
									 }
								 }						 
							 }
						 }
					 }
				}
			}
		}
	}
	
	/**
	 * This method initializes the group patterns on the basis of the ec report specification.
	 */
	private void initGroupPatterns() {
		
		LOG.debug("Init group patterns");
		if (reportSpec.getGroupSpec() != null) {
			// get group spec
			List<String> groupSpec = reportSpec.getGroupSpec().getPattern();
			// add ECGroupPatterns from spec to groupPatterns set
			for (String pattern : groupSpec) {
				try {
					groupPatterns.add(new Pattern(pattern, PatternUsage.GROUP));
				} catch (ECSpecValidationException e) {
					LOG.debug("Specification Validation Exception: ", e);
				}	
			}
		}		
	}
	
	/**
	 * This method checks on the basis of the filter patterns if the specified tag could be a member of this report.
	 * 
	 * @param tagURI to check for possible membership
	 * @return true if the tag could be a member of this report and false otherwise
	 * @throws ECSpecValidationException if the tag is invalid
	 * @throws ImplementationException if an implementation exception occurs
	 */
	private boolean isMember(String tagURI) throws ECSpecValidationException, ImplementationException {
				
		if (ECReportSetEnum.isSameECReportSet(ECReportSetEnum.ADDITIONS, reportType)) {
		
			// if report type is additions the tag is only a member if it wasn't a member of the last event cycle	
			Set<Tag> tags = currentEventCycle.getLastEventCycleTags();
			if (tags != null) {
				for (Tag tag : tags) {
					if (tag.getTagIDAsPureURI().equals(tagURI)) {
						return false;
					}
				}
			}
		}			

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
	 * @throws ECSpecValidationException if the tag is invalid
	 * @throws ImplementationException if an implementation exception occurs
	 */
	private void addTagToReportGroup(Tag tag) throws ImplementationException, ECSpecValidationException {
		try {
			// get tag URI
			String tagURI = tag.getTagIDAsTagURI();//.getTagIDAsPureURI();
			// if this one is null, try something different to compense crashes...
			if (null == tagURI) {
				tagURI = TagHelper.getTDTEngine().bin2hex(tag.getTagAsBinary());
			}
			
			// get group name (use group patterns)
			String groupName = getGroupName(tagURI);
			
			LOG.debug("The group name for tag '" + tagURI + "' is '" + groupName + "'");
			
			// get matching group
			ECReportGroup matchingGroup = null;
			List<ECReportGroup> groups = report.getGroup();
			//if (groups == null) {
			if (groups.isEmpty()) {
				matchingGroup = null;
			} else {
				for (ECReportGroup group : groups) {
					if (groupName == null) {
						if (group.getGroupName() == null) {
							matchingGroup = group;
						}
					} else {
						if (groupName.equals(group.getGroupName())) {
							matchingGroup = group;
						}
					}
				}
			}
		
			// create group if group does not already exist
			if (matchingGroup == null) {
				
				LOG.debug("Group '" + groupName + "' does not already exist, create it");
							
				// create group
				matchingGroup = new ECReportGroup();
				
				// set name
				matchingGroup.setGroupName(groupName);
				
				// set count
				if (reportSpec.getOutput().isIncludeCount()) {
					ECReportGroupCount groupCount = new ECReportGroupCount();
					groupCount.setCount(0);
					matchingGroup.setGroupCount(groupCount);
				}
				
				// create and set group list
				matchingGroup.setGroupList(new ECReportGroupList());
				
				// add to groups
				report.getGroup().add(matchingGroup);
				
			}
			
			// create group list member
			ECReportGroupListMember groupMember = new ECReportGroupListMember();
				
			TDTEngine tdt = TagHelper.getTDTEngine();
			// RAW DECIMAL	
			if (TagHelper.isReportOutputSpecIncludeRawDecimal(reportSpec.getOutput())) {
				TagHelper.addTagAsRawDecimal(tdt, groupMember, tag);
			}
			// TAG ENCODING
			if (TagHelper.isReportOutputSpecIncludeTagEncoding(reportSpec.getOutput())) {
				TagHelper.addTagAsTagEncoding(tdt, groupMember, tag);
			}
			// RAW HEX
			if (TagHelper.isReportOutputSpecIncludeRawHex(reportSpec.getOutput())) {
				TagHelper.addTagAsRawHex(tdt, groupMember, tag);
			}
			// EPC
			if (TagHelper.isReportOutputSpecIncludeEPC(reportSpec.getOutput())) {
				TagHelper.addTagAsEPC(tdt, groupMember, tag);
			}
			
			// check if we need to add tag stats
			ECReportSpecExtension ecReportSpecExtension = reportSpec.getExtension();
			if ((null != ecReportSpecExtension) && 
					(null != ecReportSpecExtension.getStatProfileNames())) {
				
				LOG.debug("adding stat profile");
				addStatProfiles(
						tag,
						groupMember,
						ecReportSpecExtension.getStatProfileNames().getStatProfileName(),
						reportType);
			}
			 
			
			//ORANGE: check if we need to add user memory in the report
			ECReportOutputSpecExtension outputExtension = reportSpec.getOutput().getExtension(); 
			if (outputExtension != null) {
				if(groupMember.getExtension() == null) {					
					ECReportGroupListMemberExtension extension = new ECReportGroupListMemberExtension();
					groupMember.setExtension(extension);
				}
				ECReportGroupListMemberExtension extension = groupMember.getExtension();
				if (outputExtension.getFieldList() != null) {
					extension.setFieldList(new FieldList());
					List<ECReportMemberField> ecReportMemberFields = extension.getFieldList().getField();
					
					// represent the user memory to binary format
					String usermemInBinary = tag.getUserMemory();
					if(usermemInBinary != null) {

				    	usermemInBinary = usermemInBinary.replaceAll("\\s", "");
				    	usermemInBinary = new StringBuilder("f").append(usermemInBinary).toString();		//usermem = "f".concat(usermem);
				    	usermemInBinary = tdt.hex2bin(usermemInBinary);
				    	usermemInBinary = usermemInBinary.substring(4);
						
						for (ECReportOutputFieldSpec outputFieldSpec : outputExtension.getFieldList().getField()) {
							if(outputFieldSpec.getFieldspec() != null) {
								if(outputFieldSpec.getFieldspec().getFieldname() != null) {
									String fieldName = outputFieldSpec.getFieldspec().getFieldname();
									
									if (fieldName.equalsIgnoreCase("UserMemory") && outputFieldSpec.isIncludeFieldSpecInReport()) {
										ECReportMemberField ecReportMemberField = new ECReportMemberField();
										ecReportMemberField.setName(fieldName);
										ecReportMemberField.setValue(tag.getUserMemory());
										
										ecReportMemberFields.add(ecReportMemberField);
									} else if(fieldName.equalsIgnoreCase("epc")) {
										// default datatype: "epc"
										// default format: "epc-tag"
										
										ECReportMemberField ecReportMemberField = new ECReportMemberField();
										ecReportMemberField.setName(fieldName);
										try {
											String bin = TagHelper.getBinaryRepresentation(tag);
											if (null != bin) {
												final String converted = TagHelper.convert_to_TAG_ENCODING(tag.getTagLength(), tag.getFilter(), tag.getCompanyPrefixLength(), bin, tdt);
												
												ecReportMemberField.setValue(converted);
											}
										} catch (Exception ex) {
											LOG.error("caught exception during tag transformation: ", ex);
										}
										ecReportMemberFields.add(ecReportMemberField);
										
									}
									else if(fieldName.equalsIgnoreCase("killPwd")) {
										//TODO : killPwd fieldname
										throw new ImplementationException("fieldname \"killPwd\": access to reserved bank is not implemented");
										/*
										ECReportMemberField ecReportMemberField = new ECReportMemberField();
										ecReportMemberField.setName(fieldName);
										ecReportMemberField.setValue(null);

										// same as "@0.32"
										field.setBank(0);
										field.setLength(32);
										field.setOffset(0);
										field.setDataType("uint");
										field.setFormat("hex");
										ecReportMemberFields.add(ecReportMemberField);
										*/
									} else if(fieldName.equalsIgnoreCase("accessPwd")) {
										//TODO : accessPwd fieldname
										throw new ImplementationException("fieldname \"accessPwd\": access to reserved bank is not implemented");
										/*
										ECReportMemberField field = new ECReportMemberField();
										field.setName(fieldName);

										// same as "@0.32.32"
										field.setBank(0);
										field.setLength(32);
										field.setOffset(32);
										field.setDataType("uint");
										field.setFormat("hex");
										*/
									} else if(fieldName.equalsIgnoreCase("epcBank")) {
										// return the contents of epc bank
										// datatype: "bits", format: "hex"
										
										ECReportMemberField ecReportMemberField = new ECReportMemberField();
										ecReportMemberField.setName(fieldName);
										ecReportMemberField.setValue(tag.getEpcBank());
										
										ecReportMemberFields.add(ecReportMemberField);
										
									} else if(fieldName.equalsIgnoreCase("tidBank")) {
										// TODO: fieldname "tidBank"
										// return the contents of tid bank
										// datatype: "bits", format: "hex"
										
										throw new ImplementationException("fieldname \"tidBank\": access to tid bank is not implemented");
										
									} else if(fieldName.equalsIgnoreCase("userBank")) {
										// return the contents of user memory bank
										// datatype: "bits", format: "hex"
										ECReportMemberField ecReportMemberField = new ECReportMemberField();
										ecReportMemberField.setName(fieldName);
										ecReportMemberField.setValue(tag.getUserMemory());	// tag.getUserMemory() is already hex string, no need to convert
										
										ecReportMemberFields.add(ecReportMemberField);
										
									} else if(fieldName.equalsIgnoreCase("afi")) {
										// TODO:  application family identifier
										// same as "@1.8.24"
										// datatype: "uint", format: "hex"
										
										ECReportMemberField ecReportMemberField = new ECReportMemberField();
										ecReportMemberField.setName(fieldName);
										
										String fieldValue = processEpcBank(tag, tdt, 8,
												24, "uint", "hex");
										ecReportMemberField.setValue(fieldValue);
										ecReportMemberFields.add(ecReportMemberField);
										//throw new ImplementationException("fieldname \"afi\": application family identifier (afi) is not implemented");
										
									} else if(fieldName.equalsIgnoreCase("nsi")) {
										// TODO: Numbering System Identifier (NSI)
										// same as "@1.9.23"
										// datatype: "uint", format: "hex"
										
										ECReportMemberField ecReportMemberField = new ECReportMemberField();
										ecReportMemberField.setName(fieldName);
										
										String fieldValue = processEpcBank(tag, tdt, 9,
												23, "uint", "hex");
										ecReportMemberField.setValue(fieldValue);
										
										ecReportMemberFields.add(ecReportMemberField);
										
										//throw new ImplementationException("fieldname \"nsi\": Numbering System Identifier (NSI) is not implemented");
										
									} else if (fieldName.startsWith("@")) {
										// TODO: fieldnames start with "@"
										// datatype: "uint", format: "hex"
										
										String[] part = fieldName.substring(1).split("\\.");
										int bank = Integer.parseInt(part[0]);
										int length = Integer.parseInt(part[1]);
										int offset = Integer.parseInt(part[2]);
										String datatype = "uint";
										String format = "hex";
										
										ECReportMemberField ecReportMemberField = new ECReportMemberField();
										ecReportMemberField.setName(fieldName);

										if(bank == 0) {
											throw new ImplementationException("Access to bank "+bank+" is not supported.");
										} else if(bank == 1) {
											
											String fieldValue = processEpcBank(tag, tdt, length,
													offset, datatype, format);
											ecReportMemberField.setValue(fieldValue);
											
										} else if(bank == 2) {
											throw new ImplementationException("Access to bank "+bank+" is not supported.");
										} else if(bank == 3) {
											String fieldValue = processUserbank(tdt, usermemInBinary, length,
													offset, datatype, format);
											ecReportMemberField.setValue(fieldValue);
											
										} else {
											throw new ImplementationException("bank "+bank+" does not exist.");
										}
										
										ecReportMemberFields.add(ecReportMemberField);

									} else {
										// TODO: symbolic fieldnames
										// datatype: "uint", format: "hex"
										
										ECReportMemberField field = new ECReportMemberField();
										field.setName(fieldName);

										// user-defined symbolic field name
										String epc_tag = TagHelper.convert_to_TAG_ENCODING(tag.getTagLength(), tag.getFilter(), tag.getCompanyPrefixLength(), tag.getTagAsBinary(), tdt);
										/*
										Map<String,SymbolicField> symbolicFields = SymbolicFieldRepo.getInstance().findSymbolicFieldMap(epc_tag);
										if(symbolicFields == null) {
											throw new ImplementationException("symbolic field does not exist for the epc "+epc_tag);
										}
										SymbolicField symbolicField = symbolicFields.get(fieldName);
										*/
										SymbolicField symbolicField = SymbolicFieldRepo.getInstance().getSymbolicField(fieldName);
										if(symbolicField != null) {
											int bank = symbolicField.getBank();
											int length = symbolicField.getLength();
											int offset = symbolicField.getOffset();
											String datatype = symbolicField.getDataType() != null? symbolicField.getDataType(): "uint";
											String format = symbolicField.getFormat() != null? symbolicField.getFormat(): "hex";
											
											ECReportMemberField ecReportMemberField = new ECReportMemberField();
											ecReportMemberField.setName(fieldName);

											if(bank == 0) {
												throw new ImplementationException("Access to bank "+bank+" is not supported.");
											} else if(bank == 1) {
												String fieldValue = processEpcBank(tag, tdt, length,
														offset, datatype, format);
												ecReportMemberField.setValue(fieldValue);
												
											} else if(bank == 2) {
												throw new ImplementationException("Access to bank "+bank+" is not supported.");
											} else if(bank == 3) {
												String fieldValue = processUserbank(tdt, usermemInBinary, length,
														offset, datatype, format);
												ecReportMemberField.setValue(fieldValue);
												
											} else {
												throw new ImplementationException("bank "+bank+" does not exist.");
											}
											ecReportMemberFields.add(ecReportMemberField);
										} else {
											LOG.debug("There is no such symbolic fieldname "+fieldName+". skip processing userdata of this tag");
										}
										
										
										
									}
								}
							}
						}
					}
				}	
			}
			//ORANGE End
			
			// add list member to group list
			List<ECReportGroupListMember> members = matchingGroup.getGroupList().getMember();
			if(groupMember.getEpc() != null && groupMember.getEpc().getValue() == null) {
				LOG.debug("Tag '" + tagURI + "' failed to convert '" + groupName + "' of report '" + name + "'");
				return;
			}
			
			// if there exists ECReportGroupListMember with the same EPCs in the group, we need to deal with this
			// this is because same tags are read from different origin readers.
			// a tag set in EventCycle did not treat duplicate tags because they are different origins 
			// FIXME current solution is inefficient. Try to find the better solution. (maybe in the tag set in EventCycle, or Tag.equals method)
			boolean exist = false;
			for(ECReportGroupListMember m : members) {
				if(m.getEpc() != null && groupMember.getEpc() != null && m.getEpc().getValue().equals(groupMember.getEpc().getValue())) {
					exist = true;
				}
				if(m.getTag() != null && groupMember.getTag() != null && m.getTag().getValue().equals(groupMember.getTag().getValue())) {
					exist = true;
				}
				if(m.getRawDecimal() != null && groupMember.getRawDecimal() != null && m.getRawDecimal().getValue().equals(groupMember.getRawDecimal().getValue())) {
					exist = true;
				}
				if(m.getRawHex() != null && groupMember.getRawHex() != null && m.getRawHex().getValue().equals(groupMember.getRawHex().getValue())) {
					exist = true;
				}
				if(exist) {
					if(m.getExtension() == null) {
						break;
					}
					if(m.getExtension().getStats() != null) {
						break;
					}
					for(ECTagStat tagstat : m.getExtension().getStats().getStat()) {
						if(tagstat.getStatBlocks() == null) {
							break;
						}
						
						ECReaderStat readerstat = new ECReaderStat();
						readerstat.setReaderName(groupMember.getExtension().getStats().getStat().get(0).getStatBlocks().getStatBlock().get(0).getReaderName());
						tagstat.getStatBlocks().getStatBlock().add(readerstat);
						
					}
					break;
					//m.getExtension().getStats().getStat().get(0).getStatBlocks().getStatBlock().get(0).getReaderName();
					//groupMember.getExtension().getStats().getStat().get(0).getStatBlocks().getStatBlock().get(0).getReaderName();
				}
			}
			if(!exist) {
				members.add(groupMember);
				
				// increment group counter
				if (reportSpec.getOutput().isIncludeCount()) {
					matchingGroup.getGroupCount().setCount(matchingGroup.getGroupCount().getCount() + 1);
				}
				
				LOG.debug("Tag '" + tagURI + "' successfully added to group '" + groupName + "' of report '" + name + "'");
			}

			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private String processEpcBank(Tag tag, TDTEngine tdt, int length, int offset,
			String datatype, String format)
			throws ImplementationException {
		String epcBankInBinary = tag.getEpcBank();
		epcBankInBinary = epcBankInBinary.replaceAll("\\s", "");
		epcBankInBinary = new StringBuilder("f").append(epcBankInBinary).toString();		//epcBankInBinary = "f".concat(epcBankInBinary);
		epcBankInBinary = tdt.hex2bin(epcBankInBinary);
		epcBankInBinary = epcBankInBinary.substring(4);
		
		String fieldValueInBinary = epcBankInBinary.substring(offset, offset+length); 
		if(datatype.equalsIgnoreCase("uint")) {
			// only "decimal" and "hex" are valid for "uint"
			if(format.equalsIgnoreCase("decimal")) {
				return tdt.bin2dec(fieldValueInBinary);
			} else if(format.equalsIgnoreCase("hex")) {
				return tdt.bin2hex(fieldValueInBinary);
			} else {
				// error, but include binary as the field value
				return fieldValueInBinary;
			}
		} else if (datatype.equalsIgnoreCase("bits")){
			// only "hex" format is valid for the datatype "bits"
			if(format.equalsIgnoreCase("hex")) {
				return tdt.bin2hex(fieldValueInBinary);
			}
		} else if (datatype.equalsIgnoreCase("iso-15962-string")){
			throw new ImplementationException("fieldname whose datatype is 'iso-15962-string' is not supported.");
		} else {
			throw new ImplementationException("fieldname whose datatype is "+datatype+" is not supported.");
		}
		throw new ImplementationException ("cannot process epc bank");
	}

	private String processUserbank(TDTEngine tdt, String usermemInBinary,
			int length, 
			int offset, 
			String datatype, 
			String format)
			throws ImplementationException {
		String fieldValueInBinary = usermemInBinary.substring(offset, offset+length); 
		if(datatype.equalsIgnoreCase("uint")) {
			// only "decimal" and "hex" are valid for "uint"
			if(format.equalsIgnoreCase("decimal")) {
				return tdt.bin2dec(fieldValueInBinary);
			} else if(format.equalsIgnoreCase("hex")) {
				return tdt.bin2hex(fieldValueInBinary);
			} else {
				// error, but include binary as the field value
				return fieldValueInBinary;
			}
		} else if (datatype.equalsIgnoreCase("bits")){
			// only "hex" format is valid for the datatype "bits"
			if(format.equalsIgnoreCase("hex")) {
				return tdt.bin2hex(fieldValueInBinary);
			}
		} else if (datatype.equalsIgnoreCase("iso-15962-string")){
			throw new ImplementationException("fieldname whose datatype is 'iso-15962-string' is not supported.");
		} else {
			throw new ImplementationException("fieldname whose datatype is "+datatype+" is not supported.");
		}
		throw new ImplementationException("cannot process userbank");
	}

	/**
	 * for each statistics profile name add the respective statistics profile.
	 * @param tag the tag holding information the statistics.
	 * @param groupMember the group member where to add the statistics.
	 * @param statProfileName a list of statistic profile names.
	 */
	private void addStatProfiles(Tag tag, ECReportGroupListMember groupMember,
			List<String> statProfileName, String reportType) {

		ECReportGroupListMemberExtension extension = 
			new ECReportGroupListMemberExtension();
		groupMember.setExtension(extension);
		
		extension.setStats(new Stats());
		List<ECTagStat> ecTagStats = extension.getStats().getStat();
		for (String profile : statProfileName) {
			if(profile.equalsIgnoreCase("TagTimestamps")) {
				ECTagTimestampStat ecTagTimestampStat = new ECTagTimestampStat();
				ecTagStats.add(ecTagTimestampStat);
				
				ecTagTimestampStat.setProfile(profile);
				ecTagTimestampStat.setStatBlocks(new StatBlocks());
				
				try {
					GregorianCalendar gregorianCalendar = new GregorianCalendar();
					gregorianCalendar.setTime(new Date(tag.getTimestamp()));
					XMLGregorianCalendar xmlGrogerianCalendar = null;
					xmlGrogerianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
					
					// TODO: firstSightingTime is dependent on ReportSetSpec
					ecTagTimestampStat.setFirstSightingTime(xmlGrogerianCalendar);					
					ecTagTimestampStat.setLastSightingTime(xmlGrogerianCalendar);
					
				} catch (DatatypeConfigurationException e) {
					LOG.debug("timestamp is not valid. timestamp inclusion skipped");
				}
				
			} else {
				LOG.debug("adding stat profile: " + profile);
				
				ECTagStat ecTagStat = new ECTagStat();
				ecTagStats.add(ecTagStat);
				
				ecTagStat.setProfile(profile);
				ecTagStat.setStatBlocks(new StatBlocks());
				ECReaderStat readerStat = new ECReaderStat();
				ecTagStat.getStatBlocks().getStatBlock().add(readerStat);
				
				readerStat.setReaderName(tag.getOrigin());
				readerStat.setSightings(new Sightings());
				readerStat.getSightings().getSighting().add(new ECSightingStat());
			}
			
		}
	}
	
	/**
	 * ORANGE: Gets the value of the user memory and added to the generated report.
	 *
	 * @param tag the tag holding information of user memory.
	 * @param groupMember the group member where to add the user memory.
	 * @param fieldName the field name "UserMemory".
	 */
	private void addUserMemoryToReport (Tag tag, ECReportGroupListMember groupMember, String fieldName) {
		ECReportGroupListMemberExtension extension = new ECReportGroupListMemberExtension();
		groupMember.setExtension(extension);
		extension.setFieldList(new FieldList());
		List<ECReportMemberField>  ecReportMemberFields = extension.getFieldList().getField();
		ECReportMemberField ecReportMemberField = new ECReportMemberField();
		ecReportMemberFields.add(ecReportMemberField);
		ecReportMemberField.setName(fieldName);
		ecReportMemberField.setValue(tag.getUserMemory());
	}

	

	/**
	 * This method get the matching group of this report for the specified tag.
	 * 
	 * @param tagURI to search group for
	 * @return group name
	 * @throws ECSpecValidationException if the tag is invalid
	 * @throws ImplementationException if an implementation exception occurs
	 */
	private String getGroupName(String tagURI) throws ImplementationException, ECSpecValidationException {
		
		for (Pattern pattern : groupPatterns) {
			if (pattern.isMember(tagURI)) {
				return pattern.getGroupName(tagURI);
			}
		}
			
		return null;
		
	}
	
	/**
	 * This method indicates if the report contains any tags.
	 * 
	 * @return true if the report is empty and false otherwise
	 */
	private boolean isEmpty() {
		
		List<ECReportGroup> groups = report.getGroup();
		if (groups != null) {
			for (ECReportGroup group : groups) {
				ECReportGroupList groupList = group.getGroupList();
				if (groupList.getMember().size() > 0) {
					return false;
				}
			}
		}
		return true;

	}
}