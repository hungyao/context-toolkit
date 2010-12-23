package context.apps.demos.homeactivity;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import context.arch.comm.DataObject;
import context.arch.enactor.EnactorComponentInfo;
import context.arch.enactor.EnactorListener;
import context.arch.enactor.EnactorParameter;
import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.presenters.Presenter;
import context.arch.storage.Attributes;

public class HomeEvidencePanel extends JPanel implements EnactorListener {

	private static final long serialVersionUID = -4469106651309897983L;
	
//	private TimeStepPanel timelinePanel;
	private FloorplanPanel floorplanPanel;

	TimelineStatesPanel statesPanel;
	
	public HomeEvidencePanel(HomeModel model) {
//		HmmExplainer explainer = (HmmExplainer) model.activityEnactor.getExplainer();
		
		setLayout(new BorderLayout());
		
		statesPanel = new TimelineStatesPanel(model.activityEnactor, false);
		this.add(statesPanel, BorderLayout.NORTH);
		
//		timelinePanel = new TimeStepPanel(model); // also does timeline stepping
		floorplanPanel = new FloorplanPanel(model);
		
//		model.setEnactorsReadyListener(timelinePanel);

//		add(queryPanel, BorderLayout.NORTH);
//		add(timelinePanel, BorderLayout.CENTER);
		add(floorplanPanel, BorderLayout.SOUTH);

		model.activityEnactor.addListener(this);
		
		presenter = new Presenter<Void>(model.activityEnactor) {
			@Override
			public Void render(Explanation explanation) {
				statesPanel.presenter.render(explanation);
				floorplanPanel.presenter.render(explanation);
				return null;
			}
		};
	}

	@Override
	public void componentEvaluated(EnactorComponentInfo eci) {
//		floorplanPanel.whyPresenter.render(
//				explainer.getExplanation(
//						new Query(Query.QUESTION_WHY, null, System.currentTimeMillis())));
	}

	@Override public void componentAdded(EnactorComponentInfo eci, Attributes paramAtts) {}
	@Override public void componentRemoved(EnactorComponentInfo eci, Attributes paramAtts) {}
	@Override public void parameterValueChanged(EnactorParameter parameter, Attributes validAtts, Object value) {}
	@Override public void serviceExecuted(EnactorComponentInfo eci, String serviceName, String functionName, Attributes input, DataObject returnDataObject) {}
	
	Presenter<Void> presenter; 

}
