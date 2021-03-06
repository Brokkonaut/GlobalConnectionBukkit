package de.cubeside.connection;

import de.cubeside.connection.event.GlobalDataEvent;
import de.cubeside.connection.event.GlobalPlayerDisconnectedEvent;
import de.cubeside.connection.event.GlobalPlayerJoinedEvent;
import de.cubeside.connection.event.GlobalServerConnectedEvent;
import de.cubeside.connection.event.GlobalServerDisconnectedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

class GlobalClientBukkit extends GlobalClient implements Listener {
    private final GlobalClientPlugin plugin;
    private boolean stoppingServer;

    public GlobalClientBukkit(GlobalClientPlugin connectionPlugin) {
        super(connectionPlugin.getLogger());
        plugin = connectionPlugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void setServer(String host, int port, String account, String password) {
        super.setServer(host, port, account, password);
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            onPlayerOnline(p.getUniqueId(), p.getName(), System.currentTimeMillis());
        }
    }

    @Override
    protected void runInMainThread(Runnable r) {
        if (!stoppingServer) {
            plugin.getServer().getScheduler().runTask(plugin, r);
        }
    }

    @Override
    protected void processData(GlobalServer source, String channel, GlobalPlayer targetPlayer, GlobalServer targetServer, byte[] data) {
        plugin.getServer().getPluginManager().callEvent(new GlobalDataEvent(source, targetPlayer, channel, data));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        onPlayerOnline(e.getPlayer().getUniqueId(), e.getPlayer().getName(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        onPlayerOffline(e.getPlayer().getUniqueId());
    }

    @Override
    protected void onPlayerJoined(GlobalServer server, GlobalPlayer player, boolean joinedTheNetwork) {
        plugin.getServer().getPluginManager().callEvent(new GlobalPlayerJoinedEvent(server, player, joinedTheNetwork));
    }

    @Override
    protected void onPlayerDisconnected(GlobalServer server, GlobalPlayer player, boolean leftTheNetwork) {
        plugin.getServer().getPluginManager().callEvent(new GlobalPlayerDisconnectedEvent(server, player, leftTheNetwork));
    }

    @Override
    protected void onServerConnected(GlobalServer server) {
        plugin.getServer().getPluginManager().callEvent(new GlobalServerConnectedEvent(server));
    }

    @Override
    protected void onServerDisconnected(GlobalServer server) {
        plugin.getServer().getPluginManager().callEvent(new GlobalServerDisconnectedEvent(server));
    }

    @Override
    public void shutdown() {
        this.stoppingServer = true;
        super.shutdown();
    }
}