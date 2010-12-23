package context.arch.intelligibility.presenters;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import context.arch.enactor.Enactor;
import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.expression.Comparison;
import context.arch.intelligibility.expression.DNF;
import context.arch.intelligibility.expression.Expression;
import context.arch.intelligibility.expression.Negated;
import context.arch.intelligibility.expression.Parameter;
import context.arch.intelligibility.expression.Reason;
import context.arch.intelligibility.query.Query;

/**
 * A basic {@link Presenter} that renders {@link Explanation} based on its content {@link DNF}.
 * If renders each explanation as a String. It only handles the content {@link Expression}
 * based on whether it is a DNF, {@link Reason} or {@link Parameter}.
 * @author Brian Y. Lim
 * 
 * @see Presenter
 * @see Explanation
 * @see Expression
 * @see DNF
 * @see Enactor
 */
public class StringPresenter extends Presenter<String> {

	/**
	 * Creates a StringPresenter associated to an enactor
	 * @param enactor to get its {@link Explainer} from.
	 */
	public StringPresenter(Enactor enactor) {
		super(enactor);
	}

	@Override
	public String render(Explanation explanation) {
		Query query = explanation.getQuery();
		String question = query.getQuestion();
		DNF expression = explanation.getContent();

		String text;
		
		if (question == null) {
			text = ""; // empty
		}
		
		text = renderDNF(expression);
		
		return text.trim();
	}

	/**
	 * Root method for rendering an explanation content.
	 * @param dnf from {@link Explanation#getContent()}
	 * @return the full string representation of the expression.
	 */
	protected String renderDNF(DNF dnf) {
		List<String> reasons = new ArrayList<String>();
		for (Reason reason : dnf) {
			reasons.add(renderReason(reason));
		}
		
		final String separator = System.getProperty("line.separator") + "or";
		return StringUtils.join(reasons, separator);
	}

	/**
	 * Render a single reason in the explanation content.
	 * @param conjunction from a {@link DNF}
	 * @return the string representation of this single reason
	 */
	protected String renderReason(Reason conjunction) {
		String s = "";
		int i = 0;
		for (Parameter<?> exp : conjunction) {
			s += System.getProperty("line.separator") + ++i + ") " + renderLiteral(exp);
		}
		return s;
	}

	/**
	 * Render a single literal in a {@link Reason}.
	 * @param literal from a Reason.
	 * @return the string representation of this single literal.
	 */
	protected String renderLiteral(Parameter<?> literal) {
		if (literal instanceof Comparison<?>) {
			Comparison<?> c = (Comparison<?>)literal;
			return c.toPrettyString(descExplainer);
		}
		else if (literal instanceof Negated<?>) {
			return "not " + ((Negated<?>)literal).getChildExpression().toPrettyString(descExplainer);
		}
		else if (literal instanceof Parameter<?>) {
			Parameter<?> p = (Parameter<?>)literal;
//			String context = p.getName();
//			if (p.getValue() == null) { return descExplainer.getPrettyName(context); }
//			return descExplainer.getPrettyName(context) + 
//				" is " + 
//				descExplainer.getPrettyValue(context, p.getValue().toString()) +
//				descExplainer.getUnit(context);
			return p.toPrettyString(descExplainer);
		}
				
		return literal.toString();
	}

}
