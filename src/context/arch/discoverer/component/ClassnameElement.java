/*
 * ClassnameElement.java
 *
 * Created on July 6, 2001, 8:39 AM
 */

package context.arch.discoverer.component;

import context.arch.discoverer.ComponentDescription;

/**
 *
 * @author Agathe
 * @author Brian Y. Lim
 */
public class ClassnameElement extends AbstractValueElement<String> {

	public ClassnameElement () {
		super(ComponentDescription.CLASSNAME_ELEMENT,
				String.class);
	}
	
	/**
	 * 
	 * @param classForName uses the fully qualified name (with package name) of this class
	 */
	public ClassnameElement(Class<?> classForName) {
		this();
		setValue(classForName.getName());
	}

	public ClassnameElement(String className) {
		this();
		setValue(className);
	}

	@Override
	public String extractElement(ComponentDescription component) {
		return component.classname;
	}

}
