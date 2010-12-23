package context.arch.enactor;

import java.util.Date;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.query.ClassifierQueryItem;
import context.arch.discoverer.query.comparison.ClassifierComparison;

public class ClassifierEnactorReference extends EnactorReference {
	
	protected Date whenClassified;
	
//	public ClassifierEnactorReference(ClassifierQueryItem classifierQuery) {
//		super(classifierQuery, null); // not associated with any single outcomeValue
//	}
	
	public ClassifierEnactorReference(ClassifierEnactor enactor) {
		super(	enactor, 
				new ClassifierQueryItem(enactor.getClassifier(), new ClassifierComparison()), 
				null); // not associated with any single outcomeValue
	}
	
	/**
	 * Since this is tied to multiple outcomeValues,
	 * need to extract current value from the EnactorComponentInfo
	 * that was just stored during the comparison.
	 */
	@Override
	public void evaluateComponent(EnactorComponentInfo eci) {
		ComponentDescription widgetState = eci.getCurrentState();
//		String outcomeValue = Enactor.getAttValue(((ClassifierQueryItem)conditionQuery).getClassAttributeName(), widgetState.getNonConstantAttributes()).toString();
		String outcomeValue = ((ClassifierQueryItem)super.conditionQuery).getClassifierComp().getLastOutcomeValue();

//		System.out.println("ClassifierEnactorReference outcomeValue = " + outcomeValue);
//		System.out.println("ClassifierEnactorReference classAttributeName = " + ((ClassifierQueryItem)conditionQuery).getClassAttributeName());
		
		// update the outcome value of the enactor and reference
		enactor.setOutcomeValue(outcomeValue); 
		enactor.setInWidgetState(widgetState);
		this.setOutcomeValue(outcomeValue);
		
		conditionSatisfied(eci); // subclass can do whatever
		
		enactor.fireComponentEvaluated(eci); // notify listeners after reacting		
	}

//	/**
//	 * Now this may be invoked when outcome takes multiple values.
//	 * Read the Enactor's outcomeValue to determine what it is.
//	 */
//	@Override
//	public abstract void conditionSatisfied(EnactorComponentInfo eci);

}
