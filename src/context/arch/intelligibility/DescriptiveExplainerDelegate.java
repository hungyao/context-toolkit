package context.arch.intelligibility;

import java.util.HashMap;
import java.util.Map;

import context.arch.intelligibility.expression.Parameter;


/**
 * Explainers can delegate to this class to provide descriptive explanations (e.g. definitions, units, rationale, pretty name).
 * Internally, these explanations are stored in look-up tables, specifically, maps.
 * Subclasses are expected to pre-populate these maps.
 * Subclasses can look up external sources like text files, databases, web services (e.g. Google Spreadsheets). 
 * @author Brian Y. Lim
 *
 */
public class DescriptiveExplainerDelegate {

	protected Map<String, String> prettyNames;
	protected Map<String, String> reversePrettyNames;
	protected Map<String, Map<Object, String>> prettyValues;
	protected Map<String, String> units;
	protected Map<String, String> definitions;
	protected Map<String, String> rationales;
	
	public DescriptiveExplainerDelegate() {
		prettyNames = new HashMap<String, String>(); // <context, prettyName>
		reversePrettyNames = new HashMap<String, String>(); // <prettyName, context>
		units = new HashMap<String, String>(); // <context, unit>
		definitions = new HashMap<String, String>(); // <context, definition>
		rationales = new HashMap<String, String>(); // <context, rationale>
		
		prettyValues = new HashMap<String, Map<Object, String>>();
	}

	/**
	 * Get the pretty form of a context name.
	 * @param context
	 * @return self if no pretty name found
	 */
	public String getPrettyName(String context) {
		String explanation = prettyNames.get(context);
		return explanation != null ? explanation : context;
	}
	
	/**
	 * Get the pretty form of a context value.
	 * @param context
	 * @param value
	 * @return
	 */
	public String getPrettyValue(String context, Object value) {
		Map<Object, String> map = prettyValues.get(context);
		if (map != null) { 
			String pretty = map.get(value);
			if (pretty != null) {
				return pretty;
			}
		}
		if (value != null) {
			return value.toString();
		}
		return null;
	}
	
	/**
	 * Reverse mapping of pretty name to context.
	 * @param prettyName
	 * @return
	 */
	public String prettyNameToContext(String prettyName) {
		String context = reversePrettyNames.get(prettyName);
		return context;
	}

	/**
	 * Get the unit of a context (e.g. kg, cm, &deg;C, number of windows).
	 * @param context
	 * @return empty if no unit found
	 */
	public String getUnit(String context) {
		String explanation = units.get(context);
		return explanation != null ? explanation : "";
	}
	
	/**
	 * Get the definition of a context. 
	 * @param context
	 * @return context if no definition found
	 */
	public String getDefinition(String context) {
		String explanation = definitions.get(context);
		return explanation != null ? explanation : context;
	}

	/**
	 * Get the rationale for a context, i.e. why it is important and/or
	 * the implication of its various values.
	 * @param context
	 * @return empty if no rationale found
	 */
	public String getRationale(String context) {
		String explanation = rationales.get(context);
		return explanation != null ? explanation : "";
	}
	
	/* ----------------------------------------------------------------------------
	 * Wrapper methods to put descriptive strings into Expressions
	 * ---------------------------------------------------------------------------- */
	
	/**
	 * Get the pretty name of a context in an {@link Expression} that can be put into an explanation {@link DNF}.
	 * @param context
	 * @return Parameter<String>(context, prettyName)
	 */
	public Parameter<String> getPrettyNameExplanation(String context) {
		return Parameter.instance(context, getPrettyName(context));
	}

	/**
	 * Get the unit of a context (e.g. kg, cm, &deg;C, number of windows) in an {@link Expression} that can be put into an explanation {@link DNF}.
	 * @param context
	 * @return Parameter<String>(context, unit)
	 */
	public Parameter<String> getUnitExplanation(String context) {
		return Parameter.instance(context, getUnit(context));
	}

	/**
	 * Get the definition of a context in an {@link Expression} that can be put into an explanation {@link DNF}.
	 * @param context
	 * @return Parameter<String>(context, definition)
	 */
	public Parameter<String> getDefinitionExplanation(String context) {
		return Parameter.instance(context, getDefinition(context));
	}

	/**
	 * Get the rationale of a context (why it is important and/or
	 * the implication of its various values) in an {@link Expression} that can be put into an explanation {@link DNF}.
	 * @param context
	 * @return Parameter<String>(context, rationale)
	 */
	public Parameter<String> getRationaleExplanation(String context) {
		return Parameter.instance(context, getRationale(context));
	}

}
