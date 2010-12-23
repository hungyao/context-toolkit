package context.arch.intelligibility.expression;

import context.arch.intelligibility.DescriptiveExplainerDelegate;

/**
 * An Expression representing name=Value. This is appropriate to represent various aspects of Widgets (or other parametric entities): e.g.
 * Attributes with values, other aspects of ComponentDescription.
 * @author Brian Y. Lim
 *
 */
public class Parameter<T extends Comparable<? super T>> implements Expression {
	
	private static final long serialVersionUID = 5828727756493852215L;

	protected String name;

	/**
	 * Value as represented in an Expression compatible form
	 */
	protected T value;
	
	public Parameter(String name, T value) {
		this.name = name;
		this.value = value;
	}
	
	/**
	 * Convenience method to instantiate the Parameter by automatically inferring the type T from value.
	 * @param <T>
	 * @param name
	 * @param value
	 * @return
	 */
	public static <T extends Comparable<? super T>> Parameter<T> instance(String name, T value) {
		return new Parameter<T>(name, value);
	}
	
	@Override
	public Parameter<T> clone() {
		return new Parameter<T>(name, value);
	}

	public String getName() {
		return name;
	}

	public T getValue() {
		return value;
	}
	
	@SuppressWarnings("unchecked")
	public Class<T> getType() {
		return (Class<T>) value.getClass();
	}
	
	@Override
	public boolean isSatisfiedBy(Expression other) {
		if (other instanceof Parameter<?>) {
			Parameter<?> p = (Parameter<?>) other;
			return this.name.equals(p.name) &&
				   this.value.equals(p.value);
		}
		else {
			return false; // not compatible to compare with other Expression types
		}
	}
	
	public String toString() {
		if (value != null) {
			return name + "=" + value;
		}
		else {
			return name;
		}
	}
	
	public String toPrettyString(DescriptiveExplainerDelegate descExplainer) {
		return descExplainer.getPrettyName(name) + " = " + descExplainer.getPrettyValue(name, value) +
		descExplainer.getUnit(name);
	}

}
