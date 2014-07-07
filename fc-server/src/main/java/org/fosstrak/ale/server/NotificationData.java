package org.fosstrak.ale.server;

import kr.ac.kaist.resl.ltk.generated.parameters.HoppingEvent;
import kr.ac.kaist.resl.ltk.generated.parameters.ROSpecEvent;
import kr.ac.kaist.resl.ltk.generated.parameters.GPIEvent;
import kr.ac.kaist.resl.ltk.generated.parameters.ReportBufferLevelWarningEvent;
import kr.ac.kaist.resl.ltk.generated.parameters.ReportBufferOverflowErrorEvent;
import kr.ac.kaist.resl.ltk.generated.parameters.ReaderExceptionEvent;
import kr.ac.kaist.resl.ltk.generated.parameters.RFSurveyEvent;
import kr.ac.kaist.resl.ltk.generated.parameters.AISpecEvent;
import kr.ac.kaist.resl.ltk.generated.parameters.AntennaEvent;
import kr.ac.kaist.resl.ltk.generated.parameters.ConnectionAttemptEvent;
import kr.ac.kaist.resl.ltk.generated.parameters.ConnectionCloseEvent;



/**
 * represents a tag that has been read on one of the readers in the Logical Reader API.
 * @author swieland
 * @author alessio orlando
 * @author roberto vergallo
 * @author wafa.soubra@orange.com
 *
 */
public class NotificationData {
	
	private HoppingEvent HoppingEvent = null;
	
	private GPIEvent GPIEvent  = null;
	
	private ROSpecEvent ROSpecEvent = null;
	
	private ReportBufferLevelWarningEvent ReportBufferLevelWarningEvent = null;
	
	private ReportBufferOverflowErrorEvent ReportBufferOverflowErrorEvent = null;
	
	private ReaderExceptionEvent ReaderExceptionEvent = null;

	private RFSurveyEvent RFSurveyEvent = null;
	
	private AISpecEvent AISpecEvent = null;
	
	private AntennaEvent AntennaEvent = null;

	private ConnectionAttemptEvent ConnectionAttemptEvent = null;

	private ConnectionCloseEvent ConnectionCloseEvent = null;

	/**
	 * constructor for a tag. (default constructor).
	 */
	public NotificationData() {		
	}

	public HoppingEvent getHoppingEvent() {
		return HoppingEvent;
	}

	public void setHoppingEvent(HoppingEvent hoppingEvent) {
		HoppingEvent = hoppingEvent;
	}

	public GPIEvent getGPIEvent() {
		return GPIEvent;
	}

	public void setGPIEvent(GPIEvent gPIEvent) {
		GPIEvent = gPIEvent;
	}

	public ROSpecEvent getROSpecEvent() {
		return ROSpecEvent;
	}

	public void setROSpecEvent(ROSpecEvent rOSpecEvent) {
		ROSpecEvent = rOSpecEvent;
	}

	public ReportBufferLevelWarningEvent getReportBufferLevelWarningEvent() {
		return ReportBufferLevelWarningEvent;
	}

	public void setReportBufferLevelWarningEvent(
			ReportBufferLevelWarningEvent reportBufferLevelWarningEvent) {
		ReportBufferLevelWarningEvent = reportBufferLevelWarningEvent;
	}

	public ReportBufferOverflowErrorEvent getReportBufferOverflowErrorEvent() {
		return ReportBufferOverflowErrorEvent;
	}

	public void setReportBufferOverflowErrorEvent(
			ReportBufferOverflowErrorEvent reportBufferOverflowErrorEvent) {
		ReportBufferOverflowErrorEvent = reportBufferOverflowErrorEvent;
	}

	public ReaderExceptionEvent getReaderExceptionEvent() {
		return ReaderExceptionEvent;
	}

	public void setReaderExceptionEvent(ReaderExceptionEvent readerExceptionEvent) {
		ReaderExceptionEvent = readerExceptionEvent;
	}

	public RFSurveyEvent getRFSurveyEvent() {
		return RFSurveyEvent;
	}

	public void setRFSurveyEvent(RFSurveyEvent rFSurveyEvent) {
		RFSurveyEvent = rFSurveyEvent;
	}

	public AISpecEvent getAISpecEvent() {
		return AISpecEvent;
	}

	public void setAISpecEvent(AISpecEvent aISpecEvent) {
		AISpecEvent = aISpecEvent;
	}

	public AntennaEvent getAntennaEvent() {
		return AntennaEvent;
	}

	public void setAntennaEvent(AntennaEvent antennaEvent) {
		AntennaEvent = antennaEvent;
	}

	public ConnectionAttemptEvent getConnectionAttemptEvent() {
		return ConnectionAttemptEvent;
	}

	public void setConnectionAttemptEvent(ConnectionAttemptEvent connectionAttemptEvent) {
		ConnectionAttemptEvent = connectionAttemptEvent;
	}

	public ConnectionCloseEvent getConnectionCloseEvent() {
		return ConnectionCloseEvent;
	}

	public void setConnectionCloseEvent(ConnectionCloseEvent connectionCloseEvent) {
		ConnectionCloseEvent = connectionCloseEvent;
	}
	
}