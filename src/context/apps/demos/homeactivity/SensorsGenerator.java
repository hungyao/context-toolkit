package context.apps.demos.homeactivity;

import java.io.File;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.ObservationVector;

import context.apps.demos.homeactivity.SensorsWidget.SensorsData;
import context.arch.discoverer.component.ClassnameElement;
import context.arch.discoverer.query.RuleQueryItem;
import context.arch.enactor.Generator;
import context.arch.intelligibility.hmm.SupervisedLearner;

public class SensorsGenerator extends Generator {

	private List<ObservationVector> allObservations;
	private final int sequenceLength = HomeModel.SEQUENCE_LENGTH; // default

	public SensorsGenerator() {
		super(
				RuleQueryItem.instance(new ClassnameElement(SensorsWidget.class)), 
				"Sensors", 
				"");
		
		/*
		 * Preset dataset
		 */
		String obsFilename = "demos/home-hmm/kasteren-jahmm-observations-test.seq";
		allObservations = SupervisedLearner.readObservationsSequencesFromFile(new File(obsFilename), SensorsWidget.FEATURES.length);
	}
	
	public int numObservations() {
		return allObservations.size();
	}
	
	/**
	 * 
	 * @return T, the sequence length for observations
	 */
	public int getSequenceLength() {
		return sequenceLength;
	}
	
	/**
	 * Since SensorsWidget stores sequential historical information, this is sequence sensitive.
	 * @param t some number from range from 0 to #numObservations()
	 * @return true if the update was successful
	 */
	public boolean setTimeStep(int t) {
		if (t > numObservations()) { return false; } // out of bounds
		
		// load instance from dataset
//		List<ObservationVector> observations = allObservations.subList(t, t + sequenceLength);
		ObservationVector observation = allObservations.get(t);
		System.out.println("setTimeStep observation = " + observation);

		/*
		 * Set data values
		 */
		SensorsData data = new SensorsData(System.currentTimeMillis());
		observationToArray(observation, data.featureValues);
		
		// put data into widget
//		System.out.println("setTimeStep data.toAttributes() = " + data.toAttributes());
		updateOutWidget(data);
		
		return true;
	}
	
	/**
	 * 
	 * @param observations
	 * @param featureValues [ActivityEnactor.SEQUENCE_LENGTH][SensorsWidget.FEATURES.length]
	 */
	public static void observationToArray(ObservationVector observation, final int[] featureValues) {
		for (int i = 0; i < featureValues.length; i++) {
			featureValues[i] = (int)observation.value(i);
		}
	}

}
