/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.dao;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.io.Serializable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import stephen.common.Constant;
import stephen.common.Messages;
import stephen.db.DBSchemaV2;

/**
 * This class describes a data transfer object. The application will use the
 * this object to carry record data to DAO object to manipulate the data store.
 * This class also provides facilities to hold original record data and new
 * record data when users try to update the record in data store;The original
 * record data will help identify whether the same record data in data storehas
 * been changed or not.
 * <p>
 * Data transfer object implements <code>Serializable</code> interface,which
 * will help passing data transfer object to remote database system. <br>
 * This class also provide rich facilities to convert data among different
 * formats.
 * 
 * @author Stephen Liu
 * 
 */
public class DataTransferObject implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name = null;
    private String room = null;
    private String location = null;
    
    private Integer size = null;
    private Integer newSize = null;
    
    private Boolean smoking = null;
    private Boolean newSmoking = null;
    
    private Double rate = null;
    private Double newRate = null;
    
    private Date date = null;
    private Date newDate = null;
    
    private String owner = null;
    private String newOwner = null;

    /**
     * Create a default data transfer object. All variables in this object are
     * not initialised.
     */
    public DataTransferObject() {
    };

    /**
     * Create a default data transfer object and initialise it by a array of
     * string type of parameters.
     * 
     * @param data
     *            a array of string type of the parameters;<br>
     *            data[0] is hotel name;<br>
     *            data[1] is room number;<br>
     *            data[2] is city name;<br>
     *            data[3] is size of the room;<br>
     *            data[4] is smoking flag;<br>
     *            data[5] is rate value;<br>
     *            data[6] is date value;<br>
     *            data[7] is owner value
     */
    public DataTransferObject(String... data) {
	String nameStr = null, roomStr = null,locationStr = null, sizeStr = null, smokingStr = null, rateStr = null, dateStr = null, ownerStr = null;

	if (data.length >= 1) {
	    nameStr = data[0];
	}
	
	if (data.length >= 2) {
	    roomStr = data[1];
	}

	if (data.length >= 3) {
	    locationStr = data[2];
	}

	if (data.length >= 4) {
	    sizeStr = data[3];
	}

	if (data.length >= 5) {
	    smokingStr = data[4];
	}

	if (data.length >= 6) {
	    rateStr = data[5];
	}

	if (data.length >= 7) {
	    dateStr = data[6];
	}

	if (data.length >= 8) {
	    ownerStr = data[7];
	}

	this.name = nameStr;
	this.room = roomStr;
	this.location = locationStr;

	if (sizeStr != null && sizeStr.length() > 0) {
	    this.size = Integer.valueOf(sizeStr);
	}

	if (smokingStr != null && smokingStr.length() > 0) {
	    this.smoking = smokingStr.equals("Y") ? true : false;
	}

	try {
	    if (rateStr != null && rateStr.length() > 0) {
		this.rate = Double.valueOf(NumberFormat.getCurrencyInstance()
			.parse(rateStr).doubleValue());
	    }
	} catch (ParseException e) {
	    String errMsg = Messages.getString("DataTransferObject.0",new Object[]{rate, Arrays.asList(data), Locale.getDefault()});
	    throw new RuntimeException(errMsg, e);
	}

	try {
	    if (dateStr != null && dateStr.length() > 0) {
		this.date = new SimpleDateFormat(Constant.DATE_PATTERN).parse(dateStr);
	    }
	} catch (ParseException e) {
	    String errMsg = Messages.getString("DataTransferObject.1",new Object[]{date, Arrays.asList(data), Constant.DATE_PATTERN});
	    throw new RuntimeException(errMsg, e);
	}

	if (ownerStr != null && ownerStr.length() > 0) {
	    this.owner = ownerStr;
	}
    }

	/**
	 * Get all variables in this object as string array. The order of variables in
	 * the array keeps consistent with the order of all columns in database schema.
	 * 
	 * @return string array containing all variables in the object.
	 */
	public String[] getData() {
		String[] data = new String[DBSchemaV2.getInstance().getColumnNumber()];
		data[0] = name;
		data[1] = room;
		data[2] = location;
		data[3] = getSizeString(size);
		data[4] = getSmokingString(smoking);
		data[5] = getRateString(rate);
		data[6] = getDateString(date);
		data[7] = getOwnerString(owner);

		return data;
	}

	/**
	 * Get all latest variables in this object as string array. The order of
	 * variables in the array keeps consistent with the order of all columns in
	 * database schema. if new values for items are not set, the original values for
	 * the corresponding items will be used in result.
	 * 
	 * @return string array containing all latest variables in the object.
	 */
	public String[] getLatestData() {
		String[] data = new String[DBSchemaV2.getInstance().getColumnNumber()];
		data[0] = name;
		data[1] = room;
		data[2] = location;
		data[3] = (newSize != null) ? getSizeString(newSize) : getSizeString(size);
		data[4] = (newSmoking != null) ? getSmokingString(newSmoking) : getSmokingString(smoking);
		data[5] = (newRate != null) ? getRateString(newRate) : getRateString(rate);
		data[6] = (newDate != null) ? getDateString(newDate) : getDateString(date);
		data[7] = (newOwner != null) ? getOwnerString(newOwner) : getOwnerString(owner);

		return data;
	}

	/**
	 * Get hotel name.
	 * 
	 * @return hotel name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set hotel name.
	 * 
	 * @param name hotel name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get room number.
	 * 
	 * @return room number.
	 */
	public String getRoom() {
		return room;
	}

	/**
	 * Set room number.
	 * 
	 * @param room room number.
	 */
	public void setRoom(String room) {
		this.room = room;
	}

	/**
	 * Get city name.
	 * 
	 * @return city name.
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Set city name.
	 * 
	 * @param location city name.
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * Get occupancy number of this room.
	 * 
	 * @return the maximum number of people permitted in this room.
	 */
	public int getSize() {
		return size;
	}

	private String getSizeString(Integer size) {
		if (size == null) {
			return "";
		}

		String sizeStr = String.valueOf(size);
		return (sizeStr.length() < 4 ? sizeStr : sizeStr.substring(0, 3));
	}

	/**
	 * Set maximum number of people permitted in this room.
	 * 
	 * @param size maximum number.
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * Get smoking flat in this room.
	 * 
	 * @return true if allowed, otherwise false.
	 */
	public boolean isSmoking() {
		return smoking;
	}

	private String getSmokingString(Boolean smoking) {
		if (smoking == null) {
			return "";
		}
		return smoking ? "Y" : "N";
	}

	/**
	 * Set the smoking flag.
	 * 
	 * @param smoking smoking flag
	 */
	public void setSmoking(boolean smoking) {
		this.smoking = smoking;
	}

	/**
	 * Get the price of the room.
	 * 
	 * @return the price of the room.
	 */
	public double getRate() {
		return rate;
	}

	private String getRateString(Double rate) {
		if (rate == null) {
			return "";
		}

		NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
		return currencyFormatter.format(rate);
	}

	/**
	 * Set the price of the room.
	 * 
	 * @param rate the price of the room.
	 */
	public void setRate(double rate) {
		this.rate = rate;
	}

	/**
	 * Get the date available.
	 * 
	 * @return date available.
	 */
	public Date getDate() {
		return date;
	}

	private String getDateString(Date date) {
		if (date == null) {
			return "";
		}

		SimpleDateFormat formater = new SimpleDateFormat(Constant.DATE_PATTERN);
		return formater.format(date);
	}

	private String getOwnerString(String owner) {
		if (owner == null) {
			return "";
		}

		return owner;
	}

	/**
	 * Set date available.
	 * 
	 * @param date data available.
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Get owner id.
	 * 
	 * @return owner id.
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Set owner id.
	 * 
	 * @param owner owner id.
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * Get new size of the room..
	 * 
	 * @return new room size;
	 */
	public Integer getNewSize() {
		return newSize;
	}

	/**
	 * Set new size of the room.
	 * 
	 * @param newSize new room size.
	 */
	public void setNewSize(Integer newSize) {
		this.newSize = newSize;
	}

	/**
	 * Get new smoking flag.
	 * 
	 * @return true if allow to smoke; otherwise, false;
	 */
	public Boolean getNewSmoking() {
		return newSmoking;
	}

	/**
	 * Set new smoking flag.
	 * 
	 * @param newSmoking new smoking flag.
	 */
	public void setNewSmoking(Boolean newSmoking) {
		this.newSmoking = newSmoking;
	}

	/**
	 * Get new room price.
	 * 
	 * @return new room price.
	 */
	public Double getNewRate() {
		return newRate;
	}

	/**
	 * Set new room price.
	 * 
	 * @param newRate new room price.
	 */
	public void setNewRate(Double newRate) {
		this.newRate = newRate;
	}

	/**
	 * Get new available date for the record.
	 * 
	 * @return new available date.
	 */
	public Date getNewDate() {
		return newDate;
	}

	/**
	 * Set new available date for the record.
	 * 
	 * @param newDate new available date.
	 */
	public void setNewDate(Date newDate) {
		this.newDate = newDate;
	}

	/**
	 * Get new owner id.
	 * 
	 * @return new owner id.
	 */
	public String getNewOwner() {
		return newOwner;
	}

	/**
	 * Set new owner id.
	 * 
	 * @param newOwner new owner id.
	 */
	public void setNewOwner(String newOwner) {
		this.newOwner = newOwner;
	}

	/**
	 * Format all the data in this object as one string.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append(name);
		buffer.append(",");
		buffer.append(room);
		buffer.append(",");
		buffer.append(location);
		buffer.append(",");
		buffer.append(getSizeString(size));
		if (newSize != null) {
			buffer.append("(->");
			buffer.append(getSizeString(newSize));
			buffer.append(")");
		}
		buffer.append(",");

		buffer.append(getSmokingString(smoking));
		if (newSmoking != null) {
			buffer.append("(->");
			buffer.append(getSmokingString(newSmoking));
			buffer.append(")");
		}
		buffer.append(",");

		buffer.append(getRateString(rate));
		if (newRate != null) {
			buffer.append("(->");
			buffer.append(getRateString(newRate));
			buffer.append(")");
		}
		buffer.append(",");

		buffer.append(getDateString(date));
		if (newDate != null) {
			buffer.append("(->");
			buffer.append(getDateString(newDate));
			buffer.append(")");
		}
		buffer.append(",");

		buffer.append(getOwnerString(owner));
		if (newOwner != null) {
			buffer.append("(->");
			buffer.append(getOwnerString(newOwner));
			buffer.append(")");
		}

		return buffer.toString();

	}

}
