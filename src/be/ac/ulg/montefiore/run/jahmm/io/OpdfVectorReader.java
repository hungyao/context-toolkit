package be.ac.ulg.montefiore.run.jahmm.io;

import java.io.IOException;
import java.io.StreamTokenizer;

import be.ac.ulg.montefiore.run.jahmm.io.FileFormatException;
import be.ac.ulg.montefiore.run.jahmm.io.HmmReader;

/**
 * Need to put this in one of JAHMM's packages due to package protected methods that need to be used.
 * 
 * Ref: http://www.google.com/codesearch/p?hl=en#EPpzOEW5jow/trunk/src/main/java/be/ac/ulg/montefiore/run/jahmm/io/OpdfIntegerReader.java&q=OpdfIntegerReader%20package:http://jahmm\.googlecode\.com&sa=N&cd=1&ct=rc
 * Code for OpdfIntegerReader.java
 * 
 * @author Brian Y. Lim
 *
 */
public class OpdfVectorReader extends OpdfReader<OpdfVector> {
	
    private final int nbEntries; // < 0 if number of entries is not checked.
    
    public OpdfVectorReader() {
            nbEntries = -1;
    }
    
    public OpdfVectorReader(int nbEntries) {
            if (nbEntries <= 0)
                    throw new IllegalArgumentException("Argument must be strictly "
                                    + "positive");
            this.nbEntries = nbEntries;
    }


	@Override
	String keyword() {
		return "VectorOPDF";
	}

	@Override
	public OpdfVector read(StreamTokenizer st) throws IOException, FileFormatException {
		HmmReader.readWords(st, keyword());
		
        double[] probabilities = OpdfReader.read(st, -1);

        if (nbEntries > 0 && probabilities.length != nbEntries)
                throw new FileFormatException(st.lineno(),
                                "Invalid distribution (should " + "operate over 0..."
                                                + (nbEntries - 1) + ")");

        return new OpdfVector(probabilities);
	}

}
