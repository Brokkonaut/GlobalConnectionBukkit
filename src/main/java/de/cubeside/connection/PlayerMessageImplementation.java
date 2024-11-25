package de.cubeside.connection;

import de.cubeside.connection.event.GlobalDataEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

class PlayerMessageImplementation implements PlayerMessageAPI, Listener {

    private final static int MESSAGE_CHAT = 1;
    private final static int MESSAGE_CHAT_COMPONENTS = 2;
    private final static int MESSAGE_ACTION_BAR = 3;
    private final static int MESSAGE_TITLE = 4;

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
                        } else if (type == MESSAGE_CHAT_COMPONENTS) {
                            BaseComponent[] message = ComponentSerializer.parse(dis.readUTF());
                            player.spigot().sendMessage(message);
                        } else if (type == MESSAGE_ACTION_BAR) {
                            String message = dis.readUTF();
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                        } else if (type == MESSAGE_TITLE) {
                            int flags = dis.readByte();
                            String title = ((flags & 1) != 0) ? dis.readUTF() : null;
                            String subtitle = ((flags & 2) != 0) ? dis.readUTF() : null;
                            int fadeInTicks = dis.readInt();
                            int durationTicks = dis.readInt();
                            int fadeOutTicks = dis.readInt();
                            player.sendTitle((title == null || title.isEmpty()) ? " " : title, (subtitle == null || subtitle.isEmpty()) ? " " : subtitle, fadeInTicks, durationTicks, fadeOutTicks);
                        }
                    }
                }
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "Could not parse PlayerMessage message", ex);
            }
        }
    }

    @Override
    public void sendMessage(GlobalPlayer player, String message) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeByte(MESSAGE_CHAT);
            dos.writeUTF(message);
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
    public void sendMessage(GlobalPlayer player, BaseComponent... message) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeByte(MESSAGE_CHAT_COMPONENTS);
            dos.writeUTF(ComponentSerializer.toString(message));
            dos.close();
        } catch (IOException ex) {
            throw new Error("impossible");
        }
        player.sendData(CHANNEL, baos.toByteArray());
        Player p = plugin.getServer().getPlayer(player.getUniqueId());
        if (p != null) {
            p.spigot().sendMessage(message);
        }
    }

    @Override
    public void sendMessage(GlobalPlayer player, Component message) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeByte(MESSAGE_CHAT_COMPONENTS);
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
    public void sendActionBarMessage(GlobalPlayer player, String message) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeByte(MESSAGE_ACTION_BAR);
            dos.writeUTF(message);
            dos.close();
        } catch (IOException ex) {
            throw new Error("impossible");
        }
        player.sendData(CHANNEL, baos.toByteArray());
        Player p = plugin.getServer().getPlayer(player.getUniqueId());
        if (p != null) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        }
    }

    @Override
    public void sendTitleBarMessage(GlobalPlayer player, String title, String subtitle, int fadeInTicks, int durationTicks, int fadeOutTicks) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeByte(MESSAGE_TITLE);
            int flags = (title != null ? 1 : 0) | (subtitle != null ? 2 : 0);
            dos.writeByte(flags);
            if (title != null) {
                dos.writeUTF(title);
            }
            if (subtitle != null) {
                dos.writeUTF(subtitle);
            }
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
            p.sendTitle((title == null || title.isEmpty()) ? " " : title, (subtitle == null || subtitle.isEmpty()) ? " " : subtitle, fadeInTicks, durationTicks, fadeOutTicks);
        }
    }

}
