package context.arch.enactor;

import context.arch.comm.DataObject;
import context.arch.storage.Attributes;

/**
 * An event multicaster, for thread-safe enactor event broadcasting.
 * 
 * @author alann
 */
class EnactorListenerMulticaster implements EnactorListener {

	private final EnactorListener a, b;
	
	EnactorListenerMulticaster(EnactorListener a, EnactorListener b) {
		this.a = a; this.b = b;
	}

  static EnactorListener add(EnactorListener a, EnactorListener b) {
    return addInternal(a, b);
  }

  static EnactorListener remove(EnactorListener l, EnactorListener oldl) {
    return removeInternal(l, oldl);
  }

	private EnactorListener remove(EnactorListener oldl) {
    if (oldl == a)  return b;
    if (oldl == b)  return a;
    EnactorListener a2 = removeInternal(a, oldl);
    EnactorListener b2 = removeInternal(b, oldl);
    if (a2 == a && b2 == b) {
      return this;	// it's not here
    }
    return addInternal(a2, b2);
  }

  private static EnactorListener addInternal(EnactorListener a, EnactorListener b) {
    if (a == null)  return b;
    if (b == null)  return a;
    return new EnactorListenerMulticaster(a, b);
  }

  private static EnactorListener removeInternal(EnactorListener l, EnactorListener oldl) {
    if (l == oldl || l == null) {
      return null;
    } else if (l instanceof EnactorListenerMulticaster) {
      return ((EnactorListenerMulticaster)l).remove(oldl);
    } else {
      return l;   // it's not here
    }
  }
  
  public void componentEvaluated(EnactorComponentInfo eci) {
//	  System.out.println("EnactorListenerMulticaster componentEvaluated " + a + " | " + b);
    a.componentEvaluated(eci);
    b.componentEvaluated(eci);
  }

  public void componentAdded(EnactorComponentInfo eci, Attributes paramAtts) {
    a.componentAdded(eci, paramAtts);
    b.componentAdded(eci, paramAtts);
  }

  public void componentRemoved(EnactorComponentInfo eci, Attributes paramAtts) {
    a.componentRemoved(eci, paramAtts);
    b.componentRemoved(eci, paramAtts);
  }

  public void parameterValueChanged(EnactorParameter parameter, Attributes validAtts, Object value) {
    a.parameterValueChanged(parameter, validAtts, value);
    b.parameterValueChanged(parameter, validAtts, value);
  }

  public void serviceExecuted(EnactorComponentInfo eci, String serviceName, String functionName, Attributes input, DataObject returnDataObject) {
    a.serviceExecuted(eci, serviceName, functionName, input, returnDataObject);
    b.serviceExecuted(eci, serviceName, functionName, input, returnDataObject);
  }

}
