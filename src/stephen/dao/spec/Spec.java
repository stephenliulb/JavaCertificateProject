/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.dao.spec;

import java.io.Serializable;
import java.util.List;

/**
 * This class provides a composite interface to construct a dynamically search condition
 * expression to retrieve records from data store.
 * <p>
 * There are two kinds of condition operations:
 * <ul>
 * <li>Comparison operations, such as EQUAL,GREATTHAN,LESSTHAN etc,<br>
 *    which is used to set specific condition for a field value in a record. 
 *    For all matched records, their corresponding field value must satisfy 
 *    the condition.    
 * <li>Logic operations,such as AND,OR etc,<br>
 *    which is used to nest different search condition.<br>
 *    AND operation will mean for matched records,their fields' value must satisfy both
 *    of concatenated conditions.<br>
 *    OR operation  will mean for matched records,their fields' value must at least 
 *    satisfy any one of concatenated conditions.<br>
 * </ul>
 * 
 * Each of condition operation has to be inherited from the class
 * <code>Spec</code>. These condition operations can be concatenated together by
 * AND operation or OR operation to construct a flexible condition expression.
 * <p>
 * For example,<br>
 * 1. Retrieve all records in which hotel name starts with 'Palace' and city name starts with 'Smallville'.<br>    
 *   The searching condition is:<br> 
 *   <code>
 *     new ANDSpec(new EqualSpec("name","Palace"),
 *                                      new EqualSpec("location","Smallville"));
 *   </code>                                      
 * <p>
 * 2. Retrieve all records in which hotel name starts with 'Palace' or city name starts with 'Smallville'.<br>
 *   The searching condition is:<br>
 *   <code> 
 *     new ORSpec(new EqualSpec("name","Palace"),
 *                                      new EqualSpec("location","Smallville"));
 *   </code>
 * <p>
 * 3. Retrieve all records in database.<br>
 *   The searching condition is:<br>
 *   <code> 
 *     new ORSpec(new EqualSpec("name",""),new EqualSpec("location",""));
 *   </code>  
 * <p>
 * 4. Retrieve all records in which hotel name starts with 'Palace'.<br>
 *   The searching condition is:<br>
 *   <code> 
 *     new EqualSpec("name","Palace");
 *   </code>  
 * <p>
 * 5. Retrieve all records in which city name starts with 'Smallville'.<br>
 *   The searching condition is:<br>
 *   <code> 
 *     new EqualSpec("location","Smallville");
 *   </code>  
 * 
 * @see stephen.dao.spec.ANDSpec
 * @see stephen.dao.spec.ORSpec
 * @see stephen.dao.spec.EqualSpec
 * 
 * @author Stephen Liu
 * 
 */
public abstract class Spec implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * Operator of equivalent.
     */
    public static String EQUALOP = "=";

    /**
     * Get a set of criteria,which has 'OR' relationship; Each criteria is
     * composed by a set of conditions,which have 'AND' relationship. 
     * <p>
     * For example: There are a list of criteria returned: 
     * <ul>
     * <li>Criteria1: {name='Palace',location='Smallville'} 
     * <li>Criteria2: {name='Castle',location='Smallville'}
     * </ul>
     * <br>
     * That means to retrieve all records which should match (name='Palace' AND
     * location='Smallville') OR (name='Castle' AND location='Smallville').
     * 
     * @return a list of criteria.
     */
    public abstract List<List<String>> getCriteria();
}
