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

	/**
	 * Construct with given top-left and bottom-right point
	 */
	public Blob(Point inTopLeft, Point inBottomRight) {
		topLeft = inTopLeft;
		bottomRight = inBottomRight;
	}

	public int calculateWhiteness(Image i){
		int[][] g = i.getGrey();
		brightness = 0;
		for(int ii = getTop(); ii <= bottomRight.y; ii++){
			for(int jj = getLeft(); jj <= bottomRight.x; jj++){
				brightness += g[ii][jj];
			}
		}
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

	public boolean isReasonableSize(){
		return (
			(getBottom() - getTop() > 10) &&
			(getRight() - getLeft() > 10)
		);
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
			", brightness is " + brightness;
	}
}   
