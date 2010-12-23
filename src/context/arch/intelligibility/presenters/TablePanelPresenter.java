package context.arch.intelligibility.presenters;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import context.arch.discoverer.ComponentDescription;
import context.arch.enactor.Enactor;
import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.expression.Comparison;
import context.arch.intelligibility.expression.DNF;
import context.arch.intelligibility.expression.Expression;
import context.arch.intelligibility.expression.Negated;
import context.arch.intelligibility.expression.Parameter;
import context.arch.intelligibility.expression.Reason;
import context.arch.intelligibility.query.Query;
import context.arch.intelligibility.query.WhatIfQuery;
import context.arch.storage.AttributeNameValue;


/**
 * Renders explanations into a JPanel.
 * It only considers the structure of the explanation and not the context or question.
 * It requires supplied explanations to be in DNF.
 * @author Brian Y. Lim
 *
 */
public class TablePanelPresenter extends Presenter<JPanel> {
	
	protected JPanel panel;
	
	protected DecimalFormat nf = (DecimalFormat)DecimalFormat.getInstance();

	protected Query query;
	
	/**
	 * 
	 * @param parent that the rendered panel would be attached to
	 */
	public TablePanelPresenter(Enactor enactor) {
		super(enactor);
		
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
	}
	
	public JPanel getPanel() {
		return panel;
	}

	/**
	 * Each call to render really just updates the panel, but returns the same panel.
	 */
	@Override
	public JPanel render(Explanation explanation) {
		query = explanation.getQuery();
		
		/*
		 * Render content
		 */
		DNF content = explanation.getContent();
		JComponent contentComponent = renderReasons(content);
		
		panel.removeAll();
		panel.add(contentComponent);		
		panel.revalidate();
		
		return panel;
	}
	
	/* ------------------------------------------------------
	 * Rendering for explanation query
	 * ------------------------------------------------------ */
	
	// Not doing
	
	/* ------------------------------------------------------
	 * Rendering for explanation content
	 * ------------------------------------------------------ */
	
	/**
	 * Renders an expression that is terminal, i.e. not Conjunction or Disjunction
	 * @param expression
	 * @return
	 */
	protected JComponent renderTerminal(Parameter<?> expression) {
		/*
		 * The following allows multiple columns in a table, rather than a single text-field.
		 */
		
		Reason reason = new Reason();
		AbstractTableModel tableModel;	
		
		boolean whatif = false;
//		if (query != null && query.getQuestion().equals(WhatIfQuery.QUESTION_WHAT_IF)) {
		if (query != null && query.getQuestion().equals(WhatIfQuery.QUESTION_INPUTS)) {
			whatif = true;
		}

		if (expression instanceof Comparison<?>) {
			reason.add(expression);
//			tableModel = new ComparisonsTableModel(expressionList);
			tableModel = new DualComparisonsTableModel(reason);
		}
		else if (expression instanceof Parameter<?>) {
			reason.add(expression);
			tableModel = new ParametersTableModel(reason, whatif);
		}
		else {
			return null;
		}
		
		ExplanationTable table = new ExplanationTable(tableModel);
		
		if (whatif) {
			TableColumn col = table.getColumnModel().getColumn(1);
			col.setCellEditor(table.new SpinnerEditor());
		}
		
		JScrollPane scrollpane = new JScrollPane(table);		
		return scrollpane;
	}

	/**
	 * 
	 * @param reason
	 * @return the scroll pane containing the JList
	 */
	protected JComponent renderReason(Reason reason) {
		if (reason.isEmpty()) { return null; }

		AbstractTableModel tableModel;
		
		boolean whatif = false;
//		if (query != null && query.getQuestion().equals(WhatIfQuery.QUESTION_WHAT_IF)) {
		if (query != null && query.getQuestion().equals(WhatIfQuery.QUESTION_INPUTS)) {
			whatif = true;
		}
		
		/*
		 * Check type
		 */
		Expression exp0 = reason.get(0);
		if (exp0 instanceof Comparison<?>) {
//			tableModel = new ComparisonsTableModel(expressionList);
			tableModel = new DualComparisonsTableModel(reason);
		}
		else if (exp0 instanceof Parameter<?>) {
			tableModel = new ParametersTableModel(reason, whatif);
		}
		else {
			return null; 
		}
		
		JTable table = new ExplanationTable(tableModel);
		JScrollPane scrollpane = new JScrollPane(table);		
		return scrollpane;
	}

