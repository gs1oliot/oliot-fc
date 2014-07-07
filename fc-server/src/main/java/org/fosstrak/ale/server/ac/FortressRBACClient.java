/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */


package org.fosstrak.ale.server.ac;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import us.jts.fortress.AccessMgr;
import us.jts.fortress.AccessMgrFactory;
import us.jts.fortress.AdminMgr;
import us.jts.fortress.AdminMgrFactory;
import us.jts.fortress.ReviewMgr;
import us.jts.fortress.ReviewMgrFactory;

import org.fosstrak.ale.exception.SecurityException;

import us.jts.fortress.rbac.PermObj;
import us.jts.fortress.rbac.Permission;
import us.jts.fortress.rbac.Role;
import us.jts.fortress.rbac.Session;
import us.jts.fortress.rbac.User;
import us.jts.fortress.rbac.UserRole;

/**
 * Fortress RBAC client
 * @author Janggwan Im
 *
 */

public class FortressRBACClient implements RoleBasedAccessController {
	
	private AdminMgr adminMgr = null;
	
	private ReviewMgr rm = null;
	
	private AccessMgr accessMgr = null;
    private Session session = null;
	
	public FortressRBACClient() {
		
		try
        {
            adminMgr = AdminMgrFactory.createInstance("HOME");
            rm = ReviewMgrFactory.createInstance("HOME");
            accessMgr = AccessMgrFactory.createInstance("HOME");
        }
        catch (us.jts.fortress.SecurityException e)
        {
            e.printStackTrace();
        }
        
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.ale.server.ac.RoleBasedAccessController#updateClientid(java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	public void updateClientid(String userId, String password, List<String> roleNames) throws SecurityException {
		try {
		//FIXME: initial role assignment does not work
		User ue = new User();
		ue.setUserId(userId);
		ue.setPassword(password.toCharArray());
		ue.setOu("fc-client");
		
		List<UserRole> listRole = new ArrayList<UserRole>();
		if(roleNames != null) {
			for(String roleName : roleNames) {
				UserRole role = new UserRole();
				role.setName(roleName);
				role.setUserId(userId);
				listRole.add(role);
			}
			ue.setRoles(listRole);
		}
		
			adminMgr.updateUser(ue);
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}
		
	}
	/* (non-Javadoc)
	 * @see org.fosstrak.ale.server.ac.RoleBasedAccessController#updatePermission(java.lang.String, java.lang.String, java.util.Set)
	 */
	@Override
	public void updatePermission(String permObjName, String permOpName, Set<String> roles) throws SecurityException {

		Permission perm = new Permission();
		perm.setObjectName(permObjName);
		perm.setOpName(permOpName);
		perm.setRoles(roles);
		try {
			perm = adminMgr.updatePermission(perm);
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}
		        
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.ale.server.ac.RoleBasedAccessController#defineClientid(java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	public void defineClientid(String userId, String password, List<String> roleNames) throws SecurityException {
		
		//FIXME: initial role assignment does not work
		User ue = new User();
		ue.setUserId(userId);
		ue.setPassword(password.toCharArray());
		ue.setOu("fc-client");
		
		List<UserRole> listRole = new ArrayList<UserRole>();
		if(roleNames != null) {
			for(String roleName : roleNames) {
				UserRole role = new UserRole();
				role.setName(roleName);
				role.setUserId(userId);
				listRole.add(role);
			}
			ue.setRoles(listRole);
		}
		try {
			adminMgr.addUser(ue);
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}
		
	}

	
	/* (non-Javadoc)
	 * @see org.fosstrak.ale.server.ac.RoleBasedAccessController#undefineClientid(java.lang.String)
	 */
	@Override
	public void undefineClientid(String userId) throws SecurityException {
		User ue = new User();
		ue.setUserId(userId);
		try {
			adminMgr.deleteUser(ue);
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}

	}

	
	/* (non-Javadoc)
	 * @see org.fosstrak.ale.server.ac.RoleBasedAccessController#defineRole(java.lang.String)
	 */
	@Override
	public void defineRole(String roleName) throws SecurityException {

		Role re = new Role();
		re.setName(roleName);
		try {
			adminMgr.addRole(re);
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}
	}

	
	/* (non-Javadoc)
	 * @see org.fosstrak.ale.server.ac.RoleBasedAccessController#undefineRole(java.lang.String)
	 */
	@Override
	public void undefineRole(String roleName) throws SecurityException {

		Role re = new Role();
		re.setName(roleName);
		try {
			adminMgr.deleteRole(re);
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}

	}

	
	/* (non-Javadoc)
	 * @see org.fosstrak.ale.server.ac.RoleBasedAccessController#definePermission(java.lang.String, java.lang.String)
	 */
	@Override
	public void definePermission(String permObjName, String permOpName) throws SecurityException {

		Permission perm = new Permission();
		perm.setObjectName(permObjName);
		perm.setOpName(permOpName);
		try {
			perm = adminMgr.addPermission(perm);
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}
		        
	}

	
	/* (non-Javadoc)
	 * @see org.fosstrak.ale.server.ac.RoleBasedAccessController#undefinePermission(java.lang.String, java.lang.String)
	 */
	@Override
	public void undefinePermission(String permObjName, String permOpName) throws SecurityException {
		/*
		PermObj pObj = new PermObj();
		pObj.setObjectName(permObjName);
		adminMgr.deletePermObj(pObj);
		*/
		Permission p = new Permission();
		p.setObjectName(permObjName);
		p.setOpName(permOpName);
		try {
			adminMgr.deletePermission(p);
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}
	}

	
	/* (non-Javadoc)
	 * @see org.fosstrak.ale.server.ac.RoleBasedAccessController#assignPermissionToRole(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void assignPermissionToRole(String roleName, String permObjName, String opName) throws SecurityException {
		
		Permission pOp = new Permission(permObjName);
		pOp.setOpName(opName);
		try {
			adminMgr.grantPermission(pOp, new Role(roleName));
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}

	}

	
	/* (non-Javadoc)
	 * @see org.fosstrak.ale.server.ac.RoleBasedAccessController#deassignPermissionToRole(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void deassignPermissionToRole(String roleName, String permObjName, String permOpName) throws SecurityException {
	
		Permission pOp = new Permission(permObjName, permOpName);
		try {
			adminMgr.revokePermission(pOp, new Role(roleName));
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}
	
	}

	
	/* (non-Javadoc)
	 * @see org.fosstrak.ale.server.ac.RoleBasedAccessController#assignRoleToClientid(java.lang.String, java.lang.String)
	 */
	@Override
	public void assignRoleToClientid(String userId, String roleName) throws SecurityException {

    	UserRole uRole = new UserRole();
        uRole.setUserId(userId);
        uRole.setName(roleName);
		try {
			adminMgr.assignUser(uRole);
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}

	}

	
	/* (non-Javadoc)
	 * @see org.fosstrak.ale.server.ac.RoleBasedAccessController#deassignRoleToClientid(java.lang.String, java.lang.String)
	 */
	@Override
	public void deassignRoleToClientid(String userId, String roleName) throws SecurityException {
		
		UserRole uRole = new UserRole();
		uRole.setUserId(userId);
		uRole.setName(roleName);
		try {
			adminMgr.deassignUser(uRole);
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}
	}
	/* (non-Javadoc)
	 * @see org.fosstrak.ale.server.ac.RoleBasedAccessController#addPermObj(java.lang.String)
	 */
	@Override
	public void addPermObj(String objName) throws SecurityException {
		PermObj pObj = new PermObj();
		pObj.setObjectName(objName);
		pObj.setOu("fc-server");
		try {
			adminMgr.addPermObj(pObj);
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}
	}
	/* (non-Javadoc)
	 * @see org.fosstrak.ale.server.ac.RoleBasedAccessController#deletePermObj(java.lang.String)
	 */
	@Override
	public void deletePermObj(String objName) throws SecurityException {
		PermObj pObj = new PermObj();
		pObj.setObjectName(objName);
		pObj.setOu("fc-server");
		try {
			adminMgr.deletePermObj(pObj);
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}
	}
	public List<org.fosstrak.ale.server.ac.User> findUsers(String userName) throws SecurityException {
		User ue = new User();
		ue.setUserId(userName);
		
		List<User> listUser = null;
		try {
			listUser = rm.findUsers(ue);
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}
		
		List<org.fosstrak.ale.server.ac.User> toReturn = new ArrayList<org.fosstrak.ale.server.ac.User>();
		for(User u : listUser) {
			List<org.fosstrak.ale.server.ac.Role> toAdd = new ArrayList<org.fosstrak.ale.server.ac.Role>();
			
			toReturn.add(new org.fosstrak.ale.server.ac.User(u.getUserId(), null));	
		}
		
		return toReturn;
		
     
	}
	public List<org.fosstrak.ale.server.ac.Role> findRoles(String roleName) throws SecurityException {
		try {
			List<Role> listRole = rm.findRoles(roleName);
			
			List<org.fosstrak.ale.server.ac.Role> toReturn = new ArrayList<org.fosstrak.ale.server.ac.Role>();
			
			for(Role r : listRole) {
				org.fosstrak.ale.server.ac.Role toAdd = new org.fosstrak.ale.server.ac.Role(r.getName());
				
				List<org.fosstrak.ale.server.ac.Permission> listPerm = findPermissions("", "");
				for(org.fosstrak.ale.server.ac.Permission p : listPerm) {
					if(p.getRoles().contains(roleName)) {
						toAdd.addPerm(p);
					}
				}
				
				toReturn.add(toAdd);
			}
			
			return toReturn;
			
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}
		
	}
	public List<org.fosstrak.ale.server.ac.Permission> findPermissions(String permObjName, String permOpName) throws SecurityException {
		Permission pe = new Permission();
		pe.setObjectName(permObjName);
		pe.setOpName(permOpName);
		
		try {
			List<Permission> listPerm = rm.findPermissions(pe);
			List<org.fosstrak.ale.server.ac.Permission> toReturn = new ArrayList<org.fosstrak.ale.server.ac.Permission>();
			
			for(Permission p : listPerm) {
				toReturn.add(new org.fosstrak.ale.server.ac.Permission(p.getObjectName(), p.getOpName(), p.getRoles()));
			}
			
			return toReturn;
			
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}
		
		
	}
	/* (non-Javadoc)
	 * @see org.fosstrak.ale.server.ac.RoleBasedAccessController#createSession(java.lang.String, java.lang.String)
	 */
	@Override
	public void createSession(String userId, String password) throws SecurityException {
		try {
			session = accessMgr.createSession(new User(userId, password.toCharArray()), false);
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}
	}
	/* (non-Javadoc)
	 * @see org.fosstrak.ale.server.ac.RoleBasedAccessController#checkAccess(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean checkAccess(String objName, String opName) throws SecurityException {
		try {
			return accessMgr.checkAccess(session, new Permission(objName, opName));
		} catch (us.jts.fortress.SecurityException e) {
			e.printStackTrace();
			throw new SecurityException();
		}
	}
	
	public static void main(String[] args) throws SecurityException {
		RoleBasedAccessController client = new FortressRBACClient();
		
		/*
		List<String> roleNames = new ArrayList<String>();
		roleNames.add("admin");
		client.defineClientid("user1", "1111", roleNames);
		*/
		//client.undefineClientid("admin");
		
		//client.defineRole("admin");
		//client.undefineRole("tester");
		
		//client.assignRoleToClientid("user1", "user");
		//client.deassignRoleToClientid("limg00n", "tester");
		
		//client.assignPermissionToRole("user", "ale", "*");
		//client.deassignPermissionToRole("admin", "ale", "all");
		
		
		// ---------------------
		
		
		List<org.fosstrak.ale.server.ac.User> listUser = client.findUsers("");
		for(org.fosstrak.ale.server.ac.User u : listUser) {
			System.out.println(u);
			//client.undefineClientid(u.getUserId());			
		}
		
		
		/*
		List<Role> listRole = client.findRoles("");
		for(Role r : listRole) {
			System.out.println(r.getName()+"\t");
			//client.undefineRole(r.getName());
		}
		
		
		
		List<Permission> listPerm = client.findPermissions("", "");
		for(Permission p : listPerm) {
			System.out.println(p.getObjectName()+"\t"+p.getOpName()+"\t"+p.getRoles());
		}
		*/
		
		
		// -----------------------
		//client.createSession("admin", "1111");
		//System.out.println(client.checkAccess("ale", "*"));
		
	}

	
}

