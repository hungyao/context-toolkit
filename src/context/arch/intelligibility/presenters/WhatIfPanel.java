package context.arch.intelligibility.presenters;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.layout.SpringUtilities;

import context.arch.intelligibility.DescriptiveExplainerDelegate;
import context.arch.intelligibility.Explainer;
import context.arch.intelligibility.expression.Parameter;
import context.arch.intelligibility.expression.Reason;
import context.arch.intelligibility.query.Query;
import context.arch.intelligibility.query.WhatIfQuery;
import context.arch.intelligibility.reducers.ConjunctionReducer;
import context.arch.storage.AttributeNameValue;

/**
 * GUI class to provide a UI for obtaining a What-If query from the user.
 * Set it up by constructing, and setting the initial input values (via {@link #setInputs(Reason)}
 * and when this UI is visible, it will show the inputs and their pre-set values.
 * Users can change the values by typing (no type safety checks), and it would highlight if
 * the values are changed from the initial values. To get the updated input values, call
 * {@link #getInputs()}. To construct a query from these values, call 
 * {@link #getWhatIfQuery(String, long)}.
 * @author Brian Y. Lim
 * @see WhatIfQuery
 */
public class WhatIfPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;

	private String context;
	private DescriptiveExplainerDelegate descExplainer;

	private JPanel inputsPanel;
	private JScrollPane inputsWrapper;
	private JButton askButton;
	
	protected Map<Parameter<?>, JTextField> inputFields;

	private ConjunctionReducer reducer;

	/**
	 * Create the WhatIfPanel UI. Note that {@link #setInputs(Reason)} needs to be called before
	 * this can properly render.
	 * @param context the name of the attribute or output that the query will be about
	 * @param reducer for reducing which inputs to show for the user to manipulate; can be null
	 * @param descExplainer for providing pretty names to input labels
	 * @param listener for listening to when the Ask button is pressed. Then this would be a good time
	 * to get the changed input values or get the constructed What-If query.
	 */
	public WhatIfPanel(String context, ConjunctionReducer reducer, DescriptiveExplainerDelegate descExplainer, ActionListener listener) {
		super();
		setLayout(new BorderLayout());

		this.context = context;
		this.descExplainer = descExplainer;

		inputsPanel = new JPanel();
		inputsWrapper = new JScrollPane(inputsPanel);
		add(inputsWrapper, BorderLayout.CENTER);
		
		askButton = new JButton("Ask");
		askButton.addActionListener(listener);
		add(askButton, BorderLayout.SOUTH);
		
		inputFields = new HashMap<Parameter<?>, JTextField>();
		this.reducer = reducer;

		this.setPreferredSize(new Dimension(300, 360));
	}
	
	/**
	 * Set initial, or pre-set input values to show first.
	 * Normally, this is just the result from {@link Explainer#getInputsExplanation()}
	 * @param inputs
	 */
	public void setInputs(Reason inputs) {
		if (reducer != null) {
			System.out.println("pre inputs = " + inputs);
			inputs = reducer.apply(inputs); // reduce first
			System.out.println("post inputs = " + inputs);
		}
		refreshAttributesPanel(inputs);
	}
	
	/**
	 * Gets the current values of the inputs, which may have been manipulated by the user.
	 * @return
	 */
	public Reason getInputs() {
		Reason inputs = new Reason();
		
		// read from GUI and set into new attribute values
		for (Parameter<?> input : inputFields.keySet()) {
			String valueStr = inputFields.get(input).getText();
			inputs.add(Parameter.instance(
					input.getName(), 
					AttributeNameValue.valueOf(input.getType(), valueStr) // parse value from text field to type of input
				));
		}
		
		return inputs;
	}

	/**
	 * Gets a constructed {@link WhatIfQuery} from the input values.
	 * @param timestamp the time to query about
	 * @return
	 */
	public Query getWhatIfQuery(long timestamp) {
		return new WhatIfQuery(WhatIfQuery.QUESTION_WHAT_IF, context, 
				getInputs(),
				timestamp);
	}
	
	/**
	 * Called to update the panel displaying the inputs and text fields,
	 * depending on how many inputs there are, and their values.
	 * @param inputs
	 */
	private void refreshAttributesPanel(Reason inputs) {
		// remove old
		remove(inputsWrapper);
		inputFields.clear();
		
		inputsPanel = new JPanel();
		inputsPanel.setLayout(new SpringLayout());
		
		int num = 0;
		for (final Parameter<?> input : inputs) {
			String context = input.getName();
			final String value = input.getValue().toString();
				
			JLabel label = new JLabel(descExplainer.getPrettyName(context) + ":", SwingConstants.TRAILING);
			inputsPanel.add(label);
			
			JLabel icon = new JLabel(ContextIcons.get(context, value));
			inputsPanel.add(icon);
			
			final JTextField valueField = new JTextField(value, 6);
			//valueField.setEditable(editable);
			valueField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					/*
					 * check if value changed, if so, then highlight it
					 */
					if (!valueField.getText().equals(value)) {
						valueField.setBackground(Color.orange);
					}
					else {
						valueField.setBackground(Color.white);
					}
				}
			});
			inputFields.put(input, valueField);
			inputsPanel.add(valueField);
			
			num++;
		}	

		SpringUtilities.makeCompactGrid(inputsPanel,
				num, 3, // rows, cols
                6, 6,   // initX, initY
                6, 6);  // xPad, yPad
		
		// add new in old's place
		inputsWrapper = TablePanelPresenter.scrollbarWrap(inputsPanel);
		add(inputsWrapper, BorderLayout.CENTER);
		
		//revalidate();
	}

}
