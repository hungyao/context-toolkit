package context.apps.demos.homeactivity;

import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.ClassnameElement;
import context.arch.discoverer.query.HmmWrapper;
import context.arch.discoverer.query.RuleQueryItem;
import context.arch.enactor.HmmEnactor;

/**
 * Main enactor for the Home Activity Recognition demo application.
 * It uses a Hidden Markov Model (HMM) to learn and infer what a home occupant
 * is doing depending on which sensors around the home he triggers.
 * It takes in a {@link SensorsWidget}, and tracks its state to update an {@link ActivityWidget}.
 * 
 * @author Brian Y. Lim
 *
 */
public class ActivityEnactor extends HmmEnactor<ObservationVector> {

	public ActivityEnactor() {
		super(
				RuleQueryItem.instance(new ClassnameElement(SensorsWidget.class)),	
				RuleQueryItem.instance(new ClassnameElement(ActivityWidget.class)),
				ActivityWidget.ACTIVITY, 
				new HmmKasteren(), 
				"");

		start();
	}
	
	/**
	 * Wrapper for a HMM model derived from the Kasteren et al. 08 (Ubicomp 2008) dataset about 
	 * domestic activity recognition. It takes the dataset formatted to a JAHMM model.
	 * @author Brian Y. Lim
	 *
	 */
	private static class HmmKasteren extends HmmWrapper {

		public HmmKasteren() {
			super(
					"demos/home-hmm/kasteren-jahmm-header.properties", 
					"demos/home-hmm/kasteren-jahmm.model", 
					5); // sequence length (5 minutes)
		}

		@Override
		protected boolean isInstanceExtractable(ComponentDescription widgetState) {
			if (widgetState == null) { return false; }
			
			// check if widget is an instance of AccelerometerWidget by ID
			/// don't use classname, as that may be null, given the poor guarantees in the CTK
			boolean isInstanceExtractable = widgetState.id.contains(SensorsWidget.CLASSNAME)
			;
//				&& Enactor.getAttValue(SensorsWidget.FEATURES[0], widgetState) != null; // check some entry not null
			
			return isInstanceExtractable;
		}
		
	}

}
