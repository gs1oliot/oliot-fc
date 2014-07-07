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
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.fosstrak.ale.exception.CCSpecValidationException;
import org.fosstrak.ale.exception.DuplicateSubscriptionException;
import org.fosstrak.ale.exception.ImplementationException;
import org.fosstrak.ale.exception.InvalidURIException;
import org.fosstrak.ale.exception.NoSuchSubscriberException;
import org.fosstrak.ale.server.cc.CommandCycle;
import org.fosstrak.ale.server.cc.ReportsGenerator;
import org.fosstrak.ale.server.cc.ReportsGeneratorState;
import org.fosstrak.ale.server.cc.Subscriber;
import org.fosstrak.ale.server.cc.impl.CommandCycleImpl;
import org.fosstrak.ale.server.impl.ReportsGeneratorImpl.NotificationThread;
import org.fosstrak.ale.server.util.CCSpecValidator;
import org.fosstrak.ale.util.ECTimeUnit;
import org.fosstrak.ale.xsd.ale.epcglobal.CCBoundarySpec.StartTriggerList;
import org.fosstrak.ale.xsd.ale.epcglobal.CCBoundarySpec.StopTriggerList;
import org.fosstrak.ale.xsd.ale.epcglobal.CCCmdReport;
import org.fosstrak.ale.xsd.ale.epcglobal.CCCmdReport.TagReports;
import org.fosstrak.ale.xsd.ale.epcglobal.CCCmdSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.CCCmdSpec.OpSpecs;
import org.fosstrak.ale.xsd.ale.epcglobal.CCOpSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.CCReports;
import org.fosstrak.ale.xsd.ale.epcglobal.CCSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ECReports;
import org.fosstrak.ale.xsd.ale.epcglobal.CCSpec.CmdSpecs;
import org.fosstrak.ale.xsd.ale.epcglobal.CCTagReport;
import org.fosstrak.ale.xsd.ale.epcglobal.ECTime;

import com.rits.cloning.Cloner;

/**
 * default implementation of the reports generator.
 * @author swieland
 * @author Wondeuk Yoon
 *
 */
public class ReportsGeneratorImpl implements ReportsGenerator, Runnable {

	/**
	 * a negative interval means that no such interval is set.
	 */
	private static final long INTERVAL_NOT_SET = -1L;

	/**
	 * period for how long to wait for calls in between waiting times. 
	 */
	private static final long WAKEUP_PERIOD = 50L;

	/** logger */
	private static final Logger LOG = Logger.getLogger(ReportsGenerator.class);
	
	/** name of the report generator */
	private final String name;
	
	/** cc specification which defines how the report should be generated */
	private final CCSpec ccspec;
	
	/** map of subscribers of this report generator */
	private final Map<String, Subscriber> subscribers = new ConcurrentHashMap<String, Subscriber>();
	
	// boundary spec values
	
	/** start trigger */
	private final StartTriggerList startTriggerValue;
	
	/** stop trigger */
	private final StopTriggerList stopTriggerValue;
	
	/** time between one and the following command cycle in milliseconds */
	private final long repeatPeriodValue;
	
	/**
	 * The stable set interval in milliseconds. If there are no new tags 
	 * detected for this time, the reports generation should stop.
	 */
	private final long stableSetInterval;
	
	/** thread to run the main loop */
	private Thread thread;
	
	/** state of this report generator */
	private ReportsGeneratorState state = ReportsGeneratorState.UNREQUESTED;
	
	/** indicates if this report generator is running or not */
	private boolean reportsGeneratorRunning = false;

	/** indicates if somebody is polling this input generator at the moment. */
	private boolean polling = false;
	
	private CCReports pollReport = null;
	
	private CommandCycle commandCycle = null;
	
	private int AccessSpecNumber = 0;
	
	private Hashtable<Integer, CCOpSpec> OpSpecTable = new Hashtable<Integer, CCOpSpec>();

	private int OpSpecCount;
	
	/**
	 * Constructor validates the cc specification and sets some parameters.
	 * 
	 * @param name of this reports generator
	 * @param spec which defines how the reports of this generator should be build
	 * @throws ECSpecValidationException if the cc specification is invalid
	 * @throws ImplementationException if an implementation exception occurs
	 
	public ReportsGeneratorImpl(String name, CCSpec spec) throws CCSpecValidationException, ImplementationException {
		this(name, spec, ALEApplicationContext.getBean(ECSpecValidator.class), ALEApplicationContext.getBean(ECReportsHelper.class));
		
	}*/
	
