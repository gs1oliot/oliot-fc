/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */


package org.fosstrak.ale.server.ac;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fosstrak.ale.exception.SecurityException;


public interface RoleBasedAccessController {

	public abstract void updateClientid(String userId, String password,
			List<String> roleNames) throws SecurityException;

	public abstract void updatePermission(String permObjName,
			String permOpName, Set<String> roles) throws SecurityException;

	public abstract void defineClientid(String userId, String password,
			List<String> roleNames) throws SecurityException;

	public abstract void undefineClientid(String userId)
			throws SecurityException;

	public abstract void defineRole(String roleName) throws SecurityException;

	public abstract void undefineRole(String roleName) throws SecurityException;

	public abstract void definePermission(String permObjName, String permOpName)
			throws SecurityException;

	public abstract void undefinePermission(String permObjName,
			String permOpName) throws SecurityException;

	public abstract void assignPermissionToRole(String roleName,
			String permObjName, String opName) throws SecurityException;

	public abstract void deassignPermissionToRole(String roleName,
			String permObjName, String permOpName) throws SecurityException;

	public abstract void assignRoleToClientid(String userId, String roleName)
			throws SecurityException;

	public abstract void deassignRoleToClientid(String userId, String roleName)
			throws SecurityException;

	public abstract void addPermObj(String objName) throws SecurityException;

	public abstract void deletePermObj(String objName) throws SecurityException;

	public abstract List<User> findUsers(String userName)
			throws SecurityException;

	public abstract List<Role> findRoles(String roleName)
			throws SecurityException;

	public abstract List<Permission> findPermissions(String permObjName,
			String permOpName) throws SecurityException;

	public abstract void createSession(String userId, String password)
			throws SecurityException;

	public abstract boolean checkAccess(String objName, String opName)
			throws SecurityException;

}