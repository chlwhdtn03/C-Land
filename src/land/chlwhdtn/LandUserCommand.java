package land.chlwhdtn;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;

import economy.chlwhdtn.MoneyFileManager;
import economy.chlwhdtn.MoneyManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import util.chlwhdtn.CUtil;

public class LandUserCommand implements CommandExecutor {

	public String prefix = "§a[토지] ";

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
		
		if(cmd.getName().equals("spawn")) {
			Player p = (Player)cs;
			if (isInRect(p.getLocation().getBlock(), new Location(null, -26, 21, -22),
					new Location(null, -13, 21, -4))) { // PVP장에서 사용하면
		
				if(p.getHealth() != p.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
					CUtil.addScore(p.getPlayer(), "penalty", "비매너", 1);
					p.sendTitle("§c비매너 행위!", "비매너 점수 1점을 부여합니다.",20,100,20);
				}	
				
				int deader_point;
				if((deader_point = CUtil.getScore(p, "아레나 포인트")) >= 5) {
					Bukkit.broadcastMessage("§7[§5PVP 아레나§7]§b " + p.getName() + "님이 " + deader_point+"점을 얻고 귀환했습니다.");
				}
				p.sendMessage("§7[§5PVP 아레나§7]§a " + deader_point + "점을 얻어, " + deader_point * 100 + "원이 지급되었습니다.");
				MoneyManager.addMoney(p.getName(), 100 * deader_point);
				MoneyFileManager.saveConfig();
				CUtil.resetTempScore(p, "아레나 포인트");
//				p.getScoreboard().resetScores("아레나 포인트");
//				sb.resetScores("아레나 포인트");
//				p.setScoreboard(sb);
			}
			

			
			Bukkit.getWorld("land").getSpawnLocation().add(0, -2, 0).getBlock().setType(Material.AIR);
			Bukkit.getWorld("land").getSpawnLocation().add(-1, -2, 0).getBlock().setType(Material.AIR);
			Bukkit.getWorld("land").getSpawnLocation().add(0, -2, -1).getBlock().setType(Material.AIR);
			Bukkit.getWorld("land").getSpawnLocation().add(-1, -2, -1).getBlock().setType(Material.AIR);
			((Player)cs).teleport(Bukkit.getWorld("land").getSpawnLocation().add(0, 1, 0));
			((Player)cs).setAllowFlight(false);
			((Player)cs).setAllowFlight(true);
			Bukkit.getScheduler().scheduleSyncDelayedTask(Land.land, new Runnable() {
				
				@Override
				public void run() {
					Bukkit.getWorld("land").getSpawnLocation().add(0, -2, 0).getBlock().setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("land").getSpawnLocation().add(-1, -2, 0).getBlock().setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("land").getSpawnLocation().add(0, -2, -1).getBlock().setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("land").getSpawnLocation().add(-1, -2, -1).getBlock().setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("land").spawnParticle(Particle.EXPLOSION_HUGE, 0, 3, 0, 1);
				}
			}, 10L);
			return true;
		}

		if (args.length == 0) {
			cs.sendMessage(ChatColor.AQUA + "/토지 목록 - 자신이 소유하고 있는 토지의 목록을 확인합니다.");
			cs.sendMessage(ChatColor.AQUA + "/토지 공유 <번호> <플레이어> - 소유중인 <토지>를 <플레이어>와 공유합니다.");
			cs.sendMessage(ChatColor.AQUA + "/토지 취소 <번호> <플레이어> - <플레이어>와의 <토지>공유를 끝냅니다.");
			cs.sendMessage(ChatColor.AQUA + "/토지 정보 <번호> - 토지 정보를 확인합니다.");
			cs.sendMessage(ChatColor.AQUA + "/토지 설정 <번호> [<burn>,<explode>] - 토지의 외부요인을 설정합니다");
			return true;
		}

		if (args[0].equals("목록")) {
			if (LandManager.hasLand(cs.getName()))
				cs.sendMessage(prefix + ChatColor.AQUA + "가지고 있는 땅");
			for (Landata land : LandManager.getLands(cs.getName())) {
				cs.sendMessage(land.name + " (화재 : " + (land.canburn ? "§cON" : "§aOFF") + " §f폭발 : " + (land.canexplode ? "§cON" : "§aOFF" ) + "§f)");
			}
		}
		
		if (args[0].equals("공유")) {
			if (args.length == 3) {
				if (!LandManager.isLand(args[1])) {
					cs.sendMessage(prefix + "§c존재하지 않는 땅 입니다.");
					return false;
				}
				if(LandManager.getLand(args[1]).owner == null) {
					cs.sendMessage(prefix + "§c당신은 그 땅의 주인이 아닙니다.");
					return false;
				}
				if(!LandManager.getLand(args[1]).owner.equals(cs.getName()) ){
					cs.sendMessage(prefix + "§c당신은 그 땅의 주인이 아닙니다.");
					return false;
				}
				Landata land = LandManager.getLand(args[1]);
				if(land.isSlave(args[2])) {
					cs.sendMessage(prefix + "§c그 플레이어는 이미 함께 공유중입니다.");
					return false;
				}
				land.addSlave(args[2]);
				cs.sendMessage(prefix + "§a" + land.name + "을 " + args[2] + "님과 함께 공유합니다.");
			} else {
				cs.sendMessage(prefix + "§c/토지 공유 <번호> <플레이어>");
			}
		}	
		
