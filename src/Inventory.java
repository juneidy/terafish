import java.awt.Point;

import java.util.LinkedList;
import java.util.TreeSet;

public class Inventory{
	private final Integer[] gridRow;
	private final Integer[] gridCol;
	private final Image img;
	private final Blob blob; // so we know the offset

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

		for(Integer v : gridRow){
			System.out.print(v + " ");
		}
		System.out.println("");

		for(Integer v : gridCol){
			System.out.print(v + " ");
		}
		System.out.println("");
	}

	public Blob getInventSlot(int x, int y){
		x *= 2;
		y *= 2;
		return new Blob(
			new Point(gridRow[x], gridCol[y]),
			new Point(gridRow[x+1], gridCol[y+1])
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

	public LinkedList<int[]> matches(Image tpl, boolean stacked){
		LinkedList<int[]> matches = new LinkedList<int[]>();
		for(int ii = gridCol.length / 2 - 1; ii >= 0; ii--){
			for(int jj = gridRow.length / 2 - 1; jj >= 0; jj--){
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
