import java.io.IOException;

public class Templates{
	public static final int[][] F;
	public static final int[] GAUGE;
	public static final Image FILLET;
	public static final Image[] BAIT = new Image[4];
	static{
		int[][] tmpF = null;
		int[] tmpGauge = null;
		Image tmpFillet = null;
		try{
			tmpF = Image.loadGreyTemplate("templates/f.tpl");
			tmpGauge = Image.loadRgbTemplate("templates/gaugeframe.tpl");

			for(int ii = 0; ii < BAIT.length; ii++){
				BAIT[ii] = Image.loadRgbImage(
					"templates/baits/bait" + (ii + 2) + ".tpl"
				);
			}

			tmpFillet = Image.loadRgbImage("templates/fillet.tpl");
		}catch(IOException ex){
			System.out.println("Error loading template " + ex);
			System.exit(1);
		}finally{
			F = tmpF;
			GAUGE = tmpGauge;
			FILLET = tmpFillet;
		}
	}

	public static Image[] getBait(int tier){
		return new Image[]{ BAIT[tier - 2] };
	}

	public static Image[] getFishMatch(Location loc)throws IOException{
		switch(loc){
			case MURICAI:
				return new Image[]{
					Image.loadRgbImage("templates/fishes/fish7-chroma-salmon.tpl"),
					Image.loadRgbImage("templates/fishes/fish8-dipturus.tpl"),
				};
			case CUTTHROAT_HARBOR:
				return new Image[]{
					Image.loadRgbImage("templates/fishes/fish6-mottled-ray.tpl"),
					Image.loadRgbImage("templates/fishes/fish7-gula-shark.tpl"),
					Image.loadRgbImage("templates/fishes/fish8-prism-carp.tpl"),
				};
			case LAKE_OF_TEARS:
				return new Image[]{
					Image.loadRgbImage("templates/fishes/fish7-electric-eel.tpl"),
					Image.loadRgbImage("templates/fishes/fish8-crimson-marlin.tpl"),
				};
			case VERXATUS_SWAMP:
				return new Image[]{
					Image.loadRgbImage("templates/fishes/fish7-chroma-salmon.tpl"),
					Image.loadRgbImage("templates/fishes/fish8-dipturus.tpl"),
				};
			case CELSIAN_LAKE:
				return new Image[]{
					Image.loadRgbImage("templates/fishes/fish7-yellowfin.tpl"),
					Image.loadRgbImage("templates/fishes/fish8-prism-carp.tpl"),
				};
			case SERENS_LAKE:
				return new Image[]{
					Image.loadRgbImage("templates/fishes/fish7-electric-eel.tpl"),
					Image.loadRgbImage("templates/fishes/fish8-stone-octopus.tpl"),
				};
		}
		return null;
	}
}
