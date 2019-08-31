package land.chlwhdtn;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public class LandUserCommand implements CommandExecutor {

	public String prefix = "§a[토지] ";

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {

		if (args.length == 0) {
			cs.sendMessage(ChatColor.AQUA + "/토지 목록 - 자신이 소유하고 있는 토지의 목록을 확인합니다.");
			cs.sendMessage(ChatColor.AQUA + "/토지 정보 <번호> - 토지 정보를 확인합니다.");
			cs.sendMessage(ChatColor.AQUA + "/토지 설정 <번호> [<burn>,<explode>] - 토지의 외부요인을 설정합니다");
			return true;
		}

		if (args[0].equals("목록")) {
			if (LandManager.hasLand(cs.getName()))
				cs.sendMessage(prefix + ChatColor.AQUA + "가지고 있는 땅");
			String str = "";
			for (Landata land : LandManager.getLands(cs.getName())) {
				cs.sendMessage(land.name + " + (화재 : " + (land.canburn ? "§cON" : "§aOFF") + " 폭발 : " + (land.canexplode ? "§cON" : "§aOFF"));
			}
		}

		if (args[0].equals("정보")) {
			if (args.length == 2) {
				if (!LandManager.isLand(args[1])) {
					cs.sendMessage(prefix + "§c존재하지 않는 땅 입니다.");
					return false;
				}
				Landata land = LandManager.getLand(args[1]);
				cs.sendMessage("-- " + land.name + " --");
				cs.sendMessage("소유주 : " + (land.owner == null ? "매물입니다." : land.owner));
				if(land.owner.equals(cs.getName())) {
					cs.sendMessage("화재 : " + (land.canburn ? "§c으악 불이야!" : "§a블럭이 불에 타지 않습니다."));
					cs.sendMessage("폭발 : " + (land.canexplode ? "§c뇌관 작동!" : "§a블럭이 폭발에 손실되지 않습니다."));
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
				if(!LandManager.getLand(args[1]).owner.equals(cs.getName())) {
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
}
