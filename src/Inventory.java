import java.awt.Point;

import java.awt.event.InputEvent;

import java.util.LinkedList;
import java.util.TreeSet;

public class Inventory{
	public enum Type { MAIN, PET, UNKNOWN }

	private final Integer[] gridRow;
	private final Integer[] gridCol;
	private final Image img;
	public final Blob blob; // so we know the offset
	private final int row;
	private final int col;
	private final Type type;

	private static final int[] INVENT_EMPTY_SLOT = new int[]{ 16, 20, 24 };
	private static final double IGNORE_BORDER = 0.03;
	private static final double IGNORE_BORDER_ACTIVE = 0.12;
	private static final double SIMILARITY_TOLERANCE = 0.1;
	private static final double STACK_HEIGHT_RATIO = 0.74;

	public Inventory(Image i, Blob bl){
		blob = bl;
		img = i;
		img.cacheGrey(
			Preprocess.filterColour(img, INVENT_EMPTY_SLOT, 1)
		);
		Blob[] blobs = Blobbing.getBlobs(
			img,
			b -> b.isReasonableSize() && b.getBrightnessRatio(img) > 0.8
		);
		TreeSet<Integer> rowSet = new TreeSet<Integer>();
		TreeSet<Integer> colSet = new TreeSet<Integer>();

		for(Blob b : blobs){
			rowSet.add(b.getLeft());
			rowSet.add(b.getRight());
			colSet.add(b.getTop());
			colSet.add(b.getBottom());
		}

		gridRow = rowSet.toArray(new Integer[0]);
		gridCol = colSet.toArray(new Integer[0]);

		row = gridRow.length / 2;
		col = gridCol.length / 2;

		if(row==8){
			if(col==9){
				type = Type.PET;
			}else if(col>=11){
				type = Type.MAIN;
			}else{
				type = Type.UNKNOWN;
			}
		}else{
			type = Type.UNKNOWN;
		}

		for(Integer v : gridRow){
			System.out.print(v + " ");
		}
		System.out.println("");

		for(Integer v : gridCol){
			System.out.print(v + " ");
		}
		System.out.println("");
	}

	public Type getType(){
		return type;
	}

	public Point getDismantlePos(){
		Point p = null;
		if(getType()==Type.MAIN){
			Blob b = getInventSlot(0, 0);
			p = new Point(
				blob.getLeft() + gridRow[6],
				blob.getBottom() + b.getHeight()
			);
		}
		return p;
	}

	public Blob getInventSlot(int x, int y){
		x *= 2;
		y *= 2;
		return new Blob(
			new Point(gridRow[x], gridCol[y]),
			new Point(gridRow[x+1], gridCol[y+1])
		);
	}

	public Point getInventCentroid(int x, int y){
		x *= 2;
		y *= 2;
		int width = gridRow[x+1] - gridRow[x];
		int height = gridRow[y+1] - gridRow[y];
		return new Point(
			gridRow[x] + width / 2 + blob.getLeft(),
			gridCol[y] + height / 2 + blob.getTop()
		);
	}

	public Image crop(int x, int y, boolean stacked){
		Image tmp = img.crop(getInventSlot(x, y));
		return stacked
			? tmp.crop(
				0,
				0,
				tmp.width,
				(int)(tmp.height * STACK_HEIGHT_RATIO)
			)
			: tmp;
	}

	public void dismantle(Image tpl)throws InterruptedException{
		int max = 20;
		for(int ii = col - 1; ii >= 0; ii--){
			for(int jj = row - 1; jj >= 0; jj--){
				double abs = Preprocess.sumOfAbsoluteRatio(
					Preprocess.resize(
						crop(jj, ii, false),
						tpl.width,
						tpl.height
					),
					tpl,
					IGNORE_BORDER
				);
				if(abs < SIMILARITY_TOLERANCE && max > 0){
					TeraFish.click(
						getInventCentroid(jj, ii),
						InputEvent.BUTTON3_DOWN_MASK
					);
					max--;
				}
			}
		}
	}

	public LinkedList<int[]> matches(Image tpl, boolean stacked){
		LinkedList<int[]> matches = new LinkedList<int[]>();
		for(int ii = col - 1; ii >= 0; ii--){
			for(int jj = row - 1; jj >= 0; jj--){
				double abs = Preprocess.sumOfAbsoluteRatio(
					Preprocess.resize(
						crop(jj, ii, stacked),
						tpl.width,
						tpl.height
					),
					tpl,
					stacked ? IGNORE_BORDER_ACTIVE : IGNORE_BORDER
				);
				if(abs < SIMILARITY_TOLERANCE){
					matches.add(new int[]{ jj, ii });
					System.out.println(">>>>> Pos " + jj + ", " + ii + ": " + abs);
				}
				System.out.println("Pos " + jj + ", " + ii + ": " + abs);
			}
		}
		return matches;
	}
}
