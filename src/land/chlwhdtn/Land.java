package land.chlwhdtn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server.Spigot;
import org.bukkit.SkullType;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import economy.chlwhdtn.Economy;
import economy.chlwhdtn.MoneyFileManager;
import economy.chlwhdtn.MoneyManager;
import util.chlwhdtn.CUtil;

public class Land extends JavaPlugin implements CommandExecutor {
	public static Land land;
	@Override
	public void onEnable() {
		land = this;
		if(Bukkit.getPluginManager().getPlugin("C-Economy") == null) {
			System.out.println("필수 플러그인이 존재하지 않습니다.");
			Bukkit.getPluginManager().disablePlugin(this);
		} else {
			Economy.online(this);
		}
		
		// 커맨드
		
		getCommand("world").setExecutor(this);
		getCommand("land").setExecutor(new LandTool());
		getCommand("토지").setExecutor(new LandUserCommand());
		getCommand("spawn").setExecutor(new LandUserCommand());
		
		// 이벤트
		
		Bukkit.getPluginManager().registerEvents(new LandTool(), this);
		
		// 필수 월드 생성
		World world = Bukkit.createWorld(new WorldCreator("land")
																				.environment(Environment.NORMAL)
																				.generateStructures(false)
																				.type(WorldType.FLAT));
		world.setGameRule(GameRule.KEEP_INVENTORY, true);
		world.setDifficulty(Difficulty.NORMAL);
		world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		world.setSpawnLocation(0, 4, 0);
		
		LandFileManager.reloadConfig();
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(land, new Runnable() {
			
			@Override
			public void run() {
				Scoreboard sb;
				for(Player p : Bukkit.getOnlinePlayers()) {
					CUtil.updateScoreboard(p);
				}
				Iterator it = sortByValue(MoneyManager.getMoneyMap()).iterator();
				for(int i = 0; i < 3; i++) {
					Location loc = new Location(Bukkit.getWorld("land"), Bukkit.getWorld("land").getSpawnLocation().add(-3, 0, 0).getX(), 5, Bukkit.getWorld("land").getSpawnLocation().add(0, 0, 4-i).getZ());
					try {
						String pname = it.next().toString();
						loc.getBlock().setType(Material.PLAYER_HEAD);
						Skull skull = (Skull) loc.getBlock().getState(); // make sure to import org.bukkit.block.Skull;
						skull.setRotation(BlockFace.SOUTH_WEST);
						skull.setOwningPlayer(Bukkit.getOfflinePlayer(pname));
						skull.update();
					
						loc.add(1, -1, 0);
						loc.getBlock().setType(Material.BIRCH_WALL_SIGN);
						Sign s = (Sign) loc.getBlock().getState();
						WallSign ws = (WallSign) loc.getBlock().getState().getBlockData();
						ws.setFacing(BlockFace.EAST);
						s.setBlockData(ws);
						s.setLine(0, (i+1)+"위");
						s.setLine(1, pname);
						s.setLine(2, String.format("%,d원", MoneyManager.getMoney(pname)));
						s.update();
					} catch(Exception e) {
						loc.getBlock().setType(Material.PLAYER_HEAD);
						Skull skull = (Skull) loc.getBlock().getState();
						skull.setRotation(BlockFace.SOUTH_WEST);
						skull.update();
						loc.add(1, -1, 0);
						loc.getBlock().setType(Material.BIRCH_WALL_SIGN);
						Sign s = (Sign) loc.getBlock().getState();
						WallSign ws = (WallSign) loc.getBlock().getState().getBlockData();
						ws.setFacing(BlockFace.EAST);
						s.setBlockData(ws);
						s.setLine(0, "서버에 사람이 없어요 :(");
						s.update();
					}
				
				}
			}
		},0L, 200L);
	}
	
	@Override
	public void onDisable() {
		LandFileManager.saveConfig();
		Bukkit.getScheduler().cancelTasks(land);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender.isOp() == false)
			return false;
		if(args.length == 0) {
			for(World world : Bukkit.getWorlds())
				sender.sendMessage(world.getName());
			return true;
		}
		if(args.length >= 1) {
			try {
				World world = Bukkit.getWorld(args[0]);
				((Player)sender).teleport(world.getSpawnLocation());
				return true;
			} catch(NullPointerException e) {
				sender.sendMessage("§c존재하지 않는 월드입니다.");
				return false;
			}
		}
		return false;
	}
	
	public List<String> sortByValue(final Map<String, ?> map) {
        List<String> list = new ArrayList<String>();
        list.addAll(map.keySet());
        Collections.sort(list,new Comparator<Object>() {

            public int compare(Object o1,Object o2) {
                Object v1 = map.get(o1);
                Object v2 = map.get(o2);
                return ((Comparable<Object>) v2).compareTo(v1);
            }
        });
//        Collections.reverse(list); // 주석시 오름차순
        return list;
    }

}
