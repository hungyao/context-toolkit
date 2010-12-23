package context.arch.util;

import context.arch.comm.DataObject;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This class maintains a list of ContextUser objects.
 *
 * @see context.arch.util.ContextUser
 */
public class ContextUsers extends Hashtable<String, ContextUser> {

	private static final long serialVersionUID = -4451219100108689011L;

	/**
	 * Tag for version
	 */
	public static final String VERSION = "VERSION";

	/**
	 * Tag for context users
	 */
	public static final String CONTEXT_USERS = "CONTEXTUSERS";

	/**
	 * Tag for context user
	 */
	public static final String CONTEXT_USER = "CONTEXTUSER";

	/**
	 * Tag for description
	 */
	public static final String DESCRIPTION = "description";

	/**
	 * Tag for user's name
	 */
	public static final String REALNAME = "username";

	/**
	 * Tag for user's email address
	 */
	public static final String EMAIL = "email";

	/**
	 * Tag for user's organization
	 */
	public static final String ORGANIZATION = "organization";

	/**
	 * Tag for user's iButton id
	 */
	public static final String IBUTTONID = "iButtonID";

	private String version = null;

	/**
	 * Basic empty constructor
	 */
	public ContextUsers() {
		super();
	}

	/**
	 * Constructor that creates a ContextUser object from a 
	 * DataObject.
	 *
	 * @param data DataObject containing context user information
	 */
	public ContextUsers(DataObject data) {
		super();
		DataObject cu = data.getDataObject(CONTEXT_USERS);
		Vector<DataObject> v = cu.getChildren();
		for (int i=0; i<v.size(); i++) {
			DataObject d = (DataObject)v.elementAt(i);
			if (d.getName().equals(Constants.VERSION)) {
				version = (String)d.getDataObjectFirstValue(VERSION);
			}
			else if (d.getName().equals(CONTEXT_USER)) {
				String description = (String)d.getDataObjectFirstValue(DESCRIPTION);
				String name = (String)d.getDataObjectFirstValue(REALNAME);
				String email = (String)d.getDataObjectFirstValue(EMAIL);
				String organization = (String)d.getDataObjectFirstValue(ORGANIZATION);
				String ibuttonid = (String)d.getDataObjectFirstValue(IBUTTONID);
				addContextUser(new ContextUser(description,name,email,organization,ibuttonid));
			}
		}
	}

	/**
	 * Adds a context user to the list
	 * 
	 * @param user Context user to add
	 */
	public synchronized void addContextUser(ContextUser user) {
		put(user.getDescription(), user);
	}

	/**
	 * Retrieves a context user from the list
	 * 
	 * @param description Description to use to retrieve the context user
	 * @return ContextUser object with the corresponding description
	 */
	public synchronized ContextUser getContextUser(String description) {
		return (ContextUser)get(description);
	}

	/**
	 * Returns an enumeration of the context users
	 *
	 * @return Enumeration of the context users
	 */
	public synchronized Enumeration<ContextUser> getEnumeration() {
		return elements();
	}

	/**
	 * Returns the version number
	 *
	 * @return version number for this list
	 */
	public String getVersion() {
		return version;
	}

}
