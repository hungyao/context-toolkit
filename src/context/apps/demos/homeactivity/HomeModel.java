package context.apps.demos.homeactivity;

import context.apps.ContextModel;

public class HomeModel extends ContextModel {
	
	public static final int SEQUENCE_LENGTH = 5;
	
	SensorsWidget sensorsWidget;
	ActivityWidget activityWidget;

	SensorsGenerator sensorsGenerator;
	ActivityEnactor activityEnactor;

	public HomeModel() {
		super();
		
		/*
		 * Widgets
		 */
		sensorsWidget = new SensorsWidget();
		activityWidget = new ActivityWidget();
		addWidget(sensorsWidget);
		addWidget(activityWidget);
		
		/*
		 * Enactors
		 */
		sensorsGenerator = new SensorsGenerator();
		activityEnactor = new ActivityEnactor();
		addEnactor(sensorsGenerator);
		addEnactor(activityEnactor);
		
		start();
	}
	
	public int numTimeSteps() {
		return sensorsGenerator.numObservations();
	}
	
	protected void setTimeStep(int timeStep) {
		sensorsGenerator.setTimeStep(timeStep);
	}

}