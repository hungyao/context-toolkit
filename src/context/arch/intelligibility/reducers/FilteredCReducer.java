package context.arch.intelligibility.reducers;

import java.util.ArrayList;
import java.util.List;

import context.arch.intelligibility.expression.Parameter;
import context.arch.intelligibility.expression.Reason;
import context.arch.intelligibility.reducers.ConjunctionReducer;
import context.arch.storage.Attributes;


/**
 * Conjunction reducer to reduce expressions to a pre-selected list of attribute names.
 * It assumes a conjunction of Parameters (not Values); 
 * child Expressions that are not Parameters (or subclasses of them) are ignored.
 * @author Brian Y. Lim
 *
 */
public class FilteredCReducer extends ConjunctionReducer {
	
	protected List<String> names;
	
	/**
	 * @param attributes from which to extract names
	 */
	public FilteredCReducer(Attributes attributes) {
		names = new ArrayList<String>();
		for (String name : attributes.keySet()) {
			names.add(name);
		}
	}
	
	/**
	 * @param attributeNames
	 */
	public FilteredCReducer(String ... attributeNames) {
		names = new ArrayList<String>();
		for (String name : attributeNames) {
			names.add(name);
		}
	}
	
	/**
	 * @param attributeNames
	 */
	public FilteredCReducer(List<String> attributeNames) {
		this.names = attributeNames;
	}

	@Override
	public Reason apply(Reason original) {
		Reason reduced = new Reason();
		
		for (Parameter<?> param : original) {
			if (names.contains(param.getName())) {
				reduced.add(param);
			}
		}
		
		return reduced;
	}

}
