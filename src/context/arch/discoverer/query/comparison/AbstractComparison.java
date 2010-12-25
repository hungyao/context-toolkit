/*
 * AbstractComparison.java
 *
 * Created on July 5, 2001, 11:16 AM
 */

package context.arch.discoverer.query.comparison;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;

/**
 * Abstract class to represent a comparison to the discovery subscription and query system in the context toolkit.
 * Comparisons could be simple like an equality test or a distance calculation.
 * Comparisons may even be on objects of different classes, specified by the C1 and C2 type parameters.
 * 
 * @param <C1> class of the first comparison entity
 * @param <C2> class of the second comparison entity
 * 
 * @author Agathe
 * @author Brian Y. Lim
 */
public abstract class AbstractComparison<C1, C2> {

	public static final String ABSTRACT_COMPARISON = "abComp";
	
	/** Tag for the C1 generics class parameter of AbstractComparison */
	public static final String ABSTRACT_COMPARISON_C1 = "abCompC1";
	/** Tag for the C2 generics class parameter of AbstractComparison */
	public static final String ABSTRACT_COMPARISON_C2 = "abCompC2";

	/*
	 * c1 and c2 are a hack to be able to retain knowledge of which class C1 and C2 are at runtime, after generics erasure.
	 */
	private Class<C1> c1;
	private Class<C2> c2;
	public Class<?> getC1() { return c1; }
	public Class<?> getC2() { return c2; }

	/**
	 * The comparison type
	 */
	private String comparisonName;

	/** 
	 * Creates new AbstractComparison 
	 */
	public AbstractComparison(String comparisonName,
			Class<C1> c1, Class<C2> c2) {
		this.comparisonName = comparisonName;
		this.c1 = c1;
		this.c2 = c2;
	}

	/**
	 * Returns the comparison name
	 *
	 * return String
	 */
	public String getComparisonName() {
		return comparisonName;
	}

	/**
	 * Compares 2 objects
	 *
	 * @param o1 The first object
	 * @param o2 The second object
	 * @return boolean The result of the comparison
	 */
	public abstract Boolean compare(C1 o1, C2 o2);

	public String toString(){
		return getComparisonName();
	}

	public DataObject toDataObject() {
		DataObjects v = new DataObjects();
		// add parameter class types
		v.add(new DataObject(ABSTRACT_COMPARISON_C1, getC1().getName()));
		v.add(new DataObject(ABSTRACT_COMPARISON_C2, getC2().getName()));
		
		DataObject data = new DataObject(
				ABSTRACT_COMPARISON, 
				getComparisonName(), 
				v);
		return data;
	}

	@SuppressWarnings("unchecked")
	public static <C1,C2> AbstractComparison<C1,C2> fromDataObject(DataObject data) {
		String c1 = data.getDataObject(ABSTRACT_COMPARISON_C1).getValue();
		String c2 = data.getDataObject(ABSTRACT_COMPARISON_C2).getValue();
		
		try {
			return (AbstractComparison<C1, C2>) 
			AbstractComparison.fromDataObject(data,
					// use Reflection to get classes
					(Class<C1>) Class.forName(c1),
					(Class<C2>) Class.forName(c2));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <C1,C2> AbstractComparison<C1,C2> fromDataObject(DataObject data, Class<C1> c1, Class<C2> c2) {
		String name = data.getDataObject(AbstractComparison.ABSTRACT_COMPARISON).getValue();
		
		/*
		 * Iterate through possibilities of ValueComparison
		 */
		for (ValueComparison.Comparison comparison : ValueComparison.Comparison.values()) {
			if (name.equals(comparison.toString())) {
				return (AbstractComparison<C1, C2>) ValueComparison.instance(comparison, c1);
			}
		}

		/*
		 * Iterate through possibilities of AttributeComparison
		 */
		for (AttributeComparison.Comparison comparison : AttributeComparison.Comparison.values()) {
			if (name.equals(comparison.toString())) {
				return (AbstractComparison<C1, C2>) new AttributeComparison(comparison);
			}
		}

		/*
		 * some other comparison type not defined by the existing library
		 */
		// use reflections to initialize
		// TODO: this seems brittle
		try {
			AbstractComparison<C1,C2> instance = 
				(AbstractComparison<C1,C2>) Class.forName(name)
											     .getConstructor(c1,c2)
											     .newInstance();
			return instance;			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		/*
		 * As long as custom queries are checked as rule queries (vs. subscription queries)
		 * They do not seem to need to be initiated here 
		 */

//		else if (name.startsWith(WithinThresholdComparison.WITHIN_THRESHOLD)){
//			String thresholdStr = name.substring(name.lastIndexOf('_')+1); // format e.g.: WithinThreshold_class java.lang.Double_0.0018
//			Number threshold = Double.parseDouble(thresholdStr);
//			return new WithinThresholdComparison(threshold);
//		}
//		else if (name.startsWith(NotWithinThresholdComparison.NOT_WITHIN_THRESHOLD)){
//			String thresholdStr = name.substring(name.lastIndexOf('_')+1); // format e.g.: NotWithinThreshold_class java.lang.Double_0.0018
//			Number threshold = Double.parseDouble(thresholdStr);
//			return new NotWithinThresholdComparison(threshold);
//		}
	}

}
