/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */

package org.fosstrak.ale.server.ac;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.fosstrak.ale.server.ALEApplicationContext;
import org.fosstrak.ale.server.ALESettings;
import org.fosstrak.ale.wsdl.aleac.epcglobal.ClientIdentityValidationExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.DuplicateClientIdentityExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.DuplicatePermissionExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.DuplicateRoleExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.ImplementationExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.NoSuchClientIdentityExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.NoSuchPermissionExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.NoSuchRoleExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.PermissionValidationExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.RoleValidationExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.UnsupportedOperationExceptionResponse;
import org.fosstrak.ale.xsd.ale.epcglobal.ACClientCredential;
import org.fosstrak.ale.xsd.ale.epcglobal.ACClientIdentity;
import org.fosstrak.ale.xsd.ale.epcglobal.ACPermission;
import org.fosstrak.ale.xsd.ale.epcglobal.ACRole;
import org.fosstrak.ale.xsd.ale.epcglobal.ACClientIdentity.Credentials;
import org.fosstrak.ale.xsd.ale.epcglobal.ACClientIdentity.RoleNames;
import org.fosstrak.ale.xsd.ale.epcglobal.ACPermission.Instances;
import org.fosstrak.ale.xsd.ale.epcglobal.ACRole.PermissionNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * This class implements ALE Access Control API
 * 
 * @author Janggwan Im <limg00n@kaist.ac.kr>
 *
 */

@Service("aleac")
public class ALEACImpl {
	/**
	 * logger.
	 */
	private static final Logger LOG = Logger.getLogger(ALEACImpl.class);
	
	public RoleBasedAccessController rbac;// = new FortressRBACClient();

	
	private String loggedInId = null;
	
	@Autowired
    private ALESettings aleSettings;
	
	@Autowired
	public void setAleSettings(ALESettings aleSettings) {
		this.aleSettings = aleSettings;
	}
	
	public ALESettings getAleSettings() {
		return aleSettings;
	}
	
	public List<String> getPermissionNames() throws org.fosstrak.ale.exception.SecurityException {
		List<String> toReturn = new ArrayList<String>();
		List<Permission> listPerm = findPermissions("");
		for(Permission p : listPerm) {
			toReturn.add(p.getObjectName());
		}
		
		return toReturn;
	}
	
	public void definePermission(String permName, ACPermission perm) throws org.fosstrak.ale.exception.SecurityException, PermissionValidationExceptionResponse, DuplicatePermissionExceptionResponse {
		if(perm.getPermissionClass().equalsIgnoreCase("Method")) {
			for(String instanceName : perm.getInstances().getInstance()) {
				// for example, instanceName = "ALE.*"
				if(instanceName.equalsIgnoreCase("*")) {
					
				} else {
					String[] instanceStr = instanceName.split("\\.");
					if(instanceStr.length == 1) {
						if(!instanceStr[0].equalsIgnoreCase("ALE") &&
								!instanceStr[0].equalsIgnoreCase("ALECC") &&
								!instanceStr[0].equalsIgnoreCase("ALETM") &&
								!instanceStr[0].equalsIgnoreCase("ALELR") &&
								!instanceStr[0].equalsIgnoreCase("ALEAC")) {
							throw new PermissionValidationExceptionResponse("invalid instance name");
						}
					} else if(instanceStr.length > 2){
						throw new PermissionValidationExceptionResponse("invalid instance name");
					} else {
						if(!instanceStr[0].equalsIgnoreCase("ALE") &&
								!instanceStr[0].equalsIgnoreCase("ALECC") &&
								!instanceStr[0].equalsIgnoreCase("ALETM") &&
								!instanceStr[0].equalsIgnoreCase("ALELR") &&
								!instanceStr[0].equalsIgnoreCase("ALEAC")) {
							throw new PermissionValidationExceptionResponse("invalid instance name");
						}
					}
				}
			}
			
			List<Permission> listPerm = findPermissions(permName);
			boolean exist = false;
			for(Permission p : listPerm) {
				if(p.getObjectName().equalsIgnoreCase(permName)) {
					exist = true;
				}
			}
			if(exist) throw new DuplicatePermissionExceptionResponse("permission already exists");
			
			for(String instanceName : perm.getInstances().getInstance()) {
				rbac.addPermObj(permName);
				rbac.definePermission(permName, instanceName);
			}
		} else {
			throw new PermissionValidationExceptionResponse("invalid permission class");
		}
		
	}
	
