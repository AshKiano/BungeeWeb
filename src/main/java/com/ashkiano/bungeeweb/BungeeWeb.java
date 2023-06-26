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
import java.net.URL;

//TODO udělat config s nastavením jazyka a pár překladových souborů předpřipravit do verze 1.4
//TODO udělat správně update configu a jeho oprabvu, když je něco špatně
public class BungeeWeb extends Plugin {
    private Server server;
    private Configuration configuration;

    @Override
    public void onEnable() {
        // Check and create the plugin configuration folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getLogger().info(getMsg("config-folder-created", "Created config folder: " + getDataFolder().mkdir()));
        }

        // Check and create 'www' directory if it doesn't exist
        File wwwDir = new File("www");
        if (!wwwDir.exists()) {
            getLogger().info(getMsg("www-folder-created", "Created 'www' folder: " + wwwDir.mkdir()));

            // Create 'index.html' file inside 'www' directory with "HelloWorld!" content
            try {
                FileWriter fileWriter = new FileWriter(new File(wwwDir, "index.html"));
                fileWriter.write(getMsg("index-content", "HelloWorld!"));
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
            try {
                URL whatismyip = new URL("https://checkip.amazonaws.com");
                BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
                String ip = in.readLine();
                getLogger().info("Server started on: " + ip + ":" + configuration.getInt("port"));
            } catch (Exception e) {
                getLogger().info(getMsg("server-started", "Server started on port " + configuration.getInt("port")));
                getLogger().info(getMsg("public-ip-failed", "Failed to retrieve public IP address."));
                e.printStackTrace();
            }
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
                getLogger().info(getMsg("server-stopped", "Server stopped"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Returns the message from the config if exists, otherwise returns the default message
    private String getMsg(String key, String defaultMsg) {
        if (configuration != null && configuration.contains(key)) {
            return configuration.getString(key);
        } else {
            return defaultMsg;
        }
    }
}
