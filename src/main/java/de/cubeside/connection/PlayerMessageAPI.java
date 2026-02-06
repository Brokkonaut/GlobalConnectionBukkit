package de.cubeside.connection;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;

public interface PlayerMessageAPI {
    @Deprecated
    public void sendMessage(GlobalPlayer player, String message);

    @Deprecated
    public void sendMessage(GlobalPlayer player, BaseComponent... message);

    public void sendMessage(GlobalPlayer player, Component message);

    public void sendActionBarMessage(GlobalPlayer player, Component message);

    public void sendTitleBarMessage(GlobalPlayer player, Component title, Component subtitle, int fadeInTicks, int durationTicks, int fadeOutTicks);
}
