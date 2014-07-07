package org.fosstrak.ale.wsdl.aleac.epcglobal;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.fosstrak.ale.exception.SecurityException;
import org.fosstrak.ale.server.ac.ALEACImpl;
import org.fosstrak.ale.xsd.ale.epcglobal.ACClientCredential;
import org.fosstrak.ale.xsd.ale.epcglobal.ACClientIdentity;
import org.fosstrak.ale.xsd.ale.epcglobal.ACClientIdentity.Credentials;
import org.fosstrak.ale.xsd.ale.epcglobal.ACClientIdentity.RoleNames;
import org.fosstrak.ale.xsd.ale.epcglobal.ACPermission;
import org.fosstrak.ale.xsd.ale.epcglobal.ACPermission.Instances;
import org.fosstrak.ale.xsd.ale.epcglobal.ACRole;
import org.fosstrak.ale.xsd.ale.epcglobal.ACRole.PermissionNames;
import org.springframework.beans.factory.annotation.Autowired;

import us.jts.fortress.rbac.Permission;
import us.jts.fortress.rbac.Role;
import us.jts.fortress.rbac.User;
import us.jts.fortress.rbac.UserRole;

public class ALEACServicePortTypeImpl implements ALEACServicePortType {
	
	private static final Logger log = Logger.getLogger(ALEACServicePortTypeImpl.class.getName());
	
	@Autowired
	private ALEACImpl aleac;
	
	private String authScope = "aleac";

