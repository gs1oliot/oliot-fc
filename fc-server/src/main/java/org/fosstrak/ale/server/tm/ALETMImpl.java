/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */

package org.fosstrak.ale.server.tm;

import java.util.ArrayList;
import java.util.List;

import org.fosstrak.ale.server.ALESettings;
import org.fosstrak.ale.wsdl.aletm.epcglobal.DuplicateNameExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.ImplementationExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.NoSuchNameExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.TMSpecValidationExceptionResponse;
import org.fosstrak.ale.xsd.ale.epcglobal.TMSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service("aletm")
public class ALETMImpl implements ALETM {
	
	@Autowired
    private ALESettings aleSettings;

	public void initialize() {

	}
	
	@Override
	public void defineTMSpec(String name, TMSpec spec) throws TMSpecValidationExceptionResponse, ImplementationExceptionResponse, DuplicateNameExceptionResponse {
		SymbolicFieldRepo.getInstance().addSymbolicField(name, spec);
	}

	@Override
	public void undefineTMSpec(String name) throws NoSuchNameExceptionResponse {
		SymbolicFieldRepo.getInstance().removeSymbolicField(name);
	}

	@Override
	public List<String> getTMSpecNames() {
		List<String> listNames = new ArrayList<String>();
		for(String str : SymbolicFieldRepo.getInstance().getSymbolicFieldNames()) {
			listNames.add(str);
		}
		return listNames;
	}

	@Override
	public TMSpec getTMSpec(String name) throws NoSuchNameExceptionResponse {
		return SymbolicFieldRepo.getInstance().getTMSpec(name);
	}

	@Override
	public String getStandardVersion() {
		return aleSettings.getAleStandardVersion();
	}

	@Override
	public String getVendorVersion() {
		return aleSettings.getVendorVersion();
	}

}
