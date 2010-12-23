package context.apps.demos.helloroom;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import context.apps.ContextModel;
import context.arch.enactor.Enactor;
import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.presenters.QueryPanel;
import context.arch.intelligibility.presenters.StringPresenter;
import context.arch.intelligibility.query.Query;
import context.arch.intelligibility.query.QueryListener;
import context.arch.intelligibility.reducers.ConjunctionReducer;
import context.arch.intelligibility.reducers.FilteredCReducer;

public class HelloRoomIntelligible extends HelloRoom {
	
	/** Intelligibility UI */
	protected JPanel iui;
	
	public HelloRoomIntelligible() {
		super();
	}
	
	@Override
	public void enactorsReady() {		
		// setup UI components
		ui.setVisible(true);
		iui =  new IntelligibleUI(enactor);
		
		/*
		 * start GUI
		 */
		JFrame frame = new JFrame("Hello Room Intelligible");
		frame.add(ui, BorderLayout.NORTH);
		frame.add(iui, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(320, 360));
		frame.setLocationRelativeTo(null); // center of screen
		frame.setVisible(true);
	}
	
	/**
	 * Panel for displaying intelligibility query UI and explanations.
	 * @author Brian Y. Lim
	 *
	 */
	public class IntelligibleUI extends JPanel {

		private static final long serialVersionUID = -1419171329700935534L;
		
		private QueryPanel queryPanel;
		private ConjunctionReducer creducer;
		private StringPresenter presenter;
		
		private JTextArea explanationArea;

		public IntelligibleUI(final Enactor enactor) {
			super();
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createTitledBorder("Explanations"));
			
			// reducer for showing only brightness and presence in explanations
			creducer = new FilteredCReducer("brightness", "presence", "light");
			
			// presenter for rendering explanations
			presenter = new StringPresenter(enactor);
			
			// UI for obtaining queries from the user
			queryPanel = new QueryPanel(enactor, creducer, true);
			add(queryPanel, BorderLayout.NORTH);
			
			// UI for showing explanation
			explanationArea = new JTextArea();
			add(explanationArea, BorderLayout.CENTER);

			// query listener for responding to queries
			queryPanel.addQueryListener(new QueryListener() {
				@Override
				public void queryInvoked(Query query) {
					// generate explanation
					Explanation explanation = enactor.getExplainer().getExplanation(query);
					System.out.println("explanation = " + explanation);
					
					// reduce
					explanation = creducer.apply(explanation);
					
					// render
					String explanationText = presenter.render(explanation);
					
					explanationArea.setText(explanationText);
				}
			});
		}
		
	}
	
	public static void main(String[] args) {
		ContextModel.startDiscoverer();
		
		HelloRoomIntelligible app = new HelloRoomIntelligible();
		app.start();
	}

}
