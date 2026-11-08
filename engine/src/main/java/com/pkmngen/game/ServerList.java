package com.pkmngen.game;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.pkmngen.game.util.Json;

/** Local server bookmarks, independent of world saves. */
final class ServerList {
    static final class Entry {
        final String name, host;
        final int port;
        Entry(String name, String endpoint) {
            int colon = endpoint.lastIndexOf(':');
            if (colon <= 0) throw new IllegalArgumentException("Use IP:Port or hostname:port");
            host = endpoint.substring(0, colon).trim();
            int parsed;
            try { parsed = Integer.parseInt(endpoint.substring(colon + 1)); }
            catch (NumberFormatException invalid) { throw new IllegalArgumentException("Invalid port"); }
            port = parsed;
            if (host.isEmpty() || host.length() > 253 || host.contains("/") || host.matches(".*\\s.*") || port < 1 || port > 65535)
                throw new IllegalArgumentException("Invalid server address");
            this.name = name.trim();
            if (this.name.isEmpty() || this.name.length() > 32) throw new IllegalArgumentException("Name must be 1-32 characters");
        }
        String endpoint() { return host + ":" + port; }
    }
    final List<Entry> entries = new ArrayList<>();
    private final Path path = Paths.get("servers.json");
    void load() throws IOException {
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                Entry[] saved = Json.gson.fromJson(reader, Entry[].class);
                List<Entry> validated = new ArrayList<>();
                if (saved == null) throw new IOException("Empty server list");
                for (Entry entry : saved) validated.add(new Entry(entry.name, entry.host + ":" + entry.port));
                entries.clear(); entries.addAll(validated);
                return;
            } catch (RuntimeException invalid) { throw new IOException("Invalid saved server list", invalid); }
        }
        Path legacy = Paths.get("servers.properties");
        if (!Files.exists(legacy)) return;
        Properties data = new Properties();
        try (Reader reader = Files.newBufferedReader(legacy)) { data.load(reader); }
        entries.clear();
        for (int i = 0; data.containsKey(i + ".name"); i++) {
            try { entries.add(new Entry(data.getProperty(i + ".name"), data.getProperty(i + ".address", ""))); }
            catch (IllegalArgumentException invalid) { throw new IOException("Invalid saved server " + (i + 1), invalid); }
        }
    }
    void save() throws IOException {
        Path temp = Files.createTempFile(path.toAbsolutePath().getParent(), "servers-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temp)) { Json.gson.toJson(entries, writer); }
            Files.move(temp, path.toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temp); }
    }
}
