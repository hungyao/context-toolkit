package context.arch.intelligibility.weka;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.query.ClassifierWrapper;
import context.arch.enactor.ClassifierEnactor;
import context.arch.enactor.Enactor;
import context.arch.intelligibility.Explainer;
import context.arch.intelligibility.expression.DNF;
import context.arch.intelligibility.expression.Parameter;
import context.arch.intelligibility.expression.Reason;
import context.arch.intelligibility.query.AltQuery;
import context.arch.intelligibility.query.Query;
import context.arch.intelligibility.query.WhatIfQuery;
import context.arch.intelligibility.weka.bayes.NaiveBayesExplainer;
import context.arch.intelligibility.weka.j48.J48Explainer;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Mainly adapts the explanation methods of Explainer to handle widget states with Instance data.
 * @author Brian Y. Lim
 * @param <C> class of the Weka Classifier that the Explainer can explain
 */
public abstract class WekaExplainer<C extends Classifier> extends Explainer {

	protected ClassifierWrapper classifierWrapper;
	protected C classifier;
	protected Instances header;
	protected Attribute classAttribute;
//	protected Enumeration<Attribute> attributes; // not good, as will always need to reset after use, so that others can enumerate
	
	/** Same reference as enactor field, but cast as ClassifierEnactor */
	protected ClassifierEnactor classifierEnactor;
	
	/**
	 * Gets the appropriate subclass explainer (from the standard set that has been implemented in the toolkit),
	 * depending on the classifier model (e.g. NaiveBayes, J48).
	 * @param enactor
	 * @param classifierWrapper
	 * @return
	 */
	public static WekaExplainer<?> instance(Enactor enactor, ClassifierWrapper classifierWrapper) throws Exception {
		Classifier classifier = classifierWrapper.getClassifier();
		
		if (classifier instanceof NaiveBayes) {
			return new NaiveBayesExplainer(enactor, classifierWrapper);
		}
		else if (classifier instanceof J48) {
			return new J48Explainer(enactor, classifierWrapper);
		}
		else {
			return null; // TODO: doesn't support other Weka Classifiers yet
		}
	}
	
	@SuppressWarnings("unchecked")
	protected WekaExplainer(Enactor enactor, ClassifierWrapper classifierWrapper) throws Exception {
		super(enactor);
		this.classifierEnactor = (ClassifierEnactor)enactor;
		
		this.classifierWrapper = classifierWrapper;
		this.header = classifierWrapper.getHeader();
		this.classAttribute = header.classAttribute();
		this.classifier = (C) classifierWrapper.getClassifier();
	}

	/**
	 * Overrides superclass because ClassifierEnactors only have one EnactorReference.
	 * Also does processing using the classifierWrapper instead of EnactorReference queries.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Comparable<? super T>> Parameter<T> getWhatIfExplanation(Reason altInputs) {
		// duplicate widget state and modify with what-if inputs
		ComponentDescription widgetState = enactor.getInWidgetState().clone();
		widgetState.addNonConstantAttributes(altInputs.toAttributes());
		
		String outcomeName = enactor.getOutcomeName();
		String outcomeValue = classifierWrapper.classify(widgetState);
		
		if (outcomeValue == null) { return null; } // no rule satisfied by state, so no valid reaction
		
		// return output of selected enactorRef
		Parameter<T> p = Parameter.instance(outcomeName, (T) outcomeValue);
		return p;
	}

	/**
	 * Overridden to give classifier accuracy as uncertainty.
	 */
	@Override
	public DNF getCertaintyExplanation() {
		String outcomeValue = enactor.getOutcomeValue();
		return new DNF(getAltCertaintyExplanation(outcomeValue));
	}

	/**
	 * Get certainty of an alternative outcome. May be zero.
	 */
	public Parameter<?> getAltCertaintyExplanation(String outcomeValue) {
		Instance instance = classifierWrapper.extractInstance(enactor.getInWidgetState());		
		try {	
			// find index of this outcome value
			int classIndex;
			for (classIndex = 0; classIndex < classAttribute.numValues(); classIndex++) {
				if (classAttribute.value(classIndex).equals(outcomeValue)) {
					break;
				}
			}
			double[] distroForInstance = classifier.distributionForInstance(instance);			
			double certainty = distroForInstance[classIndex];
			
//			System.out.println("getCertaintyExplanation outcomeValue = " + outcomeValue);			
//			System.out.println("getCertaintyExplanation distroForInstance");			
//			for (int i = 0; i < distroForInstance.length; i++) {
//				System.out.println("\t " + classAttribute.value(i) + ": " + distroForInstance[i]);
//			}
			
			return Parameter.instance(Query.QUESTION_CERTAINTY, certainty);
		} catch (Exception e) {
			e.printStackTrace(); // if distributionForInstance fails
		}
		return null;
	}
	
	public static void testBattery(WekaExplainer<?> explainer, String context, long timestamp, ComponentDescription widgetState, String altValue) {
		System.out.println(explainer.getExplanation(new Query(Query.QUESTION_WHAT, context, timestamp)));
		System.out.println(explainer.getExplanation(new Query(Query.QUESTION_WHY, context, timestamp)));
		System.out.println(explainer.getExplanation(new AltQuery(AltQuery.QUESTION_WHY_NOT, context, altValue, timestamp)));
		System.out.println(explainer.getExplanation(new AltQuery(AltQuery.QUESTION_HOW_TO, context, altValue, timestamp)));
		System.out.println(explainer.getExplanation(new Query(WhatIfQuery.QUESTION_WHAT_IF, context, timestamp)));
		System.out.println(explainer.getExplanation(new Query(Query.QUESTION_CERTAINTY, context, timestamp)));
//		System.out.println(explainer.getExplanation(Query.TYPE_CONTROL, instance));
		System.out.println(explainer.getExplanation(new Query(Query.QUESTION_INPUTS, context, timestamp)));
		System.out.println(explainer.getExplanation(new Query(Query.QUESTION_OUTPUTS, context, timestamp)));
//		System.out.println(explainer.getExplanation(Query.TYPE_DEFINITION, instance));
	}
	
	
	/* ==========================================================================================
	 * Convenience file storage/retrieval methods
	 * ========================================================================================== */

	public static void saveModel(Classifier cModel, String filename) throws IOException {
		OutputStream file = new FileOutputStream(filename);
		OutputStream buffer = new BufferedOutputStream(file);
		ObjectOutput output = new ObjectOutputStream(buffer);
		
		try{ output.writeObject(cModel); }
		finally{ output.close(); }
	}
	
	public static Classifier loadModel(String filename) throws IOException {
		InputStream file = new FileInputStream(filename);
		InputStream buffer = new BufferedInputStream(file);
		ObjectInput input = new ObjectInputStream(buffer);
		
		try{ return (Classifier)input.readObject(); } 
		catch (ClassCastException e) { e.printStackTrace(); }
		catch (ClassNotFoundException e) { e.printStackTrace(); }		
		finally{ input.close(); }
		
		return null;
	}

}
