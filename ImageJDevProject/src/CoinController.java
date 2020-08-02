import java.util.List;
import java.util.Map;

/**
 * Represents a pixel in an image.
 *
 * @author  Michael Eder
 * @version 1.0
 * @since   2020-06-14
 */
public class CoinController {

  /**
   * Segments the reference marker from an image.
   * @param width Image width.
   * @param height Image height.
   * @param inDataArrInt Input RGB image.
   * @return Returns the segmented reference marker image.
   */
  public static int[][] segmentReferenceMarker(int width, int height, int[][][] inDataArrInt) {
    int[][] transformedImage =  CoinUtils.getTransformedImage(inDataArrInt, width, height, 0, 74, 6);
    return CoinUtils.normalizeReferenceMarker(transformedImage, width, height);
  }

  /**
   *  Segments the coins excluding the reference marker.
   * @param width Image width.
   * @param height Image height.
   * @param inDataArrInt Input RGB image.
   * @param referenceMarkerPoints All points (pixels) where the reference marker is located.
   * @return Returns the segmented coin image.
   */
  public static int[][] segmentCoins(int width, int height, int[][][] inDataArrInt, List<Point> referenceMarkerPoints) {
    int[][] transformedImage =  CoinUtils.getTransformedImage(inDataArrInt, width, height, 74, 202, 22, true);
    CoinUtils.applyReferencePoints(transformedImage, referenceMarkerPoints);
    return CoinUtils.normalizeCoins(transformedImage, width, height);
  }

  /**
   *  Calculates the diameter of the reference marker.
   * @param points All pixels of the reference marker.
   * @return The calculated diameter of the reference marker.
   */
  public static double measureReferenceMarker(List<Point> points) {
    return MathUtils.calculateDiameter(points.size());
  }

  /**
   * Region labelling for all coins in the image.
   * @param coinImage The image of the segmented coins.
   * @param segmentationResult The result of the RGB region growing.
   * @return A map of IDs for each region and the a list of all points, describing the region in the image
   */
  public static Map<Integer, List<Point>> labelRegions(int[][] coinImage, int[][][] segmentationResult) {
    return CoinUtils.rgbRegionGrowing(coinImage, segmentationResult);
  }

  /**
   * Classifies the coins in the image and calculates the coin total sum.
   * @param regionLabels The labelled regions in the image.
   * @param inDataArrInt The input RGB image.
   * @param scalingFactor Scaling factor for all coin diameters.
   * @return Returns the calculate coin sum in the image.
   */
  public static double classifyCoins(Map<Integer, List<Point>> regionLabels, int[][][] inDataArrInt, double scalingFactor) {
      float[][][] hsbImage = CoinUtils.convertRGBToHSB(inDataArrInt);
      return CoinUtils.calcSumFromRegions(regionLabels, hsbImage, scalingFactor);
  }

}
