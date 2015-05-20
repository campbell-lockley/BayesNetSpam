/* ****************************************************************************************************************** *
 * Name:        Node.java
 * Description: 
 * Author:      Campbell Lockley        studentID: 1178618
 * Date:        19/05/15
 * ****************************************************************************************************************** */

/* ******* *
 * Imports *
 * ******* */
import java.util.*;

/**
 * TODO: comment
 */
public class Node {
        /* ************** *
         * Public members *
         * ************** */
        public int[] parents;           //
        public Frac[] probs;            // The CPT of this node.

        /* *************** *
         * Private members *
         * *************** */
        private String name;            // TODO: comment


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
         * Gets the name of this node.
         * @return Name of this node as a String.
         */
        public String getName() {
                return this.name;
        }
}

