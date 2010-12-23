package context.arch.intelligibility.presenters;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import context.arch.enactor.Enactor;
import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.expression.Expression;
import context.arch.intelligibility.expression.Parameter;
import context.arch.intelligibility.query.Query;

public class QueryableTablePanelPresenter extends TypePanelPresenter {

	public QueryableTablePanelPresenter(Enactor enactor) {
		super(enactor);
	}
	
	/**
	 * Overridden to add right-click query popups.
	 */
	@Override
	protected void enhanceTable(final ExplanationTable table) {
		super.enhanceTable(table);

		final ContextQueryPopupMenu popup = new ContextQueryPopupMenu();
		addContextQueryPopupMenuItems(popup);
		
		// add listener
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent evt) {
//				if (evt.isPopupTrigger()) { // also allow left click
					Point p = evt.getPoint();
					int row = table.rowAtPoint(p);
					int col = table.columnAtPoint(p);
					
					// select cell to highlight
					table.setRowSelectionInterval(row, row);
					table.setColumnSelectionInterval(col, col);
					
					// only show popup if it is a Context name (not value)
					if (table.getColumnName(col).equals("Context")) {		
						popup.context = (String)table.getValueAt(row, col);
						popup.show(table, evt.getX(), evt.getY());
					}
//				}
			}
			
			@Override
			public void mouseExited(MouseEvent evt) {	
				table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
		
		table.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent evt) {
				Point p = evt.getPoint();
				int col = table.columnAtPoint(p);
				if (table.getColumnName(col).equals("Context")) {		
					table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				else {
					table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});
	}
	
	public class ContextQueryPopupMenu extends JPopupMenu implements ActionListener {
		
		private static final long serialVersionUID = -8212178694360220450L;
		
		public String context;
		
		public ContextQueryPopupMenu() {
			super();
		}
		
		public ContextQueryPopupMenu(String context) {
			this();
			this.context = context;
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			String question = evt.getActionCommand();
			long timestamp = System.currentTimeMillis();
			
			Query query = new Query(question, context, timestamp);
			handleContextQueryPopupMenuItem(query);
		}
	}
	
	/**
	 * Subclasses can override this to support more questions. 
	 * @param popup
	 */
	public void addContextQueryPopupMenuItems(ContextQueryPopupMenu popup) {
		QueryButton.attachMenuItem(Query.QUESTION_WHAT, popup, popup); // so that user can easily be reminded of value, when seeing another explanation
		QueryButton.attachMenuItem(Query.QUESTION_DEFINITION, popup, popup);
		QueryButton.attachMenuItem(Query.QUESTION_RATIONALE, popup, popup);		
	}

	/**
	 * Subclasses can override this to support more questions. 
	 * @param query
	 */
	protected void handleContextQueryPopupMenuItem(Query query) {
		String question = query.getQuestion();
		String context = query.getContext();

		Explanation explanation = explainer.getExplanation(query);
		
		if (question.equals(Query.QUESTION_DEFINITION)) {
			renderDefinitionDialog(context, explanation.getContent());
		}
		else if (question.equals(Query.QUESTION_RATIONALE)) {
			renderRationaleDialog(context, explanation.getContent());
		}
		else if (question.equals(Query.QUESTION_WHAT)) {
			renderWhatDialog(context, explanation.getContent());
		}
	}

	protected <T> void renderWhatDialog(String context, Expression expression) {
		JDialog dialog = new JDialog();
		dialog.setTitle(context);
		
		Object val = ((Parameter<?>)expression).getValue();
		
		JTextField field = new JTextField(val.toString());
		field.setEditable(false);
		dialog.add(field);

		dialog.pack();
		dialog.setLocationRelativeTo(null); // center of screen
		dialog.setVisible(true);
	}

	protected void renderDefinitionDialog(String context, Expression expression) {
		JFrame dialog = new JFrame();
		dialog.setTitle("Definition of " + context);
		
		JScrollPane scrollpane = (JScrollPane)renderDefinition(expression);
		scrollpane.setPreferredSize(new Dimension(480,400));
		dialog.add(scrollpane);
		
		dialog.pack();
		dialog.setLocationRelativeTo(null); // center of screen
		dialog.setVisible(true);
	}

	protected void renderRationaleDialog(String context, Expression expression) {
		JFrame dialog = new JFrame();
		dialog.setTitle("Rationale for " + context);
		
		JScrollPane scrollpane = (JScrollPane)renderDefinition(expression);
		scrollpane.setPreferredSize(new Dimension(480,400));
		dialog.add(scrollpane);
		
		dialog.pack();
		dialog.setLocationRelativeTo(null); // center of screen
		dialog.setVisible(true);
	}

}
