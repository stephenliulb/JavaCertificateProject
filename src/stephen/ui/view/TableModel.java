/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.ui.view;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import stephen.common.Messages;
import stephen.dao.DataTransferObject;
import stephen.dao.exception.DAOException;
import stephen.db.DBSchema;
import stephen.db.DBSchemaV2;
import stephen.ui.model.UIDataModel;

/**
 * This class is inherited from the class
 * 'javax.swing.table.AbstractTableModel'. It provides a table view to retrieve
 * data from data model. This class directly support <code>JTable</code> view in
 * client GUI.
 * 
 * @author Stephen Liu
 * 
 */
public class TableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private String[] columns;
	private UIDataModel dataModel;

	/**
	 * Create an TableModel object which is initalized by the passing parameters.
	 * 
	 * @param columns   tables columns' names.
	 * @param dataModel the data source where the TableModel will grab the data and
	 *                  pass them to JTable.
	 */
	public TableModel(String[] columns, UIDataModel dataModel) {
		this.columns = columns;
		this.dataModel = dataModel;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return dataModel.getData().size();
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		DataTransferObject obj = dataModel.getData().get(row);
		String[] data = obj.getData();
		return data[col];
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columns.length;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int col) {
		return columns[col];
	}

	/**
	 * Only column 'room'(room number) 'owner'(owner id) is editable.
	 * 
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowNum, int colNum) {
		if (columns[colNum].equalsIgnoreCase(DBSchemaV2.OWNER) || columns[colNum].equalsIgnoreCase(DBSchemaV2.ROOM)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Change the value at a specific position in JTable view. If primary key value
	 * is needed to be changed, a new record,which has the changed primary value,
	 * will be inserted into database firstly and then the original record will be
	 * deleted.
	 * 
	 * Only valid value will be saved into database; otherwise,an error message box
	 * will pop up to give the reason of error and allow user to re-input a new
	 * value.
	 * 
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int,
	 *      int)
	 */
	public void setValueAt(Object value, int rowNum, int colNum) {

		DataTransferObject obj = dataModel.getData().get(rowNum);

		if (columns[colNum].equalsIgnoreCase(DBSchemaV2.OWNER)) {

			String originalOwnerValue = obj.getOwner();

			String newOwnerValue = (String) value;

			if (newOwnerValue.equals(originalOwnerValue)
					|| (originalOwnerValue == null && newOwnerValue.length() == 0)) {
				return;
			}

			if (!validateOwner(newOwnerValue)) {
				return;
			}

			obj.setNewOwner((String) value);
			try {
				dataModel.updateData(obj);
			} catch (DAOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);

				// restore the original value when exception happened.
				obj.setOwner(originalOwnerValue);
			}

		} else if (columns[colNum].equalsIgnoreCase(DBSchemaV2.ROOM)) {

			// change the room number which is the part of primary key(name+room+location)
			String newRoomNumberValue = (String) value;
			String originalRoomNumberValue = obj.getRoom();

			if (newRoomNumberValue.equals(originalRoomNumberValue)) {
				return;
			}

			if (!validateRoom(newRoomNumberValue)) {
				return;
			}

			try {

				// delete original record.
				dataModel.deleteData(obj);

				// insert a new record with new primary key.
				obj.setRoom(newRoomNumberValue);
				dataModel.insertData(obj);

			} catch (DAOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);

			}

		}

		// notify all views
		this.fireTableDataChanged();

	}

	private boolean validateRoom(String room) {
		int roomLength = DBSchemaV2.getInstance().getColumnLength(DBSchema.ROOM);

		if (room == null || room.length() > roomLength) {
			String errMsg = Messages.getString("TableModel.roomNumberInvalid", new Object[] { room, roomLength }); //$NON-NLS-1$
			JOptionPane.showMessageDialog(null, errMsg, "Exception", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;

	}

	private boolean validateOwner(String ownerId) {
		if (ownerId == null || ownerId.length() == 0) {
			return true;
		}

		int ownerIDLength = DBSchemaV2.getInstance().getColumnLength(DBSchema.OWNER);

		if (ownerId.length() != ownerIDLength) {
			String errMsg = Messages.getString("TableModel.OwnerIdInvalid", new Object[] { ownerId, ownerIDLength }); //$NON-NLS-1$
			JOptionPane.showMessageDialog(null, errMsg, "Exception", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// ownId is all blanks
		if (ownerId.trim().length() == 0) {
			return true;
		}

		try {
			Integer.valueOf(ownerId);
		} catch (NumberFormatException exception) {
			String errMsg = Messages.getString("TableModel.OwnerIdHasIlegalChars", new Object[] { ownerId }); //$NON-NLS-1$
			JOptionPane.showMessageDialog(null, errMsg, "Exception", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}
}