/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved.
 *
 */

package stephen.dao;

import java.util.List;

import stephen.dao.exception.DAOException;
import stephen.dao.spec.Spec;

/**
 * This class describes the interface of Data Access Object. It defines the
 * basic data access operations to manipulate the underlying data store,such as
 * update/insert/delete/search records in data store. Any manipulation on stale
 * record will be terminated to avoid data loss <br>
 * This interface decouples upper layer application from data store type,
 * location and implementation mechanism.
 * 
 * @author Stephen Liu
 * @param <T>
 *            It is a generic type of data transfer object which will help carry
 *            data into data store or return data from data store.
 */

public interface DAOInterface<T> {
    /**
     * Select a list of data records from data store based on specific search
     * conditions. The search conditions are represented by <code>Spec</code>
     * object which flexible reflects different combination of retrieve
     * requirements.
     * 
     * @param spec
     *            describes the combination of search conditions.
     * @return result set which holds a list of data records.
     * @throws DAOException
     *             throws when some errors happen when accessing the data store.
     */
    ResultSet<T> select(Spec spec) throws DAOException;

    /**
     * Delete a list of data records from the data store.
     * 
     * @param deleteRecords
     *            a list of data records which will be removed from data store.
     * @throws DAOException
     *             throws when some errors happen when accessing the data store.
     */
    void delete(List<T> deleteRecords) throws DAOException;

    /**
     * Update a list of data records in the data store.
     * 
     * @param updateRecords
     *            a list of data records which will be updated in data store.
     *         an exception object if failed.
     * @throws DAOException
     *             throws when some errors happen when accessing the data store.
     */
    void update(List<T> updateRecords) throws DAOException;

    /**
     * Insert a list of data records into the data store.
     * 
     * @param newRecords
     *            a list of data records which will be inserted into data store.
     * @throws DAOException
     *             throws when some errors happen when accessing the data store.
     */
    void insert(List<T> newRecords) throws DAOException;
}
