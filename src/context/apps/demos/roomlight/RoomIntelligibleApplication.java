package context.apps.demos.roomlight;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;

import context.arch.discoverer.Discoverer;
import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.presenters.QueryPanel;
import context.arch.intelligibility.presenters.StringPresenter;
import context.arch.intelligibility.query.Query;
import context.arch.intelligibility.query.QueryListener;

/**
 * Main application with GUI to display the *intelligible* smart room application.
 * This demonstrates some intelligibility features.
 * @author Brian Y. Lim
 *
 */
public class RoomIntelligibleApplication extends RoomApplication implements QueryListener {

	private static final long serialVersionUID = 2850838476909478218L;

	private QueryPanel queryPanel;
	private JLabel explanationLabel;
	private JPanel intelligibilityPanel;
	
	public RoomIntelligibleApplication() {
		super();

		/*
		 * Context intelligibility
		 */
		explainer = enactor.getExplainer();
//		presenter = new TypePanelPresenter(enactor);
		presenter = new StringPresenter(enactor);

		queryPanel = new QueryPanel(enactor);
		intelligibilityPanel.add(queryPanel);
		explanationLabel = new JLabel();
		intelligibilityPanel.add(explanationLabel);

		queryPanel.addQueryListener(this);
	}
	
	@Override
	protected void initLayout() {
		super.initLayout();
		
		/*
		 * Bottom panel for explanations
		 */
		intelligibilityPanel = new JPanel();		
		intelligibilityPanel.setPreferredSize(new Dimension(10, 80));
		add(intelligibilityPanel, BorderLayout.SOUTH);
	}
	
	@Override
	public void setLight(int light) {
		super.setLight(light);
		queryPanel.update();
	}

	/**
	 * Called when the user selects a query.
	 * It generates the corresponding explanation, and has it rendered.
	 */
	@Override
	public void queryInvoked(Query query) {
		Explanation explanation = explainer.getExplanation(query);
		System.out.println("explanation: " + explanation);
		
		String text = presenter.render(explanation);
		explanationLabel.setText("<html>" + text + "</html>");
	}
	
	public static void main(String[] args) {
		Discoverer.start();		
		new RoomIntelligibleApplication().setVisible(true);
	}

}
