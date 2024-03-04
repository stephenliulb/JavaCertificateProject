/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.db;

import java.util.List;

import stephen.db.exception.RecordNotFoundException;

/**
 * The class describes the interface of database search index.
 * 
 * @author Stephen Liu
 * 
 */
public interface Indexable<T> {
    /**
     * Get a list of record numbers for a specified key value. The key can be
     * primary key or not.
     * 
     * @param key
     *            searching key.
     * @return a list of record numbers which match the searching key.
     * @throws RecordNotFoundException
     *             throws when no records match the searching key.
     */
    public List<Integer> getIndex(T key) throws RecordNotFoundException;
}
