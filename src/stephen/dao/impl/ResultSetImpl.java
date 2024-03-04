/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.dao.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import stephen.common.Messages;
import stephen.dao.DataTransferObject;
import stephen.dao.ResultSet;

/**
 * This class implements the interface <code>ResultSet</code> by using class
 * <code>DataTransferObject</code> as parameterized class.
 * 
 * @see stephen.dao.ResultSet
 * @author Stephen Liu
 * 
 */
public class ResultSetImpl extends ResultSet<DataTransferObject> {

	private List<DataTransferObject> resultSet;

	/**
	 * Create a result set object.
	 */
	public ResultSetImpl() {
		resultSet = new ArrayList<DataTransferObject>();
	}

	/**
	 * Create a result set object initialized with a list of data.
	 * 
	 * @param resultSet -- a list of records as return to caller.
	 */
	public ResultSetImpl(List<DataTransferObject> resultSet) {
		this.resultSet = resultSet;
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<DataTransferObject> iterator() {
		return new RSIterator();
	}

	private class RSIterator implements Iterator<DataTransferObject> {
		private int index = 0;

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return index < resultSet.size() ? true : false;
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		public DataTransferObject next() {
			return resultSet.get(index++);
		}

		/**
		 * Currently this method is not supported in the class.
		 * 
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException(Messages.getString("_Global.removeNotSupported")); //$NON-NLS-1$

		}
	}

	/**
	 * Format all data in result set object to a string.
	 * 
	 * @return a string.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();

		for (DataTransferObject item : resultSet) {
			sb.append(item.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
}
