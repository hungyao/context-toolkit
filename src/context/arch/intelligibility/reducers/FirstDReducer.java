package context.arch.intelligibility.reducers;

import context.arch.intelligibility.expression.DNF;

public class FirstDReducer extends DisjunctionReducer {

	@Override
	public DNF apply(DNF original) {
		if (original != null && !original.isEmpty()) { 
			return new DNF(original.get(0)); 
		}
		else { 
			return null; 
		}
	}

}
