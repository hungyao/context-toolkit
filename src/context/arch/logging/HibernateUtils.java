/*
 * Created on Mar 7, 2004
 *
 * $Id: HibernateUtils.java,v 1.8 2004/05/04 09:05:02 happihouse Exp $
 */
package context.arch.logging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.classic.Session;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import context.arch.logging.hibernate.*;

/**
 * Manages Hibernate package functionality.
 * 
 * TODO: need to have hibernate folder with various files in the project's directory --Brian
 * So if we are deriving a project from the Context Toolkit, we'll also need to have a hibernate folder.
 * Best to be able to toggle to disable this, and/or easily set one up and customize it.
 * 
 * @author alann
 * @author Brian Y. Lim
 */
public class HibernateUtils {

	/**
	 * Used to replace deprecated Hibernate.STRING 
	 * which does not have an implemented solution until Hibernate 3.6, which is not out yet (currently using 3.5)
	 */
	public static final StringType STRING = new StringType();

	private static SessionFactory sessionFactory;
	private static ArrayList<Session> sessionsToClose = new ArrayList<Session>();

	public static synchronized SessionFactory getSessionFactory() throws HibernateException {
		if (sessionFactory == null) {

			sessionFactory = new AnnotationConfiguration()
			
			// see http://java.dzone.com/articles/hibernate-3-annotations?utm_source=feedburner&utm_medium=feed&utm_campaign=Feed%3A+javalobby%2Ffrontpage+%28Javalobby+%2F+Java+Zone%29
			.addPackage("context.arch.logging.hibernate")
			.addAnnotatedClass(ComponentSubscription.class)
			.addAnnotatedClass(ComponentUpdate.class)
			.addAnnotatedClass(CUAttribute.class)
			.addAnnotatedClass(CUDestination.class)
			.addAnnotatedClass(WidgetRegistration.class)
			.addAnnotatedClass(WRAttribute.class)
			.addAnnotatedClass(WRCallback.class)
			.addAnnotatedClass(WRService.class)
			.addAnnotatedClass(WRServiceFunction.class)
			.addAnnotatedClass(EnactorRegistration.class)
			.addAnnotatedClass(ERParameter.class)
			.addAnnotatedClass(ERReference.class)
			.addAnnotatedClass(ComponentEvaluated.class)
			.addAnnotatedClass(ComponentAdded.class)
			.addAnnotatedClass(CAParamAttribute.class)
			.addAnnotatedClass(ParameterValueChanged.class)
			.addAnnotatedClass(PVCParamAttribute.class)
			.addAnnotatedClass(ServiceExecution.class)
			.addAnnotatedClass(SEInputAttribute.class)

			.buildSessionFactory();

		}
		return sessionFactory;
	}

	/**
	 * returns a new Session based on the singleton Session Factory.
	 * 
	 * @return a new session
	 */
	public static Session getNewSession() { //throws LoggingException {
		try {
			return getSessionFactory().openSession();
		} catch (HibernateException e) {
//			throw new LoggingException(e);
			return null;
		}
	}

	/**
	 *
	 * @return a new session that will be added to a list of Sessions to close
	 * @throws HibernateException
	 */
	public static Session getNewSessionToClose() throws HibernateException {
		Session s = getSessionFactory().openSession();
		synchronized(sessionsToClose){
			sessionsToClose.add(s);
		}
		return s;
	}

	/**
	 * closes all sessions in the static sessionsToClose array list
	 * 
	 * @throws HibernateException
	 */
	public static void closeSessions() throws HibernateException {
		synchronized(sessionsToClose){
			Iterator<Session> i = sessionsToClose.iterator();
			while (i.hasNext()){
				Session s = i.next();
				s.close();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
//		SessionFactory sf = getSessionFactory();
//
//		Session session = sf.openSession();
//
////		List updates = session.find("from ComponentUpdate"); // actually deprecated;
////		List updates = session.createQuery("from ComponentUpdate").list(); // some case sensitivity loss is happening!
//		List<ComponentUpdate> updates = session.createQuery("from ComponentUpdate").list();
//		System.out.println("Start");
//		for (ComponentUpdate cu : updates) {
//			System.out.println(cu.getComponentid());
//
//			for (CUDestination cud : cu.getCUDestinations()) {
////				System.out.println("\t" + cud.getComp_id().getDestinationcomponentid());
//
//				for (@SuppressWarnings("unused") CUAttribute cua : cud.getCUAttributes()) {
////					System.out.println("\t\t" + cua.getComp_id().getAttributename() + " : " + cua.getAttributevaluestring() + " : " + cua.getAttributevaluenumeric());
//				}
//			}
//		}
//
//		session.close();
//		System.out.println("End");
		
		

		Session session = null;
		try {
			session = HibernateUtils.getNewSession();
		} catch (HibernateException e) {
			throw new LoggingException(e);
		}
		if (session == null) return;
		
		try {
			String in_enactorId = "edu.cmu.laksa.context.enactors.LocationEnactor_BrianHP_64234";
			
			String query = 
				"SELECT enReg1 " + 
				"FROM EnactorRegistration enReg1 " +
				"WHERE enReg1.enactorid = ? " +
				"AND enReg1.registrationtime = ( " +
					"SELECT max(enReg2.registrationtime) " +
					"FROM EnactorRegistration enReg2 " +
					"WHERE enReg2.enactorid = enReg1.enactorid ) ";

			List<EnactorRegistration> list = session.createQuery(query).setParameters(
					new Object[]{in_enactorId},
					new Type[] {HibernateUtils.STRING}).list();
			
			if (list.size() == 0) {
				System.out.println("query: " + query);
				throw new LoggingException("getMostRecentEnactorRegistration() returned null list");
			}
			
			System.out.println( list.get(0) );	
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
