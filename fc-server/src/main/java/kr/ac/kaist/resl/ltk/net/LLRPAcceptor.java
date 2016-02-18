package kr.ac.kaist.resl.ltk.net;




import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;



/**
 * LLRPAcceptor implements a remotely initiated LLRP connection. 
 * 
 * Here is a simple code example:
 * <p>
 * <code> LLRPAcceptor c = new LLRPAcceptor(endpoint); </code> <p>
 * <code> c.bind(); </code> <p>
 * <code> // wait for incoming reader initiated connection ..... </code> <p>
 * <code> // send message asynchronously </code> <p>
 * <code> c.send(llrpmessage); </code> <p>
  * <code> // asynchronously LLRP messages arrive via LLRPEndpoint.messageReceived</code> <p>
 * <code> // send message synchronously </code> <p>
 * <code> LLRPMessage m = c.transact(llrpmessage); </code>
 * 
 * @author Basil Gasser - ETH Zurich
 * @author Christian Floerkemeier - MIT
 * 
 */
public class LLRPAcceptor extends LLRPConnection  {

	public static final int IDLE_TIME = 20;

	private static final Logger log = Logger.getLogger(LLRPAcceptor.class);
	private int port = 5084;
	private IoAcceptor acceptor;
	private InetSocketAddress socketAddress;

	public LLRPAcceptor() {
		super();
	}

	/**
	 * creates a remotely initiated LLRP connection on default PORT 5084 
	 * and uses LLRPIoHandlerAdapterImpl by default
	 * 
	 * @param endpoint that handles incoming, asynchronous LLRP messages
	 */
	
	public LLRPAcceptor(LLRPEndpoint endpoint){
		this.endpoint = endpoint;
	}
	
	/**
	 * creates a remotely initiated LLRP connection and uses LLRPIoHandlerAdapterImpl by default
	 * 
	 * @param endpoint that handles incoming, asynchronous LLRP messages
	 * @param port on which LLRPAcceptor is waiting for incoming connections
	 */

	public LLRPAcceptor(LLRPEndpoint endpoint, int port){
		this.endpoint = endpoint;
		this.port = port;
	}

	/**
	 * creates a remotely initiated LLRP connection and uses LLRPIoHandlerAdapterImpl by default
	 * 
	 * @param endpoint that handles incoming, asynchronous LLRP messages
	 * @param port on which LLRPAcceptor is waiting for incoming connections
	 * @param handler which handles incoming LLRP messages
	 */
	
	public LLRPAcceptor(LLRPEndpoint endpoint, int port, LLRPIoHandlerAdapter handler){
		super.endpoint = endpoint;
		this.port = port;
		super.handler = handler;
	}

	/**
	 * creates a remotely initiated LLRP connection on default PORT 5084 
	 * 
	 * @param endpoint that handles incoming, asynchronous LLRP messages
	 * @param handler which handles incoming LLRP messages
	 */
	
	public LLRPAcceptor(LLRPEndpoint endpoint, LLRPIoHandlerAdapter handler){
		super.endpoint = endpoint;
		super.handler = handler;
	}
	
	
	/**
	 * binds the LLRPIOHandler registered to the port specified. It
	 * does not wait for an incoming READER_NOTIFICATION message with a successful 
	 * ConnectionAttemptEvent. Use the bind method where you can specify a timeout instead.
	 */
	
	public void bind() throws LLRPConnectionAttemptFailedException{
		bind(0);
	}
	
	/**
	 * binds the LLRPIOHandler registered to the port specified. The bind method
	 * waits for an incoming READER_NOTIFICATION message with a successful 
	 * ConnectionAttemptEvent for the timeout specified. If the timeout is set to zero,
	 * the bind method will not wait for an incoming READER_NOTIFICATION message. 
	 * 
	 * @param timeout time in ms
	 * @throws LLRPConnectionAttemptFailedException if ConnectionAttemptStatus 'Success' in READER_NOTIFICATION 
	 * message is not received within time interval specified by timeout
	 */
	
	public void bind(long timeout) throws LLRPConnectionAttemptFailedException{
		ExecutorService executor = new OrderedThreadPoolExecutor();
		
		acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors());
		acceptor.getFilterChain().addLast("executor", new ExecutorFilter(executor));
		acceptor.getFilterChain().addLast( "logger", new LoggingFilter() );
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new LLRPProtocolCodecFactory(LLRPProtocolCodecFactory.BINARY_ENCODING)));
		// MINA 2.0
		acceptor.setHandler(handler);
		acceptor.getSessionConfig().setReadBufferSize( 2048 );
		acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, IDLE_TIME );
		
		// MINA 1.1
		//SocketAcceptorConfig cfg = new SocketAcceptorConfig();
		//cfg.getSessionConfig().setReceiveBufferSize(2048);
		
		try {        
			socketAddress = new InetSocketAddress(port);
			acceptor.bind(socketAddress);//, handler);
			log.info("server listening on port "+port);
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new LLRPConnectionAttemptFailedException(e.getMessage());
		}
		
		
		//check if llrp reader reply with a status report to indicate connection success.
		//the client shall not send any information to the reader until this status report message is received
		
		if (timeout>0) {
			checkLLRPConnectionAttemptStatus(timeout);
		}
	}

	/** 
	 * close acceptor socket.
	 * 
	 **/

	public void close(){
		acceptor.unbind(socketAddress);
	}

	/** 
	 * reconnect method. currently not implemented. always returns false.
	 * 
	 **/
	
	public boolean reconnect(){
		return false;
	}
	

	/**
	 * get host address of reader device.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

}
