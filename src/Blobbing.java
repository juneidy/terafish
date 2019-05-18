import java.awt.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import java.util.stream.Collectors;

/**
 * Blobbing algorithm based on Wikipedia
 * @see <a href="https://en.wikipedia.org/wiki/Connected-component_labeling">
 * Wiki Connected-component Labelling</a>
 * This class provides functionality to perform blobbing
 */
public class Blobbing {
	public static Blob[] getBlobs(Image img){
		int[][] grey = img.getGrey();
		boolean[][] visited = new boolean[img.height][img.width];

		int [][] labelPix = new int[img.height][img.width];
		for (int ii = 0; ii < img.height; ii++) {
			for (int jj = 0; jj < img.width; jj++) {
				labelPix[ii][jj] = grey[ii][jj] < 100 ?
					-1 : Integer.MAX_VALUE;
			}
		}

		List<TreeSet<Integer>> labels = new ArrayList<TreeSet<Integer>>();
		firstLabel(labelPix, labels);
		return connectLabel(labelPix, labels);
	}

	/**
	 * This is the first pass for blobbing. It searches for objects and labels
	 * them. At the same time, it also checks for connectivity of the labels and
	 * storing them into a data structure.
	 * @param labelPix The label pixels array with discarded background
	 */
	private static void firstLabel(
		int[][] labelPix,
		List<TreeSet<Integer>> labels
	){
		int height = labelPix.length;
		int width = labelPix[0].length;
		for (int ii = 0; ii < height; ii++) {
			for (int jj = 0; jj < width; jj++) {
				if (labelPix[ii][jj] > 0) {
					TreeSet<Integer> neighbours = findNeighbours(labelPix, ii, jj);

					if (neighbours.isEmpty()) {
						TreeSet<Integer> newSet = new TreeSet<Integer>();
						int newLabel = labels.size();
						labels.add(newSet);
						newSet.add(newLabel);
						labelPix[ii][jj] = newLabel;
						/* FOR DEBUG
							System.out.println("x = " + jj + ", y = " + ii);
							System.out.println("label = " + newLabel);
							*/
					} else {
						/* FOR DEBUG
							if (neighbours.size() > 1) {
							System.out.println("x = " + jj + ", y = " + ii);
							System.out.println(neighbours);
							System.exit(0);
							}
							*/
						// Find the smallest label
						int min = Integer.MAX_VALUE;
						for (int label : neighbours) {
							if (label < min)
								min = label;
							// Union the label with neighbours
							labels.get(label).addAll(neighbours);
						}
						labelPix[ii][jj] = min;
					}
				}
			}
		}
	}

	/**
	 * This function will find neighbouring labelled pixel(s) given the position
	 * of the selected pixel and array of greyPxls. Finds the neighbouring pixels
	 * with 8 connection style
	 * @param inPixels the 2D array of greyPxls image
	 * @param inRow the row of which we look for the neighbour labelled pixel
	 * @param inCol the column of which we look for the neighbour labelled pixel
	 */
	private static TreeSet<Integer> findNeighbours(
		int[][] inPixels,
		int inRow,
		int inCol
	){
		TreeSet<Integer> neighbours = new TreeSet<Integer>();
		if (
			(inRow-1) >= 0 &&
			(inCol-1) >= 0 &&
			inPixels[inRow-1][inCol-1] >= 0 &&
			inPixels[inRow-1][inCol-1] < Integer.MAX_VALUE
		){
			neighbours.add(inPixels[inRow-1][inCol-1]);
		}
		if ((inRow-1) >= 0 && inPixels[inRow-1][inCol] >= 0
				&& inPixels[inRow-1][inCol] < Integer.MAX_VALUE) {
			neighbours.add(inPixels[inRow-1][inCol]);
				}
		if ((inRow-1) >= 0 && (inCol+1) < inPixels[0].length 
				&& inPixels[inRow-1][inCol+1] >= 0
				&& inPixels[inRow-1][inCol+1] < Integer.MAX_VALUE) {
			neighbours.add(inPixels[inRow-1][inCol+1]);
				}
		if ((inCol-1) >= 0 && inPixels[inRow][inCol-1] >= 0
				&& inPixels[inRow][inCol-1] < Integer.MAX_VALUE) {
			neighbours.add(inPixels[inRow][inCol-1]);
				}
		return neighbours;
	}

	/**
	 * This function will connect all the labels that are supposed to be one blob
	 * and stores the bounding box points.
	 * @param labelPix The labelled pixels that are labelled from the first
	 * pass
	 */
	private static Blob[] connectLabel(
		int[][] labelPix,
		List<TreeSet<Integer>> labels
	) {
		int height = labelPix.length;
		int width = labelPix[0].length;
		HashMap<Integer, Blob> blobs = new HashMap<Integer, Blob>();
		for (int ii = 0; ii < height; ii++) {
			for (int jj = 0; jj < width; jj++) {
				if (labelPix[ii][jj] >= 0) {
					/* Get the lowest value of connected label */
					int min = labels.get(labelPix[ii][jj]).first();
					int potential = labels.get(min).first();
					while (potential < min) {
						min = potential;
						potential = labels.get(min).first();
					}
					labelPix[ii][jj] = min;

					/* Get the boundary of each blob */
					Blob blob;
					// Check if boundary for the label exist
					if ((blob = blobs.get(min)) != null) {
						if (ii < blob.getTop())
							blob.setTop(ii);
						else if (ii > blob.getBottom())
							blob.setBottom(ii);

						if (jj < blob.getLeft())
							blob.setLeft(jj);
						else if (jj > blob.getRight())
							blob.setRight(jj);
					} else {
						blob = new Blob(new Point(jj, ii), new Point(jj, ii));
						blobs.put(min, blob);
					}
				}
			}
		}
		return Arrays.stream(blobs.values().toArray(new Blob[0]))
		.filter(b -> b.isReasonableSize())
		.toArray(Blob[]::new);
	}
}
