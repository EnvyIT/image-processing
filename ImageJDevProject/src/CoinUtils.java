import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Represents a pixel in an image.
 *
 * @author  Michael Eder
 * @version 1.0
 * @since   2020-06-14
 */
public class CoinUtils {

  public static final int BACKGROUND_COLOR = 0;
  public static final int FOREGROUND_COLOR = 255;

  private static final int MARKER = -1;
  private static final int NORMALIZE_COUNT = 7;
  private static final int RGB_CHANNELS = 3;
  private static final int MIN_THRESHOLD = 12_000;
  private static final RGBColor BACKGROUND = new RGBColor(BACKGROUND_COLOR, BACKGROUND_COLOR, BACKGROUND_COLOR);
  private static final Set<Coin> goldCoins;
  private static final Set<Coin> copperCoins;

  /**
   * Initalizes the static gold coins and copper coins for classifications.
   */
  static {
    goldCoins = new HashSet<>();
    copperCoins = new HashSet<>();
    copperCoins.add(new Coin(0.01, 16.25));
    copperCoins.add(new Coin(0.02, 18.75));
    copperCoins.add(new Coin(0.05, 21.25));
    goldCoins.add(new Coin(0.10, 19.75, true));
    goldCoins.add(new Coin(0.20, 22.25, true));
    goldCoins.add(new Coin(0.50, 24.25, true));
  }


  private CoinUtils() {
  }

  /**
   *  Threshold segmentation with an input image, a width and height of the image.
   *  Minimum threshold and a maximum threshold and a given delta for the RGB channels.
   * @param inImg The input image.
   * @param width The image width.
   * @param height The image height.
   * @param minThreshold The minimum threshold.
   * @param maxThreshold The maximum threshold.
   * @param delta The delta for the RGB values.
   * @return Returns the segmented image.
   */
  public static int[][] getTransformedImage(int[][][] inImg, int width, int height, int minThreshold, int maxThreshold,
      int delta) {
    return getTransformedImage(inImg, width, height, minThreshold, maxThreshold, delta, false);
  }

