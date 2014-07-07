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

package org.fosstrak.alecc.util;

/**
 * for backwards compatibility to provide the ECTerminationConditions.
 * @author swieland
 *
 */
public class CCTerminationCondition {
	/** termination by trigger. */
	public static final String TRIGGER = "TRIGGER";
	
	/** termination by the duration. */
	public static final String DURATION = "DURATION";
	
	/** the no new tags set. */
	public static final String NO_NEW_TAGS = "NO_NEW_TAGS";
	
	/** data is available. */
	public static final String COUNT = "COUNT";
	
	/** data is available. */
	public static final String ERROR = "ERROR";
	
	/** termination by unrequest. */
	public static final String UNREQUEST = "UNREQUEST";
	
	/** termination by undefine. */
	public static final String UNDEFINE = "UNDEFINE";
}