	/**
	 * Constructor validates the cc specification and sets some parameters.
	 * 
	 * @param name of this reports generator
	 * @param spec which defines how the reports of this generator should be build
	 * @param validator the CCSpec validator to use for the validation of the CCSpec.
	 * @throws ECSpecValidationException if the cc specification is invalid
	 * @throws ImplementationException if an implementation exception occurs
	 */
	public ReportsGeneratorImpl(String name, CCSpec spec, CCSpecValidator validator/*, ECReportsHelper reportsHelper*/) throws CCSpecValidationException, ImplementationException {

		LOG.debug("Try to create new ReportGenerator '" + name + "'.");
		
		AccessSpecNumber = org.fosstrak.ale.server.cc.impl.ALECCImpl.getAccessSpecCounter();
		org.fosstrak.ale.server.cc.impl.ALECCImpl.setAccessSpecCounter(AccessSpecNumber+1);
	
		// set name
		this.name = name;
		
		//this.reportsHelper = reportsHelper;
		
		// set spec
		try {
			validator.validateSpec(spec);
		} catch (CCSpecValidationException e) {
			LOG.error(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
			throw e;
		} catch (ImplementationException e) {
			LOG.error(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
			throw e;
		}
		
		//TODO : Validator. wdyoon
		this.ccspec = spec;
		
		// init boundary spec values
		startTriggerValue = getCCStartTriggerValue();
		stopTriggerValue = getCCStopTriggerValue();
		repeatPeriodValue = getRepeatPeriodValue();
		stableSetInterval = getStableSetInterval();
		
		//Parsing OPSpec and register opname in to OPSpecTable
		OpSpecCount = 1000;
		List<CCCmdSpec> cmdspecs = ccspec.getCmdSpecs().getCmdSpec();
		for (CCCmdSpec cccmdspec : cmdspecs)
		{
			List<CCOpSpec> opspecs = cccmdspec.getOpSpecs().getOpSpec();
			for (CCOpSpec ccopspec : opspecs)
			{
				OpSpecTable.put(OpSpecCount, ccopspec);
				OpSpecCount++;
			}
		}
		
		LOG.debug(String.format("[startCCTriggerValue: %s, stopCCTriggerValue: %s, repeatPeriodValue: %s, stableSetInterval: %s]",
				startTriggerValue, stopTriggerValue, repeatPeriodValue, stableSetInterval));
		LOG.debug("ReportGenerator '" + name + "' successfully created.");
	}
	
	@Override
	public Hashtable<Integer, CCOpSpec> getOpSpecTable() {
		return OpSpecTable;
	}

	/**
	 * This method returns the cc specification of this generator.
	 * 
	 * @return cc specification
	 */
	@Override
	public CCSpec getCCSpec() {
		// TODO Auto-generated method stub
		return ccspec;
	}
	
	/**
	 * This method sets the state of this report generator.
	 * If the state changes from UNREQUESTED to REQUESTED, the report generators 
	 * main loop will be started.
	 * If the state changes from REQUESTED to UNREQUESTED, the report generators 
	 * main loop will be stopped.
	 * <strong>please notice that this method is not available through the ReportsGeneratorInterface.</strong>
	 * 
	 * @param state to set
	 */
	public void setState(ReportsGeneratorState state) {		
		ReportsGeneratorState oldState = this.state;
		this.state = state;
		
		LOG.debug("ReportGenerator '" + name + "' change state from '" + oldState + "' to '" + state + "'");
		
		if (isStateRequested() && !isRunning()) {
			start();
		} else if (isStateUnRequested() && isRunning()) {
			stop();
		}
	}
	
	/** 
	 * This method returns the state of this report generator.
	 * <strong>please notice that this method is not available through the ReportsGeneratorInterface.</strong>
	 * 
	 * @return state the state of the generator.
	 */
	public ReportsGeneratorState getState() {
		return state;
	}
	
	/**
	 * This method subscribes a notification uri of a subscriber to this 
	 * report generator. 
	 * @param notificationURI to subscribe
	 * @throws DuplicateSubscriptionException if the specified notification uri 
	 * is already subscribed
	 * @throws InvalidURIException if the notification uri is invalid
	 */
	@Override
	public void subscribe(String notificationURI) throws DuplicateSubscriptionException, InvalidURIException {
		
		Subscriber uri = new Subscriber(notificationURI);
		if (subscribers.containsKey(notificationURI)) {
			throw new DuplicateSubscriptionException(String.format("the URI is already subscribed on this specification %s, %s", name, uri));
		} else {
			subscribers.put(notificationURI, uri);
			LOG.debug("NotificationURI '" + notificationURI + "' subscribed to spec '" + name + "'.");
			if (isStateUnRequested()) {
				setState(ReportsGeneratorState.REQUESTED);
			}
		}
	}
	
	/**
	 * This method unsubscribes a notification uri of a subscriber from this 
	 * report generator.
	 * @param notificationURI to unsubscribe
	 * @throws NoSuchSubscriberException if the specified notification uri is 
	 * not yet subscribed
	 * @throws InvalidURIException if the notification uri is invalid
	 */
	@Override
	public void unsubscribe(String notificationURI) throws NoSuchSubscriberException, InvalidURIException {
		// validate the URI:
		new Subscriber(notificationURI);
		
		if (subscribers.containsKey(notificationURI)) {
			subscribers.remove(notificationURI);
			LOG.debug("NotificationURI '" + notificationURI	+ "' unsubscribed from spec '" + name + "'.");
			
			if (subscribers.isEmpty() && !isPolling()) {
				setState(ReportsGeneratorState.UNREQUESTED);
			}
		} else {
			throw new NoSuchSubscriberException("there is no subscriber on the given notification URI: " + notificationURI);
		}
	}
	
	/**
	 * This method return the notification uris of all the subscribers of this 
	 * report generator.
	 * @return list of notification uris
	 */
	@Override
	public List<String> getSubscribers() {
		return new ArrayList<String>(subscribers.keySet());
	}
	
	/**
	 * This method notifies all subscribers of this report generator about the 
	 * specified cc reports.
	 * @param reports to notify the subscribers about
	 */
	@Override
	public void notifySubscribers(CCReports reports, CommandCycle cc) {		
		
//		according the ALE 1.1 standard:
//		When the processing of reportIfEmpty and reportOnlyOnChange
//		results in all CCReport instances being omitted from an 
//		CCReports for an command cycle, then the delivery of results
//		to subscribers SHALL be suppressed altogether. [...] poll 
//		and immediate SHALL always be returned [...] even if that 
//		CCReports instance contains zero CCReport instances.
		
//		An CCReports instance SHALL include an CCReport instance corresponding to each
//		CCReportSpec in the governing CCSpec, in the same order specified in the CCSpec,
//		except that an CCReport instance SHALL be omitted under the following circumstances:
//		- If an CCReportSpec has reportIfEmpty set to false, then the corresponding
//		  CCReport instance SHALL be omitted from the CCReports for this evcommandycle if
//		  the final, filtered set of Tags is empty (i.e., if the final Tag list would be empty, or if
//		  the final count would be zero).
//		- If an ECReportSpec has reportOnlyOnChange set to true, then the
//		  corresponding ECReport instance SHALL be omitted from the ECReports for
//		  this command cycle if the filtered set of Tags is identical to the filtered prior set of Tags,
//		  where equality is tested by considering the primaryKeyFields as specified in the
//		  ECSpec (see Section 8.2), and where the phrase 'the prior set of Tags' is as defined
//		  in Section 8.2.6. This comparison takes place before the filtered set has been modified
//		  based on reportSet or output parameters. The comparison also disregards
//		  whether the previous ECReports was actually sent due to the effect of this
//		  parameter, or the reportIfEmpty parameter.
//		When the processing of reportIfEmpty and reportOnlyOnChange results in all
//		ECReport instances being omitted from an ECReports for an command cycle, then the
//		delivery of results to subscribers SHALL be suppressed altogether. That is, a result
//		consisting of an ECReports having zero contained ECReport instances SHALL NOT
//		be sent to a subscriber. (Because an ECSpec must contain at least one
//		ECReportSpec, this can only arise as a result of reportIfEmpty or
//		reportOnlyOnChange processing.) This rule only applies to subscribers (command cycle
//		requestors that were registered by use of the subscribe method); an ECReports
//		instance SHALL always be returned to the caller of immediate or poll at the end of
//		an command cycle, even if that ECReports instance contains zero ECReport instances.
		
		
		Cloner cloner  = new Cloner();
		// deep clone the original input in order to keep it as the 
		// next command cycles last cycle reports. 
		CCReports originalInput = cloner.deepClone(reports);
		
		// we deep clone (clone not sufficient) for the pollers
		// in order to deliver them the correct set of reports.
		if (isPolling()) {
			// deep clone for the pollers (poll and immediate)
			pollReport = cloner.deepClone(reports);
		}

		// we remove the reports that are equal to the ones in the 
		// last command cycle. then we send the subscribers.
		List<CCCmdReport> equalReps = new LinkedList<CCCmdReport> ();
		List<CCCmdReport> reportsToNotify = new LinkedList<CCCmdReport> ();
		try {
			for (CCCmdReport r : reports.getCmdReports().getCmdReport()) {
				final CCCmdSpec reportSpec = cc.getCmdReportSpecByName(r.getCmdSpecName());
				
				boolean tagsInReport = hasTags(r);
				// case no tags in report but report if empty
				if (!tagsInReport && reportSpec.isReportIfEmpty()) {
					LOG.debug("requesting empty for report: " + r.getCmdSpecName());
					reportsToNotify.add(r);
				} else if (tagsInReport) {
					reportsToNotify.add(r);
				}
				// check for equal reports since last notification.
				/*if (reportSpec.isReportOnlyOnChange()) {
					// report from the previous CommandCycle run.
					ECReport oldR = cc.getLastReports().get(r.getReportName());
					
					// compare the new report with the old one.
					if (reportsHelper.areReportsEqual(reportSpec, r, oldR)) {
						equalReps.add(r);
					}
				}*/
				//TODO: wdyoon
			}
		} catch (Exception e) {
			LOG.error("caught exception while processing reports: ", e);
		}
		
		// check if the intersection of all reports to notify (including empty ones) and the equal ones is empty
		// -> if so, do not notify at all.
		//reportsToNotify.removeAll(equalReps);
		
		// remove the equal reports
		/*Reports re = reports.getCmdReports();
		if (null != re) re.getReport().removeAll(equalReps);
		LOG.debug("reports size: " + reports.getCmdReports().getCmdReport().size());
		*/
		// next step is to check, if the total report is empty (even if requestIfEmpty but when all reports are equal, do not deliver) 
		
		// notify the ECReports
		notifySubscribersWithFilteredReports(reports);

		// store the new reports as old reports
		/*ec.getLastReports().clear();
		if (null != originalInput.getCmdReports()) {
			for (ECReport r : originalInput.getCmdReports().getCmdReport()) {
				ec.getLastReports().put(r.getReportName(), r);
			}
		}*/
		//TODO: wdyoon
		
		// notify pollers
		// pollers always receive reports (even when empty).
		if (isPolling()) {
			polling = false;
			if (subscribers.isEmpty()) {
				setState(ReportsGeneratorState.UNREQUESTED);
			}
			synchronized (this) {
				this.notifyAll();
			}
		}	
	}
	
	/**
	 * check if a given CCCmdReport contains at least one tag in its data structures.
	 * @param r the report to check.
	 * @return true if tags contained, false otherwise.
	 */
	private boolean hasTags(CCCmdReport r) {
		int count = 0;
		try {
			for (CCTagReport g : r.getTagReports().getTagReport()) {
				if (g.getId().isEmpty() == false)
					count++;
			}
			if (count > 0)
				return true;
		} catch (Exception ex) {
			LOG.debug("could not check for tag occurence - report considered not to containing tags", ex);
		}
		return false;
	}

	/**
	 * once all the filtering is done eventually notify the subscribers with the reports.
	 * @param reports the filtered reports.
	 */
	protected void notifySubscribersWithFilteredReports(CCReports reports) {
		// notify subscribers 
		Thread threadNotify = new Thread(new NotificationThread(reports, subscribers));
		threadNotify.start();	
		
		/*for (Subscriber listener : subscribers.values()) {
			try {
				listener.notify(reports);
			} catch (Exception e) {
				LOG.error("Could not notify subscriber '" + listener.toString(), e);
			}
		}*/
	}

	/**
	 * This method is invoked if somebody polls this report generator.
	 * The result of the polling can be picked up by the method getPollReports.
	 */
	@Override
	public void poll() {
		
		LOG.debug("Spec '" + name + "' polled.");
		pollReport = null;
		polling = true;
		if (isStateUnRequested()) {
			setState(ReportsGeneratorState.REQUESTED);
		}
	}
	
	/**
	 * This method delivers the cc reports which have been generated because 
	 * of a poll.
	 * @return cc reports
	 */
	@Override
	public CCReports getPollCCReports() {
		return pollReport;
	}
	
	/**
	 * This method starts the main loop of the report generator.
	 */
	protected void start() {
		
		thread = new Thread(this, name);
		thread.setDaemon(true);
		setRunning(true);
		thread.start();
		LOG.debug("Thread of spec '" + name + "' started.");		
	}
	
	/**
	 * This method stops the main loop of the report generator.
	 */
	public void stop() {
		commandCycle.stop();
		
		// stop Thread
		setRunning(false);
		thread.interrupt();
		
		LOG.debug("Thread of spec '" + name + "' stopped.");
	}
	
	/**
	 * This method returns the name of this reports generator.
	 * 
	 * @return name of reports generator
	 */
	@Override
	public String getName() {
		return name;
		
	}
	
	/**
	 * create a new CommandCycle that can be used by this reports generator.
	 * @return the newly created command cycle.
	 * @throws ImplementationException when the command cycle cannot be created.
	 */
	protected CommandCycle createCommandCycle() throws ImplementationException {
		LOG.debug("creating new command cycle.");
		return new CommandCycleImpl(this);
	}
	
	/**
	 * This method contains the main loop of the reports generator.
	 * Here the command cycles will be generated and started.
	 */
	@Override
	public void run() {
		
		try {
			commandCycle = createCommandCycle();
		} catch (ImplementationException e) {
			LOG.error("could not create a new CommandCycle - aborting.", e);
			return;
		}
		
		if (startTriggerValue != null) {
			LOG.debug("start trigger defined - not invoking the command cycle start..");
			if (!isRepeatPeriodSet()) {
				// startTrigger is specified and repeatPeriod is not specified
				// commandCycle is started when:
				// state is REQUESTED and startTrigger is received
				
			}
		} else {
			if (isRepeatPeriodSet()) {
						
				// startTrigger is not specified and repeatPeriod is specified
				// commandCycle is started when:
				// state transitions from UNREQUESTED to REQUESTED or
				// repeatPeriod has elapsed from start of the last commandCycle and
				// in that interval the state was never UNREQUESTED
				while (isRunning()) {
					
					// wait until state is REQUESTED
					synchronized (state) {
						while (!isStateRequested()) {
							try {
								// wakeup the reports generator every once in a while
								state.wait(WAKEUP_PERIOD);
							} catch (InterruptedException e) {
								LOG.debug("caught interrupted exception - leaving reports generator.");
								return;
							}
						}
					}
					
					// while state is REQUESTED start every repeatPeriod a 
					// new CommandCycle
					while (isStateRequested()) {
						if (commandCycle == null) {
							LOG.error("coommandCycle is null");
						} else {
							commandCycle.launch();
						}

						try {
							synchronized (state) {
								state.wait(repeatPeriodValue);
							}
							// wait for the command cycle to finish...
							commandCycle.join();
							
						} catch (InterruptedException e) {
							LOG.debug("caught interrupted exception - leaving reports generator.");
							return;
						}
					}
					LOG.debug("Stopping ReportsGenerator " + getName());
					
				}
			} else {
				
				// neither startTrigger nor repeatPeriod are specified
				// commandCycle is started when:
				// state transitions from UNREQUESTED to REQUESTED or
				// immediately after the previous command cycle, if the state 
				// is still REQUESTED
				while (isRunning()) {
					
					// wait until state is REQUESTED
					while (!isStateRequested()) {
						try {
							synchronized (state) {
								state.wait();
							}
						} catch (InterruptedException e) {
							LOG.debug("caught interrupted exception - leaving reports generator.");
							return;
						}
					}
					
					// while state is REQUESTED start one CommandCycle 
					// after the other
					while (isStateRequested()) {
						commandCycle.launch();
						
						while (!commandCycle.isTerminated()) {
							try {
								synchronized (commandCycle) {
									commandCycle.wait(WAKEUP_PERIOD);
								}
							} catch (InterruptedException e) {
								LOG.debug("caught interrupted exception - leaving reports generator.");
								return;
							}
						}
					}
				}
				LOG.debug("Stopping ReportsGenerator " + getName());
			}
		}
	}
	
	@Override
	public void setStateRequested() {
		setState(ReportsGeneratorState.REQUESTED);
	}
	
	@Override
	public void setStateUnRequested() {
		setState(ReportsGeneratorState.UNREQUESTED);
	}
	
	@Override
	public boolean isStateRequested() {
		return state == ReportsGeneratorState.REQUESTED;
	}
	
	@Override
	public boolean isStateUnRequested() {
		return state == ReportsGeneratorState.UNREQUESTED;		
	}
	
	/**
	 * This method returns the repeat period value on the basis of the command 
	 * cycle specification.
	 * @return repeat period value or NO_REPEAT_PERIOD if none set.
	 * @throws ImplementationException if the time unit in use is unknown
	 */
	private long getRepeatPeriodValue() throws ImplementationException {
		
		ECTime repeatPeriod = ccspec.getBoundarySpec().getRepeatPeriod();
		if (repeatPeriod != null) {
			if (repeatPeriod.getUnit().compareToIgnoreCase(ECTimeUnit.MS) != 0) {
				throw new ImplementationException("The only ECTimeUnit allowed is milliseconds (MS).");
			} else {
				return repeatPeriod.getValue();
			}
		}
		return INTERVAL_NOT_SET;		
	}
	
	private boolean isRepeatPeriodSet() {
		return repeatPeriodValue != INTERVAL_NOT_SET;
	}

	/**
	 * This method returns the start trigger value on the basis of the command 
	 * cycle specification.
	 * @return start trigger value
	 */
	private StartTriggerList getCCStartTriggerValue() {
		
		StartTriggerList startTrigger = ccspec.getBoundarySpec().getStartTriggerList();
		if (startTrigger != null) {
			return startTrigger;
		}
		return null;
		
	}
	
	/**
	 * This method returns the stop trigger value on the basis of the command 
	 * cycle specification.
	 * @return stop trigger value
	 */
	private StopTriggerList getCCStopTriggerValue() {
		
		StopTriggerList stopTrigger = ccspec.getBoundarySpec().getStopTriggerList();
		if (stopTrigger != null) {
			return stopTrigger;
		}
		return null;
		
	}
	
	/**
	 * This method returns the NoNewTagsInterval on the basis of the command 
	 * cycle specification.
	 * @return stable set interval
	 */
	private long getStableSetInterval() {
		
		ECTime stableSetInterval = ccspec.getBoundarySpec().getNoNewTagsInterval();
		if (stableSetInterval != null) {
			return stableSetInterval.getValue();
		}
		return INTERVAL_NOT_SET;
		
	}
	
	/**
	 * flags whether this reports generator is running or stopped.
	 * @return true if running, false otherwise.
	 */
	protected boolean isRunning() {
		return reportsGeneratorRunning;
	}
	
	/**
	 * activate/deactivate a reports generator.
	 * @param runningState the new state of the reports generator. 
	 */
	protected void setRunning(boolean runningState) {
		reportsGeneratorRunning = runningState;
	}
	
	/**
	 * is the reports generator in polling state???
	 * @return true if polling, false otherwise.
	 */
	protected boolean isPolling() {
		return polling;
	}
	
	/**
	 * the poll reports - <strong>Attention></strong> not null safe.
	 * @return the poll reports - <strong>Attention></strong> not null safe.
	 */
	protected CCReports getPollReport() {
		return pollReport;
	}
	
	public int getAccessSpecNumber() {
		return AccessSpecNumber;
	}
	
	public class NotificationThread implements Runnable {

		private CCReports reports;
		private Map<String, Subscriber> subscribers;
		
		public NotificationThread(CCReports reports, Map<String, Subscriber> subscribers) {
			this.reports = reports;
			this.subscribers = subscribers;
		}
		
		@Override
		public void run() {
			// notify subscribers 
			for (Subscriber listener : subscribers.values()) {
				try {
					listener.notify(reports);
				} catch (Exception e) {
					LOG.error("Could not notify subscriber '" + listener.toString(), e);
				}
			}
		}
		
	}

}