	public void updatePermission(String permName, ACPermission perm) throws org.fosstrak.ale.exception.SecurityException, PermissionValidationExceptionResponse, NoSuchPermissionExceptionResponse {
		if(!perm.getPermissionClass().equalsIgnoreCase("Method")) throw new PermissionValidationExceptionResponse("invalid permission class");
		
		List<Permission> listPerm = findPermissions(permName);
		boolean noSuchPerm = true;
		for(Permission p : listPerm) {
			if(p.getObjectName().equalsIgnoreCase(permName)) {
				noSuchPerm = false;
			}
		}
		if(noSuchPerm) throw new NoSuchPermissionExceptionResponse("no such permission");
		
		String permObjName = permName;
		for(String permOpName : perm.getInstances().getInstance()) {
			Set<String> roles = new HashSet<String>();
			rbac.updatePermission(permObjName, permOpName, roles);	
		}
	}
	
	public ACPermission getPermission(String permName) throws org.fosstrak.ale.exception.SecurityException, NoSuchPermissionExceptionResponse {
		ACPermission toReturn = new ACPermission();

		List<Permission> listPerm = findPermissions(permName);
		if(listPerm.size() == 0) throw new NoSuchPermissionExceptionResponse("no such permission");
		
		toReturn.setPermissionClass("Method");
		toReturn.setInstances(new Instances());
		for(Permission p : listPerm) {
			toReturn.getInstances().getInstance().add(p.getOpName());
		}
		
		return toReturn;
	}
	
	public void undefinePermission(String permName) throws org.fosstrak.ale.exception.SecurityException, NoSuchPermissionExceptionResponse {
		List<Permission> listPerm = findPermissions(permName);
		boolean exist = false;
		for(Permission p : listPerm) {
			if(p.getObjectName().equalsIgnoreCase(permName)) {
				exist = true;
			}
		}
		if(!exist) throw new NoSuchPermissionExceptionResponse("no such permission");
		
		rbac.deletePermObj(permName);
	}
	
	
	public List<String> getRoleNames() throws org.fosstrak.ale.exception.SecurityException {
		List<String> toReturn = new ArrayList<String>();
		
		List<Role> listRole = findRoles("");
		for(Role r : listRole) {
			toReturn.add(r.getName());
		}

		return toReturn;
	}
	
	public void defineRole(String roleName, ACRole role) throws org.fosstrak.ale.exception.SecurityException, RoleValidationExceptionResponse, DuplicateRoleExceptionResponse {
		List<Role> rlist = findRoles(roleName);
		boolean exist = false;
		for(Role r : rlist) {
			if(r.getName().equalsIgnoreCase(roleName)) {
				exist = true;
			}
		}
		if(exist) throw new DuplicateRoleExceptionResponse("role to define already exists");
		
		
		// add permission
		PermissionNames listPermName = (PermissionNames) role.getPermissionNames();
		if(listPermName.getPermissionName() != null) {
			List<String> listPerms = (List<String>) listPermName.getPermissionName();
			for(String permName : listPerms) {
				
				List<Permission> perms = findPermissions(permName);
				exist = false;
				for(Permission p : perms) {
					if(p.getObjectName().equalsIgnoreCase(permName)) {
						exist = true;
					}
				}
				if(!exist) throw new RoleValidationExceptionResponse("no such permission");
				
				rbac.defineRole(roleName);
				for(Permission p : perms) {
					String opName = p.getOpName();
					rbac.assignPermissionToRole(roleName, permName, opName);
				}
								
			}
		}
		
		
	}
	
