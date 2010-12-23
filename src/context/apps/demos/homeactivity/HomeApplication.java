package context.apps.demos.homeactivity;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import context.apps.ContextModel;
import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.presenters.Presenter;
import context.arch.intelligibility.query.Query;

public class HomeApplication extends JFrame {

	static final long serialVersionUID = -1719370727845915424L;

	HomeModel contextModel;
	HomeEvidencePanel evidencePanel;
	SimpleHomeQueryPanel homeQueryPanel;

	private TimeStepPanel timeStepPanel;
	
	Presenter<Void> presenter;
		
	public HomeApplication() {
		super("Intelligibility - Home");
		
		/*
		 * Context modeling
		 */
		contextModel = new HomeModel();
		
		presenter = new Presenter<Void>(contextModel.activityEnactor) {
			@Override
			public Void render(Explanation explanation) {				
				Query query = explanation.getQuery();
				if (query == null) { return null; }
				String question = query.getQuestion();
				if (question == null) { return null; }
				
				evidencePanel.presenter.render(explanation);
				
				return null;
			}
		};
		
		evidencePanel = new HomeEvidencePanel(contextModel); // scenario choosing done by TimelinePanel in HomePanel
		evidencePanel.setBorder(BorderFactory.createEtchedBorder());
		add(evidencePanel, BorderLayout.CENTER);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		homeQueryPanel = new SimpleHomeQueryPanel(this);
		topPanel.add(homeQueryPanel, BorderLayout.CENTER);
		timeStepPanel = new TimeStepPanel(this);	
		contextModel.setEnactorsReadyListener(timeStepPanel);
		topPanel.add(timeStepPanel, BorderLayout.EAST);
		add(topPanel, BorderLayout.NORTH);
		
		/*
		 * JFrame settings
		 */
		pack();
//		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args) {
		ContextModel.startDiscoverer(); // TODO uncomment
		HomeApplication f = new HomeApplication();
		f.setVisible(true);
	}

}
