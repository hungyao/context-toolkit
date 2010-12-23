package context.arch.intelligibility.reducers;

import context.arch.intelligibility.expression.Reason;

/**
 * A conjunction reducer that simply reduces the length of a conjunction 
 * (single reason trace) to at most a pre-specified number of child expressions (conditionals).
 * @author Brian Y. Lim
 *
 */
public class TruncationCReducer extends ConjunctionReducer {
	
	protected int maxLength;
	
	/**
	 * @param maxLength maximum length to set reduced Conjunctions to
	 */
	public TruncationCReducer(int maxLength) {
		this.maxLength = maxLength;
	}

	@Override
	public Reason apply(Reason original) {
		Reason reduced = new Reason();
		
		// add from first up to maxLength
		for (int i = 0; i < maxLength; i++) {
			reduced.add(original.get(i));
		}
		
		return reduced;
	}

}
