package land.chlwhdtn;

import java.util.ArrayList;
import java.util.List;

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
	 
	
	

}
