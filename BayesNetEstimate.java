/* ****************************************************************************************************************** *
 * Name:        BayesNetEstimate.java
 * Description: Builds a bayesian network using a predefined netwrok structure and some sample date.
 * Author:      Campbell Lockley        studentID: 1178618
 * Date:        19/05/15
 * ****************************************************************************************************************** */

/* Imports */
import java.io.*;
import java.util.*;

/**
 * Constructs a {@link Node Node} array representation of a bayesian network.
 * <p>
 * BayesNetEstimate reads in a predefined structure for the bayesian network from a file and parses model data to 
 * calculate the conditional probabilities for the nodes in the network.
 * <p>
 * Running {@link main(String[]) main} with the name of a file containing the network structure and the name of a file 
 * with sample data will build the network and output each state with its parents and associated conditional 
 * probabilities to a file called "output.txt".
 * <p>
 * Nodes in the network structure file must be declared in the order of parents first.
 *
 * @see Node
 */
public class BayesNetEstimate {
        /* Private Members */
        private Node[] bayesNet;                // Representation of bayesian network
        private FileReader netFile;             // File specifying the network structure
        private FileReader eventsFile;          // File with model data for calulating the CPTs

        /**
         * Constructor
         *
         * @param netFileName Name of file containing structure of bayesian network.
         * @param eventsFileName Name of file containing training data for bayesian network.
         */
        public BayesNetEstimate(String netFileName, String eventsFileName) throws FileNotFoundException {
                /* Try and open input files */
                netFile = new FileReader(netFileName);
                eventsFile = new FileReader(eventsFileName);
        }

        /**
         * Constructs the bayesian network from the network structure file and the model data file.
         *
         * @return Representation of bayesian network.
         */
        public Node[] getBayesianNetwork() throws IOException {
                HashMap<String, Integer> indices;

                /* Parse net file and construct the bayesian network */
                indices = readNetFile();

                /* Parse events file to calculate estimate probabilities */
                readDataFile(indices);
                
                return bayesNet;
        }

        /**
         * Reads in the structure of the bayesian network from file and constructs the nodes in the network.
         *
         * @return HashMap of (node name, index) key-value pairs.
         */
        private HashMap<String, Integer> readNetFile() throws IOException {
                BufferedReader br = new BufferedReader(netFile);
                HashMap<String, Integer> indices = new HashMap<String, Integer>();
                ArrayList<Node> net = new ArrayList<Node>();
                ArrayList<Integer> pNodes;
                int[] parents;
                Scanner sc;
                String line, name;
                int index = 0;

                /* Parse net file and construct the bayesian network */
                while ((line = br.readLine()) != null) {
                        /* Parses line from network input file. Input file format should be:
                           [node name]: [[parent 1] [parent 2] ... ]                         */
                        sc = new Scanner(line);
                        sc.useDelimiter(" ");

                        /* Extract node name */
                        name = sc.next();
                        name = name.substring(0, name.length() - 1);    // Remove tailing ':'

                        /* Extract node's parents */
                        /* Parents must be declared above children in the network file */
                        pNodes = new ArrayList<Integer>();
                        while (sc.hasNext()) pNodes.add(indices.get(sc.next()));
                        parents = new int[pNodes.size()];
                        for (int i = 0; i < pNodes.size(); i++) parents[i] = pNodes.get(i).intValue();

                        /* Add node to bayesian network */
                        indices.put(name, new Integer(index++));
                        net.add(new Node(name, parents));
                }
                bayesNet = new Node[net.size()];
                net.toArray(bayesNet);

                br.close();
                netFile.close();

                return indices;
        }

        /**
         * Reads in model data for nodes in network and calculates CPTs for each node.
         *
         * @param indices HashMap (node name, index) key-value pairs. 
         */
        private void readDataFile(HashMap<String, Integer> indices) throws IOException {
                BufferedReader br = new BufferedReader(eventsFile);
                int[] filePos = new int[bayesNet.length];
                boolean[] values = new boolean[bayesNet.length];
                String[] vars;
                String line;

                /* Uses the first line from the input data file to find the relevant token indices in the rest of the 
                   file for Nodes which are in this Bayesian network.                                                 */
                vars = br.readLine().split(",");
                for (int i = 0; i < vars.length; i++) {
                        if (indices.containsKey(vars[i])) filePos[indices.get(vars[i])] = i;
                }

                /* Parse events file to calculate estimate probabilities */
                while ((line = br.readLine()) != null) {
                        vars = line.split(",");

                        /* Extract values from line of input */
                        for (int i = 0; i < values.length; i++) {
                                values[i] = vars[indices.get(bayesNet[i].getName())].equals("1");
                        }

                        /* Update CPT for each node */
                        boolean[] pValues;
                        for (int i = 0; i < bayesNet.length; i++) {
                                pValues = new boolean[bayesNet[i].parents.length];
                                for (int j = 0; j < pValues.length; j++) pValues[j] = values[bayesNet[i].parents[j]];
                                bayesNet[i].update(pValues, values[i]);
                        }
                }

                br.close();
                eventsFile.close();
        }

        /**
         * Gets input files from command line args and builds the bayesian network.
         * Prints resulting network with calculated conditional probabilities to "output.txt".
         */
        public static void main(String[] args) throws FileNotFoundException, IOException {
                /* If wrong arg num print usage and exit */
                if (args.length != 2) {
                        System.out.println("Error: Incorrect number of arguments.");
                        printUsage(System.err);
                        return;
                }

                /* Construct bayesian network and output with calculated conditional probabilities */
                PrintWriter w = new PrintWriter(new File("output.txt"));
                BayesNetEstimate bNetEst = new BayesNetEstimate(args[0], args[1]);
                Node[] bayesNet = bNetEst.getBayesianNetwork();
                for (Node n : bayesNet) {
                        w.println(Node.printNode(n, bayesNet));
                }
                w.flush();
                w.close();
        }

        /** Print Usage statement to specified PrintStream (i.e. System.out). */
        private static void printUsage(PrintStream s) {
                s.println("Usage: BayesNetEstimate [network file] [events file]\n" +
                          "             network file - The file which contains the structure of the bayesian network\n"+
                          "             events file  - The file which contains the data from which the probabilities " +
                                                      "of the bayesian network is estimated");
        }
}

