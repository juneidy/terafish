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
import java.util.LinkedList;
import java.util.Properties;

import javax.imageio.ImageIO;

public class TeraFish{
	private static final Rectangle SCREEN = new Rectangle(0, 0, 1920, 1080);
	private static final int[] INVENT_FRAME = new int[]{ 22, 28, 35 };
	private static final int[] DISMANTLE_AREA = new int[]{ 2, 11, 19 };
	private static final int[] BAIT_NUMBER = new int[]{ 16, 20, 24 };
	private static Inventory main;
	private static Inventory pet;

	private static final Image[] fishToDismantle;
	private static final Image[] bait = Templates.getBait(Config.BAIT);
	private static final Image[] fillet = new Image[]{ Templates.FILLET };
	public static final Robot r;
	static{
		Robot tmpR = null;
		Image[] tmpDismantle = null;
		try{
			tmpR = new Robot();
			tmpDismantle = Templates.getFishMatch(Config.LOCATION);
		}catch(AWTException ex){
			System.out.println("Error creating robot " + ex);
			System.exit(1);
		}catch(IOException ex){
			System.out.println("Error loading template " + ex);
			System.exit(1);
		}finally{
			r = tmpR;
			fishToDismantle = tmpDismantle;
		}
	}

	// Debug vars
	public static final boolean debug = false;
	public static final String FORMAT = "png";
	public static final File debugOutput = new File("/tmp/foo");
	public static final int[] WHITE = new int[]{ 255, 255, 255 };

	public static void main(String[] args){
		try{
			if(debug){
				long startTime = System.currentTimeMillis();
				debugCode();
				long executionTime = System.currentTimeMillis() - startTime;
				System.out.println("Execution time: " + executionTime + "ms");
				//Thread.sleep(120000);
			}else{
				System.out.println("Initiating for location " + Config.LOCATION);
				System.out.println("Using bait " + Config.BAIT);
				System.out.println("Dismantling after " + Config.DISMANTLE_CAP + " fish");
				System.out.println("System is starting in 3 seconds");
				Thread.sleep(3000);

				if(fishToDismantle!=null){
					initState();
				}

				int fished = 0;
				int success = 0;
				int failed = 0;
				int golden = 0;
				while(true){
					if(fished < Config.DISMANTLE_CAP){
						fished++;
						if(Fishing.fish()){
							success++;
						}else{
							failed++;
						}
						if(Fishing.golden){
							golden++;
						}
						System.out.println(
							String.format(
								"Success: %04d, Failed: %04d, Golden: %04d",
								success,
								failed,
								golden
							)
						);
					}else{
						if(fishToDismantle!=null){
							dismantle();
							reloadBait();
						}
						fished = 0;
					}
				}
			}
		}catch(Exception ex){
			System.out.println("exception " + ex);
			ex.printStackTrace();
		}
	}
	private static void initState()throws InterruptedException{
		resetMouse();
		pressKey(Config.PET_KEY);
		Thread.sleep(1000);
		pressKey(Config.PET_STORAGE_KEY);
		Thread.sleep(1000);
		findInventories();
		Thread.sleep(1000);
		pressKey(Config.PET_KEY);
		Thread.sleep(1000);
	}
	public static void pressKey(int key)throws InterruptedException{
		pressKey(key, 200);
	}
	public static void pressKey(int key, int millis)throws InterruptedException{
		r.keyPress(key);
		Thread.sleep(millis);
		r.keyRelease(key);
	}
	public static void click(Point pos, int key)throws InterruptedException{
		click(pos.x, pos.y, key);
	}
	private static void resetMouse()throws InterruptedException{
		r.mouseMove(0, 1079);
	}
	private static void mouseMove(int x, int y)throws InterruptedException{
		r.mouseMove(x, y);
		Thread.sleep(100);
		r.mouseMove(x + 1, y + 1);
	}
	public static void click(int x, int y, int key)throws InterruptedException{
		if(debug){
			System.out.println("Clicking x: " + x + ", y: " + y);
		}else{
			mouseMove(x, y);

			Thread.sleep(200);

			r.mousePress(key);
			r.mouseRelease(key);
		}
	}
	public static void dragAndDrop(Point a, Point b)throws InterruptedException{
		int key = InputEvent.BUTTON1_DOWN_MASK;
		mouseMove(a.x, a.y);
		Thread.sleep(500);
		r.mousePress(key);
		Thread.sleep(500);
		mouseMove(b.x, b.y);
		Thread.sleep(500);
		r.mouseRelease(key);
	}
	public static Image screenshot(){
		return screenshot(SCREEN);
	}
	public static Image screenshot(Rectangle rec){
		return new Image(r.createScreenCapture(rec), false);
	}

