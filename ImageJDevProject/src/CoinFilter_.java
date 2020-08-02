import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.List;
import java.util.Map;

public class CoinFilter_ implements PlugInFilter {

  public int setup(String arg, ImagePlus imp) {
    if (arg.equals("about")) {
      showAbout();
      return DONE;
    }
    return DOES_RGB + DOES_STACKS + SUPPORTS_MASKING;
  } //setup

  public void run(ImageProcessor ip) {
    int width = ip.getWidth();
    int height = ip.getHeight();
    int[][][] inDataArrInt = ImageJUtility.getChannelImageFromIP(ip, width, height, 3);
    /* TASK 1 - 1*/
    /* Segments the reference marker from the image and shows it.*/
    int[][] referenceMarkerImage = CoinController.segmentReferenceMarker(width, height, inDataArrInt);
    ImageJUtility.showNewImage(referenceMarkerImage, width, height,"Segementierte Referenzmarkierung");

    /* TASK 1 - 2*/
    /* Segments the coins without the reference marker and shows them.*/
    int[][][] segmentationResult = new int[width][height][3];
    Map<Integer,  List<Point>> referenceMarkerLabel = CoinController.labelRegions(referenceMarkerImage, segmentationResult);
    List<Point>  referenceMarkerPoints = referenceMarkerLabel.get(1);
    int[][] coinsImage = CoinController.segmentCoins(width, height, inDataArrInt, referenceMarkerPoints);
    ImageJUtility.showNewImage(coinsImage, width, height,"Segementierte Münzen ohne Referenzmarkierung");

    /* TASK 1 - 3*/
    /* Calculating the diameter of the reference marker + the scaling factor and log them.*/
    double referenceDiameter = CoinController.measureReferenceMarker(referenceMarkerPoints);
    double scalingFactor = MathUtils.calculateScalingFactor(30.0 , referenceDiameter);
    IJ.log(String.format("size black = %d diameter= %f s=%f", referenceMarkerPoints.size(), referenceDiameter, scalingFactor));

    /* TASK 2 - 1*/
    /* Region labelling for all coins - result are the labelled regions with IDs [1;254]. */
    segmentationResult = new int[width][height][3];
    Map<Integer,  List<Point>> regionLabels = CoinController.labelRegions(coinsImage, segmentationResult);

    /* TASK 2 - 2*/
    /* Showing the labbeled coin image and print total coins + for each coin diameter + pixels.  */
    ImageJUtility.showNewImageRGB(segmentationResult, segmentationResult.length, segmentationResult[0].length, "Labelled image");
    logRegionLabels(regionLabels, scalingFactor);

    /* TASK 3 - 1*/
    /* Classify each coin and count the total coin sum in the image. Finally, logging the coin sum.*/
    double coinSum = CoinController.classifyCoins(regionLabels, inDataArrInt, scalingFactor);
    IJ.log(String.format("Coin value: %.2f \u20ac", coinSum));
  } //run

  private void logRegionLabels(Map<Integer, List<Point>> regionLabels, double scalingFactor) {
    regionLabels.forEach((id, points) -> {
      int area = points.size();
      double scaledDiameter = MathUtils.scale(MathUtils.calculateDiameter(area), scalingFactor);
      IJ.log(String.format("ID [%d]: coin with %d pixels, diameter in mm = %f",id,area ,scaledDiameter ));
    });
    IJ.log(String.format("Total coins in image: %d", regionLabels.size()));
  }

  void showAbout() {
    IJ.showMessage("About Template_...",
        "this is a PluginFilter template\n");
  } //showAbout

} //class FilterTemplate_

