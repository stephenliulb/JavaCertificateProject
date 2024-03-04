/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */
package stephen.network;

import stephen.db.DuplicateKeyException;
import stephen.db.exception.RecordNotFoundException;

/**
 * This class provides the interface of processing database commands.
 * 
 * @author Stephen Liu
 * 
 */
public interface CommandHandler {
    
    /**
     * Execute database command. The result is Object type. It is callers' responsibility to parse the result.
     * 
     * @param command command object which contains command type and command parameters.
     * @return the result after processed the command.
     * @throws RecordNotFoundException throws if the record which the command applies on doesn't exist.
     * @throws DuplicateKeyException throws if create an new record which is existing in data store.
     * @throws RemoteException throws if any exception occurred during executing the command in remote database server.
     */
    
    public Object handle(Command command) throws RecordNotFoundException,DuplicateKeyException, RemoteException;
}
