package context.apps.demos.homeactivity;

/**
 * 
 * @author Brian Y. Lim
 *
 */
public class HomeModel {
	
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
		
		/*
		 * Enactors
		 */
		sensorsGenerator = new SensorsGenerator();
		activityEnactor = new ActivityEnactor();
	}
	
	public int numTimeSteps() {
		return sensorsGenerator.numObservations();
	}
	
	protected void setTimeStep(int timeStep) {
		sensorsGenerator.setTimeStep(timeStep);
	}

}