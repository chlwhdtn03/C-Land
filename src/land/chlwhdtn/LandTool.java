package land.chlwhdtn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import economy.chlwhdtn.Economy;
import economy.chlwhdtn.MoneyFileManager;
import economy.chlwhdtn.MoneyManager;
import gamble.chlwhdtn.Gamble;
import gamble.chlwhdtn.Machine;
import gamble.chlwhdtn.MachineManager;
import mine.chlwhdtn.Mine;
import mine.chlwhdtn.MineManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import util.chlwhdtn.CUtil;

public class LandTool implements CommandExecutor, Listener {

	int taskid;

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			cs.sendMessage("/land change <땅번호> <플레이어> - 땅 주인을 강제로 변경합니다.");
			cs.sendMessage("/land sign - 토지 생성 도구 (반드시 교육 후 사용하세요 pdf참고)");
			cs.sendMessage("/land price <가격> - 토지 가격을 설정합니다. (현재 토지 가격(10x10기준) : " + LandManager.landprice + ")");
			cs.sendMessage("/land snow - 크리스마스! 스폰구역에 눈이 내립니다!");
			return true;
		}

		if (args[0].equals("snow")) {

			if (cs.isOp() == false)
				return false;
			World world = Bukkit.getWorld("land");
			for (int x = -25; x <= 25; x++) {
				for (int y = 0; y <= 10; y++) {
					for (int z = -25; z <= 25; z++) {
						world.setBiome(x, y, z, Biome.SNOWY_BEACH);
					}
				}
			}
			cs.sendMessage("완료!");
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

		if (args[0].equals("change")) {

			if (cs.isOp() == false)
				return false;
			if (!(cs instanceof Player))
				return false;
			if (args.length == 2) {
				if (LandManager.isLand(args[1]) == false) {
					cs.sendMessage("§c존재하지 않는 땅입니다.");
					return false;
				}
				LandManager.getLand(args[1]).setOwner(null);
				LandFileManager.saveConfig();
				cs.sendMessage("§a" + args[1] + "의 주인을 초기화했습니다.");
			} else if (args.length == 3) {
				if (LandManager.isLand(args[1]) == false) {
					cs.sendMessage("§c존재하지 않는 땅입니다.");
					return false;
				}
				LandManager.getLand(args[1]).setOwner(args[2]);
				LandFileManager.saveConfig();
				cs.sendMessage("§a" + args[1] + "의 주인을 " + args[2] + " 로 변경했습니다.");
			}
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
				if (LandManager.getLands(cs.getName()).size() >= 4) {
					cs.sendMessage("§c땅을 4개 이상 가질 수 없습니다. (추후 개선)");
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
				if (data.canburn == false) {
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
				if (data.canexplode == false) {
					event.setCancelled(true);
					return;
				}
				List<Block> del_list = new ArrayList<Block>();

				for (Block b : event.blockList()) {
					if (b.getType().equals(Material.TNT)) {
						del_list.add(b);
						continue;
					}
					if (!isInRect(b, new Location(b.getLocation().getWorld(), data.startx, 0, data.startz),
							new Location(b.getLocation().getWorld(), data.endx, 0, data.endz))) {
						del_list.add(b);
					}
				}
				for (Block b : del_list) {
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
		event.getPlayer().setAllowFlight(false);
		event.getPlayer().setAllowFlight(true);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Land.land, new Runnable() {

			@Override
			public void run() {
				Bukkit.getWorld("land").getSpawnLocation().add(0, -2, 0).getBlock().setType(Material.REDSTONE_BLOCK);
				Bukkit.getWorld("land").getSpawnLocation().add(-1, -2, 0).getBlock().setType(Material.REDSTONE_BLOCK);
				Bukkit.getWorld("land").getSpawnLocation().add(0, -2, -1).getBlock().setType(Material.REDSTONE_BLOCK);
				Bukkit.getWorld("land").getSpawnLocation().add(-1, -2, -1).getBlock().setType(Material.REDSTONE_BLOCK);
				Bukkit.getWorld("land").spawnParticle(Particle.EXPLOSION_HUGE, event.getRespawnLocation(), 1);
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
		for (Landata data : LandManager.getLandMap().values()) {
			if (data.getOwner() == null)
				continue;
			if (isInRect(event.getBlock(), new Location(event.getPlayer().getWorld(), data.startx, 0, data.startz),
					new Location(event.getPlayer().getWorld(), data.endx, 0, data.endz))) {
				if (data.getOwner().equals(event.getPlayer().getName()) || data.isSlave(event.getPlayer().getName())) {
					return;
				}
			}
		}

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBreak(BlockBreakEvent event) {
		if (MineManager.isMinedBlock(event.getBlock().getLocation())) {
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

		for (Landata data : LandManager.getLandMap().values()) {
			if (data.getOwner() == null)
				continue;
			if (isInRect(event.getBlock(), new Location(event.getPlayer().getWorld(), data.startx, 0, data.startz),
					new Location(event.getPlayer().getWorld(), data.endx, 0, data.endz))) {
				if (data.getOwner().equals(event.getPlayer().getName()) || data.isSlave(event.getPlayer().getName())) {
					return;
				}
			}
		}

		if (event.getBlock().getWorld().getName().equals("land")) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onRightClick(PlayerInteractEvent event) {
		if (event.getAction().equals(Action.PHYSICAL)) {
			if(event.getClickedBlock().getLocation().getBlockX() == -8 || event.getClickedBlock().getLocation().getBlockY() == 8 || event.getClickedBlock().getLocation().getBlockZ() == 23) { // 점프맵 1단계 보상
				CUtil.setScore(event.getPlayer(), "pakuru", "파쿠르", 1);
				event.getPlayer().sendMessage(ChatColor.GREEN + "[파쿠르] 이제 더블점프가 가능합니다!");
				return;
			} else if(event.getClickedBlock().getLocation().getBlockX() == -25 || event.getClickedBlock().getLocation().getBlockY() == 10 || event.getClickedBlock().getLocation().getBlockZ() == 19) { // 점프맵 2단계 보상
					CUtil.setScore(event.getPlayer(), "pakuru", "파쿠르", 2);
				event.getPlayer().sendMessage(ChatColor.GREEN + "[파쿠르] 더블점프가 2단계로 향상됩니다!");
				return;
			}

			for (Landata data : LandManager.getLandMap().values()) {
				if (data.getOwner() == null) {
					continue;
				}
				if (isInRect(event.getClickedBlock(),
						new Location(event.getPlayer().getWorld(), data.startx, 0, data.startz),
						new Location(event.getPlayer().getWorld(), data.endx, 0, data.endz))) {
					if (data.getOwner().equals(event.getPlayer().getName())
							|| data.isSlave(event.getPlayer().getName())) {
						return;
					}
				}
			}

			if (event.getClickedBlock().getType().equals(Material.FARMLAND)) {
				event.setCancelled(true);
				return;
			}

		} else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
				|| event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			if (event.getClickedBlock().getType().equals(Material.ENDER_CHEST)) {
				return;
			}
			if (MineManager.isMinedBlock(event.getClickedBlock().getLocation())) {
				return;
			}
			Machine target;
			if ((target = MachineManager.getMachineButton(event.getClickedBlock().getLocation())) != null) {
				if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
					return;
				if (target.isRunning) {
					event.getPlayer().sendMessage("§c해당 기계가 작동중입니다. 끝날때까지 기다려주세요.");
					return;
				}

				if (!MoneyManager.hasEnoghMoney(event.getPlayer().getName(), 3000)) {
					event.getPlayer().sendMessage("§c돈이 모자랍니다.");
					return;
				}
				if (event.getPlayer().isOp() == false) {
					MoneyManager.addMoney(event.getPlayer().getName(), -3000);
					MoneyFileManager.saveConfig();
				}
				Sign sign = ((Sign) Bukkit.getWorld("land").getBlockAt(-11, 6, 10).getState());
				long origin = Long.parseLong(sign.getLine(1));
				origin += 3000;
				sign.setLine(1, origin + "");
				sign.setLine(2, "마지막 이용자");
				sign.setLine(3, event.getPlayer().getName());
				
				sign.update();
				

				target.isRunning = true;
				event.getPlayer().sendMessage("§a도박이 시작되었습니다!");
				Bukkit.getWorld("land").playSound(event.getClickedBlock().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1,
						1);

				taskid = Bukkit.getScheduler().scheduleSyncRepeatingTask(Economy.getInstance(), new Runnable() {

					double second = 0;
					int thisid = taskid;
					int temp;
					int rand = 1;

					@Override
					public void run() {
						if (second == 10) {
							if (target.B1.getBlock().getType().equals(target.B2.getBlock().getType())
									&& target.B1.getBlock().getType().equals(target.B3.getBlock().getType())) {
								Bukkit.getWorld("land").playSound(event.getClickedBlock().getLocation(),
										Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1, 1);
								Sign sign = ((Sign) Bukkit.getWorld("land").getBlockAt(-11, 6, 11).getState());
								long origin = Long.parseLong(sign.getLine(1));
								switch (target.B1.getBlock().getType()) {
								case IRON_BLOCK:
									Bukkit.broadcastMessage(
											"§a[도박장] " + event.getPlayer().getName() + "님이 5등에 당첨됬습니다!");
									MoneyManager.addMoney(event.getPlayer().getName(), 5000);
									origin += 5000;
									sign.setLine(1, origin + "");
									sign.update();
									break;
								case GOLD_BLOCK:
									Bukkit.broadcastMessage(
											"§a[도박장] " + event.getPlayer().getName() + "님이 4등에 당첨됬습니다!");
									MoneyManager.addMoney(event.getPlayer().getName(), 10000);
									origin += 10000;
									sign.setLine(1, origin + "");
									sign.update();
									break;
								case DIAMOND_BLOCK:
									Bukkit.broadcastMessage(
											"§a[도박장] " + event.getPlayer().getName() + "님이 3등에 당첨됬습니다!");
									MoneyManager.addMoney(event.getPlayer().getName(), 50000);
									origin += 50000;
									sign.setLine(1, origin + "");
									sign.update();
									break;
								case EMERALD_BLOCK:
									Bukkit.broadcastMessage(
											"§a[도박장] " + event.getPlayer().getName() + "님이 2등에 당첨됬습니다!");
									MoneyManager.addMoney(event.getPlayer().getName(), 200000);
									origin += 200000;
									sign.setLine(1, origin + "");
									sign.update();
									break;
								case LAPIS_BLOCK:
									Bukkit.broadcastMessage(
											"§a[도박장] " + event.getPlayer().getName() + "님이 1등에 당첨됬습니다!");
									MoneyManager.addMoney(event.getPlayer().getName(), 500000);
									origin += 500000;
									sign.setLine(1, origin + "");
									sign.setLine(2, "마지막 1등");
									sign.setLine(3, event.getPlayer().getName());
									sign.update();
									break;
								}
								MoneyFileManager.saveConfig();
							} else {
								event.getPlayer().sendMessage("§c꽝입니다...");
							}
							target.isRunning = false;
							Bukkit.getScheduler().cancelTask(thisid);
						}
						if (second == 3 || second == 6 || second == 9) {
							Bukkit.getWorld("land").playSound(event.getClickedBlock().getLocation(),
									Sound.BLOCK_ANVIL_PLACE, 1, 1);
						}
						switch (rand) {
						case 1:
							if (second < 3)
								target.B1.getBlock().setType(Material.IRON_BLOCK);
							if (second < 6)
								target.B2.getBlock().setType(Material.IRON_BLOCK);
							if (second < 9)
								target.B3.getBlock().setType(Material.IRON_BLOCK);
							break;
						case 2:
							if (second < 3)
								target.B1.getBlock().setType(Material.GOLD_BLOCK);
							if (second < 6)
								target.B2.getBlock().setType(Material.GOLD_BLOCK);
							if (second < 9)
								target.B3.getBlock().setType(Material.GOLD_BLOCK);
							break;
						case 3:
							if (second < 3)
								target.B1.getBlock().setType(Material.DIAMOND_BLOCK);
							if (second < 6)
								target.B2.getBlock().setType(Material.DIAMOND_BLOCK);
							if (second < 9)
								target.B3.getBlock().setType(Material.DIAMOND_BLOCK);
							break;
						case 4:
							if (second < 3)
								target.B1.getBlock().setType(Material.EMERALD_BLOCK);
							if (second < 6)
								target.B2.getBlock().setType(Material.EMERALD_BLOCK);
							if (second < 9)
								target.B3.getBlock().setType(Material.EMERALD_BLOCK);
							break;
						case 5:
							if (second < 3)
								target.B1.getBlock().setType(Material.LAPIS_BLOCK);
							if (second < 6)
								target.B2.getBlock().setType(Material.LAPIS_BLOCK);
							if (second < 9)
								target.B3.getBlock().setType(Material.LAPIS_BLOCK);
							break;

						}
						second += 0.25;
						temp = rand;
						while (true) {
							rand = new Random().nextInt(5) + 1;
							if (rand == 5)
								rand = new Random().nextInt(5) + 1;
							if (temp != rand)
								break;
						}
					}
				}, 0, 5L);
				return;
			} else if (event.getClickedBlock().getType().equals(Material.OAK_SIGN)) {
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
				if (event.getPlayer().isOp()) {
					if (event.getItem() != null) {
						if (event.getItem().getType().equals(Material.COMPASS)) {
							for (Landata data : LandManager.getLandMap().values()) {
								if (isInRect(event.getClickedBlock(),
										new Location(event.getPlayer().getWorld(), data.startx, 0, data.startz),
										new Location(event.getPlayer().getWorld(), data.endx, 0, data.endz))) {
									event.getPlayer().sendMessage(data.name + " 토지 입니다.");
								}
							}
						}
					}
					return;
				}

				for (Landata data : LandManager.getLandMap().values()) {
					if (data.getOwner() == null)
						continue;
					if (isInRect(event.getClickedBlock(),
							new Location(event.getPlayer().getWorld(), data.startx, 0, data.startz),
							new Location(event.getPlayer().getWorld(), data.endx, 0, data.endz))) {
						if (data.getOwner().equals(event.getPlayer().getName())
								|| data.isSlave(event.getPlayer().getName())) {
							return;
						}
					}
				}

				if (event.getClickedBlock().getWorld().getName().equals("land")) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onBucketEmpty(PlayerBucketEmptyEvent e) {
		if (e.getPlayer().isOp())
			return;
		for (Landata data : LandManager.getLandMap().values()) {
			if (data.getOwner() == null)
				continue;
			if (isInRect(e.getBlockClicked(), new Location(e.getPlayer().getWorld(), data.startx, 0, data.startz),
					new Location(e.getPlayer().getWorld(), data.endx, 0, data.endz))) {
				if (data.getOwner().equals(e.getPlayer().getName()) || data.isSlave(e.getPlayer().getName())) {
					return;
				}
			}
		}
		if (e.getBlockClicked().getWorld().getName().equals("land")) {
			e.setCancelled(true);
			return;
		}

	}

	@EventHandler
	public void onGrow(BlockGrowEvent event) {
		for (Landata data : LandManager.getLandMap().values()) {
			if (data.getOwner() == null)
				continue;
			if (isInRect(event.getBlock(), new Location(event.getBlock().getWorld(), data.startx, 0, data.startz),
					new Location(event.getBlock().getWorld(), data.endx, 0, data.endz))) {
				if (Bukkit.getPlayer(data.getOwner()) != null) {
					return;
				} else {
					for (String pname : data.slaves) {
						if (Bukkit.getPlayer(pname) != null) {
							return;
						}
					}
				}
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBucketFill(PlayerBucketFillEvent e) {
		if (e.getPlayer().isOp())
			return;
		for (Landata data : LandManager.getLandMap().values()) {
			if (data.getOwner() == null)
				continue;
			if (isInRect(e.getBlockClicked(), new Location(e.getPlayer().getWorld(), data.startx, 0, data.startz),
					new Location(e.getPlayer().getWorld(), data.endx, 0, data.endz))) {
				if (data.getOwner().equals(e.getPlayer().getName()) || data.isSlave(e.getPlayer().getName())) {
					return;
				}
			}
		}
		if (e.getBlock().getWorld().getName().equals("land")) {
			e.setCancelled(true);
			return;
		}

	}

	@EventHandler(ignoreCancelled = true)
	public void onHitVehicle(VehicleDamageEvent e) {
		Vehicle target = e.getVehicle();
		if (e.getAttacker() instanceof Player) {
			Player attacker = (Player) e.getAttacker();

			for (Landata data : LandManager.getLandMap().values()) {
				if (data.getOwner() == null)
					continue;
				if (isInRect(target.getLocation().getBlock(),
						new Location(target.getWorld(), data.startx, 0, data.startz),
						new Location(target.getWorld(), data.endx, 0, data.endz))) { // 맞는 얘가 어느땅에 있는지 확인
					if (data.getOwner().equals(attacker.getName()) || data.isSlave(attacker.getName())) { // 때린얘가 그 땅
																											// 주인이면
						return; // 진행
					}
				}
			}

			if (attacker.isOp())
				return; // 진행

			e.setCancelled(true);

		} else if (e.getAttacker() instanceof Projectile) {
			Projectile attacker = (Projectile) e.getAttacker();
			Player shooter = (Player) attacker.getShooter();

			for (Landata data : LandManager.getLandMap().values()) {
				if (data.getOwner() == null)
					continue;
				if (isInRect(target.getLocation().getBlock(),
						new Location(target.getWorld(), data.startx, 0, data.startz),
						new Location(target.getWorld(), data.endx, 0, data.endz))) { // 맞는 얘가 어느땅에 있는지 확인
					if (data.getOwner().equals(attacker.getName()) || data.isSlave(attacker.getName())) { // 때린얘가 그 땅
																											// 주인이면
						return; // 진행
					}
				}
			}

			if (shooter.isOp())
				return;

			e.setCancelled(true);

		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onHit(EntityDamageByEntityEvent e) {
		Entity target = e.getEntity();
		if (e.getEntityType().equals(EntityType.VILLAGER))
			return;

		if (e.getDamager() instanceof Player) {
			Player attacker = (Player) e.getDamager();

			for (Landata data : LandManager.getLandMap().values()) {
				if (data.getOwner() == null)
					continue;
				if (isInRect(target.getLocation().getBlock(),
						new Location(target.getWorld(), data.startx, 0, data.startz),
						new Location(target.getWorld(), data.endx, 0, data.endz))) { // 맞는 얘가 어느땅에 있는지 확인
					if (data.getOwner().equals(attacker.getName()) || data.isSlave(attacker.getName())) { // 때린얘가 그 땅
																											// 주인이면
						return; // 진행
					}
				}
			}

			if (isInRect(e.getEntity().getLocation().getBlock(), new Location(null, -26, 21, -22),
					new Location(null, -13, 21, -4)))
				return;

			if (attacker.isOp())
				return; // 진행

			e.setCancelled(true);

		} else if (e.getDamager() instanceof Projectile) {
			Projectile attacker = (Projectile) e.getDamager();
			Player shooter = (Player) attacker.getShooter();

			if (isInRect(e.getEntity().getLocation().getBlock(), new Location(null, -26, 21, -22),
					new Location(null, -13, 21, -4)))
				return;

			for (Landata data : LandManager.getLandMap().values()) {
				if (data.getOwner() == null)
					continue;
				if (isInRect(target.getLocation().getBlock(),
						new Location(target.getWorld(), data.startx, 0, data.startz),
						new Location(target.getWorld(), data.endx, 0, data.endz))) { // 맞는 얘가 어느땅에 있는지 확인
					if (data.getOwner().equals(attacker.getName()) || data.isSlave(attacker.getName())) { // 때린얘가 그 땅
																											// 주인이면
						return; // 진행
					}
				}
			}

			if (shooter.isOp())
				return;

			e.setCancelled(true);

		}
	}

	@EventHandler
	public void onDead(PlayerDeathEvent e) {
		if (isInRect(e.getEntity().getLocation().getBlock(), new Location(null, -26, 21, -22),
				new Location(null, -13, 21, -4))) { // PVP장에서
			if (e.getEntity().getKiller() == null) {
				CUtil.resetTempScore(e.getEntity(), "아레나 포인트");
				return;
			}
			if (!(e.getEntity().getKiller() instanceof Player))
				return;

			CUtil.addTempScore(e.getEntity().getKiller(), "아레나 포인트", 1 + CUtil.getScore(e.getEntity(), "아레나 포인트") / 2);
			System.out.println(CUtil.getScore(e.getEntity().getKiller(), "아레나 포인트") + 1
					+ CUtil.getScore(e.getEntity(), "아레나 포인트") / 2);
//			Scoreboard sb = e.getEntity().getKiller().getScoreboard();
//			Objective obj;
//			if ((obj = sb.getObjective("stat")) == null) {
//				sb = Bukkit.getScoreboardManager().getNewScoreboard();
//				obj = sb.registerNewObjective("stat", "dummy", "상태", RenderType.INTEGER);
//			}
//			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
//
//			obj.getScore("아레나 포인트").setScore(obj.getScore("아레나 포인트").getScore() + 1
//					+ e.getEntity().getScoreboard().getObjective("stat").getScore("아레나 포인트").getScore() / 2);
//			

//			if ((e.getEntity().getScoreboard().getObjective("stat").getScore("아레나 포인트").getScore() / 2) >= 1) {
			if (CUtil.getScore(e.getEntity(), "아레나 포인트") / 2 >= 1) {
				e.getEntity().getKiller().sendMessage("§7[§5PVP 아레나§7]§b 연속 처치자를 죽여 "
						+ CUtil.getScore(e.getEntity(), "아레나 포인트") / 2 + "점을 추가 획득했습니다");
			}
//			if (obj.getScore("아레나 포인트").getScore() == 5) {
			if (CUtil.getScore(e.getEntity().getKiller(), "아레나 포인트") == 5) {
				Bukkit.broadcastMessage("§7[§5PVP 아레나§7]§b " + e.getEntity().getKiller().getName() + "님이 5점을 획득했습니다.");
			} else if (CUtil.getScore(e.getEntity().getKiller(), "아레나 포인트") == 10) {
				Bukkit.broadcastMessage("§7[§5PVP 아레나§7]§b " + e.getEntity().getKiller().getName() + "님이 10점을 획득했습니다.");
			}
			int deader_point;
//			if ((deader_point = e.getEntity().getScoreboard().getObjective("stat").getScore("아레나 포인트")
//					.getScore()) >= 5) {
			if ((deader_point = CUtil.getScore(e.getEntity(), "아레나 포인트")) >= 5) {
				Bukkit.broadcastMessage(
						"§7[§5PVP 아레나§7]§b " + e.getEntity().getName() + "님이 " + deader_point + "점을 얻고 전사했습니다.");
			}
			e.getEntity()
					.sendMessage("§7[§5PVP 아레나§7]§a " + deader_point + "점을 얻어, " + deader_point * 100 + "원이 지급되었습니다.");
			MoneyManager.addMoney(e.getEntity().getName(), 100 * deader_point);
			MoneyFileManager.saveConfig();
			CUtil.resetTempScore(e.getEntity(), "아레나 포인트");
		}
	}

	@EventHandler // 싸우다 나갈경우
	public void onLeave(PlayerQuitEvent event) {
		if (isInRect(event.getPlayer().getLocation().getBlock(), new Location(null, -26, 21, -22),
				new Location(null, -13, 21, -4))) { // PVP장에서
			if (event.getPlayer().getHealth() != event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH)
					.getValue()) {
				CUtil.addScore(event.getPlayer(), "penalty", "비매너", 1);
			}
		}
	}

	@EventHandler
	public void onInventoryOpening(InventoryOpenEvent e) {
		Inventory inv = e.getInventory();
		if (e.getPlayer() instanceof Player) {
			Player p = (Player) e.getPlayer();
			if (p.isOp())
				return;
			if (inv.getHolder() instanceof Minecart) {
				for (Landata data : LandManager.getLandMap().values()) {
					if (data.getOwner() == null)
						continue;
					if (isInRect(e.getInventory().getLocation().getBlock(),
							new Location(e.getPlayer().getWorld(), data.startx, 0, data.startz),
							new Location(e.getPlayer().getWorld(), data.endx, 0, data.endz))) {
						if (data.getOwner().equals(e.getPlayer().getName()) || data.isSlave(e.getPlayer().getName())) {
							return;
						}
					}
				}
				e.setCancelled(true);
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
			int firstID;
			String dir;
			if (event.getLine(0).isEmpty()) {
				event.getPlayer().sendMessage("§c첫번째 줄에 번지수를 입력해주세요");
				return;
			}
			try {
				firstID = Integer.parseInt(event.getLine(0));
				if (firstID <= 0)
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				event.getPlayer().sendMessage("§c번지수를 양수로 입력해주세요.");
				return;
			}

			if (event.getLine(1).isEmpty()) {
				event.getPlayer().sendMessage("§c두번째 줄에 표지판의 위치를 입력해주세요. left 또는 right");
				return;
			}
			dir = event.getLine(1);
			if (!(dir.equalsIgnoreCase("left") || dir.equalsIgnoreCase("right"))) {
				event.getPlayer().sendMessage("§c두번째 줄에 표지판의 위치만 입력해주세요. left 또는 right");
				return;
			}

			if (event.getLine(2).isEmpty()) {
				size = 10;
			} else {
				try {
					size = Integer.parseInt(event.getLine(2));
				} catch (NumberFormatException e) {
					event.getPlayer().sendMessage("§c땅 범위를 입력하려 하셨나요? 한 변의 길이만 입력해주세요. 정사각형입니다.");
					return;
				}
			}

			switch (getDirection(event.getPlayer())) {
			case "North":
				if (dir.equalsIgnoreCase("left"))
					data = LandManager.addLand(firstID, event.getBlock().getX() + 1, event.getBlock().getZ() - size,
							size);
				else // right
					data = LandManager.addLand(firstID, event.getBlock().getX() - size, event.getBlock().getZ() - size,
							size);
				break;
			case "South":
				if (dir.equalsIgnoreCase("right"))
					data = LandManager.addLand(firstID, event.getBlock().getX() + 1, event.getBlock().getZ() + 1, size);
				else // left
					data = LandManager.addLand(firstID, event.getBlock().getX() - size, event.getBlock().getZ() + 1,
							size);
				break;
			case "East":
				if (dir.equalsIgnoreCase("left"))
					data = LandManager.addLand(firstID, event.getBlock().getX() + 1, event.getBlock().getZ() + 1, size);
				else
					data = LandManager.addLand(firstID, event.getBlock().getX() + 1, event.getBlock().getZ() - size,
							size);
				break;
			case "West":
				if (dir.equalsIgnoreCase("right"))
					data = LandManager.addLand(firstID, event.getBlock().getX() - size, event.getBlock().getZ() + 1,
							size);
				else
					data = LandManager.addLand(firstID, event.getBlock().getX() - size, event.getBlock().getZ() - size,
							size);
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
			// 블록경계
			for (int i = data.startx; i <= data.endx + 1; i++) {
				event.getPlayer().getWorld().getBlockAt(i, event.getBlock().getY() - 1, data.startz - 1)
						.setType(Material.WHITE_CONCRETE);
				event.getPlayer().getWorld().getBlockAt(i, event.getBlock().getY() - 1, data.endz + 1)
						.setType(Material.WHITE_CONCRETE);
			}
			for (int i = data.startz - 1; i <= data.endz + 1; i++) {
				event.getPlayer().getWorld().getBlockAt(data.startx - 1, event.getBlock().getY() - 1, i)
						.setType(Material.WHITE_CONCRETE);
				event.getPlayer().getWorld().getBlockAt(data.endx + 1, event.getBlock().getY() - 1, i)
						.setType(Material.WHITE_CONCRETE);
			}
			LandFileManager.saveConfig();
		}
	}

	@EventHandler
	public void onPortal(PlayerPortalEvent event) {
		if (event.getFrom().getWorld().getName().equals("land")) {
			event.setCancelled(true);

			CUtil.addTempScore(event.getPlayer(), "아레나 포인트", 0);

//			Scoreboard sb = event.getPlayer().getScoreboard();
//			Objective obj;
//			if ((obj = sb.getObjective("stat")) == null) {
//				sb = Bukkit.getScoreboardManager().getNewScoreboard();
//				obj = sb.registerNewObjective("stat", "dummy", "상태", RenderType.INTEGER);
//			}
//			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
//
//			obj.getScore("아레나 포인트").setScore(0);
//
//			event.getPlayer().setScoreboard(sb);

//			if(event.getPlayer().isOp() == false) {
//				if(!MoneyManager.hasEnoghMoney(event.getPlayer().getName(), 5000)) {
//					event.getPlayer().sendMessage("§c가지고 있는 돈이 부족합니다.");
//					return;
//				}
//				
//				MoneyManager.addMoney(event.getPlayer().getName(), -5000);
//				MoneyFileManager.saveConfig();
//			}
			Location loc = null;
			double randomX;
			double randomZ;
			int randomXW;
			int randomZW;
			while (true) {
				randomX = new Random().nextInt(13) + -26;
				randomZ = new Random().nextInt(18) + -22;
				loc = new Location(Bukkit.getWorld("land"), randomX, 7, randomZ);
				if (loc.add(0, -1, 0).getBlock().getType().equals(Material.WATER)
						|| loc.add(0, -1, 0).getBlock().getType().equals(Material.BLACK_CONCRETE)) {
					continue;
				}
				break;
			}
			event.getPlayer().setAllowFlight(false);
			event.getPlayer().teleport(loc);
			event.getPlayer().sendMessage("§aPVP장으로 이동되었습니다!");
			event.getPlayer().sendMessage("§a여기서 플레이어를 죽일때마다 아레나 포인트를 획득합니다.");
			event.getPlayer().sendMessage("§b죽거나 돌아갈 경우 아레나 포인트를 통해 보상을 지급합니다");
			event.getPlayer().sendMessage("§b돌아가시려면 /spawn을 입력하세요!");
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler
	public void onMakePortal(PortalCreateEvent event) {
		if (event.getEntity().isOp() == false) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onJoinGiveFly(PlayerJoinEvent e) {
		e.getPlayer().setAllowFlight(true);

	}
	
	@EventHandler
	public void onChangeGamemode(PlayerGameModeChangeEvent event) {
		event.getPlayer().setAllowFlight(true);
	}
	
	

	@EventHandler
	public void onDoubleJump(PlayerToggleFlightEvent e) {
		Player player = e.getPlayer();
		if(player.getGameMode().equals(GameMode.CREATIVE))
			return;
		if(CUtil.getScore(player, "pakuru") == 0) {
			e.setCancelled(true);
			return;
		}
		if (player.getGameMode().equals(GameMode.SURVIVAL)) {
			e.setCancelled(true);
			Block b = player.getWorld().getBlockAt(player.getLocation().subtract(0, 2, 0));
			if (!b.getType().equals(Material.AIR)) {
				Vector v = player.getLocation().getDirection().multiply(CUtil.getScore(player, "pakuru") * 0.5).setY(CUtil.getScore(player, "pakuru") * 0.5);
				player.setVelocity(v);
			}
		}
	}

	@EventHandler
	public void onFishing(PlayerFishEvent event) {
		event.getHook().setGlowing(true);
		event.getHook().setCustomNameVisible(true);
		event.getHook().setCustomName(event.getPlayer().getName() + "의 낚시바늘");
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
