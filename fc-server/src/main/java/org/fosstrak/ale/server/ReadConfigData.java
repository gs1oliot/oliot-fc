package org.fosstrak.ale.server;

import java.util.List;

import kr.ac.kaist.resl.ltk.generated.parameters.LLRPStatus;
import kr.ac.kaist.resl.ltk.generated.parameters.LLRPConfigurationStateValue;
import kr.ac.kaist.resl.ltk.generated.parameters.ReaderEventNotificationSpec;
import kr.ac.kaist.resl.ltk.generated.parameters.AntennaProperties;
import kr.ac.kaist.resl.ltk.generated.parameters.AntennaConfiguration;
import kr.ac.kaist.resl.ltk.generated.parameters.ROReportSpec;
import kr.ac.kaist.resl.ltk.generated.parameters.AccessReportSpec;
import kr.ac.kaist.resl.ltk.generated.parameters.Identification;
import kr.ac.kaist.resl.ltk.generated.parameters.KeepaliveSpec;
import kr.ac.kaist.resl.ltk.generated.parameters.GPIPortCurrentState;
import kr.ac.kaist.resl.ltk.generated.parameters.GPOWriteData;
import kr.ac.kaist.resl.ltk.generated.parameters.EventsAndReports;


/**
 * represents a tag that has been read on one of the readers in the Logical Reader API.
 * @author swieland
 * @author alessio orlando
 * @author roberto vergallo
 * @author wafa.soubra@orange.com
 *
 */
public class ReadConfigData {
	
	private LLRPStatus LLRPStatus = null;
	
	private LLRPConfigurationStateValue LLRPConfigurationStateValue = null;
	
	private ReaderEventNotificationSpec ReaderEventNotificationSpec  = null;
	
	private List<AntennaProperties> AntennaProperties = null;
	
	private List<AntennaConfiguration> AntennaConfiguration = null;
	
	private ROReportSpec ROReportSpec = null;
	
	private AccessReportSpec AccessReportSpec = null;

	private Identification Identification = null;
	
	private KeepaliveSpec KeepaliveSpec = null;
	
	private List<GPIPortCurrentState> GPIPortCurrentState = null;

	private List<GPOWriteData> GPOWriteData = null;

	private EventsAndReports EventsAndReports = null;
	
	/**
	 * constructor for a tag. (default constructor).
	 */
	public ReadConfigData() {		
	}

	public LLRPStatus getLLRPStatus() {
		return LLRPStatus;
	}

	public void setLLRPStatus(LLRPStatus lLRPStatus) {
		LLRPStatus = lLRPStatus;
	}

	public LLRPConfigurationStateValue getLLRPConfigurationStateValue() {
		return LLRPConfigurationStateValue;
	}

	public void setLLRPConfigurationStateValue(
			LLRPConfigurationStateValue lLRPConfigurationStateValue) {
		LLRPConfigurationStateValue = lLRPConfigurationStateValue;
	}

	public ReaderEventNotificationSpec getReaderEventNotificationSpec() {
		return ReaderEventNotificationSpec;
	}

	public void setReaderEventNotificationSpec(
			ReaderEventNotificationSpec readerEventNotificationSpec) {
		ReaderEventNotificationSpec = readerEventNotificationSpec;
	}

	public List<AntennaProperties> getAntennaProperties() {
		return AntennaProperties;
	}

	public void setAntennaProperties(List<AntennaProperties> antennaProperties) {
		AntennaProperties = antennaProperties;
	}

	public List<AntennaConfiguration> getAntennaConfiguration() {
		return AntennaConfiguration;
	}

	public void setAntennaConfiguration(List<AntennaConfiguration> antennaConfiguration) {
		AntennaConfiguration = antennaConfiguration;
	}

	public ROReportSpec getROReportSpec() {
		return ROReportSpec;
	}

	public void setROReportSpec(ROReportSpec rOReportSpec) {
		ROReportSpec = rOReportSpec;
	}

	public AccessReportSpec getAccessReportSpec() {
		return AccessReportSpec;
	}

	public void setAccessReportSpec(AccessReportSpec accessReportSpec) {
		AccessReportSpec = accessReportSpec;
	}

	public Identification getIdentification() {
		return Identification;
	}

	public void setIdentification(Identification identification) {
		Identification = identification;
	}

	public KeepaliveSpec getKeepaliveSpec() {
		return KeepaliveSpec;
	}

	public void setKeepaliveSpec(KeepaliveSpec keepaliveSpec) {
		KeepaliveSpec = keepaliveSpec;
	}

	public EventsAndReports getEventsAndReports() {
		return EventsAndReports;
	}

	public void setEventsAndReports(EventsAndReports eventsAndReports) {
		EventsAndReports = eventsAndReports;
	}

	public List<GPIPortCurrentState> getGPIPortCurrentState() {
		return GPIPortCurrentState;
	}

	public void setGPIPortCurrentState(List<GPIPortCurrentState> gPIPortCurrentState) {
		GPIPortCurrentState = gPIPortCurrentState;
	}

	public List<GPOWriteData> getGPOWriteData() {
		return GPOWriteData;
	}

	public void setGPOWriteData(List<GPOWriteData> gPOWriteData) {
		GPOWriteData = gPOWriteData;
	}


}
	
	