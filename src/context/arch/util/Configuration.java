package context.arch.util;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.storage.Attributes;
import context.arch.storage.AttributeNameValue;

/**
 * This class implements a subscriber object, encapsulating the information
 * needed to create a subscriber and send information to it.
 *
 * @see context.arch.subscriber.Subscribers
 */
public class Configuration {

	/**
	 * Tag for a version
	 */
	public static final String VERSION = "VERSION";

	/**
	 * Tag for a configuration
	 */
	public static final String CONFIGURATION = "CONFIGURATION";

	/**
	 * Tag for author
	 */
	public static final String AUTHOR = "AUTHOR";

	/**
	 * Tag for description
	 */
	public static final String DESCRIPTION = "DESCRIPTION";

	/**
	 * Tag for parameters
	 */
	public static final String PARAMETERS = "PARAMETERS";

	/**
	 * Tag for widgets
	 */
	public static final String WIDGETS = "WIDGETS";

	/**
	 * Tag for interpreters
	 */
	public static final String INTERPRETERS = "INTERPRETERS";

	/**
	 * Tag for servers
	 */
	public static final String SERVERS = "SERVERS";

	/**
	 * Tag for other components
	 */
	public static final String OTHER_COMPONENTS = "OTHER_COMPONENTS";

	private String version = null;
	private String description = null;
	private String author = null;
	private Attributes parameters = null;
	private ConfigObjects widgets = null;
	private ConfigObjects interpreters = null;
	private ConfigObjects servers = null;
	private ConfigObjects others = null;

	/**
	 * Basic constructor that creates a configuration object from a DataObject.
	 * The DataObject must contain a <CONFIGURATION> tag
	 *
	 * @param data DataObject containing the subscriber info
	 */
	public Configuration(DataObject data) {
		DataObject cfg = data.getDataObject(CONFIGURATION);
		if (cfg == null) {
			return;
		}

		version = (String)cfg.getDataObjectFirstValue(VERSION);
		description = (String)cfg.getDataObjectFirstValue(DESCRIPTION);
		author = (String)cfg.getDataObjectFirstValue(AUTHOR);

		DataObject paramsObj = cfg.getDataObject(PARAMETERS);
		if (paramsObj != null) {
			parameters = getParameters(paramsObj);
		}

		DataObject widgetsObj = cfg.getDataObject(WIDGETS);
		if (widgetsObj != null) {
			widgets = new ConfigObjects(widgetsObj);
		}

		DataObject interpretersObj = cfg.getDataObject(INTERPRETERS);
		if (interpretersObj != null) {
			interpreters = new ConfigObjects(interpretersObj);
		}

		DataObject serversObj = cfg.getDataObject(SERVERS);
		if (serversObj != null) {
			servers = new ConfigObjects(serversObj);
		}

		DataObject othersObj = cfg.getDataObject(OTHER_COMPONENTS);
		if (othersObj != null) {
			others = new ConfigObjects(othersObj);
		}
	}

	/**
	 * This private method takes the parameter information and returns
	 * an AttributeNameValues object containing the information.
	 *
	 * @param data DataObject containing parameter information
	 * @return AttributeNameValues object containing the parameter info
	 */
	private Attributes getParameters(DataObject data) {
		Attributes atts = new Attributes();
		DataObjects v = data.getChildren();
		for (int i = 0; i < v.size(); i++) {
			DataObject d = (DataObject)v.elementAt(i);
			atts.add(new AttributeNameValue<String>(d.getName(), d.getValue()));
		}
		return atts;
	}

	/**
	 * Returns the version number of this configuration
	 *
	 * @return the configuration version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version number of this configuration
	 *
	 * @param version Version of this configuration
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Returns the author of this configuration
	 *
	 * @return the configuration author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Sets the author of this configuration
	 *
	 * @param author Author of this configuration
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * Returns the description of this configuration
	 *
	 * @return the configuration description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of this configuration
	 *
	 * @param description Description of this configuration
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the parameters for this configuration
	 *
	 * @return the configuration parameters
	 */
	public Attributes getParameters() {
		return parameters;
	}

	/**
	 * Sets the parameters for this configuration
	 *
	 * @param parameters Parameters for this configuration
	 */
	public void setParameters(Attributes parameters) {
		this.parameters = parameters;
	}

	/**
	 * Returns the widget configurations for this configuration
	 *
	 * @return the widget configurations
	 */
	public ConfigObjects getWidgetConfigurations() {
		return widgets;
	}

	/**
	 * Sets the widget configurations for this configuration
	 *
	 * @param widgets Widget configurations for this configuration
	 */
	public void setWidgetConfigurations(ConfigObjects widgets) {
		this.widgets = widgets;
	}

	/**
	 * Returns the interpreter configurations for this configuration
	 *
	 * @return the interpreter configurations
	 */
	public ConfigObjects getInterpreterConfigurations() {
		return interpreters;
	}

	/**
	 * Sets the interpreter configurations for this configuration
	 *
	 * @param interpreters Interpreter configurations for this configuration
	 */
	public void setInterpreterConfigurations(ConfigObjects interpreters) {
		this.interpreters = interpreters;
	}

	/**
	 * Returns the server configurations for this configuration
	 *
	 * @return the server configurations
	 */
	public ConfigObjects getServerConfigurations() {
		return servers;
	}

	/**
	 * Sets the server configurations for this configuration
	 *
	 * @param servers Server configurations for this configuration
	 */
	public void setServerConfigurations(ConfigObjects servers) {
		this.servers = servers;
	}

	/**
	 * Returns the other component (not servers, widgets, or interpreters)
	 * configurations for this configuration
	 *
	 * @return the other component configurations
	 */
	public ConfigObjects getOtherConfigurations() {
		return others;
	}

	/**
	 * Sets the the other component (not servers, widgets, or interpreters)
	 * configurations for this configuration
	 *
	 * @param others Other configurations for this configuration
	 */
	public void setOtherConfigurations(ConfigObjects others) {
		this.others = others;
	}

}
