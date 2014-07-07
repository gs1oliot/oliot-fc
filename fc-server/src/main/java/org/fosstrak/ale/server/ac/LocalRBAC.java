/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */


package org.fosstrak.ale.server.ac;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fosstrak.ale.exception.SecurityException;
import org.fosstrak.ale.server.ALESettings;

public class LocalRBAC implements RoleBasedAccessController {

	List<User> listUser = new ArrayList<User>();
	List<Role> listRole = new ArrayList<Role>();
	List<Permission> listPerm = new ArrayList<Permission>();
	
	/**
	 * constructor for LocalRBAC
	 * initialize RBAC info
	 */
	
	public LocalRBAC() {
		
		String adminPassword = ALEACImpl.getInstance().getAleSettings().getAdminPassword();
		
		Permission adminPerm = new Permission("admin", "*", null);
		listPerm.add(adminPerm);
		
		Role adminRole = new Role("admin");
		adminRole.addPerm(adminPerm);
		adminPerm.addRole(adminRole.getName());
		listRole.add(adminRole);
		
		User adminUser = new User("admin", adminPassword);
		adminUser.addRole(adminRole.getName());
		listUser.add(adminUser);
	}
	
	@Override
	public void updateClientid(String userId, String password,
			List<String> roleNames) throws SecurityException {
		List<User> foundUsers = findUsers(userId);
		if(foundUsers.size() == 1) {
			User u = foundUsers.get(0);
			listUser.remove(u);
			u.setUserId(userId);
			u.setPassword(password);
			u.setRoles(roleNames);
			listUser.add(u);
		}
	}

	@Override
	public void updatePermission(String permObjName, String permOpName,
			Set<String> roles) throws SecurityException {
		List<Permission> foundPerm = findPermissions(permObjName, permOpName);
		if(foundPerm.size() == 1) {
			Permission p = foundPerm.get(0);
			listPerm.remove(p);
			
			p.setObjectName(permObjName);
			p.setOpName(permOpName);
			p.setRoles(roles);
			
			listPerm.add(p);
		}
	}

	@Override
	public void defineClientid(String userId, String password,
			List<String> roleNames) throws SecurityException {
		List<User> foundUser = findUsers(userId);
		if(foundUser.size() == 1) {
			listUser.remove(foundUser.get(0));
		}
		User u = new User(userId, password);
		u.setRoles(roleNames);
		listUser.add(u);
	}

	@Override
	public void undefineClientid(String userId) throws SecurityException {
		List<User> foundUser = findUsers(userId);
		if(foundUser.size() == 1) {
			listUser.remove(foundUser.get(0));
		}
	}

	@Override
	public void defineRole(String roleName) throws SecurityException {
		List<Role> foundRole = findRoles(roleName);
		if(foundRole.size() == 1) {
			listRole.remove(foundRole.get(0));
		}
		Role r = new Role(roleName);
		listRole.add(r);
	}

	@Override
	public void undefineRole(String roleName) throws SecurityException {
		List<Role> foundRole = findRoles(roleName);
		if(foundRole.size() == 1) {
			listRole.remove(foundRole.get(0));
		}
	}

	@Override
	public void definePermission(String permObjName, String permOpName)
			throws SecurityException {
		List<Permission> foundPerm = findPermissions(permObjName, permOpName);
		if(foundPerm.size() == 1) {
			listPerm.remove(foundPerm.get(0));
		}
		Permission p = new Permission(permObjName, permOpName, null);
		listPerm.add(p);
	}

	@Override
	public void undefinePermission(String permObjName, String permOpName)
			throws SecurityException {
		List<Permission> foundPerm = findPermissions(permObjName, permOpName);
		if(foundPerm.size() == 1) {
			listPerm.remove(foundPerm.get(0));
		}
	}

