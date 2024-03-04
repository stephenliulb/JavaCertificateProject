/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.network;

import java.io.Serializable;
import java.util.Arrays;

import stephen.common.Messages;

/**
 * This class describes the database operation command. Each command has a
 * command type and a list of parameters. It implements the interface
 * <code>Serializable</code> to make it possible that the command object can be
 * serialized and then transfered to remote database server.
 * 
 * @author Stephen Liu
 * 
 */
public class Command implements Serializable {
	private static final long serialVersionUID = 1L;
	private CommandType type;
	private Object[] parameters;

	/**
	 * Create a database remote command.
	 * 
	 * @param type       command type.
	 * @param parameters command parameters.
	 */
	public Command(CommandType type, Object... parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	/**
	 * Get command type.
	 * 
	 * @return command type.
	 */
	public CommandType getCommandType() {
		return type;
	}

	/**
	 * Get command parameters.
	 * 
	 * @return command parameters.
	 */
	public Object[] getParameters() {
		return parameters;
	}

	/**
	 * Convert command type and parameters into a string.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		int recNo;
		String[] data;

		StringBuffer buffer = new StringBuffer();
		buffer.append(type.name());
		buffer.append("/");

		switch (type) {
		case CREATE:
			data = (String[]) parameters[0];
			buffer.append(Arrays.asList(data));
			break;

		case DELETE:
			recNo = ((Integer) parameters[0]).intValue();
			buffer.append(recNo);
			break;

		case FIND:
			String[] criteria = (String[]) parameters[0];
			buffer.append(Arrays.asList(criteria));
			break;

		case ISLOCKED:
			recNo = ((Integer) parameters[0]).intValue();
			buffer.append(recNo);
			break;

		case LOCK:
			recNo = ((Integer) parameters[0]).intValue();
			buffer.append(recNo);
			break;

		case READ:
			recNo = ((Integer) parameters[0]).intValue();
			buffer.append(recNo);
			break;

		case UNLOCK:
			recNo = ((Integer) parameters[0]).intValue();
			buffer.append(recNo);
			break;

		case UPDATE:
			recNo = ((Integer) parameters[0]).intValue();
			data = (String[]) parameters[1];
			buffer.append(recNo);
			buffer.append(",");
			buffer.append(Arrays.asList(data));
			break;

		case CLOSE:
			break;

		default:
			buffer.append(Messages.getString("_Global.operatorNotSupported", new Object[] { type.name() }));
			break;
		}

		return buffer.toString();

	}
}
