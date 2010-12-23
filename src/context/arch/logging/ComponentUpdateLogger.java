/*
 * Created on Feb 21, 2004
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package context.arch.logging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import context.arch.discoverer.ComponentDescription;
import context.arch.logging.hibernate.CUAttribute;
import context.arch.logging.hibernate.CUDestination;
import context.arch.logging.hibernate.ComponentUpdate;
import context.arch.storage.Attribute;
import context.arch.storage.AttributeNameValue;

/**
 * @author Marti Motoyama
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ComponentUpdateLogger {
	private static ComponentUpdateLogger CULInstance;

	protected ComponentUpdateLogger() {
	}

	public static ComponentUpdateLogger getCULInstance() {
		if (CULInstance == null) {
			synchronized (ComponentUpdateLogger.class) {
				if (CULInstance == null) {
					CULInstance = new ComponentUpdateLogger();
					CULInstance.initialize();
				}
			}
		}
		return CULInstance;
	}

	private void initialize() {
	}

	public void insertComponentUpdateEntry(
		String in_componentId,
		String in_updateName,
		List<ComponentDescription> in_componentDescriptions)
		throws LoggingException {

		Session session = null;
		session = HibernateUtils.getNewSession();

		if (session == null)
			return;

		ArrayList<CUDestination> cuDestinationList = new ArrayList<CUDestination>();
		ArrayList<CUAttribute> cuAttributeList = new ArrayList<CUAttribute>();

		ComponentUpdate componentUpdateEntry = new ComponentUpdate();
		componentUpdateEntry.setComponentid(in_componentId);
		componentUpdateEntry.setUpdatename(in_updateName);
		componentUpdateEntry.setUpdatetime(new Date());

		for (ComponentDescription compDescr : in_componentDescriptions) {
			//Set up the cuDestination entry
			CUDestination cuDestinationEntry = new CUDestination();
			cuDestinationEntry.setComponentUpdate(componentUpdateEntry);
			cuDestinationEntry.setDestinationcomponentid(compDescr.id);
			cuDestinationEntry.setSuccess(new Boolean(true));
			cuDestinationList.add(cuDestinationEntry);

			AttributeNameValue<?> attributeNameValue;

			//Set up the constant attribute entries
			for (Attribute<?> attribute : compDescr.getConstantAttributes()) {
				CUAttribute cuAttributeEntry = new CUAttribute();
				cuAttributeEntry.setAttributename(attribute.getName());
				cuAttributeEntry.setAttributetype(attribute.getType());
				cuAttributeEntry.setConstant(true);
				cuAttributeEntry.setCUDestination(cuDestinationEntry);

				//AttributeNameValue is a subclass of Attribute
				if (attribute instanceof AttributeNameValue<?>) {
					//check the value associated with this AttributeNameValue
					attributeNameValue = (AttributeNameValue<?>) attribute;
					if (attributeNameValue.getType().equals(String.class)) {
						cuAttributeEntry.setAttributevaluestring((String) attributeNameValue.getValue());
					} 
					else if (attributeNameValue.getType().isInstance(Number.class)) {
						cuAttributeEntry.setAttributevaluenumeric(Float.valueOf(attributeNameValue.getValue().toString()));
					}
				}
				cuAttributeList.add(cuAttributeEntry);
			}

			//Set up the non constant attribute entries
			for (Attribute<?> attribute : compDescr.getNonConstantAttributes().values()) {
				CUAttribute cuAttributeEntry = new CUAttribute();
				cuAttributeEntry.setAttributename(attribute.getName());
				cuAttributeEntry.setAttributetype(attribute.getType());
				cuAttributeEntry.setConstant(false);
				cuAttributeEntry.setCUDestination(cuDestinationEntry);

				if (attribute instanceof AttributeNameValue<?>) {
					attributeNameValue = (AttributeNameValue<?>) attribute;
					if (attributeNameValue.getType().equals(String.class)) {
						cuAttributeEntry.setAttributevaluestring((String) attributeNameValue.getValue());
					} 
					else if (attributeNameValue.getType().isInstance(Number.class)) {
						cuAttributeEntry.setAttributevaluenumeric(Float.valueOf(attributeNameValue.getValue().toString()));
					}
				}
				cuAttributeList.add(cuAttributeEntry);
			}
		}

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(componentUpdateEntry);

			for (int i = 0; i < cuDestinationList.size(); i++) {
				session.save(cuDestinationList.get(i));
			}

			for (int i = 0; i < cuAttributeList.size(); i++) {
				session.save(cuAttributeList.get(i));
			}

			tx.commit(); // flush the Session and commit the transaction
		} catch (Exception e) {
			try {
				if (tx != null) tx.rollback(); // rollback the transaction
			} catch (Exception x) {
				throw new LoggingException(x);
			}
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				throw new LoggingException(e);
			}
		}

	}

}
