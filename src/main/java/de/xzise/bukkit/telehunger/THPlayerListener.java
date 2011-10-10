package de.xzise.bukkit.telehunger;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class THPlayerListener extends PlayerListener {

    private final TeleHunger plugin;
    
    public THPlayerListener(TeleHunger plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (this.plugin.useEvent() && event.getFrom().getWorld() == event.getTo().getWorld() && !event.isCancelled()) {
            Player player = event.getPlayer();
            double distance = event.getFrom().distance(event.getTo());
            event.setCancelled(!this.plugin.handleTeleport(player, distance));
        }
    }
}
