package dev.metanoia.smartitemsort.griefprevention;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.logging.Level;

// ColorLogger is a simple colorized-logger for console messages

public class ColorLogger {
    private static final Map<Level, LogLevel> levelMap = new TreeMap<>() {{
        put(Level.SEVERE, LogLevel.ERROR);
        put(Level.WARNING, LogLevel.WARN);
        put(Level.INFO, LogLevel.INFO);
        put(Level.CONFIG, LogLevel.CONFIG);
        put(Level.FINE, LogLevel.DEBUG);
        put(Level.FINER, LogLevel.TRACE);
        put(Level.FINEST, LogLevel.TRACE);
    }};

    private final String pluginName;
    private final ConsoleCommandSender sender;
    private LogLevel minLevel;



    public ColorLogger(final JavaPlugin plugin) {
        this.pluginName = plugin.getName();
        this.sender = plugin.getServer().getConsoleSender();
        this.minLevel = LogLevel.INFO;
    }

    public void setLevel(Level newMinLevel) {
        this.minLevel = ColorLogger.levelMap.getOrDefault(newMinLevel, LogLevel.INFO);
    }

    public void config(final Supplier<String> message)  { log(LogLevel.CONFIG, message); }
    public void debug(final Supplier<String> message)   { log(LogLevel.DEBUG, message); }
    public void error(final Supplier<String> message)   { log(LogLevel.ERROR, message); }
    public void info(final Supplier<String> message)    { log(LogLevel.INFO, message); }
    public void trace(final Supplier<String> message)    { log(LogLevel.TRACE, message); }


    private void log(LogLevel config, final Supplier<String> message) {
        if (config.getSeverity() < this.minLevel.getSeverity()) {
            return;
        }

        String colorizedMessage = String.format("§r[" + this.pluginName + "%s]:§r %s%s§r", config.getLabel(), config.getColor(), message.get());
        this.sender.sendMessage(colorizedMessage);
    }



    static final class LogLevel {
        private final int severity;
        private final String label;
        private final String color;

        public LogLevel(int severity, String label, String color) {
            this.severity = severity;
            this.label = label;
            this.color = color;
        }

        public String getColor() { return this.color; }
        public String getLabel() { return this.label; }
        public int getSeverity() { return this.severity; }


        public static final LogLevel CONFIG   = new LogLevel(-1,   "/CONFIG",  "§9");
        public static final LogLevel DEBUG    = new LogLevel(-2,   "/DEBUG",   "§2");
        public static final LogLevel ERROR    = new LogLevel(2,    "/ERROR",   "§4§l");
        public static final LogLevel INFO     = new LogLevel(0,    "",         "");
        public static final LogLevel TRACE    = new LogLevel(-3,   "/TRACE",   "§2");
        public static final LogLevel WARN     = new LogLevel(1,    "/WARN",    "§6§l");
    }
}
