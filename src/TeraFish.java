import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;

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
	public static final boolean debug = false;
	public static final String FORMAT = "png";
	public static final File debugOutput = new File("/tmp/foo");
	public static final int[] WHITE = new int[]{ 255, 255, 255 };

	private static final Rectangle SCREEN = new Rectangle(0, 0, 1920, 1080);
	private static final int[] INVENT_FRAME = new int[]{ 22, 28, 35 };
	private static final int[] DISMANTLE_AREA = new int[]{ 2, 11, 19 };
	private static Inventory main;
	private static Inventory pet;

	public static void main(String[] args){
		try{
			if(debug){
				long startTime = System.currentTimeMillis();

				//continueFish();

				//Image i = new Image(r.createScreenCapture(gaugeFrame));

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

				//Image i = new Image(screenshot());
				//Image i = Image.loadTestImage("pet-small.png");
				//i.cacheGrey(
				//	Preprocess.filterColour(i, INVENT_FRAME, 1)
				//);
				//Blob[] blobs = Blobbing.getBlobs(
				//	i,
				//	b -> b.isReasonableInventorySize()
				//);
				//Blob inventory = null;
				//for(Blob b : blobs){
				//	b.getBrightness(i);
				//	System.out.println(b.toString());
				//	if(inventory==null){
				//		inventory = b;
				//	}else if(inventory.getBrightness() < b.getBrightness()){
				//		inventory = b;
				//	}
				//}

				//Inventory inv = new Inventory(i.crop(inventory), inventory);

				Image i = Image.loadTestImage("dismantling-empty.png");
				i.cacheGrey(
					Preprocess.filterColour(i, DISMANTLE_AREA, 0)
				);
				Blob[] blobs = Blobbing.getBlobs(i);
				for(Blob b : blobs){
					b.getBrightness(i);
					System.out.println(b.toString());
				}
				i.toGrey();

				//dismantle offset x: +70, y: +50

				//inv.matches(Image.loadRgbImage("src/fish6-mottled-ray.tpl"), false);
				//inv.matches(Image.loadRgbImage("src/bait5.tpl"), true);

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
				ImageIO.write(i.toBufferedImage(), FORMAT, debugOutput);
			}else{
				System.out.println("System is starting in 3 seconds");
				Thread.sleep(3000);

				pressKey(KeyEvent.VK_I);
				//pressKey(KeyEvent.VK_X);

				Thread.sleep(2000);

				Image i = new Image(screenshot());
				i.cacheGrey(Preprocess.filterColour(i, INVENT_FRAME, 1));
				Blob[] blobs = Blobbing.getBlobs(
					i,
					b -> b.isReasonableInventorySize()
				);

				Inventory[] inventories = Arrays.stream(blobs)
					.map(b -> new Inventory(i.crop(b), b))
					.filter(inv -> inv.getType()!=Inventory.Type.UNKNOWN)
					.toArray(Inventory[]::new);

				for(Inventory in : inventories){
					if(in.getType()==Inventory.Type.MAIN){
						main = in;
					}else if(in.getType()==Inventory.Type.PET){
						pet = in;
					}
				}

				click(main.getDismantlePos(), InputEvent.BUTTON1_DOWN_MASK);

				Image dismantle = new Image(screenshot());
				i.cacheGrey(
					Preprocess.filterColour(i, DISMANTLE_AREA, 0)
				);
				blobs = Blobbing.getBlobs(
					i,
					b -> b.isReasonableDismantleSize()
				);

				Blob dis;
				if(blobs.length==1){
					dis = blobs[0];
				}

				main.dismantle(Image.loadRgbImage("fish6-mottled-ray.tpl"));

				//while(true){
				//	Fishing.fish();
				//}
				Thread.sleep(120000);
			}
		}catch(Exception ex){
			System.out.println("exception " + ex);
			ex.printStackTrace();
		}
	}
	public static void pressKey(int key)throws InterruptedException{
		pressKey(key, 200);
	}
	public static void pressKey(int key, int millis)throws InterruptedException{
		r.keyPress(key);
		Thread.sleep(millis);
		r.keyRelease(key);
	}
	public static void click(Point p, int key)throws InterruptedException{
		System.out.println("moving to " + p.x + ", " + p.y);
		r.mouseMove(p.x, p.y);
		Thread.sleep(100);
		r.mouseMove(p.x + 1, p.y + 1);
		Thread.sleep(200);
		System.out.println("Clicking");
		r.mousePress(key);
		r.mouseRelease(key);
	}
	public static void printDebug(Image i, Blob[] blobs)throws IOException{
		i.toGrey();
		for(Blob blob : blobs){
			System.out.println(blob.toString());
			i.drawBox(WHITE, blob);
		}
		ImageIO.write(i.toBufferedImage(), FORMAT, debugOutput);
	}
	public static BufferedImage screenshot(){
		return screenshot(SCREEN);
	}
	public static BufferedImage screenshot(Rectangle rec){
		return r.createScreenCapture(rec);
	}
}
