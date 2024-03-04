/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.dao;


/**
 * This class describes the result set after DAO object successfully processes the 
 * requests. The result set contains a list of data records.<p>
 * 
 * It inherits from the interface <code>Iterable<T></code>. It is convenient to use enhanced 
 * <code>for</code> loop to retrieve data inside.
 * 
 * @author Stephen Liu
 * 
 */
public abstract class ResultSet<T> implements Iterable<T> {
    
}
