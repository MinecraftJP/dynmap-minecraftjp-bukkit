# DynmapMinecraftJP for Bukkit

A Bukkit plugin that allows you to login with minecraft.jp account to dynmap.

## Requirement

- CraftBukkit/Spigot 1.7.10+
- dynmap 1.9+

## Install

1. Download latest zip archive from https://github.com/MinecraftJP/dynmap-minecraftjp-bukkit/releases/latest
2. Extract zip and copy to "<BukkitDir>/plugins"
3. copy "login_minecraftjp1.html"(dynmap login and minecraft.jp) or "login_minecraftjp2.html"(minecraft.jp only) to "<BukkitDir>/plugins/dynmap/web/login.html"

## Configure

You need to create a App in minecraft.jp App Console. https://minecraft.jp/en/developer/apps

Application Type: Service Account
Redirect Uri: http://<dynmap ip or hostname>:8123/up/minecraftjp/login

## License

MIT License
