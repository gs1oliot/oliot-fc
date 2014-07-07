/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */

package org.fosstrak.ale.server.tm;

import java.util.List;

import org.fosstrak.ale.wsdl.aletm.epcglobal.DuplicateNameException;
import org.fosstrak.ale.wsdl.aletm.epcglobal.DuplicateNameExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.ImplementationExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.NoSuchNameExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.TMSpecValidationExceptionResponse;
import org.fosstrak.ale.xsd.ale.epcglobal.TMSpec;

public interface ALETM {
	public void defineTMSpec(String name, TMSpec spec) throws TMSpecValidationExceptionResponse, ImplementationExceptionResponse, DuplicateNameExceptionResponse;
	public void undefineTMSpec(String name) throws NoSuchNameExceptionResponse;
	public List<String> getTMSpecNames();
	public TMSpec getTMSpec(String name) throws NoSuchNameExceptionResponse;
	
	public String getStandardVersion();
	public String getVendorVersion();
}
