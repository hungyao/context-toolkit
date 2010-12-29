package context.arch.enactor;

import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import context.arch.discoverer.ComponentDescription;
import context.arch.storage.Attribute;
import context.arch.storage.AttributeNameValue;

/**
 * Parser to parse a Javascript expression, and evaluate it with the Javascript eval() function
 * to assign a value to an {@link Attribute}. This is used by {@link EnactorXmlParser} to parse
 * expressions for outcome values for References.
 * @author Brian Y. Lim
 *
 * @param <T>
 */
public class AttributeEvalParser<T extends Comparable<? super T>> {

	private Attribute<?> attribute;
	private Class<T> type;
	
	private ScriptEngineManager manager;
	private ScriptEngine engine;
	private String scriptText;
	private CompiledScript script;
	private Bindings bindings;
	
	/**
	 * @see #instance(Attribute, String, Map)
	 * @param attribute
	 * @param scriptText
	 * @param constVars
	 */
	private AttributeEvalParser(
			Attribute<T> attribute, 
			String scriptText, 
			Map<String, Comparable<?>> constVars) {
		this.attribute = attribute;
		this.type = (Class<T>) attribute.getType();
		this.scriptText = scriptText;
		
		manager = new ScriptEngineManager();
		engine = manager.getEngineByName("js"); // javascript
		
		// compile to make script run faster on each execution
		try {
			script = ((Compilable) engine).compile(scriptText);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		
		// bindings for vars
		bindings = engine.createBindings();
		
		// add vars from constants
		bindings.putAll(constVars);
	}
	
	/**
	 * Create an instance of the parser for an attribute from a Javascript expression.
	 * Rather than calling a static parse method, we need to instantiate the parser to
	 * configure it appropriately, and invoke the parsing later during runtime. This also
	 * allows for retaining information about the original script expression.
	 * 
	 * @param <T> the inferred type of the attribute to set
	 * @param attribute the Attribute to assign to from this script expression
	 * @param script Javascript expression as a string
	 * @param constVars map of constant variables (and their values) that may be referenced in the script expression.
	 * These will be bound as variables to the script engine.
	 * 
	 * @return an instance of the parser that can parse the script expression to an Attribute.
	 */
	public static <T extends Comparable<? super T>> AttributeEvalParser<T> instance(
			Attribute<T> attribute, 
			String script, 
			Map<String, Comparable<?>> constVars) {
		return new AttributeEvalParser<T>(attribute, script, constVars);
	}
	
	/**
	 * Get the name of the attribute that this parser will create on parsing.
	 * @return
	 */
	public String getAttributeName() {
		return attribute.getName();
	}
	
	/**
	 * Pseudonym for {@link #eval(ComponentDescription)}
	 * @param inWidgetStub from which to obtain values of non-constant attributes that may be references in the script.
	 * @return Javascript eval() result converted to string so that it can be forced back to the appropriate type.
	 * @see #eval(ComponentDescription)
	 */
	public T getAttributeValue(ComponentDescription inWidgetStub) {
		Object value = eval(inWidgetStub);
		return cast(value, type);
	}
	
	/**
	 * Javascript eval() result converted to string so that it can be forced back to the appropriate type.
	 * @param inWidgetStub from which to obtain values of non-constant attributes that may be references in the script.
	 * @return
	 */
	protected String eval(ComponentDescription inWidgetStub) {
		try {
			// extract non-constant attribute values, and put into script
			for (Attribute<?> a : inWidgetStub.getNonConstantAttributes().values()) {
				if (a instanceof AttributeNameValue<?>) {
					AttributeNameValue<?> att = (AttributeNameValue<?>) a;
					String name = att.getName();
					Object value = att.getValue();
					bindings.put(name, value);
				}
			}
			
			engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
			
			if (script != null) { return script.eval().toString(); } // compiled version: faster
			else { return engine.eval(scriptText).toString(); } // uncompiled version: slower
			
		} catch (ScriptException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Convenience method to cast because javascript eval() would return values as String.
	 * @param <T> type to cast to
	 * @param value returned from the javascript eval() method
	 * @param type to cast to
	 * @return the value in the correct type cast
	 */
	@SuppressWarnings("unchecked")
	private T cast(Object value, Class<T> type) {
		if (type.equals(String.class)) {
			return (T) value;
		}
		
		// is number type, so would be mapped to double
		if (Number.class.isAssignableFrom(type)) {
			//double number = (Double) value;
			// actually, it seems eval() returns string	
			double number = new Double((String) value);
			//System.out.println("number = " + number);
			
			if (type.equals(Double.class)) { return (T) value; }
			else if (type.equals(Integer.class)) { return (T) new Integer((int) number); }
			else if (type.equals(Float.class)) { return (T) new Float((float) number); }
			else if (type.equals(Short.class)) { return (T) new Short((short) number); }
			else if (type.equals(Byte.class)) { return (T) new Byte((byte) number); }
			else { return (T) value; } // unknown
		}
		
		else if (type.equals(Boolean.class)) {
			return (T) new Boolean((String) value);
		}
		
		// type is some proprietary class
		// use reflection to extract from String
		else {
			String strValue = (String) value;
			T tValue = null;
			try {
				tValue = (T) type.getMethod("valueOf", String.class)
								 .invoke(null, strValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return tValue;
		}
	}

}
