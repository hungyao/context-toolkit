package context.arch.enactor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import context.arch.BaseObject;
import context.arch.comm.DataObject;
import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.enactor.server.EnactorXMLServer;
import context.arch.intelligibility.Explainer;
import context.arch.intelligibility.rules.RulesExplainer;
import context.arch.logging.EnactorRegistrationLogger;
import context.arch.logging.EnactorRuntimeLogger;
import context.arch.logging.LoggingException;
import context.arch.service.helper.ServiceInput;
import context.arch.storage.Attribute;
import context.arch.storage.AttributeNameValue;
import context.arch.storage.Attributes;
import context.arch.widget.Widget;
import context.arch.widget.Widget.WidgetData;

/**
 * The Enactor component encapsulates application logic and simplifies
 * the acquisition of context data. Use it by making subclasses that
 * define their own EnactorReferences and EnactorParameters. 
 * 
 * An enactor subclass will typically define all necessary metadata upon
 * construction. Users of the enactor can construct it, and then call
 * {@link #start()}rt() to begin operation and initialization into the CTK network. Calling
 * startXMLServer() will allow the enactor to send and receive information in XML
 * to the specified port, useful for Flash or .NET clients. 
 * 
 * @see Generator
 * 
 * @author alann
 * @author Brian Y. Lim
 * @author Kanupriya Tavri
 */
@SuppressWarnings("unchecked")
public abstract class Enactor {
	
	private static final Logger LOGGER = Logger.getLogger(Enactor.class.getName());
	static {LOGGER.setLevel(Level.INFO);} // this should be set in a configuration file
	
	//we keep one listener and use a thread-safe multicaster
	protected EnactorListener enactorListener;
	protected List<EnactorParameter> enactorParameters = new ArrayList<EnactorParameter>();
	protected Map<String, List<EnactorReference>> enactorReferences = new LinkedHashMap<String, List<EnactorReference>>(); // <outcomeValue, List<enactorReference>> multi-map; linked hashmap to maintain insertion order
	
	protected EnactorXMLServer xmlServer = null;
	protected EnactorSubscriptionManager subscriptionManager;
	
	protected String id;
	protected String hostname;
	protected int port;
	
	protected String outcomeName;
	protected String outcomeValue;
	protected ComponentDescription inWidgetState;

	public static final int IN_WIDGET_INDEX = 0;
	public static final int OUT_WIDGET_INDEX = 1;
	protected AbstractQueryItem<?,?>[] widgetSubscriptionQueries = (AbstractQueryItem<String,String>[]) new AbstractQueryItem[2]; // {inWidgetSubscriptionQuery, outWidgetSubscriptionQuery}
	protected ComponentDescription[] widgetComponentDescriptions = new ComponentDescription[2]; // {inWidgetComponentDescription, outWidgetComponentDescription}

//	protected Class<? extends Widget> inWidgetClass;
//	protected Class<? extends Widget> outWidgetClass;
	protected AbstractQueryItem<?, ?> lastSatisfiedQuery;
	protected ComponentDescription lastSatisfiedInWidgetState;
	
	protected Explainer explainer;
	
	public Enactor(AbstractQueryItem<?,?> inWidgetSubscriptionQuery, AbstractQueryItem<?,?> outWidgetSubscriptionQuery, 
			String outcomeName, String shortId) {
//		this.inWidgetClass = inWidgetClass; // may be null for Generator (subclass)
//		this.outWidgetClass = outWidgetClass;

		// null for Generator, non-null for Enactor
		if (inWidgetSubscriptionQuery != null) {
			this.widgetSubscriptionQueries[0] = inWidgetSubscriptionQuery;
		}
//		this.widgetSubscriptionQueries[1] = new RuleQueryItem<String,String>(new ClassnameElement(outWidgetClass.getName()));
		this.widgetSubscriptionQueries[1] = outWidgetSubscriptionQuery;
		
		setOutcomeName(outcomeName);
		
		setId(BaseObject.createId(getClassname(), shortId));

		this.hostname = BaseObject.getHostName();
		this.port = BaseObject.findFreePort();
//		subscriptionManager = new EnactorSubscriptionManager(this);
		
		// Explainer is lazy loaded (see getExplainer())
	}
	
