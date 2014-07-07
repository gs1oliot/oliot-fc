/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */

package kr.ac.kaist.resl;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.fosstrak.ale.util.DeserializerUtil;
import org.fosstrak.ale.wsdl.ale.epcglobal.ALEServicePortType;
import org.fosstrak.ale.wsdl.ale.epcglobal.DuplicateSubscriptionExceptionResponse;
import org.fosstrak.ale.wsdl.ale.epcglobal.GetSubscribers;
import org.fosstrak.ale.wsdl.ale.epcglobal.InvalidURIExceptionResponse;
import org.fosstrak.ale.wsdl.ale.epcglobal.NoSuchNameExceptionResponse;
import org.fosstrak.ale.wsdl.ale.epcglobal.NoSuchSubscriberExceptionResponse;
import org.fosstrak.ale.wsdl.ale.epcglobal.Unsubscribe;
import org.fosstrak.ale.wsdl.aleac.epcglobal.ALEACServicePortType;
import org.fosstrak.ale.wsdl.aleac.epcglobal.ClientIdentityValidationExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.DefineClientIdentity;
import org.fosstrak.ale.wsdl.aleac.epcglobal.DefinePermission;
import org.fosstrak.ale.wsdl.aleac.epcglobal.DefineRole;
import org.fosstrak.ale.wsdl.aleac.epcglobal.DuplicateClientIdentityExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.DuplicatePermissionExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.DuplicateRoleExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.GetClientIdentity;
import org.fosstrak.ale.wsdl.aleac.epcglobal.GetPermission;
import org.fosstrak.ale.wsdl.aleac.epcglobal.GetRole;
import org.fosstrak.ale.wsdl.aleac.epcglobal.NoSuchClientIdentityExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.NoSuchPermissionExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.NoSuchRoleExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.PermissionValidationExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.RoleValidationExceptionResponse;
import org.fosstrak.ale.wsdl.aleac.epcglobal.UndefineClientIdentity;
import org.fosstrak.ale.wsdl.aleac.epcglobal.UndefinePermission;
import org.fosstrak.ale.wsdl.aleac.epcglobal.UndefineRole;
import org.fosstrak.ale.wsdl.aleac.epcglobal.UnsupportedOperationExceptionResponse;
import org.fosstrak.ale.wsdl.alecc.epcglobal.ALECCServicePortType;
import org.fosstrak.ale.wsdl.alelr.epcglobal.ALELRServicePortType;
import org.fosstrak.ale.wsdl.alelr.epcglobal.ImmutableReaderExceptionResponse;
import org.fosstrak.ale.wsdl.alelr.epcglobal.ImplementationExceptionResponse;
import org.fosstrak.ale.wsdl.alelr.epcglobal.InUseExceptionResponse;
import org.fosstrak.ale.wsdl.alelr.epcglobal.Undefine;
import org.fosstrak.ale.wsdl.aletm.epcglobal.ALETMServicePortType;
import org.fosstrak.ale.wsdl.aletm.epcglobal.DefineTMSpec;
import org.fosstrak.ale.wsdl.aletm.epcglobal.EmptyParms;
import org.fosstrak.ale.wsdl.aletm.epcglobal.SecurityExceptionResponse;
import org.fosstrak.ale.xsd.ale.epcglobal.ACClientCredential;
import org.fosstrak.ale.xsd.ale.epcglobal.ACClientIdentity;
import org.fosstrak.ale.xsd.ale.epcglobal.ACPermission;
import org.fosstrak.ale.xsd.ale.epcglobal.ACRole;
import org.fosstrak.ale.xsd.ale.epcglobal.ECSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.LRSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.TMFixedFieldListSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.ACClientIdentity.Credentials;
import org.fosstrak.ale.xsd.ale.epcglobal.ACPermission.Instances;
import org.fosstrak.ale.xsd.ale.epcglobal.ACRole.PermissionNames;

public class ApiExample {

	public static ALEServicePortType ale;
	public static ALECCServicePortType alecc;
	public static ALELRServicePortType alelr;
	public static ALEACServicePortType aleac;
	public static ALETMServicePortType aletm;