	public void updateRole(String roleName, ACRole role) throws org.fosstrak.ale.exception.SecurityException, NoSuchRoleExceptionResponse, RoleValidationExceptionResponse {
		List<Role> rlist = findRoles(roleName);
		boolean exist = false;
		for(Role r : rlist) {
			if(r.getName().equalsIgnoreCase(roleName)) {
				exist = true;
			}
		}
		if(!exist) throw new NoSuchRoleExceptionResponse("no such role");
		
		List<Permission> plist = findPermissions("");
		for(Permission p : plist) {
			if(p != null && p.getRoles() != null && p.getRoles().contains(roleName)) {
				deassignPermissionToRole(roleName, p.getObjectName(), p.getOpName());		
			}
		}
		for(String permName : role.getPermissionNames().getPermissionName()) {
			List<Permission> perms = findPermissions(permName);
			exist = false;
			for(Permission p : perms) {
				if(p.getObjectName().equalsIgnoreCase(permName)) {
					exist = true;
				}
			}
			if(!exist) throw new RoleValidationExceptionResponse("no such permission");
			
			for(Permission p : perms) {
				String opName = p.getOpName();
				rbac.assignPermissionToRole(roleName, permName, opName);
			}
		}
	}
	
	public ACRole getRole(String roleName) throws org.fosstrak.ale.exception.SecurityException, NoSuchRoleExceptionResponse {
		List<Role> listRole = findRoles(roleName);
		
		boolean exist = false;
		for(Role r : listRole) {
			if(r.getName().equalsIgnoreCase(roleName)) {
				exist = true;
			}
		}
		if(!exist) throw new NoSuchRoleExceptionResponse("no such role");
		
		
		if(listRole != null && listRole.get(0) != null && listRole.get(0).getName().equals(roleName)) {
			ACRole acRole = new ACRole();
			acRole.setPermissionNames(new PermissionNames());
			List<Permission> plist = findPermissions("");
			for(Permission p : plist) {
				if(p != null && p.getRoles() != null && p.getRoles().contains(roleName)) {
					acRole.getPermissionNames().getPermissionName().add(p.getObjectName());
				}
			}
			return acRole;
		}
		return null;
	}
	
	public void undefineRole(String roleName) throws org.fosstrak.ale.exception.SecurityException, NoSuchRoleExceptionResponse {
		List<Role> listRole = findRoles(roleName);
		
		boolean exist = false;
		for(Role r : listRole) {
			if(r.getName().equalsIgnoreCase(roleName)) {
				exist = true;
			}
		}
		if(!exist) throw new NoSuchRoleExceptionResponse("no such role");
		
		rbac.undefineRole(roleName);
	}
	
	public void addPermissions(String roleName, List<String> permissionNames) throws org.fosstrak.ale.exception.SecurityException, NoSuchPermissionExceptionResponse, NoSuchRoleExceptionResponse {
		List<Role> listRole = findRoles(roleName);
		
		boolean exist = false;
		for(Role r : listRole) {
			if(r.getName().equalsIgnoreCase(roleName)) {
				exist = true;
			}
		}
		if(!exist) throw new NoSuchRoleExceptionResponse("no such role");
		
		
		for(String permName : permissionNames) {
			List<Permission> perms = findPermissions(permName);
			if(perms.size() == 0) throw new NoSuchPermissionExceptionResponse("no such permission");
			for(Permission p : perms) {
				String opName = p.getOpName();
				
				rbac.assignPermissionToRole(roleName, permName, opName);
			}
		}	
	}
	
	public void setPermissions(String roleName, List<String> permissionNames) throws UnsupportedOperationExceptionResponse {
		throw new UnsupportedOperationExceptionResponse();
	}
	
	public void removePermissions(String roleName, List<String> permissionNames) throws org.fosstrak.ale.exception.SecurityException, NoSuchRoleExceptionResponse, NoSuchPermissionExceptionResponse {
		List<Role> listRole = findRoles(roleName);
		boolean exist = false;
		for(Role r : listRole) {
			if(r.getName().equalsIgnoreCase(roleName)) {
				exist = true;
			}
		}
		if(!exist) throw new NoSuchRoleExceptionResponse("no such role");
		
		
		
		for(String permName : permissionNames) {
			
			List<Permission> perms = findPermissions(permName);
			exist = false;
			for(Permission p : perms) {
				if(p.getObjectName().equalsIgnoreCase(permName)) {
					exist = true;
				}
			}
			if(!exist) throw new NoSuchPermissionExceptionResponse("no such permission");
			
			
			for(Permission p : perms) {
				if(p.getObjectName().equalsIgnoreCase(permName)) {
					String opName = p.getOpName();
					rbac.deassignPermissionToRole(roleName, permName, opName);
				}
			}
		}
	}
	
