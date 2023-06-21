package com.ashkiano.bungeeweb;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.*;
//TODO udělat všechny hlášky nastavitelné v configu do verze 1.3
public class BungeeWeb extends Plugin {
    private Server server;

    @Override
    public void onEnable() {
        // Check and create the plugin configuration folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getLogger().info("Created config folder: " + getDataFolder().mkdir());
        }

        // Check and create 'www' directory if it doesn't exist
        File wwwDir = new File("www");
        if (!wwwDir.exists()) {
            getLogger().info("Created 'www' folder: " + wwwDir.mkdir());

            // Create 'index.html' file inside 'www' directory with "HelloWorld!" content
            try {
                FileWriter fileWriter = new FileWriter(new File(wwwDir, "index.html"));
                fileWriter.write("HelloWorld!");
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File configFile = new File(getDataFolder(), "config.yml");

        Metrics metrics = new Metrics(this, 18808);

        // Check and copy default config if it doesn't exist
        if (!configFile.exists()) {
            try {
                FileOutputStream outputStream = new FileOutputStream(configFile);
                InputStream in = getResourceAsStream("config.yml"); // This file should exist in the jar resources folder
                in.transferTo(outputStream); // Throws IOException
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Configuration configuration;
        try {
            // Load YAML configuration file
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        server = new Server(configuration.getInt("port"));

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase(wwwDir.getAbsolutePath());

        ServletHolder holder = context.addServlet(DefaultServlet.class, "/");
        holder.setInitParameter("dirAllowed", "true");
        server.setHandler(context);

        try {
            // Start the server
            server.start();
            getLogger().info("Server started on port " + configuration.getInt("port"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            // Stop the server if it is not null
            if (server != null) {
                server.stop();
                getLogger().info("Server stopped");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}