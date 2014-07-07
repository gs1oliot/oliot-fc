/*
 * Copyright 2007 ETH Zurich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package kr.ac.kaist.resl.ltk.net;

import java.math.BigInteger;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import kr.ac.kaist.resl.ltk.generated.LLRPMessageFactory;
import org.llrp.ltk.types.LLRPMessage;

/**
	 * LLRPBinaryDecoder decodes incoming binary LLRP messages to LLRPMessage objects.
 */

public class LLRPBinaryDecoder extends CumulativeProtocolDecoder {

	private static final String MESSAGE_VERSION_KEY = "MessageVersion";
	private static final String MESSAGE_LENGTH_ARRAY = "LengthArray";
	private static final String MESSAGE_LENGTH_KEY = "MessageLength";
	private Logger log = Logger.getLogger(LLRPBinaryDecoder.class);

	enum BYTESTATE {
		STAT_VERSION0,
		STAT_VERSION1,
		STAT_LENGTH0,
		STAT_LENGTH1,
		STAT_LENGTH2,
		STAT_LENGTH3,
		STAT_DATA,
		STAT_NEXT_VERSION0,
		STAT_NEXT_VERSION1,
		STAT_NEXT_LENGTH0,
		STAT_NEXT_LENGTH1,
		STAT_NEXT_LENGTH2,
		STAT_NEXT_LENGTH3,
		STAT_NEXT_DATA,
	};
	
	byte[] versionArray = new byte[2];
	int version = 0;
	
	byte[] lengthArray = new byte[4];
	int length = -1;
	
	byte[] nextVersionArray = new byte[2];
	int nextVersion = 0;
	
	byte[] nextLengthArray = new byte[4];
	int nextLength = -1;

	
	
