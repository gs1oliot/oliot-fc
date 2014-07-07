/*
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 * 
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

package org.fosstrak.ale.client.tabs;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.CharArrayWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.ws.soap.SOAPFaultException;

import kr.ac.kaist.resl.ClientPasswordCallback;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.log4j.Logger;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.fosstrak.ale.client.FosstrakAleClient;
import org.fosstrak.ale.client.exception.FosstrakAleClientException;
import org.fosstrak.ale.client.exception.FosstrakAleClientServiceDownException;
import org.fosstrak.ale.util.DeserializerUtil;
import org.fosstrak.ale.util.SerializerUtil;
import org.fosstrak.ale.wsdl.aletm.epcglobal.ALETMServicePortType;
import org.fosstrak.ale.wsdl.aletm.epcglobal.ArrayOfString;
import org.fosstrak.ale.wsdl.aletm.epcglobal.DefineTMSpec;
import org.fosstrak.ale.wsdl.aletm.epcglobal.DuplicateNameExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.EmptyParms;
import org.fosstrak.ale.wsdl.aletm.epcglobal.GetTMSpec;
import org.fosstrak.ale.wsdl.aletm.epcglobal.ImplementationExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.NoSuchNameExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.SecurityExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.TMSpecValidationExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.UndefineTMSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.TMFixedFieldListSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.TMSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.TMVariableFieldListSpec;

/**
 * This class implements a graphical user interface for the application level
 * events client. The client send all commands as SOAP messages to the ale
 * server. The configuration of this class is described in the file
 * ALEClient.properties. The most important parameter is the parameter endpoint,
 * which specifies the address of the ale webservice which runs on the server.
 * 
 * @author regli
 * @author swieland
 */
public class ALETMClient extends AbstractTab {

	/** serial version uid */
	private static final long serialVersionUID = 1L;
	
	/**
	 * endpoint parameter for the configuration.
	 */
	public static final String CFG_ENDPOINT = "org.fosstrak.ale.client.aletm.endpoint";
	
	/** 
	 * text field which contains the notification uri.
	 */
	private JTextField m_notificationUriField;
	
	/**
	 * if the user checks this combo box, then an event sink is created for the subscription.
	 */
	private JCheckBox m_createEventSink;
	
	/** 
	 * text field which contains the reader name.
	 */
	private JTextField m_specNameValueField;
	
	/** 
	 * combobox which contains all defined subscribers for a selected event cycle.
	 */
	private JComboBox m_subscribersComboBox;
	
	// logger.
	private static final Logger s_log = Logger.getLogger(ALETMClient.class);
	
	private static final int CMD__DEFINE_TMSPEC = 1;
	private static final int CMD__UNDEFINE_TMSPEC = 2;
	private static final int CMD__GET_TMSPEC = 3;
	private static final int CMD__GET_TMSPEC_NAMES = 4;
	private static final int CMD__GET_STANDARD_VERSION = 5;
	private static final int CMD__GET_VENDOR_VERSION = 6;
	
	/**
	 * @param parent the parent frame.
	 * @throws NoSuchMethodException given to the fact that we need to pass in a test method via reflection.
	 * @throws SecurityException given to the fact that we need to pass in a test method via reflection.
	 */
	public ALETMClient(JFrame parent) throws SecurityException, NoSuchMethodException {
		super(ALETMServicePortType.class, CFG_ENDPOINT, parent, ALETMServicePortType.class.getMethod("getStandardVersion", EmptyParms.class), new EmptyParms());
	}

	@Override
	public String getBaseNameKey() {
		return "org.fosstrak.ale.client.ale.lang.base";
	}

	@Override
	protected void setCommandPanel(int command) {
		
		if (command == CMD__UNDEFINED_COMMAND) {
			m_commandPanel.removeAll();
			m_commandPanel.setBorder(null);
			this.setVisible(false);
			this.setVisible(true);
			return;
		}

		m_commandPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder(m_guiText.getString("Command" + command)),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		m_commandPanel.removeAll();

		switch (command) {

		case CMD__GET_TMSPEC_NAMES: // getECSpecNames
		case CMD__GET_STANDARD_VERSION: // getStandardVersion
		case CMD__GET_VENDOR_VERSION: // getVendorVersion
			m_commandPanel.setLayout(new GridLayout(1, 1, 5, 0));
			break;

		case CMD__UNDEFINE_TMSPEC: // undefine
		case CMD__GET_TMSPEC: // getECSpec
			m_commandPanel.setLayout(new GridLayout(5, 1, 5, 0)); 
			addECSpecNameComboBox(m_commandPanel);
			addSeparator(m_commandPanel);
			break;

		case CMD__DEFINE_TMSPEC: // define
			m_commandPanel.setLayout(new GridLayout(8, 1, 5, 0));
			addSpecNameValueField(m_commandPanel);
			addChooseFileField(m_commandPanel);
			addSeparator(m_commandPanel);
			break;

		}

		m_commandPanel.setFont(m_font);
		addExecuteButton(m_commandPanel);
		
		validate();
		this.setVisible(true);
	}

