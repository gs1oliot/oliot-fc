package org.fosstrak.ale.server;

import kr.ac.kaist.resl.ltk.generated.interfaces.AirProtocolLLRPCapabilities;
import kr.ac.kaist.resl.ltk.generated.parameters.GeneralDeviceCapabilities;
import kr.ac.kaist.resl.ltk.generated.parameters.LLRPCapabilities;
import kr.ac.kaist.resl.ltk.generated.parameters.LLRPStatus;
import kr.ac.kaist.resl.ltk.generated.parameters.RegulatoryCapabilities;


/**
 * represents a tag that has been read on one of the readers in the Logical Reader API.
 * @author swieland
 * @author alessio orlando
 * @author roberto vergallo
 * @author wafa.soubra@orange.com
 *
 */
public class CapabilitiesData {
	
	private LLRPStatus LLRPStatus = null;
	
	private GeneralDeviceCapabilities GeneralDeviceCapabilities  = null;
	
	private LLRPCapabilities LLRPCapabilities = null;
	
	private RegulatoryCapabilities RegulatoryCapabilities = null;
	
	private AirProtocolLLRPCapabilities AirProtocolLLRPCapabilities = null;
	
	/**
	 * constructor for a tag. (default constructor).
	 */
	public CapabilitiesData() {		
	}

	public LLRPStatus getLLRPStatus() {
		return LLRPStatus;
	}

	public void setLLRPStatus(LLRPStatus lLRPStatus) {
		LLRPStatus = lLRPStatus;
	}

	public GeneralDeviceCapabilities getGeneralDeviceCapabilities() {
		return GeneralDeviceCapabilities;
	}

	public void setGeneralDeviceCapabilities(GeneralDeviceCapabilities generalDeviceCapabilities) {
		GeneralDeviceCapabilities = generalDeviceCapabilities;
	}

	public LLRPCapabilities getLLRPCapabilities() {
		return LLRPCapabilities;
	}

	public void setLLRPCapabilities(LLRPCapabilities lLRPCapabilities) {
		LLRPCapabilities = lLRPCapabilities;
	}

	public RegulatoryCapabilities getRegulatoryCapabilities() {
		return RegulatoryCapabilities;
	}

	public void setRegulatoryCapabilities(RegulatoryCapabilities regulatoryCapabilities) {
		RegulatoryCapabilities = regulatoryCapabilities;
	}

	public AirProtocolLLRPCapabilities getAirProtocolLLRPCapabilities() {
		return AirProtocolLLRPCapabilities;
	}

	public void setAirProtocolLLRPCapabilities(
			AirProtocolLLRPCapabilities airProtocolLLRPCapabilities) {
		AirProtocolLLRPCapabilities = airProtocolLLRPCapabilities;
	}
	

}