	public static void main(String[] args) throws org.fosstrak.ale.wsdl.ale.epcglobal.ImplementationExceptionResponse, org.fosstrak.ale.wsdl.alecc.epcglobal.ImplementationExceptionResponse, org.fosstrak.ale.wsdl.aleac.epcglobal.ImplementationExceptionResponse, org.fosstrak.ale.wsdl.aletm.epcglobal.ImplementationExceptionResponse, ImplementationExceptionResponse {
		String userId = "admin";

		aletm = createAletm("http://211.43.180.76:8080/fc-server-1.2.0/services/ALETMService");
		//defineTM("tempHumid", "C:\\Users\\Janggwan\\Desktop\\TMFixedFieldListSpec.xml");
		printTMSpecNames();

		alelr = createAlelr("http://211.43.180.76:8080/fc-server-1.2.0/services/ALELRService");
		//defineLR("limg00n_emulator2", "C:\\Users\\Janggwan\\Desktop\\LRSpec-TestLLRPReader2.xml");
		//defineLR("test2", "C:\\Users\\Janggwan\\Desktop\\LRSpec-TestLLRPReader2.xml");
		//undefineLR("limg00n_emulator2");
		//undefineLR("test2");
		printReaderNames();
		
		
		ale = createAle("http://211.43.180.76:8080/fc-server-1.2.0/services/ALEService");
		
		//getSubscribers(specName);
		//defineEC("eventcycle1", "C:\\Users\\Janggwan\\Desktop\\ECSpec_current.xml");
		//undefineEC("eventcycle1");
		//subscribe("eventcycle1", "http://limg00n.kaist.ac.kr:9999");
		//unsubscribeEC("eventcycle1", "http://limg00n.kaist.ac.kr:9999");		
		
		//defineEC("eventcycle2", "C:\\Users\\Janggwan\\Desktop\\ECSpec_current2.xml");
		//undefineEC("eventcycle2");
		//subscribe("eventcycle2", "http://limg00n.kaist.ac.kr:9999");
		//unsubscribeEC("eventcycle2", "http://limg00n.kaist.ac.kr:9999");
		printECNames();
		
		
		/*
		String addressCC = "http://localhost:8080/fc-server-1.2.0/services/ALECCService";
		testALECC(addressCC);
		*/
		//aleac = createAleac("http://211.43.180.142:8080/fc-server-1.2.0/services/ALEACService");
	}
	private static void getSubscribers(String specName)
			throws org.fosstrak.ale.wsdl.ale.epcglobal.ImplementationExceptionResponse {
		GetSubscribers getsubscribers = new GetSubscribers();
		getsubscribers.setSpecName(specName);
		try {
			org.fosstrak.ale.wsdl.ale.epcglobal.ArrayOfString aos = ale.getSubscribers(getsubscribers);
			System.out.print("subscribers of "+specName+" : ");
			for(String subscriber : aos.getString()) {
				System.out.print(subscriber+"\t");
			}
			System.out.println();
		} catch (NoSuchNameExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.fosstrak.ale.wsdl.ale.epcglobal.SecurityExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void undefineEC(String specName)
			throws org.fosstrak.ale.wsdl.ale.epcglobal.ImplementationExceptionResponse {
		try {
			org.fosstrak.ale.wsdl.ale.epcglobal.Undefine undefineEC = new org.fosstrak.ale.wsdl.ale.epcglobal.Undefine();
			undefineEC.setSpecName(specName);
			ale.undefine(undefineEC);
		} catch (NoSuchNameExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.fosstrak.ale.wsdl.ale.epcglobal.SecurityExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void unsubscribeEC(String specName, String notiURI)
			throws org.fosstrak.ale.wsdl.ale.epcglobal.ImplementationExceptionResponse {
		try {
			Unsubscribe unsubscribe = new Unsubscribe();
			unsubscribe.setNotificationURI(notiURI);
			unsubscribe.setSpecName(specName);
			ale.unsubscribe(unsubscribe);
		} catch (NoSuchSubscriberExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchNameExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.fosstrak.ale.wsdl.ale.epcglobal.SecurityExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidURIExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void undefineLR(String readerName)
			throws ImplementationExceptionResponse {
		try {
			Undefine undefineReader = new Undefine();
			undefineReader.setName(readerName);
			alelr.undefine(undefineReader);
		} catch (org.fosstrak.ale.wsdl.alelr.epcglobal.SecurityExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InUseExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ImmutableReaderExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.fosstrak.ale.wsdl.alelr.epcglobal.NoSuchNameExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void printPermInfo(String permName)
			throws org.fosstrak.ale.wsdl.aleac.epcglobal.ImplementationExceptionResponse {

			GetPermission getP = new GetPermission();
			getP.setPermName(permName);
			ACPermission acP;
			try {
				acP = aleac.getPermission(getP);
				System.out.println("class: "+acP.getPermissionClass()+"\t instances: "+acP.getInstances().getInstance());
			} catch (UnsupportedOperationExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (org.fosstrak.ale.wsdl.aleac.epcglobal.SecurityExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPermissionExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

	}

	private static void printRolePermission(String roleName)
			throws org.fosstrak.ale.wsdl.aleac.epcglobal.ImplementationExceptionResponse {
		
			GetRole getR = new GetRole();
			getR.setRoleName(roleName);
			ACRole acRole;
			try {
				acRole = aleac.getRole(getR);
				System.out.println("permissions of the role \'"+roleName+"\' :\t"+acRole.getPermissionNames().getPermissionName());
			} catch (NoSuchRoleExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedOperationExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (org.fosstrak.ale.wsdl.aleac.epcglobal.SecurityExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
	}

	private static void printUserIdentity(String userT)
			throws org.fosstrak.ale.wsdl.aleac.epcglobal.ImplementationExceptionResponse {
		GetClientIdentity getCl = new GetClientIdentity();
		getCl.setIdentityName(userT);
	
		ACClientIdentity ret;
		try {
			ret = aleac.getClientIdentity(getCl);
			System.out.println("roles of the user \'"+userT+"\' :\t"+ret.getRoleNames().getRoleName());
		} catch (NoSuchClientIdentityExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedOperationExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.fosstrak.ale.wsdl.aleac.epcglobal.SecurityExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}

	private static void printPerms()
			throws org.fosstrak.ale.wsdl.aleac.epcglobal.ImplementationExceptionResponse {
		
			List<String> permNames;
			try {
				permNames = aleac.getPermissionNames(new org.fosstrak.ale.wsdl.aleac.epcglobal.EmptyParms()).getString();
				System.out.println(permNames);
			} catch (UnsupportedOperationExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (org.fosstrak.ale.wsdl.aleac.epcglobal.SecurityExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
	}

	private static void printRoles()
			throws org.fosstrak.ale.wsdl.aleac.epcglobal.ImplementationExceptionResponse {

			List<String> roleNames;
			try {
				roleNames = aleac.getRoleNames(new org.fosstrak.ale.wsdl.aleac.epcglobal.EmptyParms()).getString();
				System.out.println(roleNames);
			} catch (UnsupportedOperationExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (org.fosstrak.ale.wsdl.aleac.epcglobal.SecurityExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
	}

	private static void printClientIds()
			throws org.fosstrak.ale.wsdl.aleac.epcglobal.ImplementationExceptionResponse {
		
			List<String> clientIds;
			try {
				clientIds = aleac.getClientIdentityNames(new org.fosstrak.ale.wsdl.aleac.epcglobal.EmptyParms()).getString();
				System.out.println(clientIds);
			} catch (UnsupportedOperationExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (org.fosstrak.ale.wsdl.aleac.epcglobal.SecurityExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
	}

	private static void undefinePermission(String permName)
			throws org.fosstrak.ale.wsdl.aleac.epcglobal.ImplementationExceptionResponse {
		
			UndefinePermission undefinePermission = new UndefinePermission();
			undefinePermission.setPermName(permName);
			try {
				aleac.undefinePermission(undefinePermission);
			} catch (UnsupportedOperationExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (org.fosstrak.ale.wsdl.aleac.epcglobal.SecurityExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPermissionExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}

	private static void definePermission(String permName)
			throws org.fosstrak.ale.wsdl.aleac.epcglobal.ImplementationExceptionResponse {
		
			DefinePermission definePermission = new DefinePermission();
			definePermission.setPermName(permName);
			definePermission.setPerm(new ACPermission());
			definePermission.getPerm().setPermissionClass("Method");
			definePermission.getPerm().setInstances(new Instances());
			definePermission.getPerm().getInstances().getInstance().add(permName);	
			
			
			try {
				aleac.definePermission(definePermission);
			} catch (UnsupportedOperationExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (org.fosstrak.ale.wsdl.aleac.epcglobal.SecurityExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PermissionValidationExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DuplicatePermissionExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}

	private static void undefineRole(String roleName)
			throws org.fosstrak.ale.wsdl.aleac.epcglobal.ImplementationExceptionResponse {
		UndefineRole undefineRole = new UndefineRole();
		undefineRole.setRoleName(roleName);
		
		try {
			aleac.undefineRole(undefineRole);
		} catch (NoSuchRoleExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedOperationExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.fosstrak.ale.wsdl.aleac.epcglobal.SecurityExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void defineRole(String roleName, String[] listPerm)
			throws org.fosstrak.ale.wsdl.aleac.epcglobal.ImplementationExceptionResponse {
		
			
			DefineRole defineRole = new DefineRole();
			defineRole.setRoleName(roleName);
			defineRole.setRole(new ACRole());
			defineRole.getRole().setPermissionNames(new PermissionNames());
			for(String permName : listPerm) {
				defineRole.getRole().getPermissionNames().getPermissionName().add(permName);	
			}
			
			
			try {
				aleac.defineRole(defineRole);
			} catch (UnsupportedOperationExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DuplicateRoleExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RoleValidationExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (org.fosstrak.ale.wsdl.aleac.epcglobal.SecurityExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}

	private static void defineClientId(String userIdToDefine, String password)
			throws org.fosstrak.ale.wsdl.aleac.epcglobal.ImplementationExceptionResponse {
		
			DefineClientIdentity defineClientId = new DefineClientIdentity();
			defineClientId.setIdentityName(userIdToDefine);
			defineClientId.setId(new ACClientIdentity());
			defineClientId.getId().setCredentials(new Credentials());
			
			ACClientCredential cred = new ACClientCredential();
			cred.getOtherAttributes().put(new QName("password"), password);
			defineClientId.getId().getCredentials().getCredential().add(cred);
						
			System.out.println("defineClientId: "+userIdToDefine);
			try {
				aleac.defineClientIdentity(defineClientId);
			} catch (UnsupportedOperationExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (org.fosstrak.ale.wsdl.aleac.epcglobal.SecurityExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientIdentityValidationExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DuplicateClientIdentityExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
	}
	
	private static void undefineClientId(String userIdToUndefine) {
		
			UndefineClientIdentity undefineClientId = new UndefineClientIdentity();
			undefineClientId.setIdentityName(userIdToUndefine);
			try {
				aleac.undefineClientIdentity(undefineClientId);
			} catch (NoSuchClientIdentityExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedOperationExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (org.fosstrak.ale.wsdl.aleac.epcglobal.SecurityExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (org.fosstrak.ale.wsdl.aleac.epcglobal.ImplementationExceptionResponse e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
	}

	

	private static void printECNames()
			throws org.fosstrak.ale.wsdl.ale.epcglobal.ImplementationExceptionResponse {
		System.out.println("***** ECSpec Names *****");
		try {
			List<String> listEC = ale.getECSpecNames(new org.fosstrak.ale.wsdl.ale.epcglobal.EmptyParms()).getString();
			for(String s : listEC) {
				System.out.println(s);
			}
		} catch (org.fosstrak.ale.wsdl.ale.epcglobal.SecurityExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("************************");
	}

	

	

	

	private static void defineTM(String specName, String path)
			throws org.fosstrak.ale.wsdl.aletm.epcglobal.ImplementationExceptionResponse {
		String standardVersion;
		try {
			TMFixedFieldListSpec spec = (TMFixedFieldListSpec) DeserializerUtil.deserializeTMSpec(path);
			
			DefineTMSpec define = new DefineTMSpec();
			define.setSpecName(specName);
			//"urn:epc:tag:sgtin-96:3.0037000.030241.1041970"
			//define.setSpecName("urn:epc:tag:sgtin-96:3.1234567.000000.[5-7]");
			define.setSpec(spec);
			
			System.out.println(aletm.defineTMSpec(define));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void printTMSpecNames()
			throws org.fosstrak.ale.wsdl.aletm.epcglobal.ImplementationExceptionResponse {
		System.out.println("***** TMSpec Names *****");
		try {
			org.fosstrak.ale.wsdl.aletm.epcglobal.ArrayOfString aos = aletm.getTMSpecNames(new EmptyParms());
			for(String s : aos.getString()) {
				System.out.println(s);
			}
			
		} catch (SecurityExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("************************");
	}

	

	private static void defineLR(String specName, String path)
			throws org.fosstrak.ale.wsdl.alelr.epcglobal.ImplementationExceptionResponse {
		
		try {
			
			LRSpec spec = (LRSpec) DeserializerUtil.deserializeLRSpec(path);
			
			org.fosstrak.ale.wsdl.alelr.epcglobal.Define define = new org.fosstrak.ale.wsdl.alelr.epcglobal.Define();
			define.setName(specName);
			define.setSpec(spec);
			
			System.out.println(alelr.define(define));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void printReaderNames() {
		System.out.println("***** LRSpec Names *****");
		try {
			List<String> list = alelr.getLogicalReaderNames(new org.fosstrak.ale.wsdl.alelr.epcglobal.EmptyParms()).getString();
			for(String s : list) {
				System.out.println(s);
			}
		} catch (org.fosstrak.ale.wsdl.alelr.epcglobal.SecurityExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ImplementationExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("************************");
	}

	private static void defineEC(String specName, String specPath)
			throws org.fosstrak.ale.wsdl.ale.epcglobal.ImplementationExceptionResponse {
	
		
		//ale.* can wired to ale serviceportimpl. 
		
		try {
			ECSpec spec = (ECSpec) DeserializerUtil.deserializeECSpec(specPath);
			
			org.fosstrak.ale.wsdl.ale.epcglobal.Define define = new org.fosstrak.ale.wsdl.ale.epcglobal.Define();
			define.setSpecName(specName);
			define.setSpec(spec);
			
			System.out.println(ale.define(define));
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void subscribe(String specName, String notiURI) {
		org.fosstrak.ale.wsdl.ale.epcglobal.Subscribe subscribe = new org.fosstrak.ale.wsdl.ale.epcglobal.Subscribe();
		subscribe.setSpecName(specName);
		//Tuan's : http://143.248.55.113:9999
		//Mine : http://localhost:9999
		subscribe.setNotificationURI(notiURI);
		
		try {
			System.out.println(ale.subscribe(subscribe));
		} catch (org.fosstrak.ale.wsdl.ale.epcglobal.ImplementationExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchNameExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.fosstrak.ale.wsdl.ale.epcglobal.SecurityExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidURIExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DuplicateSubscriptionExceptionResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static ALEServicePortType createAle(String address) {
		JaxWsProxyFactoryBean factory;
		factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(ALEServicePortType.class);
		factory.setAddress(address);
		return (ALEServicePortType)factory.create();
	}
	
	private static ALETMServicePortType createAletm(String address) {
		JaxWsProxyFactoryBean factory;
		factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(ALETMServicePortType.class);
		factory.setAddress(address);
		return (ALETMServicePortType)factory.create();
	}

	private static ALELRServicePortType createAlelr(String address) {
		JaxWsProxyFactoryBean factory;
		factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(ALELRServicePortType.class);
		factory.setAddress(address);
		return (ALELRServicePortType)factory.create();
	}
	private static ALEACServicePortType createAleac(String address) {
		JaxWsProxyFactoryBean factory;
		factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(ALEACServicePortType.class);
		factory.setAddress(address);
		return (ALEACServicePortType)factory.create();
	}
	
}
