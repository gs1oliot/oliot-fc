package org.fosstrak.ale.server.readers.llrp.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import kr.ac.kaist.resl.ltk.generated.enumerations.ROSpecStartTriggerType;
import kr.ac.kaist.resl.ltk.generated.enumerations.ROSpecState;
import kr.ac.kaist.resl.ltk.generated.enumerations.ROSpecStopTriggerType;
import kr.ac.kaist.resl.ltk.generated.enumerations.StatusCode;
import kr.ac.kaist.resl.ltk.generated.messages.GET_ROSPECS;
import kr.ac.kaist.resl.ltk.generated.messages.GET_ROSPECS_RESPONSE;
import kr.ac.kaist.resl.ltk.generated.parameters.AISpec;
import kr.ac.kaist.resl.ltk.generated.parameters.LLRPStatus;
import kr.ac.kaist.resl.ltk.generated.parameters.PeriodicTriggerValue;
import kr.ac.kaist.resl.ltk.generated.parameters.ROBoundarySpec;
import kr.ac.kaist.resl.ltk.generated.parameters.ROSpec;
import kr.ac.kaist.resl.ltk.generated.parameters.ROSpecStartTrigger;
import kr.ac.kaist.resl.ltk.generated.parameters.ROSpecStopTrigger;
import org.llrp.ltk.types.UTF8String_UTF_8;
import org.llrp.ltk.types.UnsignedByte;
import org.llrp.ltk.types.UnsignedInteger;

public class LLRPTest {
	@Test
	public void testGetRospec() throws Exception {
		GET_ROSPECS getRospec = new GET_ROSPECS();
		System.out.println(getRospec.toXMLString());
	}
	
	@Test
	public void testGetRospecResponse() throws InvalidLLRPMessageException {
		GET_ROSPECS_RESPONSE getRospecResponse = new GET_ROSPECS_RESPONSE();
		
		LLRPStatus status = new LLRPStatus();
		status.setStatusCode(new StatusCode(StatusCode.M_Success));
		status.setErrorDescription(new UTF8String_UTF_8("No Error"));
		getRospecResponse.setLLRPStatus(status);

		
		
		
		ROSpec rospec1 = new ROSpec();
		rospec1.setROSpecID(new UnsignedInteger(1));
		rospec1.setPriority(new UnsignedByte(0));
		rospec1.setCurrentState(new ROSpecState(0));
		rospec1.setROBoundarySpec(new ROBoundarySpec());
		rospec1.getROBoundarySpec().setROSpecStartTrigger(new ROSpecStartTrigger());
		rospec1.getROBoundarySpec().getROSpecStartTrigger().setROSpecStartTriggerType(new ROSpecStartTriggerType(2));
		rospec1.getROBoundarySpec().getROSpecStartTrigger().setPeriodicTriggerValue(new PeriodicTriggerValue());
		rospec1.getROBoundarySpec().getROSpecStartTrigger().getPeriodicTriggerValue().setOffset(new UnsignedInteger(0));
		rospec1.getROBoundarySpec().getROSpecStartTrigger().getPeriodicTriggerValue().setPeriod(new UnsignedInteger(10000));
		rospec1.getROBoundarySpec().setROSpecStopTrigger(new ROSpecStopTrigger());
		rospec1.getROBoundarySpec().getROSpecStopTrigger().setROSpecStopTriggerType(new ROSpecStopTriggerType(0));
		rospec1.getROBoundarySpec().getROSpecStopTrigger().setDurationTriggerValue(new UnsignedInteger(0));
		
		getRospecResponse.setROSpecList(new ArrayList<ROSpec>());
		getRospecResponse.getROSpecList().add(rospec1);
		
		rospec1.equals(rospec1);
		
		System.out.println(getRospecResponse.toXMLString());

	}
}
