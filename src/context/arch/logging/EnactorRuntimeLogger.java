/*
 * Created on Apr 11, 2004
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package context.arch.logging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

import context.arch.discoverer.ComponentDescription;
import context.arch.enactor.EnactorComponentInfo;
import context.arch.enactor.EnactorParameter;
import context.arch.enactor.EnactorReference;
import context.arch.logging.hibernate.CAParamAttribute;
import context.arch.logging.hibernate.ComponentAdded;
import context.arch.logging.hibernate.ComponentEvaluated;
import context.arch.logging.hibernate.ERParameter;
import context.arch.logging.hibernate.ERReference;
import context.arch.logging.hibernate.EnactorRegistration;
import context.arch.logging.hibernate.PVCParamAttribute;
import context.arch.logging.hibernate.ParameterValueChanged;
import context.arch.logging.hibernate.SEInputAttribute;
import context.arch.logging.hibernate.ServiceExecution;
import context.arch.storage.Attribute;
import context.arch.storage.AttributeNameValue;
import context.arch.storage.Attributes;

/**
 * @author Marti Motoyama
 * @author Brian Y. Lim
 */
public class EnactorRuntimeLogger {
	private static EnactorRuntimeLogger ERLogger;

	protected EnactorRuntimeLogger() {
	}

	public static EnactorRuntimeLogger getEnactorRuntimeLogger() {
		if (ERLogger == null) {
			synchronized (EnactorRuntimeLogger.class) {
				if (ERLogger == null) {
					ERLogger = new EnactorRuntimeLogger();
					ERLogger.initialize();
				}
			}
		}
		return ERLogger;
	}

	private void initialize() {
	}

