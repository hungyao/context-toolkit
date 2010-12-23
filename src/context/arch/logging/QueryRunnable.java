/*
 * Created on May 5, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package context.arch.logging;

import org.hibernate.type.Type;

/**
 * @author Marti Motoyama
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class QueryRunnable implements Runnable {
	private String query;
	private Object[] parameters;
	private Type[] types;
		
	public QueryRunnable (String query, Object[] parameters, Type[] types){
		this.query = query;
		this.parameters = parameters;
		this.types = types;
	}
	
	public String getQuery(){
		return query;
	}
	
	public Object[] getParameters(){
		if (parameters == null) parameters = new Object[]{};
		return parameters;
	}
	
	public Type[] getTypes(){
		if (types == null) types = new Type[]{};
		return types;
	}

}
