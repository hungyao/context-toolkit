package context.arch.enactor;

import java.util.ArrayList;
import java.util.List;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.service.helper.ServiceInput;
import context.arch.storage.Attribute;
import context.arch.storage.Attributes;
import context.arch.widget.Widget;

/**
 * Fully describes a widget via a set of attributes, and a set of conditions on
 * those attributes.  Any attribute conditioned on should be represented in
 * Attributes.
 * 
 * TODO: enforce data correspondence, possible change over to collections.
 * 
 * Added outcome field to specify what is the outcome when the rule is satisfied.
 * 
 * @author alann
 * @author Brian Y. Lim
 */
public class EnactorReference {
	
//	/** 
//	 * Even though the convention now (Jun 2010) is that each enactor has one descriptionQuery, and
//	 * all of its references have the same one, 
//	 * in future, they may be able to support a different descriptionQuery for each enactor reference
//	 */
//	protected AbstractQueryItem descriptionQuery;
	
	protected AbstractQueryItem<?,?> conditionQuery; // TODO is this being saved by hibernate?
	// TODO: also since we are now using 2 query items, the hibernate storage needs to be fixed
	
	protected Enactor enactor;

	protected String outcomeName;
	protected String outcomeValue; // value
	/**
	 * For interpreting actions in script form
	 */
	protected List<AttributeEvalParser<?>> assnParsers;
	protected List<ServiceInput> serviceInputs;

	/**
	 * 
	 * @param enactor
	 * @param conditionQuery
	 * @param outcomeValue
	 * @param serviceInput to bind to a service to automatically invoke when the conditionQuery returns true
	 */
	public EnactorReference(Enactor enactor, AbstractQueryItem<?,?> conditionQuery, 
			String outcomeValue, 
			List<AttributeEvalParser<?>> assnParsers, 
			List<ServiceInput> serviceInputs) {
		super();
		setEnactor(enactor);
		this.conditionQuery = conditionQuery; // requiring during initialization ensures that this is unlikely to be null
		this.outcomeValue = outcomeValue;
		
		this.assnParsers = new ArrayList<AttributeEvalParser<?>>(assnParsers);
		this.serviceInputs = new ArrayList<ServiceInput>(serviceInputs);
	}

	public EnactorReference(Enactor enactor, AbstractQueryItem<?,?> conditionQuery, 
			String outcomeValue, 
			List<ServiceInput> serviceInputs) {
		super();
		setEnactor(enactor);
		this.conditionQuery = conditionQuery; // requiring during initialization ensures that this is unlikely to be null
		this.outcomeValue = outcomeValue;
		this.serviceInputs = serviceInputs;
		
		this.assnParsers = new ArrayList<AttributeEvalParser<?>>();
		this.serviceInputs = new ArrayList<ServiceInput>();
	}
	
	/**
	 * @param outcomeValue
	 * @param logicalRule
	 */
	public EnactorReference(Enactor enactor, AbstractQueryItem<?,?> conditionQuery, String outcomeValue) {
		super();
		setEnactor(enactor);
		this.conditionQuery = conditionQuery; // requiring during initialization ensures that this is unlikely to be null
		this.outcomeValue = outcomeValue;
		
		this.assnParsers = new ArrayList<AttributeEvalParser<?>>();
		this.serviceInputs = new ArrayList<ServiceInput>();
	}

	public void setEnactor(Enactor r) {
		enactor = r;
	}

	public Enactor getEnactor() {
		return enactor;
	}

	public AbstractQueryItem<?,?> getConditionQuery() {
		return conditionQuery;
	}

	public void setConditionQuery(AbstractQueryItem<?,?> conditionQuery) {
		this.conditionQuery = conditionQuery;
	}

	public void setOutcomeName(String outcomeName) {
		this.outcomeName = outcomeName;
	}
	public String getOutcomeName() {
		return outcomeName;
	}

	public void setOutcomeValue(String outcomeValue) {
		this.outcomeValue = outcomeValue;
	}
	public String getOutcomeValue() {
		return outcomeValue;
	}

