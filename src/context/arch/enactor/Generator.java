package context.arch.enactor;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.query.AbstractQueryItem;

/**
 * Generators are one-sided Enactors that do not take In any Widget. 
 * It only couples with an Out Widget to manipulate it through the subscription mechanism.
 * This class should be overridden to make use of the {@link #updateOutWidget(context.arch.widget.Widget.WidgetData)}
 * method to manipulate the Out Widget.
 * 
 * Though not strictly the best approach, but convenient, Generator subclasses Enactor because it shares many similarities, but has some differences.
 * Similarities: both
 * - Have Out Widgets
 * - Subscribe to their widgets
 * - Can manipulate widgets through the subscription mechanism
 * Differences: Generator
 * - Doesn't have In Widgets, and so no subscription query for that
 * - Uses the logical query item to specifically match with an Out Widget of interest (more specific than just the classname)
 *   - Whereas Enactors should typically apply the logical query on an In Widget
 * - Once it matches the Out Widget of interest, it gets its subscription details
 *   - and doesn't need to listen for queries anymore
 * - Can use an external process to set the outcomeValue, 
 *   - and should also set the value of the Out Widget
 * 
 * The main purpose of subclassing Enactors is to be able to have a subscription handle on the Out Widget.
 * 
 * @author Brian Y. Lim
 *
 */
public class Generator extends Enactor {
	
	public Generator(AbstractQueryItem<?,?> outWidgetSubscriptionQuery, String outcomeName, String shortId) {
		super(null, outWidgetSubscriptionQuery, outcomeName, shortId);
	}
	
	/**
	 * Overridden to prevent adding of references.
	 */
	@Override
	@Deprecated
	public void addReference(EnactorReference reference) {
		// do nothing
	}
	
	/**
	 * Overridden to do its simplified version of start(), 
	 * since even though it does not have any EnactorReferences, it needs to save widget descriptions.
	 */
	@Override
	public void startSubscriptionManager() throws EnactorException {
		super.startSubscriptionManager();

		// this was learned from the jumble in EnactorSubscriptionManager.init()
		for (ComponentDescription cd : subscriptionManager.sendDiscovererAttributeQuery(widgetSubscriptionQueries[OUT_WIDGET_INDEX])) {
			subscriptionManager.saveWidgetComponentDescriptions(cd, widgetSubscriptionQueries[OUT_WIDGET_INDEX]);
		}
	}

}