	public void insertComponentEvaluatedEntry(
		String in_enactorId,
		EnactorReference in_er,
		ComponentDescription in_cd)
		throws LoggingException {

		Session session = HibernateUtils.getNewSession();
		if (session == null) return;

		//locate the most recent enactorRegistration with enactor id = in_enactorId
		EnactorRegistration enactorRegistration = null;
		try{
			enactorRegistration = getMostRecentEnactorRegistration(in_enactorId);
		}catch(LoggingException e){
			throw new LoggingException(e);
		}
		
		//locate the most recent erReference with the desired description query
		//for enactors that match the input enactorId
		ERReference erReference = null;
		try{
			erReference = getMostRecentEnactorReference(in_er, in_enactorId);
		}catch(LoggingException e){
			throw new LoggingException(e);
		}
		
		//set up the entry to be added to the ComponentEvaluated table
		ComponentEvaluated componentEvaluatedEntry = new ComponentEvaluated();
		componentEvaluatedEntry.setComponentdescriptionid(in_cd.id);
		componentEvaluatedEntry.setEnactorRegistration(enactorRegistration);
		componentEvaluatedEntry.setERReference(erReference);
		componentEvaluatedEntry.setComponentevaluatedtime(new Date());
		
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			session.save(componentEvaluatedEntry);
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
	
	public void insertComponentAddedEntry(
			String in_enactorId,
			EnactorReference in_er,
			ComponentDescription in_cd,
			Attributes paramAtts)
			throws LoggingException {

		Session session = HibernateUtils.getNewSession();
		if (session == null) return;

		//locate the most recent enactorRegistration with enactor id = in_enactorId
		EnactorRegistration enactorRegistration = getMostRecentEnactorRegistration(in_enactorId);
		
		//locate the most recent erReference with the desired description query
		//for enactors that match the input enactorId
		ERReference erReference = getMostRecentEnactorReference(in_er, in_enactorId);
		
		//set up the entry to be added to the ComponentAdded table
		ComponentAdded componentAddedEntry = new ComponentAdded();
		componentAddedEntry.setComponentdescriptionid(in_cd.id);
		componentAddedEntry.setEnactorRegistration(enactorRegistration);
		componentAddedEntry.setERReference(erReference);
		componentAddedEntry.setComponentaddedtime(new Date());
		
		//set up the entries to be added to the CAParamAttributes table
		ArrayList<CAParamAttribute> caParamAttributesList = new ArrayList<CAParamAttribute>();
		if (paramAtts != null){
			for (Attribute<?> paramAttribute : paramAtts.values()) {
				CAParamAttribute caParamAttribute = new CAParamAttribute();
				caParamAttribute.setComponentAdded(componentAddedEntry);
				caParamAttribute.setAttributename(paramAttribute.getName());
				caParamAttribute.setAttributetype(paramAttribute.getType());
				
				if (paramAttribute instanceof AttributeNameValue<?>) {
					AttributeNameValue<?> paramAttributeNameValue = (AttributeNameValue<?>) paramAttribute;
					if (paramAttributeNameValue.getType().isInstance(Number.class)) {
						Float f = new Float(Float.parseFloat(paramAttributeNameValue.getValue().toString()));
						caParamAttribute.setAttributevaluenumeric(f);
					}
					else if (paramAttributeNameValue.getType().equals(String.class)) {
						caParamAttribute.setAttributevaluestring(paramAttributeNameValue.getValue().toString());
					}
				}
				caParamAttributesList.add(caParamAttribute);	
			}
		}

		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			session.save(componentAddedEntry);
			
			for (int i = 0; i < caParamAttributesList.size(); i++){
				session.save(caParamAttributesList.get(i));
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
	
	public void insertParameterValueChangedEntry(
			String in_enactorId,
			EnactorParameter in_ep, 
			Attributes paramAtts, 
			Object value)
			throws LoggingException {

		Session session = HibernateUtils.getNewSession();
		if (session == null) return;

		//locate the most recent enactorRegistration with enactor id = in_enactorId
		EnactorRegistration enactorRegistration = null;
		try{
			enactorRegistration = getMostRecentEnactorRegistration(in_enactorId);
		}catch(LoggingException e){
			throw new LoggingException(e);
		}
		
		//locate the most ERParameter that matches the input EnactorParameter
		ERParameter erParameter = null;
		try{
			//locate the most recent erParameter with the desired name
			//for enactors that match the input enactorId
			erParameter = getMostRecentEnactorParameter(in_ep, in_enactorId);
		}catch(LoggingException e){
			throw new LoggingException(e);
		}
		
		//set up the entry to be added to the ParameterValueChanged table
		ParameterValueChanged parameterValueChangedEntry = new ParameterValueChanged();
		parameterValueChangedEntry.setEnactorRegistration(enactorRegistration);
		parameterValueChangedEntry.setERParameter(erParameter);
		parameterValueChangedEntry.setParametervaluechangedtime(new Date());
		
		//Determine whether the object is of type string or numeric.
		//All values begin as strings (?), so we must determine whether or not
		//the value is a float in string form.
		if (value instanceof String){
			String valueString = (String) value;
			String numericRegEx = "[0-9]+(.[0-9]*)?";
			if (valueString.matches(numericRegEx)){
				Float f = Float.valueOf(valueString);
				parameterValueChangedEntry.setParametervaluenumeric(f);
			}
			else{
				parameterValueChangedEntry.setParametervaluestring(value.toString());
			}			
		}
		
		//set up the entries to be added to the PVCParamAttributes table
		ArrayList<PVCParamAttribute> pvcParamAttributesList = new ArrayList<PVCParamAttribute>();
		if (paramAtts != null){
			for (Attribute<?> paramAttribute : paramAtts.values()) {
				PVCParamAttribute pvcParamAttribute = new PVCParamAttribute();
				pvcParamAttribute.setParameterValueChanged(parameterValueChangedEntry);
				pvcParamAttribute.setAttributename(paramAttribute.getName());
				pvcParamAttribute.setAttributetype(paramAttribute.getType());
				
				if (paramAttribute instanceof AttributeNameValue<?>) {
					AttributeNameValue<?> paramAttributeNameValue = (AttributeNameValue<?>) paramAttribute;
					if (paramAttributeNameValue.getType().isInstance(Number.class)) {
						Float f = Float.valueOf(paramAttributeNameValue.getValue().toString());
						pvcParamAttribute.setAttributevaluenumeric(f);
					}
					else if (paramAttributeNameValue.getType().equals(String.class)) {
						pvcParamAttribute.setAttributevaluestring(paramAttributeNameValue.getValue().toString());
					}
				}
				pvcParamAttributesList.add(pvcParamAttribute);	
			}
		}

		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			session.save(parameterValueChangedEntry);
			
			for (int i = 0; i < pvcParamAttributesList.size(); i++){
				session.save(pvcParamAttributesList.get(i));
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

	public void insertServiceExecutionEntry(
			String in_enactorId,
			EnactorComponentInfo in_eci,
			String in_serviceName, 
			String in_functionName, 
			Attributes in_input)
	throws LoggingException {

		Session session = HibernateUtils.getNewSession();
		if (session == null) return;

		//locate the most recent enactorRegistration with the desired enactor id
		EnactorRegistration enactorRegistration = null;
		try{
			enactorRegistration = getMostRecentEnactorRegistration(in_enactorId);
		}catch(LoggingException e){
			throw new LoggingException(e);
		}
		
		//locate the most recent componentAdded entry associated with the component
		//we are executing the service upon
		ComponentAdded componentAdded = null;
		try{
			componentAdded = getMostRecentComponentAdded(in_enactorId, in_eci.getComponentDescription().id, in_eci.getReference());
		}catch(LoggingException e){
			throw new LoggingException(e);
		}
		
		//set up the entry to be added to the ServiceExecution table
		ServiceExecution serviceExecutionEntry = new ServiceExecution();
		serviceExecutionEntry.setEnactorRegistration(enactorRegistration);
		serviceExecutionEntry.setComponentAdded(componentAdded);
		serviceExecutionEntry.setServicename(in_serviceName);
		serviceExecutionEntry.setFunctionname(in_functionName);
		serviceExecutionEntry.setExecutiontime(new Date());
	
		//set up the entries to be added to the SEInputAttributes table
		ArrayList<SEInputAttribute> seInputAttributeList = new ArrayList<SEInputAttribute>();
		if (in_input != null){
			for (Attribute<?> inputAttribute : in_input.values()) {
				SEInputAttribute seInputAttribute = new SEInputAttribute();
				seInputAttribute.setServiceExecution(serviceExecutionEntry);
				seInputAttribute.setAttributename(inputAttribute.getName());
				seInputAttribute.setAttributetype(inputAttribute.getType());
				
				if (inputAttribute instanceof AttributeNameValue<?>){
					AttributeNameValue<?> inputAttributeNameValue = (AttributeNameValue<?>) inputAttribute;
					if (inputAttributeNameValue.getType().isInstance(Number.class)) {
						Float f = Float.valueOf(inputAttributeNameValue.getValue().toString());
						seInputAttribute.setAttributevaluenumeric(f);
					}
					else if (inputAttributeNameValue.getType().equals(String.class)) {
						seInputAttribute.setAttributevaluestring(inputAttributeNameValue.getValue().toString());
					}
				}
				seInputAttributeList.add(seInputAttribute);	
			}
		}
		
		
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			session.save(serviceExecutionEntry);
			
			for (int i = 0; i < seInputAttributeList.size(); i++){
				session.save(seInputAttributeList.get(i));
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
	
	@SuppressWarnings("unchecked")
	public ComponentAdded getMostRecentComponentAdded(
			String in_enactorId,
			String in_componentDescriptionId,
			EnactorReference in_erReference)
	throws LoggingException{
		
		Session session = HibernateUtils.getNewSession();
		if (session == null) return null;
	
		try {
			String query = 
				"SELECT comAdd1 " + 
				"FROM ComponentAdded comAdd1, ERReference erRef1 " +
				"WHERE comAdd1.EnactorRegistration.enactorregistrationid = erRef1.EnactorRegistration.enactorregistrationid " +
				"AND erRef1.descriptionquery = ? " + 
				"AND comAdd1.EnactorRegistration.enactorid = ? " +
				"AND comAdd1.componentdescriptionid = ? " +
				"AND comAdd1.componentaddedtime = ( " +
					"SELECT max(comAdd2.componentaddedtime) " +
					"FROM ComponentAdded comAdd2, ERReference erRef2 " +
					"WHERE comAdd2.EnactorRegistration.enactorregistrationid = erRef2.EnactorRegistration.enactorregistrationid " +
					"AND erRef2.descriptionquery = ? " +
					"AND comAdd2.EnactorRegistration.enactorid = ? " +
					"AND comAdd2.componentdescriptionid = ? " + ")";
			
			List<ComponentAdded> list = session.createQuery(query).setParameters(
					// TODO: should adapt to include inWidgetSubscriptionQuery
					// currently associated with outWidgetSubscriptionQuery because both Enactor and Generator have them
					new Object[]{	in_erReference.getEnactor().getOutWidgetSubscriptionQuery().toString(), in_enactorId, in_componentDescriptionId, 
									in_erReference.getEnactor().getOutWidgetSubscriptionQuery().toString(), in_enactorId, in_componentDescriptionId}, 
					new Type[] {HibernateUtils.STRING, HibernateUtils.STRING, HibernateUtils.STRING,
								HibernateUtils.STRING, HibernateUtils.STRING, HibernateUtils.STRING}).list();
			
			if (list.size() == 0) {
				throw new LoggingException("getMostRecentComponentAdded() returned null list");
			}
			
			return list.get(0);	
		} catch (HibernateException e) {
			throw new LoggingException(e);
		}
		finally{
			try{
				session.close();
			}catch (Exception e){
				throw new LoggingException(e);
			}
		}
	}
	
	public EnactorRegistration getMostRecentEnactorRegistration(String in_enactorId) throws LoggingException{
		Session session = HibernateUtils.getNewSession();
		if (session == null) return null;
		
		try {
			String query = 
				"SELECT enReg1 " + 
				"FROM EnactorRegistration enReg1 " +
				"WHERE enReg1.enactorid = ? " +
				"AND enReg1.registrationtime = ( " +
					"SELECT max(enReg2.registrationtime) " +
					"FROM EnactorRegistration enReg2 " +
					"WHERE enReg2.enactorid = enReg1.enactorid ) ";

//			List list = session.createQuery(query).setParameters(
//					new Object[] {in_enactorId}, 
//					new Type[] {HibernateUtils.STRING}).list();
			EnactorRegistration result = (EnactorRegistration) session.createQuery(query).setParameters(
					new Object[] {in_enactorId}, 
					new Type[] {HibernateUtils.STRING}).uniqueResult();
			
			if (result == null) {
				System.out.println("query: " + query);
				System.out.println("enReg1.enactorid: " + in_enactorId);
				
				query = "SELECT COUNT(*) FROM EnactorRegistration";
				System.out.println(session.createQuery(query).list());
				
				throw new LoggingException("getMostRecentEnactorRegistration() returned null list");
//				System.exit(-1);
				// TODO: this gets called even before the entry can be added! So obviously, it's not there yet!
			}
			
//			return (EnactorRegistration) list.get(0);	
			return result;
			
		} catch (HibernateException e) {
			throw new LoggingException(e);
		}
		finally{
			try{
				session.close();
			}catch (Exception e){
				throw new LoggingException(e);
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public ERParameter getMostRecentEnactorParameter(EnactorParameter in_ep, String in_enactorId) throws LoggingException {		
		Session session = HibernateUtils.getNewSession();
		if (session == null) return null;
		
		try {
			String query = 
				"SELECT erPar1 " + 
				"FROM ERParameter erPar1 " +
				"WHERE erPar1.parametername = ? " + 
				"AND erPar1.EnactorRegistration.enactorid = ? " +
				"AND erPar1.EnactorRegistration.registrationtime = ( " +
					"SELECT max(erPar2.EnactorRegistration.registrationtime) " +
					"FROM ERParameter erPar2 " +
					"WHERE erPar2.EnactorRegistration.enactorid = ? " +
					"AND erPar2.parametername = ? )";

			List<ERParameter> list = session.createQuery(query).setParameters( 
					new Object[]{in_ep.getName(), in_enactorId,in_enactorId, in_ep.getName()}, 
					new Type[] {HibernateUtils.STRING, HibernateUtils.STRING, HibernateUtils.STRING, HibernateUtils.STRING}).list();
			
			if (list.size() == 0) 
				throw new LoggingException("getMostRecentEnactorParameter() returned null list");
			
			return list.get(0);	
		} catch (HibernateException e) {
			throw new LoggingException(e);
		}
		finally{
			try{
				session.close();
			}catch (Exception e){
				throw new LoggingException(e);
			}
		}
	}
	
	public ERReference getMostRecentEnactorReference(
		EnactorReference in_er,
		String in_enactorId)
		throws LoggingException{

		Session session = HibernateUtils.getNewSession();
		if (session == null) return null;
		
		try {
			String query = 
				"SELECT erRef1 " + 
				"FROM ERReference erRef1 " +
				"WHERE erRef1.descriptionquery = ? " + // TODO not really known as descriptionquery anymore, but should be known as rulequery
				"AND erRef1.EnactorRegistration.enactorid = ? " +
				"AND erRef1.EnactorRegistration.registrationtime = ( " +
					"SELECT max(erRef2.EnactorRegistration.registrationtime) " +
					"FROM ERReference erRef2 " +
					"WHERE erRef2.EnactorRegistration.enactorid = erRef1.EnactorRegistration.enactorid " +
					"AND erRef2.descriptionquery = erRef1.descriptionquery )";

			ERReference result = (ERReference) session.createQuery(query).setParameters( 
					new Object[]{
//							in_er.getEnactor().getDescriptionQuery().toString(), // TODO: need to fix to also store this, but at the enactor level
							in_er.getConditionQuery().toString(), // TODO currently it is the description query being stored...
							in_enactorId },
					new Type[] {
							HibernateUtils.STRING, 
							HibernateUtils.STRING }
//					).uniqueResult(); // this would throw "org.hibernate.NonUniqueResultException: query did not return a unique result: 2" 
					).list().get(0);
			
			if (result == null) {
				System.out.println("query: " + query);
				System.out.println("in_er.getRuleQuery(): " + in_er.getConditionQuery());
				System.out.println("in_enactorId: " + in_enactorId);
				
				query = "SELECT COUNT(*) FROM ERReference";
				
				throw new LoggingException("getMostRecentEnactorReference() returned null list");
			}
			
			return result;	
		} catch (HibernateException e) {
			throw new LoggingException(e);
		}
		finally{
			try{
				session.close();
			}catch (Exception e){
				throw new LoggingException(e);
			}
		}
	}

}
