/*
 * Created on Feb 21, 2004
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package context.arch.logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;

import context.arch.enactor.EnactorParameter;
import context.arch.enactor.EnactorReference;
import context.arch.logging.hibernate.ERParameter;
import context.arch.logging.hibernate.ERReference;
import context.arch.logging.hibernate.EnactorRegistration;

/**
 * @author Marti Motoyama
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class EnactorRegistrationLogger {
	private static EnactorRegistrationLogger ERLogger;

	protected EnactorRegistrationLogger() {
	}

	public static EnactorRegistrationLogger getEnactorRegistrationLogger() {
		if (ERLogger == null) {
			synchronized (EnactorRegistrationLogger.class) {
				ERLogger = new EnactorRegistrationLogger();
				ERLogger.initialize();
			}
		}
		return ERLogger;
	}

	private void initialize() {
	}

	public void insertEnactorRegistrationEntry (
		String in_enactorId,
		Collection<EnactorParameter> in_enactorParameters,
		Collection<EnactorReference> in_enactorReferences)
		throws LoggingException {
		
		Session session = HibernateUtils.getNewSession();
		if (session == null) return;
		
		EnactorRegistration enactorRegistrationEntry = new EnactorRegistration();
		ArrayList<ERParameter> enactorParameters = new ArrayList<ERParameter>();
		ArrayList<ERReference> enactorReferences = new ArrayList<ERReference>();
		
		for (EnactorParameter ep : in_enactorParameters) {
			ERParameter erParameter = new ERParameter();
			erParameter.setEnactorRegistration(enactorRegistrationEntry);
			erParameter.setParametername(ep.getName());
			enactorParameters.add(erParameter);
		}
		
		for (EnactorReference er : in_enactorReferences) {
			ERReference erReference = new ERReference();
			erReference.setEnactorRegistration(enactorRegistrationEntry);	
//			erReference.setDescriptionquery(er.getEnactor().getDescriptionQuery().toString());
			erReference.setDescriptionquery(er.getConditionQuery().toString()); // TODO: need to fix to include both rulequery and desc query
			enactorReferences.add(erReference);
		}
		
		enactorRegistrationEntry.setEnactorid(in_enactorId);
		enactorRegistrationEntry.setRegistrationtime(new Date());
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			session.save(enactorRegistrationEntry);
			
			for (int i = 0; i < enactorParameters.size(); i++){
				session.save(enactorParameters.get(i));
			}
			for (int i = 0; i < enactorReferences.size(); i++){
				session.save(enactorReferences.get(i));
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

}
