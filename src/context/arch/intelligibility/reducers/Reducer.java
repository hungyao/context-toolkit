package context.arch.intelligibility.reducers;

import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.expression.DNF;

public abstract class Reducer {
	
	/**
	 * Call this method to apply the heuristic to reduce the explanation's expression.
	 * @param original
	 * @return
	 */
	public abstract DNF apply(DNF original);
	
	/**
	 * Convenience method, so that unwrapping Explanation is not needed.
	 * @param explanation
	 * @return
	 */
	public Explanation apply(Explanation explanation) {
		DNF content = explanation.getContent();
		content = apply(content);
		return new Explanation(explanation.getQuery(), content);
	}

}