	@Override
	public void assignPermissionToRole(String roleName, String permObjName,
			String opName) throws SecurityException {
		List<Role> foundRole = findRoles(roleName);
		List<Permission> foundPermission = findPermissions(permObjName, opName);
		if(foundRole.size() == 1 && foundPermission.size() == 1) {
			Role r = foundRole.get(0);
			Permission p = foundPermission.get(0);
			if(!r.getPermissions().contains(p)) r.addPerm(p);
			if(!p.getRoles().contains(roleName)) p.getRoles().add(roleName);
		}
	}

	@Override
	public void deassignPermissionToRole(String roleName, String permObjName,
			String permOpName) throws SecurityException {
		List<Role> foundRole = findRoles(roleName);
		List<Permission> foundPermission = findPermissions(permObjName, permOpName);
		if(foundRole.size() == 1 && foundPermission.size() == 1) {
			Role r = foundRole.get(0);
			Permission p = foundPermission.get(0);
			if(r.getPermissions().contains(p)) r.removePerm(p);
			if(p.getRoles().contains(roleName)) p.getRoles().remove(roleName);
		}
	}

	@Override
	public void assignRoleToClientid(String userId, String roleName)
			throws SecurityException {
		List<User> foundUser = findUsers(userId);
		List<Role> foundRole = findRoles(roleName);
		if(foundUser.size() == 1 && foundRole.size() == 1) {
			User u = foundUser.get(0);
			Role r = foundRole.get(0);
			u.addRole(r.getName());
		}
	}

	@Override
	public void deassignRoleToClientid(String userId, String roleName)
			throws SecurityException {
		List<User> foundUser = findUsers(userId);
		List<Role> foundRole = findRoles(roleName);
		if(foundUser.size() == 1 && foundRole.size() == 1) {
			User u = foundUser.get(0);
			Role r = foundRole.get(0);
			u.removeRole(r.getName());
		}
	}

	@Override
	public void addPermObj(String objName) throws SecurityException {
		// do nothing
	}

	@Override
	public void deletePermObj(String objName) throws SecurityException {
		// do nothing
	}

	@Override
	public List<User> findUsers(String userName) throws SecurityException {
		if(userName.equals("")) return listUser;
		List<User> toReturn = new ArrayList<User>();
		for(User u : listUser) {
			if(u.getUserId().equals(userName)) {
				toReturn.add(u);
				break;
			}
		}
		return toReturn;
	}

	@Override
	public List<Role> findRoles(String roleName) throws SecurityException {
		if(roleName.equals("")) return listRole;
		List<Role> toReturn = new ArrayList<Role>();
		for(Role r : listRole) {
			if(r.getName().equals(roleName)) {
				toReturn.add(r);
				break;
			}
		}
		return toReturn;
	}

	@Override
	public List<Permission> findPermissions(String permObjName,
			String permOpName) throws SecurityException {
		if(permObjName.equals("")) return listPerm;
		List<Permission> toReturn = new ArrayList<Permission>();
		for(Permission p : listPerm) {
			if(p.getObjectName().equals(permObjName)) {
				toReturn.add(p);
			}
		}
		return toReturn;
	}

	@Override
	public void createSession(String userId, String password)
			throws SecurityException {
		List<User> foundUsers = findUsers(userId);
		if(foundUsers.size() == 1) {
			User foundUser = foundUsers.get(0);
			if(!foundUser.getUserId().equals(userId) || !foundUser.getPassword().equals(password)) {
				throw new SecurityException("your credential is wrong");
			}
		}
	}

	@Override
	public boolean checkAccess(String objName, String opName)
			throws SecurityException {
		String loggedInId = ALEACImpl.getInstance().getLoggedInId();
		
		List<User> foundUsers = findUsers(loggedInId);
		if(foundUsers.size() == 1) {
			User foundUser = foundUsers.get(0);
			for(String foundUserRole : foundUser.getRoles()) {
				List<Role> foundRoles = findRoles(foundUserRole);
				if(foundRoles.size() == 1) {
					Role foundRole = foundRoles.get(0);
					for(Permission p : foundRole.getPermissions()) {
						if(p.getObjectName().equals(objName) && p.getOpName().equals(opName)) {
							return true;
						}
					}
				}
			}
			
		}
		return false;
	}

}
