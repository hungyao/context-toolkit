package context.arch.intelligibility.expression;

import context.arch.intelligibility.expression.Comparison.Relation;

/**
 * Expression representing the boolean NOT operation on an expression.
 * @author Brian Y. Lim
 *
 */
public class Negation implements Expression {
	
	private static final long serialVersionUID = 1296481013575235612L;
	
	/**
	 * Child expression of the negation.
	 */
	protected Expression expression;
	
	/**
	 * Made private, since the more flexible {@link #negate(Expression)} should be used instead,
	 * especially if the expression argument is already a negation
	 * @param expression the expression to negate
	 */
	private Negation(Expression expression) {
		this.expression = expression;
	}
	
	/**
	 * Get the child expression of this negation
	 * @return
	 */
	public Expression getExpression() {
		return expression;
	}
	
	@Override
	public String toString() {
		return "NOT[" + expression + "]";
	}
	
	/**
	 * Negates an expression by wrapping in a Negation expression. 
	 * If the original expression was already negated, then it unwraps it.
	 * @param expression
	 * @return
	 */
	public static Expression negate(Expression expression) {
		if (expression instanceof Negation) {
			// double negation, so neutralize
			return ((Negation)expression).getExpression();
		}
		
		return expression = new Negation(expression);
	}
	
	/**
	 * Flip relation through a negation.
	 * @param relation
	 * @return
	 */
	public static Relation negateRelation(Relation relation) {
		switch (relation) {
		case NO_RELATION: return Relation.NO_RELATION;
		case EQUALS: return Relation.NOT_EQUALS;
		case NOT_EQUALS: return Relation.EQUALS;
		case GREATER_THAN: return Relation.LESS_THAN_OR_EQUAL;
		case GREATER_THAN_OR_EQUAL: return Relation.LESS_THAN;
		case LESS_THAN: return Relation.GREATER_THAN_OR_EQUAL;
		case LESS_THAN_OR_EQUAL: return Relation.GREATER_THAN;
		default: return Relation.NO_RELATION;
		}
	}
	

	/**
	 * Recursively uses the de Morgan theorem to push NOTs to the leaves.
	 * Recurses all the way to the leaves and works backwards, i.e. depth-first processing
	 * @param expression
	 * @return
	 */
	public static Expression deMorgan(Expression expression) {
		if (expression instanceof Negation) {
			return deMorgan((Negation)expression); 
		}
		
		/*
		 * For AND and OR, unwrap, apply deMorgan to child expressions, then wrap back
		 */
		else if (expression instanceof Conjunction) {
			Conjunction<Expression> ret = new Conjunction<Expression>();
			Conjunction<?> conjunction = (Conjunction<?>)expression;
			for (Expression childExp : conjunction) {
				ret.add(deMorgan(childExp));
			}
			return ret;
		}
		else if (expression instanceof Disjunction) {
			Disjunction<Expression> ret = new Disjunction<Expression>();
			Disjunction<?> disjunction = (Disjunction<?>)expression;
			for (Expression childExp : disjunction) {
				ret.add(deMorgan(childExp));
			}
			return ret;
		}
		
		// just return original if any other expression form
		return expression;
	}

	/**
	 * Apply de Morgan's theorem: 
	 * <ul>
	 * <li>NOT(a AND b) -> NOT(a) OR NOT(b)</li>
	 * <li>NOT(a OR b) -> NOT(a) AND NOT(b)</li>
	 * </ul>
	 * Recursively does so till the NOT's are at the leaves
	 */
	protected static Expression deMorgan(Negation negated) {
		boolean recurse = true;
		Expression expression = negated.getExpression();
		
		if (expression instanceof Conjunction) {
			Conjunction<?> conjunction = (Conjunction<?>)expression;
			Disjunction<Expression> disjunction = new Disjunction<Expression>();
			for (Expression childExp : conjunction) {
				childExp = negate(childExp);
				if (recurse) { childExp = deMorgan(childExp); }
				disjunction.add(childExp);
			}
			return disjunction;
		}
		else if (expression instanceof Disjunction) { // similar as for Conjunction, but flips to Disjunction instead
			Disjunction<?> disjunction = (Disjunction<?>)expression;
			Conjunction<Expression> conjunction = new Conjunction<Expression>();
			for (Expression childExp : disjunction) {
				childExp = negate(childExp);
				if (recurse) { childExp = deMorgan(childExp); }
				conjunction.add(childExp);
			}
			return conjunction;
		}
		else { // assume terminal but not Value
			// combine negated child into a negated expression
			Parameter<?> negExpr = Negated.negate((Parameter<?>)negated.getExpression());			
			return negExpr;
		}
	}

	/**
	 * Would be the negation of isSatisfiedBy of the childExpression
	 */
	@Override
	public boolean isSatisfiedBy(Expression other) {
		return !expression.isSatisfiedBy(other);
	}

}
