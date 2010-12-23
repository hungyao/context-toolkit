package context.arch.discoverer.query;

import java.util.Collection;
import java.util.List;

import context.arch.comm.DataObject;
import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.dataModel.AbstractDataModel;

/**
 * 
 * @author Brian Y. Lim
 *
 */
public class HmmQueryItem extends AbstractQueryItem<HmmWrapper, ComponentDescription> {

	private static final long serialVersionUID = 1108567664776021828L;

	public static final String HMM_QUERY_ITEM = "HMM_QUERY_ITEM";

	protected HmmWrapper hmm;
//	protected WidgetElement elementToMatch; // not really using a comparison paradigm, so this is not compatible
	
	protected List<String> lastOutcomeValueSequence;

	public HmmQueryItem(HmmWrapper hmm) {
		this.hmm = hmm;
		
		// TODO consider format of inputs
		// AbstractDescriptionElement outcomeValue
		//    or it could be an Instances training set if classifier is not yet trained
		//    but that would be computationally expensive, and doesn't allow customization, but allows online learning
		//    can be used for kNN clustering or any other lazy learners
		// AbstractComparison as classifier (then can plug different ones)
	}
	
	public HmmWrapper getHMMWrapper() {
		return hmm;
	}
	
	public List<String> getLastOutcomeValueSequence() {
		return lastOutcomeValueSequence;
	}

	@Override
	public Collection<ComponentDescription> search(AbstractDataModel dataModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean match(ComponentDescription widgetState) {
//		// save inputs
//		List<String> inputsSequence = new ArrayList<String>(widgetState.getNonConstantAttributeNameValues());
//		enactor.setInputsSequence(inputsSequence);

//		System.out.println("process widgetState = " + widgetState);
		lastOutcomeValueSequence = hmm.classify(widgetState);
//		System.out.println("process lastOutcomeValueSequence = " + lastOutcomeValueSequence);
		if (lastOutcomeValueSequence == null) { return null; }
		else { return true; }
	}

	@Override
	public DataObject toDataObject() {
//		DataObjects v = new DataObjects();
//		v.add(classifier.toDataObject());
//		return new DataObject(CLASSIFIER_QUERY_ITEM, v);
		return null; // TODO
	}
	
	public static AbstractQueryItem<?,?> fromDataObject(DataObject data) {
		// TODO
		return null; // TODO: do I need to implement this?
	}

	@Override
	public String toString() {
		return "HMMQueryItem : " + hmm;
	}

}
