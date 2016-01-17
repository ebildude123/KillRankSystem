package com.weidizhang.killranksystem;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	PlayerPointsDB playerDB;
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		saveDefaultConfig();
		
		File dataFolder = new File(getDataFolder().getAbsolutePath());
		if (!dataFolder.exists() || !dataFolder.isDirectory()) {
			dataFolder.mkdirs();
		}
		playerDB = new PlayerPointsDB(getDataFolder().getAbsolutePath() + File.separator + "points.db");
		
		getLogger().info("KillRankSystem by ebildude123 (Weidi Zhang) is now fully initialized");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("krs")) {
			Player p = null;
			if (sender instanceof Player) {
				p = (Player) sender;
				if (!p.hasPermission("killranksystem.admin")) {
					p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
					return true;
				}
			}
			
			if (args.length == 3) {
				String command = args[0];
				String playerName = args[1];
				int amount = 0;
				
				try {
					amount = Integer.parseInt(args[2]);
				}
				catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "Invalid amount number specified.");
				}
				
				int playerPoints = playerDB.getPoints(playerName);
				
				if (command.equalsIgnoreCase("give")) {
					playerDB.addPoints(playerName, amount);
					checkPromotionAndDemotion(playerName, playerPoints, playerPoints + amount);
					
					sender.sendMessage(ChatColor.GREEN + playerName + " now has " + (playerPoints + amount) + " points.");
				}
				else if (command.equalsIgnoreCase("take")) {
					playerDB.removePoints(playerName, amount);
					checkPromotionAndDemotion(playerName, playerPoints, playerPoints - amount);
					
					sender.sendMessage(ChatColor.GREEN + playerName + " now has " + (playerPoints - amount) + " points.");
				}
				else if (command.equalsIgnoreCase("set")) {
					playerDB.setPoints(playerName, amount);
					checkPromotionAndDemotion(playerName, playerPoints, amount);
					
					sender.sendMessage(ChatColor.GREEN + playerName + " now has " + amount + " points.");
				}
				else {
					sender.sendMessage(ChatColor.RED + "Usage: /krs <give|take|set> <player name> <amount>");
				}
			}
			else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					reloadConfig();
					sender.sendMessage(ChatColor.GREEN + "KillRankSystem by ebildude123: Configuration reloaded");
				}
			}
			else {
				sender.sendMessage(ChatColor.DARK_AQUA + "Usages:");
				sender.sendMessage(ChatColor.DARK_AQUA + "Reload Config: /krs reload");
				sender.sendMessage(ChatColor.DARK_AQUA + "Edit Points: /krs <give|take|set> <player name> <amount>");
			}
			
			return true;
		}
		return false;
	}
	
	public String getRankFromPoints(int points) {
		String realRank = "";
		Set<String> ranks = getConfig().getConfigurationSection("ranks").getKeys(false);
		for (String rank : ranks) {
			int ptsRequired = getConfig().getInt("ranks." + rank + ".points");
			if (points >= ptsRequired) {
				realRank = rank;
			}
		}
		
		return realRank;
	}
	
	public void checkPromotionAndDemotion(String p, int prevPoints, int newPoints) {
		String previousRank = getRankFromPoints(prevPoints);
		String newRank = getRankFromPoints(newPoints);
		
		if (!previousRank.equals(newRank)) {
			int ptsRequiredPrev = getConfig().getInt("ranks." + previousRank + ".points");
			int ptsRequiredNew = getConfig().getInt("ranks." + newRank + ".points");
			
			List<String> commands = null;
			if (ptsRequiredNew < ptsRequiredPrev) {
				commands = getConfig().getStringList("ranks." + previousRank + ".onDemotionCommands");
			}
			else if (ptsRequiredNew > ptsRequiredPrev) {
				commands = getConfig().getStringList("ranks." + newRank + ".onPromotionCommands");				
			}
			
			if (commands != null) {
				for (String cmd : commands) {
					cmd = cmd.replaceAll("%player%", p);
					getServer().dispatchCommand(getServer().getConsoleSender(), cmd);
				}
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		int playerPoints = playerDB.getPoints(e.getPlayer().getName());		
		
		String pointsPrefix = getConfig().getString("pointsPrefix").replaceAll("%points%", playerPoints + "");
		pointsPrefix = ChatColor.translateAlternateColorCodes('&', pointsPrefix);
		
		String rank = getRankFromPoints(playerPoints);
		
		String rankPrefix = "";
		if (!rank.equals("")) {
			rankPrefix = getConfig().getString("ranks." + rank + ".prefix");
		}
		rankPrefix = ChatColor.translateAlternateColorCodes('&', rankPrefix);
		
		e.setFormat(pointsPrefix + rankPrefix + e.getFormat());
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player died = e.getEntity();
		Player killer = died.getKiller();
		
		if (killer != null) {
			int killerPoints = playerDB.getPoints(killer.getName());
			int pointsAdd = getConfig().getInt("pointsPerKill");
			
			playerDB.addPoints(killer.getName(), pointsAdd);			
			checkPromotionAndDemotion(killer.getName(), killerPoints, killerPoints + pointsAdd);
		}
		
		boolean onlyKilledDeaths = getConfig().getBoolean("onlyPlayerCausedDeaths");
		if ((onlyKilledDeaths && killer != null) || !onlyKilledDeaths) {
			int pointsRemove = getConfig().getInt("pointsLostDeath");
			int diedPoints = playerDB.getPoints(died.getName());
			int pointsToSet = diedPoints - pointsRemove;
			
			if (!getConfig().getBoolean("allowNegativePoints")) {
				pointsToSet = 0;
			}
			
			playerDB.setPoints(died.getName(), pointsToSet);
			checkPromotionAndDemotion(died.getName(), diedPoints, pointsToSet);			
		}
	}
}