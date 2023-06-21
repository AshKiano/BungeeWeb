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

public class BungeeWeb extends Plugin {
    private Server server;

    @Override
    public void onEnable() {
        // Create plugin config folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getLogger().info("Created config folder: " + getDataFolder().mkdir());
        }

        // Create 'www' folder if it doesn't exist
        File wwwDir = new File("www");
        if (!wwwDir.exists()) {
            getLogger().info("Created 'www' folder: " + wwwDir.mkdir());

            // Create 'index.html' inside 'www' folder with "HelloWorld!" content
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

        // Copy default config if it doesn't exist
        if (!configFile.exists()) {
            FileOutputStream outputStream; // Throws IOException
            try {
                outputStream = new FileOutputStream(configFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            InputStream in = getResourceAsStream("config.yml"); // This file must exist in the jar resources folder
            try {
                in.transferTo(outputStream); // Throws IOException
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Configuration configuration;
        try {
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
            server.start();
            getLogger().info("Server started on port " + configuration.getInt("port"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            if (server != null) {
                server.stop();
                getLogger().info("Server stopped");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}