	protected void addTerminalToList(Parameter<?> expression, DefaultListModel listModel) {
		if (expression instanceof Negated<?>) {}
		else if (expression instanceof Comparison<?>) {}
		else if (expression instanceof Parameter<?>) {}
		else {
			return; // do nothing if not terminal
		}

		listModel.addElement(renderTerminal(expression));
	}

	/**
	 * E.g. for multiple traces of Why Not and How To.
	 * @param disjunction
	 */
	protected JComponent renderReasons(DNF disjunction) {
		if (disjunction.size() == 1) { // if it's just size one
			// then reduce to simply a conjunction
			return renderReason(disjunction.get(0));
		}
		
		// to put lists in
		JTabbedPane tabbedPane = new JTabbedPane();
				
		int i = 0;
		for (Reason reason : disjunction) {
			JComponent trace = renderReason(reason);
			tabbedPane.addTab("Reason " + ++i, trace);
		}
		
		return tabbedPane;
	}
	
	/**
	 * Subclasses should override this to render the expression as pretty strings.
	 * @param expression
	 * @return
	 */
	protected static String expressionToPrettyString(Expression expression) {
		return expression.toString(); // TODO: this should be offloaded to a StringPresenter class
	}
	
	/* ------------------------------------------------------
	 * Classes to model explanations
	 * ------------------------------------------------------ */
	
	protected class ExplanationTable extends JTable {
		
		private static final long serialVersionUID = -8637900302897337738L;
		