	public List<String> getClientIdentityNames() throws org.fosstrak.ale.exception.SecurityException {
		List<String> toReturn = new ArrayList<String>();
		List<User> listUser = findClients("");
		for(User u : listUser) {
			toReturn.add(u.getUserId());
		}

		return toReturn;
	}
	
	
	
	public void defineClientIdentity(String identityName, ACClientIdentity id) throws org.fosstrak.ale.exception.SecurityException, ClientIdentityValidationExceptionResponse, DuplicateClientIdentityExceptionResponse {
		
		List<User> listUser = findClients(identityName);
		boolean exist = false;
		for(User u : listUser) {
			if(u.getUserId().equals(identityName)) {
				exist = true;
			}
		}
		if(exist) throw new DuplicateClientIdentityExceptionResponse("clientId to define already exists");
		
		ACClientCredential credential = id.getCredentials().getCredential().get(0);
		
		List<String> listRoleNames = null;
		if(id.getRoleNames() != null) {
			listRoleNames = id.getRoleNames().getRoleName();
			
			
			List<Role> rlist = findRoles("");
			for(String roleName : listRoleNames) {
				exist = false;
				for(Role r : rlist) {
					if(r.getName().equalsIgnoreCase(roleName)) {
						exist = true;
					}
				}
				if(!exist) throw new ClientIdentityValidationExceptionResponse("no such role");
			}
			
		}
		
		String password = credential.getOtherAttributes().get(new QName("password"));
		
		defineClientid(identityName, password, listRoleNames);
	}
	
	public void updateClientIdentity(String identityName, ACClientIdentity id) throws org.fosstrak.ale.exception.SecurityException, NoSuchClientIdentityExceptionResponse, ClientIdentityValidationExceptionResponse {
		
		List<User> listUser = findClients(identityName);
		boolean exist = false;
		for(User u : listUser) {
			if(u.getUserId().equals(identityName)) {
				exist = true;
			}
		}
		if(!exist) throw new NoSuchClientIdentityExceptionResponse("no such client");
		
		
		List<String> listRoleNames = null;
		if(id.getRoleNames() != null) {
			listRoleNames = id.getRoleNames().getRoleName();
			
			
			List<Role> rlist = findRoles("");
			for(String roleName : listRoleNames) {
				exist = false;
				for(Role r : rlist) {
					if(r.getName().equalsIgnoreCase(roleName)) {
						exist = true;
					}
				}
				if(!exist) throw new ClientIdentityValidationExceptionResponse("no such role");
			}
			
		}
		
		
		
		
		
		
		updateClientid(identityName, 
				id.getCredentials().getCredential().get(0).getOtherAttributes().get(new QName("password")), 
				id.getRoleNames().getRoleName());
		
	}
	
	public ACClientIdentity getClientIdentity(String identityName) throws org.fosstrak.ale.exception.SecurityException, NoSuchClientIdentityExceptionResponse {
		List<User> listUser = findClients(identityName);
		
		boolean exist = false;
		for(User u : listUser) {
			if(u.getUserId().equals(identityName)) {
				exist = true;
			}
		}
		if(!exist) throw new NoSuchClientIdentityExceptionResponse("no such client");
		
		
		
		if(listUser != null && listUser.get(0) != null && listUser.get(0).getUserId().equals(identityName)) {
			User u = listUser.get(0);
			
			ACClientIdentity toReturn = new ACClientIdentity();
			toReturn.setRoleNames(new RoleNames());
			toReturn.setCredentials(new Credentials());
			ACClientCredential e = new ACClientCredential();
			//e.getOtherAttributes().put(new QName("password"), u.getPassword().toString());
			toReturn.getCredentials().getCredential().add(e);
			
			for(String ur : u.getRoles()) {
				toReturn.getRoleNames().getRoleName().add(ur);	
			}
			
			return toReturn;
		}
		return null;
	}
	