	/**
	 * Can override this to replace class name; particularly for enactors created by XML declaration
	 * @return
	 */
	public String getClassname() {
		return this.getClass().getName();
	}

	public AbstractQueryItem<?,?> getInWidgetSubscriptionQuery() {
		return widgetSubscriptionQueries[0];
	}
	public void setInWidgetSubscriptionQueryQuery(AbstractQueryItem<String,String> query) {
		this.widgetSubscriptionQueries[0] = query;
	}

	public AbstractQueryItem<?,?> getOutWidgetSubscriptionQuery() {
		return widgetSubscriptionQueries[1];
	}
	public void setOutWidgetSubscriptionQueryQuery(AbstractQueryItem<String,String> query) {
		this.widgetSubscriptionQueries[1] = query;
	}
	
	/**
	 * 
	 * @return {inWidgetSubscriptionQuery, outWidgetSubscriptionQuery}
	 */
	public AbstractQueryItem<?,?>[] getSubscriptionQueries() {
		return widgetSubscriptionQueries;
	}

	public String getOutcomeName() {
		return outcomeName;
	}

	protected void setOutcomeName(String outcomeName) {
		this.outcomeName = outcomeName;
	}
	
	/**
	 * Checks if the out-widget contains a non-constant attribute with the name.
	 * @return
	 */
	public boolean containsOutAttribute(String attName) {
		//System.out.println("widgetComponentDescriptions[OUT_WIDGET_INDEX].getNonConstantAttributes() = " + widgetComponentDescriptions[OUT_WIDGET_INDEX].getNonConstantAttributes());
		return widgetComponentDescriptions[OUT_WIDGET_INDEX].getNonConstantAttributes().contains(attName);
		
		// TODO: sometimes attributes are empty, i.e. never set when subscribing
	}

	public String getOutcomeValue() {
		return outcomeValue;
	}

	public void setOutcomeValue(String outcomeValue) {
		this.outcomeValue = outcomeValue;
	}
	
	/**
	 * Can retrieve output explanation from this.
	 * @return
	 */
	public Collection<String> getOutcomeValues() {
		return enactorReferences.keySet();
	}
	
	public ComponentDescription getInWidgetState() {
		return inWidgetState;
	}
	
	public void setInWidgetState(ComponentDescription inWidgetState) {
		this.inWidgetState = inWidgetState;
	}
	
	public AbstractQueryItem<?,?> getLastSatisfiedQuery() {
		return lastSatisfiedQuery;
	}
	
	/**
	 * Saving the last satisfied query and state in the Enactor helps track the state of Enactor 
	 * due to an EnactorReference being activate.
	 * Ultimately, this can help explainers when users want an explanation during a non-event.
	 * Then they can just return the last state.
	 * 
	 * @param query
	 * @param inWidgetState may be used by subclasses to do conditional setting
	 */
	public void setLastSatisfied(AbstractQueryItem<?,?> query, ComponentDescription inWidgetState) {
		this.lastSatisfiedQuery = query;
		this.lastSatisfiedInWidgetState = inWidgetState;
	}
	
	/**
	 * Convenience method to start Enactor and the XML server its clients listen to.
	 * Both started with found free ports.
	 */
	public void start() {
		try {
			startSubscriptionManager();
			startXMLServer();
		} catch (EnactorException e) {
			e.printStackTrace();
		}
		
//		final String threadName = getId();
//		new Thread(threadName)  {
//			@Override
//			public void run() {
//				try {
//					Enactor.this.start();
//					Enactor.this.startXMLServer();
//					System.out.println("started enactor: " + getId());
//				} catch (EnactorException e) {
//					e.printStackTrace();
//				}
//			}
//		}.start();
		
		LOGGER.info(getId() + " started (port = " + this.getPort() + ")");
	}

