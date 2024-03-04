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
 * This class describe 'AND' logic operation, which means both concatenated
 * conditions must be satisfied to match a record.Each of condition operation
 * can be basic comparison operation,'OR' operation or another 'AND' operation.
 * 
 * @see stephen.dao.spec.Spec
 * @author Stephen Liu
 * 
 */
public class ANDSpec extends Spec {
	private static final long serialVersionUID = 1L;

	private Spec firstSpec, secondSpec;

	/**
	 * Create a 'AND' logic condition.
	 * 
	 * @param firstSpec  the first condition which can be a basic comparison
	 *                   operation,OR operation or another AND operation.
	 * @param secondSpec the second condition which can be a basic comparison
	 *                   operation,OR operation or another AND operation.
	 */
	public ANDSpec(Spec firstSpec, Spec secondSpec) {
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
	 * Get criteria for AND operation.Matched results must satisfy both concatenated
	 * <Spec> object criteria at same time.<br>
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
	 * The criteria of the AND operation on above two <code>Spec</code> are as
	 * following:<br>
	 * <ol>
	 * <li>{name="a1",location="b1"} <br>
	 * This criteria means the matched records must have name="a1" and
	 * location="b1".
	 * <li>{name="a1",location="b2"} <br>
	 * This criteria means the matched records must have name="a1" and
	 * location="b2".
	 * <li>{name="a2",location="b1"} <br>
	 * This criteria means the matched records must have name="a2" and
	 * location="b1".
	 * <li>{name="a2",location="b2"} <br>
	 * This criteria means the matched records must have name="a2" and
	 * location="b2".
	 * </ol>
	 * 
	 * The whole list of criteria means the matched records must have name="a1" or
	 * "a2" and location="b1" or "b2".
	 * 
	 * @see stephen.dao.spec.Spec#getCriteria()
	 */
	@Override
	public List<List<String>> getCriteria() {
		List<List<String>> criteriasFromFirst = firstSpec.getCriteria();
		List<List<String>> criteriasFromSecond = secondSpec.getCriteria();

		List<List<String>> mergedCriterias = new ArrayList<List<String>>();
		for (List<String> firstCriteria : criteriasFromFirst) {
			for (List<String> secondCriteria : criteriasFromSecond) {
				List<String> mergedCriteria = new ArrayList<String>();
				mergedCriteria.addAll(firstCriteria);
				mergedCriteria.addAll(secondCriteria);
				mergedCriterias.add(mergedCriteria);
			}
		}

		return mergedCriterias;
	}

	/**
	 * Format 'AND' condition to a string.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String str = String.format("(%s AND %s)", firstSpec, secondSpec);//$NON-NLS-1$
		return str;
	}
}
