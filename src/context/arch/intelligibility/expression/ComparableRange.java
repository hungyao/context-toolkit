package context.arch.intelligibility.expression;

import context.arch.intelligibility.DescriptiveExplainerDelegate;
import context.arch.intelligibility.expression.Comparison.Relation;

/**
 * Continuous range that can specify whether the bounds are inclusive or exclusive.
 * This is similar to org.apache.commons.lang.math.NumberRange.
 * Easiest to think of this as a Number range, but it also applies to any Comparable object
 * @author Brian Y. Lim
 *
 */
public class ComparableRange<T extends Comparable<? super T>> {

	private T min;
	private T max;

	private boolean minInclusive = false;
	private boolean maxInclusive = false;
	
	private String name = "number";
	
	/**
	 * Creates an unbounded range.
	 */
	public ComparableRange() {}

	public ComparableRange(String name) {
		this.name = name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public ComparableRange(T min, boolean minInclusive, T max, boolean maxInclusive) {
		this.min = min;
		this.max = max;
		this.minInclusive = minInclusive;
		this.maxInclusive = maxInclusive;
	}
	
	public boolean isMinInclusive() {
		return minInclusive;
	}

	public boolean isMaxInclusive() {
		return maxInclusive;
	}
	
	public boolean setRange(ComparableRange<T> other) {
//		System.out.print("setRange("+other+"): ");
		return setRange(other.getMin(), other.minInclusive, other.getMax(), other.maxInclusive);
	}
	
	/*
	 * Doesn't undo if it partially fails
	 */
	private boolean setRange(T min, boolean minInclusive, T max, boolean maxInclusive) {
		boolean tightened = false;
		
		if (min != null) {
			tightened = setBound(min, true, minInclusive);
		}
		if (max != null) {
			tightened |= setBound(max, false, maxInclusive); // OR with previous
		}

//		System.out.println("range: " + this);
		
		return tightened;
	}
	
	/**
	 * Would either not change the bound, or tighten it.
	 * @param bound
	 * @param relation
	 * @return
	 */
	public boolean setBound(T bound, Relation relation) {
		boolean min;
		boolean inclusive;
		
		switch (relation) {
		case EQUALS:
			// set both bounds to the same, and inclusive
			boolean ret = setBound(bound, true, true);
			ret &= setBound(bound, false, true);
			return ret;
		case GREATER_THAN:
			min = true;
			inclusive = false;
			break;
		case GREATER_THAN_OR_EQUAL:
			min = true;
			inclusive = true;
			break;
		case LESS_THAN:
			min = false;
			inclusive = false;
			break;
		case LESS_THAN_OR_EQUAL:
			min = false;
			inclusive = true;
			break;
		default:
			return false;
		}
		
		boolean ret = setBound(bound, min, inclusive);

//		System.out.print("setBound("+bound+", "+relation+"): ");
//		System.out.println("range: " + this);
		
		return ret;
	}
	
	public boolean setBound(T bound, boolean min, boolean inclusive) {
		if (containsValue(bound)) {
			if (min) {
				this.min = bound;
				this.minInclusive = inclusive;
			}
			else {
				this.max = bound;
				this.maxInclusive = inclusive;
			}
			return true;
		}
		return false;
	}
	
	public boolean containsValue(Comparable<? super T> value) {
		return satisfiesMin(value) && satisfiesMax(value);
	}
	
	public boolean containsRange(ComparableRange<? super T> range) {
		boolean minSatisfied = satisfiesMin(range.getMin(), range.minInclusive) &&
							   satisfiesMax(range.getMin());
		boolean maxSatisfied = satisfiesMax(range.getMax(), range.maxInclusive) && 
							   satisfiesMin(range.getMax());
		return minSatisfied && maxSatisfied;
	}
	
	/**
	 * Whether value is more than min; value and max may be inclusive or exclusive.
	 * Several cases for range.min to satisfy min:
	 * 1) range.min(incl. or excl.) >= min(incl.)
	 * 2) range.min(excl.) >= min(excl.)
	 * @param value
	 * @param valueInclusive
	 * @return
	 */
	private boolean satisfiesMin(Comparable<? super T> value, boolean valueInclusive) {
		// null would mean unbounded
		if (getMin() == null) { return true; }
		
		if (minInclusive || // when current boundary is inclusive, then other doesn't matter
				(!valueInclusive && !minInclusive) // both boundaries exclusive
				) {
			//return value.doubleValue() >= getMin().doubleValue();
			// true if value >= min
			return value.compareTo(getMin()) >= 0;
		}
		else {
			//return value.doubleValue() > getMin().doubleValue();
			// true if value > min 
			return value.compareTo(getMin()) > 0;
		}
	}
	
	private boolean satisfiesMin(Comparable<? super T> value) {
		return satisfiesMin(value, true);
	}
	
	/**
	 * Whether value is less than max; value and max may be inclusive or exclusive.
	 * @param value
	 * @param valueInclusive
	 * @return
	 */
	private boolean satisfiesMax(Comparable<? super T> value, boolean valueInclusive) {
		// null would mean unbounded
		if (getMax() == null) { return true; }

		if (maxInclusive || // when current boundary is inclusive, then other doesn't matter
				(!valueInclusive && !maxInclusive) // both boundaries exclusive
				) {
			//return value.doubleValue() <= getMax().doubleValue();
			return value.compareTo(getMax()) <= 0;
		}
		else {
			//return value.doubleValue() < getMax().doubleValue();
			return value.compareTo(getMax()) < 0;
		}
	}
	
	private boolean satisfiesMax(Comparable<? super T> value) {
		return satisfiesMax(value, true);
	}

	public T getMax() {
		return max;
	}

	public T getMin() {
		return min;
	}
	
	@Override
	public String toString() {
		if (min == max) { // not null and equal
			// means this Range is really an equality
			return name + " = " + min;
		}
		
		return (min == null ? "" :
					(min.toString() + " " +
					(minInclusive ? Relation.LESS_THAN_OR_EQUAL : Relation.LESS_THAN) +
					" ")) + 
			   name +
			   (max == null ? "" : 
				    (" " +
				   	(maxInclusive ? Relation.LESS_THAN_OR_EQUAL : Relation.LESS_THAN) +
				   	" " + max.toString()));
	}
	
	/**
	 * Returns toString() in pretty form where the names and values are made pretty.
	 * @param descExplainer used as a look-up dictionary to convert variable names to pretty names
	 * @return
	 */
	public String toPrettyString(DescriptiveExplainerDelegate descExplainer) {
		if (min == max) { // not null and equal
			// means this Range is really an equality
			return name + " = " + min + descExplainer.getUnit(name);
		}
		
		// only lower bound
		if (max == null) {
			return descExplainer.getPrettyName(name) + " " +
				(minInclusive ? Relation.LESS_THAN_OR_EQUAL : Relation.LESS_THAN) +
				" " + descExplainer.getPrettyValue(name, min) +
				descExplainer.getUnit(name);
		}
		
		// only upper bound or both bounds
		return (min == null ? "" :
					(descExplainer.getPrettyValue(name, min) + " " +
					(minInclusive ? Relation.LESS_THAN_OR_EQUAL : Relation.LESS_THAN) +
					" ")) + 
			   descExplainer.getPrettyName(name) +
			   (" " + (maxInclusive ? Relation.LESS_THAN_OR_EQUAL : Relation.LESS_THAN) +
				   	" " + descExplainer.getPrettyValue(name, max)) +
			   descExplainer.getUnit(name);
	}

}
