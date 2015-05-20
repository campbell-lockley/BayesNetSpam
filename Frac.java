/* ****************************************************************************************************************** *
 * Name:        Frac.java
 * Description: Simple representation of a fraction.
 * Author:      Campbell Lockley        studentID: 1178618
 * Date:        20/05/15
 * ****************************************************************************************************************** */

/**
 * Simple representation of a fraction.
 * 
 * Frac exposes two public members:
 * <ul>
 *      <li>num   - The numerator of the fraction.</li>
 *      <li>denom - The denominator of the fraction.</li>
 * </ul>
 * 
 * Calling Frac's getDouble() method will calculate and return this fraction as a double.
 */
public class Frac {
        /* Public Members */
        public int num;         // Numerator
        public int denom;       // Denominator

        /** Constructor */
        public Frac(int num, int denom) {
                this.num = num;
                this.denom = denom;
        }

        /**
         * Calculates and returns this fraction as a double.
         * Throws IllegalStateExeption if the denominator is zero.
         *
         * @return Value of this fraction, represented as a double.
         */
        public double getDouble() throws IllegalStateException {
                if (denom == 0) throw new IllegalStateException("getDouble() cannot be called when denominator is 0");

                return num / (double)denom;
        }
}