	/**
	 * Starts operation of this enactor. Before a call to
	 * this method, the enactor is "dormant". This call registers
	 * the enactor with the CTK and processes any necessary subscriptions.
	 */
	public void startSubscriptionManager() throws EnactorException {
		subscriptionManager = new EnactorSubscriptionManager(this);

		// register this first before adding to subscription manager that also registers references, parameters, etc,
		// where that would expect this enactor to already have been registered
		try{
			EnactorRegistrationLogger.getEnactorRegistrationLogger()
									 .insertEnactorRegistrationEntry(id, 
											 enactorParameters, getReferences());
		} catch (LoggingException e) {
//			e.printStackTrace();
			/*
			 * thrown if HibernateException is thrown
			 * e.g. if Hibernate is not properly set up or the application doesn't want to suppor it
			 */			
		}
	}

	/**
	 * Starts an EnactorXMLServer running at the specified port. This
	 * server notifies clients of Enactor events, and can accept
	 * parameter changes.
	 * 
	 * @param port
	 */
	public void startXMLServer(int port) {
		//stop any existing execution -- only one XMLServer at a time
		//(if you need more you can construct your own)
		stopXMLServer();
		xmlServer = new EnactorXMLServer(this, port);
	}

	/**
	 * Starts the XML server using a found free port.
	 * @throws EnactorException
	 */
	public void startXMLServer() {
		this.startXMLServer(BaseObject.findFreePort());
//		System.out.println(this.getClass().getSimpleName() + ".startXMLServer() port = " + port);
	}

