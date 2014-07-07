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
 * Lesser General License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Fosstrak; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package org.fosstrak.ale.server.cc;

import java.util.Map;

import org.fosstrak.ale.exception.DuplicateNameException;
import org.fosstrak.ale.exception.DuplicateSubscriptionException;
import org.fosstrak.ale.exception.CCSpecValidationException;
import org.fosstrak.ale.exception.ImplementationException;
import org.fosstrak.ale.exception.InvalidURIException;
import org.fosstrak.ale.exception.NoSuchNameException;
import org.fosstrak.ale.exception.NoSuchSubscriberException;
import org.fosstrak.ale.exception.SecurityException;
import org.fosstrak.ale.xsd.ale.epcglobal.CCReports;
import org.fosstrak.ale.xsd.ale.epcglobal.CCSpec;

/**
 * This class represents the application level events interface.
 * All ale operations are executed by this class.
 * 
 * @author regli
 * @author swieland
 * @author haennimi
 * @author benoit.plomion@orange.com
 * @author Wondeuk Yoon
 */
public interface ALECC {

	/**
	 * This method indicates if the ALE is ready or not.
	 * 
	 * @return true if the ALE is ready and false otherwise
	 */
	boolean isReady();
	
	/**
	 * With this method an cc specification can be defined.
	 * 
	 * @param specName of the cc specification
	 * @param spec to define
	 * @throws DuplicateNameException if a cc specification with the same name is already defined
	 * @throws ECSpecValidationException if the cc specification is not valid
	 * @throws ImplementationException if an implementation exception occurs
	 * @throws SecurityException 
	 */
	void define(String specName, CCSpec spec) throws DuplicateNameException, CCSpecValidationException, ImplementationException, SecurityException;
	
	/**
	 * With this method an cc specification can be undefined.
	 * 
	 * @param specName of the cc specification to undefine
	 * @throws NoSuchNameException if there is no cc specification with this name defined
	 */
	void undefine(String specName) throws NoSuchNameException;
	
	/**
	 * This method returns an cc specification depending on a given name.
	 * 
	 * @param specName of the cc specification to return
	 * @return cc specification with the specified name
	 * @throws NoSuchNameException if no such cc specification exists
	 */
	CCSpec getCCSpec(String specName) throws NoSuchNameException;
	
	/**
	 * This method returns the names of all defined cc specifications.
	 * 
	 * @return string array with names
	 */
	String[] getCCSpecNames();
	
	/**
	 * With this method a notification uri can be subscribed to a defined cc specification.
	 * 
	 * @param specName of the cc specification
	 * @param notificationURI to subscribe
	 * @throws NoSuchNameException if there is no cc specification with the given name defined
	 * @throws InvalidURIException if the specified notification uri is invalid
	 * @throws DuplicateSubscriptionException if the same subscription is already done
	 */
	void subscribe(String specName, String notificationURI) throws NoSuchNameException, InvalidURIException, DuplicateSubscriptionException;

	/**
	 * With this method a notification uri can be unsubscribed from a defined cc specification.
	 * 
	 * @param specName of the cc specification
	 * @param notificationURI to unsubscribe
	 * @throws NoSuchNameException if there is no cc specification with the given name defined
	 * @throws NoSuchSubscriberException if the specified notification uri is not subscribed to the cc specification.
	 * @throws InvalidURIException if the specified notification uri is invalid
	 */
	void unsubscribe(String specName, String notificationURI) throws NoSuchNameException, NoSuchSubscriberException, InvalidURIException;

	/**
	 * With this method a defined cc specification can be polled.
	 * Polling is the same as subscribe to a cc specification, waiting for one event cycle and then unsubscribe
	 * with the difference that the report is the result of the method instead of sending it to an uri.
	 * 
	 * @param specName of the cc specification which schould be polled
	 * @return cc report of the next event cycle
	 * @throws NoSuchNameException if there is no cc specification with the given name defined
	 */
	CCReports poll(String specName) throws NoSuchNameException;
	
	/**
	 * With this method a undefined cc specifcation can be executed.
	 * It's the same as defining the cc specification, polling and undefining it afterwards.
	 * 
	 * @param spec cc specification to execute
	 * @return cc report of the next event cycle
	 * @throws ECSpecValidationException if the cc specification is not valid
	 * @throws ImplementationException if an implementation exception occures
	 */
	CCReports immediate(CCSpec spec) throws CCSpecValidationException, ImplementationException;
	
	/**
	 * This method returns all subscribers to a given cc specification name.
	 * 
	 * @param specName of which the subscribers should be returned
	 * @return array of string with notification uris
	 * @throws NoSuchNameException if there is no cc specification with the given name is defined
	 */
	String[] getSubscribers(String specName) throws NoSuchNameException;
		
	/**
	 * This method returns the standard version to which this implementation is compatible.
	 * 
	 * @return standard version
	 */
	String getStandardVersion();
	
	/**
	 * This method returns the vendor version of this implementation.
	 * 
	 * @return vendor version
	 */
	String getVendorVersion();
	
	/**
	 * This method closes the ale and remove all input generators and there objects on the reader devices.
	 */
	void close();
	
	/**
	 * returns a handle on the report generators in the ALE.<br/>this method never returns null. 
	 * @return a handle on the report generators in the ALE.
	 */
	//ORANGE: add for ALEController ws
	Map<String, ReportsGenerator> getReportGenerators();
	
}