package context.arch.intelligibility.query;

/**
 * Listens to when a query is invoked, so that it can react to it.
 * @author Brian Y. Lim
 */
public interface QueryListener {
	
	public void queryInvoked(Query query);

}
