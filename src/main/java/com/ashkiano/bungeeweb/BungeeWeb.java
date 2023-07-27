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

//TODO zmenit port a jazyk prikazem
//TODO udělat správně update configu a jeho oprabvu, když je něco špatně
//TODO upravit tak, aby se tady nemusel definovat seznam jazyků
public class BungeeWeb extends Plugin {
    private Server server;
    private Configuration configuration;

    // Array of supported languages
    private String[] languages = {"en", "cs", "sk", "de", "zh_cn", "pl", "es", "ru"};

    @Override
    public void onEnable() {

        // Check and create the plugin configuration folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getLogger().info("Created config folder: " + getDataFolder().mkdir());
        }

        File configFile = new File(getDataFolder(), "config.yml");

        // Check and copy default config if it doesn't exist
        if (!configFile.exists()) {
            try {
                FileOutputStream outputStream = new FileOutputStream(configFile);
                InputStream in = getResourceAsStream("config.yml"); // This file should exist in the jar resources folder
                in.transferTo(outputStream); // Throws IOException

                // Check and create default language files if they don't exist
                for (String lang : languages) {
                    File langFile = new File(getDataFolder(), "messages_" + lang + ".yml");

                    if (!langFile.exists()) {
                        try {
                            FileOutputStream outputStreamLang = new FileOutputStream(langFile);
                            InputStream inLang = getResourceAsStream("messages_" + lang + ".yml"); // These files should exist in the jar resources folder
                            if (inLang == null) {
                                getLogger().info("Language file messages_" + lang + ".yml not found in resources");
                                continue;
                            }
                            inLang.transferTo(outputStreamLang); // Throws IOException
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
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

        Metrics metrics = new Metrics(this, 18808);

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

        System.out.println("Thank you for using the BungeeWeb plugin! If you enjoy using this plugin, please consider making a donation to support the development. You can donate at: https://paypal.me/josefvyskocil");
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

    // Method to get the message corresponding to a particular key from the language configuration file.
    // If the key is not present in the language configuration file, it returns a default message.
    private String getMsg(String key, String defaultMsg) {
        // Fetch the language setting from the main configuration file
        String lang = configuration.getString("language");
        Configuration langConfig;

        try {
            // Attempt to load the configuration file for the selected language
            langConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "messages_" + lang + ".yml"));
        } catch (IOException e) {
            // If the language file fails to load, attempt to load the default language file
            try {
                langConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "messages_en.yml"));
            } catch (IOException ex) {
                // If the default language file also fails to load, throw an exception
                throw new RuntimeException(ex);
            }
        }

        // Check if the key exists in the language configuration file
        if (langConfig != null && langConfig.contains(key)) {
            // If the key exists, return the corresponding message
            return langConfig.getString(key);
        } else {
            // If the key does not exist, return the default message
            return defaultMsg;
        }
    }
}
