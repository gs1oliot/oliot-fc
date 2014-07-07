/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */


package org.fosstrak.ale.server.ac;

import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.message.token.UsernameToken;
import org.apache.ws.security.validate.Credential;
import org.apache.ws.security.validate.UsernameTokenValidator;
import org.fosstrak.ale.exception.SecurityException;


/**
 * A custom UsernameToken Validator that wraps the default Validator in WSS4J and set a Subject
 * on the context as well. It validates through Fosstrak RBAC.
 * 
 * @author Janggwan Im
 */
public class CustomUTValidator extends UsernameTokenValidator {

    public Credential validate(Credential credential, RequestData data) throws WSSecurityException {
    	// credential : credential to validate from client
    	// data : data provided from server (ServerPasswordCallback), meaningless
        //Credential cred = super.validate(credential, data);
    	String userId = credential.getUsernametoken().getName();
    	String password = credential.getUsernametoken().getPassword();
    	
    	ALEACImpl aleac = ALEACImpl.getInstance();
    	try {
			if(aleac.login(userId, password)) {
				return credential;
			} else {
				return null;
			}
		} catch (SecurityException e) {
			throw new WSSecurityException(e.getMessage());
		}
        /*
        UsernameToken ut = credential.getUsernametoken();
        WSUsernameTokenPrincipalImpl principal = 
            new WSUsernameTokenPrincipalImpl(ut.getName(), ut.isHashed());
        principal.setCreatedTime(ut.getCreated());
        principal.setNonce(principal.getNonce());
        principal.setPassword(ut.getPassword());
        principal.setPasswordType(ut.getPasswordType());
        
        Subject subject = new Subject();
        subject.getPrincipals().add(principal);
        if ("Alice".equals(ut.getName())) {
            subject.getPrincipals().add(new SimpleGroup("manager", ut.getName()));
        }
        subject.getPrincipals().add(new SimpleGroup("worker", ut.getName()));
        cred.setSubject(subject);
        
        return cred;
        */
    }
    protected void verifyPlaintextPassword(UsernameToken usernameToken,
            RequestData data) throws WSSecurityException {
    	
    }
}