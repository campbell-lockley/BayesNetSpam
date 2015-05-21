/* ****************************************************************************************************************** *
 * Name:        BayesNetPredict.java
 * Description: Makes predictions about some test data based of a bayesian network.
 * Author:      Campbell Lockley        studentID: 1178618
 * Date:        21/05/15
 * ****************************************************************************************************************** */

/* Imports */
import java.io.*;
import java.util.*;

/**
 * Constructs a bayesian network from a predefined structure and trains it with some training data. BayesNetPredict 
 * then makes predictions about the most probable state of an unknown variable in some test data, writting the 
 * corrected data out to the file "completedTest.csv".
 * <p>
 * The bayesian network is represented as a {@link Node Node} array.
 * <p>
 * Running {@link main(String[]) main} with the name of a file containing the network structure, the name of a file 
 * containing the training data and the name of a file containing the test data will get BayesianNetPredict to write 
 * every line of the test data to the file "completedtTest.csv" with the unknown variable corrected to its most 
 * probable value.
 * <p>
 * N.B. The unkown variable(s) in the test data must be a node specified in the network structure file.
 *
 * @see BayesNetEstimate
 * @see Node
 */
public class BayesNetPredict {
        /* Private members */
        private BayesNetEstimate bNetEst;       // Builds bayesian network and calculates CPTs of nodes
        private FileReader testFile;            // File with training data to classify with bayesian network

        /**
         * Constructor.
         * @param netFileName Name of file containing structure of bayesian network.
         * @param trainFileName Name of file containing training data for bayesian network.
         * @param testFileName Name of file containing test data for bayesian network.
         */
        public BayesNetPredict(String netFileName, String trainFileName, String testFileName) 
                        throws FileNotFoundException {
                /* Try and open input files */
                testFile = new FileReader(testFileName);
                bNetEst = new BayesNetEstimate(netFileName, trainFileName);
        }

        /**
         * Constructs a bayesian netowork using a predefined network structure and some training data and attempts to
         * classify some test data which has an unknown variable by predicting the most probable state of this unknown
         * variable based on the bayesian network trained with the training data.
         * <p>
         * The test data is corrected with the predictions and is written out to "completedTest.csv".
         */
        public void predict() throws IOException {
                /* Construct bayesian network */
                Node[] bayesNet = bNetEst.getBayesianNetwork();

                BufferedReader br = new BufferedReader(testFile);
                PrintWriter w = new PrintWriter("completedTest.csv");
                String line = br.readLine();
                w.println(line);

                /* Work out position of each node in the test input file */
                HashMap<String, Integer> indices = new HashMap<String, Integer>();
                for (int i = 0; i < bayesNet.length; i++) indices.put(bayesNet[i].getName(), i);
                String[] vars = line.split(",");
                int[] fileToNet = new int[vars.length];
                int[] netToFile = new int[bayesNet.length];
                for (int i = 0; i < vars.length; i++) {
                        if (indices.containsKey(vars[i])) {
                                fileToNet[i] = indices.get(vars[i]);
                                netToFile[indices.get(vars[i])] = i;
                        }
                }

                /* Parse test file, making predictions from node's conditional probabilities, and write to output */
                int pos;
                Node q;
                boolean[] pValues;
                double prob;
                while ((line = br.readLine()) != null) {
                        vars = line.split(",");

                        /* Find pos of missing variable (starting from back) */
                        for (pos = (vars.length - 1); pos >= 0; pos--) if (vars[pos].equals("?")) break;
                        assert (pos >=0);
                        q = bayesNet[fileToNet[pos]];

                        /* Find state of parents */
                        pValues = new boolean[q.parents.length];
                        for (int i = 0; i < pValues.length; i++) {
                                pValues[i] = (vars[netToFile[q.parents[i]]].equals("1")) ? true : false;
                        }

                        /* Get probability that node is true given parents */
                        prob = q.getProb(pValues, true);

                        /* Make prediction and write out */
                        line = line.replaceFirst(java.util.regex.Pattern.quote("?"), (prob >= 0.5) ? "1" : "0");
                        w.println(line);
                }
                br.close();
                w.flush();
                w.close();
        }

        /** 
         * Gets input files from command line, builds bayesian network and uses it to make predictions about test 
         * data. Prints test data with predictions to "completedTest.csv".
         */
        public static void main(String[] args) throws IOException, FileNotFoundException {
                if (args.length != 3) {
                        System.out.println("Error: Incorrect number of arguments.");
                        printUsage(System.err);
                        return;
                }

                /* Construct bayesian network and classify test data */
                BayesNetPredict bNetPred = new BayesNetPredict(args[0], args[1], args[2]);
                bNetPred.predict();
        }

        /** Print Usage statement to specified PrintStream (i.e. System.out). */
        private static void printUsage(PrintStream s) {
                s.println("Usage: BayesNetPredict [network file] [training data file] [test data file]\n" +
                          "             network file       - The file which contains the structure of the bayesian " +
                                                            "network\n" +
                          "             training data file - The file which contains the data from which the " +
                                                            "probabilities of the bayesian network is estimated\n" +
                          "             test data file     - The file which contains the data to classify");
        }
}

