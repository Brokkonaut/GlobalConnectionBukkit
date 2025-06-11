package de.cubeside.connection;

import de.cubeside.connection.util.GlobalLocation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class GlobalClientPlugin extends JavaPlugin {
    private GlobalClientBukkit globalClient;
    private PlayerMessageAPI messageAPI;
    private PlayerPropertiesAPI propertiesAPI;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigurationSerialization.registerClass(GlobalLocation.class);
        globalClient = new GlobalClientBukkit(this);
        reconnectClient();
        getServer().getServicesManager().register(ConnectionAPI.class, globalClient, this, ServicePriority.Normal);

        messageAPI = new PlayerMessageImplementation(this);
        getServer().getServicesManager().register(PlayerMessageAPI.class, messageAPI, this, ServicePriority.Normal);

        propertiesAPI = new PlayerPropertiesImplementation(this);
        getServer().getServicesManager().register(PlayerPropertiesAPI.class, propertiesAPI, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        if (globalClient != null) {
            globalClient.shutdown();
        }
        globalClient = null;
        messageAPI = null;
        propertiesAPI = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("globalclient.reload")) {
            return false;
        }
        reloadConfig();
        reconnectClient();
        return true;
    }

    public void reconnectClient() {
        String account = getConfig().getString("client.account");
        String password = getConfig().getString("client.password");
        String host = getConfig().getString("server.host");
        int port = getConfig().getInt("server.port");
        if (!getConfig().isSet("server.connect")) {
            getConfig().set("server.connect", true);
            saveConfig();
        }
        boolean connect = getConfig().getBoolean("server.connect");
        if (!connect) {
            getLogger().warning("Not connecting to a server, because connecting is disabled in the config!");
        }
        globalClient.setServer(connect ? host : null, port, account, password);
    }

    public ConnectionAPI getConnectionAPI() {
        return globalClient;
    }

    public PlayerMessageAPI getMessageAPI() {
        return messageAPI;
    }

    public PlayerPropertiesAPI getPlayerPropertiesAPI() {
        return propertiesAPI;
    }
}
