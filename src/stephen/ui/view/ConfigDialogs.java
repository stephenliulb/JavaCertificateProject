/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.ui.view;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import stephen.Config;
import stephen.common.Constant;
import stephen.common.Messages;
import stephen.common.Constant.Mode;

/**
 * This class is configuration GUI, which let users dynamically change the
 * application configuration during the runtime; all changes will be saved into
 * application properties file when users accept the changes.
 * 
 * @author Stephen Liu
 * 
 */
public class ConfigDialogs extends JDialog {
	private static final long serialVersionUID = 1L;

	private JCheckBox localDatabaseSelector;
	private JTextField datafileTxt;
	private JButton browserBtn;
	private JTextField serverTxt, portTxt;
	private JButton okBtn, cancelBtn;

	private JLabel serverLabel = new JLabel(Messages.getString("ConfigDialogs.server")); //$NON-NLS-1$
	private JLabel portLabel = new JLabel(Messages.getString("ConfigDialogs.port")); //$NON-NLS-1$
	private JLabel datafileLabel = new JLabel(Messages.getString("ConfigDialogs.file")); //$NON-NLS-1$

	private Config properties;
	private Constant.Mode mode;
	private boolean isApproved = false;

	/**
	 * Get a configuration dialogs object for specific application work mode.
	 * 
	 * @param parent     parent window.
	 * @param mode       application work mode.
	 * @param configData current configuration data.
	 */
	public ConfigDialogs(JFrame parent, Constant.Mode mode, Config configData) {
		super(parent, getTitle(mode), true);

		this.properties = configData;
		this.mode = mode;
		isApproved = false;

		Container cp = this.getContentPane();
		switch (mode) {
		case alone:
		case network:
			setupFrameForClient(cp, mode, configData);
			break;

		case server:
			setupFrameForServer(cp, mode, configData);
			break;

		}
	}

	/**
	 * Determine which button user pressed.
	 * 
	 * @return true if users click the 'OK' button on the GUI; false if users click
	 *         the 'Cancel' button on the GUI.
	 */
	public boolean isApproved() {
		return isApproved;
	}

	private static String getTitle(Constant.Mode mode) {
		String title = null;
		switch (mode) {
		case alone:
		case network:
			title = Messages.getString("ConfigDialogs.clientTitle"); //$NON-NLS-1$
			break;
		case server:
			title = Messages.getString("ConfigDialogs.serverTitle"); //$NON-NLS-1$
			break;
		}
		return title;
	}

	private void setupFrameForClient(Container cp, Constant.Mode mode, Config configData) {
		cp.setLayout(new GridLayout(4, 1));

		JPanel modePanel = this.buildModePanel(mode);
		cp.add(modePanel);

		JPanel datafilePanel = this.buildDataFilePanel();
		cp.add(datafilePanel);

		// network client configuration panel
		JPanel networkPanel = this.buildNetworkPanel();
		cp.add(networkPanel);

		// OK/cancel button
		JPanel decisionPanel = this.buildDecisionPanel();
		cp.add(decisionPanel);

		initConfiguration(mode, configData);
	}

	private void setupFrameForServer(Container cp, Constant.Mode mode, Config configData) {
		cp.setLayout(new GridLayout(3, 1));

		JPanel portPanel = this.buildServerPortPanel();
		cp.add(portPanel);

		JPanel datafilePanel = this.buildDataFilePanel();
		cp.add(datafilePanel);

		// OK/cancel button
		JPanel decisionPanel = this.buildDecisionPanel();
		cp.add(decisionPanel);

		initConfiguration(mode, configData);
	}

	private JPanel buildModePanel(Constant.Mode mode) {
		JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		if (mode != Mode.network) {
			localDatabaseSelector = new JCheckBox(Messages.getString("ConfigDialogs.userLocalDatabase"), true);
		} else {
			localDatabaseSelector = new JCheckBox(Messages.getString("ConfigDialogs.userLocalDatabase"), false);
		}
		modePanel.add(localDatabaseSelector);
		return modePanel;

	}

	private JPanel buildDataFilePanel() {
		// local database configuration panel
		JPanel localPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		localPanel.add(datafileLabel);
		datafileTxt = new JTextField(35);
		localPanel.add(datafileTxt);
		browserBtn = new JButton(Messages.getString("ConfigDialogs.browser"));
		browserBtn.addActionListener(new FileChooseActionListener());
		localPanel.add(browserBtn);

		return localPanel;
	}

	private JPanel buildNetworkPanel() {
		JPanel networkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		networkPanel.add(serverLabel);
		serverTxt = new JTextField(32);
		networkPanel.add(serverTxt);
		networkPanel.add(portLabel);
		portTxt = new JTextField(4);
		networkPanel.add(portTxt);
		return networkPanel;
	}

	private JPanel buildServerPortPanel() {
		JPanel serverPortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		serverPortPanel.add(portLabel);
		portTxt = new JTextField(4);
		serverPortPanel.add(portTxt);
		return serverPortPanel;
	}