	public List<String> getClientPermissionNames(String clientId) throws org.fosstrak.ale.exception.SecurityException, ImplementationExceptionResponse {
		throw new ImplementationExceptionResponse("not supported");
		/*
		List<String> toReturn = new ArrayList<String>();
		
		List<User> listUser = findClients(clientId);
		for(User u : listUser) {
			for(UserRole r : u.getRoles()) {
				r.get
			}
			//toReturn.getString().add(u.getRoles());
		}
		return toReturn;
		*/
	}
	
	public void undefineClientIdentity(String identityName) throws org.fosstrak.ale.exception.SecurityException, NoSuchClientIdentityExceptionResponse {
		List<User> listUser = findClients(identityName);
		
		boolean exist = false;
		for(User u : listUser) {
			if(u.getUserId().equals(identityName)) {
				exist = true;
			}
		}
		if(!exist) throw new NoSuchClientIdentityExceptionResponse("no such client");
		undefineClientid(identityName);
	}
	
	public void addRoles(String identityName, List<String> roleNames) throws org.fosstrak.ale.exception.SecurityException, NoSuchClientIdentityExceptionResponse, NoSuchRoleExceptionResponse {
		
		List<User> listUser = findClients(identityName);
		boolean exist = false;
		for(User u : listUser) {
			if(u.getUserId().equalsIgnoreCase(identityName)) {
				exist = true;
			}
		}
		if(!exist) throw new NoSuchClientIdentityExceptionResponse("no such client");
		
		for(String roleName : roleNames) {
			List<Role> listRole = findRoles(roleName);
			exist = false;
			for(Role r : listRole) {
				if(r.getName().equalsIgnoreCase(roleName)) {
					exist = true;
				}
			}
			if(!exist) throw new NoSuchRoleExceptionResponse("no such role");
		}
		
		for(String roleName : roleNames) {
			assignRoleToClientid(identityName, roleName);	
		}
	}
	
	public void removeRoles(String idName, List<String> roleNames) throws org.fosstrak.ale.exception.SecurityException, NoSuchClientIdentityExceptionResponse {
		List<User> listUser = findClients(idName);
		boolean exist = false;
		for(User u : listUser) {
			if(u.getUserId().equalsIgnoreCase(idName)) {
				exist = true;
			}
		}
		if(!exist) throw new NoSuchClientIdentityExceptionResponse("no such client");
		
		for(String roleName : roleNames) {
			deassignRoleToClientid(idName, roleName);	
		}
	}
	
	public void setRoles(String identityName, List<String> roleNames) throws UnsupportedOperationExceptionResponse {
		throw new UnsupportedOperationExceptionResponse();
	}
	
	
	private void defineClientid(String idName, String password, List<String> listRoleNames) throws org.fosstrak.ale.exception.SecurityException {
		
		rbac.defineClientid(idName, password, listRoleNames);
	}

	
	private void undefineClientid(String idName) throws org.fosstrak.ale.exception.SecurityException {
		
		rbac.undefineClientid(idName);
	}

	
	private void defineRole(String roleName) throws org.fosstrak.ale.exception.SecurityException {
		rbac.defineRole(roleName);
	}

	
	private void deassignPermissionToRole(String roleName, String permObjName, String permOpName) throws org.fosstrak.ale.exception.SecurityException {
		rbac.deassignPermissionToRole(roleName, permObjName, permOpName);
	}

	
	private void assignRoleToClientid(String userId, String roleName) throws org.fosstrak.ale.exception.SecurityException {
		rbac.assignRoleToClientid(userId, roleName);
	}
	
	private void deassignRoleToClientid(String userId, String roleName) throws org.fosstrak.ale.exception.SecurityException {
		rbac.deassignRoleToClientid(userId, roleName);
	}
	
	private List<org.fosstrak.ale.server.ac.User> findClients(String clientName) throws org.fosstrak.ale.exception.SecurityException {
		return rbac.findUsers(clientName);
	}
	private List<org.fosstrak.ale.server.ac.Role> findRoles(String roleName) throws org.fosstrak.ale.exception.SecurityException {
		return rbac.findRoles(roleName);
	}
	
