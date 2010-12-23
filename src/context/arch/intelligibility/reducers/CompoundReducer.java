package context.arch.intelligibility.reducers;

import java.util.ArrayList;
import java.util.List;

import context.arch.intelligibility.expression.DNF;


/**
 * A convenience class that takes multiple Reducers and applies them consecutively.
 * @author Brian Y. Lim
 *
 */
public class CompoundReducer extends Reducer {
	
	protected List<Reducer> reducers = new ArrayList<Reducer>();
	
	public CompoundReducer() {}
	
	public void addReducer(Reducer reducer) {
		reducers.add(reducer);
	}
	
	/**
	 * Applies reducers iteratively in the order they were added.
	 * @param original
	 * @return
	 */
	@Override
	public DNF apply(DNF original) {
		DNF reduced = original;
		for (Reducer reducer : reducers) {
			reduced = reducer.apply(reduced);
		}
		return reduced;
	}

}
