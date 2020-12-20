package land.chlwhdtn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LandManager {
	private static HashMap<String, Landata> hashmap = new HashMap<String, Landata>();
	public static int landprice = 100000; // 10X10 기준
	public static int default_size = 10;
	public static void setLandOwner(String land, String playername) {
		hashmap.get(land).owner = playername;
	}
	public static Landata addLand(int firstID, int startx, int startz, int size) {
		String key = firstID + "-1";
		int i = 2;
		while(isLand(key)) {
			key = firstID + "-"+i;
			i++;
		}
		Landata data =  new Landata(key,startx,startx+(size-1),startz,startz+(size-1), size);
		hashmap.put(key,data);
		return data;
	}
	public static Landata getLand(String string) {
		return hashmap.get(string);
	}
	public static int getLandPrice(String string) {
		return (int)((double)((double)landprice * ((double)Math.pow(((double)hashmap.get(string).size / (double)default_size), 2))));
	}
	public static void LoadLand(String key, Landata data) {
		hashmap.put(key, data);
	}
	public static boolean isLand(String landname)
    {
		return hashmap.containsKey(landname);
    }
	public static boolean hasLand(String playername)
    {
		for(String s : hashmap.keySet()) {
			if(hashmap.get(s).owner == null)
				continue;
			if(hashmap.get(s).owner.equals(playername)) {
				return true;
			} else {
				continue;
			}
		}
		return false;
    }
	public static List<Landata> getLands(String playername)
    {
		List<Landata> list = new ArrayList<Landata>();
		if(hasLand(playername) == false)
			return list;
		
		for(String s : hashmap.keySet()) {
			if(hashmap.get(s).owner == null)
				continue;
			
			if(hashmap.get(s).owner.equals(playername)) {
				list.add(hashmap.get(s));
			}
		}
		return list;
    }
	public static HashMap<String, Landata> getLandMap() {
		return hashmap;
	}
}
