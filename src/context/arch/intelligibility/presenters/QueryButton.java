package context.arch.intelligibility.presenters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import context.arch.discoverer.ComponentDescription;
import context.arch.enactor.Enactor;
import context.arch.intelligibility.Explainer;
import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.query.AltQuery;
import context.arch.intelligibility.query.Query;
import context.arch.intelligibility.query.QueryListener;
import context.arch.intelligibility.query.WhatIfQuery;

public class QueryButton extends JButton implements ActionListener {
	
	private static final long serialVersionUID = 857616575202708153L;

	protected JPopupMenu popup;
	private int x, y;
	protected String context;
	protected String value;
	
	protected Enactor enactor;
	
	protected ComponentDescription widgetState;
	
	protected Map<String, Icon> contextIcons;
	
	public QueryButton(String label,
			ComponentDescription widgetState, Enactor enactor) {
		this(label,
				new HashMap<String, Icon>(), // empty icon map 
				widgetState, enactor);
	}
	
	public QueryButton(String label,
			Map<String, Icon> contextIcons, // to map icons, if no icons, then just supply an empty map
			ComponentDescription widgetState, Enactor enactor) {
		super(label);

		this.contextIcons = contextIcons;

		this.widgetState = widgetState;
		this.enactor = enactor;		
		
		this.context = enactor.getOutcomeName();
		this.value = enactor.getOutcomeValue();
		
		this.addActionListener(this); // listen to button press

		this.popup = createPopupMenu();
		this.x = 0;
		this.y = (int)this.getPreferredSize().getHeight();
	}
	
	public void setWidgetState(ComponentDescription widgetState) {
		this.widgetState = widgetState;
		this.popup = createPopupMenu(); // re-create
	}
	
	protected JPopupMenu createPopupMenu() {
		JPopupMenu popup = new JPopupMenu();
		JMenu menu;
		
		/*
		 * Preload outcome values for use in some queries
		 */
		List<String> outputValues = getOutputs();

		// What
		attachMenuItem(Query.QUESTION_WHAT, popup, this);
		// Certainty
		attachMenuItem(Query.QUESTION_CERTAINTY, popup, this);
		// Inputs
		attachMenuItem(Query.QUESTION_INPUTS + " Values", Query.QUESTION_INPUTS, popup, this);
		
		/* ------------------------------------------------- */
		popup.add(new JSeparator());		

		// When
		attachMenuItem(Query.QUESTION_WHEN, popup, this);
		// When Last
		menu = new JMenu(AltQuery.QUESTION_WHEN_LAST);
		popup.add(menu);
		for (String outputValue : outputValues) {
			attachMenuItem(outputValue, outputValue, AltQuery.QUESTION_WHEN_LAST, contextIcons.get(outputValue), menu, this);
		}				
		// What At Time
		attachMenuItem(Query.QUESTION_WHAT_AT_TIME + "...", Query.QUESTION_WHAT_AT_TIME, popup, this);
		
		/* ------------------------------------------------- */
		popup.add(new JSeparator());		

		// Why
		attachMenuItem(Query.QUESTION_WHY, popup, this);
		// Why Not
		menu = new JMenu(AltQuery.QUESTION_WHY_NOT);
		popup.add(menu);
		for (String outputValue : outputValues) {
			if (outputValue.equals(value)) { continue; } // skip if matches current value
			attachMenuItem(outputValue, outputValue, AltQuery.QUESTION_WHY_NOT, contextIcons.get(outputValue), menu, this);
		}
		// How To
		menu = new JMenu("How Does"); // AltQuery.QUESTION_HOW_TO
		popup.add(menu);
		for (String outputValue : outputValues) {
			attachMenuItem(outputValue, outputValue, AltQuery.QUESTION_HOW_TO, contextIcons.get(outputValue), menu, this);
		}
		// What If
		attachMenuItem(WhatIfQuery.QUESTION_WHAT_IF + "...", WhatIfQuery.QUESTION_WHAT_IF, popup, this);
		
		/* ------------------------------------------------- */
		popup.add(new JSeparator());		

		// Definition
		attachMenuItem(Query.QUESTION_DEFINITION, popup, this);
		// Rationale
		attachMenuItem(Query.QUESTION_RATIONALE, popup, this);
		// Outputs
		attachMenuItem("Possible " + Query.QUESTION_OUTPUTS, Query.QUESTION_OUTPUTS, popup, this);		
		
		return popup;
	}

	/**
	 * To help preload outcome values for use in some queries
	 */
	protected List<String> getOutputs() {
		Query query = new Query(Query.QUESTION_OUTPUTS, context, System.currentTimeMillis());
		Explanation explanation = enactor.getExplainer().getExplanation(query);
		List<String> outputValues = Explainer.outputsToLabels(explanation.getContent());	
		return outputValues;
	}

