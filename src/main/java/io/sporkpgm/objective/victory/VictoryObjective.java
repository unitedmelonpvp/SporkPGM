package io.sporkpgm.objective.victory;

import io.sporkpgm.Spork;
import io.sporkpgm.map.SporkMap.ScoreAPI;
import io.sporkpgm.map.event.BlockChangeEvent;
import io.sporkpgm.match.phase.ServerPhase;
import io.sporkpgm.module.ModuleInfo;
import io.sporkpgm.module.builder.Builder;
import io.sporkpgm.objective.ObjectiveModule;
import io.sporkpgm.player.SporkPlayer;
import io.sporkpgm.region.types.BlockRegion;
import io.sporkpgm.team.SporkTeam;
import io.sporkpgm.util.Log;
import io.sporkpgm.util.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.meta.FireworkMeta;

import java.lang.reflect.Method;

@ModuleInfo(name = "VictoryObjective", description = "Defines a region where blocks fall")
public class VictoryObjective extends ObjectiveModule {

	String name;
	DyeColor dye;
	ChatColor color;
	BlockRegion place;

	SporkPlayer completer;
	boolean complete;

	SporkTeam team;
	OfflinePlayer player;
	StringBuilder spaces;

	public VictoryObjective(String name, SporkTeam team, DyeColor dye, ChatColor color, BlockRegion place) {
		this.name = name;
		this.team = team;
		this.dye = dye;
		this.color = color;
		this.place = place;

		update();
	}

	@Override
	public SporkTeam getTeam() {
		return team;
	}

	@Override
	public boolean isComplete() {
		return complete;
	}

	@Override
	public void setComplete(boolean complete) {
		this.complete = complete;
		if(!complete) {
			return;
		}

		StringBuilder builder = new StringBuilder();
		builder.append(color).append(name.toUpperCase()).append(ChatColor.GRAY).append(" was completed by ");
		builder.append(ChatColor.AQUA).append(completer.getName());

		Bukkit.broadcastMessage(builder.toString());
		update();
		Location location = completer.getPlayer().getLocation();
		Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
		FireworkMeta meta = firework.getFireworkMeta();
		meta.clearEffects();
		meta.addEffect(getFirework());
		firework.setFireworkMeta(meta);

		try {
			Method method = firework.getClass().getMethod("getHandle");
			method.setAccessible(true);
			method.invoke(firework);
		} catch(Exception e) {
			Log.warning("Server isn't running a version of Bukkit which allows the use of Firework.detonate() - resorting to manual detonation.");
			try {
				Object craft = NMSUtil.getClassBukkit("entity.CraftFirework").cast(firework);
				Method method = craft.getClass().getMethod("getHandle");
				method.setAccessible(true);
				Object handle = method.invoke(craft);
				handle.getClass().getField("expectedLifespan").set(handle, 0);
			} catch(Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	@Override
	public void update() {
		int score = 0;
		if(this.player != null) {
			score = team.getMap().getObjective().getScore(player).getScore();
			ScoreAPI.reset(team.getMap().getObjective().getScore(player));
		}

		this.spaces = new StringBuilder();

		String title = getStatusColour() + name + spaces.toString();
		if(title.length() > 16) {
			title = title.substring(0, 16);
		}
		this.player = Spork.get().getServer().getOfflinePlayer(title);

		while(ScoreAPI.isSet(team.getMap().getObjective().getScore(player))) {
			spaces.append(" ");

			title = getStatusColour() + name + spaces.toString();
			if(title.length() > 16) {
				String coloured = (getStatusColour() + name).substring(0, 15 - spaces.length());
				title = coloured + spaces.toString();
			}

			player = Spork.get().getServer().getOfflinePlayer(title);
		}

		team.getMap().getObjective().getScore(player).setScore(score);
	}

	@Override
	public ChatColor getStatusColour() {
		return (isComplete() ? ChatColor.GREEN : ChatColor.RED);
	}

	@Override
	public OfflinePlayer getPlayer() {
		return player;
	}

	@EventHandler
	public void onBlockChange(BlockChangeEvent event) {
		if(!event.hasPlayer()) {
			if(place.isInside(event.getLocation())) {
				event.setCancelled(true);
			}

			return;
		}

		SporkPlayer player = event.getPlayer();
		if(place.isInside(event.getLocation())) {
			if(event.isBreak()) {
				event.setCancelled(true);
				player.getPlayer().sendMessage(ChatColor.RED + "You can't break that block");
				return;
			} else {
				if(player.getTeam() != team) {
					event.setCancelled(true);
					player.getPlayer().sendMessage(ChatColor.RED + "You can't place blocks there");
					return;
				}

				Block block = event.getNewBlock();
				if(block.getType() != Material.WOOL || block.getState().getData().getData() != dye.getWoolData()) {
					event.setCancelled(true);
					player.getPlayer().sendMessage(ChatColor.RED + "You can't place that block there");
					return;
				}

				completer = player;
				setComplete(true);
				event.setCancelled(false);
			}
		}
	}

	public FireworkEffect getFirework() {
		FireworkEffect.Builder builder = FireworkEffect.builder();
		builder.withColor(ServerPhase.getColor(color));
		builder.with(Type.BALL_LARGE);
		builder.trail(false);
		builder.flicker(true);
		return builder.build();
	}

	public Class<? extends Builder> builder() {
		return VictoryBuilder.class;
	}

	@Override
	public String toString() {
		return "VictoryObjective{name=" + name + ",dye=" + dye.name() + ",color=" + color.name() + ",place=" + place.toString() + ",complete=" + complete + "}";
	}

}