	/**
	 * This method adds a specification name combobox to the panel.
	 * 
	 * @param panel to which the specification name combobox should be added
	 */
	private void addECSpecNameComboBox(JPanel panel) {

		m_specNameComboBox = new JComboBox();
		m_specNameComboBox.setFont(m_font);
		m_specNameComboBox.setEditable(false);

		List<String> ecSpecNames = null;
		try {
			ecSpecNames = getAletmServiceProxy().getTMSpecNames(new EmptyParms()).getString();
		} catch (Exception e) {
		}
		if (ecSpecNames != null && ecSpecNames.size() > 0) {
			for (String specName : ecSpecNames) {
				m_specNameComboBox.addItem(specName);
			}
		} else {
			m_specNameComboBox.addItem("no specs defined");
		}
		JLabel lbl = new JLabel(m_guiText.getString("SpecNameLabel"));
		lbl.setFont(m_font);
		panel.add(lbl);
		panel.add(m_specNameComboBox);
	}
	
	
	/**
	 * This method adds a notification property value field to the panel.
	 * 
	 * @param panel to which the property value field should be added
	 */
	private void addSpecNameValueField(JPanel panel) {
		
		m_specNameValueField = new JTextField();
		m_specNameValueField.setFont(m_font);
		
		JLabel lbl = new JLabel(m_guiText.getString("SpecNameLabel"));
		lbl.setFont(m_font);
		panel.add(lbl);
		panel.add(m_specNameValueField);
	}

	/**
	 * This method adds a notification uri field to the panel.
	 * 
	 * @param panel to which the norification uri field should be added
	 */
	private void addNotificationURIField(JPanel panel) {

		m_notificationUriField = new JTextField();
		m_notificationUriField.setFont(m_font);

		JLabel lbl = new JLabel(m_guiText.getString("NotificationURILabel"));
		lbl.setFont(m_font);
		panel.add(lbl);
		panel.add(m_notificationUriField);
	}

	@Override
	protected void executeCommand() {

		Object result = null;
		String specName = null;
		String notificationURI = null;
		Exception ex = null;
		try {

			switch (m_commandSelection.getSelectedIndex()) {

			case CMD__GET_TMSPEC_NAMES: // getECSpecNames
				result = getAletmServiceProxy().getTMSpecNames(new EmptyParms());
				break;

			case CMD__GET_STANDARD_VERSION: // getStandardVersion
				result = getAletmServiceProxy().getStandardVersion(new EmptyParms());
				break;

			case CMD__GET_VENDOR_VERSION: // getVendorVersion
				result = getAletmServiceProxy().getVendorVersion(new EmptyParms());
				break;

			case CMD__UNDEFINE_TMSPEC: // undefine
			case CMD__GET_TMSPEC: // getECSpec
				// get specName
				specName = (String) m_specNameComboBox.getSelectedItem();
				if (specName == null || "".equals(specName)) {
					FosstrakAleClient.instance().showExceptionDialog(
							m_guiText.getString("SpecNameNotSpecifiedDialog"));
					break;
				}

				switch (m_commandSelection.getSelectedIndex()) {

				case CMD__UNDEFINE_TMSPEC: // undefine
					UndefineTMSpec undefineParms = new UndefineTMSpec();
					undefineParms.setSpecName(specName);
					getAletmServiceProxy().undefineTMSpec(undefineParms);
					result = m_guiText.getString("SuccessfullyUndefinedMessage");
					break;

				case CMD__GET_TMSPEC: // getECSpec
					GetTMSpec getTMSpecParms = new GetTMSpec();
					getTMSpecParms.setSpecName(specName);
					result = getAletmServiceProxy().getTMSpec(getTMSpecParms);
					break;


				}

				break;

			case CMD__DEFINE_TMSPEC: // defineTmspec

				if (m_commandSelection.getSelectedIndex() == CMD__DEFINE_TMSPEC) {
					// get specName
					specName = m_specNameValueField.getText();
					if (specName == null || "".equals(specName)) {
						FosstrakAleClient.instance().showExceptionDialog(m_guiText.getString("SpecNameNotSpecifiedDialog"));
						break;
					}
				}

				// get filePath
				String filePath = m_filePathField.getText();
				if (filePath == null || "".equals(filePath)) {
					FosstrakAleClient.instance().showExceptionDialog(m_guiText.getString("FilePathNotSpecifiedDialog"));
					break;
				}

				// get ecSpec
				TMSpec tmSpec;
				try {
					tmSpec = getTMSpecFromFile(filePath);
				} catch (FileNotFoundException e) {
					FosstrakAleClient.instance().showExceptionDialog(m_guiText.getString("FileNotFoundDialog"));
					ex = e;
					break;
				} catch (Exception e) {
					FosstrakAleClient.instance().showExceptionDialog(m_guiText.getString("UnexpectedFileFormatDialog"));
					ex = e;
					break;
				}

				if (m_commandSelection.getSelectedIndex() == CMD__DEFINE_TMSPEC) {
					DefineTMSpec defineParms = new DefineTMSpec();
					defineParms.setSpecName(specName);
					defineParms.setSpec(tmSpec);
					getAletmServiceProxy().defineTMSpec(defineParms);
					result = m_guiText.getString("SuccessfullyDefinedMessage");
				}
				break;

			}

		} catch (Exception e) {
			String reason = e.getMessage();
			
			String text = "Unknown Error";
			if (e instanceof DuplicateNameExceptionResponse) {
				text = m_guiText.getString("DuplicateNameExceptionDialog");
			} else if (e instanceof TMSpecValidationExceptionResponse) {
				text = m_guiText.getString("ECSpecValidationExceptionDialog");
			} else if (e instanceof ImplementationExceptionResponse) {
				text = m_guiText.getString("ImplementationExceptionDialog");
			} else if (e instanceof NoSuchNameExceptionResponse) {
				text = m_guiText.getString("NoSuchNameExceptionDialog");
			} else if (e instanceof SecurityExceptionResponse) {
				text = m_guiText.getString("SecurityExceptionDialog");
			} else if (e instanceof SOAPFaultException) {
				text = "Service error";
			} else if (e instanceof FosstrakAleClientServiceDownException) {
				text = "Unable to execute command.";
				reason = "Service is down or endpoint wrong.";
			}
			
			FosstrakAleClient.instance().showExceptionDialog(text, reason);
			ex = e;
		}

		if (null == ex) {
			showResult(result);
		} else {
			showResult(ex);
		}

		// update spec name combobox
		List<String> ecSpecNames = null;
		try {
			ecSpecNames = getAletmServiceProxy().getTMSpecNames(new EmptyParms()).getString();
		} catch (Exception e) {
		}
		if (ecSpecNames != null && m_specNameComboBox != null
				&& m_specNameComboBox.getSelectedObjects() != null
				&& m_specNameComboBox.getSelectedObjects().length > 0) {
			String current = (String) m_specNameComboBox.getSelectedObjects()[0];
			m_specNameComboBox.removeAllItems();
			if (ecSpecNames != null && ecSpecNames.size() > 0) {
				for (String name : ecSpecNames) {
					m_specNameComboBox.addItem(name);
				}
			}
			m_specNameComboBox.setSelectedItem(current);
		}
	}

