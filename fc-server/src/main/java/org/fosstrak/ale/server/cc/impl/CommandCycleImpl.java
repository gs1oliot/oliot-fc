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

package org.fosstrak.ale.server.cc.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.log4j.Logger;
import org.fosstrak.ale.exception.CCSpecValidationException;
import org.fosstrak.ale.exception.ImplementationException;
import org.fosstrak.ale.server.ALEApplicationContext;
import org.fosstrak.ale.server.Tag;
import org.fosstrak.ale.server.cc.CommandCycle;
import org.fosstrak.ale.server.cc.Report;
import org.fosstrak.ale.server.cc.ReportsGenerator;
import org.fosstrak.ale.server.readers.LogicalReader;
import org.fosstrak.ale.server.readers.LogicalReaderManager;
import org.fosstrak.ale.util.ECTimeUnit;
import org.fosstrak.ale.xsd.ale.epcglobal.CCFilterSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.CCReports;
import org.fosstrak.ale.xsd.ale.epcglobal.CCCmdReport;
import org.fosstrak.ale.xsd.ale.epcglobal.CCOpReport;
import org.fosstrak.ale.xsd.ale.epcglobal.CCReports.CmdReports;
import org.fosstrak.ale.xsd.ale.epcglobal.CCTagReport;
import org.fosstrak.ale.xsd.ale.epcglobal.CCCmdSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.CCOpSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.CCSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECFilterListMember;
import org.fosstrak.ale.xsd.ale.epcglobal.ECTime;
import org.fosstrak.alecc.util.CCTerminationCondition;
import kr.ac.kaist.resl.ltk.generated.parameters.AccessSpecID;


/**
 * default implementation of the command cycle.
 * 
 * @author regli
 * @author swieland
 * @author benoit.plomion@orange.com
 * @author nkef@ait.edu.gr
 * @author Wondeuk Yoon
 */
public final class CommandCycleImpl implements CommandCycle, Runnable {

	/** logger. */
	private static final Logger LOG = Logger.getLogger(CommandCycleImpl.class);

	/** random numbers generator. */
	private static final Random rand = new Random(System.currentTimeMillis());
	
	/** ale id. */
	private static final String ALEID = "ETHZ-ALE" + rand.nextInt();
	
	/** number of this command cycle. */
	private static int number = 0;
	
	/** name of this command cycle. */
	private final String name;
	
	/** report generator which contains this command cycle. */
	private final ReportsGenerator generator;
	
	/** thread. */
	private final Thread thread;
	
	/** command cycle specification for this command cycle. */
	private final CCSpec ccspec;
	
	/** set of logical readers which deliver tags for this command cycle. */
	private final Set<LogicalReader> logicalReaders = new HashSet<LogicalReader>();
	
	/** set of reports for this command cycle. */
	private final Set<Report> reports = new HashSet<Report>();
	
	/** a hash map with all the reports generated in the last round. */
	private final Map<String, CCReports> lastCCReports = new HashMap<String, CCReports> ();
	
	/** contains all the cc cmd specs hashed by their report name. */
	private final Map<String, CCCmdSpec> CmdSpecByName = new HashMap<String, CCCmdSpec> ();
	
	/** set of tags for this command cycle. */
	private  Set<Tag> tags = Collections.synchronizedSet(new HashSet<Tag>());
	
	/** this set stores the tags from the previous CommandCycle run. */
	private Set<Tag> lastCommandCycleTags = null;
	
	/** this set stores the tags between two command cycle in the case of rejectTagsBetweenCycle is false */
	private Set<Tag> betweenCommandsCycleTags =  Collections.synchronizedSet(new HashSet<Tag>());	

	/** flags to know if the command cycle haven t to reject tags in the case than duration and repeatPeriod is same */
	private boolean rejectTagsBetweenCycle = true;

	/** indicates if this command cycle is terminated or not .*/
	private boolean isTerminated = false;
	
	/** 
	 * lock for thread synchronization between reports generator and this.
	 * swieland 2012-09-29: do not use primitive type as int or Integer as autoboxing can result in new thread object for the lock -> non-threadsafe... 
	 */
	private final CommandCycleLock lock = new CommandCycleLock();
	
	/** flag whether the command cycle has passed through or not. */ 
	private boolean roundOver = false;
	
	/** the duration of collecting tags for this command cycle in milliseconds. */
	private long durationValue;
	
	/** the total time this command cycle runs in milliseconds. */
	private long totalTime;
	
	/** the termination condition of this command cycle. */
	private String terminationCondition = null;

