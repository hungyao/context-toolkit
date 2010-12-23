package context.arch.discoverer.query.comparison;

public class ValueComparison<T extends Comparable<? super T>> extends AbstractComparison<T, T> {

	public static final String EQUAL = "EQUAL";
	public static final String DIFFERENT = "DIFFERENT";
	public static final String GREATER = "GREATER";
	public static final String GREATER_EQUAL = "GREATER_EQUAL";
	public static final String LESS = "LESS";
	public static final String LESS_EQUAL = "LESS_EQUAL";
	
	public enum Comparison {
		EQUAL,
		DIFFERENT,
		GREATER,
		GREATER_EQUAL,
		LESS,
		LESS_EQUAL;
	}

	private Comparison comparison;

	private ValueComparison(Comparison comparison, Class<T> valueClass) {
		super(comparison.toString(), valueClass, valueClass);
		this.comparison = comparison;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<? super T>> ValueComparison<T> instance(Comparison comparison, Class<?> valueClass) {
		if (!Comparable.class.isAssignableFrom(valueClass)) { // valueClass not Comparable
			return null;
		}
		return new ValueComparison<T>(comparison, (Class<T>) valueClass);
	}

	@Override
	public Boolean compare(T value1, T value2) {
		int comp = value1.compareTo(value2);
		
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
