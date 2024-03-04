/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.common;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class manages all string resources in the application.
 * 
 * @author Stephen Liu
 * 
 */
public class Messages {
	private static final String BUNDLE_NAME = "stephen.common.messages";

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * Get a message string for a specified key.
	 * 
	 * @param key message key.
	 * @return a message string for the specified key.
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * This method deals with compound message(contains variables) in the properties
	 * file.
	 * 
	 * @param key              represents the key of message in the properties file.
	 * @param messageArguments The variable values replacing the corresponding
	 *                         arguments in the message pattern.
	 * @return a message string for the specified key.
	 */
	public static String getString(String key, Object[] messageArguments) {
		MessageFormat formatter = new MessageFormat("");
		formatter.applyPattern(RESOURCE_BUNDLE.getString(key));
		return formatter.format(messageArguments);
	}
}
