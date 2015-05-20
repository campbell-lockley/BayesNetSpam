/* ****************************************************************************************************************** *
 * Name:        BayesNetEstimate.java
 * Description: 
 * Author:      Campbell Lockley        studentID: 1178618
 * Date:        19/05/15
 * ****************************************************************************************************************** */

/* ******* *
 * Imports *
 * ******* */
import java.io.*;
import java.util.*;

/**
 *
 */
public class BayesNetEstimate {
        /* *************** *
         * Private Members *
         * *************** */
        private Node[] bayesNet;                // 
        private FileReader netFile;             // TODO: comment
        private FileReader eventsFile;          // 

        /**
         * TODO: comment
         */
        public void run() throws IOException {
                BufferedReader br;
                String line;

                /* Parse net file and construct the bayesian network */
                br = new BufferedReader(netFile);
                ArrayList<Node> net = new ArrayList<Node>();
                HashMap<String, Integer> indices = new HashMap<String, Integer>();
                Scanner sc;
                String name;
                ArrayList<Integer> pNodes;
                int[] parents;
                int index = 0;
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

                /* Parse events file to calculate estimate probabilities */
                br = new BufferedReader(eventsFile);
                /* Uses the first line from the input data file to find the relevant token indices in the rest of the 
                   file for Nodes which are in this Bayesian network.                                                 */
                int[] filePos = new int[bayesNet.length];
                String[] vars = br.readLine().split(",");
                for (int i = 0; i < vars.length; i++) {
                        if (indices.containsKey(vars[i])) filePos[indices.get(vars[i])] = i;
                }

                boolean[] values = new boolean[bayesNet.length];
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
                
                /* Output bayesian network with calculated conditional probabilities */
                PrintWriter w = new PrintWriter(new File("output.txt"));
                String s;
                for (Node n : bayesNet) {
                        w.println(printNode(n));
                }
                w.flush();
                w.close();
        }

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

        /** Gets input files from command line args and TODO: finish comment */
        public static void main(String[] args) throws FileNotFoundException, IOException {
                /* If wrong arg num print usage and exit */
                if (args.length != 2) {
                        System.out.println("Error: Incorrect number of arguments.");
                        printUsage(System.out);
                        return;
                }

                /* Run bayesian network estimate */
                BayesNetEstimate bne = new BayesNetEstimate(args[0], args[1]);
                bne.run();
        }

        /**
         * Returns a string representation of a Node object.
         *
         * @param n Node to print.
         * @return A string representation of a Node object.
         */
        public String printNode(Node n) {
                StringBuilder sb = new StringBuilder();

                /* 1: [Name]:                        */
                sb.append(n.getName());
                sb.append(":\n");
                /* 2: [[P1] [P2] ... ]               */
                for (int i = 0; i < n.parents.length; i++) {
                        sb.append(bayesNet[n.parents[i]].getName());
                        if (i < (n.parents.length - 1)) sb.append(" ");
                        else sb.append("\n");
                }
                /* 3: [[v1] [v2] ... ] [probability]
                 * ...
                 * N: [[v1] [v2] ... ] [probability] */
                boolean[] pTableVals = new boolean[n.parents.length];
                for (int i = (n.probs.length - 1); i >= 0; i--) {
                        for (int j = 0; j < n.parents.length; j++) {
                                sb.append((pTableVals[j]) ? "1" : "0");
                                sb.append(", ");
                                if ((i % (int)Math.pow(2, n.parents.length -j - 1)) == 0) {
                                        pTableVals[j] = !pTableVals[j];
                                }
                        }
                        sb.append(n.probs[i].getDouble());
                        sb.append("\n");
                }

                return sb.toString();
        }

        /** Print Usage statement to specified PrintStream (i.e. System.out). */
        private static void printUsage(PrintStream s) {
                s.println("Usage: BayesNetEstimate [network file] [events file]\n" +
                          "             network file - The file which contains the structure of the bayesian network\n"+
                          "             events file  - The file which contains the data from which the probabilities " +
                                       "of the bayesian network is estimated");
        }
}

