package context.arch.comm;

import java.util.Vector;

/**
 * Use this instead of Vector<DataObject> which occurs throughout the code base.
 * It has more features than just Vector.
 * 
 * TODO: replace with ConcurrentHashMap?
 * 
 * @author Brian Y. Lim
 *
 */
public class DataObjects extends Vector<DataObject> {
	
	private static final long serialVersionUID = -8497099181973402788L;
	
	private DataObject parent;
	
	public DataObjects() {
		super();
	}
	
	public DataObjects(DataObject parent) {
		super();
		this.parent = parent;
	}
	
	public void setParent(DataObject parent) {
		this.parent = parent;
	}
	
	public DataObject getParent() {
		return parent;
	}

}
