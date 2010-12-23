package context.arch.intelligibility.expression;

import context.arch.intelligibility.DescriptiveExplainerDelegate;

/**
 * This only deals with numeric values that can be subjected to inequalities.
 * @author Brian Y. Lim
 */
public class Comparison<T extends Comparable<? super T>> extends Parameter<T> {
	
	private static final long serialVersionUID = -3714993009026305198L;
	
	public enum Relation {
		NO_RELATION("no-relation"),
		EQUALS("="),
		NOT_EQUALS("is not"), // TODO consider not supporting this; just negate Equals; it is quite troublesome
		LESS_THAN_OR_EQUAL("<="),
		GREATER_THAN_OR_EQUAL(">="),
		LESS_THAN("<"),
		GREATER_THAN(">");
		
		private String toString;
		
		private Relation(String toString) {
			this.toString = toString;
		}
		
		public static Relation toRelation(String relation) {
			if (relation.equals(NO_RELATION.toString)) {
				return Relation.NO_RELATION;
			}
			else if (relation.equals(EQUALS.toString)) {
				return Relation.EQUALS;
			}
			else if (relation.equals(LESS_THAN_OR_EQUAL.toString)) {
				return Relation.LESS_THAN_OR_EQUAL;
			}
			else if (relation.equals(GREATER_THAN_OR_EQUAL.toString)) {
				return Relation.GREATER_THAN_OR_EQUAL;
			}
			else if (relation.equals(LESS_THAN.toString)) {
				return Relation.LESS_THAN;
			}
			else if (relation.equals(GREATER_THAN.toString)) {
				return Relation.GREATER_THAN;
			}
			else {
				return Relation.NO_RELATION;
			}
		}
		
		@Override
		public String toString() {
			return toString;
		}
	}
	
	protected Relation relationship;
	
	protected ComparableRange<T> range;
	
	public Comparison(String name, T value, Relation relationship) {
		super(name, value);
		this.relationship = relationship;
		
		range = new ComparableRange<T>(name);
		range.setBound(value, relationship);
	}
	
	public static <T extends Comparable<? super T>> Comparison<T> instance(String name, T value, Relation relationship) {
		return new Comparison<T>(name, value, relationship);
	}
	
	@Override
	public Comparison<T> clone() {
		return new Comparison<T>(name, value, relationship);
	}
	
	public boolean setBound(T bound, Relation relationship) {
		return range.setBound(bound, relationship);
	}
	
	@SuppressWarnings("unchecked")
	public boolean setRange(Comparison<?> other) {
		return range.setRange(((Comparison<T>) other).range);
	}

	public Relation getRelationship() {
		if (range.getMax() == null) {
			return Negation.negateRelation(getRelationship1());
		}
		else {
			if (range.isMaxInclusive()) { return Relation.LESS_THAN_OR_EQUAL; }
			else { return Relation.LESS_THAN; }
		}
	}
	
	public Relation getRelationship1() {
		if (range.isMinInclusive()) { return Relation.LESS_THAN_OR_EQUAL; }
		else { return Relation.LESS_THAN; }
	}
	
	@Override
	public T getValue() {
		if (range.getMax() == null) { return getValue1(); }
		else { return range.getMax(); }
	}
	
	@SuppressWarnings("unchecked")
	public T getValue1() {
		T value1 = range.getMin();
		return value1 != null ? value1 :
			(T) new Double(Double.NEGATIVE_INFINITY); // TODO: this assumption that T is Double is bad
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean isSatisfiedBy(Expression other) {
		if (other instanceof Comparison<?>) {
			Comparison<? super T> c = (Comparison<? super T>) other;

			return this.name.equals(c.name) &&
				   // compare range in range
				   this.range.containsRange(c.range);
		}
		
		else if (other instanceof Parameter<?>) {
			Parameter<?> p = (Parameter<?>) other;

			return this.name.equals(p.name) &&
				   // different from Parameter, by checking if in range
				   this.range.containsValue((Comparable<? super T>) p.getValue());
		}
		
		else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return range.toString();
	}
	
	public String toPrettyString(DescriptiveExplainerDelegate descExplainer) {
		return range.toPrettyString(descExplainer);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		if (o instanceof Comparison<?>) {
			Comparison<T> c2 = (Comparison<T>)o;
			return this.name.equals(c2.name) &&
				   this.value.equals(c2.value) &&
				   this.relationship.equals(c2.relationship);
		}
		else {
			return false;
		}
	}

}
