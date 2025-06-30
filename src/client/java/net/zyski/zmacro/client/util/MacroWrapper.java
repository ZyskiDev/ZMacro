package net.zyski.zmacro.client.util;


import java.nio.file.Path;

public class MacroWrapper {

    private final String version;
    private final String name;
    private final String author;
    private final String description;
    private final String icon;
    private  Path path;

    public MacroWrapper(Path path, String name, String version,
                        String author, String description, String icon) {
        this.path = path;
        this.version = version;
        this.name = name;
        this.author = author;
        this.description = description;
        this.icon = icon;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public Path getPath() {
        return path;
    }
}