	/**
	 * creates an event sink from a given url.
	 * @param text
	 */
	private void createEventSink(String eventSinkURL) {
		try {
			EventSink sink = new EventSink(eventSinkURL);
			FosstrakAleClient.instance().addTab(eventSinkURL, sink);
		} catch (Exception e) {
			s_log.error("Could not start requested event sink.");
			e.printStackTrace();
		}
	}

	/**
	 * This method loads the ec specification from a file.
	 * 
	 * @param filename of ec specification file
	 * @return ec specification
	 * @throws Exception if specification could not be loaded
	 */
	private TMSpec getTMSpecFromFile(String filename) throws Exception {
		FileInputStream inputStream = new FileInputStream(filename);
		return DeserializerUtil.deserializeTMSpec(inputStream);

	}
	
	@Override
	protected void decodeResult(StringBuffer sb, Object result) {
		if (result instanceof ArrayOfString) {
			ArrayOfString resultStringArray = (ArrayOfString) result;
			if (resultStringArray.getString().size() == 0) {
				sb.append(m_guiText.getString("EmptyArray"));
			} else {
				for (String s : resultStringArray.getString()) {
					sb.append(s);
					sb.append("\n");
				}
			}
		} else if (result instanceof TMFixedFieldListSpec) {
			CharArrayWriter writer = new CharArrayWriter();
			try {
				SerializerUtil.serializeTMFixedFieldListSpec((TMFixedFieldListSpec) result, writer);
			} catch (Exception e) {
				FosstrakAleClient.instance().showExceptionDialog(m_guiText.getString("SerializationExceptionMessage"));
			}
			sb.append(writer.toString());

		} else if (result instanceof TMVariableFieldListSpec) {
			CharArrayWriter writer = new CharArrayWriter();
			try {
				SerializerUtil.serializeTMVariableFieldListSpec((TMVariableFieldListSpec) result, writer);
			} catch (Exception e) {
				FosstrakAleClient.instance().showExceptionDialog(m_guiText.getString("SerializationExceptionMessage"));
			}
			sb.append(writer.toString());

		}
	}

	@Override
	protected String[] getCommands() {

		String[] commands = new String[11];
		for (int i = 1; i < 12; i++) {
			commands[i - 1] = m_guiText.getString("Command" + i);
		}
		return commands;
	}

	/**
	 * @return returns the proxy object already casted.
	 */
	protected ALETMServicePortType getAletmServiceProxy() throws FosstrakAleClientException {
		
		return (ALETMServicePortType) getProxy();
	}

}