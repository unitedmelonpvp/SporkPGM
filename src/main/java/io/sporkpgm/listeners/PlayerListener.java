package io.sporkpgm.listeners;

import io.sporkpgm.Spork;
import io.sporkpgm.player.SporkPlayer;
import io.sporkpgm.player.event.PlayingPlayerMoveEvent;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerMove(PlayerMoveEvent event) {
		SporkPlayer player = SporkPlayer.getPlayer(event.getPlayer());
		if(player.isObserver())
			return;

		PlayingPlayerMoveEvent ppme = new PlayingPlayerMoveEvent(event, player, event.getFrom(), event.getTo());
		Spork.callEvent(ppme);
		if(ppme.isCancelled()) {
			event.setTo(event.getFrom());
		}
	}

	@EventHandler
	public void onObserverMove(PlayerMoveEvent event) {
		SporkPlayer player = SporkPlayer.getPlayer(event.getPlayer());
		if(!player.isObserver())
			return;

		if(player.getPlayer().getLocation().getBlockY() <= 0) {
			player.setTeam(player.getTeam(), false, false, true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		SporkPlayer player = SporkPlayer.getPlayer(event.getPlayer());
		if(player.isObserver())
			return;

		PlayingPlayerMoveEvent ppme = new PlayingPlayerMoveEvent(event, player, event.getFrom(), event.getTo());
		Spork.callEvent(ppme);
		if(ppme.isCancelled()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onVehicleEnter(VehicleEnterEvent event) {
		if(event.getEntered() instanceof Player) {
			SporkPlayer player = SporkPlayer.getPlayer((Player) event.getEntered());
			if(player.isObserver())
				return;

			PlayingPlayerMoveEvent ppme = new PlayingPlayerMoveEvent(event, player, player.getPlayer().getLocation(), event.getVehicle().getLocation());
			Spork.callEvent(ppme);
			if(ppme.isCancelled()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onAchievementAwarded(PlayerAchievementAwardedEvent event) {
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		SporkPlayer player = SporkPlayer.getPlayer(event.getPlayer());
		player.updateInventory();
		if(!player.isParticipating()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		SporkPlayer player = SporkPlayer.getPlayer(event.getPlayer());
		player.updateInventory();
		if(player.isObserver()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			SporkPlayer damager = SporkPlayer.getPlayer((Player) event.getDamager());
			SporkPlayer victim = SporkPlayer.getPlayer((Player) event.getEntity());

			if(victim.getTeam().equals(damager.getTeam())) {
				event.setCancelled(true);
			} else {
				victim.updateInventory();
				damager.updateInventory();
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event) {
		SporkPlayer player = SporkPlayer.getPlayer(event.getPlayer());
		if(!player.isParticipating()) {
			event.setCancelled(true);
		}

		player.updateInventory();
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType().equals(Material.CHEST)) {

			if(player.isObserver()) {
				event.setCancelled(true);
				Chest chest = (Chest) event.getClickedBlock().getState();
				player.getPlayer().openInventory(chest.getInventory());
			}
		} else if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if(player.isObserver() && (!event.getPlayer().getItemInHand().getType().equals(Material.AIR) || event.getPlayer().getItemInHand().equals(Material.COMPASS))) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.getRightClicked() instanceof Player) {
			SporkPlayer player = SporkPlayer.getPlayer((Player) event.getRightClicked());

			if(!player.isParticipating()) {
				event.getPlayer().openInventory(player.getInventory());
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getWhoClicked() instanceof Player) {
			SporkPlayer player = SporkPlayer.getPlayer((Player) event.getWhoClicked());

			if(player.isObserver() && !event.getInventory().equals(player.getPlayer().getInventory())) {
				event.setCancelled(true);
			}

			if(!event.isCancelled()) {
				player.updateInventory();
			}
		}
	}

}
