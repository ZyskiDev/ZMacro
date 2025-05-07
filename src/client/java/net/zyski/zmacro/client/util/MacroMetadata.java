package net.zyski.zmacro.client.util;

public class MacroMetadata {

    String version;
    String name;
    String author;
    String description;
    String icon;

    public MacroMetadata(String name, String version, String author, String description, String icon){
        this.version = version;
        this.name = name;
        this.author = author;
        this.description = description;
        this.icon = icon;
    }


    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getIcon() {
        return icon;
    }
}
