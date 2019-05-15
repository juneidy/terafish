import java.util.ArrayList;
import java.util.Arrays;

public class Preprocess {
	public static void histogram(Image img){
		int min = 256, max = 0;
		int[] histogram = new int[256];

		int[][] grey = new int[img.height][img.width];
		boolean[][] isCached = new boolean[img.height][img.width];

		int[] rgb = img.getRgb();

		// Tally the gray values and get the min and max
		for(int ii = img.height - 1; ii >= 0; ii--){
			for(int jj = img.width - 1; jj >= 0; jj--){
				// calculating grey on the fly
				if(!isCached[ii][jj]){
					isCached[ii][jj] = true;
					int rgbPos = (ii * img.width + jj) * 3;
					grey[ii][jj] = (
						(rgb[rgbPos] + rgb[rgbPos + 1] + rgb[rgbPos + 2]) / 3
					);
					if(grey[ii][jj]<70){
						grey[ii][jj] = 0;
					}
				}
				histogram[grey[ii][jj]]++;
				if(grey[ii][jj] < min)
					min = grey[ii][jj];
				if(grey[ii][jj] > max)
					max = grey[ii][jj];
			}
		}

		// Get the cumulative
		int last = min;
		for(int ii = min+1; ii <= max; ii++){
			if(histogram[ii] > 0){
				histogram[ii] += histogram[last];
				last = ii;
			}
		}

		// Calculate M*N - cdf.min
		double d = img.height * img.width - histogram[min];
		int cdfmin = histogram[min];

		// Calculate cdf scaled
		for(int ii = min; ii <= max; ii++){
			if(histogram[ii] > 0){
				histogram[ii] = (int)Math.round((double)(histogram[ii] - cdfmin) /
						d * 255.0);
			}
		}

		// Replace each inGrey with histogram equalised
		for(int ii = img.height - 1; ii >= 0; ii--){
			for(int jj = img.width - 1; jj >= 0; jj--){
				grey[ii][jj] = histogram[grey[ii][jj]];
			}
		}

		img.cacheGrey(grey);
	}

	public static int[][] getGrey(Image i){
		int[] rgb = i.getRgb();
		int[][] grey = new int[i.height][i.width];
		for(int ii = i.height - 1; ii >= 0; ii--){
			for(int jj = i.width - 1; jj >= 0; jj--){
					int rgbPos = (ii * i.width + jj) * 3;
					grey[ii][jj] = (
						(rgb[rgbPos] + rgb[rgbPos + 1] + rgb[rgbPos + 2]) / 3
					);
			}
		}
		return grey;
	}

	public static double sumOfAbsoluteRatio(int[][] a, int[][] b){
		int h = a.length;
		int w = a[0].length;
		int total = 0;
		for(int ii = h - 1; ii >= 0; ii--){
			for(int jj = w - 1; jj >= 0; jj--){
				total += Math.abs(a[ii][jj] - b[ii][jj]);
			}
		}
		return total / (double)(h * w * 255);
	}

	public static double sumOfAbsoluteRatio(int[] a, int[] b){
		int len = a.length;
		int total = 0;
		for(int ii = len - 1; ii >= 0; ii--){
			total += Math.abs(a[ii] - b[ii]);
		}
		return total / (double)(len * 255 * 3);
	}
}
