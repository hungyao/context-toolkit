package context.arch.intelligibility.expression;

import context.arch.storage.Attribute;
import context.arch.storage.AttributeNameValue;
import context.arch.storage.Attributes;

/**
 * Convenience class to represent a single reason, or reason trace.
 * It is a Conjunction of terminal Parameters.
 * @author Brian Y. Lim
 *
 */
public class Reason extends Conjunction<Parameter<?>> {

	private static final long serialVersionUID = 2500473806018907049L;
	
	public Reason() {
		super();
	}
	
	public Reason(Reason original) {
		super(original);
	}
	
	/**
	 * Create a Reason with a literal added.
	 * @param literal the first literal added
	 */
	public Reason(Parameter<?> literal) {
		super();
		add(literal);
	}
	
	public Reason clone() {
		return new Reason(this);
	}
	
	/**
	 * Convenience method to convert Attributes to a conjunction Reason.
	 * Only collects AttributeNameValue and ignores if Attribute child has no value.
	 * @param atts
	 * @return
	 */
	public static Reason fromAttributes(Attributes atts) {
		Reason reason = new Reason();
		for (Attribute<?> a : atts.values()) {
			if (a instanceof AttributeNameValue) {
				AttributeNameValue<?> att = (AttributeNameValue<?>) a; 
				reason.add(Parameter.instance(
						att.getName(), 
						att.getValue()
					));
			}
		}
		return reason;
	}
	
	/**
	 * Convenience method to convert to Attributes.
	 * @return
	 */
	public Attributes toAttributes() {
		Attributes atts = new Attributes();		
		for (Parameter<?> child : this) {
			atts.add(AttributeNameValue.instance(child.getName(), child.getValue()));
		}		
		return atts;
	}

}
