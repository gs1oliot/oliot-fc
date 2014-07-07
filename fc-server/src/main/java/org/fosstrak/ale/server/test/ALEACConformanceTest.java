/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */

package org.fosstrak.ale.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.fosstrak.ale.exception.SecurityException;
import org.fosstrak.ale.server.ALE;
import org.fosstrak.ale.server.ac.ALEACImpl;
import org.fosstrak.ale.server.cc.ALECC;
import org.fosstrak.ale.server.readers.LogicalReaderManager;
import org.fosstrak.ale.server.tm.ALETM;
import org.fosstrak.ale.util.DeserializerUtil;
import org.fosstrak.ale.wsdl.aleac.epcglobal.ClientIdentityValidationExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.DuplicateClientIdentityExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.DuplicatePermissionExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.DuplicateRoleExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.NoSuchClientIdentityExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.NoSuchPermissionExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.NoSuchRoleExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.PermissionValidationExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.RoleValidationExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.UnsupportedOperationExceptionResponse;
import org.fosstrak.ale.xsd.ale.epcglobal.ACClientCredential;
import org.fosstrak.ale.xsd.ale.epcglobal.ACClientIdentity;
import org.fosstrak.ale.xsd.ale.epcglobal.ACRole;
import org.fosstrak.ale.xsd.ale.epcglobal.LRSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ACClientIdentity.Credentials;
import org.fosstrak.ale.xsd.ale.epcglobal.ACClientIdentity.RoleNames;
import org.fosstrak.ale.xsd.ale.epcglobal.ACPermission;
import org.fosstrak.ale.xsd.ale.epcglobal.ACPermission.Instances;
import org.fosstrak.ale.xsd.ale.epcglobal.ACRole.PermissionNames;
import org.fosstrak.ale.xsd.ale.epcglobal.CCSpec;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ALEACConformanceTest {
	/** logger. */
	private static final Logger LOG = Logger.getLogger(ALEACConformanceTest.class);
	
	static ALEACImpl aleac = null;
	
	static ALE ale = null;
	
	static ALECC alecc = null;
	
	static LogicalReaderManager alelr = null;
	
	static ALETM aletm = null;
	
	@BeforeClass
	public static void beforeClass() throws Exception {

		LOG.info("Initializing Spring context.");
        
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext.xml");
        
        LOG.info("Spring context initialized.");
        
        aleac = (ALEACImpl) applicationContext.getBean("aleac");
        aleac.login("admin", "1111");
        
        ale = (ALE) applicationContext.getBean("ale");
        alecc = (ALECC) applicationContext.getBean("alecc");
        alelr = (LogicalReaderManager) applicationContext.getBean("logicalReaderManager");
        aletm = (ALETM) applicationContext.getBean("aletm");
        
        LRSpec lrspec = DeserializerUtil.deserializeLRSpec(ALEACConformanceTest.class.getResourceAsStream("/lrspecs/LRSpec_A.xml"));
        
        System.out.println("Please connect reader... (timeout: 5000ms)");
        Thread.sleep(5000);
        
        alelr.define("LogicalReader1", lrspec);
        
        for(String permName : aleac.getPermissionNames()) {
        	aleac.undefinePermission(permName);
        }
        for(String roleName : aleac.getRoleNames()) {
        	aleac.undefineRole(roleName);
        }
        for(String userId : aleac.getClientIdentityNames()) {
        	if(!userId.equalsIgnoreCase("admin")) {
        		aleac.undefineClientIdentity(userId);
        	}
        }
	}
	
	@Before
	public void beforeEachTest() {
		
	}
	
	@Test
	public void test_A1() {
		assertEquals("1.1", aleac.getStandardVersion());
		assertEquals("", aleac.getVendorVersion());
	}
	
	@Test
	public void test_A2() {
		// step 1
		List<String> supportedOperations = aleac.getSupportedOperations();
		assertTrue(supportedOperations.contains("getStandardVersion"));
		assertTrue(supportedOperations.contains("getVendorVersion"));
		assertTrue(supportedOperations.contains("getSupportedOperations"));
		
		// step 2 : list subset of methods => TODO
		try {
			aleac.setRoles("Client1", new ArrayList<String>());
			Assert.fail();
		} catch(UnsupportedOperationExceptionResponse e) {
			
		}
		try {
			aleac.setPermissions("perm1", new ArrayList<String>());
			Assert.fail();
		} catch(UnsupportedOperationExceptionResponse e) {
			
		}
		
		// step 3 : no anonymous user => skip
		
		// step 4 : documentation on how at least one client establishes permissions => skip
	}
	
	@Test
	public void test_A3() throws Exception {
		String specName = "CCSpec_A3_31";
		CCSpec spec = DeserializerUtil.deserializeCCSpec(getClass().getResourceAsStream("/ccspecs/CCSpec_A3_31.xml"));
		
		//ACClass acClass = new ACClass();
		ACPermission perm1 = new ACPermission();
		perm1.setPermissionClass("METHOD");
		perm1.setInstances(new Instances());
		perm1.getInstances().getInstance().add("ALE");
		
		ACPermission perm2 = new ACPermission();
		perm2.setPermissionClass("METHOD");
		perm2.setInstances(new Instances());
		perm2.getInstances().getInstance().add("*");
		
		ACPermission perm3 = new ACPermission();
		perm3.setPermissionClass("METHOD");
		perm3.setInstances(new Instances());
		perm3.getInstances().getInstance().add("ALECC.subscribe");
		
		ACClientCredential cred1 = new ACClientCredential();
		cred1.getOtherAttributes().put(new QName("password"), "1111");
		
		ACClientCredential cred2 = new ACClientCredential();
		cred2.getOtherAttributes().put(new QName("password"), "1111");
		
		ACClientIdentity client1 = new ACClientIdentity();
		client1.setCredentials(new Credentials());
		client1.getCredentials().getCredential().add(cred1);
		client1.setRoleNames(new RoleNames());
		client1.getRoleNames().getRoleName().add("role1");
		
		ACClientIdentity client2 = new ACClientIdentity();
		client2.setCredentials(new Credentials());
		client2.getCredentials().getCredential().add(cred1);
		client2.setRoleNames(new RoleNames());
		client2.getRoleNames().getRoleName().add("role2");
		
		ACRole role1 = new ACRole();
		role1.setPermissionNames(new PermissionNames());
		role1.getPermissionNames().getPermissionName().add("perm1");
		
		ACRole role2 = new ACRole();
		role2.setPermissionNames(new PermissionNames());
		role2.getPermissionNames().getPermissionName().add("perm2");
		
		ACPermission perm4 = new ACPermission();
		perm4.setPermissionClass("METHOD");
		perm4.setInstances(new Instances());
		perm4.getInstances().getInstance().add("ALE.subscribe");
		
		ACPermission perm5 = new ACPermission();
		perm5.setPermissionClass("METHOD");
		perm5.setInstances(new Instances());
		perm5.getInstances().getInstance().add("ALELR.update");
		
		ACPermission perm6 = new ACPermission();
		perm6.setPermissionClass("METHOD");
		perm6.setInstances(new Instances());
		perm6.getInstances().getInstance().add("ALETM.defineTMSpec");
		
		// step 1
		try {
			aleac.definePermission("perm1", perm1);
		} catch (DuplicatePermissionExceptionResponse e) {
			Assert.fail();
		}
		
		// step 2
		assertTrue(aleac.getPermissionNames().contains("perm1"));
		
		// step 3
		ACPermission perm1_3 = aleac.getPermission("perm1");
		assertTrue(perm1.getPermissionClass().equalsIgnoreCase(perm1_3.getPermissionClass()));
		assertTrue(perm1.getInstances().getInstance().get(0).equalsIgnoreCase(perm1_3.getInstances().getInstance().get(0)));
		
		// step 4
		try {
			aleac.defineRole("role1", role1);
		} catch (RoleValidationExceptionResponse e1) {
			Assert.fail();
		} catch (DuplicateRoleExceptionResponse e) {
			Assert.fail();
		}
		
		// step 5
		assertTrue(aleac.getRoleNames().contains("role1"));
		
		// step 6
		aleac.getRole("role1");
		
		// step 7 : update permission => skip
		aleac.undefinePermission("perm1");
		try {
			aleac.definePermission("perm1", perm2);
		} catch (DuplicatePermissionExceptionResponse e) {
			Assert.fail();
		}
		List<String> perm1InList = new ArrayList<String>();
		perm1InList.add("perm1");
		aleac.addPermissions("role1", perm1InList);
		
		// step 8
		ACPermission perm2_8 = aleac.getPermission("perm1");
		assertTrue(perm2.getPermissionClass().equalsIgnoreCase(perm2_8.getPermissionClass()));
		assertTrue(perm2.getInstances().getInstance().get(0).equalsIgnoreCase(perm2_8.getInstances().getInstance().get(0)));
		
		// step 9
		try {
			aleac.definePermission("perm2", perm3);
		} catch (DuplicatePermissionExceptionResponse e) {
			Assert.fail();
		}
		
		// step 10
		List<String> permissionNames = new ArrayList<String>();
		permissionNames.add("perm2");
		aleac.addPermissions("role1", permissionNames);
		
		// step 11
		ACRole role1_11 = aleac.getRole("role1");
		assertTrue(role1_11.getPermissionNames().getPermissionName().contains("perm1"));
		assertTrue(role1_11.getPermissionNames().getPermissionName().contains("perm2"));
		
		
		// step 12
		aleac.removePermissions("role1", permissionNames);
		
		// step 13
		ACRole role1_13 = aleac.getRole("role1");
		assertTrue(role1_13.getPermissionNames().getPermissionName().contains("perm1"));
		
		// step 14
		try {
			aleac.defineRole("role2", role2);
		} catch (RoleValidationExceptionResponse e1) {
			Assert.fail();
		} catch (DuplicateRoleExceptionResponse e) {
			Assert.fail();
		}
		
		// step 15
		assertTrue(aleac.getRoleNames().contains("role1"));
		assertTrue(aleac.getRoleNames().contains("role2"));
		
		// step 16
		try {
			aleac.defineClientIdentity("Client1", client1);
		} catch (ClientIdentityValidationExceptionResponse e3) {
			Assert.fail();
		} catch (DuplicateClientIdentityExceptionResponse e) {
			Assert.fail();
		}
		
		// step 17
		assertTrue(aleac.getClientIdentityNames().contains("Client1"));
		
		// step 18
		try {
			ACClientIdentity client1_18 = aleac.getClientIdentity("Client1");
		} catch (NoSuchClientIdentityExceptionResponse e2) {
			Assert.fail();
		}
		//TODO assert (there is bug in Fortress, no role info is returned)
		//assertTrue(client1_18.getCredentials().getCredential().get(0).getOtherAttributes().get(new QName("password")).equalsIgnoreCase("1111"));
		//assertTrue(client1_18.getRoleNames().getRoleName().contains("role1"));
		
		// step 19 : getClientPermissionNames unsupported => skip
		
		// step 20
		try {
			aleac.updateRole("role1", role2);
		} catch (RoleValidationExceptionResponse e1) {
			Assert.fail();
		}
		
		// step 21
		ACRole role1_21 = aleac.getRole("role1");
		assertTrue(role1_21.getPermissionNames().getPermissionName().contains("perm2"));
		
		// step 22 : getClientPermissionNames unsupported => skip
		
		// step 23
		try {
			aleac.updateRole("role1", role1);
		} catch (RoleValidationExceptionResponse e1) {
			Assert.fail();
		}
		
		// step 24
		ACRole role1_24 = aleac.getRole("role1");
		assertTrue(role1_24.getPermissionNames().getPermissionName().contains("perm1"));
		
		// step 25
		try {
			List<String> roleNames_25 = new ArrayList<String>();
			roleNames_25.add("role2");
			aleac.addRoles("Client1", roleNames_25);
		} catch (NoSuchClientIdentityExceptionResponse e) {
			Assert.fail();
		}
		
		// step 26 : getClientPermissionNames unsupported => skip
		
		// step 27
		try {
			ACClientIdentity client1_27 = aleac.getClientIdentity("Client1");
		} catch (NoSuchClientIdentityExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO assert (there is bug in Fortress, no role info is returned)
		//assertTrue(client1_27.getRoleNames().getRoleName().contains("role1"));
		//assertTrue(client1_27.getRoleNames().getRoleName().contains("role2"));
		
		// step 28
		List<String> roleNames_28 = new ArrayList<String>();
		roleNames_28.add("role2");
		try {
			aleac.removeRoles("Client1", roleNames_28);
		} catch (NoSuchClientIdentityExceptionResponse e) {
			Assert.fail();
		}
		
		// step 29 : getClientPermissionNames unsupported => skip
		
		// step 30
		try {
			ACClientIdentity client1_30 = aleac.getClientIdentity("Client1");
		} catch (NoSuchClientIdentityExceptionResponse e) {
			Assert.fail();
		}
		//TODO assert (there is bug in Fortress, no role info is returned)
		//assertTrue(client1_30.getRoleNames().getRoleName().size() == 1);
		//assertTrue(client1_30.getRoleNames().getRoleName().contains("role1"));
		

		// -------------------- step 31~36 : Writing API
		
		/*
		System.out.println(aleac.getPermissionNames());
		System.out.println("perm1: "+aleac.getPermission("perm1").getPermissionClass()+"/"+aleac.getPermission("perm1").getInstances().getInstance());
		System.out.println("perm2: "+aleac.getPermission("perm2").getPermissionClass()+"/"+aleac.getPermission("perm2").getInstances().getInstance());
		System.out.println();
		
		System.out.println(aleac.getRoleNames());
		System.out.println("role1: "+aleac.getRole("role1").getPermissionNames().getPermissionName());
		System.out.println("role2: "+aleac.getRole("role2").getPermissionNames().getPermissionName());
		System.out.println();
		
		System.out.println(aleac.getClientIdentityNames());
		System.out.println("Client1:"+aleac.getClientIdentity("Client1").getRoleNames().getRoleName());
		
		
		System.out.println(aleac.getSupportedOperations());
		*/
		System.out.println(aleac.login("Client1", "1111"));
		

		try {
			alecc.define(specName, spec);			
		} catch(SecurityException e) {
			
		}
		
		aleac.login("admin", "1111");
		List<String> roleList = new ArrayList<String>();
		roleList.add("role1");
		aleac.addRoles("Client1", roleList);
		
		System.out.println(aleac.login("Client1", "1111"));
		
		// step 31 TODO		
		alecc.define(specName, spec);
		
		// step 32
		try {
			aleac.updateClientIdentity("Client1", client2);
		} catch (NoSuchClientIdentityExceptionResponse e) {
			Assert.fail();
		} catch (ClientIdentityValidationExceptionResponse e) {
			Assert.fail();
		}
		
		// step 33
		aleac.updatePermission("perm2", perm3);
		
		// step 34
		
		// step 37~42 : Reading API
		
		
		
		// step 43~48 : LR API
		// step 49~54 : TM API
		// step 55~64 : 
		
		
		

		
	}
	@Test
	public void test_A4() throws SecurityException {
		// step 1
		try {
			aleac.getPermission("unknown_perm");
			Assert.fail();
		} catch(NoSuchPermissionExceptionResponse e) {
			
		}
		
		// step 2
		try {
			ACPermission perm1 = new ACPermission();
			perm1.setPermissionClass("METHOD");
			perm1.setInstances(new Instances());
			perm1.getInstances().getInstance().add("ALE");
			aleac.updatePermission("unknown_perm", perm1);
			Assert.fail();
		} catch (PermissionValidationExceptionResponse e1) {
			Assert.fail();
		} catch (NoSuchPermissionExceptionResponse e) {
			
		}
		
		
		// step 3
		try {
			aleac.undefinePermission("unknown_perm");
			Assert.fail();
		} catch(NoSuchPermissionExceptionResponse e) {
			
		}
		
		// step 4
		
		try {
			List<String> perms = new ArrayList<String>();
			perms.add("unknown_perm");
			aleac.addPermissions("unknown_role", perms);
			Assert.fail();
		} catch(NoSuchPermissionExceptionResponse e) {
			
		} catch(NoSuchRoleExceptionResponse e) {
			
		}
		
		// step 5 : setPermission unsupported => skip
		
		
		// step 6
		try {
			ACPermission perm = new ACPermission();
			perm.setPermissionClass("invalud_name");
			perm.setInstances(new Instances());
			perm.getInstances().getInstance().add("*");
			aleac.definePermission("perm1", perm);
			Assert.fail();
		} catch(PermissionValidationExceptionResponse e) {
			
		} catch (DuplicatePermissionExceptionResponse e) {
			Assert.fail();
		}
		
		// step 7
		try {
			ACPermission perm = new ACPermission();
			perm.setPermissionClass("invalud_name");
			perm.setInstances(new Instances());
			perm.getInstances().getInstance().add("*");
			aleac.updatePermission("perm1", perm);
			Assert.fail();
		} catch(PermissionValidationExceptionResponse e) {
			
		} catch (NoSuchPermissionExceptionResponse e) {
			Assert.fail();
		} 
		
		// step 8
		try {
			ACPermission perm = new ACPermission();
			perm.setPermissionClass("invalud_name");
			perm.setInstances(new Instances());
			perm.getInstances().getInstance().add("invalid_instance_string");
			aleac.definePermission("perm1", perm);
			Assert.fail();
		} catch(PermissionValidationExceptionResponse e) {
			
		} catch (DuplicatePermissionExceptionResponse e) {
			Assert.fail();
		}
		
		// step 9
		try {
			ACPermission perm = new ACPermission();
			perm.setPermissionClass("invalud_name");
			perm.setInstances(new Instances());
			perm.getInstances().getInstance().add("invalid_instance_string");
			aleac.updatePermission("perm1", perm);
			Assert.fail();
		} catch(PermissionValidationExceptionResponse e) {
			
		} catch (NoSuchPermissionExceptionResponse e) {
			Assert.fail();
		}
		
		// step 10
		try {
			ACPermission perm1 = new ACPermission();
			perm1.setPermissionClass("METHOD");
			perm1.setInstances(new Instances());
			perm1.getInstances().getInstance().add("ALE");
			aleac.definePermission("perm1", perm1);
			aleac.definePermission("perm1", perm1);
			Assert.fail();
		} catch(PermissionValidationExceptionResponse e) {
			Assert.fail();
		} catch (DuplicatePermissionExceptionResponse e) {
			
		} finally {
			try {
				aleac.undefinePermission("perm1");
			} catch (NoSuchPermissionExceptionResponse e) {
				Assert.fail();
			}
		}
		
		// step 11
		try {
			ACRole role1 = new ACRole();
			role1.setPermissionNames(new PermissionNames());
			role1.getPermissionNames().getPermissionName().add("perm1");
			aleac.updateRole("unknown_role", role1);
			Assert.fail();
		} catch(NoSuchRoleExceptionResponse e) {
			
		} catch (RoleValidationExceptionResponse e) {
			Assert.fail();
		}
		
		// step 12
		try {
			aleac.getRole("unknown_role");
			Assert.fail();
		} catch(NoSuchRoleExceptionResponse e) {
			
		}
		
		// step 13
		try {
			aleac.undefineRole("unknown_role");
			Assert.fail();
		} catch(NoSuchRoleExceptionResponse e) {
			
		}
		
		// step 14
		try {
			aleac.addPermissions("unknown_role", new ArrayList<String>());
			Assert.fail();
		} catch(NoSuchRoleExceptionResponse e) {
			
		} catch (NoSuchPermissionExceptionResponse e) {
			Assert.fail();
		}
		
		// step 15 : setPermissions not supported => skip
		
		// step 16
		try {
			aleac.removePermissions("unknown_role", new ArrayList<String>());
			Assert.fail();
		} catch(NoSuchRoleExceptionResponse e) {
			
		} catch (NoSuchPermissionExceptionResponse e) {
			Assert.fail();
		}
		
		// step 17
		try {
			List<String> roleNames = new ArrayList<String>();
			roleNames.add("unknown_role");
			aleac.addRoles("Client1", roleNames);
			Assert.fail();
		} catch(NoSuchRoleExceptionResponse e) {
			
		} catch (NoSuchClientIdentityExceptionResponse e) {
			Assert.fail();
		}
		
		// step 18 : setRoles not supported => skip
		
		// step 19
		try {
			ACRole invalid_role = new ACRole();
			invalid_role.setPermissionNames(new PermissionNames());
			invalid_role.getPermissionNames().getPermissionName().add("invalid_perm");
			aleac.defineRole("invalid_role", invalid_role);
			Assert.fail();
		} catch(RoleValidationExceptionResponse e) {
			
		} catch (DuplicateRoleExceptionResponse e) {
			Assert.fail();
		}
		
		// step 20
		try {
			ACRole invalid_role = new ACRole();
			invalid_role.setPermissionNames(new PermissionNames());
			invalid_role.getPermissionNames().getPermissionName().add("invalid_perm");
			aleac.updateRole("role1", invalid_role);
			Assert.fail();
		} catch(RoleValidationExceptionResponse e) {
			
		} catch (NoSuchRoleExceptionResponse e) {
			Assert.fail();
		}
		
		// step 21
		try {
			ACRole role1 = new ACRole();
			role1.setPermissionNames(new PermissionNames());
			role1.getPermissionNames().getPermissionName().add("perm1");
			aleac.defineRole("role1", role1);
		} catch(DuplicateRoleExceptionResponse e) {
			
		} catch (RoleValidationExceptionResponse e) {
			Assert.fail();
		}
		
		// step 22
		try {
			ACClientCredential cred1 = new ACClientCredential();
			cred1.getOtherAttributes().put(new QName("password"), "1111");
			
			ACClientIdentity client1 = new ACClientIdentity();
			client1.setCredentials(new Credentials());
			client1.getCredentials().getCredential().add(cred1);
			client1.setRoleNames(new RoleNames());
			client1.getRoleNames().getRoleName().add("role1");
			
			aleac.updateClientIdentity("unknown_clientId", client1); 
			Assert.fail();
		} catch(NoSuchClientIdentityExceptionResponse e) {
			
		} catch (ClientIdentityValidationExceptionResponse e) {
			Assert.fail();
		}
		
		// step 23
		try {	
			aleac.getClientIdentity("unknown_client"); 
			Assert.fail();
		} catch(NoSuchClientIdentityExceptionResponse e) {
			
		}
		
		// step 24 : getClientPermissionNames not supported => skip
		
		// step 25
		try {	
			aleac.undefineClientIdentity("unknown_client"); 
			Assert.fail();
		} catch(NoSuchClientIdentityExceptionResponse e) {
			
		}
		
		// step 26
		try {	
			aleac.addRoles("unknown_client", new ArrayList<String>()); 
			Assert.fail();
		} catch(NoSuchClientIdentityExceptionResponse e) {
			
		} catch (NoSuchRoleExceptionResponse e) {
			Assert.fail();
		}
		
		// step 27
		try {	
			aleac.removeRoles("unknown_client", new ArrayList<String>()); 
			Assert.fail();
		} catch(NoSuchClientIdentityExceptionResponse e) {
			
		}
		
		// step 28
		try {
			ACClientCredential cred1 = new ACClientCredential();
			cred1.getOtherAttributes().put(new QName("password"), "1111");
			
			ACClientIdentity client1 = new ACClientIdentity();
			client1.setCredentials(new Credentials());
			client1.getCredentials().getCredential().add(cred1);
			client1.setRoleNames(new RoleNames());
			client1.getRoleNames().getRoleName().add("unknown_role");
			
			aleac.defineClientIdentity("invalid_client", client1);
			Assert.fail();
		} catch(ClientIdentityValidationExceptionResponse e) {
			
		} catch (DuplicateClientIdentityExceptionResponse e) {
			Assert.fail();
		}
		
		// step 29
		try {
			ACClientCredential cred1 = new ACClientCredential();
			cred1.getOtherAttributes().put(new QName("password"), "1111");
			
			ACClientIdentity client1 = new ACClientIdentity();
			client1.setCredentials(new Credentials());
			client1.getCredentials().getCredential().add(cred1);
			client1.setRoleNames(new RoleNames());
			client1.getRoleNames().getRoleName().add("unknown_role");
			
			aleac.updateClientIdentity("Client1", client1);
			Assert.fail();
		} catch(ClientIdentityValidationExceptionResponse e) {
			
		} catch (NoSuchClientIdentityExceptionResponse e) {
			Assert.fail();
		}
		
		// step 30
		try {
			ACClientCredential cred1 = new ACClientCredential();
			cred1.getOtherAttributes().put(new QName("password"), "1111");
			
			ACClientIdentity client1 = new ACClientIdentity();
			client1.setCredentials(new Credentials());
			client1.getCredentials().getCredential().add(cred1);
			client1.setRoleNames(new RoleNames());
			client1.getRoleNames().getRoleName().add("unknown_role");
			
			aleac.defineClientIdentity("Client1", client1);
			Assert.fail();
		} catch(ClientIdentityValidationExceptionResponse e) {
			Assert.fail();
		} catch (DuplicateClientIdentityExceptionResponse e) {
			
		}
		
	}
}