	/**
	 * Stops execution of a running EnactorXMLServer, if it exists.
	 */
	public void stopXMLServer() {
		if (xmlServer != null) xmlServer.stop();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public int getPort() {
		return port;
	}

	public EnactorParameter getParameter(String name) {
		if (name != null) {
			Iterator<EnactorParameter> i = enactorParameters.iterator();
			while (i.hasNext()) {
				EnactorParameter rp = (EnactorParameter) i.next();
				if (name.equals(rp.getName()))
					return rp;
			}
		}
		return null;
	}

	public List<EnactorParameter> getParameters() {
		return enactorParameters;
	}

	/**
	 * The result can be added to or removed from; i.e. not unmodifiable
	 * @return
	 */
	public Collection<EnactorReference> getReferences() {
		Collection<EnactorReference> references = new ArrayList<EnactorReference>();
		for (List<EnactorReference> refs : enactorReferences.values()) {
			references.addAll(refs);
		}
		return references;
	}
	
	/**
	 * Retrieve the EnactorReference associated with the outcome value.
	 * @param outcomeValue
	 * @return
	 */
	public List<EnactorReference> getReferences(String outcomeValue) {
		return enactorReferences.get(outcomeValue);
	}
	
	/**
	 * Convenience method to retrieve one reference, especially if there is only one per outcome value.
	 * @param outcomeValue
	 * @return
	 */
	public EnactorReference getReference(String outcomeValue) {
		return enactorReferences.get(outcomeValue).get(0);
	}

	/**
	 * Internal method for executing a service. Whenever an enactor wants to make something
	 * happen through the CTK, it will execute some service on a component. Components are 
	 * specified via a subscriptionID which is exposed to enactors through add and removal events.
	 * 
	 * @param subscriptionId The identifier for the CTK component hosting the service.
	 * @param serviceName The service name
	 * @param functionName The function name (services can have multiple functions)
	 * @param input Input attributes to the service call
	 * @return a DataObject representing any data returned by the Service
	 */
	protected DataObject executeWidgetService(
//			EnactorComponentInfo eci, 
			ComponentDescription cd, 
			ServiceInput serviceInput) {
		DataObject returnDataObject = subscriptionManager.executeWidgetService(
//				eci.getComponentDescription(),
				cd,
				serviceInput);

		// TODO: need to redo to use eci of out widget, not in widget
		//fireServiceExecuted(eci, serviceName, functionName, input, returnDataObject);
		
		return returnDataObject;
	}
	
	/**
	 * Call this to update the attribute(s) of the out Widget.
	 * @param data
	 * @return a DataObject representing any data returned by the Service
	 */
	public DataObject updateOutWidget(WidgetData data) {
		return updateOutWidget(data.toAttributes());
	}

	/**
	 * Call this to update the attribute(s) of the out Widget.
	 * @param atts Attributes of AttributeNameValue to set attribute values to
	 * @return a DataObject representing any data returned by the Service
	 */
	public DataObject updateOutWidget(Attributes atts) {
//        System.out.println("Enactor.updateOutWidget\t\t" + data);

		if (widgetComponentDescriptions[Enactor.OUT_WIDGET_INDEX] == null) { // not yet started
			return null;
		}
		
		/*
		 * Set timestamp
		 */
		Long originalTimestamp = atts.getAttributeValue(Widget.TIMESTAMP);
		if (originalTimestamp == null) { // null if atts forgets to set TIMESTAMP
			originalTimestamp = System.currentTimeMillis();
			atts.addAttribute(Widget.TIMESTAMP, originalTimestamp); // add current time as timestamp
		}
		addLastModifiedProperties(atts, originalTimestamp);
		
		/*
		 * Set constant attribute values
		 */
		// TODO: may not need to set, since these would always be constant

		/*
		 * Update via delegate
		 */
        DataObject returnDataObject = subscriptionManager.updateOutWidget(
        		widgetComponentDescriptions[Enactor.OUT_WIDGET_INDEX], atts);
//        System.out.println("Enactor.updateOutWidget returnDataObject = " + returnDataObject);
		return returnDataObject;
		
		// TODO maybe should also fireUpdateOutWidget
	}
	
	/**
	 * Convenience method to request to set attribute value of the widget associated with this Generator.
	 * @param <T>
	 * @param attName
	 * @param value
	 */
	@SuppressWarnings("serial")
	public <T extends Comparable<? super T>> void setAttributeValue(final String attName, final T value) {
		Attributes data = new Attributes() {{
			addAttribute(Widget.TIMESTAMP, System.currentTimeMillis()); // add timestamp
			addAttribute(attName, value);
		}};
		this.updateOutWidget(data);
	}
	
	/**
	 * Convenience method to add modification details to attributes before sending them off.
	 * @param atts
	 * @param timestamp
	 */
	protected void addLastModifiedProperties(Attributes atts, long timestamp) {
		for (Attribute<?> att : atts.values()) {
			((AttributeNameValue<?>)att).setLastModified(
					timestamp,
					this.id, this.hostname, this.port);
		}
	}

	protected boolean addParameter(EnactorParameter ep) {
		ep.setEnactor(this);
		return enactorParameters.add(ep);
	}

	protected boolean removeParameter(EnactorParameter ep) {
		ep.setEnactor(null);
		return enactorParameters.remove(ep);
	}
	
	/**
	 * Convenience method to add reference, where the outcome value is extracted from er.
	 * @param er
	 */
	protected void addReference(EnactorReference er) {
		addReference(er.getOutcomeValue(), er);
	}

	/**
	 * 
	 * @param er
	 * @param outcomeValue value associated with this enactor reference condition
	 * @return
	 */
	protected void addReference(String outcomeValue, EnactorReference er) {
		er.setEnactor(this);
		
		List<EnactorReference> refs = enactorReferences.get(outcomeValue);
		if (refs == null) { 
			refs = new ArrayList<EnactorReference>();
			enactorReferences.put(outcomeValue, refs);
		}
		refs.add(er);
		
		// also notify subscriptionManager
//		subscriptionManager.addEnactorReference(er);
	}
	
	protected void removeAllReferences() {
		for (List<EnactorReference> refs : enactorReferences.values()) {
			for (EnactorReference er : refs) {
				er.setEnactor(null);
			}
			refs.clear();
		}
		enactorReferences.clear();
	}
	
	protected void setExplainer(Explainer explainer) {
		this.explainer = explainer;
	}
	
	public Explainer getExplainer() {
		// lazily initialize explainer if not already set
		if (explainer == null) {
			explainer = new RulesExplainer(this);
		}
		return explainer;
	}

	//////////////////////
	// Begin Listener Code
	//////////////////////

	public void addListener(EnactorListener sml) {
		fireInitialAddEvents(sml);
		enactorListener = EnactorListenerMulticaster.add(enactorListener, sml);
	}

	public void removeListener(EnactorListener sml) {
		enactorListener = EnactorListenerMulticaster.remove(enactorListener, sml);
		fireFinalRemoveEvents(sml);
	}

	/* ---------------------------------------------------------------------------------------------
	 * Fire methods that call all listeners. Refer to EnactorListener class for documentation.
	 * --------------------------------------------------------------------------------------------- */

	protected final void fireComponentEvaluated(EnactorComponentInfo eci) {
		EnactorRuntimeLogger erl = EnactorRuntimeLogger.getEnactorRuntimeLogger();
		try{
			erl.insertComponentEvaluatedEntry(getId(),eci.getReference(),eci.getCurrentState());
		}catch(LoggingException e){
			e.printStackTrace();
		}
		fireComponentEvaluated(enactorListener, eci);
	}

	protected final void fireComponentAdded(EnactorComponentInfo eci, Attributes paramAtts) {
		EnactorRuntimeLogger erl = EnactorRuntimeLogger.getEnactorRuntimeLogger();
		try{
			erl.insertComponentAddedEntry(getId(),eci.getReference(),eci.getComponentDescription(),paramAtts);
		}catch(LoggingException e){
			e.printStackTrace();
		}
		fireComponentAdded(enactorListener, eci, paramAtts);
	}

	protected final void fireComponentRemoved(EnactorComponentInfo eci, Attributes paramAtts) {
		//TODO: logging hooks here
		fireComponentRemoved(enactorListener, eci, paramAtts);
	}

	protected final void fireParameterValueChanged(EnactorParameter parameter, Attributes paramAtts, Object value) {
		fireParameterValueChanged(enactorListener, parameter, paramAtts, value);
	}

	protected final void fireServiceExecuted(EnactorComponentInfo eci, String serviceName, String functionName, Attributes input, DataObject returnDataObject) {
		EnactorRuntimeLogger erl = EnactorRuntimeLogger.getEnactorRuntimeLogger();
		try{
			erl.insertServiceExecutionEntry(getId(), eci, serviceName, functionName, input);
		}catch(LoggingException e){
			e.printStackTrace();
		}
		fireServiceExecuted(enactorListener, eci, serviceName, functionName, input, returnDataObject);
	}

	//methods that inform particular listeners. override these in subclasses.

	protected void fireComponentEvaluated(EnactorListener listener, EnactorComponentInfo eci) {
		if (listener != null) listener.componentEvaluated(eci);
	}

	protected void fireComponentAdded(EnactorListener listener, EnactorComponentInfo eci, Attributes paramAtts) {
		if (listener != null) listener.componentAdded(eci, paramAtts);
	}

	protected void fireComponentRemoved(EnactorListener listener, EnactorComponentInfo eci, Attributes paramAtts) {
		if (paramAtts == null) {
			paramAtts = new Attributes();
		}
		if (listener != null) listener.componentRemoved(eci, paramAtts);
	}

	protected void fireParameterValueChanged(EnactorListener listener, EnactorParameter parameter, Attributes paramAtts, Object value) {
		if (paramAtts == null) {
			paramAtts = new Attributes();
		}

		if (listener != null) listener.parameterValueChanged(parameter, paramAtts, value);

		//ADDED for logging purposes
		EnactorRuntimeLogger erl = EnactorRuntimeLogger.getEnactorRuntimeLogger();
		try{
			erl.insertParameterValueChangedEntry(getId(),parameter,paramAtts,value);
		}catch(LoggingException e){
			e.printStackTrace();
		}
	}

	protected void fireServiceExecuted(EnactorListener listener, EnactorComponentInfo eci, String serviceName, String functionName, Attributes input, DataObject returnDataObject) {
		if (listener != null) {
			listener.serviceExecuted(eci, serviceName, functionName, input, returnDataObject); 
		}
	}

	//semantics-preserving methods

	/**
	 * fires add events to "catch up" listeners to the state of the enactor.
	 * 
	 * @param sml
	 */
	private void fireInitialAddEvents(EnactorListener sml) {
		if (subscriptionManager != null) subscriptionManager.fireAddEventsForAll(this, sml);
	}

	/**
	 * fires remove events to "catch up" listeners to the state of the enactor.
	 * 
	 * @param sml
	 */
	private void fireFinalRemoveEvents(EnactorListener sml) {
		//TODO: implement removal    
	}
	
}
