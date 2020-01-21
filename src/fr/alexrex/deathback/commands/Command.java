package fr.alexrex.deathback.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.earth2me.essentials.utils.LocationUtil;

import fr.alexrex.deathback.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Command implements CommandExecutor,Listener {
	
	private HashMap<Player, Location> dPlayer = new HashMap<Player,Location>();
	private static Main main;

	public Command(Main main) {
		Command.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(cmd.getName().equalsIgnoreCase("dback")) {
				if(args.length > 0) {
					if(args[0].equalsIgnoreCase("reload") && player.hasPermission("dback.reload")) {
						main.reloadConfig();
						player.sendMessage("§6La config de DeathBack a été reload");
					}
					return false;
				}
				if(dPlayer.containsKey(player)) {
					if(testBalance(player, getPrice(player))) {
						if (dPlayer.get(player).getY() < 1){
							String errorcoord = main.getConfig().getString("error_coord");
							errorcoord = transformConfig(errorcoord, player);
							player.sendMessage(errorcoord);
							return true;
						}
						player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,main.getConfig().getInt("tresi")*20,255) ,true);
						player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,main.getConfig().getInt("tresi")*20,255) ,true);
						wait(player);						
						Location safeLocation = null;
						try {
							safeLocation = LocationUtil.getSafeDestination(dPlayer.get(player));
						} catch (Exception e) {
							e.printStackTrace();
						}
						player.teleport(safeLocation);
						dPlayer.remove(player);
						Main.economy.withdrawPlayer(player, getPrice(player));
						
						String valid = main.getConfig().getString("valid_teleportation");						
						valid = transformConfig(valid, player);
						player.sendMessage(valid);					
					} else{
						String errormoney = main.getConfig().getString("error_money");
						errormoney = transformConfig(errormoney, player);
						player.sendMessage(errormoney);
					}
				} else {
					String errortp = main.getConfig().getString("error_tp");
					errortp = transformConfig(errortp, player);
					player.sendMessage(errortp);
				}
			}
		}
		return false;
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity().getPlayer();
		Location loc = player.getLocation();
		dPlayer.put(player, loc);
        
        TextComponent declineText = new TextComponent();
        declineText.setText(ChatColor.GREEN + "[ACCEPTER]");
        declineText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dback"));
        declineText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Accepter la téléportation").create()));
        
        TextComponent broadText = new TextComponent();
        broadText.setText(ChatColor.GOLD + " Voulez vous vous tp à votre point de mort ?");
        
        player.spigot().sendMessage(broadText);
        player.spigot().sendMessage(declineText);
	}
	
	public Boolean testBalance(Player player, double montant) {
		Boolean b = false;
		if(main.setupEconomy()) {
			double balance = Main.economy.getBalance(player);
			if(balance >= montant) {
				b = true;
			} else {
				b = false;
			}
		}
		return b;
	}
	
	public Integer getPrice(Player player) {
		Integer price = null;
		List<String> grade = Arrays.asList("survivant","aventurier","baron","duc","comte","marquis","hero","legende","fantome");
		for(String g : grade) {
			if(player.hasPermission("dback." + g)) {
				price = main.getConfig().getInt(g);
				return price;
			}
		}
		return 0;
	}
	
	public String transformConfig(String s, Player player) {
		s = s.replace("{PLAYER}", player.getName());
		s = s.replace("{PRICE}", getPrice(player).toString());
		s = s.replace("{MONEY}",main.getConfig().getString("money"));
		s = ChatColor.translateAlternateColorCodes('&',s);
		//s = s.replaceAll("(&([a-f0-9]))", "\u00A7$2");
		return s;
	}	
	
	public static Runnable wait(final Player player) { // You'll most likely want to include a final player parameter
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
            public void run() {
                player.setHealth(20);
                player.setFireTicks(0);
            }
        }, main.getConfig().getInt("tresi")*20L); // 600L (ticks) is equal to 30 seconds (20 ticks = 1 second)
        return null;
	}
}
