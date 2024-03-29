import java.awt.Point;

/**
 * This class represents a blob
 * @author Juneidy Wibowo
 */
public class Blob {
	private Point topLeft;
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

	/**
	 * Calculate and return the brightness ratio of the blob in the image.
	 * @param i The image to calculate.
	 */
	public double getBrightnessRatio(Image i){
		getBrightness(i);
		return brightnessRatio;
	}

	/**
	 * Calculate and return the brightness of the blob in the image.
	 * @param i The image to calculate.
	 */
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

	/**
	 * Minimum reasonable blob size
	 */
	public boolean isReasonableSize(){
		return getHeight() > 10 && getWidth() > 10;
	}

	/**
	 * Minimum reasonable inventory size
	 */
	public boolean isReasonableInventorySize(){
		return getWidth() > 200 && getHeight() > 250;
	}

	/**
	 * Minimum reasonable dismantle size
	 */
	public boolean isReasonableDismantleSize(){
		return getWidth() > 300 && getHeight() > 400;
	}

	/**
	 * Minimum reasonable withdraw bait OK area size
	 */
	public boolean isReasonableBaitOkSize(){
		int w = getWidth();
		int h = getHeight();
		return w > 370 && w < 410 && h > 30 && h < 42;
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

	// For debug only
	public String toString() {
		return "Top is " + getTop() + ", bottom is " + getBottom() +
			", left is " + getLeft() + ", right is " + getRight() +
			", height is " + getHeight() + ", width is " + getWidth() +
			", brightness is " + brightness + ", brightness ratio is " + brightnessRatio;
	}
}   
