/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import stephen.Config;
import stephen.common.Constant;
import stephen.common.Messages;
import stephen.common.Utils;
import stephen.common.Constant.Mode;
import stephen.dao.DAOInterface;
import stephen.dao.DataTransferObject;
import stephen.dao.exception.DAOException;
import stephen.dao.impl.DAOFactory;
import stephen.dao.spec.ANDSpec;
import stephen.dao.spec.EqualSpec;
import stephen.dao.spec.ORSpec;
import stephen.dao.spec.Spec;
import stephen.db.DBSchemaV2;
import stephen.ui.model.UIDataModel;

/**
 * This class is the Client GUI main window, which provides user interface to
 * let users view data, search data and update data in database. User can search
 * the data for all records, or for records where the name and/or location
 * fields exactly match values specified by the user; user can also book a
 * selected record and update the database accordingly.
 * 
 * All configuration parameters for the application can be done on the GUI. User
 * can specify the location of the database in network mode ,or location of
 * database file in alone mode.
 * 
 * Users can use the GUI to connect to local database or remote database based
 * on mode flag input in command line. There is a status message in status panel
 * on the bottom to show local database or remote database is used.
 * 
 * @author Stephen Liu
 * 
 */
public class ControllerFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ControllerFrame.class.getName());

	private final static String[] ops = { Constant.AND, Constant.OR };

	private Mode mode;
	private Config configuration;

	private final UIDataModel model;

	private JTextField hotelTxt = null;
	private JComboBox logicOperator = null;
	private JTextField cityTxt = null;
	private JTable table = null;
	private JLabel status = null;

	/**
	 * Create client GUI window object for specific application mode with the
	 * configuration data.
	 * 
	 * @param configuration application configuration data.
	 * @param mode          application working mode.
	 * @see stephen.common.Constant.Mode
	 */
	public ControllerFrame(Config configuration, Mode mode) {
		super(Messages.getString("ControllerFrame.appTitle")); //$NON-NLS-1$

		this.mode = mode;
		this.configuration = configuration;

		model = new UIDataModel();

		// construct searching conditions to retrieve all data at the begining.
		model.setFilter(generateQueryCondition(null, null, Constant.OR));
		setupFrame();
		try {
			initDataModel(configuration, mode);
			updateStatusInfo(mode, true);
		} catch (Exception e) {
			updateStatusInfo(mode, false);
			logger.warning(e.getMessage());
		}

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(800, 500);
		this.setVisible(true);
	}

	private void initDataModel(Config configuration, Mode mode) throws DAOException {
		DAOInterface<DataTransferObject> dao = this.getDAO(configuration, mode);
		model.setDAO(dao);
		model.refresh();
	}

	private DAOInterface<DataTransferObject> getDAO(Config config, Mode mode) throws DAOException {
		DAOInterface<DataTransferObject> dao = null;

		String modeCfg = config.getProperty(Constant.ConfigParameters.MODE);

		switch (mode) {
		case alone:
			String dbfile = config.getProperty(Constant.ConfigParameters.DATAFILE);
			if (modeCfg == null || !modeCfg.equalsIgnoreCase(Mode.alone.name()) || dbfile == null
					|| dbfile.length() == 0) {
				String errMsg = Messages.getString("ControllerFrame.noDBFile"); //$NON-NLS-1$
				throw new DAOException(errMsg);
			}
			dao = DAOFactory.getDAO(true, dbfile, null, -1);
			break;

		case network:
			String server = config.getProperty(Constant.ConfigParameters.SERVER);
			String port = config.getProperty(Constant.ConfigParameters.PORT);

			if (modeCfg == null || !modeCfg.equalsIgnoreCase(Mode.network.name()) || server == null
					|| server.length() == 0 || port == null || port.length() == 0) {
				String errMsg = Messages.getString("ControllerFrame.serverUnconfiged", //$NON-NLS-1$
						new Object[] { Constant.PROPERTY_FILENAME });
				throw new DAOException(errMsg);
			}

			dao = DAOFactory.getDAO(false, null, server, Integer.valueOf(port));
			break;

		default:
			dao = null;
			break;
		}

		return dao;
	}

	private void setupFrame() {
		Container cp = this.getContentPane();

		// Menu area
		MenuModel mm = new MenuModel();
		mm.add(Messages.getString("ControllerFrame.file"),
				mm.new MenuItemModel(Messages.getString("ControllerFrame.close"), new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.exit(0);
					}
				}));

		mm.add(Messages.getString("ControllerFrame.configuration"), mm.new MenuItemModel(
				Messages.getString("ControllerFrame.setConfiguration"), new ConfigurationAction()));

		mm.add(Messages.getString("ControllerFrame.help"),
				mm.new MenuItemModel(Messages.getString("ControllerFrame.about"), new HelpAction()));

		JMenuBar mb = mm.getMenuBar();
		this.setJMenuBar(mb);

		// search condition area
		JPanel searchPanel = new JPanel(new GridBagLayout());
		searchPanel.setBorder(new TitledBorder(Messages.getString("ControllerFrame.searchTitle"))); //$NON-NLS-1$
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 5, 0, 5);

		c.gridx = 0;
		c.gridy = 0;
		searchPanel.add(new JLabel(Messages.getString("ControllerFrame.hotel")), c); //$NON-NLS-1$
		c.gridx = 1;
		c.weightx = 1.0;

		hotelTxt = new JTextField();
		hotelTxt.addActionListener(new SearchAction());
		searchPanel.add(hotelTxt, c);

		c.gridx = 2;
		c.weightx = 0.0;

		logicOperator = new JComboBox(ops);
		logicOperator.setSelectedIndex(0);
		searchPanel.add(logicOperator, c);

		c.gridx = 3;
		c.weightx = 0.0;
		c.ipadx = 5;
		searchPanel.add(new JLabel(Messages.getString("ControllerFrame.city")), c); //$NON-NLS-1$
		c.gridx = 4;
		c.weightx = 1.0;

		cityTxt = new JTextField();
		cityTxt.addActionListener(new SearchAction());
		searchPanel.add(cityTxt, c);
		c.gridx = 5;
		c.weightx = 0.0;
		JButton goBtn = new JButton(Messages.getString("ControllerFrame.search")); //$NON-NLS-1$
		goBtn.addActionListener(new SearchAction());
		searchPanel.add(goBtn, c);
		cp.add(searchPanel, BorderLayout.NORTH);

		// Data display area
		JPanel dataPanel = new JPanel(new BorderLayout());
		dataPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
		TableModel tModel = new TableModel(DBSchemaV2.getInstance().getColumnNames(), model);
		table = new JTable(tModel);
		tModel.addTableModelListener(table);

		JScrollPane jp = new JScrollPane(table);
		dataPanel.add(jp, BorderLayout.CENTER);
		cp.add(dataPanel, BorderLayout.CENTER);

		// Status display area
		JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		statusPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
		status = new JLabel("");
		statusPanel.add(status);
		cp.add(statusPanel, BorderLayout.SOUTH);
	}

	private void updateStatusInfo(Mode mode, boolean isConnected) {
		switch (mode) {
		case alone:
			if (isConnected) {
				status.setText(Messages.getString("ControllerFrame.useLocalDB")); //$NON-NLS-1$
				status.setForeground(Color.BLACK);
			} else {
				String errMsg = Messages.getString("ControllerFrame.localDBDisconnected"); //$NON-NLS-1$
				status.setText(errMsg);
				status.setForeground(Color.RED);
			}
			break;

		case network:
			if (isConnected) {
				status.setText(Messages.getString("ControllerFrame.userRemoteDB", //$NON-NLS-1$
						new Object[] { configuration.getProperty(Constant.ConfigParameters.SERVER),
								configuration.getProperty(Constant.ConfigParameters.PORT) }));
				status.setForeground(Color.BLACK);
			} else {
				String errMsg = Messages.getString("ControllerFrame.remoteDBDisconnected"); //$NON-NLS-1$
				status.setText(errMsg);
				status.setForeground(Color.RED);
			}

			break;
		default:
			break;
		}
	}

	private Spec generateQueryCondition(String hotelname, String location, String logicalOperation) {
		EqualSpec hotelCondition = new EqualSpec(DBSchemaV2.NAME, hotelname);
		EqualSpec locationCondition = new EqualSpec(DBSchemaV2.LOCATION, location);

		Spec queryCondition = null;
		if (logicalOperation.equalsIgnoreCase(Constant.OR)) {
			queryCondition = new ORSpec(hotelCondition, locationCondition);
		} else {
			queryCondition = new ANDSpec(hotelCondition, locationCondition);
		}

		return queryCondition;
	}

	private class ConfigurationAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ConfigDialogs diag = new ConfigDialogs(null, mode, configuration);

			diag.pack();
			diag.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			diag.setVisible(true);
			boolean isApproved = diag.isApproved();

			if (!isApproved) {
				return;
			}

			try {
				initDataModel(configuration, mode);
				updateStatusInfo(mode, true);
			} catch (DAOException x) {
				logger.warning(Utils.getExceptionString(x));
				JOptionPane.showMessageDialog(null, x.getMessage(),
						Messages.getString("ControllerFrame.exceptionTitle"), JOptionPane.ERROR_MESSAGE);
				model.setDAO(null);
				try {
					model.refresh();
				} catch (DAOException xx) {
					logger.warning(Utils.getExceptionString(xx));
				}
				updateStatusInfo(mode, false);
			}
			table.updateUI();
		}

	}

	private class SearchAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			Spec queryCondition = generateQueryCondition(hotelTxt.getText().trim(), cityTxt.getText().trim(),
					ops[logicOperator.getSelectedIndex()]);

			logger.finer(Messages.getString("ControllerFrame.searchCondition", new Object[] { queryCondition }));

			model.setFilter(queryCondition);
			try {
				model.refresh();
			} catch (DAOException exception) {
				logger.warning(Utils.getExceptionString(exception));

				JOptionPane.showMessageDialog(null, exception.getMessage(),
						Messages.getString("ControllerFrame.exceptionTitle"), JOptionPane.ERROR_MESSAGE);
			}

			logger.finer(Messages.getString("ControllerFrame.recordCount", new Object[] { model.getData().size() }));
			table.updateUI();
		}
	}

	private class HelpAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String message = Messages.getString("ControllerFrame.authorDeclare");
			JOptionPane.showMessageDialog(null, message, Messages.getString("ControllerFrame.help"),
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
	}
}
