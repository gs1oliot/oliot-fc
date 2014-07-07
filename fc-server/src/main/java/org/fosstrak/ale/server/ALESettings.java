/*
 * Copyright (C) 2007 ETH Zurich
 *
 * This file is part of Fosstrak (www.fosstrak.org).
 *
 * Fosstrak is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 *
 * Fosstrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Fosstrak; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */
package org.fosstrak.ale.server;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * settings class with all application specific settings. <br/>
 * <br/>
 * the content of the settings variables is auto-loaded from the properties file ale.properties
 * @author swieland
 *
 */
@Service("aleSettings")
public class ALESettings {
	
	/** logger. */
	private static final Logger LOG = Logger.getLogger(ALESettings.class);
	
	@Value(value = "${ale.standard.version}")
	private String aleStandardVersion;
	
	@Value(value = "${alecc.standard.version}")
	private String aleCCStandardVersion;
	
	@Value(value = "${lr.standard.version}")
	private String lrStandardVersion;
	
	@Value(value = "${vendor.version}")
	private String vendorVersion;
	
	
	
	@Value(value = "${jconsole.use}")
	private String jconsoleUse;

	@Value(value = "${jconsole.rmiRegistryPort}")
	private String jconsoleRmiRegistryPort;

	@Value(value = "${jconsole.rmiConnPort}")
	private String jconsoleRmiConnPort;

	@Value(value = "${fortress.enable}")
	private String fortressEnable;
	
	@Value(value = "${admin.password}")
	private String adminPassword;

	public ALESettings() {

		//initJConsole();			

				

	}

	private void initJConsole() {
		LOG.debug("initializing JConsole");
        try {
			// Ensure cryptographically strong random number generator used
	        // to choose the object number - see java.rmi.server.ObjID
	        //
	        System.setProperty("java.rmi.server.randomIDs", "true");
	
	        // Start an RMI registry on port 5001.
	        //
	        LOG.debug("Create RMI registry on port 5001");
	
			//LocateRegistry.createRegistry(5001);
			//int registryPort = org.fosstrak.llrp.adaptor.Constants.registryPort;
	
	        // Retrieve the PlatformMBeanServer.
	        //
			LOG.debug("Get the platform's MBean server");
	        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	
	        // Environment map.
	        //
	        LOG.debug("Initialize the environment map");
	        HashMap<String,Object> env = new HashMap<String,Object>();
	
	        // Provide SSL-based RMI socket factories.
	        //
	        // The protocol and cipher suites to be enabled will be the ones
	        // defined by the default JSSE implementation and only server
	        // authentication will be required.
	        //
	        /*
	        SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
	        SslRMIServerSocketFactory ssf = new SslRMIServerSocketFactory();
	        env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
	        env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);
	*/
	        // Provide the password file used by the connector server to
	        // perform user authentication. The password file is a properties
	        // based text file specifying username/password pairs.
	        //
	        //env.put("jmx.remote.x.password.file", "password.properties");
	
	        // Provide the access level file used by the connector server to
	        // perform user authorization. The access level file is a properties
	        // based text file specifying username/access level pairs where
	        // access level is either "readonly" or "readwrite" access to the
	        // MBeanServer operations.
	        //
	        //env.put("jmx.remote.x.access.file", "access.properties");
	
	        // Create an RMI connector server.
	        //
	        // As specified in the JMXServiceURL the RMIServer stub will be
	        // registered in the RMI registry running in the local host on
	        // port 3000 with the name "jmxrmi". This is the same name the
	        // out-of-the-box management agent uses to register the RMIServer
	        // stub too.
	        //
	        //-Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=5000
	        env.put("com.sun.management.jmxremote.authenticate", "false");
	        env.put("com.sun.management.jmxremote.ssl", "false");
	        
	        LOG.debug("Create an RMI connector server where RmiConnPort=5002, RmiRegistryPort=5001");
	        //JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost:" + 5002  + "/jndi/rmi://localhost:" + 5001 + "/jmxrmi");
	        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost:" + 5002  + "/jndi/rmi://localhost:" + 5556 + "/jmxrmi");
	        
	        JMXConnectorServer cs =
	            JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
	
	        // Start the RMI connector server.
	        //
	        LOG.debug("Start the RMI connector server");
	        cs.start();
        } catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getJconsoleUse() {
		return jconsoleUse;
	}

	public void setJconsoleUse(String jconsoleUse) {
		this.jconsoleUse = jconsoleUse;
	}
	
	public String getJconsoleRmiRegistryPort() {
		return jconsoleRmiRegistryPort;
	}

	public void setJconsoleRmiRegistryPort(String jconsoleRmiRegistryPort) {
		this.jconsoleRmiRegistryPort = jconsoleRmiRegistryPort;
	}
	
	public String getJconsoleRmiConnPort() {
		return jconsoleRmiConnPort;
	}

	public void setJconsoleRmiConnPort(String jconsoleRmiConnPort) {
		this.jconsoleRmiConnPort = jconsoleRmiConnPort;
	}
	
	/**
	 * return the current standard version of the ALE.
	 * @return current standard version.
	 */
	public String getAleStandardVersion() {
		return aleStandardVersion;
	}

	public void setAleStandardVersion(String aleStandardVersion) {
		this.aleStandardVersion = aleStandardVersion;
	}
	
	/**
	 * return the current standard version of the ALE.
	 * @return current standard version.
	 */
	public String getAleCCStandardVersion() {
		return aleCCStandardVersion;
	}

	public void setAleCCStandardVersion(String aleCCStandardVersion) {
		this.aleCCStandardVersion = aleCCStandardVersion;
	}

	/**
	 * return the current standard version of the logical reader management.
	 * @return current standard version.
	 */
	public String getLrStandardVersion() {
		return lrStandardVersion;
	}
	
	public void setLrStandardVersion(String lrStandardVersion) {
		this.lrStandardVersion = lrStandardVersion;
	}

	/**
	 * encodes the current vendor version of this filtering and collection - the current build.
	 * @return current vendor version.
	 */
	public String getVendorVersion() {
		return vendorVersion;
	}

	public void setVendorVersion(String vendorVersion) {
		this.vendorVersion = vendorVersion;
	}
	public String getFortressEnable() {
		return fortressEnable;
	}
	public void setFortressEnable(String value) {
		this.fortressEnable = value;
	}
	public String getAdminPassword() {
		return adminPassword;
	}
	public void setAdminPassword(String value) {
		this.adminPassword = value;
	}
}
