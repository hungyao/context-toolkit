package context.arch.intelligibility.presenters;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import context.arch.comm.DataObject;
import context.arch.enactor.Enactor;
import context.arch.enactor.EnactorComponentInfo;
import context.arch.enactor.EnactorListener;
import context.arch.enactor.EnactorParameter;
import context.arch.intelligibility.DescriptiveExplainerDelegate;
import context.arch.intelligibility.query.AltQuery;
import context.arch.intelligibility.query.Query;
import context.arch.intelligibility.query.QueryListener;
import context.arch.intelligibility.reducers.ConjunctionReducer;
import context.arch.storage.Attributes;

/**
 * <p>
 * Utility class to obtain {@link Query} from a Swing user interface (JPanel).
 * It allows the user to ask several questions of an output context:
 * <ul>
 * <li>Q_WHAT</li>
 * <li>Q_WHEN</li>
 * <li>Q_WHY</li>
 * <li>Q_WHY_NOT</li>
 * <li>Q_HOW_TO</li>
 * <li>Q_OUTPUTS</li>
 * <li>Q_INPUTS</li>
 * <li>Q_WHAT_IF</li>
 * </ul>
 * </p>
 * <p>
 * These questions are selected via a combo box. 
 * The Why not and How To questions provide an extra combo box to choose the
 * alternative outcome value to ask about. The What If question provides a
 * panel to manipulate input values to ask about.
 * </p>
 * <p>
 * Once the question is selected, this notifies its {@link QueryListener}s. 
 * Explanations are not generated here, but the listeners can take the passed
 * Query to generate {@link Explanation}s using an {@link Explainer}.
 * </p>
 * <p>
 * This class can be used as a reference implementation for how to create
 * Queries in an application.
 * </p>
 * @author Brian Y. Lim
 * 
 * @see QueryListener
 * @see Query
 * @see AltQuery
 * @see WhatIfQuery
 */