	protected boolean doDecode_old(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		// if 6 bytes in the buffer we can determine the next length to see
		// if buffer contains a completely delivered message.
		// in.getInt(2) will throw a BufferUnderflowException if there are not
		// even enough bytes to determine length
		
		System.out.println("doDecode is called");
		in.mark();
		
		
		BYTESTATE _state = BYTESTATE.STAT_VERSION0; 
		while(in.remaining() > 0) {
			try {
				byte data = in.get();
				
				switch(_state) {
				
					case STAT_VERSION0:
						versionArray[0] = data;
						_state = BYTESTATE.STAT_VERSION1;
						break;
						
					case STAT_VERSION1:
						versionArray[1] = data;
						version = new BigInteger(versionArray).intValue();
						
						//TODO version check
						
						_state = BYTESTATE.STAT_LENGTH0;
						break;
						
					case STAT_LENGTH0:				
						lengthArray[0] = data;
						_state = BYTESTATE.STAT_LENGTH1;
						break;
						
					case STAT_LENGTH1:
						lengthArray[1] = data;
						_state = BYTESTATE.STAT_LENGTH2;
						break;
						
					case STAT_LENGTH2:
						lengthArray[2] = data;
						_state = BYTESTATE.STAT_LENGTH3;
						break;
						
					case STAT_LENGTH3:
						lengthArray[3] = data;
						length = new BigInteger(lengthArray).intValue();
						
						// TODO length validity check
						_state = BYTESTATE.STAT_DATA;
						break;
						
					case STAT_DATA:
					
						// message generation and write
						byte[] msg = new byte[length];
						msg[0] = versionArray[0];
						msg[1] = versionArray[1];
						msg[2] = lengthArray[0];
						msg[3] = lengthArray[1];
						msg[4] = lengthArray[2];
						msg[5] = lengthArray[3];
						for (int i = 6; i < length; i++) {
							msg[i] = (byte) in.get();
						}
						log.debug("message completely received");
						log.debug("start decoding message");
						LLRPMessage message = LLRPMessageFactory.createLLRPMessage(msg);
						
						log.debug("message decoded: " + message.getClass());
						out.write(message);						
						
						// there might be an other message to be decoded
						// see if there's another completly delivered message in the buffer
						// in this case, we would have to return true
						
						in.mark();
						_state = BYTESTATE.STAT_NEXT_VERSION0;
						break;
						
					case STAT_NEXT_VERSION0:
						nextVersionArray[0] = data;
						_state = BYTESTATE.STAT_NEXT_VERSION1;
						break;
						
					case STAT_NEXT_VERSION1:
						nextVersionArray[1] = data;
						nextVersion = new BigInteger(nextVersionArray).intValue();
						
						//TODO version check
						
						_state = BYTESTATE.STAT_NEXT_LENGTH0;
						break;
						
					case STAT_NEXT_LENGTH0:				
						nextLengthArray[0] = data;
						_state = BYTESTATE.STAT_NEXT_LENGTH1;
						break;
						
					case STAT_NEXT_LENGTH1:
						nextLengthArray[1] = data;
						_state = BYTESTATE.STAT_NEXT_LENGTH2;
						break;
						
					case STAT_NEXT_LENGTH2:
						nextLengthArray[2] = data;
						_state = BYTESTATE.STAT_NEXT_LENGTH3;
						break;
						
					case STAT_NEXT_LENGTH3:
						nextLengthArray[3] = data;
						nextLength = new BigInteger(nextLengthArray).intValue();
						
						// TODO length validity check
						_state = BYTESTATE.STAT_NEXT_DATA;
						break;
						
					case STAT_NEXT_DATA:
						try {
							for (int i = 6; i < nextLength; i++) {
								in.get();
							}
							System.out.println("next message completely received");
							in.position(in.markValue());
							return true;					
						} catch(Exception e) {
							// not enough bytes to determine length
							System.out.println("not enough bytes to determine next message length");
							in.position(in.markValue());
							return false;
						}
						
					default:
						in.position(in.markValue());
						throw new Exception("Error during LLRP decode");
				}
			} catch(Exception e) {
				System.out.println("not enough bytes to determine message length");
				in.position(in.markValue());
				return false;
			}
			
			
		}
		in.position(in.markValue());
		return false;
	}
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		// if 6 bytes in the buffer we can determine the next length to see
		// if buffer contains a completely delivered message.
		// in.getInt(2) will throw a BufferUnderflowException if there are not
		// even enough bytes to determine length
		int length = -1;
		byte[] lengthArray = null;
		byte[] version = null;
		if (in.remaining() >= 6
				&& session.getAttribute(MESSAGE_LENGTH_KEY) == null) {
			// enough bytes to decode length
			log.debug("determine length of message");
			version = new byte[2];
			version[0] = in.get();
			version[1] = in.get();
			lengthArray = new byte[4];
			lengthArray[0] = in.get();
			lengthArray[1] = in.get();
			lengthArray[2] = in.get();
			lengthArray[3] = in.get();
			length = new BigInteger(lengthArray).intValue();
			session.setAttribute(MESSAGE_LENGTH_ARRAY, lengthArray);
			session.setAttribute(MESSAGE_LENGTH_KEY, new Integer(length));
			session.setAttribute(MESSAGE_VERSION_KEY, version);
			// if the entire message is already available, call doDecode again.
			return (in.remaining()>=length-6);
		} else if (session.getAttribute(MESSAGE_LENGTH_KEY) != null) {
			log.debug("length already determined, see if enough bytes are available");
			length = ((Integer)session.getAttribute(MESSAGE_LENGTH_KEY)).intValue();
			version = (byte[]) session.getAttribute(MESSAGE_VERSION_KEY);
			lengthArray = (byte[]) session.getAttribute(MESSAGE_LENGTH_ARRAY);
			if (in.remaining() >= length-6) {
				// all bytes received to decode message
				byte[] msg = new byte[length];
				msg[0] = version[0];
				msg[1] = version[1];
				msg[2] = lengthArray[0];
				msg[3] = lengthArray[1];
				msg[4] = lengthArray[2];
				msg[5] = lengthArray[3];
				for (int i = 6; i < length; i++) {
					msg[i] = (byte) in.get();
				}
				log.debug("message completely received");
				log.debug("start decoding message");
				LLRPMessage message = LLRPMessageFactory.createLLRPMessage(msg);
				log.debug("message decoded: " + message.getClass());
				out.write(message);
				session.removeAttribute(MESSAGE_LENGTH_ARRAY);
				session.removeAttribute(MESSAGE_LENGTH_KEY);
				session.removeAttribute(MESSAGE_VERSION_KEY);
				// there might be an other message to be decoded

				// see if there's another completly delivered message in the
				// buffer
				// in this case, we would have to return true
				try {
					if (in.remaining() >= 6) {
						version = new byte[2];
						version[0] = in.get();
						version[1] = in.get();
						lengthArray = new byte[4];
						lengthArray[0] = in.get();
						lengthArray[1] = in.get();
						lengthArray[2] = in.get();
						lengthArray[3] = in.get();
						length = new BigInteger(lengthArray).intValue();
						session.setAttribute(MESSAGE_LENGTH_ARRAY, lengthArray);
						session.setAttribute(MESSAGE_LENGTH_KEY, new Integer(
								length));
						session.setAttribute(MESSAGE_VERSION_KEY, version);
						if (in.remaining() - in.markValue() >= length-6) {
							log.debug("another message already in the buffer");
							return true;
						} else {
							log.debug("message not yet completly delivered");
							return false;
						}
					}
				} catch (Exception e) {
					// not enough bytes to determine length
					log.debug("not enough bytes to determine message length");
					return false;
				}
			} else {
				// not enough bytes to determin length

				log.debug("not enough bytes to determine message length");
				return false;
			}
		} else {
			log.debug("not enough bytes to determine length");
			return false;
		}
		return false;
	}

}
