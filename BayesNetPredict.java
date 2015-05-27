/* ****************************************************************************************************************** *
 * Name:        BayesNetPredict.java
 * Description: Makes predictions about some test data based of a bayesian network.
 * Author:      Campbell Lockley        studentID: 1178618
 * Date:        21/05/15
 * ****************************************************************************************************************** */

/* Imports */
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Constructs a bayesian network from a predefined structure and trains it with some training data. BayesNetPredict 
 * then makes predictions about the most probable state of an unknown variable (denoted by a "?") in some test data, 
 * writting the corrected data out to the file "completedTest.csv".
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
                /* Construct and train bayesian network */
                Node[] bayesNet = bNetEst.getBayesianNetwork();

                /* Open I/O */
                BufferedReader br = new BufferedReader(testFile);
                PrintWriter w = new PrintWriter("completedTest.csv");

                /* Echo header from input to output */
                String line = br.readLine();
                w.println(line);

                /* Work out position of each node in the test input file */
                int index;
                String[] vars = line.split(",");
                int[] fileToNet = new int[vars.length];
                int[] netToFile = new int[bayesNet.length];
                HashMap<String, Integer> indices = new HashMap<String, Integer>();
                for (int i = 0; i < bayesNet.length; i++) indices.put(bayesNet[i].getName(), i);
                for (int i = 0; i < vars.length; i++) {
                        if (indices.containsKey(vars[i])) {
                                index = indices.get(vars[i]);
                                fileToNet[i] = index;
                                netToFile[index] = i;
                        }
                }

                /* Parse test file, making predictions from exact event probabilities, and write to output */
                int pos;
                boolean[] values = new boolean[bayesNet.length];
                double probTrue, probFalse;
                while ((line = br.readLine()) != null) {
                        /* Get values of nodes from input */
                        vars = line.split(",");
                        for (int i = 0; i < bayesNet.length; i++) {
                                values[i] = (vars[netToFile[i]].equals("1")) ? true : false;
                        }

                        /* Find pos of missing variable (starting from back) */
                        for (pos = (vars.length - 1); pos >= 0; pos--) if (vars[pos].equals("?")) break;
                        assert (pos >=0);       // Must find query variable

                        /* Calculate probability for spam = true */
                        values[fileToNet[pos]] = true;
                        probTrue = calcExactEventProb(bayesNet, values);

                        /* Calculate probability for spam = false */
                        values[fileToNet[pos]] = false;
                        probFalse = calcExactEventProb(bayesNet, values);

                        /* Make prediction and write out */
                        line = line.replaceFirst(Pattern.quote("?"), (probTrue >= probFalse) ? "1" : "0");
                        w.println(line);
                }
                br.close();
                w.flush();
                w.close();
        }

        /**
         * Caclucates the exact event probabilities for the bayesian network given the specified model of the 
         * variables.
         * <p>
         * i.e. ExactEventP(x1, ..., xn) = PRODUCT(P(xi | PARENTS(Xi)));
         * 
         * @param bayesNet The bayesian network.
         * @param values The model for the variables.
         * @return The exact event probability for the given model.
         */
        public double calcExactEventProb(Node[] bayesNet, boolean[] values) {
                assert (values.length == bayesNet.length);

                double prob = 1;

                /* Calculate exact event probability */
                Node n;
                boolean[] pValues;
                for (int i = 0; i < bayesNet.length; i++) {
                        n = bayesNet[i];

                        /* Find state of parents in model */
                        pValues = new boolean[n.parents.length];
                        for (int j = 0; j < pValues.length; j++) {
                                pValues[j] = values[n.parents[j]];
                        }

                        /* Exact event probability is product of individual conditional probabilities */
                        prob *= n.getProb(pValues, values[i]);
                }

                return prob;
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

                try {
                        /* Construct bayesian network and classify test data */
                        BayesNetPredict bNetPred = new BayesNetPredict(args[0], args[1], args[2]);
                        bNetPred.predict();
                } catch (Exception e) {
                        System.err.println("BayesNetPredict encountered and error:");
                        e.printStackTrace();
                }
        }

        /** Print Usage statement to specified PrintStream (i.e. System.err). */
        private static void printUsage(PrintStream s) {
                s.println("Usage: BayesNetPredict [network file] [training data file] [test data file]\n" +
                          "             network file       - The file which contains the structure of the bayesian " +
                                                            "network\n" +
                          "             training data file - The file which contains the data from which the " +
                                                            "probabilities of the bayesian network is estimated\n" +
                          "             test data file     - The file which contains the data to classify");
        }
}

