package de.cubeside.connection;

import net.kyori.adventure.text.Component;

public interface PlayerMessageAPI {
    public void sendMessage(GlobalPlayer player, Component message);

    public void sendActionBarMessage(GlobalPlayer player, Component message);

    public void sendTitleBarMessage(GlobalPlayer player, Component title, Component subtitle, int fadeInTicks, int durationTicks, int fadeOutTicks);
}
