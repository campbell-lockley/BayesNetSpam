/* ****************************************************************************************************************** *
 * Name:        Node.java
 * Description: Representation of a node in a bayesian network.
 * Author:      Campbell Lockley        studentID: 1178618
 * Date:        19/05/15
 * ****************************************************************************************************************** */

/**
 * This is a Representation of a node in a bayesian network.
 * <p>
 * Nodes have:
 * <ul>
 *      <li>a name, </li>
 *      <li>some parents, </li>
 *      <li>and a conditional probability table (CPT).</li>
 * </ul>
 * <p>
 * The probability of this node depends on the states of its parents, which determines the index into its CPT for the 
 * conditional probability.
 * <p>
 * The probabilities in this implementation are represented as fractions with discrete numerator-denominator components.
 *  This is to facilitate the calculation step. Calling the fraction's {@link Frac#getDouble() getDouble()} method will
 * return the probability as a double.
 * <p>
 * In order to avoid the zero-frequency problem the fractions representing the CPT is initialised as 1/1.
 *
 * @see Frac
 */
public class Node {
        /* Package Private members */
        protected int[] parents;        // Indices of this nodes parents in representation of bayesian network
        protected Frac[] probs;         // The CPT of this node.

        /* Private members */
        private String name;            // Name of this node

        /** Constructor */
        public Node(String name, int[] parents) {
                this.name = name;
                this.parents = parents;
                if (parents.length != 0) this.probs = new Frac[(int)Math.pow(2, parents.length)];
                else this.probs = new Frac[1];
                /* Initialise counts as 1 to avoid zero-frequency error */
                for (int i = 0; i < probs.length; i++) probs[i] = new Frac(1, 1);
        }

        /**
         * Updates the CPT of this node given an observation of its value and its parents value's.
         *
         * @param pValues Values of its parents, in the order specified in the Node.parents[].
         * @param value Value of this node.
         */
        public void update(boolean[] pValues, boolean value) {
                assert (pValues.length == parents.length);
                int index = 0;
                for (int i = 0; i < pValues.length; i++) {
                        if (pValues[i] == false) index += Math.pow(2, pValues.length -i - 1);
                }
                probs[index].denom++;
                if (value) probs[index].num++;
        }

        /**
         * Returns the probability of this node being the value specified given the state of its parents.
         *
         * @param pValues Values of its parents, in the order specified in the Node.parents[].
         * @param value Value of this node.
         */
        public double getProb(boolean[] pValues, boolean value) {
                assert (pValues.length == parents.length);
                int index = 0;
                for (int i = 0; i < pValues.length; i++) {
                        if (pValues[i] == false) index += Math.pow(2, pValues.length -i - 1);
                }
                double prob = probs[index].getDouble();
                return (value == true) ? prob : (1 - prob);
        }

        /** 
         * Gets the name of this node.
         *
         * @return Name of this node as a String.
         */
        public String getName() {
                return this.name;
        }

        /**
         * Returns a string representation of a Node object.
         *
         * @param n Node to print.
         * @return A string representation of a Node object.
         */
        public static String printNode(Node n, Node[] bayesNet) {
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
}

