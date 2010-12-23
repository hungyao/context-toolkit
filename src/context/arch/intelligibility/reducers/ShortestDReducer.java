package context.arch.intelligibility.reducers;

import context.arch.intelligibility.expression.DNF;
import context.arch.intelligibility.expression.Reason;


public class ShortestDReducer extends DisjunctionReducer {

	@Override
	public DNF apply(DNF original) {		
		int minIndex = 0;
		int minNumDiff = Integer.MAX_VALUE;
		for (int i = 0; i < original.size(); i++) {
			Reason trace = original.get(i);
			int numDiff = trace.size();
			
			if (numDiff < minNumDiff) { // pick fewest differences
				minIndex = i;
				minNumDiff = numDiff;
			}
			else if (numDiff == minNumDiff && // if same # of differences
					original.get(i).size() < original.get(minIndex).size()) { // then pick from shortest trace
				minIndex = i;
			}
		}
		return new DNF(original.get(minIndex));
	}

}
