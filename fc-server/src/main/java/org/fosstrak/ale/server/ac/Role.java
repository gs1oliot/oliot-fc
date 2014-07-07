/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */


package org.fosstrak.ale.server.ac;

import java.util.ArrayList;
import java.util.List;

public class Role {
	private String name;
	private List<Permission> permissions = new ArrayList<Permission>();
	
	public Role(String name) {
		this.name = name;
	}
	
	public void addPerm(Permission p) {
		permissions.add(p);
	}
	
	public void removePerm(Permission p) {
		permissions.remove(p);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Permission> getPermissions() {
		return permissions;
	}
	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
	}
	
}
