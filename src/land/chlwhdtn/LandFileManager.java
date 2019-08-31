package land.chlwhdtn;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;

import economy.chlwhdtn.Economy;

public class LandFileManager {

	private static FileConfiguration LandConfig = null;
	private static File LandConfigFile = null;

	public static void reloadConfig() {
		if (LandConfigFile == null) {
			LandConfigFile = new File(Economy.getInstance().getDataFolder(), "Land.yml");
		}
		LandConfig = YamlConfiguration.loadConfiguration(LandConfigFile);
		if (!LandConfig.contains("Land"))
			return;
		LandManager.landprice = LandConfig.getInt("price");
		for (String name : LandConfig.getConfigurationSection("Land").getKeys(false)) {
			LandManager.LoadLand(name, new Landata(name, LandConfig.getInt("Land." + name + ".startx"),
					LandConfig.getInt("Land." + name + ".endx"), LandConfig.getInt("Land." + name + ".startz"),
					LandConfig.getInt("Land." + name + ".endz"), LandConfig.getInt("Land." + name + ".size"), LandConfig.getString("Land." + name + ".owner")
					,LandConfig.getBoolean("Land." + name + ".canburn"),LandConfig.getBoolean("Land." + name + ".canexplode")));
		}
	}

	public static FileConfiguration getConfig() {
		if (LandConfig == null) {
			reloadConfig();
		}
		return LandConfig;
	}

	public static void saveConfig() {
		if (LandConfig == null || LandConfigFile == null) {
			return;
		}
		try {
			LandConfig.set("price", LandManager.landprice);
			for (String name : LandManager.getLandMap().keySet()) {
				LandConfig.set("Land." + name + ".startx", LandManager.getLandMap().get(name).startx);
				LandConfig.set("Land." + name + ".startz", LandManager.getLandMap().get(name).startz);
				LandConfig.set("Land." + name + ".endx", LandManager.getLandMap().get(name).endx);
				LandConfig.set("Land." + name + ".endz", LandManager.getLandMap().get(name).endz);
				LandConfig.set("Land." + name + ".size", LandManager.getLandMap().get(name).size);
				LandConfig.set("Land." + name + ".owner", LandManager.getLandMap().get(name).owner);
				LandConfig.set("Land." + name + ".canburn", LandManager.getLandMap().get(name).canburn);
				LandConfig.set("Land." + name + ".canexplode", LandManager.getLandMap().get(name).canexplode);
			}
			getConfig().save(LandConfigFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
