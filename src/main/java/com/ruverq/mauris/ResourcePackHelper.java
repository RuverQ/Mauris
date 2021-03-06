package com.ruverq.mauris;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.Getter;
import lombok.SneakyThrows;
import net.lingala.zip4j.ZipFile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Executors;

import static com.ruverq.mauris.commands.CommandManager.format;

public class ResourcePackHelper implements Listener {

    static List<Integer> ports;

    static String ip = "localhost";
    static String randomUUID;
    boolean isHosted;
    boolean isZiped;

    static String overrideUrl;
    static boolean kickEnabled;
    static String kickMessage = "bruh";
    static String kickMessageFailure = "bruh";
    static String bypassKickPermission = "";

    public void sendTo(Player player){
        String url = "http://" + ip + ":" + ports.get(new Random().nextInt(ports.size())) + "/rp.zip#" + randomUUID;
        if(overrideUrl != null) url = overrideUrl + "#" + randomUUID;

        player.setResourcePack(url);
    }

    @SneakyThrows
    public void zipResourcePack(){
        File file = DataHelper.getDir("resource_pack");
        File rpzip = new File(file.getAbsolutePath() + File.separator + "rp.zip");
        rpzip.delete();

        generateMCMETA();

        ZipFile zipFile = new ZipFile(file.getAbsolutePath() + File.separator + "rp.zip");
        zipFile.addFile(file.getAbsoluteFile() + File.separator + "pack.png");
        zipFile.addFile(file.getAbsoluteFile() + File.separator + "pack.mcmeta");
        zipFile.addFolder(DataHelper.getDir("resource_pack/assets"));

        isZiped = true;
    }

    public void generateMCMETA(){
        DataHelper.deleteFile("resource_pack/pack.mcmeta");

        JsonObject jsonObject = new JsonObject();
        JsonObject packObject = new JsonObject();
        packObject.addProperty("description", new Date(System.currentTimeMillis()).toString());
        packObject.addProperty("pack_format", 7);

        jsonObject.add("pack", packObject);

        DataHelper.createFile("resource_pack/pack.mcmeta", jsonObject.toString());
    }

    @Getter
    static ResourcePackHelper current;

    HashMap<Integer, HttpServer> httpServerHashMap = new HashMap<>();

    @SneakyThrows
    public void hostResourcePack(int port){
        File file = DataHelper.getDir("resource_pack");

        HttpServer server = httpServerHashMap.get(port);

        if(server != null){
            server.stop(0);
            httpServerHashMap.remove(port);
        }

        randomUUID = UUID.randomUUID().toString();

        String url = "http://" + ip + ":" + port + "/rp.zip#" + randomUUID;
        if(overrideUrl != null) url = overrideUrl + "#" + randomUUID;

        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/rp.zip", new HHandler(file.getPath() + File.separator + "rp.zip"));
        server.setExecutor(Executors.newCachedThreadPool());

        server.start();

        httpServerHashMap.put(port, server);

        Bukkit.getLogger().info("Host URL " + url);

        isHosted = true;
    }

    static class HHandler implements HttpHandler {

        String path;
        public HHandler(String path) {
            this.path = path;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            Headers h = t.getResponseHeaders();

            File newFile = new File(path);
            byte[] data = Files.readAllBytes(newFile.toPath());

            h.add("Content-Type", "application/zip");
            t.sendResponseHeaders(200, data.length);
            OutputStream os = t.getResponseBody();
            os.write(data);
            os.close();

        }
    }


    public static void setupRP(){

        if(current != null) current.stopAll();

        ResourcePackHelper rph = new ResourcePackHelper();

        rph.zipResourcePack();

        ConfigurationSection config = Mauris.getInstance().getConfig();

        kickEnabled = config.getBoolean("kick-on-decline.enabled");
        if(kickEnabled){
            kickMessage = format(config.getString("kick-on-decline.message", "kicked"));
            kickMessageFailure = format(config.getString("kick-on-decline.failureMessage", "kicked"));
            bypassKickPermission = config.getString("kick-on-decline.bypassPermission");
        }

        boolean enabled = config.getBoolean("self-host.enabled", true);
        if(!enabled) return;

        ip = config.getString("self-host.ip", Bukkit.getServer().getIp());
        ports = config.getIntegerList("self-host.ports");
        overrideUrl = config.getString("self-host.overrideUrl", null);
        if(overrideUrl != null && overrideUrl.isEmpty()) overrideUrl = null;

        for(int port : ports){
            rph.hostResourcePack(port);
        }

        new BukkitRunnable() {
            @Override
            public void run() {

                for(Player player : Bukkit.getOnlinePlayers()){
                    rph.sendTo(player);
                }

            }
        }.runTaskLater(Mauris.getInstance(), 20);

        current = rph;

    }

    static List<Player> withResourcePack = new ArrayList<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        this.sendTo(e.getPlayer());
    }

    public static boolean withResourcePack(Player player){
        return withResourcePack.contains(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        withResourcePack.remove(e.getPlayer());
    }

    public static void stopAll(){
        getCurrent().stop();
    }

    public void stop(){
        httpServerHashMap.values().forEach((httpServer -> {httpServer.stop(0);}));
        httpServerHashMap.clear();
    }

    @EventHandler
    public void onStatus(PlayerResourcePackStatusEvent e){
        if(e.getStatus() == PlayerResourcePackStatusEvent.Status.ACCEPTED) {
            withResourcePack.add(e.getPlayer());
            return;
        }

        if(!kickEnabled) return;
        if((bypassKickPermission != null && !bypassKickPermission.isEmpty()) && e.getPlayer().hasPermission(bypassKickPermission)) return;

        if(e.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED) e.getPlayer().kickPlayer(kickMessage);
    }




}
