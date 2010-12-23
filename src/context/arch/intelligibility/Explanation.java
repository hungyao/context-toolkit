package context.arch.intelligibility;

import java.io.Serializable;

import context.arch.intelligibility.expression.DNF;
import context.arch.intelligibility.query.Query;


public class Explanation implements Serializable {
	
	private static final long serialVersionUID = 7154411340728402254L;
	
	protected Query query;
	protected DNF content;
	
	/**
	 * 
	 * @param type
	 * @param query
	 * @param content
	 * @param timestamp of query?
	 */
	public Explanation(Query query, DNF content) {
		this.query = query;
		this.content = content;
	}

	public Query getQuery() {
		return query;
	}

	public DNF getContent() {
		return content;
	}
	
	@Override
	public String toString() {
		return query + " -> " + content;
	}

}
