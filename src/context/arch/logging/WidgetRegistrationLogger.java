/*
 * Created on Feb 16, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package context.arch.logging;

import java.util.ArrayList;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;

import context.arch.logging.hibernate.WRAttribute;
import context.arch.logging.hibernate.WRCallback;
import context.arch.logging.hibernate.WRService;
import context.arch.logging.hibernate.WRServiceFunction;
import context.arch.logging.hibernate.WidgetRegistration;
import context.arch.service.Service;
import context.arch.service.Services;
import context.arch.service.helper.FunctionDescription;
import context.arch.service.helper.FunctionDescriptions;
import context.arch.storage.Attribute;
import context.arch.storage.AttributeNameValue;
import context.arch.storage.Attributes;
import context.arch.subscriber.Callback;
import context.arch.subscriber.Callbacks;

/**
 * @author Marti Motoyama
 * @author Brian Y. Lim
 */
public class WidgetRegistrationLogger {
	private static WidgetRegistrationLogger WRLInstance;
	@SuppressWarnings("unused")
	private boolean isInitialized;
	
	protected WidgetRegistrationLogger() {
	}

	public static WidgetRegistrationLogger getWRLInstance() {
		if (WRLInstance == null) {
			synchronized (WidgetRegistrationLogger.class) {
				if (WRLInstance == null) {
					WRLInstance = new WidgetRegistrationLogger();
					WRLInstance.initialize();
				}
			}
		}
		return WRLInstance;
	}
	
	private void initialize(){
	}
	
	public void insertWidgetRegistrationEntry(	String in_id, Attributes in_constantAttributes, Attributes in_nonConstantAttributes,
												Callbacks in_callbacks, Services in_services) throws LoggingException {
		Session session = HibernateUtils.getNewSession();
		if (session == null) return;

		//Containers for entries to submit
		ArrayList<WRAttribute> wrAttributeList = new ArrayList<WRAttribute>();
		ArrayList<WRCallback> wrCallbackList = new ArrayList<WRCallback>();
		ArrayList<WRService> wrServiceList = new ArrayList<WRService>();
		ArrayList<WRServiceFunction> wrServiceFunctionList = new ArrayList<WRServiceFunction>();
		
		//Set up a widget registration entry
		WidgetRegistration widgetRegistrationEntry = new WidgetRegistration();
		widgetRegistrationEntry.setWidgetid(in_id);
		widgetRegistrationEntry.setRegistrationtime(new Date());
	    
	    //Set up the constant attribute entries
	    for (Attribute<?> attribute : in_constantAttributes.values()) {
	    	WRAttribute wrAttributeEntry = new WRAttribute();
	    	wrAttributeEntry.setAttributename(attribute.getName());
	    	wrAttributeEntry.setAttributetype(attribute.getType());
	    	wrAttributeEntry.setConstant(true);
	    	wrAttributeEntry.setWidgetRegistration(widgetRegistrationEntry);
	    	
	    	if (attribute instanceof AttributeNameValue<?>){
	    		AttributeNameValue<?> attributeNameValue = (AttributeNameValue<?>) attribute;
	    		if (attributeNameValue.getType().equals(String.class)) {
	    			wrAttributeEntry.setAttributevaluestring((String) attributeNameValue.getValue());
	    		}
	    		else if (attributeNameValue.getType().isInstance(Number.class)) {
	    			wrAttributeEntry.setAttributevaluenumeric(Float.valueOf(attributeNameValue.getValue().toString()));
	    		}
	    	}
	    	wrAttributeList.add(wrAttributeEntry);
	    }  
	    
	    // Set up the non-constant attribute entries
	    for (Attribute<?> attribute : in_nonConstantAttributes.values()) {
	    	WRAttribute wrAttributeEntry = new WRAttribute();
	    	wrAttributeEntry.setAttributename(attribute.getName());
	    	wrAttributeEntry.setAttributetype(attribute.getType());
	    	wrAttributeEntry.setConstant(false);
	    	wrAttributeEntry.setWidgetRegistration(widgetRegistrationEntry);
	    	
	    	if (attribute instanceof AttributeNameValue<?>){
	    		AttributeNameValue<?> attributeNameValue = (AttributeNameValue<?>) attribute;
	    		if (attributeNameValue.getType().equals(String.class)) {
	    			wrAttributeEntry.setAttributevaluestring((String) attributeNameValue.getValue());
	    		}
	    		else if (attributeNameValue.getType().isInstance(Number.class)) {
	    			wrAttributeEntry.setAttributevaluenumeric(Float.valueOf(attributeNameValue.getValue().toString()));
	    		}
	    	}
	    	wrAttributeList.add(wrAttributeEntry);
	    }  
	    
	    //Set up the callback entries
	    for (Callback callback : in_callbacks.values()) {
	    	WRCallback wrCallbackEntry = new WRCallback();
	    	wrCallbackEntry.setCallbackname(callback.getName());
	    	wrCallbackEntry.setWidgetRegistration(widgetRegistrationEntry);
	    	wrCallbackList.add(wrCallbackEntry);
	    }
	    
	    //Set up the service and the servicefunction entries
	    FunctionDescriptions tempFnDescriptions;
	    
	    for (Service tempService : in_services.values()) {
	    	WRService wrServiceEntry = new WRService();
	    	wrServiceEntry.setServicename(tempService.getName());
	    	wrServiceEntry.setWidgetRegistration(widgetRegistrationEntry);
	    	tempFnDescriptions = tempService.getFunctionDescriptions();
	    	
	    	for (FunctionDescription tempFnDescription : tempFnDescriptions){
	    		WRServiceFunction wrServiceFunctionEntry = new WRServiceFunction();
	    		wrServiceFunctionEntry.setWRService(wrServiceEntry);
	    		wrServiceFunctionEntry.setFunctionname(tempFnDescription.getName());
	    		wrServiceFunctionEntry.setFunctiondescription(tempFnDescription.getDescription());
	    		wrServiceFunctionList.add(wrServiceFunctionEntry);
	    	}
	    	
	    	wrServiceList.add(wrServiceEntry);
	    }
	  
	
	    Transaction tx = null;
	    try{
	    	tx = session.beginTransaction();
	    	session.save(widgetRegistrationEntry);
	    	
	    	for (int i = 0; i < wrAttributeList.size(); i++){
	    		session.save(wrAttributeList.get(i));
	    	}

	    	for (int i = 0; i < wrCallbackList.size(); i++){
	    		session.save(wrCallbackList.get(i));
	    	}
	    	
	    	for (int i = 0; i < wrServiceList.size(); i++){
	    		session.save(wrServiceList.get(i));
	    	}
	    	for (int i = 0; i < wrServiceFunctionList.size(); i++){
	    		session.save(wrServiceFunctionList.get(i));
	    	}
	    	
	    	tx.commit(); // flush the Session and commit the transaction
	    }
	    catch(Exception e){
	    	try{
	    		if (tx != null) tx.rollback();  // rollback the transaction
	    	}catch(Exception x){
	    		throw new LoggingException(x);
	    	}
	    }
	    finally{
	    	try{
	    		session.close();
	    	}catch (Exception e){
	    		throw new LoggingException(e);
	    	}
	    }
	    
	}
	
	public static void main(String argv[]) {
		@SuppressWarnings("unused")
		WidgetRegistrationLogger testWRL = WidgetRegistrationLogger.getWRLInstance();
	}
	
}
