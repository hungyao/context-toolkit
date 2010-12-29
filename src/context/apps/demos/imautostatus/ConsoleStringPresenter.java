package context.apps.demos.imautostatus;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import context.arch.enactor.Enactor;
import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.expression.Expression;
import context.arch.intelligibility.expression.Parameter;
import context.arch.intelligibility.presenters.StringPresenter;
import context.arch.intelligibility.query.AltQuery;
import context.arch.intelligibility.query.Query;
import context.arch.intelligibility.reducers.ConjunctionReducer;
import context.arch.intelligibility.reducers.DisjunctionReducer;
import context.arch.intelligibility.reducers.FilteredCReducer;
import context.arch.intelligibility.reducers.ShortestDReducer;

public class ConsoleStringPresenter extends StringPresenter {
	
	public static final List<String> inputsDesc = new ArrayList<String>();
	static {
		// exclude less intelligible features (those with log), and include up to 5
		inputsDesc.add("UserInputCountFeature(120)=#");
		inputsDesc.add("timeSinceLastOMsg=#");
		inputsDesc.add("Focus={'out of focus','in focus'}");
		inputsDesc.add("KBCountFeature(30)=#");
		inputsDesc.add("KBCountFeature(60)=#");
	};
	
	public static final List<String> inputs = new ArrayList<String>();
	static {
		// exclude less intelligible features (those with log), and include up to 5
		inputs.add("UserInputCountFeature(120)");
		inputs.add("timeSinceLastOMsg");
		inputs.add("Focus");
		inputs.add("KBCountFeature(30)");
		inputs.add("KBCountFeature(60)");
	};

	protected ConjunctionReducer creducer = new FilteredCReducer(inputs); // filter to only meaningful features
	protected DisjunctionReducer dreducer = new ShortestDReducer(); // choose shortest of disjunction, because we use Occam's razor that the reason is minimal
	
	public ConsoleStringPresenter(Enactor enactor) {
		super(enactor);
	}

	@Override
	public String render(Explanation explanation) {
		explanation = creducer.apply(explanation);
		
		Query query = explanation.getQuery();
		String question = query.getQuestion();
		Expression expression = explanation.getContent();

		if (question.equals(Query.QUESTION_CERTAINTY)) {
			return renderCertainty(expression);
		}
		else if (question.equals(AltQuery.QUESTION_WHY_NOT)) {
			explanation = dreducer.apply(explanation);
			if (expression == null) { // probably because asking Why Not the current value
				return "Invalid argument";
			}
		}
		else if (question.equals(AltQuery.QUESTION_HOW_TO)) {
			explanation = dreducer.apply(explanation);
		}
				
		return '\n' + super.render(explanation);
	}
	
	protected DecimalFormat nf = (DecimalFormat)DecimalFormat.getInstance();
	
	/**
	 * Convert to percentage
	 */
	@SuppressWarnings("unchecked")
	protected String renderCertainty(Expression expression) {
		double certainty = ((Parameter<Double>)expression).getValue();
		nf.applyPattern("#0.0");
		return nf.format(certainty * 100) + "%";
	}

}
