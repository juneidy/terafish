import java.awt.Rectangle;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Image {
	private int[] rgb;
	public final int height;
	public final int width;
	private int[][] grey;
	private int type;

	public Image(BufferedImage buf, boolean isGrey){
		WritableRaster raster = buf.getRaster();
		height = raster.getHeight();
		width = raster.getWidth();
		type = buf.getType();

		rgb = new int[3 * height * width];
		raster.getPixels(0, 0, width, height, rgb);
		if(isGrey){
			grey = new int[height][width];
			for(int ii = height - 1; ii >= 0; ii--){
				for(int jj = width - 1; jj >= 0; jj--){
					int rgbPos = (ii * width + jj) * 3;
					grey[ii][jj] = rgb[rgbPos];
				}
			}
		}
	}

	public Image(int h, int w, int[] rgb){
		height = h;
		width = w;
		this.rgb = rgb;
		type = BufferedImage.TYPE_INT_RGB;
	}

	/**
	 * Returns a copy of cropped image
	 */
	public Image crop(Blob b){
		int height = b.getHeight();
		int width = b.getWidth();
		int[] rgb = new int[3 * height * width];

		int top = b.getTop();
		int left = b.getLeft();
		for(int ii = height - 1; ii >= 0; ii--){
			for(int jj = width - 1; jj >= 0; jj--){
				int origPos = ((ii + top) * this.width + (jj + left)) * 3;
				int croppedPos = (ii * width + jj) * 3;
				for(int kk = 2; kk >= 0; kk--){
					rgb[croppedPos + kk] = this.rgb[origPos + kk];
				}
			}
		}

		return new Image(height, width, rgb);
	}

	/**
	 * Returns a copy of cropped image
	 */
	public Image crop(int left, int top, int w, int h){
		int[] rgb = new int[3 * h * w];
		for(int ii = h - 1; ii >= 0; ii--){
			for(int jj = w - 1; jj >= 0; jj--){
				int origPos = ((ii + top) * width + (jj + left)) * 3;
				int croppedPos = (ii * w + jj) * 3;
				for(int kk = 2; kk >= 0; kk--){
					rgb[croppedPos + kk] = this.rgb[origPos + kk];
				}
			}
		}

		return new Image(h, w, rgb);
	}

	public int[] getRgb(){
		return rgb;
	}

	public void setRgb(int[] rgb){
		this.rgb = rgb;
	}

	public void setRgb(int[][] grey){
		for (int ii = 0; ii < height; ii++) {
			for (int jj = 0; jj < width; jj++) {
				int rgbPos = (ii * width + jj) * 3;
				int c = grey[ii][jj];
				for (int kk = 0; kk < 3; kk++) {
					rgb[rgbPos + kk] = c;
				}
			}
		}
	}

	public void cacheGrey(){
		grey = new int[height][width];
		for(int ii = height - 1; ii >= 0; ii--){
			for(int jj = width - 1; jj >= 0; jj--){
				int rgbPos = (ii * width + jj) * 3;
				grey[ii][jj] = (
					(rgb[rgbPos] + rgb[rgbPos + 1] + rgb[rgbPos + 2]) / 3
				);
			}
		}
	}

	public void cacheGrey(int[][] grey){
		this.grey = grey;
	}

	public int[][] getGrey(){
		return grey;
	}

	public void toGrey(){
		setRgb(grey);
	}

	/**
	 * Get a copy of BufferedImage representation of this Image object
	 */
	public BufferedImage toBufferedImage(){
		BufferedImage buf = new BufferedImage(width, height, type);
		WritableRaster raster = buf.getRaster();

		raster.setPixels(0, 0, width, height, rgb);
		return buf;
	}

	/**
	 * Load Greyscale tpl.
	 */
	public static int[][] loadGreyTemplate(String f)throws IOException{
		return (new Image(ImageIO.read(new File(f)), true)).getGrey();
	}

	/**
	 * Load RGB tpl.
	 */
	public static int[] loadRgbTemplate(String f)throws IOException{
		WritableRaster raster = ImageIO.read(new File(f)).getRaster();
		int height = raster.getHeight();
		int width = raster.getWidth();

		int[] rgb = new int[3 * height * width];
		raster.getPixels(0, 0, width, height, rgb);
		return rgb;
	}

	public static Image loadRgbImage(String f)throws IOException{
		WritableRaster raster = ImageIO.read(new File(f)).getRaster();
		int height = raster.getHeight();
		int width = raster.getWidth();

		int[] rgb = new int[3 * height * width];
		raster.getPixels(0, 0, width, height, rgb);

		return new Image(height, width, rgb);
	}

	/**
	 * DEBUG
	 * Draw a 1px thick box to the image
	 */
	public Image drawBox(int[] c, Blob b) {
		return drawBox(c, b.getTop(), b.getLeft(), b.getBottom(), b.getRight());
	}

	/**
	 * DEBUG
	 * Draw a 1px thick box to the image
	 */
	public Image drawBox(int[] c, Rectangle r) {
		return drawBox(
			c,
			(int)r.getMinY(),
			(int)r.getMinX(),
			(int)r.getMaxY(),
			(int)r.getMaxX()
		);
	}

	/**
	 * DEBUG
	 * Draw a 1px thick box to the image
	 */
	public Image drawBox(
		int[] c,
		int top,
		int left,
		int bottom,
		int right
	) {
		// Draw vertical bounding box
		for (int ii = top; ii <= bottom; ii++) {
			int l = (ii * width + left) * 3;
			int r = (ii * width + right) * 3;
			for (int jj = 0; jj < 3; jj++){
				rgb[l+jj] = c[jj];
				rgb[r+jj] = c[jj];
			}
		}

		// Cache the Y axis
		top = top * width;
		bottom = bottom * width;
		// Draw horizontal bounding box
		for (int ii = left; ii <= right; ii++) {
			int t = (top + ii) * 3;
			int b = (bottom + ii) * 3;
			for (int jj = 1; jj < 3; jj++){
				rgb[t+jj] = c[jj];
				rgb[b+jj] = c[jj];
			}
		}
		return this;
	}

	/**
	 * DEBUG
	 * so that it's easy to load test image.
	 */
	public static Image loadTestImage(String f)throws IOException{
		BufferedImage buf = ImageIO.read(new File("testfile/" + f));
		if(buf.getType()!=BufferedImage.TYPE_INT_RGB){
			long startTime = System.currentTimeMillis();
			BufferedImage tmp = new BufferedImage(
				buf.getWidth(),
				buf.getHeight(),
				BufferedImage.TYPE_INT_RGB
			);
			tmp.getGraphics().drawImage(buf, 0, 0, null);
			buf = tmp;
			long executionTime = System.currentTimeMillis() - startTime;
			System.out.println("Converting image took: " + executionTime + "ms");
		}
		return new Image(buf, true);
	}
}
