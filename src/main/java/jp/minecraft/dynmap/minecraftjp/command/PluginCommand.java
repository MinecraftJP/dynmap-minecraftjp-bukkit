package jp.minecraft.dynmap.minecraftjp.command;

import jp.commun.minecraft.util.command.Command;
import jp.commun.minecraft.util.command.CommandHandler;
import jp.minecraft.dynmap.minecraftjp.DynmapMinecraftJPPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Created by ayu on 2016/02/15.
 */
@RequiredArgsConstructor
public class PluginCommand implements CommandHandler {
    private final DynmapMinecraftJPPlugin plugin;

    @Command( names = { "dynmapmcjp reload" }, permissions = { "dynmapmcjp.reload" })
    public void reload(CommandSender sender, String commandName, String[] args) {
        plugin.reloadConfig();

        sender.sendMessage(ChatColor.GREEN + "[DynmapMinecraftJP] reloaded.");
    }
}
