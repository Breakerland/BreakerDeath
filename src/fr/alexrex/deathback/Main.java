package fr.alexrex.deathback;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import fr.alexrex.deathback.commands.Command;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {
	public static Economy economy = null;
	
	@Override
	public void onEnable() {
		Command instance = new Command(this);
        getServer().getPluginManager().registerEvents(instance, this);
        saveDefaultConfig();
        this.getCommand("dback").setExecutor(instance);
	}
	
    public boolean setupEconomy(){
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }
	
}
