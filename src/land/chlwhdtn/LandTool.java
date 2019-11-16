package land.chlwhdtn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import economy.chlwhdtn.Economy;
import economy.chlwhdtn.MoneyFileManager;
import economy.chlwhdtn.MoneyManager;
import mine.chlwhdtn.Mine;
import mine.chlwhdtn.MineManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_14_R1.BlockPistonMoving;

public class LandTool implements CommandExecutor, Listener {

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			cs.sendMessage("/land sign - 토지 생성 도구 (반드시 교육 후 사용하세요 pdf참고)");
			cs.sendMessage("/land price <가격> - 토지 가격을 설정합니다. (현재 토지 가격(10x10기준) : " + LandManager.landprice + ")");
			return true;
		}

		if (args[0].equals("sign")) {

			if (cs.isOp() == false)
				return false;
			if (!(cs instanceof Player))
				return false;
			((Player) cs).getInventory().addItem(getLandTool());
			return true;
		}

		if (args[0].equals("price")) {

			if (cs.isOp() == false)
				return false;
			if (args.length == 1) {
				cs.sendMessage("현재 토지 가격 : " + LandManager.landprice);
			} else if (args.length == 2) {
				try {
					LandManager.landprice = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					cs.sendMessage("§c정수를 입력하세요");
					return false;
				}
				cs.sendMessage("§a토지의 가격을 " + LandManager.landprice + "원 으로 수정하였습니다!");
				LandFileManager.saveConfig();
			}
			return true;
		}

		// /land buy "+ sign.getLines()[2].split(" : ")[1] +" "+ sign.getLines()[1] +" "
		// +Bukkit.getPluginManager().hashCode()
		if (args[0].equals("buy")) {
			if (args[3].equals(Integer.toString(Bukkit.getPluginManager().hashCode()))) {
				if (LandManager.getLandMap().get(args[2]).owner != null) {
					cs.sendMessage(ChatColor.RED + "이미 땅 주인이 존재합니다.");
					return false;
				}
				if (MoneyManager.hasEnoghMoney(cs.getName(), Integer.parseInt(args[1]))) {
					LandManager.getLandMap().get(args[2]).owner = cs.getName();
					MoneyManager.addMoney(cs.getName(), -Integer.parseInt(args[1]));
					cs.sendMessage(ChatColor.GREEN + args[2] + "땅을 구매했습니다! (§c구매비용 "
							+ String.format("%,d원", Integer.parseInt(args[1])) + "§a)");
					cs.sendMessage(ChatColor.GREEN + "구입한 땅의 표지판을 파괴하여, 자신의 땅이라는것을 알리세요!");
					MoneyFileManager.saveConfig();
					LandFileManager.saveConfig();
					return true;
				} else {
					cs.sendMessage("§c돈이 충분하지 않습니다.");
					return false;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public static ItemStack getLandTool() {
		ItemStack is = new ItemStack(Material.OAK_SIGN);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("부동산땅 생성기");
		im.setLore(Arrays.asList("사고치면 밴", "반드시 사용법을 숙지하세요", "사고치면 밴"));
		is.setItemMeta(im);
		return is;
	}
	

	@EventHandler(priority = EventPriority.HIGH)
	public void onBurn(BlockBurnEvent event) {
		for (Landata data : LandManager.getLandMap().values()) {
			if (isInRect(event.getBlock(),
					new Location(event.getBlock().getLocation().getWorld(), data.startx, 0, data.startz),
					new Location(event.getBlock().getLocation().getWorld(), data.endx, 0, data.endz))) {
				if(data.canburn == false) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onExplode(EntityExplodeEvent event) {
		if (!event.getEntity().getWorld().getName().equals("land"))
			return;

		for (Landata data : LandManager.getLandMap().values()) {
			if (isInRect(event.getLocation().getBlock(),
					new Location(event.getEntity().getLocation().getWorld(), data.startx, 0, data.startz),
					new Location(event.getEntity().getLocation().getWorld(), data.endx, 0, data.endz))) {
				if(data.canexplode == false) {
					event.setCancelled(true);
					return;
				}
				List<Block> del_list = new ArrayList<Block>();
				
				for (Block b : event.blockList()) {
					if(b.getType().equals(Material.TNT)) {
						del_list.add(b);
						continue;
					}
					if (!isInRect(b,
							new Location(b.getLocation().getWorld(), data.startx, 0, data.startz),
							new Location(b.getLocation().getWorld(), data.endx, 0, data.endz))) {
						del_list.add(b);
					}
				}
				for(Block b : del_list) {
					event.blockList().remove(b);
				}
				return;
			}
		}
		if (event.getEntity().getWorld().getName().equals("land"))
			event.setCancelled(true);
	}
	
	
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		Bukkit.getWorld("land").getSpawnLocation().add(0, -2, 0).getBlock().setType(Material.AIR);
		Bukkit.getWorld("land").getSpawnLocation().add(-1, -2, 0).getBlock().setType(Material.AIR);
		Bukkit.getWorld("land").getSpawnLocation().add(0, -2, -1).getBlock().setType(Material.AIR);
		Bukkit.getWorld("land").getSpawnLocation().add(-1, -2, -1).getBlock().setType(Material.AIR);
		event.setRespawnLocation(Bukkit.getWorld("land").getSpawnLocation());
		Bukkit.getScheduler().scheduleSyncDelayedTask(Land.land, new Runnable() {
			
			@Override
			public void run() {
				Bukkit.getWorld("land").getSpawnLocation().add(0, -2, 0).getBlock().setType(Material.REDSTONE_BLOCK);
				Bukkit.getWorld("land").getSpawnLocation().add(-1, -2, 0).getBlock().setType(Material.REDSTONE_BLOCK);
				Bukkit.getWorld("land").getSpawnLocation().add(0, -2, -1).getBlock().setType(Material.REDSTONE_BLOCK);
				Bukkit.getWorld("land").getSpawnLocation().add(-1, -2, -1).getBlock().setType(Material.REDSTONE_BLOCK);
				Bukkit.getWorld("land").spawnParticle(Particle.EXPLOSION_HUGE, event.getRespawnLocation().add(0, 0, 0), 1);
			}
		}, 10L);
	}

	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onFlow(BlockFromToEvent event) {
		if (!event.getBlock().getWorld().getName().equals("land"))
			return;
		for (Landata data : LandManager.getLandMap().values()) {
			if (isInRect(event.getToBlock(),
					new Location(event.getBlock().getLocation().getWorld(), data.startx, 0, data.startz),
					new Location(event.getBlock().getLocation().getWorld(), data.endx, 0, data.endz))) {
				event.setCancelled(false);
				return;
			} else {
				event.setCancelled(true);
				continue;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPiston(BlockPistonExtendEvent event) {
		if (!event.getBlock().getWorld().getName().equals("land"))
			return;
		for (Landata data : LandManager.getLandMap().values()) {
			if (isInRect(event.getBlock(),
					new Location(event.getBlock().getLocation().getWorld(), data.startx, 0, data.startz),
					new Location(event.getBlock().getLocation().getWorld(), data.endx, 0, data.endz))) {
				for (Block b : event.getBlocks()) {
					if (!isInRect(b,
							new Location(event.getBlock().getLocation().getWorld(), data.startx, 0, data.startz),
							new Location(event.getBlock().getLocation().getWorld(), data.endx, 0, data.endz))) {

						event.setCancelled(true);
					}
				}
			} else {
				continue;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPiston(BlockPistonRetractEvent event) {
		if (!event.getBlock().getWorld().getName().equals("land"))
			return;

		for (Landata data : LandManager.getLandMap().values()) {
			if (isInRect(event.getBlock(),
					new Location(event.getBlock().getLocation().getWorld(), data.startx, 0, data.startz),
					new Location(event.getBlock().getLocation().getWorld(), data.endx, 0, data.endz))) {
				for (Block b : event.getBlocks()) {
					if (!isInRect(b,
							new Location(event.getBlock().getLocation().getWorld(), data.startx, 0, data.startz),
							new Location(event.getBlock().getLocation().getWorld(), data.endx, 0, data.endz))) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlace(BlockPlaceEvent event) {

		if (event.getPlayer().isOp())
			return;

		if (LandManager.hasLand(event.getPlayer().getName())) {
			for (Landata data : LandManager.getLands(event.getPlayer().getName())) {
				if (isInRect(event.getBlock(), new Location(event.getPlayer().getWorld(), data.startx, 0, data.startz),
						new Location(event.getPlayer().getWorld(), data.endx, 0, data.endz))) {
					return;
				}
			}
			event.setCancelled(true);
		}

		if (event.getBlock().getWorld().getName().equals("land"))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBreak(BlockBreakEvent event) {
		if(MineManager.isMinedBlock(event.getBlock().getLocation())) {
			return;
		}
		
		if (event.getBlock().getType().equals(Material.OAK_SIGN)) {
			Sign sign = ((Sign) event.getBlock().getState());
			if (sign.getLines()[0].equals("§a[토지]")) {

				if (LandManager.isLand(sign.getLines()[1])) { // 유효한 땅 표지판일 경우
					Landata data = LandManager.getLand(sign.getLines()[1]);
					if (event.getPlayer().getName().equals(data.owner) || event.getPlayer().isOp()) { // 땅 주인이 표지판을 건드리면
																										// (오피가 건드려도)
																										// 새로고침
						sign.setLine(0, "§a[토지]");
						sign.setLine(1, data.name);
						if (data.owner != null) {
							sign.setLine(2, "소유주 : " + data.owner);
						} else {
							sign.setLine(2, "매물 : " + String.format("%,d원", LandManager.getLandPrice(data.name)));
						}
						sign.setLine(3, (data.endx - data.startx + 1) + "x" + (data.endz - data.startz + 1));
						if (sign.update()) {
							event.getPlayer().playSound(sign.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
						}
						event.setCancelled(true);
						return;
					}
					event.setCancelled(true);
				}
				return;
			}
		}

		if (event.getPlayer().isOp())
			return;

		if (LandManager.hasLand(event.getPlayer().getName())) {
			for (Landata data : LandManager.getLands(event.getPlayer().getName())) {
				if (isInRect(event.getBlock(), new Location(event.getPlayer().getWorld(), data.startx, 0, data.startz),
						new Location(event.getPlayer().getWorld(), data.endx, 0, data.endz))) {
					return;
				}
			}
			event.setCancelled(true);
		}

		if (event.getBlock().getWorld().getName().equals("land")) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onRightClick(PlayerInteractEvent event) {
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			if(MineManager.isMinedBlock(event.getClickedBlock().getLocation())) {
				return;
			}
			if (event.getClickedBlock().getType().equals(Material.OAK_SIGN)) {
				Sign sign = ((Sign) event.getClickedBlock().getState());
				if (sign.getLines()[0].equals("§a[토지]")) {
					if (LandManager.getLand(sign.getLines()[1]).owner != null) {
						if (event.getPlayer().getName().equals(LandManager.getLand(sign.getLines()[1]).owner)) {
							event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
									new TextComponent("당신의 땅 입니다!"));
							return;
						}
						event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
								new TextComponent(LandManager.getLand(sign.getLines()[1]).owner + "님의 땅 입니다!"));
						return;
					}
					TextComponent text = new TextComponent("정말로 구매하시겠습니까? ");
					TextComponent confirm = new TextComponent(
							"클릭 시 " + sign.getLines()[2].split(" : ")[1] + "이 차감되며 땅을 얻게됩니다!");
					TextComponent confirm2 = new TextComponent("\n정말로 구매하시겠습니까?");
					confirm.addExtra(confirm2);
					TextComponent stat = new TextComponent(sign.getLines()[1] + " " + sign.getLines()[2]);
					stat.setColor(ChatColor.GOLD);
					text.addExtra(stat);
					TextComponent yestext = new TextComponent(" [예]");
					yestext.setHoverEvent(
							new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(confirm).create()));
					yestext.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
							"/land buy " + sign.getLines()[2].split(" : ")[1].split("원")[0].replace(",", "") + " "
									+ sign.getLines()[1] + " " + Bukkit.getPluginManager().hashCode()));
					yestext.setColor(ChatColor.GREEN);
					event.getPlayer().spigot().sendMessage(text, yestext);
				}
			} else {
				if (event.getPlayer().isOp())
					return;
				
				if (LandManager.hasLand(event.getPlayer().getName())) {
					for (Landata data : LandManager.getLands(event.getPlayer().getName())) {
						if (isInRect(event.getClickedBlock(), new Location(event.getPlayer().getWorld(), data.startx, 0, data.startz),
								new Location(event.getPlayer().getWorld(), data.endx, 0, data.endz))) {
							return;
						}
					}
					event.setCancelled(true);
				}
				
				if (event.getClickedBlock().getWorld().getName().equals("land"))
					event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onSignPlace(SignChangeEvent event) {
		if (!event.getBlock().getWorld().getName().equals("land"))
			return;
		if (event.getPlayer().getItemInHand().isSimilar(getLandTool())) {
			Landata data;
			int size;
			if(event.getLine(0).isEmpty()) {
				size = 10;
			} else {
				try {
				size = Integer.parseInt(event.getLine(0));
				} catch(NumberFormatException e) {
					event.getPlayer().sendMessage("§c땅 범위를 입력하려 하셨나요? 한 변의 길이만 입력해주세요. 정사각형입니다.");
					return;
				}
			}
			
			switch (getDirection(event.getPlayer())) {
			case "North":
				data = LandManager.addLand(event.getBlock().getX() + 1, event.getBlock().getZ() - size, size);
				break;
			case "South":
				data = LandManager.addLand(event.getBlock().getX() + 1, event.getBlock().getZ() + 1, size);
				break;
			case "East":
				data = LandManager.addLand(event.getBlock().getX() + 1, event.getBlock().getZ() + 1, size);
				break;
			case "West":
				data = LandManager.addLand(event.getBlock().getX() - size, event.getBlock().getZ() + 1,size);
				break;
			default:
				return;
			}
			double scale = size / 10; 
			event.setLine(0, "§a[토지]");
			event.setLine(1, data.name);
			event.setLine(2, "매물 : " + String.format("%,d원", LandManager.getLandPrice(data.name)));
			event.setLine(3, (data.endx - data.startx + 1) + "x" + (data.endz - data.startz + 1));
			// 유리판경계
			for (int i = data.startx; i <= data.endx; i++) {
				event.getPlayer().getWorld().getBlockAt(i, event.getBlock().getY(), data.startz - 1)
						.setType(Material.WHITE_STAINED_GLASS_PANE);
				event.getPlayer().getWorld().getBlockAt(i, event.getBlock().getY(), data.endz + 1)
						.setType(Material.WHITE_STAINED_GLASS_PANE);
			}
			for (int i = data.startz; i <= data.endz; i++) {
				event.getPlayer().getWorld().getBlockAt(data.startx - 1, event.getBlock().getY(), i)
						.setType(Material.WHITE_STAINED_GLASS_PANE);
				event.getPlayer().getWorld().getBlockAt(data.endx + 1, event.getBlock().getY(), i)
						.setType(Material.WHITE_STAINED_GLASS_PANE);
			}
			//블록경계
			for (int i = data.startx; i <= data.endx+1; i++) {
				event.getPlayer().getWorld().getBlockAt(i, event.getBlock().getY()-1, data.startz - 1)
						.setType(Material.WHITE_CONCRETE);
				event.getPlayer().getWorld().getBlockAt(i, event.getBlock().getY()-1, data.endz + 1)
						.setType(Material.WHITE_CONCRETE);
			}
			for (int i = data.startz-1; i <= data.endz+1; i++) {
				event.getPlayer().getWorld().getBlockAt(data.startx - 1, event.getBlock().getY()-1, i)
						.setType(Material.WHITE_CONCRETE);
				event.getPlayer().getWorld().getBlockAt(data.endx + 1, event.getBlock().getY()-1, i)
						.setType(Material.WHITE_CONCRETE);
			}
			LandFileManager.saveConfig();
		}
	}

	public String getDirection(Player playerSelf) {
		String dir = "";
		float y = playerSelf.getLocation().getYaw();
		if (y < 0) {
			y += 360;
		}
		y %= 360;
		int i = (int) ((y + 8) / 22.5);
		if (i == 0) {
			dir = "South";
		} else if (i == 4) {
			dir = "West";
		} else if (i == 8) {
			dir = "North";
		} else if (i == 12) {
			dir = "East";
		}
		return dir;
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
