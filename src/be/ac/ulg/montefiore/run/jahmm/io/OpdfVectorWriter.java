package be.ac.ulg.montefiore.run.jahmm.io;

import java.io.IOException;
import java.io.Writer;

import be.ac.ulg.montefiore.run.jahmm.ObservationVector;

public class OpdfVectorWriter extends OpdfWriter<OpdfVector> {

	public void write(Writer writer, OpdfVector opdf) throws IOException {
		String s = "VectorOPDF [";
		
		int NUM_OBSERVATION_DIM = opdf.dimension();
		int NUM_OBSERVATION_VALS = opdf.nbValues();

		for (int j = 0; j < NUM_OBSERVATION_VALS; j++) {
//			System.out.println("j = " + j);
			double[] obsVal = OpdfVector.toVector(j, NUM_OBSERVATION_VALS, NUM_OBSERVATION_DIM);
			s += opdf.probability(new ObservationVector(obsVal)) + " ";
		}

		writer.write(s + "]\n");
	}
}