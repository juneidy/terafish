import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;

public class Config{
	private static final Properties prop = new Properties();
	public static final Location LOCATION;
	public static final int BAIT;
	public static final int DISMANTLE_CAP;
	static{
		try(FileInputStream input = new FileInputStream("./terafish.cfg")){
			prop.load(input);
		}catch(IOException ex){
			System.out.println("Failed to load config file " + ex);
			ex.printStackTrace();
			System.exit(1);
		}
		switch(prop.getProperty("location")){
			case "muricai":
				LOCATION = Location.MURICAI;
				break;
			case "cutthroat":
				LOCATION = Location.CUTTHROAT_HARBOR;
				break;
			case "tears":
				LOCATION = Location.LAKE_OF_TEARS;
				break;
			case "verxatus":
				LOCATION = Location.VERXATUS_SWAMP;
				break;
			case "celsian":
				LOCATION = Location.CELSIAN_LAKE;
				break;
			case "seren":
				LOCATION = Location.SERENS_LAKE;
				break;
			default:
				LOCATION = Location.NONE;
				break;
		}
		BAIT = Integer.parseInt(prop.getProperty("bait"));
		DISMANTLE_CAP = Integer.parseInt(prop.getProperty("dismantleCap", "55"));
	}
}
