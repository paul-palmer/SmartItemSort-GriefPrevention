package dev.metanoia.smartitemsort.griefprevention;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;


class Config {

    private final SmartItemSortGriefPrevention plugin;
    private final Level logLevel;
    private final boolean allowFromUnclaimedLand;
    private final boolean ignoreHeight;
    private final boolean ignoreSubClaims;


    public Config(final SmartItemSortGriefPrevention plugin) {
        this.plugin = plugin;

        plugin.reloadConfig();

        FileConfiguration pluginConfig = plugin.getConfig();
        pluginConfig.addDefault("allowFromUnclaimedLand", true);
        pluginConfig.addDefault("ignoreHeight", false);
        pluginConfig.addDefault("ignoreSubClaims", true);
        pluginConfig.addDefault("logLevel", "CONFIG");

        pluginConfig.options().copyDefaults(true);
        plugin.saveDefaultConfig();

        this.allowFromUnclaimedLand = pluginConfig.getBoolean("allowFromUnclaimedLand");
        this.ignoreHeight = pluginConfig.getBoolean("ignoreHeight");
        this.ignoreSubClaims = pluginConfig.getBoolean("ignoreSubClaims");

        this.logLevel = parseLogLevel(pluginConfig.getString("logLevel"));
        this.plugin.setLevel(this.logLevel);

        if (!this.logLevel.equals(Level.CONFIG)) {
            config(() -> String.format("Logging level set to %s.", this.logLevel));
        }

        createDefaultConfig();
    }



    ///
    /// Getters
    ///

    public boolean getAllowFromUnclaimedLand() {
        return this.allowFromUnclaimedLand;
    }
    public boolean getIgnoreHeight() {
        return this.ignoreHeight;
    }
    public boolean getIgnoreSubClaims() {
        return this.ignoreSubClaims;
    }



    ///
    /// Private
    ///

    // create/overwrite an example config file that also documents the default values for each configuration
    // item.
    private void createDefaultConfig() {
        // create config.yml in the plugin's config directory if it does not already exist
        this.plugin.saveDefaultConfig();

        // save the default config.yml as config.default.yml (always) as documentation of the available settings
        try {
            InputStream defaultConfig = this.plugin.getResource("config.yml");
            if (defaultConfig == null) {
                error(() -> "Could not find internal config.yml resource.");
                return;
            }

            // the plugin's data folder should have been created above when plugin.saveDefaultConfig()
            // was performed.
            File dataFolder = this.plugin.getDataFolder();
            File configFile = new File(dataFolder, "config.example.yml");
            OutputStream defaultConfigFile = new FileOutputStream(configFile);
            defaultConfig.transferTo(defaultConfigFile);
        } catch (IOException ex) {
            error(() -> "Could not create example config file.");
        }
    }


    // map logging level label from configuration into the corresponding enum.
    private Level parseLogLevel(final String levelName) {
        if (levelName == null) {
            return Level.CONFIG;
        }

        final String upperLevelName = levelName.toUpperCase(Locale.ROOT);
        if (upperLevelName.equals("DEBUG")) {
            return Level.FINE;
        } else if (upperLevelName.equals("TRACE")) {
            return Level.FINER;
        }

        return Level.parse(upperLevelName);
    }


    private void config(Supplier<String> message) { this.plugin.config(message); }
    private void error(Supplier<String> message) { this.plugin.error(message); }

}
