package com.pkmngen.game;

/** Explicit opt-in LAN configuration; ordinary single-player args are unchanged. */
public final class LanOptions {
    public String mode;
    public String address = "127.0.0.1";
    public int port = 54555;
    public String name = "player";
    public String world;
    public boolean createWorld;
    public int size = 30000;

    public static LanOptions forJoin(String endpoint, String name) {
        if (endpoint == null || endpoint.trim().isEmpty()) endpoint = "127.0.0.1";
        endpoint = endpoint.trim();
        if (endpoint.indexOf(':') < 0) endpoint += ":54555";
        return parse(new String[]{"--coop-join=" + endpoint, "--coop-name=" + name});
    }

    public static LanOptions parse(String[] args) {
        LanOptions out = new LanOptions();
        for (String arg : args) {
            if (!arg.startsWith("--coop-")) continue;
            int equals = arg.indexOf('=');
            if (equals < 0) throw new IllegalArgumentException("LAN option needs =value: " + arg);
            String key = arg.substring(0, equals);
            String value = arg.substring(equals + 1);
            switch (key) {
                case "--coop-host": case "--coop-join":
                    if (out.mode != null) throw new IllegalArgumentException("Choose only host or join");
                    out.mode = key.equals("--coop-host") ? "host" : "join";
                    int colon = value.lastIndexOf(':');
                    if (colon <= 0) throw new IllegalArgumentException("Use ADDRESS:PORT for LAN endpoint");
                    out.address = value.substring(0, colon);
                    out.port = Integer.parseInt(value.substring(colon + 1));
                    if (out.port < 1 || out.port > 65535 || out.address.length() > 253 || out.address.contains("/"))
                        throw new IllegalArgumentException("Invalid LAN endpoint");
                    break;
                case "--coop-name":
                    if (!value.matches("[A-Za-z0-9_-]{1,24}")) throw new IllegalArgumentException("LAN name: 1-24 letters, digits, _ or -");
                    out.name = value;
                    break;
                case "--coop-new": case "--coop-world":
                    if (out.world != null || !value.matches("[A-Za-z0-9_-]{1,32}")) throw new IllegalArgumentException("Choose one safe LAN world name");
                    out.world = value;
                    out.createWorld = key.equals("--coop-new");
                    break;
                case "--coop-size":
                    out.size = Integer.parseInt(value);
                    if (out.size < 30000 || out.size > 100000) throw new IllegalArgumentException("LAN generation size must be 30000..100000");
                    break;
                default: throw new IllegalArgumentException("Unknown LAN option: " + key);
            }
        }
        if (out.world != null && !"host".equals(out.mode)) throw new IllegalArgumentException("World selection is host-only");
        return out;
    }
}
