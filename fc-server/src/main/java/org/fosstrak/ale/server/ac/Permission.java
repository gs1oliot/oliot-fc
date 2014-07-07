/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */


package org.fosstrak.ale.server.ac;

import java.util.HashSet;
import java.util.Set;

public class Permission {
	private String objectName;
	private String opName;
	private Set<String> roles = new HashSet<String>();
	
	public Permission(String permObjStr, String permOpStr, Set<String> roles) {
		this.setObjectName(permObjStr);
		this.setOpName(permOpStr);
		if(roles != null) this.roles.addAll(roles);
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getOpName() {
		return opName;
	}

	public void setOpName(String opName) {
		this.opName = opName;
	}

	public void addRole(String roleName) {
		roles.add(roleName);
	}
	
	public void removeRole(String roleName) {
		roles.remove(roleName);
	}
	
	public Set<String> getRoles() {
		return roles;
	}
	
	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}
	
}