	@Override
	@WebResult(name = "AddPermissionsResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "addPermissionsReturn")
	@WebMethod
	public AddPermissionsResult addPermissions(
			@WebParam(partName = "parms", name = "AddPermissions", targetNamespace = "urn:epcglobal:aleac:wsdl:1") AddPermissions arg0)
			throws NoSuchRoleExceptionResponse,
			UnsupportedOperationExceptionResponse, SecurityExceptionResponse,
			ImplementationExceptionResponse, NoSuchPermissionExceptionResponse {
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			aleac.addPermissions(arg0.getRoleName(), arg0.getPermissionNames().getPermissionName());
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		
		return new AddPermissionsResult();
	}

	@Override
	@WebResult(name = "AddRolesResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "addRolesReturn")
	@WebMethod
	public AddRolesResult addRoles(
			@WebParam(partName = "parms", name = "AddRoles", targetNamespace = "urn:epcglobal:aleac:wsdl:1") AddRoles arg0)
			throws NoSuchClientIdentityExceptionResponse,
			NoSuchRoleExceptionResponse, UnsupportedOperationExceptionResponse,
			SecurityExceptionResponse, ImplementationExceptionResponse {
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			aleac.addRoles(arg0.getIdentityName(), arg0.getRoleNames().getRoleName());
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		
		return new AddRolesResult();
	}

	@Override
	@WebResult(name = "DefineClientIdentityResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "defineClientIdentityReturn")
	@WebMethod
	public DefineClientIdentityResult defineClientIdentity(
			@WebParam(partName = "parms", name = "DefineClientIdentity", targetNamespace = "urn:epcglobal:aleac:wsdl:1") DefineClientIdentity arg0)
			throws UnsupportedOperationExceptionResponse,
			SecurityExceptionResponse, ImplementationExceptionResponse,
			ClientIdentityValidationExceptionResponse,
			DuplicateClientIdentityExceptionResponse {
		log.debug("defineClientIdentity whose userId: "+arg0.getIdentityName());
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			aleac.defineClientIdentity(arg0.getIdentityName(), arg0.getId());
			
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		
		return new DefineClientIdentityResult();
	}

	@Override
	@WebResult(name = "DefinePermissionResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "definePermissionReturn")
	@WebMethod
	public DefinePermissionResult definePermission(
			@WebParam(partName = "parms", name = "DefinePermission", targetNamespace = "urn:epcglobal:aleac:wsdl:1") DefinePermission arg0)
			throws UnsupportedOperationExceptionResponse,
			PermissionValidationExceptionResponse, SecurityExceptionResponse,
			ImplementationExceptionResponse,
			DuplicatePermissionExceptionResponse {
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			aleac.definePermission(arg0.getPermName(), arg0.getPerm());
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		
		return new DefinePermissionResult();
	}

	@Override
	@WebResult(name = "DefineRoleResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "defineRoleReturn")
	@WebMethod
	public DefineRoleResult defineRole(
			@WebParam(partName = "parms", name = "DefineRole", targetNamespace = "urn:epcglobal:aleac:wsdl:1") DefineRole arg0)
			throws UnsupportedOperationExceptionResponse,
			DuplicateRoleExceptionResponse, RoleValidationExceptionResponse,
			SecurityExceptionResponse, ImplementationExceptionResponse {
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			aleac.defineRole(arg0.getRoleName(), arg0.getRole());
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		
		return new DefineRoleResult();
	}

	@Override
	@WebResult(name = "GetClientIdentityResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "getClientIdentityReturn")
	@WebMethod
	public ACClientIdentity getClientIdentity(
			@WebParam(partName = "parms", name = "GetClientIdentity", targetNamespace = "urn:epcglobal:aleac:wsdl:1") GetClientIdentity arg0)
			throws NoSuchClientIdentityExceptionResponse,
			UnsupportedOperationExceptionResponse, SecurityExceptionResponse,
			ImplementationExceptionResponse {
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			return aleac.getClientIdentity(arg0.getIdentityName());
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
	}

	@Override
	@WebResult(name = "GetClientIdentityNamesResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "getClientIdentityNamesReturn")
	@WebMethod
	public ArrayOfString getClientIdentityNames(
			@WebParam(partName = "parms", name = "GetClientIdentityNames", targetNamespace = "urn:epcglobal:aleac:wsdl:1") EmptyParms arg0)
			throws UnsupportedOperationExceptionResponse,
			SecurityExceptionResponse, ImplementationExceptionResponse {
		ArrayOfString toReturn = new ArrayOfString();
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> names = aleac.getClientIdentityNames();
			for(String name : names) {
				toReturn.getString().add(name);
			}
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		return toReturn;
	}

	@Override
	@WebResult(name = "GetClientPermissionNamesResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "getClientPermissionNamesReturn")
	@WebMethod
	public ArrayOfString getClientPermissionNames(
			@WebParam(partName = "parms", name = "GetClientPermissionNames", targetNamespace = "urn:epcglobal:aleac:wsdl:1") GetClientPermissionNames arg0)
			throws NoSuchClientIdentityExceptionResponse,
			UnsupportedOperationExceptionResponse, SecurityExceptionResponse,
			ImplementationExceptionResponse {
		ArrayOfString toReturn = new ArrayOfString();
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			
			List<String> permNames = aleac.getClientPermissionNames(arg0.getIdentityName());
			for(String name : permNames) {
				toReturn.getString().add(name);
			}
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		return toReturn;
	}

	@Override
	@WebResult(name = "GetPermissionResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "getPermissionReturn")
	@WebMethod
	public ACPermission getPermission(
			@WebParam(partName = "parms", name = "GetPermission", targetNamespace = "urn:epcglobal:aleac:wsdl:1") GetPermission arg0)
			throws UnsupportedOperationExceptionResponse,
			SecurityExceptionResponse, ImplementationExceptionResponse,
			NoSuchPermissionExceptionResponse {
		
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			return aleac.getPermission(arg0.getPermName());
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
	}

	@Override
	@WebResult(name = "GetPermissionNamesResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "getPermissionNamesReturn")
	@WebMethod
	public ArrayOfString getPermissionNames(
			@WebParam(partName = "parms", name = "GetPermissionNames", targetNamespace = "urn:epcglobal:aleac:wsdl:1") EmptyParms arg0)
			throws UnsupportedOperationExceptionResponse,
			SecurityExceptionResponse, ImplementationExceptionResponse {
		ArrayOfString toReturn = new ArrayOfString();
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> names = aleac.getPermissionNames();
			for(String name : names) {
				toReturn.getString().add(name);
			}
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		return toReturn;
	}

	@Override
	@WebResult(name = "GetRoleResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "getRoleReturn")
	@WebMethod
	public ACRole getRole(
			@WebParam(partName = "parms", name = "GetRole", targetNamespace = "urn:epcglobal:aleac:wsdl:1") GetRole arg0)
			throws NoSuchRoleExceptionResponse,
			UnsupportedOperationExceptionResponse, SecurityExceptionResponse,
			ImplementationExceptionResponse {
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			return aleac.getRole(arg0.getRoleName());
			
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
	}

	@Override
	@WebResult(name = "GetRoleNamesResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "getRoleNamesReturn")
	@WebMethod
	public ArrayOfString getRoleNames(
			@WebParam(partName = "parms", name = "GetRoleNames", targetNamespace = "urn:epcglobal:aleac:wsdl:1") EmptyParms arg0)
			throws UnsupportedOperationExceptionResponse,
			SecurityExceptionResponse, ImplementationExceptionResponse {
		ArrayOfString toReturn = new ArrayOfString();
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			
			List<String> names = aleac.getRoleNames();
			for(String name : names) {
				toReturn.getString().add(name);
			}
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		return toReturn;
	}

	@Override
	@WebResult(name = "GetStandardVersionResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "getStandardVersionReturn")
	@WebMethod
	public String getStandardVersion(
			@WebParam(partName = "parms", name = "GetStandardVersion", targetNamespace = "urn:epcglobal:aleac:wsdl:1") EmptyParms arg0)
			throws ImplementationExceptionResponse {
		return aleac.getStandardVersion();
	}

	@Override
	@WebResult(name = "GetSupportedOperationsResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "getSupportedOperationsReturn")
	@WebMethod
	public ArrayOfString getSupportedOperations(
			@WebParam(partName = "parms", name = "GetSupportedOperations", targetNamespace = "urn:epcglobal:aleac:wsdl:1") EmptyParms arg0)
			throws ImplementationExceptionResponse {
		ArrayOfString aos = new ArrayOfString();
		List<String> ops = aleac.getSupportedOperations();
		for(String name : ops) {
			aos.getString().add(name);	
		}
		return aos;
	}

	@Override
	@WebResult(name = "GetVendorVersionResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "getVendorVersionReturn")
	@WebMethod
	public String getVendorVersion(
			@WebParam(partName = "parms", name = "GetVendorVersion", targetNamespace = "urn:epcglobal:aleac:wsdl:1") EmptyParms arg0)
			throws ImplementationExceptionResponse {
		return aleac.getVendorVersion();
	}

	@Override
	@WebResult(name = "RemovePermissionsResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "removePermissionsReturn")
	@WebMethod
	public RemovePermissionsResult removePermissions(
			@WebParam(partName = "parms", name = "RemovePermissions", targetNamespace = "urn:epcglobal:aleac:wsdl:1") RemovePermissions arg0)
			throws NoSuchRoleExceptionResponse,
			UnsupportedOperationExceptionResponse, SecurityExceptionResponse,
			ImplementationExceptionResponse {
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			aleac.removePermissions(arg0.getRoleName(), arg0.getPermissionNames().getPermissionName());
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		} catch (NoSuchPermissionExceptionResponse e) {
			throw new ImplementationExceptionResponse(e.getMessage()); 
		}
		
		return new RemovePermissionsResult();
	}

	@Override
	@WebResult(name = "RemoveRolesResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "removeRolesReturn")
	@WebMethod
	public RemoveRolesResult removeRoles(
			@WebParam(partName = "parms", name = "RemoveRoles", targetNamespace = "urn:epcglobal:aleac:wsdl:1") RemoveRoles arg0)
			throws NoSuchClientIdentityExceptionResponse,
			UnsupportedOperationExceptionResponse, SecurityExceptionResponse,
			ImplementationExceptionResponse {
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			aleac.removeRoles(arg0.getIdentityName(), arg0.getRoleNames().getRoleName());
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		
		return new RemoveRolesResult();
	}

	@Override
	@WebResult(name = "SetPermissionsResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "setPermissionsReturn")
	@WebMethod
	public SetPermissionsResult setPermissions(
			@WebParam(partName = "parms", name = "SetPermissions", targetNamespace = "urn:epcglobal:aleac:wsdl:1") SetPermissions arg0)
			throws NoSuchRoleExceptionResponse,
			UnsupportedOperationExceptionResponse, SecurityExceptionResponse,
			ImplementationExceptionResponse, NoSuchPermissionExceptionResponse {
		//aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
		throw new ImplementationExceptionResponse();
	}

	@Override
	@WebResult(name = "SetRolesResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "setRolesReturn")
	@WebMethod
	public SetRolesResult setRoles(
			@WebParam(partName = "parms", name = "SetRoles", targetNamespace = "urn:epcglobal:aleac:wsdl:1") SetRoles arg0)
			throws NoSuchClientIdentityExceptionResponse,
			NoSuchRoleExceptionResponse, UnsupportedOperationExceptionResponse,
			SecurityExceptionResponse, ImplementationExceptionResponse {
		//aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
		throw new ImplementationExceptionResponse();
	}

	@Override
	@WebResult(name = "UndefineClientIdentityResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "undefineClientIdentityReturn")
	@WebMethod
	public UndefineClientIdentityResult undefineClientIdentity(
			@WebParam(partName = "parms", name = "UndefineClientIdentity", targetNamespace = "urn:epcglobal:aleac:wsdl:1") UndefineClientIdentity arg0)
			throws NoSuchClientIdentityExceptionResponse,
			UnsupportedOperationExceptionResponse, SecurityExceptionResponse,
			ImplementationExceptionResponse {
		log.debug("undefineClientIdentity whose userId: "+arg0.getIdentityName());
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			aleac.undefineClientIdentity(arg0.getIdentityName());
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		
		return new UndefineClientIdentityResult();
	}

	@Override
	@WebResult(name = "UndefinePermissionResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "undefinePermissionReturn")
	@WebMethod
	public UndefinePermissionResult undefinePermission(
			@WebParam(partName = "parms", name = "UndefinePermission", targetNamespace = "urn:epcglobal:aleac:wsdl:1") UndefinePermission arg0)
			throws UnsupportedOperationExceptionResponse,
			SecurityExceptionResponse, ImplementationExceptionResponse,
			NoSuchPermissionExceptionResponse {
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			aleac.undefinePermission(arg0.getPermName());
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		
		return new UndefinePermissionResult();
	}

	@Override
	@WebResult(name = "UndefineRoleResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "undefineRoleReturn")
	@WebMethod
	public UndefineRoleResult undefineRole(
			@WebParam(partName = "parms", name = "UndefineRole", targetNamespace = "urn:epcglobal:aleac:wsdl:1") UndefineRole arg0)
			throws NoSuchRoleExceptionResponse,
			UnsupportedOperationExceptionResponse, SecurityExceptionResponse,
			ImplementationExceptionResponse {
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			aleac.undefineRole(arg0.getRoleName());
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse(e.getMessage(), e);
		}
		
		return new UndefineRoleResult();
	}

	@Override
	@WebResult(name = "UpdateClientIdentityResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "updateClientIdentityReturn")
	@WebMethod
	public UpdateClientIdentityResult updateClientIdentity(
			@WebParam(partName = "parms", name = "UpdateClientIdentity", targetNamespace = "urn:epcglobal:aleac:wsdl:1") UpdateClientIdentity arg0)
			throws NoSuchClientIdentityExceptionResponse,
			UnsupportedOperationExceptionResponse, SecurityExceptionResponse,
			ImplementationExceptionResponse,
			ClientIdentityValidationExceptionResponse {
		try {
			aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			aleac.updateClientIdentity(arg0.getIdentityName(), arg0.getId());
		
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new UpdateClientIdentityResult();
	}

	@Override
	@WebResult(name = "UpdatePermissionResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "updatePermissionReturn")
	@WebMethod
	public UpdatePermissionResult updatePermission(
			@WebParam(partName = "parms", name = "UpdatePermission", targetNamespace = "urn:epcglobal:aleac:wsdl:1") UpdatePermission arg0)
			throws UnsupportedOperationExceptionResponse,
			PermissionValidationExceptionResponse, SecurityExceptionResponse,
			ImplementationExceptionResponse, NoSuchPermissionExceptionResponse {
		//aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
		throw new ImplementationExceptionResponse();
	}

	@Override
	@WebResult(name = "UpdateRoleResult", targetNamespace = "urn:epcglobal:aleac:wsdl:1", partName = "updateRoleReturn")
	@WebMethod
	public UpdateRoleResult updateRole(
			@WebParam(partName = "parms", name = "UpdateRole", targetNamespace = "urn:epcglobal:aleac:wsdl:1") UpdateRole arg0)
			throws NoSuchRoleExceptionResponse,
			UnsupportedOperationExceptionResponse,
			RoleValidationExceptionResponse, SecurityExceptionResponse,
			ImplementationExceptionResponse {
		try {
			//aleac.checkAccess(authScope, Thread.currentThread().getStackTrace()[1].getMethodName());
			aleac.updateRole(arg0.getRoleName(), arg0.getRole());
		} catch (SecurityException e) {
			throw new SecurityExceptionResponse();
		}
		
		throw new ImplementationExceptionResponse();
	}

}
