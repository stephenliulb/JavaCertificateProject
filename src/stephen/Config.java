/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import stephen.common.Constant;
import stephen.common.Messages;

/**
 * This class provides single access point to all parameters in configuration
 * file. The parameters for different work mode is different. <br>
 * The class loads all parameters from the specified configuration file.When
 * these parameters are changed, the changes will be persisted for next reuse.
 * <br>
 * <p>
 * For alone mode, the parameters are as following:
 * <ol>
 * <li>MODE: working mode of the application.</li>
 * <li>DATAFILE: database data file path where data will be persisted.</li>
 * </ol>
 * <p>
 * For network mode, the parameters are as following:
 * <ol>
 * <li>MODE: working mode of the application.
 * <li>SERVER: remote database server host name or IP address.
 * <li>PORT: remote database server port number.
 * </ol>
 * <p>
 * For server mode, the configuration parameters are as following:
 * <ol>
 * <li>MODE: working mode of the application.
 * <li>PORT: database server port number.
 * <li>DATAFILE: database data file path.
 * 
 * </ol>
 * <p>
 * 
 * @author Stephen Liu
 * @see stephen.common.Constant.Mode
 */
public class Config {
	private static Logger logger = Logger.getLogger(Config.class.getName());
	private Properties properties = new Properties();
	private String configFilename;

	/**
	 * Create a Config Object and initialize it by loading a specified configuration
	 * file. When failed during loading the parameters, the object is created
	 * without any configuration parameters.
	 * 
	 * @param configFilename configuration file.
	 */
	public Config(String configFilename) {
		this.configFilename = configFilename;
		File file = new File(configFilename);

		try (FileInputStream fis = new FileInputStream(file);) {

			properties.load(fis);
			logger.info(Messages.getString("Config.fileLoaded", new Object[] { file.getCanonicalPath() }));
		} catch (IOException e) {
			properties.clear();
			String errMsg = Messages.getString("Config.failedLoad", new Object[] { configFilename, e.getMessage() });
			logger.warning(errMsg);
		}
	}

	/**
	 * Get value for a property key.
	 * 
	 * @param key key name.
	 * @return value of the key.
	 */
	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	/**
	 * Validate if the port string is valid integer string. All the chars in port
	 * string must be digital number.
	 * 
	 * @param portString the string of port number.
	 * @return true if it is integer string otherwise false.
	 */
	public static boolean validatePort(String portString) {
		if (portString == null || portString.length() == 0) {
			return false;
		}

		// validate Port
		try {
			Integer.valueOf(portString);
			return true;
		} catch (NumberFormatException exception) {
			return false;
		}
	}

	/**
	 * Set the pair of key and its value.
	 * 
	 * @param key   key name.
	 * @param value key value.
	 */
	public synchronized void setProperty(String key, String value) {
		properties.setProperty(key, value);
		return;
	}

	/**
	 * Save all pairs of key and value into configuration file.
	 * 
	 * @throws IOException thrown if any error happens during saving configuration
	 *                     data into the file.
	 */
	public synchronized void save() throws IOException {

		File file = new File(configFilename);

		if (!file.exists()) {
			file.createNewFile();
			logger.info(Messages.getString("Config.createdFile", new Object[] { file.getCanonicalPath() }));
		}

		try (FileOutputStream out = new FileOutputStream(file);) {
			properties.store(out, Constant.ConfigParameters.COMMENTS);
		}
		logger.info(Messages.getString("Config.saveToFile"));
	}

	/**
	 * Clear all configuration data keep in this object.
	 */
	public synchronized void clear() {
		properties.clear();
		return;
	}

	/**
	 * Get configuration file name.
	 * 
	 * @return configuration file name.
	 */
	public String getConfigFileName() {
		return this.configFilename;
	}

	/**
	 * Convert all configuration data into a string.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		List<String> keyList = Arrays.asList(properties.keySet().toArray(new String[0]));
		Collections.sort(keyList);

		for (String key : keyList) {
			if (buffer.length() > 0)
				buffer.append("\n");
			String value = properties.getProperty(key);
			buffer.append(key + "=" + value);
		}

		return buffer.toString();
	}

}
