import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TeraFish{
	public static Robot r;
	static{
		try{
			r = new Robot();
		}catch(AWTException ex){
			System.out.println("Error creating robot " + ex);
			System.exit(1);
		}
	}

	// Debug vars
	public static final boolean debug = true;
	public static final String FORMAT = "png";
	public static final File debugOutput = new File("/tmp/foo");
	public static final int[] WHITE = new int[]{ 255, 255, 255 };

	private static final Rectangle SCREEN = new Rectangle(0, 0, 1920, 1080);
	private static final int[] INVENT_FRAME = new int[]{ 22, 28, 35 };

	public static void main(String[] args){
		try{
			if(debug){
				long startTime = System.currentTimeMillis();

				//continueFish();

				//Image i = new Image(r.createScreenCapture(gaugeFrame), false);

				//double ratio = Preprocess.sumOfAbsoluteRatio(
				//	gaugeFrameTpl,
				//	i.getRgb()
				//);

				//System.out.println("ratio is " + ratio);
				//if(ratio < 0.01){
				//	System.out.println("not yet!");
				//}else{
				//	System.out.println("Finished fishing!");
				//}

				//ImageIO.write(i.toBufferedImage(), FORMAT, debugOutput);

				//Image i = new Image(screenshot(SCREEN), false);
				Image i = Image.loadTestImage("inventory-big.png");
				i.cacheGrey(
					Preprocess.filterColour(i, INVENT_FRAME, 1)
				);
				Blob[] blobs = Blobbing.getBlobs(i);
				Blob inventory = null;
				for(Blob b : blobs){
					b.getBrightness(i);
					System.out.println(b.toString());
					if(inventory==null){
						inventory = b;
					}else if(inventory.getBrightness() < b.getBrightness()){
						inventory = b;
					}
				}

				Inventory inv = new Inventory(i.crop(inventory), inventory);

				//inv.matches(Image.loadRgbImage("src/fish6-mottled-ray.tpl"), false);
				inv.matches(Image.loadRgbImage("src/bait5.tpl"), true);

				//System.out.println(
				//	Preprocess.sumOfAbsoluteRatio(
				//		Preprocess.resize(
				//			inv.crop(getInventSlot(3, 0)),
				//			tpl.width,
				//			tpl.height
				//		).getRgb(),
				//		tpl.getRgb()
				//	)
				//);

				//Image iFish = inv.crop(getInventSlot(1, 1));

				//Image bait = inv.crop(3, 2, true);
				// active ignore border buffer 0.12
				//bait = bait.crop(0, 0, bait.width, (int)(bait.height * 0.74));

				long executionTime = System.currentTimeMillis() - startTime;
				System.out.println("Execution time: " + executionTime + "ms");
				//ImageIO.write(bait.toBufferedImage(), FORMAT, debugOutput);
			}else{
				System.out.println("System is starting in 3 seconds");
				Thread.sleep(3000);
				while(true){
					Fishing.fish();
				}
			}
		}catch(Exception ex){
			System.out.println("exception " + ex);
			ex.printStackTrace();
		}
	}
	public static void pressKey(int key, int millis)throws InterruptedException{
		r.keyPress(key);
		Thread.sleep(millis);
		r.keyRelease(key);
	}
	public static void printDebug(Image i, Blob[] blobs)throws IOException{
		i.toGrey();
		for(Blob blob : blobs){
			System.out.println(blob.toString());
			i.drawBox(WHITE, blob);
		}
		ImageIO.write(i.toBufferedImage(), FORMAT, debugOutput);
	}
	public static BufferedImage screenshot(Rectangle rec){
		return r.createScreenCapture(rec);
	}
}
