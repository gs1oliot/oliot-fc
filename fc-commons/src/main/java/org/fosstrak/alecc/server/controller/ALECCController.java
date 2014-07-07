package org.fosstrak.alecc.server.controller;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * ORANGE
 * interface of ALE CCSpec Dynamic
 * @author Wondeuk Yoon
 */
@WebService(name="ALECCControllerServicePortType", endpointInterface = "org.fosstrak.alecc.server.controller.ALECCController")
public interface ALECCController {

	/**
	 * this method return the status of an CCSpec: started or not
	 * @param specName the name of the specification to test.
	 * @return true if the specification is running, false otherwise.
	 * @throws org.fosstrak.ale.exception.NoSuchNameException the requested CCSpec does not exist. 
	 */
	@WebMethod	
	public boolean ccSpecIsStarted(String specName) throws org.fosstrak.ale.exception.NoSuchNameException;

	/**
	 * this method return all CCSpec which is started
	 */
	@WebMethod
	public List<String> getAllCCSpecNameStarted();
	
	/**
	 * this method start a specified CCSpec
	 * @param specName
	 * @throws org.fosstrak.ale.exception.NoSuchNameException the requested CCSpec does not exist. 
	 */	
	@WebMethod
	public void startCCSpec(String specName) throws org.fosstrak.ale.exception.NoSuchNameException;
	
	/**
	 * this method stop a specified CCSpec
	 * @param specName
	 * @throws org.fosstrak.ale.exception.NoSuchNameException the requested CCSpec does not exist. 
	 */
	@WebMethod
	public void stopCCSpec(String specName) throws org.fosstrak.ale.exception.NoSuchNameException;
	
	/**
	 * this method is used to stop all CCSpec
	 */
	@WebMethod
	public void stopAllCCSpec();
	
	/**
	 * this method is used to stop all CCSpec started for one LogicalReader
	 * @param logicalReaderName
	 * @throws org.fosstrak.ale.exception.NoSuchNameException the requested CCSpec does not exist. 
	 */
	@WebMethod
	public void stopAllCCSpec4LogicalReader(String logicalReaderName) throws org.fosstrak.ale.exception.NoSuchNameException;

	/**
	 * this method is used to stop all CCSpec started for one LogicalReader searching by ccspec
	 * @param specName
	 * @throws org.fosstrak.ale.exception.NoSuchNameException the requested CCSpec does not exist. 
	 */
	@WebMethod
	public void stopAllCCSpec4LogicalReaderByCCSpecName(String specName) throws org.fosstrak.ale.exception.NoSuchNameException;
	
	/**
	 * obtain all the names of the logical readers.
	 * @param isComposite select either only composite readers (case true) or base readers (case false).
	 * @return the requested logical reader names.
	 */
	@WebMethod
	public String[] getLogicalReaderNames(boolean isComposite);
	
}
