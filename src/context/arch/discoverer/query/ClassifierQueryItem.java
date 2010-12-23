package context.arch.discoverer.query;

import java.util.Collection;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.dataModel.AbstractDataModel;
import context.arch.discoverer.query.comparison.ClassifierComparison;

/**
 * TODO: if each EnactorReference initializes their own QI at recovery time from DataObjects,
 * then they each would have separate classifiers, and we lose the caching 
 * 
 * TODO: rename to WekaClassifierQueryItem?
 * 
 * @author Brian Y. Lim
 *
 */
public class ClassifierQueryItem extends AbstractQueryItem<ClassifierWrapper, ComponentDescription> {

	private static final long serialVersionUID = 1108567664776021828L;

	public static final String CLASSIFIER_QUERY_ITEM = "CLASSIFIER_QUERY_ITEM";

	protected ClassifierWrapper classifier;
	protected ClassifierComparison classifierComp;
//	protected WidgetElement elementToMatch; // not really using a comparison paradigm, so this is not compatible

	public ClassifierQueryItem(ClassifierWrapper classifier, ClassifierComparison classifierComp) {
		this.classifier = classifier;
		this.classifierComp = classifierComp;
		
		// TODO consider format of inputs
		// AbstractDescriptionElement outcomeValue
		//    or it could be an Instances training set if classifier is not yet trained
		//    but that would be computationally expensive, and doesn't allow customization, but allows online learning
		//    can be used for kNN clustering or any other lazy learners
		// AbstractComparison as classifier (then can plug different ones)
	}
	
	public ClassifierWrapper getClassifier() {
		return classifier;
	}

	public ClassifierComparison getClassifierComp() {
		return classifierComp;
	}
	
	/**
	 * Note that this is the name of the class attribute of the Weka dataset.
	 * Not necessarily about Widget Attribute. May be different.
	 * May not match the outcomeName of the Enactor too.
	 * @return
	 */
	public String getClassAttributeName() {
		return classifier.getClassAttributeName();
	}

	@Override
	public Collection<ComponentDescription> search(AbstractDataModel dataModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean match(ComponentDescription widgetState) {
		return classifierComp.compare(classifier, widgetState);
	}

	@Override
	public DataObject toDataObject() {
		DataObjects v = new DataObjects();
		v.add(classifier.toDataObject());
		return new DataObject(CLASSIFIER_QUERY_ITEM, v);
	}
	
	public static AbstractQueryItem<?,?> fromDataObject(DataObject data) {
		// TODO
		return null;
	}

	@Override
	public String toString() {
		return "LearnerQueryItem : " + classifier;
	}

}
