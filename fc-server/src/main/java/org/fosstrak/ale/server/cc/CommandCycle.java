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

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.fosstrak.ale.exception.ImplementationException;
import org.fosstrak.ale.server.Tag;
import org.fosstrak.ale.xsd.ale.epcglobal.CCReports;
import org.fosstrak.ale.xsd.ale.epcglobal.CCCmdSpec;


/**
 * This interface represents an command cycle. It collects the tags and manages the 
 * reports.
 * 
 * @author regli
 * @author swieland
 * @author benoit.plomion@orange.com
 * @author nkef@ait.edu.gr
 * @author Wondeuk Yoon
 */
public interface CommandCycle extends Observer {

	/**
	 * This method adds a tag to this command cycle.
	 * 
	 * @param tag to add
	 * @throws ImplementationException if an implementation exception occurs
	 * @throws ECSpecValidationException if the tag is not valid
	 */
	void addTag(Tag tag);
	
	/**
	 * implementation of the observer interface for tags.
	 * @param o an observable object that triggered the update
	 * @param arg the arguments passed by the observable
	 */
	@Override
	void update(Observable o, Object arg);
	
	/**
	 * This method stops the thread.
	 */
	void stop();
	
	/**
	 * This method returns the name of this command cycle.
	 * 
	 * @return name of command cycle
	 */
	String getName();
	
	/**
	 * This method indicates if this command cycle is terminated or not.
	 * 
	 * @return true if this command cycle is terminated and false otherwise
	 */
	boolean isTerminated();
	
	/**
	 * starts this CommandCycle.
	 */
	void launch();
	
	/**
	 * returns the set of tags from the previous CommandCycle run.
	 * @return a set of tags from the previous CommandCycle run
	 */
	Set<Tag> getLastCommandCycleTags();

	/**
	 * This method return all tags of this command cycle.
	 * 
	 * @return set of tags
	 */
	Set<Tag> getTags();
	
	/**
	 * @return the number of rounds this command cycle has already run through.
	 */
	int getRounds();
	
	/**
	 * thread synchronizer for the end of this command cycle. if the command cycle 
	 * has already finished, then the method returns immediately. otherwise the 
	 * thread waits for the finish.
	 * @throws InterruptedException
	 */
	void join() throws InterruptedException;

	/**
	 * get the CCCmd spec identified by the given name.
	 * @param name the name of the spec to obtain.
	 * @return the ECReportSpec.
	 */
	CCCmdSpec getCmdReportSpecByName(String name);
	
	/**
	 * @return the lastReports
	 */
	Map<String, CCReports> getLastCCReports();


}