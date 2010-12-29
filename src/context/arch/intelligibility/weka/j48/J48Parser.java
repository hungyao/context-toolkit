package context.arch.intelligibility.weka.j48;

import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instances;
import weka.gui.treevisualizer.Edge;
import weka.gui.treevisualizer.Node;
import weka.gui.treevisualizer.TreeBuild;
import context.arch.discoverer.query.ClassifierWrapper;
import context.arch.intelligibility.expression.Comparison;
import context.arch.intelligibility.expression.DNF;
import context.arch.intelligibility.expression.Parameter;
import context.arch.intelligibility.expression.Reason;
import context.arch.storage.AttributeNameValue;
import context.arch.widget.ClassifierWidget;

/**
 * Utility class to parse the decision tree model from WEKA's J48 class,
 * and build a Disjunctions of traces (Conjunctions) from each class value.
 * 
 * Converts proprietary TreeDOT format used by J48 to format used in Intelligibility Toolkit.
 * In the former, nodes represent attribute names (e.g. "brightness") and edges represent conditions (e.g. "<= 5").
 * In the latter, nodes represent full conditions (e.g. "brightness <= 5") 
 * @author Brian Y. Lim
 *
 */
public class J48Parser {

	/**
	 * Parse the J48 tree model with supplementary information about weka attributes from the header.
	 * Each Disjunction in Disjunctive Normal Form (DNF).
	 * @param cModel J48 tree model to parse
	 * @param header to reference Weka Attributes
	 * @return Map of Disjunctions of traces for each class value
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, DNF> parse(J48 cModel, Instances header) throws Exception {
		Map<String, DNF> valueTraces = new HashMap<String, DNF>();
		
		// get tree node structure from model
		Reader treeDot = new StringReader(cModel.graph());
		TreeBuild treeBuild = new TreeBuild();
		Node treeRoot = treeBuild.create(treeDot);
		
		// set up one disjunction per class value
		Attribute classAttr = header.classAttribute();
		Enumeration<String> values = classAttr.enumerateValues();
		while (values.hasMoreElements()) {
			valueTraces.put(values.nextElement(), new DNF());
		}
		
		// recursively parse
		parse(
			treeRoot, header,
			new Reason(),
			valueTraces
		);
		
		// each Disjunction is naturally in DNF according to parsing process
		
		return valueTraces;
	}
	
	/**
	 * Recursively parse the tree Node.
	 * @param node of the tree structure from the J48 model
	 * @param header to reference Weka Attributes
	 * @param parentTrace of the trace to be appended to as we recurse
	 * @param valueTraces to store Disjunctions of traces for each class value
	 */
	private static void parse(Node node, Instances header, Reason parentTrace, Map<String, DNF> valueTraces) {
		Edge edgeToChild;
		int i = 0; // need to use funky counter because Node doesn't return count of children

		while ((edgeToChild = node.getChild(i)) != null) { // depth-first search
			/*
			 * Each branch means there's an alternative path,
			 * so create a new trace for it.
			 * Make sure it is a duplicate copy.
			 * Always clone so that sibling traces would not see changes done by previous siblings
			 */
			Reason trace = parentTrace.clone();
			
			/*
			 * Next, prepare extension to the trace
			 */
			Parameter<?> childExpression = createParameter(node, edgeToChild, header);
			
			/*
			 * Append child to trace
			 */
			// first check if a previous Comparison was already about this attribute, then just update its bounds
			trace.addOrMerge(childExpression);
//			System.out.println("trace = " + trace);
//			System.out.println("parentTrace = " + parentTrace);
			
			// recurse
			Node childNode = edgeToChild.getTarget();
			parse(childNode, header, trace, valueTraces);

			i++;
		}
		
		if (i == 0) { // no child edges => is leaf node
			// expect format "yes (2.0)" or "Not Exercising (2.0)"
			String nodeLabel = node.getLabel();
			String classValue = nodeLabel.split("[()]")[0].trim(); // just take the part before '('
			
			valueTraces.get(classValue) // get Disjunction for the class value
					   .add(parentTrace); // add to its traces
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Comparable<? super T>> Parameter<?> createParameter(Node node, Edge edgeToChild, Instances header) {
		String attrName = node.getLabel(); // e.g. humidity
		
		// extract relation and value
		String cond = edgeToChild.getLabel(); // e.g. "<= 42", "= hello world"
		String[] condParts = cond.split(" ", 2); // split at first ' '
		Comparison.Relation relation = Comparison.Relation.toRelation(condParts[0]);
		String strValue = condParts[1];
		
		// cast value class type
		Attribute attr = header.attribute(attrName);
		T value = (T) AttributeNameValue.valueOf(
				ClassifierWidget.wekaTypeToClass(attr.type()), 
				strValue);
		
		// create Expression for name and condition
		Parameter<?> expression;
		if (attr.isNumeric()) {
			expression = Comparison.instance(attrName, value, relation);
		}
		else { // assume nominal, string or date
			expression = Parameter.instance(attrName, value);
		}
		
		return expression;
	}
	
	/**
	 * For testing
	 * @param args
	 */
	public static void main(String[] args) {
		// load cModel and header from files
		J48 cModel = (J48) ClassifierWrapper.loadClassifier("demos/imautostatus-dtree/imautostatus.model");
		Instances header = ClassifierWrapper.loadDataset("demos/imautostatus-dtree/imautostatus-test.arff");
//		Instance instance = header.instance(0); // use one instance for testing
		
		// then parse it
		try {
			Map<String, DNF> valueTraces = J48Parser.parse(cModel, header);
			
			for (String value : valueTraces.keySet()) {
				DNF traces = valueTraces.get(value);
				System.out.println(value + "(size=" + traces.size() + "): " + traces);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
