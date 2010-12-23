/*
 * Created on Mar 9, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package context.arch.enactor;

import java.util.ArrayList;
import java.util.List;

import context.arch.storage.Attributes;

/**
 * Defines a parameter for a rule. A parameter has a name, optional description, and
 * a value or set of values. Each parameter value in a set of values will be bound
 * by the enactor with a set of attributes depending upon current widget values.
 * 
 * For instance, a parameter signifying a movement level after which to turn on room lights
 * may have an attribute that specifies room name. The parameter can then maintain a set of 
 * movement level values scoped to that room name attribute.
 * 
 * Currently, parameters or enactors do no management of multiple values themselves, but
 * applications can maintain various sets of attributes and let listeners know about them.
 * This may change in the future with increased system support for parameter sets.
 * 
 * @author newbergr
 */
public class EnactorParameter {
	
	protected Enactor enactor;
	protected String description;
	protected String name;
	protected Attributes attsTemplate;
//	protected ArrayList values = new ArrayList(); // this is terrible and should have been a type safe Map or Hashtable instead of a List of Object pairs --Brian
//	protected Map<Attributes, Object> values = new LinkedHashMap<Attributes, Object>(); // preserves insertion order ; // actually, this is inconvenient to obtain by index too --Brian
	protected List<Attributes> attributes = new ArrayList<Attributes>(); // couple two lists together instead;
	protected List<Object> values = new ArrayList<Object>();

	/**
	 * constructs a parameter with a given name, and no base attributes.
	 * 
	 * @param nm the name of this parameter
	 */
	public EnactorParameter(String nm) {
		this(nm, new Attributes());
	}

	/**
	 * constructs a parameter with a name and a base set of attributes.
	 * 
	 * @param nm
	 * @param template
	 */
	public EnactorParameter(String nm, Attributes template) {
		name = nm;
		attsTemplate = template;
	}

	public void setEnactor(Enactor r) {
		enactor = r;
	}

	public Enactor getEnactor() {
		return enactor;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * sets the description, a sentence that describes the
	 * function of this parameter.
	 * 
	 * @param description The description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * returns the base attribute template for this parameter. only
	 * attributes contained in the template can be set on the parameter.
	 *  
	 * @return the attribute template
	 */
	public Attributes getAttributesTemplate() {
		return attsTemplate;
	}

	/**
	 * set a value for the parameter with the given attributes. only
	 * attributes that are contained in the attribute template are stored, and 
	 * the value is stored along with the attribute.
	 * 
	 * TODO: overwrite values with identical attributes
	 * 
	 * @param atts
	 * @param value
	 */
	public void setValue(Attributes atts, Object value) {
		Attributes validAtts = atts.getSubset(attsTemplate);
		if (!validAtts.isEmpty()) {
			attributes.add(validAtts);
			values.add(value);
			enactor.fireParameterValueChanged(this, validAtts, value);
		}
	}

	public int getNumValues() {
		return values.size();
	}

	public Attributes getAttributesAt(int i) {
//		return (Attributes) ((Object[]) values.get(i))[0];
		return attributes.get(i);
	}

	public Object getValueAt(int i) {
//		return (Attributes) ((Object[]) values.get(i))[1];
		return values.get(i);
	}
}
