package context.arch.discoverer.component;

import java.util.Collection;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.query.comparison.AbstractComparison;

/**
 * Description Element to encapsulate two Widget attributes so that they can be used together to calculate a rule.
 * Useful for cases such as calculating distance based on latitude and longitude.
 * 
 * TODO: class not ready for use
 * 
 * @author Brian Y. Lim
 *
 */
public abstract class Attribute2DElement<C1,C2> extends AbstractCollectionElement<C1,C2> {

	public static final String _2D_ATT_ELEMENT = "2Datt";

	protected Attribute2DElement(Class<C1> c1, Class<C2> c2) {
		super(_2D_ATT_ELEMENT, 
				c1, c2);
	}

	@Override
	public Collection<C1> extractElement(ComponentDescription component) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean processQueryItem(ComponentDescription componentDescription,
			AbstractComparison<C1,C2> comparison) {
		// TODO Auto-generated method stub
		return false;
	}

}
