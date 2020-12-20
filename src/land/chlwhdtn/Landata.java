package land.chlwhdtn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class Landata {
	
	public String owner = null, name;
	public List<String> slaves = new ArrayList<String>();
	public int startx,endx,startz,endz,size;
	public boolean canburn = false, canexplode = false;
	
	public Landata(String landname, int startx, int endx, int startz, int endz, int size) {
		this.name = landname;
		this.startx = startx;
		this.endx = endx;
		this.startz = startz;
		this.endz = endz;
		this.size = size;
	}
	
	public Landata(String landname, int startx, int endx, int startz, int endz,int size,String owner,boolean canburn,boolean canexplode) {
		this.name = landname;
		this.startx = startx;
		this.endx = endx;
		this.startz = startz;
		this.endz = endz;
		this.size = size;
		this.owner = owner;
		this.canburn = canburn;
		this.canexplode = canexplode;
	}
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String playername) {
		owner = playername;
	}
	
	public void addSlave(String playername) {
		slaves.add(playername);
	}
	
	public boolean isSlave(String playername) {
		return slaves.contains(playername);
	}
	
	public void removeSlave(String playername) {
		slaves.remove(playername);
	}

	public void reset() {

		World world = Bukkit.getWorld("land");

		int[] dimX = new int[2];
		int[] dimZ = new int[2];

		dimX[0] = startx;
		dimX[1] = endx;
		Arrays.sort(dimX);

		dimZ[0] = startz;
		dimZ[1] = endz;
		Arrays.sort(dimZ);
		
			
				for(int x = dimX[0]; x <= dimX[1]; x++)
					for(int y = 1; y <= world.getMaxHeight(); y++)
						for(int z = dimZ[0]; z <= dimZ[1]; z++) {
							if(y < 3)
								world.getBlockAt(x, y, z).setType(Material.DIRT);
							else if(y < 4)
								world.getBlockAt(x, y, z).setType(Material.GRASS_BLOCK);
							else
								world.getBlockAt(x,y,z).setType(Material.AIR);
						}				
				
	}
	 
	
	

}