		if (args[0].equals("취소")) {
			if (args.length == 3) {
				if (!LandManager.isLand(args[1])) {
					cs.sendMessage(prefix + "§c존재하지 않는 땅 입니다.");
					return false;
				}
				if(LandManager.getLand(args[1]).owner == null) {
					cs.sendMessage(prefix + "§c당신은 그 땅의 주인이 아닙니다.");
					return false;
				}
				if(!LandManager.getLand(args[1]).owner.equals(cs.getName()) ){
					cs.sendMessage(prefix + "§c당신은 그 땅의 주인이 아닙니다.");
					return false;
				}
				Landata land = LandManager.getLand(args[1]);
				if(land.isSlave(args[2]) == false) {
					cs.sendMessage(prefix + "§c그 플레이어와 공유중이 아닙니다.");
					return false;
				}
				land.removeSlave(args[2]);
				cs.sendMessage(prefix + "§a" + land.name + "을 더이상 " + args[2] + "님과 함께 공유하지 않습니다.");
			} else {
				cs.sendMessage(prefix + "§c/토지 취소 <번호> <플레이어>");
			}
		}
		
			

		if (args[0].equals("정보")) {
			if (args.length == 2) {
				if (!LandManager.isLand(args[1])) {
					cs.sendMessage(prefix + "§c존재하지 않는 땅 입니다.");
					return false;
				}
				Landata land = LandManager.getLand(args[1]);
				cs.sendMessage(prefix + "-- " + land.name + " --");
				cs.sendMessage("소유주 : " + (land.owner == null ? "매물입니다." : land.owner));
				if(land.owner != null) {
					if(land.owner.equals(cs.getName())) {
						cs.sendMessage("화재 : " + (land.canburn ? "§c으악 불이야!" : "§a블럭이 불에 타지 않습니다."));
						cs.sendMessage("폭발 : " + (land.canexplode ? "§c뇌관 작동!" : "§a블럭이 폭발에 손실되지 않습니다."));
						if(land.slaves.isEmpty() == false) {
							cs.sendMessage(prefix + "함께 공유중인 플레이어");
							for(String slave : land.slaves) {
								cs.sendMessage(slave);
							}
						}
					}
				}
			} else {
				cs.sendMessage("§c/토지 정보 <번호>");
			}
		}
		if (args[0].equals("설정")) {
			if (args.length == 3) {
				if (!LandManager.isLand(args[1])) {
					cs.sendMessage(prefix + "§c존재하지 않는 땅 입니다.");
					return false;
				}
				if(LandManager.getLand(args[1]).owner == null) {
					cs.sendMessage(prefix + "§c당신은 그 땅의 주인이 아닙니다.");
					return false;
				}
				if(!LandManager.getLand(args[1]).owner.equals(cs.getName()) ){
					cs.sendMessage(prefix + "§c당신은 그 땅의 주인이 아닙니다.");
					return false;
				}
				
				Landata land = LandManager.getLand(args[1]);
				
				if(args[2].equals("burn")) {
					land.canburn = !land.canburn;
					cs.sendMessage(prefix + "§b" + args[1]+ "땅이 불로 인해 타는것이 " + (land.canburn ? "§c활성화 " : "§a비활성화 ") + "되었습니다.");
				}
				if(args[2].equals("explode")) {
					land.canexplode = !land.canexplode;
					cs.sendMessage(prefix + "§b" + args[1]+ "땅이 폭발로 인해 손실되는것이 " + (land.canexplode ? "§c활성화 " : "§a비활성화 ") + "되었습니다.");
				}
				} else {
				cs.sendMessage(prefix + "§c/토지 설정 <번호> burn - 땅이 불로 인해 타는것을 설정합니다");
				cs.sendMessage(prefix + "§c/토지 설정 <번호> explode - 땅이 폭발로 인해 손실되는것을 설정합니다");
			}
		}		
		return false;
	}
	
	public boolean isInRect(Block block, Location loc1, Location loc2) {
		double[] dim = new double[2];

		dim[0] = loc1.getX();
		dim[1] = loc2.getX();
		Arrays.sort(dim);
		if (block.getLocation().getX() > dim[1] || block.getLocation().getX() < dim[0])
			return false;

		dim[0] = loc1.getZ();
		dim[1] = loc2.getZ();
		Arrays.sort(dim);
		if (block.getLocation().getZ() > dim[1] || block.getLocation().getZ() < dim[0])
			return false;

		return true;
	}
}
