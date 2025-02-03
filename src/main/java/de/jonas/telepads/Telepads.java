package de.jonas.telepads;

import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import de.jonas.telepads.Teleportation.TeleportationManager;
import de.jonas.telepads.commands.Admin;
import de.jonas.telepads.commands.GiveBuildItem;
import de.jonas.telepads.commands.GivePortableTeleportItem;
import de.jonas.telepads.commands.ReloadCommand;
import de.jonas.telepads.commands.Teleportation;
import de.jonas.telepads.listener.OpenGui;
import de.jonas.telepads.listener.PreventChangePad;
import de.jonas.telepads.listener.UseTelepad;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import net.milkbowl.vault.economy.Economy;

public class Telepads extends JavaPlugin{
    
    private static Economy econ = null;
    public static Telepads INSTANCE;
    public DataBasePool basePool;
    public Events events;
    public TeleportationManager teleportationManager;

    @Override
    public void onLoad() {

        INSTANCE = this;

        this.saveDefaultConfig();

        teleportationManager = new TeleportationManager();
        basePool = new DataBasePool();
        basePool.init();

        try {
            basePool.createTableTelepads();
            basePool.createTableTelePermission();
            basePool.createTableTeleFavorites();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        events = new Events();

        if (!CommandAPI.isLoaded()) CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
        new ReloadCommand();   
        new GiveBuildItem();
        new GivePortableTeleportItem();
        new Admin();
        new Teleportation();
    }

    @Override
    public void onEnable() {

        this.listener();

        CommandAPI.onEnable();

        if (!setupEconomy()) getLogger().log(Level.WARNING, "Economy wasnt setupped correctly, have you installed Vault and an Eco-plugin?");

    }

    @Override
    public void onDisable() {

        CommandAPI.onDisable();

        basePool.shutdown();

    }

    public void listener() {
        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new OpenGui(), this);
        pm.registerEvents(new PreventChangePad(), this);
        pm.registerEvents(new UseTelepad(), this);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

}
