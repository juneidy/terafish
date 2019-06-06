import java.awt.Point;

import java.awt.event.InputEvent;

import java.util.LinkedList;
import java.util.TreeSet;

public class Inventory{
	private enum Type { MAIN, PET, UNKNOWN }

	private final Integer[] gridRow;
	private final Integer[] gridCol;
	private Image img;
	private Blob blob; // so we know the offset
	private final int row;
	private final int col;
	private final Type type;

	private static final int[] INVENT_EMPTY_SLOT = new int[]{ 16, 20, 24 };
	private static final double IGNORE_BORDER = 0.03;
	private static final double IGNORE_BORDER_ACTIVE = 0.12;
	private static final double SIMILARITY_TOLERANCE = 0.1;
	private static final double STACK_HEIGHT_RATIO = 0.74;

	private static final int SIZE_TOLERANCE = 5;

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

	public boolean update(Image i, Blob bl){
		if(
			Math.abs(bl.getHeight() - blob.getHeight()) < SIZE_TOLERANCE &&
			Math.abs(bl.getWidth() - blob.getWidth()) < SIZE_TOLERANCE
		){
			blob = bl;
			img = i.crop(bl);
			return true;
		}
		return false;
	}

	public boolean isMain(){
		return type == Type.MAIN;
	}

	public boolean isMaxInventory(){
		return col==15;
	}

	public boolean isPet(){
		return type == Type.PET;
	}

	public boolean isUnknown(){
		return type == Type.UNKNOWN;
	}

	public Point openDismantle()throws InterruptedException{
		Point p = null;
		if(isMain()){
			Blob b = getInventSlot(0, 0);
			int yOffset;
			if(isMaxInventory()){
				yOffset = (int)(b.getHeight() * 1.25);
			}else{
				yOffset = (int)(b.getHeight() * 2.5);
			}
			TeraFish.click(
				blob.getLeft() + gridRow[6],
				blob.getTop() + gridCol[gridCol.length - 1] + yOffset,
				InputEvent.BUTTON1_DOWN_MASK
			);
		}
		return p;
	}

	private Blob getInventSlot(int x, int y){
		x *= 2;
		y *= 2;
		return new Blob(
			new Point(gridRow[x], gridCol[y]),
			new Point(gridRow[x+1], gridCol[y+1])
		);
	}

	public Point getInventCentroid(int[] pos){
		return getInventCentroid(pos[0], pos[1]);
	}

	public Point getInventCentroid(int x, int y){
		x *= 2;
		y *= 2;
		int width = gridRow[x+1] - gridRow[x];
		int height = gridCol[y+1] - gridCol[y];
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

	public LinkedList<int[]> matches(Image[] tpl, boolean stacked){
		LinkedList<int[]> matches = new LinkedList<int[]>();
		int tplLen = tpl.length - 1;
		for(int ii = col - 1; ii >= 0; ii--){
			for(int jj = row - 1; jj >= 0; jj--){
				int kk = tplLen;
				boolean match = false;
				while(!match && kk >= 0){
					double abs = Preprocess.sumOfAbsoluteRatio(
						Preprocess.resize(
							crop(jj, ii, stacked),
							tpl[kk].width,
							tpl[kk].height
						),
						tpl[kk],
						stacked ? IGNORE_BORDER_ACTIVE : IGNORE_BORDER
					);
					match = abs < SIMILARITY_TOLERANCE;
					kk--;
				}
				if(match) matches.add(new int[]{ jj, ii });
			}
		}
		return matches;
	}
}
