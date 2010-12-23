package context.arch.intelligibility.reducers;

import context.arch.intelligibility.expression.DNF;
import context.arch.intelligibility.expression.Reason;

public abstract class ConjunctionReducer extends Reducer {
	
	public abstract Reason apply(Reason original);
	
	/**
	 * Applys the reduction to all Conjunctions within the Disjunction.
	 * @param original needs to be in DNF form.
	 * @return
	 */
	public DNF apply(DNF original) {
		DNF reduced = new DNF();
		
		for (Reason reason : original) {
			Reason conjReduced = apply(reason);
			if (!conjReduced.isEmpty()) {
				reduced.add(conjReduced);
			}
		}
		
		return reduced;
	}

}
