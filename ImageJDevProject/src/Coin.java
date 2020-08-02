import java.util.Objects;

/**
 * Represents a pixel in an image.
 *
 * @author  Michael Eder
 * @version 1.0
 * @since   2020-06-14
 */
public class Coin {

  private double value;
  private double diameter;
  private boolean isGold;

 public Coin(double value, double diameter) {
   this(value, diameter, false);
 }

  public Coin(double value, double diameter, boolean isGold) {
    this.value = value;
    this.diameter = diameter;
    this.isGold = isGold;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

  public double getDiameter() {
    return diameter;
  }

  public void setDiameter(double diameter) {
    this.diameter = diameter;
  }

  public boolean isGold() {
    return isGold;
  }

  public void setGold(boolean gold) {
    isGold = gold;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Coin coin = (Coin) o;
    return Double.compare(coin.value, value) == 0 &&
        Double.compare(coin.diameter, diameter) == 0 &&
        isGold == coin.isGold;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, diameter, isGold);
  }
}
