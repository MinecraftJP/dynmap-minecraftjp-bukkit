package jp.minecraft.dynmap.minecraftjp;

import com.google.gson.Gson;
import jp.commun.minecraft.util.command.CommandManager;
import jp.commun.minecraft.util.command.CommandPermissionException;
import jp.minecraft.dynmap.minecraftjp.command.PluginCommand;
import jp.minecraft.dynmap.minecraftjp.servlet.MCJPLoginServlet;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCore;
import org.dynmap.bukkit.DynmapPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

/**
 * Created by ayu on 2016/02/15.
 */
public class DynmapMinecraftJPPlugin extends JavaPlugin {
    private final CommandManager commandManager = new CommandManager();
    private Plugin dynmapPlugin;
    @Getter
    private Gson gson = new Gson();
    @Getter
    private String certificate;

    @Override
    public void onEnable() {
        super.onEnable();

        commandManager.register(new PluginCommand(this));

        loadConfiguration();
        try {
            loadCertificate();
        } catch (IOException e) {
            getLogger().severe("load certificate failed.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        dynmapPlugin = getServer().getPluginManager().getPlugin("dynmap");
        if (dynmapPlugin == null) {
            getLogger().severe("dynmap is not installed.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        registerServlet();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            commandManager.execute(sender, command, args);
        } catch (CommandPermissionException e) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return false;
    }

    protected void registerServlet() {
        DynmapCore core = getDynmapCore();
        if (core == null) {
            getLogger().warning("oops...unable to access DynmapCore.");
            return;
        }

        core.addServlet("/up/minecraftjp/login", new MCJPLoginServlet(this));
    }

    private DynmapCore getDynmapCore() {
        if (dynmapPlugin == null) {
            return null;
        }

        try {
            Field coreField = DynmapPlugin.class.getDeclaredField("core");
            coreField.setAccessible(true);
            return (DynmapCore) coreField.get(dynmapPlugin);
        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private void loadConfiguration() {
        FileConfiguration config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();
    }

    public void loadCertificate() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/MinecraftJP.crt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        certificate = sb.toString();
    }
}
