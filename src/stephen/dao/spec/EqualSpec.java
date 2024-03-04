/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.dao.spec;

import java.util.ArrayList;
import java.util.List;

/**
 * This class describe equivalent condition operation.
 * 
 * @see stephen.dao.spec.Spec
 * @author Stephen Liu
 * 
 */
public class EqualSpec extends Spec {
	private static final long serialVersionUID = 1L;
	private String name;
	private String expectedValue;

	/**
	 * Create a equivalent condition.
	 * 
	 * @param name          -- key name.
	 * @param expectedValue -- key value.
	 */
	public EqualSpec(String name, String expectedValue) {
		this.name = name;
		this.expectedValue = expectedValue;
	}

	/**
	 * Get the criteria that key is equal to specific value.
	 * 
	 * @see stephen.dao.spec.Spec#getCriteria()
	 */
	@Override
	public List<List<String>> getCriteria() {
		String expr = name + EQUALOP + (expectedValue == null ? "" : expectedValue); //$NON-NLS-1$
		List<String> criterias1 = new ArrayList<String>();
		criterias1.add(expr);

		List<List<String>> allCriterias = new ArrayList<List<String>>();
		allCriterias.add(criterias1);

		return allCriterias;
	}

	/**
	 * Format equivalent condition to a string.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String str = String.format("(%s=%s)", name, expectedValue); //$NON-NLS-1$
		return str;
	}

}