	private JPanel buildDecisionPanel() {
		JPanel decisionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		okBtn = new JButton("OK");
		okBtn.addActionListener(new OKActionListener());
		decisionPanel.add(okBtn);
		cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isApproved = false;
				ConfigDialogs.this.setVisible(false);
				dispose();
			}
		});
		decisionPanel.add(cancelBtn);
		return decisionPanel;
	}

	private void initTextField(JTextField comp, Config configData, String key, String defaultValue) {
		String value = null;
		if (configData != null) {
			value = configData.getProperty(key);
		}

		if (value == null) {
			value = defaultValue;
		}

		comp.setText(value);
		return;
	}

	private void initConfiguration(Mode mode, Config configData) {
		switch (mode) {
		case alone:
			datafileTxt.setEnabled(true);
			browserBtn.setEnabled(true);
			serverLabel.setEnabled(false);
			serverTxt.setEnabled(false);
			portLabel.setEnabled(false);
			portTxt.setEnabled(false);
			initTextField(datafileTxt, properties, Constant.ConfigParameters.DATAFILE, Constant.DEFAULT_DATAFILE_NAME);
			localDatabaseSelector.setEnabled(false);
			break;
		case network:
			serverLabel.setEnabled(true);
			serverTxt.setEnabled(true);
			portLabel.setEnabled(true);
			portTxt.setEnabled(true);

			datafileTxt.setEnabled(false);
			browserBtn.setEnabled(false);
			datafileLabel.setEnabled(false);
			initTextField(serverTxt, properties, Constant.ConfigParameters.SERVER, Constant.DEFAULT_SERVER);
			initTextField(portTxt, properties, Constant.ConfigParameters.PORT, String.valueOf(Constant.DEFAULT_PORT));
			localDatabaseSelector.setEnabled(false);
			break;
		case server:
			datafileTxt.setEnabled(true);
			browserBtn.setEnabled(true);
			portTxt.setEnabled(true);
			initTextField(datafileTxt, properties, Constant.ConfigParameters.DATAFILE, Constant.DEFAULT_DATAFILE_NAME);
			initTextField(portTxt, properties, Constant.ConfigParameters.PORT, String.valueOf(Constant.DEFAULT_PORT));
			break;
		}
		return;
	}

	private boolean saveProperties() {
		boolean isSuccess = false;
		try {
			properties.save();
			isSuccess = true;
		} catch (IOException exception) {
			JOptionPane.showMessageDialog(null, exception.getMessage(), Messages.getString("ConfigDialogs.hint"),
					JOptionPane.WARNING_MESSAGE);
			isSuccess = false;
		}
		return isSuccess;
	}

	private class FileChooseActionListener implements ActionListener {
		/**
		 * Let use choose data file path from a file dialog.
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 *      )
		 */
		public void actionPerformed(ActionEvent e) {
			JFileChooser c = new JFileChooser();
			int rVal = c.showOpenDialog(ConfigDialogs.this);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				datafileTxt.setText(c.getSelectedFile().getAbsolutePath());
			}
		}
	}

	private class OKActionListener implements ActionListener {

		/**
		 * When user press the 'OK' button, all the input configuration information
		 * corresponding to different work mode will be validated first, then be saved
		 * into property file on disk if passed, otherwise, an error message box will
		 * pop up and reminder use re-input again.
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 *      )
		 */
		public void actionPerformed(ActionEvent e) {
			if (properties == null) {
				JOptionPane.showMessageDialog(null, Messages.getString("ConfigDialogs.noConfigFile"), //$NON-NLS-1$
						Messages.getString("ConfigDialogs.hint"), JOptionPane.WARNING_MESSAGE);
				return;
			}

			switch (mode) {
			case alone:
			case network:

				if (localDatabaseSelector.isSelected()) {
					String datafile = datafileTxt.getText().trim();
					if (datafile.length() == 0) {
						JOptionPane.showMessageDialog(null, Messages.getString("ConfigDialogs.inputDBFile"), //$NON-NLS-1$
								Messages.getString("ConfigDialogs.hint"), JOptionPane.WARNING_MESSAGE);
						return;
					}

					properties.clear();
					properties.setProperty(Constant.ConfigParameters.DATAFILE, datafile);
					properties.setProperty(Constant.ConfigParameters.MODE, Mode.alone.name().toLowerCase());

				} else {
					String server = serverTxt.getText().trim();
					if (server.length() == 0) {
						JOptionPane.showMessageDialog(null, Messages.getString("ConfigDialogs.inputServer"),
								Messages.getString("ConfigDialogs.hint"), JOptionPane.WARNING_MESSAGE);
						return;
					}

					String port = portTxt.getText().trim();
					if (!Config.validatePort(port)) {
						JOptionPane.showMessageDialog(null, Messages.getString("ConfigDialogs.wrongPort"), //$NON-NLS-1$
								Messages.getString("ConfigDialogs.hint"), JOptionPane.WARNING_MESSAGE);
						return;
					}

					properties.clear();
					properties.setProperty(Constant.ConfigParameters.SERVER, server);
					properties.setProperty(Constant.ConfigParameters.PORT, port);
					properties.setProperty(Constant.ConfigParameters.MODE.toLowerCase(), Mode.network.name());
				}
				break;

			case server:
				String datafile = datafileTxt.getText().trim();
				if (datafile.length() == 0) {
					JOptionPane.showMessageDialog(null, Messages.getString("ConfigDialogs.inputDBFile"),
							Messages.getString("ConfigDialogs.hint"), JOptionPane.WARNING_MESSAGE);
					return;
				}

				String port = portTxt.getText().trim();
				if (!Config.validatePort(port)) {
					JOptionPane.showMessageDialog(null, Messages.getString("ConfigDialogs.wrongPort"),
							Messages.getString("ConfigDialogs.hint"), JOptionPane.WARNING_MESSAGE);
					return;
				}

				properties.clear();
				properties.setProperty(Constant.ConfigParameters.PORT, port);
				properties.setProperty(Constant.ConfigParameters.DATAFILE, datafile);
				properties.setProperty(Constant.ConfigParameters.MODE.toLowerCase(), Mode.server.name());
				break;
			}

			if (!saveProperties()) {
				return;
			}

			isApproved = true;
			ConfigDialogs.this.setVisible(false);
			dispose();
		}
	}
}