	private static void findInventories(){
		findInventories(screenshot());
	}
	private static Blob[] getInventoryBlob(Image i){
		i.cacheGrey(Preprocess.filterColour(i, INVENT_FRAME, 1));
		return Blobbing.getBlobs(
			i,
			b -> b.isReasonableInventorySize()
		);
	}
	private static void findInventories(Image i){
		Blob[] blobs = getInventoryBlob(i);

		Inventory[] inventories = Arrays.stream(blobs)
			.map(b -> new Inventory(i.crop(b), b))
			.filter(inv -> !inv.isUnknown())
			.toArray(Inventory[]::new);

		for(Inventory in : inventories){
			if(in.isMain()){
				main = in;
			}else if(in.isPet()){
				pet = in;
			}
		}
	}
	private static void dismantle()throws InterruptedException, IOException{
		resetMouse();
		Blob[] blobs;
		Image i;

		Thread.sleep(1000);
		i = screenshot();
		updateInventory(i);

		main.openDismantle();

		Thread.sleep(1000);

		i = screenshot();
		i.cacheGrey(Preprocess.filterColour(i, DISMANTLE_AREA, 1));
		blobs = Blobbing.getBlobs(
			i,
			b -> b.isReasonableDismantleSize()
		);
		Blob dismantleBlob;
		if(blobs.length==1){
			dismantleBlob = blobs[0];
		}else{
			throw new IllegalStateException("failed to detect dismantle");
		}

		Point dismantleBtn = new Point(
			dismantleBlob.getLeft() + 70,
			dismantleBlob.getBottom() + 50
		);

		selectAndDismantle(dismantleBtn);
		// Try it again just to be sure
		resetMouse();
		updateInventory(screenshot());
		selectAndDismantle(dismantleBtn);
		resetMouse();
	}
	private static void updateInventory(Image i){
		Blob[] blobs = getInventoryBlob(i);
		int ii = blobs.length - 1;
		while(ii >=0 && !main.update(i, blobs[ii])){ ii--; }
	}
	private static void selectAndDismantle(Point dismantle)throws InterruptedException, IOException{
		LinkedList<int[]> matches = main.matches(fishToDismantle, false);

		int inserted = 0;
		for(int[] match : matches){
			click(
				main.getInventCentroid(match),
				InputEvent.BUTTON3_DOWN_MASK
			);
			Thread.sleep(500);
			inserted++;
			if(inserted % 20 == 19){
				click(dismantle, InputEvent.BUTTON1_DOWN_MASK);
				Thread.sleep(4000);
			}
		}
		click(dismantle, InputEvent.BUTTON1_DOWN_MASK);
		Thread.sleep(4000);
	}
	private static void reloadBait()throws InterruptedException{
		LinkedList<int[]> matches;

		resetMouse();
		Thread.sleep(1000);
		pressKey(Config.PET_KEY);
		Thread.sleep(1000);
		pressKey(Config.PET_STORAGE_KEY);
		Thread.sleep(1000);

		updateInventory(screenshot());
		// Transfer fillet
		matches = main.matches(fillet, true);
		Thread.sleep(1000);
		if(matches.size()==1){
			click(
				main.getInventCentroid(matches.peek()),
				InputEvent.BUTTON3_DOWN_MASK
			);
		}else{
			throw new IllegalStateException("Identified " + matches.size() + " fillet");
		}
		
		// Transfer bait
		matches = pet.matches(bait, true);
		Thread.sleep(1000);
		dragAndDrop(
			pet.getInventCentroid(matches.peek()),
			main.getInventCentroid(7, 0)
		);
		Thread.sleep(1000);
		pressKey(KeyEvent.VK_6);
		Thread.sleep(200);
		pressKey(KeyEvent.VK_6);

		Image i = screenshot();
		i.cacheGrey(Preprocess.filterColour(i, BAIT_NUMBER, 3));
		Blob[] blobs = Blobbing.getBlobs(
			i,
			b -> b.isReasonableBaitOkSize()
		);
		Blob okBtn = null;
		if(blobs.length==1){
			okBtn = blobs[0];
		}else{
			throw new IllegalStateException("failed to ok button");
		}

		click(
			okBtn.getLeft() + 150,
			okBtn.getTop() + 16,
			InputEvent.BUTTON1_DOWN_MASK
		);

		Thread.sleep(1000);
		pressKey(Config.PET_KEY);
		Thread.sleep(1000);
		resetMouse();
	}
	private static Image getTpl(Image i, int x, int y){
		findInventories(i);
		return main.crop(x, y, false);
	}
	private static void debugCode()throws IOException{
		//Image i = Image.loadTestImage("pet-big.png");
		//findInventories(i);
		//LinkedList<int[]> matches = pet.matches(new Image[]{ bait }, true);

		//Image i = Image.loadTestImage("max-inventory.png");
		//findInventories(i);
		//main.openDismantle();

		//for(int[] pos : matches){
		//	System.out.println(pos[0] + ", " + pos[1]);
		//}
		//Image i = Image.loadTestImage("bait-number.png");
		//i.cacheGrey(Preprocess.filterColour(i, BAIT_NUMBER, 3));
		//Blob[] blobs = Blobbing.getBlobs(
		//	i,
		//	b -> b.isReasonableBaitOkSize()
		//);
		////offset topleft +150, +16
		//for(Blob b : blobs){
		//	System.out.println(b.toString());
		//}
		//i.toGrey();
		//ImageIO.write(i.toBufferedImage(), FORMAT, debugOutput);

		// To extract fish tpl
		Image i = getTpl(Image.loadTestImage("fish9-golden-eel.png"), 0, 3);
		ImageIO.write(i.toBufferedImage(), FORMAT, debugOutput);

		//Image i = Image.loadTestImage("dismantle-weird.png");
		//i.cacheGrey(Preprocess.filterColour(i, DISMANTLE_AREA, 1));
		//i.toGrey();
		//ImageIO.write(i.toBufferedImage(), FORMAT, debugOutput);

		// To test the dismantle
		//findInventories(Image.loadTestImage("fish.png"));
		//LinkedList<int[]> matches = main.matches(fishToDismantle, false);
		//for(int[] pos : matches){
		//	System.out.println(pos[0] + ", " + pos[1]);
		//}

		//System.out.println("System is starting in 3 seconds");
		//Thread.sleep(3000);

		//initState();
		//dismantle();
		//reloadBait();

		//long ts = 1559824202529L;
		//int s = 0;
		//int e = 249;
		//s = 22;
		//e = 23;
		//for(int ii = s; ii < e; ii++){
		//	System.out.println("Frame " + ii);
		//	Image i = Image.loadDebugImage(ts, ii);
		//	Fishing.fish(i);
		//	System.out.println("");
		//}
	}
	public static void printDebug(Image i, Blob[] blobs)throws IOException{
		i.toGrey();
		for(Blob blob : blobs){
			//System.out.println(blob.toString());
			i.drawBox(WHITE, blob);
		}
		ImageIO.write(i.toBufferedImage(), FORMAT, debugOutput);
	}
}
