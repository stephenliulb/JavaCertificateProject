/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * This class houses independent common utility functions across the application.  
 * @author Stephen Liu
 * 
 */
public class Utils {

    /**
     * Convert an throwable object into a string.
     * @param t throwable object.
     * @return the exception stack trace string.
     */
	public static String getExceptionString(Throwable t) {
		String content = null;
		try (ByteArrayOutputStream bo = new ByteArrayOutputStream(); 
			 PrintStream pstream = new PrintStream(bo);) {
			t.printStackTrace(pstream);
			content = bo.toString();
		} catch (IOException x) {
			x.printStackTrace();
		}

		return content;
	}
    
    /**
     * Convert from Interger array to int array.
     * 
     * @param integerArray
     *            integer array.
     * @return int array.
     */
    public static int[] getIntArray(Integer[] integerArray) {
    	return Arrays.stream(integerArray).mapToInt(Integer::intValue).toArray();
    }
    
    /**
     * Convert from Interger List to int array.
     * 
     * @param integerList
     *            integer list.
     * @return int array.
     */
    public static int[] getIntArray(List<Integer> integerList) {
    	return integerList.stream().mapToInt(Integer::intValue).toArray();
    }
    
    
}