public class QueryPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 7157745347576384770L;

	public int Q_WHAT = 0;
	public int Q_WHEN = 1;
	public int Q_WHY = 3;
	public int Q_WHY_NOT = 4;
	public int Q_HOW_TO = 5;
	public int Q_OUTPUTS = 7;
	public int Q_INPUTS = 8;
	public int Q_WHAT_IF = 9;
	
	protected static String[] questions = {
		"What", "When", 
		ComboBoxRenderer.SEPARATOR_MARKER, 
		"Why", "<html>Why isn't...</html>", "When would...", 
		ComboBoxRenderer.SEPARATOR_MARKER,
		"What else", "What details", "What if..."
	};
	
	protected Vector<String> howToValues;
	protected Vector<String> whyNotValues = new Vector<String>();
	private String value;

	protected String context;
	protected String contextPretty;

	private JLabel label1;
	private JLabel label2;

	private JComboBox questionCombo;
	private JComboBox whyNotValuesCombo;
	private JComboBox howToValuesCombo;

	private Enactor enactor;

	private WhatIfPanel whatIfPanel;

	private JPanel wrapper;

	private Query query;
	
	protected DescriptiveExplainerDelegate descExplainer;
	
	/**
	 * This should be called only after the enactor is properly started,
	 * or else there may be some corruption in the layout.
	 * @param enactor to associate questions with
	 * @param whatifReducer to reduce the inputs list for the What If UI.
	 * @param autoUpdate if true, then it will refresh its display whenever it detects a change in the enactor
	 */
	public QueryPanel(Enactor enactor, ConjunctionReducer whatifReducer, boolean autoUpdate) {
		this.enactor = enactor;
		
		descExplainer = enactor.getExplainer().getDescriptionExplainer();
		this.context = enactor.getOutcomeName();
		this.contextPretty = descExplainer.getPrettyName(enactor.getOutcomeName());
		
		/*
		 * Layout
		 */
		
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEtchedBorder());
		
		wrapper = new JPanel();
		wrapper.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.add(wrapper, BorderLayout.NORTH);
		
		whatIfPanel = new WhatIfPanel(
				context,
				whatifReducer,
				descExplainer, 
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						notifyListeners(getWhatIfQuery(System.currentTimeMillis()));
					}
				});
		this.add(whatIfPanel, BorderLayout.CENTER);
		
		update();
		
		if (autoUpdate) {
			enactor.addListener(new EnactorListener() {
				
				@Override public void serviceExecuted(EnactorComponentInfo eci, String serviceName, String functionName, Attributes input, DataObject returnDataObject) {}				
				@Override public void parameterValueChanged(EnactorParameter parameter, Attributes validAtts, Object value) {}				
				@Override public void componentRemoved(EnactorComponentInfo eci, Attributes paramAtts) {}
				@Override public void componentAdded(EnactorComponentInfo eci, Attributes paramAtts) {}
				
				@Override
				public void componentEvaluated(EnactorComponentInfo eci) {
					update();
				}
				
			});
		}
	}
	
	/**
	 * This should be called only after the enactor is properly started,
	 * or else there may be some corruption in the layout.
	 * @param enactor to associate questions with
	 * @param autoUpdate if true, then it will refresh its display whenever it detects a change in the enactor
	 */
	public QueryPanel(Enactor enactor, boolean autoUpdate) {
		this(enactor, null, autoUpdate);
	}

	/**
	 * This should be called only after the enactor is properly started,
	 * or else there may be some corruption in the layout.
	 * This does not auto-update.
	 * @param enactor to associate questions with
	 */
	public QueryPanel(Enactor enactor) {
		this(enactor, null);
	}

	/**
	 * This should be called only after the enactor is properly started,
	 * or else there may be some corruption in the layout.
	 * @param enactor to associate questions with
	 * @param whatifReducer to reduce the inputs list for the What If UI.
	 */
	public QueryPanel(Enactor enactor, ConjunctionReducer whatifReducer) {
		this(enactor, whatifReducer, false);
	}
	
	/**
	 * If the panel was not set to auto-update, then this needs to be manually
	 * called whenever the enactor state changes.
	 */
	public void update() {
		/*
		 * Reset
		 */
		wrapper.removeAll();
		
		/*
		 * Extract output values
		 */		
		value = enactor.getOutcomeValue();
		
		howToValues = new Vector<String>(enactor.getOutcomeValues());
		howToValues.add(0, "[select]"); // add empty as first
		
		whyNotValues.clear();
		for (String val : howToValues) {
			if (!val.equals(value)) {
				whyNotValues.add(val);
			}
		}
		
		/*
		 * Components
		 */
		
		questionCombo = new JComboBox(questions);
		questionCombo.setRenderer(new ComboBoxRenderer());
		questionCombo.setMaximumRowCount(10); // so that we don't have to scroll
		questionCombo.addActionListener(this);
		wrapper.add(questionCombo);
		
		label1 = new JLabel();
		wrapper.add(label1);
		
		whyNotValuesCombo = new JComboBox(whyNotValues);
		whyNotValuesCombo.setRenderer(new ContextIcons.IconListCellRenderer());
		whyNotValuesCombo.setVisible(false);
		whyNotValuesCombo.addActionListener(this);
		wrapper.add(whyNotValuesCombo);
		
		howToValuesCombo = new JComboBox(howToValues);
		howToValuesCombo.setRenderer(new ContextIcons.IconListCellRenderer());
		howToValuesCombo.setVisible(false);
		howToValuesCombo.addActionListener(this);
		wrapper.add(howToValuesCombo);
		
		label2 = new JLabel("?");
		wrapper.add(label2);
		
		whatIfPanel.setInputs(enactor.getExplainer().getInputsExplanation());

		questionCombo.setSelectedIndex(0); // to invoke proper text rendering
		
		//repaint();
		invalidate();
	}
	
	/**
	 * Renderer for a JComboBox to add separators, if it detects a child as SEPARATOR_MARKER.
	 * @author Brian Y. Lim\
	 */
	protected class ComboBoxRenderer extends DefaultListCellRenderer {
		
		private static final long serialVersionUID = -8082661052540808631L;
		
		public static final String SEPARATOR_MARKER = "---";
		public final JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if (value != null && value.equals(SEPARATOR_MARKER)) {
				return separator;
			}
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
	}

	/**
	 * To listen to actions performed on the combo boxes.
	 */
	@Override
	public void actionPerformed(ActionEvent evt) {
		
		Object src = evt.getSource();
		
		if (src == questionCombo) {
			int selectedIndex = questionCombo.getSelectedIndex();
			questionSelected(selectedIndex);
		}

		else if (src == whyNotValuesCombo) {
			questionSelected(Q_WHY_NOT);
		}
		else if (src == howToValuesCombo) {
			questionSelected(Q_HOW_TO);
		}
		
	}	
	
	/**
	 * Called when a question is selected.
	 * @param selectedIndex the index of the question (Q_WHAT, Q_WHY, etc)
	 */
	protected void questionSelected(int selectedIndex) {
		/*
		 * Change layout according to question type.
		 * Execute query if ready
		 */

		// reset
		label1.setText("");
		whyNotValuesCombo.setVisible(false);
		howToValuesCombo.setVisible(false);	
		whatIfPanel.setVisible(false);	

		long timestamp = System.currentTimeMillis();
		
		if (selectedIndex == Q_WHAT) {
			label1.setText("is the " + contextPretty);
			
			query = new Query(Query.QUESTION_WHAT, context, timestamp);
		}
		
		else if (selectedIndex == Q_WHEN) {
			label1.setText("<html>did " + contextPretty + " become <i>" + value + "</i></html>");
			
			query = new Query(Query.QUESTION_WHEN, context, timestamp);
		}
		
		else if (selectedIndex == Q_WHY) {
			label1.setText("<html>is " + contextPretty + " <i>" + value + "</i></html>");
			
			query = new Query(Query.QUESTION_WHY, context, timestamp);
		}
		
		else if (selectedIndex == Q_WHY_NOT) {
			label1.setText(contextPretty);
			whyNotValuesCombo.setVisible(true);
			
			String altValue = whyNotValuesCombo.getSelectedItem().toString();
			if (!altValue.equals("[select]")) {
				query = new AltQuery(AltQuery.QUESTION_WHY_NOT, context, altValue, timestamp);
			}
			else {
//				query = new Query(Query.QUESTION_NONE, context, timestamp);
				query = new Query(null, context, timestamp);
			}
		}
		
		else if (selectedIndex == Q_HOW_TO) {
			label1.setText(contextPretty + " be");
			howToValuesCombo.setVisible(true);
			
			String altValue = howToValuesCombo.getSelectedItem().toString();
			if (!altValue.equals("[select]")) {
				query = new AltQuery(AltQuery.QUESTION_HOW_TO, context, altValue, timestamp);
			}
			else {
				query = new Query(null, context, timestamp);
			}
		}
		
		else if (selectedIndex == Q_OUTPUTS) {
			label1.setText("can " + contextPretty + " be");
			
			query = new Query(Query.QUESTION_OUTPUTS, context, timestamp);
		}
		
		else if (selectedIndex == Q_INPUTS) {
			label1.setText("affect " + contextPretty);
			
			query = new Query(Query.QUESTION_INPUTS, context, timestamp);
		}
		
		else if (selectedIndex == Q_WHAT_IF) {
			label1.setText("conditions are different");
			whatIfPanel.setVisible(true);
			
			// send off void query to clear explanation
			query = new Query(Query.QUESTION_NONE, context, timestamp);
		}

		if (query != null) {
//			render(query, widgetState, value);
			notifyListeners(query);
		}
	}
	
	/**
	 * Can be overridden by subclasses to provide customized UI to get what-if input.
	 * @return
	 */
	public Query getWhatIfQuery(long timestamp) {
		return whatIfPanel.getWhatIfQuery(timestamp);
	}
	
	/* -----------------------------------------------------------
	 * Query Listening code
	 * ----------------------------------------------------------- */
	
	/** List of {@link QueryListener}s subscribed to this. */
	protected List<QueryListener> listeners = new ArrayList<QueryListener>();

	/**
	 * Add a {@link QueryListener} to be notified when the user asks a {@link Query}.
	 * @param listener
	 */
	public void addQueryListener(QueryListener listener) {
		listeners.add(listener);
		// notify if any query exists
		if (query != null) {
			notifyListeners(query);
		}
	}
	
	/**
	 * Remove a {@link QueryListener}.
	 * @param listener
	 */
	public void removeQueryListener(QueryListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Called to notify {@link QueryListener}s that a new {@link Query}
	 * has been asked by the user.
	 * @param query
	 */
	protected void notifyListeners(Query query) {
		for (QueryListener listener : listeners) {
			listener.queryInvoked(query);
		}
	}

}