	public static JMenuItem attachMenuItem(String actionCommand, JComponent menu, ActionListener listener) {
		return attachMenuItem(actionCommand, actionCommand, actionCommand, menu, listener);
	}
	public static JMenuItem attachMenuItem(String label, String actionCommand, JComponent menu, ActionListener listener) {
		return attachMenuItem(label, actionCommand, actionCommand, menu, listener);
	}
	public static JMenuItem attachMenuItem(String label, String name, String actionCommand, JComponent menu, ActionListener listener) {
		return attachMenuItem(label, name, actionCommand, null, menu, listener);
	}
	public static JMenuItem attachMenuItem(String label, String name, String actionCommand, Icon icon, JComponent menu, ActionListener listener) {
		JMenuItem mi = new JMenuItem(label, icon); 
		mi.setName(name); 
		mi.setActionCommand(actionCommand); 
		menu.add(mi); 		
		mi.addActionListener(listener);		
		
		String tooltipText = getQuestionDefinition(actionCommand); // may be null if actionCommand is not a question
		mi.setToolTipText(tooltipText);
		
		return mi;
	}
	
	private static final Map<String, String> questionDefinitions = new HashMap<String, String>(); // <question, definition>
	static {
		questionDefinitions.put(Query.QUESTION_WHAT, "The output value of the context.");
		questionDefinitions.put(Query.QUESTION_CERTAINTY, "The uncertainty regarding the context value.");
		questionDefinitions.put(Query.QUESTION_INPUTS, "The inputs (and their values) that La\u03BAsa used to determine the context value.");
		
		questionDefinitions.put(Query.QUESTION_WHEN, "When the context was changed to the value shown.");
		questionDefinitions.put(AltQuery.QUESTION_WHEN_LAST, "When the context last had the value chosen.");
		questionDefinitions.put(Query.QUESTION_WHAT_AT_TIME, "The output value of the context at another chosen time.");
		
		questionDefinitions.put(Query.QUESTION_WHY, "The reason why La\u03BAsa inferred the context value.");
		questionDefinitions.put(AltQuery.QUESTION_WHY_NOT, "The reason why La\u03BAsa did not infer a particular context value.");
		questionDefinitions.put(AltQuery.QUESTION_HOW_TO, "A facility for you to find a way to get La\u03BAsa to achive a certain context value.");
		questionDefinitions.put(WhatIfQuery.QUESTION_WHAT_IF, "A facility for you to change input values to see how the context value would change.");

		questionDefinitions.put(Query.QUESTION_DEFINITION, "The meaning or definition of the context term.");
		questionDefinitions.put(Query.QUESTION_RATIONALE, "The rationale for considering this context.");
		questionDefinitions.put(Query.QUESTION_OUTPUTS, "The possible output values that this context can have.");
	}
	
	private static String getQuestionDefinition(String question) {
		// TODO: offload to an external file or Google Docs
		return questionDefinitions.get(question); 
	}

	/**
	 * To listen to when menu items are selected.
	 * It identifies the menu item and generates the appropriate query.	
	 */
	@Override
	public void actionPerformed(ActionEvent evt) {
		Object src = evt.getSource();
		
		if (src == this) {
	        popup.show(this, x, y);
		}
		
		else if (src instanceof JMenuItem) {		
			String question = evt.getActionCommand();
//			System.out.println("QueryButton question = " + question);
	
			long timestamp = System.currentTimeMillis();
			
			/*
			 * Process query
			 */
			Query query;
			
			if (question.equals(Query.QUESTION_WHAT)) {
				query = new Query(question, context, timestamp);
			}
			
			else if (question.equals(Query.QUESTION_WHAT_AT_TIME)) {
//				long time = TimePickerDialog.retrievePickedTimestamp(timestamp); // time being questioned about
//				System.out.println("QuestionButton QUESTION_WHAT_AT_TIME time = " + time);
//				// TODO
//				
				query = new Query(question, context, timestamp);
			}
			
			else if (question.equals(AltQuery.QUESTION_WHY_NOT) ||
					 question.equals(AltQuery.QUESTION_HOW_TO) || 
					 question.equals(AltQuery.QUESTION_WHEN_LAST)) {
				String altValue = ((JComponent)evt.getSource()).getName();
				query = new AltQuery(question, context, altValue, timestamp);
			}
			
			else { // unknown
				query = new Query(question, context, timestamp);
			}
			
			notifyListeners(query);
		}
	}
	
	/* -----------------------------------------------------------
	 * Query Listening code
	 * ----------------------------------------------------------- */
	
	protected List<QueryListener> listeners = new ArrayList<QueryListener>();
	
	public void addQueryListener(QueryListener listener) {
		listeners.add(listener);
	}
	
	protected void notifyListeners(Query query) {
		for (QueryListener listener : listeners) {
			listener.queryInvoked(query);
		}
	}

}
