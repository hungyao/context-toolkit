package context.apps.demos.accelerometer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Original Hashtable storage is of the model; counts of attributes at certain values.
 * These counts can be used to derive probabilities required for the Naive Bayes calculations.
 * 
 * This class reconstitutes instance data so that I can create ARFF files to be used specifically for the Weka implementation of Naive Bayes.
 *  
 * @author Brian Y. Lim
 *
 */
public class AccelerometerDataConverter {

	public static String ATTR_X_MEAN = "x_mean";
	public static String ATTR_Y_MEAN = "y_mean";
	public static String ATTR_Z_MEAN = "z_mean";
	public static String ATTR_X_SD = "x_sd";
	public static String ATTR_Y_SD = "y_sd";
	public static String ATTR_Z_SD = "z_sd";
	public static String ATTR_X_ENERGY = "x_energy";
	public static String ATTR_Y_ENERGY = "y_energy";
	public static String ATTR_Z_ENERGY = "z_energy";
	public static String ATTR_XY_CORR = "xy_corr";
	public static String ATTR_XZ_CORR = "xz_corr";
	public static String ATTR_YZ_CORR = "yz_corr";

	Hashtable<String, Integer> sitTrainingTable;
	Hashtable<String, Integer> standTrainingTable;
	Hashtable<String, Integer> walkTrainingTable;
	
	protected List<Integer> xMeans, yMeans, zMeans;
	protected List<Integer> xSDs, ySDs, zSDs;
	protected List<Integer> xEnergies, yEnergies, zEnergies;
	protected List<Integer> xyCorrs, xzCorrs, yzCorrs;
	protected List<String> activities;
	
	protected Map<String, List<Integer>> featureToListMap;
	
	public AccelerometerDataConverter() throws FileNotFoundException {
		// this dataset was extracted from an early version of Laksa (circa Dec 2009)
		sitTrainingTable = getTrainingTable("demos/accelerometer-nb/hashtable_sit.ser");
		standTrainingTable = getTrainingTable("demos/accelerometer-nb/hashtable_stand.ser");
		walkTrainingTable = getTrainingTable("demos/accelerometer-nb/hashtable_walk.ser");

		xMeans = new ArrayList<Integer>();
		yMeans = new ArrayList<Integer>();
		zMeans = new ArrayList<Integer>();
		xSDs = new ArrayList<Integer>();
		ySDs = new ArrayList<Integer>();
		zSDs = new ArrayList<Integer>();
		xEnergies = new ArrayList<Integer>();
		yEnergies = new ArrayList<Integer>();
		zEnergies = new ArrayList<Integer>();
		xyCorrs = new ArrayList<Integer>();
		xzCorrs = new ArrayList<Integer>();
		yzCorrs = new ArrayList<Integer>();
		activities = new ArrayList<String>();
		
		featureToListMap = new TreeMap<String, List<Integer>>(); // to maintain order of keys
		featureToListMap.put(ATTR_X_MEAN, xMeans);
		featureToListMap.put(ATTR_Y_MEAN, yMeans);
		featureToListMap.put(ATTR_Z_MEAN, zMeans);
		featureToListMap.put(ATTR_X_SD, xSDs);
		featureToListMap.put(ATTR_Y_SD, ySDs);
		featureToListMap.put(ATTR_Z_SD, zSDs);
		featureToListMap.put(ATTR_X_ENERGY, xEnergies);
		featureToListMap.put(ATTR_Y_ENERGY, yEnergies);
		featureToListMap.put(ATTR_Z_ENERGY, zEnergies);
		featureToListMap.put(ATTR_XY_CORR, xyCorrs);
		featureToListMap.put(ATTR_XZ_CORR, xzCorrs);
		featureToListMap.put(ATTR_YZ_CORR, yzCorrs);

//		PrintWriter out = new PrintWriter(System.out);
		PrintWriter out = new PrintWriter("demos/accelerometer-nb/accelerometer-activity-train.arff");
		
		prepareHeaders(out);
		prepareTrainingTable(sitTrainingTable, "Sit");
		prepareTrainingTable(standTrainingTable, "Stand");
		prepareTrainingTable(walkTrainingTable, "Walk");
		
		writeInstances(out);
		
		out.flush();

		// now add class value same number of times
	}
	
	public void prepareHeaders(PrintWriter out) {
		out.println("@relation 'AccelerometerActivity_intelligibility'");
		out.println();
		
		for (String key : featureToListMap.keySet()) {
			out.println("@attribute " + key + " numeric");
		}
		out.println();
		out.println("@attribute activity {Sit, Stand, Walk}");

		out.println();
		out.println("@data");
	}
	
	public void prepareTrainingTable(Hashtable<String, Integer> trainingTable, String activity) {
		/*
		 * Since we are using Naive Bayes, we don't have to worry about matching attribute values to specific instances
		 * as long as they are for the same label 
		 */
		
		// add counts
		for (String key : trainingTable.keySet()) {
			/*
			 * E.g.
			 * key of form x_mean_#, where # is the reading of x_mean
			 * val = get(key) is the count of how many times x_mean=val
			 */
			String attrName = key.substring(0, key.lastIndexOf('_')); // note that '_' may occur in key name, so use lastIndexOf
			int reading = Integer.parseInt(key.substring(key.lastIndexOf('_') + 1)); // grab _# portion and parse as integer
			int count = trainingTable.get(key);
			
			// add count number of times this reading
			for (int i = 0; i < count; i++) {
				List<Integer> list = featureToListMap.get(attrName); // e.g. xMeans, xEnergies
				list.add(reading);
			}
		}
				
		// check size of lists; for debugging
//		for (String attrName : featureToListMap.keySet()) {
//			List<Integer> list = featureToListMap.get(attrName);
//			System.out.println(attrName + ".size: " + list.size());
//		}
//		System.out.println();
		
		// all lists should have standardized sizes by now
		int size = featureToListMap.get(ATTR_X_MEAN).size(); // just use first
		// add labels
		for (int i = 0; i < size; i++) {
			activities.add(activity);
		} 
	}
	
	public void writeInstances(PrintWriter out) {
		int size = featureToListMap.get(ATTR_X_MEAN).size(); // just use first
		
		// iterate by instance (along size)
		for (int i = 0; i < size; i++) {
			// iterate by attribute names
			for (String attrName : featureToListMap.keySet()) {
				List<Integer> list = featureToListMap.get(attrName);				
				int value = list.get(i); // assume no missing values
				out.print(value + ", ");
			}
			// print label
			out.println(activities.get(i));
			
			out.flush(); // flush after each instance
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Hashtable<String, Integer> getTrainingTable(String filename) {
		try {

			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(fis);
			
			Hashtable<String, Integer> trainingTable = (Hashtable<String, Integer>)in.readObject();
			in.close();
			
			return trainingTable;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		try {
			new AccelerometerDataConverter();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
