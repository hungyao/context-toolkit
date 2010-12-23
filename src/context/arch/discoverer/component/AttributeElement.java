package context.arch.discoverer.component;

import context.arch.comm.DataObject;
import context.arch.discoverer.query.comparison.AbstractComparison;
import context.arch.discoverer.query.comparison.AttributeComparison;
import context.arch.storage.AttributeNameValue;

/**
 * 
 * 
 * @author newbergr
 * @author Brian Y. Lim
 */
@SuppressWarnings("unchecked")
public abstract class AttributeElement extends AbstractCollectionElement<AttributeNameValue<?>,AttributeNameValue<?>> {

	// some hack to bet a class of AttributeNameValue<?> with the <?>
	public static Class<AttributeNameValue<?>> attributeClass = (Class<AttributeNameValue<?>>) AttributeNameValue.instance("class", String.class).getClass();
	
	public AttributeElement(String elementName) {
		super(elementName, 
				attributeClass, attributeClass);
	}

	public AttributeElement(String elementName, AttributeNameValue<?> attribute) {
		super(elementName, 
				attributeClass, attributeClass,
				attribute);
	}
	
	@Override
	public AbstractComparison<AttributeNameValue<?>,AttributeNameValue<?>> getDefaultComparison() {
		return new AttributeComparison(AttributeComparison.Comparison.EQUAL);
	}
	
	/**
	 * Overridden to use toValueCodex() of AttributeNameValue.
	 */
	@Override
	public String getValueCodex() {
		new RuntimeException("" + getValue()).printStackTrace();
		return getValue().toValueCodex();
	}
	
	@Override
	public DataObject getValueDataObject() {
		return getValue().toDataObject();
	}

}
