package net.zyski.zmacro.client.util;

import net.zyski.zmacro.client.Macro.ZMacro;

public class MacroWrapper {

    private final ZMacro macro;
    private final String version;
    private final String name;
    private final String author;
    private final String description;
    private final String icon;

    public MacroWrapper(ZMacro macro, String name, String version,
                        String author, String description, String icon) {
        this.macro = macro;
        this.version = version;
        this.name = name;
        this.author = author;
        this.description = description;
        this.icon = icon;
    }

    // Getters
    public ZMacro getMacro() { return macro; }
    public String getVersion() { return version; }
    public String getName() { return name; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }

}
