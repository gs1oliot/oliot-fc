/*
 * Copyright (C) 2021 Krakul OÜ
 * @author Mikk Leini <mikk.leini@krakul.eu>
 *
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.apache.log4j.Logger;

import org.fosstrak.ale.client.FosstrakAleClient;
import org.fosstrak.ale.server.llrp.LLRPController;
import org.fosstrak.ale.client.exception.FosstrakAleClientException;
import org.fosstrak.ale.client.exception.FosstrakAleClientServiceDownException;
import org.fosstrak.ale.util.DeserializerUtil;
import org.fosstrak.ale.util.SerializerUtil;
import org.fosstrak.ale.wsdl.ale.epcglobal.ArrayOfString;
import org.fosstrak.ale.wsdl.ale.epcglobal.Immediate;
import org.fosstrak.ale.wsdl.ale.epcglobal.DuplicateNameExceptionResponse;
import org.fosstrak.ale.wsdl.ale.epcglobal.ImplementationExceptionResponse;
import org.fosstrak.ale.wsdl.ale.epcglobal.NoSuchNameExceptionResponse;
import org.fosstrak.ale.wsdl.ale.epcglobal.SecurityExceptionResponse;
import org.fosstrak.ale.wsdl.ale.epcglobal.EmptyParms;
import org.llrp.ltk.generated.messages.ADD_ROSPEC;

/**
 * This class implements a graphical user interface for the Application Level
 * Events (ALE) client. The client send all commands as SOAP messages to the ALE
 * server. This class configures LLRP controller in the ALE server.
 */
public class LLRPClient extends AbstractTab {

    /**
     * endpoint parameter for the configuration.
     */
    public static final String CFG_ENDPOINT = "org.fosstrak.ale.client.llrp.endpoint";

    /**
     * text field which contains the reader name.
     */
    private JTextField m_specNameValueField;

    // logger.
    private static final Logger s_log = Logger.getLogger(LLRPClient.class);

    private static final int CMD__DEFINE_LLRP = 1;
    private static final int CMD__UNDEFINE_LLRP = 2;
    private static final int CMD__START_LLRP = 3;
    private static final int CMD__STOP_LLRP = 4;
    private static final int CMD__ENABLE_LLRP = 5;
    private static final int CMD__DISABLE_LLRP = 6;
    private static final int CMD__DISABLE_ALL_LLRP = 7;

    /**
     * @param parent the parent frame.
     * @throws NoSuchMethodException given to the fact that we need to pass in a test method via reflection.
     * @throws SecurityException given to the fact that we need to pass in a test method via reflection.
     */
    public LLRPClient(JFrame parent) throws SecurityException, NoSuchMethodException {
        super(LLRPController.class, CFG_ENDPOINT, parent, null, null);
    }

    @Override
    public String getBaseNameKey() {
        return "org.fosstrak.ale.client.llrp.lang.base";
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

        m_commandPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(m_guiText.getString("Command" + command)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        m_commandPanel.removeAll();

        switch (command) {

            case CMD__DEFINE_LLRP:
                m_commandPanel.setLayout(new GridLayout(8, 1, 5, 0));
                addSpecNameValueField(m_commandPanel);
                addChooseFileField(m_commandPanel);
                addSeparator(m_commandPanel);
                break;

            case CMD__UNDEFINE_LLRP:
            case CMD__START_LLRP:
            case CMD__STOP_LLRP:
            case CMD__ENABLE_LLRP:
            case CMD__DISABLE_LLRP:
                m_commandPanel.setLayout(new GridLayout(5, 1, 5, 0));
                addSpecNameValueField(m_commandPanel);
                addSeparator(m_commandPanel);
                break;

            case CMD__DISABLE_ALL_LLRP:
                m_commandPanel.setLayout(new GridLayout(1, 1, 5, 0));
                break;
        }

        m_commandPanel.setFont(m_font);
        addExecuteButton(m_commandPanel);

        validate();
        this.setVisible(true);
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

    @Override
    protected void executeCommand() {

        Object result = null;
        String specName = null;
        Exception ex = null;

        try {

            switch (m_commandSelection.getSelectedIndex()) {

                case CMD__DEFINE_LLRP:
                    // get specName
                    specName = m_specNameValueField.getText();
                    if (specName == null || "".equals(specName)) {
                        FosstrakAleClient.instance().showExceptionDialog(
                                m_guiText.getString("SpecNameNotSpecifiedDialog"));
                        break;
                    }

                    // get filePath
                    String filePath = m_filePathField.getText();
                    if (filePath == null || "".equals(filePath)) {
                        FosstrakAleClient.instance().showExceptionDialog(m_guiText.getString("FilePathNotSpecifiedDialog"));
                        break;
                    }

                    // File exists?
                    if (Files.notExists(Paths.get(filePath)))
                    {
                        FosstrakAleClient.instance().showExceptionDialog(m_guiText.getString("FileNotFoundDialog"));
                        break;
                    }

                    // Define
                    getLLRPServiceProxy().define(specName, filePath);
                    result = m_guiText.getString("SuccessfullyDefinedMessage");
                    break;

                case CMD__UNDEFINE_LLRP:
                case CMD__START_LLRP:
                case CMD__STOP_LLRP:
                case CMD__ENABLE_LLRP:
                case CMD__DISABLE_LLRP:
                    // get specName
                    specName = m_specNameValueField.getText();
                    if (specName == null || "".equals(specName)) {
                        FosstrakAleClient.instance().showExceptionDialog(
                                m_guiText.getString("SpecNameNotSpecifiedDialog"));
                        break;
                    }

                    switch (m_commandSelection.getSelectedIndex()) {
                        case CMD__UNDEFINE_LLRP:
                            getLLRPServiceProxy().undefine(specName);
                            break;

                        case CMD__START_LLRP:
                            getLLRPServiceProxy().start(specName);
                            break;

                        case CMD__STOP_LLRP:
                            getLLRPServiceProxy().stop(specName);
                            break;

                        case CMD__ENABLE_LLRP:
                            getLLRPServiceProxy().enable(specName);
                            break;

                        case CMD__DISABLE_LLRP:
                            getLLRPServiceProxy().disable(specName);
                            break;
                    }

                    break;

                case CMD__DISABLE_ALL_LLRP:
                    getLLRPServiceProxy().disableAll();
                    break;
            }

        } catch (Exception e) {
            String reason = e.getMessage();

            String text = "Unknown Error";
            if (e instanceof DuplicateNameExceptionResponse) {
                text = m_guiText.getString("DuplicateNameExceptionDialog");
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
    }

    /**
     * This method loads the ADD_ROSPEC specification from a file.
     *
     * @param filename of ec specification file
     * @return ec specification
     * @throws Exception if specification could not be loaded
     */
    private ADD_ROSPEC getADDROSPECFromFile(String filename) throws Exception {
        FileInputStream inputStream = new FileInputStream(filename);
        return DeserializerUtil.deserializeAddROSpec(inputStream);
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
        }
    }

    @Override
    protected String[] getCommands() {

        String[] commands = new String[7];
        for (int i = 1; i <= 7; i++) {
            commands[i - 1] = m_guiText.getString("Command" + i);
        }
        return commands;
    }

    /**
     * @return returns the proxy object already casted.
     */
    protected LLRPController getLLRPServiceProxy() throws FosstrakAleClientException {

        return (LLRPController) getProxy();
    }
}