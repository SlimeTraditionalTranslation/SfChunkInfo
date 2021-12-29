package me.fnfal113.sfchunkinfo.commands;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.WorldUtils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

public class ScanChunk implements TabExecutor {

    private final Map<String, Integer> AMOUNT = new HashMap<>();
    private final Map<String, String> INFO = new HashMap<>();
    private final Map<String, Double> TIMINGS = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player){
            Player player = (Player) sender;
            if(args.length == 0) {
                if (player.hasPermission("sfchunkinfo.scan")) {
                    Chunk chunk = player.getLocation().getChunk();

                    getAmount(chunk, player);

                } else {
                    player.sendMessage("你沒有權限使用這個指令 (權限節點: sfchunkinfo.scan)");
                }
            } else {
                if (player.hasPermission("sfchunkinfo.scan.others")) {
                    Player target = Bukkit.getPlayer(args[0]);

                    if(target == null){
                        player.sendMessage("玩家不能為空或不在線上");
                        return true;
                    }

                    Chunk chunk = target.getLocation().getChunk();

                    getAmountOthers(chunk, target, player);

                } else {
                    player.sendMessage("你沒有權限使用這個指令 (權限節點: sfchunkinfo.scan.others)");
                }
            }
        }

        return true;
    }

    public void getAmount(Chunk chunk, Player player){
        if (!Slimefun.getProtectionManager().hasPermission(
                Bukkit.getOfflinePlayer(player.getUniqueId()),
                player.getLocation(),
                Interaction.PLACE_BLOCK)
        ) {
            player.sendMessage("你沒有權限掃描此區塊 (保護區插件), 請求權限或使用保護區插件指令來覆蓋");
            return;
        }

        for(int y = WorldUtils.getMinHeight(chunk.getWorld()); y <= chunk.getWorld().getMaxHeight(); y++) {
            for(int x = 0; x <= 15; x++) {
                for(int z = 0; z <= 15; z++) {
                    Block itemStack = chunk.getBlock(x, y, z);

                    if(BlockStorage.check(itemStack) != null) {
                        TIMINGS.put(Objects.requireNonNull(BlockStorage.check(itemStack)).getItemName(), TIMINGS.getOrDefault(Objects.requireNonNull(BlockStorage.check(itemStack)).getItemName(), (double) 0)
                                + Double.parseDouble(Slimefun.getProfiler().getTime(itemStack).substring(0, Slimefun.getProfiler().getTime(itemStack).length() - 2)));
                        INFO.put(Objects.requireNonNull(BlockStorage.check(itemStack)).getItemName(), Objects.requireNonNull(BlockStorage.check(itemStack)).getAddon().getName());
                        AMOUNT.put(Objects.requireNonNull(BlockStorage.check(itemStack)).getItemName(),  AMOUNT.getOrDefault(Objects.requireNonNull(BlockStorage.check(itemStack)).getItemName(), 0) + 1);
                    }
                }
            }
        }

        player.sendMessage(ChatColor.GOLD + "# 這個區塊上的黏液科技物品資訊:", "");

        if (AMOUNT.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "沒有黏液科技物品在這個區塊");
            return;
        }

        AMOUNT.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(e -> player.sendMessage(e.getKey() + ": " + ChatColor.GREEN + e.getValue()));

        player.spigot().sendMessage(hoverInfo(INFO));
        player.spigot().sendMessage(hoverInfoTimings(TIMINGS));

        AMOUNT.clear();
        INFO.clear();
        TIMINGS.clear();

    }

    public void getAmountOthers(Chunk chunk, Player player, Player sender){
        for(int y = WorldUtils.getMinHeight(chunk.getWorld()); y <= chunk.getWorld().getMaxHeight(); y++) {
            for(int x = 0; x <= 15; x++) {
                for(int z = 0; z <= 15; z++) {
                    Block itemStack = chunk.getBlock(x, y, z);

                    if(BlockStorage.check(itemStack) != null) {
                        TIMINGS.put(Objects.requireNonNull(BlockStorage.check(itemStack)).getItemName(), TIMINGS.getOrDefault(Objects.requireNonNull(BlockStorage.check(itemStack)).getItemName(), (double) 0)
                                + Double.parseDouble(Slimefun.getProfiler().getTime(itemStack).substring(0, Slimefun.getProfiler().getTime(itemStack).length() - 2)));
                        INFO.put(Objects.requireNonNull(BlockStorage.check(itemStack)).getItemName(), Objects.requireNonNull(BlockStorage.check(itemStack)).getAddon().getName());
                        AMOUNT.put(Objects.requireNonNull(BlockStorage.check(itemStack)).getItemName(),  AMOUNT.getOrDefault(Objects.requireNonNull(BlockStorage.check(itemStack)).getItemName(), 0) + 1);
                    }
                }
            }
        }

        sender.sendMessage(ChatColor.GOLD + "# 在 " + ChatColor.WHITE + player.getName() + ChatColor.GOLD + " 區塊上的黏液科技物品資訊:", "");

        if (AMOUNT.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "沒有黏液科技物品在 " + ChatColor.WHITE + player.getName() + ChatColor.GOLD + " 的區塊");
            return;
        }

        AMOUNT.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(e -> sender.sendMessage(e.getKey() + ": " + ChatColor.GREEN + e.getValue()));

        sender.spigot().sendMessage(hoverInfo(INFO));
        sender.spigot().sendMessage(hoverInfoTimings(TIMINGS));

        AMOUNT.clear();
        INFO.clear();
        TIMINGS.clear();

    }

    public TextComponent hoverInfo(Map<String, String> info){
        TextComponent infoAddon = new TextComponent( "\n懸停來獲取一些訊息" );
        infoAddon.setColor(net.md_5.bungee.api.ChatColor.WHITE);
        infoAddon.setItalic(true);
        infoAddon.setHoverEvent(new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text(info.toString().replace("{","").replace("}","").replace(", ", "\n").replace("=", ChatColor.WHITE + " | 來自: "))));

        return infoAddon;
    }

    public TextComponent hoverInfoTimings(Map<String, Double> timings){
        TextComponent infoChunk = new TextComponent( "懸停來獲取方塊的總 timings" );
        infoChunk.setColor(net.md_5.bungee.api.ChatColor.WHITE);
        infoChunk.setItalic(true);
        infoChunk.setHoverEvent(new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GOLD + "總 Timings" + "\n\n" + timings.toString().replace("{","").replace("}","").replace(", ", " ms\n").replace("=", ChatColor.WHITE + ": ").concat(ChatColor.WHITE + " ms"))));

        return infoChunk;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if(args.length == 1){
            List<String> playerNames = new ArrayList<>();
            Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
            Bukkit.getServer().getOnlinePlayers().toArray(players);
            for (Player player : players) {
                playerNames.add(player.getName());
            }

            return playerNames;
        }


        return null;
    }
}