  /**
   *  Threshold segmentation with an input image, a width and height of the image.
   *  Minimum threshold and a maximum threshold and a given delta for the RGB channels.
   * @param inImg The input image.
   * @param width The image width.
   * @param height The image height.
   * @param minThreshold The minimum threshold.
   * @param maxThreshold The maximum threshold.
   * @param delta The delta for the RGB values.
   * @param invert An marker to invert the colors of the segmented image.  0 becomes 255 and 255 becomes 0.
   * @return Returns the segmented image.
   */
  public static int[][] getTransformedImage(int[][][] inImg, int width, int height, int minThreshold, int maxThreshold, int delta,
      boolean invert) {
    int[][] returnImg = new int[width][height];
    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        int r = inImg[x][y][0];
        int g = inImg[x][y][1];
        int b = inImg[x][y][2];
        if (isInRange(minThreshold, maxThreshold, r) && isInRange(minThreshold, maxThreshold, g) &&
            isInRange(minThreshold, maxThreshold, b) &&
            Math.abs(r - g) <= delta && Math.abs(g - b) <= delta) {
          returnImg[x][y] = invert ? BACKGROUND_COLOR : FOREGROUND_COLOR;
        } else {
          returnImg[x][y] = invert ? FOREGROUND_COLOR : BACKGROUND_COLOR;
        }
      }
    }
    return returnImg;
  }

  /**
   * Determines if a value is in range.
   * @param minThreshold Min threshold.
   * @param maxThreshold Max threshold.
   * @param value The value.
   * @return Returns true if the value is in range, otherwise false.
   */
  private static boolean isInRange(int minThreshold, int maxThreshold, int value) {
    return value >= minThreshold && value <= maxThreshold;
  }

  /**
   * Normalizes the reference marker with Mathematical Morphology. That means artifacts in an image are removed.
   * Combination of  dilate + erode + dilate iterations.
   * @param image The image which should be normalized.
   * @param width The image width.
   * @param height The image height.
   * @return Returns the normalized image a given amount of iterations.
   */
  public static int[][] normalizeReferenceMarker(int[][] image, int width, int height) {
    for (int i = 0; i < NORMALIZE_COUNT; ++i) {
      dilate(image, width, height, FOREGROUND_COLOR, BACKGROUND_COLOR);
    }
    for (int j = 0; j < NORMALIZE_COUNT * 4; ++j) {
      erode(image, width, height, FOREGROUND_COLOR, BACKGROUND_COLOR);
    }
    for (int j = 0; j < NORMALIZE_COUNT * 3 - NORMALIZE_COUNT; ++j) {
      dilate(image, width, height, FOREGROUND_COLOR, BACKGROUND_COLOR);
    }

    return image;
  }

  /**
   * Normalizes the reference marker with Mathematical Morphology. That means artifacts in an image are removed.
   * Combination of  dilate + erode + dilate iterations.
   * @param image The image which should be normalized.
   * @param width The image width.
   * @param height  The image height.
   * @return Returns the normalized image a given amount of iterations.
   */
  public static int[][] normalizeCoins(int[][] image, int width, int height) {
    for (int i = 0; i < NORMALIZE_COUNT * 2; ++i) {
      dilate(image, width, height, FOREGROUND_COLOR, BACKGROUND_COLOR);
    }
    for (int i = 0; i < NORMALIZE_COUNT * 4; ++i) {
      erode(image, width, height, FOREGROUND_COLOR, BACKGROUND_COLOR);
    }
    for (int i = 0; i < NORMALIZE_COUNT * 2; ++i) {
      dilate(image, width, height, FOREGROUND_COLOR, BACKGROUND_COLOR);
    }
    return image;
  }

  /**
   * Implementation of the Mathematical Morphology method "dilate".
   * @param image The input image which should be dilated.
   * @param width The image width.
   * @param height The image height.
   * @param foregroundColor The foreground color.
   * @param backgroundColor The background color.
   */
  private static void dilate(int[][] image, int width, int height, int foregroundColor, int backgroundColor) {
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        if (image[x][y] == foregroundColor) {
          if (x > 0 && image[x - 1][y] == backgroundColor) {
            image[x - 1][y] = MARKER;
          }
          if (y > 0 && image[x][y - 1] == backgroundColor) {
            image[x][y - 1] = MARKER;
          }
          if (x + 1 < width && image[x + 1][y] == backgroundColor) {
            image[x + 1][y] = MARKER;
          }
          if (y + 1 < height && image[x][y + 1] == backgroundColor) {
            image[x][y + 1] = MARKER;
          }
        }
      }
    }
    for (int i = 0; i < image.length; i++) {
      for (int j = 0; j < image[i].length; j++) {
        if (image[i][j] == -1) {
          image[i][j] = foregroundColor;
        }
      }
    }
  }

  /**
   * Implementation of the Mathematical Morphology method "erode".
   * @param image The input image which should be dilated.
   * @param width The image width.
   * @param height The image height.
   * @param foregroundColor The foreground color.
   * @param backgroundColor The background color.
   */
  private static void erode(int[][] image, int width, int height, int foregroundColor, int backgroundColor) {
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        if (image[x][y] == foregroundColor) {
          if ((x > 0 && image[x - 1][y] == backgroundColor) || (y > 0 && image[x][y - 1] == backgroundColor) ||
              (x + 1 < width && image[x + 1][y] == backgroundColor) || (y + 1 < height && image[x][y + 1] == backgroundColor)) {
            image[x][y] = MARKER;
          }
        }
      }
    }
    for (int i = 0; i < image.length; i++) {
      for (int j = 0; j < image[i].length; j++) {
        if (image[i][j] == MARKER) {
          image[i][j] = backgroundColor;
        }
      }
    }
  }

  /**
   *  Returns a list with a maximum of one seed point. The list is needed to remove unnecessary add() calls.
   * @param transformedImage
   * @return Returns an empty list if no seedPoint is found - otherwise the starting point for the region growing is returned.
   */
  private static List<Point> getSeedPoints(int[][][] transformedImage) {
    List<Point> seedPoints = new ArrayList<>(1);
    for (int x = 0; x < transformedImage.length; ++x) {
      for (int y = 0; y < transformedImage[0].length; ++y) {
        if (transformedImage[x][y][0] == MARKER) {
          seedPoints.add(new Point(x, y));
          return seedPoints;
        }
      }
    }
    return seedPoints;
  }

  /**
   * Applies for all given points the background color (0) to the input image.
   * @param image The input image which gets the background color set.
   * @param points The points where the background color is applied.
   */
  public static void applyReferencePoints(int[][] image, List<Point> points) {
    for (Point point : points) {
      image[point.getX()][point.getY()] = BACKGROUND_COLOR;
    }
  }


  /**
   * Fills the image on the given points with the background color.
   * @param image The RGB image.
   * @param points The points which should be set to black background color.
   */
  public static void fillWithBackgroundColor(int[][][] image, List<Point> points) {
    for (Point point : points) {
      setColor(image, point.getX(), point.getY(), BACKGROUND);
    }
  }

  /**
   * Runs regino growing on a given input image and saves all operations in the segmentation result image.
   * @param coinImage The input image.
   * @param segmentationResultImage The segementation result.
   * @return Returns a Map of IDs for each coin and all their pixels represented in a list of points.
   */
  public static Map<Integer, List<Point>> rgbRegionGrowing(int[][] coinImage, int[][][] segmentationResultImage) {
    Map<Integer, List<Point>> idLabels = new HashMap<>();
    Stack<Point> processingStack = new Stack<>();
    initSegmentation(coinImage, segmentationResultImage);
    List<Point> seedPoints = getSeedPoints(segmentationResultImage);

    int idSequence = 1;
    while (!seedPoints.isEmpty()) {
      Point firstSeedPoint = seedPoints.get(0);
      int initVal = coinImage[firstSeedPoint.getX()][firstSeedPoint.getY()];
      double tolerance = FOREGROUND_COLOR * 0.1; // 10 % tolerance 25
      int lowerThreshold = Math.max(0, (int) (initVal - tolerance / 2.0 + 0.5));
      int upperThreshold = Math.min(255, (int) (initVal + tolerance / 2.0 + 0.5));
      RGBColor currentColor = generateRandomRGB();
      List<Point> foundPoints = new ArrayList<>();
      for (Point actualPoint : seedPoints) {
        int currentValue = coinImage[actualPoint.getX()][actualPoint.getY()];
        if (segmentationResultImage[actualPoint.getX()][actualPoint.getY()][0] == MARKER) {
          if (currentValue >= lowerThreshold && currentValue <= upperThreshold) {
            setColor(segmentationResultImage, actualPoint.getX(), actualPoint.getY(), currentColor);
            processingStack.add(actualPoint);
            foundPoints.add(actualPoint);
          } else {
            setColor(segmentationResultImage, actualPoint.getX(), actualPoint.getY(), BACKGROUND);
          }
        }
      }
      seedPoints.remove(0);
      //expand and grow
      while (!processingStack.isEmpty()) {
        Point actualPoint = processingStack.pop();
        //expanding N4 + ND  = N8
        for (int xOffset = -1; xOffset <= 1; ++xOffset) {
          for (int yOffset = -1; yOffset <= 1; ++yOffset) {
            int neighbourX = actualPoint.getX() + xOffset;
            int neighbourY = actualPoint.getY() + yOffset;
            //change if we are still in boundaries
            if (neighbourX >= 0 && neighbourY >= 0 && neighbourX < segmentationResultImage.length && neighbourY < segmentationResultImage[0].length) {
              int currentValue = coinImage[neighbourX][neighbourY];
              if (segmentationResultImage[neighbourX][neighbourY][0] == MARKER) {
                if (currentValue >= lowerThreshold && currentValue <= upperThreshold) {
                  setColor(segmentationResultImage, neighbourX, neighbourY, currentColor);
                  Point point = new Point(neighbourX, neighbourY);
                  processingStack.add(point);
                  foundPoints.add(point);
                } else {
                  setColor(segmentationResultImage, neighbourX, neighbourY, BACKGROUND);
                }
              }
            }
          }
        }
      }
      if (MIN_THRESHOLD <= foundPoints.size()) {
        idLabels.put(idSequence, foundPoints);
      } else {
        fillWithBackgroundColor(segmentationResultImage, foundPoints);
      }
      seedPoints = getSeedPoints(segmentationResultImage);
      ++idSequence;
    }
    return idLabels;
  }

  /**
   * Sets a RGB color in a RGB image.
   * @param segmentedImg The RGB image.
   * @param x The width position in the image.
   * @param y The height position in the image.
   * @param color The RGB value which should be applied on this pixel.
   */
  private static void setColor(int[][][] segmentedImg, int x, int y, RGBColor color) {
    segmentedImg[x][y][0] = color.getRed();
    segmentedImg[x][y][1] = color.getGreen();
    segmentedImg[x][y][2] = color.getBlue();
  }

  /**
   * Initalizes the segemtnation image with MARKERS. These are an indication for the region growing algorithm, that the pixel
   * has not been processed yet.
   * @param image The input image.
   * @param segmentationResult The segmentation image as RGB image.
   */
  public static void initSegmentation(int[][] image, int[][][] segmentationResult) {
    for (int x = 0; x < image.length; ++x) {
      for (int y = 0; y < image[0].length; ++y) {
        if (image[x][y] == FOREGROUND_COLOR) {
          for (int z = 0; z < RGB_CHANNELS; ++z) {
            segmentationResult[x][y][z] = MARKER;
          }
        }
      }
    }
  }

  /**
   * Generates a random RGB color.
   * @return Returns a RGB color which is randomly generated
   */
  private static RGBColor generateRandomRGB() {
    Random random = new Random(System.nanoTime());
    int red = random.nextInt(FOREGROUND_COLOR + 1) + 1;
    int green = random.nextInt(FOREGROUND_COLOR + 1) + 1;
    int blue = random.nextInt(FOREGROUND_COLOR + 1) + 1;
    return new RGBColor(red, green, blue);
  }

  /**
   * Converts a RGB image to a HSB image.
   * @param rgbImage The input RGB image.
   * @return Returns a HSB image.
   */
  public static float[][][] convertRGBToHSB(int[][][] rgbImage) {
    float[][][] hsbImage = new float[rgbImage.length][rgbImage[0].length][RGB_CHANNELS];
    float[] hsbValues = new float[3];
    for (int x = 0; x < rgbImage.length; ++x) {
      for (int y = 0; y < rgbImage[0].length; ++y) {
        Color.RGBtoHSB(rgbImage[x][y][0], rgbImage[x][y][1], rgbImage[x][y][2], hsbValues);
        System.arraycopy(hsbValues, 0, hsbImage[x][y], 0, RGB_CHANNELS);
      }
    }
    return hsbImage;
  }

  /**
   * Calculates all sums of the coins from the region labelling.
   * @param regionLabels The regions which are labelled.
   * @param hsbImage The HSB image representation of the original RGB image.
   * @param scalingFactor The scaling factor for each coins.
   * @return Returns the calculated total sum of all coins in the image.
   */
  public static double calcSumFromRegions(Map<Integer, List<Point>> regionLabels, float[][][] hsbImage, double scalingFactor) {
    AtomicReference<Double> sum = new AtomicReference<>(0.0);
    regionLabels.forEach((id, points) -> {
      int area = points.size();
      double scaledDiameter = MathUtils.scale(MathUtils.calculateDiameter(area), scalingFactor);
      Optional<Coin> optionalCoin;
      if (isGold(hsbImage, points)) {
        optionalCoin = goldCoins.stream().min((c1, c2) -> Double.compare(Math.abs(scaledDiameter - c1.getDiameter()), Math.abs(scaledDiameter - c2.getDiameter())));
      } else {
        optionalCoin = copperCoins.stream().min((c1, c2) -> Double.compare(Math.abs(scaledDiameter - c1.getDiameter()), Math.abs(scaledDiameter - c2.getDiameter())));
      }
      Coin coin = optionalCoin.orElse(new Coin(0.0, 0.0));
      sum.updateAndGet(value -> value + coin.getValue());
    });
    return sum.get();
  }

  /**
   * Determines if a coin in the HSB image is a gold or a copper one.
   * The algorithm calculates the whole pixel Hue value and calculates the average. Regarding to the Hue value,
   * it can be determines if the coin is golden or copper.
   * @param hsbImage The HSB image.
   * @param points All pixels of the coin.
   * @return Returns true, if the coin is golden, otherwhise false.
   */
  private static boolean isGold(float[][][] hsbImage, List<Point> points) {
    float count = points.size();
    AtomicReference<Float> h = new AtomicReference<>(0.0f);
    points.forEach(point -> h.updateAndGet(value -> (value + hsbImage[point.getX()][point.getY()][0])));
    h.updateAndGet(value -> value / count);
    return h.get() >= 0.12f;
  }

}
