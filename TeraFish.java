import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;

import java.awt.event.KeyEvent;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TeraFish{
	private static final Rectangle gauge = new Rectangle(714, 309, 420, 30);
	private static final Rectangle F = new Rectangle(957, 262, 12, 16);
	private static final Rectangle gaugeFrame = new Rectangle(702, 307, 10, 50);

	private static final int BASE_WAIT_TIME = 20000; //20 secs
	private static final int WAIT_CHECK_INTERVAL = 1000; //1 sec
	private static final int TIME_TO_FISH_BUFFER = 2000; //2 secs
	private static final int MAX_FISHING_TIME = 30000; //30 secs
	private static final int AFTER_FISHING_DELAY = 3500; //3.5 secs

	private static final double fRatio = 0.2;

	private static int[][] fTpl;
	private static int[] gaugeFrameTpl;

	private enum State {
		START,
		WAIT,
		START_FISH,
		FISH
	}

	//Colors for debug
	private static final int[] WHITE = new int[]{ 255, 255, 255 };

	private static State state = State.START;
	private static Robot r;
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

	//Pressing state for efficiency?
	private static boolean pressing = false;

	//Time when fishing started, used to determine when to stop
	private static long fishingStarted;
	public static void main(String[] args){
		try{
			if(debug){
				System.out.println("System is starting in 2 seconds");
				Thread.sleep(2000);

				//fish();

				Image i = new Image(r.createScreenCapture(gaugeFrame), false);

				double ratio = Preprocess.sumOfAbsoluteRatio(
					gaugeFrameTpl,
					i.getRgb()
				);

				System.out.println("ratio is " + ratio);
				if(ratio < 0.01){
					System.out.println("not yet!");
				}else{
					System.out.println("Finished fishing!");
				}

				ImageIO.write(i.toBufferedImage(), FORMAT, debugOutput);
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
							}else{
								Thread.sleep(WAIT_CHECK_INTERVAL);
							}
							break;
						case FISH:
							boolean failed = System.currentTimeMillis() - fishingStarted > MAX_FISHING_TIME;
							if(isFinishedFishing() || failed){
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
		int top = b.getTop() + 9;
		int right = b.getRight() - 10;
		int left = b.getLeft() + 10;
		int[][] grey = i.getGrey();
		for(int ii = right; ii >= left; ii--){
			if(grey[top][ii] > 150){
				//this is the fin
				if(b.getRight() - ii < 30){
					return true;
					//System.out.println("the fin is at " + ii);
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

		return ratio < fRatio;
	}

	private static boolean isFinishedFishing(){
		double ratio = Preprocess.sumOfAbsoluteRatio(
			gaugeFrameTpl,
			(new Image(r.createScreenCapture(gaugeFrame), false)).getRgb()
		);

		return ratio > 0.01;
	}
}