		protected ExplanationTable(AbstractTableModel model) {
			super(model);
			
			// can only select one cell at a time
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  
			setCellSelectionEnabled(true);  
			
			/*
			 * Enhance with whatever functionality that subclass presenters may add
			 */
			enhanceTable(this);
			
			/*
			 * Adjust column widths
			 */
			Enumeration<TableColumn> cols = getColumnModel().getColumns();;
			while (cols.hasMoreElements()) {
				TableColumn col = cols.nextElement();
				if (col.getHeaderValue().equals("Comparison")) {
					col.setPreferredWidth(10);
				}
			}
			
			if (getColumnModel().getColumnCount() > 1) {
				TableColumn col = getColumnModel().getColumn(1);
				col.setCellRenderer(new DefaultTableCellRenderer() {
					private static final long serialVersionUID = 1L;				
					
					@Override
					public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
						if (!(value instanceof Double)) { return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); }
						final double val = (Double)value;
						nf.applyPattern("#0.00");
						
						JLabel comp = new JLabel("" + nf.format(val)) {
							private static final long serialVersionUID = 4626767143154932272L;
							private Color POS_COLOR = new Color(30,100,255, 180);
							private Color NEG_COLOR = new Color(245,40,30, 180);
							private Color LINE_COLOR = new Color(120,120,120, 200);
							@Override
							public void paint(Graphics g) {
								int W = getWidth() / 2;
								int H = getHeight();
	
								int w = (int)(W*val/50);
								if (val > 0) {
									g.setColor(POS_COLOR);
									g.fillRect(W, 0, w, H);
									g.setColor(LINE_COLOR);
									g.drawRect(W, 0, w, H);
								}
								else {
									g.setColor(NEG_COLOR);
									g.fillRect(W+w, 0, -w, H-1);
									g.setColor(LINE_COLOR);
									g.drawRect(W+w, 0, -w, H-1);
								}
								
								super.paint(g);
							}
						};
						comp.setHorizontalTextPosition(JLabel.CENTER); // this somehow doesn't work!
						return comp;
					}
				});
			}
		}
		
		/**
		 * Allows cell in JTable to be editable when double clicked.
		 * Ref: http://www.exampledepot.com/egs/javax.swing.table/Spinner.html
		 */
		public class SpinnerEditor extends AbstractCellEditor implements TableCellEditor, ChangeListener { 
			
			private static final long serialVersionUID = -8330020225607069931L;
			
			final JSpinner spinner = new JSpinner(); // Initializes the spinner. 
			
			private JTable table;
			private int row = -1;
			
			public SpinnerEditor() { 
				SpinnerModel model =
			        new SpinnerNumberModel(0, //initial value
			                               -100, //min
			                               100, //max
			                               .2); //step
				spinner.setModel(model); 
				spinner.addChangeListener(this);
			} 
			
			// Prepares the spinner component and returns it. 
			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) { 
				spinner.setValue(value);

				this.table = table;
				this.row = row;
				
				return spinner; 
			} 
			
			// Enables the editor only for double-clicks. 
			public boolean isCellEditable(EventObject evt) { 
				if (evt instanceof MouseEvent) { 
					return ((MouseEvent)evt).getClickCount() >= 2; 
				} 
				return true; 
			} 
			
			// Returns the spinners current value. 
			public Object getCellEditorValue() { 
				return spinner.getValue(); 
			}

			@Override
			public void stateChanged(ChangeEvent evt) {
				if (table != null && row > -1) {
					int column = 1;
					this.table.getModel().setValueAt(spinner.getValue(), row, column);
				}
			}

		}

		@Override
		public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
			/*
			 * Show text of cell in tooltip, since cell may not be big enough to show everything
			 */
			Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
			if (c instanceof JComponent) {
				JComponent jc = (JComponent)c;
				Object value = this.getValueAt(rowIndex, vColIndex);
				if (value != null) {
					jc.setToolTipText(value.toString());
				}
			}
			if (c instanceof JLabel) {
				if (getColumnName(vColIndex).equals("Comparison")) {
					((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
				}
			}			
			return c;
		}
		
	}
	
	/**
	 * This assumes that most explanations are rendered in tables, and specifically in table cells.
	 * The default behavior is to add tooltips to the cells.
	 * 
	 * Subclasses can enhance further, e.g. by supporting right-click popups to ask for explanations.
	 * 
	 * @param comp the table cell component to enhance
	 * @param table
	 * @param rowIndex
	 * @param vColIndex
	 */
	protected void enhanceTable(ExplanationTable table) {
		// do nothing for now
	}
	
	protected static class ValuesTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 3689437044797418681L;
		
		protected Comparable<?>[] values;
		protected int numCols;
		
		protected ValuesTableModel() {}
		
		protected ValuesTableModel(List<Expression> list) {
			values = new Comparable<?>[list.size()];
//			numCols = 1;
//			
//			for (int i = 0; i < values.length; i++) {
//				Value<?> val = (Value<?>)list.get(i);
//				values[i] = val.getValue();
//			}
		}
		
		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0: return "Value";
			default: return null;
			}
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			switch (col) {
			case 0: return values[row];
			default: return null;
			}
		}
		
		@Override public boolean isCellEditable(int row, int col) { return false; }		
		@Override public int getRowCount() { return values.length; }		
		@Override public int getColumnCount() { return numCols; }
		
	}
	
	protected class ParametersTableModel extends ValuesTableModel {

		private static final long serialVersionUID = 5755342189654583969L;
		
		protected String[] contexts; // name

		private boolean editable;
		
		protected ParametersTableModel(Reason list, boolean editable) {
			super();
			this.editable = editable;
			
			contexts = new String[list.size()];
			values = new Comparable<?>[list.size()];
			numCols = 2;
			
			for (int i = 0; i < contexts.length; i++) {
				Parameter<?> param = (Parameter<?>)list.get(i);
				contexts[i] = param.getName();
				values[i] = param.getValue();
			}
		}
		
		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0: return "Context";
			case 1: return "Value";
			default: return null;
			}
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			switch (col) {
			case 0: return contexts[row];
			case 1: return values[row];
			default: return null;
			}
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			switch (col) {
			case 0: 
				contexts[row] = (String)value;
			case 1: 
				values[row] = (Comparable<?>) value;
			}
		}

		@Override 
		public boolean isCellEditable(int row, int col) { 
			if (editable && col == 1) { return true; }
			return false; 
		}
		
		/**
		 * TODO This actually overrides the original widget state after a What If query.
		 * We can then subsequently ask other types of questions of this new state.
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public <T extends Comparable<? super T>> ComponentDescription saveWidgetState() {
			ComponentDescription widgetState = TablePanelPresenter.this.enactor.getInWidgetState();
			
			for (int r = 0; r < contexts.length; r++) {
				widgetState.getNonConstantAttributes().add(AttributeNameValue.instance(contexts[r], (T) values[r]));
			}
			
			return widgetState;
		}
		
	}
	
	protected class ComparisonsTableModel extends ParametersTableModel {

		private static final long serialVersionUID = -1561280486178631629L;
		
		protected Comparison.Relation[] comparisons;
		
		protected ComparisonsTableModel(Reason list) {
			super(list, false);
			
			comparisons = new Comparison.Relation[list.size()];
			numCols = 3;
			
			for (int i = 0; i < contexts.length; i++) {
				Comparison<?> comp = (Comparison<?>)list.get(i);
				comparisons[i] = comp.getRelationship();
			}
		}
		
		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0: return "Context";
			case 1: return "Comparison";
			case 2: return "Value";
			default: return null;
			}
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			switch (col) {
			case 0: return contexts[row];
			case 1: return comparisons[row];
			case 2: return values[row];
			default: return null;
			}
		}
		
	}
	
	protected class DualComparisonsTableModel extends ComparisonsTableModel {

		private static final long serialVersionUID = 2715542289639911385L;
		
		protected Object[] values1;
		protected Comparison.Relation[] comparisons1;
		
		protected DualComparisonsTableModel(Reason list) {
			super(list);

			comparisons1 = new Comparison.Relation[list.size()];
			values1 = new Object[list.size()];
			numCols = 5;
			
			for (int i = 0; i < contexts.length; i++) {
				Comparison<?> comp = (Comparison<?>)list.get(i);
				comparisons1[i] = comp.getRelationship1();
				if (comparisons1[i] == null) { // not set
					comparisons1[i] = Comparison.Relation.NO_RELATION; // TODO set non-null to prevent null pointer exception
					values1[i] = "";
				}
				else {
					values1[i] = comp.getValue1();
				}
			}
		}
		
		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0: return "Lower Value";
			case 1: return "Comparison";
			case 2: return "Context";
			case 3: return "Comparison";
			case 4: return "Upper Value";
			default: return null;
			}
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			switch (col) {
			case 0: return values1[row];
			case 1: return comparisons1[row];
			case 2: return contexts[row];
			case 3: return comparisons[row];
			case 4: return values[row];
			default: return null;
			}
		}
		
	}
	
	/**
	 * Convenience method to wrap a JComponent in a scrollbar
	 * @param component
	 * @return
	 */
	public static JScrollPane scrollbarWrap(JComponent component) {
		// wrap so that it has a flow layout
		JPanel wrapper = new JPanel();
		wrapper.add(component);
		
		// so that it can scroll
		JScrollPane scrollpane = new JScrollPane(component);		
		scrollpane.getVerticalScrollBar().setUnitIncrement(16);
		
		// doesn't work since no size has been determined yet
//		scrollpane.getViewport().setViewPosition(new Point(0,0)); 
////		wrapper.scrollRectToVisible(new Rectangle(0,0,0,0));
//		scrollpane.revalidate();
//		System.out.println(scrollpane.getViewport().getViewRect());
//		System.out.println(scrollpane.getViewport().getViewPosition());
		
		return scrollpane;
	}

}
