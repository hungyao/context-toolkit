package context.arch.intelligibility.expression;

import java.io.Serializable;

/**
 * To represent an explanation expression that can be structured into a tree structure, that can be reformed into Disjunctive Normal Form.
 * Serializable, so that it can be sent in communication packets
 * @author Brian Y. Lim
 * @see Disjunction#toDNF(Expression)
 *
 */
public interface Expression extends Serializable {
	
	public String toString();
	
	/**
	 * Checks whether the other Expression satisfies this expression.
	 * @param other should generally be "smaller" (i.e. contains less information) than this.
	 * @return
	 */
	public boolean isSatisfiedBy(Expression other);

}