	/** flags the commandCycle whether it shall run several times or not.	 */
	private boolean running = false;
	
	/** flags whether the CommandCycle is currently not accepting tags. */
	private boolean acceptTags = false;
	
	/** tells how many times this CommandCycle has been scheduled. */
	private int rounds = 0;
	
	/** number of this command cycle. */
	private int accessSpecID = 0;
	
	// TODO: check if we can use this instead of the dummy class.
	private final class CommandCycleLock {
		
	}

	/**
	 * Constructor sets parameter and starts thread.
	 * 
	 * @param generator to which this command cycle belongs to
	 * @throws ImplementationException if an implementation exception occurs
	 */
	public CommandCycleImpl(ReportsGenerator generator) throws ImplementationException {
		this(generator, ALEApplicationContext.getBean(LogicalReaderManager.class));
	}
	
	/**
	 * Constructor sets parameter and starts thread.
	 * 
	 * @param generator to which this command cycle belongs to
	 * @throws ImplementationException if an implementation exception occurs
	 */
	public CommandCycleImpl(ReportsGenerator generator, LogicalReaderManager logicalReaderManager) throws ImplementationException {	
		
		// set accessSpecID
		accessSpecID = number+11;
		
		// set name
		name = generator.getName() + "_" + number++;
		
		// set ReportGenerator
		this.generator = generator;
		
		// set spec
		ccspec = generator.getCCSpec();
		
		// get cmd specs and create a report for each spec
		for (CCCmdSpec cccmdSpec : ccspec.getCmdSpecs().getCmdSpec()) {
			
			// add report spec and report to reports
			try {
				reports.add(new Report(cccmdSpec, this, this.generator));
			} catch (CCSpecValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// hash into the report spec structure
			CmdSpecByName.put(cccmdSpec.getName(), cccmdSpec);
			
		}
		
		// init BoundarySpec values
		durationValue = getDurationValue();
		
		long repeatPeriod = getRepeatPeriodValue();
		if (durationValue == repeatPeriod) {
			setRejectTagsBetweenCycle(false);
		}
		
		LOG.debug(String.format("durationValue: %s\n",
				durationValue));
		
		setAcceptTags(false);
		
		LOG.debug("adding logicalReaders to CommandCycle");
		// get LogicalReaderStubs
		if (ccspec.getLogicalReaders() != null) {
			List<String> logicalReaderNames = ccspec.getLogicalReaders().getLogicalReader();
			for (String logicalReaderName : logicalReaderNames) {
				LOG.debug("retrieving logicalReader " + logicalReaderName);
				LogicalReader logicalReader = logicalReaderManager.getLogicalReader(logicalReaderName);
				
				if (logicalReader != null) {
					LOG.debug("adding logicalReader " +	logicalReader.getName() + " to CommandCycle " + name);
					logicalReaders.add(logicalReader);
				}
			}
		} else {
			LOG.error("CCSpec contains no readers");
		}
		
		for (LogicalReader logicalReader : logicalReaders) {

			// Start the reader
			if (!logicalReader.isStarted()) {
				logicalReader.start();
			}
			
			// subscribe this command cycle to the logical readers
			LOG.debug("registering CommandCycle " + name + " on reader " + logicalReader.getName());
			logicalReader.addObserver(this);

		}		
		
		rounds = 0;
		
		// create and start Thread
		thread = new Thread(this, "CommandCycle" + name);
		thread.setDaemon(true);
		thread.start();
		
		LOG.debug("New CommandCycle  '" + name + "' created.");
	}
	
	/**
	 * This method returns the cc reports.
	 * 
	 * @return cc reports
	 * @throws CCSpecValidationException if the tags of the report are not valid
	 * @throws ImplementationException if an implementation exception occurs.
	 */
	private CCReports getCCReports() throws CCSpecValidationException, ImplementationException {
		
		// create CCReports
		CCReports reports = new CCReports();

		// set spec name
		reports.setSpecName(generator.getName());
		
		// set date
		try {
			reports.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
		} catch (DatatypeConfigurationException e) {
			LOG.error("Could not create date: " + e.getMessage());
		}

		// set ale id
		reports.setALEID(ALEID);
		
		// set total time in milliseconds
		reports.setTotalMilliseconds(totalTime);
		
		// set termination condition
		reports.setTerminationCondition(terminationCondition);
		
		//TODO: This code for preemption.
/*		for(Tag t : tags) {
			AccessSpecID AccessSpecID = t.getAccessSpecID();
		}*/

		// set spec
		if (ccspec.isIncludeSpecInReports()) {
			reports.setCCSpec(ccspec);
		}
		//TODO: �대뼡 Spec���곕씪��AccessID瑜�李얘퀬 �ｌ쓬
		
		// set reports
		reports.setCmdReports(new CmdReports());
		reports.getCmdReports().getCmdReport().addAll(getCmdReportList());
		
		return reports;
	}

	@Override
	public void addTag(Tag tag) {
		
		if (!isAcceptingTags()) {
			return;
		}
		
		// add command only if CommandCycle is still running
		if (isCommandCycleActive()) {
			logTagOnDebugEnabled(tag);
			
			// add tag to tags
			addTagAndLogOnNotAdded(tags, tag);
		}
	}

	/**
	 * This method adds a tag between 2 command cycle.
	 * 
	 * @param tag to add
	 * @throws ImplementationException if an implementation exception occurs
	 * @throws CCSpecValidationException if the tag is not valid
	 */
	private void addTagBetweenCommandsCycle(Tag tag) {
		
		if (isRejectTagsBetweenCycle()) {
			return;
		}
		
		// add command only if CommandCycle is still running
		if (isCommandCycleActive()) {
			logTagOnDebugEnabled(tag);
			
			// add tag to tags
			addTagAndLogOnNotAdded(betweenCommandsCycleTags, tag);			
		}
	}
	
	/**
	 * determine if this command cycle is active (running) or not.
	 * @return true if the command cycle is active, false if not.
	 */
	private boolean isCommandCycleActive() {
		return thread.isAlive();
	}

	/**
	 * log a tag to the logger if debug is enabled. the command cycles name and the tag as pure URI is written to the log on one line.
	 * @param tagToLog the tag to be logged.
	 */
	private void logTagOnDebugEnabled(Tag tagToLog) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("CommandCycle '" + name + "' add Tag '" + tagToLog.getTagIDAsPureURI() + "'."); 
		}
	}

	/**
	 * little helper method adding a tag to a given set. if the tag is not added (as already contained) log it.
	 * @param whereToAddTheTag the set where to add the tag to.
	 * @param theTagToAdd the tag which is meant to be added.
	 */
	private void addTagAndLogOnNotAdded(Set<Tag> whereToAddTheTag, Tag theTagToAdd) {
		if (!whereToAddTheTag.add(theTagToAdd) && LOG.isDebugEnabled()) {
			LOG.debug("tag already contained, therefore not adding.");
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		LOG.debug("CommandCycle "+ getName() + ": Update notification received. ");
		List<Tag> tags = new LinkedList<Tag> ();
		// process the new tag.
		if (arg instanceof Tag) {
			LOG.debug("processing one tag");
			// process one tag
			tags.add((Tag) arg);
		} else if (arg instanceof List) {
			LOG.debug("processing a list of tags");
			for (Object entry : (List<?>) arg) {
				if (entry instanceof Tag) {
					tags.add((Tag) entry);					
				}
			}
		}
		
		if (tags.size() > 0) {
			handleTags(tags);
		} else {
			LOG.debug("CommandCycle "+ getName() + ": Update notification received - but not with any tags - ignoring. ");			
		}
	}
	
	private void handleTags(List<Tag> tags) {
		if (!isAcceptingTags()) {
			handleTagsWhileNotAccepting(tags);
		} else {
			handleTagsWhileAccepting(tags);
		}		
	}
	
	/**
	 * deal with new tags.
	 * @param tags
	 */
	private void handleTagsWhileAccepting(List<Tag> tags) {	
		// process all the tags we did not process between two commandcycles (or while we did not accept any tags).
		if (!isRejectTagsBetweenCycle()) {			
			for (Tag tag : betweenCommandsCycleTags) {
				addTag(tag);			
			}
			
			betweenCommandsCycleTags.clear();			
		}
		LOG.debug("CommandCycle "+ getName() + ": Received list of tags :");
		for (Tag tag : tags) {
			addTag(tag);
		}
	}

	/**
	 * deal with tags while the command cycle is not accepting tags. (eg. between two command cycles).
	 * @param arg the update we received.
	 */
	private void handleTagsWhileNotAccepting(List<Tag> tags) {
		if (!isRejectTagsBetweenCycle()) {
			for (Tag tag : tags) {	
				LOG.debug("received tag between commandcycles: " + tag.getTagIDAsPureURI());					
				addTagBetweenCommandsCycle(tag);	
			}
		}
	}

	@Override
	public void stop() {
		// unsubscribe this command cycle from logical readers
		for (LogicalReader logicalReader : logicalReaders) {
			logicalReader.deleteObserver(this);
		}

		running = false;
		thread.interrupt();
		LOG.debug("CommandCycle '" + name + "' stopped.");
		
		isTerminated = true;
		
		synchronized (this) {
			this.notifyAll();
		}		
	}

	@Override
	public String getName() {
		return name;	
	}

	@Override
	public boolean isTerminated() {
		return isTerminated;
	}
	
	/**
	 * This method is the main loop of the command cycle in which the tags will be collected.
	 * At the end the reports will be generated and the subscribers will be notified.
	 */
	@Override
	public void run() {
		
		lastCommandCycleTags = new HashSet<Tag>();
		
		// wait for the start
		// running will be set by the ReportsGenerator when the CommandCycle
		// has a subscriber
		if (!running) {
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					LOG.info("commandcycle got interrupted");
					return;
				}
			}
		}
		
		while (running) {

			rounds ++;
			synchronized (lock) {
				roundOver = false;
			}
			LOG.info("CommandCycle "+ getName() + ": Starting (Round " + rounds + ").");
			
			// set start time
			long startTime = System.currentTimeMillis();
	
			// accept tags
			setAcceptTags(true);
			
			//------------------------------ run for the specified time
			try {
				
				if (durationValue > 0) {
					
					// if durationValue is specified and larger than zero, 
					// wait for notify or durationValue elapsed.
					synchronized (this) {
						long dt = (System.currentTimeMillis() - startTime);
						this.wait(Math.max(1, durationValue - dt));
						terminationCondition = CCTerminationCondition.DURATION;
					}
				} else {
					
					// if durationValue is not specified or smaller than zero, 
					// wait for notify.
					synchronized (this) {
						this.wait();
					}
				}
			
			} catch (InterruptedException e) {
				
				// if Thread is stopped with method stop(), 
				// then return without notify subscribers.
				LOG.info("commandcycle got interrupted");
				return;
			}
			
			// don't accept tags anymore
			setAcceptTags(false);
			//------------------------ generate the reports
			
			// get reports
			try {
				// compute total time
				totalTime = System.currentTimeMillis() - startTime;
				
				LOG.info("CommandCycle "+ getName() + 
						": Number of Tags read in the current CommandCyle.java: " 
						+ tags.size());
							
				CCReports ccReports = getCCReports();

				for (LogicalReader logicalReader : logicalReaders) {
					logicalReader.DELETEACCESSSPEC();
					logicalReader.recoveryACCESSSPEC3();
				}
				
				// notifySubscribers
				generator.notifySubscribers(ccReports, this);
				
				// store the current tags into the old tags
				// explicitly clear the tags
				if (lastCommandCycleTags != null) {
					lastCommandCycleTags.clear();
				}
				if (null != tags) {
					lastCommandCycleTags.addAll(tags);
				}
				
				tags = Collections.synchronizedSet(new HashSet<Tag>());
				
			} catch (Exception e) {
				if (e instanceof InterruptedException) {
					LOG.info("commandcycle got interrupted");
					return;
				}
				LOG.error("CommandCycle "+ getName() + ": Could not create CCReports", e);
			}
			
			LOG.info("CommandCycle "+ getName() +  ": CommandCycle finished (Round " + rounds + ").");
			
			try {
				// inform possibly waiting workers about the finish
				synchronized (lock) {
					roundOver = true;
					lock.notifyAll();
				}
				// wait until reschedule.
				synchronized (this) {	
					this.wait();
				}
				LOG.debug("commandcycle continues");
			} catch (InterruptedException e) {
				LOG.info("commandcycle got interrupted");
				
				return;
			}			
		}
		
			
		// stop CommandCycle
		stop();
		
	}
	
	@Override
	public void launch() {
		for (LogicalReader logicalReader : logicalReaders) {
			// subscribe this command cycle to the logical readers
			logicalReader.ADDACCESSSPECfromCCSpec(ccspec, this.generator.getOpSpecTable());
			LOG.debug("registering CommandCycle " + name + " on reader " + logicalReader.getName());	
		}
		this.running = true;
		LOG.debug("launching commandCycle" + getName());
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	/**
	 * This method returns all reports of this command cycle as command cycle 
	 * reports. 
	 * @return array of cmd report
	 * @throws CCSpecValidationException if a tag of this report is not valid
	 * @throws ImplementationException if an implementation exception occurs.
	 */
	private List<CCCmdReport> getCmdReportList() throws CCSpecValidationException, ImplementationException {
		ArrayList<CCCmdReport> ccCmdReports = new ArrayList<CCCmdReport>();
		for (Report report : reports) {
			CCCmdReport r = report.getCCCmdReport();
			if (null != r) ccCmdReports.add(r);
		}
		return ccCmdReports;
	}
	
	/**
	 * This method returns the duration value extracted from the command cycle 
	 * specification. 
	 * @return duration value in milliseconds
	 * @throws ImplementationException if an implementation exception occurs
	 */
	private long getDurationValue() throws ImplementationException {
		if (ccspec.getBoundarySpec() != null) {
			ECTime duration = ccspec.getBoundarySpec().getDuration();
			if (duration != null) {
				if (duration.getUnit().compareToIgnoreCase(ECTimeUnit.MS) == 0) {
					return duration.getValue();
				} else {
					throw new ImplementationException(
							"The only ECTimeUnit allowed is milliseconds (MS).");
				}
			}
		}
		return -1;		
	}
	
	/**	
	 * This method returns the repeat period value on the basis of the command 
	 * cycle specification.
	 * @return repeat period value
	 * @throws ImplementationException if the time unit in use is unknown
	 */
	private long getRepeatPeriodValue() throws ImplementationException {
		if (ccspec.getBoundarySpec() != null) {		
			ECTime repeatPeriod = ccspec.getBoundarySpec().getRepeatPeriod();
			if (repeatPeriod != null) {
				if (repeatPeriod.getUnit().compareToIgnoreCase(ECTimeUnit.MS) != 0) {
					throw new ImplementationException(
							"The only ECTimeUnit allowed is milliseconds (MS).");
				} else {
					return repeatPeriod.getValue();
				}
			}
		}
		return -1;
	}

	@Override
	public Set<Tag> getLastCommandCycleTags() {
		return copyContentToNewDatastructure(lastCommandCycleTags);
	}

	@Override
	public Set<Tag> getTags() {
		return copyContentToNewDatastructure(tags);		
	}
	
	/**
	 * create a copy of the content of the given data structure -> we use synchronized sets -> make sure not to leak them.<br/>
	 * this method synchronizes the original data structure during to copy process.
	 * <br/>
	 * <strong>Notice that the content is NOT cloned, simply referenced!</strong>
	 * 
	 * @param contentToCopy the data structure to copy.
	 * @return a copy of the data structure with the content of the input.
	 */
	private Set<Tag> copyContentToNewDatastructure(Set<Tag> contentToCopy) {
		Set<Tag> copy = new HashSet<Tag> ();
		synchronized (contentToCopy) {
			for (Tag tag : contentToCopy) {
				copy.add(tag);
			}
		}
		return copy;
	}
	
	private boolean isRejectTagsBetweenCycle() {
		return rejectTagsBetweenCycle;
	}

	private void setRejectTagsBetweenCycle(boolean rejectTagsBetweenCycle) {
		this.rejectTagsBetweenCycle = rejectTagsBetweenCycle;
	}

	/** 
	 * tells whether the cc accepts tags.
	 * @return boolean telling whether the cc accepts tags
	 */
	private boolean isAcceptingTags() {
		return acceptTags;
	}

	/**
	 * sets the flag acceptTags to the passed boolean value. 
	 * @param acceptTags sets the flag acceptTags to the passed boolean value.
	 */
	private void setAcceptTags(boolean acceptTags) {
		this.acceptTags = acceptTags;
	}

	@Override
	public int getRounds() {
		return rounds;
	}

	@Override
	public void join() throws InterruptedException {
		synchronized (lock) {
			while (!isRoundOver()) {
				lock.wait();
			}
		}
	}

	/**
	 * whether the command cycle round is over.
	 * <strong>notice that this method is not exported via interface</strong>.
	 * @return true if over, false otherwise
	 */
	public boolean isRoundOver() {
		return roundOver;
	}

	// FIXME: Implementation is currently leaking... need to do something.
	@Override
	public CCCmdSpec getCmdReportSpecByName(String name) {
		return CmdSpecByName.get(name);
	}
	
	/**
	 * @return the lastReports
	 */

	
	/**
	 * get a handle onto the map holding all the report specs.
	 * @return the map.
	 */
	protected Map<String, CCCmdSpec> getCmdReportSpecByName() {
		return CmdSpecByName;
	}

	// FIXME: Implementation is currently leaking... need to do something.
	@Override
	public Map<String, CCReports> getLastCCReports() {
		return lastCCReports;
	}



}