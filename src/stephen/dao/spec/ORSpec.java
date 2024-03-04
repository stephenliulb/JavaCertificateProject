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
 * This class describe 'OR' logic operation, which means at least any one of
 * concatenated conditions must be satisfied to match a record.Each of condition
 * operation can be basic comparison operation,'AND' operation or another 'OR'
 * operation.
 * 
 * @see stephen.dao.spec.Spec
 * @author Stephen Liu
 * 
 */
public class ORSpec extends Spec {
	private static final long serialVersionUID = 1L;
	private Spec firstSpec, secondSpec;

	/**
	 * Create a 'OR' logic condition.
	 * 
	 * @param firstSpec  the first condition which can be a basic comparison
	 *                   operation,'OR' operation or 'AND' operation.
	 * @param secondSpec the second condition which can be a basic comparison
	 *                   operation,'OR' operation or 'AND' operation.
	 */
	public ORSpec(Spec firstSpec, Spec secondSpec) {
		this.firstSpec = firstSpec;
		this.secondSpec = secondSpec;
	}

	/**
	 * Get the first condition.
	 * 
	 * @return the firstSpec
	 */
	public Spec getFirstSpec() {
		return firstSpec;
	}

	/**
	 * Set the first condition.
	 * 
	 * @param firstSpec the firstSpec to set
	 */
	public void setFirstSpec(Spec firstSpec) {
		this.firstSpec = firstSpec;
	}

	/**
	 * Get the second condition.
	 * 
	 * @return the secondSpec
	 */
	public Spec getSecondSpec() {
		return secondSpec;
	}

	/**
	 * Set the second condition.
	 * 
	 * @param secondSpec the secondSpec to set
	 */
	public void setSecondSpec(Spec secondSpec) {
		this.secondSpec = secondSpec;
	}

	/**
	 * Get criteria for OR operation. Matched results at least match anyone of
	 * concatenated <Spec> objects'.<br>
	 * For example:<br>
	 * 
	 * The first <code>Spec</code> object has two criteria:
	 * <ol>
	 * <li>{ name="a1" } <br>
	 * This criteria means the matched records must have name="a1"
	 * <li>{ name="a2" } <br>
	 * This criteria means the matched records must have name="a2"
	 * </ol>
	 * 
	 * The second <code>Spec</code> object has two criteria:
	 * <ol>
	 * <li>{ location="b1" } <br>
	 * This criteria means the matched records must have location="b1"
	 * <li>{ location="b2" } <br>
	 * This criteria means the matched records must have location="b2"
	 * </ol>
	 * 
	 * The criteria of the OR operation on above two <code>Spec</code> is as
	 * following:<br>
	 * <ol>
	 * <li>{ name="a1" } <br>
	 * This criteria means the matched records must have name="a1"
	 * <li>{ name="a2" } <br>
	 * This criteria means the matched records must have name="a2"
	 * <li>{ location="b1" } <br>
	 * This criteria means the matched records must have location="b1"
	 * <li>{ location="b2" } <br>
	 * This criteria means the matched records must have location="b2"
	 * </ol>
	 * 
	 * The whole list of criteria means the matched records must have name="a1" or
	 * "a2" or location="b1" or "b2".
	 * 
	 * 
	 * @see stephen.dao.spec.Spec#getCriteria()
	 */
	@Override
	public List<List<String>> getCriteria() {
		List<List<String>> criteriasFromFirst = firstSpec.getCriteria();
		List<List<String>> criteriasFromSecond = secondSpec.getCriteria();

		List<List<String>> mergedCriterias = new ArrayList<List<String>>();
		mergedCriterias.addAll(criteriasFromFirst);
		mergedCriterias.addAll(criteriasFromSecond);

		return mergedCriterias;
	}

	/**
	 * Format 'OR' condition to a string.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String str = String.format("(%s OR %s)", firstSpec, secondSpec); //$NON-NLS-1$
		return str;
	}
}