	private List<org.fosstrak.ale.server.ac.Permission> findPermissions(String permName) throws org.fosstrak.ale.exception.SecurityException {
		return rbac.findPermissions(permName, "");
	}
	
	private List<org.fosstrak.ale.server.ac.Permission> findPermissionsByOpname(String opName) throws org.fosstrak.ale.exception.SecurityException {
		return rbac.findPermissions("", opName);
	}
	
	private void updateClientid(String idName, String password, List<String> listRoleNames) throws org.fosstrak.ale.exception.SecurityException {
		
		rbac.updateClientid(idName, password, listRoleNames);
	}
	
	private void updatePermission(String permObjName, String permOpName, Set<String> roles) throws org.fosstrak.ale.exception.SecurityException {
		rbac.updatePermission(permObjName, permOpName, roles);
	}
	
	
	
	public boolean login(String userId, String password) throws org.fosstrak.ale.exception.SecurityException {
		if(aleSettings.getFortressEnable().equalsIgnoreCase("true")) {
			if(rbac == null) rbac = new FortressRBACClient();
		} else {
			if(rbac == null) rbac = new LocalRBAC();
		}
		rbac.createSession(userId, password);
		setLoggedInId(userId);
		return true;
	}
	public void checkAccess(String apiName, String methodName) throws org.fosstrak.ale.exception.SecurityException {
		
		if(aleSettings.getFortressEnable().equalsIgnoreCase("true")) {
			if(loggedInId.equals("admin")) return;
			
			List<String> listOp = getSupportedOperations();
			if(listOp.contains("*")) {
				return;
			}
			String permOpName = apiName+"."+methodName;
			if(listOp.contains(permOpName)) {
				return;
			}
			throw new org.fosstrak.ale.exception.SecurityException(permOpName+" is not authorized for the user "+loggedInId);
			/*
			if(loggedInId.equals("admin")) return;
			
			List<Permission> permList = findPermissionsByOpname(permOpName);
			boolean permExist = false;
			System.out.println("permList:"+findPermissionsByOpname("").get(0));
			for(Permission p : permList) {
				
				if(permOpName.equalsIgnoreCase(p.getOpName())) {
					String permObjName = p.getObjectName();
					try {
						if(fortress.checkAccess(permObjName, "*")) return;
						if(!fortress.checkAccess(permObjName, permOpName)) {
							throw new org.fosstrak.ale.exception.SecurityException(permObjName+"."+permOpName+" is not authorized for the user "+loggedInId);
						}
						permExist = true;
					} catch (SecurityException e) {
						throw new org.fosstrak.ale.exception.SecurityException(e.getMessage());
					}
				}
			}
			if(!permExist) {
				throw new org.fosstrak.ale.exception.SecurityException(apiName+"."+methodName+" is not authorized for the user "+loggedInId);
			}
			*/
		}
		return;
	}
	public static ALEACImpl getInstance() {
		return ALEApplicationContext.getBean(ALEACImpl.class);
	}


	public String getLoggedInId() {
		return loggedInId;
	}


	public void setLoggedInId(String loggedInId) {
		this.loggedInId = loggedInId;
	}
	
	
	public String getStandardVersion() {
		return "1.1";
	}
	
	public String getVendorVersion() {
		return "";
	}
	
	public List<String> getSupportedOperations() {
		List<String> toReturn = new ArrayList<String>();
		try {			
			List<User> listUser = rbac.findUsers(loggedInId);
			for(User u : listUser) {
				if(u.getUserId().equals(loggedInId)) {
					for(String ur : u.getRoles()) {
						String roleName = ur;
						
						List<Permission> listPerm = rbac.findPermissions("", "");
						for(Permission p : listPerm) {
							if(p.getRoles().contains(roleName)) {
								toReturn.add(p.getOpName());
							}
						}
					}
				}
			}

		} catch (org.fosstrak.ale.exception.SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		toReturn.add("getStandardVersion");
		toReturn.add("getVendorVersion");
		toReturn.add("getSupportedOperations");
		return toReturn;
	}
}