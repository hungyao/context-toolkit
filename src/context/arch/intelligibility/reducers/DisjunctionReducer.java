package context.arch.intelligibility.reducers;

import context.arch.intelligibility.expression.DNF;

public abstract class DisjunctionReducer extends Reducer {
	
	public abstract DNF apply(DNF original);

}
