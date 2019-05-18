import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;

import java.awt.event.KeyEvent;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TeraFish{
	private static final int GAUGE_WIDTH = 420;
	private static final Rectangle gauge = new Rectangle(714, 309, GAUGE_WIDTH, 30);
	private static final Rectangle F = new Rectangle(957, 262, 12, 16);
	private static final Rectangle gaugeFrame = new Rectangle(702, 307, 10, 50);

	private static final int BASE_WAIT_TIME = 20000; //20 secs
	private static final int WAIT_CHECK_INTERVAL = 1000; //1 sec
	private static final int TIME_TO_FISH_BUFFER = 2000; //2 secs
	private static final int SOONEST_POSSIBLE_FISH_TIME = 7000; //7 secs
	private static final int MAX_FISHING_TIME = 30000; //30 secs
	private static final int AFTER_FISHING_DELAY = 3500; //3.5 secs

	private static final double F_RATIO = 0.2;
	private static final double FRAME_RATIO = 0.01;

	private static final int MIN_GOLDEN_WHITENESS = 80000;


	private static int[][] fTpl;
	private static int[] gaugeFrameTpl;

	private enum State {
		START,
		WAIT,
		START_FISH,
		FISH
	}

	private static State state = State.START;
	private static Robot r;
	private static boolean needCheckGolden = true;
	private static boolean golden = false;
	static{
		try{
			fTpl = Image.loadGreyTemplate("./f.tpl");
			gaugeFrameTpl = Image.loadRgbTemplate("./gaugeframe.tpl");
		}catch(IOException ex){
			System.out.println("Error loading template " + ex);
			System.exit(1);
		}
		try{
			r = new Robot();
		}catch(AWTException ex){
			System.out.println("Error creating robot " + ex);
			System.exit(1);
		}
	}

	// Debug vars
	private static final boolean debug = false;
	private static final String FORMAT = "png";
	private static final File debugOutput = new File("/tmp/foo");
	private static final int[] WHITE = new int[]{ 255, 255, 255 };

	//Pressing state for efficiency?
	private static boolean pressing = false;

	//Time when fishing started, used to determine when to stop
	private static long fishingStarted;
	public static void main(String[] args){
		try{
			if(debug){
				System.out.println("System is starting in 1 seconds");
				Thread.sleep(1000);

				needCheckGolden = false;
				golden = false;

				fish();

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
			}else{
				System.out.println("System is starting in 3 seconds");
				Thread.sleep(3000);
				while(true){
					long startTime = System.currentTimeMillis();
					switch(state){
						case START:
							pressKey(KeyEvent.VK_R, 3000);
							state = State.WAIT;
							break;
						case WAIT:
							Thread.sleep(BASE_WAIT_TIME);
							state = State.START_FISH;
							break;
						case START_FISH:
							if(isTimeToFish()){
								pressKey(KeyEvent.VK_F, 200);
								fishingStarted = System.currentTimeMillis();
								Thread.sleep(TIME_TO_FISH_BUFFER);
								state = State.FISH;
								needCheckGolden = true;
								golden = false;
							}else{
								Thread.sleep(WAIT_CHECK_INTERVAL);
							}
							break;
						case FISH:
							long cur = System.currentTimeMillis();
							boolean failed = cur - fishingStarted > MAX_FISHING_TIME;
							boolean aboutTime = cur - fishingStarted > SOONEST_POSSIBLE_FISH_TIME;
							if(aboutTime && isFinishedFishing() || failed){
								pressKey(KeyEvent.VK_F, 0);
								state = State.START;
								pressing = false;
								Thread.sleep(AFTER_FISHING_DELAY);
							}else{
								fish();
							}
							break;
					}
					long executionTime = System.currentTimeMillis() - startTime;
					System.out.println("Execution time: " + executionTime + "ms");
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
	public static void fish()throws IOException{
		Image i = new Image(r.createScreenCapture(gauge), false);
		Preprocess.histogram(i);
		Blob[] blobs = Blobbing.getBlobs(i);
		if(blobs.length==2){
			int box, fish;
			if(blobs[0].getTop() < blobs[1].getTop()){
				box = 0;
				fish = 1;
			}else{
				box = 1;
				fish = 0;
			}

			if(needCheckGolden){
				golden = blobs[fish].calculateWhiteness(i) > MIN_GOLDEN_WHITENESS;
				needCheckGolden = false;
			}

			if(blobs[fish].getLeft() < (blobs[box].getRight() - 20)){
				if(debug){
					System.out.println("Behind, press F");
				}else{
					if(!pressing){
						r.keyPress(KeyEvent.VK_F);
						pressing = true;
					}
				}
			}else{
				if(debug){
					System.out.println("In front, release F");
				}else{
					if(pressing){
						r.keyRelease(KeyEvent.VK_F);
						pressing = false;
					}
				}
			}
		}else if(blobs.length==1){
			//search -8y, -8x of the edge
			//up to +2
			//not 15 px
			//>155
			if(fishTooFar(i, blobs[0])){
				if(debug){
					System.out.println("In, too far, release F");
				}else{
					if(pressing){
						r.keyRelease(KeyEvent.VK_F);
						pressing = false;
					}
				}
			}else{
				if(debug){
					System.out.println("In, not too far, hold F");
				}else{
					if(!pressing){
						r.keyPress(KeyEvent.VK_F);
						pressing = true;
					}
				}
			}
		}

		if(debug){
			i.toGrey();
			for(Blob blob : blobs){
				System.out.println(blob.toString());
				i.drawBox(WHITE, blob);
			}
			ImageIO.write(i.toBufferedImage(), FORMAT, debugOutput);
		}
	}

	private static boolean fishTooFar(Image i, Blob b){
		int rightMost = b.getRight();
		int leftMost = b.getLeft();
		// Calling this function means they are in the same blob
		if(
			// if they are in the right-most of the pixels, just hold F
			rightMost==(GAUGE_WIDTH - 1) ||
			// if only the fish is detected, keep holding F
			// Golden fish brightness is so high that the box disappeared when
			// golden is in the box
			(golden && rightMost - leftMost < 50)
		){
			return false;
		}
		int top = b.getTop() + 9;
		int right = rightMost - 10;
		int left = leftMost + 10;
		int[][] grey = i.getGrey();
		int distance = golden ? 12 : 30;
		for(int ii = right; ii >= left; ii--){
			if(grey[top][ii] > 150){
				//this is the fin
				//the head if golden
				if(rightMost - ii < distance){
					return true;
				}else{
					return false;
				}
			}
		}
		return false;
	}

	private static boolean isTimeToFish(){
		double ratio = Preprocess.sumOfAbsoluteRatio(
			fTpl,
			Preprocess.getGrey(new Image(r.createScreenCapture(F), false))
		);

		return ratio < F_RATIO;
	}

	private static boolean isFinishedFishing(){
		double ratio = Preprocess.sumOfAbsoluteRatio(
			gaugeFrameTpl,
			(new Image(r.createScreenCapture(gaugeFrame), false)).getRgb()
		);

		return ratio > FRAME_RATIO;
	}
}
