/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */


package org.fosstrak.ale.server.ac;

import java.util.ArrayList;
import java.util.List;

public class User {
	private String userId;
	private String password;
	private List<String> roles = new ArrayList<String>();
	
	
	public User(String userId, String password) {
		this.setUserId(userId);
		this.password = password;
	}
	
	public void addRole(String r) {
		roles.add(r);
	}
	public void removeRole(String r) {
		roles.remove(r);
	}
	
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public String toString() {
		String toReturn = "[userId="+userId+", password="+password+", roles=[";
		for(String r : roles) {
			toReturn += r;
		}
		toReturn += "]]";
		return toReturn;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
