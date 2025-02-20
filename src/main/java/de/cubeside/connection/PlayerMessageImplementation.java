package de.cubeside.connection;

import com.google.common.base.Preconditions;
import de.cubeside.connection.event.GlobalDataEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

class PlayerMessageImplementation implements PlayerMessageAPI, Listener {

    private final static int MESSAGE_CHAT = 1;
    private final static int MESSAGE_CHAT_COMPONENT = 2;
    private final static int MESSAGE_ACTION_BAR = 3;
    private final static int MESSAGE_TITLE = 4;
    private final static int MESSAGE_ACTION_BAR_COMPONENT = 5;
    private final static int MESSAGE_TITLE_COMPONENT = 6;

    private GlobalClientPlugin plugin;

    private final static String CHANNEL = "GlobalClient.chat";

    public PlayerMessageImplementation(GlobalClientPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onGlobalData(GlobalDataEvent e) {
        if (e.getChannel().equals(CHANNEL)) {
            DataInputStream dis = new DataInputStream(e.getData());
            try {
                GlobalPlayer target = e.getTargetPlayer();
                if (target != null) {
                    Player player = plugin.getServer().getPlayer(target.getUniqueId());
                    if (player != null) {
                        int type = dis.readByte();
                        if (type == MESSAGE_CHAT) {
                            String message = dis.readUTF();
                            player.sendMessage(message);
                        } else if (type == MESSAGE_CHAT_COMPONENT) {
                            Component message = JSONComponentSerializer.json().deserialize(dis.readUTF());
                            player.sendMessage(message);
                        } else if (type == MESSAGE_ACTION_BAR) {
                            String message = dis.readUTF();
                            player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(message));
                        } else if (type == MESSAGE_TITLE) {
                            int flags = dis.readByte();
                            String title = ((flags & 1) != 0) ? dis.readUTF() : null;
                            String subtitle = ((flags & 2) != 0) ? dis.readUTF() : null;
                            int fadeInTicks = dis.readInt();
                            int durationTicks = dis.readInt();
                            int fadeOutTicks = dis.readInt();
                            Component titleComponent = LegacyComponentSerializer.legacySection().deserialize((title == null || title.isEmpty()) ? " " : title);
                            Component subtitleComponent = LegacyComponentSerializer.legacySection().deserialize((subtitle == null || subtitle.isEmpty()) ? " " : subtitle);
                            Title titleToSend = Title.title(titleComponent, subtitleComponent, Times.times(Duration.ofMillis(fadeInTicks * 50), Duration.ofMillis(durationTicks * 50), Duration.ofMillis(fadeOutTicks * 50)));
                            player.showTitle(titleToSend);
                        } else if (type == MESSAGE_ACTION_BAR_COMPONENT) {
                            Component message = JSONComponentSerializer.json().deserialize(dis.readUTF());
                            player.sendActionBar(message);
                        } else if (type == MESSAGE_TITLE_COMPONENT) {
                            String title = dis.readUTF();
                            String subtitle = dis.readUTF();
                            int fadeInTicks = dis.readInt();
                            int durationTicks = dis.readInt();
                            int fadeOutTicks = dis.readInt();
                            Component titleComponent = JSONComponentSerializer.json().deserialize(title);
                            Component subtitleComponent = JSONComponentSerializer.json().deserialize(subtitle);
                            Title titleToSend = Title.title(titleComponent, subtitleComponent, Times.times(Duration.ofMillis(fadeInTicks * 50), Duration.ofMillis(durationTicks * 50), Duration.ofMillis(fadeOutTicks * 50)));
                            player.showTitle(titleToSend);
                        }
                    }
                }
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Could not parse PlayerMessage message", ex);
            }
        }
    }

    @Deprecated
    @Override
    public void sendMessage(GlobalPlayer player, String message) {
        sendActionBarMessage(player, LegacyComponentSerializer.legacySection().deserialize(message));
    }

    @Deprecated
    @Override
    public void sendMessage(GlobalPlayer player, BaseComponent... message) {
        sendActionBarMessage(player, JSONComponentSerializer.json().deserialize(ComponentSerializer.toString(message)));
    }

    @Deprecated
    @Override
    public void sendActionBarMessage(GlobalPlayer player, String message) {
        sendActionBarMessage(player, LegacyComponentSerializer.legacySection().deserialize(message));
    }

    @Deprecated
    @Override
    public void sendTitleBarMessage(GlobalPlayer player, String title, String subtitle, int fadeInTicks, int durationTicks, int fadeOutTicks) {
        Component titleComponent = title == null ? Component.empty() : LegacyComponentSerializer.legacySection().deserialize(title);
        Component subtitleComponent = subtitle == null ? Component.empty() : LegacyComponentSerializer.legacySection().deserialize(subtitle);
        sendTitleBarMessage(player, titleComponent, subtitleComponent, fadeInTicks, durationTicks, fadeOutTicks);
    }

    @Override
    public void sendMessage(GlobalPlayer player, Component message) {
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(message, "message");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeByte(MESSAGE_CHAT_COMPONENT);
            dos.writeUTF(JSONComponentSerializer.json().serialize(message));
            dos.close();
        } catch (IOException ex) {
            throw new Error("impossible");
        }
        player.sendData(CHANNEL, baos.toByteArray());
        Player p = plugin.getServer().getPlayer(player.getUniqueId());
        if (p != null) {
            p.sendMessage(message);
        }
    }

    @Override
    public void sendActionBarMessage(GlobalPlayer player, Component message) {
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(message, "message");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeByte(MESSAGE_ACTION_BAR_COMPONENT);
            dos.writeUTF(JSONComponentSerializer.json().serialize(message));
            dos.close();
        } catch (IOException ex) {
            throw new Error("impossible");
        }
        player.sendData(CHANNEL, baos.toByteArray());
        Player p = plugin.getServer().getPlayer(player.getUniqueId());
        if (p != null) {
            p.sendActionBar(message);
        }
    }

    @Override
    public void sendTitleBarMessage(GlobalPlayer player, Component title, Component subtitle, int fadeInTicks, int durationTicks, int fadeOutTicks) {
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(title, "title");
        Preconditions.checkNotNull(subtitle, "subtitle");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeByte(MESSAGE_TITLE_COMPONENT);
            dos.writeUTF(JSONComponentSerializer.json().serialize(title));
            dos.writeUTF(JSONComponentSerializer.json().serialize(subtitle));
            dos.writeInt(fadeInTicks);
            dos.writeInt(durationTicks);
            dos.writeInt(fadeOutTicks);
            dos.close();
        } catch (IOException ex) {
            throw new Error("impossible");
        }
        player.sendData(CHANNEL, baos.toByteArray());
        Player p = plugin.getServer().getPlayer(player.getUniqueId());
        if (p != null) {
            Title titleToSend = Title.title(title, subtitle, Times.times(Duration.ofMillis(fadeInTicks * 50), Duration.ofMillis(durationTicks * 50), Duration.ofMillis(fadeOutTicks * 50)));
            p.showTitle(titleToSend);
        }
    }

}
