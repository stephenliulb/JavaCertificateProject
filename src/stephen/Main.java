/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import stephen.common.Constant;
import stephen.common.Messages;
import stephen.common.Constant.Mode;
import stephen.server.DBServer;
import stephen.ui.view.ControllerFrame;

/**
 * The class is entry point of the application. It parses work mode from command
 * line and loads configuration data from configuration file
 * 'stephen.properties' if it is available. Then it will delegate all processing
 * control to the component corresponding to the specific work mode.
 * <p>
 * The application has three working modes:
 * <ul>
 * <li>"alone" means that the application directly access the local database
 * file;
 * <li>"network" means that database server is running in remote machine and the
 * client use network facilities to access the remote database;
 * <li>"server" means that the application plays as the role of database server.
 * </ul>
 * 
 * <p>
 * If file handler is configurated in java logging properites file, a default
 * log file "output.log" will be created in current working directory.
 * 
 * 
 * @author Stephen Liu
 * 
 */
public class Main {
	private static Logger logger = Logger.getLogger("stephen");
	static {
		try {
			FileHandler fh = new FileHandler(Constant.DEFAULT_LOG_FILE, true);
			logger.addHandler(fh);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	private static void usage() {
		String usage = "Usage:\njava -jar <path_and_filename> [<mode>]\n\n"
				+ "mode can be \"alone\" or \"server\" or left out entirely (mode is case sensitive);\n"
				+ "   \"alone\" indicate standalone mode;\n " + "   \"server\" indicate server program;\n "
				+ "   left out entirely,in which case the newwork client and gui will run";

		System.out.println(usage);
	}

	private static void version() {
		logger.info("java.version=" + System.getProperty("java.version"));
		logger.info("java.runtime.version=" + System.getProperty("java.runtime.version"));
		logger.info("os.name=" + System.getProperty("os.name"));
	}

	private static Config getConfigData() {
		Config cfg = new Config(Constant.PROPERTY_FILENAME);
		return cfg;
	}

	private static void startAloneMode(Config cfg) {
		logger.info(Messages.getString("Main.startAtAloneMode")); //$NON-NLS-1$
		version();
		new ControllerFrame(cfg, Mode.alone);
		return;
	}

	private static void startNetworkClientMode(Config cfg) {
		logger.info(Messages.getString("Main.startAtNetworkMode")); //$NON-NLS-1$
		version();
		new ControllerFrame(cfg, Mode.network);
		return;
	}

	private static void startServerMode(Config cfg) {
		logger.info(Messages.getString("Main.startAtServerMode")); //$NON-NLS-1$
		version();
		DBServer server = new DBServer(cfg);
		server.doService();
		return;
	}

	/**
	 * Application executing entry point. No more than one command line argument is
	 * expected and the argument is for application mode flag if existed.
	 * <p>
	 * The mode flag must be either "server",indicating the server program must run,
	 * "alone",indicating standalone mode, or left out entirely,in which case the
	 * network client and GUI must run.
	 * 
	 * @param args -- parameters in command line.
	 */
	public static void main(String[] args) {

		switch (args.length) {
		case 0:
			startNetworkClientMode(getConfigData());
			break;
		case 1:
			if (args[0].equals(Mode.alone.name().toLowerCase())) {
				startAloneMode(getConfigData());
				break;
			} else if (args[0].equals(Mode.server.name().toLowerCase())) {
				startServerMode(getConfigData());
				break;
			} else {
				usage();
				System.exit(1);
				break;
			}

		default:
			usage();
			System.exit(1);
			break;
		}

	}

}
