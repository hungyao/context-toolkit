package context.arch.discoverer.query.comparison;

import context.arch.discoverer.component.AttributeElement;
import context.arch.storage.AttributeNameValue;

public final class AttributeComparison extends AbstractComparison<AttributeNameValue<?>, AttributeNameValue<?>> {

	public static final String ATTRIBUTE_EQUAL = "ATTRIBUTE_EQUAL";
	public static final String ATTRIBUTE_DIFFERENT = "ATTRIBUTE_DIFFERENT";
	public static final String ATTRIBUTE_GREATER = "ATTRIBUTE_GREATER";
	public static final String ATTRIBUTE_GREATER_EQUAL = "ATTRIBUTE_GREATER_EQUAL";
	public static final String ATTRIBUTE_LESS = "ATTRIBUTE_LESS";
	public static final String ATTRIBUTE_LESS_EQUAL = "ATTRIBUTE_LESS_EQUAL";
	
	public enum Comparison {
		EQUAL(ATTRIBUTE_EQUAL),
		DIFFERENT(ATTRIBUTE_DIFFERENT),
		GREATER(ATTRIBUTE_GREATER),
		GREATER_EQUAL(ATTRIBUTE_GREATER_EQUAL),
		LESS(ATTRIBUTE_LESS),
		LESS_EQUAL(ATTRIBUTE_LESS_EQUAL);
		
		private String name;
		
		Comparison(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}

	private Comparison comparison;

	public static final AttributeComparison EQUAL = new AttributeComparison(Comparison.EQUAL);
	public static final AttributeComparison DIFFERENT = new AttributeComparison(Comparison.DIFFERENT);
	public static final AttributeComparison GREATER = new AttributeComparison(Comparison.GREATER);
	public static final AttributeComparison GREATER_EQUAL = new AttributeComparison(Comparison.GREATER_EQUAL);
	public static final AttributeComparison LESS = new AttributeComparison(Comparison.LESS);
	public static final AttributeComparison LESS_EQUAL = new AttributeComparison(Comparison.LESS_EQUAL);

	public AttributeComparison(Comparison comparison) {
		super(comparison.toString(), 
				AttributeElement.attributeClass, AttributeElement.attributeClass);
		
		this.comparison = comparison;
	}
	
	public Comparison getComparison() {
		return comparison;
	}
	
	@Override
	public Boolean compare(AttributeNameValue<?> att1, AttributeNameValue<?> att2) {		
		Integer comp = att1.compareToValue(att2);
		if (comp == null) { return false; } // att1 and att2 not compatible
		
		switch (comparison) {
		case EQUAL:
			return comp == 0;
		case DIFFERENT:
			return comp != 0;
		case GREATER:
			return comp > 0;
		case GREATER_EQUAL:
			return comp >= 0;
		case LESS:
			return comp < 0;
		case LESS_EQUAL:
			return comp <= 0;
		default:
			return null; // should never happen
		}
	}

}
