package context.arch.intelligibility.presenters;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import context.arch.enactor.Enactor;
import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.expression.DNF;
import context.arch.intelligibility.expression.Expression;
import context.arch.intelligibility.expression.Parameter;
import context.arch.intelligibility.expression.Reason;
import context.arch.intelligibility.query.AltQuery;
import context.arch.intelligibility.query.Query;
import context.arch.intelligibility.query.WhatIfQuery;

/**
 * Extends TablePanelPresenter by considering explanation by question type.
 * @author Brian Y. Lim
 *
 */
public class TypePanelPresenter extends TablePanelPresenter {
	
	public TypePanelPresenter(Enactor enactor) {
		super(enactor);
	}
	
	@Override
	public JPanel render(Explanation explanation) {
		JComponent contentComponent = renderContent(explanation);
		
		panel.removeAll();
		panel.add(contentComponent);		
		panel.revalidate();
		
		return panel;
	}
	
	/**
	 * Can be extended by subclass to support more questions.
	 * @param question
	 * @param context
	 * @param expression
	 * @return
	 */
	protected JComponent renderContent(Explanation explanation) {
		query = explanation.getQuery();
		String question = query.getQuestion();
//		String context = query.getContext();		
		DNF expression = explanation.getContent();
		
		if (question == null) {
			return new JPanel(); // empty
		}

		if (question.equals(Query.QUESTION_DEFINITION)) {
			return renderDefinition(expression);
		}
		else if (question.equals(Query.QUESTION_INPUTS)) {
			return renderInputs(expression.get(0));
		}
		else if (question.equals(Query.QUESTION_OUTPUTS)) {
			return renderOutputs(expression);
		}
		else if (question.equals(Query.QUESTION_WHAT)) {
			return renderWhat(expression.getFirstLiteral());
		}
		else if (question.equals(Query.QUESTION_WHEN)) {
			return renderWhen(expression.getFirstLiteral());
		}
		else if (question.equals(AltQuery.QUESTION_WHEN_LAST)) {
			return renderWhenLast(expression);
		}
		else if (question.equals(Query.QUESTION_CERTAINTY)) {
			return renderCertainty(expression);
		}
		else if (question.equals(Query.QUESTION_WHY)) {
			return renderWhy(expression);
		}
		else if (question.equals(WhatIfQuery.QUESTION_WHAT_IF)) {
			return renderWhatIf(expression.getFirstLiteral());
		}
		else if (question.equals(AltQuery.QUESTION_WHY_NOT)) {
			String altValue = ((AltQuery)query).getAltOutcomeValue();
			return renderWhyNot(altValue, expression);
		}
		else if (question.equals(AltQuery.QUESTION_HOW_TO)) {
			String altValue = ((AltQuery)query).getAltOutcomeValue();
			return renderHowTo(altValue, expression);
		}
		else {
			return super.renderReasons(expression);
		}
	}
	
	/* ------------------------------------------------------
	 * Rendering for specific question types
	 * ------------------------------------------------------ */

	@SuppressWarnings("unchecked")
	protected JComponent renderDefinition(Expression expression) {
		String definition = ((Parameter<String>)expression).getValue();
		
		final JEditorPane definitionPane = new JEditorPane();
		definitionPane.setContentType("text/html");
		definitionPane.setEditable(false);
		definitionPane.setText(definition); 

		final JScrollPane scrollpane = new JScrollPane(definitionPane);
		
		definitionPane.addHyperlinkListener(new HyperlinkListener() {			
			@Override
			public void hyperlinkUpdate(HyperlinkEvent evt) {
				if (evt.getEventType() != HyperlinkEvent.EventType.ACTIVATED) { return; }
				URL url = evt.getURL();
				try {
					definitionPane.setPage(url);
					scrollpane.validate();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		definitionPane.setCaretPosition(0); // scroll to top
		
		return scrollpane;
	}
	
	protected JComponent renderInputs(Reason reason) {
		return renderReason(reason);
	}
	
	protected JComponent renderOutputs(DNF reasons) {
		return renderReasons(reasons);
	}
	
	protected JComponent renderWhat(Parameter<?> literal) {
		return renderTerminal(literal);
	}

	protected JComponent renderWhen(Parameter<?> literal) {
		return renderTerminal(literal);
	}

	protected JComponent renderWhenLast(Expression expression) {
		// TODO Auto-generated method stub
		return null;
	}

	protected JComponent renderCertainty(DNF expression) {	
		return renderReasons(expression);
	}

	protected JComponent renderWhy(DNF expression) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		JLabel label = new JLabel("Because");
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(label, BorderLayout.NORTH);
				
		JComponent c = renderReasons(expression);
		panel.add(c, BorderLayout.CENTER);
		
		return panel;
	}

	protected JComponent renderWhatIf(Parameter<?> literal) {
		return renderWhat(literal);
	}

	protected JComponent renderWhyNot(String altOutcomeValue, DNF expression) {		
		JPanel disjPanel = new JPanel();
		disjPanel.setLayout(new BorderLayout());
		
		// descriptive label
//		JLabel descLabel = new JLabel("<html>Because of any one of the following conditions was <font color=red>not</font> satisfied:</html>");
		JLabel descLabel = new JLabel("<html>Because:</html>");
		disjPanel.add(descLabel, BorderLayout.NORTH);
		
		JComponent c = renderReasons(expression);		
		disjPanel.add(c, BorderLayout.CENTER);
		
		return disjPanel;
	}

	protected JComponent renderHowTo(String altOutcomeValue, DNF expression) {	
		JPanel disjPanel = new JPanel();
		disjPanel.setLayout(new BorderLayout());
		
		// descriptive label
//		JLabel descLabel = new JLabel("<html>If:</html>");
//		disjPanel.add(descLabel, BorderLayout.NORTH);

		JComponent c = renderReasons(expression);	
		disjPanel.add(c, BorderLayout.CENTER);
		
		return disjPanel;
	}

}
