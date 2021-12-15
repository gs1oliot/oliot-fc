package org.fosstrak.ale.server.llrp;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.List;

import org.fosstrak.ale.exception.DuplicateNameException;
import org.fosstrak.ale.exception.NoSuchNameException;

/**
 * ORANGE: Interface to define dynamic ROSPEC for LLRP Reader.
 *
 * FIXME: swieland: need to comment this interface...
 * 
 * @author wafa.soubra@orange.com
 */
@WebService(name="LLRPControllerServicePortType")
public interface LLRPController {

	/**
	 * define a new ROSpec on the given logical reader.
	 * @param lrSpecName the name of the AddROSpec.
	 * @param addRoSpec serialized AddROSpec
	 * @throws DuplicateNameException
	 * @throws NoSuchNameException
	 */
	@WebMethod
	public void define(String lrSpecName, String addRoSpec) throws DuplicateNameException, NoSuchNameException;
	
	@WebMethod
	public void undefine(String lrSpecName) throws NoSuchNameException;

	@WebMethod
	public List<String> getSpecNames();
	
	@WebMethod
	public void start (String lrSpecName) throws NoSuchNameException;
	
	@WebMethod
	public void stop(String lrSpecName) throws NoSuchNameException;
	
	@WebMethod
	public void enable(String lrSpecName) throws NoSuchNameException;
	
	@WebMethod
	public void disable(String lrSpecName) throws NoSuchNameException;
	
	@WebMethod
	public void disableAll();
	
}

