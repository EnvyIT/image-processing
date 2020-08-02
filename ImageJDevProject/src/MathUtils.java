/**
 * Represents a pixel in an image.
 *
 * @author  Michael Eder
 * @version 1.0
 * @since   2020-06-14
 */

public class MathUtils {

  private MathUtils() {}

  /**
   * Calculated the diameter of a given circle area.
   * @param area The area.
   * @return Returns the diameter of the circle area.
   */
  public static double calculateDiameter(int area) {
    return Math.sqrt(4 * area / Math.PI);
  }

  /**
   * Calculates the scaling factor.
   * @param numerator The numerator of the fraction.
   * @param denominator The denominator of the fraction
   * @return Returns the scaling factor.
   */
  public static double calculateScalingFactor(double numerator, double denominator) {
    return numerator/denominator;
  }

  /**
   * Scales a given value with a given factor.
   * @param value The value.
   * @param scalingFactor The scaling factor for the value.
   * @return Returns the scaled value.
   */
  public static double scale(double value, double scalingFactor) {
    return value * scalingFactor;
  }

}