	/**
	 * This method is called when a new batch of state data concerning a widget
	 * should be evaluated by this EnactorReference.
	 * Called to notify listeners.
	 * 
	 * @param widgetSubId the unique identifier for the widget subscription
	 * @param widgetState the current state of the widget
	 */
	public void evaluateComponent(EnactorComponentInfo eci) {
		// check if timestamp has been set yet
		// if not, then it is likely that the widget had never been set up yet, though it
//		Enactor.getAtt(Widget.TIMESTAMP, eci.getCurrentState().getAllAttributes());
		
		// add outcome attribute to the current state
		ComponentDescription widgetState = eci.getCurrentState();
//		String outcomeName = enactor.getOutcomeName();
		
		// update the input state and outcome value of the enactor
		enactor.setOutcomeValue(outcomeValue);
		enactor.setInWidgetState(widgetState);
		
		// save this enactor reference as the last activated one
		enactor.setLastSatisfied(this.getConditionQuery(), widgetState);
		
		// subclass can do whatever
		conditionSatisfied(eci); 
		
		// notify listeners after reacting
		enactor.fireComponentEvaluated(eci); 
	}
	
//	public abstract void conditionSatisfied(EnactorComponentInfo eci);	
	/**
	 * Does some default tasks that are common for Enactors.
	 * Namely extract the event timestamp and save it into the data widget.
	 * @param eci sent from changes in the input Widget
	 */
	public void conditionSatisfied(EnactorComponentInfo eci) {
		// get state of widget
		ComponentDescription inWidgetState = eci.getCurrentState();			

//        System.out.println(this.getClass().getSimpleName() + ".conditionSatisfied query = " + this.conditionQuery);	 
//        System.out.println(this.getClass().getSimpleName() + ".conditionSatisfied outcome = " + this.outcomeValue);	 
//        System.out.println(this.getClass().getSimpleName() + ".conditionSatisfied inWidgetState = " + inWidgetState);	

        /*
         * Evaluate assignment scripts
         */
        Attributes outAtts = new Attributes();
        
		// get time of the source, so that we can trace back by ID and timestamp
        Long timestamp = inWidgetState.getAttributeValue(Widget.TIMESTAMP);
        timestamp = timestamp != null ? timestamp : System.currentTimeMillis();
        outAtts.addAttribute(Widget.TIMESTAMP, timestamp);
		
		for (AttributeEvalParser<?> assnParser : assnParsers) {
			outAtts.addAttribute(
					assnParser.getAttributeName(), 
					assnParser.getAttributeValue(inWidgetState));
		}
		
		// legacy method that uses outcomeName and outcomeValue
		if (outcomeName != null && outcomeValue != null) {
			outAtts.addAttribute(outcomeName, outcomeValue); 
		}
		
		// subclass enactor may override attribute values
		outAtts = conditionSatisfied(inWidgetState, outAtts);
		
		// send request to update Out Widget with new data
		enactor.updateOutWidget(outAtts); 

		/*
		 * Send request to execute service
		 */
        for (ServiceInput serviceInput : serviceInputs) {
        	// set attribute values
        	Attributes inputAtts = serviceInput.getInput();
        	for (Attribute<?> att : serviceInput.getInput().values()) {
        		String attName = att.getName();
        		
        		// copy value from WidgetData into input attribute
        		//((AttributeNameValue<?>) att).copyValue((AttributeNameValue<?>) outAtts.get(attName)); // this fails if each of serviceInput.getInput() originally set as Attribute instead of AttributeNameValue
        		inputAtts.add(outAtts.get(attName));
        	}
        	
	        enactor.executeWidgetService(
	        		enactor.widgetComponentDescriptions[Enactor.OUT_WIDGET_INDEX], 
	        		serviceInput);
        }
	}
	
	/**
	 * Subclasses implement this to help build and return a data entity.
	 * Other tasks can also be done, such as generating explanations.
	 * @param inWidgetState
	 * @param outAtts out-widget attributes with some attributes already set (e.g. timestamp)
	 * @param originalTimestamp
	 * @return
	 */
	protected Attributes conditionSatisfied(ComponentDescription inWidgetState, Attributes outAtts) {
		return outAtts;
	}

	/**
	 * Called whenever a widget satisfies the EnactorReference
	 * descriptionQuery. A call to this method is advance notice that
	 * evaluateWidget will be called with this widgetSubId. A EnactorReference
	 * may override this method to provide more custom parameter Attributes to be
	 * set up by the Enactor, if unsure just leave the third arg null.
	 * 
	 * @param widgetSubId
	 */
	public void componentAdded(EnactorComponentInfo eci) {
		//TODO: warn if eci.getReference() != this
		enactor.fireComponentAdded(eci, null);
	}

	/**
	 * Called whenever a widget no longer satisfies the descriptionQuery.
	 * 
	 * @param widgetSubId
	 */
	public void componentRemoved(EnactorComponentInfo eci) {
		//TODO: warn if eci.getReference() != this
		enactor.fireComponentRemoved(eci, null);
	}
}
