import java.awt.Point;

/**
 * This class represents a blob
 * @author Juneidy Wibowo
 */
public class Blob {
	/**
	 * Top-left pixel of the object
	 */
	private Point topLeft;
	/**
	 * Bottom-right pixel of the object
	 */
	private Point bottomRight;
	private int brightness;
	private double brightnessRatio;

	/**
	 * Construct with given top-left and bottom-right point
	 */
	public Blob(Point inTopLeft, Point inBottomRight) {
		topLeft = inTopLeft;
		bottomRight = inBottomRight;
	}

	public double getBrightnessRatio(Image i){
		getBrightness(i);
		return brightnessRatio;
	}

	public int getBrightness(Image i){
		int[][] g = i.getGrey();
		brightness = 0;
		for(int ii = getTop(); ii <= bottomRight.y; ii++){
			for(int jj = getLeft(); jj <= bottomRight.x; jj++){
				brightness += g[ii][jj];
			}
		}
		double max = getHeight() * getWidth() * 255f;
		brightnessRatio = (double)brightness / max;
		return brightness;
	}

	public int getBrightness(){
		return brightness;
	}

	public int getTop() {
		return topLeft.y;
	}

	public int getBottom() {
		return bottomRight.y;
	}

	public int getLeft() {
		return topLeft.x;
	}

	public int getRight() {
		return bottomRight.x;
	}

	public int getHeight(){
		return bottomRight.y - topLeft.y + 1;
	}

	public int getWidth(){
		return bottomRight.x - topLeft.x + 1;
	}

	public boolean isReasonableSize(){
		return getHeight() > 10 && getWidth() > 10;
	}

	public boolean isReasonableInventorySize(){
		return getWidth() > 200 && getHeight() > 250;
	}

	public boolean isReasonableDismantleSize(){
		return getWidth() > 300 && getHeight() > 400;
	}

	public Blob setTop(int inTop) {
		topLeft.y = inTop;
		return this;
	}

	public Blob setBottom(int inBottom) {
		bottomRight.y = inBottom;
		return this;
	}

	public Blob setLeft(int inLeft) {
		topLeft.x = inLeft;
		return this;
	}

	public Blob setRight(int inRight) {
		bottomRight.x = inRight;
		return this;
	}

	public String toString() {
		return "Top is " + getTop() + ", bottom is " + getBottom() +
			", left is " + getLeft() + ", right is " + getRight() +
			", height is " + getHeight() + ", width is " + getWidth() +
			", brightness is " + brightness + ", brightness ratio is " + brightnessRatio;
	}
}   
