package org.fosstrak.ale.wsdl.aletm.epcglobal;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;

import org.fosstrak.ale.exception.SecurityException;
import org.fosstrak.ale.server.ac.ALEACImpl;
import org.fosstrak.ale.server.tm.ALETM;
import org.fosstrak.ale.wsdl.aletm.epcglobal.ALETMServicePortType;
import org.fosstrak.ale.xsd.ale.epcglobal.TMSpec;
import org.springframework.beans.factory.annotation.Autowired;

public class ALETMServicePortTypeImpl implements ALETMServicePortType {

	@Autowired
	private ALETM aletm;
	
    @Autowired
    private ALEACImpl aleac;
	
    private String authScope = "ALETM";
    
	@Override
	@WebResult(name = "DefineTMSpecResult", targetNamespace = "urn:epcglobal:aletm:wsdl:1", partName = "defineTMSpecReturn")
	@WebMethod
	public DefineTMSpecResult defineTMSpec(
			@WebParam(partName = "parms", name = "DefineTMSpec", targetNamespace = "urn:epcglobal:aletm:wsdl:1") DefineTMSpec arg0)
			throws TMSpecValidationExceptionResponse,
			DuplicateNameExceptionResponse, SecurityExceptionResponse,
			ImplementationExceptionResponse {
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			aletm.defineTMSpec(arg0.getSpecName(), arg0.getSpec());
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		
		return new DefineTMSpecResult();
	}

	@Override
	@WebResult(name = "GetStandardVersionResult", targetNamespace = "urn:epcglobal:aletm:wsdl:1", partName = "getStandardVersionReturn")
	@WebMethod
	public String getStandardVersion(
			@WebParam(partName = "parms", name = "GetStandardVersion", targetNamespace = "urn:epcglobal:aletm:wsdl:1") EmptyParms arg0)
			throws ImplementationExceptionResponse {
		
		return aletm.getStandardVersion();
	}

	@Override
	@WebResult(name = "GetTMSpecResult", targetNamespace = "urn:epcglobal:aletm:wsdl:1", partName = "getTMSpecReturn")
	@WebMethod
	public TMSpec getTMSpec(
			@WebParam(partName = "parms", name = "GetTMSpec", targetNamespace = "urn:epcglobal:aletm:wsdl:1") GetTMSpec arg0)
			throws ImplementationExceptionResponse,
			NoSuchNameExceptionResponse, SecurityExceptionResponse {
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			return aletm.getTMSpec(arg0.getSpecName());
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		
	}

	@Override
	@WebResult(name = "GetTMSpecNamesResult", targetNamespace = "urn:epcglobal:aletm:wsdl:1", partName = "getTMSpecNamesReturn")
	@WebMethod
	public ArrayOfString getTMSpecNames(
			@WebParam(partName = "parms", name = "GetTMSpecNames", targetNamespace = "urn:epcglobal:aletm:wsdl:1") EmptyParms arg0)
			throws SecurityExceptionResponse, ImplementationExceptionResponse {
		ArrayOfString aos = new ArrayOfString();
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			aos.string = aletm.getTMSpecNames(); 
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		return aos;
	}

	@Override
	@WebResult(name = "GetVendorVersionResult", targetNamespace = "urn:epcglobal:aletm:wsdl:1", partName = "getVendorVersionReturn")
	@WebMethod
	public String getVendorVersion(
			@WebParam(partName = "parms", name = "GetVendorVersion", targetNamespace = "urn:epcglobal:aletm:wsdl:1") EmptyParms arg0)
			throws ImplementationExceptionResponse {
		return aletm.getVendorVersion();
	}

	@Override
	@WebResult(name = "UndefineTMSpecResult", targetNamespace = "urn:epcglobal:aletm:wsdl:1", partName = "undefineTMSpecReturn")
	@WebMethod
	public UndefineTMSpecResult undefineTMSpec(
			@WebParam(partName = "parms", name = "UndefineTMSpec", targetNamespace = "urn:epcglobal:aletm:wsdl:1") UndefineTMSpec arg0)
			throws ImplementationExceptionResponse,
			NoSuchNameExceptionResponse, SecurityExceptionResponse {
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			aletm.undefineTMSpec(arg0.getSpecName());
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		
		return new UndefineTMSpecResult();
	}

}
