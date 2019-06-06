import java.io.FileInputStream;
import java.io.IOException;

import java.awt.event.KeyEvent;

import java.util.Properties;

public class Config{
	private static final Properties prop = new Properties();
	public static final Location LOCATION;
	public static final int BAIT;
	public static final int DISMANTLE_CAP;
	public static final boolean DEBUG_FISHING;
	public static final String DEBUG_OUTPUT_LOCATION;
	public static final int ROD_KEY;
	public static final int FISH_KEY;
	public static final int PET_KEY;
	public static final int PET_STORAGE_KEY;

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
		BAIT = Integer.parseInt(prop.getProperty("bait", "5"));
		DISMANTLE_CAP = Integer.parseInt(prop.getProperty("dismantle_cap", "55"));
		DEBUG_FISHING = Integer.parseInt(prop.getProperty("debug_fishing", "0")) != 0;
		if(DEBUG_FISHING){
			DEBUG_OUTPUT_LOCATION = prop.getProperty("debug_output_location");
		}else{
			DEBUG_OUTPUT_LOCATION = null;
		}
		ROD_KEY = convertToKeyEvent(
			prop.getProperty("rod_key", "r")
		);
		FISH_KEY = convertToKeyEvent(
			prop.getProperty("fish_key", "f")
		);
		PET_KEY = convertToKeyEvent(
			prop.getProperty("pet_key", "`")
		);
		PET_STORAGE_KEY = convertToKeyEvent(
			prop.getProperty("pet_storage_key", "x")
		);
	}

	public static int convertToKeyEvent(String key){
		switch(key){
			case "`":
				return KeyEvent.VK_BACK_QUOTE;
			case "f1":
				return KeyEvent.VK_F1;
			case "f2":
				return KeyEvent.VK_F2;
			case "f3":
				return KeyEvent.VK_F3;
			case "f4":
				return KeyEvent.VK_F4;
			case "f5":
				return KeyEvent.VK_F5;
			case "f6":
				return KeyEvent.VK_F6;
			case "f7":
				return KeyEvent.VK_F7;
			case "f8":
				return KeyEvent.VK_F8;
			case "f9":
				return KeyEvent.VK_F9;
			case "f10":
				return KeyEvent.VK_F10;
			case "f11":
				return KeyEvent.VK_F11;
			case "f12":
				return KeyEvent.VK_F12;
			case "1":
				return KeyEvent.VK_1;
			case "2":
				return KeyEvent.VK_2;
			case "3":
				return KeyEvent.VK_3;
			case "4":
				return KeyEvent.VK_4;
			case "5":
				return KeyEvent.VK_5;
			case "6":
				return KeyEvent.VK_6;
			case "7":
				return KeyEvent.VK_7;
			case "8":
				return KeyEvent.VK_8;
			case "9":
				return KeyEvent.VK_9;
			case "0":
				return KeyEvent.VK_0;
			case "a":
				return KeyEvent.VK_A;
			case "b":
				return KeyEvent.VK_B;
			case "c":
				return KeyEvent.VK_C;
			case "d":
				return KeyEvent.VK_D;
			case "e":
				return KeyEvent.VK_E;
			case "f":
				return KeyEvent.VK_F;
			case "g":
				return KeyEvent.VK_G;
			case "h":
				return KeyEvent.VK_H;
			case "i":
				return KeyEvent.VK_I;
			case "j":
				return KeyEvent.VK_J;
			case "k":
				return KeyEvent.VK_K;
			case "l":
				return KeyEvent.VK_L;
			case "m":
				return KeyEvent.VK_M;
			case "n":
				return KeyEvent.VK_N;
			case "o":
				return KeyEvent.VK_O;
			case "p":
				return KeyEvent.VK_P;
			case "q":
				return KeyEvent.VK_Q;
			case "r":
				return KeyEvent.VK_R;
			case "s":
				return KeyEvent.VK_S;
			case "t":
				return KeyEvent.VK_T;
			case "u":
				return KeyEvent.VK_U;
			case "v":
				return KeyEvent.VK_V;
			case "w":
				return KeyEvent.VK_W;
			case "x":
				return KeyEvent.VK_X;
			case "y":
				return KeyEvent.VK_Y;
			case "z":
				return KeyEvent.VK_Z;
		}
		throw new IllegalStateException("Unrecognised key event: " + key);
	}
}
