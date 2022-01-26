/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */


package org.fosstrak.ale.server.ac;
import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.ws.security.WSPasswordCallback;

/**
 * 
 * @author Janggwan Im
 *
 */

public class ServerPasswordCallback implements CallbackHandler {
 
    public void handle(Callback[] callbacks) throws IOException, 
        UnsupportedCallbackException {
 
        WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
 
        if (pc.getIdentifier().equals("admin")) {
            // set the password on the callback. This will be compared to the
            // password which was sent from the client.
            //pc.setPassword("1111");
        	
        	// do not use this for Fosstrak login
            
        }
    }
 
}